# Actualizaciones desde Hogar finanzas · 1.5.4

La 1.5.4 añade aviso de nueva versión y Perfil → Actualizaciones. Es necesario instalar esta primera APK encima de la versión actual una vez. Después, las versiones futuras se podrán descargar e iniciar desde la app.

## Uso

- Se consulta al abrir la app, como máximo cada seis horas tras una consulta correcta, y aproximadamente una vez al día mediante un trabajo de Android. La red y el ahorro de batería pueden retrasarlo. También hay un botón Buscar actualizaciones.
- El aviso de Android requiere permitir notificaciones. La app muestra igualmente un aviso interno cuando detecta una versión nueva.
- Descargar actualización descarga solo al pulsar el botón. Al terminar, Instalar actualización abre el instalador de Android.
- Android exige autorizar la instalación. Si solicita permitir instalaciones desde Hogar finanzas, concede ese permiso para esta app, vuelve y pulsa Instalar actualización de nuevo. La instalación nunca es silenciosa.
- Se mantienen el paquete `cu.hogarplaya.recuperada` y el certificado fijado. La actualización conserva los datos al instalarse encima de la actual. La vinculación al Sheet permanece.

## Canal de distribución

El canal es un archivo público de versiones, independiente del Sheet:

`https://raw.githubusercontent.com/Arlbode/google_sheet_bill/actualizaciones-android/actualizacion.json`

El JSON contiene versión, versionCode, Android mínimo, nombre/dirección de APK, tamaño exacto, SHA-256, certificado esperado y notas. La APK se aloja en la misma rama `actualizaciones-android` del repositorio. Preparar el archivo local no activa este canal: es necesario publicar el manifiesto y su APK con autorización del usuario.

El actualizador no envía tokens, PIN, perfiles, ubicaciones ni información financiera a GitHub. Usa preferencias y caché independientes y no modifica movimientos, tablas financieras ni la sesión diaria.

Antes de instalar se comprueban tamaño, hash, paquete, versión superior y certificado tanto de la APK como de la app instalada. La fuente y las rutas admitidas son fijas. Las notas se muestran como texto. El archivo se comparte con el instalador mediante un proveedor privado de solo lectura y una autorización temporal; no se expone el almacenamiento interno.

## Preparar una versión futura

1. Incrementar versionName y versionCode en `recovered-source/release.json` y compilar con la firma existente.
2. Ejecutar las pruebas y comprobar el informe de conservación de métodos originales.
3. Preparar el canal local con:

```sh
cd /workspace/google_sheet_bill
source /workspace/.tools/hogar-environment/env.sh
python3 recovered-source/scripts/build_release.py
python3 recovered-source/scripts/prepare_update_channel.py \
  --apk recovered-source/build/Hogar_finanzas_1.5.4.apk \
  --output /workspace/shared/downloads/actualizaciones-android \
  --notes-file /ruta/a/notas.txt
```

4. Tras autorizarse la publicación, subir la nueva APK y `actualizacion.json` juntos en el mismo commit de la rama del canal. El generador verifica realmente firma, identidad y versión con apksigner/aapt2 antes de crear el JSON. La clave y contraseña nunca se publican.

Para otra versión, sustituye el nombre de APK por el de esa versión. No hace falta desplegar Apps Script ni generar códigos Android por publicar una nueva APK.

## Pruebas

- `test_update_policy.py` ejecuta la política Java real en la JVM: versión, identidad, origen, hash, límites y rutas del proveedor.
- `browser_updates.py` comprueba pantalla, aviso, botones, errores y notas escapadas con un puente simulado.
- `run_update_smoke.py` usa exclusivamente un emulador identificado. Comprueba descarga con HTTPS controlado, hash incorrecto, certificado de APK, proveedor privado, notificación, trabajo diario, permiso de instalación, apertura del instalador y conservación de SQLite al actualizar a una APK de prueba. Los paquetes de instrumentación y la versión futura ficticia no se entregan al usuario.

La APK de usuario es 1.5.4. La publicación del canal y una descarga real desde ese canal se validan por separado después de su publicación.

Para repetir la prueba nativa en el mismo emulador después de instalar la versión ficticia superior, usa `python3 recovered-source/tests/run_update_smoke.py --reset`. Esa opción borra únicamente las dos apps de prueba del emulador identificado, nunca un teléfono.
