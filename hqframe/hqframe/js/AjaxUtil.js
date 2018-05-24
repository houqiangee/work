var AjaxUtil=(function(){
	var robj={};
	
	robj.REQ=function(url){
		var url = "index_"+mk+".jsp?_="+new Date();  
		var data = {type:1};  
		$.ajax({  
		    type : "get",  
		    async : false, 
		    url : url,  
		    data : data,  
		    timeout:1000,  
		    success:function(dates){  
		    },  
		    error: function() {  
		    }  
		});  
	};
	
	robj.aREQ=function(){
		
	};
	
	return robj;
})();