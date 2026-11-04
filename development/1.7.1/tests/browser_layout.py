"""Regresión del formulario largo mostrado por el usuario, con puente ficticio."""
from pathlib import Path
from playwright.sync_api import sync_playwright,expect
BASE=Path(__file__).resolve().parents[2]/'1.6.7/tests/browser_shared.py'
ns={'__file__':str(BASE)};exec(BASE.read_text().split('try:\n with sync_playwright()')[0],ns)
server=ns['server'];script=ns['script']+'''testState.online=false;let product=testState.snapshot.products.find(p=>p.name==='Picadillo de pollo');product.note='Oferta de prueba: confirmar mensajería y disponibilidad. '+product.note+' Fin de la información del proveedor.';'''
try:
 with sync_playwright() as pw:
  b=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=b.new_context(viewport={'width':390,'height':780},locale='es-ES');ctx.add_init_script(script);p=ctx.new_page();errors=[];p.on('pageerror',lambda e:errors.append(str(e)));p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
  p.locator('.nav [data-tab="shop"]').click();basket=p.locator('.nav [data-tab="shop"] svg');expect(basket).to_be_visible();expect(basket).to_have_attribute('aria-hidden','true')
  p.locator('.product').filter(has=p.locator('h3',has_text='Picadillo de pollo')).locator('[data-product]').click()
  expect(p.locator('.modal-scroll')).to_be_visible();expect(p.locator('.form-actions')).to_be_visible()
  for width,height in [(320,740),(360,740),(390,780),(390,420),(320,360)]:
   p.set_viewport_size({'width':width,'height':height});p.wait_for_timeout(150)
   assert not p.evaluate('document.documentElement.scrollWidth>innerWidth')
   scroller=p.locator('.modal-scroll');scroller.evaluate('(el)=>el.scrollTop=el.scrollHeight')
   footer=p.locator('.form-actions').bounding_box();button=p.locator('#submitForm').bounding_box();info=p.locator('.modal-scroll .notice.info').last.bounding_box();header=p.locator('.modal-header').bounding_box()
   assert info['y']+info['height']<=footer['y']+1,(width,height,info,footer)
   assert button['y']>=footer['y'] and button['y']+button['height']<=height,(width,height,button)
   assert header['y']+header['height']<=footer['y'],(width,height,header,footer)
   expect(p.locator('.modal-scroll')).to_contain_text('Fin de la información del proveedor.')
  p.set_viewport_size({'width':390,'height':780});p.wait_for_timeout(150);p.locator('#lineQty').fill('2');p.locator('#linePrice').fill('1000');p.locator('.modal-scroll').evaluate('(el)=>el.scrollTop=el.scrollHeight');p.screenshot(path='/workspace/shared/downloads/Formulario_Compra_1.7.1.png')
  p.locator('#submitForm').click();expect(p.locator('#overlay [role="dialog"]')).to_have_count(0);assert p.evaluate('testState.cart.length')==1;assert p.evaluate('testState.operations.length')==0
  p.locator('[data-action="cart"]').first.click();expect(p.locator('#submitForm')).to_be_visible();p.locator('.modal-scroll').evaluate('(el)=>el.scrollTop=el.scrollHeight');p.wait_for_timeout(200);p.screenshot(path='/workspace/shared/downloads/Cesta_Compra_1.7.1.png')
  # Dos pulsaciones antes de recibir SQLite no deben reintroducir un artículo retirado.
  p.locator('#closeModal').click();p.evaluate("testState.cart=Array.from({length:8},(_,i)=>({...testState.cart[0],id:'local-'+i}));let original=HogarNative.call;HogarNative.call=function(text){let q=JSON.parse(text);if(q.action==='cart'){if(window.failRemove){calls.push(q);setTimeout(()=>hogarReply(q.id,{ok:false,error:'Fallo local ficticio'}),100);return;}setTimeout(()=>original(text),150);}else original(text);};window.hogarChanged()")
  p.wait_for_timeout(400);p.locator('[data-action="cart"]').first.click();p.locator('#purchasePlace').fill('Lugar conservado');p.locator('.modal-scroll').evaluate('(el)=>el.scrollTop=100');p.evaluate("window.beforeRemoveCalls=calls.filter(q=>q.action==='cart').length;document.querySelectorAll('.removeLine')[0].click();document.querySelectorAll('.removeLine')[1].click();document.querySelector('#submitForm').click()")
  expect(p.locator('.removeLine')).to_have_count(7);assert p.evaluate("calls.filter(q=>q.action==='cart').length-beforeRemoveCalls")==1;expect(p.locator('#purchasePlace')).to_have_value('Lugar conservado');assert p.evaluate('testState.operations.length')==0
  p.evaluate("document.querySelectorAll('.removeLine')[0].click()");expect(p.locator('.removeLine')).to_have_count(6);assert p.evaluate('testState.cart.length')==6
  p.evaluate("window.failRemove=true;document.querySelectorAll('.removeLine')[0].click()");p.wait_for_timeout(250);assert p.evaluate('testState.cart.length')==6;expect(p.locator('.removeLine').first).to_be_enabled();expect(p.locator('#submitForm')).to_be_enabled()
  assert not errors,errors;b.close();print('PASS: cesta SVG, formulario largo, texto y botón separados, guardar sin gasto; 320/360/390 px y altura reducida 360/420 px.')
finally:server.shutdown();server.server_close()
