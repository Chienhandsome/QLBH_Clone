package kazuko.ex.ui.touch;

import kazuko.ex.config.SystemConfigRestartPolicy;
import kazuko.ex.dao.STemplateDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.STemplate;
import kazuko.ex.model.system.SystemConfig;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Giao diện CẤU HÌNH TOÀN HỆ THỐNG cho màn hình cảm ứng POS (TouchCauHinhToanHeThongPanel).
 * Chuẩn hóa giao diện khớp 100% hai ảnh thiết kế:
 * - media_1789540444537.png: Tab "Thông tin chung" (Header banner, Sidebar 12 nút, Thông tin công ty + bàn phím ảo, Logo preview, Mẫu hóa đơn, Checkbox cảm ứng).
 * - media_1789540472604.png: Tab "Bán hàng" (Danh sách checkbox vẽ dấu X trắng đậm, các ô số kèm nút máy tính).
 */
public final class TouchCauHinhToanHeThongPanel extends JPanel {
    // Theme colors matching the design images exactly
    public static final Color BG_FRAME = new Color(11, 46, 56);         // #0B2E38 Dark teal-black frame
    public static final Color HEADER_BG = new Color(53, 98, 116);       // #356274 Slate cyan header banner
    public static final Color SIDEBAR_BTN_NORMAL = new Color(0, 96, 0);  // #006000 Dark green inactive tab
    public static final Color SIDEBAR_BTN_ACTIVE = new Color(255, 140, 0); // #FF8C00 Vibrant orange active tab
    public static final Color CONTENT_BG = new Color(71, 118, 139);     // #47768B Slate-blue content area
    public static final Color BTN_SAVE = new Color(0, 122, 20);         // #007A14 Dark green
    public static final Color BTN_EXIT = new Color(155, 0, 0);          // #9B0000 Dark red
    public static final Color FIELD_BTN_BG = new Color(19, 78, 94);     // #134E5E Dark teal button bg
    public static final Color FIELD_BTN_HOVER = new Color(25, 100, 120);

    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final STemplateDAO templateDAO = new STemplateDAO();
    private final Runnable backAction;
    private SystemConfig currentConfig;

    // CardLayout switcher
    private JPanel pnlCardContainer;
    private CardLayout cardLayout;
    private final List<JButton> sidebarButtons = new ArrayList<>();
    private String currentActiveTab = "Thông tin chung";

    // Tab 1: Thông tin chung
    private JTextField txtTenCongTy;
    private JTextField txtDiaChi;
    private JTextField txtSoDienThoai;
    private JTextField txtEmail;
    private JTextField txtLoiCamOn;
    private JTextField txtFax;
    private JTextField txtMauHoaDon;
    private JCheckBox chkLuaChonMauKhiIn;
    private JCheckBox chkHienThiTruocKhiIn;
    private JLabel lblLogoPreview;
    private String selectedLogoPath;

    // Tab 2: Bán hàng
    private JCheckBox chkChoPhepTrungTenKhachHang;
    private JCheckBox chkChoPhepNhapGiamGia;
    private JTextField txtMacDinhGiamGiaPt;
    private JCheckBox chkBanHangDungDauDocMaVach;
    private JTextField txtSoLanIn;
    private JCheckBox chkHienThiCuaSoNhapSoLuong;
    private JCheckBox chkChoPhepThayDoiNgay;
    private JCheckBox chkCoPhiVanChuyen;
    private JCheckBox chkCoThueSuat;
    private JTextField txtMacDinhThueSuat;
    private JCheckBox chkBatBuocNhapKhachHang;
    private JCheckBox chkBatBuocNhapNVBH;
    private JCheckBox chkChoPhepInTamTinh;
    private JCheckBox chkBatBuocDoiTraTheoDon;
    private JTextField txtSoNgayChoPhepDoiTra;
    private JCheckBox chkHienThiAnhSanPham;
    private JCheckBox chkKichHoatKhuyenMaiTuDong;
    private JCheckBox chkHienThiMatHangKhuyenMaiKhiThanhToan;
    private JCheckBox chkCoGiaoHang;
    private JCheckBox chkSuDungGiaTheoKhach;
    private JCheckBox chkSuDungGiamGiaMacDinh;
    private JTextField txtLamTronTien;

    // Tab 3: Thanh toán
    private JCheckBox chkCoThanhToanVoucher;
    private JCheckBox chkChoPhepKhachNo;
    private JCheckBox chkLuaChonVoucherTuDanhSach;
    private JCheckBox chkSuDungTheTraTruoc;
    private JCheckBox chkSuDungDiemTichLuyDeThanhToan;
    private JTextField txtQuyDoi1DiemSangTien;
    private JCheckBox chkCoThanhToanThe;
    private JCheckBox chkCoThanhToanChuyenKhoan;

    // Tab 4: Tích điểm
    private JTextField txtDoanhSoTuongUng1Diem;
    private JComboBox<String> cbCachTinhDiem;
    private JCheckBox chkTuDongNangCapThanhVien;
    private JCheckBox chkHienThiDiemKhachHang;
    private JCheckBox chkKhongTichDiemNeuCoKhuyenMai;

    // Tab 5: Kho hàng
    private JCheckBox chkChoPhepNhapMotMatHangNhieuLan;
    private JCheckBox chkBatBuocChonNhanVienNhapKho;
    private JCheckBox chkBatBuocChonNhaCungCapNhapKho;
    private JCheckBox chkSuDung2DonViTinh;
    private JCheckBox chkTuDongTinhGiaVon;
    private JCheckBox chkNhapKhoBangDauDocMaVach;
    private JComboBox<String> cbGiaSuDungKhiChuyenXuatKho;
    private JCheckBox chkSuDungChucNangSanXuat;

    // Tab 6: Mặt hàng
    private JCheckBox chkSuDungMaSanCo;
    private JCheckBox chkSuDungMaHang;
    private JCheckBox chkTuDongSinhMaVach;
    private JTextField txtChieuDaiMaVach;
    private JCheckBox chkCoKyTuKiemTra;
    private JCheckBox chkSuDungTemInBangCanDienTu;
    private JTextField txtChuoiBatDauCanDienTu;
    private JComboBox<String> cbTimTheo;
    private JComboBox<String> cbCachTim;
    private JCheckBox chkChoPhepTrungTenMatHang;
    private JCheckBox chkHangCoBaoHanh;
    private JCheckBox chkSuDungHangSanXuat;
    private JCheckBox chkSuDungGia2;
    private JTextField txtDienGiaiGia2;
    private JCheckBox chkSuDungGia3;
    private JTextField txtDienGiaiGia3;
    private JCheckBox chkSuDungGia4;
    private JTextField txtDienGiaiGia4;
    private JCheckBox chkHienThiDeLuaChonNeuCoNhieuGia;
    private JComboBox<String> cbSapXepThuTuTheo;
    private JCheckBox chkMatHangCoHanSuDung;
    private JCheckBox chkMatHangCoKichThuoc;
    private JCheckBox chkMatHangCoVatTuDinhLuong;

    // Tab 7: Cây hiển thị giá
    private JCheckBox chkCoCayHienThiGia;
    private JCheckBox chkCayHienThiGiaMotDong;
    private JTextField txtCongCayHienThiGia;
    private JCheckBox chkCoManHinhPhu;
    private JLabel lblLogoManHinhPhuPreview;
    private String selectedLogoManHinhPhuPath;
    private JCheckBox chkManHinhPhuSuDungGiaoDienThuong;

    // Tab 8: Cân điện tử
    private JCheckBox chkKichHoatSuDungCanDienTu;
    private JTextField txtCongComCanDienTu;
    private JComboBox<String> cbBaudRateCanDienTu;
    private JTextField txtDatabitCanDienTu;
    private JComboBox<String> cbParityCanDienTu;
    private JComboBox<String> cbStopBitsCanDienTu;
    private JTextField txtTimeoutCanDienTu;
    private JTextField txtViTriCanDienTu;
    private JTextField txtDoDaiCanDienTu;
    private JCheckBox chkTuDongNhanDangCanDienTu;
    private JComboBox<String> cbDonViTrenCan;

    // Tab 9: Cảnh báo
    private JCheckBox chkHienThiCanhBao;
    private JCheckBox chkThongBaoHangDuoiMucAnToan;
    private JCheckBox chkThongBaoKhachDenNgaySinhNhat;
    private JCheckBox chkThongBaoHangHetHanDung;
    private JCheckBox chkThongBaoHangSapHetHanDung;
    private JTextField txtThongBaoTruocNgay;

    // Tab 10: Bảo mật
    private JCheckBox chkBatBuocXacNhanHuyMon;
    private JCheckBox chkYeuCauMatKhauGiamGia;
    private JCheckBox chkTuDongKhoaManHinh;
    private JTextField txtThoiGianKhoaManHinh;
    private JCheckBox chkGhiLogDieuChinh;

    // Tab 11: Cảm ứng
    private JCheckBox chkInPhaCheKhiInHoaDon;
    private JComboBox<String> cbMauInPhaChe;
    private JComboBox<String> cbMayInPhaChe;
    private JCheckBox chkInPhaCheKhiLuuTam;
    private JComboBox<String> cbMauInPhaCheLuuTam;

    // Tab 12: Quản trị
    private JCheckBox chkPhieuGanNhatNamPhiaDuoi;
    private JComboBox<String> cbPhimNhapLieuTrenLuoi;
    private JCheckBox chkTuDongSaoLuuDuLieu;
    private JTextField txtDuongDanSaoLuu;
    private JTextField txtSoNgaySaoLuu;
    private JComboBox<String> cbChucNangMacDinh;
    private JCheckBox chkLocDuLieuBoKhoangTrong;
    private JCheckBox chkHienThiXuongDongNeuDoRongCotNho;
    private JComboBox<String> cbDoToChuHienThi;
    private JComboBox<String> cbGiaoDienChuongTrinh;
    private JCheckBox chkTuDongTaiLaiDuLieuKhiChuyenTab;
    private JCheckBox chkKichHoatLuuVetHoatDong;
    private JCheckBox chkHeThongChayNhieuMayTram;

    private static final String[] TAB_NAMES = {
            "Thông tin chung",
            "Bán hàng",
            "Thanh toán",
            "Tích điểm",
            "Kho hàng",
            "Mặt hàng",
            "Cây hiển thị giá",
            "Cân điện tử",
            "Cảnh báo",
            "Bảo mật",
            "Cảm ứng",
            "Quản trị"
    };

    public TouchCauHinhToanHeThongPanel(Runnable backAction) {
        this.backAction = backAction;
        this.currentConfig = configDAO.loadConfig();
        if (this.currentConfig == null) {
            this.currentConfig = new SystemConfig();
        }

        setLayout(new BorderLayout(0, 8));
        setBackground(BG_FRAME);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        initUI();
        loadConfigToUI();
    }

