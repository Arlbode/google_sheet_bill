# Hogar Playa · Android 1.2.2

Android 8 o posterior. Instala encima de la versión anterior para conservar perfiles, PIN, movimientos y conexión.

- [Hogar_Playa.apk](Hogar_Playa.apk): APK firmada.
- [Hogar_Playa_Instalacion.zip](Hogar_Playa_Instalacion.zip): APK, único script completo, manual, oferta CSV y verificaciones.
- [Script_Completo_Hogar_Android.txt](Script_Completo_Hogar_Android.txt): sustituye todo Código.gs y ejecuta iniciarHogar.
- [Manual_Android.md](Manual_Android.md): instalación, actualización y operación.

## Novedades

- Inicio o Perfil → Plan CUP y protección de USD: consultar, añadir, editar, cancelar y reactivar planes. Muestra previsto, pagado y pendiente, con filtros para pendientes, pagados y cancelados.
- Cambios de planes offline, guardados separados de los movimientos de dinero. Se sincroniza primero el plan y después sus pagos. Las ediciones simultáneas se muestran para revisión. Cancelar conserva pagos y trazabilidad.
- Registrar → Ingreso reúne USD y CUP. Al elegir CUP aparece Efectivo/Transferencia, también desde Registrar movimiento → Ingreso. Se elimina el acceso separado a otro ingreso CUP. El Sheet utiliza un único menú Registrar ingreso · USD o CUP.
- Conserva selección múltiple, importe de la lista pendiente, tope diario, desglose de saldos, protección USD, perfiles/PIN, estadísticas, prueba de aviso y oferta Yosvany.

## Actualizar

1. En el Sheet → Extensiones → Apps Script, sustituye TODO Código.gs por el archivo completo. Guarda y ejecuta iniciarHogar.
2. Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar. Conserva URL /exec y permisos.
3. Instala la APK encima de la anterior y sincroniza. Si conservaste la URL y el proyecto, no necesitas otro código de conexión.

La gestión móvil de planes necesita el script y la implementación actualizados. El menú Guía incluye los nuevos pasos de operación. Crear o editar un plan no descuenta dinero: cada gasto real se registra una vez y su vínculo reduce lo pendiente del plan.

La autorización e implementación de Google las realiza el propietario. No se publican credenciales ni movimientos financieros reales.
