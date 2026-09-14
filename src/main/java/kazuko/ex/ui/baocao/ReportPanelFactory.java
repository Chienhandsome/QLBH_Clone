package kazuko.ex.ui.baocao;

import kazuko.ex.ui.hoadon.ThongKeDoanhThuPanel;
import kazuko.ex.ui.hoadon.ThongKeMatHangBanPanel;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.DanhMucMatHangPanel;
import kazuko.ex.ui.danhmuc.DanhMucNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.DanhMucDotKhuyenMaiPanel;
import kazuko.ex.ui.dathang.KhachDatHangPanel;
import kazuko.ex.ui.khohang.QuanLyChuyenKhoPanel;
import kazuko.ex.ui.khohang.QuanLyKiemKePanel;
import kazuko.ex.ui.khohang.QuanLyNhapKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonNhieuKhoPanel;
import kazuko.ex.ui.khohang.QuanLyXuatKhoPanel;
import kazuko.ex.ui.quy.DanhMucPhieuChiPanel;
import kazuko.ex.ui.quy.DanhMucPhieuThuPanel;
import kazuko.ex.ui.congno.QuanLyCongNoKhachHangPanel;
import kazuko.ex.ui.congno.QuanLyCongNoNhaCungCapPanel;

import javax.swing.*;
import java.awt.*;

public final class ReportPanelFactory {

    private ReportPanelFactory() {}

