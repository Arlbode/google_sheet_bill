# Hogar finanzas: continuidad y firma

El nombre visible actual es **Hogar finanzas**, antes Hogar Playa Nueva. El cambio de nombre no cambia la identidad Android ni su firma.

- Mantén la identidad actual `cu.hogarplaya.recuperada` y el certificado fijado en `recovered-source/signing/new-certificate.sha256`. El usuario autorizó esta nueva app y retirará la antigua; las actualizaciones futuras son sobre la nueva.
- Lee `RESPALDO_Y_RECUPERACION_FIRMA.md` antes de compilar o recuperar una firma. El usuario pidió que el agente se encargue del procedimiento en sesiones futuras.
- Usa el checkout existente. Conserva cambios locales; no crees worktrees sin petición explícita.
- Nunca regeneres la firma de la app nueva si falta: restaura el respaldo y comprueba su certificado. Nunca publiques el PKCS12, contraseñas, ZIP privado ni archivo de acceso privado.
- El respaldo cifrado puede guardarse en el repositorio solo si está protegido por el código público generado en la computadora del usuario. Nunca añadas la clave privada de recuperación al repositorio ni a logs.
- El usuario ya proporcionó su código público: el respaldo preparado es `Firma_Hogar_Playa_Cifrada.json`, distribuido también en `Respaldo_Firma_Cifrado.zip`. Para recuperarlo se necesita el archivo privado que el usuario conserva. No pedir otro código ni generar otro acceso para abrir esta copia.
- No compiles el Java decompilado de referencia. `build_release.py` conserva exactamente el DEX original de `Hogar_Playa.apk`; conserva esa APK como base de compilación.
- Para validar usa `HOGAR_PYTHON=/workspace/google_sheet_bill/.venv/bin/python bash recovered-source/scripts/verify_environment.sh`. La prueba de navegador adicional es `python3 recovered-source/tests/browser_smoke.py`.
- El panel de configuración de ChatGPT tuvo un conflicto de estado. No confundas trabajar en la sesión activa con haber resuelto la publicación ni cambies el borrador para intentar desbloquearlo sin un diagnóstico nuevo.