    private void initUI() {
        // 1. Top Header Banner
        add(createHeaderBanner(), BorderLayout.NORTH);

        // 2. Center Split (Sidebar Left + Card Container Right)
        JPanel pnlCenter = new JPanel(new BorderLayout(8, 0));
        pnlCenter.setOpaque(false);

        // Sidebar
        pnlCenter.add(createSidebarPanel(), BorderLayout.WEST);

        // Content Area with CardLayout
        cardLayout = new CardLayout();
        pnlCardContainer = new JPanel(cardLayout);
        pnlCardContainer.setBackground(CONTENT_BG);
        pnlCardContainer.setBorder(new LineBorder(new Color(40, 80, 95), 1, true));

        // Build 12 Tabs
        pnlCardContainer.add(createTabThongTinChung(), "Thông tin chung");
        pnlCardContainer.add(createTabBanHang(), "Bán hàng");
        pnlCardContainer.add(createTabThanhToan(), "Thanh toán");
        pnlCardContainer.add(createTabTichDiem(), "Tích điểm");
        pnlCardContainer.add(createTabKhoHang(), "Kho hàng");
        pnlCardContainer.add(createTabMatHang(), "Mặt hàng");
        pnlCardContainer.add(createTabCayHienThiGia(), "Cây hiển thị giá");
        pnlCardContainer.add(createTabCanDienTu(), "Cân điện tử");
        pnlCardContainer.add(createTabCanhBao(), "Cảnh báo");
        pnlCardContainer.add(createTabBaoMat(), "Bảo mật");
        pnlCardContainer.add(createTabCamUng(), "Cảm ứng");
        pnlCardContainer.add(createTabQuanTri(), "Quản trị");

        pnlCenter.add(pnlCardContainer, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Footer Bar
        add(createFooterBar(), BorderLayout.SOUTH);
    }

    /** Top Header Banner matching screenshot */
    private JPanel createHeaderBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(HEADER_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(0, 36));
        banner.setLayout(new GridBagLayout());

        JLabel lblTitle = new JLabel("THIẾT LẬP CẤU HÌNH HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        banner.add(lblTitle);

        return banner;
    }

    /** Left Sidebar containing 12 tab buttons */
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(12, 1, 0, 5));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(175, 0));

        sidebarButtons.clear();
        for (String name : TAB_NAMES) {
            JButton btn = createSidebarButton(name);
            sidebarButtons.add(btn);
            sidebar.add(btn);
        }

        updateSidebarButtons();
        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = text.equals(currentActiveTab);
                g2.setColor(isActive ? SIDEBAR_BTN_ACTIVE : SIDEBAR_BTN_NORMAL);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 10, 0, 4));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> switchTab(text));
        return btn;
    }

    public void switchTab(String tabName) {
        currentActiveTab = tabName;
        cardLayout.show(pnlCardContainer, tabName);
        updateSidebarButtons();
    }

    private void updateSidebarButtons() {
        for (JButton b : sidebarButtons) {
            b.repaint();
        }
    }

    /** Footer Bar with [CẬP NHẬT] and [THOÁT] */
    private JPanel createFooterBar() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        footer.setOpaque(false);
        footer.setPreferredSize(new Dimension(0, 46));

        JButton btnSave = new JButton("CẬP NHẬT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_SAVE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setForeground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(110, 36));
        btnSave.setOpaque(false);
        btnSave.setContentAreaFilled(false);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> onSave());

        JButton btnExit = new JButton("THOÁT") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_EXIT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExit.setForeground(Color.WHITE);
        btnExit.setPreferredSize(new Dimension(100, 36));
        btnExit.setOpaque(false);
        btnExit.setContentAreaFilled(false);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> {
            if (backAction != null) backAction.run();
        });

        footer.add(btnSave);
        footer.add(btnExit);
        return footer;
    }

    // ==================== TAB 1: THÔNG TIN CHUNG ====================
    private JComponent createTabThongTinChung() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int lblX = 20;
        int lblW = 100;
        int fieldX = 125;
        int fieldW = 460;
        int fieldH = 30;
        int y = 14;
        int gap = 38;

        // 1. Tên công ty
        JLabel lblTen = createWhiteLabel("Tên công ty:", lblX, y, lblW, fieldH);
        pnl.add(lblTen);
        txtTenCongTy = createStyledTextField();
        JPanel rowTen = wrapWithKeyboardButton(txtTenCongTy, "Tên công ty", fieldW, fieldH);
        rowTen.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowTen);

        y += 28;
        JLabel lblNoteTen = new JLabel("(Tên công ty sẽ hiển thị dưới báo cáo)");
        lblNoteTen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNoteTen.setForeground(new Color(210, 230, 240));
        lblNoteTen.setBounds(fieldX, y, 350, 18);
        pnl.add(lblNoteTen);

        // 2. Địa chỉ & Logo bên phải
        y += 22;
        JLabel lblDiaChi = createWhiteLabel("Địa chỉ:", lblX, y, lblW, fieldH);
        pnl.add(lblDiaChi);
        txtDiaChi = createStyledTextField();
        JPanel rowDiaChi = wrapWithKeyboardButton(txtDiaChi, "Địa chỉ", fieldW, fieldH);
        rowDiaChi.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowDiaChi);

        // Logo on right side
        int logoX = fieldX + fieldW + 20;
        int logoY = y - 4;
        JLabel lblLogo = createWhiteLabel("Logo:", logoX, logoY, 45, 24);
        pnl.add(lblLogo);

        JPanel pnlLogoBox = new JPanel(new BorderLayout());
        pnlLogoBox.setBounds(logoX + 45, logoY, 95, 75);
        pnlLogoBox.setBackground(Color.WHITE);
        pnlLogoBox.setBorder(new LineBorder(new Color(20, 60, 80), 1));
        lblLogoPreview = new JLabel("", SwingConstants.CENTER);
        lblLogoPreview.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLogoPreview.setForeground(Color.GRAY);
        pnlLogoBox.add(lblLogoPreview, BorderLayout.CENTER);
        pnl.add(pnlLogoBox);

        JButton btnChonLogo = createTealButton("CHỌN", 70, 26);
        btnChonLogo.setBounds(logoX + 57, logoY + 80, 70, 26);
        btnChonLogo.addActionListener(e -> chooseLogo());
        pnl.add(btnChonLogo);

        // 3. Số điện thoại
        y += gap;
        JLabel lblPhone = createWhiteLabel("Số điện thoại:", lblX, y, lblW, fieldH);
        pnl.add(lblPhone);
        txtSoDienThoai = createStyledTextField();
        JPanel rowPhone = wrapWithKeyboardButton(txtSoDienThoai, "Số điện thoại", fieldW, fieldH);
        rowPhone.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowPhone);

        // 4. Email
        y += gap;
        JLabel lblEmail = createWhiteLabel("Email:", lblX, y, lblW, fieldH);
        pnl.add(lblEmail);
        txtEmail = createStyledTextField();
        JPanel rowEmail = wrapWithKeyboardButton(txtEmail, "Email", fieldW, fieldH);
        rowEmail.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowEmail);

        // 5. Lời cảm ơn
        y += gap;
        JLabel lblThanks = createWhiteLabel("Lời cảm ơn:", lblX, y, lblW, fieldH);
        pnl.add(lblThanks);
        txtLoiCamOn = createStyledTextField();
        JPanel rowThanks = wrapWithKeyboardButton(txtLoiCamOn, "Lời cảm ơn", fieldW, fieldH);
        rowThanks.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowThanks);

        // 6. Fax
        y += gap;
        JLabel lblFax = createWhiteLabel("Fax:", lblX, y, lblW, fieldH);
        pnl.add(lblFax);
        txtFax = createStyledTextField();
        JPanel rowFax = wrapWithKeyboardButton(txtFax, "Fax", fieldW, fieldH);
        rowFax.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowFax);

        // 7. Mẫu hóa đơn
        y += gap;
        JLabel lblMauHD = createWhiteLabel("Mẫu hóa đơn:", lblX, y, lblW, fieldH);
        pnl.add(lblMauHD);
        txtMauHoaDon = createStyledTextField();
        txtMauHoaDon.setText("Mẫu in bill 80mm");
        JPanel rowMauHD = wrapWithSearchButton(txtMauHoaDon, "Mẫu hóa đơn", fieldW, fieldH);
        rowMauHD.setBounds(fieldX, y, fieldW, fieldH);
        pnl.add(rowMauHD);

        // Checkboxes below
        y += gap;
        chkLuaChonMauKhiIn = createTouchCheckBox("Lựa chọn mẫu khi in", true);
        chkLuaChonMauKhiIn.setBounds(fieldX, y, 260, 26);
        pnl.add(chkLuaChonMauKhiIn);

        y += 30;
        chkHienThiTruocKhiIn = createTouchCheckBox("Hiển thị trước khi in", true);
        chkHienThiTruocKhiIn.setBounds(fieldX, y, 260, 26);
        pnl.add(chkHienThiTruocKhiIn);

        pnl.setPreferredSize(new Dimension(800, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 2: BÁN HÀNG ====================
    private JComponent createTabBanHang() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int rightX = 420;
        int y = 12;
        int rowH = 34;

        // Row 1: [X] Cho phép trùng tên khách hàng
        chkChoPhepTrungTenKhachHang = createTouchCheckBox("Cho phép trùng tên khách hàng", true);
        chkChoPhepTrungTenKhachHang.setBounds(leftX, y, 380, 26);
        pnl.add(chkChoPhepTrungTenKhachHang);

        // Row 2: [X] Cho phép nhập giảm giá | Mặc định giảm giá (%) [ 0 ] [🖩]
        y += rowH;
        chkChoPhepNhapGiamGia = createTouchCheckBox("Cho phép nhập giảm giá", true);
        chkChoPhepNhapGiamGia.setBounds(leftX, y, 320, 26);
        pnl.add(chkChoPhepNhapGiamGia);

        JLabel lblGiamGia = createWhiteLabel("Mặc định giảm giá (%)", rightX, y, 160, 26);
        pnl.add(lblGiamGia);
        txtMacDinhGiamGiaPt = createNumericTextField("0");
        JPanel rowGiamGia = wrapWithCalculatorButton(txtMacDinhGiamGiaPt, "Mặc định giảm giá (%)", 100, 28);
        rowGiamGia.setBounds(rightX + 165, y, 100, 28);
        pnl.add(rowGiamGia);

        // Row 3: [X] Bán hàng dùng đầu đọc mã vạch
        y += rowH;
        chkBanHangDungDauDocMaVach = createTouchCheckBox("Bán hàng dùng đầu đọc mã vạch", true);
        chkBanHangDungDauDocMaVach.setBounds(leftX, y, 380, 26);
        pnl.add(chkBanHangDungDauDocMaVach);

        // Row 4: Số lần in [ 1 ] [🖩]
        y += rowH;
        JLabel lblSoLanIn = createWhiteLabel("Số lần in", leftX, y, 120, 26);
        pnl.add(lblSoLanIn);
        txtSoLanIn = createNumericTextField("1");
        JPanel rowSoLanIn = wrapWithCalculatorButton(txtSoLanIn, "Số lần in", 100, 28);
        rowSoLanIn.setBounds(leftX + 125, y, 100, 28);
        pnl.add(rowSoLanIn);

        // Row 5: [ ] Hiển thị cửa sổ nhập số lượng khi quét mã vạch
        y += rowH;
        chkHienThiCuaSoNhapSoLuong = createTouchCheckBox("Hiển thị cửa sổ nhập số lượng khi quét mã vạch", false);
        chkHienThiCuaSoNhapSoLuong.setBounds(leftX, y, 400, 26);
        pnl.add(chkHienThiCuaSoNhapSoLuong);

        // Row 6: [X] Cho phép thay đổi ngày trên hóa đơn
        y += rowH;
        chkChoPhepThayDoiNgay = createTouchCheckBox("Cho phép thay đổi ngày trên hóa đơn", true);
        chkChoPhepThayDoiNgay.setBounds(leftX, y, 380, 26);
        pnl.add(chkChoPhepThayDoiNgay);

        // Row 7: [ ] Có phí vận chuyển | [ ] Có thuế suất
        y += rowH;
        chkCoPhiVanChuyen = createTouchCheckBox("Có phí vận chuyển", false);
        chkCoPhiVanChuyen.setBounds(leftX, y, 300, 26);
        pnl.add(chkCoPhiVanChuyen);

        chkCoThueSuat = createTouchCheckBox("Có thuế suất", false);
        chkCoThueSuat.setBounds(rightX, y, 250, 26);
        pnl.add(chkCoThueSuat);

        // Row 8: Mặc định thuế suất [ 0 ] [🖩]
        y += rowH;
        JLabel lblThueSuat = createWhiteLabel("Mặc định thuế suất", leftX, y, 120, 26);
        pnl.add(lblThueSuat);
        txtMacDinhThueSuat = createNumericTextField("0");
        JPanel rowThue = wrapWithCalculatorButton(txtMacDinhThueSuat, "Mặc định thuế suất", 100, 28);
        rowThue.setBounds(leftX + 125, y, 100, 28);
        pnl.add(rowThue);

        // Row 9: [ ] Bắt buộc nhập khách hàng
        y += rowH;
        chkBatBuocNhapKhachHang = createTouchCheckBox("Bắt buộc nhập khách hàng", false);
        chkBatBuocNhapKhachHang.setBounds(leftX, y, 380, 26);
        pnl.add(chkBatBuocNhapKhachHang);

        // Row 10: [ ] Bắt buộc nhập nhân viên bán hàng
        y += rowH;
        chkBatBuocNhapNVBH = createTouchCheckBox("Bắt buộc nhập nhân viên bán hàng", false);
        chkBatBuocNhapNVBH.setBounds(leftX, y, 380, 26);
        pnl.add(chkBatBuocNhapNVBH);

        // Row 11: [X] Cho phép in tạm tính
        y += rowH;
        chkChoPhepInTamTinh = createTouchCheckBox("Cho phép in tạm tính", true);
        chkChoPhepInTamTinh.setBounds(leftX, y, 380, 26);
        pnl.add(chkChoPhepInTamTinh);

        // Row 12: [X] Bắt buộc đổi trả hàng theo đơn | Số ngày cho phép đổi trả [ 7 ] [🖩]
        y += rowH;
        chkBatBuocDoiTraTheoDon = createTouchCheckBox("Bắt buộc đổi trả hàng theo đơn", true);
        chkBatBuocDoiTraTheoDon.setBounds(leftX, y, 320, 26);
        pnl.add(chkBatBuocDoiTraTheoDon);

        JLabel lblSoNgayDoiTra = createWhiteLabel("Số ngày cho phép đổi trả", rightX, y, 160, 26);
        pnl.add(lblSoNgayDoiTra);
        txtSoNgayChoPhepDoiTra = createNumericTextField("7");
        JPanel rowDoiTra = wrapWithCalculatorButton(txtSoNgayChoPhepDoiTra, "Số ngày đổi trả", 100, 28);
        rowDoiTra.setBounds(rightX + 165, y, 100, 28);
        pnl.add(rowDoiTra);

        // Row 13: [X] Hiển thị ảnh sản phẩm trong giao diện bán hàng
        y += rowH;
        chkHienThiAnhSanPham = createTouchCheckBox("Hiển thị ảnh sản phẩm trong giao diện bán hàng", true);
        chkHienThiAnhSanPham.setBounds(leftX, y, 420, 26);
        pnl.add(chkHienThiAnhSanPham);

        // Row 14: [X] Kích hoạt khuyến mại tự động
        y += rowH;
        chkKichHoatKhuyenMaiTuDong = createTouchCheckBox("Kích hoạt khuyến mại tự động", true);
        chkKichHoatKhuyenMaiTuDong.setBounds(leftX, y, 420, 26);
        pnl.add(chkKichHoatKhuyenMaiTuDong);

        // Row 15: [X] Hiển thị mặt hàng khuyến mại khi thanh toán
        y += rowH;
        chkHienThiMatHangKhuyenMaiKhiThanhToan = createTouchCheckBox("Hiển thị mặt hàng khuyến mại khi thanh toán", true);
        chkHienThiMatHangKhuyenMaiKhiThanhToan.setBounds(leftX, y, 420, 26);
        pnl.add(chkHienThiMatHangKhuyenMaiKhiThanhToan);

        // Row 16: [ ] Có giao hàng
        y += rowH;
        chkCoGiaoHang = createTouchCheckBox("Có giao hàng", false);
        chkCoGiaoHang.setBounds(leftX, y, 380, 26);
        pnl.add(chkCoGiaoHang);

        // Row 17: [ ] Sử dụng giá theo khách
        y += rowH;
        chkSuDungGiaTheoKhach = createTouchCheckBox("Sử dụng giá theo khách", false);
        chkSuDungGiaTheoKhach.setBounds(leftX, y, 380, 26);
        pnl.add(chkSuDungGiaTheoKhach);

        // Row 18: [X] Sử dụng giảm giá mặc định theo mặt hàng
        y += rowH;
        chkSuDungGiamGiaMacDinh = createTouchCheckBox("Sử dụng giảm giá mặc định theo mặt hàng", true);
        chkSuDungGiamGiaMacDinh.setBounds(leftX, y, 420, 26);
        pnl.add(chkSuDungGiamGiaMacDinh);

        // Row 19: Làm tròn tiền [ 1,000 ] [🖩]
        y += rowH;
        JLabel lblLamTron = createWhiteLabel("Làm tròn tiền", leftX, y, 120, 26);
        pnl.add(lblLamTron);
        txtLamTronTien = createNumericTextField("1,000");
        JPanel rowLamTron = wrapWithCalculatorButton(txtLamTronTien, "Làm tròn tiền", 120, 28);
        rowLamTron.setBounds(leftX + 125, y, 120, 28);
        pnl.add(rowLamTron);

        pnl.setPreferredSize(new Dimension(780, y + 45));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 3: THANH TOÁN ====================
    private JComponent createTabThanhToan() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 34;

        chkCoThanhToanVoucher = createTouchCheckBox("Có thanh toán voucher", false);
        chkCoThanhToanVoucher.setBounds(leftX, y, 380, 26);
        pnl.add(chkCoThanhToanVoucher);

        y += gap;
        chkChoPhepKhachNo = createTouchCheckBox("Cho phép khách nợ", false);
        chkChoPhepKhachNo.setBounds(leftX, y, 380, 26);
        pnl.add(chkChoPhepKhachNo);

        y += gap;
        chkLuaChonVoucherTuDanhSach = createTouchCheckBox("Lựa chọn voucher từ danh sách", false);
        chkLuaChonVoucherTuDanhSach.setBounds(leftX, y, 380, 26);
        pnl.add(chkLuaChonVoucherTuDanhSach);

        y += gap;
        chkSuDungTheTraTruoc = createTouchCheckBox("Sử dụng thẻ trả trước", false);
        chkSuDungTheTraTruoc.setBounds(leftX, y, 380, 26);
        pnl.add(chkSuDungTheTraTruoc);

        y += gap;
        chkSuDungDiemTichLuyDeThanhToan = createTouchCheckBox("Sử dụng điểm tích lũy để thanh toán", false);
        chkSuDungDiemTichLuyDeThanhToan.setBounds(leftX, y, 380, 26);
        pnl.add(chkSuDungDiemTichLuyDeThanhToan);

        y += gap;
        JLabel lblQuyDoi = createWhiteLabel("Quy đổi 1 điểm sang tiền", leftX, y, 180, 26);
        pnl.add(lblQuyDoi);
        txtQuyDoi1DiemSangTien = createNumericTextField("1,000");
        JPanel rowQuyDoi = wrapWithCalculatorButton(txtQuyDoi1DiemSangTien, "Quy đổi 1 điểm sang tiền", 120, 28);
        rowQuyDoi.setBounds(leftX + 190, y, 120, 28);
        pnl.add(rowQuyDoi);

        y += gap;
        chkCoThanhToanThe = createTouchCheckBox("Có thanh toán thẻ", true);
        chkCoThanhToanThe.setBounds(leftX, y, 380, 26);
        pnl.add(chkCoThanhToanThe);

        y += gap;
        chkCoThanhToanChuyenKhoan = createTouchCheckBox("Có thanh toán chuyển khoản", false);
        chkCoThanhToanChuyenKhoan.setBounds(leftX, y, 380, 26);
        pnl.add(chkCoThanhToanChuyenKhoan);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 4: TÍCH ĐIỂM ====================
    private JComponent createTabTichDiem() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 36;

        JLabel lblDoanhSo = createWhiteLabel("Doanh số tương ứng với 1 điểm", leftX, y, 220, 26);
        pnl.add(lblDoanhSo);
        txtDoanhSoTuongUng1Diem = createNumericTextField("20,000");
        JPanel rowDoanhSo = wrapWithCalculatorButton(txtDoanhSoTuongUng1Diem, "Doanh số 1 điểm", 130, 28);
        rowDoanhSo.setBounds(leftX + 230, y, 130, 28);
        pnl.add(rowDoanhSo);

        y += gap;
        JLabel lblCachTinh = createWhiteLabel("Cách tính điểm", leftX, y, 220, 26);
        pnl.add(lblCachTinh);
        cbCachTinhDiem = createStyledComboBox(new String[]{
                "Điểm được tính trên từng hóa đơn",
                "Điểm được tính trên tổng doanh số"
        });
        cbCachTinhDiem.setBounds(leftX + 230, y, 320, 28);
        pnl.add(cbCachTinhDiem);

        y += gap;
        chkTuDongNangCapThanhVien = createTouchCheckBox("Tự động nâng cấp thành viên khi đạt hạn mức", true);
        chkTuDongNangCapThanhVien.setBounds(leftX, y, 420, 26);
        pnl.add(chkTuDongNangCapThanhVien);

        y += gap;
        chkHienThiDiemKhachHang = createTouchCheckBox("Hiển thị điểm của khách hàng trên hóa đơn", true);
        chkHienThiDiemKhachHang.setBounds(leftX, y, 420, 26);
        pnl.add(chkHienThiDiemKhachHang);

        y += gap;
        chkKhongTichDiemNeuCoKhuyenMai = createTouchCheckBox("Không tích điểm nếu có khuyến mại", false);
        chkKhongTichDiemNeuCoKhuyenMai.setBounds(leftX, y, 420, 26);
        pnl.add(chkKhongTichDiemNeuCoKhuyenMai);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 5: KHO HÀNG ====================
    private JComponent createTabKhoHang() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 34;

        chkChoPhepNhapMotMatHangNhieuLan = createTouchCheckBox("Cho phép nhập một mặt hàng nhiều lần trong phiếu", false);
        chkChoPhepNhapMotMatHangNhieuLan.setBounds(leftX, y, 450, 26);
        pnl.add(chkChoPhepNhapMotMatHangNhieuLan);

        y += gap;
        chkBatBuocChonNhanVienNhapKho = createTouchCheckBox("Bắt buộc chọn nhân viên trong nhập kho", false);
        chkBatBuocChonNhanVienNhapKho.setBounds(leftX, y, 450, 26);
        pnl.add(chkBatBuocChonNhanVienNhapKho);

        y += gap;
        chkBatBuocChonNhaCungCapNhapKho = createTouchCheckBox("Bắt buộc chọn nhà cung cấp trong nhập kho", false);
        chkBatBuocChonNhaCungCapNhapKho.setBounds(leftX, y, 450, 26);
        pnl.add(chkBatBuocChonNhaCungCapNhapKho);

        y += gap;
        chkSuDung2DonViTinh = createTouchCheckBox("Sử dụng 2 đơn vị tính", false);
        chkSuDung2DonViTinh.setBounds(leftX, y, 450, 26);
        pnl.add(chkSuDung2DonViTinh);

        y += gap;
        chkTuDongTinhGiaVon = createTouchCheckBox("Tự động tính giá vốn", true);
        chkTuDongTinhGiaVon.setBounds(leftX, y, 450, 26);
        pnl.add(chkTuDongTinhGiaVon);

        y += gap;
        chkNhapKhoBangDauDocMaVach = createTouchCheckBox("Nhập kho bằng đầu đọc mã vạch", true);
        chkNhapKhoBangDauDocMaVach.setBounds(leftX, y, 450, 26);
        pnl.add(chkNhapKhoBangDauDocMaVach);

        y += gap;
        JLabel lblGiaXuat = createWhiteLabel("Giá sử dụng khi chuyển xuất kho", leftX, y, 220, 26);
        pnl.add(lblGiaXuat);
        cbGiaSuDungKhiChuyenXuatKho = createStyledComboBox(new String[]{"Giá vốn", "Giá bán", "Giá nhập"});
        cbGiaSuDungKhiChuyenXuatKho.setBounds(leftX + 230, y, 220, 28);
        pnl.add(cbGiaSuDungKhiChuyenXuatKho);

        y += gap;
        chkSuDungChucNangSanXuat = createTouchCheckBox("Sử dụng chức năng sản xuất", false);
        chkSuDungChucNangSanXuat.setBounds(leftX, y, 450, 26);
        pnl.add(chkSuDungChucNangSanXuat);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 6: MẶT HÀNG ====================
    private JComponent createTabMatHang() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int rightX = 380;
        int y = 14;
        int gap = 32;

        chkSuDungMaSanCo = createTouchCheckBox("Sử dụng mã sẵn có", false);
        chkSuDungMaSanCo.setBounds(leftX, y, 250, 26);
        pnl.add(chkSuDungMaSanCo);

        chkSuDungMaHang = createTouchCheckBox("Sử dụng mã hàng", true);
        chkSuDungMaHang.setBounds(rightX, y, 250, 26);
        pnl.add(chkSuDungMaHang);

        y += gap;
        chkTuDongSinhMaVach = createTouchCheckBox("Tự động sinh mã vạch", true);
        chkTuDongSinhMaVach.setBounds(leftX, y, 250, 26);
        pnl.add(chkTuDongSinhMaVach);

        y += gap;
        JLabel lblChieuDai = createWhiteLabel("Chiều dài mã vạch", leftX + 10, y, 140, 26);
        pnl.add(lblChieuDai);
        txtChieuDaiMaVach = createNumericTextField("7");
        JPanel rowChieuDai = wrapWithCalculatorButton(txtChieuDaiMaVach, "Chiều dài mã vạch", 80, 28);
        rowChieuDai.setBounds(leftX + 155, y, 80, 28);
        pnl.add(rowChieuDai);

        chkCoKyTuKiemTra = createTouchCheckBox("Có ký tự kiểm tra", false);
        chkCoKyTuKiemTra.setBounds(rightX, y, 250, 26);
        pnl.add(chkCoKyTuKiemTra);

        y += gap;
        chkSuDungTemInBangCanDienTu = createTouchCheckBox("Sử dụng tem in bằng cân điện tử", true);
        chkSuDungTemInBangCanDienTu.setBounds(leftX, y, 260, 26);
        pnl.add(chkSuDungTemInBangCanDienTu);

        JLabel lblChuoiBatDau = createWhiteLabel("Chuỗi bắt đầu:", rightX, y, 110, 26);
        pnl.add(lblChuoiBatDau);
        txtChuoiBatDauCanDienTu = createStyledTextField();
        txtChuoiBatDauCanDienTu.setText("29");
        JPanel rowChuoi = wrapWithKeyboardButton(txtChuoiBatDauCanDienTu, "Chuỗi bắt đầu cân", 90, 28);
        rowChuoi.setBounds(rightX + 115, y, 90, 28);
        pnl.add(rowChuoi);

        y += gap;
        JLabel lblTimTheo = createWhiteLabel("Tìm theo", leftX, y, 100, 26);
        pnl.add(lblTimTheo);
        cbTimTheo = createStyledComboBox(new String[]{"Cả tên hàng và mã hàng", "Chỉ theo tên hàng", "Chỉ theo mã hàng"});
        cbTimTheo.setBounds(leftX + 110, y, 200, 28);
        pnl.add(cbTimTheo);

        JLabel lblCachTim = createWhiteLabel("Cách tìm", rightX, y, 80, 26);
        pnl.add(lblCachTim);
        cbCachTim = createStyledComboBox(new String[]{"Tìm có chứa", "Tìm bắt đầu bằng", "Tìm chính xác"});
        cbCachTim.setBounds(rightX + 85, y, 180, 28);
        pnl.add(cbCachTim);

        y += gap;
        chkChoPhepTrungTenMatHang = createTouchCheckBox("Cho phép trùng tên mặt hàng", true);
        chkChoPhepTrungTenMatHang.setBounds(leftX, y, 280, 26);
        pnl.add(chkChoPhepTrungTenMatHang);

        chkHangCoBaoHanh = createTouchCheckBox("Hàng có bảo hành", true);
        chkHangCoBaoHanh.setBounds(rightX, y, 250, 26);
        pnl.add(chkHangCoBaoHanh);

        y += gap;
        chkSuDungHangSanXuat = createTouchCheckBox("Sử dụng hàng sản xuất", true);
        chkSuDungHangSanXuat.setBounds(leftX, y, 280, 26);
        pnl.add(chkSuDungHangSanXuat);

        // Giá 2, 3, 4
        y += gap;
        chkSuDungGia2 = createTouchCheckBox("Sử dụng giá 2", false);
        chkSuDungGia2.setBounds(leftX, y, 150, 26);
        pnl.add(chkSuDungGia2);
        txtDienGiaiGia2 = createStyledTextField();
        txtDienGiaiGia2.setText("Đơn giá 2");
        JPanel rowGia2 = wrapWithKeyboardButton(txtDienGiaiGia2, "Diễn giải giá 2", 180, 28);
        rowGia2.setBounds(leftX + 160, y, 180, 28);
        pnl.add(rowGia2);

        y += gap;
        chkSuDungGia3 = createTouchCheckBox("Sử dụng giá 3", false);
        chkSuDungGia3.setBounds(leftX, y, 150, 26);
        pnl.add(chkSuDungGia3);
        txtDienGiaiGia3 = createStyledTextField();
        txtDienGiaiGia3.setText("Giá bán 3");
        JPanel rowGia3 = wrapWithKeyboardButton(txtDienGiaiGia3, "Diễn giải giá 3", 180, 28);
        rowGia3.setBounds(leftX + 160, y, 180, 28);
        pnl.add(rowGia3);

        y += gap;
        chkSuDungGia4 = createTouchCheckBox("Sử dụng giá 4", false);
        chkSuDungGia4.setBounds(leftX, y, 150, 26);
        pnl.add(chkSuDungGia4);
        txtDienGiaiGia4 = createStyledTextField();
        txtDienGiaiGia4.setText("Giá bán 4");
        JPanel rowGia4 = wrapWithKeyboardButton(txtDienGiaiGia4, "Diễn giải giá 4", 180, 28);
        rowGia4.setBounds(leftX + 160, y, 180, 28);
        pnl.add(rowGia4);

        y += gap;
        chkHienThiDeLuaChonNeuCoNhieuGia = createTouchCheckBox("Hiển thị để lựa chọn nếu có nhiều giá", true);
        chkHienThiDeLuaChonNeuCoNhieuGia.setBounds(leftX, y, 350, 26);
        pnl.add(chkHienThiDeLuaChonNeuCoNhieuGia);

        y += gap;
        JLabel lblSapXep = createWhiteLabel("Sắp xếp thứ tự theo", leftX, y, 150, 26);
        pnl.add(lblSapXep);
        cbSapXepThuTuTheo = createStyledComboBox(new String[]{"Mã hàng", "Tên hàng", "Nhóm hàng"});
        cbSapXepThuTuTheo.setBounds(leftX + 160, y, 180, 28);
        pnl.add(cbSapXepThuTuTheo);

        y += gap;
        chkMatHangCoHanSuDung = createTouchCheckBox("Mặt hàng có hạn sử dụng", false);
        chkMatHangCoHanSuDung.setBounds(leftX, y, 280, 26);
        pnl.add(chkMatHangCoHanSuDung);

        chkMatHangCoKichThuoc = createTouchCheckBox("Mặt hàng có kích thước", false);
        chkMatHangCoKichThuoc.setBounds(rightX, y, 250, 26);
        pnl.add(chkMatHangCoKichThuoc);

        y += gap;
        chkMatHangCoVatTuDinhLuong = createTouchCheckBox("Mặt hàng có vật tư định lượng", true);
        chkMatHangCoVatTuDinhLuong.setBounds(leftX, y, 350, 26);
        pnl.add(chkMatHangCoVatTuDinhLuong);

        pnl.setPreferredSize(new Dimension(780, y + 45));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 7: CÂY HIỂN THỊ GIÁ ====================
    private JComponent createTabCayHienThiGia() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 34;

        chkCoCayHienThiGia = createTouchCheckBox("Có cây hiển thị giá", false);
        chkCoCayHienThiGia.setBounds(leftX, y, 240, 26);
        pnl.add(chkCoCayHienThiGia);

        chkCayHienThiGiaMotDong = createTouchCheckBox("Cây hiển thị giá 1 dòng", false);
        chkCayHienThiGiaMotDong.setBounds(leftX + 260, y, 240, 26);
        pnl.add(chkCayHienThiGiaMotDong);

        y += gap;
        JLabel lblCong = createWhiteLabel("Cổng sử dụng", leftX, y, 120, 26);
        pnl.add(lblCong);
        txtCongCayHienThiGia = createStyledTextField();
        JPanel rowCong = wrapWithKeyboardButton(txtCongCayHienThiGia, "Cổng sử dụng (ví dụ: COM1)", 250, 28);
        rowCong.setBounds(leftX + 130, y, 250, 28);
        pnl.add(rowCong);

        y += gap;
        chkCoManHinhPhu = createTouchCheckBox("Có màn hình phụ", false);
        chkCoManHinhPhu.setBounds(leftX, y, 240, 26);
        pnl.add(chkCoManHinhPhu);

        y += gap;
        JLabel lblLogoMHP = createWhiteLabel("Logo màn hình phụ:", leftX, y, 160, 26);
        pnl.add(lblLogoMHP);

        JPanel pnlMhpBox = new JPanel(new BorderLayout());
        pnlMhpBox.setBounds(leftX + 170, y, 80, 80);
        pnlMhpBox.setBackground(Color.WHITE);
        pnlMhpBox.setBorder(new LineBorder(new Color(20, 60, 80), 1));
        lblLogoManHinhPhuPreview = new JLabel("", SwingConstants.CENTER);
        pnlMhpBox.add(lblLogoManHinhPhuPreview, BorderLayout.CENTER);
        pnl.add(pnlMhpBox);

        JButton btnChonMhpLogo = createTealButton("CHỌN", 75, 26);
        btnChonMhpLogo.setBounds(leftX + 172, y + 86, 75, 26);
        btnChonMhpLogo.addActionListener(e -> chooseLogoManHinhPhu());
        pnl.add(btnChonMhpLogo);

        y += 120;
        chkManHinhPhuSuDungGiaoDienThuong = createTouchCheckBox("Màn hình phụ sử dụng cả giao diện thường", true);
        chkManHinhPhuSuDungGiaoDienThuong.setBounds(leftX, y, 420, 26);
        pnl.add(chkManHinhPhuSuDungGiaoDienThuong);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 8: CÂN ĐIỆN TỬ ====================
    private JComponent createTabCanDienTu() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int lblW = 140;
        int fieldX = 170;
        int fieldW = 280;
        int y = 14;
        int gap = 32;

        chkKichHoatSuDungCanDienTu = createTouchCheckBox("Kích hoạt sử dụng cân điện tử", false);
        chkKichHoatSuDungCanDienTu.setBounds(leftX, y, 350, 26);
        pnl.add(chkKichHoatSuDungCanDienTu);

        y += gap;
        pnl.add(createWhiteLabel("Cổng COM", leftX, y, lblW, 26));
        txtCongComCanDienTu = createStyledTextField();
        JPanel rowCom = wrapWithKeyboardButton(txtCongComCanDienTu, "Cổng COM", fieldW, 28);
        rowCom.setBounds(fieldX, y, fieldW, 28);
        pnl.add(rowCom);

        y += gap;
        pnl.add(createWhiteLabel("Baud rate", leftX, y, lblW, 26));
        cbBaudRateCanDienTu = createStyledComboBox(new String[]{"1200", "2400", "4800", "9600"});
        cbBaudRateCanDienTu.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbBaudRateCanDienTu);

        y += gap;
        pnl.add(createWhiteLabel("Databit (8)", leftX, y, lblW, 26));
        txtDatabitCanDienTu = createNumericTextField("0");
        JPanel rowDatabit = wrapWithCalculatorButton(txtDatabitCanDienTu, "Databit", 100, 28);
        rowDatabit.setBounds(fieldX, y, 100, 28);
        pnl.add(rowDatabit);

        y += gap;
        pnl.add(createWhiteLabel("Parity", leftX, y, lblW, 26));
        cbParityCanDienTu = createStyledComboBox(new String[]{"None", "Odd", "Even", "Mark", "Space"});
        cbParityCanDienTu.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbParityCanDienTu);

        y += gap;
        pnl.add(createWhiteLabel("Stop bits", leftX, y, lblW, 26));
        cbStopBitsCanDienTu = createStyledComboBox(new String[]{"", "1", "2", "1.5"});
        cbStopBitsCanDienTu.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbStopBitsCanDienTu);

        y += gap;
        pnl.add(createWhiteLabel("Timeout", leftX, y, lblW, 26));
        txtTimeoutCanDienTu = createNumericTextField("0");
        JPanel rowTimeout = wrapWithCalculatorButton(txtTimeoutCanDienTu, "Timeout", 100, 28);
        rowTimeout.setBounds(fieldX, y, 100, 28);
        pnl.add(rowTimeout);

        y += gap;
        pnl.add(createWhiteLabel("Vị trí", leftX, y, lblW, 26));
        txtViTriCanDienTu = createNumericTextField("0");
        JPanel rowViTri = wrapWithCalculatorButton(txtViTriCanDienTu, "Vị trí", 100, 28);
        rowViTri.setBounds(fieldX, y, 100, 28);
        pnl.add(rowViTri);

        y += gap;
        pnl.add(createWhiteLabel("Độ dài", leftX, y, lblW, 26));
        txtDoDaiCanDienTu = createNumericTextField("0");
        JPanel rowDoDai = wrapWithCalculatorButton(txtDoDaiCanDienTu, "Độ dài", 100, 28);
        rowDoDai.setBounds(fieldX, y, 100, 28);
        pnl.add(rowDoDai);

        y += gap;
        chkTuDongNhanDangCanDienTu = createTouchCheckBox("Tự động nhận dạng", false);
        chkTuDongNhanDangCanDienTu.setBounds(leftX, y, 350, 26);
        pnl.add(chkTuDongNhanDangCanDienTu);

        y += gap;
        pnl.add(createWhiteLabel("Đơn vị trên cân", leftX, y, lblW, 26));
        cbDonViTrenCan = createStyledComboBox(new String[]{"Gram", "Kg", "Gr"});
        cbDonViTrenCan.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbDonViTrenCan);

        pnl.setPreferredSize(new Dimension(780, y + 45));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 9: CẢNH BÁO ====================
    private JComponent createTabCanhBao() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 34;

        chkHienThiCanhBao = createTouchCheckBox("Hiển thị cảnh báo", true);
        chkHienThiCanhBao.setBounds(leftX, y, 380, 26);
        pnl.add(chkHienThiCanhBao);

        y += gap;
        chkThongBaoHangDuoiMucAnToan = createTouchCheckBox("Thông báo hàng dưới mức an toàn", false);
        chkThongBaoHangDuoiMucAnToan.setBounds(leftX, y, 380, 26);
        pnl.add(chkThongBaoHangDuoiMucAnToan);

        y += gap;
        chkThongBaoKhachDenNgaySinhNhat = createTouchCheckBox("Thông báo khách đến ngày sinh nhật", false);
        chkThongBaoKhachDenNgaySinhNhat.setBounds(leftX, y, 380, 26);
        pnl.add(chkThongBaoKhachDenNgaySinhNhat);

        y += gap;
        chkThongBaoHangHetHanDung = createTouchCheckBox("Thông báo hàng hết hạn dùng", false);
        chkThongBaoHangHetHanDung.setBounds(leftX, y, 380, 26);
        pnl.add(chkThongBaoHangHetHanDung);

        y += gap;
        chkThongBaoHangSapHetHanDung = createTouchCheckBox("Thông báo hàng sắp hết hạn dùng", false);
        chkThongBaoHangSapHetHanDung.setBounds(leftX, y, 380, 26);
        pnl.add(chkThongBaoHangSapHetHanDung);

        y += gap;
        JLabel lblNgayBao = createWhiteLabel("Thông báo trước (ngày)", leftX, y, 170, 26);
        pnl.add(lblNgayBao);
        txtThongBaoTruocNgay = createNumericTextField("3");
        JPanel rowNgayBao = wrapWithCalculatorButton(txtThongBaoTruocNgay, "Thông báo trước ngày", 100, 28);
        rowNgayBao.setBounds(leftX + 180, y, 100, 28);
        pnl.add(rowNgayBao);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 10: BẢO MẬT ====================
    private JComponent createTabBaoMat() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int y = 15;
        int gap = 36;

        chkBatBuocXacNhanHuyMon = createTouchCheckBox("Bắt buộc xác nhận khi hủy hóa đơn hoặc xóa món", true);
        chkBatBuocXacNhanHuyMon.setBounds(leftX, y, 450, 26);
        pnl.add(chkBatBuocXacNhanHuyMon);

        y += gap;
        chkYeuCauMatKhauGiamGia = createTouchCheckBox("Yêu cầu mật khẩu quản lý khi giảm giá vượt mức cho phép", true);
        chkYeuCauMatKhauGiamGia.setBounds(leftX, y, 450, 26);
        pnl.add(chkYeuCauMatKhauGiamGia);

        y += gap;
        chkTuDongKhoaManHinh = createTouchCheckBox("Tự động khóa màn hình cảm ứng sau thời gian chờ", false);
        chkTuDongKhoaManHinh.setBounds(leftX, y, 450, 26);
        pnl.add(chkTuDongKhoaManHinh);

        y += gap;
        JLabel lblThoiGian = createWhiteLabel("Thời gian khóa tự động (phút)", leftX, y, 220, 26);
        pnl.add(lblThoiGian);
        txtThoiGianKhoaManHinh = createNumericTextField("5");
        JPanel rowKhoa = wrapWithCalculatorButton(txtThoiGianKhoaManHinh, "Thời gian khóa", 100, 28);
        rowKhoa.setBounds(leftX + 230, y, 100, 28);
        pnl.add(rowKhoa);

        y += gap;
        chkGhiLogDieuChinh = createTouchCheckBox("Ghi log nhật ký mọi thao tác điều chỉnh giá và kho", true);
        chkGhiLogDieuChinh.setBounds(leftX, y, 450, 26);
        pnl.add(chkGhiLogDieuChinh);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 11: CẢM ỨNG ====================
    private JComponent createTabCamUng() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int lblW = 180;
        int fieldX = 210;
        int fieldW = 380;
        int y = 15;
        int gap = 34;

        String[] detectedPrinters = getAvailablePrinters();
        List<String> templateNames = templateDAO.getInvoiceTemplateNames();
        if (templateNames == null || templateNames.isEmpty()) {
            templateNames = Arrays.asList(
                    "BIÊN BẢN GIAO NHẬN HÀNG HÓA", "Mẫu 54 mm x 2 dòng", "Mẫu in bill 54mm",
                    "Mẫu in bill 80mm", "Mẫu in chế biến", "Phiếu giao hàng"
            );
        }
        String[] templatesArr = templateNames.toArray(new String[0]);

        chkInPhaCheKhiInHoaDon = createTouchCheckBox("In pha chế khi in hóa đơn", false);
        chkInPhaCheKhiInHoaDon.setBounds(leftX, y, 350, 26);
        pnl.add(chkInPhaCheKhiInHoaDon);

        y += gap;
        pnl.add(createWhiteLabel("Mẫu in pha chế", leftX, y, lblW, 26));
        cbMauInPhaChe = createStyledComboBox(templatesArr);
        cbMauInPhaChe.setSelectedItem("Mẫu in chế biến");
        cbMauInPhaChe.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbMauInPhaChe);

        y += gap;
        pnl.add(createWhiteLabel("Máy in pha chế", leftX, y, lblW, 26));
        cbMayInPhaChe = createStyledComboBox(detectedPrinters);
        cbMayInPhaChe.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbMayInPhaChe);

        y += gap;
        chkInPhaCheKhiLuuTam = createTouchCheckBox("In pha chế khi lưu tạm", false);
        chkInPhaCheKhiLuuTam.setBounds(leftX, y, 350, 26);
        pnl.add(chkInPhaCheKhiLuuTam);

        y += gap;
        pnl.add(createWhiteLabel("Mẫu in pha chế lưu tạm", leftX, y, lblW, 26));
        List<String> tamTemplates = new ArrayList<>();
        tamTemplates.add("");
        tamTemplates.addAll(templateNames);
        cbMauInPhaCheLuuTam = createStyledComboBox(tamTemplates.toArray(new String[0]));
        cbMauInPhaCheLuuTam.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbMauInPhaCheLuuTam);

        pnl.setPreferredSize(new Dimension(780, y + 40));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== TAB 12: QUẢN TRỊ ====================
    private JComponent createTabQuanTri() {
        JPanel pnl = new JPanel(null);
        pnl.setBackground(CONTENT_BG);

        int leftX = 20;
        int lblW = 180;
        int fieldX = 210;
        int fieldW = 380;
        int y = 14;
        int gap = 30;

        chkPhieuGanNhatNamPhiaDuoi = createTouchCheckBox("Phiếu gần nhất nằm phía dưới", false);
        chkPhieuGanNhatNamPhiaDuoi.setBounds(leftX, y, 350, 26);
        pnl.add(chkPhieuGanNhatNamPhiaDuoi);

        y += gap;
        pnl.add(createWhiteLabel("Phím nhập liệu trên lưới", leftX, y, lblW, 26));
        cbPhimNhapLieuTrenLuoi = createStyledComboBox(new String[]{
                "Tab để sang ngang, Enter để xuống dòng",
                "Enter để sang ngang, Tab để xuống dòng",
                "Enter và Tab đều sang ngang"
        });
        cbPhimNhapLieuTrenLuoi.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbPhimNhapLieuTrenLuoi);

        y += gap;
        chkTuDongSaoLuuDuLieu = createTouchCheckBox("Tự động sao lưu dữ liệu", true);
        chkTuDongSaoLuuDuLieu.setBounds(leftX, y, 350, 26);
        pnl.add(chkTuDongSaoLuuDuLieu);

        y += gap;
        pnl.add(createWhiteLabel("Đường dẫn sao lưu", leftX, y, lblW, 26));
        txtDuongDanSaoLuu = createStyledTextField();
        JPanel rowSaoLuu = wrapWithKeyboardButtonAndBrowse(txtDuongDanSaoLuu, "Đường dẫn sao lưu", 250, 28);
        rowSaoLuu.setBounds(fieldX, y, 250, 28);
        pnl.add(rowSaoLuu);

        JLabel lblSoNgay = createWhiteLabel("Số ngày:", fieldX + 265, y, 65, 26);
        pnl.add(lblSoNgay);
        txtSoNgaySaoLuu = createNumericTextField("1");
        JPanel rowNgaySL = wrapWithCalculatorButton(txtSoNgaySaoLuu, "Số ngày sao lưu", 65, 28);
        rowNgaySL.setBounds(fieldX + 335, y, 65, 28);
        pnl.add(rowNgaySL);

        y += gap;
        pnl.add(createWhiteLabel("Chức năng mặc định", leftX, y, lblW, 26));
        cbChucNangMacDinh = createStyledComboBox(new String[]{
                "Hóa đơn bán hàng", "Báo giá", "Đơn hàng", "Phiếu nhập kho", "Phiếu xuất kho", "Danh mục mặt hàng", "Khách hàng"
        });
        cbChucNangMacDinh.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbChucNangMacDinh);

        y += gap;
        chkLocDuLieuBoKhoangTrong = createTouchCheckBox("Lọc dữ liệu bỏ khoảng trống", false);
        chkLocDuLieuBoKhoangTrong.setBounds(leftX, y, 350, 26);
        pnl.add(chkLocDuLieuBoKhoangTrong);

        y += gap;
        chkHienThiXuongDongNeuDoRongCotNho = createTouchCheckBox("Hiển thị xuống dòng nếu độ rộng cột nhỏ", false);
        chkHienThiXuongDongNeuDoRongCotNho.setBounds(leftX, y, 400, 26);
        pnl.add(chkHienThiXuongDongNeuDoRongCotNho);

        y += gap;
        pnl.add(createWhiteLabel("Độ to chữ hiển thị", leftX, y, lblW, 26));
        cbDoToChuHienThi = createStyledComboBox(new String[]{
                "Nhỏ (110%)", "Mặc định (100%)", "Vừa (120%)", "Lớn (130%)"
        });
        cbDoToChuHienThi.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbDoToChuHienThi);

        y += gap;
        pnl.add(createWhiteLabel("Giao diện chương trình", leftX, y, lblW, 26));
        cbGiaoDienChuongTrinh = createStyledComboBox(new String[]{
                "Office 2010 - Blue", "Office 2010 - Black", "Office 2010 - Silver", "Windows Standard", "FlatLaf Light"
        });
        cbGiaoDienChuongTrinh.setBounds(fieldX, y, fieldW, 28);
        pnl.add(cbGiaoDienChuongTrinh);

        y += gap;
        chkTuDongTaiLaiDuLieuKhiChuyenTab = createTouchCheckBox("Tự động tải lại dữ liệu khi chuyển tab ở giao diện chính", true);
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setBounds(leftX, y, 450, 26);
        pnl.add(chkTuDongTaiLaiDuLieuKhiChuyenTab);

        y += gap;
        chkKichHoatLuuVetHoatDong = createTouchCheckBox("Kích hoạt lưu vết hoạt động", true);
        chkKichHoatLuuVetHoatDong.setBounds(leftX, y, 400, 26);
        pnl.add(chkKichHoatLuuVetHoatDong);

        y += gap;
        chkHeThongChayNhieuMayTram = createTouchCheckBox("Hệ thống chạy nhiều máy trạm", true);
        chkHeThongChayNhieuMayTram.setBounds(leftX, y, 400, 26);
        pnl.add(chkHeThongChayNhieuMayTram);

        pnl.setPreferredSize(new Dimension(780, y + 45));
        return wrapInTouchScrollPane(pnl);
    }

    // ==================== UI HELPERS ====================

    private JLabel createWhiteLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setCaretColor(Color.BLACK);
        tf.setBorder(new EmptyBorder(0, 6, 0, 6));
        return tf;
    }

    private JTextField createNumericTextField(String initial) {
        JTextField tf = new JTextField(initial);
        tf.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBackground(Color.WHITE);
        tf.setForeground(Color.BLACK);
        tf.setBorder(new EmptyBorder(0, 4, 0, 4));
        return tf;
    }

    private <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setForeground(Color.BLACK);
        return cb;
    }

    private JButton createTealButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 6, 6));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(w, h));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Wrap text field with dark teal [⌨] keyboard button */
    private JPanel wrapWithKeyboardButton(JTextField field, String labelName, int width, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, height));
        wrapper.setBorder(new LineBorder(new Color(20, 60, 80), 1));

        JButton btnKeyboard = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw mini keyboard vector glyph
                int kw = 20;
                int kh = 13;
                int kx = (getWidth() - kw) / 2;
                int ky = (getHeight() - kh) / 2;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(kx, ky, kw, kh, 3, 3);

                // row 1 keys
                g2.fillRect(kx + 3, ky + 3, 2, 2);
                g2.fillRect(kx + 7, ky + 3, 2, 2);
                g2.fillRect(kx + 11, ky + 3, 2, 2);
                g2.fillRect(kx + 15, ky + 3, 2, 2);

                // row 2 keys
                g2.fillRect(kx + 3, ky + 6, 2, 2);
                g2.fillRect(kx + 7, ky + 6, 2, 2);
                g2.fillRect(kx + 11, ky + 6, 2, 2);
                g2.fillRect(kx + 15, ky + 6, 2, 2);

                // spacebar
                g2.fillRect(kx + 5, ky + 9, 10, 2);

                g2.dispose();
            }
        };
        btnKeyboard.setPreferredSize(new Dimension(38, height));
        btnKeyboard.setOpaque(false);
        btnKeyboard.setContentAreaFilled(false);
        btnKeyboard.setBorderPainted(false);
        btnKeyboard.setFocusPainted(false);
        btnKeyboard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable openKb = () -> {
            Window owner = SwingUtilities.getWindowAncestor(TouchCauHinhToanHeThongPanel.this);
            String res = TouchTextKeyboardDialog.showDialog(owner, "Nhập " + labelName, field.getText());
            if (res != null) {
                field.setText(res);
            }
        };

        btnKeyboard.addActionListener(e -> openKb.run());
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openKb.run();
                }
            }
        });

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(btnKeyboard, BorderLayout.EAST);
        return wrapper;
    }

    /** Wrap numeric field with dark teal [🖩] calculator button */
    private JPanel wrapWithCalculatorButton(JTextField field, String labelName, int width, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, height));
        wrapper.setBorder(new LineBorder(new Color(20, 60, 80), 1));

        JButton btnCalc = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw miniature calculator glyph
                int cx = getWidth() / 2 - 7;
                int cy = getHeight() / 2 - 8;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(cx, cy, 14, 16, 2, 2);
                g2.fillRect(cx + 2, cy + 2, 10, 3); // screen
                g2.fillRect(cx + 2, cy + 7, 2, 2);
                g2.fillRect(cx + 6, cy + 7, 2, 2);
                g2.fillRect(cx + 10, cy + 7, 2, 2);
                g2.fillRect(cx + 2, cy + 11, 2, 2);
                g2.fillRect(cx + 6, cy + 11, 2, 2);
                g2.fillRect(cx + 10, cy + 11, 2, 2);

                g2.dispose();
            }
        };
        btnCalc.setPreferredSize(new Dimension(30, height));
        btnCalc.setOpaque(false);
        btnCalc.setContentAreaFilled(false);
        btnCalc.setBorderPainted(false);
        btnCalc.setFocusPainted(false);
        btnCalc.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable openCalc = () -> {
            Window owner = SwingUtilities.getWindowAncestor(TouchCauHinhToanHeThongPanel.this);
            int currentVal = 0;
            try {
                currentVal = Integer.parseInt(field.getText().replace(",", "").trim());
            } catch (Exception ignored) {}

            Integer res = TouchNumberInputDialog.showDialog(owner, labelName, currentVal);
            if (res != null) {
                if ("Làm tròn tiền".equals(labelName) || "Doanh số 1 điểm".equals(labelName) || "Quy đổi 1 điểm sang tiền".equals(labelName)) {
                    field.setText(String.format("%,d", res));
                } else {
                    field.setText(String.valueOf(res));
                }
            }
        };

        btnCalc.addActionListener(e -> openCalc.run());
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openCalc.run();
            }
        });

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(btnCalc, BorderLayout.EAST);
        return wrapper;
    }

    /** Wrap field with [🔍] search button (Invoice Template) */
    private JPanel wrapWithSearchButton(JTextField field, String labelName, int width, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, height));
        wrapper.setBorder(new LineBorder(new Color(20, 60, 80), 1));

        JButton btnSearch = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Draw magnifying glass
                int cx = getWidth() / 2 - 2;
                int cy = getHeight() / 2 - 2;
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(cx - 5, cy - 5, 10, 10);
                g2.drawLine(cx + 3, cy + 3, cx + 8, cy + 8);
                g2.dispose();
            }
        };
        btnSearch.setPreferredSize(new Dimension(38, height));
        btnSearch.setOpaque(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setFocusPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Runnable openSearch = () -> {
            List<String> list = templateDAO.getInvoiceTemplateNames();
            if (list == null || list.isEmpty()) {
                list = Arrays.asList(
                        "BIÊN BẢN GIAO NHẬN HÀNG HÓA", "Mẫu 54 mm x 2 dòng", "Mẫu in bill 54mm",
                        "Mẫu in bill 80mm", "Mẫu in bill A4", "Mẫu in bill A5", "Mẫu in chế biến", "Phiếu giao hàng"
                );
            }
            String selected = (String) JOptionPane.showInputDialog(
                    TouchCauHinhToanHeThongPanel.this,
                    "Chọn mẫu in hóa đơn:",
                    "MẪU HÓA ĐƠN",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    list.toArray(),
                    field.getText()
            );
            if (selected != null && !selected.trim().isEmpty()) {
                field.setText(selected.trim());
            }
        };

        btnSearch.addActionListener(e -> openSearch.run());
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSearch.run();
            }
        });

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(btnSearch, BorderLayout.EAST);
        return wrapper;
    }

    /** Wrap with both Keyboard button and folder Browse button */
    private JPanel wrapWithKeyboardButtonAndBrowse(JTextField field, String labelName, int width, int height) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(width, height));
        wrapper.setBorder(new LineBorder(new Color(20, 60, 80), 1));

        JPanel rightBtns = new JPanel(new GridLayout(1, 2, 1, 0));
        rightBtns.setOpaque(false);

        JButton btnKb = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? FIELD_BTN_HOVER : FIELD_BTN_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                int kw = 16;
                int kh = 11;
                int kx = (getWidth() - kw) / 2;
                int ky = (getHeight() - kh) / 2;
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(kx, ky, kw, kh, 2, 2);
                g2.fillRect(kx + 2, ky + 2, 2, 2);
                g2.fillRect(kx + 6, ky + 2, 2, 2);
                g2.fillRect(kx + 10, ky + 2, 2, 2);
                g2.fillRect(kx + 2, ky + 5, 2, 2);
                g2.fillRect(kx + 6, ky + 5, 2, 2);
                g2.fillRect(kx + 10, ky + 5, 2, 2);
                g2.fillRect(kx + 4, ky + 8, 8, 2);
                g2.dispose();
            }
        };
        btnKb.setPreferredSize(new Dimension(30, height));
        btnKb.setOpaque(false);
        btnKb.setContentAreaFilled(false);
        btnKb.setBorderPainted(false);
        btnKb.setFocusPainted(false);
        btnKb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKb.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(TouchCauHinhToanHeThongPanel.this);
            String res = TouchTextKeyboardDialog.showDialog(owner, "Nhập " + labelName, field.getText());
            if (res != null) field.setText(res);
        });

        JButton btnBrowse = createTealButton("...", 30, height);
        btnBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(TouchCauHinhToanHeThongPanel.this) == JFileChooser.APPROVE_OPTION) {
                File dir = chooser.getSelectedFile();
                if (dir != null) field.setText(dir.getAbsolutePath());
            }
        });

        rightBtns.add(btnKb);
        rightBtns.add(btnBrowse);

        wrapper.add(field, BorderLayout.CENTER);
        wrapper.add(rightBtns, BorderLayout.EAST);
        return wrapper;
    }

    /** Create custom touch checkbox matching design [X] */
    private JCheckBox createTouchCheckBox(String text, boolean selected) {
        JCheckBox chk = new JCheckBox(text, selected);
        chk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chk.setForeground(Color.WHITE);
        chk.setOpaque(false);
        chk.setFocusPainted(false);
        chk.setIcon(new TouchCheckBoxIcon(false));
        chk.setSelectedIcon(new TouchCheckBoxIcon(true));
        chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return chk;
    }

    /** Custom Checkbox Icon rendering square with bold white 'X' */
    private static class TouchCheckBoxIcon implements Icon {
        private final boolean checked;
        private final int size = 24;

        public TouchCheckBoxIcon(boolean checked) {
            this.checked = checked;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Box outline
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(x + 1, y + 1, size - 2, size - 2, 3, 3);

            if (checked) {
                // Crisp bold white 'X' inside box
                g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int pad = 5;
                g2.drawLine(x + pad, y + pad, x + size - pad, y + size - pad);
                g2.drawLine(x + size - pad, y + pad, x + pad, y + size - pad);
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /** Sleek custom ScrollPane for touch panel */
    private JScrollPane wrapInTouchScrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        sp.getVerticalScrollBar().setBackground(CONTENT_BG);
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(20, 60, 80);
                this.trackColor = CONTENT_BG;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 75, 95));
                g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 2, thumbBounds.y + 2,
                        thumbBounds.width - 4, thumbBounds.height - 4, 8, 8));
                g2.dispose();
            }
        });
        return sp;
    }

    // ==================== LOGO HELPERS ====================

    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn hình ảnh logo công ty");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null && f.exists()) {
                selectedLogoPath = f.getAbsolutePath();
                displayLogo(selectedLogoPath);
            }
        }
    }

    private void displayLogo(String path) {
        if (lblLogoPreview == null) return;
        if (path == null || path.trim().isEmpty()) {
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
            return;
        }
        try {
            String resolved = SystemConfigDAO.resolveLogoPath(path);
            File f = (resolved != null) ? new File(resolved) : new File(path);
            if (f.exists() && f.isFile()) {
                BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(85, 65, Image.SCALE_SMOOTH);
                    lblLogoPreview.setIcon(new ImageIcon(scaled));
                    lblLogoPreview.setText("");
                    return;
                }
            }
        } catch (Exception ignored) {}
        lblLogoPreview.setIcon(null);
        lblLogoPreview.setText("Chưa có");
    }

    private void chooseLogoManHinhPhu() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn logo màn hình phụ");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Hình ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "png", "jpg", "jpeg", "gif", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (f != null && f.exists()) {
                selectedLogoManHinhPhuPath = f.getAbsolutePath();
                displayLogoManHinhPhu(selectedLogoManHinhPhuPath);
            }
        }
    }

    private void displayLogoManHinhPhu(String path) {
        if (lblLogoManHinhPhuPreview == null) return;
        if (path == null || path.trim().isEmpty()) {
            lblLogoManHinhPhuPreview.setIcon(null);
            lblLogoManHinhPhuPreview.setText("Chưa có");
            return;
        }
        try {
            String resolved = SystemConfigDAO.resolveLogoPath(path);
            File f = (resolved != null) ? new File(resolved) : new File(path);
            if (f.exists() && f.isFile()) {
                BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                    lblLogoManHinhPhuPreview.setIcon(new ImageIcon(scaled));
                    lblLogoManHinhPhuPreview.setText("");
                    return;
                }
            }
        } catch (Exception ignored) {}
        lblLogoManHinhPhuPreview.setIcon(null);
        lblLogoManHinhPhuPreview.setText("Chưa có");
    }

    private String[] getAvailablePrinters() {
        List<String> list = new ArrayList<>();
        list.add("");
        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            if (services != null) {
                for (PrintService ps : services) {
                    String name = ps.getName();
                    if (name != null && !name.trim().isEmpty() && !list.contains(name.trim())) {
                        list.add(name.trim());
                    }
                }
            }
        } catch (Exception ignored) {}
        return list.toArray(new String[0]);
    }

    // ==================== LOAD & SAVE DATA ====================

    private void loadConfigToUI() {
        if (currentConfig == null) return;

        // Tab 1: Thông tin chung
        txtTenCongTy.setText(currentConfig.getTenCongTy());
        txtDiaChi.setText(currentConfig.getDiaChi());
        selectedLogoPath = currentConfig.getLogoPath();
        displayLogo(selectedLogoPath);
        txtSoDienThoai.setText(currentConfig.getSoDienThoai());
        txtEmail.setText(currentConfig.getEmail());
        txtLoiCamOn.setText(currentConfig.getLoiCamOn());
        txtFax.setText(currentConfig.getFax());
        txtMauHoaDon.setText(currentConfig.getMauHoaDon() != null && !currentConfig.getMauHoaDon().isEmpty()
                ? currentConfig.getMauHoaDon() : "Mẫu in bill 80mm");
        chkLuaChonMauKhiIn.setSelected(currentConfig.isLuaChonMauKhiIn());
        chkHienThiTruocKhiIn.setSelected(currentConfig.isHienThiTruocKhiIn());

        // Tab 2: Bán hàng
        chkChoPhepTrungTenKhachHang.setSelected(currentConfig.isChoPhepTrungTenKhachHang());
        chkChoPhepNhapGiamGia.setSelected(currentConfig.isChoPhepNhapGiamGia());
        txtMacDinhGiamGiaPt.setText(String.valueOf(currentConfig.getMacDinhGiamGiaPt()));
        chkBanHangDungDauDocMaVach.setSelected(currentConfig.isBanHangDungDauDocMaVach());
        txtSoLanIn.setText(String.valueOf(currentConfig.getSoLanIn()));
        chkHienThiCuaSoNhapSoLuong.setSelected(currentConfig.isHienThiCuaSoNhapSoLuongKhiQuet());
        chkChoPhepThayDoiNgay.setSelected(currentConfig.isChoPhepThayDoiNgayTrenHoaDon());
        chkCoPhiVanChuyen.setSelected(currentConfig.isCoPhiVanChuyen());
        chkCoThueSuat.setSelected(currentConfig.isCoThueSuat());
        txtMacDinhThueSuat.setText(String.valueOf(currentConfig.getMacDinhThueSuat()));
        chkBatBuocNhapKhachHang.setSelected(currentConfig.isBatBuocNhapKhachHang());
        chkBatBuocNhapNVBH.setSelected(currentConfig.isBatBuocNhapNhanVienBanHang());
        chkChoPhepInTamTinh.setSelected(currentConfig.isChoPhepInTamTinh());
        chkBatBuocDoiTraTheoDon.setSelected(currentConfig.isBatBuocDoiTraHangTheoDon());
        txtSoNgayChoPhepDoiTra.setText(String.valueOf(currentConfig.getSoNgayChoPhepDoiTra()));
        chkHienThiAnhSanPham.setSelected(currentConfig.isHienThiAnhSanPhamTrongGiaoDien());
        chkKichHoatKhuyenMaiTuDong.setSelected(currentConfig.isKichHoatKhuyenMaiTuDong());
        chkHienThiMatHangKhuyenMaiKhiThanhToan.setSelected(currentConfig.isHienThiMatHangKhuyenMaiKhiThanhToan());
        chkCoGiaoHang.setSelected(currentConfig.isCoGiaoHang());
        chkSuDungGiaTheoKhach.setSelected(currentConfig.isSuDungGiaTheoKhach());
        chkSuDungGiamGiaMacDinh.setSelected(currentConfig.isSuDungGiamGiaMacDinhTheoMatHang());
        txtLamTronTien.setText(currentConfig.getLamTronTien() != null ? currentConfig.getLamTronTien() : "1,000");

        // Tab 3: Thanh toán
        chkCoThanhToanVoucher.setSelected(currentConfig.isCoThanhToanVoucher());
        chkChoPhepKhachNo.setSelected(currentConfig.isChoPhepKhachNo());
        chkLuaChonVoucherTuDanhSach.setSelected(currentConfig.isLuaChonVoucherTuDanhSach());
        chkSuDungTheTraTruoc.setSelected(currentConfig.isSuDungTheTraTruoc());
        chkSuDungDiemTichLuyDeThanhToan.setSelected(currentConfig.isSuDungDiemTichLuyDeThanhToan());
        txtQuyDoi1DiemSangTien.setText(currentConfig.getQuyDoi1DiemSangTien() != null ? currentConfig.getQuyDoi1DiemSangTien() : "1,000");
        chkCoThanhToanThe.setSelected(currentConfig.isCoThanhToanThe());
        chkCoThanhToanChuyenKhoan.setSelected(currentConfig.isCoThanhToanChuyenKhoan());

        // Tab 4: Tích điểm
        txtDoanhSoTuongUng1Diem.setText(currentConfig.getDoanhSoTuongUng1Diem() != null ? currentConfig.getDoanhSoTuongUng1Diem() : "20,000");
        if (currentConfig.getCachTinhDiem() != null) cbCachTinhDiem.setSelectedItem(currentConfig.getCachTinhDiem());
        chkTuDongNangCapThanhVien.setSelected(currentConfig.isTuDongNangCapThanhVien());
        chkHienThiDiemKhachHang.setSelected(currentConfig.isHienThiDiemKhachHangTrenHoaDon());
        chkKhongTichDiemNeuCoKhuyenMai.setSelected(currentConfig.isKhongTichDiemNeuCoKhuyenMai());

        // Tab 5: Kho hàng
        chkChoPhepNhapMotMatHangNhieuLan.setSelected(currentConfig.isChoPhepNhapMotMatHangNhieuLan());
        chkBatBuocChonNhanVienNhapKho.setSelected(currentConfig.isBatBuocChonNhanVienNhapKho());
        chkBatBuocChonNhaCungCapNhapKho.setSelected(currentConfig.isBatBuocChonNhaCungCapNhapKho());
        chkSuDung2DonViTinh.setSelected(currentConfig.isSuDung2DonViTinh());
        chkTuDongTinhGiaVon.setSelected(currentConfig.isTuDongTinhGiaVon());
        chkNhapKhoBangDauDocMaVach.setSelected(currentConfig.isNhapKhoBangDauDocMaVach());
        if (currentConfig.getGiaSuDungKhiChuyenXuatKho() != null) cbGiaSuDungKhiChuyenXuatKho.setSelectedItem(currentConfig.getGiaSuDungKhiChuyenXuatKho());
        chkSuDungChucNangSanXuat.setSelected(currentConfig.isSuDungChucNangSanXuat());

        // Tab 6: Mặt hàng
        chkSuDungMaSanCo.setSelected(currentConfig.isSuDungMaSanCo());
        chkSuDungMaHang.setSelected(currentConfig.isSuDungMaHang());
        chkTuDongSinhMaVach.setSelected(currentConfig.isTuDongSinhMaVach());
        txtChieuDaiMaVach.setText(String.valueOf(currentConfig.getChieuDaiMaVach()));
        chkCoKyTuKiemTra.setSelected(currentConfig.isCoKyTuKiemTra());
        chkSuDungTemInBangCanDienTu.setSelected(currentConfig.isSuDungTemInBangCanDienTu());
        txtChuoiBatDauCanDienTu.setText(currentConfig.getChuoiBatDauCanDienTu() != null ? currentConfig.getChuoiBatDauCanDienTu() : "29");
        if (currentConfig.getTimTheo() != null) cbTimTheo.setSelectedItem(currentConfig.getTimTheo());
        if (currentConfig.getCachTim() != null) cbCachTim.setSelectedItem(currentConfig.getCachTim());
        chkChoPhepTrungTenMatHang.setSelected(currentConfig.isChoPhepTrungTenMatHang());
        chkHangCoBaoHanh.setSelected(currentConfig.isHangCoBaoHanh());
        chkSuDungHangSanXuat.setSelected(currentConfig.isSuDungHangSanXuat());
        chkSuDungGia2.setSelected(currentConfig.isSuDungGia2());
        txtDienGiaiGia2.setText(currentConfig.getDienGiaiGia2() != null ? currentConfig.getDienGiaiGia2() : "Đơn giá 2");
        chkSuDungGia3.setSelected(currentConfig.isSuDungGia3());
        txtDienGiaiGia3.setText(currentConfig.getDienGiaiGia3() != null ? currentConfig.getDienGiaiGia3() : "Giá bán 3");
        chkSuDungGia4.setSelected(currentConfig.isSuDungGia4());
        txtDienGiaiGia4.setText(currentConfig.getDienGiaiGia4() != null ? currentConfig.getDienGiaiGia4() : "Giá bán 4");
        chkHienThiDeLuaChonNeuCoNhieuGia.setSelected(currentConfig.isHienThiDeLuaChonNeuCoNhieuGia());
        if (currentConfig.getSapXepThuTuTheo() != null) cbSapXepThuTuTheo.setSelectedItem(currentConfig.getSapXepThuTuTheo());
        chkMatHangCoHanSuDung.setSelected(currentConfig.isMatHangCoHanSuDung());
        chkMatHangCoKichThuoc.setSelected(currentConfig.isMatHangCoKichThuoc());
        chkMatHangCoVatTuDinhLuong.setSelected(currentConfig.isMatHangCoVatTuDinhLuong());

        // Tab 7: Cây hiển thị giá
        chkCoCayHienThiGia.setSelected(currentConfig.isCoCayHienThiGia());
        chkCayHienThiGiaMotDong.setSelected(currentConfig.isCayHienThiGiaMotDong());
        txtCongCayHienThiGia.setText(currentConfig.getCongCayHienThiGia() != null ? currentConfig.getCongCayHienThiGia() : "");
        chkCoManHinhPhu.setSelected(currentConfig.isCoManHinhPhu());
        selectedLogoManHinhPhuPath = currentConfig.getLogoManHinhPhuPath();
        displayLogoManHinhPhu(selectedLogoManHinhPhuPath);
        chkManHinhPhuSuDungGiaoDienThuong.setSelected(currentConfig.isManHinhPhuSuDungGiaoDienThuong());

        // Tab 8: Cân điện tử
        chkKichHoatSuDungCanDienTu.setSelected(currentConfig.isKichHoatSuDungCanDienTu());
        txtCongComCanDienTu.setText(currentConfig.getCongComCanDienTu() != null ? currentConfig.getCongComCanDienTu() : "");
        if (currentConfig.getBaudRateCanDienTu() != null) cbBaudRateCanDienTu.setSelectedItem(currentConfig.getBaudRateCanDienTu());
        txtDatabitCanDienTu.setText(currentConfig.getDatabitCanDienTu() != null ? currentConfig.getDatabitCanDienTu() : "0");
        if (currentConfig.getParityCanDienTu() != null) cbParityCanDienTu.setSelectedItem(currentConfig.getParityCanDienTu());
        if (currentConfig.getStopBitsCanDienTu() != null) cbStopBitsCanDienTu.setSelectedItem(currentConfig.getStopBitsCanDienTu());
        txtTimeoutCanDienTu.setText(String.valueOf(currentConfig.getTimeoutCanDienTu()));
        txtViTriCanDienTu.setText(String.valueOf(currentConfig.getViTriCanDienTu()));
        txtDoDaiCanDienTu.setText(String.valueOf(currentConfig.getDoDaiCanDienTu()));
        chkTuDongNhanDangCanDienTu.setSelected(currentConfig.isTuDongNhanDangCanDienTu());
        if (currentConfig.getDonViTrenCan() != null) cbDonViTrenCan.setSelectedItem(currentConfig.getDonViTrenCan());

        // Tab 9: Cảnh báo
        chkHienThiCanhBao.setSelected(currentConfig.isHienThiCanhBao());
        chkThongBaoHangDuoiMucAnToan.setSelected(currentConfig.isThongBaoHangDuoiMucAnToan());
        chkThongBaoKhachDenNgaySinhNhat.setSelected(currentConfig.isThongBaoKhachDenNgaySinhNhat());
        chkThongBaoHangHetHanDung.setSelected(currentConfig.isThongBaoHangHetHanDung());
        chkThongBaoHangSapHetHanDung.setSelected(currentConfig.isThongBaoHangSapHetHanDung());
        txtThongBaoTruocNgay.setText(String.valueOf(currentConfig.getThongBaoTruocNgay()));

        // Tab 11: Cảm ứng
        chkInPhaCheKhiInHoaDon.setSelected(currentConfig.isInPhaCheKhiInHoaDon());
        if (currentConfig.getMauInPhaChe() != null) cbMauInPhaChe.setSelectedItem(currentConfig.getMauInPhaChe());
        if (currentConfig.getMayInPhaChe() != null && !currentConfig.getMayInPhaChe().isEmpty()) cbMayInPhaChe.setSelectedItem(currentConfig.getMayInPhaChe());
        chkInPhaCheKhiLuuTam.setSelected(currentConfig.isInPhaCheKhiLuuTam());
        if (currentConfig.getMauInPhaCheLuuTam() != null) cbMauInPhaCheLuuTam.setSelectedItem(currentConfig.getMauInPhaCheLuuTam());

        // Tab 12: Quản trị
        chkPhieuGanNhatNamPhiaDuoi.setSelected(currentConfig.isPhieuGanNhatNamPhiaDuoi());
        if (currentConfig.getPhimNhapLieuTrenLuoi() != null) cbPhimNhapLieuTrenLuoi.setSelectedItem(currentConfig.getPhimNhapLieuTrenLuoi());
        chkTuDongSaoLuuDuLieu.setSelected(currentConfig.isTuDongSaoLuuDuLieu());
        txtDuongDanSaoLuu.setText(currentConfig.getDuongDanSaoLuu() != null ? currentConfig.getDuongDanSaoLuu() : "");
        txtSoNgaySaoLuu.setText(String.valueOf(currentConfig.getSoNgaySaoLuu()));
        if (currentConfig.getChucNangMacDinh() != null) cbChucNangMacDinh.setSelectedItem(currentConfig.getChucNangMacDinh());
        chkLocDuLieuBoKhoangTrong.setSelected(currentConfig.isLocDuLieuBoKhoangTrong());
        chkHienThiXuongDongNeuDoRongCotNho.setSelected(currentConfig.isHienThiXuongDongNeuDoRongCotNho());
        if (currentConfig.getDoToChuHienThi() != null) cbDoToChuHienThi.setSelectedItem(currentConfig.getDoToChuHienThi());
        if (currentConfig.getGiaoDienChuongTrinh() != null) cbGiaoDienChuongTrinh.setSelectedItem(currentConfig.getGiaoDienChuongTrinh());
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setSelected(currentConfig.isTuDongTaiLaiDuLieuKhiChuyenTab());
        chkKichHoatLuuVetHoatDong.setSelected(currentConfig.isKichHoatLuuVetHoatDong());
        chkHeThongChayNhieuMayTram.setSelected(currentConfig.isHeThongChayNhieuMayTram());
    }

    public void onSave() {
        if (currentConfig == null) currentConfig = new SystemConfig();

        String previousFontSize = currentConfig.getDoToChuHienThi();
        String previousSystemTheme = currentConfig.getGiaoDienChuongTrinh();

        // Tab 1
        currentConfig.setTenCongTy(txtTenCongTy.getText().trim());
        currentConfig.setDiaChi(txtDiaChi.getText().trim());
        currentConfig.setLogoPath(selectedLogoPath != null && !selectedLogoPath.trim().isEmpty() ? selectedLogoPath.trim() : null);
        currentConfig.setSoDienThoai(txtSoDienThoai.getText().trim());
        currentConfig.setEmail(txtEmail.getText().trim());
        currentConfig.setLoiCamOn(txtLoiCamOn.getText().trim());
        currentConfig.setFax(txtFax.getText().trim());
        currentConfig.setMauHoaDon(txtMauHoaDon.getText().trim());
        currentConfig.setLuaChonMauKhiIn(chkLuaChonMauKhiIn.isSelected());
        currentConfig.setHienThiTruocKhiIn(chkHienThiTruocKhiIn.isSelected());

        // Tab 2
        currentConfig.setChoPhepTrungTenKhachHang(chkChoPhepTrungTenKhachHang.isSelected());
        currentConfig.setChoPhepNhapGiamGia(chkChoPhepNhapGiamGia.isSelected());
        try { currentConfig.setMacDinhGiamGiaPt(Integer.parseInt(txtMacDinhGiamGiaPt.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setBanHangDungDauDocMaVach(chkBanHangDungDauDocMaVach.isSelected());
        try { currentConfig.setSoLanIn(Integer.parseInt(txtSoLanIn.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setHienThiCuaSoNhapSoLuongKhiQuet(chkHienThiCuaSoNhapSoLuong.isSelected());
        currentConfig.setChoPhepThayDoiNgayTrenHoaDon(chkChoPhepThayDoiNgay.isSelected());
        currentConfig.setCoPhiVanChuyen(chkCoPhiVanChuyen.isSelected());
        currentConfig.setCoThueSuat(chkCoThueSuat.isSelected());
        try { currentConfig.setMacDinhThueSuat(Integer.parseInt(txtMacDinhThueSuat.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setBatBuocNhapKhachHang(chkBatBuocNhapKhachHang.isSelected());
        currentConfig.setBatBuocNhapNhanVienBanHang(chkBatBuocNhapNVBH.isSelected());
        currentConfig.setChoPhepInTamTinh(chkChoPhepInTamTinh.isSelected());
        currentConfig.setBatBuocDoiTraHangTheoDon(chkBatBuocDoiTraTheoDon.isSelected());
        try { currentConfig.setSoNgayChoPhepDoiTra(Integer.parseInt(txtSoNgayChoPhepDoiTra.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setHienThiAnhSanPhamTrongGiaoDien(chkHienThiAnhSanPham.isSelected());
        currentConfig.setKichHoatKhuyenMaiTuDong(chkKichHoatKhuyenMaiTuDong.isSelected());
        currentConfig.setHienThiMatHangKhuyenMaiKhiThanhToan(chkHienThiMatHangKhuyenMaiKhiThanhToan.isSelected());
        currentConfig.setCoGiaoHang(chkCoGiaoHang.isSelected());
        currentConfig.setSuDungGiaTheoKhach(chkSuDungGiaTheoKhach.isSelected());
        currentConfig.setSuDungGiamGiaMacDinhTheoMatHang(chkSuDungGiamGiaMacDinh.isSelected());
        currentConfig.setLamTronTien(txtLamTronTien.getText().trim());

        // Tab 3
        currentConfig.setCoThanhToanVoucher(chkCoThanhToanVoucher.isSelected());
        currentConfig.setChoPhepKhachNo(chkChoPhepKhachNo.isSelected());
        currentConfig.setLuaChonVoucherTuDanhSach(chkLuaChonVoucherTuDanhSach.isSelected());
        currentConfig.setSuDungTheTraTruoc(chkSuDungTheTraTruoc.isSelected());
        currentConfig.setSuDungDiemTichLuyDeThanhToan(chkSuDungDiemTichLuyDeThanhToan.isSelected());
        currentConfig.setQuyDoi1DiemSangTien(txtQuyDoi1DiemSangTien.getText().trim());
        currentConfig.setCoThanhToanThe(chkCoThanhToanThe.isSelected());
        currentConfig.setCoThanhToanChuyenKhoan(chkCoThanhToanChuyenKhoan.isSelected());

        // Tab 4
        currentConfig.setDoanhSoTuongUng1Diem(txtDoanhSoTuongUng1Diem.getText().trim());
        currentConfig.setCachTinhDiem(cbCachTinhDiem.getSelectedItem() != null ? cbCachTinhDiem.getSelectedItem().toString() : "Điểm được tính trên từng hóa đơn");
        currentConfig.setTuDongNangCapThanhVien(chkTuDongNangCapThanhVien.isSelected());
        currentConfig.setHienThiDiemKhachHangTrenHoaDon(chkHienThiDiemKhachHang.isSelected());
        currentConfig.setKhongTichDiemNeuCoKhuyenMai(chkKhongTichDiemNeuCoKhuyenMai.isSelected());

        // Tab 5
        currentConfig.setChoPhepNhapMotMatHangNhieuLan(chkChoPhepNhapMotMatHangNhieuLan.isSelected());
        currentConfig.setBatBuocChonNhanVienNhapKho(chkBatBuocChonNhanVienNhapKho.isSelected());
        currentConfig.setBatBuocChonNhaCungCapNhapKho(chkBatBuocChonNhaCungCapNhapKho.isSelected());
        currentConfig.setSuDung2DonViTinh(chkSuDung2DonViTinh.isSelected());
        currentConfig.setTuDongTinhGiaVon(chkTuDongTinhGiaVon.isSelected());
        currentConfig.setNhapKhoBangDauDocMaVach(chkNhapKhoBangDauDocMaVach.isSelected());
        currentConfig.setGiaSuDungKhiChuyenXuatKho(cbGiaSuDungKhiChuyenXuatKho.getSelectedItem() != null ? cbGiaSuDungKhiChuyenXuatKho.getSelectedItem().toString() : "Giá vốn");
        currentConfig.setSuDungChucNangSanXuat(chkSuDungChucNangSanXuat.isSelected());

        // Tab 6
        currentConfig.setSuDungMaSanCo(chkSuDungMaSanCo.isSelected());
        currentConfig.setSuDungMaHang(chkSuDungMaHang.isSelected());
        currentConfig.setTuDongSinhMaVach(chkTuDongSinhMaVach.isSelected());
        try { currentConfig.setChieuDaiMaVach(Integer.parseInt(txtChieuDaiMaVach.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setCoKyTuKiemTra(chkCoKyTuKiemTra.isSelected());
        currentConfig.setSuDungTemInBangCanDienTu(chkSuDungTemInBangCanDienTu.isSelected());
        currentConfig.setChuoiBatDauCanDienTu(txtChuoiBatDauCanDienTu.getText().trim());
        currentConfig.setTimTheo(cbTimTheo.getSelectedItem() != null ? cbTimTheo.getSelectedItem().toString() : "Cả tên hàng và mã hàng");
        currentConfig.setCachTim(cbCachTim.getSelectedItem() != null ? cbCachTim.getSelectedItem().toString() : "Tìm có chứa");
        currentConfig.setChoPhepTrungTenMatHang(chkChoPhepTrungTenMatHang.isSelected());
        currentConfig.setHangCoBaoHanh(chkHangCoBaoHanh.isSelected());
        currentConfig.setSuDungHangSanXuat(chkSuDungHangSanXuat.isSelected());
        currentConfig.setSuDungGia2(chkSuDungGia2.isSelected());
        currentConfig.setDienGiaiGia2(txtDienGiaiGia2.getText().trim());
        currentConfig.setSuDungGia3(chkSuDungGia3.isSelected());
        currentConfig.setDienGiaiGia3(txtDienGiaiGia3.getText().trim());
        currentConfig.setSuDungGia4(chkSuDungGia4.isSelected());
        currentConfig.setDienGiaiGia4(txtDienGiaiGia4.getText().trim());
        currentConfig.setHienThiDeLuaChonNeuCoNhieuGia(chkHienThiDeLuaChonNeuCoNhieuGia.isSelected());
        currentConfig.setSapXepThuTuTheo(cbSapXepThuTuTheo.getSelectedItem() != null ? cbSapXepThuTuTheo.getSelectedItem().toString() : "Mã hàng");
        currentConfig.setMatHangCoHanSuDung(chkMatHangCoHanSuDung.isSelected());
        currentConfig.setMatHangCoKichThuoc(chkMatHangCoKichThuoc.isSelected());
        currentConfig.setMatHangCoVatTuDinhLuong(chkMatHangCoVatTuDinhLuong.isSelected());

        // Tab 7
        currentConfig.setCoCayHienThiGia(chkCoCayHienThiGia.isSelected());
        currentConfig.setCayHienThiGiaMotDong(chkCayHienThiGiaMotDong.isSelected());
        currentConfig.setCongCayHienThiGia(txtCongCayHienThiGia.getText().trim());
        currentConfig.setCoManHinhPhu(chkCoManHinhPhu.isSelected());
        currentConfig.setLogoManHinhPhuPath(selectedLogoManHinhPhuPath != null && !selectedLogoManHinhPhuPath.trim().isEmpty()
                ? selectedLogoManHinhPhuPath.trim() : null);
        currentConfig.setManHinhPhuSuDungGiaoDienThuong(chkManHinhPhuSuDungGiaoDienThuong.isSelected());

        // Tab 8
        currentConfig.setKichHoatSuDungCanDienTu(chkKichHoatSuDungCanDienTu.isSelected());
        currentConfig.setCongComCanDienTu(txtCongComCanDienTu.getText().trim());
        currentConfig.setBaudRateCanDienTu(cbBaudRateCanDienTu.getSelectedItem() != null ? cbBaudRateCanDienTu.getSelectedItem().toString() : "1200");
        currentConfig.setDatabitCanDienTu(txtDatabitCanDienTu.getText().trim());
        currentConfig.setParityCanDienTu(cbParityCanDienTu.getSelectedItem() != null ? cbParityCanDienTu.getSelectedItem().toString() : "None");
        currentConfig.setStopBitsCanDienTu(cbStopBitsCanDienTu.getSelectedItem() != null ? cbStopBitsCanDienTu.getSelectedItem().toString() : "");
        try { currentConfig.setTimeoutCanDienTu(Integer.parseInt(txtTimeoutCanDienTu.getText().trim())); } catch (Exception ignored) {}
        try { currentConfig.setViTriCanDienTu(Integer.parseInt(txtViTriCanDienTu.getText().trim())); } catch (Exception ignored) {}
        try { currentConfig.setDoDaiCanDienTu(Integer.parseInt(txtDoDaiCanDienTu.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setTuDongNhanDangCanDienTu(chkTuDongNhanDangCanDienTu.isSelected());
        currentConfig.setDonViTrenCan(cbDonViTrenCan.getSelectedItem() != null ? cbDonViTrenCan.getSelectedItem().toString() : "Gram");

        // Tab 9
        currentConfig.setHienThiCanhBao(chkHienThiCanhBao.isSelected());
        currentConfig.setThongBaoHangDuoiMucAnToan(chkThongBaoHangDuoiMucAnToan.isSelected());
        currentConfig.setThongBaoKhachDenNgaySinhNhat(chkThongBaoKhachDenNgaySinhNhat.isSelected());
        currentConfig.setThongBaoHangHetHanDung(chkThongBaoHangHetHanDung.isSelected());
        currentConfig.setThongBaoHangSapHetHanDung(chkThongBaoHangSapHetHanDung.isSelected());
        try { currentConfig.setThongBaoTruocNgay(Integer.parseInt(txtThongBaoTruocNgay.getText().trim())); } catch (Exception ignored) {}

        // Tab 11
        currentConfig.setInPhaCheKhiInHoaDon(chkInPhaCheKhiInHoaDon.isSelected());
        currentConfig.setMauInPhaChe(cbMauInPhaChe.getSelectedItem() != null ? cbMauInPhaChe.getSelectedItem().toString() : "Mẫu in chế biến");
        currentConfig.setMayInPhaChe(cbMayInPhaChe.getSelectedItem() != null ? cbMayInPhaChe.getSelectedItem().toString() : "Microsoft XPS Document Writer");
        currentConfig.setInPhaCheKhiLuuTam(chkInPhaCheKhiLuuTam.isSelected());
        currentConfig.setMauInPhaCheLuuTam(cbMauInPhaCheLuuTam.getSelectedItem() != null ? cbMauInPhaCheLuuTam.getSelectedItem().toString() : "");

        // Tab 12
        currentConfig.setPhieuGanNhatNamPhiaDuoi(chkPhieuGanNhatNamPhiaDuoi.isSelected());
        currentConfig.setPhimNhapLieuTrenLuoi(cbPhimNhapLieuTrenLuoi.getSelectedItem() != null ? cbPhimNhapLieuTrenLuoi.getSelectedItem().toString() : "Tab để sang ngang, Enter để xuống dòng");
        currentConfig.setTuDongSaoLuuDuLieu(chkTuDongSaoLuuDuLieu.isSelected());
        currentConfig.setDuongDanSaoLuu(txtDuongDanSaoLuu.getText().trim());
        try { currentConfig.setSoNgaySaoLuu(Integer.parseInt(txtSoNgaySaoLuu.getText().trim())); } catch (Exception ignored) {}
        currentConfig.setChucNangMacDinh(cbChucNangMacDinh.getSelectedItem() != null ? cbChucNangMacDinh.getSelectedItem().toString() : "Hóa đơn bán hàng");
        currentConfig.setLocDuLieuBoKhoangTrong(chkLocDuLieuBoKhoangTrong.isSelected());
        currentConfig.setHienThiXuongDongNeuDoRongCotNho(chkHienThiXuongDongNeuDoRongCotNho.isSelected());
        currentConfig.setDoToChuHienThi(cbDoToChuHienThi.getSelectedItem() != null ? cbDoToChuHienThi.getSelectedItem().toString() : "Nhỏ (110%)");
        currentConfig.setGiaoDienChuongTrinh(cbGiaoDienChuongTrinh.getSelectedItem() != null ? cbGiaoDienChuongTrinh.getSelectedItem().toString() : "Office 2010 - Blue");
        currentConfig.setTuDongTaiLaiDuLieuKhiChuyenTab(chkTuDongTaiLaiDuLieuKhiChuyenTab.isSelected());
        currentConfig.setKichHoatLuuVetHoatDong(chkKichHoatLuuVetHoatDong.isSelected());
        currentConfig.setHeThongChayNhieuMayTram(chkHeThongChayNhieuMayTram.isSelected());

        boolean restartRequired = SystemConfigRestartPolicy.requiresRestart(
                previousFontSize,
                previousSystemTheme,
                currentConfig.getDoToChuHienThi(),
                currentConfig.getGiaoDienChuongTrinh()
        );

        boolean ok = configDAO.saveConfig(currentConfig);
        if (ok) {
            JOptionPane.showMessageDialog(
                    this,
                    restartRequired
                            ? "Đã lưu cấu hình thành công!\n"
                                    + "Bạn đã thay đổi cỡ chữ hoặc màu hệ thống. "
                                    + "Vui lòng khởi động lại ứng dụng để áp dụng."
                            : "Đã lưu và áp dụng cấu hình thành công!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            if (backAction != null) backAction.run();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu cấu hình hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public SystemConfig getCurrentConfig() {
        return currentConfig;
    }

    public String getCurrentActiveTab() {
        return currentActiveTab;
    }
}
