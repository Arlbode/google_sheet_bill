# Hogar Playa · Android 1.2.0

Paquete para Android 8 o posterior. Instala sobre la versión anterior para conservar perfiles, PIN, cestas y registros.

- [Hogar_Playa.apk](Hogar_Playa.apk): aplicación firmada.
- [Script_Completo_Hogar_Android.txt](Script_Completo_Hogar_Android.txt): único script completo para Código.gs.
- [Hogar_Playa_Instalacion.zip](Hogar_Playa_Instalacion.zip): APK, script, manual y verificaciones.
- [Manual_Android.md](Manual_Android.md): instalación, actualización y uso.
- Hogar_Playa.apk.sha256: huella de la APK.

## Cambios

Cuenta CUP única con efectivo y transferencia, desglose en el Tablero y en el móvil, formato al ingresar/gastar CUP y efectivo predeterminado al cambiar USD. Modo compra con límite por jornada, lista con cantidad × precio y unidad, gasto automático al confirmar, lista tachada, sugerencias según historial y confirmación antes de repetir mensuales. Mejoras de teclado, barras de Android y logotipo para volver a Inicio.

## Actualizar

1. Sustituye TODO Código.gs por el script completo y ejecuta iniciarHogar.
2. Recarga el Sheet. Cuentas B7 es el CUP inicial total; B24 indica qué parte estaba en transferencia. Los registros antiguos sin formato se consideran efectivo y pueden reclasificarse.
3. Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar. Conserva la URL /exec, Ejecutar como Yo y acceso Cualquier persona.
4. Instala la APK encima de la anterior y sincroniza. Actualiza todos los teléfonos antes de registrar transferencias. Si ya estaba vinculada con la misma URL, no necesitas otro código.

El límite es por fecha y teléfono, compartido entre sus perfiles. Salir no reinicia el gasto. Las sugerencias requieren al menos tres fechas de compra identificadas. Lista compras N configura compra mensual (1) o repetición libre (0). El manual detalla uso sin internet, unidades y actualización.

No se incluyen credenciales de conexión ni movimientos financieros reales. La autorización e implementación final de Google se hacen desde la cuenta propietaria.
