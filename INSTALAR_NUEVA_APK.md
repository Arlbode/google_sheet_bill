# Hogar finanzas 1.5.2: instalación y uso

Esta APK actualiza 1.4.0/1.4.1/1.5.0/1.5.1 con el mismo identificador y firma. Conserva perfiles, gastos, listas, conexión y compras existentes. Se retiró la app original 1.3.0 por decisión del usuario; su firma era distinta.

## 1. Comprueba el script del Sheet

Es necesario sustituir el script completo incluso si ya usabas 1.5.0: 1.5.2 añade referenceSync para recibir las referencias que consulta la APK.

1. Descarga [Script_Completo_Hogar_Tasa_CUP.txt](Script_Completo_Hogar_Tasa_CUP.txt).
2. Abre tu Sheet → Extensiones → Apps Script y sustituye todo Código.gs por ese archivo completo. Guarda y ejecuta iniciarHogar.
3. Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar. Mantén tu URL /exec, Ejecutar como: Yo y Acceso: Cualquier persona.

También es necesario este paso si ya tenías la 1.4.1. La nueva versión permite varias compras del mismo día y prepara en Registro: W Lugar, X Latitud, Y Longitud, Z Precisión, AA Fecha de captura, AB Estado y AC Mapa. AD contiene JSON interno y se oculta. Si esas columnas tenían datos propios, el script los conserva y pide reservar el espacio antes de continuar. No borra gastos ni modifica sus tasas históricas.

## 2. Instala la actualización

Actualiza y publica primero el script completo 1.5.2, conservando la URL /exec.

Descarga [Hogar_finanzas_1.5.2.apk](Hogar_finanzas_1.5.2.apk) e instálala encima de Hogar finanzas 1.5.1, 1.5.0, 1.4.1 o Hogar Playa Nueva 1.4.0. No desinstales la actual para actualizar y no crees otros perfiles por este cambio. Conserva su conexión al Sheet.

Si es la primera instalación de esta identidad, crea un perfil y PIN; en el Sheet usa Hogar → Conectar app Android, genera un código nuevo y conecta la app desde Perfil con tu URL /exec. El código dura 10 minutos y se usa una vez.

## 3. Cerrar y volver a abrir el Modo compra

1. Comprar → Iniciar Modo compra: fija el tope, moneda, tasa del tope y, si quieres, el mercado o tienda.
2. Añade productos y confirma los que compras. Solo la confirmación crea el gasto.
3. Usa **Cerrar Modo compra**. Se conserva el historial y todos los gastos. Puedes marcar llevar los pendientes a la siguiente compra.
4. Inicia otro Modo compra el mismo día con un tope distinto. La nueva compra comienza con gasto cero; los gastos anteriores siguen contando en Consumo y en los Plan CUP vinculados.
5. La opción recuperar pendientes copia solo lo que faltaba, con IDs nuevos. No copia productos comprados ni genera gastos por el traslado. Revisa precios, unidades, tasas y vínculos al plan antes de confirmar.

Salir de la vista mantiene la compra abierta; Cerrar la archiva. El tope es por compra y por teléfono, compartido entre sus perfiles. No se crea otro pago de Plan CUP por abrir o cerrar un modo.

**Historial de Modos compra** muestra las compras cerradas de este teléfono, importes, productos, pendientes al cerrar y ubicaciones. Persiste al reiniciar y al actualizar, y se incluye en Exportar respaldo local. Los gastos se sincronizan con el Sheet; el historial de sesiones es local, no un historial compartido entre teléfonos.

## 4. Lugar y ubicación al comprar

Activa Ubicación en las compras desde Perfil o desde la confirmación de un producto. Android pedirá permiso. Puedes permitir una ubicación aproximada si tu sistema lo ofrece. Es opcional: desactivarla o denegar permiso no impide registrar gastos.

Al confirmar, se solicita una posición puntual; se puede usar una posición de hasta 30 segundos y se espera hasta seis segundos por una nueva. No se rastrea en segundo plano. Se guardan coordenadas, precisión disponible y hora de la posición; no se inventa un nombre de comercio desde el GPS.

Escribe el mercado o tienda si quieres identificarlo por nombre. El lugar indicado al iniciar la compra aparece como sugerencia al confirmar cada producto y puedes cambiarlo. Con GPS apagado, sin permiso o si no se obtiene posición, se conserva el lugar escrito y el motivo de falta de coordenadas. El gasto se guarda normalmente. Puedes consultar el lugar en el detalle del movimiento, el historial local y las columnas del Sheet. El mapa requiere una conexión para cargarlo.

## 5. Tasa y HTTP 403 de elTOQUE

La tarjeta de referencia mantiene equivalentes CUP y el botón ↻. Ver motivo del fallo permite consultar la causa. El usuario confirmó HTTP 403: elTOQUE rechaza la consulta web desde Apps Script, por lo que no se obtuvo una tasa nueva.

