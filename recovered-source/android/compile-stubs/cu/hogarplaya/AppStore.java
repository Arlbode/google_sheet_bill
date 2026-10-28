package cu.hogarplaya;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONObject;
import org.json.JSONArray;
/** Solo firmas de compilación. No se empaqueta esta clase. */
public class AppStore extends SQLiteOpenHelper {
 public SharedPreferences prefs;
 public String deviceId;
 public JSONArray profiles()throws Exception{return null;}
 public AppStore(Context c){super(c,"stub",null,3);}
 public static AppStore get(Context c){return null;}
 public JSONObject profile(String id)throws Exception{return null;}
 public String cache(String key,String fallback){return fallback;}
 public void cache(String key,Object value){}
 public void correctRejected(String actor,String id,JSONObject data)throws Exception{}
 public JSONArray cart(String actor)throws Exception{return null;}
 public void saveCart(String actor,JSONArray items)throws Exception{}
 public int checkout(String actor,String date,boolean repeat)throws Exception{return 0;}
 public void onCreate(SQLiteDatabase db){}
 public void onUpgrade(SQLiteDatabase db,int a,int b){}
}
