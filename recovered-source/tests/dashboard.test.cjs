const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const path=require('node:path');
const vm=require('node:vm');
const zlib=require('node:zlib');
const root=path.resolve(__dirname,'..');
const now=Date.parse('2026-10-28T03:00:00.000Z');
class FixedDate extends Date { constructor(...args){super(...(args.length?args:[now]));} static now(){return now;} }

function dashboard(state){
 const pendingCalls=[],elements={toast:{style:{}}};
 const document={getElementById:id=>elements[id]||null,activeElement:null,visibilityState:'visible',
  documentElement:{style:{setProperty(){}}},body:{classList:{toggle(){}}},addEventListener(){}};
 const context={Date:FixedDate,document,Uint8Array,crypto:require('node:crypto').webcrypto,
  setTimeout:()=>1,clearTimeout(){},setInterval(){},fetch:()=>new Promise(()=>{}),innerHeight:800,addEventListener(){}};
 context.window=context;
 context.HogarNative={call(payload){pendingCalls.push(JSON.parse(payload));}};
 vm.createContext(context);
 const source=fs.readFileSync(path.join(root,'android/assets/app.js'),'utf8');
 vm.runInContext(source.replace(/\}\)\(\);\s*$/,`window.testApp={home,quoteReference,cupEquivalent,quoteCard,projection,refreshQuote,maybeRefreshQuote,setState(v){state=v;},finishQuoteRequest};})();`),context);
 context.testApp.setState(state);
 return {api:context.testApp,calls:pendingCalls,context};
}

function sample(){return {activeProfileId:'person',paired:true,online:true,today:'2026-10-27',profile:{name:'Prueba'},operations:[],planning:{quote:{rate:870,consultedAt:'2026-10-28T02:55:00.000Z',day:'2026-10-27',source:'elTOQUE'}},snapshot:{products:[],month:'2026-10',income:350,savings:175,spent:20,consumptionBudget:133,balances:{usd:300,cup:6000,cupCash:5000,cupTransfer:1000},rate:870,expectedIncome:350,serverAt:'before'}};}

test('todas las tarjetas usan la misma referencia y conservan los valores USD',()=>{
 const state=sample(),before=JSON.stringify(state),{api}=dashboard(state),html=api.home('Prueba');
 assert.equal(api.quoteReference().rate,870);
 for(const amount of [175,350,20,113,300])assert.ok(html.includes(api.cupEquivalent(amount)),String(amount));
 assert.ok(html.includes('1 USD ='));
 assert.ok(html.includes('Actualizar tasa del dólar'));
 assert.equal(JSON.stringify(state),before,'renderizar no modifica saldos ni operaciones');
});

test('offline y tasa sin verificar se etiquetan sin inventar fecha actual',()=>{
 const state=sample();state.online=false;state.planning.quote.consultedAt='2026-10-26T00:00:00Z';
 const {api,calls}=dashboard(state);
 assert.equal(api.quoteReference().fresh,false);
 assert.match(api.quoteCard(),/Sin conexión/);
 assert.match(api.quoteCard(),/disabled/);
 api.refreshQuote(true);assert.equal(calls.length,0);
 state.online=true;state.planning.quote={rate:0};state.snapshot.rate=0;
 assert.match(api.cupEquivalent(350),/referencia pendiente/);
 assert.doesNotMatch(api.quoteCard(),/NaN|Infinity/);
});

test('actualización manual pide sincronización; no registra movimientos nuevos',()=>{
 const state=sample(),{api,calls,context}=dashboard(state);
 api.refreshQuote(true);
 assert.equal(calls.length,1);assert.equal(calls[0].action,'sync');
 assert.match(api.quoteCard(),/Consultando/);
 api.refreshQuote(true);assert.equal(calls.length,1,'una sola consulta simultánea');
 context.hogarReply(calls[0].id,{ok:true,data:{}});
});

