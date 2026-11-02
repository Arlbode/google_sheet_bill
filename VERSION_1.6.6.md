# Hogar finanzas 1.6.6 — foto y carrito compartido

- Papa/Patata utiliza una imagen SVG compatible con el cargador gráfico de Android. El PNG anterior se entregaba con tipo SVG y mostraba el texto alternativo. La ilustración conserva sus colores y transparencia.
- Se retiran de Comprar los bloques Cesta del hogar compartida y Ofertas y proveedores.
- En el carrito ya preparado aparece Compartir este carrito con el hogar. Se comparten nombres, cantidades y unidades, usando los artículos guardados en el carrito del perfil. Los precios y pagos permanecen en el carrito local.
- Ver mi carrito está disponible aunque no haya productos locales, para consultar lo compartido desde otro teléfono. La coordinación, comprado y conflictos se gestionan dentro del carrito.
- Compartir no crea gastos, no confirma pagos ni vacía el carrito. Repetir no duplica las mismas líneas. La cola conserva revisiones y no sobrescribe conflictos. Fecha, lugar y fondo del formulario se conservan al volver de consultar lo compartido.
- La guía refleja el flujo actual y retira las instrucciones del menú de ofertas. Las ofertas anteriores continúan guardadas y se incluyen en los respaldos.

## Instalar

Desde Perfil → Actualizaciones → Buscar actualizaciones, o descarga la APK de esta rama e instala encima de la actual, sin desinstalar ni borrar datos. Se conserva el descargador TLS corregido en 1.6.5. No requiere ejecutar iniciarHogar ni cambios adicionales de Apps Script si ya está desplegado el script completo 1.6.0, que activa la colaboración.

## Validación

Versión 1.6.6, código 24; mismo paquete y certificado. Se conserva el código financiero original, autenticación, documentos, TLS e instalador, comparados en smali después de ensamblar. La nueva planificación se valida con 22 comprobaciones JVM de atomicidad, identidad, cantidades, unidades, límites, repetición, revisión y conflicto. Pruebas de navegador con el MIME SVG real del cargador Android: imagen cargada, compartir desde carrito, consulta con carrito vacío, conservación del lugar, sin duplicados ni gastos, sin los bloques retirados; pantallas a 320/360/390px sin desbordamiento. Guía: 20 temas, 30 pasos de siete herramientas actuales y siete demostraciones; búsqueda y progreso conservados. Firma, versión, alineación e integridad de la APK verificadas. No se accedió al Sheet privado ni se probó en el teléfono del usuario.

El entorno anterior fue eliminado por la plataforma. Las fuentes se recuperaron de GitHub y la firma original del respaldo privado en un entorno nuevo. No se creó otra clave.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/carrito-hogar-1.6.6
