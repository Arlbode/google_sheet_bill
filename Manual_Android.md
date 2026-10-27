# Hogar Playa 1.3.0 · instalación y uso en Android

La APK funciona en **Android 8 o posterior**. Cada persona crea su nombre y PIN al abrir la app. Puedes usar dos perfiles en un teléfono o instalarla en dos teléfonos conectados a la misma hoja.

## Actualizar desde una versión anterior

1. Conserva la APK instalada. En tu Sheet, sustituye todo Código.gs por **Script_Completo_Hogar_Android.txt**, guarda y ejecuta **iniciarHogar**. No pegues fragmentos.
2. Recarga la hoja. Revisa el desglose inicial: **Cuentas B7** es el CUP inicial total; **B24** indica qué parte de ese mismo total estaba en transferencia. No sumes B24 otra vez a B7.
3. Actualiza la implementación existente: **Implementar → Gestionar implementaciones → lápiz → Nueva versión → Implementar**. Conserva tu URL /exec y sus permisos.
4. Instala **Hogar_Playa.apk 1.3.0 encima de la anterior**, en todos los teléfonos que vayan a registrar transferencias. No desinstales ni borres datos. La actualización conserva perfiles, PIN, cestas, movimientos y credenciales.
5. Abre la APK y sincroniza. Si ya estaba vinculada y conservaste la URL, no necesitas generar otro código. La nueva APK exige el servidor actualizado para evitar que un servidor antiguo ignore el formato CUP.

La instalación incluye Planes app y Plan CUP L:Q para gestionar y auditar planes desde Android. Conserva Registro U/V, Cambios R, Cuentas B24:B26, Lista compras N y dos indicadores al final del Tablero. Los registros antiguos sin formato se consideran **Efectivo**; puedes corregir su formato en Registro U o Cambios R sin cambiar el importe ni anotar otro ingreso.

## 1. Preparar tu hoja una vez

1. Descarga `Script_Completo_Hogar_Android.txt` y abre tu Google Sheet actual.
2. En la hoja, abre **Extensiones → Apps Script**.
3. En `Código.gs`, sustituye todo el código por el contenido completo del archivo. No añadas fragmentos al final del script anterior. Si creaste otros archivos `.gs` con copias de las mismas funciones, conserva un solo script completo; no borres tus datos de la hoja.
4. Guarda y elige **iniciarHogar** en el selector de funciones. Pulsa **Ejecutar** y autoriza con tu cuenta de Google. La primera ejecución puede tardar por las consultas de precios.
5. Vuelve a la hoja y recarga la página. Aparecerá **Hogar → Conectar app Android**. Las instrucciones también quedan en **Guía**, pasos 15 a 41.

La actualización conserva Registro, Cambios, presupuestos, ajustes y reservas. Añade Perfiles app, Dispositivos app y Auditoría app, además de columnas de trazabilidad. No introduce movimientos de ejemplo en tu hoja.

## 2. Publicar la conexión con la app

Esto se hace desde Apps Script con tu cuenta habitual. **No necesitas Google Workspace ni contratar un servidor.**

1. Arriba a la derecha, pulsa **Implementar → Nueva implementación**.
2. En el icono del engranaje, elige **Aplicación web**.
3. En **Ejecutar como**, elige **Yo**, tu cuenta propietaria de la hoja.
4. En **Quién tiene acceso**, elige **Cualquier persona**. La API exige además la credencial privada de cada teléfono; conocer la URL no permite leer ni modificar los gastos.
5. Pulsa **Implementar**, completa la autorización de tu propio proyecto y copia la **URL de la aplicación web** que termina en `/exec`. La URL de `docs.google.com/spreadsheets` no sirve aquí.

Si tu cuenta o la política de tu organización no permite “Cualquier persona”, esta modalidad de conexión no podrá funcionar con esa cuenta.

### Si aparece HTTP 401 o 403 al conectar

Google está rechazando el acceso a la URL antes de que la APK reciba los datos de la aplicación. Revisa la implementación; introducir otra vez el PIN del perfil no cambia esos permisos.

