# Hogar finanzas: continuidad y firma

El nombre visible actual es **Hogar finanzas**, antes Hogar Playa Nueva. El cambio de nombre no cambia la identidad Android ni su firma.

- Mantén la identidad actual `cu.hogarplaya.recuperada` y el certificado fijado en `recovered-source/signing/new-certificate.sha256`. El usuario autorizó esta nueva app y retirará la antigua; las actualizaciones futuras son sobre la nueva.
- Lee `RESPALDO_Y_RECUPERACION_FIRMA.md` antes de compilar o recuperar una firma. El usuario pidió que el agente se encargue del procedimiento en sesiones futuras.
- Usa el checkout existente. Conserva cambios locales; no crees worktrees sin petición explícita.
- Nunca regeneres la firma de la app nueva si falta: restaura el respaldo y comprueba su certificado. Nunca publiques el PKCS12, contraseñas, ZIP privado ni archivo de acceso privado.
- El respaldo cifrado puede guardarse en el repositorio solo si está protegido por el código público generado en la computadora del usuario. Nunca añadas la clave privada de recuperación al repositorio ni a logs.
- El usuario ya proporcionó su código público: el respaldo preparado es `Firma_Hogar_Playa_Cifrada.json`, distribuido también en `Respaldo_Firma_Cifrado.zip`. Para recuperarlo se necesita el archivo privado que el usuario conserva. No pedir otro código ni generar otro acceso para abrir esta copia.
- No compiles el Java decompilado de referencia. Desde 1.5.0 se compila únicamente `android/native-src/`, escrito a mano; `compile-stubs/` no se empaqueta. `build_native.py` conserva todos los métodos originales salvo la entrada `MainActivity$Bridge.call`, donde incorpora una delegación. Verifica esa conservación después de ensamblar. Conserva `Hogar_Playa.apk` como base verificada.
- El cierre archiva la clave de día de la sesión en SQLite, conservando su UUID, líneas y gastos. El tope es por Modo compra, no uno global por día. No borres operaciones para reiniciar un tope ni traslades líneas ya compradas.
- La ubicación es optativa y puntual al confirmar, sin permiso de segundo plano. Guarda igualmente el gasto cuando no hay permiso o posición. Conserva la ubicación al corregir un gasto rechazado.
- El 403 de elTOQUE fue confirmado en los registros del usuario. No confundirlo con un fallo de formato ni intentar evadirlo. La API oficial requiere acceso propio; el usuario aún no tiene token. Se configura como `ELTOQUE_API_TOKEN` en Propiedades del script, nunca en el chat, APK o repositorio. Lee `API_ELTOQUE.md`.
- Para validar usa `HOGAR_PYTHON=/workspace/google_sheet_bill/.venv/bin/python bash recovered-source/scripts/verify_environment.sh`. La prueba de navegador adicional es `python3 recovered-source/tests/browser_smoke.py`.
- El panel de configuración de ChatGPT tuvo un conflicto de estado. No confundas trabajar en la sesión activa con haber resuelto la publicación ni cambies el borrador para intentar desbloquearlo sin un diagnóstico nuevo.

- Desde 1.5.1 la cesta usa cartCheckoutLocated: conserva la validación financiera original y anota ubicación dentro de una transacción con recibo de confirmación idempotente. No sustituyas una confirmación de cesta por save ni registres un gasto al añadir un artículo libre.
- La navegación interna del WebView permanece cerrada: mapas se abren mediante purchaseMap, con geo y alternativa HTTPS fija. Nunca abras esquemas o URL arbitrarias recibidas del frontend.
- Prueba de compra y teclado: `python3 recovered-source/tests/browser_purchase_ux.py`. El escenario Android purchase prueba cesta, lugar, GPS, reintentos, tope e intent de mapa; keyboard prueba resultados visibles con teclado real.

- Desde 1.5.2, DailySession mantiene una autenticación diaria por calendario America/Havana en preferencias privadas, sin PIN. Conserva sesión al salir de la app o ir al fondo; lock la elimina. Cambiar de día o restablecer el PIN la invalida. No restaures el bloqueo a cinco minutos ni uses lock para Atrás.
- DailySession invoca el despachador base verificado en la misma cola de trabajo: no se modifican los métodos originales fuera de Bridge.call. El verificador del PIN, intentos y recuperación sigue siendo el original.
- Investigación de tasa en INVESTIGACION_TASA.md: TASALO documenta lectura pública sin token; su disponibilidad real no fue verificada. El usuario ordenó APK → TASALO → cola local → sincronización al Sheet. TasaloReference implementa esa ruta de forma condicional y valida sources_updated_at.eltoque; nunca usar updated_at de respuesta ni valores de ejemplos.

- TasaloSyncService usa un JobScheduler separado (5403) para la cola de referencias. El token Vault se envía solo a ApiClient.post (HTTPS Apps Script); el GET público no lleva credenciales. El HTTP se permite únicamente para tasalo.duckdns.org mediante network_security_config. No habilitar cleartext global.
- El script actual sí debe volver a desplegarse para referenceSync. IniciarHogar activa HOGAR_TASA_DESDE_APK=1; el servidor recibe datos y deja de consultar otra fuente por su cuenta. B26 controla la recepción. Una referencia solo modifica B7/B11 y Actualizaciones, nunca tablas financieras.
