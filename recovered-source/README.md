# Recuperación verificable de Hogar Playa 1.3.0

Esta carpeta conserva lo que se pudo recuperar de la APK publicada y del script consolidado. No es una nueva versión instalable de la app.

## Recuperado exactamente

- Las 96 entradas de assets de la APK: interfaz JavaScript/HTML/CSS, catálogo y las 91 imágenes SVG.
- El Apps Script completo, con 103 funciones y el cierre FIN_DEL_CODIGO.
- Los datos de la plantilla, cuentas y precios descomprimidos desde ese mismo script: 91 productos y las 56 ofertas Yosvany.
- El código nativo original dentro de classes.dex, manifiesto binario y recursos, conservados en la APK base que acompaña al proyecto.
- El certificado PÚBLICO original, para identificar qué firma debe tener una actualización compatible.

`exact-sha256.json` permite comprobar los assets y el backend recuperados. La APK base se comprueba siempre contra el SHA-256 de la versión 1.3.0 publicada.

## Java de referencia: no usar como código de producción

`android/reference-java/` contiene una decompilación con JADX 1.5.1, no los archivos Java originales. El decompilador advierte de reconstrucciones incorrectas en sincronización, recibos de planes y parte del puente nativo. Hay expresiones y flujos que requieren revisión. Que la herramienta termine sin error no significa que ese Java sea compilable o fiel al comportamiento original.

Por eso el constructor de comprobación **no compila ni ejecuta esos archivos**. Conserva byte por byte el classes.dex original, de modo que esta recuperación no introduce cambios en la lógica nativa financiera, PIN, perfiles, SQLite o sincronización.

## Comprobar la recuperación

Desde la raíz del repositorio, con las dependencias instaladas:

```sh
bash recovered-source/scripts/verify_environment.sh
```

La prueba financiera requiere openpyxl 3.1.5 y LibreOffice/soffice. No usa la hoja real del usuario: genera datos ficticios en un directorio temporal.

El respaldo se comprueba con `python3 recovered-source/scripts/create_backup.py --check Hogar_Playa_Recuperacion_Fuentes.zip`. Para reconstruirlo después de un cambio de fuentes, ejecuta `python3 recovered-source/scripts/create_backup.py`; incluye las fuentes actuales y un manifiesto de integridad, sin cachés ni claves privadas. El ZIP no contiene los datos reales de los teléfonos ni permite recuperar la firma perdida. Consulta [Qué hacer ahora](../LEEME_PRIMERO.md).

El constructor produce `recovered-source/build/Hogar_Playa_UNFIRMADA.apk`. Ese archivo **no está firmado y no es una nueva APK instalable**. Se usa para comprobar que los recursos y el código original pueden reempaquetarse sin alterarlos.

## Firma: bloqueo pendiente

La clave privada original y su contraseña no estaban en GitHub y no se han recuperado del entorno anterior. El certificado público no permite reconstruirla. El usuario eligió conservar solo la app actual: no se ha generado otra firma, creado una app paralela ni sustituido la APK que funciona.

El ayudante `scripts/sign_original.py` solo podrá usarse si se recupera la firma privada original por un medio privado. Recibe rutas de archivos, nunca la contraseña en un argumento. Comprueba que el certificado coincide con el original antes de firmar; una clave diferente se rechaza. Las claves y contraseñas no deben subirse a este repositorio público.

La firma original esperada es SHA-256:

`911fdea8894025eb62ca8e775aa23ded1725830526f46d7c3d2194c6f634a569`

## Pendiente funcional

Los equivalentes CUP bajo los montos USD y la tarjeta de tasa con actualización automática/manual aún no están implementados en la APK 1.3.0. Esta reparación conserva y respalda la base anterior; no se presenta como esa actualización.
