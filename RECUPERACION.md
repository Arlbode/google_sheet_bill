# Procedencia y estado del proyecto

Se recuperaron desde la publicación 1.3.0 los assets, el Apps Script completo y los datos. `exact-sha256.json` mantiene los hashes de esa recuperación; la APK original y el script 1.3.0 se conservan sin cambios. La referencia Java decompilada contiene errores de reconstrucción y no se utiliza para producción.

## Nueva app autorizada

El usuario autorizó una nueva APK porque está en pruebas y no tiene datos de producción. Indicó que retirará la antigua y continuará las actualizaciones sobre la nueva. Por eso se fijó la identidad `cu.hogarplaya.recuperada`, versión 1.4.0, con una nueva firma. La clave original no fue recuperada.

El constructor `build_release.py` mantiene exactamente `classes.dex` de la APK original y recompila los recursos manteniendo sus IDs. Cambia la identidad y versión del manifiesto, y empaqueta la nueva interfaz. Verifica la firma del resultado contra `signing/new-certificate.sha256`. Si falta la clave privada una vez fijado el certificado, exige restaurarla y rechaza crear otra identidad.

La clave y contraseña están fuera del checkout, en una carpeta privada del entorno. El respaldo privado se excluye de Git y del ZIP público de fuentes. La descarga directa al usuario falló. Se preparó una herramienta que genera localmente una clave de recuperación y un código público; al recibir ese código se podrá publicar el respaldo cifrado para que solo el usuario lo abra. Esa entrega queda pendiente del código público. Es necesario conservar la firma para futuras actualizaciones.

## Mejora implementada

Los equivalentes CUP y la tarjeta de referencia con fuente, fecha, solicitud automática y botón manual están implementados en la APK 1.4.0. El backend actualizado está en `Script_Completo_Hogar_Tasa_CUP.txt`. La tasa es una referencia orientativa con caducidad visible; no se garantiza una cotización en vivo sin red o ante un fallo de la fuente. No se modifican operaciones históricas.

El presupuesto y las funciones anteriores permanecen en el DEX original. Las pruebas financieras usan datos ficticios, sin acceder al Sheet real. Se añadieron pruebas de interfaz, consulta de tasa, conservación de operaciones, identidad y firma. Consulta [INSTALAR_NUEVA_APK.md](INSTALAR_NUEVA_APK.md) para instalar y conocer los límites de validación.

El entorno activo puede trabajar con el repositorio. El conflicto de guardado/publicación del panel de ChatGPT no se ha confirmado resuelto. No se modificó su borrador durante este desarrollo.
