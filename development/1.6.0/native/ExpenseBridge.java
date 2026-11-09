package cu.hogarplaya;
import android.app.Activity;import java.lang.reflect.*;import org.json.*;

/** Despacho autenticado de gastos, sin modificar las clases financieras originales. */
public final class ExpenseBridge {
 static JSONObject state(Activity a)throws Exception {Method m=Class.forName("cu.hogarplaya.ShoppingExtension").getDeclaredMethod("state",Activity.class);m.setAccessible(true);return (JSONObject)m.invoke(null,a);}
 static void done(Activity a,AppStore s,String id,JSONObject result)throws Exception {JSONObject value=state(a);value.put("expenseWarnings",result.optJSONArray("expenseWarnings")).put("savedCount",result.optInt("savedCount")).put("expenseFeatures",HouseholdFeatures.get(s));HouseholdFeatures.reply(a,id,value,null);SyncEngine.changed();SyncEngine.kick(a);}
 public static boolean intercept(Activity a,String text){
  final JSONObject q,d;final String kind,id;
  try{if(text.length()>4000000)return false;q=new JSONObject(text);kind=q.getString("action");id=q.getString("id");d=q.optJSONObject("data")==null?new JSONObject():q.getJSONObject("data");
   if(!((kind.equals("save")||kind.equals("correct"))&&"Gasto".equals(d.optString("type")))&&!kind.equals("cartCheckoutLocated")&&!kind.equals("featuresPurchaseCart")&&!kind.equals("featuresPurchaseShared")&&!kind.equals("shoppingBuyLocated"))return false;
  }catch(Exception e){return false;}
  SyncEngine.WORK.execute(()->{try{
   AppStore s=AppStore.get(a);String actor=HouseholdFeatures.actor(a);
   if(kind.equals("save")||kind.equals("correct")){done(a,s,id,ExpensePurchases.save(s,actor,d,kind.equals("correct")));return;}
   if(!kind.equals("shoppingBuyLocated")){JSONObject prior=ExpensePurchases.prior(s,actor,kind,d);if(prior!=null){done(a,s,id,prior);return;}}
   final String household=s.prefs.getString("householdId",""),session=s.prefs.getString("authenticatedDay",""),endpoint=s.prefs.getString("endpoint","");
   PurchaseLocation.capture(a,s,d.optString("place"),location->SyncEngine.WORK.execute(()->{try{
    if(!actor.equals(HouseholdFeatures.actor(a))||!household.equals(s.prefs.getString("householdId",""))||!session.equals(s.prefs.getString("authenticatedDay",""))||!endpoint.equals(s.prefs.getString("endpoint","")))throw new IllegalArgumentException("Cambió la sesión o el hogar. Revisa la compra con tu perfil.");
    PurchaseLocation.describe(a,location);done(a,s,id,kind.equals("shoppingBuyLocated")?ExpensePurchases.shopping(s,actor,d,location):ExpensePurchases.purchase(s,actor,kind,d,location));
   }catch(Exception e){HouseholdFeatures.reply(a,id,null,e);}}));
  }catch(Exception e){HouseholdFeatures.reply(a,id,null,e);}});return true;
 }
}
