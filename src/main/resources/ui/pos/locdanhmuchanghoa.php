<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<link href='css/jquerymobile.css' rel="stylesheet" />
<script type="text/javascript" language='javascript' src="js/jquery.js"></script>
<script type="text/javascript" language='javascript' src="js/jquerymobile.js"></script>
<script type="text/javascript" language='javascript' src="js/simpledialog.js"></script>
<script type="text/javascript" language='javascript' src="js/utils.js"></script>
</head>
<div data-role="page">
<div data-role="header" data-theme="b" data-position="inline">
		<a href="/main" data-icon="home">Trang chủ</a>
		<h1>Danh mục hàng hóa</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto;">
<div style="height:20px">&nbsp;</div>
<div>Nhóm hàng: </div>
<select id="cboNhom" name="cboNhom">
[NHOMHANG]
</select>
<div align="right">
<input type="button" data-inline="true" id="btnXem" name="btnXem" value="Xem báo cáo" />
<input type="button" data-inline="true" style="margin-right:40px;" id="btnBack" name="btnReset" value="Quay lại" />
</div>
</div>
<script type="text/javascript" language="javascript">
$(function() {	
	$("#btnBack").click(function(e) {
        history.back();
    });
	
	$("#btnXem").click(function(e) {
       window.location = 'danhmuchanghoa?nhom=' + $("#cboNhom").val();
    });
});
</script>
