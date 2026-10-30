# Hogar finanzas 1.5.7

Instala `Hogar_finanzas_1.5.7.apk` encima de la 1.5.6, sin desinstalar ni borrar datos. Conserva paquete y certificado.

Añade Zanahoria al catálogo local sin inventar precio ni proveedor y con ilustración SVG en el estilo existente. Pimiento mantiene su identidad, precio y proveedor; su imagen admite también el nombre Ají pimiento del Sheet. No duplica el producto.

El script corregido ya publicado se conserva sin cambios. Sincroniza la app para recibir las hortalizas de la hoja; la APK no modifica por sí sola las filas del Sheet.

Se reutilizaron byte por byte el DEX y los recursos compilados de la APK 1.5.6 verificada. Solo cambian versión del manifiesto, catálogo e imágenes. Se verificaron firma v2/v3, certificado, paquete, versión 17, alineación y catálogo sin alteraciones en productos existentes. La prueba Chromium con puente simulado comprobó ambas hortalizas, alias, imágenes y ancho móvil. No se probó instalación en Android ni sincronización contra el Sheet privado en esta sesión.

Consulta `Validacion_Hogar_finanzas_1.5.7.json` para el resultado. Ningún archivo privado de firma se incluye en esta publicación.
