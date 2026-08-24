<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link href='css/jquerymobile.css' rel="stylesheet" />
<script type="text/javascript" language='javascript' src="js/jquery.js"></script>
<script type="text/javascript" language='javascript' src="js/jquerymobile.js"></script>
<script type="text/javascript" language='javascript' src="js/simpledialog.js"></script>
<script type="text/javascript" language='javascript' src="js/utils.js"></script><title>THU CHI TRONG NGÀY</title>
</head>
<div data-role="page">
<div data-role="header" data-theme="b" data-position="inline">
		<a href="/main" data-icon="home">Trang chủ</a>
		<h1>Thu chi trong ngày</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto; text-align:center;">
<ul style="margin-top:20px;" data-role="listview" data-inset="true">
	<li data-role="list-divider">Thu</li>
	[THUCHITIET]
</ul>

<ul style="margin-top:20px;" data-role="listview" data-inset="true">
	<li data-role="list-divider">Chi</li>
	[CHICHITIET]
</ul>
<div align="right" style="margin:10px"><h4>Thu đơn hàng: [THUDONHANG]</h4></div>
	<div align="right" style="margin:10px"><h4>Thu chi (thu - chi): [TONGTHUCHI]</h4></div>
<div align="right" style="margin:10px"><h4>Tổng (đơn hàng + thu - chi): [TONG]</h4></div>
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
</script>
</html>