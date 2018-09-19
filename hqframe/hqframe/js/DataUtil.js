var laytpl;
var layer;
layui.use(['laytpl','layer'], function(){
  laytpl = layui.laytpl;
  layer = layui.layer;
});

var DataUtil=(function(){
	var exports={};
	
	exports.filldata=function(dsjson,getTpl,$target){
		var data={};
		data["data"]=dsjson;
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