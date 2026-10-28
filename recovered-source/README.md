# Desarrollo de Hogar finanzas 1.5.2

La base es la APK 1.3.0 verificada. Conserva Hogar_Playa.apk como entrada del constructor. No compiles el Java de android/reference-java/: es decompilado y tiene errores de reconstrucción.

## Extensión nativa

android/native-src/ contiene ShoppingLifecycle, PurchaseLocation, PurchaseActions, DailySession y ShoppingExtension, escritos a mano. android/compile-stubs/ contiene solo firmas para compilar; no se empaqueta en la APK.

build_native.py desensambla el DEX base con una herramienta oficial fijada por SHA-256, compila únicamente la extensión, añade una delegación en MainActivity$Bridge.call y ensambla. Después vuelve a desensamblar y verifica que todos los demás métodos originales sean idénticos. La representación del campo booleano TESTING=false puede omitir el valor falso, que es su valor inicial por defecto. Se comprobó en Android que TESTING sigue falso.

El cierre cambia la clave única de día de la sesión a fecha#closed#UUID y añade quién/cuándo cerró. No elimina sesiones, líneas ni operaciones. La fecha del payload y los UUID financieros permanecen. Abrir crea una sesión nueva; opcionalmente copia solo pendientes. Las modificaciones son transaccionales y los reintentos no duplican operaciones.

La ubicación se captura únicamente al confirmar, con permiso y opción activada. Se almacena con la operación y el artículo antes de iniciar su sincronización. Sin permiso o posición se registra igualmente. La corrección de un gasto rechazado conserva su ubicación; las demás correcciones siguen por el puente original.

## Comprobar

```sh
HOGAR_PYTHON=/workspace/google_sheet_bill/.venv/bin/python bash recovered-source/scripts/verify_environment.sh
python3 recovered-source/tests/browser_smoke.py
python3 recovered-source/tests/browser_shopping_smoke.py
python3 recovered-source/tests/browser_purchase_ux.py
```

Las pruebas usan datos ficticios y no el Sheet privado. Para la prueba nativa opcional se necesita un emulador Android 10 local en puerto 5555 y adb-shell==0.4.4:

```sh
PYTHONPATH=/workspace/.tools/hogar-adb-python python3 recovered-source/tests/run_android_smoke.py --reset
```

--reset borra solo las apps de prueba en ese emulador. El programa rechaza dispositivos que no se identifiquen como emulador. La instrumentación se firma con la misma clave para probar la actualización, pero no se empaqueta ni publica como app de usuario. Los escenarios comprueban persistencia, cierre, nuevos topes, ubicación, permisos, cesta con gastos libres, mapa nativo y resultados sobre el teclado real.

## Compilar actualizaciones

```sh
source /workspace/.tools/hogar-environment/env.sh
python3 recovered-source/scripts/build_release.py
```

Requiere JDK, Android build-tools 35.0.0 y plataforma android-35. El paquete cu.hogarplaya.recuperada y la firma fijada no se cambian. Lee RESPALDO_Y_RECUPERACION_FIRMA.md para restaurar claves, nunca para regenerarlas. El constructor descarga su herramienta de ensamblado si falta y verifica dos SHA-256 del release oficial de JADX 1.5.1.

El script completo del backend es Script_Completo_Hogar_Tasa_CUP.txt. Ejecuta iniciarHogar y publica una nueva versión de la implementación para preparar las columnas W:AD y el control por UUID de Modo compra. Lee API_ELTOQUE.md para el 403 y la integración API opcional; ninguna credencial se incluye en la APK.

El respaldo público se crea con scripts/create_backup.py y contiene solo fuentes, certificados públicos y la copia cifrada de la firma. Los paquetes sin firma de build_unsigned.py siguen siendo comprobaciones de recuperación, no instaladores de usuario.

Desde 1.5.1, PurchaseActions reutiliza el checkout original dentro de una transacción. Compara semánticamente la cesta antes y después de capturar ubicación, independientemente del orden de claves JSON. Incorpora el lugar a cada operación y al primer artículo para su auditoría sin repetir coordenadas en todos los productos del mismo grupo, y guarda un recibo por requestId para evitar duplicados en reintentos. Conserva el bloqueo de checkout si hay Modo compra activo. Mapas solo acepta coordenadas válidas o un lugar y construye geo/HTTPS; no acepta URLs arbitrarias del WebView. El geocodificador de Android es opcional, espera como máximo dos segundos y nunca sustituye un comercio escrito manualmente.

## Sesión diaria 1.5.2

DailySession guarda únicamente perfil, día de America/Havana y huella de versión del verificador PIN. Restaura la sesión al pedir estado o ejecutar una acción, incluso tras cerrar el proceso. No conserva el PIN. Cierre explícito, cambio de día, perfil inexistente o cambio del verificador invalidan esa restauración. El estado contiene día y vencimiento, nunca la huella privada.

La extensión recibe todas las llamadas del puente en la cola original de trabajo. Para comandos base invoca por reflexión el despachador sintético verificado, sin volver a encolar ni modificar su implementación; el constructor fija ese contrato. Antes de un unlock se elimina la sesión anterior y solo se conserva la nueva cuando el código original dejó activo el perfil solicitado tras verificar el PIN. CreateProfile conserva su validación original de perfiles remotos. El control de primer plano, intentos fallidos y autorización temporal de recuperación sigue intacto.

Prueba de interfaz: `python3 recovered-source/tests/browser_daily_session.py`. Escenarios nativos: daily, dailyReopen y dailyLoggedOut; verifican más de cinco minutos de inactividad simulados, segundo plano, día anterior, restablecimiento de PIN, reinicio de proceso, cierre explícito y acceso posterior bloqueado. La fecha anterior se simula en los metadatos de la sesión, sin cambiar fechas financieras ni el reloj de Android.

Investigación de fuentes de tasa: INVESTIGACION_TASA.md; la disponibilidad real de la fuente no se verificó, pero su consumo condicional se implementó por indicación del usuario.

## TASALO 1.5.2

TasaloReference consulta el endpoint público documentado, valida solo USD de elTOQUE y sources_updated_at.eltoque, y conserva una referencia pendiente duradera en SQLite. No sustituye la fecha de origen por fetchedAt/receivedAt. TasaloSyncService (job 5403 persistente, requisito de red) envía referenceSync con el Vault y ApiClient originales. Hay limitación de consultas y reintentos; un recibo antiguo no borra un evento nuevo y una respuesta de otra hoja no se acepta.

El backend autoriza dispositivo/hogar antes de recibir la referencia y conserva perfiles originales. Se registran actor, dispositivo, UUID y fechas; se modifican solo Ajustes B7/B11 y Actualizaciones. Datos vencidos, anteriores o desactivados reciben un descarte sin cambiar el valor. HOGAR_TASA_DESDE_APK evita que una consulta web del servidor sobrescriba esta modalidad. La configuración Android permite HTTP únicamente para el dominio publicado del proveedor; las credenciales van por HTTPS a Google y nunca al GET público.

Los escenarios nativos tasalo/tasaloReopen usan respuestas controladas para comprobar origen, fecha, cola persistente, recibos y aislamiento financiero. Las pruebas tasalo.test.cjs verifican el receptor y su autorización real por token de dispositivo. No sustituyen una consulta viva a la instancia ni una sincronización al Sheet privado.
