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
							<input type="text" id="gpdm" name="gpdm" lay-verify="title" autocomplete="off" placeholder="请输入股票代码" class="layui-input">
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
		<div id="cg-k-line" style="height: 400px;width: 600px;margin-left: auto;margin-right: auto" >
		</div>
	</div>
	<div id="cg-similarity-main">
		<div id="cg-xs-0" class="cg-xs"></div>
		<div id="cg-xs-1" class="cg-xs"></div>
		<div id="cg-xs-2" class="cg-xs"></div>
		<div id="cg-xs-3" class="cg-xs"></div>
		<div id="cg-xs-4" class="cg-xs"></div>
		<div id="cg-xs-5" class="cg-xs"></div>
		<div id="cg-xs-6" class="cg-xs"></div>
		<div id="cg-xs-7" class="cg-xs"></div>
		<div id="cg-xs-8" class="cg-xs"></div>
		<div class="clearfloat"></div>
	</div>
</div>
<script>
/* 
$(function() {
    var availableTags = [
      "ActionScript",
      "AppleScript",
      "Asp",
      "BASIC",
      "C",
      "C++",
      "Clojure",
      "COBOL",
      "ColdFusion",
      "Erlang",
      "Fortran",
      "Groovy",
      "Haskell",
      "Java",
      "JavaScript",
      "Lisp",
      "Perl",
      "PHP",
      "Python",
      "Ruby",
      "Scala",
      "Scheme"
    ];
    $("#index-cg-main #gpdm").autocomplete({
      source: availableTags
    });
}); */

layui.use(['form', 'layedit'], function(){
	var form = layui.form
	,layer = layui.layer
	,layedit = layui.layedit;
	
	//监听提交
	form.on('submit(fxgp)', function(data){
		showOneStockK(data.field.gpdm);
		showOneStockLikeK(data.field.gpdm);
		return false;
	});
	
	//表单初始赋值
	form.val('cg-top-form', {
		"gpdm": "600000"
	})
});


function splitData(rawData) {
    var categoryData = [];
    var values = [];
    var volumes = [];
    for (var i = 0; i < rawData.length; i++) {
        categoryData.push(rawData[i].splice(0, 1)[0]);
        values.push(rawData[i]);
        volumes.push([i, rawData[i][4], rawData[i][0] > rawData[i][1] ? 1 : -1]);
    }

    return {
        categoryData: categoryData,
        values: values,
        volumes: volumes
    };
}

function calculateMA(dayCount, data) {
	console.log(data.values);
    var result = [];
    for (var i = 0, len = data.values.length; i < len; i++) {
        if (i < dayCount) {
            result.push('-');
            continue;
        }
        var sum = 0;
        for (var j = 0; j < dayCount; j++) {
            sum += parseFloat(data.values[i - j][1]);
        }
        result.push(+(sum / dayCount).toFixed(3));
    }
    return result;
}

