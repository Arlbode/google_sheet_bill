"""Carga ficticia grande y mediciones del JS real; no representa tiempos de un teléfono."""
from pathlib import Path
import json,sys
from playwright.sync_api import sync_playwright,expect
BASE=Path(__file__).resolve().parents[2]/'1.6.7/tests/browser_shared.py'
ns={'__file__':str(BASE)};exec(BASE.read_text().split('try:\n with sync_playwright()')[0],ns)
server=ns['server'];script=ns['script']
script=script.replace('window.testState=','window.historyCalls=0;window.priceScans=0;window.priceIndexes=0;window.renderCalls=0;window.testState=',1)
script+='''testState.online=false;testState.operations=Array.from({length:3000},(_,i)=>({id:'expense-'+i,type:'Gasto',status:'SYNCED',date:'2026-10-01',amount:100,currency:'CUP',rate:500,items:Array.from({length:5},(_,j)=>({productId:testState.snapshot.products[(i+j)%testState.snapshot.products.length].id,unitPrice:100+i%50,quantity:1,unitCode:'lb',currency:'CUP'}))}));'''
baseline='--baseline' in sys.argv
def instrument(route):
 body=route.fetch().text()
 if route.request.url.endswith('/app.js'):
  body=body.replace('function history(){','function history(){window.historyCalls++;',1).replace('function render(){','function render(){window.renderCalls++;',1)
  body=body.replace('Promise.all([fetch(',"window.perfRender=function(){var t=performance.now();render();return performance.now()-t;};Promise.all([fetch(",1)
 else:
  body=body.replace('function prices(operations,productId){','function prices(operations,productId){window.priceScans++;',1).replace('function priceIndex(operations){','function priceIndex(operations){window.priceIndexes++;',1)
 route.fulfill(response=route.fetch(),body=body)
try:
 with sync_playwright() as pw:
  b=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox'])
  ctx=b.new_context(viewport={'width':390,'height':844},locale='es-ES');ctx.add_init_script(script)
  ctx.route('**/app.js',instrument);ctx.route('**/improvements.js',instrument)
  p=ctx.new_page();errors=[];p.on('pageerror',lambda e:errors.append(str(e)))
  p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html');p.locator('.nav [data-tab="shop"]').click();p.wait_for_timeout(500)
  p.evaluate('window.historyCalls=0;window.priceScans=0;window.priceIndexes=0')
  durations=[p.evaluate('perfRender()') for _ in range(5)]
  report=p.evaluate('({historyCalls,priceScans,priceIndexes,products:document.querySelectorAll(".product").length})');report['renderMs']=durations
  print(json.dumps(report))
  if not baseline:
   assert report['historyCalls']<=1,report
   assert report['priceScans']==0 and report['priceIndexes']<=1,report
   # Un carrito privado no necesita leer estadísticas ni sincronizar la cesta compartida.
   p.evaluate('calls.length=0;testState.online=true;window.hogarChanged()');p.wait_for_timeout(450)
   p.locator('[data-product]').first.click();p.locator('#linePrice').fill('100');p.locator('#submitForm').click();expect(p.locator('#overlay [role="dialog"]')).to_have_count(0)
   assert p.evaluate('testState.cart.length')==1
   assert p.evaluate('calls.filter(q=>q.action==="featuresCartChanged"||q.action==="featuresSync").length')==0
   # La recarga por notificación dibuja una sola vez y no pide de nuevo todas las fotos.
   p.wait_for_timeout(500);p.evaluate('window.renderCalls=0;calls.length=0;window.hogarChanged()');p.wait_for_timeout(600)
   assert p.evaluate('renderCalls')==1
   assert p.evaluate('calls.filter(q=>q.action==="featuresProfileAvatars").length')==0
   # Un estado nuevo invalida el índice; no conserva precios anteriores en pantalla.
   p.evaluate("let id=testState.snapshot.products[0].id;testState.operations=[...[100,200].map((price,i)=>({id:'latest-'+i,type:'Gasto',status:'SYNCED',date:'2026-11-0'+(i+1),items:[{productId:id,unitPrice:price,quantity:1,unitCode:'lb',currency:'CUP'}]})),...testState.operations];window.hogarChanged()")
   expect(p.locator('.product').first).to_contain_text('+100 %')
   # El historial grande se presenta por tramos sin borrar ni filtrar movimientos.
   p.locator('.nav [data-tab="history"]').click();expect(p.locator('#movementRows [data-operation]')).to_have_count(50)
   p.locator('[data-action="moreHistory"]').click();expect(p.locator('#movementRows [data-operation]')).to_have_count(100)
   assert p.evaluate('testState.operations.length')==3002
  assert not errors,errors;b.close()
finally:server.shutdown();server.server_close()