1. En tu Sheet abre **Extensiones → Apps Script → Implementar → Gestionar implementaciones**.
2. Selecciona la aplicación web y pulsa el **lápiz**. Comprueba **Ejecutar como: Yo** y **Quién tiene acceso: Cualquier persona**. Esta última opción debe permitir el acceso sin iniciar sesión en Google.
3. En **Versión**, elige **Nueva versión**, pulsa **Implementar** y completa cualquier autorización pendiente con tu cuenta propietaria.
4. Copia la URL terminada en `/exec` y ábrela en una **ventana de incógnito** del navegador. Debe mostrar un texto que contiene `"app":"Hogar Playa"` y `"apiVersion":1` sin pedir iniciar sesión. Esta prueba solo muestra el estado público del servicio, no los gastos.
5. Si esa prueba funciona, genera un código nuevo en **Hogar → Conectar app Android** y vuelve a **Perfil → Conectar Google Sheets** en la APK. Usa la misma URL que acabas de probar. Si el problema aparece tras una conexión que ya funcionaba y conservaste la URL, pulsa primero **Sincronizar**; renueva el código solo si la app solicita renovar la conexión.

Si la ventana de incógnito pide iniciar sesión o dice que no tienes acceso, el acceso público sigue sin estar configurado. Si muestra otro contenido, comprueba que implementaste el proyecto que contiene el script completo de Hogar y su versión actual. **No desinstales la APK ni borres sus datos para resolver el 401**: los movimientos pendientes se conservan y se enviarán al conectar correctamente.

## 3. Instalar y vincular el teléfono

1. Descarga **Hogar_Playa.apk** en el móvil, abre el archivo y sigue la instalación de Android. Si lo solicita, permite instalar desde la aplicación con la que abriste ese archivo.
2. Abre **Hogar Playa → Crear mi perfil**. Escribe tu nombre, un PIN de 4 a 6 cifras y su confirmación.
3. En el Sheet, abre **Hogar → Conectar app Android → Generar código**.
4. En la APK, abre **Perfil → Conectar Google Sheets**. Pega la URL `/exec`, el código y un nombre para ese teléfono.
5. Pulsa **Conectar mi hogar** con internet. El código dura 10 minutos, admite 5 intentos y se usa una sola vez.
6. Para el segundo teléfono, genera otro código. Para otra persona en el mismo teléfono, usa **Perfil → Crear otro perfil**. Cada movimiento conservará quién lo registró.

Puedes empezar a registrar antes de vincular el Sheet. Los movimientos quedarán pendientes en ese teléfono hasta completar la conexión.

## 4. La rutina del hogar

**Cobrar.** Inicio → Ingreso. Registra, por ejemplo, Salario, 350 USD y la fecha del cobro. La tasa de referencia al cobrar es opcional si recibiste USD; si recibiste CUP, introduce la tasa real para su equivalente en USD. El ingreso previsto del presupuesto no acredita efectivo por sí solo.

**Una cuenta CUP con dos formatos.** Al registrar un ingreso CUP elige **Efectivo** o **Transferencia**. Al pagar CUP, elige el formato realmente utilizado. El cambio USD→CUP sugiere Efectivo, pero puedes escoger Transferencia si así lo recibiste. Inicio muestra USD, CUP efectivo, CUP transferencia y el total CUP. Un ingreso bancario suma solo a transferencia; un pago en efectivo descuenta solo efectivo. Se conserva una sola cuenta CUP y el total equivale a la suma de los dos formatos. Ahorro y fondos reservan dinero dentro de esos saldos.

**Recibir otros ingresos en CUP.** En el Sheet usa **Hogar → Registrar ingreso · USD o CUP** y selecciona CUP; en la APK, **Registrar → Ingreso → Moneda CUP**. Lo mismo funciona desde **Registrar movimiento → Ingreso**. Al seleccionar CUP aparece el formato obligatorio **Efectivo / Transferencia**; al seleccionar USD se oculta. Se ha eliminado el acceso separado a otro ingreso CUP. Escribe el concepto, importe recibido y tasa para el equivalente del presupuesto. Se suman solo los CUP a su cuenta, sin vender ni restar USD. Ese dinero reduce la necesidad de cambiar dólares. No aumenta por sí solo las asignaciones de consumo.

