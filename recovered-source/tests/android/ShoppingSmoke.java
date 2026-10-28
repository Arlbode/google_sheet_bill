package cu.hogarplaya.smoke;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.webkit.WebView;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

/** Solo se instala en el emulador de pruebas. Invoca el puente REAL de la app. */
public final class ShoppingSmoke extends Instrumentation {
    private static final String PKG="cu.hogarplaya.recuperada";
    private Activity activity;private WebView web;private Object bridge;private int id=0;private Bundle args;
    public void onCreate(Bundle input){super.onCreate(input);args=input;start();}
    private void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private String eval(String code)throws Exception {
        CountDownLatch latch=new CountDownLatch(1);String[] value={null};
        runOnMainSync(()->web.evaluateJavascript(code,result->{value[0]=result;latch.countDown();}));
        if(!latch.await(15,TimeUnit.SECONDS))throw new AssertionError("WebView no respondió");return value[0];
    }
    private JSONObject call(String action,JSONObject data)throws Exception {
        Bundle progress=new Bundle();progress.putString("stream","RPC "+action+"\n");sendStatus(0,progress);
        String requestId="smoke"+(++id);
        String request=new JSONObject().put("id",requestId).put("action",action).put("data",data).toString();
        bridge.getClass().getMethod("call",String.class).invoke(bridge,request);
        long until=SystemClock.elapsedRealtime()+30000;
        while(SystemClock.elapsedRealtime()<until) {
            String raw=eval("window.__smoke["+JSONObject.quote(requestId)+"]||null");
            if(raw!=null&&!raw.equals("null"))return new JSONObject(raw);
            Thread.sleep(150);
        }
        throw new AssertionError("Sin respuesta de "+action+"; recibos: "+eval("JSON.stringify(Object.keys(window.__smoke))"));
    }
    private JSONObject ok(String action,JSONObject data)throws Exception {
        JSONObject reply=call(action,data);check(reply.optBoolean("ok"),action+": "+reply.optString("error"));return reply.getJSONObject("data");
    }
    private JSONObject line(String name,String category,String price)throws Exception {
        return new JSONObject().put("id",UUID.randomUUID().toString()).put("productId",name).put("name",name).put("category",category)
            .put("quantity","1").put("unitCode","lb").put("unit","Libra").put("unitPrice",price).put("currency","CUP")
            .put("rate","870").put("paymentFormat","Efectivo").put("planId","");
    }
    private JSONObject start(String limit)throws Exception {
        return ok("shoppingStartFresh",new JSONObject().put("requestId",UUID.randomUUID().toString()).put("limit",limit).put("currency","CUP").put("rate","900").put("carryPending",true));
    }
    private void launch()throws Exception {
        activity=startActivitySync(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(PKG,"cu.hogarplaya.MainActivity")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        waitForIdleSync();Field field=activity.getClass().getDeclaredField("web");field.setAccessible(true);web=(WebView)field.get(activity);
        long until=SystemClock.elapsedRealtime()+60000;
        while(SystemClock.elapsedRealtime()<until) {if("true".equals(eval("typeof window.hogarReply==='function'")))break;Thread.sleep(200);}
        check("true".equals(eval("typeof window.hogarReply==='function'")),"La interfaz no abrió");
        eval("window.__smoke={};window.__smokeOriginal=window.hogarReply;window.hogarReply=function(id,r){window.__smoke[id]=r;window.__smokeOriginal(id,r);}");
        Class<?> type=getTargetContext().getClassLoader().loadClass("cu.hogarplaya.MainActivity$Bridge");
        bridge=type.getConstructor(activity.getClass()).newInstance(activity);
        check(!getTargetContext().getClassLoader().loadClass("cu.hogarplaya.BuildFlags").getField("TESTING").getBoolean(null),"Se activó un modo de prueba en el instalador de usuario");
    }
    private JSONObject unlock()throws Exception {
        JSONObject s=ok("state",new JSONObject());String profile=s.getJSONArray("profiles").getJSONObject(0).getString("id");
        return ok("unlock",new JSONObject().put("profileId",profile).put("pin","1234"));
    }
    private void seed()throws Exception {
        JSONObject s=ok("createProfile",new JSONObject().put("name","Prueba Android").put("pin","1234").put("remoteId",""));
        s=ok("shoppingStart",new JSONObject().put("limit","4000").put("currency","CUP").put("rate","870"));
        JSONObject rice=line("Arroz a granel","Canasta básica","370"),sweet=line("Boniato","Complementarios","150");
        ok("shoppingLines",new JSONObject().put("items",new JSONArray().put(rice).put(sweet)));
        s=ok("shoppingBuy",new JSONObject().put("id",rice.getString("id")).put("allowRepeat",true));
        check(s.getJSONArray("operations").length()==1,"No se creó el gasto inicial");
        getTargetContext().getSharedPreferences("smoke-fixture",0).edit().putString("oldSession",s.getJSONObject("shopping").getJSONObject("session").getString("id")).putString("oldOperation",s.getJSONArray("operations").getJSONObject(0).getString("id")).commit();
    }
    private void verify()throws Exception {
        JSONObject s=unlock();String old=getTargetContext().getSharedPreferences("smoke-fixture",0).getString("oldSession","");
        String profile=s.getString("activeProfileId");ok("lock",new JSONObject());
        check(!call("shoppingHistory",new JSONObject()).getBoolean("ok"),"Se accedió al historial con el perfil bloqueado");
        check(!call("unlock",new JSONObject().put("profileId",profile).put("pin","9999")).getBoolean("ok"),"Se aceptó un PIN incorrecto");
        s=unlock();
        check(s.getJSONObject("shopping").getJSONObject("session").getString("id").equals(old),"Se perdió la compra al actualizar");
        check(s.getJSONArray("operations").length()==1,"Se perdieron los gastos al actualizar");
        s=ok("shoppingClose",new JSONObject().put("sessionId",old).put("carryPending",true));
        check(!s.getJSONObject("shopping").getBoolean("active"),"No se cerró la compra");
        check(s.getJSONArray("operations").length()==1,"Cerrar alteró los gastos");
        ok("shoppingClose",new JSONObject().put("sessionId",old).put("carryPending",true));
        JSONArray history=ok("shoppingHistory",new JSONObject()).getJSONArray("sessions");
        check(history.length()==1&&history.getJSONObject(0).getInt("boughtCount")==1&&history.getJSONObject(0).getInt("pendingCount")==1,"Historial incorrecto");
        s=start("1000");JSONObject shopping=s.getJSONObject("shopping"),session=shopping.getJSONObject("session");
        check(!session.getString("id").equals(old)&&session.getString("limit").equals("1000.00"),"No se inició otro tope");
        check(shopping.getJSONArray("lines").length()==1&&!shopping.getJSONArray("lines").getJSONObject(0).getBoolean("bought"),"No se trasladaron solo pendientes");
        String lineId=shopping.getJSONArray("lines").getJSONObject(0).getString("id");
        JSONObject buy=new JSONObject().put("id",lineId).put("sessionId",session.getString("id")).put("place","Mercado de prueba").put("allowRepeat",true);
        s=ok("shoppingBuyLocated",buy);check(s.getJSONArray("operations").length()==2,"Compra sin GPS no guardada");
        JSONObject location=s.getJSONArray("operations").getJSONObject(0).getJSONObject("location");
        check(location.getString("place").equals("Mercado de prueba")&&location.getString("status").equals("disabled"),"Lugar sin GPS incorrecto");
        s=ok("shoppingBuyLocated",buy.put("place","No sobrescribir"));check(s.getJSONArray("operations").length()==2,"Compra duplicada");
        check(s.getJSONArray("operations").getJSONObject(0).getJSONObject("location").getString("place").equals("Mercado de prueba"),"Reintento alteró el lugar");
        JSONObject rejected=call("shoppingStartFresh",new JSONObject().put("requestId",UUID.randomUUID().toString()).put("limit","2000").put("currency","CUP").put("rate","900"));
        check(!rejected.optBoolean("ok"),"Se cambió el tope de una compra abierta");
        ok("shoppingClose",new JSONObject().put("sessionId",session.getString("id")).put("carryPending",false));
        s=start("2000");getTargetContext().getSharedPreferences("smoke-fixture",0).edit().putString("gpsSession",s.getJSONObject("shopping").getJSONObject("session").getString("id")).commit();
    }
    private void gps()throws Exception {
        JSONObject s=unlock();String session=s.getJSONObject("shopping").getJSONObject("session").getString("id");
        ok("shoppingLocationEnable",new JSONObject().put("enabled",true));
        LocationManager lm=(LocationManager)getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        lm.addTestProvider(LocationManager.GPS_PROVIDER,false,false,false,false,true,true,true,Criteria.POWER_LOW,Criteria.ACCURACY_FINE);
        lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER,true);
        Location fix=new Location(LocationManager.GPS_PROVIDER);fix.setLatitude(23.1);fix.setLongitude(-82.4);fix.setAccuracy(5);fix.setTime(System.currentTimeMillis());fix.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());lm.setTestProviderLocation(LocationManager.GPS_PROVIDER,fix);
        Thread.sleep(300);
        JSONObject rice=line("Arroz a granel","Canasta básica","370");ok("shoppingLine",rice);
        s=ok("shoppingBuyLocated",new JSONObject().put("id",rice.getString("id")).put("sessionId",session).put("place","Tienda GPS de prueba").put("allowRepeat",true));
        JSONObject location=s.getJSONArray("operations").getJSONObject(0).getJSONObject("location");
        check(location.getString("status").equals("captured")&&Math.abs(location.getDouble("latitude")-23.1)<0.001,"GPS no capturado");
        check(location.optBoolean("mock"),"No se indicó la ubicación simulada de prueba");
        JSONObject tooMuch=line("Arroz a granel","Canasta básica","1900");ok("shoppingLine",tooMuch);
        check(!call("shoppingBuyLocated",new JSONObject().put("id",tooMuch.getString("id")).put("sessionId",session).put("allowRepeat",true)).getBoolean("ok"),"Se permitió superar el tope");
        lm.removeTestProvider(LocationManager.GPS_PROVIDER);
        check(ok("state",new JSONObject()).getJSONArray("operations").length()==3,"Importes o conteos alterados");
    }
    private void persisted()throws Exception {
        JSONObject s=unlock();check(s.getJSONArray("operations").length()==4,"Gastos no persistieron");
        JSONArray history=ok("shoppingHistory",new JSONObject()).getJSONArray("sessions");
        check(history.length()==2,"Historial no persistió");
        check(history.getJSONObject(0).getString("closedAt").compareTo(history.getJSONObject(1).getString("closedAt"))>=0,"Historial sin ordenar");
        String session=s.getJSONObject("shopping").getJSONObject("session").getString("id");
        JSONObject rice=line("Arroz a granel","Canasta básica","100");ok("shoppingLine",rice);
        s=ok("shoppingBuyLocated",new JSONObject().put("id",rice.getString("id")).put("sessionId",session).put("place","Sin permiso").put("allowRepeat",true));
        check(s.getJSONArray("operations").length()==5,"La falta de permiso bloqueó el gasto");
        check(s.getJSONArray("operations").getJSONObject(0).getJSONObject("location").getString("status").equals("permission_denied"),"Falta estado de permiso denegado");
    }
    private void noGps()throws Exception {
        JSONObject s=unlock();String session=s.getJSONObject("shopping").getJSONObject("session").getString("id");
        JSONObject rice=line("Arroz a granel","Canasta básica","100");ok("shoppingLine",rice);
        s=ok("shoppingBuyLocated",new JSONObject().put("id",rice.getString("id")).put("sessionId",session).put("place","Mercado sin GPS").put("allowRepeat",true));
        check(s.getJSONArray("operations").length()==4,"La falta de GPS bloqueó el gasto");
        check(s.getJSONArray("operations").getJSONObject(0).getJSONObject("location").getString("status").equals("unavailable"),"Se usó una posición con la ubicación del sistema desactivada");
    }
    public void onStart(){Bundle result=new Bundle();try{launch();String phase=args.getString("phase","verify");if(phase.equals("seed"))seed();else if(phase.equals("gps"))gps();else if(phase.equals("nogps"))noGps();else if(phase.equals("persisted"))persisted();else verify();result.putString("stream","ANDROID SMOKE OK: "+phase+"\n");finish(Activity.RESULT_OK,result);}catch(Throwable e){result.putString("stream","ANDROID SMOKE FAILED: "+android.util.Log.getStackTraceString(e));finish(Activity.RESULT_CANCELED,result);}}
}
