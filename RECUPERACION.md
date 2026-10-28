# Procedencia y estado del proyecto

Se recuperaron desde la publicación 1.3.0 los assets, el Apps Script completo y los datos. `exact-sha256.json` mantiene los hashes de esa recuperación; la APK original y el script 1.3.0 se conservan sin cambios. La referencia Java decompilada contiene errores de reconstrucción y no se utiliza para producción.

## Nueva app autorizada

El usuario autorizó una nueva APK porque está en pruebas y no tiene datos de producción. Indicó que retirará la antigua y continuará las actualizaciones sobre la nueva. Por eso se fijó la identidad `cu.hogarplaya.recuperada`, versión 1.4.0, con una nueva firma. La clave original no fue recuperada.

Hasta 1.4.1, `build_release.py` conservaba exactamente `classes.dex`. Desde 1.5.0 incorpora una extensión nativa escrita a mano para cierre, historial y ubicación. `build_native.py` verifica todos los métodos originales sin cambios salvo la entrada de delegación `MainActivity$Bridge.call`. No compila el Java decompilado. Recompila los recursos manteniendo sus IDs. Cambia la identidad y versión del manifiesto, y empaqueta la nueva interfaz. Verifica la firma del resultado contra `signing/new-certificate.sha256`. Si falta la clave privada una vez fijado el certificado, exige restaurarla y rechaza crear otra identidad.

La clave y contraseña están fuera del checkout, en una carpeta privada del entorno. El respaldo privado sin cifrar se excluye de Git y del ZIP público de fuentes. Como la descarga directa falló, el usuario generó localmente un acceso privado y entregó su código público. Con ese código se creó `Firma_Hogar_Playa_Cifrada.json`, distribuido en `Respaldo_Firma_Cifrado.zip`. Solo se puede abrir con el archivo privado correspondiente, que conserva el usuario. Es necesario conservar ese acceso para recuperar la firma en futuras actualizaciones.

La versión 1.4.1 cambia únicamente la marca visible a **Hogar finanzas** y mantiene el paquete y la firma de la 1.4.0. La aplicación se actualiza encima y conserva sus datos.

## Mejora implementada

Los equivalentes CUP y la tarjeta de referencia con fuente, fecha, solicitud automática y botón manual están implementados en la APK 1.4.0. El backend actualizado está en `Script_Completo_Hogar_Tasa_CUP.txt`. La tasa es una referencia orientativa con caducidad visible; no se garantiza una cotización en vivo sin red o ante un fallo de la fuente. No se modifican operaciones históricas.

El presupuesto y las funciones anteriores permanecen en el DEX original. Las pruebas financieras usan datos ficticios, sin acceder al Sheet real. Se añadieron pruebas de interfaz, consulta de tasa, conservación de operaciones, identidad y firma. Consulta [INSTALAR_NUEVA_APK.md](INSTALAR_NUEVA_APK.md) para instalar y conocer los límites de validación.

El entorno activo puede trabajar con el repositorio. El conflicto de guardado/publicación del panel de ChatGPT no se ha confirmado resuelto. No se modificó su borrador durante este desarrollo.

## Versión 1.5.0

Añade cierre y nuevas sesiones del mismo día, historial local persistente, traslado de pendientes y ubicación opcional por producto. Se comprobó la actualización desde 1.4.1 en Android 10 emulado, con conservación de gastos y perfil. Los cinco escenarios incluyen GPS simulado y falta de GPS/permiso. La ubicación añade columnas W:AD sin cambiar las columnas financieras.

El 403 de elTOQUE está confirmado en los registros del usuario. La API oficial requiere acceso propio que el usuario aún no tiene; el código queda preparado para un token privado y muestra los errores sin inventar tasas.
