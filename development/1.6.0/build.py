"""Construye sobre 1.5.7 verificada; solo altera Bridge.call entre métodos existentes."""
from pathlib import Path
import hashlib,json,re,shutil,struct,subprocess,zipfile,os,copy
ROOT=Path(__file__).resolve().parent;REPO=ROOT.parents[1]
WORK=Path(os.environ.get('HOGAR_BUILD_DIR','/workspace/hogar-1.6-build'));SDK=Path(os.environ.get('ANDROID_HOME','/workspace/.tools/hogar-android-sdk'));JAR=Path(os.environ.get('HOGAR_JADX_JAR','/workspace/.tools/jadx.jar'))
BASE=REPO/'Hogar_finanzas_1.5.7.apk';BASE_SHA='9c03f4a3f64940eaf15fb0e6e9f17d24dd941f41dd6b27cfa0ac1cfe741877d3'
CALL=re.compile(r'\.method public call\(Ljava/lang/String;\)V[\s\S]*?\.end method');BRIDGE='cu/hogarplaya/MainActivity$Bridge.smali'
def run(args):subprocess.run([str(a) for a in args],check=True)
def manifest(raw):
    chunks=[];pos=8;strings=[];pool=None;flags=0
    while pos<len(raw):
        typ,hs,size=struct.unpack_from('<HHI',raw,pos);chunk=bytearray(raw[pos:pos+size]);chunks.append((typ,chunk))
        if typ==1:
            pool=len(chunks)-1;count,styles,flags,start,styleStart=struct.unpack_from('<IIIII',chunk,8);assert styles==0
            offsets=struct.unpack_from('<'+'I'*count,chunk,hs)
            for offset in offsets:
                i=start+offset
                def length8(i):
                    x=chunk[i];return (((x&127)<<8)|chunk[i+1],i+2) if x&128 else (x,i+1)
                if flags&0x100:
                    _,i=length8(i);length,i=length8(i);strings.append(bytes(chunk[i:i+length]).decode())
                else:
                    length=struct.unpack_from('<H',chunk,i)[0];i+=2
                    if length&0x8000:length=((length&0x7fff)<<16)|struct.unpack_from('<H',chunk,i)[0];i+=2
                    strings.append(bytes(chunk[i:i+length*2]).decode('utf-16le'))
        pos+=size
    assert pool is not None;assert strings.count('1.5.7')==1;strings[strings.index('1.5.7')]='1.6.0'
    name_index=len(strings);strings.append('cu.hogarplaya.HouseholdDocuments')
    offsets=[];data=bytearray()
    def len8(n):return bytes([0x80|(n>>8),n&255]) if n>127 else bytes([n])
    for s in strings:
        offsets.append(len(data))
        if flags&0x100:
            encoded=s.encode();data+=len8(len(s.encode('utf-16le'))//2)+len8(len(encoded))+encoded+b'\0'
        else:
            encoded=s.encode('utf-16le');n=len(encoded)//2;assert n<32768;data+=struct.pack('<H',n)+encoded+b'\0\0'
    while len(data)%4:data+=b'\0'
    count=len(strings);start=28+4*count
    chunks[pool]=(1,struct.pack('<HHIIIIII',1,28,start+len(data),count,0,flags,start,0)+struct.pack('<'+'I'*count,*offsets)+data)
    ids=[];activity=None;end_index=None;changed=0
    for index,(typ,ch) in enumerate(chunks):
        if typ==0x180:
            hs=struct.unpack_from('<H',ch,2)[0];ids=list(struct.unpack_from('<'+'I'*((len(ch)-hs)//4),ch,hs))
        if typ==0x102:
            elem=struct.unpack_from('<I',ch,20)[0];start,asz,n=struct.unpack_from('<HHH',ch,24)
            attrs=[]
            for i in range(n):
                at=16+start+i*asz;a=bytearray(ch[at:at+asz]);name=struct.unpack_from('<I',a,4)[0];resource=ids[name] if name<len(ids) else 0
                if resource==0x0101021b:assert struct.unpack_from('<I',a,16)[0]==17;struct.pack_into('<I',ch,at+16,18);changed+=1
                if strings[elem]=='activity' and resource in (0x01010003,0x01010010):
                    if resource==0x01010003:struct.pack_into('<I',a,8,name_index);struct.pack_into('<I',a,16,name_index)
                    else:struct.pack_into('<I',a,8,0xffffffff);struct.pack_into('<I',a,16,0)
                    attrs.append(a)
            if strings[elem]=='activity' and activity is None:
                assert len(attrs)==2;head=bytearray(ch[:36]);struct.pack_into('<I',head,4,36+40);struct.pack_into('<HHHHHH',head,24,20,20,2,0,0,0);activity=(head+b''.join(attrs),struct.pack('<HHIIIII',0x103,16,24,0,0xffffffff,0xffffffff,elem))
        if typ==0x103 and strings[struct.unpack_from('<I',ch,20)[0]]=='application':end_index=index
    assert changed==1 and activity and end_index is not None
    chunks[end_index:end_index]=[(0x102,activity[0]),(0x103,activity[1])]
    body=b''.join(ch for _,ch in chunks);return struct.pack('<HHI',3,8,len(body)+8)+body

def build():
    assert hashlib.sha256(BASE.read_bytes()).hexdigest()==BASE_SHA
    assert hashlib.sha256(JAR.read_bytes()).hexdigest()=='dc717e51c08d31a66357933c8d4896755d7c4939745e03aefd02427041f9aa53'
    WORK.mkdir(parents=True,exist_ok=True);java=['java','-cp',JAR];smali=WORK/'base-smali';shutil.rmtree(smali,ignore_errors=True)
    with zipfile.ZipFile(BASE) as z:(WORK/'base.dex').write_bytes(z.read('classes.dex'));binary_manifest=manifest(z.read('AndroidManifest.xml'))
    run(java+['com.android.tools.smali.baksmali.Main','disassemble',WORK/'base.dex','--output',smali]);baseline={p.relative_to(smali).as_posix():p.read_text() for p in smali.rglob('*.smali')}
    bridge=smali/BRIDGE;before=bridge.read_text();m=CALL.search(before);assert m and '.registers 4' in m.group()
    anchor='    iget-object v0, p0, Lcu/hogarplaya/MainActivity$Bridge;->this$0:Lcu/hogarplaya/MainActivity;'
    hook=anchor+'\n\n    invoke-static {v0, p1}, Lcu/hogarplaya/HouseholdFeatures;->intercept(Landroid/app/Activity;Ljava/lang/String;)Z\n\n    move-result v0\n\n    if-eqz v0, :features160_legacy\n\n    return-void\n\n    :features160_legacy\n'
    bridge.write_text(before[:m.start()]+m.group().replace(anchor,hook+'\n'+anchor,1)+before[m.end():])
    classes=WORK/'classes';shutil.rmtree(classes,ignore_errors=True);classes.mkdir();sources=list((ROOT/'native').glob('*.java'))+list((ROOT/'compile-stubs').rglob('*.java'))
    run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-source','8','-target','8','-Xlint:-options','-classpath',SDK/'platforms/android-35/android.jar','-d',classes]+sources)
    own=[p for p in classes.rglob('*.class') if p.stem.split('$')[0] in ('HouseholdFeatures','HouseholdDocuments','BackupCipher')]
    with zipfile.ZipFile(WORK/'stubs.jar','w') as z:
        for p in classes.rglob('*.class'):
            if p not in own:z.write(p,p.relative_to(classes).as_posix())
    dex=WORK/'extension';shutil.rmtree(dex,ignore_errors=True);dex.mkdir();run([SDK/'build-tools/35.0.0/d8','--lib',SDK/'platforms/android-35/android.jar','--classpath',WORK/'stubs.jar','--min-api','26','--output',dex]+own)
    added=WORK/'added';shutil.rmtree(added,ignore_errors=True);run(java+['com.android.tools.smali.baksmali.Main','disassemble',dex/'classes.dex','--output',added])
    for p in added.rglob('*.smali'):
        destination=smali/p.relative_to(added);assert not destination.exists();destination.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,destination)
    run(java+['com.android.tools.smali.smali.Main','assemble','--api','26','--output',WORK/'classes.dex',smali])
    verify=WORK/'verify';shutil.rmtree(verify,ignore_errors=True);run(java+['com.android.tools.smali.baksmali.Main','disassemble',WORK/'classes.dex','--output',verify])
    # DEX reordena etiquetas por direcciones cuando el método cambia; el resto debe ser idéntico.
    for name,before in baseline.items():
        after=(verify/name).read_text()
        if name==BRIDGE:before=CALL.sub('',before);after=CALL.sub('',after)
        assert before==after,'Cambió método original: '+name
    tested=os.environ.get('HOGAR_VERIFIED_NATIVE_APK')
    if tested:
        verified=WORK/'runtime-verified.dex'
        with zipfile.ZipFile(tested) as z:verified.write_bytes(z.read('classes.dex'))
        decoded=WORK/'runtime-verified-smali';shutil.rmtree(decoded,ignore_errors=True)
        run(java+['com.android.tools.smali.baksmali.Main','disassemble',verified,'--output',decoded])
        actual={p.relative_to(verify).as_posix():p.read_text() for p in verify.rglob('*.smali')}
        previous={p.relative_to(decoded).as_posix():p.read_text() for p in decoded.rglob('*.smali')}
        assert actual==previous,'El DEX probado no corresponde al código recién compilado'
        shutil.copyfile(verified,WORK/'classes.dex')
    unsigned=WORK/'unsigned.apk'
    with zipfile.ZipFile(BASE) as z,zipfile.ZipFile(unsigned,'w') as out:
        for item in z.infolist():
            if item.filename.upper().startswith('META-INF/'):continue
            data=z.read(item)
            if item.filename=='AndroidManifest.xml':data=binary_manifest
            elif item.filename=='classes.dex':data=(WORK/'classes.dex').read_bytes()
            elif item.filename.startswith('assets/'):data=(ROOT/item.filename).read_bytes()
            out.writestr(copy.copy(item),data)
        out.write(ROOT/'assets/improvements.js','assets/improvements.js')
    run([SDK/'build-tools/35.0.0/zipalign','-f','-p','4',unsigned,WORK/'aligned.apk'])
    final=WORK/'Hogar_finanzas_1.6.0_CANDIDATA.apk';run([SDK/'build-tools/35.0.0/apksigner','sign','--ks','/workspace/.android-signing/hogar-playa-v2/hogar.p12','--ks-pass','file:/workspace/.android-signing/hogar-playa-v2/password','--out',final,WORK/'aligned.apk']);verification=subprocess.check_output([str(SDK/'build-tools/35.0.0/apksigner'),'verify','--verbose','--print-certs',str(final)],text=True);expected=(ROOT/'signing/new-certificate.sha256').read_text().strip();assert 'Signer #1 certificate SHA-256 digest: '+expected in verification,'Firma distinta de la identidad fijada';print(verification);run([SDK/'build-tools/35.0.0/aapt2','dump','badging',final])
    report={'baseSha256':BASE_SHA,'sha256':hashlib.sha256(final.read_bytes()).hexdigest(),'originalClassesVerified':len(baseline),'onlyOriginalMethodChanged':'MainActivity$Bridge.call','newNativeClasses':len(list(added.rglob('*.smali'))),'status':'candidate-not-published','nativeDeviceTests':'not-run'};(WORK/'build-report.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
if __name__=='__main__':build()
