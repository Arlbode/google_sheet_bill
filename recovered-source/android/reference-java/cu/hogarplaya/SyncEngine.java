package cu.hogarplaya;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class SyncEngine {
    public static volatile Runnable listener;
    private static final AtomicBoolean running = new AtomicBoolean(false);
    public static final ExecutorService WORK = Executors.newSingleThreadExecutor();
    public static final ExecutorService NETWORK = Executors.newSingleThreadExecutor();

    public static void changed() {
        Runnable runnable = listener;
        if (runnable != null) {
            runnable.run();
        }
    }

    public static boolean isRunning() {
        return running.get();
    }

    public static void kick(Context context) {
        final Context applicationContext = context.getApplicationContext();
        schedule(applicationContext);
        NETWORK.execute(new Runnable() { // from class: cu.hogarplaya.SyncEngine$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                SyncEngine.sync(applicationContext);
            }
        });
    }

    public static boolean online(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static synchronized JSONObject pair(Context context, String str, String str2, String str3) throws Exception {
        JSONObject put;
        synchronized (SyncEngine.class) {
            AppStore appStore = AppStore.get(context);
            String endpoint = ApiClient.endpoint(str);
            JSONObject post = ApiClient.post(endpoint, new JSONObject().put("apiVersion", 2).put("action", "pair").put("deviceId", appStore.deviceId).put("deviceName", str3).put("code", str2).put("householdId", appStore.prefs.getString("householdId", "")));
            if (!post.optBoolean("ok")) {
                throw new IllegalArgumentException(post.optJSONObject("error").optString("message", "No se pudo conectar."));
            }
            String string = post.getString("householdId");
            String string2 = appStore.prefs.getString("householdId", "");
            if (!string2.isEmpty() && !string2.equals(string)) {
                throw new IllegalArgumentException("Este teléfono conserva movimientos de otra hoja. No se mezclarán.");
            }
            if (!post.getString("deviceId").equals(appStore.deviceId) || !post.getString("token").matches("[a-fA-F0-9]{64}")) {
                throw new IllegalArgumentException("La respuesta de conexión no es válida.");
            }
            JSONObject jSONObject = post.getJSONObject("snapshot");
            if (!string.equals(jSONObject.optString("householdId"))) {
                throw new IllegalArgumentException("La hoja de destino no coincide.");
            }
            new Vault(context).putToken(post.getString("token"));
            appStore.prefs.edit().putString("endpoint", endpoint).putString("householdId", string).putString("deviceName", str3).putBoolean("authError", false).putString("syncError", "").commit();
            appStore.cache("snapshot", jSONObject);
            schedule(context);
            changed();
            put = new JSONObject().put("connected", true);
        }
        return put;
    }

    public static void schedule(Context context) {
        AppStore appStore = AppStore.get(context);
        if (appStore.prefs.getString("endpoint", "").isEmpty() || appStore.prefs.getBoolean("authError", false)) {
            return;
        }
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService("jobscheduler");
        if (jobScheduler.getPendingJob(5102) == null) {
            jobScheduler.schedule(new JobInfo.Builder(5102, new ComponentName(context, (Class<?>) SyncJobService.class)).setPeriodic(21600000L, 1800000L).setPersisted(true).build());
        }
        JobInfo.Builder persisted = new JobInfo.Builder(5101, new ComponentName(context, (Class<?>) SyncJobService.class)).setMinimumLatency(1000L).setOverrideDeadline(900000L).setBackoffCriteria(30000L, 1).setPersisted(true);
        if (Build.VERSION.SDK_INT >= 28) {
            persisted.setRequiredNetwork(new NetworkRequest.Builder().addCapability(12).removeCapability(16).removeCapability(15).build());
        } else {
            persisted.setRequiredNetworkType(1);
        }
        jobScheduler.schedule(persisted.build());
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:67:0x0284  */
    /* JADX WARN: Removed duplicated region for block: B:69:0x0294  */
    /* JADX WARN: Removed duplicated region for block: B:83:0x0287 A[Catch: all -> 0x02cf, TryCatch #9 {all -> 0x02cf, blocks: (B:34:0x008d, B:133:0x00d7, B:138:0x00f3, B:140:0x00fb, B:142:0x0103, B:144:0x011a, B:145:0x011f, B:146:0x010b, B:147:0x00ed, B:148:0x00e2, B:36:0x0120, B:38:0x013c, B:40:0x014a, B:42:0x0152, B:43:0x0157, B:45:0x015f, B:47:0x0199, B:48:0x019f, B:51:0x01a5, B:52:0x01aa, B:54:0x01b6, B:88:0x01e3, B:89:0x01e8, B:92:0x01ef, B:94:0x01f5, B:98:0x0210, B:101:0x0213, B:104:0x0218, B:106:0x0224, B:65:0x0280, B:72:0x0297, B:73:0x029f, B:75:0x02b9, B:83:0x0287, B:86:0x028e, B:130:0x024b, B:131:0x0252, B:155:0x0253), top: B:33:0x008d }] */
    /* JADX WARN: Type inference failed for: r14v1 */
    /* JADX WARN: Type inference failed for: r14v2, types: [int] */
    /* JADX WARN: Type inference failed for: r14v4 */
    /* JADX WARN: Type inference failed for: r2v22, types: [org.json.JSONArray] */
    /* JADX WARN: Type inference failed for: r8v25, types: [org.json.JSONArray] */
    /* JADX WARN: Type inference failed for: r9v18, types: [int] */
    /* JADX WARN: Type inference failed for: r9v25 */
    /* JADX WARN: Type inference failed for: r9v26 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static synchronized boolean sync(Context context) {
        String message;
        synchronized (SyncEngine.class) {
            boolean z = true;
            boolean z2 = false;
            if (!running.compareAndSet(false, true)) {
                return true;
            }
            AppStore appStore = AppStore.get(context);
            JSONArray jSONArray = new JSONArray();
            JSONArray jSONArray2 = new JSONArray();
            try {
                if (!appStore.prefs.getString("endpoint", "").isEmpty() && !appStore.prefs.getBoolean("authError", false)) {
                    if (!online(context)) {
                        boolean hasPending = appStore.hasPending();
                        running.set(false);
                        PlanNotifications.check(context);
                        changed();
                        return hasPending;
                    }
                    int i = 0;
                    while (i < 12) {
                        jSONArray2 = PlanStore.takePending(appStore);
                        jSONArray = PlanStore.readyOperations(appStore, appStore.pending());
                        try {
                            try {
                                JSONObject put = new JSONObject().put("apiVersion", jSONArray2.length() > 0 ? 3 : 2).put("action", "sync").put("deviceId", appStore.deviceId).put("token", new Vault(context).token()).put("householdId", appStore.prefs.getString("householdId", "")).put("profiles", appStore.profiles()).put("operations", jSONArray).put("planChanges", jSONArray2);
                                appStore.sending(jSONArray);
                                JSONObject post = ApiClient.post(appStore.prefs.getString("endpoint", ""), put);
                                if (!post.optBoolean("ok")) {
                                    JSONObject optJSONObject = post.optJSONObject("error");
                                    String optString = optJSONObject == null ? "ERROR_TEMPORAL" : optJSONObject.optString("code");
                                    String optString2 = optJSONObject == null ? "No se pudo sincronizar." : optJSONObject.optString("message");
                                    if (optString.equals("NO_AUTORIZADO") || optString.equals("DISPOSITIVO_REVOCADO") || optString.equals("OTRA_HOJA")) {
                                        appStore.prefs.edit().putBoolean("authError", z).commit();
                                    }
                                    throw new IllegalArgumentException(optString2);
                                }
                                JSONObject jSONObject = post.getJSONObject("snapshot");
                                if (!appStore.prefs.getString("householdId", "").equals(jSONObject.optString("householdId"))) {
                                    throw new IllegalArgumentException("La respuesta pertenece a otra hoja.");
                                }
                                appStore.acknowledge(jSONArray, post.getJSONArray("acks"));
                                JSONArray jSONArray3 = new JSONArray();
                                try {
                                    JSONArray optJSONArray = post.optJSONArray("planAcks");
                                    if (optJSONArray == null) {
                                        optJSONArray = new JSONArray();
                                    }
                                    PlanStore.acknowledge(appStore, jSONArray2, optJSONArray);
                                    JSONArray jSONArray4 = new JSONArray();
                                    try {
                                        appStore.cache("snapshot", jSONObject);
                                        appStore.prefs.edit().putString("lastSync", Instant.now().toString()).putString("syncError", "").commit();
                                        changed();
                                        if (!appStore.hasPending()) {
                                            running.set(z2);
                                            PlanNotifications.check(context);
                                            changed();
                                            return z2;
                                        }
                                        ?? pending = appStore.pending();
                                        boolean z3 = z2;
                                        boolean z4 = z3;
                                        for (?? r9 = z3; r9 < pending.length(); r9++) {
                                            try {
                                                JSONObject jSONObject2 = pending.getJSONObject(r9);
                                                for (?? r14 = z2; r14 < put.getJSONArray("operations").length(); r14++) {
                                                    if (jSONObject2.getString("id").equals(put.getJSONArray("operations").getJSONObject(r14).getString("id"))) {
                                                        z4 = true;
                                                    }
                                                }
                                                z2 = false;
                                            } catch (Exception e) {
                                                e = e;
                                                jSONArray2 = jSONArray4;
                                                jSONArray = jSONArray3;
                                                z = true;
                                                message = !(e instanceof UnknownHostException) ? "Sin acceso al servicio. Se reintentará al recuperar internet." : e instanceof SocketTimeoutException ? "La respuesta tardó demasiado. Se reintentará sin duplicar el movimiento." : e.getMessage();
                                                if (message == null) {
                                                    message = "No se pudo sincronizar. Tus movimientos siguen guardados.";
                                                }
                                                String str = message;
                                                try {
                                                    appStore.retry(jSONArray, str);
                                                    PlanStore.retry(appStore, jSONArray2);
                                                } catch (Exception e2) {
                                                }
                                                appStore.prefs.edit().putString("syncError", str).commit();
                                                if (!appStore.prefs.getBoolean("authError", false) || !appStore.hasPending()) {
                                                    z = false;
                                                }
                                                running.set(false);
                                                PlanNotifications.check(context);
                                                changed();
                                                return z;
                                            }
                                        }
                                        try {
                                            JSONArray pending2 = PlanStore.pending(appStore);
                                            for (int i2 = 0; i2 < pending2.length(); i2++) {
                                                for (int i3 = 0; i3 < jSONArray2.length(); i3++) {
                                                    if (pending2.getJSONObject(i2).getString("id").equals(jSONArray2.getJSONObject(i3).getString("id"))) {
                                                        z4 = true;
                                                    }
                                                }
                                            }
                                            if (z4 || (put.getJSONArray("operations").length() == 0 && jSONArray2.length() == 0)) {
                                                running.set(false);
                                                PlanNotifications.check(context);
                                                changed();
                                                return true;
                                            }
                                            i++;
                                            z = true;
                                            z2 = false;
                                        } catch (Exception e3) {
                                            e = e3;
                                            z = true;
                                            jSONArray2 = jSONArray4;
                                            jSONArray = jSONArray3;
                                            if (!(e instanceof UnknownHostException)) {
                                            }
                                            if (message == null) {
                                            }
                                            String str2 = message;
                                            appStore.retry(jSONArray, str2);
                                            PlanStore.retry(appStore, jSONArray2);
                                            appStore.prefs.edit().putString("syncError", str2).commit();
                                            if (!appStore.prefs.getBoolean("authError", false)) {
                                            }
                                            z = false;
                                            running.set(false);
                                            PlanNotifications.check(context);
                                            changed();
                                            return z;
                                        }
                                    } catch (Exception e4) {
                                        e = e4;
                                    }
                                } catch (Exception e5) {
                                    e = e5;
                                }
                            } catch (Exception e6) {
                                e = e6;
                            }
                        } catch (Throwable th) {
                            th = th;
                            running.set(false);
                            PlanNotifications.check(context);
                            changed();
                            throw th;
                        }
                    }
                    boolean hasPending2 = appStore.hasPending();
                    running.set(false);
                    PlanNotifications.check(context);
                    changed();
                    return hasPending2;
                }
                running.set(false);
                PlanNotifications.check(context);
                changed();
                return false;
            } catch (Exception e7) {
                e = e7;
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }
}
