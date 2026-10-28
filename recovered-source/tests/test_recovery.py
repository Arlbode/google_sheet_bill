from pathlib import Path
import hashlib,json,sys,tempfile,unittest,zipfile
ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'scripts'))
from build_unsigned import build,BASE_SHA256
from sign_original import sign
class Recovery(unittest.TestCase):
 def test_exact_assets_and_backend(self):
  values=json.loads((ROOT/'exact-sha256.json').read_text())
  changed={'assets/app.js','assets/app.css','assets/index.html'}
  with zipfile.ZipFile(ROOT.parent/'Hogar_Playa.apk') as original:
   for name,digest in values.items():
    if name.startswith('assets/'):
     self.assertEqual(hashlib.sha256(original.read(name)).hexdigest(),digest,'base '+name)
     if name not in changed:self.assertEqual(hashlib.sha256((ROOT/'android'/name).read_bytes()).hexdigest(),digest,name)
    else:
     self.assertEqual(hashlib.sha256((ROOT.parent/'Script_Completo_Hogar_Android.txt').read_bytes()).hexdigest(),digest,'backend original')
  self.assertEqual(len(json.loads((ROOT/'android/assets/catalog.json').read_text())['products']),91)
  self.assertTrue((ROOT/'backend/Code.gs').read_text().rstrip().endswith('// FIN_DEL_CODIGO'))
 def test_data_recovery(self):
  data=json.loads((ROOT/'data/precios.json').read_text());spec=json.loads((ROOT/'data/plantilla.json').read_text())
  self.assertEqual(len(data['products']),91);self.assertEqual(len(data['oferta']['rows']),56)
  self.assertIn('Fondo gasolina',spec['funds']);self.assertEqual(next(p for p in data['products'] if p['name']=='Guayaba')['unit'],'lb')
 def test_unsigned_package_preserves_native_code_and_resources(self):
  with tempfile.TemporaryDirectory() as tmp:
   output=build(Path(tmp)/'UNFIRMADA.apk')
   with zipfile.ZipFile(output) as z,zipfile.ZipFile(ROOT.parent/'Hogar_Playa.apk') as original:
    for name in ['classes.dex','resources.arsc','AndroidManifest.xml']:self.assertEqual(z.read(name),original.read(name))
    self.assertFalse(any(n.startswith('META-INF/') for n in z.namelist()))
    for item in z.infolist():
     if item.compress_type==zipfile.ZIP_STORED:
      with output.open('rb') as f:f.seek(item.header_offset+26);name_len=int.from_bytes(f.read(2),'little');extra_len=int.from_bytes(f.read(2),'little')
      self.assertEqual((item.header_offset+30+name_len+extra_len)%4,0,item.filename)
   self.assertFalse(json.loads(output.with_suffix('.json').read_text())['signed'])
   with zipfile.ZipFile(output) as z:
    for name in ['app.js','app.css','index.html']:
     self.assertEqual(z.read('assets/'+name),(ROOT/'android/assets'/name).read_bytes())
 def test_original_apk_unchanged(self):self.assertEqual(hashlib.sha256((ROOT.parent/'Hogar_Playa.apk').read_bytes()).hexdigest(),BASE_SHA256)
 def test_no_replacement_key_generated(self):
  with tempfile.TemporaryDirectory() as tmp:
   root=Path(tmp)
   with self.assertRaisesRegex(RuntimeError,'Falta la firma privada original'):sign(root/'missing.p12',root/'missing-password',root/'missing-signer',root/'output.apk')
   self.assertEqual(list(root.iterdir()),[])
if __name__=='__main__':unittest.main()
