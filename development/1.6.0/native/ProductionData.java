package cu.hogarplaya;
import org.json.*;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/** Transformación explícita para el único reinicio a producción autorizado. */
public final class ProductionData {
 static final String[] FINANCIAL={"operations","carts","plan_changes","shopping_lines","shopping_sessions"};
 public static JSONObject clean(JSONObject backup)throws Exception{
  if(!"hogar-finanzas-private-2".equals(backup.getString("format")))throw new IllegalArgumentException("Respaldo no compatible.");
  JSONObject tables=backup.getJSONObject("tables"),result=new JSONObject();result.put("profiles",new JSONArray(tables.getJSONArray("profiles").toString()));
  for(String name:FINANCIAL){tables.getJSONArray(name);result.put(name,new JSONArray());}
  JSONArray cache=new JSONArray(),old=tables.getJSONArray("cache");
  for(int i=0;i<old.length();i++){
   JSONObject row=old.getJSONObject(i);String name=row.getString("name");
   if(name.equals("profilePhotos170")){cache.put(new JSONObject(row.toString()));continue;}
   if(name.equals("features160")||name.startsWith("features160:")){
    JSONObject previous=new JSONObject(row.getString("payload")),value=new JSONObject();
    value.put("householdId",previous.optString("householdId")).put("products",previous.optJSONArray("products")==null?new JSONArray():previous.getJSONArray("products")).put("offers",previous.optJSONArray("offers")==null?new JSONArray():previous.getJSONArray("offers"));
    for(String k:new String[]{"shared","queue","conflicts"})value.put(k,new JSONArray());
    cache.put(new JSONObject().put("name",name).put("payload",value.toString()));
   }
  }
  result.put("cache",cache);return result;
 }
 static String canonical(Object value)throws Exception{
  if(value instanceof JSONObject){JSONObject o=(JSONObject)value;List<String> keys=new ArrayList<>();Iterator<String> i=o.keys();while(i.hasNext())keys.add(i.next());Collections.sort(keys);StringBuilder out=new StringBuilder("{");for(String k:keys){if(out.length()>1)out.append(',');out.append(JSONObject.quote(k)).append(':').append(canonical(o.get(k)));}return out.append('}').toString();}
  if(value instanceof JSONArray){JSONArray a=(JSONArray)value;StringBuilder out=new StringBuilder("[");for(int i=0;i<a.length();i++){if(i>0)out.append(',');out.append(canonical(a.get(i)));}return out.append(']').toString();}
  String scalar=new JSONArray().put(value).toString();return scalar.substring(1,scalar.length()-1);
 }
 public static String fingerprint(JSONObject backup)throws Exception{
  JSONObject copy=new JSONObject(backup.toString());copy.remove("createdAt");byte[] hash=MessageDigest.getInstance("SHA-256").digest(canonical(copy).getBytes(StandardCharsets.UTF_8));StringBuilder out=new StringBuilder();for(byte b:hash)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();
 }
}
