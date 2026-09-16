package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.baocao.ReportPanelFactory;
import kazuko.ex.ui.baogia.BaoGiaBanHangPanel;
import kazuko.ex.ui.congno.CongNoKhachHangBanDauDialog;
import kazuko.ex.ui.congno.CongNoNhaCungCapBanDauDialog;
import kazuko.ex.ui.congno.QuanLyCongNoKhachHangPanel;
import kazuko.ex.ui.congno.QuanLyCongNoNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.CaLamViecDialog;
import kazuko.ex.ui.danhmuc.DanhMucDotKhuyenMaiPanel;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.KhachHangThanThietPanel;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.danhmuc.TaiKhoanNganHangDialog;
import kazuko.ex.ui.dathang.DatHangNhaCungCapPanel;
import kazuko.ex.ui.dathang.KhachDatHangPanel;
import kazuko.ex.ui.help.AboutDialog;
import kazuko.ex.ui.help.RegistrationDialog;
import kazuko.ex.ui.hoadon.HoaDonBanHangPanel;
import kazuko.ex.ui.hoadon.QuanLyBanHangPanel;
import kazuko.ex.ui.khohang.ChonKhoBanDauDialog;
import kazuko.ex.ui.khohang.QuanLyChuyenKhoPanel;
import kazuko.ex.ui.khohang.QuanLyKiemKePanel;
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
import kazuko.ex.ui.quy.TonQuyPanel;
import kazuko.ex.ui.system.CauHinhToanHeThongDialog;
import kazuko.ex.ui.system.GhiChuNhanhDialog;
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
            case "DANH MỤC KHÁCH HÀNG" -> showTouchPanel(new TouchKhachHangPanel(this::showDashboard));
            case "GỬI TIN NHẮN KHÁCH HÀNG", "GỬI TIN NHẮN TỚI KHÁCH HÀNG" ->
                    showBusinessPanel(key, ReportPanelFactory.createPlaceholderPanel("Gửi tin nhắn tới khách hàng"));
            case "DANH MỤC ĐỢT KHUYẾN MẠI", "ĐỢT KHUYẾN MÃI" ->
                    showBusinessPanel(key, new DanhMucDotKhuyenMaiPanel());
            case "KHÁCH HÀNG THÂN THIẾT" -> showTouchPanel(new TouchKhachHangThanThietPanel(this::showDashboard));
            case "BÁO GIÁ BÁN HÀNG" -> showBusinessPanel(key, new BaoGiaBanHangPanel());
            case "KHÁCH ĐẶT HÀNG" -> showTouchPanel(new TouchKhachDatHangPanel(this::showDashboard));
            case "HÓA ĐƠN BÁN HÀNG", "HÓA ĐƠN" -> showSales();
            case "QUẢN LÝ BÁN HÀNG" -> showTouchPanel(new TouchQuanLyBanHangPanel(this::showDashboard));
            case "LƯU VẾT HOẠT ĐỘNG" -> showTouchPanel(new TouchLuuVetPanel(this::showDashboard));
            case "THỐNG KÊ DOANH THU" -> showTouchPanel(new TouchThongKeDoanhThuPanel(this::showDashboard));
            case "THỐNG KÊ MẶT HÀNG BÁN" -> showTouchPanel(new TouchThongKeMatHangBanPanel(this::showDashboard));
            case "TỔNG HỢP KẾT QUẢ KINH DOANH", "TỔNG HỢP KQKD" ->
                    showTouchPanel(new TouchPhanTichKinhDoanhPanel(false, this::showDashboard));
            case "CHI TIẾT HOẠT ĐỘNG NGÀY", "CHI TIẾT HOẠT ĐỘNG" ->
                    showTouchPanel(new TouchPhanTichKinhDoanhPanel(true, this::showDashboard));

            // KHO HÀNG
            case "DANH MỤC KHO HÀNG" -> showTouchPanel(new TouchKhoHangPanel(this::showDashboard));
            case "DANH MỤC NHÀ CUNG CẤP" -> showTouchPanel(new TouchNhaCungCapPanel(this::showDashboard));
            case "ĐẶT HÀNG NHÀ CUNG CẤP" -> showBusinessPanel(key, new DatHangNhaCungCapPanel());
            case "NHẬP HÀNG VÀO KHO" -> showTouchPanel(new TouchNhapKhoPanel(this::showDashboard));
            case "XUẤT KHO" -> showBusinessPanel(key, new QuanLyXuatKhoPanel());
            case "CHUYỂN KHO" -> showBusinessPanel(key, new QuanLyChuyenKhoPanel());
            case "KIỂM KÊ KHO" -> showBusinessPanel(key, new QuanLyKiemKePanel());
            case "TÍNH LẠI GIÁ VỐN" -> new TinhLaiGiaVonDialog(this).setVisible(true);
            case "TỒN KHO" -> showTouchPanel(new TouchTonKhoPanel(this::showDashboard));
            case "TỒN NHIỀU KHO" -> showBusinessPanel(key, new QuanLyTonNhieuKhoPanel());

            // QUỸ
            case "DANH MỤC LÝ DO THU CHI" ->
                    showTouchPanel(new TouchLyDoThuChiPanel(this::showDashboard));
            case "DANH MỤC TÀI KHOẢN NGÂN HÀNG" -> new TaiKhoanNganHangDialog(this).setVisible(true);
            case "TẠO PHIẾU THU" -> showTouchPanel(new TouchPhieuThuPanel(this::showDashboard));
            case "TẠO PHIẾU CHI" -> showTouchPanel(new TouchPhieuChiPanel(this::showDashboard));
            case "DANH MỤC PHIẾU THU" -> showTouchPanel(new TouchDanhMucPhieuThuPanel(this::showDashboard));
            case "DANH MỤC PHIẾU CHI" -> showTouchPanel(new TouchDanhMucPhieuChiPanel(this::showDashboard));
            case "TỒN QUỸ" -> showTouchPanel(new TouchTonQuyPanel(this::showDashboard));

            // CÔNG NỢ
            case "CÔNG NỢ KHÁCH HÀNG" -> showTouchPanel(new TouchCongNoKhachHangPanel(this::showDashboard));
            case "CÔNG NỢ NHÀ CUNG CẤP" -> showTouchPanel(new TouchCongNoNhaCungCapPanel(this::showDashboard));
            case "CÔNG NỢ KHÁCH HÀNG BAN ĐẦU" -> new CongNoKhachHangBanDauDialog(this).setVisible(true);
            case "CÔNG NỢ NHÀ CUNG CẤP BAN ĐẦU" -> new CongNoNhaCungCapBanDauDialog(this).setVisible(true);

            // NHÂN SỰ
            case "DANH MỤC NHÂN VIÊN" -> showTouchPanel(new TouchNhanVienPanel(this::showDashboard));
            case "DANH MỤC CA LÀM VIỆC" -> showTouchPanel(new TouchCaLamViecPanel(this::showDashboard));
            case "TẠM ỨNG LƯƠNG" -> showBusinessPanel(key, new TamUngLuongPanel());
            case "THƯỞNG PHẠT" -> showBusinessPanel(key, new ThuongPhatPanel());
            case "CHẤM CÔNG" -> showBusinessPanel(key, new ChamCongPanel());
            case "TÍNH LƯƠNG" -> showBusinessPanel(key, new TinhLuongPanel());

            // QUẢN TRỊ
            case "CHỈ TIÊU DOANH THU" -> showBusinessPanel(key, new ChiTieuDoanhThuPanel());
            case "XÓA DỮ LIỆU" -> showTouchPanel(new TouchXoaDuLieuPanel(this::showDashboard));
            case "CÔNG CỤ NHÀ PHÁT TRIỂN" -> showBusinessPanel(key, ReportPanelFactory.createPlaceholderPanel("Công cụ nhà phát triển"));
            case "NGƯỜI DÙNG VÀ PHÂN QUYỀN" -> showTouchPanel(new TouchNguoiDungVaPhanQuyenPanel(this::showDashboard));
            case "CẤU HÌNH TOÀN HỆ THỐNG" -> showTouchPanel(new TouchCauHinhToanHeThongPanel(this::showDashboard));
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
            case "ĐĂNG KÝ BẢN QUYỀN..." -> showTouchPanel(new TouchDangKyBanQuyenPanel(this::showDashboard));
            case "THÔNG TIN PHẦN MỀM" -> showTouchPanel(new TouchThongTinPhanMemPanel(this::showDashboard,
                    () -> showTouchPanel(new TouchDangKyBanQuyenPanel(this::showDashboard))));
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
