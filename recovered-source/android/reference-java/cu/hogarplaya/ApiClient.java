package cu.hogarplaya;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class ApiClient {
    private static final String ACCESS_HELP = "En tu hoja abre Extensiones → Apps Script → Implementar → Gestionar implementaciones → lápiz. Elige Aplicación web, Ejecutar como: Yo y Quién tiene acceso: Cualquier persona. Selecciona Nueva versión y pulsa Implementar. Comprueba la URL /exec en una ventana de incógnito: debe mostrar un texto con Hogar Playa y apiVersion, sin pedir iniciar sesión. Después vuelve a intentarlo en la APK. Tus movimientos siguen guardados en el teléfono.";

    public static String endpoint(String str) throws Exception {
        URL url = new URL(str.trim());
        if ((url.getProtocol().equals("https") && url.getHost().equals("script.google.com") && url.getPort() == -1 && url.getPath().matches("/macros/s/[A-Za-z0-9_-]+/exec")) && url.getUserInfo() == null && url.getQuery() == null && url.getRef() == null) {
            return url.toString();
        }
        throw new IllegalArgumentException("Copia la URL de implementación de Apps Script que termina en /exec.");
    }

    public static JSONObject post(String str, JSONObject jSONObject) throws Exception {
        URL url = new URL(endpoint(str));
        byte[] bytes = jSONObject.toString().getBytes(StandardCharsets.UTF_8);
        boolean z = true;
        int i = 0;
        while (i < 5) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setConnectTimeout(25000);
            httpURLConnection.setReadTimeout(65000);
            httpURLConnection.setRequestProperty("Accept", "application/json");
            if (z) {
                try {
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
                    httpURLConnection.setFixedLengthStreamingMode(bytes.length);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    try {
                        outputStream.write(bytes);
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } finally {
                    }
                } finally {
                    httpURLConnection.disconnect();
                }
            }
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != 301 && responseCode != 302 && responseCode != 303) {
                if (responseCode == 401 || responseCode == 403) {
                    throw new IOException("Google rechazó el acceso (HTTP " + responseCode + "). Revisa los permisos de la implementación. " + ACCESS_HELP);
                }
                if (responseCode != 200) {
                    throw new IOException("El servicio respondió " + responseCode + ". Tus pendientes siguen en el teléfono.");
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                InputStream inputStream = httpURLConnection.getInputStream();
                try {
                    byte[] bArr = new byte[8192];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read == -1) {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            String str2 = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                            if (!str2.trim().startsWith("{")) {
                                throw new IllegalArgumentException("Google devolvió una página en lugar de los datos de la app. En tu hoja abre Extensiones → Apps Script → Implementar → Gestionar implementaciones → lápiz. Elige Aplicación web, Ejecutar como: Yo y Quién tiene acceso: Cualquier persona. Selecciona Nueva versión y pulsa Implementar. Comprueba la URL /exec en una ventana de incógnito: debe mostrar un texto con Hogar Playa y apiVersion, sin pedir iniciar sesión. Después vuelve a intentarlo en la APK. Tus movimientos siguen guardados en el teléfono.");
                            }
                            JSONObject jSONObject2 = new JSONObject(str2);
                            if (jSONObject2.optInt("apiVersion") == 1) {
                                return jSONObject2;
                            }
                            throw new IllegalArgumentException("Actualiza el script completo del hogar antes de conectar esta versión.");
                        }
                        if (byteArrayOutputStream.size() + read > 3000000) {
                            throw new IOException("La respuesta supera el límite seguro.");
                        }
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                } finally {
                }
            }
            String headerField = httpURLConnection.getHeaderField("Location");
            if (headerField == null) {
                throw new IllegalArgumentException("Google no devolvió una respuesta válida.");
            }
            URL url2 = new URL(url, headerField);
            if (!url2.getProtocol().equals("https") || !url2.getHost().equals("script.googleusercontent.com") || url2.getUserInfo() != null || url2.getPort() != -1) {
                throw new IllegalArgumentException("Google redirigió fuera de la respuesta de la aplicación web. En tu hoja abre Extensiones → Apps Script → Implementar → Gestionar implementaciones → lápiz. Elige Aplicación web, Ejecutar como: Yo y Quién tiene acceso: Cualquier persona. Selecciona Nueva versión y pulsa Implementar. Comprueba la URL /exec en una ventana de incógnito: debe mostrar un texto con Hogar Playa y apiVersion, sin pedir iniciar sesión. Después vuelve a intentarlo en la APK. Tus movimientos siguen guardados en el teléfono.");
            }
            httpURLConnection.disconnect();
            i++;
            z = false;
            url = url2;
        }
        throw new IOException("Demasiadas redirecciones. Revisa la URL /exec.");
    }
}
