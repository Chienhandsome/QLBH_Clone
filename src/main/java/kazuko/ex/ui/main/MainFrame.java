package kazuko.ex.ui.main;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.service.PermissionService;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.auth.DoiMatKhauDialog;
import kazuko.ex.ui.auth.LoginForm;
import kazuko.ex.ui.baocao.BaoCaoPhieuThuTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoPhieuThuTheoLyDoPanel;
import kazuko.ex.ui.baocao.BaoCaoPhieuChiTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoPhieuChiTheoLyDoPanel;
import kazuko.ex.ui.baocao.TongHopThuChiTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopThuChiTheoLyDoPanel;
import kazuko.ex.ui.baocao.BaoCaoTonQuyPanel;
import kazuko.ex.ui.baocao.BaoCaoKhachHangTheoNhomPanel;
import kazuko.ex.ui.baocao.BaoCaoKhachHangTheoNhanVienPanel;
import kazuko.ex.ui.baocao.BaoCaoNhaCungCapTheoNhomPanel;
import kazuko.ex.ui.baocao.BaoCaoDotKhuyenMaiPanel;
import kazuko.ex.ui.baocao.BaoCaoMatHangTheoNhomPanel;
import kazuko.ex.ui.baocao.BaoCaoMatHangTheoHangSanXuatPanel;
import kazuko.ex.ui.baocao.BaoCaoKhachHangSinhNhatPanel;
import kazuko.ex.ui.baocao.BaoCaoKhachHangThanThietPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoGioPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoKichThuocPanel;
import kazuko.ex.ui.baocao.BaoCaoTongHopGiaTriBanTheoThangPanel;
import kazuko.ex.ui.baocao.BaoCaoTongHopSoLuongBanTheoThangPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoChiTietBanHangTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoKhachHangPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopMatHangBanTheoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopMatHangBanTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopMatHangBanTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopBanHangTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopBanTheoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopBanTheoNhanVienBanPanel;
import kazuko.ex.ui.baocao.TongHopBanTheoThuNganPanel;
import kazuko.ex.ui.baocao.BaoCaoChiTietBanHangTheoThuNganPanel;
import kazuko.ex.ui.baocao.BaoCaoBanHangTheoThuNganPanel;
import kazuko.ex.ui.baocao.TongHopMatHangBanTheoThuNganPanel;
import kazuko.ex.ui.baocao.TongHopTienVanChuyenTheoNhanVienPanel;
import kazuko.ex.ui.baocao.ChiTietHangBanTheoDoiTacPanel;
import kazuko.ex.ui.baocao.TongHopHoaHongTheoNVKDPanel;
import kazuko.ex.ui.baocao.ChiTietBanHangTheoHoaDonPanel;
import kazuko.ex.ui.baocao.BaoCaoChiTietHangKhuyenMaiPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachDatHangTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachDatHangTheoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopDatHangTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopDatHangTheoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopMatHangDatTheoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopMatHangDatTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoTonKhoTheoKichThuocPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuKiemKeTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopMatHangKiemKeTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopMatHangKiemKeTheoNhanVienPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuKiemKeTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopMatHangXuatTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopMatHangXuatTheoNhanVienPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuXuatTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopXuatKhacTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopXuatKhacTheoNhanVienPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuXuatTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuNhapTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuNhapTheoNhaCungCapPanel;
import kazuko.ex.ui.baocao.BaoCaoDanhSachPhieuNhapTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopMatHangNhapTheoNhaCungCapPanel;
import kazuko.ex.ui.baocao.TongHopMatHangNhapTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopNhapTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopNhapTheoNhaCungCapPanel;
import kazuko.ex.ui.baocao.TongHopNhapTheoNhanVienPanel;
import kazuko.ex.ui.baocao.TongHopMatHangNhapTheoNgayPanel;
import kazuko.ex.ui.baocao.TongHopMatHangChuyenKhoTheoNhanVienNhanPanel;
import kazuko.ex.ui.baocao.TongHopMatHangChuyenKhoTheoNhanVienChuyenPanel;
import kazuko.ex.ui.baocao.TongHopMatHangChuyenKhoTheoNgayPanel;
import kazuko.ex.ui.baocao.DanhSachPhieuChuyenKhoTheoNhanVienXuatPanel;
import kazuko.ex.ui.baocao.DanhSachPhieuChuyenKhoTheoNhanVienNhanPanel;
import kazuko.ex.ui.baocao.DanhSachPhieuChuyenKhoTheoNgayPanel;
import kazuko.ex.ui.baocao.BaoCaoHangTheoHanDungPanel;
import kazuko.ex.ui.baocao.BaoCaoHangDaHetHanPanel;
import kazuko.ex.ui.baocao.BaoCaoHangTonKhoHanDungPanel;
import kazuko.ex.ui.baocao.BaoCaoHangTonKhoPanel;
import kazuko.ex.ui.baocao.BaoCaoTongHopXuatNhapTonPanel;
import kazuko.ex.ui.baocao.TongHopLaiGopTheoMatHangGiaVonPanel;
import kazuko.ex.ui.baocao.TongHopLaiGopTheoMatHangGiaNhapPanel;
import kazuko.ex.ui.baocao.ChiTietLaiTheoHoaDonGiaVonPanel;
import kazuko.ex.ui.baocao.ChiTietLaiTheoHoaDonGiaNhapPanel;
import kazuko.ex.ui.baocao.BaoCaoKetQuaKinhDoanhPanel;
import kazuko.ex.ui.baocao.ChiTietHoatDongTrongNgayPanel;
import kazuko.ex.ui.baocao.BaoCao20MatHangBanChayNhatPanel;
import kazuko.ex.ui.hoadon.ThongKeDoanhThuPanel;
import kazuko.ex.ui.hoadon.ThongKeMatHangBanPanel;
import kazuko.ex.ui.baocao.BangTongHopThiPhanPanel;
import kazuko.ex.ui.baocao.BaoCaoPhanTichTinhHinhBanHangPanel;
import kazuko.ex.ui.baocao.BieuDoDoanhSoTheoNhomPanel;
import kazuko.ex.ui.baocao.BieuDoDoanhThuNgayTrongThangPanel;
import kazuko.ex.ui.baocao.BieuDoDoanhThuThangTrongNamPanel;
import kazuko.ex.ui.baocao.BieuDoTheoNhanVienKinhDoanhPanel;
import kazuko.ex.ui.baocao.BaoCaoCongNoNhaCungCapPanel;
import kazuko.ex.ui.baocao.TongHopCongNoNhaCungCapPanel;
import kazuko.ex.ui.baocao.BaoCaoCongNoKhachHangPanel;
import kazuko.ex.ui.baocao.TongHopCongNoKhachHangPanel;
import kazuko.ex.ui.baogia.BaoGiaBanHangPanel;



