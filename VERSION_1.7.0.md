# Hogar finanzas 1.7.0 — foto de perfil

Perfil → Agregar foto de perfil abre la galería mediante el selector de Android. Tras elegirla puedes Cambiar foto de perfil o Quitar foto. La foto circular aparece en el avatar superior, en Perfil y en la selección de personas. Cancelar mantiene la anterior; sin foto se muestra la inicial.

Cada foto se guarda en este teléfono, vinculada al identificador del perfil, y forma parte de la copia cifrada .hgf. No se envía a Apps Script. Cada persona modifica su propia foto después de entrar con el PIN; se vuelve a comprobar sesión, día de La Habana y versión del PIN al guardar. La foto no sustituye ni modifica la autenticación.

Se decodifica con límites de tamaño, se aplica orientación EXIF y recorte central a 256×256 y se guarda JPEG sin metadatos de origen. Las fotos inválidas o demasiado grandes no sustituyen la anterior. No se añade acceso general al almacenamiento ni permiso de cámara.

Instala desde Perfil → Actualizaciones → Buscar actualizaciones, encima de la versión actual sin desinstalar ni borrar datos. No requiere cambios de Apps Script. Misma identidad y firma, versión 1.7.0/código 28.

Validación: paquete, certificado, alineación, recursos compilados e integridad verificados. Se comparan todas las clases originales salvo las extensiones HouseholdFeatures/HouseholdDocuments; finanzas, autenticación, cesta, BackupCipher, TLS e instalador se conservan. Los métodos existentes de respaldo/recuperación de HouseholdDocuments permanecen idénticos en su fuente. Pruebas JVM: 24 comprobaciones de aislamiento de perfiles, quitar, formatos/límites, lector de fotos acotado, sesión/día/PIN y fotos en datos cifrados de respaldo. Navegador con datos ficticios: selección/cancelación simuladas, avatares circulares en las tres ubicaciones, quitar solo el perfil actual, selección de personas y datos financieros intactos; flujos anteriores y guía conservados. No se probó el selector real ni Bitmap en el teléfono del usuario desde esta sesión, ni se accedió a su Sheet privado.

Fuentes: https://github.com/Arlbode/google_sheet_bill/tree/foto-perfil-hogar-1.7.0
