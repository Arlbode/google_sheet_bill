# Hogar finanzas 1.6.9 — eliminar cesta compartida

Dentro de Cesta compartida aparece Eliminar cesta compartida. Al tocarlo se confirma la retirada de todos los artículos de la instantánea actual, incluidos comprados y pendientes, independientemente del filtro o búsqueda. Cancelar conserva la cesta. Confirmar la vacía, cierra la vista y envía los cambios automáticamente al hogar. Sin red guarda la retirada para enviarla después.

El carrito personal y los gastos registrados se conservan. Se desactiva el vínculo automático de las líneas retiradas para que editar el carrito personal no haga reaparecer la cesta eliminada. Se puede volver a compartir un carrito explícitamente. Los artículos que otros teléfonos añadan después de la instantánea confirmada son cambios nuevos; no se borran silenciosamente. Los conflictos remotos posteriores requieren revisión.

Instala desde Perfil → Actualizaciones → Buscar actualizaciones, encima de la versión actual sin desinstalar ni borrar datos. No requiere cambios adicionales en Apps Script sobre 1.6.0 ya desplegado. Guía actualizada.

Versión 1.6.9/código 27, identidad y firma originales. Retirada lógica compatible con el protocolo existente; conserva recibos, UUID e idempotencia. La cola de envío mantiene lotes de 20 y máximo 100 en curso, con intenciones pendientes para el resto. No elimina operaciones financieras ni reinicia límites del servidor. El constructor compara todas las clases originales excepto las extensiones propias, conserva recursos compilados, finanzas, autenticación, documentos, TLS e instalador. Firma y alineación verificadas.

Pruebas JVM: 30 comprobaciones de borrado completo, estados, cola inmutable, reintentos, lotes, vínculos y compartir de nuevo; 31 comprobaciones de cesta automática conservadas. Navegador con datos ficticios: confirmar/cancelar, búsqueda activa, cierre, cesta vacía al abrir, offline/reconexión y ningún cambio financiero. Regresión de cesta compacta y guía de 20 temas, 31 pasos de herramientas y siete demos. No se accedió al Sheet privado ni al teléfono físico del usuario.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/eliminar-cesta-hogar-1.6.9
