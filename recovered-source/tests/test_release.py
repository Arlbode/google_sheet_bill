from pathlib import Path
import hashlib
import json
import sys
import tempfile
import unittest

ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'scripts'))
from build_release import signing_identity,build,private_backup


class ReleaseIdentity(unittest.TestCase):
    def test_pinned_identity_cannot_be_replaced_if_private_key_is_missing(self):
        with tempfile.TemporaryDirectory() as tmp:
            missing=Path(tmp)/'missing'
            with self.assertRaisesRegex(RuntimeError,'Restaura la firma privada'):
                signing_identity(init=True,private=missing)
            self.assertFalse(missing.exists())

    def test_new_app_has_separate_identity_and_public_pin(self):
        config=json.loads((ROOT/'release.json').read_text())
        self.assertEqual(config['application_id'],'cu.hogarplaya.recuperada')
        expected=(ROOT/'signing/new-certificate.sha256').read_text().strip()
        self.assertEqual(hashlib.sha256((ROOT/'signing/new-certificate.der').read_bytes()).hexdigest(),expected)
        self.assertNotEqual(expected,(ROOT/'signing/expected-certificate.sha256').read_text().strip())

    def test_release_cannot_overwrite_original_installer(self):
        base=ROOT.parent/'Hogar_Playa.apk'
        before=hashlib.sha256(base.read_bytes()).hexdigest()
        with self.assertRaisesRegex(RuntimeError,'No se sustituye'):
            build(output=base)
        self.assertEqual(hashlib.sha256(base.read_bytes()).hexdigest(),before)

    def test_private_backup_is_never_written_into_the_repository(self):
        with self.assertRaisesRegex(RuntimeError,'fuera del repositorio'):
            private_backup(Path('/missing-key'),Path('/missing-password'),ROOT/'private.zip')
        self.assertFalse((ROOT/'private.zip').exists())


if __name__=='__main__':unittest.main()
