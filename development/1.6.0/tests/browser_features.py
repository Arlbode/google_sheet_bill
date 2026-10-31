"""Interfaz 1.6.0 con puente simulado, datos ficticios y cero conexiones al Sheet."""
from pathlib import Path
import json,threading,importlib.util
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from functools import partial
from playwright.sync_api import sync_playwright,expect
R=Path(__file__).resolve().parents[1]
class Quiet(SimpleHTTPRequestHandler):
 def log_message(self,*args):pass
server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(R/'assets')));threading.Thread(target=server.serve_forever,daemon=True).start()
catalog=json.loads((R/'assets/catalog.json').read_text());catalog['products'].append({'id':'fixture-potato','name':'Papa (patata)','category':'Complementarios','unit':'lb','currency':'CUP','price':None})
fixture={'activeProfileId':'p','paired':True,'online':True,'today':'2026-10-30','profile':{'name':'Persona'},'profiles':[{'id':'p','name':'Persona'}],'operations':[{'id':'test-op','type':'Gasto','status':'PENDING','actorId':'p','date':'2026-10-29','currency':'CUP','amount':200,'rate':500,'items':[{'productId':'Pimiento','name':'Pimiento','unitCode':'lb','unitPrice':100,'quantity':2}]}],'cart':[],'pending':1,'planPending':0,'deviceName':'Prueba','officialRate':{'configured':True},'snapshot':{'products':catalog['products'],'categories':catalog['categories'],'spent':100,'consumptionBudget':300,'income':350,'savings':100,'rate':500,'month':'2026-10','balances':{'usd':100,'cup':5000}}}
script='''window.testState=STATE;window.features={products:[],offers:[],shared:[],queue:[],conflicts:[],householdId:'home'};window.calls=[];
window.HogarNative={call(text){let q=JSON.parse(text),d={},err=null;calls.push(q);if(q.action==='state')d=testState;
else if(q.action==='featuresGet')d=features;
else if(q.action==='featuresSave'){Object.assign(features,q.data);d=features;}
else if(q.action==='featuresShared'){features.queue.push({id:'change',item:q.data.item,entityId:q.data.item.id,baseRevision:q.data.baseRevision});d=features;}
else if(q.action==='featuresSync'){features.shared=features.queue.map(o=>({...o.item,revision:1,actorName:'Persona'}));features.queue=[];d=features;}
else if(q.action==='featuresDiscard'){features.queue=features.queue.filter(o=>o.id!==q.data.id);d=features;}
else if(q.action==='featuresResolve'){features.conflicts=[];d=features;}
else if(q.action==='featuresBackup'||q.action==='featuresVerify'||q.action==='featuresImage')d={opened:true};
setTimeout(()=>window.hogarReply(q.id,err?{ok:false,error:err}:{ok:true,data:JSON.parse(JSON.stringify(d))}),0);}};'''.replace('STATE',json.dumps(fixture))
try:
 with sync_playwright() as pw:
  browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=browser.new_context(viewport={'width':390,'height':844},locale='es-ES',timezone_id='America/Havana');ctx.add_init_script(script);page=ctx.new_page();errors=[];page.on('pageerror',lambda e:errors.append(str(e)));page.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html');expect(page.locator('[data-tab="profile"]')).to_be_visible()
  def go(tab):
   if page.locator('#closeModal').count():page.locator('#closeModal').click()
   page.locator('.nav [data-tab="'+tab+'"]').click()
  def choose(name):page.locator('[data-feature="'+name+'"]').first.click()
  go('profile');assert page.locator('[data-feature="dashboard"]').count()==0;assert page.locator('[data-feature="backup"]').count()==1
  assert page.locator('.content [data-action="planning"]').count()==0;assert page.locator('.content [data-action="testProtection"]').count()==0
  page.locator('[data-action="permissions"]').click();expect(page.locator('#overlay')).to_contain_text('Avisos de necesidad de CUP');expect(page.locator('#overlay')).to_contain_text('Ubicación en las compras');page.locator('#closeModal').click()
  choose('backup');expect(page.locator('#overlay')).to_contain_text('Último archivo guardado');choose('backupCreate');page.wait_for_timeout(50);assert page.evaluate("calls.some(q=>q.action==='featuresBackup')")
  go('home');expect(page.locator('.content')).to_contain_text('Estimado al cierre');assert 'Envío al Sheet' not in page.locator('.content').inner_text();assert 'Confirmaciones, errores y reintentos' not in page.locator('.content').inner_text()
  assert page.locator('.content > section').last.locator('h2').inner_text()=='Previsión del mes'
  register=page.get_by_role('heading',name='Registrar',exact=True);wallet=page.get_by_role('heading',name='Saldos por moneda',exact=True);assert register.bounding_box()['y']<wallet.bounding_box()['y']
  free=page.locator('[data-feature="freeDaily"]');assert free.locator('xpath=..').get_by_role('heading',name='Saldos por moneda').count()==1
  before=page.evaluate('JSON.stringify({operations:testState.operations,cart:testState.cart,features:features})');start=page.evaluate('calls.length');choose('freeDaily');expect(page.locator('#overlay')).to_contain_text('Paso 1 de 3');expect(page.locator('#overlay')).to_contain_text('CUP efectivo');choose('freeNext');expect(page.locator('#overlay')).to_contain_text('Paso 2 de 3');choose('freeBack');expect(page.locator('#overlay')).to_contain_text('Paso 1 de 3');choose('freeNext');choose('freeNext');expect(page.locator('#overlay')).to_contain_text('Paso 3 de 3');expect(page.locator('#overlay')).to_contain_text('Disponible para consumo');choose('returnScreen');assert page.locator('#overlay [role="dialog"]').count()==0;assert before==page.evaluate('JSON.stringify({operations:testState.operations,cart:testState.cart,features:features})');assert page.evaluate('calls.length')==start
  go('history');page.locator('#historyProduct').select_option('Pimiento');expect(page.locator('#priceHistoryResult')).to_contain_text('100,00 CUP');assert page.locator('#overlay [role="dialog"]').count()==0
  go('shop');assert page.locator('.content > section').last.get_attribute('id')=='householdList';choose('compare');page.locator('#fcPriceA').fill('100');page.locator('#fcPriceB').fill('200');page.locator('#fcUnitB').select_option('kg');page.locator('#submitForm').click();expect(page.locator('#fcResult')).to_contain_text('B cuesta menos')
  go('shop');page.locator('#search').fill('Papa');expect(page.locator('.product')).to_have_count(1);photo=page.locator('.product img');expect(photo).to_have_attribute('src','product-papa.png');assert photo.evaluate('(x)=>x.complete && x.naturalWidth>0');page.screenshot(path='/workspace/shared/downloads/Papa_1.6.4.png');page.locator('#search').fill('');choose('productNew');page.locator('#fpName').fill('Producto prueba');page.locator('#fpPrice').fill('150');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('Producto guardado');assert page.evaluate("features.products.some(p=>p.name==='Producto prueba')")
  go('shop');page.locator('#search').fill('Producto prueba');page.locator('.product [data-feature="productEdit"]').click();page.locator('#fpName').fill('Producto corregido');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('Producto guardado');assert page.evaluate('features.products.length')==1;choose('productImage');page.wait_for_timeout(50);assert page.evaluate("calls.some(q=>q.action==='featuresImage')")
  go('shop');choose('productNew');page.locator('#fpName').fill('Pimiento');page.locator('#submitForm').click();expect(page.locator('#toast')).to_contain_text('Ese nombre ya existe');assert page.evaluate('features.products.length')==1
  go('shop');page.locator('#householdList summary').first.click();choose('sharedAdd');page.locator('#fsName').fill('Zanahoria');page.locator('#fsQty').fill('2');page.locator('#submitForm').click();expect(page.locator('#householdList')).to_contain_text('Zanahoria');assert page.locator('#overlay [role="dialog"]').count()==0;choose('sharedSync');expect(page.locator('#householdList')).to_contain_text('0 cambio(s) pendientes');assert page.evaluate('testState.operations.length')==1
  page.locator('#householdOffers summary').click();choose('offerAdd');page.locator('#foProduct').fill('<img src=x onerror=alert(1)>');page.locator('#foPrice').fill('100');page.locator('#foUrl').fill('https://www.revolico.com/item/prueba');page.locator('#submitForm').click();expect(page.locator('#householdOffers')).to_contain_text('<img src=x onerror=alert(1)>');assert page.locator('#householdOffers img').count()==0
  page.locator('#search').fill('Zanahoria');expect(page.locator('.product')).to_have_count(1);page.locator('.product [data-product]').click();expect(page.locator('#overlay')).to_contain_text('Zanahoria');assert page.evaluate('testState.operations.length')==1
  go('history');page.evaluate("testState.operations[0].concept='Zanahoria y ají pimiento para la compra de la semana';testState.operations[0].status='SYNCED';window.hogarChanged()");page.wait_for_timeout(350)
  for width in [320,360,390]:
   page.set_viewport_size({'width':width,'height':844});go('history');row=page.locator('[data-operation="test-op"]');expect(row).to_contain_text('Zanahoria y ají pimiento');badge=row.locator('.badge');expect(badge).to_have_text('Confirmado');assert badge.bounding_box()['width']<85;assert row.locator('.row-body').bounding_box()['width']>width*.60
   for tab in ['home','shop','history','profile']:
    go(tab);assert not page.evaluate('document.documentElement.scrollWidth>innerWidth'),(width,tab)
    if width==390:page.screenshot(path='/workspace/shared/downloads/Inicio_Compras_1.6.4_'+tab+'.png',full_page=True)
  go('home');choose('freeDaily');page.screenshot(path='/workspace/shared/downloads/Libre_Dia_1.6.4.png');go('shop');page.evaluate("testState.shopping={active:true,session:{id:'fictional-session',limit:5000,currency:'CUP',rate:500},lines:[],spent:0,remaining:5000};window.hogarChanged()");page.wait_for_timeout(350);page.locator('[data-action="shoppingResume"]').click();assert page.locator('.content > section').last.get_attribute('id')=='householdList';expect(page.locator('[data-feature="compare"]')).to_be_visible();page.locator('[data-action="shoppingExit"]').click();assert page.locator('.content > section').last.get_attribute('id')=='householdList';assert page.evaluate('testState.operations.length')==1
  go('profile');page.evaluate("testState.updates={available:true,latestVersion:'1.6.5',installedVersion:'1.6.4',bytes:900000,error:'Read error: BAD_DECRYPT'};window.hogarChanged()");page.wait_for_timeout(350);page.locator('[data-action="updates"]').first.click();page.locator('[data-action="updateDownload"]').click();page.wait_for_timeout(150);assert page.evaluate("calls.some(q=>q.action==='featuresUpdateDownload')");assert not page.evaluate("calls.some(q=>q.action==='updateDownload')");page.locator('[data-action="updateBrowser"]').click();page.wait_for_timeout(50);assert page.evaluate("calls.some(q=>q.action==='featuresUpdateBrowser')")
  assert not errors,errors
  browser.close();print('PASS: mejoras integradas en Inicio/Comprar/Movimientos/Perfil; edición, foto, comparación, cola sin gastos, ofertas escapadas, respaldo y 320/360/390px.')
finally:server.shutdown();server.server_close()
