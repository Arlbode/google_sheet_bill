package cu.hogarplaya;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.webkit.WebView;
import android.net.Uri;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.json.JSONObject;

/** Puente acotado: los comandos anteriores siguen en el puente original. */
public final class ShoppingExtension {
    private static Object invoke(Activity activity,String name) throws Exception {
        Method method=activity.getClass().getDeclaredMethod(name);method.setAccessible(true);
        try{return method.invoke(activity);}catch(InvocationTargetException e){Throwable cause=e.getCause();if(cause instanceof Exception)throw (Exception)cause;throw e;}
    }
    private static String actor(Activity activity) throws Exception {
        invoke(activity,"requireProfile");Field field=activity.getClass().getDeclaredField("active");field.setAccessible(true);return (String)field.get(activity);
    }
    private static JSONObject state(Activity activity) throws Exception {
        JSONObject result=(JSONObject)invoke(activity,"state");
        if(!result.optString("activeProfileId").isEmpty())result.put("shoppingLocation",PurchaseLocation.settings(activity,AppStore.get(activity)));
        return result;
    }
    private static void reply(Activity activity,String id,JSONObject data,Exception error) {
        try {
            JSONObject result=new JSONObject().put("ok",error==null);
            if(error==null)result.put("data",data);else result.put("error",error.getMessage()==null?"No se pudo completar la acción.":error.getMessage());
            String script="window.hogarReply("+JSONObject.quote(id)+","+result.toString()+")";
            activity.runOnUiThread(()->{try{Field field=activity.getClass().getDeclaredField("web");field.setAccessible(true);WebView web=(WebView)field.get(activity);if(web!=null)web.evaluateJavascript(script,null);}catch(Exception ignored){}});
        }catch(Exception ignored){}
    }
    private static JSONObject operation(SQLiteDatabase db,String id) throws Exception {
        try(Cursor c=db.rawQuery("SELECT payload FROM operations WHERE id=?",new String[]{id})) {
            return c.moveToFirst()?new JSONObject(c.getString(0)):null;
        }
    }
    private static void buy(AppStore store,String actor,JSONObject data,JSONObject location) throws Exception {
        synchronized(store) {
            SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
            try {
                JSONObject current=Shopping.state(store);
                if(!current.optBoolean("active")||!data.getString("sessionId").equals(current.getJSONObject("session").getString("id")))
                    throw new IllegalArgumentException("Esta compra ya se cerró. Revisa tu lista actual.");
                String lineId=data.getString("id"),opId;JSONObject line;
                try(Cursor c=db.rawQuery("SELECT payload,operation_id FROM shopping_lines WHERE id=? AND session_id=?",new String[]{lineId,data.getString("sessionId")})) {
                    if(!c.moveToFirst())throw new IllegalArgumentException("El producto ya no está en esta compra.");
                    line=new JSONObject(c.getString(0));opId=c.getString(1);
                }
                if(opId.isEmpty()) {
                    // La ubicación viaja también en el artículo para conservarla en la auditoría.
                    line.put("location",location);ContentValues v=new ContentValues();v.put("payload",line.toString());
                    db.update("shopping_lines",v,"id=?",new String[]{lineId});
                    Shopping.buy(store,actor,lineId,data.optBoolean("allowRepeat"));
                    try(Cursor c=db.rawQuery("SELECT operation_id FROM shopping_lines WHERE id=?",new String[]{lineId})) {c.moveToFirst();opId=c.getString(0);}
                    JSONObject op=operation(db,opId);op.put("location",location);
                    v=new ContentValues();v.put("payload",op.toString());db.update("operations",v,"id=?",new String[]{opId});
                }
                db.setTransactionSuccessful();
            }finally{db.endTransaction();}
        }
    }
    public static boolean intercept(Activity activity,String text) {
        final JSONObject request;final String action,id;
        try {
            if(text.length()>220000)return false;
            request=new JSONObject(text);action=request.getString("action");id=request.getString("id");
            if(action.equals("correct")&&(request.optJSONObject("data")==null||!"Gasto".equals(request.getJSONObject("data").optString("type"))))return false;
            if(!action.equals("state")&&!action.equals("shoppingClose")&&!action.equals("shoppingStartFresh")
                &&!action.equals("shoppingHistory")&&!action.equals("shoppingBuyLocated")&&!action.equals("shoppingLocationEnable")
                &&!action.equals("correct")&&!action.equals("cartCheckoutLocated")&&!action.equals("purchaseMap"))return false;
        }catch(Exception e){return false;}
        SyncEngine.WORK.execute(()->{
            try {
                JSONObject data=request.optJSONObject("data");if(data==null)data=new JSONObject();
                AppStore store=AppStore.get(activity);
                if(action.equals("state")){reply(activity,id,state(activity),null);return;}
                String actor=actor(activity);
                if(action.equals("purchaseMap")) {
                    Uri[] uris=PurchaseActions.mapUris(data);
                    activity.runOnUiThread(()->{
                        try{PurchaseActions.openMap(activity,uris);reply(activity,id,new JSONObject().put("opened",true),null);}
                        catch(Exception e){reply(activity,id,null,e);}
                    });return;
                }
                if(action.equals("cartCheckoutLocated")) {
                    int prior=PurchaseActions.saved(store,actor,data.getString("requestId"));
                    if(prior>=0){reply(activity,id,state(activity).put("savedCount",prior),null);return;}
                    String expected=PurchaseActions.cartSnapshot(store,actor,data);final JSONObject payload=data;
                    PurchaseLocation.capture(activity,store,data.optString("place"),location->SyncEngine.WORK.execute(()->{
                        try {
                            String currentActor=actor(activity);
                            if(!actor.equals(currentActor))throw new IllegalArgumentException("Cambió el perfil. Revisa la cesta y confirma con tu perfil.");
                            PurchaseLocation.describe(activity,location);
                            int count=PurchaseActions.checkout(store,currentActor,payload,expected,location);
                            reply(activity,id,state(activity).put("savedCount",count),null);SyncEngine.kick(activity);
                        }catch(Exception e){reply(activity,id,null,e);}
                    }));return;
                }
                if(action.equals("shoppingHistory")){reply(activity,id,new JSONObject().put("sessions",ShoppingLifecycle.history(store)),null);return;}
                if(action.equals("shoppingClose"))ShoppingLifecycle.close(store,actor,data);
                else if(action.equals("shoppingStartFresh"))ShoppingLifecycle.start(store,actor,data);
                else if(action.equals("shoppingLocationEnable"))PurchaseLocation.enable(activity,store,data.optBoolean("enabled"));
                else if(action.equals("correct")) {
                    synchronized(store) {
                        SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
                        try {
                            JSONObject old=operation(db,data.getString("id"));
                            store.correctRejected(actor,data.getString("id"),data);
                            if(old!=null&&old.has("location")) {
                                JSONObject saved=operation(db,data.getString("id"));saved.put("location",old.get("location"));
                                ContentValues values=new ContentValues();values.put("payload",saved.toString());db.update("operations",values,"id=?",new String[]{data.getString("id")});
                            }
                            db.setTransactionSuccessful();
                        }finally{db.endTransaction();}
                    }
                    SyncEngine.kick(activity);
                } else if(action.equals("shoppingBuyLocated")) {
                    final JSONObject payload=data;
                    PurchaseLocation.capture(activity,store,data.optString("place"),location->SyncEngine.WORK.execute(()->{
                        try{String current=actor(activity);if(!actor.equals(current))throw new IllegalArgumentException("Cambió el perfil. Confirma la compra con tu perfil.");PurchaseLocation.describe(activity,location);buy(store,current,payload,location);reply(activity,id,state(activity),null);SyncEngine.kick(activity);}
                        catch(Exception e){reply(activity,id,null,e);}
                    }));return;
                }
                reply(activity,id,state(activity),null);
            }catch(Exception e){reply(activity,id,null,e);}
        });
        return true;
    }
}
