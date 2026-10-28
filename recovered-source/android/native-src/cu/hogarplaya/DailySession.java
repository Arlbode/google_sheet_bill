package cu.hogarplaya;

import android.app.Activity;
import android.database.Cursor;
import android.os.SystemClock;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import org.json.JSONObject;

/** Una autenticación por día de La Habana. Nunca guarda el PIN ni sustituye su verificador. */
public final class DailySession {
    private static final String KEY="authenticatedDay";
    private static final ZoneId ZONE=ZoneId.of("America/Havana");
    private static final String LEGACY="lambda$call$5$cu-hogarplaya-MainActivity$Bridge";
    private static Field field(Activity activity,String name)throws Exception {
        Field value=activity.getClass().getDeclaredField(name);value.setAccessible(true);return value;
    }
    public static String active(Activity activity)throws Exception {return (String)field(activity,"active").get(activity);}
    public static String today(){return LocalDate.now(ZONE).toString();}
    private static void setActive(Activity activity,String id)throws Exception {
        field(activity,"active").set(activity,id);field(activity,"touched").setLong(activity,SystemClock.elapsedRealtime());
    }
    private static String pinVersion(AppStore store,String id)throws Exception {
        try(Cursor c=store.getReadableDatabase().rawQuery("SELECT pin_hash,salt FROM profiles WHERE id=?",new String[]{id})) {
            if(!c.moveToFirst())throw new IllegalArgumentException("El perfil ya no existe.");
            byte[] bytes=MessageDigest.getInstance("SHA-256").digest((c.getString(0)+":"+c.getString(1)).getBytes(StandardCharsets.UTF_8));
            StringBuilder result=new StringBuilder();for(byte b:bytes)result.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return result.toString();
        }
    }
    public static void logout(Activity activity)throws Exception {
        setActive(activity,"");
        if(!AppStore.get(activity).prefs.edit().remove(KEY).commit())throw new IllegalStateException("No se pudo guardar el cierre de sesión. Inténtalo de nuevo.");
    }
    public static void prepare(Activity activity)throws Exception {
        AppStore store=AppStore.get(activity);String raw=store.prefs.getString(KEY,"");
        if(raw.isEmpty()){setActive(activity,"");return;}
        JSONObject saved;
        try {
            saved=new JSONObject(raw);
            if(!today().equals(saved.getString("day"))||!pinVersion(store,saved.getString("profileId")).equals(saved.getString("pinVersion"))) {
                logout(activity);return;
            }
        }catch(Exception invalid){logout(activity);return;}
        // Mantener el estado al pasar a segundo plano. requireProfile original sigue exigiendo foreground para operar.
        setActive(activity,saved.getString("profileId"));
    }
    private static void remember(Activity activity,String profile)throws Exception {
        if(profile.isEmpty())return;
        AppStore store=AppStore.get(activity);
        JSONObject value=new JSONObject().put("profileId",profile).put("day",today()).put("pinVersion",pinVersion(store,profile));
        if(!store.prefs.edit().putString(KEY,value.toString()).commit()) {
            setActive(activity,"");throw new IllegalStateException("No se pudo conservar la sesión en el teléfono.");
        }
        setActive(activity,profile);
    }
    public static JSONObject metadata(Activity activity)throws Exception {
        JSONObject result=new JSONObject().put("policy","daily").put("timeZone",ZONE.getId()).put("currentDay",today());
        String raw=AppStore.get(activity).prefs.getString(KEY,"");
        if(!raw.isEmpty()) {
            JSONObject saved=new JSONObject(raw);String day=saved.getString("day");
            result.put("day",day).put("expiresAt",LocalDate.parse(day).plusDays(1).atStartOfDay(ZONE).toInstant().toEpochMilli());
        }
        return result;
    }
    /** Ejecuta el despachador base, sin modificar sus verificaciones ni compilar Java decompilado. */
    public static void legacy(Activity activity,String action,JSONObject data,String request)throws Exception {
        String previous=active(activity);
        if(action.equals("unlock")||action.equals("lock"))logout(activity);
        Class<?> bridge=Class.forName("cu.hogarplaya.MainActivity$Bridge");
        Object instance=bridge.getConstructor(activity.getClass()).newInstance(activity);
        Method method=bridge.getDeclaredMethod(LEGACY,String.class);method.setAccessible(true);
        try{method.invoke(instance,request);}catch(InvocationTargetException e){Throwable cause=e.getCause();if(cause instanceof Exception)throw (Exception)cause;throw e;}
        String current=active(activity);
        if(action.equals("unlock")&&!current.isEmpty()&&current.equals(data.optString("profileId")))remember(activity,current);
        if(action.equals("createProfile")&&!current.isEmpty()&&!current.equals(previous))remember(activity,current);
        if(action.equals("resetPin"))prepare(activity);
        if(action.equals("sync")||action.equals("pair"))TasaloReference.kick(activity);
    }
}
