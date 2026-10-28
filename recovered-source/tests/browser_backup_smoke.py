"""Comprueba la herramienta local sin usar la firma real de la APK."""
import io
from pathlib import Path
import sys
import tempfile
import zipfile
import threading
from http.server import ThreadingHTTPServer,SimpleHTTPRequestHandler
from functools import partial
from playwright.sync_api import sync_playwright,expect

ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'scripts'))
from encrypt_signing_backup import encrypt


def main():
    class Quiet(SimpleHTTPRequestHandler):
        def log_message(self,*args):pass
    server=ThreadingHTTPServer(('127.0.0.1',0),partial(Quiet,directory=str(ROOT/'tools')))
    threading.Thread(target=server.serve_forever,daemon=True).start()
    with tempfile.TemporaryDirectory(prefix='hogar-backup-browser-') as tmp,sync_playwright() as pw:
        stage=Path(tmp)
        browser=pw.chromium.launch(executable_path='/usr/bin/chromium',args=['--no-sandbox'])
        page=browser.new_page()
        page.goto('http://127.0.0.1:'+str(server.server_port)+'/respaldo-firma.html')
        page.locator('#create').click()
        expect(page.locator('#publicCode')).to_have_value(__import__('re').compile(r'HOGAR-PUBLICA-1:.+'))
        code=page.locator('#publicCode').input_value()
        with page.expect_download() as event:page.locator('#save').click()
        private=stage/'local-private.json';event.value.save_as(private)
        sample=stage/'dummy.zip'
        with zipfile.ZipFile(sample,'w') as z:z.writestr('LEEME.txt','Solo datos ficticios, sin claves de la app.')
        encrypted=encrypt(sample,code,stage/'cipher.json')
        page.locator('#privateFile').set_input_files(private)
        page.locator('#encryptedFile').set_input_files(encrypted)
        with page.expect_download() as event:page.locator('#decrypt').click()
        restored=stage/'restored.zip';event.value.save_as(restored)
        assert restored.read_bytes()==sample.read_bytes()
        expect(page.locator('#status')).to_contain_text('ZIP privado verificado')
        browser.close()
    server.shutdown();server.server_close()
    print('Respaldo: creación local, descarga privada y descifrado de prueba verificados en Chrome, sin firma real.')


if __name__=='__main__':main()
