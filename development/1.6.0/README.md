# Hogar finanzas 1.6.0 — ocho mejoras

Base: APK 1.5.7 publicada, SHA-256 `9c03f4a3f64940eaf15fb0e6e9f17d24dd941f41dd6b27cfa0ac1cfe741877d3`. Se conserva `cu.hogarplaya.recuperada` y el certificado `7374684814395e3df3e4a41b6d0d20a739dddba331a828d0f72d5d37ea78e6c1`.

En Perfil → Mejoras del hogar:

1. **Respaldo completo cifrado y recuperación.** El selector de Android guarda un `.hgf`. PBKDF2-HMAC-SHA256 (210 000 iteraciones), sal aleatoria y AES-256-GCM autentican el contenido. Contraseña mínima de 10 caracteres, con confirmación. La fecha de último respaldo se registra después de cerrar el archivo correctamente. Comprobar archivo descifra y valida estructura sin importar. El respaldo incluye perfiles con verificadores PIN, conexión y tablas locales; nunca firma APK ni contraseña de firma. La recuperación solo funciona sobre una instalación vacía y conserva UUID, estados, perfiles, compras, catálogos y conexión. No combina bases de datos. Después de migrar se utiliza solo el teléfono nuevo, pues conserva la identidad de conexión del anterior. Conexiones revocadas deben renovarse. La clave local de la API de elTOQUE debe configurarse nuevamente. El JSON antiguo sigue disponible como exportación de movimientos, no como copia completa recuperable.
2. **Sincronización comprensible.** Separación entre guardado local, envío sin confirmar, confirmado por Sheet y rechazado. Panel con movimientos, planes, cola compartida, conflictos y reintentos. La conexión a internet no se presenta como confirmación financiera.
3. **Historial de precios.** Por producto, UUID, fecha, unidad y moneda. Excluye rechazados y duplicados. Comparación con la última compra comparable y aviso de subida ≥15 %. El script incorpora detalles de artículos desde Auditoría app a los últimos 250 movimientos. Las correcciones manuales de totales del Sheet no reconstruyen precios de artículos anteriores. Compras sin detalle no generan precios inventados.
4. **Comparador por unidad.** Libras/kilogramos, litros, unidades y paquetes con contenido conocido. Exige la misma moneda y dimensiones compatibles. No adivina tamaños ni convierte monedas con tasas actuales.
5. **Catálogo editable y fotos.** Nombres, categoría, unidad, referencia, moneda y proveedor; IDs existentes inmutables. Los nuevos tienen UUID. Las fotos pasan por selector Android, decodificación y reducción a JPEG, sin aceptar SVG ni URL ejecutables introducidas por el usuario. Los cambios son locales a cada teléfono y se incluyen en el respaldo. No modifican movimientos ni cotizaciones de la hoja.
6. **Lista compartida.** Cola local duradera con UUID. El script autentica hogar, dispositivo y perfil. Versión por artículo y recibos idempotentes; un conflicto conserva ambas propuestas y requiere elegir. Conserva quién añadió el artículo y quién lo marcó comprado; permite volver a pendiente o retirarlo sin registrar gastos. Cada teléfono conserva su propia conexión. Mientras la app está abierta se intenta sincronizar cada minuto después de abrir la lista o cuando hay datos pendientes; también al recuperar el foco/conexión. No se promete ejecución en segundo plano. Las nuevas pestañas son `Lista compartida app` y `Recibos lista app`; no se usan tablas financieras.
7. **Previsión del presupuesto.** Consumo mensual confirmado más pendientes locales, conservando sus tasas propias y excluyendo fondos y rechazados. Compara dos escenarios: ritmo acumulado/día del mes × días del mes, y acumulado más cesta/lista pendientes con precio y tasa guardados. Toma el mayor sin sumar dos veces las mismas compras futuras. Muestra margen, fórmulas y artículos sin cotizar; no actúa sobre saldos. Una instantánea de otro mes no se calcula como vigente.
8. **Ofertas con procedencia.** Producto, precio, moneda, unidad, proveedor, fecha, enlace y notas de entrega. Las referencias de siete días o más se marcan para confirmar. Registro manual; no usa API ni scraping de Revolico. Los enlaces se presentan como texto escapado y no abren esquemas arbitrarios.