    public static JPanel createReportPanel(String title) {
        if (title == null) return createPlaceholderPanel("Báo cáo");
        switch (title.trim().toUpperCase()) {
            case "DANH SÁCH PHIẾU THU THEO NGÀY":
            case "BÁO CÁO PHIẾU THU THEO NGÀY":
            case "DANH SÁCH PHIẾU THU THE...":
                return new BaoCaoPhieuThuTheoNgayPanel();
            case "DANH SÁCH PHIẾU THU THEO LÝ DO THU CHI":
            case "BÁO CÁO PHIẾU THU THEO LÝ DO THU CHI":
                return new BaoCaoPhieuThuTheoLyDoPanel();
            case "DANH SÁCH PHIẾU CHI THEO NGÀY":
            case "BÁO CÁO PHIẾU CHI THEO NGÀY":
            case "DANH SÁCH PHIẾU CHI THE...":
                return new BaoCaoPhieuChiTheoNgayPanel();
            case "DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI":
            case "BÁO CÁO PHIẾU CHI THEO LÝ DO THU CHI":
                return new BaoCaoPhieuChiTheoLyDoPanel();
            case "TỔNG HỢP THU CHI THEO NGÀY":
                return new TongHopThuChiTheoNgayPanel();
            case "TỔNG HỢP THU CHI THEO LÝ DO":
            case "TỔNG HỢP THU CHI THEO LÝ DO...":
                return new TongHopThuChiTheoLyDoPanel();
            case "BÁO CÁO TỒN QUỸ":
            case "TỒN QUỸ":
            case "SỔ QUỸ":
                return new BaoCaoTonQuyPanel();
            case "KHÁCH HÀNG THÂN THIẾT":
            case "BÁO CÁO KHÁCH HÀNG THÂN THIẾT":
                return new BaoCaoKhachHangThanThietPanel();
            case "BÁO CÁO BÁN HÀNG THEO GIỜ":
            case "BÁN HÀNG THEO GIỜ":
                return new BaoCaoBanHangTheoGioPanel();
            case "BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC":
            case "BÁN HÀNG THEO KÍCH THƯỚC":
                return new BaoCaoBanHangTheoKichThuocPanel();
            case "BÁO CÁO TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG":
            case "TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG":
                return new BaoCaoTongHopGiaTriBanTheoThangPanel();
            case "BÁO CÁO TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG":
            case "TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG":
                return new BaoCaoTongHopSoLuongBanTheoThangPanel();
            case "BÁO CÁO BÁN HÀNG THEO NGÀY":
                return new BaoCaoBanHangTheoNgayPanel();
            case "BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY":
                return new BaoCaoChiTietBanHangTheoNgayPanel();
            case "BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG":
                return new BaoCaoBanHangTheoKhachHangPanel();
            case "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH":
            case "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN":
                return new BaoCaoBanHangTheoNhanVienPanel();
            case "TỔNG HỢP MẶT HÀNG BÁN THEO KHÁCH HÀNG":
                return new TongHopMatHangBanTheoKhachHangPanel();
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NGÀY":
                return new TongHopMatHangBanTheoNgayPanel();
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN KINH DOANH":
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN":
                return new TongHopMatHangBanTheoNhanVienPanel();
            case "TỔNG HỢP BÁN HÀNG THEO NGÀY":
                return new TongHopBanHangTheoNgayPanel();
            case "TỔNG HỢP BÁN THEO KHÁCH HÀNG":
                return new TongHopBanTheoKhachHangPanel();
            case "TỔNG HỢP BÁN THEO NHÂN VIÊN BÁN":
            case "TỔNG HỢP BÁN THEO NHÂN VIÊN":
                return new TongHopBanTheoNhanVienBanPanel();
            case "TỔNG HỢP BÁN THEO THU NGÂN":
                return new TongHopBanTheoThuNganPanel();
            case "TỔNG HỢP MẶT HÀNG BÁN THEO THU NGÂN":
                return new TongHopMatHangBanTheoThuNganPanel();
            case "BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN":
            case "CHI TIẾT BÁN HÀNG THEO THU NGÂN":
                return new BaoCaoChiTietBanHangTheoThuNganPanel();
            case "BÁO CÁO BÁN HÀNG THEO THU NGÂN":
            case "BÁO CÁO BÁN HÀNGTHEO THU NGÂN":
                return new BaoCaoBanHangTheoThuNganPanel();
            case "TỔNG HỢP TIỀN VẬN CHUYỂN THEO NHÂN VIÊN":
            case "TIỀN VẬN CHUYỂN THEO NHÂN VIÊN":
                return new TongHopTienVanChuyenTheoNhanVienPanel();
            case "CHI TIẾT BÁN HÀNG THEO ĐỐI TÁC":
            case "CHI TIẾT HÀNG BÁN THEO ĐỐI TÁC":
            case "BÁN HÀNG THEO ĐỐI TÁC":
                return new ChiTietHangBanTheoDoiTacPanel();
            case "TỔNG HỢP HOA HỒNG THEO NVKD":
            case "TỔNG HỢP HOA HỒNG THEO NHÂN VIÊN":
            case "HOA HỒNG THEO NVKD":
                return new TongHopHoaHongTheoNVKDPanel();
            case "CHI TIẾT BÁN HÀNG THEO HÓA ĐƠN":
            case "BÁN HÀNG THEO HÓA ĐƠN":
                return new ChiTietBanHangTheoHoaDonPanel();
            case "BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI":
            case "CHI TIẾT HÀNG KHUYẾN MẠI":
            case "HÀNG KHUYẾN MẠI":
                return new BaoCaoChiTietHangKhuyenMaiPanel();
            case "DANH SÁCH ĐỢT KHUYẾN MẠI":
            case "BÁO CÁO ĐỢT KHUYẾN MẠI":
            case "DANH MỤC ĐỢT KHUYẾN MÃI":
            case "ĐỢT KHUYẾN MÃI":
                return new BaoCaoDotKhuyenMaiPanel();
            case "KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT":
            case "SINH NHẬT KHÁCH HÀNG":
                return new BaoCaoKhachHangSinhNhatPanel();
            case "DANH SÁCH ĐẶT HÀNG THEO NGÀY":
            case "BÁO CÁO ĐẶT HÀNG THEO NGÀY":
            case "ĐẶT HÀNG THEO NGÀY":
                return new BaoCaoDanhSachDatHangTheoNgayPanel();
            case "DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "BÁO CÁO ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "ĐẶT HÀNG THEO KHÁCH HÀNG":
                return new BaoCaoDanhSachDatHangTheoKhachHangPanel();
            case "TỔNG HỢP ĐẶT HÀNG THEO NGÀY":
                return new TongHopDatHangTheoNgayPanel();
            case "TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG":
                return new TongHopDatHangTheoKhachHangPanel();
            case "TỔNG HỢP MẶT HÀNG ĐẶT THEO KHÁCH HÀNG":
            case "MẶT HÀNG ĐẶT THEO KHÁCH HÀNG":
                return new TongHopMatHangDatTheoKhachHangPanel();
            case "TỔNG HỢP MẶT HÀNG ĐẶT THEO NGÀY":
            case "MẶT HÀNG ĐẶT THEO NGÀY":
                return new TongHopMatHangDatTheoNgayPanel();
            case "ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG":
            case "BÁO CÁO CÔNG NỢ KHÁCH HÀNG":
                return new BaoCaoCongNoKhachHangPanel();
            case "TỔNG HỢP CÔNG NỢ KHÁCH HÀNG":
                return new TongHopCongNoKhachHangPanel();
            case "CÔNG NỢ KHÁCH HÀNG":
            case "CÔNG NỢ KHÁCH":
            case "QUẢN LÝ CÔNG NỢ KHÁCH HÀNG":
                return new QuanLyCongNoKhachHangPanel();
            case "ĐỐI CHIẾU CÔNG NỢ NHÀ CUNG CẤP":
            case "BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP":
                return new BaoCaoCongNoNhaCungCapPanel();
            case "TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP":
                return new TongHopCongNoNhaCungCapPanel();
            case "CÔNG NỢ NHÀ CUNG CẤP":
            case "CÔNG NỢ NCC":
            case "QUẢN LÝ CÔNG NỢ NHÀ CUNG CẤP":
                return new QuanLyCongNoNhaCungCapPanel();
            case "BÁO CÁO TỒN KHO THEO KÍCH THƯỚC":
            case "TỒN KHO THEO KÍCH THƯỚC":
                return new BaoCaoTonKhoTheoKichThuocPanel();
            case "BÁO CÁO HÀNG THEO HẠN DÙNG":
            case "BÁO CÁO THEO HẠN DÙNG":
            case "HÀNG THEO HẠN DÙNG":
                return new BaoCaoHangTheoHanDungPanel();
            case "BÁO CÁO HÀNG ĐÃ HẾT HẠN":
            case "HÀNG ĐÃ HẾT HẠN":
                return new BaoCaoHangDaHetHanPanel();
            case "BÁO CÁO HÀNG TỒN KHO CÓ HẠN DÙNG":
            case "HÀNG TỒN KHO CÓ HẠN DÙNG":
                return new BaoCaoHangTonKhoHanDungPanel();
            case "BÁO CÁO HÀNG TỒN KHO":
            case "HÀNG TỒN KHO":
                return new BaoCaoHangTonKhoPanel();
            case "BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN":
            case "TỔNG HỢP XUẤT NHẬP TỒN":
                return new BaoCaoTongHopXuatNhapTonPanel();
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)":
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG GIÁ VỐN":
            case "LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)":
            case "LÃI GỘP MẶT HÀNG GIÁ VỐN":
                return new TongHopLaiGopTheoMatHangGiaVonPanel();
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)":
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG GIÁ NHẬP":
            case "LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)":
            case "LÃI GỘP MẶT HÀNG GIÁ NHẬP":
                return new TongHopLaiGopTheoMatHangGiaNhapPanel();
            case "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ VỐN)":
            case "CHI TIẾT LÃI THEO HÓA ĐƠN GIÁ VỐN":
            case "CHI TIẾT LÃI HÓA ĐƠN (GIÁ VỐN)":
            case "CHI TIẾT LÃI HÓA ĐƠN GIÁ VỐN":
                return new ChiTietLaiTheoHoaDonGiaVonPanel();
            case "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ NHẬP)":
            case "CHI TIẾT LÃI THEO HÓA ĐƠN GIÁ NHẬP":
            case "CHI TIẾT LÃI HÓA ĐƠN (GIÁ NHẬP)":
            case "CHI TIẾT LÃI HÓA ĐƠN GIÁ NHẬP":
                return new ChiTietLaiTheoHoaDonGiaNhapPanel();
            case "BÁO CÁO KẾT QUẢ KINH DOANH":
            case "KẾT QUẢ KINH DOANH":
            case "TỔNG HỢP KẾT QUẢ KINH DOANH":
            case "TỔNG HỢP KQKD":
                return new BaoCaoKetQuaKinhDoanhPanel();
            case "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY":
            case "HOẠT ĐỘNG TRONG NGÀY":
                return new ChiTietHoatDongTrongNgayPanel();
            case "BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT":
            case "20 MẶT HÀNG BÁN CHẠY NHẤT":
            case "TOP 20 MẶT HÀNG BÁN CHẠY":
                return new BaoCao20MatHangBanChayNhatPanel();
            case "BẢNG TỔNG HỢP THỊ PHẦN":
            case "TỔNG HỢP THỊ PHẦN":
                return new BangTongHopThiPhanPanel();
            case "BÁO CÁO PHÂN TÍCH TÌNH HÌNH BÁN HÀNG":
            case "PHÂN TÍCH TÌNH HÌNH BÁN HÀNG":
                return new BaoCaoPhanTichTinhHinhBanHangPanel();
            case "BIỂU ĐỒ DOANH SỐ THEO NHÓM":
            case "BIỂU ĐỒ DOANH SỐ THEO NHÓM HÀNG HÓA":
            case "DOANH SỐ THEO NHÓM":
                return new BieuDoDoanhSoTheoNhomPanel();
            case "BIỂU ĐỒ DOANH THU NGÀY TRONG THÁNG":
            case "DOANH THU NGÀY TRONG THÁNG":
                return new BieuDoDoanhThuNgayTrongThangPanel();
            case "BIỂU ĐỒ DOANH THU THÁNG TRONG NĂM":
            case "DOANH THU THÁNG TRONG NĂM":
                return new BieuDoDoanhThuThangTrongNamPanel();
            case "BIỂU ĐỒ THEO NHÂN VIÊN KINH DOANH":
            case "BIỂU ĐỒ DOANH SỐ THEO NHÂN VIÊN KINH DOANH":
            case "DOANH SỐ THEO NHÂN VIÊN KINH DOANH":
                return new BieuDoTheoNhanVienKinhDoanhPanel();
            case "BÁO CÁO NHẬP XUẤT TỒN THEO KHO":
            case "BÁO CÁO TỒN KHO THEO MẶT HÀNG":
            case "THẺ KHO":
            case "TỒN KHO":
            case "QUẢN LÝ TỒN KHO":
                return new QuanLyTonKhoPanel();
            case "TỒN NHIỀU KHO":
            case "QUẢN LÝ TỒN NHIỀU KHO":
                return new QuanLyTonNhieuKhoPanel();
            case "DANH SÁCH MẶT HÀNG THEO NHÓM":
                return new BaoCaoMatHangTheoNhomPanel();
            case "DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT":
                return new BaoCaoMatHangTheoHangSanXuatPanel();
            case "DANH MỤC MẶT HÀNG":
                return new DanhMucMatHangPanel();
            case "DANH SÁCH KHÁCH HÀNG THEO NHÓM":
                return new BaoCaoKhachHangTheoNhomPanel();
            case "DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN":
                return new BaoCaoKhachHangTheoNhanVienPanel();
            case "DANH MỤC KHÁCH HÀNG":
                return new DanhMucKhachHangPanel();
            case "DANH SÁCH NHÀ CUNG CẤP THEO NHÓM":
                return new BaoCaoNhaCungCapTheoNhomPanel();
            case "DANH MỤC NHÀ CUNG CẤP":
                return new DanhMucNhaCungCapPanel();
            case "DANH MỤC PHIẾU THU":
                return new DanhMucPhieuThuPanel();
            case "DANH MỤC PHIẾU CHI":
                return new DanhMucPhieuChiPanel();
            case "KHÁCH ĐẶT HÀNG":
            case "ĐẶT HÀNG":
                return new KhachDatHangPanel();
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU NHẬP THEO NGÀY":
                return new BaoCaoDanhSachPhieuNhapTheoNgayPanel();
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP":
            case "DANH SÁCH PHIẾU NHẬP THEO NHÀ CUNG CẤP":
                return new BaoCaoDanhSachPhieuNhapTheoNhaCungCapPanel();
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP":
            case "DANH SÁCH PHIẾU NHẬP THEO NHÂN VIÊN":
                return new BaoCaoDanhSachPhieuNhapTheoNhanVienPanel();
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NHÀ CUNG CẤP":
                return new TongHopMatHangNhapTheoNhaCungCapPanel();
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NHÂN VIÊN":
                return new TongHopMatHangNhapTheoNhanVienPanel();
            case "TỔNG HỢP NHẬP THEO NGÀY":
                return new TongHopNhapTheoNgayPanel();
            case "TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP":
                return new TongHopNhapTheoNhaCungCapPanel();
            case "TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP NHẬP THEO NHÂN VIÊN":
                return new TongHopNhapTheoNhanVienPanel();
            case "TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NGÀY":
                return new TongHopMatHangNhapTheoNgayPanel();
            case "BÁO CÁO TỔNG HỢP NHẬP KHO":
            case "QUẢN LÝ NHẬP KHO":
                return new QuanLyNhapKhoPanel();
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NGÀY":
                return new TongHopMatHangXuatTheoNgayPanel();
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN":
                return new TongHopMatHangXuatTheoNhanVienPanel();
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NHÂN VIÊN XUẤT":
            case "DANH SÁCH PHIẾU XUẤT THEO NHÂN VIÊN":
                return new BaoCaoDanhSachPhieuXuatTheoNhanVienPanel();
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU XUẤT THEO NGÀY":
                return new BaoCaoDanhSachPhieuXuatTheoNgayPanel();
            case "TỔNG HỢP XUẤT KHÁC THEO NGÀY":
                return new TongHopXuatKhacTheoNgayPanel();
            case "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN":
                return new TongHopXuatKhacTheoNhanVienPanel();
            case "BÁO CÁO TỔNG HỢP XUẤT KHO":
            case "QUẢN LÝ XUẤT KHO":
                return new QuanLyXuatKhoPanel();
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NGÀY":
                return new DanhSachPhieuChuyenKhoTheoNgayPanel();
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NHÂN VIÊN XUẤT":
                return new DanhSachPhieuChuyenKhoTheoNhanVienXuatPanel();
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NHÂN VIÊN NHẬP":
                return new DanhSachPhieuChuyenKhoTheoNhanVienNhanPanel();
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NHÂN VIÊN NHẬN":
                return new TongHopMatHangChuyenKhoTheoNhanVienNhanPanel();
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NHÂN VIÊN CHUYỂN":
                return new TongHopMatHangChuyenKhoTheoNhanVienChuyenPanel();
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NGÀY":
                return new TongHopMatHangChuyenKhoTheoNgayPanel();
            case "BÁO CÁO TỔNG HỢP CHUYỂN KHO":
            case "QUẢN LÝ CHUYỂN KHO":
                return new QuanLyChuyenKhoPanel();
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY":
            case "BÁO CÁO KIỂM KÊ THEO NGÀY":
                return new BaoCaoDanhSachPhieuKiemKeTheoNgayPanel();
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "BÁO CÁO KIỂM KÊ THEO NHÂN VIÊN":
                return new BaoCaoDanhSachPhieuKiemKeTheoNhanVienPanel();
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG KIỂM KÊ THEO NGÀY":
                return new TongHopMatHangKiemKeTheoNgayPanel();
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "TỔNG HỢP MẶT HÀNG KIỂM KÊ THEO NHÂN VIÊN":
                return new TongHopMatHangKiemKeTheoNhanVienPanel();
            case "BÁO CÁO TỔNG HỢP KIỂM KÊ KHO":
            case "QUẢN LÝ KIỂM KÊ":
            case "PHIẾU KIỂM KÊ":
                return new QuanLyKiemKePanel();
            default:
                return createPlaceholderPanel(title);
        }
    }

    public static boolean isReport(String title) {
        if (title == null) return false;
        String t = title.trim().toUpperCase();
        return t.startsWith("BÁO CÁO") || t.startsWith("TỔNG HỢP") || t.startsWith("DANH SÁCH PHIẾU")
                || t.startsWith("CHI TIẾT") || t.startsWith("BIỂU ĐỒ") || t.startsWith("ĐỐI CHIẾU")
                || t.equals("THẺ KHO") || t.equals("BẢNG TỔNG HỢP THỊ PHẦN") || t.equals("PHÂN TÍCH TÌNH HÌNH BÁN HÀNG")
                || t.equals("KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT") || t.equals("DANH SÁCH ĐỢT KHUYẾN MẠI")
                || t.equals("DANH SÁCH MẶT HÀNG THEO NHÓM") || t.equals("DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT")
                || t.equals("DANH SÁCH KHÁCH HÀNG THEO NHÓM") || t.equals("DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN")
                || t.equals("DANH SÁCH NHÀ CUNG CẤP THEO NHÓM") || t.equals("DANH SÁCH ĐẶT HÀNG THEO NGÀY")
                || t.equals("DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG");
    }

    public static JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("<html><center><h3>" + title + "</h3><p style='color:gray;'>Chức năng đang mở trong phiên làm việc.</p></center></html>");
        panel.add(label);
        return panel;
    }
}
