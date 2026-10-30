# Hogar finanzas 1.6.0 — candidata firmada

Instala `Hogar_finanzas_1.6.0_CANDIDATA.apk` encima de la 1.5.7, sin desinstalar ni borrar datos. En Perfil → Mejoras del hogar encontrarás las ocho herramientas solicitadas. Conserva paquete y certificado.

Para compartir listas entre teléfonos y recibir historial remoto con artículos, sustituye Código.gs por `Script_Completo_Hogar_1.6.0.txt` y publica una nueva versión de la misma implementación, conservando su URL /exec. Desde una hoja funcional 1.5.7 no hace falta reinicializarla. Las nuevas pestañas auxiliares se crean al sincronizar. Publicar el archivo en GitHub no despliega el script del Sheet.

La lista compartida conserva quién añadió y quién marcó comprado cada artículo, y permite resolver conflictos. No registra gastos automáticamente. Catálogo, fotos y ofertas son locales a cada teléfono y se incluyen en el respaldo cifrado. La sincronización automática de la lista funciona mientras la app está abierta.

Antes de cambiar de teléfono crea y comprueba el respaldo .hgf. Guarda su contraseña aparte. Recupera solo sobre una instalación vacía; se conserva la identidad de conexión, por lo que después de migrar debes dejar de usar el teléfono anterior. Las conexiones revocadas necesitan renovarse y la clave local de elTOQUE se configura de nuevo. El JSON antiguo es una exportación de movimientos, no el nuevo respaldo completo.

La previsión considera gastos de consumo y compras pendientes cotizadas, conservando tasas guardadas y excluyendo fondos. Usa el mayor entre proyección por ritmo y consumo más compras pendientes, sin sumar ambos escenarios. No modifica saldos.

Validación: siete pruebas de cálculo/modelo, escenarios de backend con autenticación/reintentos/conflictos, ocho pantallas y compra desde catálogo en Chromium, cifrado/manipulación en JVM, actualización real 1.5.7 → 1.6.0 conservando perfil/UUID/importe y 23 comprobaciones nativas en Android 10. Incluyen recuperación real, fotos, autenticación y cola persistente. El DEX nativo final es idéntico al probado. No se desplegó ni sincronizó contra el Sheet privado.

Esta candidata y su manifiesto de revisión no activan el canal estable: `actualizacion.json` permanece en 1.5.7. Fuentes y límites: `development/1.6.0/README.md`. Evidencia: `Validacion_Hogar_finanzas_1.6.0.json`.
