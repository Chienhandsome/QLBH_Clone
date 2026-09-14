package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.baocao.BaoCaoKetQuaKinhDoanhPanel;
import kazuko.ex.ui.baocao.ChiTietHoatDongTrongNgayPanel;
import kazuko.ex.ui.baocao.ReportPanelFactory;
import kazuko.ex.ui.baogia.BaoGiaBanHangPanel;
import kazuko.ex.ui.congno.CongNoKhachHangBanDauDialog;
import kazuko.ex.ui.congno.CongNoNhaCungCapBanDauDialog;
import kazuko.ex.ui.congno.QuanLyCongNoKhachHangPanel;
import kazuko.ex.ui.congno.QuanLyCongNoNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.CaLamViecDialog;
import kazuko.ex.ui.danhmuc.DanhMucDotKhuyenMaiPanel;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.DanhMucNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.KhachHangThanThietPanel;
import kazuko.ex.ui.danhmuc.KhoHangDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiDialog;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.danhmuc.TaiKhoanNganHangDialog;
import kazuko.ex.ui.dathang.DatHangNhaCungCapPanel;
import kazuko.ex.ui.dathang.KhachDatHangPanel;
import kazuko.ex.ui.help.AboutDialog;
import kazuko.ex.ui.help.RegistrationDialog;
import kazuko.ex.ui.hoadon.HoaDonBanHangPanel;
import kazuko.ex.ui.hoadon.QuanLyBanHangPanel;
import kazuko.ex.ui.hoadon.ThongKeDoanhThuPanel;
import kazuko.ex.ui.hoadon.ThongKeMatHangBanPanel;
import kazuko.ex.ui.khohang.ChonKhoBanDauDialog;
import kazuko.ex.ui.khohang.QuanLyChuyenKhoPanel;
import kazuko.ex.ui.khohang.QuanLyKiemKePanel;
import kazuko.ex.ui.khohang.QuanLyNhapKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonNhieuKhoPanel;
import kazuko.ex.ui.khohang.QuanLyXuatKhoPanel;
import kazuko.ex.ui.khohang.TinhLaiGiaVonDialog;
import kazuko.ex.ui.khohang.TonKhoBanDauDialog;
import kazuko.ex.ui.nhansu.ChamCongPanel;
import kazuko.ex.ui.nhansu.TamUngLuongPanel;
import kazuko.ex.ui.nhansu.ThuongPhatPanel;
import kazuko.ex.ui.nhansu.TinhLuongPanel;
import kazuko.ex.ui.quantri.ChiTieuDoanhThuPanel;
import kazuko.ex.ui.quy.DanhMucPhieuChiPanel;
import kazuko.ex.ui.quy.DanhMucPhieuThuPanel;
import kazuko.ex.ui.quy.TaoPhieuChiDialog;
import kazuko.ex.ui.quy.TaoPhieuThuDialog;
import kazuko.ex.ui.quy.TonQuyPanel;
import kazuko.ex.ui.system.CauHinhToanHeThongDialog;
import kazuko.ex.ui.system.GhiChuNhanhDialog;
import kazuko.ex.ui.system.LuuVetHoatDongPanel;
import kazuko.ex.ui.system.QuanLyNguoiDungDialog;
import kazuko.ex.ui.system.TaoMoiCSDLDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Full-screen touch shell for the existing sales invoice workflow.
 *
 * The screen only supplies a touch-oriented presentation. All invoice state,
 * validation, pricing, persistence and reporting remain in
 * {@link HoaDonBanHangPanel} and its existing DAO collaborators.
 */
public final class TouchSalesFrame extends JFrame {
    private static final String DASHBOARD_CARD = "dashboard";
    private static final String SALES_CARD = "sales";
    private static final String MODULE_CARD = "module";

    private final SUser user;
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private JPanel activeModuleScreen;

