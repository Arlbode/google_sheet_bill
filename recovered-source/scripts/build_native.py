#!/usr/bin/env python3
"""Extensión escrita a mano; nunca compila el Java decompilado.

Conserva todos los métodos originales salvo la entrada Bridge.call, que delega
comandos nuevos. Verifica la equivalencia de los demás métodos tras ensamblar.
"""
from pathlib import Path
import argparse,hashlib,json,os,re,shutil,subprocess,tempfile,urllib.request,zipfile
from build_unsigned import BASE_SHA256

ROOT=Path(__file__).resolve().parents[1]
JAR_SHA='dc717e51c08d31a66357933c8d4896755d7c4939745e03aefd02427041f9aa53'
ZIP_SHA='12fd966431903b8e15c36e5007f19343475be7d8f2a55f082e7a929eeabc937e'
BRIDGE='cu/hogarplaya/MainActivity$Bridge.smali'
CALL=re.compile(r'\.method public call\(Ljava/lang/String;\)V[\s\S]*?\.end method')


def tool_jar():
 target=Path(os.environ.get('HOGAR_NATIVE_TOOLS_CACHE',str(ROOT/'build/native-tools')))/'jadx-1.5.1-all.jar'
 for candidate in [target,Path('/workspace/.tools/hogar-native/jadx-1.5.1-all.jar'),Path('/tmp/hogar-recovery-tools/jadx/lib/jadx-1.5.1-all.jar')]:
  if candidate.is_file() and hashlib.sha256(candidate.read_bytes()).hexdigest()==JAR_SHA:
   target.parent.mkdir(parents=True,exist_ok=True)
   if candidate!=target:shutil.copyfile(candidate,target)
   return target
 with tempfile.TemporaryDirectory(prefix='hogar-native-tools-') as tmp:
  package=Path(tmp)/'jadx.zip'
  with urllib.request.urlopen('https://github.com/skylot/jadx/releases/download/v1.5.1/jadx-1.5.1.zip',timeout=60) as r,package.open('wb') as out:shutil.copyfileobj(r,out)
  if hashlib.sha256(package.read_bytes()).hexdigest()!=ZIP_SHA:raise RuntimeError('Checksum incorrecto del ensamblador oficial.')
  with zipfile.ZipFile(package) as z:
   name=next(n for n in z.namelist() if n.endswith('lib/jadx-1.5.1-all.jar'))
   data=z.read(name)
   if hashlib.sha256(data).hexdigest()!=JAR_SHA:raise RuntimeError('Checksum incorrecto de la herramienta Java.')
   target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
 return target


def build(original,output,sdk):
 output=Path(output);jar=tool_jar();java=['java','-cp',str(jar)]
 disassemble=lambda dex,directory:subprocess.run(java+['com.android.tools.smali.baksmali.Main','disassemble',str(dex),'--output',str(directory)],check=True)
 with tempfile.TemporaryDirectory(prefix='hogar-native-build-') as tmp:
  stage=Path(tmp);base=stage/'base.dex';base.write_bytes(original)
  smali=stage/'smali';disassemble(base,smali)
  baseline={p.relative_to(smali).as_posix():p.read_text() for p in smali.rglob('*.smali')}
  bridge=smali/BRIDGE;source=bridge.read_text();match=CALL.search(source)
  if not match:raise RuntimeError('No se encuentra la entrada del puente base verificado.')
  method=match.group()
  anchor='    sget-object v0, Lcu/hogarplaya/SyncEngine;->WORK:Ljava/util/concurrent/ExecutorService;'
  hook='''    iget-object v0, p0, Lcu/hogarplaya/MainActivity$Bridge;->this$0:Lcu/hogarplaya/MainActivity;

    invoke-static {v0, p1}, Lcu/hogarplaya/ShoppingExtension;->intercept(Landroid/app/Activity;Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :shopping_legacy

    return-void

    :shopping_legacy
'''
  if method.count(anchor)!=1 or '.registers 4' not in method:raise RuntimeError('Cambió el contrato del puente base; no se parchea.')
  bridge.write_text(source[:match.start()]+method.replace(anchor,hook+anchor)+source[match.end():])
  classes=stage/'classes';classes.mkdir()
  sources=list((ROOT/'android/compile-stubs').rglob('*.java'))+list((ROOT/'android/native-src').rglob('*.java'))
  subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',str(sdk/'platforms/android-35/android.jar'),'-d',str(classes)]+[str(p) for p in sources],check=True)
  own=('ShoppingExtension','ShoppingLifecycle','PurchaseLocation','PurchaseActions')
  real=[p for p in classes.rglob('*.class') if p.stem.split('$')[0] in own]
  with zipfile.ZipFile(stage/'stubs.jar','w') as stubs:
   for p in classes.rglob('*.class'):
    if p not in real:stubs.write(p,p.relative_to(classes).as_posix())
  dex=stage/'extension';dex.mkdir()
  subprocess.run([str(sdk/'build-tools/35.0.0/d8'),'--lib',str(sdk/'platforms/android-35/android.jar'),'--classpath',str(stage/'stubs.jar'),'--min-api','26','--output',str(dex)]+[str(p) for p in real],check=True)
  added=stage/'added';disassemble(dex/'classes.dex',added)
  added_names=[]
  for p in added.rglob('*.smali'):
   relative=p.relative_to(added);destination=smali/relative
   if destination.exists():raise RuntimeError('La extensión intenta sustituir una clase original: '+str(relative))
   destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,destination);added_names.append(relative.as_posix())
  output.parent.mkdir(parents=True,exist_ok=True)
  subprocess.run(java+['com.android.tools.smali.smali.Main','assemble','--api','26','--output',str(output),str(smali)],check=True)
  verify=stage/'verify';disassemble(output,verify)
  for name,before in baseline.items():
   after=(verify/name).read_text()
   if name==BRIDGE:before=CALL.sub('',before);after=CALL.sub('',after)
   # DEX omite el valor estático booleano falso, que sigue siendo el inicial por defecto.
   if name=='cu/hogarplaya/BuildFlags.smali':before=before.replace('.field public static final TESTING:Z = false','.field public static final TESTING:Z')
   if before!=after:raise RuntimeError('Se modificaron instrucciones originales fuera del punto autorizado: '+name)
  return {'original_methods_preserved_except':['MainActivity$Bridge.call'],
          'added_classes':sorted(added_names),'native_sha256':hashlib.sha256(output.read_bytes()).hexdigest()}


if __name__=='__main__':
 parser=argparse.ArgumentParser(description=__doc__)
 parser.add_argument('--output',type=Path,required=True)
 parser.add_argument('--sdk',type=Path,default=Path(os.environ.get('ANDROID_HOME','/workspace/.tools/hogar-android-sdk')))
 args=parser.parse_args()
 if hashlib.sha256((ROOT.parent/'Hogar_Playa.apk').read_bytes()).hexdigest()!=BASE_SHA256:raise RuntimeError('La APK base no coincide con la copia verificada.')
 with zipfile.ZipFile(ROOT.parent/'Hogar_Playa.apk') as original:
  result=build(original.read('classes.dex'),args.output,args.sdk)
 print(json.dumps(result,indent=2))
