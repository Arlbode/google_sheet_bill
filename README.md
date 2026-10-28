# Hogar finanzas · Android 1.5.0

Actualización compatible con 1.4.0 y 1.4.1: mismo paquete y firma, con perfiles, movimientos y conexión conservados.

- [Descargar APK firmada](Hogar_finanzas_1.5.0.apk).
- [Script completo del Sheet](Script_Completo_Hogar_Tasa_CUP.txt): sustituye Código.gs completo, ejecuta iniciarHogar y publica una nueva versión de la implementación.
- [Instalación y uso](INSTALAR_NUEVA_APK.md).
- [elTOQUE: HTTP 403 y acceso API](API_ELTOQUE.md).
- [Respaldo cifrado de la firma](Respaldo_Firma_Cifrado.zip).
- [Fuentes y recuperación](Hogar_Playa_Recuperacion_Fuentes.zip).

## Novedades 1.5.0

- Cerrar un Modo compra y abrir otro el mismo día con un nuevo tope, moneda y tasa.
- Historial local de compras cerradas, productos, importes y lugares. Los gastos se conservan y no se duplican al cerrar o repetir una confirmación.
- Trasladar solo los productos pendientes a la siguiente compra, con IDs nuevos y sin registrar gastos por trasladarlos.
- Lugar opcional por compra y producto. Captura puntual de coordenadas al confirmar, con permiso de Android; sin seguimiento en segundo plano. Si falta permiso o posición, se guarda el gasto igualmente.
- Lugar, coordenadas, precisión, fecha, estado y mapa en Registro W:AC del Sheet; JSON interno en AD. La ubicación se conserva al corregir un gasto rechazado.
- Soporte del acceso API oficial de elTOQUE mediante ELTOQUE_API_TOKEN privado en Apps Script. El usuario confirmó HTTP 403 en la consulta web y todavía no tiene acceso API: no se promete una tasa nueva sin ese requisito. La app muestra el motivo del fallo y conserva la referencia anterior.

Se mantienen equivalentes CUP, moneda USD/CUP y formatos, Plan CUP, perfiles/PIN, cola sin conexión, selección múltiple, unidades, estadísticas, imágenes y fondos. Cada tope pertenece a una compra; todos los gastos siguen contando en el presupuesto mensual.

## Actualizar

Esta versión requiere actualizar también el script del Sheet para permitir varios modos del mismo día y preparar las columnas de ubicación. Conserva la URL /exec. Instala la APK encima de Hogar finanzas 1.4.1 o Hogar Playa Nueva 1.4.0, sin desinstalar ni volver a crear perfiles. El respaldo de firma ya entregado sigue siendo válido.

La identidad estable es cu.hogarplaya.recuperada. La clave secreta no está en GitHub; solo está su respaldo cifrado. [Procedimiento de recuperación para futuras sesiones](RESPALDO_Y_RECUPERACION_FIRMA.md).

## Validación

Pruebas de código y navegador más cinco escenarios sobre Android 10 en emulador: actualización con datos previos, cierre/reapertura/pendientes/idempotencia, GPS simulado, GPS desactivado y reinicio con permiso denegado. Se verifican todos los métodos nativos originales salvo la entrada del puente que delega en la extensión. No se compila el Java decompilado. No se realizó una consulta API real de elTOQUE ni una sincronización contra el Sheet privado del usuario.

El panel de configuración de ChatGPT tuvo un conflicto independiente. Esta entrega no afirma haberlo resuelto ni requiere publicar ese panel para instalar la APK.
