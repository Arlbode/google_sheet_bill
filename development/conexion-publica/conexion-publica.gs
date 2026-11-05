/** URL pública explícita: getService().getUrl() puede devolver /dev en el editor. */
var HOGAR_ANDROID_EXEC_KEY = 'HOGAR_ANDROID_EXEC_URL';

function conexionProyectoAndroid_(){return movilHash_(ScriptApp.getScriptId()).slice(0,24);}

function conexionUrlValidaAndroid_(url){return /^https:\/\/script\.google\.com\/macros\/s\/[A-Za-z0-9_-]+\/exec$/.test(String(url||'').trim());}

function conexionUrlAndroid_(){
  var guardada=PropertiesService.getScriptProperties().getProperty(HOGAR_ANDROID_EXEC_KEY)||'';
  if(guardada){if(!conexionUrlValidaAndroid_(guardada))throw new Error('La URL guardada no es pública /exec. Ejecuta configurarConexionAndroid y copia la URL de Aplicación web.');return guardada.trim();}
  var contexto=ScriptApp.getService().getUrl()||'';
  if(conexionUrlValidaAndroid_(contexto))return contexto.trim();
  throw new Error('El proyecto devuelve una URL de pruebas /dev o no tiene URL pública configurada. Publica Aplicación web y ejecuta configurarConexionAndroid con la URL /exec que muestra Google.');
}

function conexionVerificarAndroid_(url){
  if(!conexionUrlValidaAndroid_(url))throw new Error('Pega la URL completa de Aplicación web terminada en /exec. No conviertas un enlace /dev cambiando su terminación.');
  var respuesta=UrlFetchApp.fetch(url,{method:'get',muteHttpExceptions:true,followRedirects:true}),http=respuesta.getResponseCode();
  if(http===404)throw new Error('La URL /exec devuelve 404. Copia otra vez la URL completa de una implementación web activa. No se ha generado ni invalidado ningún código.');
  if(http===401||http===403)throw new Error('Google no permite el acceso público (HTTP '+http+'). Configura Ejecutar como: Yo y Acceso: Cualquier persona.');
  if(http!==200)throw new Error('No se pudo verificar la URL pública (HTTP '+http+'). No se modificó la conexión.');
  var data;try{data=JSON.parse(respuesta.getContentText());}catch(e){throw new Error('Google devolvió una página en lugar del servicio. Revisa el acceso Cualquier persona y la URL de Aplicación web.');}
  if(!data||data.ok!==true||data.apiVersion!==1)throw new Error('Esta URL no devuelve el servicio de Hogar finanzas.');
  if(!data.connectionProject)throw new Error('La URL ejecuta una versión anterior del script. Guarda este Código.gs y publica Nueva versión en esa implementación antes de configurar la conexión.');
  if(data.connectionProject!==conexionProyectoAndroid_())throw new Error('La URL pertenece a otro proyecto Apps Script. Copia la implementación de ESTE proyecto, donde generas los códigos.');
  return url.trim();
}

function configurarConexionAndroid(){
  var ui=SpreadsheetApp.getUi(),entrada=ui.prompt('Configurar conexión pública de Android','Pega la URL completa /exec de Implementar → Aplicación web. Debe ejecutar como tu cuenta y permitir Cualquier persona.',ui.ButtonSet.OK_CANCEL);
  if(entrada.getSelectedButton()!==ui.Button.OK)return;
  var url=conexionVerificarAndroid_(entrada.getResponseText().trim());
  PropertiesService.getScriptProperties().setProperty(HOGAR_ANDROID_EXEC_KEY,url);
  ui.alert('Conexión pública verificada','URL guardada para este proyecto. Abre Hogar → Conectar app Android, genera un código nuevo y usa la URL mostrada allí en la APK.',ui.ButtonSet.OK);
  return {ok:true,url:url};
}

function diagnosticarConexionAPK(){
  var contexto=ScriptApp.getService().getUrl()||'',guardada=PropertiesService.getScriptProperties().getProperty(HOGAR_ANDROID_EXEC_KEY)||'',resultado={urlDelContexto:contexto,urlConfigurada:guardada,contextoEsDePruebas:/\/dev$/.test(contexto)};
  try{resultado.urlParaAPK=conexionUrlAndroid_();conexionVerificarAndroid_(resultado.urlParaAPK);resultado.servicioPublicoVerificado=true;}catch(e){resultado.servicioPublicoVerificado=false;resultado.error=e.message;}
  console.log(JSON.stringify(resultado,null,2));return resultado;
}
