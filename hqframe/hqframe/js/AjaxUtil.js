var AjaxUtil=(function(){
	var robj={};
	
	robj.REQ=function(url){
		var data = {};  
		$.ajax({  
		    type : "get",  
		    async : false, 
		    url : url,  
		    data : data,  
		    timeout:1000,  
		    success:function(dates){  
		    	try{
		    		var re=JSON.parse(dates);
		    		if(re._hqglo_error_type="bus"){//业务异常
		    			
		    		}else if(re._hqglo_error_type="userless"){//没登录
		    			
		    		}else if(re._hqglo_error_type="sys"){//代码异常
		    			
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
	
	robj.aREQ=function(url,callback){
		
	};
	
	return robj;
})();