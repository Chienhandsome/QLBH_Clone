<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link href='css/jquerymobile.css' rel="stylesheet" />
<script type="text/javascript" language='javascript' src="js/jquery.js"></script>
<script type="text/javascript" language='javascript' src="js/jquerymobile.js"></script>
<script type="text/javascript" language='javascript' src="js/simpledialog.js"></script>
<script type="text/javascript" language='javascript' src="js/utils.js"></script></head>
<div data-role="page">
<div data-role="header" data-theme="b" data-position="inline">
		<a href="/main" data-icon="home">Trang chủ</a>
		<h1>Tổng hợp công nợ</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto; text-align:center;">
<div data-role="collapsible-set">
	<div data-collapsed="false" data-role='collapsible' data-theme='b'>
	<h3>Tổng hợp công nợ</h3>
	<ul data-role="listview" data-inset="true">
	[DATA]
	</ul>
	</div>
</div>
<div align="right" style="margin:10px"><h4>Tổng nợ: [TONGCONG]</h4></div>
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