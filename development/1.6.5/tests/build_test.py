from pathlib import Path
import subprocess,zipfile,struct,hashlib,json,copy
R=Path(__file__).resolve().parent;SDK=Path('/workspace/.tools/hogar-android-sdk');W=Path('/workspace/hogar-1.6.5/instrumentation');W.mkdir(parents=True,exist_ok=True);T=SDK/'build-tools/35.0.0'
def run(args):subprocess.run([str(a) for a in args],check=True)
def sign(src,dst):
 run([T/'zipalign','-f','4',src,W/'aligned.apk']);run([T/'apksigner','sign','--ks','/workspace/.android-signing/hogar-playa-v2/hogar.p12','--ks-pass','file:/workspace/.android-signing/hogar-playa-v2/password','--out',dst,W/'aligned.apk'])
# Candidata futura exclusivamente ficticia para comprobar el verificador de Android.
with zipfile.ZipFile(W.parent/'Hogar_finanzas_1.6.5.apk') as a,zipfile.ZipFile(W/'future-unsigned.apk','w') as out:
 m=bytearray(a.read('AndroidManifest.xml'));pos=8;ids=[];changed=0
 while pos<len(m):
  kind,header,size=struct.unpack_from('<HHI',m,pos)
  if kind==0x180:ids=list(struct.unpack_from('<'+'I'*((size-header)//4),m,pos+header))
  if kind==0x102:
   start,attrsize,count=struct.unpack_from('<HHH',m,pos+24)
   for i in range(count):
    attr=pos+16+start+i*attrsize;index=struct.unpack_from('<I',m,attr+4)[0]
    if index<len(ids) and ids[index]==0x0101021b:
     assert struct.unpack_from('<I',m,attr+16)[0]==23;struct.pack_into('<I',m,attr+16,24);changed+=1
  pos+=size
 assert changed==1
 replaced=0
 for old,new in [(b'1.6.5',b'1.6.6'),('1.6.5'.encode('utf-16le'),'1.6.6'.encode('utf-16le'))]:replaced+=m.count(old);m=m.replace(old,new)
 assert replaced==1
 for item in a.infolist():
  if not item.filename.upper().startswith('META-INF/'):out.writestr(copy.copy(item),m if item.filename=='AndroidManifest.xml' else a.read(item))
sign(W/'future-unsigned.apk',W/'fixture.apk')
data=(W/'fixture.apk').read_bytes();manifest={'applicationId':'cu.hogarplaya.recuperada','versionName':'1.6.6','versionCode':24,'minSdk':26,'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest(),'certificateSha256':'7374684814395e3df3e4a41b6d0d20a739dddba331a828d0f72d5d37ea78e6c1','url':'https://raw.githubusercontent.com/Arlbode/google_sheet_bill/actualizaciones-android/Hogar_finanzas_1.6.6.apk','notes':'Solo fixture local; no publicar.'};(W/'fixture.json').write_text(json.dumps(manifest))
run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',SDK/'platforms/android-35/android.jar','-d',W/'classes',R/'UpdateSmoke.java'])
(W/'dex').mkdir(exist_ok=True);run([T/'d8','--lib',SDK/'platforms/android-35/android.jar','--min-api','26','--output',W/'dex']+list((W/'classes').rglob('*.class')))
run([T/'aapt2','link','--manifest',R/'AndroidManifest.xml','-I',SDK/'platforms/android-35/android.jar','-o',W/'unsigned.apk'])
with zipfile.ZipFile(W/'unsigned.apk','a') as z:
 z.write(W/'dex/classes.dex','classes.dex')
 for name in ['fixture.apk','fixture.json']:z.write(W/name,'assets/'+name)
sign(W/'unsigned.apk',W/'UpdateTest.apk')