function showOneStockK(gpdm){
	if(gpdm==null ||gpdm==""){
		layer.msg('请填写股票代码'); 
		return false;
	}
	
	var url = "Stock.do?action=showOneStockK&gpdm="+gpdm;  
	AjaxUtil.aReq(url,function(dates){
		var myChart = echarts.init(document.getElementById("cg-k-line"));
		var upColor = "#ec0000";
		var downColor = "#00da3c";
		
    	var result=JSON.parse(dates);
    	var re=result.re;
    	var eachday=re.split(",");
    	var rawData=[];
    	for(var i=0;i<eachday.length;i++){
    		if(eachday[i]!=null && eachday[i]!=""){
    			rawData.push(eachday[i].split("#"));
    		}
    	}

        var data = splitData(rawData);

        myChart.setOption(option = {
            backgroundColor: '#fff',
            animation: false,
            title : {
                text: '南丁格尔玫瑰图',
                x:'left'
            },
            legend: {//图例
                top: 5,
                left: 'center',
                data: ['MA5', 'MA10', 'MA20', 'MA30']
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross'
                },
                backgroundColor: 'rgba(245, 245, 245, 0.8)',
                borderWidth: 1,
                borderColor: '#ccc',
                padding: 10,
                textStyle: {
                    color: '#000'
                },
                position: function (pos, params, el, elRect, size) {
                    var obj = {top: 10};
                    obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 30;
                    return obj;
                }
                // extraCssText: 'width: 170px'
            },
            axisPointer: {
                link: {xAxisIndex: 'all'},
                label: {
                    backgroundColor: '#777'
                }
            },
            toolbox: {
                feature: {
                    dataZoom: {
                        yAxisIndex: false
                    },
                    brush: {
                        type: ['lineX', 'clear']
                    }
                }
            },
            brush: {
                xAxisIndex: 'all',
                brushLink: 'all',
                outOfBrush: {
                    colorAlpha: 0.1
                }
            },
            visualMap: {
                show: false,
                seriesIndex: 5,
                dimension: 2,
                pieces: [{
                    value: 1,
                    color: downColor
                }, {
                    value: -1,
                    color: upColor
                }]
            },
            grid: [ //数据区域    K线 和 交易量
                {
                    left: '40',
                    right: '10',
                    top: '30',
                    bottom:'95'
                },
                {
                    left: '40',
                    right: '10',
                    bottom: '31',
                    height: '50'
                }
            ],
            xAxis: [
                {
                    type: 'category',
                    data: data.categoryData,
                    scale: true,
                    boundaryGap : false,
                    axisLine: {onZero: false},
                    splitLine: {show: false},
                    splitNumber: 20,
                    min: 'dataMin',
                    max: 'dataMax',
                    axisPointer: {
                        z: 100
                    }
                },
                {
                    type: 'category',
                    gridIndex: 1,
                    data: data.categoryData,
                    scale: true,
                    boundaryGap : false,
                    axisLine: {onZero: false},
                    axisTick: {show: false},
                    splitLine: {show: false},
                    axisLabel: {show: false},
                    splitNumber: 20,
                    min: 'dataMin',
                    max: 'dataMax'
                }
            ],
            yAxis: [
                {
                    scale: true,
                    splitArea: {
                        show: true
                    }
                },
                {
                    scale: true,
                    gridIndex: 1,
                    splitNumber: 2,
                    axisLabel: {show: false},
                    axisLine: {show: false},
                    axisTick: {show: false},
                    splitLine: {show: false}
                }
            ],
            dataZoom: [
                {
                    type: 'inside',
                    xAxisIndex: [0, 1],
                    start: 80,
                    end: 100
                },
                { //下方拖动条
                    show: true,
                    xAxisIndex: [0, 1],
                    type: 'slider',
                    bottom: '1',
                    height:'20',
                    start: 80,
                    end: 100
                }
            ],
            series: [
                {
                    name: '',
                    type: 'candlestick',
                    data: data.values,
                    itemStyle: {
                        normal: {
                            color: upColor,
                            color0: downColor,
                            borderColor: null,
                            borderColor0: null
                        }
                    },
                    tooltip: {
                        formatter: function (param) {
                            param = param[0];
                            return [
                                'Date: ' + param.name + '<hr size=1 style="margin: 3px 0">',
                                '开盘: ' + param.data[0] + '<br/>',
                                '收盘: ' + param.data[1] + '<br/>',
                                '最低: ' + param.data[2] + '<br/>',
                                '最高: ' + param.data[3] + '<br/>'
                            ].join('');
                        }
                    }
                },
                {
                    name: 'MA5',
                    type: 'line',
                    data: calculateMA(5, data),
                    smooth: true,
                    symbol: "none",
                    lineStyle: {
                        normal: {opacity: 0.5}
                    }
                },
                {
                    name: 'MA10',
                    type: 'line',
                    data: calculateMA(10, data),
                    smooth: true,
                    symbol: "none",
                    lineStyle: {
                        normal: {opacity: 0.5}
                    }
                },
                {
                    name: 'MA20',
                    type: 'line',
                    data: calculateMA(20, data),
                    smooth: true,
                    symbol: "none",
                    lineStyle: {
                        normal: {opacity: 0.5}
                    }
                },
                {
                    name: 'MA30',
                    type: 'line',
                    data: calculateMA(30, data),
                    smooth: true,
                    symbol: "none",
                    lineStyle: {
                        normal: {opacity: 0.5}
                    }
                },
                { //交易量柱状图
                    name: 'Volume',
                    type: 'bar',
                    xAxisIndex: 1,
                    yAxisIndex: 1,
                    data: data.volumes
                }
            ]
        }, true);
	});
}


