#!/usr/bin/env python3
"""Cifra el respaldo con un código PÚBLICO generado en la computadora del usuario.
No solicita ni recibe la clave privada del destinatario.
"""
import argparse
import base64
import hashlib
import json
from pathlib import Path
import secrets

from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import padding, rsa
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


def encrypt(backup,public_code,output):
    code=public_code.strip()
    if not code.startswith('HOGAR-PUBLICA-1:') or len(code)>8192:
        raise ValueError('Se requiere solo el código PÚBLICO HOGAR-PUBLICA-1, no un archivo privado.')
    der=base64.b64decode(code.split(':',1)[1],validate=True)
    public=serialization.load_der_public_key(der)
    if not isinstance(public,rsa.RSAPublicKey) or public.key_size<2048:
        raise ValueError('Clave pública RSA no válida.')
    clear=Path(backup).read_bytes();session=secrets.token_bytes(32);nonce=secrets.token_bytes(12)
    wrapped=public.encrypt(session,padding.OAEP(mgf=padding.MGF1(hashes.SHA256()),algorithm=hashes.SHA256(),label=None))
    b64=lambda data:base64.b64encode(data).decode('ascii')
    envelope={'format':'hogar-firma-cifrada-1','recipient':hashlib.sha256(der).hexdigest(),
              'wrappedKey':b64(wrapped),'nonce':b64(nonce),'ciphertext':b64(AESGCM(session).encrypt(nonce,clear,None)),
              'sha256':hashlib.sha256(clear).hexdigest()}
    Path(output).write_text(json.dumps(envelope,indent=2)+'\n')
    return Path(output)


if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--backup',type=Path,required=True)
    parser.add_argument('--public-code-file',type=Path,required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    print('Respaldo cifrado:',encrypt(args.backup,args.public_code_file.read_text(),args.output))
