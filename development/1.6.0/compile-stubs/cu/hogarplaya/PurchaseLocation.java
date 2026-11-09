package cu.hogarplaya;import android.app.Activity;import android.content.Context;import org.json.*;
public class PurchaseLocation {
 public interface Done {void accept(JSONObject location);}
 public static void capture(Activity a,AppStore s,String place,Done done){}
 public static void describe(Context a,JSONObject location){}
}
