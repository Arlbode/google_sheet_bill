/** Lista colaborativa aislada: nunca registra compras ni modifica tablas financieras. */
function featuresNormalize160_(change,profiles) {
  movilAssert_(change&&movilUuid_(change.id)&&movilUuid_(change.entityId),'ID_INVALIDO','Cambio de lista inválido.');
  const actor=profiles[change.actorId];movilAssert_(actor&&actor.active,'PERFIL_INACTIVO','El perfil no está activo.');
  movilAssert_(Number.isSafeInteger(change.baseRevision)&&change.baseRevision>=0,'VERSION_INVALIDA','Revisa la versión de la lista.');
  const value=change.item||{},name=String(value.name||'').trim(),quantity=Number(value.quantity);
  movilAssert_(name.length>0&&name.length<=120&&Number.isFinite(quantity)&&quantity>0&&quantity<=100000,'LISTA_INVALIDA','Revisa nombre y cantidad.');
  const unit=String(value.unit||'unidad');movilAssert_(['lb','kg','litro','unidad','paquete'].includes(unit),'UNIDAD_INVALIDA','Unidad no admitida.');
  return {id:change.entityId,name,quantity,unit,bought:value.bought===true,deleted:value.deleted===true,actorId:change.actorId,actorName:actor.name};
}
function featuresDecision160_(change,current,receipt,profiles) {
  const item=featuresNormalize160_(change,profiles),hash=movilHash_(JSON.stringify([change.entityId,change.baseRevision,item]));
  if(receipt){movilAssert_(receipt.hash===hash,'ID_CONFLICTO','El identificador ya se utilizó con otros datos.');return {ack:{id:change.id,status:'duplicate'},item:null,hash};}
  if((current?current.revision:0)!==change.baseRevision)return {ack:{id:change.id,status:'conflict',current:current||null},item:null,hash};
  item.addedById=current?(current.addedById||current.actorId):item.actorId;item.addedByName=current?(current.addedByName||current.actorName):item.actorName;
  item.boughtById=item.bought?(current&&current.bought&&current.boughtById?current.boughtById:item.actorId):'';
  item.boughtByName=item.bought?(current&&current.bought&&current.boughtByName?current.boughtByName:item.actorName):'';
  item.createdAt=current&&current.createdAt||new Date().toISOString();
  item.revision=change.baseRevision+1;item.updatedAt=new Date().toISOString();return {ack:{id:change.id,status:'accepted'},item,hash};
}
function featuresSheet160_(ss,name,header){let sheet=ss.getSheetByName(name);if(!sheet){sheet=ss.insertSheet(name);sheet.appendRow(header);sheet.setFrozenRows(1);}return sheet;}
function featuresSync160_(ss,device,profiles,request){
  const changes=request.changes||[];movilAssert_(Array.isArray(changes)&&changes.length<=20,'LOTE_INVALIDO','Máximo 20 cambios por lote.');
  const list=featuresSheet160_(ss,'Lista compartida app',['ID','Versión','Datos JSON','Dispositivo','Actualizado']),ledger=featuresSheet160_(ss,'Recibos lista app',['ID cambio','Hash','Fecha','Dispositivo']);
  const rows=list.getDataRange().getValues().slice(1),current={},positions={};
  rows.forEach((r,i)=>{if(r[0]){current[r[0]]=JSON.parse(String(r[2]));positions[r[0]]=i+2;}});
  const receipts={};ledger.getDataRange().getValues().slice(1).forEach(r=>{if(r[0])receipts[r[0]]={hash:String(r[1])};});
  const ids=new Set(),acks=[];
  changes.forEach(change=>{
    movilAssert_(!ids.has(change.id),'ID_INVALIDO','Cambio repetido en el lote.');ids.add(change.id);
    const prior=current[change.entityId];
    if(!receipts[change.id]&&prior&&prior.lastChangeId===change.id){ledger.appendRow([change.id,prior.lastChangeHash,new Date(),device.id]);receipts[change.id]={hash:prior.lastChangeHash};}
    const receipt=receipts[change.id]||(prior&&prior.lastChangeId===change.id?{hash:prior.lastChangeHash}:null);
    const result=featuresDecision160_(change,prior,receipt,profiles);
    if(result.item){
      movilAssert_(current[change.entityId]||Object.keys(current).length<500,'LISTA_LLENA','La lista admite hasta 500 artículos, incluidos los retirados.');
      const row=positions[change.entityId]||list.getLastRow()+1;
      // Primero el estado junto con su recibo de operación. El reintento reconoce lastChange si falla appendRow.
      result.item.lastChangeId=change.id;result.item.lastChangeHash=result.hash;
      list.getRange(row,1,1,5).setValues([[change.entityId,result.item.revision,JSON.stringify(result.item),device.id,new Date()]]);
      current[change.entityId]=result.item;positions[change.entityId]=row;
      ledger.appendRow([change.id,result.hash,new Date(),device.id]);receipts[change.id]={hash:result.hash};
    }
    acks.push(result.ack);
  });
  SpreadsheetApp.flush();
  return {ok:true,apiVersion:1,householdId:request.householdId,featureAcks:acks,shared:Object.values(current)};
}
