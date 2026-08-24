package kazuko.ex.ui.main;

import kazuko.ex.model.system.SUser;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.auth.DoiMatKhauDialog;
import kazuko.ex.ui.auth.LoginForm;
import kazuko.ex.ui.common.StatusPanel;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.khohang.QuanLyXuatKhoPanel;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class MainFrame extends JFrame {
    private final SUser currentUser;
    private JTabbedPane mainTabbedPane;

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
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Menu Bar trên cùng
        setJMenuBar(createTopMenuBar());

        // 2. Toolbar Ribbon chứa các nút thao tác nhanh
        JPanel topToolBar = createRibbonToolBar();
        add(topToolBar, BorderLayout.NORTH);

        // 3. Hệ thống Tab trung tâm (Main TabbedPane)
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

//        mainTabbedPane.setSelectedIndex(1);

        add(mainTabbedPane, BorderLayout.CENTER);

        // 4. Thanh trạng thái StatusPanel dưới chân trang
        StatusPanel statusPanel = new StatusPanel(currentUser);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JMenuBar createTopMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. HỆ THỐNG
        JMenu mHeThong = new JMenu("HỆ THỐNG");

        // Submenu Cơ sở dữ liệu
        JMenu mCSDL = new JMenu("Cơ sở dữ liệu");
        JMenuItem miTaoMoi = new JMenuItem("Tạo mới");
        JMenuItem miSaoLuu = new JMenuItem("Sao lưu");
        JMenuItem miPhucHoi = new JMenuItem("Phục hồi");
        mCSDL.add(miTaoMoi);
        mCSDL.add(miSaoLuu);
        mCSDL.add(miPhucHoi);

        JMenuItem miDoiMK = new JMenuItem("Đổi mật khẩu");
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất");
        JMenuItem miThoat = new JMenuItem("Thoát");

        miDoiMK.addActionListener(e -> new DoiMatKhauDialog(this, currentUser).setVisible(true));
        miDangXuat.addActionListener(e -> onLogout());
        miThoat.addActionListener(e -> onExit());

        mHeThong.add(mCSDL);
        mHeThong.add(miDoiMK);
        mHeThong.add(miDangXuat);
        mHeThong.add(miThoat);

        // 2. HOẠT ĐỘNG
        JMenu mHoatDong = new JMenu("HOẠT ĐỘNG");
        mHoatDong.add(new JMenuItem("Danh mục cửa hàng"));
        mHoatDong.add(new JMenuItem("Danh mục mặt hàng"));
        mHoatDong.add(new JMenuItem("Danh mục khách hàng"));
        mHoatDong.add(new JMenuItem("Gửi tin nhắn khách hàng"));
        mHoatDong.add(new JMenuItem("Danh mục đợt khuyến mãi"));
        mHoatDong.add(new JMenuItem("Khách hàng thân thiết"));
        mHoatDong.add(new JMenuItem("Báo giá khách hàng"));
        mHoatDong.add(new JMenuItem("Khách đặt hàng"));
        mHoatDong.add(new JMenuItem("Hóa đơn bán hàng"));
        mHoatDong.add(new JMenuItem("Quản lý bán hàng"));
        mHoatDong.add(new JMenuItem("Lưu vết hoạt động"));
        mHoatDong.add(new JMenuItem("Thống kê doanh thu"));
        mHoatDong.add(new JMenuItem("Thống kê mặt hàng bán"));
        mHoatDong.add(new JMenuItem("Tổng hợp kết quả kinh doanh"));
        mHoatDong.add(new JMenuItem("Chi tiết hoạt động ngày"));

        // 3. KHO HÀNG
        JMenu mKhoHang = new JMenu("KHO HÀNG");
        JMenuItem miXuatKho = new JMenuItem("Xuất kho");
        miXuatKho.addActionListener(e -> openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel()));

        mKhoHang.add(new JMenuItem("Danh mục kho hàng"));
        mKhoHang.add(new JMenuItem("Danh mục nhà cung cấp"));
        mKhoHang.add(new JMenuItem("Đặt hàng nhà cung cấp"));
        mKhoHang.add(new JMenuItem("Nhập hàng vào kho"));
        mKhoHang.add(miXuatKho);
        mKhoHang.add(new JMenuItem("Chuyển kho"));
        mKhoHang.add(new JMenuItem("Kiểm kê kho"));
        mKhoHang.add(new JMenuItem("Tính lại giá vốn"));
        mKhoHang.add(new JMenuItem("Tồn kho"));
        mKhoHang.add(new JMenuItem("Tồn nhiều kho"));

        // 4. QUỸ
        JMenu mQuy = new JMenu("QUỸ");
        mQuy.add(new JMenuItem("Danh mục lý do thu chi"));
        mQuy.add(new JMenuItem("Danh mục tài khoản ngân hàng"));
        mQuy.add(new JMenuItem("Tạo phiếu thu"));
        mQuy.add(new JMenuItem("Tạo phiếu chi"));
        mQuy.add(new JMenuItem("Danh mục phiếu thu"));
        mQuy.add(new JMenuItem("Danh mục phiếu chi"));
        mQuy.add(new JMenuItem("Tồn quỹ"));

        // 5. CÔNG NỢ
        JMenu mCongNo = new JMenu("CÔNG NỢ");
        mCongNo.add(new JMenuItem("Công nợ khách hàng"));
        mCongNo.add(new JMenuItem("Công nợ nhà cung cấp"));

        // 6. NHÂN SỰ
        JMenu mNhanSu = new JMenu("NHÂN SỰ");
        JMenuItem miNhanVien = new JMenuItem("Danh mục nhân viên");
        miNhanVien.addActionListener(e -> new NhanVienDialog(this).setVisible(true));
        mNhanSu.add(miNhanVien);
        mNhanSu.add(new JMenuItem("Danh mục ca làm việc"));
        mNhanSu.add(new JMenuItem("Tạm ứng lương"));
        mNhanSu.add(new JMenuItem("Thưởng phạt"));
        mNhanSu.add(new JMenuItem("Chấm công"));
        mNhanSu.add(new JMenuItem("Tính lương"));

        // 7. QUẢN TRỊ
        JMenu mQuanTri = new JMenu("QUẢN TRỊ");
        mQuanTri.add(new JMenuItem("Thư viện ảnh"));

        // Submenu: Dữ liệu ban đầu
        JMenu mDuLieuBanDau = new JMenu("Dữ liệu ban đầu");
        mDuLieuBanDau.add(new JMenuItem("Công nợ khách hàng ban đầu"));
        mDuLieuBanDau.add(new JMenuItem("Công nợ nhà cung cấp ban đầu"));
        mDuLieuBanDau.add(new JMenuItem("Tồn kho ban đầu"));
        mQuanTri.add(mDuLieuBanDau);

        mQuanTri.add(new JMenuItem("Chỉ tiêu doanh thu"));
        mQuanTri.add(new JMenuItem("Xóa dữ liệu"));
        mQuanTri.add(new JMenuItem("Công cụ nhà phát triển"));
        mQuanTri.add(new JMenuItem("Người dùng và phân quyền"));
        mQuanTri.add(new JMenuItem("Cấu hình toàn hệ thống"));

        // 8. BÁO CÁO
        JMenu mBaoCao = new JMenu("BÁO CÁO");
        mBaoCao.add(new JMenuItem("Báo cáo bán hàng"));
        mBaoCao.add(new JMenuItem("Báo cáo kho hàng"));
        mBaoCao.add(new JMenuItem("Báo cáo quỹ"));
        mBaoCao.add(new JMenuItem("Tổng hợp kết quả kinh doanh (KQKD)"));

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

    private JPanel createRibbonToolBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.RIBBON_BG);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, AppColors.BORDER_BLUE));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        leftPanel.setOpaque(false);

        JButton btnHoaDon = createToolButton("Hóa đơn");
        JButton btnQLBanHang = createToolButton("Quản lý bán hàng");
        JButton btnTKDoanhThu = createToolButton("Thống kê doanh thu");
        JButton btnTKMatHang = createToolButton("Thống kê mặt hàng bán");
        JButton btnChiTietHD = createToolButton("Chi tiết hoạt động");
        JButton btnTongHopKQKD = createToolButton("Tổng hợp KQKD");
        JButton btnTonKho = createToolButton("Tồn kho");
        JButton btnGhiChu = createToolButton("Ghi chú");
        JButton btnBaoCao = createToolButton("Báo cáo");
        JButton btnThoat = createToolButton("Thoát");

        btnThoat.addActionListener(e -> onExit());

        leftPanel.add(btnHoaDon);
        leftPanel.add(btnQLBanHang);
        leftPanel.add(btnTKDoanhThu);
        leftPanel.add(btnTKMatHang);
        leftPanel.add(btnChiTietHD);
        leftPanel.add(btnTongHopKQKD);
        leftPanel.add(btnTonKho);
        leftPanel.add(btnGhiChu);
        leftPanel.add(btnBaoCao);
        leftPanel.add(btnThoat);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightPanel.setOpaque(false);

        JButton btnMoNhanh = createToolButton("Mở nhanh");
        btnMoNhanh.setToolTipText("Tìm kiếm và mở nhanh chức năng (Ctrl + G)");
        btnMoNhanh.addActionListener(e -> openQuickSearchDialog());
        rightPanel.add(btnMoNhanh);

        bar.add(leftPanel, BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);

        return bar;
    }

    private JButton createToolButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(AppColors.WHITE);
        return btn;
    }

    private void openTab(String title, JPanel panel) {
        int index = mainTabbedPane.indexOfTab(title);
        if (index >= 0) {
            mainTabbedPane.setSelectedIndex(index);
        } else {
            mainTabbedPane.addTab(title, panel);
            mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
        }
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel("<html><center><h2>HỆ THỐNG QUẢN LÝ BÁN HÀNG & KHO</h2><p>Chào mừng <b>" + (currentUser != null ? currentUser.getName() : "Admin") + "</b> đã đăng nhập vào hệ thống.</p></center></html>");
        panel.add(label);
        return panel;
    }

    private void onLogout() {
        this.dispose();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
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
            case "CẤU HÌNH CƠ SỞ DỮ LIỆU":
            case "CƠ SỞ DỮ LIỆU - CẤU HÌNH":
                new DatabaseConfigDialog(this).setVisible(true);
                break;
            case "QUẢN LÝ XUẤT KHO":
            case "XUẤT KHO":
                openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel());
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
                openTab(action, createPlaceholderPanel(action));
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
}
