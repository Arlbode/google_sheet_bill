import urllib.request,xml.etree.ElementTree as E,hashlib,zipfile,io,concurrent.futures,os
from pathlib import Path
root=Path('/workspace/.tools');sdk=root/'hogar-android-sdk';sdk.mkdir(exist_ok=True)
def fetch(u):
 with urllib.request.urlopen(u,timeout=120) as r:return r.read()
x=E.fromstring(fetch('https://dl.google.com/android/repository/repository2-1.xml'))
def install(path,dest):
 p=next(n for n in x if n.tag.endswith('remotePackage') and n.get('path')==path)
 archive=next(n for n in p.findall('.//archive') if n.findtext('host-os') in [None,'linux']);complete=archive.find('complete');data=fetch('https://dl.google.com/android/repository/'+complete.findtext('url'));assert hashlib.sha1(data).hexdigest()==complete.findtext('checksum')
 z=zipfile.ZipFile(io.BytesIO(data));names=z.namelist();prefix=names[0].split('/')[0]+'/'
 for n in names:
  if n.endswith('/'):continue
  rel=n[len(prefix):];target=sdk/dest/rel;target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(z.read(n))
  mode=(z.getinfo(n).external_attr>>16)&0o777
  if mode:target.chmod(mode)
 print('Verificado e instalado:',path,flush=True)
def jadx():
 data=fetch('https://github.com/skylot/jadx/releases/download/v1.5.1/jadx-1.5.1.zip');z=zipfile.ZipFile(io.BytesIO(data));n=next(n for n in z.namelist() if n.endswith('.jar') and 'jadx-1.5.1-all' in n);jar=z.read(n);assert hashlib.sha256(jar).hexdigest()=='dc717e51c08d31a66357933c8d4896755d7c4939745e03aefd02427041f9aa53';(root/'jadx.jar').write_bytes(jar);print('JADX verificado',flush=True)
def base():
 data=fetch('https://raw.githubusercontent.com/Arlbode/google_sheet_bill/actualizaciones-android/Hogar_finanzas_1.6.5.apk');assert hashlib.sha256(data).hexdigest()=='ec57a5f86aa6a0adc3f260a45997daf4113e0351280af94d69fb6e026af97ea3';Path('/workspace/hogar-1.6.5/Hogar_finanzas_1.6.5.apk').write_bytes(data);print('APK base verificada',flush=True)
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as e:
 fs=[e.submit(install,'build-tools;35.0.0',Path('build-tools/35.0.0')),e.submit(install,'platforms;android-35',Path('platforms/android-35')),e.submit(jadx),e.submit(base)]
 for f in fs:f.result()
url='https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar'
data=fetch(url);assert hashlib.sha1(data).hexdigest()==fetch(url+'.sha1').decode().strip();(root/'json.jar').write_bytes(data)
