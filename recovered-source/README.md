# Desarrollo de Hogar finanzas 1.4.1

La base nativa procede de la APK 1.3.0 verificada. No se compila el Java de `android/reference-java/`: es una referencia decompilada con errores de reconstrucción. El DEX original se conserva exactamente en la nueva APK.

Conserva `Hogar_Playa.apk` como base de compilación aunque la app antigua se retire del teléfono. El constructor comprueba su hash antes de reutilizar el DEX.

## Código actual y procedencia

Se modificaron `android/assets/app.js`, `app.css`, `index.html` y `backend/Code.gs` para añadir equivalentes CUP y la tarjeta de tasa. Los hashes `exact-sha256.json` describen los originales recuperados, no la interfaz modificada; las pruebas contrastan los originales con la APK base y el script 1.3.0 preservado, y mantienen la integridad de los demás assets. El backend completo actual se publica como `Script_Completo_Hogar_Tasa_CUP.txt`.

## Validar

```sh
HOGAR_PYTHON=/workspace/google_sheet_bill/.venv/bin/python bash recovered-source/scripts/verify_environment.sh
```

Requiere Python con openpyxl 3.1.5, Node y LibreOffice. Las pruebas financieras no usan el Sheet real. El ZIP de fuentes se reconstruye con `python3 recovered-source/scripts/create_backup.py`; `--check` verifica su manifiesto sin extraerlo.

## Compilar y firmar futuras actualizaciones

```sh
source /workspace/.tools/hogar-environment/env.sh
python3 recovered-source/scripts/build_release.py
```

Requiere Android build-tools 35.0.0 y plataforma android-35. Restaura los archivos del respaldo privado en `/workspace/.android-signing/hogar-playa-v2/`. El certificado debe coincidir con `signing/new-certificate.sha256`. No subas la clave, contraseña ni ZIP privado a GitHub. No generes otra clave para una actualización de la nueva app.

`release.json` fija la identidad `cu.hogarplaya.recuperada`. Incrementa su versión para futuras entregas, manteniendo la identidad y la firma. `--init-signing` solo se utilizó en la creación autorizada de esta nueva app; una vez fijado el certificado, el constructor no permite regenerar la clave si falta.

`build_unsigned.py` sigue disponible para comprobar la recuperación de assets conservando el manifiesto original. Sus paquetes sin firma no son instaladores. `sign_original.py` solo sirve para la identidad antigua si se recuperase su firma; no es el constructor de la app nueva.

[Instalación de usuario](../INSTALAR_NUEVA_APK.md).