La versión 1.5.0 permite configurar la API oficial, que requiere un token propio. El usuario todavía no tiene acceso. [Pasos y enlace para solicitarlo](API_ELTOQUE.md). No envíes tokens por el chat ni los pegues en Código.gs. Hasta tener acceso, B7 admite una referencia manual, sin presentarla como una consulta verificada; B26 controla los intentos automáticos.

## 6. Conserva la nueva firma

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

Se comprobaron 36 pruebas de código y cuatro flujos de navegador. En Android 10 emulado se verificaron sesión diaria, segundo plano, reapertura del proceso, cierre explícito, cambio de día y restablecimiento de PIN. También se comprobaron cesta con gasto libre/GPS/mapa y resultados de búsqueda con teclado real. Se usaron datos ficticios; no se probó un teléfono físico ni se accedió al Sheet privado del usuario.

El código de referencia decompilado no se compila. La extensión se escribe a mano y se verifica que los métodos originales permanezcan intactos salvo la delegación en Bridge.call. Las comprobaciones originales del PIN, intentos fallidos, recuperación y controles financieros se conservan. El backend incorpora referenceSync para recibir la referencia consultada por la APK.

TASALO se consulta condicionalmente en la APK. Se probaron respuestas controladas; no se confirmó una respuesta vigente real desde este entorno.

## Compra y navegación en 1.5.1

- Buscar: se ocultan los paneles grandes y aparecen resultados compactos sobre el teclado. Puedes cambiar categoría, borrar el texto o tocar un resultado sin perder el foco por una reconstrucción de pantalla. «Ver resultados» cierra el teclado.
- Gasto libre: indica concepto, categoría, cantidad, unidad, moneda, precio, formato CUP y plan opcional. Al añadirlo no se registra el gasto: suma con los productos en la cesta o lista pendiente. En Mi cesta puedes editarlo o quitarlo.
- Confirmar cesta: indica el comercio y activa ubicación si quieres coordenadas. Los gastos de la misma confirmación comparten ese lugar; para otro comercio confirma otra cesta. Se conserva la advertencia de compra mensual y el bloqueo de la cesta si hay un Modo compra abierto: importa la cesta a su lista para respetar el tope.
- Mapas: Movimientos → Detalle → Ver mapa abre una aplicación de mapas o, en su ausencia, el navegador. Un comercio sin coordenadas se puede buscar por nombre. Sin ubicación previa no se puede reconstruir el sitio de una compra antigua.
- Dirección: Android intenta reconocer una dirección aproximada si tiene el servicio disponible y conexión, con espera limitada. No garantiza el nombre del mercado o tienda; para identificarlo con precisión escribe su nombre. Sin permiso o sin GPS se guarda el gasto con el lugar manual.

## Sesión diaria y PIN en 1.5.2

Introduce el PIN una vez por día de La Habana. Volver de mapas, apagar la pantalla, pasar a segundo plano, cerrar la app o reiniciar su proceso no cierra la sesión del mismo día. Atrás desde Inicio cierra la app conservando la autenticación.

Para cerrar la sesión o cambiar de persona, usa Perfil → Cerrar sesión. Después se pide el PIN incluso si ya lo introdujiste ese día. Al cambiar de día se vuelve a pedir. Restablecer el PIN invalida la sesión anterior. Se conservan perfiles, movimientos, cesta y listas; no se guarda el PIN para restaurar la sesión.

El botón con un ojo a la derecha permite mostrar u ocultar el PIN. Los campos comienzan ocultos y no pierden su valor al pulsarlo.

La integración condicional y sus límites están en INVESTIGACION_TASA.md; no se afirma haber obtenido una cotización vigente real durante las pruebas.

## Referencia TASALO → teléfono → Sheet

La APK consulta la URL pública documentada por TASALO, sin enviar información del hogar. Lee data.eltoque.USD.rate y la fecha propia sources_updated_at.eltoque. Solo guarda respuestas numéricas válidas del día de La Habana con antigüedad máxima de 30 minutos. No usa la hora de la respuesta HTTP para presentar como nuevo un dato antiguo.

El valor se usa en los equivalentes del teléfono y queda Pendiente de sincronizar hasta que el Sheet confirma su recepción. Si no hay conexión con el Sheet, se conserva en la cola local. Android reintenta el envío mediante un trabajo con requisito de red; el sistema puede retrasarlo por batería. Actualizar manualmente no crea movimientos.

El script recibe el dato con el dispositivo/hogar autorizado, perfil que lo consultó e identificador duradero. Actualiza Ajustes B7 y la fecha de origen B11, y registra la recepción en Actualizaciones. No cambia ingresos, gastos ni tasas históricas. Referencias vencidas, desactivadas o anteriores a la de la hoja se descartan sin sustituirla. Una versión antigua del script deja el dato en espera y muestra el motivo para actualizar.

Mantén B26 = Sí. Si prefieres conservar un valor manual, pon B26 = No. No se garantiza el acceso a TASALO desde todas las redes: el endpoint publicado utiliza HTTP en el dominio tasalo.duckdns.org, habilitado específicamente para ese dominio. Las credenciales de la hoja siguen viajando solo por HTTPS a Google. Ver motivo del fallo conserva la causa de una consulta o sincronización fallida.
