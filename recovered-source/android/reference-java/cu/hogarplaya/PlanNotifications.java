package cu.hogarplaya;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Locale;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class PlanNotifications {
    private static final String CHANNEL = "cup-planning";

    private static String amount(double d) {
        return String.format(new Locale("es", "ES"), "%,.2f", Double.valueOf(d));
    }

    public static void check(Context context) {
        String str;
        try {
            AppStore appStore = AppStore.get(context);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL, "Necesidad de CUP y protección USD", 3));
            JSONObject evaluate = Planning.evaluate(PlanStore.snapshot(appStore), appStore.operations(), Money.today());
            if (evaluate.optBoolean("available") && evaluate.optBoolean("enabled") && evaluate.optBoolean("urgent") && !appStore.prefs.getBoolean("authError", false)) {
                if (enabled(context)) {
                    JSONObject optJSONObject = evaluate.optJSONObject("quote");
                    if (optJSONObject == null) {
                        str = "Tasa pendiente de verificar";
                    } else {
                        str = amount(optJSONObject.optDouble("rate")) + " CUP/USD · " + optJSONObject.optString("source") + (optJSONObject.optBoolean("fresh") ? " · consultada hoy" : " · última consulta " + optJSONObject.optString("day", "sin fecha"));
                    }
                    String str2 = evaluate.optDouble("maxUSD") > 0.0d ? "Necesitas CUP para pagos planificados" : "Revisa el plan: la reserva USD está protegida";
                    String str3 = "Faltan " + amount(evaluate.optDouble("shortfall")) + " CUP. " + (evaluate.optDouble("maxUSD") > 0.0d ? "Cambio estimado: " + amount(evaluate.optDouble("maxUSD")) + " USD. " : "Revisa el plan o registra ingresos CUP. ") + str + ". " + (!evaluate.optBoolean("fullyFunded") ? "Ese máximo no cubre todos los pagos: revisa el plan o registra ingresos CUP. " : "") + "Confirma la tasa real antes de cambiar.";
                    String str4 = Money.today() + "|" + amount(evaluate.optDouble("shortfall")) + "|" + amount(evaluate.optDouble("maxUSD")) + "|" + str;
                    if (str4.equals(appStore.prefs.getString("lastPlanNotification", ""))) {
                        return;
                    }
                    notificationManager.notify(7101, new Notification.Builder(context, CHANNEL).setSmallIcon(context.getResources().getIdentifier("notification", "drawable", context.getPackageName())).setContentTitle(str2).setContentText(str3).setStyle(new Notification.BigTextStyle().bigText(str3)).setContentIntent(PendingIntent.getActivity(context, 71, new Intent(context, (Class<?>) MainActivity.class).putExtra("openPlanning", true).addFlags(603979776), 201326592)).setAutoCancel(true).setOnlyAlertOnce(true).setVisibility(0).build());
                    appStore.prefs.edit().putString("lastPlanNotification", str4).apply();
                    return;
                }
                return;
            }
            notificationManager.cancel(7101);
            appStore.prefs.edit().remove("lastPlanNotification").apply();
        } catch (Exception e) {
        }
    }

    public static boolean enabled(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL);
        return notificationManager.areNotificationsEnabled() && (notificationChannel == null || notificationChannel.getImportance() != 0) && (Build.VERSION.SDK_INT < 33 || context.checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0);
    }

    public static JSONObject test(Context context) throws Exception {
        AppStore appStore = AppStore.get(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL, "Necesidad de CUP y protección USD", 3));
        if (!enabled(context)) {
            return new JSONObject().put("notified", false).put("message", "Android tiene desactivados estos avisos. En Perfil → Avisos de necesidad de CUP, permite las notificaciones y vuelve a probar.");
        }
        JSONObject evaluate = Planning.evaluate(PlanStore.snapshot(appStore), appStore.operations(), Money.today());
        String str = !evaluate.optBoolean("available") ? "Conecta y sincroniza el Sheet para recibir Plan CUP. Esta prueba solo comprueba las notificaciones." : !evaluate.optBoolean("enabled") ? "La protección está desactivada en el Sheet." : evaluate.getJSONArray("issues").length() > 0 ? "Completa los datos: " + evaluate.getJSONArray("issues").getString(0) : !evaluate.optBoolean("urgent") ? "Ahora no hay necesidad urgente según el plan: tus CUP cubren los pagos próximos o todavía no llegó la fecha de aviso." : "Cálculo actual: faltan " + amount(evaluate.optDouble("shortfall")) + " CUP; cambio máximo estimado " + amount(evaluate.optDouble("maxUSD")) + " USD. Reserva protegida: " + amount(evaluate.optDouble("protectedUSD")) + " USD.";
        String str2 = "Prueba local: no registra ingresos, gastos ni cambios. " + str;
        notificationManager.notify(7102, new Notification.Builder(context, CHANNEL).setSmallIcon(context.getResources().getIdentifier("notification", "drawable", context.getPackageName())).setContentTitle("PRUEBA · Protección USD").setContentText(str2).setStyle(new Notification.BigTextStyle().bigText(str2)).setContentIntent(PendingIntent.getActivity(context, 72, new Intent(context, (Class<?>) MainActivity.class).putExtra("openPlanning", true).addFlags(603979776), 201326592)).setAutoCancel(true).setVisibility(0).build());
        return new JSONObject().put("notified", true).put("message", "Busca la notificación PRUEBA · Protección USD en la barra de Android. " + str + " No se modificó ningún saldo ni plan.");
    }
}
