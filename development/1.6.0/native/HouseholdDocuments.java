package cu.hogarplaya;
import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import org.json.*;

/** Selector de documentos del sistema; los respaldos privados nunca pasan al WebView. */
public final class HouseholdDocuments extends Activity {
    private static final String[] TABLES={"profiles","operations","carts","cache","plan_changes","shopping_sessions","shopping_lines"};
    private static final String[] PREFS={"deviceId","deviceName","householdId","endpoint"};
    private byte[] pending;private char[] password;private String mode;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);mode=getIntent().getStringExtra("mode");if(b!=null){finish();return;}
        if("featuresImage".equals(mode)){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,3);return;}
        if("featuresBackup".equals(mode))passwordDialog(true,null);else{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,2);}
    }
    private void error(){runOnUiThread(()->new AlertDialog.Builder(this).setTitle("No se completó la operación").setMessage("El archivo, contraseña o estado del teléfono no permiten continuar. No se sobrescribieron tus datos. Usa un respaldo .hgf generado con esta versión; la recuperación requiere una instalación vacía.").setPositiveButton("Cerrar",(d,w)->finish()).setOnCancelListener(d->finish()).show());}
    private void info(String message){runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Respaldo de Hogar finanzas").setMessage(message).setPositiveButton("Cerrar",(d,w)->finish()).setOnCancelListener(d->finish()).show());}
    private void passwordDialog(boolean create,byte[] encrypted){
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(32,16,32,16);EditText pass=new EditText(this);pass.setHint("Contraseña de al menos 10 caracteres");pass.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);box.addView(pass);EditText repeat=new EditText(this);if(create){repeat.setHint("Repite la contraseña");repeat.setInputType(pass.getInputType());box.addView(repeat);}
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(create?"Crear respaldo cifrado":"Abrir respaldo cifrado").setMessage(create?"Guarda la contraseña aparte. Protege perfiles, conexión y datos de este teléfono. Para migrar, deja de usar el teléfono anterior después de recuperar.":"La contraseña nunca se envía a internet.").setView(box).setPositiveButton("Continuar",null).setNegativeButton("Cancelar",(d,w)->finish()).create();dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{String value=pass.getText().toString();if(value.length()<10||create&&!value.equals(repeat.getText().toString())){pass.setError("Revisa la contraseña y su confirmación");return;}password=value.toCharArray();pass.setText("");repeat.setText("");dialog.dismiss();SyncEngine.WORK.execute(()->{try{if(create){JSONObject saved=snapshot();validate(saved);byte[] plain=saved.toString().getBytes(StandardCharsets.UTF_8);if(plain.length>20000000)throw new IOException();try{pending=BackupCipher.encrypt(plain,password);}finally{Arrays.fill(plain,(byte)0);}clearPassword();runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/octet-stream").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"Hogar_finanzas_"+java.time.LocalDate.now(java.time.ZoneId.of("America/Havana"))+".hgf");startActivityForResult(i,1);});}else{JSONObject content=new JSONObject(new String(BackupCipher.decrypt(encrypted,password),StandardCharsets.UTF_8));clearPassword();validate(content);if("featuresVerify".equals(mode)){AppStore.get(this).prefs.edit().putString("featuresBackupVerified",Instant.now().toString()).commit();SyncEngine.changed();info("Archivo descifrado e íntegro. Contiene "+content.getJSONObject("tables").getJSONArray("operations").length()+" movimientos. Creado: "+content.optString("createdAt")+". No se importó ni modificó ningún dato.");}else runOnUiThread(()->confirmRestore(content));}}catch(Exception e){clearPassword();error();}});}));dialog.setOnCancelListener(d->finish());dialog.show();
    }
    private void clearPassword(){if(password!=null)Arrays.fill(password,'\0');password=null;}
    private JSONObject snapshot()throws Exception {
        AppStore s=AppStore.get(this);JSONObject tables=new JSONObject();synchronized(s){SQLiteDatabase db=s.getWritableDatabase();db.beginTransaction();try{
            for(String table:TABLES){JSONArray rows=new JSONArray();try(Cursor c=db.rawQuery("SELECT * FROM "+table,null)){while(c.moveToNext()){JSONObject row=new JSONObject();for(int i=0;i<c.getColumnCount();i++)row.put(c.getColumnName(i),c.isNull(i)?JSONObject.NULL:c.getString(i));rows.put(row);}}tables.put(table,rows);}db.setTransactionSuccessful();}finally{db.endTransaction();}}
        JSONObject prefs=new JSONObject();for(String k:PREFS)prefs.put(k,s.prefs.getString(k,""));return new JSONObject().put("format","hogar-finanzas-private-2").put("createdAt",Instant.now().toString()).put("prefs",prefs).put("token",new Vault(this).token()).put("tables",tables);
    }
    private void validate(JSONObject v)throws Exception {
        if(!"hogar-finanzas-private-2".equals(v.getString("format")))throw new IllegalArgumentException();
        JSONObject tables=v.getJSONObject("tables"),prefs=v.getJSONObject("prefs");UUID.fromString(prefs.getString("deviceId"));
        if(!prefs.optString("endpoint").isEmpty()&&!prefs.getString("endpoint").matches("https://script\\.google\\.com/macros/s/[A-Za-z0-9_-]+/exec"))throw new IllegalArgumentException();
        if(!v.optString("token").isEmpty()&&!v.getString("token").matches("[a-fA-F0-9]{64}"))throw new IllegalArgumentException();
        int total=0;SQLiteDatabase db=AppStore.get(this).getReadableDatabase();
        for(String table:TABLES){JSONArray rows=tables.getJSONArray(table);total+=rows.length();if(total>100000)throw new IllegalArgumentException();Set<String> columns=new HashSet<>();try(Cursor c=db.rawQuery("PRAGMA table_info("+table+")",null)){while(c.moveToNext())columns.add(c.getString(1));}if(columns.isEmpty())throw new IllegalArgumentException();for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);Iterator<String> keys=row.keys();while(keys.hasNext())if(!columns.contains(keys.next()))throw new IllegalArgumentException();}}
    }
    private void confirmRestore(JSONObject v){new AlertDialog.Builder(this).setTitle("Recuperar en este teléfono vacío").setMessage("Recupera perfiles, sus PIN, movimientos, listas y conexión. No combina dos teléfonos. El dispositivo anterior y este compartirían identidad: utiliza solo este después de migrar. La app se cerrará al terminar; vuelve a abrirla e introduce tu PIN.").setPositiveButton("Recuperar y cerrar",(d,w)->SyncEngine.WORK.execute(()->{try{restore(v);runOnUiThread(()->{Toast.makeText(this,"Recuperación completada. Abre Hogar finanzas de nuevo.",Toast.LENGTH_LONG).show();finishAffinity();android.os.Process.killProcess(android.os.Process.myPid());});}catch(Exception e){error();}})).setNegativeButton("Cancelar",(d,w)->finish()).show();}
    private void restore(JSONObject v)throws Exception {
        AppStore s=AppStore.get(this);synchronized(s){if(s.profiles().length()!=0)throw new IllegalArgumentException();SQLiteDatabase db=s.getWritableDatabase();try(Cursor c=db.rawQuery("SELECT COUNT(*) FROM operations",null)){c.moveToFirst();if(c.getInt(0)!=0)throw new IllegalArgumentException();}
            validate(v);JSONObject prefs=v.getJSONObject("prefs");String oldToken=new Vault(this).token();Map<String,String> old=new HashMap<>();for(String k:PREFS)old.put(k,s.prefs.getString(k,""));
            db.beginTransaction();boolean saved=false;try{for(int i=TABLES.length-1;i>=0;i--)db.delete(TABLES[i],null,null);
                for(String table:TABLES){JSONArray rows=v.getJSONObject("tables").getJSONArray(table);for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);ContentValues values=new ContentValues();Iterator<String> keys=row.keys();while(keys.hasNext()){String k=keys.next();if(row.isNull(k))values.putNull(k);else values.put(k,row.getString(k));}if((table.equals("operations")||table.equals("plan_changes"))&&"SENDING".equals(values.getAsString("status")))values.put("status","PENDING");db.insertOrThrow(table,null,values);}}
                new Vault(this).putToken(v.getString("token"));if(!new Vault(this).token().equals(v.getString("token")))throw new IOException();android.content.SharedPreferences.Editor editor=s.prefs.edit();for(String k:PREFS)editor.putString(k,prefs.getString(k));editor.remove("authenticatedDay");if(!editor.commit())throw new IOException();db.setTransactionSuccessful();saved=true;
            }finally{db.endTransaction();if(!saved){new Vault(this).putToken(oldToken);android.content.SharedPreferences.Editor editor=s.prefs.edit();for(String k:PREFS)editor.putString(k,old.get(k));editor.commit();}}
        }
    }
    private byte[] read(Uri uri,int limit)throws Exception {try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream()){if(in==null)throw new IOException();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException();out.write(b,0,n);}return out.toByteArray();}}
    private void image(Uri uri)throws Exception {
        byte[] raw=read(uri,8000000);BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(raw,0,raw.length,options);if(options.outWidth<=0||options.outHeight<=0)throw new IOException();options.inSampleSize=1;while(options.outWidth/options.inSampleSize>800||options.outHeight/options.inSampleSize>800)options.inSampleSize*=2;options.inJustDecodeBounds=false;
        Bitmap image=BitmapFactory.decodeByteArray(raw,0,raw.length,options);if(image==null)throw new IOException();ByteArrayOutputStream out=new ByteArrayOutputStream();image.compress(Bitmap.CompressFormat.JPEG,80,out);image.recycle();if(out.size()>500000)throw new IOException();
        AppStore s=AppStore.get(this);JSONObject v=HouseholdFeatures.get(s);JSONArray products=v.getJSONArray("products");String id=getIntent().getStringExtra("productId");boolean found=false;for(int i=0;i<products.length();i++){JSONObject p=products.getJSONObject(i);if(p.getString("id").equals(id)){p.put("image","data:image/jpeg;base64,"+android.util.Base64.encodeToString(out.toByteArray(),2));found=true;break;}}if(!found||products.toString().length()>3000000)throw new IOException();HouseholdFeatures.store(s,v);SyncEngine.changed();info("Imagen guardada localmente. Se incluye en tu próximo respaldo cifrado.");
    }
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null){pending=null;clearPassword();finish();return;}Uri uri=data.getData();SyncEngine.WORK.execute(()->{try{
        if(request==1){if(pending==null)throw new IOException();try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException();out.write(pending);}pending=null;AppStore.get(this).prefs.edit().putString("featuresLastBackup",Instant.now().toString()).commit();SyncEngine.changed();info("Respaldo cifrado guardado. Compruébalo desde Perfil antes de cambiar de teléfono. Conserva su contraseña fuera del teléfono.");}
        else if(request==2){byte[] bytes=read(uri,24000000);runOnUiThread(()->passwordDialog(false,bytes));}
        else if(request==3)image(uri);
    }catch(Exception e){pending=null;error();}});}
    @Override protected void onDestroy(){clearPassword();super.onDestroy();}
}
