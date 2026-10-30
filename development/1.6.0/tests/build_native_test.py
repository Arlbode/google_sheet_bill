from pathlib import Path
import subprocess,zipfile
R=Path(__file__).resolve().parent;SDK=Path('/workspace/.tools/hogar-android-sdk');W=Path('/workspace/hogar-1.6-build/instrumentation');W.mkdir(parents=True,exist_ok=True);T=SDK/'build-tools/35.0.0'
def run(args):subprocess.run([str(a) for a in args],check=True)
run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',SDK/'platforms/android-35/android.jar','-d',W/'classes',R/'NativeFeaturesSmoke.java',R/'UpgradeSeed.java'])
(W/'dex').mkdir(exist_ok=True);run([T/'d8','--lib',SDK/'platforms/android-35/android.jar','--min-api','26','--output',W/'dex']+list((W/'classes').rglob('*.class')))
run([T/'aapt2','link','--manifest',R/'AndroidManifest.xml','-I',SDK/'platforms/android-35/android.jar','-o',W/'unsigned.apk'])
with zipfile.ZipFile(W/'unsigned.apk','a') as z:z.write(W/'dex/classes.dex','classes.dex')
run([T/'zipalign','-f','4',W/'unsigned.apk',W/'aligned.apk']);run([T/'apksigner','sign','--ks','/workspace/.android-signing/hogar-playa-v2/hogar.p12','--ks-pass','file:/workspace/.android-signing/hogar-playa-v2/password','--out',W/'NativeFeaturesTest.apk',W/'aligned.apk'])
