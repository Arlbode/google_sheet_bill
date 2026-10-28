# Hogar finanzas · Android 1.4.1

La versión 1.4.1 cambia el nombre visible a **Hogar finanzas**. Se instala encima de **Hogar Playa Nueva 1.4.0**, manteniendo perfiles, movimientos y conexión porque conserva el identificador y la firma.

- [Descargar APK firmada](Hogar_finanzas_1.4.1.apk).
- [Script completo del Sheet](Script_Completo_Hogar_Tasa_CUP.txt): un único archivo para Código.gs.
- [Instalación y conexión paso a paso](INSTALAR_NUEVA_APK.md).
- [Respaldo público de fuentes](Hogar_Playa_Recuperacion_Fuentes.zip), sin claves privadas ni datos reales.
- [Respaldo cifrado de la firma](Respaldo_Firma_Cifrado.zip), con herramienta e instrucciones. Se abre con el archivo privado que ya generaste y conservas en tu computadora.

## Novedades

- Equivalentes CUP pequeños bajo las cantidades USD de Inicio, incluida la partida disponible para consumo.
- Tarjeta de referencia USD/CUP con fuente, fecha/hora, consulta automática y botón ↻.
- Una sola tasa para todos los equivalentes; los movimientos conservan su tasa histórica.
- Sin conexión o si falla la fuente, conserva y etiqueta la última referencia.
- Mantiene perfiles, PIN, cola sin conexión, Plan CUP, efectivo/transferencia, Modo compra, unidades, imágenes y fondos, usando el DEX original sin cambios.

## Instalación inicial y próximas actualizaciones

Esta app se muestra como **Hogar finanzas**, paquete `cu.hogarplaya.recuperada`. Continúa la firma de la versión 1.4.0; la app original 1.3.0 utilizaba otra firma y fue retirada por decisión del usuario. Para las siguientes actualizaciones se conserva la nueva firma, cuyo certificado público está fijado en el proyecto. La copia cifrada ya está preparada para el código público entregado por el usuario; se necesita su archivo privado local para recuperarla. La clave sin cifrar no está en GitHub.

Lee [INSTALAR_NUEVA_APK.md](INSTALAR_NUEVA_APK.md) antes de conectar la app. El servicio de publicación del entorno de ChatGPT no interviene en la instalación de esta APK. Esta entrega no afirma haber reparado el conflicto del panel de configuración.

Los antiguos instaladores y script 1.3.0 permanecen en el historial y en sus archivos originales. [RECUPERACION.md](RECUPERACION.md) explica la procedencia y límites del código recuperado.
