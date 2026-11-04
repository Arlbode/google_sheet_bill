# Hogar finanzas 1.7.1 · respuesta local y cesta

Parte de la APK pública 1.7.0/código 28, SHA-256 `d67706ff6020e28e9ec0783398e5b18db286ec46e642daa9e8d6f8fbde229ced`. Genera 1.7.1/código 29 con el mismo paquete y certificado. Conserva datos al instalar encima. No requiere cambiar Apps Script ni ejecutar iniciarHogar.

## Problemas encontrados y correcciones

1. `HouseholdFeatures` hacía HTTP de `featuresSync` dentro de `SyncEngine.WORK`, un ejecutor de un solo hilo que también atiende acciones locales. Una petición lenta detenía las siguientes acciones. `SharedSync` prepara el lote en WORK, hace HTTP en un ejecutor separado y vuelve a WORK para aplicar recibos. No mantiene bloqueada la base durante la red. Hay una sola petición compartida simultánea.
2. Al liberar WORK pueden producirse cambios locales durante HTTP. `SharedSyncBatch` relee el estado actual al recibir la respuesta: retira solo UUID del lote realmente confirmado y conserva productos, intenciones, eliminaciones y operaciones nuevas. Un cambio posterior recibe otro UUID/revisión mediante el modelo existente. Mantiene los conflictos para resolución manual. Respuestas de otra conexión, token, sesión o perfil no se aplican. Un error se anota sobre el estado actual, sin reponer una copia antigua.
3. Cada tarjeta volvía a combinar/ordenar el historial y recorrer todos los artículos. El historial se memoriza por identidad de los arrays y un índice de precios se calcula una vez por conjunto de datos. Un nuevo estado invalida ambos. Las comparaciones mantienen moneda, unidad, UUID y exclusión de rechazos. Se elimina una búsqueda del índice del movimiento que no se utilizaba.
4. `load` dibujaba antes y después de cargar extensiones, y volvía a solicitar todas las fotos. Ahora agrupa lecturas simultáneas y dibuja una vez. Una notificación llegada durante la lectura programa otra pasada. Las fotos se reutilizan durante un minuto fuera de Perfil; Perfil y el selector bloqueado las vuelven a leer. Guardar un carrito con estado completo evita otra consulta redundante.
5. Los carritos privados provocaban conciliación y sondeo compartido sin estar vinculados. Ahora se coordinan solo los vinculados y se solicita envío cuando hay cambios pendientes o la cesta compartida está abierta. Las cantidades, retiradas y compras de líneas vinculadas siguen solicitando conciliación automática después del éxito financiero. No hay transacción conjunta entre el gasto original y esa conciliación; si el proceso se interrumpe entre ambos sigue existiendo esa limitación anterior.
6. El botón sticky de los formularios se dibujaba encima de las notas. La cabecera, el área desplazable y el pie opaco son independientes. La acción permanece visible sin superponerse al texto. Compartir el carrito queda en su contenido, debajo de los artículos, y no agranda el pie de confirmación.
7. Dos retiradas rápidas del carrito podían enviar dos copias antiguas de sus líneas. La interfaz serializa la retirada hasta recibir el guardado local, conserva lugar/repetición/desplazamiento y permite reintentar un fallo. No confirma un pago mientras se retira un artículo.
8. Movimientos presenta 50 filas al entrar y permite continuar con «Ver más movimientos». Conserva todos los registros y los cálculos; limita el DOM inicial. Comprar utiliza una cesta SVG con el color activo del menú. La guía explica el formulario y el acceso gradual al historial.

## Medición y pruebas

Fixture: 3.000 gastos, cinco artículos por gasto, catálogo de 92 productos, Chromium del entorno a 390 px. Cinco redibujados con el mismo estado pasaron de 460 recorridos completos y 128–163 ms por dibujo a 0 recorridos adicionales y 10–16 ms en la primera medición posterior. El índice ya estaba preparado; se reconstruye una vez al cambiar el estado. Los tiempos fluctúan con la carga del entorno y no representan un teléfono Android.

- `python3 development/1.7.1/tests/run_native_queue.py`: ejecuta los dos ayudantes Java reales con dobles de Android/almacenamiento/transporte. 67 comprobaciones de HTTP detenido, WORK disponible, cambios posteriores, borrar durante envío, UUID inmutables, respuestas incompletas/incorrectas, conflictos, error de red y rechazo al cambiar endpoint/hogar/token/perfil/sesión. Las tareas locales tardaron 0–2 ms mientras HTTP esperaba una señal. No es una medición de SQLite o de red real.
- `.venv/bin/python development/1.7.1/tests/browser_performance.py`: JS/CSS reales, puente ficticio. Índice reutilizado e invalidado con compras nuevas, sin sondeo del carrito privado, un dibujo por notificación, fotos reutilizadas y 50/100 filas del historial sin borrar datos.
- `.venv/bin/python development/1.7.1/tests/browser_layout.py`: producto con nota larga, anchos 320/360/390 px, alturas 360/420 px simulando espacio reducido. Texto separado de la acción, añadir sin gasto, retiradas rápidas, error local y datos del formulario conservados.
- Regresiones: siete pruebas Node de herramientas; 107 comprobaciones JVM de compartir/coordinar/borrar/fotos/copia cifrada; navegador de catálogo, cesta automática, borrar cesta, perfiles y guía de 20 temas con 31 pasos/7 demostraciones de herramientas.

No se ha perfilado un teléfono físico ni ejecutado un emulador Android en esta entrega. No se probó el Sheet privado. La comprobación de altura reducida no sustituye el teclado Android real.

## Construcción reproducible

`python3 development/1.7.1/build.py`. Requiere las herramientas y rutas de 1.7.0; firma privada fuera del repositorio. Lee AGENTS.md y el procedimiento original de recuperación; nunca generes otra clave. No compiles Java decompilado.

Compila solo fuentes propias y sustituye `HouseholdFeatures`, añadiendo `SharedSync` y `SharedSyncBatch`. Compara todas las otras clases smali de 1.7.0, incluidas finanzas, almacenamiento, autenticación diaria, fotos, documentos, copia cifrada, descarga TLS e instalador. Los stubs no se empaquetan. Verifica identidad, firma y alineación, y que todos los recursos/archivos no autorizados sean idénticos a la base. El canal se activa solo al publicar APK y manifiesto y descargar/verificar la copia pública.