**Planificar los pagos.** En el Sheet abre **Hogar → Planificar pagos en CUP**. Completa cada fila amarilla de **Plan CUP**: fecha, concepto, categoría, previsto CUP, imprescindible Sí/No y activo Sí/No. Los pagos son opcionales por defecto; marca **Imprescindible: Sí** para que justifiquen un cambio. No se suman gastos hasta registrar el pago real. También puedes añadir y gestionar estos pagos desde la APK: **Inicio o Perfil → Plan CUP y protección de USD**. Consulta el total previsto, pagado y pendiente; usa los filtros Pendientes, Pagados, Cancelados y Todos. **Añadir Plan CUP** y **Editar** permiten concepto, fecha, categoría, importe total, imprescindible, estado y notas. El importe es el total del compromiso, no solo el restante. Con pagos asociados se conserva la categoría y no se reduce el importe por debajo de lo pagado. **Cancelar plan** conserva los pagos y la trazabilidad, y deja de contar el pendiente como obligación futura; **Reactivar** vuelve a incluirlo. No se devuelve dinero al cancelar.

**Proteger los USD.** **Protección USD** queda activa con estos valores editables:

| Celda | Función | Valor inicial |
|---|---|---:|
| B7 | Días de pagos que cubriría un cambio | 15 |
| B8 | Antelación del aviso al primer pago sin cobertura CUP | 3 días |
| B9 | Reserva USD adicional a la mitad del ingreso previsto | 0 USD |
| B10 | CUP reservados que no se deben usar | 0 CUP |
| B11 | Mínimo USD protegido, calculado | 175 USD |

La regla suma los pagos imprescindibles pendientes dentro del horizonte, usa primero los CUP disponibles después de su reserva y calcula el déficit. Solo justifica cambiar cuando se acerca el primer pago sin cobertura. Limita los USD al déficit convertido con la **tasa real que introduzcas**, redondeado hacia arriba a centavos, y al dinero que supere la reserva USD. Si un plan queda incompleto, no autoriza cambios hasta revisarlo. Si los USD libres no alcanzan, lo señala sin tocar la reserva.

Ejemplo: pagos imprescindibles de 30.000 CUP y 5.000 CUP utilizables dejan una necesidad de 25.000 CUP. A 840, la estimación es 29,77 USD; si acuerdas 870, el máximo necesario pasa a 28,74 USD. No solicita vender 100 USD por costumbre. Si antes cobras 25.000 CUP de otro ingreso, la necesidad desaparece.

**Recibir el aviso.** En la APK abre **Perfil → Avisos de necesidad de CUP** y permite las notificaciones de Android. La app avisa con el déficit, cambio estimado permitido y referencia de tasa con fuente y fecha. Si la consulta es anterior o falla la fuente, muestra la última fecha o pide confirmar la tasa; no inventa una cotización actual. Las revisiones se hacen al abrir/sincronizar y con un trabajo periódico de 6 horas. Android puede aplazarlas por ahorro de batería, falta de red o detención forzada.

**Cambiar dinero.** Inicio → Cambiar USD muestra el límite del plan. Registra el cambio real permitido y la tasa acordada. Por ejemplo, 50 USD a 870 suman 43.500 CUP y restan 50 USD, siempre que la necesidad planificada y la reserva lo permitan. Cada cambio conserva su tasa y referencia del cobro. Los formularios de Hogar y la API bloquean nuevos cambios sin necesidad planificada o que superen el máximo. El cambio no crea otro ingreso ni consume presupuesto, y la aplicación nunca cambia dinero automáticamente.

**Registrar una compra.** Comprar → busca el producto → `+`. Confirma presentación, cantidad, moneda, precio real y tasa histórica si pagaste en CUP. Abre **Mi cesta → Guardar compra como gasto** cuando quieras registrar la compra realizada. Añadir artículos a la cesta no descuenta dinero. Guardarla crea gastos por categoría, moneda y tasa, con el detalle de los artículos. Si corresponde a un pago planificado, elige ese plan al añadir los artículos. Aquí no se envían pedidos a comercios.

