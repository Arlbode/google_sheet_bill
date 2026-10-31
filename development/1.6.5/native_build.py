from pathlib import Path
import subprocess,zipfile,shutil,os,hashlib
ROOT=Path(__file__).resolve().parent;SRC=ROOT.parent/'1.6.0';WORK=Path('/workspace/hogar-1.6.5/native');SDK=Path('/workspace/.tools/hogar-android-sdk');JAR=Path('/workspace/.tools/jadx.jar')
def run(args):subprocess.run([str(a) for a in args],check=True)
def rebuild(base):
 assert hashlib.sha256(JAR.read_bytes()).hexdigest()=='dc717e51c08d31a66357933c8d4896755d7c4939745e03aefd02427041f9aa53'
 WORK.mkdir(parents=True,exist_ok=True);java=['java','-cp',JAR]
 with zipfile.ZipFile(base) as z:(WORK/'base.dex').write_bytes(z.read('classes.dex'))
 smali=WORK/'smali';shutil.rmtree(smali,ignore_errors=True);run(java+['com.android.tools.smali.baksmali.Main','disassemble',WORK/'base.dex','--output',smali]);baseline={p.relative_to(smali).as_posix():p.read_text() for p in smali.rglob('*.smali')}
 own=lambda name:name.startswith('cu/hogarplaya/HouseholdFeatures') or name.startswith('cu/hogarplaya/HouseholdDocuments') or name.startswith('cu/hogarplaya/CompatibleUpdate') or name.startswith('cu/hogarplaya/UpdateTransfer')
 classes=WORK/'classes';shutil.rmtree(classes,ignore_errors=True);classes.mkdir();run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',SDK/'platforms/android-35/android.jar','-d',classes]+list((SRC/'native').glob('*.java'))+list((SRC/'compile-stubs').rglob('*.java')))
 real=[p for p in classes.rglob('*.class') if p.stem.split('$')[0] in ('HouseholdFeatures','HouseholdDocuments','CompatibleUpdate','UpdateTransfer')]
 with zipfile.ZipFile(WORK/'stubs.jar','w') as z:
  for p in classes.rglob('*.class'):
   if p not in real:z.write(p,p.relative_to(classes).as_posix())
 dex=WORK/'added-dex';shutil.rmtree(dex,ignore_errors=True);dex.mkdir();run([SDK/'build-tools/35.0.0/d8','--lib',SDK/'platforms/android-35/android.jar','--classpath',WORK/'stubs.jar','--min-api','26','--output',dex]+real)
 added=WORK/'added';shutil.rmtree(added,ignore_errors=True);run(java+['com.android.tools.smali.baksmali.Main','disassemble',dex/'classes.dex','--output',added])
 for p in list(smali.rglob('*.smali')):
  if own(p.relative_to(smali).as_posix()):p.unlink()
 for p in added.rglob('*.smali'):
  name=p.relative_to(added).as_posix();assert own(name);target=smali/name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,target)
 output=WORK/'classes.dex';run(java+['com.android.tools.smali.smali.Main','assemble','--api','26','--output',output,smali]);verify=WORK/'verify';shutil.rmtree(verify,ignore_errors=True);run(java+['com.android.tools.smali.baksmali.Main','disassemble',output,'--output',verify])
 for name,before in baseline.items():
  if not own(name):assert (verify/name).read_text()==before,'Cambió código no autorizado: '+name
 print('Verificado: solo cambian las extensiones autorizadas; AppUpdates, su verificación y todo el código financiero permanecen iguales.');return output
