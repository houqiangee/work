
//重写alert
Window.prototype.alert=function(msg){
	layer.alert(msg, {title:'',icon: 7,btnAlign: 'c',shadeClose:true,closeBtn:false,anim: 6});
}