**Anotar otro gasto.** Inicio → Gasto. Selecciona categoría e importe. Sirve también para transporte, servicios, ocio, veterinario y otros pagos sin artículos del catálogo. Para un gasto previsto en CUP, selecciona su plan de la misma categoría o entra en Plan CUP y protección USD → Registrar pago. Así el importe reduce tanto el saldo CUP como el pendiente del plan. Si lo registras manualmente en el Sheet, copia el Plan ID a Registro T.

**Apartar ahorro y fondos.** En Registrar movimiento, elige Aporte ahorro o Aporte fondo después de separar realmente el dinero. La propuesta es ahorrar al menos 175 USD de un ingreso de 350. Los aportes reservan dinero dentro de tus saldos; no lo vuelven a sumar como ingreso ni lo descuentan como un pago. Una consulta veterinaria pagada desde su fondo se registra como Gasto / Fondo veterinario.

**Consultar el disponible actualizado.** Inicio muestra **Disponible para consumo**:

> Asignaciones con destino Consumo en Presupuesto − gastos del mes de esas categorías.

Con el plan inicial: 350 USD − 175 de ahorro − 42 de fondos = **133 USD asignados a consumo**. Una compra de 20 USD deja **113 USD**. Los gastos en CUP usan la tasa guardada en cada movimiento. La tarjeta incluye los pendientes de este teléfono en una vista provisional y muestra un valor negativo cuando te excedes. Tócala para ver asignado, gastado y disponible por categoría.

Los aportes a ahorro/fondos, cambios de moneda y pagos desde fondos no reducen esta partida. Los movimientos rechazados tampoco cuentan como gastos confirmados y se señalan para revisión. Las compras del otro teléfono se incorporan cuando ambos sincronizan. Edita **Presupuesto, columna C**, y sincroniza para recibir las nuevas asignaciones.

El disponible del presupuesto es un margen de planificación. **Saldos por moneda** muestra el efectivo contabilizado en USD y CUP, incluido el dinero reservado; no todo ese efectivo está libre para gastar. El plan de compras puede seguir incompleto aunque una partida tenga disponible.

## Gestionar planes sin internet y resolver cambios simultáneos

Los planes se guardan en este teléfono antes de sincronizarse, separados de los movimientos de dinero. Puedes editar un borrador que todavía no se ha intentado enviar. Si aparece **En espera de confirmación**, sincroniza antes de volver a modificarlo: así un reintento conserva exactamente los mismos datos. Cerrar la app conserva el plan y sus identificadores.

La sincronización envía primero el plan y, después de confirmarlo, los gastos vinculados. Si el Sheet u otro teléfono cambió el mismo plan, aparece **Revisar y corregir** con el motivo y la versión actual. Revisa los datos y guarda tu corrección. Los pagos vinculados esperan mientras haya un cambio de plan sin resolver. **Descartar este cambio y conservar el Sheet** abandona únicamente la modificación local; si el plan era nuevo y aún no existe en el Sheet, corrígelo para poder enviar sus pagos. La pestaña **Planes app** conserva quién modificó cada plan, desde qué dispositivo y cuándo; no registra ingresos ni gastos.

## Plan CUP, campo opcional y prueba del aviso

**Plan CUP es tu agenda de pagos futuros en pesos.** Indica concepto, fecha, importe, categoría, imprescindible Sí/No y activo Sí/No. Crear un plan no descuenta dinero ni registra un gasto.

**Pago planificado CUP (opcional)** enlaza un gasto real con esa agenda. Si planificaste 10.000 CUP para alimentos y pagas 3.000 CUP, selecciona el plan y quedarán 7.000 CUP pendientes. La app no registra otro gasto por vincularlo. Si no habías creado un plan, deja Sin asociar; tu gasto se guarda normalmente. Solo se vinculan gastos CUP de la misma categoría.

