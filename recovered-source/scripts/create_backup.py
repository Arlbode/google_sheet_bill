#!/usr/bin/env python3
"""Respaldo reproducible de fuentes recuperadas; excluye secretos y compilaciones.

El manifiesto comprueba integridad, no sustituye una firma de procedencia.
No lee ni modifica datos de la app instalada ni la hoja del usuario.
"""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import zipfile

ROOT = Path(__file__).resolve().parents[2]
BACKUP_NAME = 'Hogar_Playa_Recuperacion_Fuentes.zip'
MANIFEST = 'MANIFIESTO_RESPALDO.json'
ROOT_FILES = (
    '.gitignore', 'AGENTS.md', 'README.md', 'RECUPERACION.md', 'LEEME_PRIMERO.md',
    'RESPALDO_Y_RECUPERACION_FIRMA.md', 'Herramienta_Respaldo_Firma.zip',
    'Hogar_Playa.apk', 'Hogar_Playa.apk.sha256', 'Script_Completo_Hogar_Android.txt',
    'Manual_Android.md', 'Oferta_Yosvany_2026-10-27.csv',
    'Script_Completo_Hogar_Tasa_CUP.txt', 'INSTALAR_NUEVA_APK.md',
    'Hogar_Playa_Nueva_1.4.0.apk', 'Hogar_Playa_Nueva_1.4.0.apk.sha256',
)
EXCLUDED_PARTS = {'build', '__pycache__', '.venv', '.git', 'node_modules', '.android-signing'}
FORBIDDEN_SUFFIXES = {'.p12', '.pfx', '.jks', '.keystore', '.key', '.pem', '.pyc'}


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def safe_name(name):
    path = PurePosixPath(name)
    return bool(name) and not path.is_absolute() and '\\' not in name and all(
        part not in {'', '.', '..'} for part in name.split('/')
    )


def collect(root):
    root = Path(root).resolve()
    paths = [root / name for name in ROOT_FILES]
    paths += sorted((root / 'recovered-source').rglob('*'))
    workflow = root / '.github/workflows/verificar-recuperacion.yml'
    if workflow.is_file():
        paths.append(workflow)
    encrypted = root / 'Firma_Hogar_Playa_Cifrada.json'
    if encrypted.is_file():
        paths.append(encrypted)
    encrypted_bundle = root / 'Respaldo_Firma_Cifrado.zip'
    if encrypted_bundle.is_file():
        paths.append(encrypted_bundle)
    files = {}
    for path in paths:
        rel = path.relative_to(root)
        if set(rel.parts) & EXCLUDED_PARTS or path.is_dir():
            continue
        if path.is_symlink() or not path.resolve().is_relative_to(root):
            raise ValueError('El respaldo no admite enlaces: ' + str(rel))
        if (path.suffix.lower() in FORBIDDEN_SUFFIXES or path.name.startswith('.env')
                or 'password' in path.name.lower() or path.name.startswith(('Firma_PRIVADA','Clave_PRIVADA'))):
            raise ValueError('No se respaldan archivos de credenciales: ' + str(rel))
        data = path.read_bytes()
        # Construir los patrones evita que este propio código coincida con ellos.
        if any(b'-----BEGIN ' + kind + b'PRIVATE KEY-----' in data
               for kind in (b'', b'RSA ', b'EC ', b'ENCRYPTED ', b'OPENSSH ')):
            raise ValueError('Se detectó una clave privada en: ' + str(rel))
        files[rel.as_posix()] = data
    return files


def create(output=None, root=ROOT):
    root = Path(root)
    output = Path(output) if output else root / BACKUP_NAME
    files = collect(root)
    manifest = {
        'format': 'hogar-playa-recovery-2',
        'base_apk_sha256': sha256(files['Hogar_Playa.apk']),
        'private_signing_key_included': False,
        'encrypted_signing_backup_included': (root / 'Firma_Hogar_Playa_Cifrada.json').is_file(),
        'user_financial_data_included': False,
        'new_signed_apk_generated': True,
        'java_source': 'JADX reference only; original DEX retained in base APK',
        'files': {name: sha256(data) for name, data in sorted(files.items())},
    }
    files[MANIFEST] = (json.dumps(manifest, ensure_ascii=False, indent=2) + '\n').encode()
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for name, data in sorted(files.items()):
            info = zipfile.ZipInfo(name, date_time=(2026, 10, 27, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o100644 << 16
            archive.writestr(info, data)
    check(output)
    output.with_suffix(output.suffix + '.sha256').write_text(
        sha256(output.read_bytes()) + '  ' + output.name + '\n', encoding='utf-8'
    )
    return output


def check(path):
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)) or not all(safe_name(name) for name in names):
            raise ValueError('El ZIP contiene rutas no válidas o repetidas.')
        if archive.testzip() is not None:
            raise ValueError('El ZIP está dañado.')
        manifest = json.loads(archive.read(MANIFEST))
        if manifest['format'] != 'hogar-playa-recovery-2':
            raise ValueError('Formato de respaldo desconocido.')
        expected = manifest['files']
        if set(expected) != set(names) - {MANIFEST}:
            raise ValueError('El manifiesto y los archivos del ZIP no coinciden.')
        for name, digest in expected.items():
            if sha256(archive.read(name)) != digest:
                raise ValueError('El archivo no coincide con su SHA-256: ' + name)
        if sha256(archive.read('Hogar_Playa.apk')) != manifest['base_apk_sha256']:
            raise ValueError('La APK base no coincide con el manifiesto.')
        return len(expected)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', type=Path, help='Verificar un ZIP existente, sin extraerlo.')
    parser.add_argument('--output', type=Path, help='Ruta para crear el ZIP y su SHA-256.')
    args = parser.parse_args()
    if args.check:
        print('Respaldo verificado:', check(args.check), 'archivos; no se ha extraído nada.')
    else:
        print('Respaldo creado y verificado:', create(args.output))
