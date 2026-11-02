# Hogar finanzas 1.6.7 — cesta compartida operativa

- Vista compacta con fotos del catálogo de cada teléfono, cantidades/unidades, autor y estado. Cabecera, búsqueda y filtros con contadores Pendientes/Comprados; lista desplazable con filas pequeñas. Las fotos personalizadas de ese catálogo mantienen prioridad.
- Marca de comprado y opciones de editar cantidad/retirar en un menú pequeño. Deshacer recupera la última retirada. Las respuestas automáticas conservan filtros, búsqueda, foco, posición y edición abierta.
- Los cambios se guardan localmente y se envían automáticamente. Mientras la cesta está abierta se reciben cambios aproximadamente cada 10 segundos. Al volver a la app o recuperar red se solicita actualización. La cola se envía con la app abierta incluso al salir de esta vista, con reintentos acotados ante fallo; no se garantiza ejecución en segundo plano.
- Modificar, retirar o confirmar una compra del carrito local ya compartido concilia sus líneas vinculadas. Las líneas importadas a Modo compra mantienen el vínculo para sus cambios/compras. La coordinación nunca crea gastos ni revierte o repite una compra guardada.
- Una intención pendiente se conserva aparte del recibo ya enviado: no se reescribe su UUID ni su payload. Tras confirmarlo, se envía otro cambio con nueva revisión si es necesario. Los conflictos entre teléfonos siguen requiriendo elegir qué conservar.
- Guía actualizada. No hay cambios adicionales de Apps Script si ya está desplegado el script 1.6.0.

## Instalación

Perfil → Actualizaciones → Buscar actualizaciones → Descargar. Instala encima de la versión actual sin desinstalar ni borrar datos. Misma identidad y firma; se conserva el descargador TLS corregido. Los precios y pagos del carrito permanecen locales; la coordinación comparte artículos, cantidades y unidades. Marcar comprado en la lista no registra el gasto: confirma el pago desde Comprar.

## Validación y límites

Versión1.6.7/código25, firma y alineación verificadas. Las clases financieras, autenticación, documentos, SharedCartPlan, TLS e instalador se conservan comparando smali. Solo cambia HouseholdFeatures y se añade SharedCartLive. Recursos compilados idénticos a1.6.6. Pruebas JVM:31 comprobaciones de recibos, intenciones, revisiones, conflictos, borrar/deshacer, migración y conciliación. Pruebas de navegador con datos ficticios: imagen SVG bajo MIME nativo, búsqueda/filtros, filas compactas, recepción automática de cantidad y retirada remotas, envío sin botón de sincronización, offline/deshacer/reconexión y anchos320/360/390px. Una compra simulada rechazada no concilia; si la compra simulada se guarda y luego falla la coordinación, conserva el gasto sin repetirla. Guía:20 temas,30 pasos y7 demos de herramientas, con búsqueda y progreso conservados.

La conciliación es posterior y separada de la operación financiera: no promete atomicidad conjunta si el proceso se interrumpe entre ambas. No se accedió al Sheet privado ni se probó en el teléfono físico del usuario.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/cesta-operativa-hogar-1.6.7