El aviso usa pagos imprescindibles pendientes dentro de 15 días, los CUP utilizables (total efectivo + transferencia menos la reserva CUP) y una antelación de 3 días al primer pago sin cobertura. Protege inicialmente 175 USD y calcula el cambio máximo como el menor entre el déficit convertido a tu tasa y los USD que superan la reserva. Si no quedan USD libres, avisa que hay que revisar el plan u obtener CUP, sin vender la reserva. Una fecha más lejana puede tener déficit sin necesitar un cambio todavía.

Ejemplo de cálculo, no de cotización del día: saldo real de 350 USD y 5.000 CUP, reserva de 175 USD y pago imprescindible de 30.000 CUP dentro de dos días. Faltan 25.000 CUP. A 870 CUP/USD, el máximo necesario es 28,74 USD, redondeado hacia arriba a centavos. A 840 serían 29,77 USD. El aviso real muestra su referencia, fuente y fecha; no etiqueta una tasa antigua como consultada hoy.

Para comprobar las notificaciones sin alterar tus cuentas:

1. APK → Perfil → Avisos de necesidad de CUP: permite las notificaciones de Android.
2. Pulsa **Probar aviso de protección USD**. Aparecerá **PRUEBA · Protección USD** y el diagnóstico actual. Esta prueba no registra dinero, no cambia planes y no reinicia el aviso automático.
3. Para probar también la regla, puedes crear una fila temporal en Plan CUP con fecha hoy, concepto PRUEBA, categoría válida, importe mayor que los CUP utilizables, Imprescindible Sí y Activo Sí. No cambies ingresos ni saldos iniciales para simular dinero.
4. Sincroniza la APK y abre Plan CUP y protección USD. Si faltan CUP y hay USD por encima de la reserva, mostrará el máximo; si no hay USD libres, pedirá revisar sin tocar la reserva. Planes incompletos o protección desactivada impiden la recomendación normal.
5. Al terminar, cambia Activo a No en esa fila y sincroniza para retirar la necesidad de prueba. No registres un cambio de moneda ficticio.

El aviso se revisa al abrir/sincronizar y con un trabajo periódico de 6 horas. Android puede retrasarlo. No se repite continuamente si la necesidad y la referencia son iguales; el botón PRUEBA permite comprobar la entrega sin esperar ni modificar esa regla.

## Oferta Yosvany recibida el 27/10/2026

Se conservan los **56 importes** en Ofertas recibidas y se añaden **41 productos**, para un catálogo de **88**. Moneda CUP y precios por libra confirmados para venta a granel. Se respetan las presentaciones explícitas; no se supone el peso de un paquete ni de un tubo. 19 envasados sin presentación clara conservan su precio recibido y quedan señalados para confirmar unidad. En la APK puedes ver ese importe; debes elegir la unidad y el precio aplicable antes de registrar el gasto.

Ejemplos: boniato 150 CUP/lb, arroz a granel 370 CUP/lb, pollo de 11 lb 9.500 CUP/paquete, leche en polvo de 1 kg 4.200 CUP y aceite de 900 ml 2.800 CUP/botella. El aceite conserva el cálculo por litro y compra botellas completas: si el plan requiere un litro, se necesitan dos botellas. Al cambiar arroz/azúcar de kg a lb se convierte la cantidad planificada. Las unidades sin equivalencia comprobada se revisan, guardando la cantidad anterior en observaciones. La fórmula de boniato de los perros se conserva.

Directorio identifica Oferta Yosvany; el usuario no proporcionó contacto, dirección ni enlace externo. No se atribuye la oferta a un vendedor web anterior. La revisión diaria conserva estos precios manuales; el tipo de cambio y las demás fuentes mantienen sus revisiones normales. La oferta no modifica movimientos de dinero, cantidades elegidas para productos de la misma unidad ni asignaciones del presupuesto. Los productos nuevos tienen cantidad mensual 0, como opcionales; elige los que uses.

## Modo compra · lista, tope y reposición

