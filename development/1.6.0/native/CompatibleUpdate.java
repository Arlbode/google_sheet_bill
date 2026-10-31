package cu.hogarplaya;
import android.app.Activity;
import android.os.Build;
import android.webkit.WebView;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
/** Reutiliza política y verificación originales; no toca el instalador. */
public final class CompatibleUpdate {
    static final AtomicBoolean active=new AtomicBoolean();
    static void changed(Activity a){a.runOnUiThread(()->{try{Field f=a.getClass().getDeclaredField("web");f.setAccessible(true);WebView w=(WebView)f.get(a);if(w!=null)w.evaluateJavascript("window.hogarChanged()",null);}catch(Exception ignored){}});}
    static void accept(android.content.Context context,File temp,UpdatePolicy policy)throws Exception {
        AppUpdates.verify(context,temp,policy);
        File target=AppUpdates.file(context,policy.versionCode);
        if(target.exists()&&!target.delete())throw new IllegalStateException("No se pudo renovar la descarga.");
        if(!temp.renameTo(target))throw new IllegalStateException("No se pudo guardar la actualización verificada.");
        AppUpdates.prefs(context).edit().putString("downloadedHash",policy.sha256).putString("error","").apply();
    }
    public static void browser(Activity a)throws Exception {
        UpdatePolicy policy=AppUpdates.parse(AppUpdates.cached(a));
        String url="https://github.com/Arlbode/google_sheet_bill/raw/refs/heads/actualizaciones-android/Hogar_finanzas_"+policy.versionName+".apk";
        a.runOnUiThread(()->{try{a.startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(url)));}catch(android.content.ActivityNotFoundException e){android.widget.Toast.makeText(a,"No hay un navegador disponible en este teléfono.",android.widget.Toast.LENGTH_LONG).show();}});
    }
    public static void start(Activity a)throws Exception {
        if(!active.compareAndSet(false,true))return;
        Field flag=AppUpdates.class.getDeclaredField("downloading");flag.setAccessible(true);
        boolean reserved=false;
        try {
            UpdatePolicy policy=AppUpdates.parse(AppUpdates.cached(a));
            if(policy.minSdk>Build.VERSION.SDK_INT)throw new IllegalArgumentException("La actualización necesita un Android más reciente.");
            synchronized(AppUpdates.class){if(flag.getBoolean(null))throw new IllegalStateException("Ya hay una descarga en curso.");flag.setBoolean(null,true);reserved=true;}
            AppUpdates.prefs(a).edit().putString("error","").apply();
            new Thread(()->{
                File temp=null;
                try {
                    temp=UpdateTransfer.download(policy.url,policy.bytes,AppUpdates.directory(a));
                    accept(a,temp,policy);
                }catch(Exception e){
                    AppUpdates.prefs(a).edit().remove("downloadedHash").putString("error","No se pudo completar la descarga segura. Se descartó el archivo incompleto. Puedes reintentar o descargar la APK desde GitHub en el navegador.").apply();
                }finally{if(temp!=null&&temp.exists())temp.delete();try{flag.setBoolean(null,false);}catch(Exception ignored){}active.set(false);changed(a);}
            },"hogar-compatible-update").start();
        }catch(Exception e){if(reserved)flag.setBoolean(null,false);active.set(false);throw e;}
    }
}
