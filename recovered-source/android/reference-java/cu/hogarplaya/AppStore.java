package cu.hogarplaya;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class AppStore extends SQLiteOpenHelper {
    private static AppStore instance;
    public final String deviceId;
    public final SharedPreferences prefs;

    private AppStore(Context context) {
        super(context, "hogar.db", (SQLiteDatabase.CursorFactory) null, 3);
        this.prefs = context.getSharedPreferences("hogar", 0);
        String string = this.prefs.getString("deviceId", "");
        if (string.isEmpty()) {
            string = UUID.randomUUID().toString();
            this.prefs.edit().putString("deviceId", string).commit();
        }
        this.deviceId = string;
        getWritableDatabase().execSQL("UPDATE operations SET status='PENDING' WHERE status='SENDING'");
        getWritableDatabase().execSQL("UPDATE plan_changes SET status='PENDING' WHERE status='SENDING'");
    }

    public static synchronized AppStore get(Context context) {
        AppStore appStore;
        synchronized (AppStore.class) {
            if (instance == null) {
                instance = new AppStore(context.getApplicationContext());
            }
            appStore = instance;
        }
        return appStore;
    }

    private void shoppingTables(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shopping_sessions(id TEXT PRIMARY KEY,day TEXT NOT NULL UNIQUE,payload TEXT NOT NULL)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shopping_lines(id TEXT PRIMARY KEY,session_id TEXT NOT NULL,payload TEXT NOT NULL,operation_id TEXT NOT NULL DEFAULT '',FOREIGN KEY(session_id) REFERENCES shopping_sessions(id))");
    }

    public synchronized void acknowledge(JSONArray jSONArray, JSONArray jSONArray2) throws Exception {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (int i = 0; i < jSONArray2.length(); i++) {
            JSONObject jSONObject = jSONArray2.getJSONObject(i);
            String string = jSONObject.getString("id");
            if (linkedHashMap.containsKey(string)) {
                throw new IllegalArgumentException("Respuesta de sincronización repetida.");
            }
            linkedHashMap.put(string, jSONObject);
        }
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            try {
                String string2 = jSONArray.getJSONObject(i2).getString("id");
                JSONObject jSONObject2 = (JSONObject) linkedHashMap.get(string2);
                ContentValues contentValues = new ContentValues();
                String optString = jSONObject2 == null ? "retry" : jSONObject2.optString("status");
                if (!optString.equals("accepted") && !optString.equals("duplicate")) {
                    if (optString.equals("rejected")) {
                        contentValues.put("status", "REJECTED");
                        contentValues.put("error_code", jSONObject2.optString("code"));
                        contentValues.put("error_message", jSONObject2.optString("message"));
                    } else {
                        contentValues.put("status", "PENDING");
                        contentValues.put("error_code", jSONObject2 == null ? "" : jSONObject2.optString("code"));
                        contentValues.put("error_message", jSONObject2 == null ? "La respuesta quedó incompleta; se reintentará." : jSONObject2.optString("message"));
                    }
                    writableDatabase.update("operations", contentValues, "id=? AND status='SENDING'", new String[]{string2});
                }
                contentValues.put("status", "SYNCED");
                contentValues.put("error_code", "");
                contentValues.put("error_message", "");
                contentValues.put("server_at", jSONObject2.optString("serverAt"));
                contentValues.put("sheet", jSONObject2.optString("sheet"));
                contentValues.put("sheet_row", Integer.valueOf(jSONObject2.optInt("row")));
                writableDatabase.update("operations", contentValues, "id=? AND status='SENDING'", new String[]{string2});
            } finally {
                writableDatabase.endTransaction();
            }
        }
        writableDatabase.setTransactionSuccessful();
    }

    public synchronized JSONObject backup() throws Exception {
        return new JSONObject().put("format", "hogar-playa-backup-1").put("createdAt", Instant.now().toString()).put("householdId", this.prefs.getString("householdId", "")).put("profiles", profiles()).put("operations", operations()).put("planChanges", PlanStore.changes(this)).put("shopping", Shopping.export(this));
    }

    public synchronized String cache(String str, String str2) {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT payload FROM cache WHERE name=?", new String[]{str});
        try {
            if (rawQuery.moveToFirst()) {
                str2 = rawQuery.getString(0);
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
        return str2;
    }

    public synchronized void cache(String str, Object obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", str);
        contentValues.put("payload", obj.toString());
        getWritableDatabase().insertWithOnConflict("cache", null, contentValues, 5);
    }

    public synchronized JSONArray cart(String str) throws Exception {
        JSONArray jSONArray;
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT payload FROM carts WHERE profile_id=?", new String[]{str});
        try {
            jSONArray = new JSONArray(rawQuery.moveToFirst() ? rawQuery.getString(0) : "[]");
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
        return jSONArray;
    }

    public synchronized int checkout(String str, String str2) throws Exception {
        return checkout(str, str2, false);
    }

    public synchronized int checkout(String str, String str2, boolean z) throws Exception {
        JSONArray jSONArray;
        if (Shopping.hasSession(this)) {
            throw new IllegalArgumentException("Ya tienes una jornada para hoy. Pasa la cesta al Modo compra y confirma los productos dentro de su tope.");
        }
        JSONArray cart = cart(str);
        if (cart.length() == 0) {
            throw new IllegalArgumentException("Tu carrito está vacío.");
        }
        for (int i = 0; i < cart.length(); i++) {
            String repeatWarning = Shopping.repeatWarning(this, cart.getJSONObject(i));
            if (!repeatWarning.isEmpty() && !z) {
                throw new IllegalArgumentException("REPETIDA: " + repeatWarning);
            }
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (int i2 = 0; i2 < cart.length(); i2++) {
            JSONObject jSONObject = cart.getJSONObject(i2);
            String str3 = jSONObject.getString("category") + "|" + jSONObject.getString("currency") + "|" + jSONObject.optString("rate") + "|" + jSONObject.optString("planId") + "|" + jSONObject.optString("paymentFormat", "Efectivo");
            if (!linkedHashMap.containsKey(str3)) {
                linkedHashMap.put(str3, new JSONArray());
            }
            ((JSONArray) linkedHashMap.get(str3)).put(jSONObject);
        }
        String uuid = UUID.randomUUID().toString();
        jSONArray = new JSONArray();
        for (JSONArray jSONArray2 : linkedHashMap.values()) {
            StringBuilder sb = new StringBuilder("Compra: ");
            long j = 0;
            for (int i3 = 0; i3 < jSONArray2.length(); i3++) {
                JSONObject jSONObject2 = jSONArray2.getJSONObject(i3);
                j += Money.lineTotal(jSONObject2.getString("quantity"), jSONObject2.getString("unitPrice"));
                if (i3 > 0) {
                    sb.append(", ");
                }
                sb.append(jSONObject2.getString("name")).append(" × ").append(jSONObject2.getString("quantity"));
            }
            JSONObject jSONObject3 = jSONArray2.getJSONObject(0);
            jSONArray.put(operation(str, new JSONObject().put("type", "Gasto").put("category", jSONObject3.getString("category")).put("date", str2).put("amount", Money.decimal(j)).put("currency", jSONObject3.getString("currency")).put("rate", jSONObject3.optString("rate")).put("planId", jSONObject3.optString("planId")).put("paymentFormat", jSONObject3.optString("paymentFormat", "Efectivo")).put("concept", sb.substring(0, Math.min(500, sb.length()))).put("items", jSONArray2).put("batchId", uuid)));
        }
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        for (int i4 = 0; i4 < jSONArray.length(); i4++) {
            try {
                insert(writableDatabase, jSONArray.getJSONObject(i4));
            } catch (Throwable th) {
                writableDatabase.endTransaction();
                throw th;
            }
        }
        writableDatabase.delete("carts", "profile_id=?", new String[]{str});
        writableDatabase.setTransactionSuccessful();
        writableDatabase.endTransaction();
        return jSONArray.length();
    }

    public synchronized void correctRejected(String str, String str2, JSONObject jSONObject) throws Exception {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT status,error_code,profile_id,payload FROM operations WHERE id=?", new String[]{str2});
        try {
            if (!rawQuery.moveToFirst() || !rawQuery.getString(0).equals("REJECTED") || !rawQuery.getString(2).equals(str) || rawQuery.getString(1).equals("ID_CONFLICTO")) {
                throw new IllegalArgumentException("Ese movimiento no puede modificarse aquí. Revísalo en el Sheet.");
            }
            JSONObject jSONObject2 = new JSONObject(rawQuery.getString(3));
            if (jSONObject2.optJSONObject("shopping") != null) {
                if (!jSONObject2.optString("amount").equals(jSONObject.optString("amount")) || !jSONObject2.optString("currency").equals(jSONObject.optString("currency")) || !jSONObject2.optString("date").equals(jSONObject.optString("date"))) {
                    throw new IllegalArgumentException("Esta compra pertenece a una jornada con tope. Corrige categoría, formato o tasa; un cambio del importe se revisa en el Sheet.");
                }
                jSONObject.put("shopping", jSONObject2.getJSONObject("shopping"));
            }
            jSONObject.put("id", str2);
            JSONObject operation = operation(str, jSONObject);
            ContentValues contentValues = new ContentValues();
            contentValues.put("payload", operation.toString());
            contentValues.put("status", "PENDING");
            contentValues.put("error_code", "");
            contentValues.put("error_message", "");
            getWritableDatabase().update("operations", contentValues, "id=? AND status='REJECTED'", new String[]{str2});
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
    }

    public synchronized int count(String str) {
        int i;
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM operations WHERE status=?", new String[]{str});
        try {
            rawQuery.moveToFirst();
            i = rawQuery.getInt(0);
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
        return i;
    }

    public synchronized String createProfile(String str, String str2, String str3) throws Exception {
        String uuid;
        String trim = str.trim();
        if (trim.isEmpty() || trim.length() > 40 || trim.matches(".*[\\r\\n\\t].*")) {
            throw new IllegalArgumentException("Escribe un nombre de hasta 40 caracteres.");
        }
        if (profiles().length() >= 20) {
            throw new IllegalArgumentException("Ya hay 20 perfiles en este teléfono.");
        }
        uuid = str3.isEmpty() ? UUID.randomUUID().toString() : Money.uuid(str3);
        String salt = Vault.salt();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", uuid);
        contentValues.put("name", trim);
        contentValues.put("salt", salt);
        contentValues.put("pin_hash", Vault.pinHash(str2, salt));
        getWritableDatabase().insertOrThrow("profiles", null, contentValues);
        return uuid;
    }

    public synchronized boolean hasPending() {
        return ((count("PENDING") + count("SENDING")) + PlanStore.count(this, "PENDING")) + PlanStore.count(this, "SENDING") > 0;
    }

    void insert(SQLiteDatabase sQLiteDatabase, JSONObject jSONObject) throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", jSONObject.getString("id"));
        contentValues.put("profile_id", jSONObject.getString("actorId"));
        contentValues.put("payload", jSONObject.toString());
        sQLiteDatabase.insertOrThrow("operations", null, contentValues);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onConfigure(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.setForeignKeyConstraintsEnabled(true);
        sQLiteDatabase.enableWriteAheadLogging();
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE profiles(id TEXT PRIMARY KEY,name TEXT NOT NULL,pin_hash TEXT NOT NULL,salt TEXT NOT NULL,failures INTEGER NOT NULL DEFAULT 0,locked_until INTEGER NOT NULL DEFAULT 0)");
        sQLiteDatabase.execSQL("CREATE TABLE operations(seq INTEGER PRIMARY KEY AUTOINCREMENT,id TEXT NOT NULL UNIQUE,profile_id TEXT NOT NULL,payload TEXT NOT NULL,status TEXT NOT NULL DEFAULT 'PENDING',attempts INTEGER NOT NULL DEFAULT 0,error_code TEXT NOT NULL DEFAULT '',error_message TEXT NOT NULL DEFAULT '',server_at TEXT NOT NULL DEFAULT '',sheet TEXT NOT NULL DEFAULT '',sheet_row INTEGER NOT NULL DEFAULT 0,FOREIGN KEY(profile_id) REFERENCES profiles(id))");
        sQLiteDatabase.execSQL("CREATE INDEX operations_pending ON operations(status,seq)");
        sQLiteDatabase.execSQL("CREATE TABLE carts(profile_id TEXT PRIMARY KEY,payload TEXT NOT NULL,FOREIGN KEY(profile_id) REFERENCES profiles(id))");
        sQLiteDatabase.execSQL("CREATE TABLE cache(name TEXT PRIMARY KEY,payload TEXT NOT NULL)");
        shoppingTables(sQLiteDatabase);
        PlanStore.tables(sQLiteDatabase);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        if (i < 2) {
            shoppingTables(sQLiteDatabase);
        }
        if (i < 3) {
            PlanStore.tables(sQLiteDatabase);
        }
    }

    JSONObject operation(String str, JSONObject jSONObject) throws Exception {
        String str2;
        JSONObject profile = profile(str);
        String string = jSONObject.getString("type");
        String str3 = "currency";
        String string2 = jSONObject.getString("currency");
        if (!new JSONArray("[\"Gasto\",\"Ingreso\",\"Aporte ahorro\",\"Aporte fondo\",\"Retiro ahorro\",\"Cambio\"]").toString().contains("\"" + string + "\"")) {
            throw new IllegalArgumentException("Tipo no válido.");
        }
        if (!string2.equals("CUP") && !string2.equals("USD")) {
            throw new IllegalArgumentException("Moneda no válida.");
        }
        String str4 = "amount";
        long cents = Money.cents(jSONObject.getString("amount"));
        String str5 = "concept";
        String trim = jSONObject.optString("concept").trim();
        if (trim.isEmpty() || trim.length() > 500) {
            throw new IllegalArgumentException("Escribe un concepto de hasta 500 caracteres.");
        }
        String str6 = "items";
        JSONArray jSONArray = jSONObject.optJSONArray("items") == null ? new JSONArray() : new JSONArray(jSONObject.getJSONArray("items").toString());
        if (jSONArray.length() <= 50 && (jSONArray.length() <= 0 || string.equals("Gasto"))) {
            long j = 0;
            int i = 0;
            while (true) {
                str2 = str6;
                if (i >= jSONArray.length()) {
                    break;
                }
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                long lineTotal = j + Money.lineTotal(jSONObject2.getString("quantity"), jSONObject2.getString("unitPrice"));
                jSONObject2.put("quantity", AppStore$$ExternalSyntheticBackportWithForwarding0.m(new BigDecimal(jSONObject2.getString("quantity").replace(',', '.'))).toPlainString());
                jSONObject2.put("unitPrice", Money.decimal(Money.cents(jSONObject2.getString("unitPrice"))));
                i++;
                str6 = str2;
                profile = profile;
                str5 = str5;
                trim = trim;
                str3 = str3;
                j = lineTotal;
                str4 = str4;
            }
            JSONObject jSONObject3 = profile;
            String str7 = str3;
            String str8 = str4;
            String str9 = str5;
            String str10 = trim;
            if (jSONArray.length() > 0 && j != cents) {
                throw new IllegalArgumentException("El total no coincide con los artículos.");
            }
            if (jSONArray.toString().length() > 20000) {
                throw new IllegalArgumentException("El detalle de la compra es demasiado largo.");
            }
            String optString = jSONObject.optString("id", UUID.randomUUID().toString());
            Money.uuid(optString);
            String optString2 = (string2.equals("CUP") || string.equals("Cambio")) ? jSONObject.optString("paymentFormat", "Efectivo") : "";
            if (!optString2.isEmpty() && !optString2.equals("Efectivo") && !optString2.equals("Transferencia")) {
                throw new IllegalArgumentException("Selecciona Efectivo o Transferencia.");
            }
            if ((string2.equals("CUP") || string.equals("Cambio")) && optString2.isEmpty()) {
                throw new IllegalArgumentException("Selecciona el formato de los CUP.");
            }
            return new JSONObject().put("paymentFormat", optString2).put("shopping", jSONObject.optJSONObject("shopping")).put("id", optString).put("type", string).put("category", jSONObject.getString("category")).put("date", Money.date(jSONObject.getString("date"))).put(str8, Money.decimal(cents)).put(str7, string2).put("rate", Money.rate(jSONObject.optString("rate"), string2.equals("CUP") || string.equals("Cambio"))).put("referenceRate", Money.rate(jSONObject.optString("referenceRate"), false)).put(str9, str10).put("actorId", str).put("actorName", jSONObject3.getString("name")).put("deviceId", this.deviceId).put("clientCreatedAt", Instant.now().toString()).put("batchId", jSONObject.optString("batchId")).put("planId", jSONObject.optString("planId")).put(str2, jSONArray);
        }
        throw new IllegalArgumentException("Revisa el detalle de la compra.");
    }

    public synchronized JSONArray operations() throws Exception {
        JSONArray jSONArray;
        jSONArray = new JSONArray();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT payload,status,error_code,error_message,server_at,sheet,sheet_row,attempts FROM operations ORDER BY seq DESC", null);
        while (rawQuery.moveToNext()) {
            try {
                JSONObject jSONObject = new JSONObject(rawQuery.getString(0));
                jSONObject.put("status", rawQuery.getString(1)).put("errorCode", rawQuery.getString(2)).put("error", rawQuery.getString(3)).put("serverAt", rawQuery.getString(4)).put("sheet", rawQuery.getString(5)).put("row", rawQuery.getInt(6)).put("attempts", rawQuery.getInt(7));
                jSONArray.put(jSONObject);
            } finally {
            }
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        return jSONArray;
    }

    public synchronized JSONArray pending() throws Exception {
        JSONArray jSONArray;
        jSONArray = new JSONArray();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT payload FROM operations WHERE status='PENDING' ORDER BY seq LIMIT 20", null);
        int i = 0;
        while (rawQuery.moveToNext()) {
            try {
                String string = rawQuery.getString(0);
                i += string.getBytes(StandardCharsets.UTF_8).length;
                if (i > 150000 && jSONArray.length() > 0) {
                    break;
                }
                jSONArray.put(new JSONObject(string));
            } finally {
            }
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        return jSONArray;
    }

    public synchronized JSONObject profile(String str) throws Exception {
        JSONObject put;
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT id,name FROM profiles WHERE id=?", new String[]{str});
        try {
            if (!rawQuery.moveToFirst()) {
                throw new IllegalArgumentException("Perfil no encontrado.");
            }
            put = new JSONObject().put("id", rawQuery.getString(0)).put("name", rawQuery.getString(1));
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
        return put;
    }

    public synchronized JSONArray profiles() throws Exception {
        JSONArray jSONArray;
        jSONArray = new JSONArray();
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT id,name FROM profiles ORDER BY rowid", null);
        while (rawQuery.moveToNext()) {
            try {
                jSONArray.put(new JSONObject().put("id", rawQuery.getString(0)).put("name", rawQuery.getString(1)));
            } finally {
            }
        }
        if (rawQuery != null) {
            rawQuery.close();
        }
        return jSONArray;
    }

    public synchronized void resetPin(String str, String str2) throws Exception {
        profile(str);
        String salt = Vault.salt();
        ContentValues contentValues = new ContentValues();
        contentValues.put("salt", salt);
        contentValues.put("pin_hash", Vault.pinHash(str2, salt));
        contentValues.put("failures", (Integer) 0);
        contentValues.put("locked_until", (Integer) 0);
        getWritableDatabase().update("profiles", contentValues, "id=?", new String[]{str});
    }

    public synchronized void retry(JSONArray jSONArray, String str) throws Exception {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        for (int i = 0; i < jSONArray.length(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("status", "PENDING");
            contentValues.put("error_message", str);
            writableDatabase.update("operations", contentValues, "id=? AND status='SENDING'", new String[]{jSONArray.getJSONObject(i).getString("id")});
        }
    }

    public synchronized void saveCart(String str, JSONArray jSONArray) throws Exception {
        if (jSONArray.length() > 50) {
            throw new IllegalArgumentException("El carrito admite hasta 50 artículos.");
        }
        JSONArray jSONArray2 = new JSONArray();
        for (int i = 0; i < jSONArray.length(); i++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            Money.lineTotal(jSONObject.getString("quantity"), jSONObject.getString("unitPrice"));
            String string = jSONObject.getString("currency");
            if (!string.equals("CUP") && !string.equals("USD")) {
                throw new IllegalArgumentException("Moneda no válida.");
            }
            if (jSONObject.optString("name").isEmpty() || jSONObject.optString("name").length() > 120) {
                throw new IllegalArgumentException("Nombre del artículo no válido.");
            }
            jSONObject.put("quantity", AppStore$$ExternalSyntheticBackportWithForwarding0.m(new BigDecimal(jSONObject.getString("quantity").replace(',', '.'))).toPlainString());
            jSONObject.put("unitPrice", Money.decimal(Money.cents(jSONObject.getString("unitPrice"))));
            jSONObject.put("rate", Money.rate(jSONObject.optString("rate"), string.equals("CUP")));
            jSONArray2.put(jSONObject);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_id", str);
        contentValues.put("payload", jSONArray2.toString());
        getWritableDatabase().insertWithOnConflict("carts", null, contentValues, 5);
    }

    public synchronized String saveOperation(String str, JSONObject jSONObject) throws Exception {
        String optString;
        String str2;
        JSONObject operation = operation(str, jSONObject);
        String string = operation.getString("id");
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT payload FROM operations WHERE id=?", new String[]{string});
        try {
            if (!rawQuery.moveToFirst()) {
                if (rawQuery != null) {
                    rawQuery.close();
                }
                insert(getWritableDatabase(), operation);
                return string;
            }
            JSONObject jSONObject2 = new JSONObject(rawQuery.getString(0));
            String[] strArr = {"actorId", "type", "category", "date", "amount", "currency", "rate", "referenceRate", "concept", "items", "batchId", "planId", "paymentFormat", "shopping"};
            for (int i = 0; i < 14; i++) {
                String str3 = strArr[i];
                if (str3.equals("paymentFormat")) {
                    if (!jSONObject2.optString("currency").equals("CUP") && !jSONObject2.optString("type").equals("Cambio")) {
                        str2 = "";
                        optString = jSONObject2.optString(str3, str2);
                    }
                    str2 = "Efectivo";
                    optString = jSONObject2.optString(str3, str2);
                } else {
                    optString = jSONObject2.optString(str3);
                }
                if (!optString.equals(operation.optString(str3))) {
                    throw new IllegalArgumentException("Este intento ya se guardó con otros datos. Revisa Movimientos antes de registrar otro gasto.");
                }
            }
            if (rawQuery != null) {
                rawQuery.close();
            }
            return string;
        } finally {
        }
    }

    public synchronized void sending(JSONArray jSONArray) throws Exception {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.beginTransaction();
        for (int i = 0; i < jSONArray.length(); i++) {
            try {
                writableDatabase.execSQL("UPDATE operations SET status='SENDING',attempts=attempts+1 WHERE id=? AND status='PENDING'", new Object[]{jSONArray.getJSONObject(i).getString("id")});
            } finally {
                writableDatabase.endTransaction();
            }
        }
        writableDatabase.setTransactionSuccessful();
    }

    public synchronized JSONObject snapshot() throws Exception {
        return new JSONObject(cache("snapshot", "{}"));
    }

    public synchronized void unlock(String str, String str2) throws Exception {
        Cursor rawQuery = getReadableDatabase().rawQuery("SELECT pin_hash,salt,failures,locked_until FROM profiles WHERE id=?", new String[]{str});
        try {
            if (!rawQuery.moveToFirst()) {
                throw new IllegalArgumentException("Perfil no encontrado.");
            }
            if (rawQuery.getLong(3) > System.currentTimeMillis()) {
                throw new IllegalArgumentException("Espera cinco minutos antes de probar otro PIN.");
            }
            int i = 0;
            if (!Vault.same(rawQuery.getString(0), Vault.pinHash(str2, rawQuery.getString(1)))) {
                int i2 = rawQuery.getInt(2) + 1;
                ContentValues contentValues = new ContentValues();
                if (i2 < 5) {
                    i = i2;
                }
                contentValues.put("failures", Integer.valueOf(i));
                contentValues.put("locked_until", Long.valueOf(i2 >= 5 ? System.currentTimeMillis() + 300000 : 0L));
                getWritableDatabase().update("profiles", contentValues, "id=?", new String[]{str});
                throw new IllegalArgumentException("PIN incorrecto.");
            }
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put("failures", (Integer) 0);
            contentValues2.put("locked_until", (Integer) 0);
            getWritableDatabase().update("profiles", contentValues2, "id=?", new String[]{str});
            if (rawQuery != null) {
                rawQuery.close();
            }
        } finally {
        }
    }
}
