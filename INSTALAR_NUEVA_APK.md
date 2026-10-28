# Instalar Hogar Playa Nueva 1.4.0

Esta APK usa una identidad y una firma nuevas, autorizadas porque la app anterior está en pruebas y no contiene datos de producción. **Las próximas actualizaciones se harán sobre esta nueva app.** No es una actualización compatible con la firma antigua.

## 1. Instala la nueva APK

Descarga [Hogar_Playa_Nueva_1.4.0.apk](Hogar_Playa_Nueva_1.4.0.apk) en el teléfono e instálala. Android la muestra como **Hogar Playa Nueva**. Si Android lo solicita, permite instalar este archivo desde la aplicación que utilizaste para descargarlo.

Se instala aparte de la anterior. Cuando hayas comprobado que abre y se conecta al Sheet, puedes desinstalar la antigua y continuar solo con Hogar Playa Nueva, como solicitaste. La nueva app comienza sin los datos locales de la anterior; al conectarse recibe el estado e historial disponibles en tu Sheet.

## 2. Actualiza el único script completo del Sheet

1. Descarga [Script_Completo_Hogar_Tasa_CUP.txt](Script_Completo_Hogar_Tasa_CUP.txt).
2. En tu Sheet, abre **Extensiones → Apps Script**.
3. Sustituye todo el contenido de **Código.gs** por ese archivo completo, guarda y ejecuta **iniciarHogar**.
4. Abre **Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar**. Conserva tu URL `/exec`, **Ejecutar como: Yo** y **Acceso: Cualquier persona**.
5. En **Ajustes B26**, mantén **Sí** para habilitar la consulta de la tasa.

El archivo termina en `FIN_DEL_CODIGO`. El anterior `Script_Completo_Hogar_Android.txt` se conserva como referencia de la publicación 1.3.0; usa el nuevo archivo de esta entrega.

## 3. Conecta la nueva app

1. Abre **Hogar Playa Nueva** y crea tu perfil y PIN.
2. En el Sheet: **Hogar → Conectar app Android** y genera un código de conexión nuevo.
3. En la nueva app: **Perfil → Conectar Google Sheets**. Introduce tu URL `/exec` y el código. El código dura 10 minutos y se usa una sola vez.
4. Sincroniza y comprueba los saldos recibidos de tu Sheet.

## 4. Comprueba las tarjetas

- Inicio muestra **≈ CUP** debajo del ahorro, su meta, ingresos, consumo, disponible para consumo y saldo USD.
- Una tarjeta pequeña muestra **1 USD = X CUP**, la fuente y la fecha/hora de consulta. Todos los equivalentes usan esa misma referencia.
- Pulsa **↻** para solicitar la referencia al Sheet y sincronizar. La fuente se consulta como máximo una vez por minuto para evitar peticiones repetidas de los teléfonos.
- Con la app abierta se solicita una referencia cuando vence la consulta de 30 minutos y al volver a la app. Se comprueba el vencimiento cada minuto. Las sincronizaciones automáticas nativas también consultan la referencia; Android puede retrasarlas en segundo plano.
- Sin conexión o si la fuente falla, se conserva la última referencia y su fecha. No se presenta como una tasa en vivo. La tasa publicada por una fuente tampoco garantiza el precio que te ofrecerá un vendedor.
- El equivalente orientativo no cambia las tasas guardadas en salarios, cambios USD→CUP ni gastos históricos. Confirma siempre la tasa real de una nueva operación.

Ejemplo: si la tarjeta indica 870 CUP/USD, 350 USD muestran aproximadamente 304.500 CUP. Si la referencia cambia a 900, el equivalente será 315.000 CUP; tus movimientos antiguos conservan su tasa original.

## 5. Conserva la nueva firma

La descarga directa del ZIP privado falló en el chat. Se preparó esta alternativa:

1. Descarga [Herramienta_Respaldo_Firma.zip](Herramienta_Respaldo_Firma.zip), extráelo y abre **Respaldo_Firma_Hogar.html** con Chrome.
2. Pulsa **Crear acceso** y luego **Guardar clave PRIVADA**. Conserva el archivo descargado y otra copia privada.
3. Copia únicamente el **código PÚBLICO** que empieza por `HOGAR-PUBLICA-1:` y compártelo en el chat. No compartas el archivo privado ni su contenido.
4. Con ese código se podrá entregar un respaldo cifrado por GitHub. Solo se podrá abrir con tu archivo privado local.
5. Cuando lo recibas, usa la sección **Abrir el respaldo cifrado recibido** de la misma herramienta para obtener **Firma_PRIVADA_Hogar_Playa_Nueva.zip**. Guarda el ZIP y otra copia privada.

El ZIP privado contiene la clave y su contraseña: **no lo subas al repositorio público ni lo compartas públicamente**. La herramienta funciona sin internet. La entrega cifrada queda pendiente hasta recibir el código público generado en tu computadora.

La firma pública queda fijada en `recovered-source/signing/new-certificate.sha256`. El constructor rechazará otra clave y no generará automáticamente una sustituta si la firma se pierde. El ZIP público de fuentes no incluye la firma privada.

El nuevo identificador Android es `cu.hogarplaya.recuperada`. Todas sus futuras actualizaciones deben mantener ese identificador y esa firma.

## Validación de esta entrega

Se comprobaron la interfaz en Chromium con el puente nativo simulado, los cálculos, los estados sin conexión, la solicitud manual de sincronización y la conservación de tasas históricas en pruebas. El instalador se valida con AAPT2, zipalign y apksigner; conserva exactamente el DEX original. No se ha hecho una instalación física ni una sincronización contra tu Sheet real desde este entorno. La consulta en vivo de elTOQUE desde esta máquina fue bloqueada por su política de red; los fallos se muestran sin inventar una cotización.
