<!DOCTYPE html>
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
<div data-role="header" data-theme="b">
		<h1>Thuần Việt Shop</h1>
</div>
<div id="divContainer" style="margin:auto; text-align:center;">	
	<input type="button" id="btnDoanhThu" name="btnDoanhThu" value="1. Hôm nay: [HOMNAY]" />
	<input type="button" id="btnThangNay" name="btnThangNay" value="2. Tháng này: [THANGNAY]" />
	<input type="button" id="btnTrangThai" name="btnTrangThai" value="3. Trạng thái hiện tại" />
	<input type="button" id="btnMatHangBanTrongNgay" name="btnMatHangBanTrongNgay" value="4. Mặt hàng bán trong ngày" />
    <input type="button" id="btnThuChi" name="btnThuChi" value="5. Thu chi trong ngày" />        
    <input type="button" id="btnTongHop" name="btnTongHop" value="6. Báo cáo tổng hợp bán hàng" />
    <input type="button" id="btnChiTiet" name="btnChiTiet" value="7. Báo cáo chi tiết bán hàng" />
    <input type="button" id="btnTongHopCongNo" name="btnTongHopCongNo" value="8. Công nợ khách hàng" />
    <input type="button" id="btnTongHopCongNoNcc" name="btnTongHopCongNoNcc" value="9. Công nợ nhà cung cấp" />
    <input type="button" id="btnTongHopTheoThuNgan" name="btnTongHopTheoThuNgan" value="10. Tổng hợp theo thu ngân" />
    <input type="button" id="btnChiTietTheoThuNgan" name="btnChiTietTheoThuNgan" value="11. Chi tiết theo thu ngân" />
    <input type="button" id="btnChiTietTheoMatHang" name="btnChiTietTheoMatHang" value="12. Báo cáo lãi lỗ" />
    <input type="button" id="btnDoanhSoTheoThang" name="btnDoanhSoTheoThang" value="13. Doanh số theo tháng" />    
    <input type="button" id="btnThuChiTheoLyDo" name="btnThuChiTheoLyDo" value="14. Tổng hợp thu chi theo lý do" />    
    <input type="button" id="btnTonKho" name="btnTonKho" value="15. Xem tồn kho" />
    <input type="button" id="btnDanhMucHangHoa" name="btnDanhMucHangHoa" value="16. Danh mục hàng hóa" />
	<input type="button" id="btnLogout" name="btnLogout" value="Đăng xuất" />
</div>
<script type="text/javascript" language="javascript">
$(function() {	
	$("#btnHoatDong").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'trangthai';
    });
    
    $("#btnChiTietTheoMatHang").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'locchitiettheomathang';
    });
    
    $("#btnTrangThai").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'trangthai';
    });
   	
   	$("#btnDanhMucHangHoa").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'locdanhmuchanghoa';
    });
    
    $("#btnThuChiTheoLyDo").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'loctonghopthuchitheolydo';
    });
    
    $("#btnTongHopCongNo").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'tonghopcongno';
    });
    
    $("#btnTongHopCongNoNcc").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'tonghopcongnoncc';
    });
    
    $("#btnThangNay").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'tonghop?fromDay=[FROMDAY]&fromMonth=[FROMMONTH]&fromYear=[FROMYEAR]&toDay=[TODAY]&toMonth=[TOMONTH]&toYear=[TOYEAR]';
    });
    
    $("#btnDoanhSoTheoThang").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'locdoanhsotheothang';
    });
	
	
	$("#btnThuChi").click(function(e) {
        //hien thi bao cao hoat dong trong ngay
		window.location = 'thuchi';
    });
	
	$("#btnDoanhThu").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'doanhthu';
    });
	
	$("#btnTongHop").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'loctonghop';
    });
    
    $("#btnMatHangBanTrongNgay").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'chitiettheomathang?fromDay=[DAY]&fromMonth=[MONTH]&fromYear=[YEAR]&toDay=[DAY]&toMonth=[MONTH]&toYear=[YEAR]&nhom=&laitheo=GIANHAP';
    });
    
    $("#btnChiTietTheoThuNgan").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'locchitiettheothungan';
    });
    
    $("#btnTongHopTheoThuNgan").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'loctonghoptheothungan';
    });
	
	$("#btnChiTiet").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'locchitiet';
    });
    
    $("#btnTonKho").click(function(e) {
        //hien thi bao cao doanh thu trong ngay
		window.location = 'loctonkho';
    });
	
	$("#btnLogout").click(function(e) {
		$.ajax
		({
			url: '?',
			type: 'POST',
			data: 'cmd=onLogout',
			success:function(string)
			{
				window.location = 'login';
			},
			error: function(xhr,err)
			{
				window.location = 'login';
			}
		});	
    });
});
</script>
</div>
</html>