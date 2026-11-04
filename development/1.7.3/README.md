# Hogar finanzas 1.7.3 · inicio de producción autorizado

Implementa el reinicio a cero solicitado expresamente por el dueño, después de usar datos de prueba. Es una operación de una sola vez con copia previa; no modifica el cierre ordinario de Modo compra ni los despachadores financieros originales. No ejecuta una limpieza al instalar o actualizar la APK.

## Sheet

`Migracion_Produccion.gs` se añade al mismo proyecto Apps Script que atiende la APK. La copia descargable completa preparada para el usuario conserva byte por byte su script aportado y añade `dejarListoParaProduccion`, con el ID del destino concreto. Ese archivo específico no se publica en Git. La plantilla pública usa `__SHEET_ID__` y rechaza otro destino o un proyecto no configurado para esa hoja/hogar.

Bajo el mismo ScriptLock de la API, comprueba cabeceras y fórmulas, crea una carpeta privada en Drive con copia de la hoja y un JSON de las conexiones que retira. Si la copia o la validación fallan, no borra. Mantiene el ID del hogar, perfiles, catálogo, proveedores, presupuesto, permisos, formato, fórmulas y configuración de tasa. El respaldo no se comparte ni se imprime su contenido.

Retira códigos de emparejamiento y todas las credenciales `MOVIL_DEVICE_*`, marca los dispositivos anteriores REVOCADO y vacía entradas de Registro, Cambios, Plan CUP, auditoría móvil, cambios de planes, lista compartida y sus recibos. Pone a cero USD/CUP iniciales, transferencia inicial, ahorro inicial y fondos iniciales. Mantiene fórmulas, validación y formatos; conserva los ID de filas vacías de la plantilla de planes. La fecha inicial pasa al día de ejecución en La Habana; el mes consultado vuelve a la fórmula dinámica actual.

Verifica entradas vacías, reservas iniciales y saldos totales a cero. Guarda una marca DONE. Otra ejecución devuelve el respaldo anterior y conserva los registros nuevos. Un fallo parcial deja las conexiones antiguas bloqueadas y permite reanudar con la misma copia. No utiliza iniciarHogar ni cambia los endpoints de la API; la nueva función se ejecuta en el editor.

No se han vaciado datos en Google desde este entorno: no hay sesión ni credenciales Google configuradas. La publicación de un script no equivale a ejecutarlo sobre la hoja privada.

## Cada teléfono

Actualizar a1.7.3; ejecutar primero el script sobre el Sheet; activar modo avión. En Perfil → Inicio de producción, confirmar que ya se hizo la migración. El puente exige perfil autenticado, desconexión y que ese teléfono todavía no completó el reinicio. Crea un .hgf con el selector y cifrado existentes; vuelve a leer los bytes escritos y comprueba que coinciden antes de limpiar. Cancelar, contraseña errónea, copia no verificable, conexión recuperada o cambios posteriores a la copia impiden el borrado.

Mientras se guarda la copia se bloquean otras acciones de escritura del puente. Un ejecutor separado espera el monitor del sincronizador financiero; WORK sigue libre mientras espera. La limpieza se ejecuta en WORK, vuelve a comprobar sesión, desconexión y huella del respaldo, escribe una marca persistente y retira token y endpoint antes de limpiar.

Una transacción vacía operaciones, carritos, cambios de planes, sesiones y líneas de compra y cachés anteriores. Conserva la tabla de perfiles/PIN, fotos de perfil y productos/ofertas locales con sus imágenes; elimina colas compartidas, conflictos, vínculos de carritos y snapshot financiero. No crea ingresos ni gastos. Cambia la identidad de conexión del teléfono por un UUID nuevo y elimina la sesión diaria; mantiene el hogar y recuerda la URL para conectar después. Los permisos y la configuración local de la API no se borran.

Se guarda un recibo de finalización en SQLite y se cierra el proceso, como en la recuperación existente. Al abrir se pide el PIN y se conecta con un código nuevo. Una interrupción reanuda la limpieza pendiente; si SQLite ya terminó, solo finaliza preferencias. El recibo impide repetirla incluso tras restaurar después un respaldo completo de producción. Cada teléfono se limpia por separado. No se debe restaurar una copia de pruebas sobre la producción.

## Construcción y pruebas

`python3 development/1.7.3/build.py`. Base firmada1.7.2 con SHA-256 fijado; produce1.7.3/código31. Mantiene paquete y certificado originales. La clave privada permanece fuera del repositorio. Leer AGENTS.md y recuperación de firma; nunca regenerarla.

Solo sustituye las extensiones propias HouseholdFeatures/HouseholdDocuments y añade ProductionReset/ProductionData. Compara todas las otras clases smali: almacenamiento, finanzas, autenticación diaria, sincronizador, TLS, instalador, fotos y modelos compartidos permanecen idénticos. Los métodos originales snapshot/validate/restore de documentos se comparan por fuente. Los stubs no se empaquetan. Se conservan recursos compilados, espaguetis y todos los assets no autorizados.

- `node development/1.7.3/tests/migration.test.cjs`: cinco pruebas del script real con servicios simulados: respaldo antes de borrar, fórmulas/perfiles/catálogo, idempotencia, fallo sin copia/destino incorrecto, fallo parcial/reanudación y rechazo de credenciales retiradas usando el autorizador original.
- `ProductionDataTest`:33 comprobaciones de vaciado, copia inmutable, PIN/fotos/productos conservados, cachés/colas retiradas y huella ante cambios.
- `python3 development/1.7.3/tests/run_reset_native.py`: ejecuta ProductionReset real con dobles de Android/SQLite;67 comprobaciones de guardia, espera fuera de WORK, operación posterior al respaldo, rollback, interrupción al guardar preferencias y recuperación sin borrar dos veces.
- Navegador de producción: pasos, confirmación, rechazo online, una solicitud por pulsación, marca de terminado, protección de registros nuevos y URL de reconexión. Regresiones de catálogo, fotos, espaguetis, herramientas y guía de20 temas/31 pasos/7 demos.

Las pruebas usan datos ficticios. No se ejecutó la limpieza ni el selector real en un teléfono físico o emulador, ni se verificó Drive/Apps Script con la cuenta del usuario. La copia Excel proporcionada se utilizó para contrastar cabeceras y presencia de las fórmulas, sin modificarla ni publicarla.
