#!/usr/bin/env python3
"""Solo admite la firma original, suministrada por rutas privadas. Nunca crea claves."""
from pathlib import Path
import argparse,hashlib,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
def sign(keystore,password_file,signer,output):
 keystore=Path(keystore);password_file=Path(password_file)
 if not keystore.is_file() or not password_file.is_file():raise RuntimeError('Falta la firma privada original o su archivo de contraseña. No se ha creado otra clave.')
 cert=subprocess.check_output(['java','-m','java.base/sun.security.tools.keytool.Main','-exportcert','-keystore',str(keystore),'-storepass:file',str(password_file),'-alias','hogar'],stderr=subprocess.PIPE)
 expected=(ROOT/'signing/expected-certificate.sha256').read_text().strip()
 if hashlib.sha256(cert).hexdigest()!=expected:raise RuntimeError('La clave no corresponde al certificado original. No se firmará una actualización incompatible.')
 from build_unsigned import build
 unsigned=build();output=Path(output);output.parent.mkdir(parents=True,exist_ok=True)
 subprocess.run([str(signer),'sign','--ks',str(keystore),'--ks-key-alias','hogar','--ks-pass','file:'+str(password_file),'--out',str(output),str(unsigned)],check=True)
 subprocess.run([str(signer),'verify','--verbose','--print-certs',str(output)],check=True)
 return output
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--keystore',required=True);p.add_argument('--password-file',required=True);p.add_argument('--apksigner',required=True);p.add_argument('--output',required=True);a=p.parse_args()
 try:print(sign(a.keystore,a.password_file,a.apksigner,a.output))
 except (RuntimeError,subprocess.CalledProcessError) as e:print('No se pudo firmar:',str(e),file=sys.stderr);sys.exit(1)
