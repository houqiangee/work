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
		    		if(re._hqglo_error_type="bus"){//ҵ���쳣
		    			
		    		}else if(re._hqglo_error_type="userless"){//û��¼
		    			
		    		}else if(re._hqglo_error_type="sys"){//�����쳣
		    			
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