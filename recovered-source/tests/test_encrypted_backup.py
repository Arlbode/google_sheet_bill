import base64
import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest

from cryptography.hazmat.primitives import hashes,serialization
from cryptography.hazmat.primitives.asymmetric import rsa,padding
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'scripts'))
from encrypt_signing_backup import encrypt


class EncryptedBackup(unittest.TestCase):
    def test_public_envelope_requires_the_local_private_key(self):
        private=rsa.generate_private_key(public_exponent=65537,key_size=2048)
        der=private.public_key().public_bytes(serialization.Encoding.DER,serialization.PublicFormat.SubjectPublicKeyInfo)
        code='HOGAR-PUBLICA-1:'+base64.b64encode(der).decode()
        with tempfile.TemporaryDirectory() as tmp:
            source=Path(tmp)/'private.zip';source.write_bytes(b'dummy signing backup; not a real key')
            target=encrypt(source,code,Path(tmp)/'encrypted.json');data=json.loads(target.read_text())
            key=private.decrypt(base64.b64decode(data['wrappedKey']),padding.OAEP(mgf=padding.MGF1(hashes.SHA256()),algorithm=hashes.SHA256(),label=None))
            plain=AESGCM(key).decrypt(base64.b64decode(data['nonce']),base64.b64decode(data['ciphertext']),None)
            self.assertEqual(plain,source.read_bytes())
            self.assertEqual(data['sha256'],hashlib.sha256(plain).hexdigest())
            self.assertNotIn('privateKey',data)
            self.assertNotIn('dummy signing backup',target.read_text())

    def test_rejects_private_file_instead_of_public_code(self):
        with self.assertRaisesRegex(ValueError,'PÚBLICO'):
            encrypt(Path('/missing'),'hogar-acceso-privado-1',Path('/not-written'))

    def test_published_backup_is_bound_to_the_provided_public_key(self):
        public_der=(ROOT/'signing/recovery-recipient-public.der').read_bytes()
        public=serialization.load_der_public_key(public_der)
        data=json.loads((ROOT.parent/'Firma_Hogar_Playa_Cifrada.json').read_text())
        self.assertEqual(public.key_size,3072)
        self.assertEqual(data['recipient'],hashlib.sha256(public_der).hexdigest())
        self.assertEqual(len(base64.b64decode(data['wrappedKey'])),public.key_size//8)
        self.assertEqual(len(base64.b64decode(data['nonce'])),12)
        self.assertGreater(len(base64.b64decode(data['ciphertext'])),16)
        self.assertNotIn('privateKey',data)


if __name__=='__main__':unittest.main()
