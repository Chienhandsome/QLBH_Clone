<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<title>ĐĂNG NHẬP</title>
<link href='css/jquerymobile.css' rel="stylesheet" />
<script type="text/javascript" language='javascript' src="js/jquery.js"></script>
<script type="text/javascript" language='javascript' src="js/jquerymobile.js"></script>
<script type="text/javascript" language='javascript' src="js/simpledialog.js"></script>
<script type="text/javascript" language='javascript' src="js/utils.js"></script>
</head>
<body>
<div data-role="page">
<div data-role="header" data-theme="b">
		<h1>Đăng nhập hệ thống</h1>
</div>
<div id="divContainer" style="width:100%; margin:auto;">
<div data-role="fieldcontain">
<label for="txtName">Tài khoản:</label>
<input type="text" name="txtName" id="txtName" value="admin" />
</div>
<div data-role="fieldcontain">
<label for="txtPass">Mật khẩu:</label>
<input type="password" name="txtPass" id="txtPass" value="" />
</div>
<div align="right">
<input type="button" data-inline="true" data-theme="b" id="btnLogin" name="btnLogin" value="Đăng nhập" />
<input type="button" data-inline="true" style="margin-right:40px;" id="btnReset" name="btnReset" value="Làm lại" />
</div>
</div>
</div>
</body>
<script type="text/javascript" language="javascript">
$(function() {	
	$("#btnLogin").click(function(e) {
        //post to database
		$("#txtPass").attr("disabled", "disabled");
		$("#txtName").attr("disabled", "disabled");
		$("#btnLogin").attr("disabled", "disabled");
		$("#btnReset").attr("disabled", "disabled");
		
		$.ajax
		({
			url: '?',
			type: 'POST',
			data: 'login?username=' + $("#txtName").val() + '&password=' + $("#txtPass").val(),
			success:function(string)
			{
				//kiem tra xem ok khong?
				if (string == 1)
				{
					window.location = '?';
				}
				else
				{
					jAlert("Người dùng không tồn tại hoặc sai mật khẩu. Mời bạn thử lại");
					$("#txtPass").removeAttr("disabled");
					$("#txtName").removeAttr("disabled");
					$("#btnLogin").removeAttr("disabled");
					$("#btnReset").removeAttr("disabled");
				}
			},
			error: function(xhr,err)
			{
				jAlert("Có lỗi xảy ra, không thể đăng nhập vào hệ thống");
				$("#txtPass").removeAttr("disabled");
				$("#txtName").removeAttr("disabled");
				$("#btnLogin").removeAttr("disabled");
				$("#btnReset").removeAttr("disabled");
			}
		});		
    });
	
	$("#btnReset").click(function(e) {
        $("#txtName").val("admin");
		$("#txtPass").val("");
    });
});
</script>
</html>