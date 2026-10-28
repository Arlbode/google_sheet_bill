const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs'),vm=require('node:vm'),path=require('node:path'),zlib=require('node:zlib'),crypto=require('node:crypto');
const NOW=Date.parse('2026-10-28T13:00:00Z');
class FixedDate extends Date{constructor(...a){super(...(a.length?a:[NOW]));}static now(){return NOW;}}
const actor='123e4567-e89b-42d3-a456-426614174000',deviceId='123e4567-e89b-42d3-a456-426614174001',household='123e4567-e89b-42d3-a456-426614174002';
function fixture(){
 const properties=new Map(),writes=[],logs=[],settings={B7:870,B11:'',B26:'Sí'};
 const props={getProperty:k=>properties.get(k)||null,setProperty(k,v){properties.set(k,v);return this;},deleteProperty:k=>properties.delete(k)};
 const blob=b=>({bytes:Buffer.from(b),getDataAsString:()=>Buffer.from(b).toString('utf8')});
 const c={Date:FixedDate,PropertiesService:{getScriptProperties:()=>props},Utilities:{base64Decode:x=>Buffer.from(x,'base64'),newBlob:blob,ungzip:b=>blob(zlib.gunzipSync(b.bytes)),computeDigest:(_,s)=>[...crypto.createHash('sha256').update(s).digest()],DigestAlgorithm:{SHA_256:1},Charset:{UTF_8:1}},
 ContentService:{MimeType:{JSON:'json'},createTextOutput:body=>({body,setMimeType(){return this;}})},LockService:{getScriptLock:()=>({tryLock:()=>true,releaseLock(){}})}};
 vm.createContext(c);vm.runInContext(fs.readFileSync(path.resolve(__dirname,'../backend/Code.gs'),'utf8'),c);
 c.movilFecha_=date=>new Date(date).toLocaleDateString('en-CA',{timeZone:'America/Havana'});c.historialPrecios_=()=>logs;c.registrarRevision_=(...args)=>logs.push(args);
 const device={id:deviceId,name:'Teléfono de prueba',row:2},profiles={[actor]:{id:actor,name:'Prueba',active:true}};
 const ss={getSheetByName(name){
  if(name==='Ajustes')return {getRange:key=>({getValue:()=>settings[key],setValue(v){writes.push(key);settings[key]=v;}})};
  if(name==='Registro')return {getRange:key=>{assert.equal(key,'U5','la tasa no toca filas financieras');return {getValue:()=> 'Formato CUP'};}};
  if(name==='Plan CUP')return {getRange:key=>{assert.equal(key,'L5');return {getValue:()=> 'Último cambio app ID'};}};
  if(name==='Planes app')return {};
  if(name==='Dispositivos app')return {getDataRange:()=>({getValues:()=>[['ID'],[deviceId,'Teléfono de prueba','','ACTIVO']]})};
  if(name==='Perfiles app')return {getDataRange:()=>({getValues:()=>[['ID'],[actor,'Prueba','ACTIVO']]})};
  throw Error('La referencia no debe tocar '+name);
 }};
 c.movilHoja_=()=>ss;
 const token='a'.repeat(64);properties.set('MOVIL_DEVICE_'+deviceId,JSON.stringify({hash:crypto.createHash('sha256').update(token).digest('hex'),householdId:household}));
 const reference=(extra={})=>({id:crypto.randomUUID(),rate:900,sourceAt:new Date(NOW-300000).toISOString(),fetchedAt:new Date(NOW-60000).toISOString(),actorId:actor,provider:'TASALO',source:'eltoque',url:'http://tasalo.duckdns.org:8040/api/v1/tasas/latest?max_age_minutes=30',...extra});
 return {c,ss,device,profiles,reference,properties,writes,logs,settings,token,receive:q=>c.recibirTasaDesdeApk_(ss,device,profiles,q),post:q=>JSON.parse(c.doPost({postData:{contents:JSON.stringify({apiVersion:3,action:'referenceSync',deviceId,householdId:household,token,profiles:[{id:actor,name:'Prueba'}],reference:q})}}).body)};
}
test('la referencia de la APK escribe solo B7/B11 y conserva la fecha propia de elTOQUE',()=>{
 const f=fixture(),q=f.reference(),ack=f.receive(q);assert.equal(ack.status,'accepted');assert.equal(f.settings.B7,900);assert.equal(f.settings.B11.toISOString(),q.sourceAt);assert.deepEqual(f.writes,['B7','B11']);
 const saved=JSON.parse(f.properties.get('HOGAR_TASA_VERIFICADA'));assert.equal(saved.consultedAt,q.sourceAt);assert.equal(saved.receivedAt,new FixedDate().toISOString());assert.equal(saved.actorId,actor);assert.match(saved.source,/TASALO/);assert.equal(f.c.tasaParaPlan_(f.ss).fresh,true);
});
test('un reintento no vuelve a escribir y un mismo UUID con otro importe se rechaza',()=>{
 const f=fixture(),q=f.reference();f.receive(q);assert.equal(f.receive(q).status,'duplicate');assert.equal(f.writes.length,2);assert.equal(f.logs.length,1);assert.throws(()=>f.receive({...q,rate:901}),/otros datos/);assert.equal(f.settings.B7,900);
});
test('una referencia retrasada o anterior a la del Sheet no pisa la vigente',()=>{
 const f=fixture();f.receive(f.reference({rate:902,sourceAt:new Date(NOW-60000).toISOString(),fetchedAt:new Date(NOW).toISOString()}));
 assert.equal(f.receive(f.reference()).status,'superseded');assert.equal(f.settings.B7,902);
 assert.equal(f.receive(f.reference({sourceAt:new Date(NOW-7200000).toISOString(),fetchedAt:new Date(NOW-7140000).toISOString()})).status,'expired');assert.equal(f.writes.length,2);
});
test('se rechazan fuente distinta, importe textual, perfil inactivo y fecha incoherente',()=>{
 for(const invalid of [{source:'bcc'},{provider:'otro'},{rate:'900'},{rate:-1},{url:'https://otro.example/rate'},{sourceAt:new Date(NOW+600000).toISOString(),fetchedAt:new Date(NOW+600000).toISOString()},{sourceAt:''}]){
  const f=fixture();assert.throws(()=>f.receive(f.reference(invalid)));assert.equal(f.writes.length,0);
 }
 const f=fixture();f.profiles[actor].active=false;assert.throws(()=>f.receive(f.reference()),/perfil/);assert.equal(f.writes.length,0);
});
test('B26 desactivado confirma el descarte sin cambiar el valor manual',()=>{
 const f=fixture();f.settings.B26='No';assert.equal(f.receive(f.reference()).status,'disabled');assert.equal(f.settings.B7,870);assert.equal(f.writes.length,0);
});
test('la recepción autenticada verifica dispositivo y hogar sin crear movimientos',()=>{
 const f=fixture(),result=f.post(f.reference());assert.equal(result.ok,true);assert.equal(result.householdId,household);assert.equal(result.referenceAck.status,'accepted');assert.equal(result.quote.rate,900);assert.equal(f.writes.length,2);assert.ok(!JSON.stringify(f.logs.map(row=>row.slice(1))).includes(f.token));
 const unauthorized=fixture();unauthorized.properties.delete('MOVIL_DEVICE_'+deviceId);const denied=unauthorized.post(unauthorized.reference());assert.equal(denied.ok,false);assert.equal(denied.error.code,'NO_AUTORIZADO');assert.equal(unauthorized.writes.length,0);
});
test('el modo APK impide que la consulta web del Sheet sustituya la referencia recibida',()=>{
 const f=fixture();f.receive(f.reference());f.c.UrlFetchApp={fetch(){throw Error('No se debe consultar otra fuente desde el Sheet');}};f.c.revisarTasaParaApp_(f.ss);assert.equal(f.settings.B7,900);assert.equal(f.writes.length,2);
});
