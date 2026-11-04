package cu.hogarplaya;

import java.time.Instant;
import java.util.HashMap;
import org.json.*;

/** Aplica recibos a la cola ACTUAL, sin sustituir cambios hechos durante la petición. */
public final class SharedSyncBatch {
    public static JSONObject apply(JSONObject current,JSONArray batch,JSONObject response,String household)throws Exception {
        if(!response.optBoolean("ok")||!household.equals(response.optString("householdId")))
            throw new IllegalArgumentException("El Sheet no confirmó la lista. Instala el script 1.6.0 y conserva la misma URL /exec.");
        JSONArray acks=response.getJSONArray("featureAcks"),items=response.getJSONArray("shared");
        if(items.length()>500||items.toString().length()>500000||acks.length()>batch.length())
            throw new IllegalArgumentException("Respuesta de lista demasiado grande; se conserva la cola.");
        HashMap<String,JSONObject> sent=new HashMap<>(),receipts=new HashMap<>();
        for(int i=0;i<batch.length();i++){JSONObject op=batch.getJSONObject(i);sent.put(op.getString("id"),op);}
        for(int i=0;i<acks.length();i++){
            JSONObject ack=acks.getJSONObject(i);String id=ack.getString("id");
            if(!sent.containsKey(id)||receipts.put(id,ack)!=null)
                throw new IllegalArgumentException("Confirmación desconocida o repetida; se conserva la cola.");
        }
        JSONObject v=new JSONObject(current.toString());JSONArray queue=v.getJSONArray("queue"),remain=new JSONArray(),conflicts=v.getJSONArray("conflicts");
        for(int i=0;i<queue.length();i++){
            JSONObject op=queue.getJSONObject(i),ack=receipts.get(op.getString("id"));
            if(ack==null||!java.util.Arrays.asList("accepted","duplicate","conflict").contains(ack.optString("status"))){remain.put(op);continue;}
            // Los UUID enviados son inmutables. Ante una anomalía no se descarta ninguna operación.
            if(!op.toString().equals(sent.get(op.getString("id")).toString()))
                throw new IllegalArgumentException("El cambio enviado ya no coincide; se conserva la cola.");
            if(ack.optString("status").equals("conflict")){
                boolean exists=false;for(int j=0;j<conflicts.length();j++)if(op.getString("id").equals(conflicts.getJSONObject(j).getJSONObject("local").optString("id")))exists=true;
                if(!exists)conflicts.put(new JSONObject().put("local",op).put("remote",ack.optJSONObject("current")));
            }
        }
        v.put("queue",remain).put("shared",new JSONArray(items.toString())).put("conflicts",conflicts).put("lastSync",Instant.now().toString());v.remove("syncError");
        return SharedCartLive.acknowledged(v,batch,acks);
    }
}
