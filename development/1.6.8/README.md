# Hogar finanzas 1.6.8 — enlaces del catálogo

Añadir producto al catálogo y Comparar por unidad pasan a enlaces simples al final del catálogo, después de los productos, con la clase link utilizada por Ocultar productos mensuales ya comprados. Se retiran los botones superiores. El comparador de Modo compra también utiliza enlace al final de esa vista. Los controladores y las funciones se conservan. La guía indica la ubicación nueva.

`python3 development/1.6.8/build.py` usa APK1.6.7 con SHA fijado, incrementa a 1.6.8/código26 y empaqueta app.js, app.css y guide.js. Compara byte por byte el resto de entradas, incluido classes.dex y recursos compilados. No recompila ni cambia código nativo. Mantiene identidad, firma, datos, cesta compartida automática y descargador existentes.

Comprobado con la prueba funcional de navegador existente (enlaces accionables, carrito, catálogo y anchos320/360/390px), guía (20 temas,30 pasos y7 demostraciones de herramientas), e inspección móvil de la posición tras el catálogo. Firma, paquete, versión y alineación verificados. No requiere cambios en Apps Script.
