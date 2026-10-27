package cu.hogarplaya;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class PlanStore {
    /* JADX WARN: Removed duplicated region for block: B:31:0x0080  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0090  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0093 A[Catch: all -> 0x00b2, TryCatch #0 {all -> 0x00b2, blocks: (B:16:0x0030, B:18:0x0036, B:21:0x0051, B:23:0x0060, B:26:0x0069, B:29:0x0079, B:32:0x0089, B:36:0x0099, B:37:0x0093, B:39:0x0083, B:42:0x004b, B:44:0x00aa), top: B:15:0x0030, outer: #1 }] */
    /* JADX WARN: Removed duplicated region for block: B:39:0x0083 A[Catch: all -> 0x00b2, TryCatch #0 {all -> 0x00b2, blocks: (B:16:0x0030, B:18:0x0036, B:21:0x0051, B:23:0x0060, B:26:0x0069, B:29:0x0079, B:32:0x0089, B:36:0x0099, B:37:0x0093, B:39:0x0083, B:42:0x004b, B:44:0x00aa), top: B:15:0x0030, outer: #1 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static void acknowledge(AppStore appStore, JSONArray jSONArray, JSONArray jSONArray2) throws Exception {
        String str;
        synchronized (appStore) {
            HashMap hashMap = new HashMap();
            for (int i = 0; i < jSONArray2.length(); i++) {
                JSONObject jSONObject = jSONArray2.getJSONObject(i);
                if (hashMap.put(jSONObject.getString("id"), jSONObject) != null) {
                    throw new IllegalArgumentException("Respuesta de planes repetida.");
                }
            }
            SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
            writableDatabase.beginTransaction();
            for (int i2 = 0; i2 < jSONArray.length(); i2++) {
                try {
                    String string = jSONArray.getJSONObject(i2).getString("id");
                    JSONObject jSONObject2 = (JSONObject) hashMap.get(string);
                    String optString = jSONObject2 == null ? "retry" : jSONObject2.optString("status");
                    ContentValues contentValues = new ContentValues();
                    if (!optString.equals("accepted") && !optString.equals("duplicate")) {
                        str = optString.equals("rejected") ? "REJECTED" : "PENDING";
                        contentValues.put("status", str);
                        contentValues.put("error_code", jSONObject2 != null ? "" : jSONObject2.optString("code"));
                        contentValues.put("error_message", jSONObject2 != null ? "No llegó la confirmación; se reintentará." : jSONObject2.optString("message"));
                        writableDatabase.update("plan_changes", contentValues, "id=? AND status='SENDING'", new String[]{string});
                    }
                    str = "SYNCED";
                    contentValues.put("status", str);
                    contentValues.put("error_code", jSONObject2 != null ? "" : jSONObject2.optString("code"));
                    contentValues.put("error_message", jSONObject2 != null ? "No llegó la confirmación; se reintentará." : jSONObject2.optString("message"));
                    writableDatabase.update("plan_changes", contentValues, "id=? AND status='SENDING'", new String[]{string});
                } finally {
                    writableDatabase.endTransaction();
                }
            }
            writableDatabase.setTransactionSuccessful();
        }
    }

    public static JSONArray changes(AppStore appStore) throws Exception {
        JSONArray jSONArray = new JSONArray();
        Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT payload,status,attempts,error_code,error_message FROM plan_changes ORDER BY seq", null);
        while (rawQuery.moveToNext()) {
            try {
                jSONArray.put(new JSONObject(rawQuery.getString(0)).put("status", rawQuery.getString(1)).put("attempts", rawQuery.getInt(2)).put("errorCode", rawQuery.getString(3)).put("error", rawQuery.getString(4)));
            } catch (Throwable th) {
                if (rawQuery != null) {
                    try {
                        rawQuery.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        return jSONArray;
    }

    private static boolean contains(JSONArray jSONArray, String str) throws Exception {
        for (int i = 0; i < jSONArray.length(); i++) {
            if (jSONArray.getString(i).equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static int count(AppStore appStore, String str) {
        Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM plan_changes WHERE status=?", new String[]{str});
        try {
            rawQuery.moveToFirst();
            int i = rawQuery.getInt(0);
            if (rawQuery != null) {
                rawQuery.close();
            }
            return i;
        } catch (Throwable th) {
            if (rawQuery != null) {
                try {
                    rawQuery.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    public static void discardRejected(AppStore appStore, String str) throws Exception {
        boolean z;
        synchronized (appStore) {
            JSONArray changes = changes(appStore);
            JSONObject jSONObject = null;
            for (int i = 0; i < changes.length(); i++) {
                if (changes.getJSONObject(i).getString("id").equals(str) && changes.getJSONObject(i).getString("status").equals("REJECTED")) {
                    jSONObject = changes.getJSONObject(i);
                }
            }
            if (jSONObject == null) {
                return;
            }
            String string = jSONObject.getString("planId");
            JSONObject optJSONObject = appStore.snapshot().optJSONObject("planning");
            JSONArray optJSONArray = optJSONObject != null ? optJSONObject.optJSONArray("allPlans") : null;
            if (optJSONArray == null && optJSONObject != null) {
                optJSONArray = optJSONObject.optJSONArray("plans");
            }
            if (optJSONArray != null) {
                z = false;
                for (int i2 = 0; i2 < optJSONArray.length(); i2++) {
                    if (optJSONArray.getJSONObject(i2).getString("id").equals(string)) {
                        z = true;
                    }
                }
            } else {
                z = false;
            }
            if (!z) {
                JSONArray operations = appStore.operations();
                for (int i3 = 0; i3 < operations.length(); i3++) {
                    JSONObject jSONObject2 = operations.getJSONObject(i3);
                    if (jSONObject2.optString("planId").equals(string) && (jSONObject2.getString("status").equals("PENDING") || jSONObject2.getString("status").equals("SENDING"))) {
                        throw new IllegalArgumentException("Este plan aún no existe en el Sheet y tiene pagos pendientes. Corrige el plan antes de descartar su cambio.");
                    }
                }
            }
            appStore.getWritableDatabase().execSQL("UPDATE plan_changes SET status='DISMISSED' WHERE id=? AND status='REJECTED'", new Object[]{str});
        }
    }

    public static JSONArray pending(AppStore appStore) throws Exception {
        JSONArray jSONArray;
        synchronized (appStore) {
            jSONArray = new JSONArray();
            Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT payload FROM plan_changes WHERE status='PENDING' ORDER BY seq LIMIT 20", null);
            while (rawQuery.moveToNext()) {
                try {
                    jSONArray.put(new JSONObject(rawQuery.getString(0)));
                } finally {
                }
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
        }
        return jSONArray;
    }

    public static JSONArray readyOperations(AppStore appStore, JSONArray jSONArray) throws Exception {
        HashSet hashSet = new HashSet();
        JSONArray changes = changes(appStore);
        for (int i = 0; i < changes.length(); i++) {
            JSONObject jSONObject = changes.getJSONObject(i);
            if (jSONObject.getString("status").equals("PENDING") || jSONObject.getString("status").equals("SENDING") || jSONObject.getString("status").equals("REJECTED")) {
                hashSet.add(jSONObject.getString("planId"));
            }
        }
        JSONArray jSONArray2 = new JSONArray();
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            JSONObject jSONObject2 = jSONArray.getJSONObject(i2);
            if (!hashSet.contains(jSONObject2.optString("planId"))) {
                jSONArray2.put(jSONObject2);
            }
        }
        return jSONArray2;
    }

    public static void retry(AppStore appStore, JSONArray jSONArray) throws Exception {
        synchronized (appStore) {
            for (int i = 0; i < jSONArray.length(); i++) {
                appStore.getWritableDatabase().execSQL("UPDATE plan_changes SET status='PENDING' WHERE id=? AND status='SENDING'", new Object[]{jSONArray.getJSONObject(i).getString("id")});
            }
        }
    }

    public static void save(AppStore appStore, String str, JSONObject jSONObject) throws Exception {
        JSONObject jSONObject2;
        String str2;
        String str3;
        String str4;
        JSONArray jSONArray;
        synchronized (appStore) {
            JSONObject profile = appStore.profile(str);
            String uuid = Money.uuid(jSONObject.getString("id"));
            String trim = jSONObject.optString("concept").trim();
            String trim2 = jSONObject.optString("note").trim();
            String string = jSONObject.getString("date");
            String string2 = jSONObject.getString("category");
            if (!LocalDate.parse(string).toString().equals(string) || string.compareTo("2000-01-01") < 0 || string.compareTo("2099-12-31") > 0) {
                throw new IllegalArgumentException("Selecciona una fecha de pago válida entre 2000 y 2099.");
            }
            if (trim.isEmpty() || trim.length() > 500 || trim2.length() > 1000) {
                throw new IllegalArgumentException("Escribe un concepto de hasta 500 caracteres y notas de hasta 1000.");
            }
            long cents = Money.cents(jSONObject.getString("amount"));
            JSONArray optJSONArray = appStore.snapshot().optJSONArray("expenseCategories");
            if (optJSONArray == null || !contains(optJSONArray, string2)) {
                throw new IllegalArgumentException("Conecta y sincroniza el Sheet para recibir sus categorías antes de crear planes.");
            }
            JSONArray optJSONArray2 = Planning.evaluate(snapshot(appStore), appStore.operations(), Money.today()).optJSONArray("allPlans");
            if (optJSONArray2 != null) {
                jSONObject2 = null;
                for (int i = 0; i < optJSONArray2.length(); i++) {
                    if (optJSONArray2.getJSONObject(i).getString("id").equals(uuid)) {
                        jSONObject2 = optJSONArray2.getJSONObject(i);
                    }
                }
            } else {
                jSONObject2 = null;
            }
            if (jSONObject2 != null) {
                if (cents < Math.round(jSONObject2.optDouble("paid", 0.0d) * 100.0d) && cents != Math.round(jSONObject2.optDouble("amount", 0.0d) * 100.0d)) {
                    throw new IllegalArgumentException("El importe previsto no puede ser menor que lo que ya pagaste.");
                }
                if (jSONObject2.optDouble("paid", 0.0d) > 0.0d && !jSONObject2.getString("category").equals(string2)) {
                    throw new IllegalArgumentException("Conserva la categoría: este plan ya tiene gastos asociados.");
                }
            }
            String optString = jSONObject.optString("baseVersion");
            String optString2 = jSONObject.optString("changeId", UUID.randomUUID().toString());
            Money.uuid(optString2);
            JSONArray changes = changes(appStore);
            int i2 = 0;
            JSONObject jSONObject3 = null;
            while (true) {
                str2 = optString;
                if (i2 >= changes.length()) {
                    break;
                }
                JSONObject jSONObject4 = changes.getJSONObject(i2);
                String str5 = optString2;
                if (jSONObject4.getString("planId").equals(uuid)) {
                    jSONArray = changes;
                    if (jSONObject4.getString("status").equals("PENDING") || jSONObject4.getString("status").equals("SENDING")) {
                        jSONObject3 = jSONObject4;
                    }
                } else {
                    jSONArray = changes;
                }
                i2++;
                optString = str2;
                optString2 = str5;
                changes = jSONArray;
            }
            String str6 = optString2;
            if (jSONObject3 == null) {
                str3 = str2;
                str4 = str6;
            } else {
                if (jSONObject3.optInt("attempts") > 0 || !jSONObject3.getString("status").equals("PENDING")) {
                    throw new IllegalArgumentException("Este plan se está sincronizando o espera confirmación. Sincroniza antes de volver a editarlo.");
                }
                str4 = jSONObject3.getString("id");
                str3 = jSONObject3.getString("baseVersion");
            }
            JSONObject put = new JSONObject().put("id", str4).put("planId", uuid).put("baseVersion", str3).put("date", string).put("concept", trim).put("category", string2).put("amount", Money.decimal(cents)).put("essential", jSONObject.getBoolean("essential")).put("active", jSONObject.getBoolean("active")).put("note", trim2).put("actorId", str).put("actorName", profile.getString("name")).put("deviceId", appStore.deviceId).put("clientCreatedAt", Instant.now().toString());
            SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                writableDatabase.execSQL("UPDATE plan_changes SET status='DISMISSED' WHERE plan_id=? AND status='REJECTED'", new Object[]{uuid});
                ContentValues contentValues = new ContentValues();
                contentValues.put("profile_id", str);
                contentValues.put("payload", put.toString());
                if (jSONObject3 == null) {
                    contentValues.put("id", str4);
                    contentValues.put("plan_id", uuid);
                    writableDatabase.insertOrThrow("plan_changes", null, contentValues);
                } else {
                    writableDatabase.update("plan_changes", contentValues, "id=?", new String[]{str4});
                }
                writableDatabase.setTransactionSuccessful();
            } finally {
                writableDatabase.endTransaction();
            }
        }
    }

    public static void sending(AppStore appStore, JSONArray jSONArray) throws Exception {
        synchronized (appStore) {
            for (int i = 0; i < jSONArray.length(); i++) {
                appStore.getWritableDatabase().execSQL("UPDATE plan_changes SET status='SENDING',attempts=attempts+1 WHERE id=? AND status='PENDING'", new Object[]{jSONArray.getJSONObject(i).getString("id")});
            }
        }
    }

    public static JSONObject snapshot(AppStore appStore) throws Exception {
        int i;
        JSONArray jSONArray;
        JSONObject jSONObject;
        String str;
        String str2;
        JSONObject snapshot = appStore.snapshot();
        String str3 = "planning";
        JSONObject optJSONObject = snapshot.optJSONObject("planning");
        if (optJSONObject == null) {
            return snapshot;
        }
        JSONArray optJSONArray = optJSONObject.optJSONArray("allPlans");
        String str4 = "plans";
        if (optJSONArray == null) {
            optJSONArray = optJSONObject.optJSONArray("plans");
        }
        if (optJSONArray == null) {
            optJSONArray = new JSONArray();
        }
        JSONArray jSONArray2 = new JSONArray(optJSONArray.toString());
        JSONArray changes = changes(appStore);
        int i2 = 0;
        while (i2 < changes.length()) {
            JSONObject jSONObject2 = changes.getJSONObject(i2);
            String string = jSONObject2.getString("status");
            if (string.equals("PENDING") || string.equals("SENDING")) {
                JSONObject jSONObject3 = null;
                for (int i3 = 0; i3 < jSONArray2.length(); i3++) {
                    if (jSONArray2.getJSONObject(i3).getString("id").equals(jSONObject2.getString("planId"))) {
                        jSONObject3 = jSONArray2.getJSONObject(i3);
                    }
                }
                if (jSONObject3 == null) {
                    i = 0;
                    jSONObject3 = new JSONObject().put("id", jSONObject2.getString("planId")).put("paid", 0).put("version", "");
                    jSONArray2.put(jSONObject3);
                } else {
                    i = 0;
                }
                String[] strArr = {"date", "concept", "category", "essential", "active", "note"};
                int i4 = i;
                while (i4 < 6) {
                    String str5 = strArr[i4];
                    jSONObject3.put(str5, jSONObject2.get(str5));
                    i4++;
                    changes = changes;
                }
                jSONArray = changes;
                jSONObject = snapshot;
                str = str3;
                str2 = str4;
                jSONObject3.put("amount", Double.parseDouble(jSONObject2.getString("amount"))).put("remaining", Math.max(0.0d, Double.parseDouble(jSONObject2.getString("amount")) - jSONObject3.optDouble("paid", 0.0d))).put("local", true).put("changeStatus", string).put("editable", string.equals("PENDING") && jSONObject2.optInt("attempts") == 0);
            } else {
                jSONObject = snapshot;
                str = str3;
                jSONArray = changes;
                str2 = str4;
            }
            i2++;
            str4 = str2;
            snapshot = jSONObject;
            str3 = str;
            changes = jSONArray;
        }
        JSONObject jSONObject4 = snapshot;
        String str6 = str3;
        String str7 = str4;
        JSONArray jSONArray3 = new JSONArray();
        for (int i5 = 0; i5 < jSONArray2.length(); i5++) {
            if (jSONArray2.getJSONObject(i5).optBoolean("active", true)) {
                jSONArray3.put(jSONArray2.getJSONObject(i5));
            }
        }
        ArrayList arrayList = new ArrayList();
        for (int i6 = 0; i6 < jSONArray3.length(); i6++) {
            arrayList.add(jSONArray3.getJSONObject(i6));
        }
        arrayList.sort(new Comparator() { // from class: cu.hogarplaya.PlanStore$$ExternalSyntheticLambda0
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                int compareTo;
                compareTo = ((JSONObject) obj).optString("date").compareTo(((JSONObject) obj2).optString("date"));
                return compareTo;
            }
        });
        JSONArray jSONArray4 = new JSONArray();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            jSONArray4.put((JSONObject) it.next());
        }
        optJSONObject.put("allPlans", jSONArray2).put(str7, jSONArray4);
        jSONObject4.put(str6, optJSONObject);
        return jSONObject4;
    }

    static void tables(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS plan_changes(seq INTEGER PRIMARY KEY AUTOINCREMENT,id TEXT NOT NULL UNIQUE,plan_id TEXT NOT NULL,profile_id TEXT NOT NULL,payload TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'PENDING',attempts INTEGER NOT NULL DEFAULT 0,error_code TEXT NOT NULL DEFAULT '',error_message TEXT NOT NULL DEFAULT '',FOREIGN KEY(profile_id) REFERENCES profiles(id))");
        sQLiteDatabase.execSQL("CREATE INDEX IF NOT EXISTS plan_changes_pending ON plan_changes(status,seq)");
    }

    public static JSONArray takePending(AppStore appStore) throws Exception {
        JSONArray pending;
        synchronized (appStore) {
            pending = pending(appStore);
            sending(appStore, pending);
        }
        return pending;
    }
}
