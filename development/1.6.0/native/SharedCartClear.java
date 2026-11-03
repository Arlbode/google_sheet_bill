package cu.hogarplaya;
import org.json.*;import java.util.*;import java.time.Instant;
/** Retirada de una instantánea de la cesta compartida, sin tocar carritos ni operaciones. */
public final class SharedCartClear {
 public static JSONObject plan(JSONObject original,JSONArray selected,String actor)throws Exception {
  if(selected.length()>1000)throw new IllegalArgumentException("Hay demasiados artículos para retirar en una sola acción.");
  Set<String> ids=new LinkedHashSet<>();for(int i=0;i<selected.length();i++){String id=selected.getString(i);UUID.fromString(id);ids.add(id);}
  JSONObject v=SharedCartLive.copy(original),intents=SharedCartLive.object(v,"sharedIntents");JSONArray conflicts=v.getJSONArray("conflicts"),keep=new JSONArray();Map<String,JSONObject> conflictItems=new HashMap<>();
  for(int i=0;i<conflicts.length();i++){JSONObject c=conflicts.getJSONObject(i),local=c.getJSONObject("local");String id=local.getString("entityId");if(ids.contains(id))conflictItems.put(id,local.getJSONObject("item"));else keep.put(c);}v.put("conflicts",keep);
  for(String id:ids){JSONObject r=SharedCartLive.remote(v,id),q=SharedCartLive.pending(v,id),wish=intents.optJSONObject(id);JSONObject item=wish!=null?wish.getJSONObject("item"):q!=null?q.getJSONObject("item"):r!=null?r:conflictItems.get(id);if(item==null)continue;
   if(r!=null&&r.optBoolean("deleted")&&q==null&&wish==null)continue;
   JSONObject removed=SharedCartLive.clean(item).put("deleted",true);
   intents.put(id,new JSONObject().put("item",removed).put("actorId",actor).put("baseRevision",r==null?0:r.optInt("revision")).put("afterId",q==null?"":q.getString("id")));
  }
  detach(v,ids);v.put("sharedClear",new JSONObject().put("at",Instant.now().toString()).put("actorId",actor).put("count",ids.size()));
  if(intents.length()>1000||v.toString().length()>4000000)throw new IllegalArgumentException("Hay demasiados datos pendientes; espera al envío y vuelve a intentarlo.");
  return SharedCartLive.pump(v);
 }
 static void detach(JSONObject v,Set<String> ids)throws Exception {
  JSONObject owners=v.optJSONObject("sharedCartOwners"),links=v.optJSONObject("cartShares"),active=v.optJSONObject("sharedCartActive");
  for(String id:ids){if(owners!=null)owners.remove(id);if(links!=null)links.remove(id);}
  if(active==null)return;List<String> actors=new ArrayList<>();Iterator<String> a=active.keys();while(a.hasNext())actors.add(a.next());
  for(String actor:actors){boolean remaining=false;if(owners!=null){Iterator<String> keys=owners.keys();while(keys.hasNext()){JSONObject o=owners.getJSONObject(keys.next());if(actor.equals(o.optString("actorId"))&&"cart".equals(o.optString("scope","cart"))){remaining=true;break;}}}if(!remaining)active.put(actor,false);}
 }
 public static JSONObject received(JSONObject original)throws Exception {
  JSONObject v=SharedCartLive.copy(original);Set<String> ids=new HashSet<>();JSONArray rows=v.getJSONArray("shared");JSONObject intents=v.optJSONObject("sharedIntents");
  for(int i=0;i<rows.length();i++){JSONObject r=rows.getJSONObject(i);String id=r.getString("id");if(!r.optBoolean("deleted"))continue;JSONObject wish=intents==null?null:intents.optJSONObject(id),q=SharedCartLive.pending(v,id);if(wish!=null&&!wish.getJSONObject("item").optBoolean("deleted")||q!=null&&!q.getJSONObject("item").optBoolean("deleted"))continue;ids.add(id);}
  detach(v,ids);return v;
 }
}
