var version=new Date();
//引入公共js
document.write('<script src="/js/jquery-3.3.1.min.js" type="text/javascript"></script>');
document.write('<script src="/js/bootstrap/bootstrap.min.js" type="text/javascript"></script>');

//引入公共css
document.write('<link href="/css/bootstrap-theme.min.css" rel="stylesheet" type="text/css">');
document.write('<link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css">');
document.write('<link href="/css/font-awesome.min.css" rel="stylesheet" type="text/css">');
document.write('<link href="/css/common.css?_='+version+'" rel="stylesheet" type="text/css">');

document.write('<script src="/js/common.js?_='+version+'" type="text/javascript"></script>');
