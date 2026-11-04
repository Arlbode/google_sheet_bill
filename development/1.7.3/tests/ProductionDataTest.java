package cu.hogarplaya;
import org.json.*;
public class ProductionDataTest {
 static int n;static void ok(boolean b){if(!b)throw new AssertionError("check "+n);n++;}
 public static void main(String[] args)throws Exception{
  JSONObject profiles=new JSONObject().put("id","ana").put("pin_hash","fixture-verifier").put("salt","fixture-salt");
  JSONObject features=new JSONObject("{householdId:'h',products:[{id:'custom',name:'Pasta',image:'data:image/jpeg;base64,fixture'}],offers:[{id:'ref'}],shared:[{id:'shared'}],queue:[{id:'pending'}],conflicts:[{local:{id:'conflict'}}],sharedIntents:{x:{}},sharedCartOwners:{x:{}},sharedCartActive:{ana:true},cartShares:{x:1},lastSync:'old'}");
  JSONArray cache=new JSONArray().put(new JSONObject().put("name","features160").put("payload",features.toString())).put(new JSONObject().put("name","features160:other").put("payload",features.toString())).put(new JSONObject().put("name","profilePhotos170").put("payload","{ana:'data:image/jpeg;base64,fixture'}")).put(new JSONObject().put("name","snapshot").put("payload","{balances:{usd:999}}"));
  JSONObject tables=new JSONObject().put("profiles",new JSONArray().put(profiles)).put("cache",cache);
  for(String t:ProductionData.FINANCIAL)tables.put(t,new JSONArray().put(new JSONObject().put("id",t+"-fixture")));
  JSONObject backup=new JSONObject().put("format","hogar-finanzas-private-2").put("createdAt","old").put("prefs",new JSONObject().put("deviceId","old-device")).put("token","old-token").put("tables",tables);String original=backup.toString();JSONObject clean=ProductionData.clean(backup);
  ok(backup.toString().equals(original));ok(clean.getJSONArray("profiles").toString().equals(tables.getJSONArray("profiles").toString()));
  for(String t:ProductionData.FINANCIAL)ok(clean.getJSONArray(t).length()==0);
  ok(clean.getJSONArray("cache").length()==3);
  for(int i=0;i<2;i++){
   JSONObject f=new JSONObject(clean.getJSONArray("cache").getJSONObject(i).getString("payload"));ok(f.getJSONArray("products").getJSONObject(0).getString("image").contains("fixture"));ok(f.getJSONArray("offers").length()==1);ok(f.getJSONArray("queue").length()==0);ok(f.getJSONArray("conflicts").length()==0);ok(f.getJSONArray("shared").length()==0);ok(!f.has("sharedIntents"));ok(!f.has("cartShares"));ok(!f.has("sharedCartOwners"));ok(!f.has("lastSync"));
  }
  ok(clean.getJSONArray("cache").getJSONObject(2).getString("payload").equals(cache.getJSONObject(2).getString("payload")));
  String hash=ProductionData.fingerprint(backup);ok(hash.equals(ProductionData.fingerprint(new JSONObject(backup.toString()).put("createdAt","new"))));
  JSONObject changed=new JSONObject(backup.toString());changed.getJSONObject("tables").getJSONArray("operations").put(new JSONObject().put("id","new-op"));ok(!hash.equals(ProductionData.fingerprint(changed)));
  changed=new JSONObject(backup.toString()).put("token","new-token");ok(!hash.equals(ProductionData.fingerprint(changed)));
  changed=new JSONObject(backup.toString());changed.getJSONObject("tables").getJSONArray("profiles").getJSONObject(0).put("pin_hash","changed");ok(!hash.equals(ProductionData.fingerprint(changed)));
  JSONObject already=new JSONObject(backup.toString()).put("tables",clean);ok(ProductionData.clean(already).toString().equals(clean.toString()));
  try{ProductionData.clean(new JSONObject().put("format","other"));throw new AssertionError();}catch(IllegalArgumentException expected){n++;}
  System.out.println("PASS: "+n+" comprobaciones; vaciado financiero, PIN/fotos/catálogo conservados, colas retiradas, respaldo inmutable y detección de cambios.");
 }
}
