<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<div class="index_tb_main" style="height: 500px;">
	<div id="tb_yzm_condiv" style="height: 100%;">
	</div>
	<input id="hcxyz" value="此处按Tab进入下一组" onblur="saveAnswerAndGetNewImg()"></input>
	<input value="" ></input>
	<div style="margin:auto;width:100px;height: 50px;font-size: 30px;line-height: 50px;background-color: blue;color:#FFFFFF;text-align: center;" onclick="saveAnswerAndGetNewImg()">下一组</div>
</div>

<div id="eachyzm_mod" style="display: none;">
	{{#  layui.each(d.data, function(index, item){ console.log(item); }}
	  <div class="eachyzm">
		<img id="yzmimg" style="width:320;height: 70px;display: block;float: left;" src="{{ item.imgurl }}"></img>
		<input id="answer" style="width:200px;height: 65px;font-size: 30px;line-height: 65px;float: left;margin-left: 2px; "></input>
		<div class="clearfloat"></div>
	  </div>
	{{#  }); }}
	<div class="clearfloat"></div>
</div>

<style>
.eachyzm{border: 3px solid #111111;widows: 522px;float: left;height: 70px;margin-left: 10px;margin-top: 10px;}
</style>
<script>

function saveAnswerAndGetNewImg(){
	var url = "TaoBao.do?action=saveAnswerAndGetNewImg&rcount=10";
	var jsonarr=[];
	$("#tb_yzm_condiv .eachyzm").each(function(){
		var self=this;
		var jo={};
		var imgurl=$(self).find("#yzmimg").attr("src");
		var answer=$(self).find("#answer").val();
		if(imgurl==null || ""==imgurl || answer==null ||""==answer ){
	    	return false;
	    }
		jo["imgurl"]=imgurl;
		jo["answer"]=answer;
		jsonarr.push(jo);
	});
	url+="&data="+JSON.stringify(jsonarr);
	AjaxUtil.aReq(url,function(dates){
    	var result=JSON.parse(dates);
    	DataUtil.filldataList(result.vds,$("#eachyzm_mod")[0].innerHTML,$("#tb_yzm_condiv"));
    	setTimeout(function(){$($("#tb_yzm_condiv .eachyzm #answer")[0]).focus();},500);
    	
	});
}

</script> 
