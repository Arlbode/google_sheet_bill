const fs=require('node:fs'),vm=require('node:vm'),crypto=require('node:crypto'),zlib=require('node:zlib'),assert=require('node:assert/strict'),test=require('node:test');
const script=process.env.HOGAR_CONNECTION_SCRIPT;if(!script)throw new Error('Ejecuta primero python3 development/conexion-publica/tests/run.py, o define HOGAR_CONNECTION_SCRIPT con el script construido.');
const source=fs.readFileSync(script,'utf8');
const exec='https://script.google.com/macros/s/fixture-public/exec',dev='https://script.google.com/macros/s/fixture-test/dev';
function context(options={}){
 const properties=new Map([['MOVIL_PAIR','old-code-must-survive-errors'],['MOVIL_HOGAR_ID','household'],['MOVIL_DEVICE_existing','credential-kept']]);let calls=[],prepared=0,alerts=[],uiValue=exec,cancel=false;
 const blob=b=>({bytes:Buffer.from(b),getDataAsString:()=>Buffer.from(b).toString()});
 const ui={Button:{OK:'OK'},ButtonSet:{OK_CANCEL:'OK_CANCEL',OK:'OK'},prompt:()=>({getSelectedButton:()=>cancel?'CANCEL':'OK',getResponseText:()=>uiValue}),alert:(...a)=>alerts.push(a)};
 const c={console:{log(){}},ScriptApp:{getScriptId:()=> 'fixture-project',getService:()=>({getUrl:()=>options.contextUrl||dev})},PropertiesService:{getScriptProperties:()=>({getProperty:k=>properties.get(k)||null,setProperty:(k,v)=>properties.set(k,v)})},SpreadsheetApp:{getUi:()=>ui},LockService:{getScriptLock:()=>({waitLock(){},releaseLock(){}})},ContentService:{MimeType:{JSON:1},createTextOutput:body=>({body,setMimeType(){return this;}})},Utilities:{base64Decode:s=>Buffer.from(s,'base64'),newBlob:blob,ungzip:b=>blob(zlib.gunzipSync(b.bytes)),computeDigest:(_,v)=>[...crypto.createHash('sha256').update(v).digest()],DigestAlgorithm:{SHA_256:1},Charset:{UTF_8:1},getUuid:()=> '12345678-1234-4000-8000-000000000001'}};
 c.UrlFetchApp={fetch(url,opts){calls.push({url,opts});assert.equal(opts.method,'get');assert.equal(opts.payload,undefined);let data={ok:true,apiVersion:1,connectionProject:c.conexionProyectoAndroid_()};if(options.oldVersion)delete data.connectionProject;if(options.wrongProject)data.connectionProject='different-project';if(options.fetchError)throw Error('fallo de conexión ficticio');return {getResponseCode:()=>options.http||200,getContentText:()=>options.html?'<html>Iniciar sesión</html>':JSON.stringify(data)};}};
 vm.createContext(c);vm.runInContext(source,c);c.hoja_=()=>({});c.prepararAppMovil_=()=>prepared++;
 return {c,properties,calls,prepared:()=>prepared,alerts,setInput:v=>uiValue=v,cancel:()=>cancel=true};
}
test('con /dev y sin configuración no se crea código ni se toca la hoja',()=>{
 const x=context();assert.throws(()=>x.c.generarCodigoAndroid(),/pruebas \/dev/);assert.equal(x.prepared(),0);assert.equal(x.calls.length,0);assert.equal(x.properties.get('MOVIL_PAIR'),'old-code-must-survive-errors');
});
test('URL configurada /exec tiene prioridad sobre /dev y conserva el protocolo de códigos',()=>{
 const x=context();x.properties.set('HOGAR_ANDROID_EXEC_URL',exec);let start=Date.now(),r=x.c.generarCodigoAndroid();assert.equal(r.url,exec);assert.equal(r.code,'1234-5678-1234');assert.equal(x.prepared(),1);assert.equal(x.calls.length,1);let saved=JSON.parse(x.properties.get('MOVIL_PAIR'));assert.equal(saved.hash,x.c.movilHash_(r.code.replace(/-/g,'')));assert.equal(saved.attempts,0);assert(saved.expires>=start+600000);assert(saved.expires<=Date.now()+600000);assert.equal(x.properties.get('MOVIL_DEVICE_existing'),'credential-kept');
});
test('una URL /exec de contexto también se verifica antes de emitir código',()=>{
 const x=context({contextUrl:exec});assert.equal(x.c.generarCodigoAndroid().url,exec);assert.equal(x.calls.length,1);
});
test('rechaza 404, permisos, HTML de login, proyecto distinto y despliegue anterior sin consumir el código',()=>{
 for(const options of [{http:404},{http:403},{http:503},{html:true},{wrongProject:true},{oldVersion:true},{fetchError:true}]){const x=context(options);x.properties.set('HOGAR_ANDROID_EXEC_URL',exec);assert.throws(()=>x.c.generarCodigoAndroid());assert.equal(x.properties.get('MOVIL_PAIR'),'old-code-must-survive-errors');assert.equal(x.prepared(),0);}
});
test('configurar valida acceso público y mismo proyecto; solo guarda la URL',()=>{
 const x=context(),before=new Map(x.properties);x.setInput('  '+exec+'  ');let result=x.c.configurarConexionAndroid();assert.equal(result.url,exec);assert.equal(x.properties.get('HOGAR_ANDROID_EXEC_URL'),exec);for(let [k,v]of before)assert.equal(x.properties.get(k),v);assert.equal(x.prepared(),0);assert.equal(x.alerts.length,1);
});
test('cancelar o configurar /dev, URL con query o proyecto equivocado conserva la configuración anterior',()=>{
 const cancel=context();cancel.cancel();assert.equal(cancel.c.configurarConexionAndroid(),undefined);assert.equal(cancel.calls.length,0);
 for(const value of [dev,exec+'?x=1','https://example.test/macros/s/x/exec']){const x=context();x.properties.set('HOGAR_ANDROID_EXEC_URL',exec);x.setInput(value);assert.throws(()=>x.c.configurarConexionAndroid());assert.equal(x.properties.get('HOGAR_ANDROID_EXEC_URL'),exec);assert.equal(x.calls.length,0);}
 const wrong=context({wrongProject:true});wrong.properties.set('HOGAR_ANDROID_EXEC_URL',exec);assert.throws(()=>wrong.c.configurarConexionAndroid(),/otro proyecto/);assert.equal(wrong.properties.get('HOGAR_ANDROID_EXEC_URL'),exec);
});
test('doGet sigue siendo público apiVersion1 y no revela credenciales ni datos financieros',()=>{
 const x=context();let body=x.c.doGet().body,data=JSON.parse(body);assert.equal(data.apiVersion,1);assert.equal(data.ok,true);assert.equal(data.connectionVersion,2);assert.equal(data.connectionProject,x.c.conexionProyectoAndroid_());assert(!body.includes('credential-kept'));assert(!body.includes('MOVIL_PAIR'));assert.equal(data.balances,undefined);assert.equal(data.history,undefined);
});
test('diagnóstico distingue contexto /dev de URL /exec configurada',()=>{
 const x=context();x.properties.set('HOGAR_ANDROID_EXEC_URL',exec);let r=x.c.diagnosticarConexionAPK();assert.equal(r.contextoEsDePruebas,true);assert.equal(r.urlParaAPK,exec);assert.equal(r.servicioPublicoVerificado,true);assert.equal(x.properties.get('MOVIL_PAIR'),'old-code-must-survive-errors');
});
