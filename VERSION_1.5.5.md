# Hogar finanzas 1.5.5 · guía visual interactiva

La guía extensa se sustituye por un manual visual con 12 temas y 61 pasos. Se abre desde Perfil → Cómo usar Hogar finanzas o desde la pantalla de acceso, sin necesidad de crear un perfil para descubrir la app.

Comprar, Modo compra, Fondos y Plan CUP cuentan con acceso directo a su capítulo desde la pantalla correspondiente. Cada paso combina una pantalla ilustrativa, un elemento resaltado y una explicación breve. Los detalles se despliegan cuando se necesitan.

El buscador encuentra funciones dentro de los capítulos y abre el paso coincidente. La reproducción opcional avanza cada diez segundos; puede pausarse y se detiene al salir, ocultar la app o abrir los consejos. Las animaciones respetan la preferencia de reducir movimiento.

Los ejemplos permiten confirmar una cesta, pagar desde un fondo, conciliar un Plan CUP y confirmar o cerrar un Modo compra. Usan datos ficticios y no registran operaciones. En especial, explican por qué el fondo y el plan no provocan un descuento duplicado y cómo se calcula el disponible después de la lista.

El progreso se guarda en la preferencia privada `hogar_guide_v1`, con tema, paso y temas completados. Los dos comandos de lectura y escritura admiten únicamente los temas y pasos conocidos; no acceden a movimientos, PIN, conexión ni credenciales. Se conserva desactivado el almacenamiento web y se mantienen los límites de red y archivos del WebView.

Mismo paquete `cu.hogarplaya.recuperada`, misma firma y versionCode 15. Se instala encima de la 1.5.4. No requiere modificar el backend ni volver a vincular el teléfono. Los archivos de la 1.5.4 publicada se conservan.

La APK y el manifiesto se distribuyen juntos en el canal `actualizaciones-android`, con autorización del usuario. En la 1.5.4: Perfil → Actualizaciones → Buscar actualizaciones.

## Cobertura del manual

| Tema | Pasos | Recursos cubiertos |
| --- | ---: | --- |
| Comprar y mi cesta | 7 | Búsqueda, categorías, teclado, ilustraciones, precios y unidades, cantidad, monedas y formatos, gasto libre, planes por artículo, edición y retirada, fondos, ubicación, confirmación, repetición mensual, reintentos e importación al Modo compra. |
| Modo compra | 9 | Inicio, tope propio, moneda y tasa fijas, recuperar pendientes, selección individual y múltiple, estimación parcial, lista pendiente, edición, confirmación, fondos y planes, mensuales, sugerencias y requisitos, retomar, cerrar, trasladar pendientes, historial y mapas. |
| Fondos y ahorro | 6 | Total y libre, aportes por moneda y formato, los tres puntos de selección del fondo, saldo exigible, pagos, balances e historial acumulados, retiro de ahorro, presupuesto y saldos iniciales. |
| Plan CUP | 7 | Crear, importe total, fecha, categoría e importancia, previsto/pagado/pendiente, filtros, pago parcial, fondo y plan, editar/cancelar/reactivar, conflicto entre teléfonos y protección USD. |
| Entender Inicio | 4 | Meta mensual, ingresos, presupuesto y dinero libre, tasas históricas, asignaciones del Sheet, tres saldos, equivalentes, negativos, registros rápidos y última sincronización. |
| Registrar movimientos | 5 | Ingreso USD/CUP, gasto, pago desde fondos, Plan CUP, cambio real USD→CUP, referencia al cobrar, ahorro, fondos, retiro y estado del envío. |
| USD, CUP y TASALO | 4 | Fuente, fechas, referencia orientativa, consulta manual y automática, límites, error y dato anterior, cola de envío, B26 y tasa manual. |
| Movimientos y correcciones | 4 | Historial local y últimos 250 de la hoja, estados, detalle y artículos, persona, lugar, mapa, identificadores, rechazo, corrección propia y corrección en el Sheet. |
| Perfiles y conexión | 5 | Nuevo perfil o perfil del Sheet, PIN y ojito, código de un uso, URL /exec, acceso 401/403, sesión diaria de La Habana, cierre, recuperación del PIN y conservar conexión al actualizar. |
| Sin internet y respaldo | 4 | Cola local, límites sin red, sincronización, batería, dos teléfonos, exportación JSON, privacidad del respaldo y precauciones antes de borrar datos. |
| Avisos y ubicación | 3 | Permiso de notificaciones, aviso de necesidad de CUP, horizonte y antelación, prueba local del aviso, ubicación optativa y puntual y compra sin GPS. |
| Actualizaciones de la app | 3 | Versión y novedades, aviso opcional, descarga, integridad/firma, permiso de instalaciones, confirmación de Android, conservación de datos y fallo de red. |

## Verificación

La prueba `browser_guide.py` recorre todos los capítulos y pasos en la interfaz real, comprueba los ejemplos, búsqueda, progreso, reanudación, reproducción, teclado, Atrás, tamaño móvil y aislamiento de los comandos financieros.

`run_guide_smoke.py` prueba el WebView real del emulador Android 10, los recursos locales y el progreso nativo con el almacenamiento web desactivado. Rechaza dispositivos que no sean el emulador. La instrumentación es independiente del instalador de usuario.

Las comprobaciones generales y de compra, fondos, sesión y actualizaciones deben pasar antes de entregar. El constructor conserva todos los métodos originales excepto la entrada autorizada del puente, y comprueba la firma fijada.

Validación completada: 19 pruebas Python, 34 casos JavaScript y siete recorridos de navegador, incluido el manual completo. En Android 10 pasó la prueba del WebView real: recursos locales, progreso privado, rechazo de datos inválidos, reanudación y ejemplo de pago desde un fondo sin cambios en los movimientos. Se verificaron los ajustes originales de seguridad del WebView. No se probó en un teléfono físico ni se accedió al Sheet privado.

APK: 200.822 bytes. SHA-256: `bc83f4bd550d5ce7cc92321e20356b5449ff6a84bbc3891eb055a4df17ae910c`.
