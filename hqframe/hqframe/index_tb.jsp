<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<div class="index_tb_main" style="height: 500px;">
	<div id="tb_yzm_condiv" style="height: 100%;">
	</div>
	<div style="width:100px;height: 50px;font-size: 30px;line-height: 50px;background-color: blue;color:#FFFFFF;" onclick="saveAnswerAndGetNewImg()">下一组</div>
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
.eachyzm{border: 3px solid #111111;widows: 522px;float: left;height: 70px;}
</style>
<script>

function saveAnswerAndGetNewImg(){
	var url = "TaoBao.do?action=saveAnswerAndGetNewImg&rcount=10";  
	AjaxUtil.aReq(url,function(dates){
    	var result=JSON.parse(dates);
    	alert(result.vds);
    	DataUtil.filldata(result.vds,$("#eachyzm_mod")[0].innerHTML,$("#tb_yzm_condiv"));
	});
}

</script> 
