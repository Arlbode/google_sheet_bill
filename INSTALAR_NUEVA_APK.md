# Hogar finanzas 1.4.1: instalación y actualización

Esta versión cambia el nombre visible de **Hogar Playa Nueva 1.4.0** a **Hogar finanzas**. Mantiene el mismo identificador y firma.

**Si ya tienes la 1.4.0:** descarga e instala la APK 1.4.1 encima. Conserva tus perfiles, movimientos y conexión al Sheet. No desinstales la 1.4.0 ni vuelvas a conectar la hoja para este cambio de nombre. Los pasos de preparación del Sheet que siguen son para una instalación inicial.

La app original 1.3.0 usaba otra firma. La identidad nueva se autorizó durante la fase de pruebas y es la que continuará recibiendo actualizaciones.

## 1. Instala la nueva APK

Descarga [Hogar_finanzas_1.4.1.apk](Hogar_finanzas_1.4.1.apk) en el teléfono e instálala. Android la muestra como **Hogar finanzas**. Si Android lo solicita, permite instalar este archivo desde la aplicación que utilizaste para descargarlo.

Actualiza la versión 1.4.0 existente. Si vienes directamente de la antigua 1.3.0, se instala aparte de aquella porque las firmas son distintas; configura el Sheet y la conexión siguiendo esta guía, y después retira la 1.3.0 conforme a lo acordado.

## 2. Actualiza el único script completo del Sheet

1. Descarga [Script_Completo_Hogar_Tasa_CUP.txt](Script_Completo_Hogar_Tasa_CUP.txt).
2. En tu Sheet, abre **Extensiones → Apps Script**.
3. Sustituye todo el contenido de **Código.gs** por ese archivo completo, guarda y ejecuta **iniciarHogar**.
4. Abre **Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar**. Conserva tu URL `/exec`, **Ejecutar como: Yo** y **Acceso: Cualquier persona**.
5. En **Ajustes B26**, mantén **Sí** para habilitar la consulta de la tasa.

El archivo termina en `FIN_DEL_CODIGO`. El anterior `Script_Completo_Hogar_Android.txt` se conserva como referencia de la publicación 1.3.0; usa el nuevo archivo de esta entrega.

## 3. Conecta la nueva app

1. Abre **Hogar finanzas** y crea tu perfil y PIN.
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

Ya está preparado el **[Respaldo_Firma_Cifrado.zip](Respaldo_Firma_Cifrado.zip)** para el código público que entregaste. Guarda este ZIP y tu archivo **Clave_PRIVADA_Respaldo_Hogar.json**, con una segunda copia privada de ese archivo.

Para extraer el ZIP privado de firma:

1. Extrae **Respaldo_Firma_Cifrado.zip** y abre **Respaldo_Firma_Hogar.html** con Chrome.
2. Ve a **2. Abrir el respaldo cifrado recibido**. No pulses Crear acceso para abrir esta copia: utiliza tu acceso existente.
3. Selecciona tu **Clave_PRIVADA_Respaldo_Hogar.json** y el archivo **Firma_Hogar_Playa_Cifrada.json** extraído del ZIP.
4. Pulsa **Abrir y guardar ZIP privado**. Guarda **Firma_PRIVADA_Hogar_Playa_Nueva.zip** en un lugar privado.

El ZIP privado contiene la clave de firma y su contraseña. No publiques ese ZIP ni el archivo de acceso privado. La herramienta funciona sin internet y no envía archivos. El respaldo cifrado de GitHub puede conservarse públicamente; no basta para abrirlo sin tu archivo privado.

La firma pública queda fijada en `recovered-source/signing/new-certificate.sha256`. El constructor rechazará otra clave y no generará automáticamente una sustituta si la firma se pierde. El ZIP público de fuentes no incluye la firma privada.

El identificador Android que permanece estable es `cu.hogarplaya.recuperada`. Todas sus futuras actualizaciones deben mantener ese identificador y esa firma.

## Validación de esta entrega

Se comprobaron la interfaz en Chromium con el puente nativo simulado, los cálculos, los estados sin conexión, la solicitud manual de sincronización y la conservación de tasas históricas en pruebas. El instalador se valida con AAPT2, zipalign y apksigner; conserva exactamente el DEX original. No se ha hecho una instalación física ni una sincronización contra tu Sheet real desde este entorno. La consulta en vivo de elTOQUE desde esta máquina fue bloqueada por su política de red; los fallos se muestran sin inventar una cotización.