## Instalar y desplegar

Instalar la APK candidata encima de 1.5.7 sin desinstalar ni borrar datos. Para activar colaboración e historial remoto, sustituir Código.gs por `Script_Completo_Hogar_1.6.0.txt` y publicar una nueva versión de la **misma implementación**, conservando `/exec`. Desde una hoja 1.5.7 funcional no hace falta reinicializar. Las dos pestañas auxiliares se crean en el primer intercambio autenticado. Sin el nuevo script los cambios compartidos se conservan en el teléfono y se muestra el error de compatibilidad. El resto de herramientas locales permanece disponible.

La entrega candidata no modifica `actualizacion.json` ni publica un cambio en el canal estable. No se ha desplegado el script sobre el Sheet privado desde esta sesión.

## Construcción

Usar el checkout existente; cada tarea ya está aislada. No crear worktrees sin petición explícita. Leer AGENTS.md y el procedimiento de recuperación de firma. No regenerar firma ni compilar Java decompilado.

Requisitos: Python 3, JDK 21, Android build-tools 35.0.0, plataforma android-35 y JADX 1.5.1. Herramientas descargadas de Google/GitHub con comprobación de los hashes oficiales; las rutas predeterminadas se pueden ajustar con `ANDROID_HOME`, `HOGAR_JADX_JAR`, `HOGAR_BUILD_DIR`.

Las pruebas de navegador requieren Playwright 1.56.0 (`.venv/bin/python -m pip install playwright==1.56.0`) y Chromium en `/usr/bin/chromium`.

Desde `/workspace/google_sheet_bill`:

```sh
python3 development/1.6.0/build.py
node development/1.6.0/tests/improvements.test.cjs
node development/1.6.0/tests/shared.test.cjs
.venv/bin/python development/1.6.0/tests/browser_features.py
```

`build.py` valida la base y la herramienta, añade tres clases nativas escritas a mano y sus clases auxiliares, modifica solamente `MainActivity$Bridge.call` entre métodos existentes y compara el resto de las 89 clases originales después de ensamblar. Los stubs de compilación no se empaquetan. Conserva recursos compilados y todas las entradas originales salvo manifiesto, DEX y assets modificados. Añade una actividad privada no exportada para documentos. Verifica la firma real después de alinear y firmar. La clave permanece fuera del repositorio.

El backend auxiliar es `shared-backend.js`; el archivo completo de Apps Script es el que debe desplegarse. Los fixtures y pruebas no contienen datos del hogar.

## Pruebas Android

Exclusivamente en emulador identificado, con datos ficticios. `tests/build_native_test.py` prepara instrumentación de pruebas firmada con el mismo certificado; no es una APK de usuario y no debe publicarse.

`UpgradeSeed` comprueba perfil, UUID e importe antes y después de instalar encima la candidata. `NativeFeaturesSmoke` exige instalación de prueba vacía, rechaza teléfonos físicos, verifica autenticación del puente, catálogo, cola SQLite sin gastos, imagen, cifrado y recuperación real. Durante esa prueba se borran **solo los datos ficticios de la app de prueba del emulador** para simular recuperación vacía. No ejecutarla sobre un dispositivo del usuario.

Para conservar exactamente el DEX probado en Android al volver a empaquetar assets, `HOGAR_VERIFIED_NATIVE_APK` puede indicar esa APK. El constructor desensambla ambos DEX y exige que todas sus clases coincidan con la compilación actual antes de reutilizarlo. No permite reutilizar código distinto.
