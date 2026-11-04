package cu.hogarplaya;

import android.app.Activity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.*;

/** SQLite y autorización en WORK; HTTP fuera de la cola de las acciones locales. */
public final class SharedSync {
    private static final ExecutorService NETWORK=Executors.newSingleThreadExecutor();
    private static final AtomicBoolean RUNNING=new AtomicBoolean();
    private SharedSync(){}
    public static void start(Activity a,AppStore s,String actor,String replyId)throws Exception {
        JSONObject v=HouseholdFeatures.get(s);JSONArray queue=v.getJSONArray("queue");HouseholdFeatures.bounded(queue,100,150000);
        String household=s.prefs.getString("householdId",""),endpoint=s.prefs.getString("endpoint",""),session=s.prefs.getString("authenticatedDay","");
        if(household.isEmpty())throw new IllegalArgumentException("Conecta primero este teléfono con tu Sheet.");
        if(!SyncEngine.online(a))throw new IllegalArgumentException("Sin conexión. La lista sigue guardada en este teléfono.");
        if(!RUNNING.compareAndSet(false,true)){HouseholdFeatures.reply(a,replyId,v,null);return;}
        try{
            JSONArray batch=new JSONArray();for(int i=0;i<Math.min(20,queue.length());i++)batch.put(queue.getJSONObject(i));
            JSONObject request=new JSONObject().put("apiVersion",3).put("action","featuresSync").put("deviceId",s.deviceId).put("householdId",household).put("token",new Vault(a).token()).put("profiles",s.profiles()).put("changes",batch);
            NETWORK.execute(()->{
                JSONObject received=null;Exception failure=null;
                try{received=ApiClient.post(endpoint,request);}catch(Exception e){failure=e;}
                final JSONObject response=received;final Exception error=failure;
                SyncEngine.WORK.execute(()->{
                    boolean bound=false;
                    try{
                        // Una respuesta antigua nunca escribe sobre otra conexión, restauración o sesión.
                        bound=household.equals(s.prefs.getString("householdId",""))&&endpoint.equals(s.prefs.getString("endpoint",""))&&s.deviceId.equals(request.optString("deviceId"))&&session.equals(s.prefs.getString("authenticatedDay",""))&&request.optString("token").equals(new Vault(a).token())&&actor.equals(HouseholdFeatures.actor(a));
                        if(!bound)throw new IllegalArgumentException("La sesión o la conexión cambió; se conserva la lista pendiente.");
                        if(error!=null)throw error;
                        JSONObject updated=SharedSyncBatch.apply(HouseholdFeatures.get(s),batch,response,household);
                        HouseholdFeatures.store(s,updated);HouseholdFeatures.reply(a,replyId,updated,null);SyncEngine.changed();
                    }catch(Exception e){
                        if(bound)try{JSONObject latest=HouseholdFeatures.get(s);latest.put("syncError",e.getMessage());HouseholdFeatures.store(s,latest);}catch(Exception ignored){}
                        HouseholdFeatures.reply(a,replyId,null,e);
                    }finally{RUNNING.set(false);}
                });
            });
        }catch(Exception e){RUNNING.set(false);throw e;}
    }
}
