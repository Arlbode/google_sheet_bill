# Hogar finanzas · Android 1.5.1

Actualización compatible con 1.4.0 y 1.4.1: mismo paquete y firma, con perfiles, movimientos y conexión conservados.

- [Descargar APK firmada](Hogar_finanzas_1.5.1.apk).
- [Script completo del Sheet](Script_Completo_Hogar_Tasa_CUP.txt): sustituye Código.gs completo, ejecuta iniciarHogar y publica una nueva versión de la implementación.
- [Instalación y uso](INSTALAR_NUEVA_APK.md).
- [elTOQUE: HTTP 403 y acceso API](API_ELTOQUE.md).
- [Respaldo cifrado de la firma](Respaldo_Firma_Cifrado.zip).
- [Fuentes y recuperación](Hogar_Playa_Recuperacion_Fuentes.zip).

## Novedades 1.5.1

- Búsqueda compacta: al escribir, el buscador y los primeros resultados quedan sobre el teclado. Se conserva el foco al filtrar y borrar; «Ver resultados» cierra el teclado conservando la búsqueda.
- «Gasto libre» permite indicar concepto, categoría, cantidad, unidad, precio y Plan CUP. Se añade a la cesta o a la lista del Modo compra y suma al total previsto sin crear un gasto antes de confirmar.
- La cesta permite editar y quitar artículos. Al confirmar guarda comercio y ubicación para todos sus gastos y artículos; conserva perfiles, planes, formatos CUP y detalle por categoría. Los reintentos no duplican gastos.
- «Ver mapa» usa una aplicación externa de mapas o el navegador. Si solo se indicó un comercio, permite buscarlo en el mapa. Android puede reconocer una dirección aproximada al capturar coordenadas, si dispone de servicio; no se inventa el nombre de un comercio.
- Se conservan cierre, nuevo tope, historial local y traslado de pendientes incorporados en 1.5.0.

Se mantienen equivalentes CUP, moneda USD/CUP y formatos, Plan CUP, perfiles/PIN, cola sin conexión, selección múltiple, unidades, estadísticas, imágenes y fondos. Cada tope pertenece a una compra; todos los gastos siguen contando en el presupuesto mensual.

## Actualizar

Instala la APK encima de Hogar finanzas 1.4.1, 1.5.0 o Hogar Playa Nueva 1.4.0, sin desinstalar ni volver a crear perfiles. Mantiene la misma firma. Si ya instalaste el script del Sheet 1.5.0, esta actualización no requiere volver a cambiarlo. Si todavía no lo hiciste, usa el script completo y ejecuta iniciarHogar para preparar ubicaciones y varios modos del mismo día, conservando la URL /exec.

La identidad estable es cu.hogarplaya.recuperada. La clave secreta no está en GitHub; solo está su respaldo cifrado. [Procedimiento de recuperación para futuras sesiones](RESPALDO_Y_RECUPERACION_FIRMA.md).

## Validación

Pruebas de código y navegador más siete escenarios sobre Android 10 en emulador: actualización con datos previos, cierre/reapertura/pendientes/idempotencia, GPS simulado, GPS desactivado, reinicio con permiso denegado, cesta con gastos libres, ubicación y apertura nativa del mapa con alternativa al navegador y teclado real con resultados visibles. Se verifican todos los métodos nativos originales salvo la entrada del puente que delega en la extensión. No se compila el Java decompilado. No se realizó una consulta API real de elTOQUE ni una sincronización contra el Sheet privado del usuario.

El panel de configuración de ChatGPT tuvo un conflicto independiente. Esta entrega no afirma haberlo resuelto ni requiere publicar ese panel para instalar la APK.
