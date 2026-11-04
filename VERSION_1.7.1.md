# Hogar finanzas 1.7.1 · rendimiento y cesta

Añadir, editar y retirar productos ya no espera las peticiones de red de la cesta compartida. El catálogo reutiliza el historial y su índice de precios; se reducen recargas y solicitudes innecesarias. Las ediciones hechas mientras llega una confirmación se conservan con sus UUID y conflictos.

El formulario de compra separa la información y la acción de guardar: el botón ya no se dibuja sobre el proveedor o las notas. Al retirar artículos se conservan el lugar y el desplazamiento; varias pulsaciones rápidas no envían carritos antiguos ni confirman un pago durante la retirada. Compartir permanece dentro del carrito preparado.

Comprar muestra una cesta SVG. Movimientos comienza con 50 filas y permite continuar con «Ver más movimientos», conservando todos los registros. La guía explica los cambios.

Actualiza desde **Perfil → Actualizaciones → Buscar actualizaciones**, o instala [la APK firmada](Hogar_finanzas_1.7.1.apk) encima de la actual. **No desinstales ni borres datos.** Misma identidad y firma; conserva perfiles, fotos, movimientos, compras y conexión. No cambia Apps Script ni requiere ejecutar iniciarHogar. Versión 1.7.1, código29.

Validación: firma, paquete, alineación, assets y recursos comprobados. Todas las clases nativas anteriores salvo HouseholdFeatures permanecen idénticas: finanzas, almacenamiento, autenticación, fotos, documentos, respaldo cifrado, descarga TLS e instalador. Hay67 comprobaciones JVM nuevas de concurrencia y107 anteriores de los modelos; las pruebas de navegador cubren notas largas, tamaños móviles, retirada rápida/fallida, cachés y navegación con3.000 movimientos ficticios. Pasaron las regresiones del catálogo, cesta automática, borrar cesta, perfil y guía.

La primera medición del catálogo en Chromium pasó de128–163ms a10–16ms por redibujado con el mismo historial. Estos tiempos pertenecen al entorno de desarrollo; no se ha perfilado el teléfono del usuario ni probado su Sheet privado. Las alturas reducidas simulan el espacio del teclado y no sustituyen una prueba con teclado Android real.

[Revisión detallada y límites](Revision_Rendimiento_1.7.1.md) · [Validación](Validacion_Hogar_finanzas_1.7.1.json) · [Fuentes y pruebas](https://github.com/Arlbode/google_sheet_bill/tree/rendimiento-hogar-1.7.1/development/1.7.1).
