var AjaxUtil=(function(){
	var exports={};
	
	exports.Req=function(url){
		var data = {};  
		$.ajax({  
		    type : "get",  
		    async : false, 
		    url : url,  
		    data : data,  
		    timeout:10000,  
		    success:function(dates){  
		    	try{
		    		var re=JSON.parse(dates);
		    		if(re._hqglo_error_type=="bus"){//ҵ���쳣
		    			layer.alert(re._hqglo_error_msg, {title:'',icon: 7,btnAlign: 'c',shadeClose:true,closeBtn:false}); 
		    		}else if(re._hqglo_error_type=="userless"){//û��¼
		    			
		    		}else if(re._hqglo_error_type=="sys"){//�����쳣
		    			layer.msg(re._hqglo_error_msg, {time: 2000}); 
		    		}else{
		    			return dates;
		    		}
		    	}catch (e) {
					return dates;
				}
		    },  
		    error: function() {  
		    }  
		});  
	};
	
	exports.aReq=function(url,callback){
		var data = {};  
		$.ajax({  
		    type : "get",  
		    async : true, 
		    url : url,  
		    data : data,  
		    timeout:10000,  
		    success:function(dates){  
		    	try{
		    		var re=JSON.parse(dates);
		    		if(re._hqglo_error_type=="bus"){//ҵ���쳣
		    			layer.alert(re._hqglo_error_msg, {title:'',icon: 7,btnAlign: 'c',shadeClose:true,closeBtn:false}); 
		    		}else if(re._hqglo_error_type=="userless"){//û��¼
		    			
		    		}else if(re._hqglo_error_type=="sys"){//�����쳣
		    			layer.msg(re._hqglo_error_msg, {time: 2000}); 
		    		}else{
		    			if(callback){
		    				callback.call(null,dates);
		    			}
		    		}
		    	}catch (e) {
					return dates;
				}
		    },  
		    error: function() {  
		    }  
		}); 
	};
	
	exports.RefArea=function($obj,url){
		var data = {};  
		$.ajax({  
		    type : "get",  
		    async : false, 
		    url : url,  
		    data : data,  
		    timeout:10000,  
		    success:function(dates){  
		    	try{
		    		var re=JSON.parse(dates);
		    		if(re._hqglo_error_type=="bus"){//ҵ���쳣
		    			layer.alert(re._hqglo_error_msg, {title:'',icon: 7,btnAlign: 'c',shadeClose:true,closeBtn:false}); 
		    		}else if(re._hqglo_error_type=="userless"){//û��¼
		    			
		    		}else if(re._hqglo_error_type=="sys"){//�����쳣
		    			layer.msg(re._hqglo_error_msg, {time: 2000}); 
		    		}else{
		    			$obj.html(dates);
		    		}
		    	}catch (e) {
					return dates;
				}
		    },  
		    error: function() {  
		    }  
		});  
	};
	
	exports.aRefArea=function($obj,url,callback){
		var data = {};  
		$.ajax({  
		    type : "get",  
		    async : true, 
		    url : url,  
		    data : data,  
		    timeout:10000,  
		    success:function(dates){  
		    	try{
		    		var re=JSON.parse(dates);
		    		if(re._hqglo_error_type=="bus"){//ҵ���쳣
		    			layer.alert(re._hqglo_error_msg, {title:'',icon: 7,btnAlign: 'c',shadeClose:true,closeBtn:false}); 
		    		}else if(re._hqglo_error_type=="userless"){//û��¼
		    			
		    		}else if(re._hqglo_error_type=="sys"){//�����쳣
		    			layer.msg(re._hqglo_error_msg, {time: 2000}); 
		    		}else{
		    			$obj.html(dates);
		    			if(callback){
		    				callback.call(null,dates);
		    			}
		    		}
		    	}catch (e) {
					return dates;
				}
		    },  
		    error: function() {  
		    }  
		}); 
	};
	
	return exports;
})();