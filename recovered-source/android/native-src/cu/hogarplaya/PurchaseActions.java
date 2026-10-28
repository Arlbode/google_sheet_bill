package cu.hogarplaya;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Iterator;
import java.math.BigDecimal;
import org.json.JSONArray;
import org.json.JSONObject;

/** Compra de cesta y apertura de mapas; conserva las validaciones financieras originales. */
public final class PurchaseActions {
    private static boolean sameJson(Object a,Object b)throws Exception {
        if(a==JSONObject.NULL)a=null;if(b==JSONObject.NULL)b=null;
        if(a==null||b==null)return a==b;
        if(a instanceof JSONObject&&b instanceof JSONObject) {
            JSONObject x=(JSONObject)a,y=(JSONObject)b;if(x.length()!=y.length())return false;
            Iterator<String> keys=x.keys();while(keys.hasNext()){String key=keys.next();if(!y.has(key)||!sameJson(x.get(key),y.get(key)))return false;}return true;
        }
        if(a instanceof JSONArray&&b instanceof JSONArray) {
            JSONArray x=(JSONArray)a,y=(JSONArray)b;if(x.length()!=y.length())return false;
            for(int i=0;i<x.length();i++)if(!sameJson(x.get(i),y.get(i)))return false;return true;
        }
        if(a instanceof Number&&b instanceof Number)return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString()))==0;
        return a.equals(b);
    }
    public static String receiptKey(String requestId) {
        return "cartCheckout:"+UUID.fromString(requestId).toString();
    }
    public static int saved(AppStore store,String actor,String requestId)throws Exception {
        JSONObject receipt=new JSONObject(store.cache(receiptKey(requestId),"{}"));
        if(receipt.length()==0)return -1;
        if(!actor.equals(receipt.getString("actorId")))throw new IllegalArgumentException("Esta confirmación pertenece a otro perfil.");
        return receipt.getInt("savedCount");
    }
    public static String cartSnapshot(AppStore store,String actor,JSONObject request)throws Exception {
        JSONArray current=store.cart(actor),expected=request.getJSONArray("items");
        // JSON puede reordenar claves al cruzar WebView; los valores y el orden de artículos sí deben coincidir.
        if(!sameJson(current,expected))throw new IllegalArgumentException("Tu cesta cambió. Ábrela otra vez y revisa el total antes de confirmar.");
        return current.toString();
    }
    public static int checkout(AppStore store,String actor,JSONObject request,String expected,JSONObject location)throws Exception {
        synchronized(store) {
            SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
            try {
                int prior=saved(store,actor,request.getString("requestId"));
                if(prior>=0){db.setTransactionSuccessful();return prior;}
                if(!sameJson(store.cart(actor),new JSONArray(expected)))throw new IllegalArgumentException("Tu cesta cambió mientras se obtenía la ubicación. Revisa el total y confirma de nuevo.");
                Set<String> before=new HashSet<>();
                try(Cursor c=db.rawQuery("SELECT id FROM operations",null)){while(c.moveToNext())before.add(c.getString(0));}
                // El código original controla unidades, importes, perfiles, planes, repetición y Modo compra activo.
                int count=store.checkout(actor,request.getString("date"),request.optBoolean("allowRepeat"));
                try(Cursor c=db.rawQuery("SELECT id,payload FROM operations",null)) {
                    while(c.moveToNext())if(!before.contains(c.getString(0))) {
                        JSONObject op=new JSONObject(c.getString(1));op.put("location",location);
                        // El lugar es común al grupo. Una copia en el primer artículo permite auditarlo
                        // sin multiplicar coordenadas en listas grandes ni agotar el límite del backend.
                        JSONArray items=op.getJSONArray("items");items.getJSONObject(0).put("location",location);
                        ContentValues v=new ContentValues();v.put("payload",op.toString());db.update("operations",v,"id=?",new String[]{c.getString(0)});
                    }
                }
                store.cache(receiptKey(request.getString("requestId")),new JSONObject().put("actorId",actor).put("savedCount",count));
                db.setTransactionSuccessful();return count;
            }finally{db.endTransaction();}
        }
    }
    public static Uri[] mapUris(JSONObject data)throws Exception {
        String place=data.optString("place").trim();if(place.length()>120)place=place.substring(0,120);
        boolean has=data.has("latitude")||data.has("longitude");
        if(has) {
            double lat=data.getDouble("latitude"),lon=data.getDouble("longitude");
            if(!Double.isFinite(lat)||!Double.isFinite(lon)||Math.abs(lat)>90||Math.abs(lon)>180)throw new IllegalArgumentException("Las coordenadas guardadas no son válidas.");
            String point=Double.toString(lat)+","+Double.toString(lon);
            return new Uri[]{Uri.parse("geo:"+point+"?q="+Uri.encode(point+(place.isEmpty()?"":" ("+place+")"))),
                Uri.parse("https://www.openstreetmap.org/?mlat="+lat+"&mlon="+lon+"#map=18/"+lat+"/"+lon)};
        }
        if(place.isEmpty())throw new IllegalArgumentException("Esta compra no tiene coordenadas ni lugar indicado.");
        String query=Uri.encode(place+", Cuba");
        return new Uri[]{Uri.parse("geo:0,0?q="+query),Uri.parse("https://www.openstreetmap.org/search?query="+query)};
    }
    public static void openMap(Activity activity,Uri[] uris) {
        try{activity.startActivity(new Intent(Intent.ACTION_VIEW,uris[0]));}
        catch(ActivityNotFoundException e){
            try{activity.startActivity(new Intent(Intent.ACTION_VIEW,uris[1]));}
            catch(ActivityNotFoundException none){throw new IllegalArgumentException("Instala o habilita una aplicación de mapas o un navegador para abrir esta ubicación.");}
        }
    }
}
