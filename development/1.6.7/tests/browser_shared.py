from pathlib import Path
import json,threading
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from functools import partial
from playwright.sync_api import sync_playwright,expect
ROOT=Path(__file__).resolve().parents[2]/'1.6.0/assets'
class Handler(SimpleHTTPRequestHandler):
 def log_message(self,*args):pass
 def guess_type(self,path):return 'image/svg+xml' if path.endswith('.png') else super().guess_type(path)
server=ThreadingHTTPServer(('127.0.0.1',0),partial(Handler,directory=str(ROOT)));threading.Thread(target=server.serve_forever,daemon=True).start()
catalog=json.loads((ROOT/'catalog.json').read_text())
state={'activeProfileId':'p','profile':{'name':'Ana'},'profiles':[{'id':'p','name':'Ana'}],'paired':True,'online':True,'today':'2026-11-02','deviceName':'Prueba','operations':[],'cart':[],'snapshot':{'products':catalog['products'],'categories':catalog['categories'],'spent':0,'income':350,'consumptionBudget':300,'month':'2026-11','balances':{'usd':100,'cup':5000}}}
script=r'''window.testState=STATE;window.calls=[];window.events=[];window.syncCount=0;window.serverRows=Array.from({length:14},(_,i)=>({id:'item-'+i,name:['Papa','Zanahoria','Ají pimiento','Arroz','Boniato','Calabaza','Tomate'][i%7],quantity:i+1,unit:'lb',revision:1,bought:false,addedByName:'Luis',lastChangeId:'seed'}));window.features={products:[],offers:[],shared:JSON.parse(JSON.stringify(serverRows)),queue:[],conflicts:[],sharedIntents:{},householdId:'h'};let seq=0;let receipts={};window.serverDelay=20;let work=Promise.resolve();
function pump(){for(let [id,w] of Object.entries(features.sharedIntents)){if(features.queue.some(o=>o.entityId===id))continue;let r=features.shared.find(x=>x.id===id);if(r&&['name','quantity','unit','bought','deleted'].every(k=>(r[k]||false)===(w.item[k]||false))){delete features.sharedIntents[id];continue;}let rev=r?r.revision:0;features.queue.push({id:'op-'+(++seq),entityId:id,baseRevision:rev,item:JSON.parse(JSON.stringify(w.item))});}}
window.HogarNative={call(text){let q=JSON.parse(text);calls.push(q);work=work.then(async()=>{let d={},err=null;try{
if(q.action==='state')d=testState;
else if(q.action==='featuresGet')d=features;
else if(q.action==='featuresShared'){features.sharedIntents[q.data.item.id]={item:JSON.parse(JSON.stringify(q.data.item)),afterId:q.data.afterId||''};pump();d=features;}
else if(q.action==='featuresSync'){syncCount++;if(!testState.online){features.syncError='Sin conexión';throw Error('Sin conexión');}await new Promise(r=>setTimeout(r,serverDelay));let batch=features.queue.slice(0,20);for(let o of batch){let hash=JSON.stringify(o.item);if(receipts[o.id]&&receipts[o.id]!==hash)throw Error('Recibo reescrito');receipts[o.id]=hash;let at=serverRows.findIndex(x=>x.id===o.entityId);let r={...o.item,revision:o.baseRevision+1,lastChangeId:o.id};if(at<0)serverRows.push(r);else serverRows[at]=r;}features.queue=features.queue.slice(batch.length);features.shared=JSON.parse(JSON.stringify(serverRows));features.lastSync=new Date().toISOString();delete features.syncError;pump();d=features;}
else if(q.action==='featuresCartChanged'){if(window.failCoordination)throw Error('Fallo de coordinación');events.push(q.data.kind);d=features;}
else if(q.action==='cart'){testState.cart=q.data.items;d=testState;}
else if(q.action==='cartCheckoutLocated'){if(window.failCheckout)throw Error('Compra no confirmada');testState.operations.push({id:'expense-once'});testState.cart=[];d=testState;}
else if(q.action==='featuresShareCart')d=features;
}catch(e){err=e.message;}window.hogarReply(q.id,err?{ok:false,error:err}:{ok:true,data:JSON.parse(JSON.stringify(d))});});}};
'''.replace('STATE',json.dumps(state))
try:
 with sync_playwright() as pw:
  b=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=b.new_context(viewport={'width':390,'height':844},locale='es-ES');ctx.add_init_script(script);p=ctx.new_page();errors=[];p.on('pageerror',lambda e:errors.append(str(e)));p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
  p.locator('.nav [data-tab="shop"]').click();p.locator('[data-action="cart"]').first.click();p.locator('[data-feature="shared"]').click();expect(p.locator('#sharedCount')).to_contain_text('14 artículos');p.wait_for_function('() => syncCount>=1 && !!features.lastSync')
  row=p.locator('[data-shared-id="item-0"]');expect(row.locator('strong')).to_have_text('Papa');assert row.bounding_box()['height']<100;img=row.locator('img');expect(img).to_have_attribute('src','product-papa.svg');assert img.evaluate('(x)=>x.complete&&x.naturalWidth>0');assert p.locator('[data-feature="sharedSync"]').count()==0
  p.locator('#sharedSearch').fill('Zanahoria');expect(p.locator('.shared-line')).to_have_count(2);p.locator('#sharedSearch').fill('');expect(p.locator('.shared-line')).to_have_count(14)
  p.evaluate("serverRows[2].deleted=true;serverRows[2].revision++;serverRows[1].quantity=88;serverRows[1].revision++;serverRows[1].lastChangeId='other-phone'");expect(p.locator('[data-shared-id="item-1"]')).to_contain_text('88 lb',timeout=16000);expect(p.locator('[data-shared-id="item-2"]')).to_have_count(0)
  p.locator('[data-shared-id="item-0"] [data-feature="sharedToggle"]').click();expect(p.locator('[data-shared-id="item-0"]')).to_have_count(0);p.locator('#sharedBought').click();expect(p.locator('[data-shared-id="item-0"]')).to_be_visible();p.wait_for_function("() => serverRows[0].bought===true",timeout=6000);assert p.evaluate('testState.operations.length')==0
  # Sin red: retirar y deshacer se reflejan al guardar, conservando el recibo anterior.
  p.evaluate("testState.online=false;window.hogarChanged()");expect(p.locator('#sharedStatus')).to_contain_text('Sin conexión');p.locator('[data-shared-id="item-0"] [data-feature="sharedMore"]').click();p.locator('[data-shared-id="item-0"] [data-feature="sharedDelete"]').click();expect(p.locator('[data-shared-id="item-0"]')).to_have_count(0);p.locator('[data-feature="sharedUndo"]').click();expect(p.locator('[data-shared-id="item-0"]')).to_be_visible();assert p.evaluate('features.queue.length')>=1
  p.evaluate("testState.online=true;window.hogarChanged();window.dispatchEvent(new Event('online'))");p.wait_for_function("() => features.queue.length===0&&Object.keys(features.sharedIntents).length===0",timeout=10000);assert not p.evaluate('serverRows[0].deleted');assert p.evaluate('testState.operations.length')==0
  # Edición en pantalla propia no se cierra con una respuesta automática.
  p.locator('[data-shared-id="item-0"] [data-feature="sharedMore"]').click();p.locator('[data-shared-id="item-0"] [data-feature="sharedEdit"]').click();p.locator('#sharedQty').fill('5');p.wait_for_timeout(2100);expect(p.locator('#sharedQty')).to_have_value('5');p.locator('#submitForm').click();expect(p.locator('#sharedSurface')).to_be_visible();p.wait_for_function('() => serverRows[0].quantity===5',timeout=6000)
  # Navegación estable, fotos y dimensiones móviles.
  p.locator('#sharedPending').click()
  for w in [320,360,390]:
   p.set_viewport_size({'width':w,'height':844});assert not p.evaluate('document.documentElement.scrollWidth>innerWidth');assert p.locator('.shared-line').first.bounding_box()['height']<100;assert p.locator('#sharedRows').bounding_box()['height']>400
  p.screenshot(path='/workspace/shared/downloads/Cesta_Compartida_1.6.7.png',full_page=False)
  # El fallo de coordinación no repite ni deshace una compra financiera ya guardada.
  p.locator('[data-feature="cartBack"]').click();p.locator('#closeModal').click();p.evaluate("testState.cart=[{id:'buy-local',productId:'Arroz',name:'Arroz',quantity:'1',unit:'lb',unitCode:'lb',unitPrice:'100',currency:'CUP',rate:'500',category:'Complementarios'}];window.hogarChanged()");p.wait_for_timeout(350);p.locator('[data-action="cart"]').first.click();p.evaluate('window.failCheckout=true');p.locator('#submitForm').click();expect(p.locator('#formError')).to_contain_text('Compra no confirmada');assert not p.evaluate("events.includes('purchased')");assert p.evaluate('testState.operations.length')==0
  p.evaluate('window.failCheckout=false;window.failCoordination=true');p.locator('#submitForm').click();p.wait_for_timeout(350);assert p.evaluate('testState.operations.length')==1;assert p.evaluate("calls.filter(q=>q.action==='cartCheckoutLocated').length")==2;assert p.evaluate("calls.some(q=>q.action==='featuresCartChanged'&&q.data.kind==='purchased')");assert p.locator('#overlay [role="dialog"]').count()==0
  assert not errors,errors;b.close();print('PASS: fotos, búsqueda/filtros, filas compactas, recepción automática de otro teléfono, envío al marcar/editar/retirar, sin gastos, offline/deshacer/reconexión y 320/360/390px.')
finally:server.shutdown();server.server_close()
