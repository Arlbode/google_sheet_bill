/* Ledger in original currencies. Shared verbatim with Apps Script. */
var HogarReserves = (function () {
 'use strict';
 function bucket(o){return o.currency==='USD'?'usd':o.paymentFormat==='Transferencia'?'cupTransfer':'cupCash';}
 function empty(){return {usd:0,cupCash:0,cupTransfer:0};}
 function fund(o,names){return o.type==='Gasto'?(o.fundCategory||(names.indexOf(o.category)>=0?o.category:'')):'';}
 function reserve(o,names){return /^(Aporte fondo|Aporte ahorro|Retiro ahorro)$/.test(o.type)||!!fund(o,names);}
 function ledger(snapshot,operations){
  var rows=(snapshot.reserveLedger||snapshot.history||[]).slice(),seen={},known=snapshot.knownOperationIds||[];
  rows.forEach(function(o){seen[o.id]=true;});
  (operations||[]).forEach(function(o){if(!seen[o.id]&&o.status!=='REJECTED'&&o.status!=='SYNCED'&&known.indexOf(o.id)<0){rows.push(o);seen[o.id]=true;}});
  return rows;
 }
 function evaluate(rows,names){
  var savings=empty(),funds={},entries=[];names.forEach(function(name){funds[name]=empty();});
  rows.forEach(function(o){if(o.status==='REJECTED')return;var target,sign=1,name=fund(o,names),key=bucket(o),cents=Math.round(Number(o.amount)*100);
   if(!Number.isFinite(cents))return;
   if(o.type==='Aporte ahorro'||o.type==='Retiro ahorro'){target=savings;if(o.type==='Retiro ahorro')sign=-1;}
   else if(o.type==='Aporte fondo'){name=o.category;target=funds[name]||(funds[name]=empty());}
   else if(name){target=funds[name]||(funds[name]=empty());sign=-1;}
   if(target){target[key]+=sign*cents;entries.push(Object.assign({},o,{reserveName:name||'Ahorro general',reserveDelta:sign*cents/100}));}
  });
  var total=empty();Object.keys(total).forEach(function(k){total[k]=savings[k];Object.keys(funds).forEach(function(name){total[k]+=funds[name][k];});});
  [savings,total].concat(Object.keys(funds).map(function(k){return funds[k];})).forEach(function(b){Object.keys(b).forEach(function(k){b[k]/=100;});});
  return {savings:savings,funds:funds,total:total,entries:entries};
 }
 return {bucket:bucket,empty:empty,fund:fund,reserve:reserve,ledger:ledger,evaluate:evaluate};
})();
if(typeof module!=='undefined')module.exports=HogarReserves;
