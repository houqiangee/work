var laytpl;
var layer;
layui.use(['laytpl','layer'], function(){
  laytpl = layui.laytpl;
  layer = layui.layer;
});

var DataUtil=(function(){
	var exports={};
	
	exports.filldataList=function(dsjson,getTpl,$target){
		var d=[];
		if(typeof(dsjson)=="string"){
			d=JSON.parse(dsjson);
		}else{
			d=dsjson;
		}
		
		var data={};
		data["data"]=d;
		console.log(data);
		var view = $target[0];
		if(!view){
			alert("Î´ÕÒµ½Ä¿±êdiv£º"+target_div);
			return false;
		}
		laytpl(getTpl).render(data, function(html){
		    view.innerHTML = html;
		});
	};
	
	return exports;
})();