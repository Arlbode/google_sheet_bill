#!/usr/bin/env python3
"""Nueva identidad autorizada por el usuario. Conserva el DEX original.

La clave se crea SOLO con --init-signing antes de fijar el certificado público.
Una vez fijado, si falta la clave se exige restaurarla; no se sustituye.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import secrets
import shutil
import subprocess
import tempfile
import xml.etree.ElementTree as ET
import zipfile

from build_unsigned import BASE_SHA256

ROOT = Path(__file__).resolve().parents[1]
PRIVATE = Path('/workspace/.android-signing/hogar-playa-v2')
SDK = Path(os.environ.get('ANDROID_HOME', '/workspace/.tools/hogar-android-sdk'))
ANDROID = '{http://schemas.android.com/apk/res/android}'
ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')


def digest(data):
    return hashlib.sha256(data).hexdigest()


def signing_identity(init=False, private=PRIVATE):
    private = Path(private)
    config = json.loads((ROOT / 'release.json').read_text())
    key, password = private / 'hogar.p12', private / 'password'
    pin = ROOT / 'signing/new-certificate.sha256'
    if not key.is_file() or not password.is_file():
        if pin.exists() or not init:
            raise RuntimeError('Restaura la firma privada de Hogar Playa Nueva. No se generará otra identidad.')
        if key.exists() or password.exists():
            raise RuntimeError('Firma incompleta; se conservan los archivos existentes.')
        private.mkdir(parents=True, exist_ok=True, mode=0o700)
        password.write_text(secrets.token_hex(32) + '\n')
        password.chmod(0o600)
        subprocess.run(['java', '-m', 'java.base/sun.security.tools.keytool.Main',
                        '-genkeypair', '-alias', config['key_alias'], '-keystore', str(key),
                        '-storetype', 'PKCS12', '-storepass:file', str(password),
                        '-keypass:file', str(password), '-keyalg', 'RSA', '-keysize', '3072',
                        '-validity', '10000', '-dname', 'CN=Hogar Playa Nueva,O=Uso familiar,C=CU'],
                       check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        key.chmod(0o600)
    certificate = subprocess.check_output([
        'java', '-m', 'java.base/sun.security.tools.keytool.Main', '-exportcert',
        '-alias', config['key_alias'], '-keystore', str(key), '-storepass:file', str(password)
    ], stderr=subprocess.PIPE)
    fingerprint = digest(certificate)
    if pin.exists():
        if pin.read_text().strip() != fingerprint:
            raise RuntimeError('La clave no coincide con la firma fijada para Hogar Playa Nueva.')
    elif init:
        pin.write_text(fingerprint + '\n')
        (ROOT / 'signing/new-certificate.der').write_bytes(certificate)
    else:
        raise RuntimeError('Falta el certificado público fijado; no se puede verificar la identidad.')
    return key, password, fingerprint


def private_backup(key, password, target):
    target = Path(target)
    if target.resolve().is_relative_to(ROOT.parent.resolve()):
        raise RuntimeError('El respaldo privado debe quedar fuera del repositorio.')
    target.parent.mkdir(parents=True, exist_ok=True, mode=0o700)
    with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_DEFLATED) as backup:
        backup.write(key, 'hogar.p12')
        backup.write(password, 'password')
        backup.write(ROOT / 'signing/new-certificate.sha256', 'certificate.sha256')
        backup.writestr('LEEME_PRIVADO.txt',
            'RESPALDO PRIVADO DE LA FIRMA DE HOGAR PLAYA NUEVA\n'
            'Guarda este ZIP en un lugar privado y conserva una segunda copia.\n'
            'Contiene la clave y su contraseña. No lo subas a GitHub ni lo compartas públicamente.\n'
            'Para futuras compilaciones restaura hogar.p12 y password en\n'
            '/workspace/.android-signing/hogar-playa-v2/ con permisos privados.\n'
            'El constructor compara el certificado con el fijado en el repositorio.\n')
    target.chmod(0o600)
    return target


def build(output=None, init=False, private=PRIVATE, backup=None):
    config = json.loads((ROOT / 'release.json').read_text())
    base = ROOT.parent / 'Hogar_Playa.apk'
    if digest(base.read_bytes()) != BASE_SHA256:
        raise RuntimeError('La APK original no coincide con la base verificada.')
    output = Path(output) if output else ROOT / 'build' / (config.get('apk_basename', 'Hogar_Playa_Nueva') + '_' + config['version_name'] + '.apk')
    if output.resolve() == base.resolve():
        raise RuntimeError('No se sustituye el instalador original.')
    tools = SDK / 'build-tools/35.0.0'
    android_jar = SDK / 'platforms/android-35/android.jar'
    for required in [tools / 'aapt2', tools / 'apksigner', tools / 'zipalign', android_jar]:
        if not required.is_file():
            raise RuntimeError('Falta herramienta Android: ' + str(required))
    key, password, certificate = signing_identity(init, private)
    with tempfile.TemporaryDirectory(prefix='hogar-release-') as tmp:
        stage = Path(tmp)
        tree = ET.parse(ROOT / 'android/resources/AndroidManifest.xml')
        manifest = tree.getroot()
        manifest.set('package', config['application_id'])
        manifest.set(ANDROID + 'versionCode', str(config['version_code']))
        manifest.set(ANDROID + 'versionName', config['version_name'])
        manifest.find('application').set(ANDROID + 'label', config['label'])
        tree.write(stage / 'AndroidManifest.xml', encoding='utf-8', xml_declaration=True)
        ids = ET.parse(ROOT / 'android/resources/res/values/public.xml').getroot()
        stable = ''.join(f"{config['application_id']}:{r.attrib['type']}/{r.attrib['name']} = {r.attrib['id']}\n" for r in ids)
        (stage / 'stable-ids.txt').write_text(stable)
        subprocess.run([str(tools / 'aapt2'), 'compile', '--dir', str(ROOT / 'android/resources/res'),
                        '-o', str(stage / 'resources.zip')], check=True)
        subprocess.run([str(tools / 'aapt2'), 'link', '-I', str(android_jar),
                        '--manifest', str(stage / 'AndroidManifest.xml'), '--stable-ids', str(stage / 'stable-ids.txt'),
                        '-o', str(stage / 'resources.apk'), str(stage / 'resources.zip')], check=True)
        unsigned = stage / 'unsigned.apk'
        with zipfile.ZipFile(unsigned, 'w') as target, zipfile.ZipFile(stage / 'resources.apk') as resources, zipfile.ZipFile(base) as original:
            for name in resources.namelist():
                target.writestr(name, resources.read(name), compress_type=zipfile.ZIP_STORED if name == 'resources.arsc' else zipfile.ZIP_DEFLATED)
            target.writestr('classes.dex', original.read('classes.dex'), compress_type=zipfile.ZIP_DEFLATED)
            for asset in sorted((ROOT / 'android/assets').iterdir()):
                if asset.is_file():
                    target.writestr('assets/' + asset.name, asset.read_bytes(), compress_type=zipfile.ZIP_DEFLATED)
        aligned = stage / 'aligned.apk'
        subprocess.run([str(tools / 'zipalign'), '-p', '-f', '4', str(unsigned), str(aligned)], check=True)
        output.parent.mkdir(parents=True, exist_ok=True)
        subprocess.run([str(tools / 'apksigner'), 'sign', '--ks', str(key),
                        '--ks-key-alias', config['key_alias'], '--ks-pass', 'file:' + str(password),
                        '--out', str(output), str(aligned)], check=True)
        verified = subprocess.check_output([str(tools / 'apksigner'), 'verify', '--verbose', '--print-certs', str(output)], text=True)
        if 'certificate SHA-256 digest: ' + certificate not in verified:
            raise RuntimeError('No coincide el certificado del instalador firmado.')
        subprocess.run([str(tools / 'zipalign'), '-c', '-p', '4', str(output)], check=True)
    with zipfile.ZipFile(output) as package, zipfile.ZipFile(base) as original:
        assert package.read('classes.dex') == original.read('classes.dex')
        assert package.read('assets/app.js') == (ROOT / 'android/assets/app.js').read_bytes()
        assert package.testzip() is None
    report = {**config, 'apk_sha256': digest(output.read_bytes()), 'certificate_sha256': certificate,
              'original_dex_unchanged': True, 'new_identity': True, 'signed': True}
    output.with_suffix('.json').write_text(json.dumps(report, indent=2) + '\n')
    output.with_suffix('.apk.sha256').write_text(report['apk_sha256'] + '  ' + output.name + '\n')
    if backup:
        private_backup(key, password, backup)
    return output


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--init-signing', action='store_true')
    parser.add_argument('--output', type=Path)
    parser.add_argument('--private-backup', type=Path)
    args = parser.parse_args()
    print('APK firmada y verificada:', build(args.output, args.init_signing, backup=args.private_backup))
