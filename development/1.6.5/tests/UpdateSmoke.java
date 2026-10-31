package cu.hogarplaya.tests.update165;
import android.app.Instrumentation;import android.os.Bundle;import android.content.*;import java.io.*;import java.lang.reflect.*;import java.util.*;import javax.net.ssl.*;import org.json.*;
/** Solo emulador y preferencias de actualización; no abre ni modifica datos financieros. */
public class UpdateSmoke extends Instrumentation {
 static Method method(Class<?> c,String n,Class<?>...args)throws Exception{Method m=c.getDeclaredMethod(n,args);m.setAccessible(true);return m;}
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){Bundle result=new Bundle();int code=0;SharedPreferences prefs=null;Map<String,?> saved=null;File target=null;
  try {
   check(android.os.Build.HARDWARE.contains("ranchu")||android.os.Build.HARDWARE.contains("goldfish"),"Solo emulador");
   Context ctx=getTargetContext();ClassLoader loader=ctx.getClassLoader();Class<?> updates=loader.loadClass("cu.hogarplaya.AppUpdates"),policyClass=loader.loadClass("cu.hogarplaya.UpdatePolicy"),helper=loader.loadClass("cu.hogarplaya.CompatibleUpdate");
   prefs=(SharedPreferences)method(updates,"prefs",Context.class).invoke(null,ctx);saved=new HashMap<>(prefs.getAll());
   String manifest;try(InputStream in=getContext().getAssets().open("fixture.json")){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);manifest=out.toString("UTF-8");}
   prefs.edit().putString("manifest",manifest).remove("downloadedHash").putString("error","").commit();JSONObject published=new JSONObject(manifest);Object policy=method(updates,"parse",JSONObject.class).invoke(null,published);
   target=(File)method(updates,"file",Context.class,int.class).invoke(null,ctx,published.getInt("versionCode"));check(!target.exists(),"Fixture debe ser nueva");
   File candidate=new File(ctx.getCacheDir(),"update165-fixture.apk");
   try(InputStream in=getContext().getAssets().open("fixture.apk");FileOutputStream out=new FileOutputStream(candidate)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
   File bad=new File(ctx.getCacheDir(),"update165-bad.apk");try(InputStream in=new FileInputStream(candidate);FileOutputStream out=new FileOutputStream(bad)){byte[] b=new byte[32768];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}try(RandomAccessFile f=new RandomAccessFile(bad,"rw")){f.seek(90);f.write(f.read()^1);}
   Method accept=method(helper,"accept",Context.class,File.class,policyClass);
   try{accept.invoke(null,ctx,bad,policy);throw new AssertionError("Se aceptó APK alterada");}catch(InvocationTargetException expected){check(expected.getCause() instanceof Exception,"Rechazo de integridad");}finally{bad.delete();}
   check(!target.exists()&&!prefs.contains("downloadedHash"),"APK alterada no debe quedar lista");
   accept.invoke(null,ctx,candidate,policy);check(target.exists()&&!candidate.exists(),"Guardado atómico de candidata verificada");
   JSONObject state=(JSONObject)method(updates,"state",Context.class).invoke(null,ctx);check(state.getBoolean("downloaded"),"Instalador original reconoce descarga");check(prefs.getString("downloadedHash","").equals(published.getString("sha256")),"Hash original conservado");
   try(RandomAccessFile f=new RandomAccessFile(target,"rw")){f.seek(100);f.write(f.read()^1);}try{method(updates,"verify",Context.class,File.class,policyClass).invoke(null,ctx,target,policy);throw new AssertionError("Se aceptó manipulación posterior");}catch(InvocationTargetException expected){check(expected.getCause() instanceof Exception,"Verificación previa a instalar rechaza manipulación posterior");}
   Object factory=loader.loadClass("cu.hogarplaya.UpdateTransfer$CompatibleTLS").getConstructor().newInstance();try(SSLSocket socket=(SSLSocket)((SSLSocketFactory)factory).createSocket()){
    check(Arrays.equals(socket.getEnabledProtocols(),new String[]{"TLSv1.2"}),"TLS1.2 real Android");for(String c:socket.getEnabledCipherSuites())check(c.contains("ECDHE")&&(c.contains("CHACHA20")||c.contains("GCM")),"Solo AEAD efímero");
   }
   result.putString("stream","PASS: Android TLS1.2, política original, APK alterada rechazada, APK firmada verificada reconocida por instalador y manipulación posterior rechazada.\n");code=-1;
  }catch(Throwable e){result.putString("stream","FAIL: "+e+"\n");}
  finally{if(target!=null)target.delete();if(prefs!=null&&saved!=null){SharedPreferences.Editor e=prefs.edit().clear();for(Map.Entry<String,?> item:saved.entrySet()){Object v=item.getValue();String k=item.getKey();if(v instanceof String)e.putString(k,(String)v);else if(v instanceof Boolean)e.putBoolean(k,(Boolean)v);else if(v instanceof Long)e.putLong(k,(Long)v);else if(v instanceof Integer)e.putInt(k,(Integer)v);else if(v instanceof Float)e.putFloat(k,(Float)v);}e.commit();}finish(code,result);}
 }
}
