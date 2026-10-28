"""Prueba visual con puente nativo simulado. Requiere Playwright y Chromium.

No conecta al Sheet real ni equivale a instalar la APK en Android.
"""
from datetime import datetime, timezone, timedelta
from functools import partial
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
import json
from pathlib import Path
import threading
from zoneinfo import ZoneInfo

from playwright.sync_api import sync_playwright, expect

ROOT=Path(__file__).resolve().parents[1]
NOW=datetime.now(timezone.utc)
TODAY=NOW.astimezone(ZoneInfo('America/Havana')).date().isoformat()
CATALOG=json.loads((ROOT/'android/assets/catalog.json').read_text())


class QuietHandler(SimpleHTTPRequestHandler):
    def log_message(self,*args):pass


def state():
    return {'activeProfileId':'test-person','paired':True,'online':True,'today':TODAY,
            'profile':{'name':'Prueba'},'profiles':[{'id':'test-person','name':'Prueba'}],
            'operations':[],'cart':[],'pending':0,'planPending':0,'deviceName':'Teléfono de prueba',
            'planning':{'quote':{'rate':870,'consultedAt':NOW.isoformat(),'day':TODAY,'source':'elTOQUE'}},
            'snapshot':{'products':CATALOG['products'],'categories':CATALOG['categories'],
                        'month':TODAY[:7],'income':350,'savings':175,'spent':20,'consumptionBudget':133,
                        'rate':870,'serverAt':'before','balances':{'usd':300,'cup':6000,'cupCash':5000,'cupTransfer':1000}}}


def page_for(browser,url,value):
    context=browser.new_context(viewport={'width':390,'height':844},locale='es-ES',timezone_id='America/Havana')
    fixture='''
    window.fixture={state:STATE,calls:[]};
    window.HogarNative={call(payload){
      const request=JSON.parse(payload); fixture.calls.push(request.action);
      let data={};
      if(request.action==='state') data=fixture.state;
      else if(request.action==='sync'){
        fixture.state.snapshot.rate=900;
        fixture.state.snapshot.serverAt=new Date().toISOString();
        fixture.state.planning.quote={rate:900,consultedAt:new Date().toISOString(),day:fixture.state.today,source:'elTOQUE'};
        setTimeout(()=>window.hogarChanged(),30);
      }
      setTimeout(()=>window.hogarReply(request.id,{ok:true,data}),0);
    }};
    '''.replace('STATE',json.dumps(value))
    context.add_init_script(fixture)
    page=context.new_page()
    page.goto(url)
    expect(page.locator('#quoteCard')).to_be_visible()
    return context,page


def main():
    server=ThreadingHTTPServer(('127.0.0.1',0),partial(QuietHandler,directory=str(ROOT/'android/assets')))
    threading.Thread(target=server.serve_forever,daemon=True).start()
    url='http://127.0.0.1:'+str(server.server_port)+'/index.html'
    output=ROOT/'build';output.mkdir(exist_ok=True)
    try:
        with sync_playwright() as pw:
            browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox'])
            context,page=page_for(browser,url,state())
            expect(page.locator('.cup-equivalent')).to_have_count(6)
            expect(page.locator('#quoteCard')).to_contain_text('870,00 CUP')
            assert not page.evaluate('document.documentElement.scrollWidth > innerWidth')
            assert page.locator('.wallet .cup-equivalent').bounding_box()['height']<25
            page.screenshot(path=str(output/('Inicio_CUP_'+json.loads((ROOT/'release.json').read_text())['version_name']+'.png')),full_page=True)
            page.locator('#refreshQuote').click()
            expect(page.locator('#quoteCard')).to_contain_text('900,00 CUP')
            expect(page.locator('.kpi .cup-equivalent').first).to_contain_text('315.000,00 CUP')
            assert page.evaluate("fixture.calls.filter(x=>x==='sync').length") == 1
            # La llegada de un estado nuevo no debe reconstruir un formulario con foco.
            page.locator('[data-tab="profile"]').click()
            page.locator('[data-action="newProfile"]').click()
            page.locator('#personPin').fill('1234')
            page.evaluate('window.hogarChanged()')
            page.wait_for_timeout(500)
            expect(page.locator('#personPin')).to_be_focused()
            expect(page.locator('#personPin')).to_have_value('1234')
            context.close()
            offline=state();offline['online']=False
            offline['planning']['quote']['consultedAt']=(NOW-timedelta(days=1)).isoformat()
            context,page=page_for(browser,url,offline)
            expect(page.locator('#quoteCard')).to_contain_text('Sin conexión')
            expect(page.locator('#refreshQuote')).to_be_disabled()
            assert page.evaluate("fixture.calls.filter(x=>x==='sync').length") == 0
            context.close()
            stale=state();stale['planning']['quote']['consultedAt']=(NOW-timedelta(hours=2)).isoformat()
            context,page=page_for(browser,url,stale)
            expect(page.locator('#quoteCard')).to_contain_text('900,00 CUP')
            assert page.evaluate("fixture.calls.filter(x=>x==='sync').length") == 1
            context.close();browser.close()
        print('Navegador: tarjetas, consulta manual, consulta automática, modo sin conexión, ancho móvil y PIN correctos.')
    finally:
        server.shutdown();server.server_close()


if __name__=='__main__':main()
