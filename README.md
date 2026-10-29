# Hogar finanzas · canal de actualizaciones Android

Versión disponible: **1.5.4** (versionCode 14).

- [Descargar APK firmada](https://github.com/Arlbode/google_sheet_bill/raw/refs/heads/actualizaciones-android/Hogar_finanzas_1.5.4.apk)
- [Descargar paquete de instalación e instrucciones](https://github.com/Arlbode/google_sheet_bill/raw/refs/heads/actualizaciones-android/Hogar_finanzas_1.5.4_Instalacion.zip)
- [Cómo funcionan las actualizaciones](ACTUALIZACIONES_ANDROID.md)
- [Cambios y validación](VERSION_1.5.4.md)

Esta primera 1.5.4 se instala manualmente encima de la app existente, sin desinstalar ni borrar datos. Después, Perfil → Actualizaciones permite buscar y descargar versiones futuras. Android solicita confirmar la instalación. La vinculación al Sheet se conserva.

La app consulta `actualizacion.json` en esta rama. APK y manifiesto se actualizan juntos; antes de instalar se comprueban identidad, versión, tamaño, SHA-256 y firma. Este canal contiene únicamente archivos públicos de distribución. No contiene claves privadas ni datos de la hoja.
