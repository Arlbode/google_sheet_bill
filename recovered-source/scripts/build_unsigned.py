#!/usr/bin/env python3
"""Reempaqueta los assets conservando EXACTAMENTE el código nativo de la APK base.
No compila ni usa las referencias Java decompiladas. El resultado NO está firmado.
"""
from pathlib import Path
import hashlib, json, struct, zipfile
ROOT=Path(__file__).resolve().parents[1]
BASE_SHA256='bc855b31309b1fc3e9569cf16614a9952490b9304860c46010a9633ab38ea152'
def build(output=None):
 base=ROOT.parent/'Hogar_Playa.apk'
 if hashlib.sha256(base.read_bytes()).hexdigest()!=BASE_SHA256:
  raise RuntimeError('La APK base no coincide con la versión 1.3.0 verificada. Se detiene la recuperación.')
 output=Path(output) if output else ROOT/'build/Hogar_Playa_UNFIRMADA.apk';output.parent.mkdir(parents=True,exist_ok=True)
 with zipfile.ZipFile(base) as original,zipfile.ZipFile(output,'w') as target:
  for item in original.infolist():
   if item.filename.upper().startswith('META-INF/'):continue
   data=original.read(item.filename)
   if item.filename.startswith('assets/'):
    source=(ROOT/'android'/item.filename).resolve()
    if not source.is_relative_to((ROOT/'android/assets').resolve()):raise RuntimeError('Ruta de asset no válida.')
    data=source.read_bytes()
   info=zipfile.ZipInfo(item.filename,item.date_time);info.compress_type=item.compress_type;info.external_attr=item.external_attr
   if info.compress_type==zipfile.ZIP_STORED:
    offset=target.fp.tell()+30+len(info.filename.encode('utf-8'));pad=(-offset)%4
    if pad:info.extra=b'\x35\xd9'+struct.pack('<H',pad)+b'\0'*pad
   target.writestr(info,data)
 with zipfile.ZipFile(output) as built,zipfile.ZipFile(base) as original:
  if built.testzip() is not None:raise RuntimeError('El ZIP reconstruido está dañado.')
  for name in ['classes.dex','AndroidManifest.xml','resources.arsc']:
   if built.read(name)!=original.read(name):raise RuntimeError('Se modificó el núcleo original: '+name)
 manifest={'base_sha256':BASE_SHA256,'unsigned_sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'native_dex_unchanged':True,'signed':False,'installable_release':False}
 output.with_suffix('.json').write_text(json.dumps(manifest,indent=2)+'\n')
 return output
if __name__=='__main__':
 path=build();print('Paquete de comprobación generado:',path);print('SIN FIRMA: no es una nueva APK instalable ni sustituye a la publicada.')