import kazuko.ex.ui.common.StatusPanel;
import kazuko.ex.ui.danhmuc.CaLamViecDialog;
import kazuko.ex.ui.danhmuc.CuaHangDialog;
import kazuko.ex.ui.danhmuc.DanhMucDotKhuyenMaiPanel;
import kazuko.ex.ui.congno.CongNoKhachHangBanDauDialog;
import kazuko.ex.ui.congno.CongNoNhaCungCapBanDauDialog;
import kazuko.ex.ui.congno.QuanLyCongNoKhachHangPanel;
import kazuko.ex.ui.congno.QuanLyCongNoNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.DanhMucMatHangPanel;
import kazuko.ex.ui.danhmuc.DanhMucNhaCungCapPanel;
import kazuko.ex.ui.danhmuc.KhachHangThanThietPanel;
import kazuko.ex.ui.danhmuc.KhoHangDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiDialog;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.danhmuc.TaiKhoanNganHangDialog;
import kazuko.ex.ui.dathang.DatHangNhaCungCapPanel;
import kazuko.ex.ui.dathang.KhachDatHangPanel;
import kazuko.ex.ui.hoadon.HoaDonBanHangPanel;
import kazuko.ex.ui.hoadon.QuanLyBanHangPanel;
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
import kazuko.ex.ui.quy.DanhMucPhieuChiPanel;
import kazuko.ex.ui.quy.DanhMucPhieuThuPanel;
import kazuko.ex.ui.quy.TonQuyPanel;
import kazuko.ex.ui.quy.TaoPhieuChiDialog;
import kazuko.ex.ui.quy.TaoPhieuThuDialog;
import kazuko.ex.ui.system.CauHinhToanHeThongDialog;
import kazuko.ex.ui.system.GhiChuNhanhDialog;
import kazuko.ex.ui.system.KhoiPhucCSDLDialog;
import kazuko.ex.ui.system.LuuVetHoatDongPanel;
import kazuko.ex.ui.system.QuanLyNguoiDungDialog;
import kazuko.ex.ui.system.SaoLuuCSDLDialog;
import kazuko.ex.ui.system.TaoMoiCSDLDialog;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final SUser currentUser;
    private JTabbedPane mainTabbedPane;
    private ResponsiveToolBar ribbonToolBar;

    public MainFrame(SUser user) {
        this.currentUser = user;
        PermissionService.getInstance().initPermissions(user);
        initComponents();
    }

    private void initComponents() {
        setTitle("PHẦN MỀM QUẢN LÝ BÁN HÀNG");
        setWindowIcon();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onExit();
            }
        });

        // Phím nóng Ctrl + G mở nhanh danh sách menu
        getRootPane().registerKeyboardAction(
                e -> openQuickSearchDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Phím nóng Ctrl + W đóng tab hiện tại
        getRootPane().registerKeyboardAction(
                e -> closeCurrentTab(),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Phím nóng Ctrl + Shift + W đóng tất cả tab
        getRootPane().registerKeyboardAction(
                e -> closeAllTabs(),
                KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        setSize(1280, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Menu Bar trên cùng
        setJMenuBar(createTopMenuBar());

        // 2. Toolbar Ribbon chứa các nút thao tác nhanh (hỗ trợ dropdown khi co cửa sổ)
        ribbonToolBar = createRibbonToolBar();
        add(ribbonToolBar, BorderLayout.NORTH);

        // 3. Hệ thống Tab trung tâm với nút Dropdown danh sách tab & nút Đóng tab
        JPanel tabContainer = createTabContainer();
        add(tabContainer, BorderLayout.CENTER);

        // 4. Thanh trạng thái StatusPanel dưới chân trang
        StatusPanel statusPanel = new StatusPanel(currentUser);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createTabContainer() {
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mainTabbedPane.setUI(new CustomModernTabbedPaneUI());
        mainTabbedPane.addChangeListener(e -> updateTabHeaderStyles());

        // Cụm nút góc phải thanh Tab: danh sách, đóng tab hiện tại và đóng tất cả
        JPanel pnlTabControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 1));
        pnlTabControls.setOpaque(false);

        JButton btnTabList = createTabControlButton(ARROW_DOWN_ICON, "Danh sách các tab đang mở");
        JButton btnTabClose = createTabControlButton(CLOSE_CROSS_ICON, "Đóng tab hiện tại (Ctrl + W)");
        JButton btnCloseAllTabs = createTabControlButton(null, "Đóng tất cả tab (Ctrl + Shift + W)");
        btnCloseAllTabs.setText("Tắt tất cả");
        btnCloseAllTabs.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnCloseAllTabs.setPreferredSize(new Dimension(62, 20));

        btnTabList.addActionListener(e -> showTabListPopup(btnTabList));
        btnTabClose.addActionListener(e -> closeCurrentTab());
        btnCloseAllTabs.addActionListener(e -> closeAllTabs());

        pnlTabControls.add(btnTabList);
        pnlTabControls.add(btnTabClose);
        pnlTabControls.add(btnCloseAllTabs);

        JPanel container = new JPanel(null) {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                mainTabbedPane.setBounds(0, 0, w, h);
                pnlTabControls.setBounds(w - 118, 4, 114, 22);
            }
        };
        container.add(pnlTabControls);
        container.add(mainTabbedPane);

        // Mở sẵn tab mặc định được phép
        openDefaultPermittedTab();

        return container;
    }

    private void openDefaultPermittedTab() {
        if (PermissionService.getInstance().isFunctionAllowed("Hóa đơn bán hàng")) {
            openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel());
        } else if (PermissionService.getInstance().isFunctionAllowed("Danh mục mặt hàng")) {
            openTab("Danh mục mặt hàng", new DanhMucMatHangPanel());
        } else if (PermissionService.getInstance().isFunctionAllowed("Quản lý bán hàng")) {
            openTab("Quản lý bán hàng", new QuanLyBanHangPanel());
        } else if (PermissionService.getInstance().isFunctionAllowed("Tồn kho")) {
            openTab("Tồn kho", new QuanLyTonKhoPanel());
        } else if (PermissionService.getInstance().isFunctionAllowed("Danh mục khách hàng")) {
            openTab("Danh mục khách hàng", new DanhMucKhachHangPanel());
        } else {
            openTab("Bảng tin", createPlaceholderPanel("HỆ THỐNG QUẢN LÝ BÁN HÀNG"));
        }
    }

    private JButton createTabControlButton(Icon icon, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(icon);
        btn.setPreferredSize(new Dimension(20, 20));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBackground(new Color(235, 242, 252));
        btn.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255));
                btn.setBorder(new MatteBorder(1, 1, 1, 1, new Color(140, 175, 220)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(235, 242, 252));
                btn.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));
            }
        });
        return btn;
    }

    private void showTabListPopup(Component invoker) {
        if (mainTabbedPane.getTabCount() == 0) return;

        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(new LineBorder(new Color(170, 195, 225), 1));

        int selectedIdx = mainTabbedPane.getSelectedIndex();
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            final int tabIdx = i;
            String title = mainTabbedPane.getTitleAt(i);

            JMenuItem item = new JMenuItem(title);
            item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            item.setBackground(Color.WHITE);

            if (i == selectedIdx) {
                item.setIcon(CHECKED_TAB_ICON);
            } else {
                item.setIcon(UNCHECKED_TAB_ICON);
            }

            item.addActionListener(e -> mainTabbedPane.setSelectedIndex(tabIdx));
            popup.add(item);
        }

        popup.show(invoker, invoker.getWidth() - popup.getPreferredSize().width, invoker.getHeight());
    }

    private void closeCurrentTab() {
        int selected = mainTabbedPane.getSelectedIndex();
        if (selected >= 0) {
            mainTabbedPane.remove(selected);
        }
    }

    private void closeAllTabs() {
        mainTabbedPane.removeAll();
    }

    private void updateTabHeaderStyles() {
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            Component header = mainTabbedPane.getTabComponentAt(i);
            if (header instanceof CloseableTabComponent) {
                ((CloseableTabComponent) header).updateSelectionStyle();
            }
        }
    }

    private JMenuBar createTopMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. HỆ THỐNG
        JMenu mHeThong = new JMenu("HỆ THỐNG");
        JMenu mCSDL = new JMenu("Cơ sở dữ liệu...");
        mCSDL.setIcon(IconHelper.getIcon("database1.png", 16, 16));

        JMenuItem miTaoMoi = createFunctionMenuItem("Tạo mới", "Tạo mới cơ sở dữ liệu", null, null, e -> new TaoMoiCSDLDialog(this).setVisible(true));
        JMenuItem miSaoLuu = createFunctionMenuItem("Sao lưu", "Sao lưu cơ sở dữ liệu", null, null, e -> new SaoLuuCSDLDialog(this).setVisible(true));
        JMenuItem miPhucHoi = createFunctionMenuItem("Phục hồi", "Phục hồi cơ sở dữ liệu", null, null, e -> new KhoiPhucCSDLDialog(this).setVisible(true));
        
        mCSDL.add(miTaoMoi);
        mCSDL.add(miSaoLuu);
        mCSDL.add(miPhucHoi);

        JMenuItem miDoiMK = new JMenuItem("Đổi mật khẩu đăng nhập");
        miDoiMK.addActionListener(e -> new DoiMatKhauDialog(this, currentUser).setVisible(true));

        JMenuItem miDangXuat = new JMenuItem("Đăng xuất khỏi hệ thống");
        miDangXuat.setIcon(IconHelper.getIcon("window-new.png", 16, 16));
        miDangXuat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        miDangXuat.addActionListener(e -> onLogout());

        JMenuItem miThoat = new JMenuItem("Thoát khỏi hệ thống");
        miThoat.setFont(miThoat.getFont().deriveFont(Font.BOLD));
        miThoat.setIcon(IconHelper.getIcon("Cancelled.png", 16, 16));
        miThoat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        miThoat.addActionListener(e -> onExit());

        mHeThong.add(mCSDL);
        mHeThong.addSeparator();
        mHeThong.add(miDoiMK);
        mHeThong.addSeparator();
        mHeThong.add(miDangXuat);
        mHeThong.add(miThoat);

        // 2. HOẠT ĐỘNG
        JMenu mHoatDong = new JMenu("HOẠT ĐỘNG");

        // Group 1: Danh mục & Khách hàng
        JMenuItem miCuaHang = createFunctionMenuItem("Danh mục cửa hàng", "Folder2.png", e -> new CuaHangDialog(this).setVisible(true));
        JMenuItem miMatHang = createFunctionMenuItem("Danh mục mặt hàng", "1282529873_objects.png", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), e -> openTab("Danh mục mặt hàng", new DanhMucMatHangPanel()));
        JMenuItem miKhachHang = createFunctionMenuItem("Danh mục khách hàng", "users.png", KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK), e -> openTab("Danh mục khách hàng", new DanhMucKhachHangPanel()));
        JMenuItem miTinNhan = createFunctionMenuItem("Gửi tin nhắn tới khách hàng", "mail-send.png", e -> openTab("Gửi tin nhắn tới khách hàng", createPlaceholderPanel("Gửi tin nhắn tới khách hàng")));
        JMenuItem miKhuyenMai = createFunctionMenuItem("Danh mục đợt khuyến mại", "bookmark-import.png", e -> openTab("Danh mục đợt khuyến mại", new DanhMucDotKhuyenMaiPanel()));
        JMenuItem miThanThiet = createFunctionMenuItem("Khách hàng thân thiết", "btnCard.Icon.png", e -> openTab("Khách hàng thân thiết", new KhachHangThanThietPanel()));

        mHoatDong.add(miCuaHang);
        mHoatDong.add(miMatHang);
        mHoatDong.add(miKhachHang);
        mHoatDong.add(miTinNhan);
        mHoatDong.add(miKhuyenMai);
        mHoatDong.add(miThanThiet);

        // Divider 1
        mHoatDong.addSeparator();

        // Group 2: Báo giá & Đặt hàng
        JMenuItem miBaoGia = createFunctionMenuItem("Báo giá bán hàng", "Báo giá", "report--pencil.png", null, e -> openTab("Báo giá bán hàng", new BaoGiaBanHangPanel()));
        JMenuItem miDatHang = createFunctionMenuItem("Khách đặt hàng", "Đặt hàng", "tsbLogedUser.Image.png", null, e -> openTab("Khách đặt hàng", new KhachDatHangPanel()));

        mHoatDong.add(miBaoGia);
        mHoatDong.add(miDatHang);

        // Divider 2
        mHoatDong.addSeparator();

        // Group 3: Bán hàng & Hóa đơn
        JMenuItem miHoaDon = createFunctionMenuItem("Hóa đơn bán hàng", "btnNew.Image.png", KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK), e -> openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel()));
        JMenuItem miQLBanHang = createFunctionMenuItem("Quản lý bán hàng", "btnOpen.Image.png", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), e -> openTab("Quản lý bán hàng", new QuanLyBanHangPanel()));

        mHoatDong.add(miHoaDon);
        mHoatDong.add(miQLBanHang);

        // Divider 3
        mHoatDong.addSeparator();

        // Group 4: Nhật ký
        JMenuItem miLuuVet = createFunctionMenuItem("Lưu vết hoạt động", "tsbLog.Image.png", e -> openTab("Lưu vết hoạt động", new LuuVetHoatDongPanel()));

        mHoatDong.add(miLuuVet);

        // Divider 4
        mHoatDong.addSeparator();

        // Group 5: Thống kê & Báo cáo
        JMenuItem miTKDoanhThu = createFunctionMenuItem("Thống kê doanh thu", "Statistic16.png", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), e -> openTab("Thống kê doanh thu", new ThongKeDoanhThuPanel()));
        JMenuItem miTKMatHang = createFunctionMenuItem("Thống kê mặt hàng bán", "Tree.png", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), e -> openTab("Thống kê mặt hàng bán", new ThongKeMatHangBanPanel()));
        JMenuItem miTongHopKQKD = createFunctionMenuItem("Tổng hợp kết quả kinh doanh", "Report.png", e -> openTab("Tổng hợp KQKD", new BaoCaoKetQuaKinhDoanhPanel()));
        JMenuItem miChiTietHD = createFunctionMenuItem("Chi tiết hoạt động ngày", "arrow-000-medium.png", e -> openTab("Chi tiết hoạt động", new ChiTietHoatDongTrongNgayPanel()));

        mHoatDong.add(miTKDoanhThu);
        mHoatDong.add(miTKMatHang);
        mHoatDong.add(miTongHopKQKD);
        mHoatDong.add(miChiTietHD);

        // 3. KHO HÀNG
        JMenu mKhoHang = new JMenu("KHO HÀNG");

        JMenuItem miKhoHang = createFunctionMenuItem("Danh mục kho hàng", "Home.png", e -> new KhoHangDialog(this).setVisible(true));
        JMenuItem miNCC = createFunctionMenuItem("Danh mục nhà cung cấp", "users.png", e -> openTab("Danh mục nhà cung cấp", new DanhMucNhaCungCapPanel()));

        mKhoHang.add(miKhoHang);
        mKhoHang.add(miNCC);

        mKhoHang.addSeparator();

        JMenuItem miDatHangNCC = createFunctionMenuItem("Đặt hàng nhà cung cấp", "Quản lý đặt hàng nhà cung cấp", "report--pencil.png", null, e -> openTab("Đặt hàng nhà cung cấp", new DatHangNhaCungCapPanel()));
        JMenuItem miNhapKho = createFunctionMenuItem("Nhập hàng vào kho", "Nhập kho", "navigation-000-frame.png", null, e -> openTab("Quản lý nhập kho", new QuanLyNhapKhoPanel()));
        JMenuItem miXuatKhac = createFunctionMenuItem("Xuất kho", "navigation-180-frame.png", e -> openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel()));
        JMenuItem miChuyenKho = createFunctionMenuItem("Chuyển kho", "document_valid.png", e -> openTab("Quản lý chuyển kho", new QuanLyChuyenKhoPanel()));
        JMenuItem miKiemKe = createFunctionMenuItem("Kiểm kê kho", "edit-column.png", e -> openTab("Quản lý kiểm kê", new QuanLyKiemKePanel()));
        JMenuItem miTinhGiaVon = createFunctionMenuItem("Tính lại giá vốn", "Refresh.png", e -> new TinhLaiGiaVonDialog(this).setVisible(true));

        mKhoHang.add(miDatHangNCC);
        mKhoHang.add(miNhapKho);
        mKhoHang.add(miXuatKhac);
        mKhoHang.add(miChuyenKho);
        mKhoHang.add(miKiemKe);
        mKhoHang.add(miTinhGiaVon);

        mKhoHang.addSeparator();

        JMenuItem miTonKho = createFunctionMenuItem("Tồn kho", "Folder.png", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), e -> openTab("Tồn kho", new QuanLyTonKhoPanel()));
        JMenuItem miTonNhieuKho = createFunctionMenuItem("Tồn nhiều kho", "Tồn kho", "tsbTimeStamp.Image.png", null, e -> openTab("Tồn nhiều kho", new QuanLyTonNhieuKhoPanel()));

        mKhoHang.add(miTonKho);
        mKhoHang.add(miTonNhieuKho);

        // 4. QUỸ
        JMenu mQuy = new JMenu("QUỸ");

        JMenuItem miLyDoTC = createFunctionMenuItem("Danh mục lý do thu chi", "congno.png", e -> new LyDoThuChiDialog(this).setVisible(true));
        JMenuItem miTKNH = createFunctionMenuItem("Danh mục tài khoản ngân hàng", "database1.png", e -> new TaiKhoanNganHangDialog(this).setVisible(true));

        mQuy.add(miLyDoTC);
        mQuy.add(miTKNH);

        mQuy.addSeparator();

        JMenuItem miTaoThu = createFunctionMenuItem("Tạo phiếu thu", "Thu chi", "arrow-000-medium.png", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), e -> new TaoPhieuThuDialog(this).setVisible(true));
        JMenuItem miTaoChi = createFunctionMenuItem("Tạo phiếu chi", "Thu chi", "arrow-180-medium.png", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), e -> new TaoPhieuChiDialog(this).setVisible(true));
        JMenuItem miDSThu = createFunctionMenuItem("Danh mục phiếu thu", "Danh mục phiếu thu chi", "btnCard.Icon.png", null, e -> openTab("Danh mục phiếu thu", new DanhMucPhieuThuPanel()));
        JMenuItem miDSChi = createFunctionMenuItem("Danh mục phiếu chi", "Danh mục phiếu thu chi", "tsbTimeStamp.Image.png", null, e -> openTab("Danh mục phiếu chi", new DanhMucPhieuChiPanel()));

        mQuy.add(miTaoThu);
        mQuy.add(miTaoChi);
        mQuy.add(miDSThu);
        mQuy.add(miDSChi);

        mQuy.addSeparator();

        JMenuItem miTonQuy = createFunctionMenuItem("Tồn quỹ", "report--pencil.png", e -> openTab("Tồn quỹ", new TonQuyPanel()));

        mQuy.add(miTonQuy);

        // 5. CÔNG NỢ
        JMenu mCongNo = new JMenu("CÔNG NỢ");

        JMenuItem miCongNoKH = createFunctionMenuItem("Công nợ khách hàng", "congno.png", e -> openTab("Công nợ khách hàng", new QuanLyCongNoKhachHangPanel()));
        JMenuItem miCongNoNCC = createFunctionMenuItem("Công nợ nhà cung cấp", "tsbGenTotal.Image.png", e -> openTab("Công nợ nhà cung cấp", new QuanLyCongNoNhaCungCapPanel()));

        mCongNo.add(miCongNoKH);
        mCongNo.add(miCongNoNCC);

        // 6. NHÂN SỰ
        JMenu mNhanSu = new JMenu("NHÂN SỰ");

        JMenuItem miNhanVien = createFunctionMenuItem("Danh mục nhân viên", "users.png", e -> new NhanVienDialog(this).setVisible(true));
        JMenuItem miCaLamViec = createFunctionMenuItem("Danh mục ca làm việc", "property.png", e -> new CaLamViecDialog(this).setVisible(true));

        mNhanSu.add(miNhanVien);
        mNhanSu.add(miCaLamViec);

        mNhanSu.addSeparator();

        JMenuItem miTamUng = createFunctionMenuItem("Tạm ứng lương", "receipt--pencil.png", e -> openTab("Tạm ứng lương", new TamUngLuongPanel()));
        JMenuItem miThuongPhat = createFunctionMenuItem("Thưởng phạt", "Quản lý thưởng phạt", "asterisk-yellow.png", null, e -> openTab("Thưởng phạt", new ThuongPhatPanel()));
        JMenuItem miChamCong = createFunctionMenuItem("Chấm công", "document_valid.png", e -> openTab("Chấm công", new ChamCongPanel()));
        JMenuItem miTinhLuong = createFunctionMenuItem("Tính lương", "Statistic16.png", e -> openTab("Tính lương", new TinhLuongPanel()));

        mNhanSu.add(miTamUng);
        mNhanSu.add(miThuongPhat);
        mNhanSu.add(miChamCong);
        mNhanSu.add(miTinhLuong);

        // 7. QUẢN TRỊ
        JMenu mQuanTri = new JMenu("QUẢN TRỊ");

        JMenuItem miThuVienAnh = createFunctionMenuItem("Thư viện ảnh...", "Thư viện ảnh", null, null, e -> openTab("Thư viện ảnh", createPlaceholderPanel("Thư viện ảnh")));
        mQuanTri.add(miThuVienAnh);

        mQuanTri.addSeparator();

        JMenu mDuLieuBanDau = new JMenu("Dữ liệu ban đầu");
        JMenuItem miCongNoKHBD = createFunctionMenuItem("Công nợ khách hàng ban đầu", "Dữ liệu ban đầu", null, null, e -> new CongNoKhachHangBanDauDialog(this).setVisible(true));
        JMenuItem miCongNoNCCBD = createFunctionMenuItem("Công nợ nhà cung cấp ban đầu", "Dữ liệu ban đầu", null, null, e -> new CongNoNhaCungCapBanDauDialog(this).setVisible(true));
        JMenuItem miTonKhoBD = createFunctionMenuItem("Tồn kho ban đầu", "Dữ liệu ban đầu", null, null, e -> {
            ChonKhoBanDauDialog selectDlg = new ChonKhoBanDauDialog(this);
            selectDlg.setVisible(true);
            if (selectDlg.isConfirmed() && selectDlg.getSelectedKho() != null) {
                new TonKhoBanDauDialog(this, selectDlg.getSelectedKho()).setVisible(true);
            }
        });
        mDuLieuBanDau.add(miCongNoKHBD);
        mDuLieuBanDau.add(miCongNoNCCBD);
        mDuLieuBanDau.add(miTonKhoBD);
        mQuanTri.add(mDuLieuBanDau);

        JMenuItem miChiTieuDT = createFunctionMenuItem("Chỉ tiêu doanh thu", "Danh mục chỉ tiêu doanh thu", "property.png", null, e -> openTab("Chỉ tiêu doanh thu", new kazuko.ex.ui.quantri.ChiTieuDoanhThuPanel()));
        JMenuItem miXoaDuLieu = createFunctionMenuItem("Xóa dữ liệu", "clear.png", e -> openTab("Xóa dữ liệu", createPlaceholderPanel("Xóa dữ liệu")));
        JMenuItem miDevTools = createFunctionMenuItem("Công cụ nhà phát triển", "page_white_code.png", e -> openTab("Công cụ nhà phát triển", createPlaceholderPanel("Công cụ nhà phát triển")));
        JMenuItem miNguoiDung = createFunctionMenuItem("Người dùng và phân quyền", "Quản lý người dùng", "users.png", null, e -> new QuanLyNguoiDungDialog(this).setVisible(true));

        mQuanTri.add(miChiTieuDT);
        mQuanTri.add(miXoaDuLieu);
        mQuanTri.add(miDevTools);
        mQuanTri.add(miNguoiDung);

        mQuanTri.addSeparator();

        JMenuItem miCauHinh = createFunctionMenuItem("Cấu hình toàn hệ thống", "wrench-screwdriver.png", e -> openSystemConfiguration());
        if (miCauHinh != null) {
            miCauHinh.setFont(miCauHinh.getFont().deriveFont(Font.BOLD));
            mQuanTri.add(miCauHinh);
        }

        // 8. BÁO CÁO
        JMenu mBaoCao = new JMenu("BÁO CÁO");

        // 8.1 BÁO CÁO QUỸ
        JMenu mBaoCaoQuy = new JMenu("BÁO CÁO QUỸ");
        mBaoCaoQuy.add(createReportMenuItem("DANH SÁCH PHIẾU THU THEO NGÀY", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("DANH SÁCH PHIẾU THU THEO LÝ DO THU CHI", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("DANH SÁCH PHIẾU CHI THEO NGÀY", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("TỔNG HỢP THU CHI THEO NGÀY", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("TỔNG HỢP THU CHI THEO LÝ DO", "Report.png"));
        mBaoCaoQuy.add(createReportMenuItem("BÁO CÁO TỒN QUỸ", "receipt--pencil.png"));

        // 8.2 BÁO CÁO DANH MỤC
        JMenu mBaoCaoDanhMuc = new JMenu("BÁO CÁO DANH MỤC");
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH KHÁCH HÀNG THEO NHÓM", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH NHÀ CUNG CẤP THEO NHÓM", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH ĐỢT KHUYẾN MẠI", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH MẶT HÀNG THEO NHÓM", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT", "Report.png"));
        mBaoCaoDanhMuc.add(createReportMenuItem("KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT", "ptImage.Image.png"));

        // 8.3 BÁO CÁO BÁN HÀNG
        JMenu mBaoCaoBanHang = new JMenu("BÁO CÁO BÁN HÀNG");
        mBaoCaoBanHang.add(createReportMenuItem("KHÁCH HÀNG THÂN THIẾT", "btnCard.Icon.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO GIỜ", "Statistic16.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG", "page_white_excel.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG", "page_white_excel.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO NGÀY", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY", "report--pencil.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG BÁN THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG BÁN THEO NGÀY", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN KINH DOANH", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP BÁN HÀNG THEO NGÀY", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP BÁN THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP BÁN THEO NHÂN VIÊN BÁN", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP BÁN THEO THU NGÂN", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG BÁN THEO THU NGÂN", "Report.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN", "tick.png"));
        mBaoCaoBanHang.add(createReportMenuItem("BÁO CÁO BÁN HÀNG THEO THU NGÂN", "Report.png"));

        JMenu mBaoCaoBanHangKhac = new JMenu("BÁO CÁO BÁN HÀNG KHÁC");
        mBaoCaoBanHangKhac.add(createReportMenuItem("TỔNG HỢP TIỀN VẬN CHUYỂN THEO NHÂN VIÊN", "receipt--pencil.png"));
        mBaoCaoBanHangKhac.add(createReportMenuItem("CHI TIẾT BÁN HÀNG THEO ĐỐI TÁC", "document_valid.png"));
        mBaoCaoBanHangKhac.add(createReportMenuItem("TỔNG HỢP HOA HỒNG THEO NVKD", "Report.png"));
        mBaoCaoBanHangKhac.add(createReportMenuItem("CHI TIẾT BÁN HÀNG THEO HÓA ĐƠN", "tick.png"));
        mBaoCaoBanHangKhac.add(createReportMenuItem("BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI", "bookmark-import.png"));
        mBaoCaoBanHang.add(mBaoCaoBanHangKhac);

        // 8.4 BÁO CÁO ĐẶT HÀNG
        JMenu mBaoCaoDatHang = new JMenu("BÁO CÁO ĐẶT HÀNG");
        mBaoCaoDatHang.add(createReportMenuItem("DANH SÁCH ĐẶT HÀNG THEO NGÀY", "Report.png"));
        mBaoCaoDatHang.add(createReportMenuItem("DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoDatHang.add(createReportMenuItem("TỔNG HỢP ĐẶT HÀNG THEO NGÀY", "Report.png"));
        mBaoCaoDatHang.add(createReportMenuItem("TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoDatHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG ĐẶT THEO KHÁCH HÀNG", "Report.png"));
        mBaoCaoDatHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG ĐẶT THEO NGÀY", "Report.png"));

        // 8.5 BÁO CÁO KHO HÀNG
        JMenu mBaoCaoKhoHang = new JMenu("BÁO CÁO KHO HÀNG");
        mBaoCaoKhoHang.add(createReportMenuItem("BÁO CÁO TỒN KHO THEO KÍCH THƯỚC", "asterisk-yellow.png"));

        // 8.5.1 BÁO CÁO KIỂM KÊ
        JMenu mBaoCaoKiemKe = new JMenu("BÁO CÁO KIỂM KÊ");
        mBaoCaoKiemKe.add(createReportMenuItem("DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY", "Report.png"));
        mBaoCaoKiemKe.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY", "Report.png"));
        mBaoCaoKiemKe.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN", "Report.png"));
        mBaoCaoKiemKe.add(createReportMenuItem("DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN", "Report.png"));
        mBaoCaoKhoHang.add(mBaoCaoKiemKe);

        // 8.5.2 BÁO CÁO XUẤT KHÁC
        JMenu mBaoCaoXuatKhac = new JMenu("BÁO CÁO XUẤT KHÁC");
        mBaoCaoXuatKhac.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG XUẤT THEO NGÀY", "Report.png"));
        mBaoCaoXuatKhac.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN XUẤT", "Report.png"));
        mBaoCaoXuatKhac.add(createReportMenuItem("DANH SÁCH PHIẾU XUẤT KHO THEO NHÂN VIÊN XUẤT", "Report.png"));
        mBaoCaoXuatKhac.add(createReportMenuItem("TỔNG HỢP XUẤT KHÁC THEO NGÀY", "Report.png"));
        mBaoCaoXuatKhac.add(createReportMenuItem("TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN XUẤT", "Report.png"));
        mBaoCaoXuatKhac.add(createReportMenuItem("DANH SÁCH PHIẾU XUẤT KHO THEO NGÀY", "Report.png"));
        mBaoCaoKhoHang.add(mBaoCaoXuatKhac);

        // 8.5.3 BÁO CÁO NHẬP HÀNG
        JMenu mBaoCaoNhapHang = new JMenu("BÁO CÁO NHẬP HÀNG");
        mBaoCaoNhapHang.add(createReportMenuItem("DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP NHẬP THEO NGÀY", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP", "Report.png"));
        mBaoCaoNhapHang.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY", "Report.png"));
        mBaoCaoKhoHang.add(mBaoCaoNhapHang);

        // 8.5.4 BÁO CÁO CHUYỂN KHO
        JMenu mBaoCaoChuyenKho = new JMenu("BÁO CÁO CHUYỂN KHO");
        mBaoCaoChuyenKho.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN", "Report.png"));
        mBaoCaoChuyenKho.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN", "Report.png"));
        mBaoCaoChuyenKho.add(createReportMenuItem("TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY", "Report.png"));
        mBaoCaoChuyenKho.add(createReportMenuItem("DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT", "Report.png"));
        mBaoCaoChuyenKho.add(createReportMenuItem("DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP", "Report.png"));
        mBaoCaoChuyenKho.add(createReportMenuItem("DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY", "Report.png"));
        mBaoCaoKhoHang.add(mBaoCaoChuyenKho);

        // 8.5.5 BÁO CÁO THEO HSD
        JMenu mBaoCaoTheoHSD = new JMenu("BÁO CÁO THEO HSD");
        mBaoCaoTheoHSD.add(createReportMenuItem("BÁO CÁO THEO HẠN DÙNG", "document_valid.png"));
        mBaoCaoTheoHSD.add(createReportMenuItem("BÁO CÁO HÀNG ĐÃ HẾT HẠN", "document_valid.png"));
        mBaoCaoTheoHSD.add(createReportMenuItem("BÁO CÁO HÀNG TỒN KHO CÓ HẠN DÙNG", "document_valid.png"));
        mBaoCaoKhoHang.add(mBaoCaoTheoHSD);

        mBaoCaoKhoHang.add(createReportMenuItem("BÁO CÁO HÀNG TỒN KHO", "question-white.png"));
        mBaoCaoKhoHang.add(createReportMenuItem("BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN", "tick.png"));
        mBaoCaoKhoHang.add(createReportMenuItem("THẺ KHO", "report--pencil.png"));

        // 8.6 BÁO CÁO CÔNG NỢ
        JMenu mBaoCaoCongNo = new JMenu("BÁO CÁO CÔNG NỢ");
        mBaoCaoCongNo.add(createReportMenuItem("ĐỐI CHIẾU CÔNG NỢ NHÀ CUNG CẤP", "information-white.png"));
        mBaoCaoCongNo.add(createReportMenuItem("BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP", "Report.png"));
        mBaoCaoCongNo.add(createReportMenuItem("ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG", "Find.png"));
        mBaoCaoCongNo.add(createReportMenuItem("TỔNG HỢP CÔNG NỢ KHÁCH HÀNG", "warning.png"));
        mBaoCaoCongNo.add(createReportMenuItem("TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP", "navigation-000-frame.png"));
        mBaoCaoCongNo.add(createReportMenuItem("BÁO CÁO CÔNG NỢ KHÁCH HÀNG", "Report.png"));

        // 8.7 BÁO CÁO QUẢN TRỊ
        JMenu mBaoCaoQuanTri = new JMenu("BÁO CÁO QUẢN TRỊ");
        mBaoCaoQuanTri.add(createReportMenuItem("TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)", "1282529873_objects.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)", "Folder2.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ NHẬP)", "mail-send.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ VỐN)", "tsbTimeStamp.Image.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("BÁO CÁO KẾT QUẢ KINH DOANH", "Tree.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("CHI TIẾT HOẠT ĐỘNG TRONG NGÀY", "report--pencil.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT", "Statistic16.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("BẢNG TỔNG HỢP THỊ PHẦN", "tick.png"));
        mBaoCaoQuanTri.add(createReportMenuItem("PHÂN TÍCH TÌNH HÌNH BÁN HÀNG", "page_white_code.png"));

        // 8.8 BÁO CÁO BIỂU ĐỒ
        JMenu mBaoCaoBieuDo = new JMenu("BÁO CÁO BIỂU ĐỒ");
        mBaoCaoBieuDo.add(createReportMenuItem("BIỂU ĐỒ DOANH SỐ THEO NHÓM", "Report.png"));
        mBaoCaoBieuDo.add(createReportMenuItem("BIỂU ĐỒ DOANH THU NGÀY TRONG THÁNG", "Statistic16.png"));
        mBaoCaoBieuDo.add(createReportMenuItem("BIỂU ĐỒ DOANH THU THÁNG TRONG NĂM", "Report.png"));
        mBaoCaoBieuDo.add(createReportMenuItem("BIỂU ĐỒ THEO NHÂN VIÊN KINH DOANH", "ptImage.Image.png"));

        mBaoCao.add(mBaoCaoQuy);
        mBaoCao.add(mBaoCaoDanhMuc);
        mBaoCao.add(mBaoCaoBanHang);
        mBaoCao.add(mBaoCaoDatHang);
        mBaoCao.add(mBaoCaoKhoHang);
        mBaoCao.add(mBaoCaoCongNo);
        mBaoCao.add(mBaoCaoQuanTri);
        mBaoCao.add(mBaoCaoBieuDo);

        // Hide entire Báo cáo menu if Báo cáo function is not permitted
        if (!PermissionService.getInstance().isFunctionAllowed("Báo cáo")) {
            mBaoCao.setVisible(false);
        }

        // 9. TRỢ GIÚP
        JMenu mTroGiup = new JMenu("TRỢ GIÚP");
        mTroGiup.add(new JMenuItem("Đăng kí bản quyền"));
        mTroGiup.add(new JMenuItem("Thông tin phần mềm"));

        menuBar.add(mHeThong);
        menuBar.add(mHoatDong);
        menuBar.add(mKhoHang);
        menuBar.add(mQuy);
        menuBar.add(mCongNo);
        menuBar.add(mNhanSu);
        menuBar.add(mQuanTri);
        menuBar.add(mBaoCao);
        menuBar.add(mTroGiup);

        applyPermissionsToMenuBar(menuBar);

        return menuBar;
    }

    private JMenuItem createFunctionMenuItem(String title, String functionName, String iconName, KeyStroke accelerator, ActionListener listener) {
        JMenuItem mi = new JMenuItem(title);
        if (iconName != null && !iconName.isEmpty()) {
            ImageIcon icon = IconHelper.getIcon(iconName, 16, 16);
            if (icon != null) {
                mi.setIcon(icon);
            }
        }
        if (accelerator != null) {
            mi.setAccelerator(accelerator);
        }
        if (listener != null) {
            mi.addActionListener(listener);
        }

        String checkName = (functionName != null && !functionName.isEmpty()) ? functionName : title;
        boolean allowed = PermissionService.getInstance().isFunctionAllowed(checkName);
        mi.setVisible(allowed);
        mi.putClientProperty("permitted", allowed);
        return mi;
    }

    private JMenuItem createFunctionMenuItem(String title, String iconName, ActionListener listener) {
        return createFunctionMenuItem(title, title, iconName, null, listener);
    }

    private JMenuItem createFunctionMenuItem(String title, String iconName, KeyStroke accelerator, ActionListener listener) {
        return createFunctionMenuItem(title, title, iconName, accelerator, listener);
    }

    private void applyPermissionsToMenuBar(JMenuBar menuBar) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                cleanMenuAndEvaluateVisibility(menu);
            }
        }
    }

    private boolean cleanMenuAndEvaluateVisibility(JMenu menu) {
        int visibleItemCount = 0;
        Component[] components = menu.getPopupMenu().getComponents();
        boolean previousWasSeparator = true;

        for (Component c : components) {
            if (c instanceof JMenu) {
                JMenu subMenu = (JMenu) c;
                boolean subHasVisible = cleanMenuAndEvaluateVisibility(subMenu);
                subMenu.setVisible(subHasVisible);
                if (subHasVisible) {
                    visibleItemCount++;
                    previousWasSeparator = false;
                }
            } else if (c instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) c;
                if (item.isVisible()) {
                    visibleItemCount++;
                    previousWasSeparator = false;
                }
            } else if (c instanceof JPopupMenu.Separator || c instanceof JSeparator) {
                if (previousWasSeparator) {
                    c.setVisible(false);
                } else {
                    previousWasSeparator = true;
                }
            }
        }

        // Clean up trailing separator
        for (int i = components.length - 1; i >= 0; i--) {
            Component c = components[i];
            if (c.isVisible()) {
                if (c instanceof JPopupMenu.Separator || c instanceof JSeparator) {
                    c.setVisible(false);
                } else {
                    break;
                }
            }
        }

        boolean isMenuVisible = (visibleItemCount > 0);
        menu.setVisible(isMenuVisible);
        return isMenuVisible;
    }

    private ResponsiveToolBar createRibbonToolBar() {
        ResponsiveToolBar bar = new ResponsiveToolBar();

        JButton btnHoaDon = createRibbonToolButton("Hóa đơn", "Hóa đơn bán hàng", "btnNew.Image.png", e -> openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel()));
        JButton btnQLBanHang = createRibbonToolButton("Quản lý bán hàng", "Quản lý bán hàng", "btnOpen.Image.png", e -> openTab("Quản lý bán hàng", new QuanLyBanHangPanel()));
        JButton btnTKDoanhThu = createRibbonToolButton("Thống kê doanh thu", "Thống kê doanh thu", "Statistic16.png", e -> openTab("Thống kê doanh thu", new ThongKeDoanhThuPanel()));
        JButton btnTKMatHang = createRibbonToolButton("Thống kê mặt hàng bán", "Thống kê mặt hàng bán", "1282529873_objects.png", e -> openTab("Thống kê mặt hàng bán", new ThongKeMatHangBanPanel()));
        JButton btnChiTietHD = createRibbonToolButton("Chi tiết hoạt động", "Chi tiết hoạt động ngày", "Report.png", e -> openTab("Chi tiết hoạt động", new ChiTietHoatDongTrongNgayPanel()));
        JButton btnTongHopKQKD = createRibbonToolButton("Tổng hợp KQKD", "Tổng hợp kết quả kinh doanh", "Tree.png", e -> openTab("Tổng hợp KQKD", new BaoCaoKetQuaKinhDoanhPanel()));
        JButton btnTonKho = createRibbonToolButton("Tồn kho", "Tồn kho", "Folder2.png", e -> openTab("Tồn kho", new QuanLyTonKhoPanel()));
        JButton btnGhiChu = createRibbonToolButton("Ghi chú", null, "FilterEdit.png", e -> openQuickNoteDialog());
        JButton btnBaoCao = createRibbonToolButton("Báo cáo", "Báo cáo", "Preview.png", e -> openTab("Báo cáo", createPlaceholderPanel("Báo cáo")));
        JButton btnThoat = createRibbonToolButton("Thoát", null, "Cancelled.png", e -> onExit());
        JButton btnMoNhanh = createRibbonToolButton("Mở nhanh", null, "tsbQuickOpen.Image.png", e -> openQuickSearchDialog());

        bar.addToolButton(btnHoaDon);
        bar.addToolButton(btnQLBanHang);
        bar.addToolButton(btnTKDoanhThu);
        bar.addToolButton(btnTKMatHang);
        bar.addToolButton(btnChiTietHD);
        bar.addToolButton(btnTongHopKQKD);
        bar.addToolButton(btnTonKho);
        bar.addToolButton(btnGhiChu);
        bar.addToolButton(btnBaoCao);
        bar.addToolButton(btnThoat);
        bar.addToolButton(btnMoNhanh);

        return bar;
    }

    private JButton createRibbonToolButton(String text, String functionName, String iconName, ActionListener listener) {
        JButton btn = createToolButton(text, iconName);
        if (listener != null) {
            btn.addActionListener(listener);
        }
        boolean allowed = true;
        if (functionName != null && !functionName.isEmpty()) {
            allowed = PermissionService.getInstance().isFunctionAllowed(functionName);
        }
        btn.putClientProperty("permitted", allowed);
        btn.setVisible(allowed);
        return btn;
    }

    private JButton createToolButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(AppColors.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    public void openTab(String title, JPanel panel) {
        if (!PermissionService.getInstance().isActionAllowed(title)) {
            JOptionPane.showMessageDialog(this,
                    "Bạn không có quyền truy cập chức năng: " + title,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int index = mainTabbedPane.indexOfTab(title);
        if (index >= 0) {
            mainTabbedPane.setSelectedIndex(index);
        } else {
            mainTabbedPane.addTab(title, panel);
            int newIndex = mainTabbedPane.getTabCount() - 1;
            mainTabbedPane.setTabComponentAt(newIndex, new CloseableTabComponent(mainTabbedPane, panel, title));
            mainTabbedPane.setSelectedIndex(newIndex);
        }
    }

    private void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống không?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> new LoginForm(true).setVisible(true));
        }
    }

    private void onExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn thoát khỏi chương trình?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void openQuickNoteDialog() {
        new GhiChuNhanhDialog(this).setVisible(true);
    }

    private void openQuickSearchDialog() {
        QuickSearchDialog dialog = new QuickSearchDialog(this, this::handleQuickAction);
        dialog.setVisible(true);
    }

    private void openSystemConfiguration() {
        new CauHinhToanHeThongDialog(this).setVisible(true);
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            Component content = mainTabbedPane.getComponentAt(i);
            if (content instanceof HoaDonBanHangPanel) {
                ((HoaDonBanHangPanel) content).refreshSalesConfiguration();
            } else if (content instanceof QuanLyBanHangPanel) {
                ((QuanLyBanHangPanel) content).refreshSalesConfiguration();
            }
        }
    }

    private void handleQuickAction(String action) {
        if (action == null) return;
        if (!PermissionService.getInstance().isActionAllowed(action)) {
            JOptionPane.showMessageDialog(this,
                    "Bạn không có quyền truy cập chức năng: " + action,
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        switch (action) {
            case "GHI CHÚ":
            case "GHI CHÚ NHANH":
                openQuickNoteDialog();
                break;
            case "DANH MỤC NHÂN VIÊN":
                new NhanVienDialog(this).setVisible(true);
                break;
            case "DANH MỤC CA LÀM VIỆC":
            case "CA LÀM VIỆC":
                new CaLamViecDialog(this).setVisible(true);
                break;
            case "DANH MỤC LÝ DO THU CHI":
            case "LÝ DO THU CHI":
                new LyDoThuChiDialog(this).setVisible(true);
                break;
            case "DANH MỤC TÀI KHOẢN NGÂN HÀNG":
            case "TÀI KHOẢN NGÂN HÀNG":
                new TaiKhoanNganHangDialog(this).setVisible(true);
                break;
            case "DANH MỤC KHO HÀNG":
            case "KHO HÀNG":
                new KhoHangDialog(this).setVisible(true);
                break;
            case "DANH MỤC CỬA HÀNG":
            case "CỬA HÀNG":
                new CuaHangDialog(this).setVisible(true);
                break;
            case "CẤU HÌNH CƠ SỞ DỮ LIỆU":
            case "CƠ SỞ DỮ LIỆU - CẤU HÌNH":
                new DatabaseConfigDialog(this).setVisible(true);
                break;
            case "QUẢN LÝ NHẬP KHO":
            case "NHẬP KHO":
            case "NHẬP HÀNG VÀO KHO":
                openTab("Quản lý nhập kho", new QuanLyNhapKhoPanel());
                break;
            case "QUẢN LÝ XUẤT KHO":
            case "XUẤT KHO":
                openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel());
                break;
            case "BÁO GIÁ BÁN HÀNG":
            case "BÁO GIÁ KHÁCH HÀNG":
                openTab("Báo giá bán hàng", new BaoGiaBanHangPanel());
                break;
            case "ĐẶT HÀNG NHÀ CUNG CẤP":
            case "ĐẶT HÀNG NCC":
                openTab("Đặt hàng nhà cung cấp", new DatHangNhaCungCapPanel());
                break;
            case "CHUYỂN KHO":
            case "QUẢN LÝ CHUYỂN KHO":
                openTab("Quản lý chuyển kho", new QuanLyChuyenKhoPanel());
                break;
            case "KIỂM KÊ":
            case "KIỂM KÊ KHO":
            case "QUẢN LÝ KIỂM KÊ":
                openTab("Quản lý kiểm kê", new QuanLyKiemKePanel());
                break;
            case "TÍNH LẠI GIÁ VỐN":
            case "TÍNH GIÁ VỐN":
                new TinhLaiGiaVonDialog(this).setVisible(true);
                break;
            case "TỒN KHO":
            case "QUẢN LÝ TỒN KHO":
                openTab("Tồn kho", new QuanLyTonKhoPanel());
                break;
            case "TỒN NHIỀU KHO":
            case "QUẢN LÝ TỒN NHIỀU KHO":
                openTab("Tồn nhiều kho", new QuanLyTonNhieuKhoPanel());
                break;
            case "KHÁCH ĐẶT HÀNG":
            case "ĐẶT HÀNG":
                openTab("Khách đặt hàng", new KhachDatHangPanel());
                break;
            case "HÓA ĐƠN BÁN HÀNG":
            case "HÓA ĐƠN":
            case "BÁN HÀNG":
                openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel());
                break;
            case "DANH MỤC KHÁCH HÀNG":
                openTab("Danh mục khách hàng", new DanhMucKhachHangPanel());
                break;
            case "CÔNG NỢ KHÁCH HÀNG":
            case "CÔNG NỢ KHÁCH":
                openTab("Công nợ khách hàng", new QuanLyCongNoKhachHangPanel());
                break;
            case "DANH MỤC NHÀ CUNG CẤP":
            case "NHÀ CUNG CẤP":
                openTab("Danh mục nhà cung cấp", new DanhMucNhaCungCapPanel());
                break;
            case "CÔNG NỢ NHÀ CUNG CẤP":
            case "CÔNG NỢ NCC":
                openTab("Công nợ nhà cung cấp", new QuanLyCongNoNhaCungCapPanel());
                break;
            case "DANH MỤC PHIẾU THU":
            case "PHIẾU THU":
                openTab("Danh mục phiếu thu", new DanhMucPhieuThuPanel());
                break;
            case "DANH MỤC PHIẾU CHI":
            case "PHIẾU CHI":
                openTab("Danh mục phiếu chi", new DanhMucPhieuChiPanel());
                break;
            case "TỒN QUỸ":
            case "SỔ QUỸ":
                openTab("Tồn quỹ", new TonQuyPanel());
                break;
            case "TẠM ỨNG LƯƠNG":
            case "TẠM ỨNG":
                openTab("Tạm ứng lương", new TamUngLuongPanel());
                break;
            case "THƯỞNG PHẠT":
                openTab("Thưởng phạt", new ThuongPhatPanel());
                break;
            case "CHẤM CÔNG":
                openTab("Chấm công", new ChamCongPanel());
                break;
            case "THỐNG KÊ DOANH THU":
                openTab("Thống kê doanh thu", new ThongKeDoanhThuPanel());
                break;
            case "THỐNG KÊ MẶT HÀNG BÁN":
            case "THỐNG KÊ MẶT HÀNG":
                openTab("Thống kê mặt hàng bán", new ThongKeMatHangBanPanel());
                break;
            case "CHI TIẾT HOẠT ĐỘNG NGÀY":
            case "CHI TIẾT HOẠT ĐỘNG":
            case "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY":
                openTab("Chi tiết hoạt động", new ChiTietHoatDongTrongNgayPanel());
                break;
            case "TỔNG HỢP KẾT QUẢ KINH DOANH":
            case "TỔNG HỢP KQKD":
            case "KẾT QUẢ KINH DOANH":
            case "BÁO CÁO KẾT QUẢ KINH DOANH":
                openTab("Tổng hợp KQKD", new BaoCaoKetQuaKinhDoanhPanel());
                break;
            case "GỬI TIN NHẮN KHÁCH HÀNG":
                openTab("Gửi tin nhắn tới khách hàng", createPlaceholderPanel("Gửi tin nhắn tới khách hàng"));
                break;
            case "CẤU HÌNH TOÀN HỆ THỐNG":
            case "CẤU HÌNH HỆ THỐNG":
                openSystemConfiguration();
                break;
            case "CÔNG NỢ KHÁCH HÀNG BAN ĐẦU":
                new CongNoKhachHangBanDauDialog(this).setVisible(true);
                break;
            case "CÔNG NỢ NHÀ CUNG CẤP BAN ĐẦU":
                new CongNoNhaCungCapBanDauDialog(this).setVisible(true);
                break;
            case "TỒN KHO BAN ĐẦU":
                ChonKhoBanDauDialog selectDlg = new ChonKhoBanDauDialog(this);
                selectDlg.setVisible(true);
                if (selectDlg.isConfirmed() && selectDlg.getSelectedKho() != null) {
                    new TonKhoBanDauDialog(this, selectDlg.getSelectedKho()).setVisible(true);
                }
                break;
            case "ĐỔI MẬT KHẨU":
                new DoiMatKhauDialog(this, currentUser).setVisible(true);
                break;
            case "ĐĂNG XUẤT":
                onLogout();
                break;
            case "THOÁT":
                onExit();
                break;
            default:
                handleReportMenuClick(action);
                break;
        }
    }

    private JMenuItem createReportMenuItem(String title, String iconName) {
        JMenuItem mi = new JMenuItem(title);
        if (iconName != null && !iconName.isEmpty()) {
            ImageIcon icon = IconHelper.getIcon(iconName, 16, 16);
            if (icon != null) {
                mi.setIcon(icon);
            }
        }
        mi.addActionListener(e -> handleReportMenuClick(title));
        return mi;
    }

    private void handleReportMenuClick(String title) {
        if (title == null) return;
        switch (title.trim().toUpperCase()) {
            case "DANH SÁCH PHIẾU THU THEO NGÀY":
            case "BÁO CÁO PHIẾU THU THEO NGÀY":
            case "DANH SÁCH PHIẾU THU THE...":
                openTab("DANH SÁCH PHIẾU THU THE...", new BaoCaoPhieuThuTheoNgayPanel());
                break;
            case "TỔNG HỢP THU CHI THEO NGÀY":
                openTab("TỔNG HỢP THU CHI THEO NGÀY", new TongHopThuChiTheoNgayPanel());
                break;
            case "TỔNG HỢP THU CHI THEO LÝ DO":
            case "TỔNG HỢP THU CHI THEO LÝ DO...":
                openTab("TỔNG HỢP THU CHI THEO LÝ DO", new TongHopThuChiTheoLyDoPanel());
                break;
            case "BÁO CÁO TỒN QUỸ":
            case "TỒN QUỸ":
            case "SỔ QUỸ":
                openTab("BÁO CÁO TỒN QUỸ", new BaoCaoTonQuyPanel());
                break;
            case "KHÁCH HÀNG THÂN THIẾT":
            case "BÁO CÁO KHÁCH HÀNG THÂN THIẾT":
                openTab("Khách hàng thân thiết", new BaoCaoKhachHangThanThietPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO GIỜ":
            case "BÁN HÀNG THEO GIỜ":
                openTab("Báo cáo bán hàng theo giờ", new BaoCaoBanHangTheoGioPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC":
            case "BÁN HÀNG THEO KÍCH THƯỚC":
                openTab("Báo cáo bán hàng theo kích thước", new BaoCaoBanHangTheoKichThuocPanel());
                break;
            case "BÁO CÁO TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG":
            case "TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG":
                openTab("Tổng hợp giá trị bán theo tháng", new BaoCaoTongHopGiaTriBanTheoThangPanel());
                break;
            case "BÁO CÁO TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG":
            case "TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG":
                openTab("Tổng hợp số lượng bán theo tháng", new BaoCaoTongHopSoLuongBanTheoThangPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO NGÀY":
                openTab("Báo cáo bán hàng theo ngày", new BaoCaoBanHangTheoNgayPanel());
                break;
            case "BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY":
                openTab("Báo cáo chi tiết bán hàng theo ngày", new BaoCaoChiTietBanHangTheoNgayPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG":
                openTab("Báo cáo bán hàng theo khách hàng", new BaoCaoBanHangTheoKhachHangPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH":
            case "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN":
                openTab("Báo cáo bán hàng theo nhân viên kinh doanh", new BaoCaoBanHangTheoNhanVienPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG BÁN THEO KHÁCH HÀNG":
                openTab("Tổng hợp mặt hàng bán theo khách hàng", new TongHopMatHangBanTheoKhachHangPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NGÀY":
                openTab("Tổng hợp mặt hàng bán theo ngày", new TongHopMatHangBanTheoNgayPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN KINH DOANH":
            case "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN":
                openTab("Tổng hợp mặt hàng bán theo nhân viên", new TongHopMatHangBanTheoNhanVienPanel());
                break;
            case "TỔNG HỢP BÁN HÀNG THEO NGÀY":
                openTab("Tổng hợp bán hàng theo ngày", new TongHopBanHangTheoNgayPanel());
                break;
            case "TỔNG HỢP BÁN THEO KHÁCH HÀNG":
                openTab("Tổng hợp bán theo khách hàng", new TongHopBanTheoKhachHangPanel());
                break;
            case "TỔNG HỢP BÁN THEO NHÂN VIÊN BÁN":
            case "TỔNG HỢP BÁN THEO NHÂN VIÊN":
                openTab("Tổng hợp bán theo nhân viên bán", new TongHopBanTheoNhanVienBanPanel());
                break;
            case "TỔNG HỢP BÁN THEO THU NGÂN":
                openTab("Tổng hợp bán theo thu ngân", new TongHopBanTheoThuNganPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG BÁN THEO THU NGÂN":
                openTab("Tổng hợp mặt hàng bán theo thu ngân", new TongHopMatHangBanTheoThuNganPanel());
                break;
            case "BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN":
            case "CHI TIẾT BÁN HÀNG THEO THU NGÂN":
                openTab("Báo cáo chi tiết bán hàng theo thu ngân", new BaoCaoChiTietBanHangTheoThuNganPanel());
                break;
            case "BÁO CÁO BÁN HÀNG THEO THU NGÂN":
            case "BÁO CÁO BÁN HÀNGTHEO THU NGÂN":
                openTab("Báo cáo bán hàng theo thu ngân", new BaoCaoBanHangTheoThuNganPanel());
                break;
            case "TỔNG HỢP TIỀN VẬN CHUYỂN THEO NHÂN VIÊN":
            case "TIỀN VẬN CHUYỂN THEO NHÂN VIÊN":
                openTab("Tổng hợp tiền vận chuyển theo nhân viên", new TongHopTienVanChuyenTheoNhanVienPanel());
                break;
            case "CHI TIẾT BÁN HÀNG THEO ĐỐI TÁC":
            case "CHI TIẾT HÀNG BÁN THEO ĐỐI TÁC":
            case "BÁN HÀNG THEO ĐỐI TÁC":
                openTab("Chi tiết bán hàng theo đối tác", new ChiTietHangBanTheoDoiTacPanel());
                break;
            case "TỔNG HỢP HOA HỒNG THEO NVKD":
            case "TỔNG HỢP HOA HỒNG THEO NHÂN VIÊN":
            case "HOA HỒNG THEO NVKD":
                openTab("Tổng hợp hoa hồng theo NVKD", new TongHopHoaHongTheoNVKDPanel());
                break;
            case "CHI TIẾT BÁN HÀNG THEO HÓA ĐƠN":
            case "BÁN HÀNG THEO HÓA ĐƠN":
                openTab("Chi tiết bán hàng theo hóa đơn", new ChiTietBanHangTheoHoaDonPanel());
                break;
            case "BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI":
            case "CHI TIẾT HÀNG KHUYẾN MẠI":
            case "HÀNG KHUYẾN MẠI":
                openTab("Báo cáo chi tiết hàng khuyến mại", new BaoCaoChiTietHangKhuyenMaiPanel());
                break;
            case "DANH SÁCH ĐỢT KHUYẾN MẠI":
            case "BÁO CÁO ĐỢT KHUYẾN MẠI":
                openTab("DANH SÁCH ĐỢT KHUYẾN MẠI", new BaoCaoDotKhuyenMaiPanel());
                break;
            case "DANH MỤC ĐỢT KHUYẾN MÃI":
            case "ĐỢT KHUYẾN MÃI":
                openTab("Danh mục đợt khuyến mại", new DanhMucDotKhuyenMaiPanel());
                break;
            case "KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT":
            case "SINH NHẬT KHÁCH HÀNG":
                openTab("KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT", new BaoCaoKhachHangSinhNhatPanel());
                break;
            case "DANH SÁCH ĐẶT HÀNG THEO NGÀY":
            case "BÁO CÁO ĐẶT HÀNG THEO NGÀY":
            case "ĐẶT HÀNG THEO NGÀY":
                openTab("Danh sách đặt hàng theo ngày", new BaoCaoDanhSachDatHangTheoNgayPanel());
                break;
            case "DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "BÁO CÁO ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "ĐẶT HÀNG THEO KHÁCH HÀNG":
                openTab("Danh sách đặt hàng theo khách hàng", new BaoCaoDanhSachDatHangTheoKhachHangPanel());
                break;
            case "ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG":
            case "BÁO CÁO CÔNG NỢ KHÁCH HÀNG":
                openTab("Báo cáo công nợ khách hàng", new BaoCaoCongNoKhachHangPanel());
                break;
            case "TỔNG HỢP CÔNG NỢ KHÁCH HÀNG":
                openTab("Tổng hợp công nợ khách hàng", new TongHopCongNoKhachHangPanel());
                break;
            case "CÔNG NỢ KHÁCH HÀNG":
            case "CÔNG NỢ KHÁCH":
            case "QUẢN LÝ CÔNG NỢ KHÁCH HÀNG":
                openTab("Công nợ khách hàng", new QuanLyCongNoKhachHangPanel());
                break;
            case "ĐỐI CHIẾU CÔNG NỢ NHÀ CUNG CẤP":
            case "BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP":
                openTab("Báo cáo công nợ NCC", new BaoCaoCongNoNhaCungCapPanel());
                break;
            case "TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP":
                openTab("Tổng hợp công nợ NCC", new TongHopCongNoNhaCungCapPanel());
                break;
            case "CÔNG NỢ NHÀ CUNG CẤP":
            case "CÔNG NỢ NCC":
            case "QUẢN LÝ CÔNG NỢ NHÀ CUNG CẤP":
                openTab("Công nợ nhà cung cấp", new QuanLyCongNoNhaCungCapPanel());
                break;
            case "BÁO CÁO TỒN KHO THEO KÍCH THƯỚC":
            case "TỒN KHO THEO KÍCH THƯỚC":
                openTab("Báo cáo tồn kho theo kích thước", new BaoCaoTonKhoTheoKichThuocPanel());
                break;
            case "BÁO CÁO HÀNG THEO HẠN DÙNG":
            case "HÀNG THEO HẠN DÙNG":
                openTab("Báo cáo hàng theo hạn dùng", new BaoCaoHangTheoHanDungPanel());
                break;
            case "BÁO CÁO HÀNG ĐÃ HẾT HẠN":
            case "HÀNG ĐÃ HẾT HẠN":
                openTab("Báo cáo hàng đã hết hạn", new BaoCaoHangDaHetHanPanel());
                break;
            case "BÁO CÁO HÀNG TỒN KHO CÓ HẠN DÙNG":
            case "HÀNG TỒN KHO CÓ HẠN DÙNG":
                openTab("Báo cáo hàng tồn kho có hạn dùng", new BaoCaoHangTonKhoHanDungPanel());
                break;
            case "BÁO CÁO HÀNG TỒN KHO":
            case "HÀNG TỒN KHO":
                openTab("Báo cáo hàng tồn kho", new BaoCaoHangTonKhoPanel());
                break;
            case "BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN":
            case "TỔNG HỢP XUẤT NHẬP TỒN":
                openTab("Báo cáo tổng hợp xuất nhập tồn", new BaoCaoTongHopXuatNhapTonPanel());
                break;
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)":
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG GIÁ VỐN":
            case "LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)":
            case "LÃI GỘP MẶT HÀNG GIÁ VỐN":
                openTab("Lãi gộp theo mặt hàng (giá vốn)", new TongHopLaiGopTheoMatHangGiaVonPanel());
                break;
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)":
            case "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG GIÁ NHẬP":
            case "LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)":
            case "LÃI GỘP MẶT HÀNG GIÁ NHẬP":
                openTab("Lãi gộp theo mặt hàng (giá nhập)", new TongHopLaiGopTheoMatHangGiaNhapPanel());
                break;
            case "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ VỐN)":
            case "CHI TIẾT LÃI THEO HÓA ĐƠN GIÁ VỐN":
            case "CHI TIẾT LÃI HÓA ĐƠN (GIÁ VỐN)":
            case "CHI TIẾT LÃI HÓA ĐƠN GIÁ VỐN":
                openTab("Chi tiết lãi theo hóa đơn (giá vốn)", new ChiTietLaiTheoHoaDonGiaVonPanel());
                break;
            case "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ NHẬP)":
            case "CHI TIẾT LÃI THEO HÓA ĐƠN GIÁ NHẬP":
            case "CHI TIẾT LÃI HÓA ĐƠN (GIÁ NHẬP)":
            case "CHI TIẾT LÃI HÓA ĐƠN GIÁ NHẬP":
                openTab("Chi tiết lãi theo hóa đơn (giá nhập)", new ChiTietLaiTheoHoaDonGiaNhapPanel());
                break;
            case "BÁO CÁO KẾT QUẢ KINH DOANH":
            case "KẾT QUẢ KINH DOANH":
            case "TỔNG HỢP KẾT QUẢ KINH DOANH":
            case "TỔNG HỢP KQKD":
                openTab("Tổng hợp KQKD", new BaoCaoKetQuaKinhDoanhPanel());
                break;
            case "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY":
            case "HOẠT ĐỘNG TRONG NGÀY":
                openTab("Chi tiết hoạt động trong ngày", new ChiTietHoatDongTrongNgayPanel());
                break;
            case "BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT":
            case "20 MẶT HÀNG BÁN CHẠY NHẤT":
            case "TOP 20 MẶT HÀNG BÁN CHẠY":
                openTab("Top 20 mặt hàng bán chạy nhất", new BaoCao20MatHangBanChayNhatPanel());
                break;
            case "BẢNG TỔNG HỢP THỊ PHẦN":
            case "TỔNG HỢP THỊ PHẦN":
                openTab("Bảng tổng hợp thị phần", new BangTongHopThiPhanPanel());
                break;
            case "BÁO CÁO PHÂN TÍCH TÌNH HÌNH BÁN HÀNG":
            case "PHÂN TÍCH TÌNH HÌNH BÁN HÀNG":
                openTab("Phân tích tình hình bán hàng", new BaoCaoPhanTichTinhHinhBanHangPanel());
                break;
            case "BIỂU ĐỒ DOANH SỐ THEO NHÓM":
            case "BIỂU ĐỒ DOANH SỐ THEO NHÓM HÀNG HÓA":
            case "DOANH SỐ THEO NHÓM":
                openTab("Biểu đồ doanh số theo nhóm", new BieuDoDoanhSoTheoNhomPanel());
                break;
            case "BIỂU ĐỒ DOANH THU NGÀY TRONG THÁNG":
            case "DOANH THU NGÀY TRONG THÁNG":
                openTab("Biểu đồ doanh thu ngày trong tháng", new BieuDoDoanhThuNgayTrongThangPanel());
                break;
            case "BIỂU ĐỒ DOANH THU THÁNG TRONG NĂM":
            case "DOANH THU THÁNG TRONG NĂM":
                openTab("Biểu đồ doanh thu tháng trong năm", new BieuDoDoanhThuThangTrongNamPanel());
                break;
            case "BIỂU ĐỒ THEO NHÂN VIÊN KINH DOANH":
            case "BIỂU ĐỒ DOANH SỐ THEO NHÂN VIÊN KINH DOANH":
            case "DOANH SỐ THEO NHÂN VIÊN KINH DOANH":
                openTab("Biểu đồ theo NVKD", new BieuDoTheoNhanVienKinhDoanhPanel());
                break;
            case "BÁO CÁO NHẬP XUẤT TỒN THEO KHO":
            case "BÁO CÁO TỒN KHO THEO MẶT HÀNG":
            case "TỒN KHO":
            case "QUẢN LÝ TỒN KHO":
                openTab("Tồn kho", new QuanLyTonKhoPanel());
                break;
            case "TỒN NHIỀU KHO":
            case "QUẢN LÝ TỒN NHIỀU KHO":
                openTab("Tồn nhiều kho", new QuanLyTonNhieuKhoPanel());
                break;
            case "DANH SÁCH MẶT HÀNG THEO NHÓM":
                openTab("DANH SÁCH MẶT HÀNG THEO NHÓM", new BaoCaoMatHangTheoNhomPanel());
                break;
            case "DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT":
                openTab("DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT", new BaoCaoMatHangTheoHangSanXuatPanel());
                break;
            case "DANH MỤC MẶT HÀNG":
                openTab("Danh mục mặt hàng", new DanhMucMatHangPanel());
                break;
            case "DANH SÁCH KHÁCH HÀNG THEO NHÓM":
                openTab("DANH SÁCH KHÁCH HÀNG THEO NHÓM", new BaoCaoKhachHangTheoNhomPanel());
                break;
            case "DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN":
                openTab("DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN", new BaoCaoKhachHangTheoNhanVienPanel());
                break;
            case "DANH MỤC KHÁCH HÀNG":
                openTab("Danh mục khách hàng", new DanhMucKhachHangPanel());
                break;
            case "DANH SÁCH NHÀ CUNG CẤP THEO NHÓM":
                openTab("DANH SÁCH NHÀ CUNG CẤP THEO NHÓM", new BaoCaoNhaCungCapTheoNhomPanel());
                break;
            case "DANH MỤC NHÀ CUNG CẤP":
                openTab("Danh mục nhà cung cấp", new DanhMucNhaCungCapPanel());
                break;
            case "DANH SÁCH PHIẾU THU THEO LÝ DO THU CHI":
            case "BÁO CÁO PHIẾU THU THEO LÝ DO THU CHI":
                openTab("DANH SÁCH PHIẾU THU THE...", new BaoCaoPhieuThuTheoLyDoPanel());
                break;
            case "DANH MỤC PHIẾU THU":
                openTab("Danh mục phiếu thu", new DanhMucPhieuThuPanel());
                break;

            case "DANH SÁCH PHIẾU CHI THEO NGÀY":
            case "BÁO CÁO PHIẾU CHI THEO NGÀY":
            case "DANH SÁCH PHIẾU CHI THE...":
                openTab("DANH SÁCH PHIẾU CHI THE...", new BaoCaoPhieuChiTheoNgayPanel());
                break;
            case "DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI":
            case "BÁO CÁO PHIẾU CHI THEO LÝ DO THU CHI":
                openTab("DANH SÁCH PHIẾU CHI THE...", new BaoCaoPhieuChiTheoLyDoPanel());
                break;
            case "DANH MỤC PHIẾU CHI":
                openTab("Danh mục phiếu chi", new DanhMucPhieuChiPanel());
                break;
            case "TỔNG HỢP ĐẶT HÀNG THEO NGÀY":
                openTab("Tổng hợp đặt hàng theo ngày", new TongHopDatHangTheoNgayPanel());
                break;
            case "TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG":
                openTab("Tổng hợp đặt hàng theo khách hàng", new TongHopDatHangTheoKhachHangPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG ĐẶT THEO KHÁCH HÀNG":
            case "MẶT HÀNG ĐẶT THEO KHÁCH HÀNG":
                openTab("Tổng hợp mặt hàng đặt theo khách hàng", new TongHopMatHangDatTheoKhachHangPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG ĐẶT THEO NGÀY":
            case "MẶT HÀNG ĐẶT THEO NGÀY":
                openTab("Tổng hợp mặt hàng đặt theo ngày", new TongHopMatHangDatTheoNgayPanel());
                break;
            case "KHÁCH ĐẶT HÀNG":
            case "ĐẶT HÀNG":
                openTab("Khách đặt hàng", new KhachDatHangPanel());
                break;
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU NHẬP THEO NGÀY":
                openTab("Danh sách phiếu nhập kho theo ngày", new BaoCaoDanhSachPhieuNhapTheoNgayPanel());
                break;
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP":
            case "DANH SÁCH PHIẾU NHẬP THEO NHÀ CUNG CẤP":
                openTab("Danh sách phiếu nhập kho theo nhà cung cấp", new BaoCaoDanhSachPhieuNhapTheoNhaCungCapPanel());
                break;
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP":
            case "DANH SÁCH PHIẾU NHẬP THEO NHÂN VIÊN":
                openTab("Danh sách phiếu nhập kho theo nhân viên", new BaoCaoDanhSachPhieuNhapTheoNhanVienPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NHÀ CUNG CẤP":
                openTab("Tổng hợp mặt hàng phiếu nhập theo NCC", new TongHopMatHangNhapTheoNhaCungCapPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NHÂN VIÊN":
                openTab("Tổng hợp mặt hàng phiếu nhập theo NV", new TongHopMatHangNhapTheoNhanVienPanel());
                break;
            case "TỔNG HỢP NHẬP THEO NGÀY":
                openTab("Tổng hợp nhập theo ngày", new TongHopNhapTheoNgayPanel());
                break;
            case "TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP":
                openTab("Tổng hợp nhập theo nhà cung cấp", new TongHopNhapTheoNhaCungCapPanel());
                break;
            case "TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP NHẬP THEO NHÂN VIÊN":
                openTab("Tổng hợp nhập theo nhân viên", new TongHopNhapTheoNhanVienPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG NHẬP THEO NGÀY":
                openTab("Tổng hợp mặt hàng nhập kho theo ngày", new TongHopMatHangNhapTheoNgayPanel());
                break;
            case "BÁO CÁO TỔNG HỢP NHẬP KHO":
            case "QUẢN LÝ NHẬP KHO":
                openTab("Quản lý nhập kho", new QuanLyNhapKhoPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NGÀY":
                openTab("Tổng hợp mặt hàng xuất theo ngày", new TongHopMatHangXuatTheoNgayPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN":
                openTab("Tổng hợp mặt hàng xuất theo nhân viên", new TongHopMatHangXuatTheoNhanVienPanel());
                break;
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NHÂN VIÊN XUẤT":
            case "DANH SÁCH PHIẾU XUẤT THEO NHÂN VIÊN":
                openTab("Danh sách phiếu xuất kho theo nhân viên", new BaoCaoDanhSachPhieuXuatTheoNhanVienPanel());
                break;
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU XUẤT THEO NGÀY":
                openTab("Danh sách phiếu xuất kho theo ngày", new BaoCaoDanhSachPhieuXuatTheoNgayPanel());
                break;
            case "TỔNG HỢP XUẤT KHÁC THEO NGÀY":
                openTab("Tổng hợp xuất khác theo ngày", new TongHopXuatKhacTheoNgayPanel());
                break;
            case "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN":
                openTab("Tổng hợp xuất khác theo nhân viên", new TongHopXuatKhacTheoNhanVienPanel());
                break;
            case "BÁO CÁO TỔNG HỢP XUẤT KHO":
            case "QUẢN LÝ XUẤT KHO":
                openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel());
                break;
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NGÀY":
                openTab("Danh sách phiếu chuyển kho theo ngày", new DanhSachPhieuChuyenKhoTheoNgayPanel());
                break;
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NHÂN VIÊN XUẤT":
                openTab("Danh sách phiếu chuyển kho theo NV xuất", new DanhSachPhieuChuyenKhoTheoNhanVienXuatPanel());
                break;
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP":
            case "DANH SÁCH PHIẾU CHUYỂN THEO NHÂN VIÊN NHẬP":
                openTab("Danh sách phiếu chuyển kho theo NV nhập", new DanhSachPhieuChuyenKhoTheoNhanVienNhanPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NHÂN VIÊN NHẬN":
                openTab("Tổng hợp mặt hàng chuyển kho theo NV nhận", new TongHopMatHangChuyenKhoTheoNhanVienNhanPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NHÂN VIÊN CHUYỂN":
                openTab("Tổng hợp mặt hàng chuyển kho theo NV chuyển", new TongHopMatHangChuyenKhoTheoNhanVienChuyenPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN THEO NGÀY":
                openTab("Tổng hợp mặt hàng chuyển kho theo ngày", new TongHopMatHangChuyenKhoTheoNgayPanel());
                break;
            case "BÁO CÁO TỔNG HỢP CHUYỂN KHO":
            case "QUẢN LÝ CHUYỂN KHO":
                openTab("Quản lý chuyển kho", new QuanLyChuyenKhoPanel());
                break;
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY":
            case "BÁO CÁO KIỂM KÊ THEO NGÀY":
                openTab("Danh sách phiếu kiểm kê theo ngày", new BaoCaoDanhSachPhieuKiemKeTheoNgayPanel());
                break;
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "BÁO CÁO KIỂM KÊ THEO NHÂN VIÊN":
                openTab("Danh sách phiếu kiểm kê theo nhân viên", new BaoCaoDanhSachPhieuKiemKeTheoNhanVienPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG KIỂM KÊ THEO NGÀY":
                openTab("Tổng hợp mặt hàng phiếu kiểm kê theo ngày", new TongHopMatHangKiemKeTheoNgayPanel());
                break;
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "TỔNG HỢP MẶT HÀNG KIỂM KÊ THEO NHÂN VIÊN":
                openTab("Tổng hợp mặt hàng phiếu kiểm kê theo nhân viên", new TongHopMatHangKiemKeTheoNhanVienPanel());
                break;
            case "BÁO CÁO TỔNG HỢP KIỂM KÊ KHO":
            case "QUẢN LÝ KIỂM KÊ":
            case "PHIẾU KIỂM KÊ":
                openTab("Quản lý kiểm kê", new QuanLyKiemKePanel());
                break;
            default:
                openTab(title, createPlaceholderPanel(title));
                break;
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("<html><center><h3>" + title + "</h3><p style='color:gray;'>Chức năng đang mở trong phiên làm việc.</p></center></html>");
        panel.add(label);
        return panel;
    }

    private void setWindowIcon() {
        URL logoUrl = getClass().getResource("/logo.png");
        if (logoUrl != null) {
            setIconImage(new ImageIcon(logoUrl).getImage());
        }
    }

    // ==================== RESPONSIVE TOOLBAR WITH OVERFLOW DROPDOWN ====================
    public static class ResponsiveToolBar extends JPanel {
        private final List<JButton> buttons = new ArrayList<>();
        private final JButton btnOverflow;
        private final JPanel buttonPanel;

        public ResponsiveToolBar() {
            setLayout(new BorderLayout());
            setBackground(AppColors.RIBBON_BG);
            setBorder(new MatteBorder(0, 0, 1, 0, AppColors.BORDER_BLUE));

            buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
            buttonPanel.setOpaque(false);
            add(buttonPanel, BorderLayout.CENTER);

            btnOverflow = new JButton();
            btnOverflow.setIcon(ARROW_DOWN_ICON);
            btnOverflow.setPreferredSize(new Dimension(20, 24));
            btnOverflow.setMargin(new Insets(0, 0, 0, 0));
            btnOverflow.setFocusPainted(false);
            btnOverflow.setBackground(new Color(230, 240, 252));
            btnOverflow.setToolTipText("Xem thêm các chức năng bị ẩn do thu nhỏ cửa sổ");
            btnOverflow.setBorder(new MatteBorder(1, 1, 1, 1, new Color(180, 205, 235)));
            btnOverflow.addActionListener(e -> showOverflowMenu());

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
            rightPanel.setOpaque(false);
            rightPanel.add(btnOverflow);
            add(rightPanel, BorderLayout.EAST);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateOverflow();
                }
            });
        }

        public void addToolButton(JButton btn) {
            buttons.add(btn);
            buttonPanel.add(btn);
        }

        public void updateOverflow() {
            int availableWidth = getWidth() - 45;
            if (availableWidth <= 0) return;

            int currentWidth = 0;
            for (JButton btn : buttons) {
                if (Boolean.FALSE.equals(btn.getClientProperty("permitted"))) {
                    btn.setVisible(false);
                    continue;
                }
                int btnWidth = btn.getPreferredSize().width + 8;
                if (currentWidth + btnWidth <= availableWidth) {
                    btn.setVisible(true);
                    currentWidth += btnWidth;
                } else {
                    btn.setVisible(false);
                }
            }
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }

        private void showOverflowMenu() {
            JPopupMenu popup = new JPopupMenu();
            popup.setBackground(Color.WHITE);
            popup.setBorder(new LineBorder(new Color(170, 195, 225), 1));

            boolean anyHidden = false;
            for (JButton btn : buttons) {
                if (Boolean.FALSE.equals(btn.getClientProperty("permitted"))) {
                    continue;
                }
                if (!btn.isVisible()) {
                    JMenuItem item = new JMenuItem(btn.getText(), btn.getIcon());
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    item.setBackground(Color.WHITE);
                    for (ActionListener al : btn.getActionListeners()) {
                        item.addActionListener(al);
                    }
                    popup.add(item);
                    anyHidden = true;
                }
            }

            if (!anyHidden) {
                for (JButton btn : buttons) {
                    if (Boolean.FALSE.equals(btn.getClientProperty("permitted"))) {
                        continue;
                    }
                    JMenuItem item = new JMenuItem(btn.getText(), btn.getIcon());
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    item.setBackground(Color.WHITE);
                    for (ActionListener al : btn.getActionListeners()) {
                        item.addActionListener(al);
                    }
                    popup.add(item);
                }
            }

            if (popup.getComponentCount() > 0) {
                popup.show(btnOverflow, btnOverflow.getWidth() - popup.getPreferredSize().width, btnOverflow.getHeight());
            }
        }
    }

    // ==================== CUSTOM TABBED PANE UI ====================
    public static class CustomModernTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(4, 14, 4, 14);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(2, 4, 0, 122); // Chừa chỗ cho cụm điều khiển tab bên phải
        }

        @Override
        protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isSelected) {
                // Tab được chọn: Gradient Vàng Kem đặc trưng (giống ảnh 1 & 2)
                GradientPaint gp = new GradientPaint(x, y, new Color(255, 245, 195), x, y + h, new Color(255, 215, 125));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, w, h + 2, 4, 4);

                g2.setColor(new Color(230, 160, 20));
                g2.drawRoundRect(x, y, w - 1, h + 2, 4, 4);
                g2.setColor(new Color(255, 185, 45));
                g2.drawLine(x + 1, y, x + w - 2, y);
            } else {
                // Tab không được chọn: Xanh xám nhạt
                GradientPaint gp = new GradientPaint(x, y, new Color(245, 248, 253), x, y + h, new Color(225, 235, 248));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y + 2, w, h - 2, 4, 4);

                g2.setColor(new Color(190, 205, 225));
                g2.drawRoundRect(x, y + 2, w - 1, h - 2, 4, 4);
            }
            g2.dispose();
        }

        @Override
        protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            if (isSelected) {
                g2.setFont(font.deriveFont(Font.BOLD, 12f));
                g2.setColor(new Color(20, 20, 20));
            } else {
                g2.setFont(font.deriveFont(Font.PLAIN, 12f));
                g2.setColor(new Color(60, 70, 85));
            }
            g2.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            g2.dispose();
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(185, 205, 230));
            g2.drawRect(0, 0, tabPane.getWidth() - 1, tabPane.getHeight() - 1);
            g2.dispose();
        }
    }

    /** Tiêu đề tab có nút đóng riêng, luôn đóng đúng nội dung kể cả khi tab đổi vị trí. */
    static class CloseableTabComponent extends JPanel {
        private final JTabbedPane tabbedPane;
        private final Component tabContent;
        private final JLabel titleLabel;

        CloseableTabComponent(JTabbedPane tabbedPane, Component tabContent, String title) {
            super(new FlowLayout(FlowLayout.LEFT, 4, 0));
            this.tabbedPane = tabbedPane;
            this.tabContent = tabContent;
            setOpaque(false);

            titleLabel = new JLabel(title);
            titleLabel.setBorder(new EmptyBorder(0, 0, 0, 1));

            JButton closeButton = new JButton(CLOSE_CROSS_ICON);
            closeButton.setPreferredSize(new Dimension(16, 16));
            closeButton.setMargin(new Insets(0, 0, 0, 0));
            closeButton.setFocusable(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setToolTipText("Đóng tab " + title);
            closeButton.getAccessibleContext().setAccessibleName("Đóng tab " + title);
            closeButton.addActionListener(e -> tabbedPane.remove(tabContent));
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setContentAreaFilled(true);
                    closeButton.setBackground(new Color(255, 220, 215));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setContentAreaFilled(false);
                }
            });

            MouseAdapter selectTab = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int index = tabbedPane.indexOfComponent(tabContent);
                    if (index >= 0) tabbedPane.setSelectedIndex(index);
                }
            };
            addMouseListener(selectTab);
            titleLabel.addMouseListener(selectTab);

            add(titleLabel);
            add(closeButton);
            updateSelectionStyle();
        }

        void updateSelectionStyle() {
            boolean selected = tabbedPane.getSelectedComponent() == tabContent;
            titleLabel.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 12));
            titleLabel.setForeground(selected ? new Color(20, 20, 20) : new Color(60, 70, 85));
        }
    }

    public static final Icon ARROW_DOWN_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(40, 60, 95));
            int cx = x + 3;
            int cy = y + 5;
            int[] xPoints = {cx, cx + 8, cx + 4};
            int[] yPoints = {cy, cy, cy + 5};
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 14; }

        @Override
        public int getIconHeight() { return 14; }
    };

    public static final Icon CLOSE_CROSS_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(60, 75, 105));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + 3;
            int cy = y + 3;
            int size = 8;
            g2.drawLine(cx, cy, cx + size, cy + size);
            g2.drawLine(cx + size, cy, cx, cy + size);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 14; }

        @Override
        public int getIconHeight() { return 14; }
    };

    private static final Icon CHECKED_TAB_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(245, 145, 20)); // Viền cam
            g2.drawRect(x + 1, y + 1, 14, 14);
            g2.setColor(new Color(255, 248, 235));
            g2.fillRect(x + 2, y + 2, 13, 13);
            // Nét vẽ checkmark vector
            g2.setColor(new Color(30, 30, 30));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 4, y + 8, x + 7, y + 12);
            g2.drawLine(x + 7, y + 12, x + 13, y + 5);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return 18; }

        @Override
        public int getIconHeight() { return 18; }
    };

    private static final Icon UNCHECKED_TAB_ICON = new Icon() {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override
        public int getIconWidth() { return 18; }
        @Override
        public int getIconHeight() { return 18; }
    };
}
