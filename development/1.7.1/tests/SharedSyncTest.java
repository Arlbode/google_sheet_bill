package cu.hogarplaya;
import android.app.Activity;
import org.json.*;
import java.util.concurrent.*;

public class SharedSyncTest {
 static int checks;static void ok(boolean b){if(!b)throw new AssertionError("check "+checks);checks++;}
 interface Call{void run()throws Exception;}static void rejects(Call c)throws Exception{try{c.run();throw new AssertionError("Se aceptó una respuesta incorrecta");}catch(IllegalArgumentException e){checks++;}}
 static String entity="dc71ae55-98d5-49c6-822e-4320941d5ecf";
 static JSONObject empty()throws Exception{return new JSONObject("{products:[],offers:[],shared:[],queue:[],conflicts:[],householdId:'h'}");}
 static JSONObject item(int qty)throws Exception{return new JSONObject().put("id",entity).put("name","Papa").put("quantity",qty).put("unit","lb").put("bought",false).put("deleted",false);}
 static JSONObject response(JSONArray batch)throws Exception{
  JSONArray acks=new JSONArray(),rows=new JSONArray();for(int i=0;i<batch.length();i++){JSONObject op=batch.getJSONObject(i);acks.put(new JSONObject().put("id",op.getString("id")).put("status","accepted"));rows.put(new JSONObject(op.getJSONObject("item").toString()).put("revision",op.optInt("baseRevision")+1).put("lastChangeId",op.getString("id")));}
  return new JSONObject().put("ok",true).put("householdId","h").put("featureAcks",acks).put("shared",rows);
 }
 public static void main(String[] args)throws Exception {
  JSONObject initial=SharedCartLive.change(empty(),item(1),"ana",0);JSONArray sent=new JSONArray(initial.getJSONArray("queue").toString());String original=initial.toString();
  JSONObject changed=SharedCartLive.change(initial,item(9),"ana",0);changed.getJSONArray("products").put(new JSONObject().put("id","new-product"));
  JSONObject done=SharedSyncBatch.apply(changed,sent,response(sent),"h");
  ok(initial.toString().equals(original));ok(changed.getJSONArray("queue").length()==1);ok(done.getJSONArray("products").length()==1);
  ok(done.getJSONArray("queue").length()==1);JSONObject next=done.getJSONArray("queue").getJSONObject(0);
  ok(next.getJSONObject("item").getInt("quantity")==9);ok(next.getInt("baseRevision")==1);ok(!next.getString("id").equals(sent.getJSONObject(0).getString("id")));
  JSONObject deleted=SharedCartClear.plan(initial,new JSONArray().put(entity),"ana");JSONObject cleared=SharedSyncBatch.apply(deleted,sent,response(sent),"h");
  ok(cleared.getJSONArray("queue").getJSONObject(0).getJSONObject("item").getBoolean("deleted"));
  // Una operación nueva de otro artículo no pertenece al lote confirmado.
  JSONObject extra=item(4).put("id","82fe1efc-8b58-49b7-a4fc-a44c61bcd7af");JSONObject withNew=SharedCartLive.change(initial,extra,"ana",0);
  JSONObject kept=SharedSyncBatch.apply(withNew,sent,response(sent),"h");ok(kept.getJSONArray("queue").length()==1);ok(kept.getJSONArray("queue").getJSONObject(0).getString("entityId").equals(extra.getString("id")));
  JSONObject bad=response(sent).put("householdId","other");rejects(()->SharedSyncBatch.apply(initial,sent,bad,"h"));
  JSONObject duplicate=response(sent);duplicate.getJSONArray("featureAcks").put(duplicate.getJSONArray("featureAcks").getJSONObject(0));rejects(()->SharedSyncBatch.apply(initial,sent,duplicate,"h"));
  JSONObject foreign=response(sent);foreign.getJSONArray("featureAcks").getJSONObject(0).put("id","other-id");rejects(()->SharedSyncBatch.apply(initial,sent,foreign,"h"));
  JSONObject tampered=new JSONObject(initial.toString());tampered.getJSONArray("queue").getJSONObject(0).getJSONObject("item").put("quantity",99);rejects(()->SharedSyncBatch.apply(tampered,sent,response(sent),"h"));
  JSONObject missing=response(sent).put("featureAcks",new JSONArray());ok(SharedSyncBatch.apply(initial,sent,missing,"h").getJSONArray("queue").length()==1);
  JSONObject conflict=response(sent);conflict.getJSONArray("featureAcks").getJSONObject(0).put("status","conflict").put("current",item(5).put("revision",1));
  JSONObject unresolved=SharedSyncBatch.apply(changed,sent,conflict,"h");ok(unresolved.getJSONArray("conflicts").length()==1);ok(unresolved.getJSONObject("sharedIntents").getJSONObject(entity).getJSONObject("item").getInt("quantity")==9);
  // Ejecuta el despachador real con HTTP detenido: WORK debe seguir respondiendo.
  Activity a=new Activity();AppStore store=new AppStore();store.data=initial.toString();
  for(String binding:new String[]{"normal","endpoint","householdId","authenticatedDay","token","actor","networkError"}){
   store.prefs.values.put("householdId","h");store.prefs.values.put("endpoint","https://example.test/exec");store.prefs.values.put("authenticatedDay","today");Vault.value="secret-fixture";HouseholdFeatures.active="ana";HouseholdFeatures.error=null;HouseholdFeatures.answer=null;
   store.data=initial.toString();HouseholdFeatures.replied=new CountDownLatch(1);ApiClient.entered=new CountDownLatch(1);ApiClient.release=new CountDownLatch(1);ApiClient.response=response(sent);ApiClient.fail=binding.equals("networkError");
   int posts=ApiClient.posts;SyncEngine.WORK.submit(()->{try{SharedSync.start(a,store,"ana","reply");}catch(Exception e){throw new RuntimeException(e);}}).get(1,TimeUnit.SECONDS);
   ok(ApiClient.entered.await(1,TimeUnit.SECONDS));long t=System.nanoTime();
   SyncEngine.WORK.submit(()->{try{JSONObject latest=SharedCartLive.change(new JSONObject(store.data),item(9),"ana",0);latest.put("testLocalMutation",true);store.data=latest.toString();}catch(Exception e){throw new RuntimeException(e);}}).get(300,TimeUnit.MILLISECONDS);
   long localMs=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t);ok(localMs<300);
   // Un segundo sondeo no crea otra petición mientras la anterior está detenida.
   SyncEngine.WORK.submit(()->{try{SharedSync.start(a,store,"ana","second");}catch(Exception e){throw new RuntimeException(e);}}).get(300,TimeUnit.MILLISECONDS);ok(ApiClient.posts==posts+1);
   HouseholdFeatures.replied=new CountDownLatch(1);
   if(binding.equals("actor"))HouseholdFeatures.active="luis";else if(binding.equals("token"))Vault.value="changed";else if(!binding.equals("normal")&&!binding.equals("networkError"))store.prefs.values.put(binding,"changed");
   ApiClient.release.countDown();ok(HouseholdFeatures.replied.await(2,TimeUnit.SECONDS));SyncEngine.WORK.submit(()->{}).get(1,TimeUnit.SECONDS);
   JSONObject latest=new JSONObject(store.data);ok(latest.optBoolean("testLocalMutation"));
   if(binding.equals("normal")){ok(HouseholdFeatures.error==null);ok(latest.getJSONArray("queue").getJSONObject(0).getInt("baseRevision")==1);}
   else {ok(HouseholdFeatures.error!=null);if(binding.equals("networkError"))ok(latest.has("syncError"));ok(latest.getJSONArray("queue").getJSONObject(0).getInt("baseRevision")==0);}
   System.out.println("HTTP detenido; acción local "+localMs+" ms; contexto "+binding+" verificado.");
  }
  java.lang.reflect.Field f=SharedSync.class.getDeclaredField("NETWORK");f.setAccessible(true);((ExecutorService)f.get(null)).shutdownNow();SyncEngine.WORK.shutdownNow();
  System.out.println("PASS: "+checks+" comprobaciones de concurrencia, cola, conflictos y conexiones/sesiones.");
 }
}
