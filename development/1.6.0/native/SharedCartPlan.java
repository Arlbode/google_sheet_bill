package cu.hogarplaya;
import org.json.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
/** Plan puro: copia completa o error; el llamador persiste una sola vez. */
public final class SharedCartPlan {
 static JSONObject item(JSONObject line)throws Exception {
  String name=line.optString("name").trim(),unit=line.optString("unitCode",line.optString("unit"));
  double q;try{q=Double.parseDouble(line.optString("quantity").replace(',','.'));}catch(Exception e){throw new IllegalArgumentException("Revisa las cantidades del carrito.");}
  if(name.isEmpty()||name.length()>120||!Double.isFinite(q)||q<=0||q>100000||!Arrays.asList("lb","kg","litro","unidad","paquete").contains(unit))throw new IllegalArgumentException("Completa nombre, cantidad y unidad de todos los artículos antes de compartir.");
  return new JSONObject().put("name",name).put("quantity",q).put("unit",unit);
 }
 static boolean same(JSONObject a,JSONObject b){return a!=null&&b!=null&&a.optString("name").equals(b.optString("name"))&&a.optString("unit").equals(b.optString("unit"))&&Double.compare(a.optDouble("quantity"),b.optDouble("quantity"))==0;}
 public static JSONObject plan(JSONObject original,JSONArray cart,String actor,String device,String household)throws Exception {
  if(household.isEmpty())throw new IllegalArgumentException("Conecta este teléfono con tu hogar antes de compartir.");
  if(cart.length()==0||cart.length()>50)throw new IllegalArgumentException("Prepara un carrito de entre 1 y 50 artículos.");
  JSONObject v=new JSONObject(original.toString()),links=v.optJSONObject("cartShares");if(links==null)links=new JSONObject();
  JSONArray queue=v.getJSONArray("queue"),shared=v.getJSONArray("shared"),conflicts=v.getJSONArray("conflicts");Set<String> seen=new HashSet<>();
  for(int i=0;i<cart.length();i++){
   JSONObject line=cart.getJSONObject(i),desired=item(line);String lineId=line.optString("id");if(lineId.isEmpty()||lineId.length()>160||!seen.add(lineId))throw new IllegalArgumentException("Revisa la identidad de los artículos del carrito.");
   String id=UUID.nameUUIDFromBytes(new JSONArray().put(household).put(device).put(actor).put(lineId).toString().getBytes(StandardCharsets.UTF_8)).toString();
   JSONObject previous=links.optJSONObject(id);
   JSONObject pending=null,remote=null;
   for(int j=0;j<queue.length();j++)if(id.equals(queue.getJSONObject(j).optString("entityId")))pending=queue.getJSONObject(j);
   if(same(previous,desired)&&pending!=null&&same(pending.optJSONObject("item"),desired)&&!pending.getJSONObject("item").optBoolean("deleted"))continue;
   if(pending!=null)throw new IllegalArgumentException("Espera a que se envíe el cambio anterior de "+desired.getString("name")+" antes de compartir otra modificación.");
   for(int j=0;j<conflicts.length();j++)if(id.equals(conflicts.getJSONObject(j).getJSONObject("local").optString("entityId")))throw new IllegalArgumentException("Resuelve primero el conflicto de "+desired.getString("name")+" en el carrito compartido.");
   for(int j=0;j<shared.length();j++)if(id.equals(shared.getJSONObject(j).optString("id")))remote=shared.getJSONObject(j);
   if(same(previous,desired)&&remote!=null&&!remote.optBoolean("deleted")&&same(remote,desired))continue;
   if(previous!=null&&remote!=null&&(remote.optBoolean("deleted")||!same(previous,remote)))throw new IllegalArgumentException("Otro teléfono cambió "+desired.getString("name")+". Revisa el carrito compartido antes de reenviarlo.");
   JSONObject next=new JSONObject(desired.toString()).put("id",id).put("bought",remote!=null&&remote.optBoolean("bought")).put("deleted",false);
   if(queue.length()>=100)throw new IllegalArgumentException("Envía los cambios pendientes antes de compartir el carrito.");
   queue.put(new JSONObject().put("id",UUID.randomUUID().toString()).put("actorId",actor).put("entityId",id).put("baseRevision",remote==null?0:remote.optInt("revision")).put("item",next).put("createdAt",Instant.now().toString()));links.put(id,desired);
  }
  if(links.length()>1000)throw new IllegalArgumentException("El historial de carritos compartidos alcanzó su límite; conserva un respaldo.");
  v.put("cartShares",links);if(v.toString().length()>4000000)throw new IllegalArgumentException("Hay demasiados datos locales para compartir otro carrito.");return v;
 }
}
