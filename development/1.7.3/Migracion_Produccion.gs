/** Archivo adicional del MISMO proyecto Apps Script. No sustituye Código.gs. */
var HOGAR_PRODUCCION_DESTINO_173 = '__SHEET_ID__';
var HOGAR_PRODUCCION_CLAVE_173 = 'HOGAR_PRODUCCION_DESDE_CERO_173';

function dejarListoParaProduccion() {
  var ss=SpreadsheetApp.getActiveSpreadsheet();
  if(!ss||ss.getId()!==HOGAR_PRODUCCION_DESTINO_173)throw new Error('La hoja abierta no es el destino de esta migración. No se modificó nada.');
  var lock=LockService.getScriptLock();lock.waitLock(30000);
  try {
    var props=PropertiesService.getScriptProperties(),key=HOGAR_PRODUCCION_CLAVE_173+':'+ss.getId(),old=props.getProperty(key),state=old?JSON.parse(old):null;
    if(props.getProperty('HOGAR_SPREADSHEET_ID')!==ss.getId()||!props.getProperty('MOVIL_HOGAR_ID'))throw new Error('El proyecto está configurado para otra hoja. No se modificó nada.');
    if(state&&state.phase==='DONE'){console.log('Esta migración ya terminó. No se borran registros nuevos. Respaldo privado: '+state.backupUrl);return state;}
    var sheets=produccionValidar173_(ss),today=Utilities.formatDate(new Date(),'America/Havana','yyyy-MM-dd');
    if(!state){
      var folder=DriveApp.createFolder('Respaldo privado Hogar antes de producción '+today+' '+Utilities.getUuid().slice(0,8));
      var copy=DriveApp.getFileById(ss.getId()).makeCopy(ss.getName()+' · antes de producción · '+today,folder);
      if(copy.getId()===ss.getId())throw new Error('No se pudo verificar la copia previa. No se modificó la hoja.');
      var backup=SpreadsheetApp.openById(copy.getId());
      if(backup.getSheets().length!==ss.getSheets().length)throw new Error('La copia previa no está completa. No se modificó la hoja.');
      // Credenciales anteriores: archivo privado del dueño; jamás se imprimen valores.
      var removed={};props.getKeys().forEach(function(k){if(k==='MOVIL_PAIR'||k.indexOf('MOVIL_DEVICE_')===0)removed[k]=props.getProperty(k);});
      folder.createFile('Conexiones_anteriores_PRIVADO.json',JSON.stringify({sheetId:ss.getId(),createdAt:new Date().toISOString(),properties:removed}),MimeType.PLAIN_TEXT);
      state={version:173,phase:'BACKED_UP',date:today,backupId:copy.getId(),backupUrl:backup.getUrl(),backupFolderUrl:folder.getUrl()};
      props.setProperty(key,JSON.stringify(state));
    }
    DriveApp.getFileById(state.backupId); // Si se perdió el respaldo, no seguir borrando.
    // El mismo bloqueo de la API impide que un envío antiguo atraviese el reinicio.
    props.deleteProperty('MOVIL_PAIR');
    props.getKeys().forEach(function(k){if(k.indexOf('MOVIL_DEVICE_')===0)props.deleteProperty(k);});
    var devices=sheets['Dispositivos app'],last=devices.getLastRow();
    if(last>1)devices.getRange(2,4,last-1,1).setValues(Array.from({length:last-1},function(){return ['REVOCADO'];}));
    state.phase='CONNECTIONS_REVOKED';props.setProperty(key,JSON.stringify(state));
    // Solo contenido: se conservan fórmulas, formato, validación y estructura.
    produccionVaciar173_(sheets.Registro.getRange('A6:AE1005'));
    produccionVaciar173_(sheets.Cambios.getRange('A6:R505'));
    produccionVaciar173_(sheets['Plan CUP'].getRange('B6:Q205'));
    sheets['Plan CUP'].getRange('F6:G205').setValues(Array.from({length:200},function(){return ['No','Sí'];}));
    ['Auditoría app','Planes app','Lista compartida app','Recibos lista app'].forEach(function(name){
      var s=sheets[name];if(s&&s.getLastRow()>1)s.getRange(2,1,s.getLastRow()-1,s.getLastColumn()).clearContent();
    });
    ['B6','B7','B24'].forEach(function(cell){sheets.Cuentas.getRange(cell).setValue(0);});
    sheets.Ajustes.getRange('B13').setValue(0);
    sheets.Fondos.getRange('B6:B11').setValues([[0],[0],[0],[0],[0],[0]]);
    sheets.Ajustes.getRange('B12').setValue(Utilities.parseDate(state.date,'America/Havana','yyyy-MM-dd'));
    sheets.Ajustes.getRange('B6').setFormula('=DATE(YEAR(TODAY()),MONTH(TODAY()),1)');
    SpreadsheetApp.flush();produccionComprobar173_(sheets);
    state.phase='DONE';state.completedAt=new Date().toISOString();props.setProperty(key,JSON.stringify(state));
    console.log('PRODUCCIÓN A CERO: registros, cambios, planes, listas, recibos y saldos iniciales limpios. Catálogo, perfiles y fórmulas conservados. Conexiones anteriores revocadas. Respaldo privado: '+state.backupUrl);
    return state;
  }finally{lock.releaseLock();}
}

