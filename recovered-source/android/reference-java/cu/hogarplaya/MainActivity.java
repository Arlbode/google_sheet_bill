package cu.hogarplaya;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import cu.hogarplaya.MainActivity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class MainActivity extends Activity {
    private ConnectivityManager connectivity;
    private AppStore store;
    private WebView web;
    private volatile boolean foreground = false;
    private volatile String active = "";
    private volatile long touched = 0;
    private volatile long recoveryUntil = 0;
    private String exportData = "";
    private boolean openPlanning = false;
    private final ConnectivityManager.NetworkCallback network = new ConnectivityManager.NetworkCallback() { // from class: cu.hogarplaya.MainActivity.1
        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onAvailable(Network network) {
            SyncEngine.kick(MainActivity.this);
            MainActivity.this.notifyState();
        }

        @Override // android.net.ConnectivityManager.NetworkCallback
        public void onLost(Network network) {
            MainActivity.this.notifyState();
        }
    };

    public final class Bridge {
        public Bridge() {
        }

        @JavascriptInterface
        public void call(final String str) {
            SyncEngine.WORK.execute(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.m6lambda$call$5$cuhogarplayaMainActivity$Bridge(str);
                }
            });
        }

        /* renamed from: lambda$call$0$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /* synthetic */ void m1lambda$call$0$cuhogarplayaMainActivity$Bridge() {
            MainActivity.this.finish();
        }

        /* renamed from: lambda$call$1$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /* synthetic */ void m2lambda$call$1$cuhogarplayaMainActivity$Bridge() {
            ((InputMethodManager) MainActivity.this.getSystemService("input_method")).hideSoftInputFromWindow(MainActivity.this.web.getWindowToken(), 0);
            MainActivity.this.web.clearFocus();
        }

        /* renamed from: lambda$call$2$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /* synthetic */ void m3lambda$call$2$cuhogarplayaMainActivity$Bridge() {
            if (Build.VERSION.SDK_INT >= 33 && MainActivity.this.checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
                MainActivity.this.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 72);
            } else {
                MainActivity.this.startActivity(new Intent("android.settings.APP_NOTIFICATION_SETTINGS").putExtra("android.provider.extra.APP_PACKAGE", MainActivity.this.getPackageName()));
            }
        }

        /* renamed from: lambda$call$3$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /* synthetic */ void m4lambda$call$3$cuhogarplayaMainActivity$Bridge() {
            Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
            intent.setType("application/json");
            intent.addCategory("android.intent.category.OPENABLE");
            intent.putExtra("android.intent.extra.TITLE", "hogar-respaldo-" + Money.today() + ".json");
            MainActivity.this.startActivityForResult(intent, 30);
        }

        /* renamed from: lambda$call$4$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /* synthetic */ void m5lambda$call$4$cuhogarplayaMainActivity$Bridge(String str) {
            if (MainActivity.this.web != null) {
                MainActivity.this.web.evaluateJavascript(str, null);
            }
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARN: Removed duplicated region for block: B:100:0x04f5 A[Catch: Exception -> 0x04fd, TryCatch #1 {Exception -> 0x04fd, blocks: (B:94:0x04e6, B:97:0x04f9, B:100:0x04f5), top: B:93:0x04e6 }] */
        /* JADX WARN: Removed duplicated region for block: B:96:0x04f2  */
        /* renamed from: lambda$call$5$cu-hogarplaya-MainActivity$Bridge, reason: not valid java name */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        /* synthetic */ void m6lambda$call$5$cuhogarplayaMainActivity$Bridge(String str) {
            String str2;
            String str3;
            String str4;
            char c;
            boolean z;
            str2 = "";
            JSONObject jSONObject = new JSONObject();
            try {
            } catch (Exception e) {
                e = e;
            }
            if (str.length() > 220000) {
                throw new IllegalArgumentException("La petición es demasiado larga.");
            }
            JSONObject jSONObject2 = new JSONObject(str);
            String string = jSONObject2.getString("id");
            try {
                String string2 = jSONObject2.getString("action");
                JSONObject optJSONObject = jSONObject2.optJSONObject("data");
                if (optJSONObject == null) {
                    try {
                        optJSONObject = new JSONObject();
                    } catch (Exception e2) {
                        e = e2;
                        str2 = string;
                        try {
                            jSONObject.put("ok", false).put("error", e.getMessage() != null ? "No se pudo completar la acción." : e.getMessage());
                        } catch (Exception e3) {
                        }
                        str3 = str2;
                        final String str5 = "window.hogarReply(" + JSONObject.quote(str3) + "," + jSONObject.toString() + ")";
                        MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda5
                            @Override // java.lang.Runnable
                            public final void run() {
                                MainActivity.Bridge.this.m5lambda$call$4$cuhogarplayaMainActivity$Bridge(str5);
                            }
                        });
                    }
                }
                JSONObject jSONObject3 = new JSONObject();
                switch (string2.hashCode()) {
                    case -1666249097:
                        if (string2.equals("shoppingLines")) {
                            c = 7;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1659468838:
                        if (string2.equals("shoppingStart")) {
                            c = 6;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1577770628:
                        if (string2.equals("shoppingLine")) {
                            c = '\b';
                            break;
                        }
                        c = 65535;
                        break;
                    case -1289153612:
                        if (string2.equals("export")) {
                            c = 23;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1265213295:
                        if (string2.equals("dismissKeyboard")) {
                            c = 5;
                            break;
                        }
                        c = 65535;
                        break;
                    case -1159283714:
                        if (string2.equals("shoppingBuy")) {
                            c = 11;
                            break;
                        }
                        c = 65535;
                        break;
                    case -896473803:
                        if (string2.equals("planDiscard")) {
                            c = 14;
                            break;
                        }
                        c = 65535;
                        break;
                    case -840442044:
                        if (string2.equals("unlock")) {
                            c = 2;
                            break;
                        }
                        c = 65535;
                        break;
                    case -350362170:
                        if (string2.equals("resetPin")) {
                            c = 22;
                            break;
                        }
                        c = 65535;
                        break;
                    case -196238547:
                        if (string2.equals("shoppingImport")) {
                            c = '\t';
                            break;
                        }
                        c = 65535;
                        break;
                    case 3046176:
                        if (string2.equals("cart")) {
                            c = '\f';
                            break;
                        }
                        c = 65535;
                        break;
                    case 3127582:
                        if (string2.equals("exit")) {
                            c = 1;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3327275:
                        if (string2.equals("lock")) {
                            c = 4;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3433178:
                        if (string2.equals("pair")) {
                            c = 21;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3522941:
                        if (string2.equals("save")) {
                            c = 15;
                            break;
                        }
                        c = 65535;
                        break;
                    case 3545755:
                        if (string2.equals("sync")) {
                            c = 20;
                            break;
                        }
                        c = 65535;
                        break;
                    case 53946380:
                        if (string2.equals("shoppingRemove")) {
                            c = '\n';
                            break;
                        }
                        c = 65535;
                        break;
                    case 109757585:
                        if (string2.equals("state")) {
                            c = 0;
                            break;
                        }
                        c = 65535;
                        break;
                    case 955164778:
                        if (string2.equals("correct")) {
                            c = 17;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1272354024:
                        if (string2.equals("notifications")) {
                            c = 19;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1536904518:
                        if (string2.equals("checkout")) {
                            c = 16;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1868563526:
                        if (string2.equals("planSave")) {
                            c = '\r';
                            break;
                        }
                        c = 65535;
                        break;
                    case 1896099083:
                        if (string2.equals("testProtection")) {
                            c = 18;
                            break;
                        }
                        c = 65535;
                        break;
                    case 1937091853:
                        if (string2.equals("createProfile")) {
                            c = 3;
                            break;
                        }
                        c = 65535;
                        break;
                    default:
                        c = 65535;
                        break;
                }
                str4 = string;
                try {
                    switch (c) {
                        case 0:
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 1:
                            MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda1
                                @Override // java.lang.Runnable
                                public final void run() {
                                    MainActivity.Bridge.this.m1lambda$call$0$cuhogarplayaMainActivity$Bridge();
                                }
                            });
                            break;
                        case 2:
                            MainActivity.this.store.unlock(optJSONObject.getString("profileId"), optJSONObject.getString("pin"));
                            MainActivity.this.active = MainActivity.this.foreground ? optJSONObject.getString("profileId") : "";
                            MainActivity.this.touched = SystemClock.elapsedRealtime();
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 3:
                            if (MainActivity.this.store.profiles().length() > 0) {
                                MainActivity.this.requireProfile();
                            }
                            String optString = optJSONObject.optString("remoteId");
                            String string3 = optJSONObject.getString("name");
                            if (!optString.isEmpty()) {
                                JSONArray optJSONArray = MainActivity.this.store.snapshot().optJSONArray("profiles");
                                if (optJSONArray != null) {
                                    z = false;
                                    for (int i = 0; i < optJSONArray.length(); i++) {
                                        JSONObject jSONObject4 = optJSONArray.getJSONObject(i);
                                        if (jSONObject4.getString("id").equals(optString) && jSONObject4.getString("name").equals(string3)) {
                                            z = true;
                                        }
                                    }
                                } else {
                                    z = false;
                                }
                                if (!z) {
                                    throw new IllegalArgumentException("Actualiza los perfiles de la hoja antes de elegirlo.");
                                }
                            }
                            MainActivity.this.active = MainActivity.this.foreground ? MainActivity.this.store.createProfile(string3, optJSONObject.getString("pin"), optString) : "";
                            MainActivity.this.touched = SystemClock.elapsedRealtime();
                            jSONObject3 = MainActivity.this.state();
                            SyncEngine.schedule(MainActivity.this);
                            break;
                        case 4:
                            MainActivity.this.active = "";
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 5:
                            MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda2
                                @Override // java.lang.Runnable
                                public final void run() {
                                    MainActivity.Bridge.this.m2lambda$call$1$cuhogarplayaMainActivity$Bridge();
                                }
                            });
                            break;
                        case 6:
                            MainActivity.this.requireProfile();
                            Shopping.start(MainActivity.this.store, optJSONObject);
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 7:
                            MainActivity.this.requireProfile();
                            Shopping.saveLines(MainActivity.this.store, optJSONObject.getJSONArray("items"));
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case '\b':
                            MainActivity.this.requireProfile();
                            Shopping.saveLine(MainActivity.this.store, optJSONObject);
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case '\t':
                            MainActivity.this.requireProfile();
                            Shopping.importCart(MainActivity.this.store, MainActivity.this.active);
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case '\n':
                            MainActivity.this.requireProfile();
                            Shopping.removeLine(MainActivity.this.store, optJSONObject.getString("id"));
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 11:
                            MainActivity.this.requireProfile();
                            Shopping.buy(MainActivity.this.store, MainActivity.this.active, optJSONObject.getString("id"), optJSONObject.optBoolean("allowRepeat"));
                            jSONObject3 = MainActivity.this.state();
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case '\f':
                            MainActivity.this.requireProfile();
                            MainActivity.this.store.saveCart(MainActivity.this.active, optJSONObject.getJSONArray("items"));
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case '\r':
                            MainActivity.this.requireProfile();
                            PlanStore.save(MainActivity.this.store, MainActivity.this.active, optJSONObject);
                            jSONObject3 = MainActivity.this.state();
                            PlanNotifications.check(MainActivity.this);
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 14:
                            MainActivity.this.requireProfile();
                            PlanStore.discardRejected(MainActivity.this.store, optJSONObject.getString("id"));
                            jSONObject3 = MainActivity.this.state();
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 15:
                            MainActivity.this.requireProfile();
                            if (optJSONObject.optString("type").equals("Cambio")) {
                                Planning.validateChange(MainActivity.this.store, optJSONObject);
                            }
                            MainActivity.this.store.saveOperation(MainActivity.this.active, optJSONObject);
                            jSONObject3 = MainActivity.this.state();
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 16:
                            MainActivity.this.requireProfile();
                            jSONObject3 = MainActivity.this.state().put("savedCount", MainActivity.this.store.checkout(MainActivity.this.active, optJSONObject.getString("date"), optJSONObject.optBoolean("allowRepeat")));
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 17:
                            MainActivity.this.requireProfile();
                            if (optJSONObject.optString("type").equals("Cambio")) {
                                Planning.validateChange(MainActivity.this.store, optJSONObject);
                            }
                            MainActivity.this.store.correctRejected(MainActivity.this.active, optJSONObject.getString("id"), optJSONObject);
                            jSONObject3 = MainActivity.this.state();
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 18:
                            MainActivity.this.requireProfile();
                            jSONObject3 = PlanNotifications.test(MainActivity.this);
                            break;
                        case 19:
                            MainActivity.this.requireProfile();
                            MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda3
                                @Override // java.lang.Runnable
                                public final void run() {
                                    MainActivity.Bridge.this.m3lambda$call$2$cuhogarplayaMainActivity$Bridge();
                                }
                            });
                            break;
                        case 20:
                            SyncEngine.kick(MainActivity.this);
                            jSONObject3 = MainActivity.this.state();
                            break;
                        case 21:
                            jSONObject3 = SyncEngine.pair(MainActivity.this, optJSONObject.getString("url"), optJSONObject.getString("code"), optJSONObject.optString("deviceName", Build.MODEL));
                            MainActivity.this.recoveryUntil = SystemClock.elapsedRealtime() + 300000;
                            SyncEngine.kick(MainActivity.this);
                            break;
                        case 22:
                            if (SystemClock.elapsedRealtime() >= MainActivity.this.recoveryUntil) {
                                throw new IllegalArgumentException("Primero conecta el teléfono con un código nuevo generado desde el Sheet.");
                            }
                            MainActivity.this.store.resetPin(optJSONObject.getString("profileId"), optJSONObject.getString("pin"));
                            MainActivity.this.recoveryUntil = 0L;
                            jSONObject3 = new JSONObject().put("reset", true);
                            break;
                        case 23:
                            MainActivity.this.requireProfile();
                            MainActivity.this.exportData = MainActivity.this.store.backup().toString(2);
                            MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda4
                                @Override // java.lang.Runnable
                                public final void run() {
                                    MainActivity.Bridge.this.m4lambda$call$3$cuhogarplayaMainActivity$Bridge();
                                }
                            });
                            break;
                        default:
                            throw new IllegalArgumentException("Acción no admitida.");
                    }
                    jSONObject.put("ok", true).put("data", jSONObject3);
                    str3 = str4;
                } catch (Exception e4) {
                    e = e4;
                    str2 = str4;
                    jSONObject.put("ok", false).put("error", e.getMessage() != null ? "No se pudo completar la acción." : e.getMessage());
                    str3 = str2;
                    final String str52 = "window.hogarReply(" + JSONObject.quote(str3) + "," + jSONObject.toString() + ")";
                    MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda5
                        @Override // java.lang.Runnable
                        public final void run() {
                            MainActivity.Bridge.this.m5lambda$call$4$cuhogarplayaMainActivity$Bridge(str52);
                        }
                    });
                }
            } catch (Exception e5) {
                e = e5;
                str4 = string;
            }
            final String str522 = "window.hogarReply(" + JSONObject.quote(str3) + "," + jSONObject.toString() + ")";
            MainActivity.this.runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$Bridge$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    MainActivity.Bridge.this.m5lambda$call$4$cuhogarplayaMainActivity$Bridge(str522);
                }
            });
        }
    }

    static /* synthetic */ WindowInsets lambda$onCreate$0(View view, WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT < 30) {
            view.setPadding(windowInsets.getSystemWindowInsetLeft(), windowInsets.getSystemWindowInsetTop(), windowInsets.getSystemWindowInsetRight(), windowInsets.getSystemWindowInsetBottom());
            return windowInsets.consumeSystemWindowInsets();
        }
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
        view.setPadding(insets.left, insets.top, insets.right, Math.max(insets.bottom, windowInsets.getInsets(WindowInsets.Type.ime()).bottom));
        return WindowInsets.CONSUMED;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyState() {
        runOnUiThread(new Runnable() { // from class: cu.hogarplaya.MainActivity$$ExternalSyntheticLambda2
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.m0lambda$notifyState$1$cuhogarplayaMainActivity();
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requireProfile() {
        if (!this.foreground || this.active.isEmpty() || SystemClock.elapsedRealtime() - this.touched > 300000) {
            this.active = "";
            throw new IllegalArgumentException("Desbloquea tu perfil para continuar.");
        }
        this.touched = SystemClock.elapsedRealtime();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public JSONObject state() throws Exception {
        if (!this.active.isEmpty() && SystemClock.elapsedRealtime() - this.touched > 300000) {
            this.active = "";
        }
        JSONObject put = new JSONObject().put("profiles", this.store.profiles()).put("activeProfileId", this.active).put("online", SyncEngine.online(this)).put("syncing", SyncEngine.isRunning()).put("paired", !this.store.prefs.getString("endpoint", "").isEmpty()).put("authError", this.store.prefs.getBoolean("authError", false)).put("pending", this.store.count("PENDING") + this.store.count("SENDING")).put("rejected", this.store.count("REJECTED")).put("endpoint", this.store.prefs.getString("endpoint", "")).put("deviceName", this.store.prefs.getString("deviceName", Build.MODEL)).put("lastSync", this.store.prefs.getString("lastSync", "")).put("syncError", this.store.prefs.getString("syncError", "")).put("today", Money.today()).put("canRecover", SystemClock.elapsedRealtime() < this.recoveryUntil);
        put.put("notificationsEnabled", PlanNotifications.enabled(this)).put("planPending", PlanStore.count(this.store, "PENDING") + PlanStore.count(this.store, "SENDING")).put("planRejected", PlanStore.count(this.store, "REJECTED"));
        if (!this.active.isEmpty()) {
            put.put("profile", this.store.profile(this.active)).put("snapshot", this.store.snapshot()).put("operations", this.store.operations()).put("cart", this.store.cart(this.active)).put("shopping", Shopping.state(this.store)).put("deviceId", this.store.deviceId).put("planChanges", PlanStore.changes(this.store)).put("planning", Planning.evaluate(PlanStore.snapshot(this.store), this.store.operations(), Money.today())).put("openPlanning", this.openPlanning);
            this.openPlanning = false;
        }
        return put;
    }

    /* renamed from: lambda$notifyState$1$cu-hogarplaya-MainActivity, reason: not valid java name */
    /* synthetic */ void m0lambda$notifyState$1$cuhogarplayaMainActivity() {
        if (this.web != null) {
            this.web.evaluateJavascript("window.hogarChanged && window.hogarChanged()", null);
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 30 && i2 == -1 && intent != null && intent.getData() != null) {
            try {
                OutputStream openOutputStream = getContentResolver().openOutputStream(intent.getData());
                try {
                    openOutputStream.write(this.exportData.getBytes(StandardCharsets.UTF_8));
                    Toast.makeText(this, "Respaldo guardado", 1).show();
                    if (openOutputStream != null) {
                        openOutputStream.close();
                    }
                } finally {
                }
            } catch (Exception e) {
                Toast.makeText(this, "No se pudo guardar el respaldo", 1).show();
            }
        }
        this.exportData = "";
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        this.web.evaluateJavascript("window.hogarBack && window.hogarBack()", null);
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.store = AppStore.get(this);
        try {
            InputStream open = getAssets().open("catalog.json");
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = open.read(bArr);
                    if (read == -1) {
                        break;
                    } else {
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                }
                this.store.cache("catalog", new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
                if (open != null) {
                    open.close();
                }
            } finally {
            }
        } catch (Exception e) {
        }
        this.openPlanning = getIntent().getBooleanExtra("openPlanning", false);
        getWindow().addFlags(8192);
        getWindow().setSoftInputMode(16);
        getWindow().setStatusBarColor(Color.rgb(248, 248, 243));
        getWindow().setNavigationBarColor(-1);
        getWindow().getDecorView().setSystemUiVisibility(8208);
        this.web = new WebView(this);
        this.web.setBackgroundColor(Color.rgb(248, 248, 243));
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.addView(this.web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(frameLayout);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        frameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: cu.hogarplaya.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnApplyWindowInsetsListener
            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return MainActivity.lambda$onCreate$0(view, windowInsets);
            }
        });
        frameLayout.requestApplyInsets();
        this.web.getSettings().setJavaScriptEnabled(true);
        this.web.getSettings().setDomStorageEnabled(false);
        this.web.getSettings().setAllowFileAccess(false);
        this.web.getSettings().setAllowContentAccess(false);
        this.web.getSettings().setMixedContentMode(1);
        this.web.getSettings().setBlockNetworkLoads(true);
        this.web.getSettings().setSaveFormData(false);
        this.web.getSettings().setSupportMultipleWindows(false);
        WebView.setWebContentsDebuggingEnabled(false);
        this.web.setWebViewClient(new WebViewClient() { // from class: cu.hogarplaya.MainActivity.2
            @Override // android.webkit.WebViewClient
            public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
                Uri url = webResourceRequest.getUrl();
                String path = url.getPath();
                if (url.getScheme().equals("https") && url.getHost().equals("hogar.local") && path != null && path.matches("/[A-Za-z0-9_.-]*")) {
                    if (path.equals("/")) {
                        path = "/index.html";
                    }
                    try {
                        return new WebResourceResponse(path.endsWith(".html") ? "text/html" : path.endsWith(".js") ? "application/javascript" : path.endsWith(".css") ? "text/css" : path.endsWith(".json") ? "application/json" : "image/svg+xml", "UTF-8", 200, "OK", Collections.singletonMap("Cache-Control", "no-store"), MainActivity.this.getAssets().open(path.substring(1)));
                    } catch (Exception e2) {
                    }
                }
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
            }

            @Override // android.webkit.WebViewClient
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                return true;
            }
        });
        this.web.addJavascriptInterface(new Bridge(), "HogarNative");
        this.web.loadUrl("https://hogar.local/index.html");
        SyncEngine.listener = new Runnable() { // from class: cu.hogarplaya.MainActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.notifyState();
            }
        };
        this.connectivity = (ConnectivityManager) getSystemService("connectivity");
        this.connectivity.registerDefaultNetworkCallback(this.network);
        SyncEngine.kick(this);
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        SyncEngine.listener = null;
        try {
            this.connectivity.unregisterNetworkCallback(this.network);
        } catch (Exception e) {
        }
        if (this.web != null) {
            this.web.removeJavascriptInterface("HogarNative");
            this.web.destroy();
            this.web = null;
        }
        super.onDestroy();
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("openPlanning", false)) {
            this.openPlanning = true;
            notifyState();
        }
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        PlanNotifications.check(this);
        notifyState();
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        this.foreground = true;
    }

    @Override // android.app.Activity
    protected void onStop() {
        super.onStop();
        this.foreground = false;
        this.active = "";
        notifyState();
        SyncEngine.schedule(this);
    }
}
