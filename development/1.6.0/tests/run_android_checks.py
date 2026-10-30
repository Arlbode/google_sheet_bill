"""Lanza solo instrumentación sobre emulador identificado. Nunca acepta un teléfono físico."""
from pathlib import Path
import os,subprocess,sys
ADB='/workspace/.tools/hogar-android-sdk/platform-tools/adb';SERIAL='emulator-5554';WORK=Path('/workspace/hogar-1.6-build');env=dict(os.environ,ANDROID_USER_HOME='/workspace/.tools/android-user')
def adb(*args):
 p=subprocess.run([ADB,'-s',SERIAL,*args],env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,check=True);return p.stdout
hardware=adb('shell','getprop','ro.hardware').strip();assert any(v in hardware for v in ('ranchu','goldfish')),'Se rechaza cualquier dispositivo que no sea emulador'
assert adb('shell','getprop','ro.kernel.qemu').strip()=='1','El destino no es un emulador'
if '--reset' not in sys.argv:raise SystemExit('Se requiere --reset: borra solo la app de pruebas en el emulador identificado.')
print(adb('install','--no-incremental','-r',str(WORK/'Hogar_finanzas_1.6.0_CANDIDATA.apk')),flush=True)
print(adb('install','--no-incremental','-r',str(WORK/'instrumentation/NativeFeaturesTest.apk')),flush=True)
print(adb('shell','pm','clear','cu.hogarplaya.recuperada'),flush=True)
result=adb('shell','am','instrument','-w','cu.hogarplaya.tests.features160/cu.hogarplaya.tests.NativeFeaturesSmoke');(WORK/'native-test.log').write_text(result)
print(result,flush=True)
if 'PASS:' not in result or 'FAIL:' in result or 'INSTRUMENTATION_FAILED' in result:raise SystemExit('La instrumentación no completó sus comprobaciones')
