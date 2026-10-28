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
 const properties=new Map(),writes=[],settings={B7:870,B26:'Sí',B11:''};let fetches=0,fail=false,body='<table><tr><td>1 USD</td><td>900 CUP</td></tr></table>',request=null,status=200;
 const props={getProperty:k=>properties.get(k)||null,setProperty(k,v){properties.set(k,v);return this;},deleteProperty:k=>properties.delete(k)};
 const sheet={getRange(address){return {getValue:()=>settings[address],setValue(v){settings[address]=v;writes.push(address);}};}};
 const ss={getSheetByName(name){assert.equal(name,'Ajustes');return sheet;}};
 const blob=b=>({bytes:Buffer.from(b),getDataAsString:()=>Buffer.from(b).toString('utf8')});
 const context={Date:FixedDate,console,PropertiesService:{getScriptProperties:()=>props},
  Utilities:{base64Decode:x=>Buffer.from(x,'base64'),newBlob:blob,ungzip:b=>blob(zlib.gunzipSync(b.bytes))},
  UrlFetchApp:{fetch(url,options){fetches++;request={url,options};return {getResponseCode:()=>fail?503:status,getContentText:()=>body};}}};
 vm.createContext(context);vm.runInContext(fs.readFileSync(path.join(root,'backend/Code.gs'),'utf8'),context);
 context.movilFecha_=()=> '2026-10-27';context.historialPrecios_=()=>({});context.registrarRevision_=()=>{};
 return {api:context,ss,properties,writes,settings,get fetches(){return fetches;},set fail(v){fail=v;},set body(v){body=v;},set status(v){status=v;},get request(){return request;}};
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

test('lector elTOQUE admite tablas sin el prefijo 1 y datos TRMI, y rechaza referencias ambiguas',()=>{
 const b=backend();
 assert.equal(b.api.extraerTasa_('<table><tr><td>USD</td><td>900,00 CUP</td></tr></table>'),900);
 assert.equal(b.api.extraerTasa_('<tr><td>1 USD</td><td>1,200.00 CUP</td></tr>'),1200);
 assert.equal(b.api.extraerTasa_('<tr><td>USD</td><td>1.200,00 CUP</td></tr>'),1200);
 assert.throws(()=>b.api.extraerTasa_('<tr><td>2 USD</td><td>1800 CUP</td></tr>'),/No se encontró/);
 assert.throws(()=>b.api.extraerTasa_('<tr><td>1 USD</td><td>-900 CUP</td></tr>'),/No se encontró/);
 assert.equal(b.api.extraerTasa_('<script id="__NEXT_DATA__" type="application/json">{"props":{"tasas":{"USD":901,"ECU":950,"USDT":999}}}</script>'),901);
 assert.throws(()=>b.api.extraerTasa_('{"tasas":{"USDT":999}}'),/No se encontró/);
 assert.throws(()=>b.api.extraerTasa_('<tr><td>1 USD</td><td>900 CUP</td></tr><tr><td>1 USD</td><td>950 CUP</td></tr>'),/varias referencias/);
});

test('el tope se controla por compra y permite dos compras del mismo día',()=>{
 const b=backend(),device='phone',a='123e4567-e89b-42d3-a456-426614174000',id='123e4567-e89b-42d3-a456-426614174001';
 const old={id:a,date:'2026-10-27',limit:4000,currency:'CUP',rate:870,charged:370};
 const row=Array(22).fill('');row[14]=device;row[21]=JSON.stringify(old);
 let rows=[row];const ss={getSheetByName:()=>({getRange:()=>({getValues:()=>rows})})};
 const op={type:'Gasto',deviceId:device,date:'2026-10-27',currency:'CUP',amount:150,shopping:{id,date:'2026-10-27',limit:1000,currency:'CUP',rate:900}};
 assert.equal(b.api.validarCompraModo_(ss,op).charged,150);
 const second=Array(22).fill('');second[14]=device;second[21]=JSON.stringify({...op.shopping,charged:900});rows=[row,second];
 assert.throws(()=>b.api.validarCompraModo_(ss,op),/supera el tope/);
 op.shopping.limit=2000;
 assert.throws(()=>b.api.validarCompraModo_(ss,op),/otra fecha, moneda, tope o tasa/);
});

test('ubicación opcional conserva el lugar sin GPS y valida coordenadas sin afectar importes',()=>{
 const b=backend();
 const manual=b.api.ubicacionCompra_({place:'Mercado',status:'permission_denied'});
 assert.equal(manual.place,'Mercado');assert.equal(manual.latitude,null);assert.equal(manual.status,'permission_denied');
 const zero=b.api.celdasUbicacionCompra_({place:'Prueba',status:'captured',latitude:0,longitude:0,accuracy:5,capturedAt:'2026-10-27T10:00:00Z'});
 assert.equal(zero[1],0);assert.equal(zero[2],0);assert.match(zero[6],/openstreetmap/);
 const bad=b.api.ubicacionCompra_({place:'Tienda',latitude:999,longitude:-82,status:'captured'});
 assert.equal(bad.latitude,null);assert.equal(bad.place,'Tienda');
 const protectedCell=b.api.celdasUbicacionCompra_({place:'=IMPORTXML("bad")',status:'disabled'});
 assert.ok(protectedCell[0].startsWith("'="));
 assert.equal(b.api.ubicacionCompra_(null),null);
});

test('API oficial usa token privado en cabecera y no lo reenvía por redirecciones',()=>{
 const b=backend(),credential='fixture-only-no-network';b.properties.set('ELTOQUE_API_TOKEN',credential);b.body='{"tasas":{"USD":902.25}}';
 b.api.revisarTasaParaApp_(b.ss);
 assert.equal(b.settings.B7,902.25);assert.equal(b.request.url,'https://tasas.eltoque.com/v1/trmi');
 assert.equal(b.request.options.headers.Authorization,'Bearer '+credential);
 assert.equal(b.request.options.followRedirects,false);
 const quote=b.api.tasaParaPlan_(b.ss);assert.match(quote.source,/API oficial/);assert.ok(!JSON.stringify(quote).includes(credential));
});

test('HTTP 403 se explica sin inventar una tasa ni revelar la credencial',()=>{
 const b=backend();b.status=403;b.api.revisarTasaParaApp_(b.ss);
 assert.equal(b.settings.B7,870);assert.match(b.api.tasaParaPlan_(b.ss).error,/HTTP 403/);assert.match(b.api.tasaParaPlan_(b.ss).error,/ELTOQUE_API_TOKEN/);
 const api=backend(),credential='fixture-only-no-network';api.properties.set('ELTOQUE_API_TOKEN',credential);api.status=401;
 api.api.revisarTasaParaApp_(api.ss);assert.equal(api.settings.B7,870);assert.ok(!JSON.stringify(api.api.tasaParaPlan_(api.ss)).includes(credential));
 api.properties.delete('HOGAR_TASA_INTENTO');api.api.UrlFetchApp.fetch=()=>{throw new Error('detalle interno '+credential);};
 api.api.revisarTasaParaApp_(api.ss);assert.ok(!JSON.stringify(api.api.tasaParaPlan_(api.ss)).includes(credential));
});
