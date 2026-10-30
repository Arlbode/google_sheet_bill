package cu.hogarplaya.tests;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.view.WindowManager;import java.lang.reflect.*;import java.io.*;
public class CaptureSmoke extends Instrumentation {
 public void onCreate(Bundle b){super.onCreate(b);start();}
 public void onStart(){Bundle result=new Bundle();try{
  if(!Build.HARDWARE.contains("ranchu")&&!Build.HARDWARE.contains("goldfish"))throw new Exception("Se rechaza dispositivo físico");
  Context c=getTargetContext();Intent launch=c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);Activity main=startActivitySync(launch);waitForIdleSync();
  runOnMainSync(()->{try{main.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);Class.forName("cu.hogarplaya.HouseholdFeatures").getMethod("intercept",Activity.class,String.class).invoke(null,main,"invalid-json-test");}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();
  if((main.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_SECURE)!=0)throw new Exception("MainActivity aún bloquea capturas");
  Bitmap screenshot=getUiAutomation().takeScreenshot();if(screenshot==null)throw new Exception("No se pudo capturar pantalla");boolean visible=false;for(int y=screenshot.getHeight()/3;y<screenshot.getHeight()*2/3;y+=15)for(int x=20;x<screenshot.getWidth()-20;x+=15){int color=screenshot.getPixel(x,y);if(Color.red(color)>40||Color.green(color)>40||Color.blue(color)>40)visible=true;}if(!visible)throw new Exception("Captura de app vacía o negra");try(FileOutputStream out=new FileOutputStream(new File(c.getFilesDir(),"Capture_1.6.2.png"))){screenshot.compress(Bitmap.CompressFormat.PNG,100,out);}screenshot.recycle();
  Activity documents=startActivitySync(new Intent().setClassName(c.getPackageName(),"cu.hogarplaya.HouseholdDocuments").putExtra("mode","featuresBackup").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();if((documents.getWindow().getAttributes().flags&WindowManager.LayoutParams.FLAG_SECURE)!=0)throw new Exception("Documentos aún bloquea capturas");runOnMainSync(()->{documents.finish();main.finish();});result.putString("stream","PASS: capturas permitidas en MainActivity y HouseholdDocuments; captura real no negra guardada.\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){while(e.getCause()!=null)e=e.getCause();result.putString("stream","FAIL: "+e.getClass().getSimpleName()+": "+e.getMessage()+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
