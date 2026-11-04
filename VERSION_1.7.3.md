# Hogar finanzas 1.7.3 · inicio de producción

Perfil → Inicio de producción permite el reinicio a cero expresamente solicitado después de usar datos de prueba. Actualizar instala la función y conserva los datos; la limpieza requiere iniciar el flujo, confirmar la migración del Sheet, activar modo avión y guardar un respaldo cifrado que la app vuelve a leer y verificar.

En el Sheet, dejarListoParaProduccion crea copia privada antes de borrar. Vacía registros, cambios, planes, auditorías móviles, lista compartida/recibos y saldos/reservas iniciales. Conserva fórmulas, formato, catálogo, proveedores, presupuesto, perfiles y configuración. Revoca conexiones antiguas para impedir la reintroducción de sus colas. Otra ejecución después de terminar conserva los registros nuevos; un fallo parcial se puede reanudar con el mismo respaldo.

En cada teléfono, después del script: modo avión → Perfil → Inicio de producción → Crear copia cifrada y reiniciar. Conserva perfiles/PIN, fotos, productos/ofertas e imágenes locales y configuración de API; retira movimientos, carritos, planes, sesiones de compra, listas y cachés financieros. Cierra la app y pide el PIN al abrir. Reconecta el mismo Sheet con un código nuevo para la nueva identidad de dispositivo. No restaures una copia de pruebas sobre producción.

La función del script se ejecuta desde el editor del mismo proyecto de Apps Script; no requiere iniciarHogar ni cambiar /exec. La [plantilla](Migracion_Produccion_PLANTILLA.gs) necesita su destino explícito. La copia completa preparada para la hoja concreta del usuario se entrega por un archivo privado; no se publica ese destino ni sus datos.

[APK firmada](Hogar_finanzas_1.7.3.apk): instalar encima sin desinstalar. Versión1.7.3/código31, paquete y certificado originales. El estado real del Sheet no ha sido limpiado ni verificado desde este entorno, que no tiene sesión Google. Publicar los archivos no ejecuta la migración ni crea la copia en Drive.

Validación: cinco pruebas del script aislado y las mismas cinco sobre el script completo;33 comprobaciones del modelo de limpieza y67 del ayudante Java real con dobles de Android/SQLite. Cubren copia antes de borrado, fórmulas/identidades conservadas, cambios posteriores al respaldo, fallo sin copia, revocación con el autorizador original, rollback y reanudación sin borrar dos veces. Navegador: flujo de confirmación/offline, marca final, reconexión, catálogo, fotos, espaguetis y guía. No se probó el selector o borrado real en teléfono/emulador ni la cuenta Google privada.

Todas las clases nativas anteriores excepto HouseholdFeatures/HouseholdDocuments se comparan idénticas a1.7.2. Los métodos existentes snapshot/validate/restore de documentos se conservan. Firma, paquete, alineación, assets e integridad verificados. La clave y las copias privadas permanecen fuera del repositorio.

[Validación](Validacion_Hogar_finanzas_1.7.3.json) · [Fuentes y pruebas](https://github.com/Arlbode/google_sheet_bill/tree/reinicio-produccion-hogar-1.7.3/development/1.7.3).
