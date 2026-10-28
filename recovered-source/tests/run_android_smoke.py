"""Prueba nativa sobre el emulador local Hogar29. No usa un teléfono real.

Dependencia opcional: adb-shell==0.4.4. La instrumentación no se publica en la APK.
"""
from pathlib import Path
import argparse,json,os,subprocess,tempfile,zipfile
from adb_shell.adb_device import AdbDeviceTcp
import sys
ROOT=Path(__file__).resolve().parents[1];sys.path.insert(0,str(ROOT/'scripts'))
from build_release import signing_identity


def build_test(output):
    sdk=Path('/workspace/.tools/hogar-android-sdk');tools=sdk/'build-tools/35.0.0'
    key,password,_=signing_identity()
    with tempfile.TemporaryDirectory(prefix='hogar-android-test-build-') as tmp:
        stage=Path(tmp);classes=stage/'classes';classes.mkdir();dex=stage/'dex';dex.mkdir()
        subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',str(sdk/'platforms/android-35/android.jar'),'-d',str(classes),str(ROOT/'tests/android/ShoppingSmoke.java')],check=True)
        subprocess.run([str(tools/'d8'),'--lib',str(sdk/'platforms/android-35/android.jar'),'--min-api','26','--output',str(dex)]+[str(p) for p in classes.rglob('*.class')],check=True)
        (stage/'AndroidManifest.xml').write_text('<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="cu.hogarplaya.smoke"><uses-sdk android:minSdkVersion="26" android:targetSdkVersion="29"/><application android:label="Pruebas internas"/><instrumentation android:name="cu.hogarplaya.smoke.ShoppingSmoke" android:targetPackage="cu.hogarplaya.recuperada"/></manifest>')
        subprocess.run([str(tools/'aapt2'),'link','-I',str(sdk/'platforms/android-35/android.jar'),'--manifest',str(stage/'AndroidManifest.xml'),'-o',str(stage/'resources.apk')],check=True)
        with zipfile.ZipFile(stage/'unsigned.apk','w') as z,zipfile.ZipFile(stage/'resources.apk') as r:
            for name in r.namelist():z.writestr(name,r.read(name))
            z.write(dex/'classes.dex','classes.dex')
        subprocess.run([str(tools/'apksigner'),'sign','--ks',str(key),'--ks-key-alias','hogar-v2','--ks-pass','file:'+str(password),'--out',str(output),str(stage/'unsigned.apk')],check=True)


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--reset',action='store_true');parser.add_argument('--phase',choices=['all','seed','verify','gps','nogps','persisted','purchase','keyboard','daily','dailyReopen','dailyLoggedOut','tasalo','tasaloReopen'],default='all');args=parser.parse_args()
    device=AdbDeviceTcp('127.0.0.1',5555,default_transport_timeout_s=30);device.connect(auth_timeout_s=5)
    assert device.shell('getprop ro.kernel.qemu').strip()=='1','Solo se permite el emulador de pruebas.'
    def shell(command,timeout=120):return device.shell(command,read_timeout_s=timeout,transport_timeout_s=timeout)
    def install(path):
        target='/data/local/tmp/hogar-smoke.apk';device.push(str(path),target)
        answer=shell('pm install -r '+target);assert 'Success' in answer,answer
    def phase(name):
        shell('am force-stop cu.hogarplaya.recuperada')
        answer=shell('am instrument -w -e phase '+name+' cu.hogarplaya.smoke/cu.hogarplaya.smoke.ShoppingSmoke',240)
        print(answer,flush=True);assert 'ANDROID SMOKE OK: '+name in answer,answer
    try:
        shell('input keyevent 82');shell('settings put system screen_off_timeout 2147483647')
        with tempfile.TemporaryDirectory(prefix='hogar-instrumentation-') as tmp:
            test=Path(tmp)/'test.apk';build_test(test)
            if args.reset:
                shell('pm uninstall cu.hogarplaya.smoke');shell('pm uninstall cu.hogarplaya.recuperada')
            if args.phase in ['all','seed']:
                install(ROOT.parent/'Hogar_finanzas_1.4.1.apk');install(test);phase('seed')
            else:install(test)
            if args.phase in ['all','verify']:
                config=json.loads((ROOT/'release.json').read_text());install(ROOT/('build/'+config['apk_basename']+'_'+config['version_name']+'.apk'));phase('verify')
            if args.phase in ['all','gps']:
                shell('settings put secure location_mode 3')
                shell('pm grant cu.hogarplaya.recuperada android.permission.ACCESS_COARSE_LOCATION')
                shell('pm grant cu.hogarplaya.recuperada android.permission.ACCESS_FINE_LOCATION')
                shell('appops set cu.hogarplaya.recuperada android:mock_location allow')
                phase('gps')
            if args.phase in ['all','nogps']:
                shell('settings put secure location_mode 0');phase('nogps')
            if args.phase in ['all','persisted']:
                shell('pm revoke cu.hogarplaya.recuperada android.permission.ACCESS_FINE_LOCATION')
                shell('pm revoke cu.hogarplaya.recuperada android.permission.ACCESS_COARSE_LOCATION')
                phase('persisted')
            if args.phase in ['all','purchase']:
                shell('pm grant cu.hogarplaya.recuperada android.permission.ACCESS_COARSE_LOCATION')
                shell('pm grant cu.hogarplaya.recuperada android.permission.ACCESS_FINE_LOCATION')
                shell('appops set cu.hogarplaya.recuperada android:mock_location allow')
                shell('settings put secure location_mode 3')
                shell('settings put secure show_ime_with_hard_keyboard 1')
                phase('purchase')
            if args.phase in ['all','keyboard']:
                shell('settings put secure show_ime_with_hard_keyboard 1')
                phase('keyboard')
            for scenario in ['daily','dailyReopen','dailyLoggedOut','tasalo','tasaloReopen']:
                if args.phase in ['all',scenario]:phase(scenario)
    finally:device.close()


if __name__=='__main__':main()
