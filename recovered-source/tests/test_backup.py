from pathlib import Path
import json
import sys
import tempfile
import unittest
import zipfile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'scripts'))
from create_backup import create, check, MANIFEST, safe_name


class Backup(unittest.TestCase):
    def test_backup_is_reproducible_and_contains_required_sources(self):
        with tempfile.TemporaryDirectory() as tmp:
            first = create(Path(tmp) / 'first.zip')
            second = create(Path(tmp) / 'second.zip')
            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertGreater(check(first), 100)
            with zipfile.ZipFile(first) as archive:
                manifest = json.loads(archive.read(MANIFEST))
                for name in ['Hogar_Playa.apk', 'LEEME_PRIMERO.md',
                             'recovered-source/backend/Code.gs',
                             'recovered-source/android/assets/app.js']:
                    self.assertIn(name, manifest['files'])
                self.assertFalse(manifest['private_signing_key_included'])
                self.assertFalse(any('/build/' in name or '__pycache__' in name
                                     or name.endswith(('.p12', '.jks', '.keystore'))
                                     for name in archive.namelist()))

    def test_rejects_changed_file_and_unsafe_paths(self):
        with tempfile.TemporaryDirectory() as tmp:
            good = create(Path(tmp) / 'good.zip')
            bad = Path(tmp) / 'bad.zip'
            with zipfile.ZipFile(good) as source, zipfile.ZipFile(bad, 'w') as target:
                for name in source.namelist():
                    data = source.read(name)
                    if name == 'recovered-source/android/assets/app.js':
                        data += b'\nchanged'
                    target.writestr(name, data)
            with self.assertRaisesRegex(ValueError, 'SHA-256'):
                check(bad)
            for name in ('../key', '/absolute', 'a/../../key', 'a\\key', 'a//b'):
                self.assertFalse(safe_name(name), name)


if __name__ == '__main__':
    unittest.main()