1. Abre **Comprar → Iniciar Modo compra**. Fija el máximo para ese día en CUP o USD y una tasa de comparación. Esa tasa queda fija para comparar pagos en distintas monedas; cada gasto conserva además su tasa histórica.
2. Pulsa **Elegir productos para la lista → Seleccionar varios productos**. Marca los que quieras, pulsa **Añadir seleccionados a mi lista** y revisa el grupo en una pantalla. También puedes añadirlos individualmente. Si ya tienes pagos previstos, puedes elegir un plan una vez por categoría en el formulario del grupo; se aplica solo a los artículos pagados en CUP de esa categoría. No añadas el tope diario como un pago adicional en Plan CUP: es un límite de la jornada, no otro compromiso. Selecciona cantidad, **Paquete / Libra / Kilogramo / Unidad**, moneda, formato CUP y precio por esa unidad. La vista calcula cantidad × precio. Al cambiar la unidad se vacía el precio para que introduzcas el correcto; no se inventa el peso de un paquete.
3. **Guardar en mi lista / Añadir todos a mi lista** prepara la compra. Todavía no registra un gasto. Puedes dejar productos como borrador con precio o unidad pendientes; usa Completar antes de confirmar su compra. El grupo se guarda de forma atómica y reintentarlo no duplica filas.
4. Cuando realmente compres el artículo, pulsa **✓ → Confirmar y tachar**. En una sola transacción se guarda su gasto con tu perfil y se marca comprado. Si supera el disponible del tope, se bloquea antes de guardar. La comprobación se realiza también sin internet.
5. Los comprados desaparecen de pendientes. **Ver comprados hoy** muestra su lista tachada. En Movimientos puedes comprobar si están pendientes, sincronizados o requieren revisión.
6. **Salir del modo** conserva la jornada. **Retomar la compra de hoy** recupera el tope, lista y gasto. Hay una jornada por fecha, compartida entre los perfiles del teléfono; cerrar, reiniciar o cambiar de perfil no reinicia el dinero gastado. El día siguiente permite una nueva jornada. El límite de la jornada no se puede aumentar después de iniciarla en esta versión.
7. Si tenías una cesta antes de iniciar el modo, puedes usar **Pasar cesta al Modo compra de hoy**. Trasladar artículos no registra gastos; debes confirmar cada uno. Mientras existe la jornada, las nuevas selecciones de productos pasan a su lista y la cesta exige trasladarse antes de registrar sus compras.

**Disponible con toda la lista.** La tarjeta verde muestra cuánto gastarías con todos los productos pendientes y lo que quedaría del tope: **tope − ya comprado − estimación de la lista**. Cada línea suma cantidad × precio y se convierte a la moneda del tope con la tasa fija de la jornada. Por ejemplo: tope 10 USD, comprado 2 USD y lista pendiente 6 USD → disponible estimado 2 USD. Confirmar un artículo lo traslada de lista a comprado, sin descontarlo dos veces. Si faltan precios o unidades, avisa que la estimación es parcial. Si la lista supera el tope, muestra la diferencia para que ajustes cantidades o quites productos. El saldo disponible antes de descontar la lista también se muestra como referencia.

El tope se aplica a compras registradas en la jornada de **ese teléfono**. No puede impedir un pago físico ni conocer al instante las compras de otro teléfono sin conexión. Los gastos manuales de otros conceptos se siguen registrando fuera del asistente. Los importes del tope no se duplican por sincronizar. Una compra ya confirmada sigue consumiendo su tope aunque su asiento requiera una corrección; si hay que cambiar su importe o fecha, revísalo en el Sheet.

**Una compra mensual y aviso antes de repetir.** En **Lista compras N**, 1 indica una compra por mes y 0 deja libre la repetición. Inicialmente se propone 1 si la cantidad mensual D es 1; revisa esta configuración, porque una unidad mensual y una compra al mes pueden ser distintas en tu hogar. Los productos con N=1 ya comprados se ocultan del catálogo, sugerencias y lista pendiente. Para verlos, pulsa **Mostrar mensuales ya comprados**. Antes de confirmar una repetición aparece un aviso y debes marcar que quieres repetirla; después funciona normalmente y descuenta el gasto y el tope. El aviso también se aplica a la cesta habitual.

