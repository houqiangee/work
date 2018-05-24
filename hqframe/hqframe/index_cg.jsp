<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
String version=System.currentTimeMillis()+"";
%>
<link rel="stylesheet" type="text/css" href="./css/index_cg.css?_=<%=version%>">
<script type="text/javascript" src="./js/echarts.js"></script>
<div id="index-cg-main">
	<div id="cg-top">
		<div style="width: 700px;margin-left: auto;margin-right: auto;">
			<form class="layui-form" action="" lay-filter="cg-top-form">
				<div class="layui-form-item layui-form-mid">
					<div class="layui-inline">
						<div class="layui-input-inline">
							<select name="jysc">
								<option value="">请选择交易市场</option>
								<option value="h" selected="">沪</option>
								<option value="s">深</option>
							</select>
						</div>
						<div class="layui-input-inline">
							<input type="text" name="gpdm" lay-verify="title" autocomplete="off" placeholder="请输入股票代码" class="layui-input">
						</div>
						<div class="layui-input-inline">
							<button class="layui-btn" lay-submit="" lay-filter="fxgp">分析</button>
						</div>
					</div>
				</div>
			</form>
		</div>
	</div>
	<div id="cg-k-line-div" >
		<div id="cg-k-line" style="height: 500px;width: 1000px;margin-left: auto;margin-right: auto" >
		
		</div>
	</div>
</div>
<script>

layui.use(['form', 'layedit'], function(){
	var form = layui.form
	,layer = layui.layer
	,layedit = layui.layedit;
	
	//监听提交
	form.on('submit(fxgp)', function(data){
		showOneStockK(data.field.gpdm,data.field.jysc);
		return false;
	});
	
	//表单初始赋值
	form.val('cg-top-form', {
		"gpdm": "600000"
	})
});

function showOneStockK(gpdm,jysc){
	if(gpdm==null ||gpdm==""){
		layer.msg('请填写股票代码'); 
		return false;
	}
	if(jysc==null ||jysc==""){
		layer.msg('请选择交易市场'); 
		return false;
	}
	
	var url = "Stock.do?action=showOneStockK&gpdm="+gpdm+"&jysc="+jysc+"&_="+new Date();  
	var data = {type:1};  
	$.ajax({  
	    type : "get",  
	    async : false, 
	    url : url,  
	    data : data,  
	    timeout:1000,  
	    success:function(dates){  
	    	var result=JSON.parse(dates);
	    	var re=result.re;
	    	var eachday=re.split(",");
	    	var rawData=[];
	    	for(var i=0;i<eachday.length;i++){
	    		if(eachday[i]!=null && eachday[i]!=""){
	    			rawData.push(eachday[i].split("#"));
	    		}
	    	}
	    	
	    	function calculateMA(dayCount, data) {
	    	    var result = [];
	    	    for (var i = 0, len = data.length; i < len; i++) {
	    	        if (i < dayCount) {
	    	            result.push('-');
	    	            continue;
	    	        }
	    	        var sum = 0;
	    	        for (var j = 0; j < dayCount; j++) {
	    	            sum += data[i - j][1];
	    	        }
	    	        result.push(sum / dayCount);
	    	    }
	    	    return result;
	    	}


	    	var dates = rawData.map(function (item) {
	    	    return item[0];
	    	});

	    	var data = rawData.map(function (item) {
	    	    return [+item[1], +item[2], +item[5], +item[6]];
	    	});
	    	var option = {
	    	    backgroundColor: '#21202D',
	    	    legend: {
	    	        data: ['日K', 'MA5', 'MA10', 'MA20', 'MA30'],
	    	        inactiveColor: '#777',
	    	        textStyle: {
	    	            color: '#fff'
	    	        }
	    	    },
	    	    tooltip: {
	    	        trigger: 'axis',
	    	        axisPointer: {
	    	            animation: false,
	    	            type: 'cross',
	    	            lineStyle: {
	    	                color: '#376df4',
	    	                width: 2,
	    	                opacity: 1
	    	            }
	    	        }
	    	    },
	    	    xAxis: {
	    	        type: 'category',
	    	        data: dates,
	    	        axisLine: { lineStyle: { color: '#8392A5' } }
	    	    },
	    	    yAxis: {
	    	        scale: true,
	    	        axisLine: { lineStyle: { color: '#8392A5' } },
	    	        splitLine: { show: false }
	    	    },
	    	    grid: {
	    	        bottom: 80
	    	    },
	    	    dataZoom: [{
	    	        textStyle: {
	    	            color: '#8392A5'
	    	        },
	    	        handleSize: '80%',
	    	        dataBackground: {
	    	            areaStyle: {
	    	                color: '#8392A5'
	    	            },
	    	            lineStyle: {
	    	                opacity: 0.8,
	    	                color: '#8392A5'
	    	            }
	    	        },
	    	        handleStyle: {
	    	            color: '#fff',
	    	            shadowBlur: 3,
	    	            shadowColor: 'rgba(0, 0, 0, 0.6)',
	    	            shadowOffsetX: 2,
	    	            shadowOffsetY: 2
	    	        }
	    	    }, {
	    	        type: 'inside'
	    	    }],
	    	    animation: false,
	    	    series: [
	    	        {
	    	            type: 'candlestick',
	    	            name: '日K',
	    	            data: data,
	    	            itemStyle: {
	    	                normal: {
	    	                    color: '#FD1050',
	    	                    color0: '#0CF49B',
	    	                    borderColor: '#FD1050',
	    	                    borderColor0: '#0CF49B'
	    	                }
	    	            }
	    	        },
	    	        {
	    	            name: 'MA5',
	    	            type: 'line',
	    	            data: calculateMA(5, data),
	    	            smooth: true,
	    	            showSymbol: false,
	    	            lineStyle: {
	    	                normal: {
	    	                    width: 1
	    	                }
	    	            }
	    	        },
	    	        {
	    	            name: 'MA10',
	    	            type: 'line',
	    	            data: calculateMA(10, data),
	    	            smooth: true,
	    	            showSymbol: false,
	    	            lineStyle: {
	    	                normal: {
	    	                    width: 1
	    	                }
	    	            }
	    	        },
	    	        {
	    	            name: 'MA20',
	    	            type: 'line',
	    	            data: calculateMA(20, data),
	    	            smooth: true,
	    	            showSymbol: false,
	    	            lineStyle: {
	    	                normal: {
	    	                    width: 1
	    	                }
	    	            }
	    	        },
	    	        {
	    	            name: 'MA30',
	    	            type: 'line',
	    	            data: calculateMA(30, data),
	    	            smooth: true,
	    	            showSymbol: false,
	    	            lineStyle: {
	    	                normal: {
	    	                    width: 1
	    	                }
	    	            }
	    	        }
	    	    ]
	    	};
	    	
	    	echarts.init(document.getElementById('cg-k-line')).setOption(option);
	    },  
	    error: function() {  
	    }  
	});  
}

showOneStockK("000001","h");

</script> 
