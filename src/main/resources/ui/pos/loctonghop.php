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
		<h1>Báo cáo tổng hợp bán hàng</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto;">
<div style="height:20px">&nbsp;</div>
<div>Từ ngày: </div>
<div class="ui-grid-b">
    <div class="ui-block-a">
        <div data-role="fieldcontain" class="ui-hide-label">           
            <input type="text" name="txtFromDay" id="txtFromDay" value="1" />
        </div>
    </div>
    <div class="ui-block-b">
        <div data-role = "fieldcontain" class="ui-hide-label">
            <input type="text" name="txtFromMonth" id="txtFromMonth" value="[THANG]" />
        </div>
    </div>
    <div class="ui-block-c">
        <div data-role = "fieldcontain" class="ui-hide-label">
            <input type="text" name="txtFromYear" id="txtFromYear" value="[NAM]" />
        </div>
    </div>
</div>
<div style="height:20px">&nbsp;</div>
<div>Đến ngày: </div>
<div class="ui-grid-b">
    <div class="ui-block-a">
        <div data-role="fieldcontain" class="ui-hide-label">           
            <input type="text" name="txtToDay" id="txtToDay" value="[NGAY]" />
        </div>
    </div>
    <div class="ui-block-b">
        <div data-role = "fieldcontain" class="ui-hide-label">
            <input type="text" name="txtToMonth" id="txtToMonth" value="[THANG]" />
        </div>
    </div>
    <div class="ui-block-c">
        <div data-role = "fieldcontain" class="ui-hide-label">
            <input type="text" name="txtToYear" id="txtToYear" value="[NAM]" />
        </div>
    </div>
</div>
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
       window.location = 'tonghop?fromDay=' + $("#txtFromDay").val() +
	   					 '&fromMonth=' + $("#txtFromMonth").val() + 
						 '&fromYear=' + $("#txtFromYear").val() +
						 '&toDay=' + $("#txtToDay").val() +
						 '&toMonth=' + $("#txtToMonth").val() + 
						 '&toYear=' + $("#txtToYear").val();
    });
});
</script>
