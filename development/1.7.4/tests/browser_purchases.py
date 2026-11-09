"""Flujos de compra con puente simulado; importes ficticios y navegador móvil."""
from pathlib import Path
import json,threading
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from functools import partial
from playwright.sync_api import sync_playwright,expect
BASE=Path(__file__).resolve().parents[2]/'1.6.0/assets'
class Quiet(SimpleHTTPRequestHandler):
 def log_message(self,*args):pass
 def guess_type(self,path):return 'image/svg+xml' if path.endswith('.png') else super().guess_type(path)
server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(BASE)));threading.Thread(target=server.serve_forever,daemon=True).start()
catalog=json.loads((BASE/'catalog.json').read_text());products=catalog['products']
def line(id,format):return dict(id=id,productId='Arroz',name='Arroz',category='Canasta básica',quantity='3',unit='lb',unitCode='lb',unitPrice='200',currency='CUP',rate='910',paymentFormat=format,planId='')
fixture=dict(activeProfileId='ana',profile={'id':'ana','name':'Ana'},profiles=[{'id':'ana','name':'Ana'}],paired=True,online=False,today='2026-11-09',operations=[],cart=[line('a','Efectivo'),line('b','Transferencia')],shopping={'active':False},snapshot=dict(month='2026-11',balances={'usd':410,'cup':0,'cupCash':0,'cupTransfer':0},reserveVersion=1,reserveLedger=[{'id':'initial','type':'Aporte ahorro','category':'Ahorro general','currency':'USD','amount':'410'}],fundCategories=['Fondo casa'],expenseCategories=['Canasta básica','Complementarios'],rate=910,products=products,categories=catalog['categories'],consumptionBudget=100,categoryBudgets=[]))
script='''window.testState=STATE;window.testFeatures={householdId:'home',products:[],offers:[],shared:[{id:'11111111-1111-4111-8111-111111111111',name:'Arroz',quantity:3,unit:'lb',revision:1,bought:false,deleted:false}],queue:[],conflicts:[]};window.calls=[];window.receipts={};
window.HogarNative={call(text){let q=JSON.parse(text),d={},err=null;calls.push(q);let x=q.data||{};
if(q.action==='state')d=testState;else if(['featuresGet','featuresSync'].includes(q.action))d=testFeatures;
else if(q.action==='featuresShared'){let i=testFeatures.shared.find(i=>i.id===x.item.id);Object.assign(i,x.item);d=testFeatures;}
else if(q.action==='cart'){testState.cart=x.items;d=testState;}
else if(['featuresPurchaseCart','featuresPurchaseShared'].includes(q.action)){
 if(!receipts[x.requestId]){receipts[x.requestId]=true;let l=x.line;testState.operations.push({id:x.requestId,type:'Gasto',status:'PENDING',category:l.category,currency:l.currency,paymentFormat:l.paymentFormat,amount:Number(l.quantity)*Number(l.unitPrice),date:x.date,rate:l.rate,concept:l.name,actorId:'ana',items:[l]});
 if(q.action==='featuresPurchaseCart')testState.cart=testState.cart.flatMap(i=>i.id!==x.lineId?[i]:Number(i.quantity)>Number(l.quantity)?[{...i,quantity:String(Number(i.quantity)-Number(l.quantity))}]:[]);
 else {let i=testFeatures.shared.find(i=>i.id===x.sharedItem.id);if(Number(l.quantity)<Number(i.quantity))i.quantity-=Number(l.quantity);else i.bought=true;}}
 d={...testState,savedCount:1,expenseWarnings:['El gasto supera el saldo registrado.'],expenseFeatures:testFeatures};}
else if(q.action==='save'){if(!testState.operations.some(i=>i.id===x.id))testState.operations.push({...x,status:'PENDING'});d={...testState,savedCount:1,expenseFeatures:testFeatures,expenseWarnings:['Gasto guardado aunque afecte reservas.']};}
else if(q.action==='guideRead')d={};
setTimeout(()=>window.hogarReply(q.id,err?{ok:false,error:err}:{ok:true,data:JSON.parse(JSON.stringify(d))}),0);}};'''.replace('STATE',json.dumps(fixture))
try:
 with sync_playwright() as pw:
  browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=browser.new_context(viewport={'width':390,'height':844},locale='es-ES',timezone_id='America/Havana');ctx.add_init_script(script);p=ctx.new_page();errors=[];p.on('pageerror',lambda e:errors.append(str(e)));p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html');expect(p.locator('.nav [data-tab="shop"]')).to_be_visible()
  def go(tab):
   if p.locator('#closeModal').count():p.locator('#closeModal').click()
   p.locator('.nav [data-tab="'+tab+'"]').click()
  def cart():go('shop');p.locator('[data-action="cart"]').first.click()
  cart();expect(p.locator('.buyCartLine')).to_have_count(2);p.locator('.buyCartLine').first.click();expect(p.locator('#overlay')).to_contain_text('Registrar compra · Arroz');expect(p.locator('#buyAdvice')).to_contain_text('Puedes registrar este gasto');expect(p.locator('#buyQty')).to_have_value('3');p.locator('#buyQty').fill('1');p.locator('#buyPrice').fill('250');expect(p.locator('#buyTotal')).to_contain_text('250,00 CUP');p.locator('#submitForm').dblclick();expect(p.locator('.buyCartLine')).to_have_count(2);assert p.evaluate('testState.operations.length')==1;assert p.evaluate('testState.cart[0].quantity')=='2';assert p.evaluate('testState.cart[1].quantity')=='3';assert p.evaluate("calls.filter(q=>q.action==='featuresPurchaseCart').length")==1
  # Compra el otro artículo en transferencia; se conserva el primer pendiente.
  p.locator('.buyCartLine').nth(1).click();expect(p.locator('#buyPayment')).to_have_value('Transferencia');p.locator('#submitForm').click();expect(p.locator('.buyCartLine')).to_have_count(1);assert p.evaluate('testState.operations.length')==2;assert p.evaluate('testState.operations[1].paymentFormat')=='Transferencia'
  # Terminar el último artículo cierra el formulario sin una cesta vacía atascada.
  p.locator('.buyCartLine').first.click();p.locator('#submitForm').click();expect(p.locator('#overlay')).to_be_empty();assert p.evaluate('testState.cart.length')==0
  cart();p.locator('[data-feature="shared"]').click();expect(p.locator('.shared-line')).to_have_count(1);img=p.locator('.shared-line img');assert img.evaluate('(x)=>x.complete&&x.naturalWidth>0');p.locator('[data-feature="sharedPurchase"]').click();expect(p.locator('#buyQty')).to_have_value('3');p.locator('#buyQty').fill('1');p.locator('#buyPrice').fill('200');p.locator('#buyPayment').select_option('Transferencia');p.locator('#submitForm').click();expect(p.locator('.shared-line')).to_contain_text('2 lb');assert p.evaluate('testState.operations.length')==4;assert not p.evaluate('testFeatures.shared[0].bought');last=p.evaluate("calls.filter(q=>q.action==='featuresPurchaseShared').slice(-1)[0].data");assert last['sharedItem']['quantity']==3;assert last['line']['quantity']=='1';assert last['line']['paymentFormat']=='Transferencia'
  p.locator('[data-feature="sharedPurchase"]').click();p.locator('#buyPrice').fill('220');p.locator('#submitForm').click();expect(p.locator('#sharedPending')).to_contain_text('0');p.locator('[data-feature="sharedBought"]').click();expect(p.locator('.shared-line')).to_have_count(1);expect(p.locator('.shared-line')).to_contain_text('Arroz');assert p.evaluate('testState.operations.length')==5
  # Marcar pendiente/comprado solo cambia la lista; no crea dinero.
  p.locator('[data-feature="sharedToggle"]').click();p.locator('[data-feature="sharedPending"]').click();expect(p.locator('.shared-line')).to_have_count(1);p.locator('[data-feature="sharedToggle"]').click();expect(p.locator('#sharedPending')).to_contain_text('0');assert p.evaluate('testState.operations.length')==5
  go('history');p.locator('[data-action="expense"]').click();p.locator('#moveCurrency').select_option('USD');p.locator('#moveAmount').fill('20');p.locator('#moveConcept').fill('Gasto real de prueba');expect(p.locator('#exchangePreview')).to_contain_text('afecta dinero que figura reservado');expect(p.locator('#submitForm')).to_be_enabled();p.locator('#submitForm').click();expect(p.locator('#overlay')).to_be_empty();assert p.evaluate('testState.operations.length')==6;assert p.evaluate('testState.operations[5].amount')=='20'
  # Vista compacta e imágenes legibles en teléfonos estrechos.
  for width in [320,360,390]:
   p.set_viewport_size({'width':width,'height':844});cart();p.locator('[data-feature="shared"]').click();p.locator('[data-feature="sharedBought"]').click();expect(p.locator('.shared-line img')).to_be_visible();assert not p.evaluate('document.documentElement.scrollWidth>innerWidth');assert p.locator('.modal').evaluate('(x)=>x.scrollWidth<=x.clientWidth')
   if width==390:p.screenshot(path='/workspace/shared/downloads/Hogar_1.7.4_Cesta_Compartida.png')
  assert not errors,errors
  browser.close();print('PASS browser: compras individuales y parciales, formato CUP, doble toque, cierre final, gasto protegido permitido, lista sin gasto automático, imágenes y 320/360/390px.')
finally:server.shutdown();server.server_close()
