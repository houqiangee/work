<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String version=System.currentTimeMillis()+"";
%>

<!DOCTYPE>
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>滋滋猴</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<link rel="stylesheet" type="text/css" href="./js/layui/css/layui.css">
	<link rel="stylesheet" type="text/css" href="./css/index.css?_=<%=version%>">
	<link rel="stylesheet" type="text/css" href="./css/global.css?_=<%=version%>">
	
	<script type="text/javascript" src="./js/jquery-3.3.1.min.js"></script>
	<script type="text/javascript" src="./js/layui/layui.js"></script>
	<script type="text/javascript" src="./js/AjaxUtil.js?_=<%=version%>"></script>
	<script type="text/javascript" src="./js/DataUtil.js?_=<%=version%>"></script>
  </head>
<body>
  <div id="index-top-div">
    <ul class="layui-nav" lay-filter="top_menu">
    	<div class="logo" >
      		<img src="./image/index/logo.png" alt="zizizhu">
    	</div>
		<li class="layui-nav-item layui-this"><a href="javascript:;" data-mk="cg">相似股票</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="xw">读个新闻</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="sp">看个视频</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="yx">玩个游戏</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="lt">意见建议</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="xq">联系我们</a></li>
		<li class="layui-nav-item"><a href="javascript:;" data-mk="tb">淘宝验证</a></li>
	</ul>
    <ul class="layui-nav layui-layout-right">
      <li class="layui-nav-item">
        <a href="javascript:;">
          <img src="http://t.cn/RCzsdCq" class="layui-nav-img">
        	  游客
        </a>
        <dl class="layui-nav-child">
          <dd><a href="">基本资料</a></dd>
          <dd><a href="">安全设置</a></dd>
        </dl>
      </li>
      <li class="layui-nav-item"><a href="">退了</a></li>
    </ul>
  </div>
  <div id="index-mid-div">
  </div>
  <div id="index-bot-div">
  主办单位：济南门似海网络科技有限公司
  <br>
  Operated by JiNan Mensihai network technology Co., Ltd.
  <br>
  &copy; 2018 Mensihai All Rights Reserved.
  </div>
</body>
<script>
function showIndexPage(mk){
	var url = "index_"+mk+".jsp?_="+new Date();  
	
	
	var data = {type:1};  
	$.ajax({  
	    type : "get",  
	    async : false, 
	    url : url,  
	    data : data,  
	    timeout:1000,  
	    success:function(dates){  
	        $("#index-mid-div").html(dates);
	    },  
	    error: function() {  
	    }  
	});  
}

layui.use('element', function(){
	var element = layui.element; //导航的hover效果、二级菜单等功能，需要依赖element模块
	  //监听导航点击
	element.on('nav(top_menu)', function(elem){
		var mk=elem.data("mk");
		showIndexPage(mk);
	});
});

showIndexPage($("#index-top-div .layui-nav-item.layui-this a").data("mk"));

</script> 
</html>
