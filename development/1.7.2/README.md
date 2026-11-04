# Hogar finanzas 1.7.2 · espaguetis y claridad de saldos

Añade **Espaguetis** a Comprar, con ilustración de pasta en tonos dorados, banda verde y fondo transparente del estilo del catálogo. Permite buscar también `spaguetis`, `spaghetti` y `espagueti`. Categoría inicial Canasta básica, unidad inicial paquete y sin precio, proveedor ni peso inventados. La cantidad y el precio se confirman al añadir; el producto puede editarse como los demás.

El producto incorporado aparece aunque el catálogo remoto del Sheet todavía no lo tenga. Solo este producto lleva la marca de incorporación; no se mezclan indiscriminadamente los productos antiguos de la APK con los del Sheet. Si el Sheet o el catálogo local ya lo incluyen, se conserva su identidad y precio. Las ediciones y fotos locales tienen prioridad. No necesita cambiar Apps Script ni ejecutar iniciarHogar; no añade automáticamente una fila a Lista compras del Sheet.

La nueva ilustración se generó usando las ilustraciones públicas de arroz, zanahoria y papa como referencia de estilo. Se entrega en un SVG autocontenido que incluye el PNG generado, con transparencia y sin recursos externos. Así conserva el formato/MIME esperado por el cargador Android y no repite el problema de entregar un PNG declarado como SVG. El navegador comprueba los píxeles visibles del contenido interno, además de cargar el archivo.

La tarjeta Disponible para consumo explica que es un equivalente de USD y CUP libres limitado por el presupuesto. El formulario Cambiar USD muestra los USD libres sin convertir CUP. Se actualiza la guía. Son aclaraciones de lectura: no cambian importes, ahorro, fondos, protección USD, planes ni validaciones.

## Construcción y validación

`python3 development/1.7.2/build.py` usa la APK pública 1.7.1, SHA-256 `38863a429ead1eabd4bb402913f1799d2f7c5374526a9de8a9b1cb03fe65754e`. Produce 1.7.2/código 30, con el mismo paquete y certificado. La firma permanece fuera del repositorio; leer AGENTS.md y el procedimiento de recuperación antes de utilizarla. Nunca generar otra.

Conserva el DEX completo byte por byte, incluidas todas las reglas financieras, autenticación, sincronización, fotos, respaldo y descarga TLS. Conserva también los recursos compilados y las entradas no autorizadas. Solo cambia versión, cinco assets y añade la ilustración. Verifica certificado, paquete, alineación, integridad y correspondencia exacta entre assets de las fuentes probadas y APK.

- `node development/1.7.2/tests/catalog.test.cjs`: cinco pruebas del alta, catálogo remoto anterior, alias sin duplicados, precio/identidad remotos y edición/foto local conservadas.
- `.venv/bin/python development/1.7.2/tests/browser_catalog.py`: catálogo remoto sin pasta, imagen con píxeles internos, búsqueda, anchos 320/360/390 px, precio editable, añadir al carrito sin gasto, edición y recarga sin duplicar; claridad de USD/CUP con datos ficticios.
- Regresiones de herramientas: siete pruebas Node. Regresiones de navegador: catálogo/carrito/funciones y guía de 20 temas, 31 pasos y 7 demostraciones de herramientas.

No se ha probado un teléfono físico ni desplegado el Sheet privado desde este cambio. Los archivos financieros proporcionados para revisión permanecen fuera del repositorio y de la distribución; las pruebas publicadas usan datos ficticios.
