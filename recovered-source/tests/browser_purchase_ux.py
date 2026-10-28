"""Comportamiento visible: teclado, búsqueda, gastos libres, cesta, lista y mapas.

Puente nativo simulado: la persistencia y el asiento real se prueban en Android.
"""
from datetime import datetime
from functools import partial
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from pathlib import Path
import json,threading
from zoneinfo import ZoneInfo
from playwright.sync_api import sync_playwright,expect
ROOT=Path(__file__).resolve().parents[1]

class Quiet(SimpleHTTPRequestHandler):
    def log_message(self,*args):pass

def main():
    catalog=json.loads((ROOT/'android/assets/catalog.json').read_text())
    state={'activeProfileId':'person','profile':{'name':'Prueba'},'profiles':[{'id':'person','name':'Prueba'}],
           'paired':False,'online':False,'today':datetime.now(ZoneInfo('America/Havana')).date().isoformat(),
           'cart':[],'operations':[],'snapshot':catalog,'shopping':{'active':False},'shoppingLocation':{'enabled':False,'permitted':False}}
    fixture='''window.fixture={state:INITIAL,calls:[]};window.HogarNative={call(text){const r=JSON.parse(text);fixture.calls.push(r);let s=fixture.state,data=s;
    if(r.action==='cart')s.cart=r.data.items;
    if(r.action==='shoppingLine'){const i=s.shopping.lines.findIndex(x=>x.id===r.data.id);if(i>=0)s.shopping.lines[i]=r.data;else s.shopping.lines.push(r.data);}
    if(r.action==='cartCheckoutLocated'){s.operations.push({id:'expense',type:'Gasto',category:'Canasta básica',date:s.today,amount:'620',currency:'CUP',status:'PENDING',actorName:'Prueba',location:{place:r.data.place,latitude:23.1,longitude:-82.4,accuracy:5,status:'captured'},items:s.cart});s.cart=[];data={...s,savedCount:1};}
    if(r.action==='shoppingBuyLocated'){let l=s.shopping.lines.find(x=>x.id===r.data.id);l.bought=true;s.shopping.spent='250';s.shopping.remaining='750';s.operations.push({id:'mode-expense'});}
    if(r.action==='purchaseMap')data={opened:true};
    setTimeout(()=>window.hogarReply(r.id,{ok:true,data}),0);}};'''.replace('INITIAL',json.dumps(state))
    server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(ROOT/'android/assets')))
    threading.Thread(target=server.serve_forever,daemon=True).start()
    try:
        with sync_playwright() as pw:
            browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox'])
            page=browser.new_page(viewport={'width':390,'height':844},locale='es-ES',timezone_id='America/Havana')
            errors=[];page.on('pageerror',lambda error:errors.append(str(error)))
            page.add_init_script(fixture);page.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
            page.locator('[data-tab="shop"]').click()
            search=page.locator('#search');search.focus();page.set_viewport_size({'width':390,'height':430});search.fill('arroz')
            expect(search).to_be_focused();expect(page.locator('.product')).to_have_count(3)
            page.wait_for_timeout(150)
            bounds=page.locator('.product').first.bounding_box();button=page.locator('.product .add').first.bounding_box()
            assert bounds['y']>=0 and bounds['y']+bounds['height']<=430,bounds
            assert button['y']+button['height']<=430,button
            expect(page.locator('.nav')).to_be_hidden()
            # Filtrar no reconstruye el input ni oculta el teclado.
            page.locator('[data-category="Canasta básica"]').click();expect(search).to_be_focused()
            search.fill('inexistente');expect(page.locator('#products')).to_contain_text('No hay productos')
            page.locator('#clearSearch').click();expect(search).to_be_focused();expect(search).to_have_value('')
            search.fill('arroz');page.locator('#showSearchResults').click();expect(search).not_to_be_focused()
            page.set_viewport_size({'width':390,'height':844})
            page.locator('.product .add').first.click();page.locator('#linePrice').fill('370');page.locator('#submitForm').click()
            expect(page.locator('.cartbar')).to_contain_text('370,00 CUP')
            page.locator('[data-action="freeItem"]').click();page.locator('#freeName').fill('Merienda <prueba>')
            page.locator('#freeCategory').select_option('Ocio');page.locator('#lineQty').fill('2');page.locator('#linePrice').fill('125')
            expect(page.locator('#lineTotal')).to_contain_text('250,00 CUP');page.locator('#submitForm').click()
            expect(page.locator('.cartbar')).to_contain_text('620,00 CUP')
            assert page.evaluate('fixture.state.operations.length')==0
            assert not page.evaluate("fixture.calls.some(x=>['save','checkout','cartCheckoutLocated'].includes(x.action))")
            page.locator('[data-action="cart"]').click();expect(page.locator('#cartTotal')).to_contain_text('620,00 CUP')
            expect(page.locator('.cart-line')).to_have_count(2);expect(page.locator('.cart-line').last).to_contain_text('Merienda <prueba>')
            page.locator('.editCartLine').last.click();expect(page.locator('#freeName')).to_have_value('Merienda <prueba>')
            page.locator('#linePrice').fill('100');page.locator('#submitForm').click()
            expect(page.locator('.cartbar')).to_contain_text('570,00 CUP');assert page.evaluate('fixture.state.cart.length')==2
            page.locator('[data-action="cart"]').click();page.locator('#purchasePlace').fill('Mercado de 42');page.locator('#submitForm').click()
            expect(page.locator('.cartbar')).to_have_count(0)
            checkout=page.evaluate("fixture.calls.find(x=>x.action==='cartCheckoutLocated')")
            assert checkout['data']['place']=='Mercado de 42' and len(checkout['data']['items'])==2
            page.locator('[data-tab="history"]').click();page.locator('[data-operation="expense"]').click()
            expect(page.locator('.purchase-location')).to_contain_text('Mercado de 42');page.locator('.map-open').click()
            command=page.evaluate("fixture.calls.find(x=>x.action==='purchaseMap')")
            assert command['data']=={'place':'Mercado de 42','latitude':23.1,'longitude':-82.4}
            page.locator('#closeModal').click()
            # En Modo compra, un libre también es pendiente y suma al tope.
            page.evaluate("fixture.state.shopping={active:true,session:{id:'session',date:fixture.state.today,currency:'CUP',rate:'870',limit:'1000'},lines:[],spent:'0',remaining:'1000'};window.hogarChanged()")
            page.wait_for_timeout(400);page.locator('[data-tab="shop"]').click();page.locator('[data-action="shoppingResume"]').click()
            page.locator('[data-action="freeItem"]').click();page.locator('#freeName').fill('Mensajería');page.locator('#linePrice').fill('250');page.locator('#submitForm').click()
            expect(page.locator('#shoppingListTotal')).to_contain_text('250,00 CUP');expect(page.locator('#shoppingAvailable')).to_contain_text('750,00 CUP')
            assert page.evaluate('fixture.state.operations.length')==1
            page.locator('[data-shopping-edit]').click();expect(page.locator('#freeName')).to_have_value('Mensajería');page.locator('#closeModal').click()
            page.locator('[data-shopping-buy]').click();page.locator('#purchasePlace').fill('Tienda');page.locator('#submitForm').click()
            expect(page.locator('#shoppingListTotal')).to_contain_text('0,00 CUP');expect(page.locator('#shoppingAvailable')).to_contain_text('750,00 CUP')
            assert not page.evaluate('document.documentElement.scrollWidth>innerWidth')
            assert not errors,errors
            browser.close()
        print('Navegador: resultados visibles con teclado, foco al filtrar, libre en cesta/lista, edición, totales, confirmación con lugar y apertura nativa del mapa correctos.')
    finally:server.shutdown();server.server_close()

if __name__=='__main__':main()
