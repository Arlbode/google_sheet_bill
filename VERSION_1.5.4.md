# Hogar finanzas 1.5.4 · actualizaciones desde la app

Versión Android 1.5.4, versionCode 14, paquete `cu.hogarplaya.recuperada` y certificado fijado conservados.

La primera actualización desde 1.5.3 se instala manualmente encima de la app existente. Después, Perfil → Actualizaciones permite consultar versiones, descargar la APK verificada y abrir el instalador de Android. Android solicita confirmar la instalación.

Hay un aviso interno y una notificación opcional. La comprobación se programa una vez al día, se intenta al abrir la app con límite de frecuencia y también se puede iniciar manualmente. Las descargas requieren pulsar el botón. Los fallos de red conservan los datos financieros y la última referencia de versiones recibida.

La identidad, versión superior, tamaño, SHA-256 y firma de la APK se comprueban antes de instalar. Un archivo incompleto o alterado se rechaza y puede descargarse de nuevo. El instalador recibe acceso temporal de solo lectura al archivo verificado. El actualizador funciona independientemente de la conexión al Sheet.

No hace falta volver a vincular el teléfono ni actualizar Apps Script para esta función. La corrección del ahorro inicial distribuida por separado sigue siendo válida.

El canal público de versiones se prepara con `prepare_update_channel.py`. Su publicación requiere autorización y no se realiza al compilar. Sin el canal publicado, el botón informa de que todavía no está disponible. Instrucciones completas en [ACTUALIZACIONES_ANDROID.md](ACTUALIZACIONES_ANDROID.md).

La APK se compila únicamente desde la extensión nativa escrita a mano, manteniendo los métodos de la base excepto la entrada autorizada del puente. Los archivos privados de firma, datos del Sheet y paquetes de pruebas no se incluyen en la entrega pública.

## Validación completada

18 pruebas Python, 34 casos JavaScript y cuatro pruebas de navegador completados. La política Java nativa tiene 32 aserciones de versiones, firma, origen, tamaños y rutas.

En Android 10 se ejecutó la descarga con HTTPS controlado, el rechazo de un hash incorrecto y de una caché alterada, la comprobación del certificado, el proveedor de solo lectura, la notificación, el trabajo diario, la solicitud de permiso y la apertura del instalador. Después se instaló una APK futura ficticia con la misma firma y se comprobó que la tabla SQLite de prueba permanecía. Los paquetes de prueba no se entregan.

El canal de distribución usa la rama actualizaciones-android y se publica junto con la APK y el manifiesto de versión. La descarga real se comprueba después de publicar; los fallos de conexión mantienen la aplicación instalada.