function showOneStockLikeK(gpdm){
	$(".cg-xs").hide();
	if(gpdm==null ||gpdm==""){
		return false;
	}
	var upColor = "#ec0000";
	var downColor = "#00da3c";
	var option = {
            backgroundColor: '#fff',
            animation: false,
            title : {
                text: '南丁格尔玫瑰图',
                x:'left'
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross'
                },
                backgroundColor: 'rgba(245, 245, 245, 0.8)',
                borderWidth: 1,
                borderColor: '#ccc',
                padding: 10,
                textStyle: {
                    color: '#000'
                },
                position: function (pos, params, el, elRect, size) {
                    var obj = {top: 10};
                    obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 30;
                    return obj;
                }
                // extraCssText: 'width: 170px'
            },
            axisPointer: {
                link: {xAxisIndex: 'all'},
                label: {
                    backgroundColor: '#777'
                }
            },
            toolbox: {
                feature: {
                    dataZoom: {
                        yAxisIndex: false
                    },
                    brush: {
                        type: ['lineX', 'clear']
                    }
                }
            },
            brush: {
                xAxisIndex: 'all',
                brushLink: 'all',
                outOfBrush: {
                    colorAlpha: 0.1
                }
            },
            visualMap: {
                show: false,
                seriesIndex: 5,
                dimension: 2,
                pieces: [{
                    value: 1,
                    color: downColor
                }, {
                    value: -1,
                    color: upColor
                }]
            },
            grid: [ //数据区域    K线 和 交易量
                {
                    left: '40',
                    right: '10',
                    top: '30',
                    bottom:'40'
                }
            ],
            xAxis: [
                {
                    type: 'category',
                    data: null,
                    scale: true,
                    boundaryGap : false,
                    axisLine: {onZero: false},
                    splitLine: {show: false},
                    splitNumber: 20,
                    min: 'dataMin',
                    max: 'dataMax',
                    axisPointer: {
                        z: 100
                    }
                }
            ],
            yAxis: [
                {
                    scale: true,
                    splitArea: {
                        show: true
                    }
                }
            ],
            dataZoom: [
                {
                    type: 'inside',
                    xAxisIndex: [0],
                    start: 40,
                    end: 60
                },
                { //下方拖动条
                    show: true,
                    xAxisIndex: [0],
                    type: 'slider',
                    bottom: '1',
                    height:'15',
                    start: 40,
                    end: 60
                }
            ],
            series: [
                {
                    name: '',
                    type: 'candlestick',
                    data:null,
                    itemStyle: {
                        normal: {
                            color: upColor,
                            color0: downColor,
                            borderColor: null,
                            borderColor0: null
                        }
                    },
                    tooltip: {
                        formatter: function (param) {
                            param = param[0];
                            return [
                                'Date: ' + param.name + '<hr size=1 style="margin: 3px 0">',
                                '开盘: ' + param.data[0] + '<br/>',
                                '收盘: ' + param.data[1] + '<br/>',
                                '最低: ' + param.data[2] + '<br/>',
                                '最高: ' + param.data[3] + '<br/>'
                            ].join('');
                        }
                    }
                }
            ]
        };
	var url = "Stock.do?action=showOneStockLikeK&gpdm="+gpdm;  
	AjaxUtil.aReq(url,function(dates){
    	var result=JSON.parse(dates);
    	
    	for(var k=0;k<9;k++){
    		if(result["xs"+k]){
    			$("#cg-xs-"+k).show();
    			var myChart = echarts.init(document.getElementById("cg-xs-"+k));
    			var thisk=JSON.parse(result["xs"+k]);
    			var re=thisk.kdata;
    			var eachday=re.split(",");
    	    	var rawData=[];
    	    	for(var i=0;i<eachday.length;i++){
    	    		if(eachday[i]!=null && eachday[i]!=""){
    	    			rawData.push(eachday[i].split("#"));
    	    		}
    	    	}
    	        var data = splitData(rawData);
    	        option.xAxis[0].data=data.categoryData;
    	        option.series[0].data=data.values;
    	        myChart.setOption(option, true);
    		}else{
    			continue;
    		}
    	}
	});
}

showOneStockK("000001");
showOneStockLikeK("000001");
</script> 
