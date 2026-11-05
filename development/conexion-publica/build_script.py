"""Corrige solo generación/GET y añade configuración; no toca registros ni la migración."""
from pathlib import Path
import re,argparse,hashlib,json
ROOT=Path(__file__).resolve().parent
GENERATOR='''function generarCodigoAndroid() {
  const url=conexionVerificarAndroid_(conexionUrlAndroid_());
  return conBloqueo_(()=>{
    const ss=hoja_();prepararAppMovil_(ss);
    const code=Utilities.getUuid().replace(/-/g,'').slice(0,12).toUpperCase(),expires=Date.now()+10*60*1000;
    PropertiesService.getScriptProperties().setProperty('MOVIL_PAIR',JSON.stringify({hash:movilHash_(code),expires,attempts:0}));
    return {code:code.match(/.{1,4}/g).join('-'),expires:new Date(expires).toISOString(),url};
  });
}
'''
GET='''function doGet() {return movilJson_({ok:true,app:'Hogar Playa',apiVersion:1,connectionProject:conexionProyectoAndroid_(),connectionVersion:2,message:'Conecta la APK con el código que genera Hogar → Conectar app Android.'});}
'''
def patch(source):
 text=source.decode('utf-8');original=text;changed=[]
 for name,replacement in [('generarCodigoAndroid',GENERATOR),('doGet',GET)]:
  pattern=re.compile(r'^function '+name+r'\([^\r\n]*\)[^\r\n]*\{.*?^\}\r?\n' if name!='doGet' else r'^function doGet\([^\r\n]*\}[^\r\n]*\r?\n',re.M|re.S)
  found=list(pattern.finditer(text));assert len(found)==1,(name,len(found))
  match=found[0];before=match.group();newline='\r\n' if '\r\n' in before else '\n';after=replacement.replace('\n',newline)
  text=text[:match.start()]+after+text[match.end():];changed.append((before,after))
 # Restaurar las dos regiones modificadas debe recuperar exactamente la entrada.
 restored=text
 for before,after in changed:assert restored.count(after)==1;restored=restored.replace(after,before,1)
 assert restored==original
 assert 'function configurarConexionAndroid(' not in text
 text+='\n\n// CONEXION PUBLICA ANDROID: URL /exec verificada\n'+(ROOT/'conexion-publica.gs').read_text()+'\n'
 return text.encode('utf-8')
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--input',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();data=patch(a.input.read_bytes());a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_bytes(data);print(json.dumps({'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest(),'changedExistingFunctions':['generarCodigoAndroid','doGet'],'allOtherSourceBytesPreserved':True}))
