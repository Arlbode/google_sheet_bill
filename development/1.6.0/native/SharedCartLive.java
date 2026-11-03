package cu.hogarplaya;
import org.json.*;import java.util.*;import java.time.Instant;import java.nio.charset.StandardCharsets;
/** Intenciones duraderas separadas de los recibos ya enviados. Nunca modifica datos financieros. */
public final class SharedCartLive {
 static JSONObject object(JSONObject v,String key)throws Exception{JSONObject o=v.optJSONObject(key);if(o==null){o=new JSONObject();v.put(key,o);}return o;}
 static JSONObject copy(JSONObject v)throws Exception{return new JSONObject(v.toString());}
 static JSONObject remote(JSONObject v,String id)throws Exception{JSONArray a=v.getJSONArray("shared");for(int i=0;i<a.length();i++)if(id.equals(a.getJSONObject(i).optString("id")))return a.getJSONObject(i);return null;}
 static JSONObject pending(JSONObject v,String id)throws Exception{JSONArray a=v.getJSONArray("queue");for(int i=0;i<a.length();i++)if(id.equals(a.getJSONObject(i).optString("entityId")))return a.getJSONObject(i);return null;}
 static boolean conflict(JSONObject v,String id)throws Exception{JSONArray a=v.getJSONArray("conflicts");for(int i=0;i<a.length();i++)if(id.equals(a.getJSONObject(i).getJSONObject("local").optString("entityId")))return true;return false;}
 static boolean same(JSONObject a,JSONObject b){return SharedCartPlan.same(a,b)&&a.optBoolean("bought")==b.optBoolean("bought")&&a.optBoolean("deleted")==b.optBoolean("deleted");}
 static JSONObject clean(JSONObject value)throws Exception{
  JSONObject line=copy(value);line.put("unitCode",value.optString("unit","unidad"));JSONObject result=SharedCartPlan.item(line);String id=value.getString("id");UUID.fromString(id);return result.put("id",id).put("bought",value.optBoolean("bought")).put("deleted",value.optBoolean("deleted"));
 }
 static void wish(JSONObject v,JSONObject item,String actor,int revision)throws Exception{
  JSONObject intents=object(v,"sharedIntents");if(intents.length()>=500&&!intents.has(item.getString("id")))throw new IllegalArgumentException("Hay demasiados cambios pendientes.");JSONObject q=pending(v,item.getString("id"));
  intents.put(item.getString("id"),new JSONObject().put("item",clean(item)).put("actorId",actor).put("baseRevision",revision).put("afterId",q==null?"":q.getString("id")));
 }
 public static JSONObject change(JSONObject original,JSONObject item,String actor,int revision)throws Exception{return change(original,item,actor,revision,"");}
 public static JSONObject change(JSONObject original,JSONObject item,String actor,int revision,String after)throws Exception{JSONObject v=copy(original);wish(v,item,actor,revision);if(pending(v,item.getString("id"))==null&&!after.isEmpty())object(v,"sharedIntents").getJSONObject(item.getString("id")).put("afterId",after);return pump(v);}
 public static JSONObject pump(JSONObject original)throws Exception {
  JSONObject v=copy(original),intents=object(v,"sharedIntents");JSONArray queue=v.getJSONArray("queue"),conflicts=v.getJSONArray("conflicts");List<String> ids=new ArrayList<>();Iterator<String> keys=intents.keys();while(keys.hasNext())ids.add(keys.next());
  for(String id:ids){if(pending(v,id)!=null||conflict(v,id))continue;JSONObject intent=intents.getJSONObject(id),item=intent.getJSONObject("item"),r=remote(v,id);if(r!=null&&same(item,r)){intents.remove(id);continue;}
   int revision=intent.optInt("baseRevision");String after=intent.optString("afterId");if(r!=null&&!after.isEmpty()&&after.equals(r.optString("lastChangeId")))revision=r.optInt("revision");
   JSONObject op=new JSONObject().put("id",UUID.randomUUID().toString()).put("entityId",id).put("actorId",intent.getString("actorId")).put("baseRevision",revision).put("item",item).put("createdAt",Instant.now().toString());
   if(revision!=(r==null?0:r.optInt("revision"))){conflicts.put(new JSONObject().put("local",op).put("remote",r==null?JSONObject.NULL:r));continue;}
   if(queue.length()>=100)continue;
   queue.put(op);intent.put("afterId",op.getString("id")).put("baseRevision",revision);
  }return v;
 }
 public static JSONObject resolve(JSONObject original,String opId,boolean mine,String actor)throws Exception {
  JSONObject v=copy(original);JSONArray old=v.getJSONArray("conflicts"),keep=new JSONArray();JSONObject selected=null;
  for(int i=0;i<old.length();i++){JSONObject c=old.getJSONObject(i);if(opId.equals(c.getJSONObject("local").getString("id")))selected=c;else keep.put(c);}v.put("conflicts",keep);
  if(selected!=null){String id=selected.getJSONObject("local").getString("entityId");JSONObject intents=object(v,"sharedIntents"),last=intents.optJSONObject(id),item=last==null?selected.getJSONObject("local").getJSONObject("item"):last.getJSONObject("item");intents.remove(id);if(mine){JSONObject r=selected.optJSONObject("remote");wish(v,item,actor,r==null?0:r.optInt("revision"));}}
  return pump(v);
 }
 public static JSONObject noteShare(JSONObject original,JSONArray cart,String actor,String device,String household)throws Exception {
  JSONObject v=copy(original),owners=object(v,"sharedCartOwners");object(v,"sharedCartActive").put(actor,true);for(int i=0;i<cart.length();i++){JSONObject line=cart.getJSONObject(i);String id=entity(actor,device,household,line.getString("id"));JSONObject old=owners.optJSONObject(id);owners.put(id,new JSONObject().put("actorId",actor).put("lineId",line.getString("id")).put("scope","cart").put("lastLine",SharedCartPlan.item(line)).put("productId",line.optString("productId")).put("seenRevision",old==null?(remote(v,id)==null?0:remote(v,id).optInt("revision")):old.optInt("seenRevision")));}return v;
 }
 static String entity(String actor,String device,String household,String line){return UUID.nameUUIDFromBytes(new JSONArray().put(household).put(device).put(actor).put(line).toString().getBytes(StandardCharsets.UTF_8)).toString();}
 public static JSONObject migrate(JSONObject original,JSONArray cart,String actor,String device,String household)throws Exception {
  JSONObject links=original.optJSONObject("cartShares");if(links==null)return original;JSONArray known=new JSONArray();for(int i=0;i<cart.length();i++){JSONObject line=cart.getJSONObject(i);String id=entity(actor,device,household,line.optString("id"));JSONObject owners=original.optJSONObject("sharedCartOwners");JSONObject r=remote(original,id);if(links.has(id)&&(r==null||!r.optBoolean("deleted"))&&(owners==null||!owners.has(id)))known.put(line);}return known.length()==0?original:noteShare(original,known,actor,device,household);
 }
 public static JSONObject changedCart(JSONObject original,JSONArray cart,JSONObject shopping,String actor,String kind,String device,String household)throws Exception{
  JSONObject v=copy(original),owners=object(v,"sharedCartOwners");Map<String,JSONObject> local=new HashMap<>();for(int i=0;i<cart.length();i++)local.put(cart.getJSONObject(i).getString("id"),cart.getJSONObject(i));
  JSONArray shop=shopping.optJSONArray("lines");if(shop==null)shop=new JSONArray();Map<String,JSONObject> modes=new HashMap<>();for(int i=0;i<shop.length();i++)modes.put(shop.getJSONObject(i).getString("id"),shop.getJSONObject(i));List<String> ids=new ArrayList<>();Iterator<String> it=owners.keys();while(it.hasNext())ids.add(it.next());Set<String> taken=new HashSet<>();for(String id:ids){JSONObject o=owners.getJSONObject(id);if("shopping".equals(o.optString("scope")))taken.add(o.getString("lineId"));}
  if(kind.equals("cart")&&object(v,"sharedCartActive").optBoolean(actor)){
   for(int i=0;i<cart.length();i++){JSONObject line=cart.getJSONObject(i);String id=entity(actor,device,household,line.getString("id"));if(!owners.has(id)){JSONObject item=SharedCartPlan.item(line).put("id",id).put("bought",false).put("deleted",false);owners.put(id,new JSONObject().put("actorId",actor).put("lineId",line.getString("id")).put("scope","cart").put("lastLine",SharedCartPlan.item(line)).put("productId",line.optString("productId")).put("seenRevision",0));wish(v,item,actor,0);}}
  }
  if((kind.equals("purchased")||kind.equals("cart"))&&cart.length()==0||kind.equals("transfer"))object(v,"sharedCartActive").put(actor,false);
  for(String id:ids){JSONObject owner=owners.getJSONObject(id);if(!actor.equals(owner.optString("actorId")))continue;String scope=owner.optString("scope","cart"),lineId=owner.getString("lineId");JSONObject previous=owner.getJSONObject("lastLine"),line="shopping".equals(scope)?modes.get(lineId):local.get(lineId);
   if(kind.equals("transfer")&&scope.equals("cart")){for(int i=0;i<shop.length();i++){JSONObject candidate=shop.getJSONObject(i);if(!taken.contains(candidate.getString("id"))&&!candidate.optBoolean("bought")&&Arrays.asList("lb","kg","litro","unidad","paquete").contains(candidate.optString("unitCode",candidate.optString("unit")))&&SharedCartPlan.same(previous,SharedCartPlan.item(candidate))){owner.put("scope","shopping").put("lineId",candidate.getString("id"));taken.add(candidate.getString("id"));break;}}continue;}
   if(scope.equals("shopping")&&!kind.startsWith("shopping"))continue;
   if(scope.equals("cart")&&kind.startsWith("shopping"))continue;
   JSONObject r=remote(v,id),q=pending(v,id),intent=object(v,"sharedIntents").optJSONObject(id);JSONObject base=intent!=null?intent.getJSONObject("item"):q!=null?q.getJSONObject("item"):r;if(base==null)continue;
   JSONObject desired=copy(base);boolean update=false;
   if(line==null){desired.put(kind.equals("purchased")?"bought":"deleted",true);owners.remove(id);update=true;}
   else if(line.optBoolean("bought")){JSONObject boughtLine=SharedCartPlan.item(line);desired.put("name",boughtLine.getString("name")).put("quantity",boughtLine.getDouble("quantity")).put("unit",boughtLine.getString("unit")).put("bought",true);owners.remove(id);update=true;}
   else {JSONObject normalized=SharedCartPlan.item(line);if(!SharedCartPlan.same(previous,normalized)){desired.put("name",normalized.getString("name")).put("quantity",normalized.getDouble("quantity")).put("unit",normalized.getString("unit"));owner.put("lastLine",normalized);update=true;}}
   if(update)wish(v,desired,actor,owner.optInt("seenRevision",r==null?0:r.optInt("revision")));
  }return pump(v);
 }
 public static JSONObject acknowledged(JSONObject original,JSONArray batch,JSONArray acks)throws Exception{
  JSONObject v=copy(original),owners=object(v,"sharedCartOwners");for(int i=0;i<batch.length();i++){JSONObject op=batch.getJSONObject(i),r=remote(v,op.getString("entityId")),o=owners.optJSONObject(op.getString("entityId"));if(o!=null&&r!=null&&op.getString("id").equals(r.optString("lastChangeId")))o.put("seenRevision",r.optInt("revision"));}return pump(SharedCartClear.received(v));
 }
}
