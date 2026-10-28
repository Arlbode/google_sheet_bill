"""Contrato UI/puente: cierre, nuevo tope, lugar e historial con respuestas fijadas."""
from datetime import datetime,timezone
from functools import partial
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
import copy,json
from pathlib import Path
import threading
from playwright.sync_api import sync_playwright,expect

ROOT=Path(__file__).resolve().parents[1]


def main():
    catalog=json.loads((ROOT/'android/assets/catalog.json').read_text());today=datetime.now(timezone.utc).date().isoformat()
    old='123e4567-e89b-42d3-a456-426614174000';new='123e4567-e89b-42d3-a456-426614174001'
    pending={'id':'123e4567-e89b-42d3-a456-426614174002','productId':'Boniato','name':'Boniato','category':'Complementarios','quantity':'1','unitCode':'lb','unit':'Libra','unitPrice':'150','currency':'CUP','paymentFormat':'Efectivo','rate':'870','bought':False}
    original={'activeProfileId':'person','profile':{'name':'Prueba'},'profiles':[{'id':'person','name':'Prueba'}],'paired':False,'online':False,'today':today,'operations':[],
              'shoppingLocation':{'enabled':False,'permitted':False},'snapshot':{'products':catalog['products'],'rate':870},
              'shopping':{'active':True,'session':{'id':old,'date':today,'limit':'4000','currency':'CUP','rate':'870'},'lines':[pending],
                          'spent':'370','remaining':'3630','listTotal':'150','projectedRemaining':'3480','unpriced':0}}
    closed=copy.deepcopy(original);closed['shopping']={'active':False}
    fresh=copy.deepcopy(original);fresh['shopping'].update(session={'id':new,'date':today,'limit':'1000','currency':'CUP','rate':'900','place':'Mercado nuevo'},spent='0',remaining='1000',projectedRemaining='850')
    bought=copy.deepcopy(fresh);bought['shopping']['lines'][0]['bought']=True
    history=[{'id':old,'date':today,'limit':'4000','currency':'CUP','rate':'870','closedAt':datetime.now(timezone.utc).isoformat(),'closedByName':'Prueba','spent':'370','boughtCount':1,'pendingCount':1,'lines':[pending]}]
    script='''window.fixture={state:INITIAL,calls:[]};window.HogarNative={call(text){const r=JSON.parse(text);fixture.calls.push(r);let data=fixture.state;
    if(r.action==='shoppingClose')data=fixture.state=CLOSED;
    if(r.action==='shoppingStartFresh')data=fixture.state=FRESH;
    if(r.action==='shoppingBuyLocated')data=fixture.state=BOUGHT;
    if(r.action==='shoppingHistory')data={sessions:HISTORY};
    setTimeout(()=>window.hogarReply(r.id,{ok:true,data}),0);}};'''
    for label,value in [('INITIAL',original),('CLOSED',closed),('FRESH',fresh),('BOUGHT',bought),('HISTORY',history)]:script=script.replace(label,json.dumps(value))
    class Quiet(SimpleHTTPRequestHandler):
        def log_message(self,*args):pass
    server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(ROOT/'android/assets')))
    threading.Thread(target=server.serve_forever,daemon=True).start()
    try:
        with sync_playwright() as pw:
            browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);page=browser.new_page(viewport={'width':390,'height':844})
            page.add_init_script(script);page.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
            page.locator('[data-tab="shop"]').click();page.locator('[data-action="shoppingResume"]').click()
            page.locator('[data-action="shoppingClose"]').click();expect(page.locator('#carryPending')).to_be_checked()
            page.locator('#submitForm').click();expect(page.locator('[data-action="shoppingStart"]')).to_be_visible()
            close=page.evaluate("fixture.calls.find(r=>r.action==='shoppingClose')")
            assert close['data']=={'sessionId':old,'carryPending':True}
            page.locator('[data-action="shoppingStart"]').click();page.locator('#shoppingLimit').fill('1000');page.locator('#shoppingRate').fill('900');page.locator('#shoppingPlace').fill('Mercado nuevo');page.locator('#submitForm').click()
            expect(page.locator('[data-shopping-buy]')).to_be_visible()
            start=page.evaluate("fixture.calls.find(r=>r.action==='shoppingStartFresh')")
            assert start['data']['limit']=='1000' and start['data']['rate']=='900' and start['data']['carryPending']
            page.locator('[data-shopping-buy]').click();expect(page.locator('#purchasePlace')).to_have_value('Mercado nuevo')
            page.locator('#purchasePlace').fill('Tienda de prueba');page.locator('#submitForm').click()
            expect(page.locator('[data-shopping-buy]')).to_have_count(0)
            buy=page.evaluate("fixture.calls.find(r=>r.action==='shoppingBuyLocated')")
            assert buy['data']['sessionId']==new and buy['data']['place']=='Tienda de prueba'
            page.locator('[data-action="shoppingHistory"]').click();expect(page.locator('#shoppingArchive0')).to_be_visible()
            page.locator('#shoppingArchive0').click();expect(page.locator('[role="dialog"]')).to_contain_text('Boniato')
            assert not page.evaluate('document.documentElement.scrollWidth>innerWidth')
            browser.close()
        print('Navegador: cierre, pendientes, nuevo tope, lugar y navegación del historial correctos.')
    finally:server.shutdown();server.server_close()


if __name__=='__main__':main()
