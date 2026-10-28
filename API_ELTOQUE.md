## Modalidad actual desde 1.5.2

Por indicación del usuario, la APK consulta la API pública de TASALO y guarda la referencia local. La sincronización la envía al Sheet. No requiere un token de elTOQUE del usuario. Se valida USD de elTOQUE con fecha propia reciente y se conserva el valor anterior si la consulta falla. El servicio público se usa condicionalmente; su disponibilidad vigente no se pudo confirmar desde este entorno.

Esta modalidad requiere el script completo 1.5.2 y una nueva versión de la implementación. iniciarHogar establece HOGAR_TASA_DESDE_APK=1 para que el servidor no vuelva a consultar el sitio de elTOQUE por su cuenta. B26 controla la recepción. [Implementación, fuente y límites](INVESTIGACION_TASA.md).

El resto de este documento describe la modalidad anterior de API oficial desde el servidor. Un propietario que quiera retomarla debe disponer de su token y poner HOGAR_TASA_DESDE_APK=0 en Propiedades del script.

# Por qué no se actualizaba la tasa

Los registros del usuario muestran **HTTP 403** al consultar elTOQUE desde Apps Script. El sitio rechazó esas peticiones. El valor anterior de 840 se conservó; no era una consulta nueva. Cambiar únicamente el lector HTML no resuelve una denegación HTTP.

Desde 1.5.0 el script admite la **API oficial autenticada**, `https://tasas.eltoque.com/v1/trmi`. Si aún no hay token, mantiene el intento web compatible y explica el 403 sin inventar una cotización. El usuario confirmó que todavía no tiene acceso API.

## Activar el acceso oficial cuando esté disponible

1. Solicita tu acceso en **https://tasas-token.eltoque.com/**. La concesión del acceso depende de elTOQUE.
2. En el Sheet, abre **Extensiones → Apps Script → Configuración del proyecto → Propiedades del script**.
3. Añade la propiedad **ELTOQUE_API_TOKEN**. En su valor pega el token recibido del proveedor, sin el prefijo `Bearer`. **No lo pegues en el chat ni dentro de Código.gs.**
4. Guarda la propiedad. Mantén **Ajustes B26 = Sí**.
5. Usa el script completo de esta versión y su nueva implementación. Ejecuta `actualizarAutomaticamente` o pulsa ↻ en la app después de un minuto desde el último intento.

La cabecera de autorización se envía solo a la API oficial, sin seguir redirecciones. El token no se devuelve a la APK ni se guarda en los registros o archivos públicos. No se necesita otra APK al recibir el acceso: basta con configurar la propiedad.

Consulta oficial de la API: https://tasas.eltoque.com/docs/ . La integración sigue el contrato GET `/v1/trmi`, autenticación Bearer y respuesta TRMI con `tasas.USD`. Se comprobó con respuestas simuladas; no se realizó una consulta autenticada real porque no se dispone de un token del usuario.

## Mientras no haya acceso API

Puedes introducir una referencia en **Ajustes B7**. Se muestra como referencia manual, sin presentarla como una cotización automática verificada. Si no deseas que continúen los intentos automáticos, cambia **B26 a No**; vuelve a **Sí** cuando configures el acceso.

En la tarjeta de tasa de la APK, **Ver motivo del fallo** muestra la causa devuelta por el Sheet. En **Actualizaciones**, una consulta correcta aparece como `VERIFICADO CAMBIO` o `CAMBIO`. `SIN CAMBIOS` significa que la consulta se completó pero la fuente mantuvo el mismo valor.

Ni las consultas ni la edición de la referencia reescriben las tasas históricas de ingresos, gastos o cambios USD→CUP. La cotización publicada es orientativa; la tasa real se registra en cada operación.
