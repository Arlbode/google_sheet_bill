# Conexión Android: URL pública verificada

El diagnóstico ejecutado por el usuario mostró GET404 y POST404 para la URL escrita en la APK. El proyecto devolvió una URL de contexto `/dev` desde `ScriptApp.getService().getUrl()`. Ese dato no prueba que no exista una implementación pública, pero ese enlace de pruebas no se admite en la APK. La URL anterior no se obtiene convirtiendo manualmente `/dev` en `/exec`.

Se corrige el generador que antes devolvía `getService().getUrl()` sin distinguir el contexto. `configurarConexionAndroid` pide la URL completa de Aplicación web y verifica por GET anónimo: HTTP200, JSON, apiVersion1 e identidad del mismo proyecto. Solo después guarda `HOGAR_ANDROID_EXEC_URL`. El GET del servicio incorpora un hash no secreto del proyecto y connectionVersion2 para distinguir una versión actual de un despliegue anterior. No revela perfiles, saldos, códigos o credenciales.

`generarCodigoAndroid` utiliza la URL configurada con preferencia sobre el contexto. Verifica la respuesta antes de preparar la hoja o emitir un código. Si la URL devuelve404, exige acceso Google, pertenece a otro proyecto o sigue ejecutando una versión anterior, falla sin generar o consumir códigos. Conserva caducidad de10 minutos, formato y validación de un solo uso. El emparejamiento, autorización, sincronización y funciones financieras permanecen intactos.

## Instalación en el mismo proyecto

1. Sustituir Código.gs por el script completo corregido y guardar.
2. Implementar → Nueva implementación → Aplicación web, ejecutar como el propietario y acceso Cualquier persona. Copiar la URL completa `/exec` de la ventana final. También sirve actualizar una implementación pública activa a Nueva versión. Debe publicar el código nuevo antes del siguiente paso.
3. Desde el editor ejecutar `configurarConexionAndroid` y pegar esa URL. No pegar el enlace de pruebas `/dev` ni el enlace del Sheet.
4. Hogar → Conectar app Android → Generar código. Copiar esa URL y el código recién generado a la APK1.7.3.

No requiere otra APK ni ejecutar iniciarHogar o dejarListoParaProduccion. No borra datos, no cambia el hogar ni las conexiones existentes. La migración de producción se conserva en el script completo preparado para el usuario y su marca de finalización no cambia. Si se archiva o reemplaza el despliegue, volver a configurar la nueva URL.

El configurador se ejecuta en el editor; su verificación realiza una petición GET sin token, código ni operaciones financieras. El código servidor debe estar publicado para responder con su identidad nueva. El diagnóstico actualizado distingue explícitamente urlDelContexto de urlConfigurada.

## Construcción y pruebas

`python3 development/conexion-publica/build_script.py --input BASE.txt --output SCRIPT.txt`. Cambia únicamente `generarCodigoAndroid` y `doGet`, conserva exactamente los bytes de las otras regiones y añade las funciones nuevas. La copia con el destino concreto del usuario permanece fuera del repositorio. La plantilla pública no contiene ese destino.

`HOGAR_CONNECTION_SCRIPT=/ruta/SCRIPT.txt node development/conexion-publica/tests/connection.test.cjs`: ocho pruebas sobre el script completo. Cubren contexto/dev, prioridad de/exec, respuesta404/403/503, login HTML, otra versión/proyecto, cancelación/configuración fallida sin sobrescribir, códigos con caducidad/formato intactos y ausencia de datos privados en doGet. Las funciones de emparejamiento, doPost, reservas y producción se comparan intactas durante la construcción; se conservan las regresiones de autorización.

No se ha publicado el código dentro de la cuenta Google del usuario desde este entorno. Los ensayos de red de las pruebas son simulados; el resultado404/dev real fue proporcionado por el diagnóstico del usuario. El configurador hará la verificación real al ejecutarse con su sesión Google.
