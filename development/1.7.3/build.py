"""Añade reinicio de producción autorizado conservando los despachadores financieros originales."""
from native_build import rebuild
from pathlib import Path
import copy,hashlib,struct,subprocess,zipfile,json,os
ROOT=Path(__file__).resolve().parent;REPO=ROOT.parents[1];ASSETS=ROOT.parent/'1.6.0/assets';BASE=Path(os.environ.get('HOGAR_BASE_APK','/workspace/hogar-1.7.2/Hogar_finanzas_1.7.2.apk'));OUT=Path(os.environ.get('HOGAR_GUIDE_BUILD','/workspace/hogar-1.7.3'));TOOLS=Path('/workspace/.tools/hogar-android-sdk/build-tools/35.0.0')
EXPECTED='09ba6138dd09e329a0b2abbde45bb1609f6a07dc4f03525fe1ddef1c18758b89';CERT='7374684814395e3df3e4a41b6d0d20a739dddba331a828d0f72d5d37ea78e6c1'
def run(args):subprocess.run([str(a) for a in args],check=True)
def build():
 assert hashlib.sha256(BASE.read_bytes()).hexdigest()==EXPECTED;OUT.mkdir(parents=True,exist_ok=True);native=rebuild(BASE)
 with zipfile.ZipFile(BASE) as z:
  manifest=bytearray(z.read('AndroidManifest.xml'));pos=8;ids=[];changes=0
  while pos<len(manifest):
   kind,header,size=struct.unpack_from('<HHI',manifest,pos)
   if kind==0x180:ids=list(struct.unpack_from('<'+'I'*((size-header)//4),manifest,pos+header))
   if kind==0x102:
    start,attrsize,count=struct.unpack_from('<HHH',manifest,pos+24)
    for i in range(count):
     attr=pos+16+start+i*attrsize;index=struct.unpack_from('<I',manifest,attr+4)[0]
     if index<len(ids) and ids[index]==0x0101021b:
      assert struct.unpack_from('<I',manifest,attr+16)[0]==30;struct.pack_into('<I',manifest,attr+16,31);changes+=1
   pos+=size
  assert changes==1;replacements=0
  for old,new in [(b'1.7.2',b'1.7.3'),('1.7.2'.encode('utf-16le'),'1.7.3'.encode('utf-16le'))]:replacements+=manifest.count(old);manifest=manifest.replace(old,new)
  assert replacements==1
  modified=['assets/app.js','assets/app.css','assets/guide.js','assets/improvements.js']
  with zipfile.ZipFile(OUT/'unsigned.apk','w') as out:
   for item in z.infolist():
    if item.filename.upper().startswith('META-INF/'):continue
    data=z.read(item)
    if item.filename=='AndroidManifest.xml':data=manifest
    elif item.filename=='classes.dex':data=native.read_bytes()
    elif item.filename in modified:data=(ASSETS/Path(item.filename).name).read_bytes()
    out.writestr(copy.copy(item),data)
 run([TOOLS/'zipalign','-f','-p','4',OUT/'unsigned.apk',OUT/'aligned.apk']);apk=OUT/'Hogar_finanzas_1.7.3.apk'
 run([TOOLS/'apksigner','sign','--ks','/workspace/.android-signing/hogar-playa-v2/hogar.p12','--ks-pass','file:/workspace/.android-signing/hogar-playa-v2/password','--out',apk,OUT/'aligned.apk'])
 info=subprocess.check_output([str(TOOLS/'apksigner'),'verify','--verbose','--print-certs',str(apk)],text=True);assert 'Signer #1 certificate SHA-256 digest: '+CERT in info
 badging=subprocess.check_output([str(TOOLS/'aapt2'),'dump','badging',str(apk)],text=True);assert "name='cu.hogarplaya.recuperada' versionCode='31' versionName='1.7.3'" in badging;run([TOOLS/'zipalign','-c','-p','4',apk])
 with zipfile.ZipFile(BASE) as a,zipfile.ZipFile(apk) as b:
  assert b.testzip() is None
  for name in a.namelist():
   if name in ['AndroidManifest.xml','classes.dex'] or name in modified or name.upper().startswith('META-INF/'):continue
   assert a.read(name)==b.read(name),name
 report={'versionName':'1.7.3','versionCode':31,'baseSha256':EXPECTED,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'bytes':apk.stat().st_size,'certificateSha256':CERT,'originalNativeClassesPreserved':True,'compiledResourcesIdenticalTo172':True,'modifiedAssets':modified,'signatureVerified':True,'alignmentVerified':True};(OUT/'build-report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':build()
