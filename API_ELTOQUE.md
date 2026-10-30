# API oficial de elTOQUE · Hogar finanzas 1.5.6

La APK consulta directamente **https://tasas.eltoque.com/v1/trmi** por HTTPS. La clave del acceso aprobado se configura en **Perfil → API oficial de elTOQUE** y se guarda cifrada en ese teléfono, con Android Keystore. La clave no está incrustada en la APK pública, no vuelve al WebView y no se entrega al Sheet ni al respaldo de movimientos.

## Actualizar el Sheet sin perder la conexión

1. Sustituye Código.gs completo por `Script_Completo_Hogar_1.5.6.txt` y guarda.
2. Ejecuta **activarApiOficialDesdeAndroid**. Esta función selecciona la recepción de referencias y actualiza la guía; conserva tasa, saldos, movimientos, fondos, ahorros y el valor de B26. No hace consultas ni pide la clave.
3. En **Implementar → Gestionar implementaciones → lápiz**, elige **Nueva versión → Implementar** sobre la implementación que ya utiliza la app. Mantén la misma URL **/exec** y su acceso actual. No hace falta volver a vincular el teléfono.
4. Mantén **Ajustes B26 = Sí** si quieres recibir las referencias. Si está en No, se respeta la referencia manual.

## Configurar el teléfono

1. Instala la **1.5.6** sobre la actual, sin desinstalar.
2. Abre **Perfil → API oficial de elTOQUE** y pega la clave aprobada por el proveedor. Puedes pegarla con o sin el prefijo Bearer. Pulsa **Guardar**.
3. Vuelve a Inicio y pulsa **↻**. Después sincroniza para enviar al Sheet solo la referencia. La configuración intenta consultar al cerrarse el diálogo cuando hay una clave guardada y conexión.
4. En otro teléfono configura su clave una vez. En Perfil puedes reemplazarla o quitarla; actualizar la app conserva la configuración del teléfono.

No copies la clave dentro del script ni de los archivos de instalación. El modo directo no necesita ELTOQUE_API_TOKEN en el Sheet. La implementación conserva el modo opcional anterior del servidor solo si se configura expresamente; el modo recomendado y solicitado es el directo desde Android.

## Cómo se interpreta la respuesta

Se lee únicamente **tasas.USD**, numérico, finito y positivo. USD no se sustituye por EUR, MLC o variantes de USDT. GET /v1/trmi sin parámetros devuelve el cálculo sobre las últimas 24 horas al responder.

**Fecha de consulta** es el instante en que el teléfono recibió y validó la respuesta. La hora date/hour/minutes/seconds informada por el proveedor se muestra por separado, sin convertirla a UTC o La Habana: su zona no está documentada. No se presenta como hora de publicación de una tasa diaria.

Las consultas automáticas se intentan cuando vence la referencia de 30 minutos. Se limita a una por minuto y se respeta la espera tras HTTP 429. La app no sigue redirecciones con la clave. No hay llamadas a proveedores alternativos ni permisos HTTP.

Sin conexión, con 401/403, 429, error de servidor, respuesta inválida o dato de fecha anterior se conserva la referencia guardada y se muestra el motivo. La sincronización dispone de su propia cola y recibos idempotentes. Una referencia no crea movimientos ni cambia tasas históricas.

## Verificación y límite de la prueba

Se prueban respuestas controladas y una credencial ficticia, incluida la cabecera Bearer al único destino, cifrado Android, errores, límites, cola y conservación de datos. La clave real del usuario no se incorpora a las pruebas ni a los artefactos. El entorno restringe el acceso al dominio oficial, por lo que no se confirma aquí una consulta real autenticada ni una cotización vigente.

Documentación del proveedor: https://tasas.eltoque.com/docs/ y https://tasas.eltoque.com/static/swagger.json . La estructura se contrastó además con el cliente público https://github.com/Alejo2075/eltoque-python . La tasa de elTOQUE es una referencia indicativa; la operación real conserva la tasa acordada.
