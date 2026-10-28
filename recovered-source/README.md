# Desarrollo de Hogar finanzas 1.5.0

La base es la APK 1.3.0 verificada. Conserva Hogar_Playa.apk como entrada del constructor. No compiles el Java de android/reference-java/: es decompilado y tiene errores de reconstrucción.

## Extensión nativa

android/native-src/ contiene ShoppingLifecycle, PurchaseLocation y ShoppingExtension, escritos a mano. android/compile-stubs/ contiene solo firmas para compilar; no se empaqueta en la APK.

build_native.py desensambla el DEX base con una herramienta oficial fijada por SHA-256, compila únicamente la extensión, añade una delegación en MainActivity$Bridge.call y ensambla. Después vuelve a desensamblar y verifica que todos los demás métodos originales sean idénticos. La representación del campo booleano TESTING=false puede omitir el valor falso, que es su valor inicial por defecto. Se comprobó en Android que TESTING sigue falso.

El cierre cambia la clave única de día de la sesión a fecha#closed#UUID y añade quién/cuándo cerró. No elimina sesiones, líneas ni operaciones. La fecha del payload y los UUID financieros permanecen. Abrir crea una sesión nueva; opcionalmente copia solo pendientes. Las modificaciones son transaccionales y los reintentos no duplican operaciones.

La ubicación se captura únicamente al confirmar, con permiso y opción activada. Se almacena con la operación y el artículo antes de iniciar su sincronización. Sin permiso o posición se registra igualmente. La corrección de un gasto rechazado conserva su ubicación; las demás correcciones siguen por el puente original.

## Comprobar

```sh
HOGAR_PYTHON=/workspace/google_sheet_bill/.venv/bin/python bash recovered-source/scripts/verify_environment.sh
python3 recovered-source/tests/browser_smoke.py
python3 recovered-source/tests/browser_shopping_smoke.py
```

Las pruebas usan datos ficticios y no el Sheet privado. Para la prueba nativa opcional se necesita un emulador Android 10 local en puerto 5555 y adb-shell==0.4.4:

```sh
PYTHONPATH=/workspace/.tools/hogar-adb-python python3 recovered-source/tests/run_android_smoke.py --reset
```

--reset borra solo las apps de prueba en ese emulador. El programa rechaza dispositivos que no se identifiquen como emulador. La instrumentación se firma con la misma clave para probar la actualización, pero no se empaqueta ni publica como app de usuario. Cinco escenarios comprueban persistencia, cierre, nuevos topes, ubicación y permisos.

## Compilar actualizaciones

```sh
source /workspace/.tools/hogar-environment/env.sh
python3 recovered-source/scripts/build_release.py
```

Requiere JDK, Android build-tools 35.0.0 y plataforma android-35. El paquete cu.hogarplaya.recuperada y la firma fijada no se cambian. Lee RESPALDO_Y_RECUPERACION_FIRMA.md para restaurar claves, nunca para regenerarlas. El constructor descarga su herramienta de ensamblado si falta y verifica dos SHA-256 del release oficial de JADX 1.5.1.

El script completo del backend es Script_Completo_Hogar_Tasa_CUP.txt. Ejecuta iniciarHogar y publica una nueva versión de la implementación para preparar las columnas W:AD y el control por UUID de Modo compra. Lee API_ELTOQUE.md para el 403 y la integración API opcional; ninguna credencial se incluye en la APK.

El respaldo público se crea con scripts/create_backup.py y contiene solo fuentes, certificados públicos y la copia cifrada de la firma. Los paquetes sin firma de build_unsigned.py siguen siendo comprobaciones de recuperación, no instaladores de usuario.
