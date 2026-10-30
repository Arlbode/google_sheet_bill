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
catalog=json.loads((R/'assets/catalog.json').read_text())
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
  browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=browser.new_context(viewport={'width':390,'height':844},locale='es-ES',timezone_id='America/Havana');ctx.add_init_script(script);page=ctx.new_page();errors=[];page.on('pageerror',lambda e:errors.append(str(e)));page.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html');expect(page.locator('[data-tab="profile"]')).to_be_visible();page.locator('[data-tab="profile"]').click();page.locator('[data-feature="dashboard"]').click()
  def choose(name):page.locator('#overlay [data-feature="'+name+'"]').click()
  def back():choose('dashboard')
  choose('backup');expect(page.locator('#overlay')).to_contain_text('Último archivo guardado');choose('backupCreate');page.wait_for_timeout(50);assert page.evaluate("calls.some(q=>q.action==='featuresBackup')");back()
  choose('sync');expect(page.locator('#overlay')).to_contain_text('Movimientos pendientes');expect(page.locator('#overlay')).to_contain_text('1');back()
  choose('prices');page.locator('[data-feature="pricesProduct"][data-id="Pimiento"]').click();expect(page.locator('#overlay')).to_contain_text('100,00 CUP');back()
  choose('compare');page.locator('#fcPriceA').fill('100');page.locator('#fcPriceB').fill('200');page.locator('#fcUnitB').select_option('kg');page.locator('#submitForm').click();expect(page.locator('#fcResult')).to_contain_text('B cuesta menos');back()
  choose('catalog');choose('productNew');page.locator('#fpName').fill('Producto prueba');page.locator('#fpPrice').fill('150');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('Producto guardado');assert page.evaluate("features.products.some(p=>p.name==='Producto prueba')");choose('catalog');page.get_by_role('button',name='Producto prueba',exact=True).click();page.locator('#fpName').fill('Producto corregido');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('Producto guardado');assert page.evaluate('features.products.length')==1;choose('catalog');choose('productNew');page.locator('#fpName').fill('Pimiento');page.locator('#submitForm').click();expect(page.locator('#toast')).to_contain_text('Ese nombre ya existe');assert page.evaluate('features.products.length')==1;back()
  choose('shared');choose('sharedAdd');page.locator('#fsName').fill('Zanahoria');page.locator('#fsQty').fill('2');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('1 cambio(s) pendientes');choose('sharedSync');expect(page.locator('#overlay')).to_contain_text('0 cambio(s) pendientes');assert page.evaluate('testState.operations.length')==1;back()
  choose('forecast');expect(page.locator('#overlay')).to_contain_text('Consumo estimado al cierre');back()
  choose('offers');choose('offerAdd');page.locator('#foProduct').fill('<img src=x onerror=alert(1)>');page.locator('#foPrice').fill('100');page.locator('#foUrl').fill('https://www.revolico.com/item/prueba');page.locator('#submitForm').click();expect(page.locator('#overlay')).to_contain_text('<img src=x onerror=alert(1)>');assert page.locator('#overlay img').count()==0;assert not page.evaluate('document.documentElement.scrollWidth>innerWidth');assert not errors,errors
  page.screenshot(path='/workspace/hogar-1.6-build/Ofertas.png',full_page=True)
  page.locator('#closeModal').click();page.locator('[data-tab="shop"]').click();page.locator('#search').fill('Zanahoria');expect(page.locator('.product')).to_have_count(1);assert int(page.locator('.product [data-product]').get_attribute('data-product'))>=0;page.locator('.product [data-product]').click();expect(page.locator('#overlay')).to_contain_text('Zanahoria');assert page.evaluate('testState.operations.length')==1;assert not errors,errors
  browser.close();print('PASS: ocho herramientas, catálogo sin duplicar, lista sin gastos, fórmulas, respaldo delegado, ofertas escapadas y ancho móvil.')
finally:server.shutdown();server.server_close()
