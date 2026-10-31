# Hogar finanzas 1.6.3

Las ocho mejoras se integran en las pantallas existentes:

- Inicio: previsión y margen del mes visibles; desglose, confirmaciones, errores y reintentos desplegables.
- Comprar: lista compartida y ofertas con datos y acciones en la misma pantalla; comparación por unidad, alta de productos y edición/foto en cada tarjeta. Disponible también durante Modo compra.
- Movimientos: selector de producto y precios históricos antes del listado de actividad, sin abrir otra pantalla.
- Perfil: respaldo cifrado y recuperación, conexión, permisos, perfiles, actualizaciones y guía. El JSON de movimientos sigue dentro de Respaldo.

Se retira el menú aislado Mejoras del hogar. La guía conserva los 21 temas y el progreso con rutas actualizadas. Las secciones abiertas y el producto consultado se conservan al recibir actualizaciones automáticas.

## Instalación

Actualiza desde Perfil → Actualizaciones → Buscar actualizaciones, o instala la APK encima de la actual sin desinstalar. Misma identidad y firma. No hay cambios de base de datos ni se necesita ejecutar iniciarHogar. No requiere cambios adicionales de Apps Script si ya está desplegado el script 1.6.0. El script incluido es el mismo publicado anteriormente; para activar colaboración desde una versión anterior, actualiza la misma implementación /exec.

## Validación

Código 21. DEX nativo y recursos compilados idénticos a 1.6.2. Certificado, paquete, alineación y SHA verificados. Pruebas con datos ficticios de edición/foto, comparación, respaldo, lista sin gastos y ofertas escapadas. Cuatro pantallas a 320/360/390 px sin desbordamiento horizontal. Guía: 34 pasos y ocho demostraciones nuevas, búsqueda y progreso. Siete pruebas del modelo de precios, unidades, previsión y ofertas. No se ha accedido al Sheet privado.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/integracion-hogar-1.6.3
