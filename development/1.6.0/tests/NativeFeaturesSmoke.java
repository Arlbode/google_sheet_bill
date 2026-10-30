package cu.hogarplaya.tests;
import android.app.*;
import android.os.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
/** Solo para un emulador identificado, con instalación de prueba vacía. */
public class NativeFeaturesSmoke extends Instrumentation {
 private Object store;private Activity main;private int checks;
 private Object call(Object target,String name,Class<?>[] types,Object...args)throws Exception {Method m=target.getClass().getDeclaredMethod(name,types);m.setAccessible(true);try{return m.invoke(target,args);}catch(InvocationTargetException e){throw new RuntimeException(e.getCause());}}
 private Object staticCall(String cls,String name,Class<?>[] types,Object...args)throws Exception {Method m=Class.forName("cu.hogarplaya."+cls).getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(null,args);}
 private void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
 private SQLiteDatabase db()throws Exception{return ((SQLiteOpenHelper)store).getWritableDatabase();}
 private void drain()throws Exception {ExecutorService work=(ExecutorService)Class.forName("cu.hogarplaya.SyncEngine").getField("WORK").get(null);CountDownLatch done=new CountDownLatch(1);work.execute(done::countDown);if(!done.await(60,TimeUnit.SECONDS))throw new AssertionError("Cola no terminó");}
 private JSONObject features()throws Exception{return (JSONObject)staticCall("HouseholdFeatures","get",new Class[]{store.getClass()},store);}
 private void command(String action,JSONObject data)throws Exception {check((Boolean)staticCall("HouseholdFeatures","intercept",new Class[]{Activity.class,String.class},main,new JSONObject().put("id",UUID.randomUUID().toString()).put("action",action).put("data",data).toString()),"Puente no aceptó acción");drain();}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
  check(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish"),"Rechazado: no es emulador");
  Context context=getTargetContext();store=staticCall("AppStore","get",new Class[]{Context.class},context);
  check(((JSONArray)call(store,"profiles",new Class[]{})).length()==0,"Se requiere app de prueba vacía");
  Intent launch=context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);main=startActivitySync(launch);waitForIdleSync();drain();
  String profile=(String)call(store,"createProfile",new Class[]{String.class,String.class,String.class},"Prueba","123456","");
  staticCall("DailySession","remember",new Class[]{Activity.class,String.class},main,profile);
  JSONObject product=new JSONObject().put("id",UUID.randomUUID().toString()).put("name","Producto nativo").put("unit","lb").put("currency","CUP").put("price",100);
  command("featuresSave",new JSONObject().put("products",new JSONArray().put(product)));check(features().getJSONArray("products").length()==1,"Catálogo no persistió");
  String entity=UUID.randomUUID().toString();command("featuresShared",new JSONObject().put("baseRevision",0).put("item",new JSONObject().put("id",entity).put("name","Zanahoria").put("quantity",2).put("unit","lb")));
  check(features().getJSONArray("queue").length()==1,"Lista no persistió");
  String queuedId=features().getJSONArray("queue").getJSONObject(0).getString("id");command("featuresDiscard",new JSONObject().put("id",queuedId));check(features().getJSONArray("queue").length()==0,"No retiró cambio local de la cola");command("featuresShared",new JSONObject().put("baseRevision",0).put("item",new JSONObject().put("id",entity).put("name","Zanahoria").put("quantity",2).put("unit","lb")));
try(Cursor c=db().rawQuery("SELECT COUNT(*) FROM operations",null)){c.moveToFirst();check(c.getInt(0)==0,"La lista creó un gasto");}
  staticCall("DailySession","logout",new Class[]{Activity.class},main);command("featuresShared",new JSONObject().put("item",new JSONObject().put("name","Bloqueado")));check(features().getJSONArray("queue").length()==1,"Permitió escribir sin perfil");
  staticCall("DailySession","remember",new Class[]{Activity.class,String.class},main,profile);
  String opId=UUID.randomUUID().toString();JSONObject op=new JSONObject().put("id",opId).put("type","Gasto").put("currency","USD").put("amount","1.00").put("category","Complementarios").put("date",java.time.LocalDate.now(java.time.ZoneId.of("America/Havana")).toString()).put("concept","Prueba ficticia").put("items",new JSONArray());
  call(store,"saveOperation",new Class[]{String.class,JSONObject.class},profile,op);db().execSQL("UPDATE operations SET status='SENDING' WHERE id=?",new Object[]{opId});
  Activity doc=startActivitySync(new Intent().setClassName(context.getPackageName(),"cu.hogarplaya.HouseholdDocuments").putExtra("mode","featuresBackup").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();
  doc.getIntent().putExtra("productId",product.getString("id"));java.io.File photo=new java.io.File(context.getCacheDir(),"test-photo.png");android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(32,32,android.graphics.Bitmap.Config.ARGB_8888);try(java.io.FileOutputStream out=new java.io.FileOutputStream(photo)){bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();call(doc,"image",new Class[]{android.net.Uri.class},android.net.Uri.fromFile(photo));check(features().getJSONArray("products").getJSONObject(0).getString("image").startsWith("data:image/jpeg;base64,"),"No guardó foto validada");photo.delete();
  JSONObject saved=(JSONObject)call(doc,"snapshot",new Class[]{});call(doc,"validate",new Class[]{JSONObject.class},saved);check(saved.getJSONObject("tables").getJSONArray("profiles").length()==1,"Respaldo sin perfiles");check(saved.getJSONObject("tables").getJSONArray("operations").length()==1,"Respaldo sin operación");
  char[] password="clave-ficticia-prueba".toCharArray();byte[] encrypted=(byte[])staticCall("BackupCipher","encrypt",new Class[]{byte[].class,char[].class},saved.toString().getBytes("UTF-8"),password);byte[] plain=(byte[])staticCall("BackupCipher","decrypt",new Class[]{byte[].class,char[].class},encrypted,password);JSONObject restored=new JSONObject(new String(plain,"UTF-8"));check(restored.getString("format").equals("hogar-finanzas-private-2"),"Formato incorrecto");
  boolean refused=false;try{call(doc,"restore",new Class[]{JSONObject.class},restored);}catch(Exception expected){refused=true;}check(refused,"Sobrescribió instalación no vacía");
  String[] tables={"shopping_lines","shopping_sessions","plan_changes","carts","operations","profiles","cache"};for(String table:tables)db().delete(table,null,null);
  call(doc,"restore",new Class[]{JSONObject.class},restored);check(((JSONArray)call(store,"profiles",new Class[]{})).length()==1,"No recuperó perfil");
  try(Cursor c=db().rawQuery("SELECT id,status FROM operations",null)){check(c.moveToFirst(),"Falta gasto recuperado");check(opId.equals(c.getString(0)),"Cambió UUID");check("PENDING".equals(c.getString(1)),"No recuperó envío interrumpido como pendiente");}
  check(features().getJSONArray("products").length()==1,"No recuperó catálogo");check(features().getJSONArray("queue").length()==1,"No recuperó cola compartida");
  runOnMainSync(()->{doc.finish();main.finish();});result.putString("stream","PASS: "+checks+" comprobaciones nativas; autenticación, SQLite, cola sin gastos, cifrado, rechazo de sobrescritura, recuperación de perfiles/UUID/cola y envío interrumpido.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){while(e.getCause()!=null)e=e.getCause();result.putString("stream","FAIL: "+e.getClass().getSimpleName()+": "+e.getMessage()+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
