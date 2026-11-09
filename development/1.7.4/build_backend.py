"""Cambia solo la política de gastos reales; no ejecuta migraciones ni cambia conexiones."""
from pathlib import Path
import argparse,hashlib,json
ROOT=Path(__file__).resolve().parent
def patch(source):
 text=source.decode('utf-8');original=text
 header='''// HOGAR FINANZAS 1.7.4: COMPRAS INDIVIDUALES Y GASTOS REALES.
// Actualización sobre producción: conserva todos los registros y la conexión.
// Sustituir Código.gs y publicar NUEVA VERSIÓN de LA MISMA implementación /exec.
// NO ejecutar iniciarHogar, dejarFinanzasEnCeroParaProduccion, dejarListoParaProduccion ni reinicios.
// Después actualizar la APK y sincronizar. Las reservas avisan sin vetar gastos reales.

'''
 old_header=''
 if text.startswith('// LIMPIEZA FINANCIERA ÚNICA'):
  old_header=text[:text.index('function dejarFinanzasEnCeroParaProduccion()')]
  assert all(not line or line.startswith('//') for line in old_header.splitlines())
  text=text[len(old_header):]
 text=header+text
 needle='function validarReserva153_(ss,op){'
 assert text.count(needle)==1
 replacement=needle+'''\n // 1.7.4: un gasto describe una compra realizada, aunque exceda saldo o reservas.
 // movilValidar_ conserva autenticación, moneda, importe, fecha, formato, fondo,
 // detalle, planes y jornada. No se crean ingresos ni se retiran ahorros.
 if(op.type==='Gasto')return;
'''
 text=text.replace(needle,replacement,1)
 before='return {reserveVersion:reservasPreparadas153_(ss)?1:0,'
 after="return {expensePolicy:'record-actual-v1',reserveVersion:reservasPreparadas153_(ss)?1:0,"
 assert text.count(before)==1;text=text.replace(before,after,1)
 restored=text.replace(replacement,needle,1).replace(after,before,1)
 assert old_header+restored[len(header):]==original
 return text.encode('utf-8')
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--input',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args();data=patch(a.input.read_bytes());a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_bytes(data)
 print(json.dumps({'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest(),'changedExistingFunctions':['validarReserva153_','movilSnapshot_'],'allOtherExecutableSourceBytesPreserved':True,'updatedHeaderComments':True,'executedAgainstSheet':False}))
