"""Catálogo real y saldo por moneda; puente y datos ficticios, sin usar el Excel privado."""
from pathlib import Path
from playwright.sync_api import sync_playwright,expect
BASE=Path(__file__).resolve().parents[2]/'1.6.0/tests/browser_features.py'
ns={'__file__':str(BASE)};exec(BASE.read_text().split('try:\n with sync_playwright()')[0],ns)
server=ns['server'];script=ns['script']+'''testState.snapshot.products=testState.snapshot.products.filter(p=>p.id!=='Espaguetis');testState.online=false;testState.operations=[];testState.snapshot.balances={usd:200,cup:10000,cupCash:10000,cupTransfer:0};testState.snapshot.reserveLedger=[{id:'fixture-savings',type:'Aporte ahorro',category:'Ahorro general',currency:'USD',amount:'190',status:'SYNCED'}];'''
try:
 with sync_playwright() as pw:
  b=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);ctx=b.new_context(viewport={'width':390,'height':844},locale='es-ES');ctx.add_init_script(script);p=ctx.new_page();errors=[];p.on('pageerror',lambda e:errors.append(str(e)));p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
  expect(p.locator('.budgetcard')).to_contain_text('No indica los dólares que puedes cambiar')
  p.locator('[data-action="exchange"]').click();expect(p.locator('#overlay')).to_contain_text('USD libres (sin convertir tus CUP): 10,00 USD');p.locator('#closeModal').click()
  p.locator('.nav [data-tab="shop"]').click();p.locator('#search').fill('spaguetis');card=p.locator('.product');expect(card).to_have_count(1);expect(card.locator('h3')).to_have_text('Espaguetis');expect(card).to_contain_text('Añadir precio')
  img=card.locator('img');expect(img).to_have_attribute('src','product-espaguetis.svg');p.wait_for_function("()=>document.querySelector('.product img').complete&&document.querySelector('.product img').naturalWidth>0")
  assert img.evaluate("el=>{let c=document.createElement('canvas');c.width=164;c.height=148;let ctx=c.getContext('2d');ctx.drawImage(el,0,0,164,148);let a=ctx.getImageData(0,0,164,148).data,n=0;for(let i=3;i<a.length;i+=4)if(a[i]>100)n++;return n>1000;}")
  for width in [320,360,390]:
   p.set_viewport_size({'width':width,'height':844});assert not p.evaluate('document.documentElement.scrollWidth>innerWidth');expect(img).to_be_visible()
  p.locator('#search').blur();card.scroll_into_view_if_needed();p.wait_for_timeout(180);p.screenshot(path='/workspace/shared/downloads/Espaguetis_1.7.2.png')
  card.locator('[data-product]').click();expect(p.locator('#linePrice')).to_have_value('');expect(p.locator('#lineUnit')).to_have_value('paquete');p.locator('#linePrice').fill('100');p.locator('#lineQty').fill('2');p.locator('#submitForm').click();expect(p.locator('#overlay [role="dialog"]')).to_have_count(0);assert p.evaluate('testState.cart.length')==1;assert p.evaluate('testState.operations.length')==0
  p.locator('[data-action="cart"]').first.click();expect(p.locator('.cart-line img')).to_have_attribute('src','product-espaguetis.svg');p.locator('#closeModal').click()
  p.locator('#search').fill('spaghetti');expect(p.locator('.product')).to_have_count(1);p.locator('[data-feature="productEdit"]').click();p.locator('#fpPrice').fill('450');p.locator('#submitForm').click();p.locator('#closeModal').click();expect(p.locator('.product .price')).to_contain_text('450')
  # Reabrir con la misma edición local conserva una sola identidad y la imagen.
  p.evaluate('window.hogarChanged()');p.wait_for_timeout(450);expect(p.locator('.product')).to_have_count(1);assert p.evaluate("features.products.filter(x=>x.id==='Espaguetis').length")==1;assert not errors,errors;b.close();print('PASS: espaguetis con Sheet antiguo, imagen SVG con PNG interno visible, alias, 320/360/390px, precio editable, carrito sin gasto y claridad USD/CUP.')
finally:server.shutdown();server.server_close()
