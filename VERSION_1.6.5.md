# Hogar finanzas 1.6.5 — descarga TLS compatible

El fallo Read error / BAD_DECRYPT / DECRYPTION_FAILED ocurre dentro de la descarga TLS. La nueva ruta usa TLS 1.2 con confianza del sistema y validación del servidor, prioriza ChaCha20 cuando está disponible y admite AES-GCM con ECDHE. Hace hasta tres intentos ante errores de lectura TLS, EOF y fallos transitorios de socket, cada uno con conexión y archivo nuevos. No reintenta certificados inválidos, handshake, redirecciones ni excesos de tamaño. Descarta todos los parciales.

Conserva el verificador y el instalador originales: solo habilita instalar tras verificar tamaño, SHA-256, paquete, versión y certificado. No desactiva controles TLS. Un hilo propio evita bloquear las operaciones financieras. Ofrece Descargar con el navegador ante un error, mediante una ruta HTTPS cerrada de GitHub.

## Cómo instalar esta primera corrección

Si el descargador de la app sigue fallando, abre en Chrome:

https://github.com/Arlbode/google_sheet_bill/raw/refs/heads/actualizaciones-android/Hogar_finanzas_1.6.5.apk

Instala encima de la aplicación actual, sin desinstalar ni borrar datos. El descargador anterior no recibe la corrección hasta instalar esta APK. Después se utiliza Perfil → Actualizaciones normalmente. No necesitas cambiar Apps Script.

## Validación y límites

Código 23, paquete cu.hogarplaya.recuperada y firma original. Clases AppUpdates/UpdatePolicy, instalador, clases financieras y recursos compilados conservados. El constructor compara el desensamblado de las clases originales y verifica firma/alineación. Pruebas JVM: 28 comprobaciones de error SSL parcial, reintentos limitados, limpieza, certificado, tamaño, URL, redirecciones y TLS1.2/AEAD. Pruebas de interfaz con datos ficticios: nuevo comando y alternativa de navegador, más los flujos previos. La instrumentación Android comprueba TLS1.2, aceptación de candidata firmada por el verificador original y rechazo de APK alteradas antes y después de guardar. La candidata ficticia y la instrumentación no se publican.

No se ha reproducido el fallo en el teléfono del usuario. La desaparición de ese error en su conexión necesita confirmación después de instalar esta corrección. No se accedió al Sheet privado.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/descarga-tls-hogar-1.6.5
