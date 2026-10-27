package cu.hogarplaya;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class Shopping {
    public static void buy(AppStore appStore, String str, String str2, boolean z) throws Exception {
        synchronized (appStore) {
            JSONObject current = current(appStore);
            SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
            writableDatabase.beginTransaction();
            try {
                Cursor rawQuery = writableDatabase.rawQuery("SELECT payload,operation_id FROM shopping_lines WHERE id=? AND session_id=?", new String[]{str2, current.getString("id")});
                try {
                    if (!rawQuery.moveToFirst()) {
                        throw new IllegalArgumentException("El producto ya no está en esta lista.");
                    }
                    if (!rawQuery.getString(1).isEmpty()) {
                        writableDatabase.setTransactionSuccessful();
                        if (rawQuery != null) {
                            rawQuery.close();
                        }
                        return;
                    }
                    JSONObject jSONObject = new JSONObject(rawQuery.getString(0));
                    if (rawQuery != null) {
                        rawQuery.close();
                    }
                    if (jSONObject.optString("unitCode").isEmpty() || jSONObject.optString("unitPrice").isEmpty()) {
                        throw new IllegalArgumentException("Completa la unidad y el precio real antes de confirmar esta compra.");
                    }
                    String repeatWarning = repeatWarning(appStore, jSONObject);
                    if (!repeatWarning.isEmpty() && !z) {
                        throw new IllegalArgumentException("REPETIDA: " + repeatWarning);
                    }
                    long lineTotal = Money.lineTotal(jSONObject.getString("quantity"), jSONObject.getString("unitPrice"));
                    long charge = charge(lineTotal, jSONObject.getString("currency"), current);
                    long used = used(lines(appStore, current.getString("id")));
                    if (used + charge > current.getLong("limitCents")) {
                        throw new IllegalArgumentException("Esta compra supera el tope. Quedan " + Money.decimal(current.getLong("limitCents") - used) + " " + current.getString("currency") + " para hoy.");
                    }
                    String uuid = UUID.randomUUID().toString();
                    appStore.insert(writableDatabase, appStore.operation(str, new JSONObject().put("id", uuid).put("type", "Gasto").put("category", jSONObject.getString("category")).put("date", current.getString("date")).put("amount", Money.decimal(lineTotal)).put("currency", jSONObject.getString("currency")).put("rate", jSONObject.optString("rate")).put("paymentFormat", jSONObject.optString("paymentFormat")).put("concept", "Compra: " + jSONObject.getString("name") + " × " + jSONObject.getString("quantity") + " " + jSONObject.optString("unit")).put("items", new JSONArray().put(jSONObject)).put("planId", jSONObject.optString("planId")).put("shopping", new JSONObject().put("id", current.getString("id")).put("date", current.getString("date")).put("limit", current.getString("limit")).put("currency", current.getString("currency")).put("rate", current.getString("rate")).put("lineId", str2).put("repeatConfirmed", z))));
                    jSONObject.put("chargedCents", charge);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("operation_id", uuid);
                    contentValues.put("payload", jSONObject.toString());
                    writableDatabase.update("shopping_lines", contentValues, "id=?", new String[]{str2});
                    writableDatabase.setTransactionSuccessful();
                } finally {
                }
            } finally {
                writableDatabase.endTransaction();
            }
        }
    }

    private static long charge(long j, String str, JSONObject jSONObject) throws Exception {
        long estimateCharge = estimateCharge(j, str, jSONObject);
        if (estimateCharge > 0) {
            return estimateCharge;
        }
        throw new IllegalArgumentException("El importe debe representar al menos un centavo del tope.");
    }

    private static JSONObject current(AppStore appStore) throws Exception {
        JSONObject session = session(appStore, Money.today());
        if (session != null) {
            return session;
        }
        throw new IllegalArgumentException("Inicia el Modo compra y fija tu tope de hoy.");
    }

    private static long estimateCharge(long j, String str, JSONObject jSONObject) throws Exception {
        BigDecimal valueOf = BigDecimal.valueOf(j, 2);
        BigDecimal bigDecimal = new BigDecimal(jSONObject.getString("rate"));
        if (!str.equals(jSONObject.getString("currency"))) {
            valueOf = jSONObject.getString("currency").equals("CUP") ? valueOf.multiply(bigDecimal) : valueOf.divide(bigDecimal, 10, RoundingMode.HALF_UP);
        }
        return valueOf.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    public static JSONArray export(AppStore appStore) throws Exception {
        JSONArray jSONArray = new JSONArray();
        Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT payload FROM shopping_sessions ORDER BY day", null);
        while (rawQuery.moveToNext()) {
            try {
                JSONObject jSONObject = new JSONObject(rawQuery.getString(0));
                jSONObject.put("lines", lines(appStore, jSONObject.getString("id")));
                jSONArray.put(jSONObject);
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

    public static boolean hasSession(AppStore appStore) throws Exception {
        return session(appStore, Money.today()) != null;
    }

    public static void importCart(AppStore appStore, String str) throws Exception {
        String str2;
        synchronized (appStore) {
            current(appStore);
            JSONArray cart = appStore.cart(str);
            SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
            writableDatabase.beginTransaction();
            for (int i = 0; i < cart.length(); i++) {
                try {
                    JSONObject jSONObject = cart.getJSONObject(i);
                    jSONObject.put("id", UUID.randomUUID().toString());
                    if (jSONObject.optString("unitCode").isEmpty()) {
                        String lowerCase = jSONObject.optString("unit").toLowerCase(Locale.ROOT);
                        if (!lowerCase.contains("paquete") && !lowerCase.contains("bolsa") && !lowerCase.contains("pomo") && !lowerCase.contains("caja") && !lowerCase.contains("bandeja")) {
                            if (!lowerCase.equals("kg") && !lowerCase.contains("kilogramo")) {
                                if (!lowerCase.equals("lb") && !lowerCase.contains("libra")) {
                                    if (!lowerCase.equals("l") && !lowerCase.contains("litro")) {
                                        str2 = "unidad";
                                        jSONObject.put("unitCode", str2);
                                    }
                                    str2 = "litro";
                                    jSONObject.put("unitCode", str2);
                                }
                                str2 = "lb";
                                jSONObject.put("unitCode", str2);
                            }
                            str2 = "kg";
                            jSONObject.put("unitCode", str2);
                        }
                        str2 = "paquete";
                        jSONObject.put("unitCode", str2);
                    }
                    if (jSONObject.optString("currency").equals("CUP") && !jSONObject.has("paymentFormat")) {
                        jSONObject.put("paymentFormat", "Efectivo");
                    }
                    saveLine(appStore, jSONObject);
                } finally {
                    writableDatabase.endTransaction();
                }
            }
            writableDatabase.delete("carts", "profile_id=?", new String[]{str});
            writableDatabase.setTransactionSuccessful();
        }
    }

    private static String key(String str) {
        return str.trim().toLowerCase(Locale.ROOT);
    }

    private static JSONArray lines(AppStore appStore, String str) throws Exception {
        JSONArray jSONArray = new JSONArray();
        Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT payload,operation_id FROM shopping_lines WHERE session_id=? ORDER BY rowid", new String[]{str});
        while (rawQuery.moveToNext()) {
            try {
                boolean z = false;
                JSONObject put = new JSONObject(rawQuery.getString(0)).put("operationId", rawQuery.getString(1));
                if (!rawQuery.getString(1).isEmpty()) {
                    z = true;
                }
                jSONArray.put(put.put("bought", z));
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

    public static void removeLine(AppStore appStore, String str) throws Exception {
        synchronized (appStore) {
            appStore.getWritableDatabase().delete("shopping_lines", "id=? AND session_id=? AND operation_id=''", new String[]{str, current(appStore).getString("id")});
        }
    }

    public static String repeatWarning(AppStore appStore, JSONObject jSONObject) throws Exception {
        JSONObject optJSONObject;
        JSONArray optJSONArray = appStore.snapshot().optJSONArray("products");
        if (optJSONArray == null) {
            try {
                optJSONArray = new JSONObject(appStore.cache("catalog", "{}")).optJSONArray("products");
            } catch (Exception e) {
            }
        }
        if (optJSONArray == null) {
            return "";
        }
        String optString = jSONObject.optString("productId", jSONObject.optString("name"));
        JSONObject jSONObject2 = null;
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject jSONObject3 = optJSONArray.getJSONObject(i);
            if (key(jSONObject3.optString("id", jSONObject3.optString("name"))).equals(key(optString))) {
                jSONObject2 = jSONObject3;
            }
        }
        return (jSONObject2 == null || jSONObject2.optInt("monthlyLimit") != 1 || (optJSONObject = statistics(appStore).optJSONObject(key(optString))) == null || optJSONObject.optInt("monthlyCount") <= 0) ? "" : "Ya compraste " + jSONObject.optString("name") + " este mes y su plan indica una compra mensual. Puedes comprarlo otra vez si lo confirmas.";
    }

    public static void saveLine(AppStore appStore, JSONObject jSONObject) throws Exception {
        synchronized (appStore) {
            JSONObject current = current(appStore);
            JSONObject jSONObject2 = new JSONObject(jSONObject.toString());
            String uuid = Money.uuid(jSONObject2.optString("id", UUID.randomUUID().toString()));
            String trim = jSONObject2.optString("name").trim();
            if (trim.isEmpty() || trim.length() > 120) {
                throw new IllegalArgumentException("Revisa el producto.");
            }
            if (!new HashSet(Arrays.asList("", "paquete", "lb", "kg", "litro", "unidad")).contains(jSONObject2.optString("unitCode"))) {
                throw new IllegalArgumentException("Elige paquete, libra, kilogramo, litro o unidad.");
            }
            try {
                BigDecimal bigDecimal = new BigDecimal(jSONObject2.getString("quantity").replace(',', '.'));
                if (bigDecimal.signum() <= 0 || bigDecimal.compareTo(new BigDecimal("100000")) > 0 || bigDecimal.scale() > 4) {
                    throw new IllegalArgumentException("Revisa la cantidad.");
                }
                if (!jSONObject2.optString("unitPrice").isEmpty()) {
                    Money.lineTotal(jSONObject2.getString("quantity"), jSONObject2.getString("unitPrice"));
                }
                String string = jSONObject2.getString("currency");
                if (!string.equals("CUP") && !string.equals("USD")) {
                    throw new IllegalArgumentException("Moneda no válida.");
                }
                jSONObject2.put("rate", Money.rate(jSONObject2.optString("rate"), string.equals("CUP")));
                jSONObject2.put("quantity", AppStore$$ExternalSyntheticBackportWithForwarding0.m(new BigDecimal(jSONObject2.getString("quantity").replace(',', '.'))).toPlainString());
                jSONObject2.put("unitPrice", jSONObject2.optString("unitPrice").isEmpty() ? "" : Money.decimal(Money.cents(jSONObject2.getString("unitPrice"))));
                String optString = string.equals("CUP") ? jSONObject2.optString("paymentFormat") : "";
                if (string.equals("CUP") && !optString.equals("Efectivo") && !optString.equals("Transferencia")) {
                    throw new IllegalArgumentException("Elige el formato de pago CUP.");
                }
                jSONObject2.put("paymentFormat", optString).put("id", uuid).put("name", trim);
                SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
                Cursor rawQuery = writableDatabase.rawQuery("SELECT session_id,operation_id FROM shopping_lines WHERE id=?", new String[]{uuid});
                try {
                    if (rawQuery.moveToFirst()) {
                        if (!rawQuery.getString(0).equals(current.getString("id")) || !rawQuery.getString(1).isEmpty()) {
                            throw new IllegalArgumentException("Una compra confirmada no se vuelve a editar. Revisa su movimiento.");
                        }
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("payload", jSONObject2.toString());
                        writableDatabase.update("shopping_lines", contentValues, "id=?", new String[]{uuid});
                        if (rawQuery != null) {
                            rawQuery.close();
                        }
                        return;
                    }
                    if (rawQuery != null) {
                        rawQuery.close();
                    }
                    if (lines(appStore, current.getString("id")).length() >= 100) {
                        throw new IllegalArgumentException("La jornada admite hasta 100 productos.");
                    }
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put("id", uuid);
                    contentValues2.put("session_id", current.getString("id"));
                    contentValues2.put("payload", jSONObject2.toString());
                    writableDatabase.insertOrThrow("shopping_lines", null, contentValues2);
                } finally {
                }
            } catch (Exception e) {
            }
        }
    }

    public static void saveLines(AppStore appStore, JSONArray jSONArray) throws Exception {
        synchronized (appStore) {
            if (jSONArray.length() < 1 || jSONArray.length() > 100) {
                throw new IllegalArgumentException("Selecciona entre 1 y 100 productos.");
            }
            SQLiteDatabase writableDatabase = appStore.getWritableDatabase();
            writableDatabase.beginTransaction();
            for (int i = 0; i < jSONArray.length(); i++) {
                try {
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    try {
                        saveLine(appStore, jSONObject);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(jSONObject.optString("name", "Producto") + ": " + e.getMessage());
                    }
                } catch (Throwable th) {
                    writableDatabase.endTransaction();
                    throw th;
                }
            }
            writableDatabase.setTransactionSuccessful();
            writableDatabase.endTransaction();
        }
    }

    private static JSONObject session(AppStore appStore, String str) throws Exception {
        Cursor rawQuery = appStore.getReadableDatabase().rawQuery("SELECT payload FROM shopping_sessions WHERE day=?", new String[]{str});
        try {
            JSONObject jSONObject = rawQuery.moveToFirst() ? new JSONObject(rawQuery.getString(0)) : null;
            if (rawQuery != null) {
                rawQuery.close();
            }
            return jSONObject;
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

    public static void start(AppStore appStore, JSONObject jSONObject) throws Exception {
        synchronized (appStore) {
            long cents = Money.cents(jSONObject.getString("limit"));
            String string = jSONObject.getString("currency");
            String rate = Money.rate(jSONObject.getString("rate"), true);
            if (!string.equals("CUP") && !string.equals("USD")) {
                throw new IllegalArgumentException("Elige la moneda del tope.");
            }
            JSONObject session = session(appStore, Money.today());
            if (session != null) {
                if (session.getLong("limitCents") != cents || !session.getString("currency").equals(string) || !session.getString("rate").equals(rate)) {
                    throw new IllegalArgumentException("La jornada de hoy ya tiene un tope. Puedes retomarla; cerrarla no reinicia el gasto.");
                }
                return;
            }
            JSONObject put = new JSONObject().put("id", UUID.randomUUID().toString()).put("date", Money.today()).put("limitCents", cents).put("limit", Money.decimal(cents)).put("currency", string).put("rate", rate);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", put.getString("id"));
            contentValues.put("day", put.getString("date"));
            contentValues.put("payload", put.toString());
            appStore.getWritableDatabase().insertOrThrow("shopping_sessions", null, contentValues);
        }
    }

    public static JSONObject state(AppStore appStore) throws Exception {
        synchronized (appStore) {
            JSONObject session = session(appStore, Money.today());
            JSONObject put = new JSONObject().put("statistics", statistics(appStore));
            if (session == null) {
                return put.put("active", false);
            }
            JSONArray lines = lines(appStore, session.getString("id"));
            long used = used(lines);
            long j = session.getLong("limitCents");
            long j2 = 0;
            int i = 0;
            for (int i2 = 0; i2 < lines.length(); i2++) {
                JSONObject jSONObject = lines.getJSONObject(i2);
                if (!jSONObject.optBoolean("bought")) {
                    if (!jSONObject.optString("unitPrice").isEmpty() && !jSONObject.optString("unitCode").isEmpty()) {
                        j2 += estimateCharge(Money.lineTotal(jSONObject.getString("quantity"), jSONObject.getString("unitPrice")), jSONObject.getString("currency"), session);
                    }
                    i++;
                }
            }
            long j3 = j - used;
            return put.put("active", true).put("session", session).put("lines", lines).put("spent", Money.decimal(used)).put("remaining", Money.decimal(j3)).put("listTotal", Money.decimal(j2)).put("projectedRemaining", Money.decimal(j3 - j2)).put("unpriced", i).put("estimateComplete", i == 0);
        }
    }

    public static JSONObject statistics(AppStore appStore) throws Exception {
        JSONObject jSONObject;
        long j;
        JSONArray optJSONArray;
        synchronized (appStore) {
            JSONObject snapshot = appStore.snapshot();
            JSONObject optJSONObject = snapshot.optJSONObject("purchaseSummary");
            jSONObject = new JSONObject();
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            String str = Money.today();
            String substring = str.substring(0, 7);
            if (optJSONObject != null) {
                Iterator<String> keys = optJSONObject.keys();
                while (keys.hasNext()) {
                    String next = keys.next();
                    String key = key(next);
                    JSONObject jSONObject2 = optJSONObject.getJSONObject(next);
                    TreeSet treeSet = new TreeSet();
                    JSONArray optJSONArray2 = jSONObject2.optJSONArray("dates");
                    if (optJSONArray2 != null) {
                        for (int i = 0; i < optJSONArray2.length(); i++) {
                            if (optJSONArray2.getString(i).compareTo(str) <= 0) {
                                treeSet.add(optJSONArray2.getString(i));
                            }
                        }
                    }
                    hashMap.put(key, treeSet);
                    hashMap2.put(key, Integer.valueOf(snapshot.optString("month").equals(substring) ? jSONObject2.optInt("monthlyCount") : 0));
                }
            }
            HashSet hashSet = new HashSet();
            JSONArray optJSONArray3 = snapshot.optJSONArray("knownOperationIds");
            if (optJSONArray3 != null) {
                for (int i2 = 0; i2 < optJSONArray3.length(); i2++) {
                    hashSet.add(optJSONArray3.getString(i2));
                }
            }
            JSONArray operations = appStore.operations();
            for (int i3 = 0; i3 < operations.length(); i3++) {
                JSONObject jSONObject3 = operations.getJSONObject(i3);
                if (jSONObject3.optString("type").equals("Gasto") && !jSONObject3.optString("status").equals("REJECTED") && !hashSet.contains(jSONObject3.getString("id"))) {
                    String string = jSONObject3.getString("date");
                    if (string.compareTo(str) <= 0 && (optJSONArray = jSONObject3.optJSONArray("items")) != null) {
                        HashSet hashSet2 = new HashSet();
                        for (int i4 = 0; i4 < optJSONArray.length(); i4++) {
                            JSONObject jSONObject4 = optJSONArray.getJSONObject(i4);
                            String key2 = key(jSONObject4.optString("productId", jSONObject4.optString("name")));
                            if (!key2.isEmpty() && hashSet2.add(key2)) {
                                if (!hashMap.containsKey(key2)) {
                                    hashMap.put(key2, new TreeSet());
                                }
                                ((TreeSet) hashMap.get(key2)).add(string);
                                if (string.startsWith(substring)) {
                                    hashMap2.put(key2, Integer.valueOf(((Integer) hashMap2.getOrDefault(key2, 0)).intValue() + 1));
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry entry : hashMap.entrySet()) {
                ArrayList arrayList = new ArrayList((Collection) entry.getValue());
                if (!arrayList.isEmpty()) {
                    ArrayList arrayList2 = new ArrayList();
                    for (int i5 = 1; i5 < arrayList.size(); i5++) {
                        long between = ChronoUnit.DAYS.between(LocalDate.parse((CharSequence) arrayList.get(i5 - 1)), LocalDate.parse((CharSequence) arrayList.get(i5)));
                        if (between > 0) {
                            arrayList2.add(Long.valueOf(between));
                        }
                    }
                    Collections.sort(arrayList2);
                    if (arrayList2.size() >= 2) {
                        int size = arrayList2.size();
                        j = size % 2 == 1 ? ((Long) arrayList2.get(size / 2)).longValue() : Math.round((((Long) arrayList2.get((size / 2) - 1)).longValue() + ((Long) arrayList2.get(size / 2)).longValue()) / 2.0d);
                    } else {
                        j = 0;
                    }
                    String str2 = (String) arrayList.get(arrayList.size() - 1);
                    String localDate = j > 0 ? LocalDate.parse(str2).plusDays(j).toString() : "";
                    jSONObject.put((String) entry.getKey(), new JSONObject().put("sampleCount", arrayList.size()).put("lastDate", str2).put("intervalDays", j).put("nextDate", localDate).put("due", j > 0 && localDate.compareTo(str) <= 0).put("monthlyCount", hashMap2.getOrDefault(entry.getKey(), 0)));
                }
            }
        }
        return jSONObject;
    }

    private static long used(JSONArray jSONArray) throws Exception {
        long j = 0;
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            if (jSONObject.optBoolean("bought")) {
                j += jSONObject.getLong("chargedCents");
            }
        }
        return j;
    }
}
