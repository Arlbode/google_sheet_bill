package cu.hogarplaya.tests;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import java.lang.reflect.*;import org.json.*;
/** Datos ficticios para comprobar actualización real de 1.5.7 a 1.6.0. */
public class UpgradeSeed extends Instrumentation {
 private Bundle arguments;
 public void onCreate(Bundle b){arguments=b;super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();try{
  if(!(Build.HARDWARE.contains("ranchu")||Build.HARDWARE.contains("goldfish")))throw new Exception("No es emulador");
  Class<?> type=Class.forName("cu.hogarplaya.AppStore");Object s=type.getMethod("get",Context.class).invoke(null,getTargetContext());SQLiteDatabase db=((SQLiteOpenHelper)s).getWritableDatabase();
  if("seed".equals(arguments.getString("mode"))){if(Boolean.parseBoolean(arguments.getString("reset","false"))){for(String table:new String[]{"shopping_lines","shopping_sessions","plan_changes","carts","operations","profiles","cache"})db.delete(table,null,null);}if(((JSONArray)type.getMethod("profiles").invoke(s)).length()!=0)throw new Exception("App no vacía");String id=(String)type.getMethod("createProfile",String.class,String.class,String.class).invoke(s,"Perfil actualización","123456","");JSONObject op=new JSONObject().put("id","12345678-1234-4234-8234-123456789012").put("type","Gasto").put("currency","USD").put("amount","3.00").put("category","Complementarios").put("date",java.time.LocalDate.now(java.time.ZoneId.of("America/Havana")).toString()).put("concept","Gasto antes de actualizar").put("items",new JSONArray());type.getMethod("saveOperation",String.class,JSONObject.class).invoke(s,id,op);}
  try(Cursor c=db.rawQuery("SELECT id,payload FROM operations",null)){if(!c.moveToFirst()||!"12345678-1234-4234-8234-123456789012".equals(c.getString(0))||!"3.00".equals(new JSONObject(c.getString(1)).getString("amount")))throw new Exception("Gasto alterado");}
  if(((JSONArray)type.getMethod("profiles").invoke(s)).length()!=1)throw new Exception("Perfil alterado");result.putString("stream","PASS: perfil y UUID/importe conservados en "+getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(),0).versionName+"\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){while(e.getCause()!=null)e=e.getCause();result.putString("stream","FAIL: "+e.getClass().getSimpleName()+": "+e.getMessage()+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
