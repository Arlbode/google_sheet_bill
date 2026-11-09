# Hogar finanzas 1.7.4 — compras individuales y gastos reales

Mi cesta y la cesta compartida permiten **Registrar compra** por artículo, con cantidad y precio reales, moneda, formato CUP, fecha, fondo, plan opcional y lugar. Una cantidad parcial deja el resto pendiente. Sigue disponible registrar todos los pendientes del carrito personal. La marca ✓ de la lista compartida continúa siendo solo coordinación y no crea dinero.

Los gastos reales se guardan aunque excedan el saldo libre o lo reservado: se muestra un aviso con su impacto. Se conservan los saldos negativos y el fondo elegido; no se inventan ingresos, conversiones ni retiros de ahorro. Se mantienen PIN, perfil, conexión, validación de datos, UUID, correcciones y topes de las jornadas. Si un artículo compartido pertenece a Modo compra, la compra parcial conserva su gasto acumulado y crea una línea pendiente para la cantidad restante.

La transacción local incluye gasto, cantidad pendiente, cola compartida y recibo por solicitud. Un reintento no vuelve a registrar el gasto. Los cambios de otro teléfono pueden producir un conflicto de lista; no se sobrescriben y no se elimina por ello un gasto real. La sincronización financiera y la de listas son peticiones independientes al servidor: no se promete exclusión entre dos teléfonos desconectados que registren por separado una misma compra.

## Construcción y firma

Base 1.7.3/code31 verificada por SHA-256. La APK 1.7.4/code32 conserva paquete `cu.hogarplaya.recuperada` y certificado `7374684814395e3df3e4a41b6d0d20a739dddba331a828d0f72d5d37ea78e6c1`. Instalar encima conserva los datos. No hay migraciones automáticas ni limpieza.

`build.py` compila únicamente las extensiones escritas a mano y stubs de firmas. Sustituye `HouseholdFeatures` y añade `ExpenseBridge`/`ExpensePurchases`. Compara todas las demás clases de la base después de ensamblar. Las clases originales de finanzas, compras, almacenamiento, PIN, sincronización, TLS y actualizaciones permanecen iguales; la nueva política de gastos se despacha desde la extensión y reutiliza su normalización y persistencia. No se compila Java decompilado ni se empaquetan stubs. Recursos compilados e ilustraciones permanecen idénticos.

```sh
python3 development/1.7.4/build.py
python3 development/1.7.4/tests/run_native.py
node development/1.7.4/tests/backend.test.cjs /ruta/Script_Completo_Hogar_1.7.4_Compras_Individuales.txt
.venv/bin/python development/1.7.4/tests/browser_purchases.py
```

La prueba Java ejecuta las extensiones reales contra dobles JVM de Android/SQLite, con fallos y cambios de sesión controlados. No es instrumentación en un dispositivo. Chromium usa puente simulado y datos ficticios; verifica formularios, monedas, compras parciales, doble toque, imágenes y 320/360/390px. Las comprobaciones nativas del constructor verifican firma, alineación, versión e invariancia del resto de clases y recursos.

## Script: publicar una versión en la misma implementación

La APK necesita la política del script 1.7.4 para que el Sheet también acepte gastos que afecten a reservas. `build_backend.py` modifica únicamente `validarReserva153_` para gastos y el indicador `expensePolicy` de `movilSnapshot_`; exige conservar exactamente todo el resto del código ejecutable. Sustituye el antiguo encabezado de limpieza por instrucciones de actualización sin borrados.

```sh
python3 development/1.7.4/build_backend.py \
  --input /ruta/Script_Completo_Actual_Privado.txt \
  --output /ruta/Script_Completo_Hogar_1.7.4_Compras_Individuales.txt
```

El archivo completo se entrega por descarga privada. Sustituir Código.gs y usar **Implementar → Gestionar implementaciones → Editar → Nueva versión → Implementar** en la implementación actual. Conservar su `/exec`, propiedades y conexión. Después sincronizar la APK. **No ejecutar iniciarHogar ni ninguna limpieza, dejarListoParaProduccion, reinicio de acceso o finanzas cero.** Los registros de producción ya existen y deben conservarse. Si un gasto fue rechazado por el script anterior, abrirlo en Movimientos y usar Corregir y reintentar después de publicar la nueva versión; no crear otro gasto.

Publicar la APK o este archivo no despliega Apps Script. No se accedió ni se escribió al Sheet privado desde esta entrega.