**Sugerencias estadísticas.** Se usa la mediana de los intervalos entre al menos 3 fechas distintas de compra de cada producto. Por ejemplo, si compraste pollo el día 1, 16 y 31, sus intervalos son 15 y 15 días y se estima reposición 15 días después de la última compra. La sugerencia muestra fechas usadas, intervalo y última compra. No es una certeza de consumo ni una recomendación nutricional. Con poco historial se indica que faltan compras; no se inventa una frecuencia.

Se utiliza el detalle de artículos que confirma la API y los pendientes locales. Los gastos escritos solo como “comida” sin identificar productos no permiten calcular hábitos. Si corriges un total en el Sheet y ya no coincide con su detalle original, ese detalle se excluye de las estadísticas hasta revisarlo. Sin red se trabaja con el último historial recibido. En otro teléfono, las compras recientes aparecen después de que ambos sincronicen.

**Formularios y navegación.** Las actualizaciones en segundo plano conservan el formulario y el PIN que estás escribiendo. Una sesión que se bloquee sigue cerrando los formularios financieros. El teclado se cierra al enviar para que puedas leer la respuesta. Los errores se desplazan a la zona visible, el panel se adapta al espacio disponible y la barra inferior se sitúa dentro del área que dejan los controles de Android. Pulsa el logotipo **hogar playa** para volver a Inicio.

## 5. Sin internet y estados de envío

- **En el teléfono:** guardado en la base local; aún no confirmado por el Sheet.
- **Enviando:** intento de sincronización en curso.
- **Sincronizado:** el Sheet confirmó el movimiento.
- **Revisar:** el servidor explicó un problema, como falta de tasa, fecha anterior al inicio del control, saldo USD insuficiente o perfil desactivado. Abre el movimiento para leer el motivo y corregirlo cuando corresponda.

Los movimientos y las cestas sobreviven al cierre de la app y a un reinicio. Cada movimiento conserva un identificador: si se pierde la respuesta y se reenvía, se contabiliza una sola vez. La app programa el envío al volver la red y al abrirla. Desde Android 9 también se admiten conexiones que alcancen el servicio aunque falle la sonda de validación de Android. Hay una reevaluación de respaldo a los 15 minutos. Android puede retrasar trabajos en segundo plano por batería o ahorro de datos; si fuerzas la detención de la app, debes abrirla de nuevo. Usa **Movimientos → Sincronizar** y comprueba el estado antes de dar un envío por completado.

La vista móvil utiliza la última copia descargada del Sheet y los movimientos locales. Sin red no puede conocer lo que acaba de registrar otra persona. La fecha de última sincronización se muestra en Inicio. Al cambiar de mes, los pendientes conservan su fecha original y el presupuesto mensual empieza su nuevo periodo.

## 6. Trazabilidad, correcciones y mantenimiento

- **Planes app:** auditoría de creación, edición, cancelación y reactivación; Plan CUP L:Q conserva los metadatos del último cambio para recuperar un envío interrumpido. No edites estas columnas.
- **Registro:** A:K mantiene el formato original; L:S añade operación, perfil, nombre, dispositivo, creación local, sincronización, origen y huella. T vincula el gasto al pago de Plan CUP, U guarda el formato CUP y V conserva los datos de la jornada y su tope.
- **Cambios:** A:I mantiene el formato original; J:Q añade los datos de trazabilidad y R guarda el formato recibido.
- **Auditoría app:** conserva operación, estado, perfil, dispositivo, fechas, detalle y fila de destino. Los rechazos también dejan su motivo.
- **Perfiles app / Dispositivos app:** puedes cambiar ACTIVO por REVOCADO para bloquear un perfil o teléfono. No cambies sus identificadores ni sus nombres directamente.

Los perfiles identifican a las personas de este hogar y usan un PIN local; no constituyen cuentas de Google ni permisos independientes sobre el presupuesto familiar.

Para corregir un movimiento rechazado, ábrelo en la APK y usa **Corregir y reintentar**. Si provenía de una cesta puedes corregir cantidades y precios. Conserva su identificador. Si ya fue sincronizado, corrígelo en su fila original del Sheet y sincroniza; no lo anotes de nuevo. El historial local conserva los movimientos del teléfono y recibe los últimos 250 del Sheet. El detalle original de artículos queda como referencia cuando una corrección posterior cambia el importe en la hoja.

