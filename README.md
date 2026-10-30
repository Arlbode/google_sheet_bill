## Actualización 1.5.7

[Descargar APK firmada 1.5.7](Hogar_finanzas_1.5.7.apk) · [Detalles y validación](VERSION_1.5.7.md)

Zanahoria con imagen y compatibilidad de imagen para Ají pimiento. Instala encima de la versión existente, sin desinstalar. El manifiesto del canal apunta a esta versión.

# Hogar finanzas · actualizaciones Android

Versión actual: **1.5.6** (versionCode 16).

[Descargar APK 1.5.6](Hogar_finanzas_1.5.6.apk) · [ZIP de instalación con script e instrucciones](Hogar_finanzas_1.5.6_Instalacion.zip)

## Activar la API oficial de elTOQUE

1. Sustituye Código.gs por [el script completo 1.5.6](Script_Completo_Hogar_1.5.6.txt), guarda y ejecuta **activarApiOficialDesdeAndroid**.
2. Actualiza **la misma implementación /exec** eligiendo Nueva versión. Conserva la URL y el acceso. Mantén B26 = Sí para recibir referencias.
3. En la app abre **Perfil → Actualizaciones → Buscar actualizaciones**, descarga e instala. También puedes descargar la APK de esta rama e instalarla encima de la actual, sin desinstalar.
4. En **Perfil → API oficial de elTOQUE**, pega tu clave aprobada y guarda. Se configura una vez en cada teléfono. Después pulsa ↻ en Inicio y sincroniza.

La clave se guarda cifrada en el teléfono. No está incrustada en la APK y no se entrega al Sheet. Se mantienen identidad, firma, datos y vinculación. Android solicita confirmar la instalación; si pide permitir instalaciones desde Hogar finanzas, concede el permiso, vuelve y pulsa Instalar otra vez.

[Instrucciones de la API oficial](API_ELTOQUE.md) · [ZIP solo del script e instrucciones](Hogar_finanzas_1.5.6_Script_Completo.zip)

## Guía visual ampliada

13 temas y 68 pasos, con un capítulo de siete pasos sobre las sugerencias de reposición. Enseña la base estadística, cómo aparece la tarjeta y cómo añadir una sugerencia ajusta el total previsto y el tope. La animación es una demostración dentro del tutorial; la sugerencia real aparece como tarjeta dentro de Modo compra. Los ejemplos usan datos ficticios y no registran gastos.

[Detalles y validación](VERSION_1.5.6.md) · [Funcionamiento del canal](ACTUALIZACIONES_ANDROID.md)

La app verifica versión, tamaño, SHA-256, identidad y firma antes de abrir el instalador. Las versiones anteriores permanecen disponibles en esta rama. Las claves privadas y los datos del hogar no se distribuyen en el canal.
