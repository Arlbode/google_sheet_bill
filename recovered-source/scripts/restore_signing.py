#!/usr/bin/env python3
"""Restaura la firma fijada sin cambiarla. Nunca imprime contraseñas ni claves."""
import argparse
import base64
import hashlib
import io
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

from cryptography.hazmat.primitives import hashes,serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from build_release import ROOT,PRIVATE,signing_identity


def decrypt(access_private,encrypted):
    access=json.loads(Path(access_private).read_text())
    envelope=json.loads(Path(encrypted).read_text())
    if access.get('format')!='hogar-acceso-privado-1' or envelope.get('format')!='hogar-firma-cifrada-1':
        raise ValueError('Formato de respaldo no válido.')
    if access['fingerprint']!=envelope['recipient']:
        raise ValueError('El acceso privado no corresponde al respaldo cifrado.')
    private=serialization.load_der_private_key(base64.b64decode(access['privateKey'],validate=True),password=None)
    session=private.decrypt(base64.b64decode(envelope['wrappedKey']),padding.OAEP(mgf=padding.MGF1(hashes.SHA256()),algorithm=hashes.SHA256(),label=None))
    clear=AESGCM(session).decrypt(base64.b64decode(envelope['nonce']),base64.b64decode(envelope['ciphertext']),None)
    if hashlib.sha256(clear).hexdigest()!=envelope['sha256']:
        raise ValueError('El respaldo no supera la verificación de integridad.')
    return clear


def restore(clear,target=PRIVATE):
    target=Path(target).resolve()
    if target.is_relative_to(ROOT.parent.resolve()):
        raise ValueError('La firma debe restaurarse fuera del repositorio.')
    if (target/'hogar.p12').is_file() and (target/'password').is_file():
        signing_identity(private=target)
        return target
    if target.exists() and any(target.iterdir()):
        raise ValueError('Hay archivos privados existentes; se conservan sin sobrescribir.')
    if len(clear)>1000000:
        raise ValueError('Tamaño de respaldo inesperado.')
    expected=(ROOT/'signing/new-certificate.sha256').read_text().strip()
    with zipfile.ZipFile(io.BytesIO(clear)) as archive,tempfile.TemporaryDirectory(prefix='hogar-firma-restore-') as tmp:
        names=archive.namelist()
        if len(names)!=4 or set(names)!={'hogar.p12','password','certificate.sha256','LEEME_PRIVADO.txt'}:
            raise ValueError('Contenido inesperado en el ZIP de firma.')
        if any(item.file_size>200000 for item in archive.infolist()):
            raise ValueError('Contenido de respaldo demasiado grande.')
        if archive.read('certificate.sha256').decode().strip()!=expected:
            raise ValueError('El respaldo corresponde a otra firma.')
        stage=Path(tmp)
        for name in ['hogar.p12','password']:
            path=stage/name;path.write_bytes(archive.read(name));path.chmod(0o600)
        signing_identity(private=stage)  # Comprueba el certificado REAL del PKCS12.
        target.mkdir(parents=True,exist_ok=True,mode=0o700)
        for name in ['hogar.p12','password']:
            shutil.copyfile(stage/name,target/name);(target/name).chmod(0o600)
    signing_identity(private=target)
    return target


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    group=parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--private-zip',type=Path)
    group.add_argument('--access-private',type=Path)
    parser.add_argument('--encrypted',type=Path)
    parser.add_argument('--target',type=Path,default=PRIVATE)
    args=parser.parse_args()
    if args.access_private and not args.encrypted:parser.error('Falta --encrypted.')
    data=args.private_zip.read_bytes() if args.private_zip else decrypt(args.access_private,args.encrypted)
    print('Firma restaurada y verificada:',restore(data,args.target))
