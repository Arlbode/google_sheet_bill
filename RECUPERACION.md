# Estado de recuperación del proyecto

La APK 1.3.0 y los instaladores originales siguen intactos. La rama `descargas-android-1.1.0` conserva su commit `64ac245f1797510da5a8a4a6303c927bd1ef2825`.

La rama de recuperación añade `recovered-source/` y un archivo descargable `Hogar_Playa_Recuperacion_Fuentes.zip`. Se han reconstruido los assets, el backend y los datos desde las copias publicadas. La parte nativa original se mantiene en el DEX de la APK; el Java decompilado se conserva solo como referencia y no se utiliza para generar un instalador.

La validación comprueba integridad exacta de assets y backend, recuperación de datos, reempaquetado sin modificar código nativo ni recursos, conservación de la APK original, rechazo de una firma ausente, cálculo financiero en LibreOffice y respaldo reproducible con detección de alteraciones. El aporte al fondo de gasolina de 10 USD y gasto de 4 dejan 6 de reserva y 346 USD de efectivo desde un ingreso de 350, sin cargar el gasto otra vez a consumo.

La firma privada original sigue ausente. Por eso no hay todavía una actualización firmada que pueda instalarse encima de la app existente. No se ha cambiado la aplicación instalada, la hoja de Google ni sus datos.

El usuario eligió conservar solo la app actual. No se ha creado una aplicación paralela ni una firma nueva. La mejora de equivalentes CUP y tarjeta de tasa sigue pendiente en la APK instalada.

Conserva una copia del ZIP de recuperación en tu computadora. No contiene claves privadas ni movimientos financieros del usuario. [LEEME_PRIMERO.md](LEEME_PRIMERO.md) explica qué hacer con el respaldo y con la configuración del entorno. La publicación del entorno la realiza el usuario desde la interfaz de ChatGPT y debe verificarse allí; las comprobaciones locales no prueban esa publicación.

El ayudante `create_backup.py` reconstruye el ZIP con un manifiesto SHA-256 y excluye compilaciones, cachés y archivos de firma. `verify_environment.sh` ejecuta las comprobaciones sin usar la hoja real. El workflow de GitHub Actions está preparado para repetirlas en los commits de la rama de recuperación; su ejecución remota depende del servicio de Actions y sus permisos.
