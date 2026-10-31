"""Guía real en Chromium: navegación, demostraciones, progreso y cero llamadas nativas."""
from pathlib import Path
import threading,json
from functools import partial
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from playwright.sync_api import sync_playwright,expect
ROOT=Path(__file__).resolve().parents[2]/'1.6.0/assets'
HTML='''<!doctype html><html lang="es"><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><link rel="stylesheet" href="app.css"><link rel="stylesheet" href="guide.css"><div id="overlay"></div><script src="guide.js"></script><script>
window.nativeCalls=[];window.HogarNative={call(x){nativeCalls.push(x);throw Error('La guía no puede usar el puente');}};
window.openGuide=function(topic,progress){HogarGuide.open({topic:topic,progress:progress,save(v){window.savedProgress=v;},close(){HogarGuide.dispose();document.querySelector('#overlay').innerHTML='';},modal(title,body){document.querySelector('#overlay').innerHTML='<div class="backdrop"><section class="modal" role="dialog"><header class="modal-header"><h2>'+title+'</h2></header>'+body+'</section></div>';}});};openGuide();
</script></html>'''
class Handler(SimpleHTTPRequestHandler):
 def log_message(self,*args):pass
 def do_GET(self):
  if self.path=='/test':
   self.send_response(200);self.send_header('Content-Type','text/html; charset=utf-8');self.end_headers();self.wfile.write(HTML.encode())
  else:super().do_GET()
server=ThreadingHTTPServer(('127.0.0.1',0),partial(Handler,directory=str(ROOT)));threading.Thread(target=server.serve_forever,daemon=True).start()
ids=['respaldo','estado-envio','precios','comparador','catalogo','lista-compartida','prevision','ofertas']
try:
 with sync_playwright() as pw:
  browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox']);page=browser.new_page(viewport={'width':390,'height':844},locale='es-ES');errors=[];page.on('pageerror',lambda e:errors.append(str(e)));page.goto('http://127.0.0.1:'+str(server.server_port)+'/test');topics=page.evaluate('HogarGuide.catalog()');assert len(topics)==21;assert len({x['id'] for x in topics})==21;assert page.locator('.g-new-tools button').count()==8
  demos=0;steps=0
  for id in ids:
   page.evaluate('(id)=>openGuide(id)',id);count=next(t['steps'] for t in topics if t['id']==id)
   for index in range(count):
    expect(page.locator('.g-step-counter')).to_have_text(str(index+1)+' / '+str(count));assert page.locator('#guideStepTitle').inner_text();assert page.locator('.g-route').inner_text().startswith({'respaldo':'Perfil → Respaldo','estado-envio':'Movimientos · Perfil','precios':'Movimientos → Historial','comparador':'Comprar → Comparar','catalogo':'Comprar → Editar','lista-compartida':'Comprar → Cesta','prevision':'Inicio → Previsión','ofertas':'Comprar → Ofertas'}[id]);assert not page.evaluate('document.documentElement.scrollWidth>innerWidth')
    if page.locator('[data-g="demo"]').count():
     page.locator('[data-g="demo"]').click();expect(page.locator('.g-result')).to_be_visible();demos+=1;assert page.evaluate('nativeCalls.length')==0
    page.locator('[data-g="next"]').click();steps+=1
   expect(page.locator('.g-complete')).to_be_visible();assert id in page.evaluate('savedProgress.done')
  page.evaluate('openGuide()');page.locator('#guideSearch').fill('contraseña');expect(page.locator('#guideTopics')).to_contain_text('Respaldo cifrado');page.locator('#guideSearch').fill('conflicto');expect(page.locator('#guideTopics')).to_contain_text('Lista compartida');page.evaluate("openGuide('prevision',{done:['comprar'],last:{id:'comprar',step:1}})");assert 'comprar' in page.evaluate('savedProgress.done');page.locator('[data-g="play"]').click();expect(page.locator('[data-g="play"]')).to_have_attribute('aria-pressed','true');page.locator('[data-g="play"]').click();expect(page.locator('[data-g="play"]')).to_have_attribute('aria-pressed','false')
  page.evaluate("openGuide('comparador')");page.locator('.g-progress [data-g-step="1"]').click();page.locator('[data-g="play"]').click();expect(page.locator('.g-result')).to_be_visible(timeout=6000);page.locator('[data-g="play"]').click()
  page.evaluate("openGuide('respaldo')");page.screenshot(path='/workspace/shared/downloads/Guia_Hogar_1.6.1.png',full_page=True);assert not errors,errors;assert demos==8;assert page.evaluate('nativeCalls.length')==0;print(json.dumps({'topics':len(topics),'newTopics':8,'newStepsChecked':steps,'demos':demos,'nativeCalls':0,'search':'passed','mobileWidth':'passed','oldProgress':'preserved'},ensure_ascii=False));browser.close()
finally:server.shutdown();server.server_close()
