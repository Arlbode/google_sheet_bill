package cu.hogarplaya;
import org.json.*;import java.util.concurrent.*;import java.util.*;
public class ProductionResetTest{
 static int n;static void ok(boolean b){if(!b)throw new AssertionError("check "+n);n++;}
 static AppStore seed(){ProductionReset.busy=false;ProductionReset.committing=false;Vault.value="old-token";SyncEngine.connected=false;AppStore s=new AppStore();AppStore.current=s;s.prefs.values.put("endpoint","https://script.google.com/macros/s/fixture/exec");s.prefs.values.put("householdId","same-household");s.prefs.values.put("authenticatedDay","fixture-auth-day");s.prefs.values.put("api-key-fixture","preserve");s.db.tables.put("profiles",new JSONArray("[{id:'ana',pin_hash:'fixture-pin',salt:'fixture-salt'}]"));
 for(String name:ProductionData.FINANCIAL)s.db.tables.put(name,new JSONArray().put(new JSONObject().put("id",name+"-old")));
 s.db.tables.put("cache",new JSONArray().put(new JSONObject().put("name","features160").put("payload","{householdId:'same-household',products:[{id:'custom',image:'fixture-photo'}],offers:[],queue:[{id:'pending'}],shared:[],conflicts:[]}")).put(new JSONObject().put("name","profilePhotos170").put("payload","{ana:'fixture-photo'}")).put(new JSONObject().put("name","snapshot").put("payload","{balances:{usd:999}}")));android.os.Process.killed=new CountDownLatch(1);return s;}
 static void waitFor(java.util.function.BooleanSupplier condition)throws Exception{long end=System.currentTimeMillis()+3000;while(!condition.getAsBoolean()){if(System.currentTimeMillis()>end)throw new AssertionError("Tiempo agotado");Thread.sleep(10);}}
 static void zero(AppStore s)throws Exception{for(String t:ProductionData.FINANCIAL)ok(s.db.tables.getJSONArray(t).length()==0);ok(s.db.tables.getJSONArray("profiles").getJSONObject(0).getString("pin_hash").equals("fixture-pin"));ok(!s.cache("profilePhotos170","").isEmpty());ok(new JSONObject(s.cache("features160","{}")).getJSONArray("products").length()==1);ok(s.cache("snapshot","").isEmpty());ok(Vault.value.isEmpty());ok(!s.prefs.values.containsKey(ProductionReset.PENDING));ok(!s.prefs.getString(ProductionReset.DONE,"").isEmpty());ok(s.prefs.getString("endpoint","bad").isEmpty());ok(s.prefs.getString("api-key-fixture","").equals("preserve"));ok(s.prefs.getString("householdId","").equals("same-household"));UUID.fromString(s.prefs.getString("deviceId",""));ok(!s.prefs.getString("deviceId","").equals("old-device"));}
 public static void main(String[] args)throws Exception{
  try{
   HouseholdDocuments a=new HouseholdDocuments();AppStore s=seed();SyncEngine.connected=true;try{ProductionReset.begin(a);throw new AssertionError();}catch(IllegalArgumentException expected){n++;}ok(s.db.deletes==0);SyncEngine.connected=false;
   ProductionReset.begin(a);ok(ProductionReset.guard(a,"{id:'save',action:'save'}"));ok(!ProductionReset.guard(a,"{id:'state',action:'state'}"));ProductionReset.captured(a.productionSnapshot());ProductionReset.cancel();ok(!ProductionReset.busy);ok(s.db.deletes==0);
   // HTTP financiero retiene el monitor: el reinicio espera fuera de WORK.
   s=seed();a=new HouseholdDocuments();ProductionReset.begin(a);ProductionReset.captured(a.productionSnapshot());
   synchronized(SyncEngine.class){ProductionReset.afterBackup(a);SyncEngine.WORK.submit(()->{}).get(300,TimeUnit.MILLISECONDS);ok(s.db.deletes==0);}
   ok(android.os.Process.killed.await(3,TimeUnit.SECONDS));zero(s);
   s.prefs.values.remove(ProductionReset.DONE);ok(!ProductionReset.info(s).getString("completedAt").isEmpty());try{ProductionReset.busy=false;ProductionReset.begin(a);throw new AssertionError();}catch(IllegalArgumentException expected){n++;}
   // Un cambio posterior al respaldo debe impedir TODA eliminación.
   s=seed();a=new HouseholdDocuments();ProductionReset.begin(a);ProductionReset.captured(a.productionSnapshot());s.db.tables.getJSONArray("operations").put(new JSONObject().put("id","later"));ProductionReset.afterBackup(a);HouseholdDocuments changed=a;waitFor(()->changed.error!=null);ok(s.db.deletes==0);ok(s.prefs.getString("endpoint","").contains("fixture"));ok(Vault.value.equals("old-token"));
   // Fallo SQLite: revierte la transacción; marca pendiente y reanuda el mismo reinicio.
   s=seed();a=new HouseholdDocuments();ProductionReset.begin(a);ProductionReset.captured(a.productionSnapshot());s.db.failDelete=true;ProductionReset.afterBackup(a);HouseholdDocuments failed=a;waitFor(()->failed.error!=null);ok(s.db.tables.getJSONArray("operations").length()==1);ok(!s.prefs.getString(ProductionReset.PENDING,"").isEmpty());ProductionReset.guard(a,"{id:'state',action:'state'}");ok(android.os.Process.killed.await(3,TimeUnit.SECONDS));zero(s);
   // Base ya vacía y preferencias pendientes: recuperar no borra otra vez.
   s=seed();a=new HouseholdDocuments();ProductionReset.begin(a);ProductionReset.captured(a.productionSnapshot());s.prefs.failDone=true;ProductionReset.afterBackup(a);HouseholdDocuments prefsFailed=a;waitFor(()->prefsFailed.error!=null);ok(s.db.tables.getJSONArray("operations").length()==0);int deleted=s.db.deletes;ProductionReset.guard(a,"{id:'state',action:'state'}");ok(android.os.Process.killed.await(3,TimeUnit.SECONDS));ok(s.db.deletes==deleted);zero(s);
   System.out.println("PASS: "+n+" comprobaciones del reinicio real, guardia, monitor de red, cambio tras copia, rollback SQLite y recuperación idempotente de preferencias.");
  }finally{java.lang.reflect.Field f=ProductionReset.class.getDeclaredField("RESET");f.setAccessible(true);((ExecutorService)f.get(null)).shutdownNow();SyncEngine.WORK.shutdownNow();}
 }
}
