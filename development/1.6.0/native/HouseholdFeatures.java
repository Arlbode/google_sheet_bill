package cu.hogarplaya;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebView;
import java.lang.reflect.*;
import java.time.Instant;
import java.util.UUID;
import org.json.*;

/** Añade comandos sin sustituir los despachadores financieros ni las credenciales. */
public final class HouseholdFeatures {
    public static final String KEY="features160";
    static String actor(Activity a)throws Exception {
        DailySession.prepare(a);
        Method m=a.getClass().getDeclaredMethod("requireProfile");m.setAccessible(true);m.invoke(a);
        Field f=a.getClass().getDeclaredField("active");f.setAccessible(true);return (String)f.get(a);
    }
    static JSONObject get(AppStore s)throws Exception {
        JSONObject v=new JSONObject(s.cache(KEY,"{}"));String household=s.prefs.getString("householdId","");
        if(!v.optString("householdId").equals(household)){
            String previous=v.optString("householdId");s.cache(KEY+":"+previous,v);
            String saved=s.cache(KEY+":"+household,"");
            v=!saved.isEmpty()?new JSONObject(saved):previous.isEmpty()?v:new JSONObject();
            v.put("householdId",household);s.cache(KEY,v);
        }
        v.put("householdId",household);
        for(String k:new String[]{"products","offers","shared","queue","conflicts"})if(!v.has(k))v.put(k,new JSONArray());
        v.put("lastBackup",s.prefs.getString("featuresLastBackup","")).put("backupVerified",s.prefs.getString("featuresBackupVerified",""));return v;
    }
    static void store(AppStore s,JSONObject v){s.cache(KEY,v);}
    static void reply(Activity a,String id,JSONObject value,Exception error){
        try{JSONObject r=new JSONObject().put("ok",error==null);if(error==null)r.put("data",value);else r.put("error",error.getMessage()==null?"No se pudo completar la acción.":error.getMessage());
        String script="window.hogarReply("+JSONObject.quote(id)+","+r+")";
        a.runOnUiThread(()->{try{Field f=a.getClass().getDeclaredField("web");f.setAccessible(true);WebView w=(WebView)f.get(a);if(w!=null)w.evaluateJavascript(script,null);}catch(Exception ignored){}});}catch(Exception ignored){}
    }
    static void bounded(JSONArray list,int limit,int bytes)throws Exception{if(list.length()>limit||list.toString().length()>bytes)throw new IllegalArgumentException("Hay demasiados datos. Exporta un respaldo y reduce la lista.");}
    static JSONObject sync(Activity a,AppStore s,String actor)throws Exception {
        JSONObject v=get(s);JSONArray queue=v.getJSONArray("queue");bounded(queue,100,150000);
        if(s.prefs.getString("householdId","").isEmpty())throw new IllegalArgumentException("Conecta primero este teléfono con tu Sheet.");
        if(!SyncEngine.online(a))throw new IllegalArgumentException("Sin conexión. La lista sigue guardada en este teléfono.");
        JSONArray batch=new JSONArray();for(int i=0;i<Math.min(20,queue.length());i++)batch.put(queue.getJSONObject(i));
        JSONObject req=new JSONObject().put("apiVersion",3).put("action","featuresSync").put("deviceId",s.deviceId).put("householdId",s.prefs.getString("householdId","")).put("token",new Vault(a).token()).put("profiles",s.profiles()).put("changes",batch);
        JSONObject response=ApiClient.post(s.prefs.getString("endpoint",""),req);
        if(!response.optBoolean("ok")||!req.getString("householdId").equals(response.optString("householdId")))throw new IllegalArgumentException("El Sheet no confirmó la lista. Instala el script 1.6.0 y conserva la misma URL /exec.");
        JSONArray acks=response.getJSONArray("featureAcks"),items=response.getJSONArray("shared");bounded(items,500,500000);
        java.util.HashMap<String,JSONObject> receipts=new java.util.HashMap<>();
        for(int i=0;i<acks.length();i++){JSONObject ack=acks.getJSONObject(i);String id=ack.getString("id");if(receipts.put(id,ack)!=null)throw new IllegalArgumentException("Confirmación repetida; se conserva la cola.");}
        JSONArray remain=new JSONArray(),conflicts=v.getJSONArray("conflicts");
        for(int i=0;i<queue.length();i++){
            JSONObject op=queue.getJSONObject(i),ack=i<batch.length()?receipts.get(op.getString("id")):null;
            if(ack==null||!java.util.Arrays.asList("accepted","duplicate","conflict").contains(ack.optString("status"))){remain.put(op);continue;}
            if(ack.optString("status").equals("conflict"))conflicts.put(new JSONObject().put("local",op).put("remote",ack.optJSONObject("current")));
        }
        v.put("queue",remain).put("shared",items).put("conflicts",conflicts).put("lastSync",Instant.now().toString());v.remove("syncError");store(s,v);return v;
    }
    public static boolean intercept(Activity a,String text){
        final JSONObject request;final String action,id;
        try{if(text.length()>4000000)return false;request=new JSONObject(text);action=request.getString("action");id=request.getString("id");if(!action.startsWith("features"))return false;}catch(Exception e){return false;}
        SyncEngine.WORK.execute(()->{try{
            AppStore s=AppStore.get(a);JSONObject d=request.optJSONObject("data");if(d==null)d=new JSONObject();
            boolean empty=s.profiles().length()==0;
            String who="";if(!(empty&&action.equals("featuresRestore")))who=actor(a);
            JSONObject v=get(s);
            if(action.equals("featuresGet")){reply(a,id,v,null);return;}
            if(action.equals("featuresBackup")||action.equals("featuresRestore")||action.equals("featuresVerify")||action.equals("featuresImage")){
                if(action.equals("featuresRestore")&&!empty)throw new IllegalArgumentException("La recuperación completa se hace en una instalación vacía para no sobrescribir datos. Exporta primero un respaldo de este teléfono.");
                final String product=d.optString("productId");final String mode=action;
                a.runOnUiThread(()->a.startActivity(new Intent(a,HouseholdDocuments.class).putExtra("mode",mode).putExtra("productId",product)));
                reply(a,id,new JSONObject().put("opened",true),null);return;
            }
            if(action.equals("featuresSave")){
                for(String k:new String[]{"products","offers"})if(d.has(k)){
                    JSONArray list=d.getJSONArray(k);bounded(list,300,k.equals("products")?3000000:300000);
                    if(k.equals("products"))for(int i=0;i<list.length();i++){
                        JSONObject p=list.getJSONObject(i);String pid=p.getString("id");
                        if(pid.isEmpty()||pid.length()>160||p.optString("name").trim().isEmpty()||p.optString("name").length()>100)throw new IllegalArgumentException("Revisa la identidad y el nombre del producto.");
                        String image=p.optString("image");if(!image.isEmpty()&&(!image.matches("data:image/(png|jpeg|webp);base64,[A-Za-z0-9+/=]+")||image.length()>800000))throw new IllegalArgumentException("Imagen no admitida o demasiado grande.");
                    }
                    v.put(k,list);
                }store(s,v);
            }else if(action.equals("featuresShared")){
                JSONArray q=v.getJSONArray("queue");if(q.length()>=100)throw new IllegalArgumentException("Sincroniza los cambios pendientes antes de añadir otros.");
                JSONObject item=d.getJSONObject("item");String itemId=item.optString("id");if(itemId.isEmpty())item.put("id",UUID.randomUUID().toString());
                if(item.optString("name").trim().isEmpty()||item.optString("name").length()>120)throw new IllegalArgumentException("Escribe un nombre de hasta 120 caracteres.");
                UUID.fromString(item.getString("id"));double quantity=item.optDouble("quantity",0);String unit=item.optString("unit","unidad");
                if(!Double.isFinite(quantity)||quantity<=0||quantity>100000||!java.util.Arrays.asList("lb","kg","litro","unidad","paquete").contains(unit)||d.optInt("baseRevision",0)<0)throw new IllegalArgumentException("Revisa cantidad, unidad y versión del artículo.");
                item=new JSONObject().put("id",item.getString("id")).put("name",item.getString("name").trim()).put("quantity",quantity).put("unit",unit).put("bought",item.optBoolean("bought")).put("deleted",item.optBoolean("deleted"));
                JSONObject op=new JSONObject().put("id",UUID.randomUUID().toString()).put("actorId",who).put("entityId",item.getString("id")).put("baseRevision",d.optInt("baseRevision",0)).put("item",item).put("createdAt",Instant.now().toString());q.put(op);v.put("queue",q);store(s,v);
            }else if(action.equals("featuresDiscard")){
                JSONArray before=v.getJSONArray("queue"),after=new JSONArray();String remove=d.getString("id");for(int i=0;i<before.length();i++)if(!remove.equals(before.getJSONObject(i).getString("id")))after.put(before.getJSONObject(i));v.put("queue",after);store(s,v);
            }else if(action.equals("featuresResolve")){
                String opId=d.getString("id");JSONArray next=new JSONArray(),q=v.getJSONArray("queue"),conflicts=v.getJSONArray("conflicts");
                for(int i=0;i<conflicts.length();i++){
                    JSONObject c=conflicts.getJSONObject(i),op=c.getJSONObject("local");if(!opId.equals(op.optString("id"))){next.put(c);continue;}
                    if(d.optBoolean("keepMine")){op.put("id",UUID.randomUUID().toString()).put("actorId",who).put("baseRevision",c.optJSONObject("remote")==null?0:c.getJSONObject("remote").optInt("revision"));q.put(op);}
                }bounded(q,100,150000);v.put("conflicts",next).put("queue",q);store(s,v);
            }else if(action.equals("featuresSync")){
                try{v=sync(a,s,who);}catch(Exception e){v.put("syncError",e.getMessage());store(s,v);throw e;}
            }else throw new IllegalArgumentException("Acción no admitida.");
            reply(a,id,v,null);SyncEngine.changed();
        }catch(Exception e){reply(a,id,null,e);}});return true;
    }
}
