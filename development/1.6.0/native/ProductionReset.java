package cu.hogarplaya;
import android.app.Activity;
import android.content.*;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import org.json.*;

/** Reinicio local de una sola vez, con respaldo exportado y conexión anterior retirada. */
public final class ProductionReset {
 static final String PENDING="productionReset173Pending",DONE="productionReset173Done";
 static volatile boolean busy,committing;private static final ExecutorService RESET=Executors.newSingleThreadExecutor();
 static String actor,session,fingerprint;
 public static void begin(Activity a)throws Exception{
  AppStore s=AppStore.get(a);if(busy||!s.prefs.getString(PENDING,"").isEmpty())throw new IllegalArgumentException("Hay un inicio de producción pendiente.");
  if(!s.prefs.getString(DONE,"").isEmpty()||!s.cache("productionReset173Receipt","").isEmpty())throw new IllegalArgumentException("Este teléfono ya inició producción. No se vuelven a borrar datos nuevos.");
  if(SyncEngine.online(a))throw new IllegalArgumentException("Activa el modo avión para guardar la copia y limpiar sin recibir cambios nuevos. Después podrás reconectar.");
  actor=HouseholdFeatures.actor(a);session=s.prefs.getString("authenticatedDay","");fingerprint=null;committing=false;busy=true;
 }
 public static void captured(JSONObject backup)throws Exception{if(!busy)throw new IllegalStateException();ProductionData.clean(backup);fingerprint=ProductionData.fingerprint(backup);}
 public static void cancel(){if(!committing)busy=false;}
 public static void afterBackup(HouseholdDocuments a){
  committing=true;RESET.execute(()->{synchronized(SyncEngine.class){try{
   SyncEngine.WORK.submit(()->{try{
    AppStore s=AppStore.get(a);if(SyncEngine.online(a))throw new IllegalArgumentException("La copia se guardó, pero volvió la conexión. Activa el modo avión y repite; no se borró nada.");
    if(!session.equals(s.prefs.getString("authenticatedDay","")))throw new IllegalArgumentException("Cambió la sesión. La copia se guardó; entra con tu PIN y vuelve a empezar.");
    JSONObject current=a.productionSnapshot();if(fingerprint==null||!fingerprint.equals(ProductionData.fingerprint(current)))throw new IllegalArgumentException("La copia se guardó, pero los datos cambiaron durante el guardado. Repite el paso para respaldar el estado actual; no se borró nada.");
    JSONObject clean=ProductionData.clean(current),pending=new JSONObject().put("id",UUID.randomUUID().toString()).put("newDeviceId",UUID.randomUUID().toString()).put("oldDeviceId",s.deviceId).put("date",LocalDate.now(ZoneId.of("America/Havana")).toString()).put("endpoint",s.prefs.getString("endpoint",""));
    if(!s.prefs.edit().putString(PENDING,pending.toString()).putString("endpoint","").commit())throw new IllegalStateException("No se pudo preparar el reinicio.");
    finish(a,s,pending,clean);
   }catch(Exception e){busy=false;committing=false;a.productionError(e.getMessage());}}).get();
  }catch(Exception e){busy=false;committing=false;a.productionError("La copia se guardó. Vuelve a abrir Hogar para terminar el reinicio pendiente.");}}});
 }
 static void finish(Activity a,AppStore s,JSONObject pending,JSONObject clean)throws Exception{
  synchronized(s){
   SQLiteDatabase db=s.getWritableDatabase();new Vault(a).putToken("");
   db.beginTransaction();try{
    for(String table:new String[]{"shopping_lines","shopping_sessions","plan_changes","carts","operations","cache"})db.delete(table,null,null);
    JSONArray rows=clean.getJSONArray("cache");for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);ContentValues values=new ContentValues();values.put("name",row.getString("name"));values.put("payload",row.getString("payload"));db.insertOrThrow("cache",null,values);}
    ContentValues receipt=new ContentValues();receipt.put("name","productionReset173Receipt");receipt.put("payload",pending.toString());db.insertOrThrow("cache",null,receipt);db.setTransactionSuccessful();
   }finally{db.endTransaction();}
   if(!s.prefs.edit().putString("deviceId",pending.getString("newDeviceId")).putString("endpoint","").putString("productionEndpoint173",pending.optString("endpoint")).putString(DONE,pending.getString("date")).putString("lastSync","").putString("syncError","").putBoolean("authError",false).remove("authenticatedDay").remove(PENDING).commit())throw new IllegalStateException("La limpieza se guardó. Reabre Hogar para completar el cambio de conexión.");
  }
  a.runOnUiThread(()->{Toast.makeText(a,"Producción a cero. Abre Hogar e introduce tu PIN; después conecta con un código nuevo.",Toast.LENGTH_LONG).show();a.finishAffinity();android.os.Process.killProcess(android.os.Process.myPid());});
 }
 static void recover(Activity a,AppStore s,String text){if(busy)return;busy=true;RESET.execute(()->{synchronized(SyncEngine.class){try{SyncEngine.WORK.submit(()->{try{
   JSONObject p=new JSONObject(text);String receipt=s.cache("productionReset173Receipt","");
   if(!receipt.isEmpty()&&new JSONObject(receipt).optString("id").equals(p.getString("id"))){
    new Vault(a).putToken("");
    // Ya se borró la base: finalizar preferencias no vuelve a tocar las tablas.
    if(!s.prefs.edit().putString("deviceId",p.getString("newDeviceId")).putString("endpoint","").putString("productionEndpoint173",p.optString("endpoint")).putString(DONE,p.getString("date")).putString("lastSync","").putString("syncError","").putBoolean("authError",false).remove("authenticatedDay").remove(PENDING).commit())throw new IllegalStateException();
    a.runOnUiThread(()->{a.finishAffinity();android.os.Process.killProcess(android.os.Process.myPid());});
   }else{
    // La copia ya se exportó antes de escribir PENDING. No se admitieron nuevas acciones.
    JSONObject v=new JSONObject().put("format","hogar-finanzas-private-2").put("tables",HouseholdDocuments.productionTables(s));finish(a,s,p,ProductionData.clean(v));
   }
  }catch(Exception e){busy=false;a.runOnUiThread(()->Toast.makeText(a,"Reinicio pendiente. Revisa el espacio libre y vuelve a abrir Hogar; conserva tu respaldo.",Toast.LENGTH_LONG).show());}}).get();}catch(Exception e){busy=false;}}});}
 public static boolean guard(Activity a,String text){
  try{AppStore s=AppStore.get(a);String pending=s.prefs.getString(PENDING,"");if(!pending.isEmpty())recover(a,s,pending);if(!busy&&pending.isEmpty())return false;JSONObject q=new JSONObject(text);String action=q.optString("action");if(pending.isEmpty()&&Arrays.asList("state","featuresProfileAvatars","featuresGet","dismissKeyboard","exit").contains(action))return false;HouseholdFeatures.reply(a,q.getString("id"),null,new IllegalArgumentException("Inicio de producción en curso. Termina el respaldo o cancela para continuar."));return true;}catch(Exception e){return false;}
 }
 public static JSONObject info(AppStore s)throws Exception{String saved=s.cache("productionReset173Receipt","");JSONObject receipt=saved.isEmpty()?new JSONObject():new JSONObject(saved);return new JSONObject().put("completedAt",s.prefs.getString(DONE,receipt.optString("date"))).put("endpointHint",s.prefs.getString("productionEndpoint173",receipt.optString("endpoint")));}
}
