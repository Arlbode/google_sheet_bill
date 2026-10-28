# Qué hacer ahora · Hogar finanzas 1.5.2

1. Sustituye Código.gs completo por Script_Completo_Hogar_Tasa_CUP.txt. Guarda y ejecuta iniciarHogar. Este paso sí es necesario aunque ya tuvieras el script 1.5.0: añade la recepción de tasas desde la APK.
2. Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar. Conserva la URL /exec, Ejecutar como Yo y acceso Cualquier persona.
3. Instala Hogar_finanzas_1.5.2.apk encima de la app actual, sin desinstalar. Mantiene paquete y firma, perfiles, movimientos, cesta y conexión.
4. Introduce tu PIN una vez por día de La Habana. Cerrar/reabrir la app, apagar pantalla, ir a mapas o reiniciar el proceso conserva la sesión del día. Perfil → Cerrar sesión la elimina; volver a entrar requiere PIN. Al cambiar el día o restablecer PIN también se autentica de nuevo. El ojito muestra u oculta el PIN sin borrarlo.
5. La APK consulta TASALO al abrir o vencer la referencia; ↻ permite consultar manualmente. Guarda el dato de elTOQUE en el teléfono y muestra si está pendiente de sincronización. La sincronización entrega el dato al Sheet, incluso mediante una cola con reintentos de red; Ajustes B7 recibe el valor y B11 su fecha de origen. Mantén B26 = Sí. No modifica tasas históricas ni crea movimientos.
6. Si TASALO no está accesible o su dato no es reciente, se conserva la referencia anterior con su fecha. La disponibilidad real desde tu red no se pudo verificar aquí; se probaron respuestas controladas y conservación de la cola. Ver motivo del fallo explica errores de consulta o de envío.
7. Si el script aún es antiguo, el dato permanece en el teléfono y se avisa de actualizar la implementación. No se descarta por un fallo de sincronización. B26 = No permite conservar una referencia manual.

Se mantienen cierre/historial de Modo compra, gasto libre en cesta/lista, ubicación opcional, mapa externo, perfiles, planes y fondos. Guarda Respaldo_Firma_Cifrado.zip y tu archivo privado Clave_PRIVADA_Respaldo_Hogar.json; la firma no cambia.

Guía completa: INSTALAR_NUEVA_APK.md. Fuente y funcionamiento de TASALO: INVESTIGACION_TASA.md. El conflicto del panel de ChatGPT no se ha confirmado resuelto; la entrega se genera desde esta sesión activa.
