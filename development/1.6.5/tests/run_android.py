from pathlib import Path
import subprocess,os
ADB='/workspace/.tools/hogar-android-sdk/platform-tools/adb';env=dict(os.environ,ANDROID_USER_HOME='/workspace/.tools/android-user');W=Path('/workspace/hogar-1.6.5')
def adb(*args):
 p=subprocess.run([ADB,'-s','emulator-5554',*args],env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,check=True);print(p.stdout,flush=True);return p.stdout
hardware=adb('shell','getprop','ro.hardware').strip();assert hardware in ['ranchu','goldfish'],hardware
adb('install','-r','--no-streaming',str(W/'instrumentation/UpdateTest.apk'))
result=adb('shell','am','instrument','-w','-r','cu.hogarplaya.tests.update165/cu.hogarplaya.tests.update165.UpdateSmoke');(W/'android-test.log').write_text(result);assert 'PASS: Android TLS1.2' in result and 'FAIL:' not in result
