# Hogar finanzas 1.6.2 — Perfil, movimientos y capturas

- Mejoras del hogar pasa al primer lugar de Perfil, antes de la tarjeta del Sheet.
- Perfil deja de ofrecer Plan de pagos y el botón de prueba del aviso. Plan CUP conserva su acceso desde Inicio y sus funciones existentes.
- Permisos reúne Avisos de necesidad de CUP y Ubicación en las compras, usando las mismas acciones nativas y sin cambiar sus valores.
- Movimientos muestra el concepto a todo el ancho disponible junto al icono; importe y confirmación ocupan una fila inferior. La etiqueta visible se reduce a Confirmado, con el texto completo Confirmado por el Sheet en accesibilidad y ayuda.
- Se permiten capturas en la pantalla principal y en la actividad de documentos. HouseholdFeatures limpia FLAG_SECURE en la primera entrada al puente y en las siguientes; HouseholdDocuments lo limpia en onCreate. MainActivity no vuelve a activar ese indicador en onResume. No se altera la autenticación ni el almacenamiento.
- La guía indica la nueva ruta Perfil → Permisos y sustituye la explicación del botón de prueba retirado por la administración de permisos.

## Construcción y comprobación

Usar el checkout existente. `python3 development/1.6.2/build.py` parte de la APK 1.6.1 con SHA fijado. Se puede indicar su ruta con HOGAR_BASE_APK. Recompila únicamente las dos clases auxiliares HouseholdFeatures y HouseholdDocuments; conserva todos los demás métodos, incluido el puente y todo el código financiero, comparando el desensamblado completo. Verifica el JAR de herramientas, firma, identidad, versión 1.6.2/código 20, alineación y recursos compilados.

Pruebas de navegador: `development/1.6.0/tests/browser_features.py` comprueba el orden de Perfil, los menús retirados, Permisos, datos conservados y Movimientos a 320/360/390 píxeles, sin recortes y con etiqueta menor de 85 píxeles. `development/1.6.1/tests/browser_guide.py` conserva los 21 temas, 102 pasos y ocho ejemplos nuevos.

`tests/build_test.py` construye instrumentación específica para un emulador identificado. CaptureSmoke comprueba las banderas reales de ambas ventanas y obtiene una captura Android no negra. No publica esa instrumentación ni modifica datos del hogar.

No requiere cambios en Apps Script sobre una instalación 1.6.0/1.6.1 ya configurada.
