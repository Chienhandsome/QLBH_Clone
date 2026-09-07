package kazuko.ex.ui.main;

import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.auth.DoiMatKhauDialog;
import kazuko.ex.ui.auth.LoginForm;
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
import kazuko.ex.ui.khohang.QuanLyChuyenKhoPanel;
import kazuko.ex.ui.khohang.QuanLyKiemKePanel;
import kazuko.ex.ui.khohang.QuanLyNhapKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonKhoPanel;
import kazuko.ex.ui.khohang.QuanLyTonNhieuKhoPanel;
import kazuko.ex.ui.khohang.QuanLyXuatKhoPanel;
import kazuko.ex.ui.khohang.TinhLaiGiaVonDialog;
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
import kazuko.ex.ui.system.KhoiPhucCSDLDialog;
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

        // Cụm nút góc phải thanh Tab: [▾] (Danh sách tab) và [✕] (Đóng tab)
        JPanel pnlTabControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 1));
        pnlTabControls.setOpaque(false);

        JButton btnTabList = createTabControlButton(ARROW_DOWN_ICON, "Danh sách các tab đang mở");
        JButton btnTabClose = createTabControlButton(CLOSE_CROSS_ICON, "Đóng tab hiện tại (Ctrl + W)");

        btnTabList.addActionListener(e -> showTabListPopup(btnTabList));
        btnTabClose.addActionListener(e -> closeCurrentTab());

        pnlTabControls.add(btnTabList);
        pnlTabControls.add(btnTabClose);

        JPanel container = new JPanel(null) {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                mainTabbedPane.setBounds(0, 0, w, h);
                pnlTabControls.setBounds(w - 52, 4, 48, 22);
            }
        };
        container.add(pnlTabControls);
        container.add(mainTabbedPane);

        // Mở sẵn tab Danh mục mặt hàng
        openTab("Danh mục mặt hàng", new DanhMucMatHangPanel());

        return container;
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
            if (mainTabbedPane.getTabCount() == 0) {
                openTab("Danh mục mặt hàng", new DanhMucMatHangPanel());
            }
        }
    }

    private JMenuBar createTopMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. HỆ THỐNG
        JMenu mHeThong = new JMenu("HỆ THỐNG");
        JMenu mCSDL = new JMenu("Cơ sở dữ liệu...");
        mCSDL.setIcon(IconHelper.getIcon("database1.png", 16, 16));

        JMenuItem miTaoMoi = new JMenuItem("Tạo mới");
        JMenuItem miSaoLuu = new JMenuItem("Sao lưu");
        JMenuItem miPhucHoi = new JMenuItem("Phục hồi");
        
        miTaoMoi.addActionListener(e -> new TaoMoiCSDLDialog(this).setVisible(true));
        miSaoLuu.addActionListener(e -> new SaoLuuCSDLDialog(this).setVisible(true));
        miPhucHoi.addActionListener(e -> new KhoiPhucCSDLDialog(this).setVisible(true));

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
        JMenuItem miCuaHang = new JMenuItem("Danh mục cửa hàng");
        miCuaHang.setIcon(IconHelper.getIcon("Folder2.png", 16, 16));
        miCuaHang.addActionListener(e -> new CuaHangDialog(this).setVisible(true));

        JMenuItem miMatHang = new JMenuItem("Danh mục mặt hàng");
        miMatHang.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
        miMatHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        miMatHang.addActionListener(e -> openTab("Danh mục mặt hàng", new DanhMucMatHangPanel()));

        JMenuItem miKhachHang = new JMenuItem("Danh mục khách hàng");
        miKhachHang.setIcon(IconHelper.getIcon("users.png", 16, 16));
        miKhachHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        miKhachHang.addActionListener(e -> openTab("Danh mục khách hàng", new DanhMucKhachHangPanel()));

        JMenuItem miTinNhan = new JMenuItem("Gửi tin nhắn tới khách hàng");
        miTinNhan.setIcon(IconHelper.getIcon("mail-send.png", 16, 16));
        miTinNhan.addActionListener(e -> openTab("Gửi tin nhắn tới khách hàng", createPlaceholderPanel("Gửi tin nhắn tới khách hàng")));

        JMenuItem miKhuyenMai = new JMenuItem("Danh mục đợt khuyến mại");
        miKhuyenMai.setIcon(IconHelper.getIcon("bookmark-import.png", 16, 16));
        miKhuyenMai.addActionListener(e -> openTab("Danh mục đợt khuyến mại", new DanhMucDotKhuyenMaiPanel()));

        JMenuItem miThanThiet = new JMenuItem("Khách hàng thân thiết");
        miThanThiet.setIcon(IconHelper.getIcon("btnCard.Icon.png", 16, 16));
        miThanThiet.addActionListener(e -> openTab("Khách hàng thân thiết", new KhachHangThanThietPanel()));

        mHoatDong.add(miCuaHang);
        mHoatDong.add(miMatHang);
        mHoatDong.add(miKhachHang);
        mHoatDong.add(miTinNhan);
        mHoatDong.add(miKhuyenMai);
        mHoatDong.add(miThanThiet);

        // Divider 1
        mHoatDong.addSeparator();

        // Group 2: Báo giá & Đặt hàng
        JMenuItem miBaoGia = new JMenuItem("Báo giá bán hàng");
        miBaoGia.setIcon(IconHelper.getIcon("report--pencil.png", 16, 16));
        miBaoGia.addActionListener(e -> openTab("Báo giá bán hàng", new BaoGiaBanHangPanel()));

        JMenuItem miDatHang = new JMenuItem("Khách đặt hàng");
        miDatHang.setIcon(IconHelper.getIcon("tsbLogedUser.Image.png", 16, 16));
        miDatHang.addActionListener(e -> openTab("Khách đặt hàng", new KhachDatHangPanel()));

        mHoatDong.add(miBaoGia);
        mHoatDong.add(miDatHang);

        // Divider 2
        mHoatDong.addSeparator();

        // Group 3: Bán hàng & Hóa đơn
        JMenuItem miHoaDon = new JMenuItem("Hóa đơn bán hàng");
        miHoaDon.setIcon(IconHelper.getIcon("btnNew.Image.png", 16, 16));
        miHoaDon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        miHoaDon.addActionListener(e -> openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel()));

        JMenuItem miQLBanHang = new JMenuItem("Quản lý bán hàng");
        miQLBanHang.setIcon(IconHelper.getIcon("btnOpen.Image.png", 16, 16));
        miQLBanHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        miQLBanHang.addActionListener(e -> openTab("Quản lý bán hàng", new QuanLyBanHangPanel()));

        mHoatDong.add(miHoaDon);
        mHoatDong.add(miQLBanHang);

        // Divider 3
        mHoatDong.addSeparator();

        // Group 4: Nhật ký
        JMenuItem miLuuVet = new JMenuItem("Lưu vết hoạt động");
        miLuuVet.setIcon(IconHelper.getIcon("tsbLog.Image.png", 16, 16));
        miLuuVet.addActionListener(e -> openTab("Lưu vết hoạt động", createPlaceholderPanel("Lưu vết hoạt động")));

        mHoatDong.add(miLuuVet);

        // Divider 4
        mHoatDong.addSeparator();

        // Group 5: Thống kê & Báo cáo
        JMenuItem miTKDoanhThu = new JMenuItem("Thống kê doanh thu");
        miTKDoanhThu.setIcon(IconHelper.getIcon("Statistic16.png", 16, 16));
        miTKDoanhThu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        miTKDoanhThu.addActionListener(e -> openTab("Thống kê doanh thu", createPlaceholderPanel("Thống kê doanh thu")));

        JMenuItem miTKMatHang = new JMenuItem("Thống kê mặt hàng bán");
        miTKMatHang.setIcon(IconHelper.getIcon("Tree.png", 16, 16));
        miTKMatHang.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        miTKMatHang.addActionListener(e -> openTab("Thống kê mặt hàng bán", createPlaceholderPanel("Thống kê mặt hàng bán")));

        JMenuItem miTongHopKQKD = new JMenuItem("Tổng hợp kết quả kinh doanh");
        miTongHopKQKD.setIcon(IconHelper.getIcon("Report.png", 16, 16));
        miTongHopKQKD.addActionListener(e -> openTab("Tổng hợp KQKD", createPlaceholderPanel("Tổng hợp kết quả kinh doanh")));

        JMenuItem miChiTietHD = new JMenuItem("Chi tiết hoạt động ngày");
        miChiTietHD.setIcon(IconHelper.getIcon("arrow-000-medium.png", 16, 16));
        miChiTietHD.addActionListener(e -> openTab("Chi tiết hoạt động", createPlaceholderPanel("Chi tiết hoạt động")));

        mHoatDong.add(miTKDoanhThu);
        mHoatDong.add(miTKMatHang);
        mHoatDong.add(miTongHopKQKD);
        mHoatDong.add(miChiTietHD);

        // 3. KHO HÀNG
        JMenu mKhoHang = new JMenu("KHO HÀNG");

        JMenuItem miKhoHang = new JMenuItem("Danh mục kho hàng");
        miKhoHang.setIcon(IconHelper.getIcon("Home.png", 16, 16));
        miKhoHang.addActionListener(e -> new KhoHangDialog(this).setVisible(true));

        JMenuItem miNCC = new JMenuItem("Danh mục nhà cung cấp");
        miNCC.setIcon(IconHelper.getIcon("users.png", 16, 16));
        miNCC.addActionListener(e -> openTab("Danh mục nhà cung cấp", new DanhMucNhaCungCapPanel()));

        mKhoHang.add(miKhoHang);
        mKhoHang.add(miNCC);

        mKhoHang.addSeparator();

        JMenuItem miDatHangNCC = new JMenuItem("Đặt hàng nhà cung cấp");
        miDatHangNCC.setIcon(IconHelper.getIcon("report--pencil.png", 16, 16));
        miDatHangNCC.addActionListener(e -> openTab("Đặt hàng nhà cung cấp", new DatHangNhaCungCapPanel()));

        JMenuItem miNhapKho = new JMenuItem("Nhập hàng vào kho");
        miNhapKho.setIcon(IconHelper.getIcon("navigation-000-frame.png", 16, 16));
        miNhapKho.addActionListener(e -> openTab("Quản lý nhập kho", new QuanLyNhapKhoPanel()));

        JMenuItem miXuatKhac = new JMenuItem("Xuất kho");
        miXuatKhac.setIcon(IconHelper.getIcon("navigation-180-frame.png", 16, 16));
        miXuatKhac.addActionListener(e -> openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel()));

        JMenuItem miChuyenKho = new JMenuItem("Chuyển kho");
        miChuyenKho.setIcon(IconHelper.getIcon("document_valid.png", 16, 16));
        miChuyenKho.addActionListener(e -> openTab("Quản lý chuyển kho", new QuanLyChuyenKhoPanel()));

        JMenuItem miKiemKe = new JMenuItem("Kiểm kê kho");
        miKiemKe.setIcon(IconHelper.getIcon("edit-column.png", 16, 16));
        miKiemKe.addActionListener(e -> openTab("Quản lý kiểm kê", new QuanLyKiemKePanel()));

        JMenuItem miTinhGiaVon = new JMenuItem("Tính lại giá vốn");
        miTinhGiaVon.setIcon(IconHelper.getIcon("Refresh.png", 16, 16));
        miTinhGiaVon.addActionListener(e -> new TinhLaiGiaVonDialog(this).setVisible(true));

        mKhoHang.add(miDatHangNCC);
        mKhoHang.add(miNhapKho);
        mKhoHang.add(miXuatKhac);
        mKhoHang.add(miChuyenKho);
        mKhoHang.add(miKiemKe);
        mKhoHang.add(miTinhGiaVon);

        mKhoHang.addSeparator();

        JMenuItem miTonKho = new JMenuItem("Tồn kho");
        miTonKho.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
        miTonKho.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        miTonKho.addActionListener(e -> openTab("Tồn kho", new QuanLyTonKhoPanel()));

        JMenuItem miTonNhieuKho = new JMenuItem("Tồn nhiều kho");
        miTonNhieuKho.setIcon(IconHelper.getIcon("tsbTimeStamp.Image.png", 16, 16));
        miTonNhieuKho.addActionListener(e -> openTab("Tồn nhiều kho", new QuanLyTonNhieuKhoPanel()));

        mKhoHang.add(miTonKho);
        mKhoHang.add(miTonNhieuKho);

        // 4. QUỸ
        JMenu mQuy = new JMenu("QUỸ");

        JMenuItem miLyDoTC = new JMenuItem("Danh mục lý do thu chi");
        miLyDoTC.setIcon(IconHelper.getIcon("congno.png", 16, 16));
        miLyDoTC.addActionListener(e -> new LyDoThuChiDialog(this).setVisible(true));

        JMenuItem miTKNH = new JMenuItem("Danh mục tài khoản ngân hàng");
        miTKNH.setIcon(IconHelper.getIcon("database1.png", 16, 16));
        miTKNH.addActionListener(e -> new TaiKhoanNganHangDialog(this).setVisible(true));

        mQuy.add(miLyDoTC);
        mQuy.add(miTKNH);

        mQuy.addSeparator();

        JMenuItem miTaoThu = new JMenuItem("Tạo phiếu thu");
        miTaoThu.setIcon(IconHelper.getIcon("arrow-000-medium.png", 16, 16));
        miTaoThu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        miTaoThu.addActionListener(e -> new TaoPhieuThuDialog(this).setVisible(true));

        JMenuItem miTaoChi = new JMenuItem("Tạo phiếu chi");
        miTaoChi.setIcon(IconHelper.getIcon("arrow-180-medium.png", 16, 16));
        miTaoChi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        miTaoChi.addActionListener(e -> new TaoPhieuChiDialog(this).setVisible(true));

        JMenuItem miDSThu = new JMenuItem("Danh mục phiếu thu");
        miDSThu.setIcon(IconHelper.getIcon("btnCard.Icon.png", 16, 16));
        miDSThu.addActionListener(e -> openTab("Danh mục phiếu thu", new DanhMucPhieuThuPanel()));

        JMenuItem miDSChi = new JMenuItem("Danh mục phiếu chi");
        miDSChi.setIcon(IconHelper.getIcon("tsbTimeStamp.Image.png", 16, 16));
        miDSChi.addActionListener(e -> openTab("Danh mục phiếu chi", new DanhMucPhieuChiPanel()));

        mQuy.add(miTaoThu);
        mQuy.add(miTaoChi);
        mQuy.add(miDSThu);
        mQuy.add(miDSChi);

        mQuy.addSeparator();

        JMenuItem miTonQuy = new JMenuItem("Tồn quỹ");
        miTonQuy.setIcon(IconHelper.getIcon("report--pencil.png", 16, 16));
        miTonQuy.addActionListener(e -> openTab("Tồn quỹ", new TonQuyPanel()));

        mQuy.add(miTonQuy);

        // 5. CÔNG NỢ
        JMenu mCongNo = new JMenu("CÔNG NỢ");

        JMenuItem miCongNoKH = new JMenuItem("Công nợ khách hàng");
        miCongNoKH.setIcon(IconHelper.getIcon("congno.png", 16, 16));
        miCongNoKH.addActionListener(e -> openTab("Công nợ khách hàng", new QuanLyCongNoKhachHangPanel()));

        JMenuItem miCongNoNCC = new JMenuItem("Công nợ nhà cung cấp");
        miCongNoNCC.setIcon(IconHelper.getIcon("tsbGenTotal.Image.png", 16, 16));
        miCongNoNCC.addActionListener(e -> openTab("Công nợ nhà cung cấp", new QuanLyCongNoNhaCungCapPanel()));

        mCongNo.add(miCongNoKH);
        mCongNo.add(miCongNoNCC);

        // 6. NHÂN SỰ
        JMenu mNhanSu = new JMenu("NHÂN SỰ");

        JMenuItem miNhanVien = new JMenuItem("Danh mục nhân viên");
        miNhanVien.setIcon(IconHelper.getIcon("users.png", 16, 16));
        miNhanVien.addActionListener(e -> new NhanVienDialog(this).setVisible(true));

        JMenuItem miCaLamViec = new JMenuItem("Danh mục ca làm việc");
        miCaLamViec.setIcon(IconHelper.getIcon("property.png", 16, 16));
        miCaLamViec.addActionListener(e -> new CaLamViecDialog(this).setVisible(true));

        mNhanSu.add(miNhanVien);
        mNhanSu.add(miCaLamViec);

        mNhanSu.addSeparator();

        JMenuItem miTamUng = new JMenuItem("Tạm ứng lương");
        miTamUng.setIcon(IconHelper.getIcon("receipt--pencil.png", 16, 16));
        miTamUng.addActionListener(e -> openTab("Tạm ứng lương", new TamUngLuongPanel()));

        JMenuItem miThuongPhat = new JMenuItem("Thưởng phạt");
        miThuongPhat.setIcon(IconHelper.getIcon("asterisk-yellow.png", 16, 16));
        miThuongPhat.addActionListener(e -> openTab("Thưởng phạt", new ThuongPhatPanel()));

        JMenuItem miChamCong = new JMenuItem("Chấm công");
        miChamCong.setIcon(IconHelper.getIcon("document_valid.png", 16, 16));
        miChamCong.addActionListener(e -> openTab("Chấm công", new ChamCongPanel()));

        JMenuItem miTinhLuong = new JMenuItem("Tính lương");
        miTinhLuong.setIcon(IconHelper.getIcon("Statistic16.png", 16, 16));
        miTinhLuong.addActionListener(e -> openTab("Tính lương", new TinhLuongPanel()));

        mNhanSu.add(miTamUng);
        mNhanSu.add(miThuongPhat);
        mNhanSu.add(miChamCong);
        mNhanSu.add(miTinhLuong);

        // 7. QUẢN TRỊ
        JMenu mQuanTri = new JMenu("QUẢN TRỊ");

        JMenuItem miThuVienAnh = new JMenuItem("Thư viện ảnh...");
        miThuVienAnh.addActionListener(e -> openTab("Thư viện ảnh", createPlaceholderPanel("Thư viện ảnh")));
        mQuanTri.add(miThuVienAnh);

        mQuanTri.addSeparator();

        JMenu mDuLieuBanDau = new JMenu("Dữ liệu ban đầu");
        JMenuItem miCongNoKHBD = new JMenuItem("Công nợ khách hàng ban đầu");
        JMenuItem miCongNoNCCBD = new JMenuItem("Công nợ nhà cung cấp ban đầu");
        JMenuItem miTonKhoBD = new JMenuItem("Tồn kho ban đầu");
        miCongNoKHBD.addActionListener(e -> new CongNoKhachHangBanDauDialog(this).setVisible(true));
        miCongNoNCCBD.addActionListener(e -> new CongNoNhaCungCapBanDauDialog(this).setVisible(true));
        miTonKhoBD.addActionListener(e -> openTab("Tồn kho ban đầu", createPlaceholderPanel("Tồn kho ban đầu")));
        mDuLieuBanDau.add(miCongNoKHBD);
        mDuLieuBanDau.add(miCongNoNCCBD);
        mDuLieuBanDau.add(miTonKhoBD);
        mQuanTri.add(mDuLieuBanDau);

        JMenuItem miChiTieuDT = new JMenuItem("Chỉ tiêu doanh thu");
        miChiTieuDT.setIcon(IconHelper.getIcon("property.png", 16, 16));
        miChiTieuDT.addActionListener(e -> openTab("Chỉ tiêu doanh thu", createPlaceholderPanel("Chỉ tiêu doanh thu")));

        JMenuItem miXoaDuLieu = new JMenuItem("Xóa dữ liệu");
        miXoaDuLieu.setIcon(IconHelper.getIcon("clear.png", 16, 16));
        miXoaDuLieu.addActionListener(e -> openTab("Xóa dữ liệu", createPlaceholderPanel("Xóa dữ liệu")));

        JMenuItem miDevTools = new JMenuItem("Công cụ nhà phát triển");
        miDevTools.setIcon(IconHelper.getIcon("page_white_code.png", 16, 16));
        miDevTools.addActionListener(e -> openTab("Công cụ nhà phát triển", createPlaceholderPanel("Công cụ nhà phát triển")));

        JMenuItem miNguoiDung = new JMenuItem("Người dùng và phân quyền");
        miNguoiDung.setIcon(IconHelper.getIcon("users.png", 16, 16));
        miNguoiDung.addActionListener(e -> openTab("Người dùng và phân quyền", createPlaceholderPanel("Người dùng và phân quyền")));

        mQuanTri.add(miChiTieuDT);
        mQuanTri.add(miXoaDuLieu);
        mQuanTri.add(miDevTools);
        mQuanTri.add(miNguoiDung);

        mQuanTri.addSeparator();

        JMenuItem miCauHinh = new JMenuItem("Cấu hình toàn hệ thống");
        miCauHinh.setFont(miCauHinh.getFont().deriveFont(Font.BOLD));
        miCauHinh.setIcon(IconHelper.getIcon("wrench-screwdriver.png", 16, 16));
        miCauHinh.addActionListener(e -> new CauHinhToanHeThongDialog(this).setVisible(true));
        mQuanTri.add(miCauHinh);

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

        return menuBar;
    }

    private ResponsiveToolBar createRibbonToolBar() {
        ResponsiveToolBar bar = new ResponsiveToolBar();

        JButton btnHoaDon = createToolButton("Hóa đơn", "btnNew.Image.png");
        JButton btnQLBanHang = createToolButton("Quản lý bán hàng", "btnOpen.Image.png");
        JButton btnTKDoanhThu = createToolButton("Thống kê doanh thu", "Statistic16.png");
        JButton btnTKMatHang = createToolButton("Thống kê mặt hàng bán", "1282529873_objects.png");
        JButton btnChiTietHD = createToolButton("Chi tiết hoạt động", "Report.png");
        JButton btnTongHopKQKD = createToolButton("Tổng hợp KQKD", "Tree.png");
        JButton btnTonKho = createToolButton("Tồn kho", "Folder2.png");
        JButton btnGhiChu = createToolButton("Ghi chú", "FilterEdit.png");
        JButton btnBaoCao = createToolButton("Báo cáo", "Preview.png");
        JButton btnThoat = createToolButton("Thoát", "Cancelled.png");
        JButton btnMoNhanh = createToolButton("Mở nhanh", "tsbQuickOpen.Image.png");

        btnHoaDon.addActionListener(e -> openTab("Hóa đơn bán hàng", new HoaDonBanHangPanel()));
        btnQLBanHang.addActionListener(e -> openTab("Quản lý bán hàng", new QuanLyBanHangPanel()));
        btnTKDoanhThu.addActionListener(e -> openTab("Thống kê doanh thu", createPlaceholderPanel("Thống kê doanh thu")));
        btnTKMatHang.addActionListener(e -> openTab("Danh mục mặt hàng", new DanhMucMatHangPanel()));
        btnChiTietHD.addActionListener(e -> openTab("Chi tiết hoạt động", createPlaceholderPanel("Chi tiết hoạt động")));
        btnTongHopKQKD.addActionListener(e -> openTab("Tổng hợp KQKD", createPlaceholderPanel("Tổng hợp kết quả kinh doanh")));
        btnTonKho.addActionListener(e -> openTab("Tồn kho", new QuanLyTonKhoPanel()));
        btnGhiChu.addActionListener(e -> openTab("Ghi chú", createPlaceholderPanel("Ghi chú")));
        btnBaoCao.addActionListener(e -> openTab("Báo cáo", createPlaceholderPanel("Báo cáo")));
        btnThoat.addActionListener(e -> onExit());
        btnMoNhanh.addActionListener(e -> openQuickSearchDialog());

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
        int index = mainTabbedPane.indexOfTab(title);
        if (index >= 0) {
            mainTabbedPane.setSelectedIndex(index);
        } else {
            mainTabbedPane.addTab(title, panel);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
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

    private void openQuickSearchDialog() {
        QuickSearchDialog dialog = new QuickSearchDialog(this, this::handleQuickAction);
        dialog.setVisible(true);
    }

    private void handleQuickAction(String action) {
        if (action == null) return;
        switch (action) {
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
            case "GỬI TIN NHẮN KHÁCH HÀNG":
                openTab("Gửi tin nhắn tới khách hàng", createPlaceholderPanel("Gửi tin nhắn tới khách hàng"));
                break;
            case "CẤU HÌNH TOÀN HỆ THỐNG":
            case "CẤU HÌNH HỆ THỐNG":
                new CauHinhToanHeThongDialog(this).setVisible(true);
                break;
            case "CÔNG NỢ KHÁCH HÀNG BAN ĐẦU":
                new CongNoKhachHangBanDauDialog(this).setVisible(true);
                break;
            case "CÔNG NỢ NHÀ CUNG CẤP BAN ĐẦU":
                new CongNoNhaCungCapBanDauDialog(this).setVisible(true);
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
            case "BÁO CÁO TỒN QUỸ":
            case "TỒN QUỸ":
            case "SỔ QUỸ":
                openTab("Tồn quỹ", new TonQuyPanel());
                break;
            case "KHÁCH HÀNG THÂN THIẾT":
                openTab("Khách hàng thân thiết", new KhachHangThanThietPanel());
                break;
            case "DANH SÁCH ĐỢT KHUYẾN MẠI":
            case "DANH MỤC ĐỢT KHUYẾN MÃI":
                openTab("Danh mục đợt khuyến mại", new DanhMucDotKhuyenMaiPanel());
                break;
            case "ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG":
            case "BÁO CÁO CÔNG NỢ KHÁCH HÀNG":
            case "TỔNG HỢP CÔNG NỢ KHÁCH HÀNG":
            case "CÔNG NỢ KHÁCH HÀNG":
            case "CÔNG NỢ KHÁCH":
                openTab("Công nợ khách hàng", new QuanLyCongNoKhachHangPanel());
                break;
            case "ĐỐI CHIẾU CÔNG NỢ NHÀ CUNG CẤP":
            case "BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP":
            case "TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP":
            case "CÔNG NỢ NHÀ CUNG CẤP":
            case "CÔNG NỢ NCC":
                openTab("Công nợ nhà cung cấp", new QuanLyCongNoNhaCungCapPanel());
                break;
            case "BÁO CÁO HÀNG TỒN KHO":
            case "BÁO CÁO TỒN KHO THEO KÍCH THƯỚC":
            case "BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN":
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
            case "DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT":
            case "DANH MỤC MẶT HÀNG":
                openTab("Danh mục mặt hàng", new DanhMucMatHangPanel());
                break;
            case "DANH SÁCH KHÁCH HÀNG THEO NHÓM":
            case "DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN":
            case "DANH MỤC KHÁCH HÀNG":
                openTab("Danh mục khách hàng", new DanhMucKhachHangPanel());
                break;
            case "DANH SÁCH NHÀ CUNG CẤP THEO NHÓM":
            case "DANH MỤC NHÀ CUNG CẤP":
                openTab("Danh mục nhà cung cấp", new DanhMucNhaCungCapPanel());
                break;
            case "DANH SÁCH PHIẾU THU THEO NGÀY":
            case "DANH SÁCH PHIẾU THU THEO LÝ DO THU CHI":
            case "DANH MỤC PHIẾU THU":
                openTab("Danh mục phiếu thu", new DanhMucPhieuThuPanel());
                break;
            case "DANH SÁCH PHIẾU CHI THEO NGÀY":
            case "DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI":
            case "DANH MỤC PHIẾU CHI":
                openTab("Danh mục phiếu chi", new DanhMucPhieuChiPanel());
                break;
            case "DANH SÁCH ĐẶT HÀNG THEO NGÀY":
            case "DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "TỔNG HỢP ĐẶT HÀNG THEO NGÀY":
            case "TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG":
            case "KHÁCH ĐẶT HÀNG":
                openTab("Khách đặt hàng", new KhachDatHangPanel());
                break;
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP":
            case "DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP":
            case "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP NHẬP THEO NGÀY":
            case "TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP":
            case "TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY":
            case "BÁO CÁO TỔNG HỢP NHẬP KHO":
            case "QUẢN LÝ NHẬP KHO":
                openTab("Quản lý nhập kho", new QuanLyNhapKhoPanel());
                break;
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU XUẤT KHO THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN XUẤT":
            case "TỔNG HỢP XUẤT KHÁC THEO NGÀY":
            case "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN XUẤT":
            case "BÁO CÁO TỔNG HỢP XUẤT KHO":
            case "QUẢN LÝ XUẤT KHO":
                openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel());
                break;
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY":
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT":
            case "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN":
            case "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY":
            case "BÁO CÁO TỔNG HỢP CHUYỂN KHO":
            case "QUẢN LÝ CHUYỂN KHO":
                openTab("Quản lý chuyển kho", new QuanLyChuyenKhoPanel());
                break;
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY":
            case "DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY":
            case "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN":
            case "BÁO CÁO TỔNG HỢP KIỂM KÊ KHO":
            case "QUẢN LÝ KIỂM KÊ":
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
                    JMenuItem item = new JMenuItem(btn.getText(), btn.getIcon());
                    item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    item.setBackground(Color.WHITE);
                    for (ActionListener al : btn.getActionListeners()) {
                        item.addActionListener(al);
                    }
                    popup.add(item);
                }
            }

            popup.show(btnOverflow, btnOverflow.getWidth() - popup.getPreferredSize().width, btnOverflow.getHeight());
        }
    }

    // ==================== CUSTOM TABBED PANE UI ====================
    public static class CustomModernTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(4, 14, 4, 14);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(2, 4, 0, 56); // Chừa 56px bên phải cho 2 nút dropdown và close
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
