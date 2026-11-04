"""Compila SharedSync real contra dobles JVM; no compila referencias decompiladas."""
from pathlib import Path
import subprocess,tempfile
ROOT=Path(__file__).resolve().parents[2];NATIVE=ROOT/'1.6.0/native'
fixtures={
'android/app/Activity.java':'package android.app; public class Activity {}',
'cu/hogarplaya/AppStore.java':'''package cu.hogarplaya; import java.util.*;import org.json.*;
public class AppStore { public final String deviceId="fixture-device";public final Prefs prefs=new Prefs();public volatile String data;
public JSONArray profiles()throws Exception{return new JSONArray("[{id:'ana'}]");}
public static class Prefs{public final Map<String,String> values=new HashMap<>();public String getString(String k,String fallback){return values.getOrDefault(k,fallback);}}}''',
'cu/hogarplaya/SyncEngine.java':'''package cu.hogarplaya;import java.util.concurrent.*;import android.app.Activity;
public class SyncEngine {public static final ExecutorService WORK=Executors.newSingleThreadExecutor();public static boolean online(Activity a){return true;}public static void changed(){}}''',
'cu/hogarplaya/Vault.java':'''package cu.hogarplaya;import android.app.Activity;public class Vault {public static volatile String value="secret-fixture";public Vault(Activity a){}public String token(){return value;}}''',
'cu/hogarplaya/ApiClient.java':'''package cu.hogarplaya;import org.json.*;import java.util.concurrent.*;public class ApiClient{public static volatile int posts;public static volatile boolean fail;public static volatile CountDownLatch entered,release;public static JSONObject response;public static JSONObject post(String url,JSONObject req)throws Exception{posts++;entered.countDown();if(!release.await(5,TimeUnit.SECONDS))throw new IllegalArgumentException("Timeout ficticio");if(fail)throw new IllegalArgumentException("Error de red ficticio");return response;}}''',
'cu/hogarplaya/HouseholdFeatures.java':'''package cu.hogarplaya;import android.app.Activity;import org.json.*;import java.util.concurrent.*;
public class HouseholdFeatures{public static volatile String active="ana";public static volatile Exception error;public static volatile JSONObject answer;public static volatile CountDownLatch replied;
static String actor(Activity a){return active;}static JSONObject get(AppStore s)throws Exception{return new JSONObject(s.data);}static void store(AppStore s,JSONObject v){s.data=v.toString();}static void bounded(JSONArray a,int n,int bytes){}static void reply(Activity a,String id,JSONObject v,Exception e){answer=v;error=e;replied.countDown();}}'''}
with tempfile.TemporaryDirectory(prefix='hogar-queue-') as folder:
 p=Path(folder);files=[]
 for name,content in fixtures.items():
  f=p/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(content);files.append(f)
 files+=[NATIVE/(n+'.java') for n in ['SharedCartPlan','SharedCartLive','SharedCartClear','SharedSyncBatch','SharedSync']]
 files.append(Path(__file__).with_name('SharedSyncTest.java'))
 subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-cp','/workspace/.tools/json.jar','-d',str(p),*[str(f) for f in files]],check=True)
 subprocess.run(['java','-cp',str(p)+':/workspace/.tools/json.jar','cu.hogarplaya.SharedSyncTest'],check=True,timeout=30)
