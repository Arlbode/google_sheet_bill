const assert=require('node:assert/strict'),test=require('node:test');
const F=require('../../1.6.0/assets/improvements.js');
const catalog=require('../../1.6.0/assets/catalog.json');
const defaults=catalog.products.filter(p=>p.bundledDefault),p=defaults.find(p=>p.id==='Espaguetis');
test('espaguetis sin precio o proveedor inventado y cantidad editable',()=>{
 assert(p);assert.equal(p.price,null);assert.equal(p.vendor,'');assert.equal(p.unit,'paquete');assert.equal(p.monthlyLimit,0);
 assert(p.aliases.includes('spaguetis'));assert.equal(defaults.length,1);
});
test('aparece con un catálogo antiguo del Sheet sin alterar sus productos',()=>{
 const remote=[{id:'Arroz',name:'Arroz',price:123,currency:'CUP'}],before=JSON.stringify(remote);
 const merged=F.mergeProducts(remote,[],defaults);assert.equal(merged.length,2);assert.equal(merged[1].id,'Espaguetis');assert.equal(JSON.stringify(remote),before);assert.equal(merged[0].price,123);
});
test('conserva identidad y precio cuando el Sheet ya tiene esa pasta',()=>{
 for(const name of ['Espaguetis','Spaguetis','Spaghetti']){
  const remote=[{id:'id-remoto',name,price:222,currency:'USD'}];const merged=F.mergeProducts(remote,[],defaults);
  assert.equal(merged.length,1);assert.equal(merged[0].id,'id-remoto');assert.equal(merged[0].price,222);assert.equal(merged[0].currency,'USD');
 }
});
test('la edición local y las fotos mantienen su UUID y no se duplican al reabrir',()=>{
 const edited={...p,name:'Espaguetis largos',price:300,image:'data:image/jpeg;base64,fixture'};
 const result=F.mergeProducts([], [edited],defaults);assert.equal(result.length,1);assert.equal(result[0].id,p.id);assert.equal(result[0].price,300);assert.equal(result[0].image,edited.image);
 const existing={id:'custom-pasta',name:'spaguetis',unit:'kg',price:44};assert.equal(F.mergeProducts([], [existing],defaults).length,1);
});
test('las llamadas anteriores de catálogo conservan su comportamiento',()=>{
 const base=[{id:'a',name:'A',price:1}],custom=[{id:'a',name:'A editado',price:2},{id:'b',name:'B'}];
 assert.deepEqual(F.mergeProducts(base,custom),[{id:'a',name:'A editado',price:2},{id:'b',name:'B'}]);assert.equal(base[0].price,1);
});
