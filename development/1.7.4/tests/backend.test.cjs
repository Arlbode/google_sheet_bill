const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),zlib=require('node:zlib'),crypto=require('node:crypto');
const source=fs.readFileSync(process.argv[2],'utf8'),c={Utilities:{base64Decode:s=>Buffer.from(s,'base64'),newBlob:b=>({bytes:Buffer.from(b),getDataAsString:()=>Buffer.from(b).toString()}),ungzip:b=>({getDataAsString:()=>zlib.gunzipSync(b.bytes).toString()}),formatDate:d=>new Date(d).toISOString().slice(0,10),parseDate:d=>new Date(d+'T12:00:00Z'),computeDigest:(_,s)=>[...crypto.createHash('sha256').update(s).digest()],DigestAlgorithm:{SHA_256:1},Charset:{UTF_8:1}},Session:{getScriptTimeZone:()=> 'America/Havana'}};
vm.createContext(c);vm.runInContext(source,c);
let n=0;function check(f){f();n++;}
c.fondosNombres153_=()=>['Fondo casa'];c.ledgerReservas153_=()=>[{type:'Aporte ahorro',category:'Ahorro general',currency:'USD',amount:'410'},{type:'Aporte fondo',category:'Fondo casa',currency:'CUP',paymentFormat:'Transferencia',amount:'50'}];c.saldosMoneda_=()=>({usd:410,cupCash:0,cupTransfer:50});
for(const currency of ['USD','CUP'])for(const paymentFormat of ['Efectivo','Transferencia'])check(()=>c.validarReserva153_({}, {type:'Gasto',currency,paymentFormat,amount:999999}));
check(()=>c.validarReserva153_({}, {type:'Gasto',currency:'CUP',paymentFormat:'Transferencia',fundCategory:'Fondo casa',amount:200}));
check(()=>assert.throws(()=>c.validarReserva153_({}, {type:'Retiro ahorro',currency:'USD',amount:411}),e=>e.code==='AHORRO_INSUFICIENTE'));
for(const type of ['Cambio','Aporte ahorro','Aporte fondo'])check(()=>assert.throws(()=>c.validarReserva153_({}, {type,currency:'USD',amount:20}),e=>e.code==='RESERVA_PROTEGIDA'));
const ss={getSheetByName(name){return {getRange(range){return {getValue:()=> '2026-01-01',getValues:()=>name==='Presupuesto'?[['Canasta básica','Consumo'],['Fondo casa','Fondo']]:[]};}}}};
c.reservasPreparadas153_=()=>true;
const device={id:'11111111-1111-4111-8111-111111111111'},actor='22222222-2222-4222-8222-222222222222',profiles={[actor]:{id:actor,name:'Ana',active:true}};
const base={id:'33333333-3333-4333-8333-333333333333',deviceId:device.id,actorId:actor,actorName:'Ana',type:'Gasto',category:'Canasta básica',date:'2026-11-01',currency:'USD',amount:20,rate:'',concept:'Compra ficticia',clientCreatedAt:'2026-11-01T12:00:00Z',items:[],paymentFormat:'',fundCategory:''};
check(()=>assert.equal(c.movilValidar_(ss,device,profiles,base).amount,20));
check(()=>assert.equal(c.movilValidar_(ss,device,profiles,{...base,currency:'CUP',rate:910,paymentFormat:'Transferencia',fundCategory:'Fondo casa',amount:200}).fundCategory,'Fondo casa'));
for(const [change,code] of [[{deviceId:'otro'},'DISPOSITIVO_INVALIDO'],[{actorId:'otro'},'PERFIL_INACTIVO'],[{actorName:'Otro'},'PERFIL_MODIFICADO'],[{currency:'EUR'},'MONEDA_INVALIDA'],[{amount:-1},'IMPORTE_INVALIDO'],[{amount:1.001},'IMPORTE_INVALIDO'],[{date:'2050-01-01'},'FECHA_INVALIDA'],[{currency:'CUP',rate:0},'TASA_INVALIDA'],[{category:'inexistente'},'CATEGORIA_INVALIDA'],[{fundCategory:'falso'},'FONDO_INVALIDO'],[{currency:'CUP',rate:910,paymentFormat:'falso'},'FORMATO_INVALIDO'],[{items:[{name:'Arroz',quantity:2,unitPrice:1}]},'TOTAL_INVALIDO']])check(()=>assert.throws(()=>c.movilValidar_(ss,device,profiles,{...base,...change}),e=>e.code===code));
// El límite de jornada y su identidad siguen en vigor; los gastos no borran topes.
const shopping={id:'44444444-4444-4444-8444-444444444444',date:base.date,currency:'USD',rate:910,limit:10};
check(()=>assert.throws(()=>c.movilValidar_(ss,device,profiles,{...base,shopping}),e=>e.code==='TOPE_COMPRA'));
check(()=>assert.equal(c.movilValidar_(ss,device,profiles,{...base,amount:5,shopping}).shopping.charged,5));
// El recibo se reconoce antes de validar saldos: el mismo UUID nunca crea otra fila.
const raw={...base},hash=c.movilHash_(c.movilEstable_(raw)),index={[base.id]:{sheet:'Registro',row:6,hash}};let audits=0;c.movilAuditarAceptado_=()=>audits++;
check(()=>assert.equal(c.movilAplicar_(ss,device,profiles,raw,index).status,'duplicate'));
check(()=>assert.throws(()=>c.movilAplicar_(ss,device,profiles,{...raw,amount:21},index),e=>e.code==='ID_CONFLICTO'));
check(()=>assert.equal(audits,1));
console.log('PASS backend real: '+n+' comprobaciones; gastos aceptados con reservas/saldos insuficientes, validación y recibos conservados.');