    public TouchSalesFrame(SUser user) {
        this.user = user;
        String username = user != null && user.getUsername() != null ? user.getUsername() : "";
        setTitle("TAP Shop TS" + (username.isBlank() ? "" : " - " + username));
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        screens.add(new TouchDashboardPanel(user, this::showSales, this::logout, this::openModule),
                DASHBOARD_CARD);
        screens.add(new HoaDonBanHangPanel(true, this::showDashboard), SALES_CARD);
        setContentPane(screens);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                exitApplication();
            }
        });

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        showDashboard();
    }

    private void showDashboard() {
        navigation.show(screens, DASHBOARD_CARD);
    }

    private void showSales() {
        navigation.show(screens, SALES_CARD);
    }

    private void showBusinessPanel(String title, JPanel panel) {
        if (activeModuleScreen != null) screens.remove(activeModuleScreen);
        activeModuleScreen = new TouchModulePanel(title, panel, this::showDashboard);
        screens.add(activeModuleScreen, MODULE_CARD);
        navigation.show(screens, MODULE_CARD);
        screens.revalidate();
        screens.repaint();
    }

    private void showTouchPanel(JPanel panel) {
        if (activeModuleScreen != null) screens.remove(activeModuleScreen);
        activeModuleScreen = panel;
        screens.add(activeModuleScreen, MODULE_CARD);
        navigation.show(screens, MODULE_CARD);
        screens.revalidate();
        screens.repaint();
    }

    private void logout() {
        if (!TouchConfirmDialog.show(this, "BẠN CÓ MUỐN ĐĂNG XUẤT KHỎI HỆ<br>THỐNG KHÔNG?")) return;
        AuthService.logout();
        dispose();
        SwingUtilities.invokeLater(() -> {
            TouchLoginFrame login = new TouchLoginFrame(
                    nextUser -> new TouchSalesFrame(nextUser).setVisible(true));
            login.setVisible(true);
        });
    }

    private void openModule(String module) {
        if (module == null || module.isBlank()) return;
        String key = module.trim();

        switch (key) {
            // HỆ THỐNG
            case "...", "CƠ SỞ DỮ LIỆU..." -> new DatabaseConfigDialog(this).setVisible(true);
            case "TẠO MỚI CƠ SỞ DỮ LIỆU" -> new TaoMoiCSDLDialog(this).setVisible(true);
            case "SAO LƯU CƠ SỞ DỮ LIỆU", "SAO LƯU DỮ LIỆU" ->
                    showBusinessPanel("SAO LƯU DỮ LIỆU", new TouchSaoLuuPanel(this::showDashboard));
            case "PHỤC HỒI CƠ SỞ DỮ LIỆU", "PHỤC HỒI DỮ LIỆU" ->
                    showBusinessPanel("PHỤC HỒI DỮ LIỆU", new TouchKhoiPhucPanel(this::showDashboard));
            case "ĐỔI MẬT KHẨU ĐĂNG NHẬP" ->
                    showTouchPanel(new TouchDoiMatKhauPanel(user, this::showDashboard));
            case "ĐĂNG XUẤT KHỎI HỆ THỐNG", "ĐĂNG XUẤT" -> logout();
            case "THOÁT KHỎI HỆ THỐNG", "THOÁT" -> exitApplication();

            // HOẠT ĐỘNG
            case "DANH MỤC CỬA HÀNG" -> showTouchPanel(new TouchCuaHangPanel(this::showDashboard));
            case "DANH MỤC MẶT HÀNG" -> showTouchPanel(new TouchMatHangPanel(this::showDashboard));
            case "DANH MỤC KHÁCH HÀNG" -> showBusinessPanel(key, new DanhMucKhachHangPanel());
            case "GỬI TIN NHẮN KHÁCH HÀNG", "GỬI TIN NHẮN TỚI KHÁCH HÀNG" ->
                    showBusinessPanel(key, ReportPanelFactory.createPlaceholderPanel("Gửi tin nhắn tới khách hàng"));
            case "DANH MỤC ĐỢT KHUYẾN MẠI", "ĐỢT KHUYẾN MÃI" ->
                    showBusinessPanel(key, new DanhMucDotKhuyenMaiPanel());
            case "KHÁCH HÀNG THÂN THIẾT" -> showBusinessPanel(key, new KhachHangThanThietPanel());
            case "BÁO GIÁ BÁN HÀNG" -> showBusinessPanel(key, new BaoGiaBanHangPanel());
            case "KHÁCH ĐẶT HÀNG" -> showBusinessPanel(key, new KhachDatHangPanel());
            case "HÓA ĐƠN BÁN HÀNG", "HÓA ĐƠN" -> showSales();
            case "QUẢN LÝ BÁN HÀNG" -> showBusinessPanel(key, new QuanLyBanHangPanel());
            case "LƯU VẾT HOẠT ĐỘNG" -> showBusinessPanel(key, new LuuVetHoatDongPanel());
            case "THỐNG KÊ DOANH THU" -> showBusinessPanel(key, new ThongKeDoanhThuPanel());
            case "THỐNG KÊ MẶT HÀNG BÁN" -> showBusinessPanel(key, new ThongKeMatHangBanPanel());
            case "TỔNG HỢP KẾT QUẢ KINH DOANH", "TỔNG HỢP KQKD" ->
                    showBusinessPanel(key, new BaoCaoKetQuaKinhDoanhPanel());
            case "CHI TIẾT HOẠT ĐỘNG NGÀY", "CHI TIẾT HOẠT ĐỘNG" ->
                    showBusinessPanel(key, new ChiTietHoatDongTrongNgayPanel());

            // KHO HÀNG
            case "DANH MỤC KHO HÀNG" -> new KhoHangDialog(this).setVisible(true);
            case "DANH MỤC NHÀ CUNG CẤP" -> showBusinessPanel(key, new DanhMucNhaCungCapPanel());
            case "ĐẶT HÀNG NHÀ CUNG CẤP" -> showBusinessPanel(key, new DatHangNhaCungCapPanel());
            case "NHẬP HÀNG VÀO KHO" -> showBusinessPanel(key, new QuanLyNhapKhoPanel());
            case "XUẤT KHO" -> showBusinessPanel(key, new QuanLyXuatKhoPanel());
            case "CHUYỂN KHO" -> showBusinessPanel(key, new QuanLyChuyenKhoPanel());
            case "KIỂM KÊ KHO" -> showBusinessPanel(key, new QuanLyKiemKePanel());
            case "TÍNH LẠI GIÁ VỐN" -> new TinhLaiGiaVonDialog(this).setVisible(true);
            case "TỒN KHO" -> showBusinessPanel(key, new QuanLyTonKhoPanel());
            case "TỒN NHIỀU KHO" -> showBusinessPanel(key, new QuanLyTonNhieuKhoPanel());

            // QUỸ
            case "DANH MỤC LÝ DO THU CHI" -> new LyDoThuChiDialog(this).setVisible(true);
            case "DANH MỤC TÀI KHOẢN NGÂN HÀNG" -> new TaiKhoanNganHangDialog(this).setVisible(true);
            case "TẠO PHIẾU THU" -> new TaoPhieuThuDialog(this).setVisible(true);
            case "TẠO PHIẾU CHI" -> new TaoPhieuChiDialog(this).setVisible(true);
            case "DANH MỤC PHIẾU THU" -> showBusinessPanel(key, new DanhMucPhieuThuPanel());
            case "DANH MỤC PHIẾU CHI" -> showBusinessPanel(key, new DanhMucPhieuChiPanel());
            case "TỒN QUỸ" -> showBusinessPanel(key, new TonQuyPanel());

            // CÔNG NỢ
            case "CÔNG NỢ KHÁCH HÀNG" -> showBusinessPanel(key, new QuanLyCongNoKhachHangPanel());
            case "CÔNG NỢ NHÀ CUNG CẤP" -> showBusinessPanel(key, new QuanLyCongNoNhaCungCapPanel());
            case "CÔNG NỢ KHÁCH HÀNG BAN ĐẦU" -> new CongNoKhachHangBanDauDialog(this).setVisible(true);
            case "CÔNG NỢ NHÀ CUNG CẤP BAN ĐẦU" -> new CongNoNhaCungCapBanDauDialog(this).setVisible(true);

            // NHÂN SỰ
            case "DANH MỤC NHÂN VIÊN" -> new NhanVienDialog(this).setVisible(true);
            case "DANH MỤC CA LÀM VIỆC" -> new CaLamViecDialog(this).setVisible(true);
            case "TẠM ỨNG LƯƠNG" -> showBusinessPanel(key, new TamUngLuongPanel());
            case "THƯỞNG PHẠT" -> showBusinessPanel(key, new ThuongPhatPanel());
            case "CHẤM CÔNG" -> showBusinessPanel(key, new ChamCongPanel());
            case "TÍNH LƯƠNG" -> showBusinessPanel(key, new TinhLuongPanel());

            // QUẢN TRỊ
            case "CHỈ TIÊU DOANH THU" -> showBusinessPanel(key, new ChiTieuDoanhThuPanel());
            case "XÓA DỮ LIỆU" -> showBusinessPanel(key, ReportPanelFactory.createPlaceholderPanel("Xóa dữ liệu"));
            case "CÔNG CỤ NHÀ PHÁT TRIỂN" -> showBusinessPanel(key, ReportPanelFactory.createPlaceholderPanel("Công cụ nhà phát triển"));
            case "NGƯỜI DÙNG VÀ PHÂN QUYỀN" -> new QuanLyNguoiDungDialog(this).setVisible(true);
            case "CẤU HÌNH TOÀN HỆ THỐNG" -> new CauHinhToanHeThongDialog(this).setVisible(true);
            case "TỒN KHO BAN ĐẦU" -> {
                ChonKhoBanDauDialog selectDlg = new ChonKhoBanDauDialog(this);
                selectDlg.setVisible(true);
                if (selectDlg.isConfirmed() && selectDlg.getSelectedKho() != null) {
                    new TonKhoBanDauDialog(this, selectDlg.getSelectedKho()).setVisible(true);
                }
            }

            // TRỢ GIÚP
            case "HƯỚNG DẪN SỬ DỤNG" -> JOptionPane.showMessageDialog(this,
                    "Phần mềm Quản lý Bán lẻ, Cửa hàng, Siêu thị V5.1 Tân An Phát\nHotline hỗ trợ: 0967041111\nWebsite: https://tananphat.vn",
                    "Hướng dẫn sử dụng", JOptionPane.INFORMATION_MESSAGE);
            case "HỖ TRỢ TỪ XA TEAMVIEWER" -> JOptionPane.showMessageDialog(this,
                    "Vui lòng liên hệ tổng đài Tân An Phát để được hỗ trợ từ xa:\nHotline: 0967041111",
                    "Hỗ trợ từ xa", JOptionPane.INFORMATION_MESSAGE);
            case "ĐĂNG KÝ BẢN QUYỀN..." -> new RegistrationDialog(this).setVisible(true);
            case "THÔNG TIN PHẦN MỀM" -> new AboutDialog(this).setVisible(true);
            case "GHI CHÚ" -> new GhiChuNhanhDialog(this).setVisible(true);

            default -> {
                if (ReportPanelFactory.isReport(key)) {
                    showBusinessPanel(key, ReportPanelFactory.createReportPanel(key));
                } else {
                    showModuleNotice(key);
                }
            }
        }
    }

    private void exitApplication() {
        if (TouchConfirmDialog.show(this, "BẠN CÓ MUỐN THOÁT KHỎI HỆ THỐNG<br>KHÔNG?")) {
            AuthService.logout();
            dispose();
        }
    }

    private void showModuleNotice(String module) {
        JOptionPane.showMessageDialog(this,
                "Màn hình " + module + " đang được cập nhật.",
                module, JOptionPane.INFORMATION_MESSAGE);
    }
}