Para recuperar un PIN olvidado, en la pantalla de perfiles pulsa **Renovar conexión / recuperar PIN**, vincula con un código nuevo generado en el Sheet y usa **Restablecer un PIN local** durante los siguientes 5 minutos. Los pendientes se conservan.

Antes de desinstalar o borrar los datos del teléfono, sincroniza o usa **Perfil → Exportar respaldo local**. El archivo JSON permite conservar los registros para revisión o recuperación manual; esta versión no tiene un importador de respaldos. No incluye PIN ni credenciales de conexión. La credencial del dispositivo queda cifrada mediante AndroidKeyStore; los movimientos se guardan en SQLite dentro del almacenamiento privado de la app.

Cuando cambies el script, conserva la URL: **Implementar → Gestionar implementaciones → lápiz → Versión: Nueva versión → Implementar**. Después sincroniza la APK. Pegar código sin actualizar la implementación no cambia la versión que usa el teléfono.

Los precios del catálogo son referencias, no importes garantizados. La app recibe las actualizaciones que prepara tu Sheet; confirma siempre el precio pagado, entrega, compra mínima y presentación. Los límites actuales de la hoja son 1.000 movimientos y 500 cambios; si se llenan, los nuevos registros se conservan pendientes hasta preparar un archivo del periodo.

## Prueba breve al instalar

Haz la prueba en una copia de tu hoja si no quieres añadir movimientos de ensayo a tus cuentas reales:

1. Registra un ingreso de 10 USD y espera a ver Sincronizado.
2. Activa el modo avión, registra un gasto de 1 USD y comprueba En el teléfono. El disponible para consumo bajará 1 USD.
3. Cierra y vuelve a abrir la app. El gasto debe seguir ahí.
4. Desactiva el modo avión y pulsa Sincronizar. Debe aparecer una sola fila por movimiento, con tu perfil en Registro y en Auditoría app.
5. Pulsa Sincronizar otra vez: no debe crear filas nuevas ni descontar otra vez ese gasto.

La APK y el servidor se han probado en emulador con servicios Google simulados. La autorización e implementación en tu cuenta real se completan con los pasos 1 a 3.

## Catálogo ilustrado, libras y fondo de gasolina

Las 91 tarjetas tienen ilustraciones originales del producto, incluidas en la APK y disponibles sin internet. Se usan también en el detalle, la cesta y la lista del Modo compra. Guayaba, frutabomba, plátano, plátano macho maduro, cebollas y variedades de frijoles tienen imágenes distintas. Las ilustraciones representan el producto; no son fotografías de un vendedor ni certifican marcas o presentaciones.

**Guayaba se registra en libras.** Ya no se redondea su planificación a bolsas de 5 lb. La referencia online anterior era 4,83 USD por 5 lb: equivale a 0,966 USD/lb, aproximadamente 0,97 en el formulario. La oferta de origen sigue siendo por bolsa; confirma precio real y venta suelta con tu vendedor. Se conservan cantidades y precios propios cuando ya estaban expresados por libra; las unidades de masa compatibles se convierten. Los movimientos históricos y las compras ya confirmadas conservan su unidad original.

**Jamón y Queso** se añaden por libra, opcionales inicialmente y con precio pendiente: introdúcelo al comprar. **Gasolina** se añade por litro en la categoría **Fondo gasolina**. No se inventan precios ni proveedores.

**Fondo gasolina** figura en Presupuesto fila 17 y Fondos fila 11. Empieza con asignación 0; tú decides cuánto reservar. Registrar → Aporte fondo → Fondo gasolina anota dinero separado, sin descontarlo de las cuentas. Registrar → Gasto → Fondo gasolina o confirmar el producto Gasolina anota el pago real, reduce su saldo y el efectivo o transferencia utilizados. No vuelve a cargar esa misma compra a la partida de consumo. El fondo puede quedar negativo si gastas antes de reservar; el tablero lo señala. Se conserva el mínimo de ahorro del 50%; redistribuye asignaciones si el total supera el límite.
