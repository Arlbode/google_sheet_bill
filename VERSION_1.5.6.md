# Hogar finanzas 1.5.6 · API oficial directa de elTOQUE

La referencia USD/CUP se consulta directamente desde el teléfono a `https://tasas.eltoque.com/v1/trmi`, con la clave aprobada por el proveedor. La versión conserva paquete `cu.hogarplaya.recuperada`, certificado, perfiles, sesión, conexión, movimientos, fondos, ahorro, Plan CUP, compras y guía visual. versionCode 16.

## Activación

1. Sustituye Código.gs por **Script_Completo_Hogar_1.5.6.txt** y guarda.
2. Ejecuta **activarApiOficialDesdeAndroid**. Selecciona la recepción desde Android y actualiza dos explicaciones conocidas de la guía; conserva tasa, saldos, movimientos, reservas y B26.
3. Actualiza **la misma implementación /exec**, eligiendo Nueva versión. No vuelvas a vincular el teléfono. Mantén B26 = Sí para recibir referencias.
4. Instala la APK sobre la actual, sin desinstalar. En **Perfil → API oficial de elTOQUE**, pega la clave aprobada y pulsa Guardar. Se configura una vez en cada teléfono.
5. Pulsa ↻ en Inicio y sincroniza. El Sheet recibe solo la referencia y sus metadatos, sin la clave API.

Instrucciones ampliadas en [API_ELTOQUE.md](API_ELTOQUE.md).

## Guía de sugerencias de compra

La guía visual pasa a **13 temas y 68 pasos**. El capítulo «Sugerencias de compra» tiene acceso directo desde la sección «Sugerencias para reponer» y desde el capítulo de Modo compra.

Siete pasos muestran cómo las compras confirmadas en tres fechas distintas permiten calcular el intervalo habitual, cuándo aparece la tarjeta dentro de Mi lista de compra, cómo añadir el artículo y cómo afecta al total previsto. El ejemplo animado usa compras el 3, 10 y 17 de octubre y muestra el aviso al simular el día 24. La tarjeta reproduce la frecuencia, última compra, número de fechas y botón del recurso real.

El ejemplo de planificación compara un tope de 3.000 CUP, 800 ya comprados y una lista de 1.000: añadir una reposición de 400 aumenta la lista a 1.400 y deja 800 previstos. Añadir a la lista no crea un gasto. Se explican los productos ya listados, las compras mensuales, la falta de historial, la revisión de existencias y cómo la nueva compra mejora la próxima estimación.

Los datos son ficticios y las pruebas de la guía solo guardan posición de lectura. Los temas existentes conservan sus identificadores y progreso. Esta ampliación no modifica el algoritmo de estadísticas ni añade notificaciones del sistema: enseña la tarjeta que ya aparece dentro de Modo compra.

## Consulta y privacidad de la clave

La clave no está incrustada en la APK pública. El diálogo nativo la guarda con AES-GCM y Android Keystore, en una preferencia y alias separados de la conexión al Sheet. No se devuelve al WebView ni aparece en los respaldos de movimientos. Puede reemplazarse o retirarse desde Perfil.

La cabecera Bearer se envía solo al destino oficial HTTPS, sin redirecciones. Se elimina la excepción HTTP y el servicio del proveedor anterior. La última referencia anterior se conserva como dato guardado, sin atribuirla a una consulta oficial nueva. Se cancela el trabajo retirado y solo se encolan referencias oficiales nuevas.

Se valida exclusivamente tasas.USD como número finito positivo. Una respuesta válida permite tanto subidas como bajadas. Se distinguen la fecha real de consulta y la hora date/hour/minutes/seconds de la respuesta, cuya zona horaria no está documentada. La API calcula su referencia sobre las últimas 24 horas al responder; no se la presenta como una publicación diaria con hora inventada.

Sin conexión, con clave rechazada, error de servicio, respuesta inválida, fecha anterior o límite HTTP 429, se conserva el dato guardado y se explica el fallo. Hay un límite de una consulta por minuto y espera tras el límite del proveedor. Las referencias no crean ingresos/gastos, no convierten monedas y no modifican tasas históricas.

El receptor del script mantiene autenticación por hogar/dispositivo/perfil, validación de fuente y fechas, recibos idempotentes y rechazo de una referencia anterior. La recepción modifica únicamente B7/B11 y el historial de actualizaciones. Se mantiene la lógica financiera de la 1.5.5.

## Validación completada

- 21 pruebas Python, incluidas validación Java del endpoint, clave, importe y fechas, y ausencia del proveedor retirado en fuentes activas.
- 35 casos JavaScript, incluido el receptor oficial y la activación sin cambios de tasa, B26 ni datos financieros.
- Nueve escenarios de navegador, incluidos configuración desde Perfil, consulta simulada 860→855, fallo 401, guía completa, tarjeta de sugerencias real comparada con la ilustrada, planificación sin gasto al añadir, fondos, compras, teclado, sesión y actualizaciones.
- Android 10 real en emulador: clave ficticia cifrada y recuperada, migración de referencia, destino y cabecera HTTPS, respuesta USD, separación de horas, errores 401/403/302/503/429, espera y límite, respuestas inválidas o excesivas, cola y recibo. Trece solicitudes controladas; movimientos y snapshot financiero conservados.
- Firma fijada y métodos originales preservados salvo la entrada autorizada del puente. No quedan referencias al proveedor retirado en el DEX ni en los recursos activos de la APK.

El entorno restringe el dominio oficial: **no se ha usado ni validado la clave real contra el proveedor desde aquí**, ni se confirma una cotización vigente. La prueba nativa emplea una clave ficticia y HTTPS controlado. No se modificó el Sheet privado ni se probó en un teléfono físico.

La APK y el manifiesto se distribuyen juntos en el canal actualizaciones-android con autorización del usuario. El script completo 1.5.6 está disponible en esta misma rama.
