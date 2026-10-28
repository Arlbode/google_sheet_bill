package cu.hogarplaya;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONObject;

/** La APK consulta el dato público. Otra cola sincroniza la referencia con el Sheet. */
public final class TasaloReference {
    public static final String ENDPOINT="http://tasalo.duckdns.org:8040/api/v1/tasas/latest?max_age_minutes=30";
    public static final ExecutorService NETWORK=Executors.newSingleThreadExecutor();
    private static final AtomicBoolean FETCHING=new AtomicBoolean(false),SENDING=new AtomicBoolean(false);
    private static final int JOB=5403;
    private static final String CACHE="tasaloReference";
    private static JSONObject cached(AppStore store)throws Exception {return new JSONObject(store.cache(CACHE,"{}"));}
    private static boolean enabled(AppStore store)throws Exception {
        JSONObject planning=new JSONObject(store.cache("snapshot","{}")).optJSONObject("planning");
        JSONObject quote=planning==null?null:planning.optJSONObject("quote");return quote==null||quote.optBoolean("autoEnabled",true);
    }
    public static JSONObject state(Context context)throws Exception {
        JSONObject value=cached(AppStore.get(context));value.put("fetching",FETCHING.get()).put("sending",SENDING.get());return value;
    }
    /** Exige fecha propia de elTOQUE; updated_at de la respuesta no prueba frescura. */
    static JSONObject parse(JSONObject response,long now)throws Exception {
        if(!response.optBoolean("ok"))throw new IllegalArgumentException("TASALO no devolvió una respuesta válida.");
        JSONObject usd=response.getJSONObject("data").getJSONObject("eltoque").getJSONObject("USD");
        Object raw=usd.get("rate");if(!(raw instanceof Number))throw new IllegalArgumentException("TASALO no devolvió un importe numérico USD/CUP.");
        double rate=((Number)raw).doubleValue();
        if(!Double.isFinite(rate)||rate<=0||rate>1000000)throw new IllegalArgumentException("La referencia USD/CUP de TASALO no es válida.");
        String source=response.getJSONObject("sources_updated_at").getString("eltoque");long stamp=Instant.parse(source).toEpochMilli();
        if(now-stamp>1800000||stamp-now>120000)throw new IllegalArgumentException("TASALO devolvió una referencia antigua o una fecha futura. Se conserva la anterior.");
        String day=Instant.ofEpochMilli(stamp).atZone(ZoneId.of("America/Havana")).toLocalDate().toString();
        String current=Instant.ofEpochMilli(now).atZone(ZoneId.of("America/Havana")).toLocalDate().toString();
        if(!day.equals(current))throw new IllegalArgumentException("TASALO no devolvió una referencia del día de La Habana.");
        return new JSONObject().put("id",UUID.randomUUID().toString()).put("rate",rate).put("sourceAt",Instant.ofEpochMilli(stamp).toString())
            .put("fetchedAt",Instant.ofEpochMilli(now).toString()).put("day",day).put("provider","TASALO").put("source","eltoque").put("url",ENDPOINT);
    }
    private static JSONObject get()throws Exception {
        HttpURLConnection connection=(HttpURLConnection)new URL(ENDPOINT).openConnection();
        connection.setConnectTimeout(7000);connection.setReadTimeout(7000);connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("Cache-Control","no-cache");
        try {
            int status=connection.getResponseCode();if(status!=200)throw new IllegalArgumentException("TASALO: HTTP "+status+". Se conserva la última referencia.");
            try(InputStream in=connection.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[8192];int count;
                while((count=in.read(buffer))!=-1){if(out.size()+count>262144)throw new IllegalArgumentException("La respuesta de TASALO es demasiado grande.");out.write(buffer,0,count);}
                return new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
            }
        }finally{connection.disconnect();}
    }
    static void save(AppStore store,JSONObject quote,String actor)throws Exception {
        synchronized(store) {
            JSONObject value=cached(store),prior=value.optJSONObject("latest");
            if(prior!=null) {
                long old=Instant.parse(prior.getString("sourceAt")).toEpochMilli(),next=Instant.parse(quote.getString("sourceAt")).toEpochMilli();
                if(next<old)throw new IllegalArgumentException("TASALO devolvió un dato anterior al guardado.");
                if(next==old) {
                    if(quote.getDouble("rate")!=prior.getDouble("rate"))throw new IllegalArgumentException("TASALO cambió el valor sin cambiar la fecha de origen.");
                    if(actor.equals(prior.optString("actorId"))&&prior.optBoolean("active",true)){value.remove("error");value.remove("errorAt");store.cache(CACHE,value);return;}
                }
            }
            quote.put("actorId",actor).put("active",true);
            value.put("latest",quote).put("pending",true);value.remove("error");value.remove("errorAt");value.remove("syncError");
            store.cache(CACHE,value);
        }
    }
    public static void fetch(Context context,String actor,Runnable done) {
        if(!FETCHING.compareAndSet(false,true)){done.run();return;}
        NETWORK.execute(()->{
            AppStore store=AppStore.get(context);
            try {
                if(!enabled(store))throw new IllegalArgumentException("La actualización está desactivada en Ajustes B26.");
                if(!SyncEngine.online(context))throw new IllegalArgumentException("Sin conexión. Se conserva la última referencia.");
                long now=System.currentTimeMillis(),last=store.prefs.getLong("tasaloLastAttempt",0);
                if(now-last<60000)throw new IllegalArgumentException("Espera un minuto entre consultas a TASALO.");
                store.prefs.edit().putLong("tasaloLastAttempt",now).commit();
                save(store,parse(get(),System.currentTimeMillis()),actor);
            }catch(Exception e){
                try{synchronized(store){JSONObject value=cached(store);value.put("error","No se pudo actualizar desde TASALO: "+(e.getMessage()==null?"sin respuesta":e.getMessage())).put("errorAt",Instant.now().toString());store.cache(CACHE,value);}}catch(Exception ignored){}
            }finally{FETCHING.set(false);done.run();kick(context);SyncEngine.changed();}
        });
    }
    public static void kick(Context context) {
        Context app=context.getApplicationContext();
        try {
            AppStore store=AppStore.get(app);
            if(!cached(store).optBoolean("pending")||store.prefs.getString("endpoint","").isEmpty())return;
            JobScheduler scheduler=(JobScheduler)app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if(scheduler!=null&&scheduler.getPendingJob(JOB)==null)scheduler.schedule(new JobInfo.Builder(JOB,new ComponentName(app,TasaloSyncService.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setBackoffCriteria(60000,JobInfo.BACKOFF_POLICY_EXPONENTIAL).setPersisted(true).build());
            if(SyncEngine.online(app)&&System.currentTimeMillis()-store.prefs.getLong("tasaloLastSync",0)>=60000&&SENDING.compareAndSet(false,true))NETWORK.execute(()->{try{sync(app);}finally{SENDING.set(false);SyncEngine.changed();}});
        }catch(Exception ignored){}
    }
    static void acknowledge(AppStore store,JSONObject sent,JSONObject response)throws Exception {
        String household=store.prefs.getString("householdId","");
        if(!response.optBoolean("ok")) {
            JSONObject error=response.optJSONObject("error");
            if(error!=null&&error.optString("code").equals("ACCIÓN_INVALIDA"))throw new IllegalArgumentException("Actualiza el script completo del Sheet y publica una nueva versión para recibir la referencia consultada por la APK.");
            throw new IllegalArgumentException(error==null?"No se pudo sincronizar la tasa.":error.optString("message","Actualiza el script completo del Sheet."));
        }
        JSONObject ack=response.getJSONObject("referenceAck");String status=ack.getString("status");
        if(!household.equals(response.optString("householdId"))||!sent.getString("id").equals(ack.optString("id"))||!java.util.Arrays.asList("accepted","duplicate","unchanged","superseded","expired","disabled").contains(status))throw new IllegalArgumentException("La confirmación de la tasa no corresponde a esta hoja o referencia.");
        if(status.equals("duplicate"))status=ack.optString("originalStatus","accepted");
        if(!java.util.Arrays.asList("accepted","unchanged","superseded","expired","disabled").contains(status))throw new IllegalArgumentException("Estado de confirmación de tasa no válido.");
        synchronized(store) {
            JSONObject value=cached(store),latest=value.optJSONObject("latest");
            if(latest!=null&&latest.optString("id").equals(sent.getString("id"))) {
                value.put("pending",false).put("status",status).put("syncedAt",Instant.now().toString());
                if(status.equals("disabled")||status.equals("expired")||status.equals("superseded"))latest.put("active",false);
                value.remove("syncError");
                if(response.optJSONObject("quote")!=null)value.put("serverQuote",response.getJSONObject("quote"));
                store.cache(CACHE,value);
            }
        }
    }
    /** Cola independiente de movimientos: nunca crea ingresos/gastos por sincronizar una referencia. */
    public static boolean sync(Context context) {
        AppStore store=AppStore.get(context);
        try {
            JSONObject value=cached(store);if(!value.optBoolean("pending")||store.prefs.getString("endpoint","").isEmpty())return false;
            if(!SyncEngine.online(context))return true;
            if(System.currentTimeMillis()-store.prefs.getLong("tasaloLastSync",0)<30000)return true;
            store.prefs.edit().putLong("tasaloLastSync",System.currentTimeMillis()).commit();
            JSONObject quote=value.getJSONObject("latest");
            JSONObject request=new JSONObject().put("apiVersion",3).put("action","referenceSync").put("deviceId",store.deviceId)
                .put("householdId",store.prefs.getString("householdId","")).put("token",new Vault(context).token()).put("profiles",store.profiles()).put("reference",quote);
            JSONObject response=ApiClient.post(store.prefs.getString("endpoint",""),request);
            acknowledge(store,quote,response);SyncEngine.kick(context);return cached(store).optBoolean("pending");
        }catch(Exception e){try{synchronized(store){JSONObject value=cached(store);value.put("syncError",e.getMessage()==null?"No se pudo sincronizar. Se conserva en el teléfono.":e.getMessage());store.cache(CACHE,value);}}catch(Exception ignored){}return true;}
    }
}
