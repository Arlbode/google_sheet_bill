package cu.hogarplaya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.List;
import org.json.JSONObject;

/** Solo solicita una posición al confirmar una compra; no rastrea en segundo plano. */
public final class PurchaseLocation {
    private static final ExecutorService GEOCODER=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"purchase-address");t.setDaemon(true);return t;});
    public interface Done {void accept(JSONObject location);}
    public static boolean permitted(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
            ||context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
    public static JSONObject settings(Activity activity,AppStore store) throws Exception {
        return new JSONObject().put("enabled",store.prefs.getBoolean("purchaseLocation",false))
            .put("permitted",permitted(activity));
    }
    public static void enable(Activity activity,AppStore store,boolean enabled) {
        store.prefs.edit().putBoolean("purchaseLocation",enabled).commit();
        if(enabled&&!permitted(activity))activity.runOnUiThread(()->activity.requestPermissions(
            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},5401));
    }
    private static JSONObject empty(String place,String status) {
        try{return new JSONObject().put("place",place).put("status",status);}catch(Exception e){return new JSONObject();}
    }
    private static boolean fresh(Location location) {
        if(location==null)return false;
        long age=(SystemClock.elapsedRealtimeNanos()-location.getElapsedRealtimeNanos())/1000000L;
        return age>=0&&age<=30000&&Double.isFinite(location.getLatitude())&&Double.isFinite(location.getLongitude())&&Math.abs(location.getLatitude())<=90&&Math.abs(location.getLongitude())<=180;
    }
    private static JSONObject value(String place,Location location) {
        try{return new JSONObject().put("place",place).put("status","captured")
            .put("latitude",location.getLatitude()).put("longitude",location.getLongitude())
            .put("accuracy",location.hasAccuracy()?location.getAccuracy():JSONObject.NULL)
            .put("capturedAt",Instant.ofEpochMilli(location.getTime()).toString())
            .put("provider",location.getProvider()).put("mock",location.isFromMockProvider());
        }catch(Exception e){return empty(place,"unavailable");}
    }
    /** Dirección aproximada opcional. Nunca inventa un comercio ni bloquea guardar sin servicio. */
    public static void describe(Context context,JSONObject location) {
        if(!"captured".equals(location.optString("status"))||!location.optString("place").isEmpty()||!Geocoder.isPresent())return;
        Future<String> pending=null;
        try {
            ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm==null||cm.getActiveNetworkInfo()==null||!cm.getActiveNetworkInfo().isConnected())return;
            double lat=location.getDouble("latitude"),lon=location.getDouble("longitude");
            pending=GEOCODER.submit(()->{
                List<Address> addresses=new Geocoder(context,new Locale("es","CU")).getFromLocation(lat,lon,1);
                if(addresses==null||addresses.isEmpty())return "";
                String address=addresses.get(0).getAddressLine(0);return address==null?"":address.trim();
            });
            String address=pending.get(2,TimeUnit.SECONDS);
            if(!address.isEmpty())location.put("place",address.substring(0,Math.min(120,address.length()))).put("placeSource","address");
        }catch(Exception ignored){}finally{if(pending!=null)pending.cancel(true);}
    }
    public static void capture(Activity activity,AppStore store,String rawPlace,Done done) {
        String place=rawPlace.trim();if(place.length()>120)place=place.substring(0,120);
        final String label=place;
        if(!store.prefs.getBoolean("purchaseLocation",false)){done.accept(empty(label,"disabled"));return;}
        if(!permitted(activity)){done.accept(empty(label,"permission_denied"));return;}
        Handler main=new Handler(Looper.getMainLooper());
        activity.runOnUiThread(()->{
            LocationManager manager=(LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
            if(manager==null){done.accept(empty(label,"unavailable"));return;}
            Location best=null;
            for(String provider:new String[]{LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER}) {
                try {if(!manager.isProviderEnabled(provider))continue;Location l=manager.getLastKnownLocation(provider);if(fresh(l)&&(best==null||l.getAccuracy()<best.getAccuracy()))best=l;}catch(Exception ignored){}
            }
            if(best!=null){done.accept(value(label,best));return;}
            AtomicBoolean finished=new AtomicBoolean(false);
            LocationListener listener=new LocationListener(){
                public void onLocationChanged(Location location){if(fresh(location)&&finished.compareAndSet(false,true)){manager.removeUpdates(this);done.accept(value(label,location));}}
                public void onStatusChanged(String p,int s,Bundle b){}
                public void onProviderEnabled(String p){}
                public void onProviderDisabled(String p){}
            };
            boolean requested=false;
            for(String provider:new String[]{LocationManager.GPS_PROVIDER,LocationManager.NETWORK_PROVIDER}) {
                try {if(manager.isProviderEnabled(provider)){manager.requestSingleUpdate(provider,listener,Looper.getMainLooper());requested=true;}}catch(Exception ignored){}
            }
            if(!requested){done.accept(empty(label,"unavailable"));return;}
            main.postDelayed(()->{if(finished.compareAndSet(false,true)){manager.removeUpdates(listener);done.accept(empty(label,"timeout"));}},6000);
        });
    }
}
