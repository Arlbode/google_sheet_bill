"""PIN visible/oculto, foco estable, cierre explícito, Atrás y vencimiento diario."""
from functools import partial
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from pathlib import Path
import json,threading
from playwright.sync_api import sync_playwright,expect
ROOT=Path(__file__).resolve().parents[1]

class Quiet(SimpleHTTPRequestHandler):
    def log_message(self,*args):pass

def main():
    catalog=json.loads((ROOT/'android/assets/catalog.json').read_text())
    fixture='''window.fixture={calls:[],state:{profiles:[{id:'p',name:'Prueba'}],activeProfileId:'',paired:false,online:false}};
    window.HogarNative={call(text){const r=JSON.parse(text);fixture.calls.push(r);let s=fixture.state;
    if(r.action==='unlock'){s.activeProfileId='p';s.profile={name:'Prueba'};s.snapshot=CATALOG;s.operations=[];s.authentication={expiresAt:Date.now()+2500};}
    if(r.action==='lock')s.activeProfileId='';
    if(r.action==='state'&&s.authentication&&Date.now()>=s.authentication.expiresAt)s.activeProfileId='';
    setTimeout(()=>window.hogarReply(r.id,{ok:true,data:JSON.parse(JSON.stringify(s))}),0);}};'''.replace('CATALOG',json.dumps(catalog))
    server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(ROOT/'android/assets')));threading.Thread(target=server.serve_forever,daemon=True).start()
    try:
        with sync_playwright() as pw:
            b=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);p=b.new_page(viewport={'width':390,'height':844});p.add_init_script(fixture);p.goto('http://127.0.0.1:'+str(server.server_port)+'/index.html')
            p.locator('[data-unlock="p"]').click();pin=p.locator('#unlockPin');pin.fill('1234');eye=p.locator('[data-pin-toggle="unlockPin"]')
            expect(pin).to_have_attribute('type','password');expect(eye).to_have_attribute('aria-pressed','false')
            eye.click();expect(pin).to_have_attribute('type','text');expect(pin).to_have_value('1234');expect(pin).to_be_focused();expect(eye).to_have_attribute('aria-label','Ocultar PIN')
            p.evaluate('window.hogarChanged()');p.wait_for_timeout(400);expect(pin).to_be_focused();expect(pin).to_have_value('1234')
            eye.click();expect(pin).to_have_attribute('type','password');expect(pin).to_have_value('1234');expect(pin).to_be_focused()
            p.locator('#submitForm').click();expect(p.locator('[data-tab="profile"]')).to_be_visible()
            # Atrás desde Inicio cierra la app sin enviar lock.
            p.evaluate('window.hogarBack()');p.wait_for_timeout(100)
            assert p.evaluate("fixture.calls.some(r=>r.action==='exit')")
            assert not p.evaluate("fixture.calls.some(r=>r.action==='lock')")
            # En el vencimiento diario se cierra el formulario y vuelve la selección del perfil.
            p.locator('[data-tab="profile"]').click();p.locator('[data-action="newProfile"]').click();p.locator('#personPin').fill('1234')
            p.locator('[data-pin-toggle="personPin"]').click();expect(p.locator('#personPin')).to_have_attribute('type','text')
            expect(p.locator('#personPin2')).to_have_attribute('type','password')
            expect(p.locator('[data-unlock="p"]')).to_be_visible(timeout=6000);expect(p.locator('[role="dialog"]')).to_have_count(0)
            p.locator('[data-unlock="p"]').click();expect(p.locator('#unlockPin')).to_have_attribute('type','password');expect(p.locator('#unlockPin')).to_have_value('')
            p.locator('#unlockPin').fill('1234');p.locator('#submitForm').click();p.locator('[data-tab="profile"]').click()
            expect(p.locator('[data-action="lock"]')).to_contain_text('Cerrar sesión');p.locator('[data-action="lock"]').click();expect(p.locator('[data-unlock="p"]')).to_be_visible()
            assert not p.evaluate('document.documentElement.scrollWidth>innerWidth')
            b.close()
        print('Navegador: ojito, valor y foco del PIN, oculto al reabrir, salida sin bloqueo, vencimiento diario y cierre explícito correctos.')
    finally:server.shutdown();server.server_close()

if __name__=='__main__':main()
