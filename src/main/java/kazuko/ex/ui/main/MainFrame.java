package kazuko.ex.ui.main;

import kazuko.ex.model.system.SUser;
import kazuko.ex.ui.auth.DatabaseConfigDialog;
import kazuko.ex.ui.auth.LoginForm;
import kazuko.ex.ui.common.StatusPanel;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.khohang.QuanLyXuatKhoPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final SUser currentUser;
    private JTabbedPane mainTabbedPane;

    public MainFrame(SUser user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Thanh Tools - Hệ Thống Quản Lý Bán Hàng & Kho (v5.0 Clone)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Menu Bar trên cùng (HỆ THỐNG, HOẠT ĐỘNG, KHO HÀNG, QUỸ, CÔNG NỢ, NHÂN SỰ, QUẢN TRỊ, BÁO CÁO, TRỢ GIÚP)
        setJMenuBar(createTopMenuBar());

        // 2. Toolbar Ribbon chứa các nút thao tác nhanh (Hóa đơn, Quản lý bán hàng, Doanh thu, Tồn kho, Báo cáo...)
        JPanel topToolBar = createRibbonToolBar();
        add(topToolBar, BorderLayout.NORTH);

        // 3. Hệ thống Tab trung tâm (Main TabbedPane)
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Mở sẵn 2 Tab mẫu giống hệt ảnh 1
        mainTabbedPane.addTab("Lưu vết hoạt động", createWelcomePanel());
        mainTabbedPane.addTab("Quản lý xuất kho", new QuanLyXuatKhoPanel());
        mainTabbedPane.setSelectedIndex(1); // Mặc định focus vào tab Quản lý xuất kho

        add(mainTabbedPane, BorderLayout.CENTER);

        // 4. Thanh trạng thái StatusPanel dưới chân trang
        StatusPanel statusPanel = new StatusPanel(currentUser);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JMenuBar createTopMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // 1. HỆ THỐNG
        JMenu mHeThong = new JMenu("HỆ THỐNG");
        JMenuItem miDoiMK = new JMenuItem("Đổi mật khẩu");
        JMenuItem miCSDL = new JMenuItem("Cấu hình CSDL");
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất");
        JMenuItem miThoat = new JMenuItem("Thoát");

        miCSDL.addActionListener(e -> new DatabaseConfigDialog(this).setVisible(true));
        miDangXuat.addActionListener(e -> onLogout());
        miThoat.addActionListener(e -> System.exit(0));

        mHeThong.add(miDoiMK);
        mHeThong.add(miCSDL);
        mHeThong.addSeparator();
        mHeThong.add(miDangXuat);
        mHeThong.add(miThoat);

        // 2. HOẠT ĐỘNG
        JMenu mHoatDong = new JMenu("HOẠT ĐỘNG");
        mHoatDong.add(new JMenuItem("DM Cửa hàng"));
        mHoatDong.add(new JMenuItem("DM Mặt hàng"));
        mHoatDong.add(new JMenuItem("DM Khách hàng"));
        mHoatDong.addSeparator();
        mHoatDong.add(new JMenuItem("Báo giá KH"));
        mHoatDong.add(new JMenuItem("Khách đặt hàng"));
        mHoatDong.add(new JMenuItem("Hóa đơn bán hàng"));

        // 3. KHO HÀNG
        JMenu mKhoHang = new JMenu("KHO HÀNG");
        JMenuItem miXuatKho = new JMenuItem("Quản lý xuất kho");
        miXuatKho.addActionListener(e -> openTab("Quản lý xuất kho", new QuanLyXuatKhoPanel()));
        mKhoHang.add(new JMenuItem("DM Kho hàng"));
        mKhoHang.add(new JMenuItem("DM Nhà cung cấp"));
        mKhoHang.addSeparator();
        mKhoHang.add(new JMenuItem("Nhập hàng vào kho"));
        mKhoHang.add(miXuatKho);
        mKhoHang.add(new JMenuItem("Chuyển kho"));
        mKhoHang.add(new JMenuItem("Kiểm kê kho"));
        mKhoHang.add(new JMenuItem("Tồn kho"));

        // 4. QUỸ
        JMenu mQuy = new JMenu("QUỸ");
        mQuy.add(new JMenuItem("Phiếu thu"));
        mQuy.add(new JMenuItem("Phiếu chi"));
        mQuy.add(new JMenuItem("Tồn quỹ"));

        // 5. CÔNG NỢ
        JMenu mCongNo = new JMenu("CÔNG NỢ");
        mCongNo.add(new JMenuItem("Công nợ Khách hàng"));
        mCongNo.add(new JMenuItem("Công nợ Nhà cung cấp"));

        // 6. NHÂN SỰ
        JMenu mNhanSu = new JMenu("NHÂN SỰ");
        JMenuItem miNhanVien = new JMenuItem("Danh mục Nhân viên");
        miNhanVien.addActionListener(e -> new NhanVienDialog(this).setVisible(true));
        mNhanSu.add(miNhanVien);
        mNhanSu.add(new JMenuItem("Ca làm việc"));
        mNhanSu.add(new JMenuItem("Chấm công"));
        mNhanSu.add(new JMenuItem("Tính lương"));

        // 7. QUẢN TRỊ
        JMenu mQuanTri = new JMenu("QUẢN TRỊ");
        mQuanTri.add(new JMenuItem("Người dùng & Phân quyền"));
        mQuanTri.add(new JMenuItem("Chỉ tiêu doanh thu"));
        mQuanTri.add(new JMenuItem("Cấu hình hệ thống"));

        // 8. BÁO CÁO
        JMenu mBaoCao = new JMenu("BÁO CÁO");
        mBaoCao.add(new JMenuItem("Báo cáo bán hàng"));
        mBaoCao.add(new JMenuItem("Báo cáo kho hàng"));
        mBaoCao.add(new JMenuItem("Báo cáo quỹ"));
        mBaoCao.add(new JMenuItem("Tổng hợp kết quả kinh doanh (KQKD)"));

        // 9. TRỢ GIÚP
        JMenu mTroGiup = new JMenu("TRỢ GIÚP");
        mTroGiup.add(new JMenuItem("Đăng ký bản quyền"));
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
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        bar.setBackground(new Color(235, 240, 248));
        bar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 215, 230)));

        JButton btnHoaDon = createToolButton("🧾 Hóa đơn");
        JButton btnQLBanHang = createToolButton("🛒 Quản lý bán hàng");
        JButton btnTKDoanhThu = createToolButton("📈 Thống kê doanh thu");
        JButton btnTKMatHang = createToolButton("📦 Thống kê mặt hàng bán");
        JButton btnTongHopKQKD = createToolButton("📊 Tổng hợp KQKD");
        JButton btnTonKho = createToolButton("🏢 Tồn kho");
        JButton btnNhanVien = createToolButton("👥 Nhân viên");
        JButton btnThoat = createToolButton("❌ Thoát");

        btnNhanVien.addActionListener(e -> new NhanVienDialog(this).setVisible(true));
        btnThoat.addActionListener(e -> System.exit(0));

        bar.add(btnHoaDon);
        bar.add(btnQLBanHang);
        bar.add(btnTKDoanhThu);
        bar.add(btnTKMatHang);
        bar.add(btnTongHopKQKD);
        bar.add(btnTonKho);
        bar.add(btnNhanVien);
        bar.add(btnThoat);

        return bar;
    }

    private JButton createToolButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
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
}
