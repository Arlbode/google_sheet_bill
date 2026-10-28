# Firma privada: para qué sirve y cómo la recuperará el agente

La firma identifica las APK de Hogar Playa Nueva. Android permite actualizar la app instalada cuando la nueva APK mantiene su identificador y está firmada con la misma clave. La clave no cifra los gastos ni reemplaza el PIN del usuario. Si se pierde, no puede reconstruirse desde la APK o su certificado público.

El usuario pidió que el agente se encargue de la recuperación en futuras sesiones. Este procedimiento queda en GitHub para que no dependa de recordar el chat. La clave secreta no se publica ni se memoriza en el modelo.

## Identidad estable

- Paquete: `cu.hogarplaya.recuperada`.
- Certificado fijado: `recovered-source/signing/new-certificate.sha256`.
- Clave local: `/workspace/.android-signing/hogar-playa-v2/hogar.p12`.
- Contraseña local: `/workspace/.android-signing/hogar-playa-v2/password`.
- Constructor: `recovered-source/scripts/build_release.py`.

No crees otra firma ni cambies el paquete en una actualización. Si falta la clave, restaura la existente. El constructor lo exige.

## Respaldo que debe conservarse

La descarga directa de `Firma_PRIVADA_Hogar_Playa_Nueva.zip` falló. Se preparó `Herramienta_Respaldo_Firma.zip`, que genera en la computadora del usuario:

1. **Clave_PRIVADA_Respaldo_Hogar.json**: archivo privado que solo conserva el usuario, con otra copia privada. No se comparte su contenido en el chat ni se sube a GitHub.
2. **HOGAR-PUBLICA-1:…**: código público que sí puede pegarse en el chat.

Al recibir el código público, el agente ejecuta:

```sh
.venv/bin/python recovered-source/scripts/encrypt_signing_backup.py \
  --backup /workspace/shared/downloads/Firma_PRIVADA_Hogar_Playa_Nueva.zip \
  --public-code-file /tmp/codigo-publico-hogar.txt \
  --output Firma_Hogar_Playa_Cifrada.json
```

El resultado usa RSA-OAEP SHA-256 y AES-256-GCM. **Solo ese archivo cifrado** puede publicarse en GitHub. La clave privada de recuperación permanece fuera del repositorio. La entrega cifrada está pendiente hasta recibir el código público del usuario; no se debe afirmar que ya existe.

## Recuperación en otro entorno

Primero verifica si los dos archivos de firma locales ya existen. Si existen, el constructor verifica el certificado; no hace falta restaurarlos.

Si faltan, el usuario puede recuperar localmente el ZIP con la herramienta del navegador. Los archivos privados deben ponerse en el entorno por un canal privado de archivos; nunca pedir contraseñas ni claves pegadas en el chat.

Si está disponible el ZIP privado:

```sh
.venv/bin/python recovered-source/scripts/restore_signing.py \
  --private-zip /ruta/privada/Firma_PRIVADA_Hogar_Playa_Nueva.zip
```

Si están disponibles el acceso privado y el respaldo cifrado:

```sh
.venv/bin/python recovered-source/scripts/restore_signing.py \
  --access-private /ruta/privada/Clave_PRIVADA_Respaldo_Hogar.json \
  --encrypted Firma_Hogar_Playa_Cifrada.json
```

El restaurador valida formato, integridad y certificado real del PKCS12 antes de instalarlo fuera del checkout, con permisos privados. Conserva archivos existentes y rechaza otra firma. No imprime secretos.

Después, incrementa la versión en `release.json`, compila con `build_release.py` y verifica la firma con apksigner. No uses `--init-signing` para sustituir una clave que falta.

Los agentes pueden ejecutar la recuperación, pero necesitan que exista el respaldo y que el usuario conserve el acceso privado. Un chat o un entorno temporal no sustituyen esa copia durable.