function produccionVaciar173_(range){var formulas=range.getFormulas();range.clearContent();range.setFormulas(formulas);}

function produccionValidar173_(ss){
  var required={Registro:['A5','Fecha'],Cambios:['B5','USD entregados'],Cuentas:['A6','USD'],Ajustes:['A13','Ahorro acumulado al inicio · USD'],Fondos:['A5','Fondo'],'Plan CUP':['A5','Plan ID'],'Dispositivos app':['A1','Dispositivo ID'],'Perfiles app':['A1','Perfil ID'],'Auditoría app':['A1','Operación ID']};
  var found={};Object.keys(required).forEach(function(name){var s=ss.getSheetByName(name),check=required[name];if(!s||s.getRange(check[0]).getValue()!==check[1])throw new Error('No coincide la estructura de '+name+'. Se conserva la hoja.');found[name]=s;});
  var optional={'Planes app':'Cambio ID','Lista compartida app':'ID','Recibos lista app':'ID cambio'};
  Object.keys(optional).forEach(function(name){var s=ss.getSheetByName(name);if(s&&s.getRange('A1').getValue()!==optional[name])throw new Error('La pestaña '+name+' contiene otros datos. Se conserva la hoja.');found[name]=s;});
  [['Registro',6,1005,[8,9,11]],['Cambios',6,505,[4,7,9]],['Plan CUP',6,205,[8,9,10]]].forEach(function(v){v[3].forEach(function(col){var f=found[v[0]].getRange(v[1],col,v[2]-v[1]+1,1).getFormulas();if(f.some(function(r){return !r[0];}))throw new Error('Faltan fórmulas en '+v[0]+', columna '+col+'. No se limpió la hoja.');});});
  // No ejecutar sobre una plantilla que genera movimientos mediante fórmulas de entrada.
  [['Registro','A6:G1005'],['Cambios','A6:C505'],['Plan CUP','B6:G205']].forEach(function(v){if(found[v[0]].getRange(v[1]).getFormulas().some(function(r){return r.some(Boolean);}))throw new Error('Hay fórmulas propias en entradas de '+v[0]+'. Se conserva la hoja.');});
  return found;
}

function produccionComprobar173_(s){
  [['Registro','A6:G1005'],['Cambios','A6:C505'],['Plan CUP','B6:E205']].forEach(function(v){if(s[v[0]].getRange(v[1]).getValues().some(function(r){return r.some(function(x){return x!=='';});}))throw new Error('La limpieza quedó pendiente en '+v[0]+'. Las conexiones siguen bloqueadas; conserva el respaldo y vuelve a ejecutar esta misma migración.');});
  ['B6','B7','B24'].forEach(function(c){if(Number(s.Cuentas.getRange(c).getValue())!==0)throw new Error('No se confirmó saldo inicial cero.');});
  if(Number(s.Ajustes.getRange('B13').getValue())!==0||s.Fondos.getRange('B6:B11').getValues().some(function(r){return Number(r[0])!==0;}))throw new Error('No se confirmó reserva inicial cero.');
  var balances=s.Cuentas.getRange('F6:F7').getValues();if(balances.some(function(r){return typeof r[0]!=='number'||Math.abs(r[0])>1e-7;}))throw new Error('Los saldos no quedaron a cero. Conexiones bloqueadas; revisa las fórmulas antes de continuar.');
}
