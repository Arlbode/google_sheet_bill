# Hogar finanzas · Android 1.5.2

Actualización compatible con 1.4.0 y 1.4.1: mismo paquete y firma, con perfiles, movimientos y conexión conservados.

- [Descargar APK firmada](Hogar_finanzas_1.5.2.apk).
- [Script completo del Sheet](Script_Completo_Hogar_Tasa_CUP.txt): sustituye Código.gs completo, ejecuta iniciarHogar y publica una nueva versión de la implementación.
- [Instalación y uso](INSTALAR_NUEVA_APK.md).
- [TASALO desde la APK y sincronización al Sheet](INVESTIGACION_TASA.md).
- [Respaldo cifrado de la firma](Respaldo_Firma_Cifrado.zip).
- [Fuentes y recuperación](Hogar_Playa_Recuperacion_Fuentes.zip).

## Novedades 1.5.2

- PIN una vez por día de La Habana; sesión conservada al cerrar/reabrir la app o pasar a segundo plano, incluso tras terminar el proceso.
- Perfil → Cerrar sesión permite salir o cambiar de persona. Atrás cierra la app sin cerrar la sesión. El cambio de día y el restablecimiento del PIN vuelven a pedir autenticación.
- Ojito en los campos de PIN: muestra/oculta sin borrar el valor ni perder el foco. El PIN no se guarda para conservar la sesión.
- Se mantienen la búsqueda sobre el teclado, gastos libres en cesta/lista, lugar de compra, mapa externo y controles financieros de 1.5.1.
- TASALO se consulta en Android, se valida la fecha propia de elTOQUE y se guarda en una cola local. La sincronización autenticada actualiza la referencia del Sheet sin tocar movimientos. La API se utiliza si está disponible; un fallo o dato antiguo conserva la referencia anterior.

Se mantienen equivalentes CUP, moneda USD/CUP y formatos, Plan CUP, perfiles/PIN, cola sin conexión, selección múltiple, unidades, estadísticas, imágenes y fondos. Cada tope pertenece a una compra; todos los gastos siguen contando en el presupuesto mensual.

## Actualizar

Instala la APK encima de Hogar finanzas 1.4.1, 1.5.0, 1.5.1 o Hogar Playa Nueva 1.4.0, sin desinstalar ni volver a crear perfiles. Mantiene la misma firma. Esta revisión sí requiere sustituir el script completo, ejecutar iniciarHogar y publicar una nueva versión conservando la URL /exec: añade referenceSync para recibir el dato consultado por la APK.

La identidad estable es cu.hogarplaya.recuperada. La clave secreta no está en GitHub; solo está su respaldo cifrado. [Procedimiento de recuperación para futuras sesiones](RESPALDO_Y_RECUPERACION_FIRMA.md).

## Validación

36 comprobaciones de código, cuatro pruebas de navegador y siete escenarios Android 10 en emulador para esta revisión: sesión diaria, reapertura del proceso, cierre explícito, cesta con GPS/mapa, teclado real, recepción de TASALO y persistencia/confirmación de su cola. Se comprobó el cambio de día y la invalidación al restablecer el PIN, sin modificar fechas financieras ni el reloj del dispositivo. No se realizó una prueba en un teléfono físico, una sincronización contra el Sheet privado ni una consulta vigente de una fuente alternativa de tasa.

Se conserva la verificación de todos los métodos originales salvo Bridge.call. No se compila el Java decompilado y no se cambian el paquete ni la firma.

El panel de configuración de ChatGPT tuvo un conflicto independiente. Esta entrega no afirma haberlo resuelto ni requiere publicar ese panel para instalar la APK.
