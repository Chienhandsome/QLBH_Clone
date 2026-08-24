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
		<h1>Chi tiết đơn hàng</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto; text-align:center;">
<ul style="margin-top:20px;" data-role="listview" data-inset="true">
<li data-role="list-divider">Thông tin đơn hàng</li>
<li>Thu ngân: [THUNGAN]</li>
<li>Khách hàng: [KHACHHANG]</li>
<li>Giờ vào: [BATDAU]</li>
<li>Giờ ra: [KETTHUC]</li>
</ul>
<ul style="margin-top:20px;" data-role="listview" data-inset="true">
	<li data-role="list-divider">Chi tiết mặt hàng</li>
	[CHITIET]
	<li class='ui-bar-e'>Tiền hàng: <span class='ui-li-count'>[TIENHANG]</span></li>
	<li class='ui-bar-e'>Tiền giờ: <span class='ui-li-count'>[TIENBAN]</span></li>
	<li class='ui-bar-e'>Giảm giá: <span class='ui-li-count'>[GIAMGIA]</span></li>
	<li class='ui-bar-e'>Phí dịch vụ: <span class='ui-li-count'>[PHIDICHVU]</span></li>	
	<li class='ui-bar-e'>Tổng cộng: <span class='ui-li-count'>[TONGCONG]</span></li>
</ul>
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