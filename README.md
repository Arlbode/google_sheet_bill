# Hogar Playa · Android 1.2.1

Android 8 o posterior. Actualización compatible con la firma anterior; instala encima sin borrar datos.

- [Hogar_Playa.apk](Hogar_Playa.apk): APK firmada.
- [Hogar_Playa_Instalacion.zip](Hogar_Playa_Instalacion.zip): APK, único script completo, manual, oferta CSV y verificaciones.
- [Script_Completo_Hogar_Android.txt](Script_Completo_Hogar_Android.txt): sustituye todo Código.gs y ejecuta iniciarHogar.
- [Manual_Android.md](Manual_Android.md): operación, actualización y prueba del aviso USD.
- [Oferta_Yosvany_2026-10-27.csv](Oferta_Yosvany_2026-10-27.csv): los 56 importes recibidos, con unidad o presentación pendiente indicada.

## Novedades

- El PIN conserva el formulario y el foco ante una actualización en segundo plano.
- Selección múltiple de productos, revisión conjunta y guardado atómico. Los borradores sin precio se completan antes de registrar gastos.
- La tarjeta verde del Modo compra suma toda la lista y muestra: tope menos comprado menos lista pendiente. Señala estimaciones parciales y excesos; confirmar no descuenta dos veces.
- Perfil → Probar aviso de protección USD envía PRUEBA y el diagnóstico actual sin alterar dinero ni planes.
- Oferta Yosvany: 56 referencias en CUP, 41 productos nuevos, 88 en catálogo. Granel por libra confirmado por el usuario. Se respetan los envases explícitos y se marcan los que necesitan confirmar presentación. Directorio conserva el origen; no se inventa contacto, dirección o enlace externo.

## Actualizar

1. En tu Sheet → Extensiones → Apps Script, sustituye TODO Código.gs por el archivo completo; guarda y ejecuta iniciarHogar.
2. Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar. Conserva URL /exec y permisos.
3. Instala la APK encima de la anterior y sincroniza. Si conservaste la URL y el proyecto, no necesitas otro código de conexión.

Plan CUP es una agenda de pagos futuros en pesos. No registra gastos por sí solo. El campo Pago planificado CUP (opcional) vincula el gasto real a esa agenda para reducir lo pendiente; si no tenías un plan, déjalo sin asociar. El manual explica cómo probar las notificaciones y la regla sin registrar dinero ficticio.

La autorización e implementación de Google las realiza el propietario. No se publican credenciales ni movimientos financieros reales.
