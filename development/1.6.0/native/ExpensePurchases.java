package cu.hogarplaya;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.util.*;
import org.json.*;

/** Registra hechos económicos. Las reservas informan; no vetan gastos reales.
 * Reutiliza la normalización, UUID, correcciones y jornadas originales. */
public final class ExpensePurchases {
 static JSONObject copy(JSONObject v)throws Exception{return new JSONObject(v.toString());}
 static boolean same(Object a,Object b)throws Exception {
  if(a==JSONObject.NULL)a=null;if(b==JSONObject.NULL)b=null;if(a==null||b==null)return a==b;
  if(a instanceof JSONObject&&b instanceof JSONObject){JSONObject x=(JSONObject)a,y=(JSONObject)b;if(x.length()!=y.length())return false;Iterator<String> it=x.keys();while(it.hasNext()){String k=it.next();if(!y.has(k)||!same(x.get(k),y.get(k)))return false;}return true;}
  if(a instanceof JSONArray&&b instanceof JSONArray){JSONArray x=(JSONArray)a,y=(JSONArray)b;if(x.length()!=y.length())return false;for(int i=0;i<x.length();i++)if(!same(x.get(i),y.get(i)))return false;return true;}
  if(a instanceof Number&&b instanceof Number)return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()))==0;return a.equals(b);
 }
 static BigDecimal quantity(Object v){BigDecimal q=new BigDecimal(String.valueOf(v).replace(',','.'));if(q.signum()<=0||q.compareTo(new BigDecimal("100000"))>0)throw new IllegalArgumentException("Revisa la cantidad comprada.");return q;}
 static String remainder(JSONObject current,JSONObject bought)throws Exception {
  BigDecimal left=quantity(current.get("quantity")).subtract(quantity(bought.get("quantity")));if(left.signum()<0)throw new IllegalArgumentException("La cantidad comprada supera la pendiente. Revisa el artículo.");return left.stripTrailingZeros().toPlainString();
 }
 static void fund(AppStore s,JSONObject d)throws Exception {
  String name=d.optString("fundCategory");JSONArray names=FinanceReserves.names(s);
  if(!name.isEmpty()&&!FinanceReserves.contains(names,name))throw new IllegalArgumentException("Selecciona un fondo existente.");
  if(!FinanceReserves.fund(d,names).isEmpty()){JSONObject snap=s.snapshot();if(!snap.optString("householdId").isEmpty()&&snap.optInt("reserveVersion")!=1)throw new IllegalArgumentException("Actualiza el script y sincroniza los fondos antes de asociar el gasto.");}
 }
 static JSONArray warnings(AppStore s,JSONArray expenses,String except)throws Exception {
  JSONArray out=new JSONArray(),names=FinanceReserves.names(s),rows=FinanceReserves.rows(s,except);Map<String,Long> amounts=new LinkedHashMap<>(),fundAmounts=new LinkedHashMap<>(),freeAmounts=new LinkedHashMap<>();
  for(int i=0;i<expenses.length();i++){JSONObject op=expenses.getJSONObject(i);String key=FinanceReserves.bucket(op),name=FinanceReserves.fund(op,names);long a=Money.cents(op.getString("amount"));amounts.put(key,amounts.getOrDefault(key,0L)+a);if(!name.isEmpty()){String k=name+"|"+key;fundAmounts.put(k,fundAmounts.getOrDefault(k,0L)+a);}else freeAmounts.put(key,freeAmounts.getOrDefault(key,0L)+a);}
  for(String key:amounts.keySet()){long physical=FinanceReserves.physical(s,key,except),a=amounts.get(key);String label=key.equals("usd")?"USD":key.equals("cupTransfer")?"CUP transferencia":"CUP efectivo";
   if(physical==Long.MAX_VALUE)out.put(label+": saldo sin confirmar. El gasto se registra igualmente.");
   else if(a>physical)out.put(label+": el gasto supera el saldo registrado. Revisa ingresos o saldos pendientes.");
   else if(freeAmounts.getOrDefault(key,0L)>physical-FinanceReserves.reserved(rows,names,key))out.put(label+": el gasto reduce dinero que figura reservado. Revisa tus reservas después de registrarlo.");
  }
  for(String k:fundAmounts.keySet()){int at=k.lastIndexOf('|');String name=k.substring(0,at),key=k.substring(at+1);if(fundAmounts.get(k)>FinanceReserves.balance(rows,names,name,key))out.put(name+": el gasto supera lo reservado en esa moneda y formato; el fondo puede quedar negativo.");}
  return out;
 }
 static void update(SQLiteDatabase db,JSONObject op)throws Exception {ContentValues v=new ContentValues();v.put("payload",op.toString());db.update("operations",v,"id=?",new String[]{op.getString("id")});}
 public static JSONObject save(AppStore s,String actor,JSONObject d,boolean correct)throws Exception {
  synchronized(s){SQLiteDatabase db=s.getWritableDatabase();db.beginTransaction();try{
   if(!"Gasto".equals(d.optString("type")))throw new IllegalArgumentException("Esta acción solo registra gastos.");
   fund(s,d);JSONObject normalized=s.operation(actor,copy(d)).put("fundCategory",d.optString("fundCategory")),old=FinanceReserves.operation(s,d.getString("id"));
   JSONArray notices=warnings(s,new JSONArray().put(normalized),old==null?"":d.getString("id"));
   if(!correct&&old!=null&&!FinanceReserves.fund(old,FinanceReserves.names(s)).equals(FinanceReserves.fund(d,FinanceReserves.names(s))))throw new IllegalArgumentException("Este intento ya se guardó con otro fondo.");
   if(correct)s.correctRejected(actor,d.getString("id"),copy(d));else s.saveOperation(actor,copy(d));
   // Un reintento no vuelve a anotar ni cambia el asiento ya enviado.
   if(correct||old==null){FinanceReserves.annotate(s,d.getString("id"),d);if(correct&&old!=null&&old.has("location")){JSONObject op=FinanceReserves.operation(s,d.getString("id"));op.put("location",old.get("location"));update(db,op);}}
   db.setTransactionSuccessful();return new JSONObject().put("savedCount",1).put("expenseWarnings",notices);
  }finally{db.endTransaction();}}
 }
 static String key(String kind,JSONObject d)throws Exception{return (kind.equals("cartCheckoutLocated")?"cartCheckout:":"expensePurchase174:")+UUID.fromString(d.getString("requestId"));}
 static JSONObject prior(AppStore s,String actor,String kind,JSONObject d)throws Exception {
  JSONObject old=new JSONObject(s.cache(key(kind,d),"{}"));if(old.length()==0)return null;
  if(!actor.equals(old.optString("actorId")))throw new IllegalArgumentException("Esta confirmación pertenece a otro perfil.");
  if(old.has("request")&&(!kind.equals(old.optString("kind"))||!same(old.getJSONObject("request"),d)))throw new IllegalArgumentException("Este intento ya se guardó con otros datos. Revisa Movimientos.");
  return new JSONObject().put("savedCount",old.getInt("savedCount")).put("expenseWarnings",old.optJSONArray("expenseWarnings")==null?new JSONArray():old.getJSONArray("expenseWarnings"));
 }
 static JSONObject sharedCurrent(JSONObject v,String id)throws Exception {
  if(SharedCartLive.conflict(v,id))throw new IllegalArgumentException("Este artículo tiene un conflicto. Revísalo en la cesta compartida antes de registrar su compra.");
  JSONObject r=SharedCartLive.remote(v,id),q=SharedCartLive.pending(v,id),intents=v.optJSONObject("sharedIntents"),wish=intents==null?null:intents.optJSONObject(id);
  JSONObject item=wish!=null?wish.getJSONObject("item"):q!=null?q.getJSONObject("item"):r;
  if(item==null||item.optBoolean("deleted")||item.optBoolean("bought"))throw new IllegalArgumentException("El artículo ya se compró o retiró. Actualiza la cesta compartida.");
  return copy(item).put("revision",r==null?0:r.optInt("revision")).put("pendingChangeId",q!=null?q.getString("id"):wish==null?"":wish.optString("afterId"));
 }
 static JSONObject sharedBase(AppStore s,String actor,JSONObject v,JSONObject d)throws Exception {
  JSONObject item=sharedCurrent(v,d.getJSONObject("sharedItem").getString("id")),expected=d.getJSONObject("sharedItem");
  if(!SharedCartLive.same(SharedCartLive.clean(item),SharedCartLive.clean(expected))||item.optInt("revision")!=expected.optInt("revision")||!item.optString("pendingChangeId").equals(expected.optString("pendingChangeId")))throw new IllegalArgumentException("El artículo compartido cambió. Revisa su cantidad antes de confirmar.");
  JSONObject line=copy(d.getJSONObject("line"));if(!item.getString("name").equals(line.getString("name"))||!item.getString("unit").equals(line.getString("unitCode")))throw new IllegalArgumentException("Revisa el nombre y la unidad del artículo compartido.");
  remainder(item,line);return item;
 }
 static JSONArray expenseOps(AppStore s,String actor,JSONArray lines,JSONObject d)throws Exception {
  JSONArray ops=new JSONArray();for(int i=0;i<lines.length();i++){JSONObject l=lines.getJSONObject(i);JSONObject op=s.operation(actor,new JSONObject().put("type","Gasto").put("category",l.getString("category")).put("date",d.getString("date")).put("currency",l.getString("currency")).put("rate",l.optString("rate")).put("paymentFormat",l.optString("paymentFormat","Efectivo")).put("planId",l.optString("planId")).put("concept","Compra: "+l.getString("name")).put("amount",Money.decimal(Money.lineTotal(l.getString("quantity"),l.getString("unitPrice")))).put("items",new JSONArray().put(l)));op.put("fundCategory",d.optString("fundCategory"));fund(s,op);ops.put(op);}return ops;
 }
 public static JSONObject purchase(AppStore s,String actor,String kind,JSONObject d,JSONObject location)throws Exception {
  synchronized(s){SQLiteDatabase db=s.getWritableDatabase();db.beginTransaction();try{
   JSONObject old=prior(s,actor,kind,d);if(old!=null){db.setTransactionSuccessful();return old;}
   JSONArray cart=s.cart(actor),selected=new JSONArray(),remaining=new JSONArray();JSONObject features=HouseholdFeatures.get(s),shared=null,modeLine=null,modeRemainder=null;
   if(kind.equals("featuresPurchaseShared")){
    shared=sharedBase(s,actor,features,d);JSONObject line=copy(d.getJSONObject("line"));String warning=Shopping.repeatWarning(s,line);if(!warning.isEmpty()&&!d.optBoolean("allowRepeat"))throw new IllegalArgumentException("REPETIDA: "+warning);selected.put(line);
    JSONObject owners=SharedCartLive.object(features,"sharedCartOwners"),owner=owners.optJSONObject(shared.getString("id"));
    if(owner!=null&&actor.equals(owner.optString("actorId"))&&"shopping".equals(owner.optString("scope"))){
     JSONObject mode=Shopping.state(s);JSONArray modes=mode.optJSONArray("lines");if(modes!=null)for(int i=0;i<modes.length();i++)if(owner.optString("lineId").equals(modes.getJSONObject(i).optString("id")))modeLine=copy(modes.getJSONObject(i));
     if(modeLine==null||modeLine.optBoolean("bought"))throw new IllegalArgumentException("Revisa este artículo en su Modo compra: la jornada cambió o ya se compró.");
     if(!mode.optBoolean("active")||!d.getString("date").equals(mode.getJSONObject("session").getString("date")))throw new IllegalArgumentException("Esta compra pertenece a la fecha de su Modo compra. Revisa la fecha antes de registrar.");
     if(!SharedCartPlan.same(SharedCartPlan.item(modeLine),SharedCartLive.clean(shared)))throw new IllegalArgumentException("La cantidad del Modo compra cambió. Revisa la cesta compartida.");
     JSONObject bought=copy(modeLine);for(String k:new String[]{"quantity","unitPrice","currency","rate","paymentFormat","planId"})bought.put(k,line.get(k));selected=new JSONArray().put(bought);
     String left=remainder(modeLine,bought);if(!left.equals("0")){modeRemainder=copy(modeLine).put("id",UUID.randomUUID().toString()).put("quantity",left);for(String k:new String[]{"bought","operationId","chargedCents"})modeRemainder.remove(k);}
    }
    for(int i=0;i<cart.length();i++){JSONObject c=cart.getJSONObject(i);if(owner!=null&&actor.equals(owner.optString("actorId"))&&"cart".equals(owner.optString("scope"))&&c.getString("id").equals(owner.optString("lineId"))){
      if(!SharedCartPlan.same(SharedCartPlan.item(c),SharedCartLive.clean(shared)))throw new IllegalArgumentException("Tu carrito y el artículo compartido tienen cantidades distintas. Revisa ambos antes de registrar.");String left=remainder(c,line);if(!left.equals("0"))remaining.put(copy(c).put("quantity",left));
     }else remaining.put(c);}
   }else{
    if(!same(cart,d.getJSONArray("items")))throw new IllegalArgumentException("Tu cesta cambió. Ábrela otra vez y revisa la compra.");
    if(kind.equals("featuresPurchaseCart")){
     String id=d.getString("lineId");JSONObject line=d.getJSONObject("line");boolean found=false;
     for(int i=0;i<cart.length();i++){JSONObject c=cart.getJSONObject(i);if(c.getString("id").equals(id)){found=true;JSONObject bought=copy(c);for(String k:new String[]{"quantity","unitPrice","currency","rate","paymentFormat","planId"})bought.put(k,line.get(k));String left=remainder(c,bought);selected.put(bought);if(!left.equals("0"))remaining.put(copy(c).put("quantity",left));}else remaining.put(c);}
     if(!found)throw new IllegalArgumentException("El artículo ya no está en tu cesta.");
    }else selected=new JSONArray(cart.toString());
   }
   if(selected.length()==0)throw new IllegalArgumentException("No hay artículos pendientes para registrar.");
   JSONArray notices=warnings(s,expenseOps(s,actor,selected,d),"");Set<String> before=new HashSet<>();try(Cursor c=db.rawQuery("SELECT id FROM operations",null)){while(c.moveToNext())before.add(c.getString(0));}
   int count;
   if(shared==null){s.saveCart(actor,selected);count=s.checkout(actor,d.getString("date"),d.optBoolean("allowRepeat"));}
   else if(modeLine!=null){Shopping.saveLine(s,selected.getJSONObject(0));Shopping.buy(s,actor,modeLine.getString("id"),d.optBoolean("allowRepeat"));if(modeRemainder!=null)Shopping.saveLine(s,modeRemainder);count=1;}
   else {JSONObject op=expenseOps(s,actor,selected,d).getJSONObject(0);op.put("batchId",d.getString("requestId"));s.insert(db,op);count=1;}
   s.saveCart(actor,remaining);
   try(Cursor c=db.rawQuery("SELECT id,payload FROM operations",null)){while(c.moveToNext())if(!before.contains(c.getString(0))){JSONObject op=new JSONObject(c.getString(1));op.put("fundCategory",d.optString("fundCategory")).put("location",location);op.getJSONArray("items").getJSONObject(0).put("location",location);update(db,op);}}
   if(shared==null)features=SharedCartLive.changedCart(features,remaining,Shopping.state(s),actor,"purchased",s.deviceId,s.prefs.getString("householdId",""));
   else {
    JSONObject item=SharedCartLive.clean(shared),line=selected.getJSONObject(0);String left=remainder(shared,line);item.put("bought",left.equals("0"));if(!left.equals("0"))item.put("quantity",Double.parseDouble(left));
    features=SharedCartLive.change(features,item,actor,shared.optInt("revision"),shared.optString("pendingChangeId"));
    JSONObject owners=SharedCartLive.object(features,"sharedCartOwners"),owner=owners.optJSONObject(shared.getString("id"));if(owner!=null&&actor.equals(owner.optString("actorId"))){
     if(modeLine!=null){if(modeRemainder==null)owners.remove(shared.getString("id"));else owner.put("lineId",modeRemainder.getString("id")).put("lastLine",SharedCartPlan.item(modeRemainder));}
     else if("cart".equals(owner.optString("scope"))){JSONObject rest=remainingLine(remaining,owner.optString("lineId"));if(left.equals("0")||rest==null)owners.remove(shared.getString("id"));else owner.put("lastLine",SharedCartPlan.item(rest));}
    }
    if(remaining.length()==0)SharedCartLive.object(features,"sharedCartActive").put(actor,false);
   }
   HouseholdFeatures.store(s,features);
   JSONObject result=new JSONObject().put("savedCount",count).put("expenseWarnings",notices);
   s.cache(key(kind,d),copy(result).put("actorId",actor).put("kind",kind).put("request",copy(d)));db.setTransactionSuccessful();return result;
  }finally{db.endTransaction();}}
 }
 static JSONObject remainingLine(JSONArray lines,String id)throws Exception{for(int i=0;i<lines.length();i++)if(id.equals(lines.getJSONObject(i).optString("id")))return lines.getJSONObject(i);return null;}
 public static JSONObject shopping(AppStore s,String actor,JSONObject d,JSONObject location)throws Exception {
  synchronized(s){SQLiteDatabase db=s.getWritableDatabase();db.beginTransaction();try{
   JSONObject mode=Shopping.state(s);if(!mode.optBoolean("active")||!d.getString("sessionId").equals(mode.getJSONObject("session").getString("id")))throw new IllegalArgumentException("Esta compra ya se cerró. Revisa tu lista actual.");
   Set<String> before=new HashSet<>();try(Cursor c=db.rawQuery("SELECT id FROM operations",null)){while(c.moveToNext())before.add(c.getString(0));}
   // La jornada original conserva su tope, su gasto consumido y su recibo por línea.
   Shopping.buy(s,actor,d.getString("id"),d.optBoolean("allowRepeat"));JSONArray notices=new JSONArray();int count=0;
   try(Cursor c=db.rawQuery("SELECT id,payload FROM operations",null)){while(c.moveToNext())if(!before.contains(c.getString(0))){JSONObject op=new JSONObject(c.getString(1));op.put("fundCategory",d.optString("fundCategory"));fund(s,op);JSONArray a=warnings(s,new JSONArray().put(op),op.getString("id"));for(int i=0;i<a.length();i++)notices.put(a.get(i));op.put("location",location);op.getJSONArray("items").getJSONObject(0).put("location",location);update(db,op);count++;}}
   JSONObject features=SharedCartLive.changedCart(HouseholdFeatures.get(s),s.cart(actor),Shopping.state(s),actor,"shopping",s.deviceId,s.prefs.getString("householdId",""));HouseholdFeatures.store(s,features);db.setTransactionSuccessful();return new JSONObject().put("savedCount",count).put("expenseWarnings",notices);
  }finally{db.endTransaction();}}
 }
}
