# Qué hacer con lo que recibiste

## 1. Guarda el ZIP en tu computadora

Descarga `Hogar_Playa_Recuperacion_Fuentes.zip` desde esta rama de GitHub y guárdalo en una carpeta como **Documentos → Hogar Playa → Respaldos**. Conserva además una copia en otro lugar que controles.

Es un respaldo para recuperar y continuar el desarrollo. **No se instala en el teléfono, no se pega en Apps Script y no necesitas descomprimirlo para seguir usando la app.** No contiene tus movimientos reales, PIN ni la clave privada de Android. No sustituye un respaldo de tus datos.

El ZIP incluye la APK original 1.3.0, la interfaz y sus imágenes, el script completo, datos de referencia, herramientas de comprobación y Java decompilado solo como referencia. No equivale a recuperar exactamente todos los archivos fuente originales. `MANIFIESTO_RESPALDO.json` enumera los archivos con sus SHA-256.

## 2. Sigue usando la misma app y el mismo Sheet

Por esta reparación no necesitas reinstalar la app ni volver a crear la hoja. Conserva la app instalada y sus datos. Los registros pendientes de envío siguen en el teléfono hasta que se sincronicen.

En cada teléfono:

1. Abre Hogar Playa y entra con tu perfil.
2. Con internet, ve a **Perfil → Sincronizar ahora**.
3. Comprueba en **Movimientos** los estados **Sincronizado**, **En el teléfono** o **Revisar**. Un registro rechazado necesita corregirse; sincronizar no lo convierte automáticamente en válido.
4. Usa **Perfil → Exportar respaldo local** y guarda el archivo JSON. Es una copia adicional de perfiles, movimientos, cambios de planes y jornada de compra; no contiene contraseñas. La app actual no ofrece una restauración automática de ese JSON.

Si no puede sincronizar, conserva la app y sus datos mientras se resuelve la conexión. No borres almacenamiento ni desinstales para intentar reparar el entorno de ChatGPT. Son sistemas distintos.

## 3. Revisa y publica el entorno de ChatGPT

La configuración guardada selecciona `Arlbode/google_sheet_bill`, con la revisión verificada de la rama **recuperacion-fuentes-1.3.0**, y usa `/workspace/google_sheet_bill`. No debe apuntar a `master`, que no existe en este repositorio.

En la configuración del entorno, revisa los cambios preparados y guárdalos; después vuelve a pulsar **Publicar**. Esta acción prepara el entorno de trabajo de ChatGPT; no publica otra APK ni cambia tu Google Sheet. Si falla, conserva el mensaje exacto para poder identificar la causa. Una comprobación local correcta no demuestra que el servicio de publicación haya terminado.

La selección del repositorio se mantiene. No necesitas quitarlo y añadirlo otra vez para aplicar esta reparación.

## Qué sigue pendiente

No está disponible la clave privada con la que se firmó la APK original. Sin ella no se puede generar una actualización que se instale encima de esa app conservando su identidad. El usuario eligió **conservar solo la app actual**: no se ha creado una app paralela ni una clave de reemplazo.

Los equivalentes CUP bajo las tarjetas USD y la nueva tarjeta de tasa todavía no están en la APK instalada. No se ha generado una APK nueva que incluya esa mejora. Tampoco se ha modificado la hoja real del usuario durante la recuperación.

## Comprobación técnica opcional

Estos pasos son para desarrollo; no necesitas ejecutarlos para usar la app:

```sh
bash recovered-source/scripts/verify_environment.sh
python3 recovered-source/scripts/create_backup.py --check Hogar_Playa_Recuperacion_Fuentes.zip
```

La comprobación incluye datos financieros ficticios y genera un paquete temporal sin firma solo para verificar su integridad. No es un instalador para entregar al usuario. GitHub Actions ejecuta las mismas comprobaciones en los nuevos commits de esta rama; consulta su resultado en la pestaña **Actions**.
