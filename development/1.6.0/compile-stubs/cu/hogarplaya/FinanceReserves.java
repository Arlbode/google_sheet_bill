package cu.hogarplaya;import org.json.*;
/** Firmas de las clases originales; no se empaquetan. */
public class FinanceReserves {
 static JSONArray names(AppStore s)throws Exception{return null;}
 static boolean contains(JSONArray a,String v){return false;}
 static String fund(JSONObject op,JSONArray names){return "";}
 static String bucket(JSONObject op){return "";}
 static JSONArray rows(AppStore s,String except)throws Exception{return null;}
 static long balance(JSONArray rows,JSONArray names,String fund,String bucket)throws Exception{return 0;}
 static long reserved(JSONArray rows,JSONArray names,String bucket)throws Exception{return 0;}
 static long physical(AppStore s,String bucket,String except)throws Exception{return 0;}
 static JSONObject operation(AppStore s,String id)throws Exception{return null;}
 public static void annotate(AppStore s,String id,JSONObject data)throws Exception{}
}