test('actualización automática solo con sesión, conexión y referencia vencida',()=>{
 const state=sample(),{api,calls}=dashboard(state);
 api.maybeRefreshQuote();assert.equal(calls.length,0,'una referencia reciente se conserva');
 state.planning.quote.consultedAt='2026-10-28T01:00:00Z';
 api.maybeRefreshQuote();assert.equal(calls[0].action,'sync');
 const offline=sample();offline.online=false;offline.planning.quote.consultedAt='';
 const second=dashboard(offline);second.api.maybeRefreshQuote();assert.equal(second.calls.length,0);
});

function backend(){
 const properties=new Map(),writes=[],settings={B7:870,B26:'Sí',B11:''};let fetches=0,fail=false;
 const props={getProperty:k=>properties.get(k)||null,setProperty(k,v){properties.set(k,v);return this;},deleteProperty:k=>properties.delete(k)};
 const sheet={getRange(address){return {getValue:()=>settings[address],setValue(v){settings[address]=v;writes.push(address);}};}};
 const ss={getSheetByName(name){assert.equal(name,'Ajustes');return sheet;}};
 const blob=b=>({bytes:Buffer.from(b),getDataAsString:()=>Buffer.from(b).toString('utf8')});
 const context={Date:FixedDate,console,PropertiesService:{getScriptProperties:()=>props},
  Utilities:{base64Decode:x=>Buffer.from(x,'base64'),newBlob:blob,ungzip:b=>blob(zlib.gunzipSync(b.bytes))},
  UrlFetchApp:{fetch(){fetches++;return {getResponseCode:()=>fail?503:200,getContentText:()=>'<table><tr><td>1 USD</td><td>900 CUP</td></tr></table>'};}}};
 vm.createContext(context);vm.runInContext(fs.readFileSync(path.join(root,'backend/Code.gs'),'utf8'),context);
 context.movilFecha_=()=> '2026-10-27';context.historialPrecios_=()=>({});context.registrarRevision_=()=>{};
 return {api:context,ss,properties,writes,settings,get fetches(){return fetches;},set fail(v){fail=v;}};
}

test('backend renueva una tasa del mismo día y limita consultas a un minuto',()=>{
 const b=backend();b.properties.set('HOGAR_TASA_VERIFICADA',JSON.stringify({rate:870,consultedAt:'2026-10-28T02:00:00Z',day:'2026-10-27'}));
 b.api.revisarTasaParaApp_(b.ss);assert.equal(b.fetches,1);assert.equal(b.settings.B7,900);
 assert.equal(b.api.tasaParaPlan_(b.ss).fresh,true);
 b.api.revisarTasaParaApp_(b.ss);assert.equal(b.fetches,1);
 assert.deepEqual(b.writes,['B7','B11'],'no se escribe en movimientos ni cambios históricos');
});

test('fallo conserva la tasa y su fecha, y expone error; respeta desactivación',()=>{
 const b=backend();b.fail=true;
 const previous={rate:870,consultedAt:'2026-10-28T02:55:00Z',day:'2026-10-27',source:'elTOQUE'};
 b.properties.set('HOGAR_TASA_VERIFICADA',JSON.stringify(previous));
 b.api.revisarTasaParaApp_(b.ss);
 assert.equal(b.settings.B7,870);assert.equal(b.api.tasaParaPlan_(b.ss).consultedAt,previous.consultedAt);
 assert.equal(b.api.tasaParaPlan_(b.ss).fresh,false);assert.match(b.api.tasaParaPlan_(b.ss).error,/503/);
 b.settings.B26='No';b.properties.delete('HOGAR_TASA_INTENTO');b.api.revisarTasaParaApp_(b.ss);assert.equal(b.fetches,1);
});

test('referencia manual o corrupta no se presenta como consultada recientemente',()=>{
 const b=backend();b.properties.set('HOGAR_TASA_VERIFICADA','{');
 assert.equal(b.api.tasaParaPlan_(b.ss).fresh,false);
 b.properties.set('HOGAR_TASA_VERIFICADA',JSON.stringify({rate:880,consultedAt:new FixedDate().toISOString(),day:'2026-10-27'}));
 assert.equal(b.api.tasaParaPlan_(b.ss).rate,870);
 assert.equal(b.api.tasaParaPlan_(b.ss).consultedAt,'');
});
