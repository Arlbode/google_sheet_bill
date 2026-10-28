# Hogar Playa Nueva · Android 1.4.0

La nueva app sustituye la anterior en fase de pruebas. Usa una identidad y una firma estables para sus futuras actualizaciones.

- [Descargar APK firmada](Hogar_Playa_Nueva_1.4.0.apk).
- [Script completo del Sheet](Script_Completo_Hogar_Tasa_CUP.txt): un único archivo para Código.gs.
- [Instalación y conexión paso a paso](INSTALAR_NUEVA_APK.md).
- [Respaldo público de fuentes](Hogar_Playa_Recuperacion_Fuentes.zip), sin claves privadas ni datos reales.
- [Herramienta local para conservar la nueva firma](Herramienta_Respaldo_Firma.zip). Comparte solo el código público que genera; el archivo privado se queda en tu computadora.

## Novedades

- Equivalentes CUP pequeños bajo las cantidades USD de Inicio, incluida la partida disponible para consumo.
- Tarjeta de referencia USD/CUP con fuente, fecha/hora, consulta automática y botón ↻.
- Una sola tasa para todos los equivalentes; los movimientos conservan su tasa histórica.
- Sin conexión o si falla la fuente, conserva y etiqueta la última referencia.
- Mantiene perfiles, PIN, cola sin conexión, Plan CUP, efectivo/transferencia, Modo compra, unidades, imágenes y fondos, usando el DEX original sin cambios.

## Instalación inicial y próximas actualizaciones

Esta app se instala como **Hogar Playa Nueva**, paquete `cu.hogarplaya.recuperada`. No usa la firma perdida de la app anterior. Comprueba la conexión al Sheet y después retira la antigua, conforme a la decisión del usuario. Para las siguientes actualizaciones se conserva la nueva firma, cuyo certificado público está fijado en el proyecto. La descarga directa del respaldo privado falló; la herramienta local permite preparar una entrega cifrada. La clave sin cifrar no está en GitHub.

Lee [INSTALAR_NUEVA_APK.md](INSTALAR_NUEVA_APK.md) antes de conectar la app. El servicio de publicación del entorno de ChatGPT no interviene en la instalación de esta APK. Esta entrega no afirma haber reparado el conflicto del panel de configuración.

Los antiguos instaladores y script 1.3.0 permanecen en el historial y en sus archivos originales. [RECUPERACION.md](RECUPERACION.md) explica la procedencia y límites del código recuperado.
