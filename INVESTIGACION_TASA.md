# Alternativas para obtener la referencia de elTOQUE

Investigación: 28 de octubre de 2026, fecha de La Habana. La versión 1.5.2 incorpora la consulta condicional en la APK y la entrega al Sheet por sincronización. No se obtuvo una cotización vigente real desde este entorno.

## Resultado concreto: API pública de un tercero

**TASALO API** documenta una instancia pública de lectura sin API key. Es una redistribución de terceros; no es una API oficial de elTOQUE. Su código consulta la API oficial mediante las credenciales del operador del servicio.

- [Documentación de consumo publicada](https://github.com/TASALO-TEAM/taso-api/blob/1f5cbf01252018c53d06a44b003fd760a7cb1b03/docs/PUBLIC_API.md).
- [Código del cliente de elTOQUE](https://github.com/TASALO-TEAM/taso-api/blob/1f5cbf01252018c53d06a44b003fd760a7cb1b03/src/scrapers/eltoque.py).
- URL publicada: `http://tasalo.duckdns.org:8040/api/v1/tasas/latest`.
- Valor documentado: `data.eltoque.USD.rate`.
- Fecha real de la fuente: `sources_updated_at.eltoque`. **No usar `updated_at` como fecha de la cotización:** la documentación indica que puede ser solo la hora de la respuesta.
- Frecuencia anunciada: 15 minutos. Hay respaldo histórico cuando una fuente falla; una respuesta correcta puede contener un dato antiguo.

La documentación y el código se pudieron leer. **No se pudo comprobar una respuesta vigente de la instancia publicada**, ni una dirección HTTPS operativa, desde este entorno: el acceso directo fue rechazado por el proxy de red. Ese rechazo no prueba que el servicio esté caído para otros clientes.

El usuario indicó que la consulta debe realizarse en la APK. Se implementó con el endpoint HTTP publicado, permitido únicamente para el dominio tasalo.duckdns.org. No se envían credenciales a ese proveedor. La APK exige el dato USD de elTOQUE y su fecha propia, como máximo 30 minutos de antigüedad y del día de La Habana. Si no se cumple o no hay acceso, conserva el valor anterior con su fecha y muestra el fallo. Las tasas CADECA/BCC/QvaPay del mismo servicio son otras referencias y no se pueden sustituir silenciosamente por la de elTOQUE.

## Bot de Telegram indicado por el usuario

[Bot @el_toque_bot](https://t.me/el_toque_bot). El usuario señala que allí puede consultar la referencia. No se envió ningún mensaje, no se inició sesión con una cuenta de Telegram y no se leyó su chat privado.

El nombre del bot no proporciona por sí solo un endpoint de datos para Apps Script. Automatizar sus respuestas requiere una integración de Telegram autorizada y documentada. No se encontró esa integración pública durante esta comprobación; tampoco se verificó una respuesta actual del bot desde el entorno.

Una vía práctica sin token de elTOQUE sería que el usuario reenvíe o comparta el mensaje recibido con una integración propia y confirme la tasa/fecha antes de aplicarla. Requiere desarrollar esa recepción; **no está implementada en 1.5.2**. Si el mensaje es una imagen, no se debe inventar un importe a partir de una lectura dudosa. Actualmente se puede introducir el valor consultado en Ajustes B7 como referencia manual.

## Sitios que republican información

Se intentó consultar [CiberCuba](https://www.cibercuba.com/tasas-de-cambio-cuba) y [Directorio Cubano](https://www.directoriocubano.info/tasa-de-cambio/), entre otros candidatos. El proxy de este entorno rechazó las consultas, por lo que no se verificaron el contenido actual, sus fechas ni un contrato de extracción estable. No se añadieron como fuentes activas basándose solo en sus nombres o enlaces.

Un artículo sobre el dólar puede citar fechas anteriores, precios de compra/venta, USD y USDT, o tasas oficiales distintas. Una futura integración debe identificar específicamente USD/CUP atribuido a elTOQUE y la fecha de publicación del dato.

## Bloqueo en Cuba y arquitectura recomendada

El bloqueo que experimenta el usuario en Cuba afecta al acceso desde su conexión. La petición de la hoja sale desde servidores de Google; el HTTP 403 que figura en Actualizaciones también puede ser una restricción del sitio o de su protección automática. No se confirmó la causa exacta del 403 del Sheet.

La arquitectura elegida por el usuario es: **APK → TASALO → caché local → sincronización autenticada → Sheet**. La consulta pública no contiene datos del hogar. El token de conexión al Sheet se envía únicamente por HTTPS al endpoint Apps Script ya vinculado.

La cola de referencias es independiente de los movimientos, persiste al cerrar la app y usa un JobService con restricción de red para reintentar el envío. El Sheet valida dispositivo, hogar, perfil, identificador, fuente y fechas; actualiza únicamente Ajustes B7/B11 y registra la recepción. No crea ingresos, gastos ni cambios USD→CUP. Una referencia antigua o anterior a la de la hoja no la sustituye; los reintentos se identifican para no reaplicar el dato.

Se debe actualizar y publicar el script completo de 1.5.2. Ajustes B26 controla la recepción automática. Si se desea conservar una referencia manual, B26 debe estar en No. Desde iniciarHogar de esta versión se selecciona el modo HOGAR_TASA_DESDE_APK=1: el servidor recibe referencias y no consulta por su cuenta otra fuente. La disponibilidad real del servicio sigue pendiente de comprobar desde la conexión del teléfono; no se afirma que funcione sin VPN en todas las redes de Cuba.

La [API oficial con token propio](https://tasas-token.eltoque.com/) sigue siendo la vía de acceso directo documentada. Algunos proyectos comunitarios llamados «bot de tasas» o «wrapper» también necesitan ese token, por lo que instalarlos no resuelve por sí solo su ausencia. Véanse [eltoque-python](https://github.com/Alejo2075/eltoque-python) y [Alerta Dólar Cuba](https://github.com/DDD66218/alerta-dolar-cuba).

Ninguna referencia orientativa reescribe las tasas históricas ni realiza cambios USD→CUP automáticamente. La tasa real acordada se conserva en cada operación.

Documentación de TASALO contrastada con la revisión pública `1f5cbf01252018c53d06a44b003fd760a7cb1b03`; los importes que aparecen allí son ejemplos y no una tasa vigente.
