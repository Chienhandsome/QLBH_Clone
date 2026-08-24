<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link href='css/jquerymobile.css' rel="stylesheet" />
<script type="text/javascript" language='javascript' src="js/jquery.js"></script>
<script type="text/javascript" language='javascript' src="js/jquerymobile.js"></script>
<script type="text/javascript" language='javascript' src="js/simpledialog.js"></script>
<script type="text/javascript" language='javascript' src="js/utils.js"></script>
<style>
.col1
{
	width:70px; 
	display:inline-block;
}
.colTong
{
	width:100px; 
	display:inline-block;
}
h3
{
	color:blue;
}
</style>
</head>
<div data-role="page">
<div data-role="header" data-theme="b" data-position="inline">
		<a href="/main" data-icon="home">Trang chủ</a>
		<h1>Báo cáo lãi lỗ</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto; text-align:center;">
<div data-role="collapsible-set">
[DATA]
</div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Tiền nhập:</h4></span><span class="colTong"><h4 style="margin:0px;">[TIENNHAP]</h4></span></div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Tiền bán:</h4></span><span class="colTong"><h4 style="margin:0px;">[TIENBAN]</h4></span></div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Phí dịch vụ:</h4></span><span class="colTong"><h4 style="margin:0px;">[PHIDICHVU]</h4></span></div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Giảm gia:</h4></span><span class="colTong"><h4 style="margin:0px;">[GIAMGIA]</h4></span></div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Tổng lãi:</h4></span><span class="colTong"><h4 style="margin:0px;">[TONGLAI]</h4></span></div>
<div align="right" style="display:block; margin:10px"><span class="colTong"><h4 style="margin:0px;">Tỉ lệ lãi:</h4></span><span class="colTong"><h4 style="margin:0px;">[TILELAI]</h4></span></div>
<div align="right">
<input type="button" data-inline="true" id="btnRefresh" name="btnRefresh" value="Refresh" />
<input type="button" data-inline="true" style="margin-right:40px;" id="btnBack" name="btnReset" value="Quay lại" />
</div>
</div>
<script type="text/javascript" language="javascript">
$(function() {	
	$("#btnBack").click(function(e) {
        history.back();
    });
	
	$("#btnRefresh").click(function(e) {
        location.reload();
    });
});

function openBill(id)
{
	window.location = 'donhang?id=' + id;
}
</script>
</html>