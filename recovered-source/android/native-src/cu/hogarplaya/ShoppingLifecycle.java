package cu.hogarplaya;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;

/** Historial local y reapertura sin borrar compras ni cambiar importes financieros. */
public final class ShoppingLifecycle {
    private static JSONObject session(SQLiteDatabase db, String id) throws Exception {
        try (Cursor c=db.rawQuery("SELECT payload FROM shopping_sessions WHERE id=?",new String[]{id})) {
            if (!c.moveToFirst()) throw new IllegalArgumentException("La compra ya no existe en este teléfono.");
            return new JSONObject(c.getString(0));
        }
    }

    public static void close(AppStore store,String actor,JSONObject request) throws Exception {
        synchronized(store) {
            SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
            try {
                String id=request.getString("sessionId");JSONObject s=session(db,id);
                if(s.optString("closedAt").isEmpty()) {
                    s.put("closedAt",Instant.now().toString()).put("closedBy",actor)
                     .put("closedByName",store.profile(actor).optString("name"));
                    ContentValues values=new ContentValues();
                    // Se libera la clave única del día, conservando ID, fecha y líneas.
                    values.put("day",s.getString("date")+"#closed#"+id);
                    values.put("payload",s.toString());
                    db.update("shopping_sessions",values,"id=?",new String[]{id});
                    JSONObject carry=new JSONObject();
                    if(request.optBoolean("carryPending",true))carry.put("sessionId",id);
                    store.cache("shoppingCarry",(Object)carry);
                }
                db.setTransactionSuccessful();
            } finally {db.endTransaction();}
        }
    }

    public static void start(AppStore store,String actor,JSONObject request) throws Exception {
        synchronized(store) {
            SQLiteDatabase db=store.getWritableDatabase();db.beginTransaction();
            try {
                JSONObject current=Shopping.state(store);
                String requestId=request.getString("requestId");
                UUID.fromString(requestId);
                if(current.optBoolean("active")) {
                    if(!requestId.equals(current.getJSONObject("session").optString("startRequestId")))
                        throw new IllegalArgumentException("Ya tienes una compra abierta. Retómala o ciérrala antes de abrir otra.");
                    Shopping.start(store,request); // Un reintento tampoco puede cambiar el tope.
                    db.setTransactionSuccessful();return;
                }
                Shopping.start(store,request);
                JSONObject created=Shopping.state(store).getJSONObject("session");
                String place=request.optString("place").trim();if(place.length()>120)place=place.substring(0,120);
                created.put("startRequestId",requestId).put("startedAt",Instant.now().toString())
                       .put("startedBy",actor).put("startedByName",store.profile(actor).optString("name")).put("place",place);
                ContentValues values=new ContentValues();values.put("payload",created.toString());
                db.update("shopping_sessions",values,"id=?",new String[]{created.getString("id")});
                JSONObject carry=new JSONObject(store.cache("shoppingCarry","{}"));
                if(request.optBoolean("carryPending",true)&&!carry.optString("sessionId").isEmpty()) {
                    // Copiar solo pendientes, con IDs nuevos. Ningún gasto se vuelve a crear.
                    try(Cursor c=db.rawQuery("SELECT payload FROM shopping_lines WHERE session_id=? AND operation_id='' ORDER BY rowid",new String[]{carry.getString("sessionId")})) {
                        while(c.moveToNext()) {
                            JSONObject line=new JSONObject(c.getString(0));String id=UUID.randomUUID().toString();
                            line.put("id",id);line.remove("operationId");line.remove("bought");line.remove("chargedCents");line.remove("location");
                            ContentValues v=new ContentValues();v.put("id",id);v.put("session_id",created.getString("id"));v.put("payload",line.toString());
                            db.insertOrThrow("shopping_lines",null,v);
                        }
                    }
                }
                store.cache("shoppingCarry",(Object)new JSONObject());
                db.setTransactionSuccessful();
            } finally {db.endTransaction();}
        }
    }

    public static JSONArray history(AppStore store) throws Exception {
        synchronized(store) {
            JSONArray all=Shopping.export(store),result=new JSONArray();ArrayList<JSONObject> closed=new ArrayList<>();
            for(int i=all.length()-1;i>=0;i--) {
                JSONObject s=all.getJSONObject(i);
                if(s.optString("closedAt").isEmpty()&&Money.today().equals(s.optString("date")))continue;
                long spent=0;int pending=0,bought=0;JSONArray lines=s.optJSONArray("lines");
                if(lines==null)lines=new JSONArray();
                for(int j=0;j<lines.length();j++) {
                    JSONObject line=lines.getJSONObject(j);
                    if(line.optBoolean("bought")){spent+=line.optLong("chargedCents");bought++;}else pending++;
                }
                s.put("spent",Money.decimal(spent)).put("boughtCount",bought).put("pendingCount",pending);
                closed.add(s);
            }
            Collections.sort(closed,(a,b)->{
                String x=a.optString("closedAt");if(x.isEmpty())x=a.optString("date");
                String y=b.optString("closedAt");if(y.isEmpty())y=b.optString("date");
                return y.compareTo(x);
            });
            for(JSONObject s:closed)result.put(s);
            return result;
        }
    }
}
