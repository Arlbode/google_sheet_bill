from pathlib import Path
import subprocess,os
ADB='/workspace/.tools/hogar-android-sdk/platform-tools/adb';env=dict(os.environ,ANDROID_USER_HOME='/workspace/.tools/android-user');w=Path('/workspace/hogar-1.6.2')
def adb(*args):
 p=subprocess.run([ADB,'-s','emulator-5554',*args],env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,check=True);return p.stdout
assert any(x in adb('shell','getprop','ro.hardware') for x in ('ranchu','goldfish')),'Se rechaza dispositivo físico'
print(adb('install','--no-incremental','-r',str(w/'Hogar_finanzas_1.6.2.apk')),flush=True)
print(adb('install','--no-incremental','-r',str(w/'instrumentation/CaptureTest.apk')),flush=True)
text=adb('shell','am','instrument','-w','cu.hogarplaya.tests.capture162/cu.hogarplaya.tests.CaptureSmoke');(w/'capture-test.log').write_text(text);print(text,flush=True)
assert 'PASS:' in text and 'FAIL:' not in text and 'INSTRUMENTATION_FAILED' not in text,'No terminó la prueba de capturas'
