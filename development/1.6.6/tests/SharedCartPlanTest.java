package cu.hogarplaya;
import org.json.*;
public class SharedCartPlanTest {
 static int checks;static void check(boolean b){if(!b)throw new AssertionError();checks++;}
 interface Call{void go()throws Exception;}
 static void fails(Call c)throws Exception{try{c.go();throw new AssertionError("Aceptado");}catch(IllegalArgumentException e){checks++;}}
 static JSONObject empty()throws Exception{return new JSONObject("{products:[],offers:[],shared:[],queue:[],conflicts:[]}");}
 static JSONArray cart()throws Exception{return new JSONArray("[{id:'line-1',name:'Papa',quantity:'2,5',unitCode:'lb',unitPrice:'450',currency:'CUP'},{id:'line-2',name:'Zanahoria',quantity:1,unitCode:'kg'}]");}
 static JSONObject plan(JSONObject v,JSONArray c)throws Exception{return SharedCartPlan.plan(v,c,"person","device","home");}
 public static void main(String[] args)throws Exception {
  JSONObject v=empty();JSONArray cart=cart();String untouched=v.toString(),untouchedCart=cart.toString();JSONObject first=plan(v,cart);check(v.toString().equals(untouched));check(cart.toString().equals(untouchedCart));check(first.getJSONArray("queue").length()==2);check(first.getJSONArray("queue").getJSONObject(0).getJSONObject("item").getDouble("quantity")==2.5);check(!first.getJSONArray("queue").getJSONObject(0).getJSONObject("item").has("unitPrice"));
  check(plan(first,cart).getJSONArray("queue").length()==2);JSONArray modified=cart();modified.getJSONObject(0).put("quantity",3);fails(()->plan(first,modified));
  JSONObject synced=new JSONObject(first.toString());JSONArray remote=new JSONArray();for(int i=0;i<2;i++)remote.put(new JSONObject(first.getJSONArray("queue").getJSONObject(i).getJSONObject("item").toString()).put("revision",1));synced.put("queue",new JSONArray()).put("shared",remote);check(plan(synced,cart).getJSONArray("queue").length()==0);
  remote.getJSONObject(0).put("bought",true);check(plan(synced,cart).getJSONArray("queue").length()==0);JSONObject changed=plan(synced,modified);check(changed.getJSONArray("queue").length()==1);check(changed.getJSONArray("queue").getJSONObject(0).getInt("baseRevision")==1);check(changed.getJSONArray("queue").getJSONObject(0).getJSONObject("item").getBoolean("bought"));
  remote.getJSONObject(0).put("quantity",10);fails(()->plan(synced,modified));remote.getJSONObject(0).put("quantity",2.5).put("deleted",true);fails(()->plan(synced,modified));
  JSONObject discarded=new JSONObject(first.toString()).put("queue",new JSONArray());check(plan(discarded,cart).getJSONArray("queue").length()==2);
  JSONArray invalid=cart();invalid.getJSONObject(1).put("quantity",-1);fails(()->plan(v,invalid));check(v.toString().equals(untouched));invalid.getJSONObject(1).put("quantity",1).put("unitCode","");fails(()->plan(v,invalid));
  JSONObject full=empty();for(int i=0;i<99;i++)full.getJSONArray("queue").put(new JSONObject().put("entityId","different-"+i));String before=full.toString();fails(()->plan(full,cart));check(full.toString().equals(before));
  fails(()->SharedCartPlan.plan(v,cart,"person","device",""));
  check(!SharedCartPlan.plan(v,cart,"other","device","home").getJSONArray("queue").getJSONObject(0).getString("entityId").equals(first.getJSONArray("queue").getJSONObject(0).getString("entityId")));
  System.out.println("PASS: "+checks+" checks; atomicidad, deduplicación, revisiones, conflictos, límites, unidades y carrito intacto.");
 }
}
