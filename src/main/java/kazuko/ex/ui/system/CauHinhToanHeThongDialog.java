package kazuko.ex.ui.system;

import kazuko.ex.dao.STemplateDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.BillTemplateDAO;
import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.system.STemplate;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.bill.JrxmlEditorServer;
import kazuko.ex.util.IconHelper;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class CauHinhToanHeThongDialog extends JDialog {
    private final SystemConfigDAO configDAO = new SystemConfigDAO();
    private final STemplateDAO templateDAO = new STemplateDAO();
    private final BillTemplateDAO billTemplateDAO = new BillTemplateDAO();
    private SystemConfig currentConfig;

    // State mở khóa Tab Mẫu hóa đơn (lưu trong phiên làm việc)
    private static boolean isDevUnlocked = false;
    private JList<STemplate> listFolderMauHD;
    private DefaultListModel<STemplate> folderMauHDListModel;
    private JList<STemplate> listTemplateMauHD;
    private DefaultListModel<STemplate> templateMauHDListModel;

    // Sidebar
    private JList<MenuItem> listCategories;
    private DefaultListModel<MenuItem> listModel;
    private List<MenuItem> allMenuItems;
    private JTextField txtSearchSidebar;

    // Content Switcher
    private JPanel pnlCardContainer;
    private CardLayout cardLayout;

    // Tab 1: Thông tin chung
    private JTextField txtTenCongTy;
    private JTextArea txtDiaChi;
    private JLabel lblLogoPreview;
    private String selectedLogoPath;
    private JTextField txtSoDienThoai;
    private JTextField txtEmail;
    private JTextField txtLoiCamOn;
    private JTextField txtFax;
    private JComboBox<String> cbMauHoaDon;
    private JCheckBox chkLuaChonMauKhiIn;
    private JCheckBox chkHienThiTruocKhiIn;

    // Tab 2: Máy in (ComboBoxes)
    private final Map<String, JComboBox<String>> printerFields = new LinkedHashMap<>();

    // Tab 3: Số phiếu (Non-editable JTextFields)
    private final Map<String, JTextField> voucherFields = new LinkedHashMap<>();

    // Tab 4: Bán hàng
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

    // Tab 5: Thanh toán
    private JCheckBox chkCoThanhToanVoucher;
    private JCheckBox chkChoPhepKhachNo;
    private JCheckBox chkLuaChonVoucherTuDanhSach;
    private JCheckBox chkSuDungTheTraTruoc;
    private JCheckBox chkSuDungDiemTichLuyDeThanhToan;
    private JTextField txtQuyDoi1DiemSangTien;
    private JCheckBox chkCoThanhToanThe;
    private JCheckBox chkCoThanhToanChuyenKhoan;

    // Tab 6: Tích điểm
    private JTextField txtDoanhSoTuongUng1Diem;
    private JComboBox<String> cbCachTinhDiem;
    private JCheckBox chkTuDongNangCapThanhVien;
    private JCheckBox chkHienThiDiemKhachHang;
    private JCheckBox chkKhongTichDiemNeuCoKhuyenMai;

    // Tab 7: Kho hàng
    private JCheckBox chkChoPhepNhapMotMatHangNhieuLan;
    private JCheckBox chkBatBuocChonNhanVienNhapKho;
    private JCheckBox chkBatBuocChonNhaCungCapNhapKho;
    private JCheckBox chkSuDung2DonViTinh;
    private JCheckBox chkTuDongTinhGiaVon;
    private JCheckBox chkNhapKhoBangDauDocMaVach;
    private JComboBox<String> cbGiaSuDungKhiChuyenXuatKho;
    private JCheckBox chkSuDungChucNangSanXuat;

    // Tab 8: Mặt hàng
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

    // Tab Cây hiển thị giá
    private JCheckBox chkCoCayHienThiGia;
    private JCheckBox chkCayHienThiGiaMotDong;
    private JTextField txtCongCayHienThiGia;
    private JCheckBox chkCoManHinhPhu;
    private JLabel lblLogoManHinhPhuPreview;
    private String selectedLogoManHinhPhuPath;
    private JCheckBox chkManHinhPhuSuDungGiaoDienThuong;

    // Tab Cân điện tử
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

    // Tab 10: Cảm ứng
    private JCheckBox chkInPhaCheKhiInHoaDon;
    private JComboBox<String> cbMauInPhaChe;
    private JComboBox<String> cbMayInPhaChe;
    private JCheckBox chkInPhaCheKhiLuuTam;
    private JComboBox<String> cbMauInPhaCheLuuTam;

    // Tab 11: Quản trị
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

    public CauHinhToanHeThongDialog(Frame parent) {
        super(parent, "CẤU HÌNH TOÀN HỆ THỐNG", true);
        this.currentConfig = configDAO.loadConfig();

        initUI();
        loadConfigToUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(840, 560));
        setMinimumSize(new Dimension(780, 500));

        // 1. Top Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Tools.png", 26, 26));
        JLabel lblTitle = new JLabel("Thông tin cấu hình toàn bộ hệ thống");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 45, 85));
        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center Split (Sidebar Left + Card Container Right)
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(new Color(240, 245, 252));

        // Left Sidebar
        JPanel pnlSidebar = createSidebarPanel();
        pnlCenter.add(pnlSidebar, BorderLayout.WEST);

        // Right Card Container
        cardLayout = new CardLayout();
        pnlCardContainer = new JPanel(cardLayout);
        pnlCardContainer.setBackground(Color.WHITE);
        pnlCardContainer.setBorder(new MatteBorder(0, 1, 0, 0, new Color(205, 220, 240)));

        // Add Content Panels
        pnlCardContainer.add(createTabThongTinChinh(), "Thông tin chung");
        pnlCardContainer.add(createTabMauHoaDon(), "Mẫu hóa đơn");
        pnlCardContainer.add(createTabMayIn(), "Máy in");
        pnlCardContainer.add(createTabSoPhieu(), "Số phiếu");
        pnlCardContainer.add(createTabBanHang(), "Bán hàng");
        pnlCardContainer.add(createTabThanhToan(), "Thanh toán");
        pnlCardContainer.add(createTabTichDiem(), "Tích điểm");
        pnlCardContainer.add(createTabKhoHang(), "Kho hàng");
        pnlCardContainer.add(createTabMatHang(), "Mặt hàng");
        pnlCardContainer.add(createTabCayHienThiGia(), "Cây hiển thị giá");
        pnlCardContainer.add(createTabCanDienTu(), "Cân điện tử");
        pnlCardContainer.add(createTabCanhBao(), "Cảnh báo");
        pnlCardContainer.add(createTabCamUng(), "Cảm ứng");
        pnlCardContainer.add(createTabQuanTri(), "Quản trị");

        // Placeholder cards for other categories
        for (MenuItem item : allMenuItems) {
            if (!"Thông tin chung".equals(item.name) && !"Mẫu hóa đơn".equals(item.name) && !"Máy in".equals(item.name) && !"Số phiếu".equals(item.name)
                    && !"Bán hàng".equals(item.name) && !"Thanh toán".equals(item.name) && !"Tích điểm".equals(item.name)
                    && !"Kho hàng".equals(item.name) && !"Mặt hàng".equals(item.name) && !"Cây hiển thị giá".equals(item.name)
                    && !"Cân điện tử".equals(item.name) && !"Cảnh báo".equals(item.name)
                    && !"Cảm ứng".equals(item.name) && !"Quản trị".equals(item.name)) {
                pnlCardContainer.add(createPlaceholderCard(item.name), item.name);
            }
        }

        pnlCenter.add(pnlCardContainer, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Footer
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlFooter.setBackground(new Color(236, 242, 250));
        pnlFooter.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.setPreferredSize(new Dimension(95, 26));
        btnSave.setBackground(Color.WHITE);
        btnSave.addActionListener(e -> onSave());

        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(85, 26));
        btnExit.setBackground(Color.WHITE);
        btnExit.addActionListener(e -> dispose());

        pnlFooter.add(btnSave);
        pnlFooter.add(btnExit);
        add(pnlFooter, BorderLayout.SOUTH);

        // Shortcuts
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createSidebarPanel() {
        JPanel pnlSidebar = new JPanel(new BorderLayout());
        pnlSidebar.setPreferredSize(new Dimension(195, 400));
        pnlSidebar.setBackground(Color.WHITE);

        allMenuItems = new ArrayList<>();
        allMenuItems.add(new MenuItem("Thông tin chung", "Folder.png"));
        allMenuItems.add(new MenuItem("Mẫu hóa đơn", "Report.png"));
        allMenuItems.add(new MenuItem("Máy in", "Preview.png"));
        allMenuItems.add(new MenuItem("Số phiếu", "Barcode.png"));
        allMenuItems.add(new MenuItem("Bán hàng", "btnNew.Image.png"));
        allMenuItems.add(new MenuItem("Thanh toán", "FilterEdit.png"));
        allMenuItems.add(new MenuItem("Tích điểm", "Statistic16.png"));
        allMenuItems.add(new MenuItem("Kho hàng", "Folder2.png"));
        allMenuItems.add(new MenuItem("Mặt hàng", "1282529873_objects.png"));
        allMenuItems.add(new MenuItem("Cây hiển thị giá", "Tree.png"));
        allMenuItems.add(new MenuItem("Cân điện tử", "Bookmark.png"));
        allMenuItems.add(new MenuItem("Cảnh báo", "cross.png"));
        allMenuItems.add(new MenuItem("Bảo mật", "pencil.png"));
        allMenuItems.add(new MenuItem("Cảm ứng", "btnOpen.Image.png"));
        allMenuItems.add(new MenuItem("Quản trị", "Tools.png"));

        listModel = new DefaultListModel<>();
        for (MenuItem item : allMenuItems) {
            listModel.addElement(item);
        }

        listCategories = new JList<>(listModel);
        listCategories.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listCategories.setSelectedIndex(0);
        listCategories.setFixedCellHeight(26);

        listCategories.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof MenuItem) {
                    MenuItem mi = (MenuItem) value;
                    lbl.setText(mi.name);
                    lbl.setIcon(IconHelper.getIcon(mi.iconName, 15, 15));
                }
                lbl.setBorder(new EmptyBorder(2, 8, 2, 8));
                if (isSelected) {
                    lbl.setBackground(new Color(0, 120, 215));
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                return lbl;
            }
        });

        listCategories.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MenuItem selected = listCategories.getSelectedValue();
                if (selected != null) {
                    cardLayout.show(pnlCardContainer, selected.name);
                }
            }
        });

        JScrollPane scrollList = new JScrollPane(listCategories);
        scrollList.setBorder(null);
        pnlSidebar.add(scrollList, BorderLayout.CENTER);

        // Search Bar below sidebar
        JPanel pnlSearch = new JPanel(new BorderLayout());
        pnlSearch.setBackground(new Color(245, 248, 252));
        pnlSearch.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        txtSearchSidebar = new JTextField();
        txtSearchSidebar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearchSidebar.setBorder(new EmptyBorder(4, 6, 4, 6));

        JLabel lblSearchIcon = new JLabel(IconHelper.getIcon("tsbQuickOpen.Image.png", 14, 14));
        lblSearchIcon.setBorder(new EmptyBorder(0, 6, 0, 2));

        pnlSearch.add(lblSearchIcon, BorderLayout.WEST);
        pnlSearch.add(txtSearchSidebar, BorderLayout.CENTER);

        txtSearchSidebar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterSidebar(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterSidebar(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterSidebar(); }
        });

        pnlSidebar.add(pnlSearch, BorderLayout.SOUTH);
        return pnlSidebar;
    }

    private void filterSidebar() {
        String kw = txtSearchSidebar.getText().trim().toLowerCase();
        listModel.clear();
        for (MenuItem item : allMenuItems) {
            if (item.name.toLowerCase().contains(kw)) {
                listModel.addElement(item);
            }
        }
        if (!listModel.isEmpty()) {
            listCategories.setSelectedIndex(0);
        }
    }

    // ==================== TAB 1: THÔNG TIN CHÍNH ====================
    private JPanel createTabThongTinChinh() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int lblW = 100;
        int fieldH = 24;
        int startY = 15;
        int rowGap = 32;

        // Row 1: Tên công ty
        JLabel lblTenCty = new JLabel("Tên công ty:");
        lblTenCty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTenCty.setBounds(20, startY, lblW, fieldH);
        panel.add(lblTenCty);

        txtTenCongTy = new JTextField();
        txtTenCongTy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTenCongTy.setBounds(110, startY, 340, fieldH);
        panel.add(txtTenCongTy);

        startY += 26;
        JLabel lblSubNote = new JLabel("(Tên công ty sẽ hiển thị dưới báo cáo)");
        lblSubNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSubNote.setForeground(new Color(100, 100, 100));
        lblSubNote.setBounds(110, startY, 340, 18);
        panel.add(lblSubNote);

        // Row 2: Địa chỉ & Logo
        startY += 24;
        JLabel lblDiaChi = new JLabel("Địa chỉ:");
        lblDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiaChi.setBounds(20, startY, lblW, fieldH);
        panel.add(lblDiaChi);

        txtDiaChi = new JTextArea();
        txtDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiaChi.setLineWrap(true);
        txtDiaChi.setWrapStyleWord(true);
        JScrollPane scrollDiaChi = new JScrollPane(txtDiaChi);
        scrollDiaChi.setBounds(110, startY, 340, 75);
        panel.add(scrollDiaChi);

        // Logo Box on right of DiaChi
        JLabel lblLogoTitle = new JLabel("Logo:");
        lblLogoTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLogoTitle.setBounds(465, startY, 40, fieldH);
        panel.add(lblLogoTitle);

        JPanel pnlLogoBox = new JPanel(new BorderLayout());
        pnlLogoBox.setBounds(510, startY, 100, 75);
        pnlLogoBox.setBackground(new Color(245, 248, 252));
        pnlLogoBox.setBorder(new MatteBorder(1, 1, 1, 1, new Color(190, 210, 235)));

        lblLogoPreview = new JLabel();
        lblLogoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        pnlLogoBox.add(lblLogoPreview, BorderLayout.CENTER);
        panel.add(pnlLogoBox);

        JButton btnChooseLogo = new JButton("Chọn");
        btnChooseLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnChooseLogo.setBounds(495, startY + 80, 58, 22);
        btnChooseLogo.setBackground(Color.WHITE);
        btnChooseLogo.addActionListener(e -> chooseLogo());
        panel.add(btnChooseLogo);

        JButton btnClearLogo = new JButton("Xóa");
        btnClearLogo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnClearLogo.setBounds(557, startY + 80, 55, 22);
        btnClearLogo.setBackground(Color.WHITE);
        btnClearLogo.addActionListener(e -> {
            selectedLogoPath = null;
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
        });
        panel.add(btnClearLogo);

        // Row 3: Số điện thoại
        startY += 85;
        JLabel lblPhone = new JLabel("Số điện thoại:");
        lblPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhone.setBounds(20, startY, lblW, fieldH);
        panel.add(lblPhone);

        txtSoDienThoai = new JTextField();
        txtSoDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoDienThoai.setBounds(110, startY, 340, fieldH);
        panel.add(txtSoDienThoai);

        // Row 4: Email
        startY += rowGap;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEmail.setBounds(20, startY, lblW, fieldH);
        panel.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmail.setBounds(110, startY, 340, fieldH);
        panel.add(txtEmail);

        // Row 5: Lời cảm ơn
        startY += rowGap;
        JLabel lblThanks = new JLabel("Lời cảm ơn:");
        lblThanks.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThanks.setBounds(20, startY, lblW, fieldH);
        panel.add(lblThanks);

        txtLoiCamOn = new JTextField();
        txtLoiCamOn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLoiCamOn.setBounds(110, startY, 340, fieldH);
        panel.add(txtLoiCamOn);

        // Row 6: Fax
        startY += rowGap;
        JLabel lblFax = new JLabel("Fax:");
        lblFax.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFax.setBounds(20, startY, lblW, fieldH);
        panel.add(lblFax);

        txtFax = new JTextField();
        txtFax.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFax.setBounds(110, startY, 340, fieldH);
        panel.add(txtFax);

        // Row 7: Mẫu hóa đơn
        startY += rowGap;
        JLabel lblMauHD = new JLabel("Mẫu hóa đơn:");
        lblMauHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMauHD.setBounds(20, startY, lblW, fieldH);
        panel.add(lblMauHD);

        cbMauHoaDon = new JComboBox<>();
        cbMauHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMauHoaDon.setBounds(110, startY, 340, fieldH);
        panel.add(cbMauHoaDon);

        // Row 8: Checkboxes
        startY += rowGap;
        chkLuaChonMauKhiIn = new JCheckBox("Lựa chọn mẫu khi in", true);
        chkLuaChonMauKhiIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLuaChonMauKhiIn.setOpaque(false);
        chkLuaChonMauKhiIn.setBounds(110, startY, 250, 20);
        panel.add(chkLuaChonMauKhiIn);

        startY += 24;
        chkHienThiTruocKhiIn = new JCheckBox("Hiển thị trước khi in", true);
        chkHienThiTruocKhiIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiTruocKhiIn.setOpaque(false);
        chkHienThiTruocKhiIn.setBounds(110, startY, 250, 20);
        panel.add(chkHienThiTruocKhiIn);

        return panel;
    }

    // ==================== TAB: MẪU HÓA ĐƠN (NHÀ PHÁT TRIỂN) ====================
    private JPanel createTabMauHoaDon() {
        JPanel container = new JPanel(new BorderLayout(0, 8));
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(12, 16, 12, 16));

        // --- Top Header (Lời nhắc + Ô nhập mật khẩu + Nút mở khóa + Link Sửa mẫu) ---
        JPanel pnlTop = new JPanel(new BorderLayout(8, 4));
        pnlTop.setBackground(Color.WHITE);

        JLabel lblPrompt = new JLabel("Phần này dành cho nhà phát triển, mời bạn nhập mật khẩu:");
        lblPrompt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPrompt.setForeground(new Color(30, 30, 30));

        JPanel pnlAuthBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlAuthBox.setBackground(Color.WHITE);

        JPasswordField txtDevPass = new JPasswordField(10);
        txtDevPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDevPass.setPreferredSize(new Dimension(130, 24));

        JButton btnUnlock = new JButton("Mở khóa");
        btnUnlock.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnUnlock.setBackground(Color.WHITE);
        btnUnlock.setPreferredSize(new Dimension(85, 24));
        btnUnlock.setFocusPainted(false);

        pnlAuthBox.add(txtDevPass);
        pnlAuthBox.add(btnUnlock);

        JPanel pnlHeaderLeft = new JPanel(new BorderLayout(4, 6));
        pnlHeaderLeft.setBackground(Color.WHITE);
        pnlHeaderLeft.add(lblPrompt, BorderLayout.NORTH);
        pnlHeaderLeft.add(pnlAuthBox, BorderLayout.SOUTH);
        pnlTop.add(pnlHeaderLeft, BorderLayout.WEST);

        JLabel lblSuaMau = new JLabel("<html><u><font color='#0066cc'>Sửa mẫu</font></u></html>");
        lblSuaMau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSuaMau.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSuaMau.setVisible(false);
        lblSuaMau.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                STemplate selected = listTemplateMauHD != null ? listTemplateMauHD.getSelectedValue() : null;
                if (selected != null) {
                    openBillTemplateEditor(selected);
                } else {
                    JOptionPane.showMessageDialog(CauHinhToanHeThongDialog.this,
                            "Vui lòng chọn một mẫu in từ danh sách bên phải để sửa!",
                            "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        pnlHeaderRight.setBackground(Color.WHITE);
        pnlHeaderRight.add(lblSuaMau);
        pnlTop.add(pnlHeaderRight, BorderLayout.EAST);

        container.add(pnlTop, BorderLayout.NORTH);

        // --- Center Section: 2 cột (Cây Folder bên trái + Danh sách Mẫu bên phải) ---
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(8, 0, 0, 0));

        // 1. Cột trái: Thư mục
        folderMauHDListModel = new DefaultListModel<>();
        listFolderMauHD = new JList<>(folderMauHDListModel);
        listFolderMauHD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listFolderMauHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listFolderMauHD.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof STemplate) {
                    STemplate t = (STemplate) value;
                    label.setText(t.getName());
                    label.setIcon(IconHelper.getIcon("Folder2.png", 16, 16) != null ?
                            IconHelper.getIcon("Folder2.png", 16, 16) :
                            IconHelper.getIcon("Folder.png", 16, 16));
                }
                label.setBorder(new EmptyBorder(3, 6, 3, 6));
                return label;
            }
        });

        JScrollPane spFolders = new JScrollPane(listFolderMauHD);
        spFolders.setBorder(new MatteBorder(1, 1, 1, 1, new Color(180, 200, 220)));
        pnlCenter.add(spFolders);

        // 2. Cột phải: Mẫu in
        templateMauHDListModel = new DefaultListModel<>();
        listTemplateMauHD = new JList<>(templateMauHDListModel);
        listTemplateMauHD.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTemplateMauHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listTemplateMauHD.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof STemplate) {
                    STemplate t = (STemplate) value;
                    label.setText(t.getName());
                    label.setIcon(getTemplateIcon(t.getName()));
                }
                label.setBorder(new EmptyBorder(3, 6, 3, 6));
                return label;
            }
        });

        listTemplateMauHD.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    STemplate selected = listTemplateMauHD.getSelectedValue();
                    if (selected != null) {
                        openBillTemplateEditor(selected);
                    }
                }
            }
        });

        JScrollPane spTemplates = new JScrollPane(listTemplateMauHD);
        spTemplates.setBorder(new MatteBorder(1, 1, 1, 1, new Color(180, 200, 220)));
        pnlCenter.add(spTemplates);

        // Lắng nghe sự kiện click chọn thư mục
        listFolderMauHD.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                STemplate selectedFolder = listFolderMauHD.getSelectedValue();
                if (selectedFolder != null) {
                    loadTemplatesForFolder(selectedFolder);
                }
            }
        });

        container.add(pnlCenter, BorderLayout.CENTER);

        // --- Xử lý sự kiện mở khóa bằng mật khẩu "no1admin" ---
        Runnable doUnlock = () -> {
            String pass = new String(txtDevPass.getPassword()).trim();
            if ("no1admin".equals(pass)) {
                isDevUnlocked = true;
                pnlAuthBox.setVisible(false);
                lblPrompt.setText("Phần này dành cho nhà phát triển:");
                lblSuaMau.setVisible(true);
                pnlCenter.setVisible(true);
                loadFolderData();
                container.revalidate();
                container.repaint();
            } else {
                JOptionPane.showMessageDialog(CauHinhToanHeThongDialog.this,
                        "Mật khẩu không chính xác!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                txtDevPass.setText("");
                txtDevPass.requestFocus();
            }
        };

        btnUnlock.addActionListener(e -> doUnlock.run());
        txtDevPass.addActionListener(e -> doUnlock.run());

        // Kiểm tra trạng thái mở khóa ban đầu
        if (isDevUnlocked) {
            pnlAuthBox.setVisible(false);
            lblPrompt.setText("Phần này dành cho nhà phát triển:");
            lblSuaMau.setVisible(true);
            pnlCenter.setVisible(true);
            loadFolderData();
        } else {
            pnlAuthBox.setVisible(true);
            lblPrompt.setText("Phần này dành cho nhà phát triển, mời bạn nhập mật khẩu:");
            lblSuaMau.setVisible(false);
            pnlCenter.setVisible(false);
        }

        return container;
    }

    private void openBillTemplateEditor(STemplate selected) {
        if (selected == null || selected.getId() == null || selected.getId().isBlank()) {
            JOptionPane.showMessageDialog(this, "Mẫu được chọn không có ID hợp lệ.",
                    "Không thể mở mẫu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            BillTemplate javaTemplate = billTemplateDAO.findOrCreateDraft(selected.getId(), selected.getName());
            JrxmlEditorServer.open(javaTemplate);
            JOptionPane.showMessageDialog(this,
                    "Trình thiết kế đã mở trong trình duyệt mặc định.\n" +
                            "Lưu nháp không ảnh hưởng mẫu đang in; chọn Xuất bản khi muốn áp dụng.",
                    "Thiết kế mẫu bill", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể mở trình thiết kế bill Java:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void loadFolderData() {
        if (folderMauHDListModel == null) return;
        folderMauHDListModel.clear();
        List<STemplate> folders = templateDAO.getFolderList();
        for (STemplate f : folders) {
            folderMauHDListModel.addElement(f);
        }
        if (!folderMauHDListModel.isEmpty()) {
            listFolderMauHD.setSelectedIndex(0);
        }
    }

    private void loadTemplatesForFolder(STemplate folder) {
        if (templateMauHDListModel == null || folder == null) return;
        templateMauHDListModel.clear();
        List<STemplate> templates = templateDAO.getTemplatesByFolder(folder.getId(), folder.getName());
        for (STemplate t : templates) {
            templateMauHDListModel.addElement(t);
        }
        if (!templateMauHDListModel.isEmpty()) {
            listTemplateMauHD.setSelectedIndex(0);
        }
    }

    // ==================== TAB 2: MÁY IN ====================
    private JPanel createTabMayIn() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        String[] docTypes = {
                "Báo giá", "Đơn hàng", "Phiếu nhập kho", "Phiếu xuất kho",
                "Phiếu chuyển kho", "Phiếu kiểm kê", "Sản xuất", "Đặt hàng",
                "Phiếu thu", "Phiếu chi", "Phiếu thu công nợ", "Bảng lương", "Đặt hàng nhà cung cấp"
        };

        String[] detectedPrinters = getAvailablePrinters();

        int lblW = 160;
        int fieldH = 22;
        int startY = 12;
        int rowGap = 26;

        for (String type : docTypes) {
            JLabel lbl = new JLabel(type);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setBounds(25, startY, lblW, fieldH);
            panel.add(lbl);

            JComboBox<String> cb = new JComboBox<>(detectedPrinters);
            cb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cb.setBackground(Color.WHITE);
            cb.setBounds(190, startY, 380, fieldH);
            panel.add(cb);
            printerFields.put(type, cb);

            startY += rowGap;
        }

        panel.setPreferredSize(new Dimension(580, startY + 20));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private String[] getAvailablePrinters() {
        List<String> list = new ArrayList<>();
        list.add("");

        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            if (services != null && services.length > 0) {
                for (PrintService ps : services) {
                    String name = ps.getName();
                    if (name != null && !name.trim().isEmpty() && !list.contains(name.trim())) {
                        list.add(name.trim());
                    }
                }
            }
        } catch (Exception ignored) {}

        if (list.size() <= 1) {
            list.add("Microsoft Print to PDF");
            list.add("Fax");
            list.add("Microsoft XPS Document Writer");
        } else {
            if (!list.contains("Microsoft Print to PDF")) list.add("Microsoft Print to PDF");
            if (!list.contains("Microsoft XPS Document Writer")) list.add("Microsoft XPS Document Writer");
        }

        return list.toArray(new String[0]);
    }

    // ==================== TAB 3: SỐ PHIẾU ====================
    private JPanel createTabSoPhieu() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        String[] docTypes = {
                "Báo giá", "Đơn hàng", "Phiếu nhập kho", "Phiếu xuất kho",
                "Phiếu chuyển kho", "Phiếu kiểm kê", "Sản xuất", "Đặt hàng",
                "Phiếu thu", "Phiếu chi", "Phiếu thu công nợ", "Bảng lương", "Đặt hàng nhà cung cấp"
        };

        int lblW = 160;
        int fieldH = 22;
        int startY = 12;
        int rowGap = 26;

        for (String type : docTypes) {
            JLabel lbl = new JLabel(type);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setBounds(25, startY, lblW, fieldH);
            panel.add(lbl);

            JTextField txt = new JTextField();
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txt.setEditable(false);
            txt.setBackground(new Color(250, 252, 255));
            txt.setBounds(190, startY, 340, fieldH);
            panel.add(txt);
            voucherFields.put(type, txt);

            JButton btnDots = new JButton("...");
            btnDots.setFont(new Font("Segoe UI", Font.BOLD, 10));
            btnDots.setBounds(535, startY, 35, fieldH);
            btnDots.setBackground(Color.WHITE);
            btnDots.setMargin(new Insets(1, 1, 1, 1));
            btnDots.addActionListener(e -> openCauHinhCachSinh(type, txt));
            panel.add(btnDots);

            startY += rowGap;
        }

        panel.setPreferredSize(new Dimension(580, startY + 20));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private void openCauHinhCachSinh(String docType, JTextField targetField) {
        String currentPattern = targetField.getText().trim();
        CauHinhCachSinhDialog dialog = new CauHinhCachSinhDialog(this, docType, currentPattern, newPattern -> {
            targetField.setText(newPattern);
        });
        dialog.setVisible(true);
    }

    // ==================== TAB 4: BÁN HÀNG ====================
    private JPanel createTabBanHang() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 12;
        int fieldH = 22;
        int rowGap = 26;

        // 1. Cho phép trùng tên khách hàng (Default: true)
        chkChoPhepTrungTenKhachHang = new JCheckBox("Cho phép trùng tên khách hàng", true);
        chkChoPhepTrungTenKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepTrungTenKhachHang.setOpaque(false);
        chkChoPhepTrungTenKhachHang.setBounds(20, startY, 280, fieldH);
        panel.add(chkChoPhepTrungTenKhachHang);

        // 2. Cho phép nhập giảm giá & Mặc định giảm giá (%) (Default: true / 0)
        startY += rowGap;
        chkChoPhepNhapGiamGia = new JCheckBox("Cho phép nhập giảm giá", true);
        chkChoPhepNhapGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepNhapGiamGia.setOpaque(false);
        chkChoPhepNhapGiamGia.setBounds(20, startY, 230, fieldH);
        panel.add(chkChoPhepNhapGiamGia);

        JLabel lblGiamGiaPt = new JLabel("Mặc định giảm giá (%)");
        lblGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiamGiaPt.setBounds(290, startY, 140, fieldH);
        panel.add(lblGiamGiaPt);

        txtMacDinhGiamGiaPt = new JTextField("0");
        txtMacDinhGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMacDinhGiamGiaPt.setBounds(440, startY, 65, fieldH);
        panel.add(txtMacDinhGiamGiaPt);

        // 3. Bán hàng dùng đầu đọc mã vạch (Default: true)
        startY += rowGap;
        chkBanHangDungDauDocMaVach = new JCheckBox("Bán hàng dùng đầu đọc mã vạch", true);
        chkBanHangDungDauDocMaVach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBanHangDungDauDocMaVach.setOpaque(false);
        chkBanHangDungDauDocMaVach.setBounds(20, startY, 280, fieldH);
        panel.add(chkBanHangDungDauDocMaVach);

        // 4. Số lần in (Default: 1)
        startY += rowGap;
        JLabel lblSoLanIn = new JLabel("Số lần in");
        lblSoLanIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoLanIn.setBounds(25, startY, 100, fieldH);
        panel.add(lblSoLanIn);

        txtSoLanIn = new JTextField("1");
        txtSoLanIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoLanIn.setBounds(150, startY, 65, fieldH);
        panel.add(txtSoLanIn);

        // 5. Hiển thị cửa sổ nhập số lượng khi quét mã vạch (Default: false)
        startY += rowGap;
        chkHienThiCuaSoNhapSoLuong = new JCheckBox("Hiển thị cửa sổ nhập số lượng khi quét mã vạch", false);
        chkHienThiCuaSoNhapSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiCuaSoNhapSoLuong.setOpaque(false);
        chkHienThiCuaSoNhapSoLuong.setBounds(20, startY, 340, fieldH);
        panel.add(chkHienThiCuaSoNhapSoLuong);

        // 6. Cho phép thay đổi ngày trên hóa đơn (Default: true)
        startY += rowGap;
        chkChoPhepThayDoiNgay = new JCheckBox("Cho phép thay đổi ngày trên hóa đơn", true);
        chkChoPhepThayDoiNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepThayDoiNgay.setOpaque(false);
        chkChoPhepThayDoiNgay.setBounds(20, startY, 340, fieldH);
        panel.add(chkChoPhepThayDoiNgay);

        // 7. Có phí vận chuyển & Có thuế suất (Default: false / false)
        startY += rowGap;
        chkCoPhiVanChuyen = new JCheckBox("Có phí vận chuyển", false);
        chkCoPhiVanChuyen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoPhiVanChuyen.setOpaque(false);
        chkCoPhiVanChuyen.setBounds(20, startY, 200, fieldH);
        panel.add(chkCoPhiVanChuyen);

        chkCoThueSuat = new JCheckBox("Có thuế suất", false);
        chkCoThueSuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoThueSuat.setOpaque(false);
        chkCoThueSuat.setBounds(290, startY, 150, fieldH);
        panel.add(chkCoThueSuat);

        // 8. Mặc định thuế suất (Default: 0)
        startY += rowGap;
        JLabel lblThueSuat = new JLabel("Mặc định thuế suất");
        lblThueSuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThueSuat.setBounds(25, startY, 120, fieldH);
        panel.add(lblThueSuat);

        txtMacDinhThueSuat = new JTextField("0");
        txtMacDinhThueSuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMacDinhThueSuat.setBounds(150, startY, 65, fieldH);
        panel.add(txtMacDinhThueSuat);

        // 9. Bắt buộc nhập khách hàng (Default: false)
        startY += rowGap;
        chkBatBuocNhapKhachHang = new JCheckBox("Bắt buộc nhập khách hàng", false);
        chkBatBuocNhapKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBatBuocNhapKhachHang.setOpaque(false);
        chkBatBuocNhapKhachHang.setBounds(20, startY, 300, fieldH);
        panel.add(chkBatBuocNhapKhachHang);

        // 10. Bắt buộc nhập nhân viên bán hàng (Default: false)
        startY += rowGap;
        chkBatBuocNhapNVBH = new JCheckBox("Bắt buộc nhập nhân viên bán hàng", false);
        chkBatBuocNhapNVBH.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBatBuocNhapNVBH.setOpaque(false);
        chkBatBuocNhapNVBH.setBounds(20, startY, 300, fieldH);
        panel.add(chkBatBuocNhapNVBH);

        // 11. Cho phép in tạm tính (Default: true)
        startY += rowGap;
        chkChoPhepInTamTinh = new JCheckBox("Cho phép in tạm tính", true);
        chkChoPhepInTamTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepInTamTinh.setOpaque(false);
        chkChoPhepInTamTinh.setBounds(20, startY, 300, fieldH);
        panel.add(chkChoPhepInTamTinh);

        // 12. Bắt buộc đổi trả hàng theo đơn & Số ngày cho phép đổi trả (Default: true / 7)
        startY += rowGap;
        chkBatBuocDoiTraTheoDon = new JCheckBox("Bắt buộc đổi trả hàng theo đơn", true);
        chkBatBuocDoiTraTheoDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBatBuocDoiTraTheoDon.setOpaque(false);
        chkBatBuocDoiTraTheoDon.setBounds(20, startY, 240, fieldH);
        panel.add(chkBatBuocDoiTraTheoDon);

        JLabel lblSoNgayDoiTra = new JLabel("Số ngày cho phép đổi trả");
        lblSoNgayDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoNgayDoiTra.setBounds(290, startY, 150, fieldH);
        panel.add(lblSoNgayDoiTra);

        txtSoNgayChoPhepDoiTra = new JTextField("7");
        txtSoNgayChoPhepDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoNgayChoPhepDoiTra.setBounds(440, startY, 65, fieldH);
        panel.add(txtSoNgayChoPhepDoiTra);

        // 13. Hiển thị ảnh sản phẩm trong giao diện bán hàng (Default: true)
        startY += rowGap;
        chkHienThiAnhSanPham = new JCheckBox("Hiển thị ảnh sản phẩm trong giao diện bán hàng", true);
        chkHienThiAnhSanPham.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiAnhSanPham.setOpaque(false);
        chkHienThiAnhSanPham.setBounds(20, startY, 350, fieldH);
        panel.add(chkHienThiAnhSanPham);

        // 14. Kích hoạt khuyến mại tự động (Default: true)
        startY += rowGap;
        chkKichHoatKhuyenMaiTuDong = new JCheckBox("Kích hoạt khuyến mại tự động", true);
        chkKichHoatKhuyenMaiTuDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKichHoatKhuyenMaiTuDong.setOpaque(false);
        chkKichHoatKhuyenMaiTuDong.setBounds(20, startY, 350, fieldH);
        panel.add(chkKichHoatKhuyenMaiTuDong);

        // 15. Hiển thị mặt hàng khuyến mại khi thanh toán (Default: true)
        startY += rowGap;
        chkHienThiMatHangKhuyenMaiKhiThanhToan = new JCheckBox("Hiển thị mặt hàng khuyến mại khi thanh toán", true);
        chkHienThiMatHangKhuyenMaiKhiThanhToan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiMatHangKhuyenMaiKhiThanhToan.setOpaque(false);
        chkHienThiMatHangKhuyenMaiKhiThanhToan.setBounds(20, startY, 350, fieldH);
        panel.add(chkHienThiMatHangKhuyenMaiKhiThanhToan);

        // 16. Có giao hàng (Default: false)
        startY += rowGap;
        chkCoGiaoHang = new JCheckBox("Có giao hàng", false);
        chkCoGiaoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoGiaoHang.setOpaque(false);
        chkCoGiaoHang.setBounds(20, startY, 300, fieldH);
        panel.add(chkCoGiaoHang);

        // 17. Sử dụng giá theo khách (Default: false)
        startY += rowGap;
        chkSuDungGiaTheoKhach = new JCheckBox("Sử dụng giá theo khách", false);
        chkSuDungGiaTheoKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungGiaTheoKhach.setOpaque(false);
        chkSuDungGiaTheoKhach.setBounds(20, startY, 300, fieldH);
        panel.add(chkSuDungGiaTheoKhach);

        // 18. Sử dụng giảm giá mặc định theo mặt hàng (Default: true)
        startY += rowGap;
        chkSuDungGiamGiaMacDinh = new JCheckBox("Sử dụng giảm giá mặc định theo mặt hàng", true);
        chkSuDungGiamGiaMacDinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungGiamGiaMacDinh.setOpaque(false);
        chkSuDungGiamGiaMacDinh.setBounds(20, startY, 350, fieldH);
        panel.add(chkSuDungGiamGiaMacDinh);

        // 19. Làm tròn tiền & (0: không làm tròn) (Default: "1,000")
        startY += rowGap;
        JLabel lblLamTron = new JLabel("Làm tròn tiền");
        lblLamTron.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLamTron.setBounds(25, startY, 120, fieldH);
        panel.add(lblLamTron);

        txtLamTronTien = new JTextField("1,000");
        txtLamTronTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLamTronTien.setBounds(150, startY, 65, fieldH);
        panel.add(txtLamTronTien);

        startY += 20;
        JLabel lblSubNote = new JLabel("(0: không làm tròn)");
        lblSubNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSubNote.setForeground(new Color(100, 100, 100));
        lblSubNote.setBounds(150, startY, 150, 18);
        panel.add(lblSubNote);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 5: THANH TOÁN ====================
    private JPanel createTabThanhToan() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 22;
        int rowGap = 28;

        // 1. Có thanh toán voucher (Default: false)
        chkCoThanhToanVoucher = new JCheckBox("Có thanh toán voucher", false);
        chkCoThanhToanVoucher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoThanhToanVoucher.setOpaque(false);
        chkCoThanhToanVoucher.setBounds(20, startY, 280, fieldH);
        panel.add(chkCoThanhToanVoucher);

        // 2. Cho phép khách nợ (Default: false)
        startY += rowGap;
        chkChoPhepKhachNo = new JCheckBox("Cho phép khách nợ", false);
        chkChoPhepKhachNo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepKhachNo.setBounds(20, startY, 280, fieldH);
        panel.add(chkChoPhepKhachNo);

        // 3. Lựa chọn voucher từ danh sách (Default: false)
        startY += rowGap;
        chkLuaChonVoucherTuDanhSach = new JCheckBox("Lựa chọn voucher từ danh sách", false);
        chkLuaChonVoucherTuDanhSach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLuaChonVoucherTuDanhSach.setOpaque(false);
        chkLuaChonVoucherTuDanhSach.setBounds(20, startY, 280, fieldH);
        panel.add(chkLuaChonVoucherTuDanhSach);

        // 4. Sử dụng thẻ trả trước (Default: false)
        startY += rowGap;
        chkSuDungTheTraTruoc = new JCheckBox("Sử dụng thẻ trả trước", false);
        chkSuDungTheTraTruoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungTheTraTruoc.setOpaque(false);
        chkSuDungTheTraTruoc.setBounds(20, startY, 280, fieldH);
        panel.add(chkSuDungTheTraTruoc);

        // 5. Sử dụng điểm tích lũy để thanh toán (Default: false)
        startY += rowGap;
        chkSuDungDiemTichLuyDeThanhToan = new JCheckBox("Sử dụng điểm tích lũy để thanh toán", false);
        chkSuDungDiemTichLuyDeThanhToan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungDiemTichLuyDeThanhToan.setOpaque(false);
        chkSuDungDiemTichLuyDeThanhToan.setBounds(20, startY, 300, fieldH);
        panel.add(chkSuDungDiemTichLuyDeThanhToan);

        // 6. Quy đổi 1 điểm sang tiền (Default: "1,000")
        startY += rowGap;
        JLabel lblQuyDoiDiem = new JLabel("Quy đổi 1 điểm sang tiền");
        lblQuyDoiDiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQuyDoiDiem.setBounds(25, startY, 160, fieldH);
        panel.add(lblQuyDoiDiem);

        txtQuyDoi1DiemSangTien = new JTextField("1,000");
        txtQuyDoi1DiemSangTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtQuyDoi1DiemSangTien.setBounds(190, startY, 75, fieldH);
        panel.add(txtQuyDoi1DiemSangTien);

        // 7. Có thanh toán thẻ (Default: true)
        startY += rowGap;
        chkCoThanhToanThe = new JCheckBox("Có thanh toán thẻ", true);
        chkCoThanhToanThe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoThanhToanThe.setOpaque(false);
        chkCoThanhToanThe.setBounds(20, startY, 280, fieldH);
        panel.add(chkCoThanhToanThe);

        // 8. Có thanh toán chuyển khoản (Default: false)
        startY += rowGap;
        chkCoThanhToanChuyenKhoan = new JCheckBox("Có thanh toán chuyển khoản", false);
        chkCoThanhToanChuyenKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoThanhToanChuyenKhoan.setOpaque(false);
        chkCoThanhToanChuyenKhoan.setBounds(20, startY, 280, fieldH);
        panel.add(chkCoThanhToanChuyenKhoan);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 6: TÍCH ĐIỂM ====================
    private JPanel createTabTichDiem() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 24;
        int rowGap = 30;

        // 1. Doanh số tương ứng với 1 điểm (Default: "20,000")
        JLabel lblDoanhSo = new JLabel("Doanh số tương ứng với 1 điểm");
        lblDoanhSo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDoanhSo.setBounds(25, startY, 190, fieldH);
        panel.add(lblDoanhSo);

        txtDoanhSoTuongUng1Diem = new JTextField("20,000");
        txtDoanhSoTuongUng1Diem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDoanhSoTuongUng1Diem.setBounds(225, startY, 80, fieldH);
        panel.add(txtDoanhSoTuongUng1Diem);

        // 2. Cách tính điểm
        startY += rowGap;
        JLabel lblCachTinh = new JLabel("Cách tính điểm");
        lblCachTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCachTinh.setBounds(25, startY, 190, fieldH);
        panel.add(lblCachTinh);

        cbCachTinhDiem = new JComboBox<>(new String[]{
                "Điểm được tính trên từng hóa đơn",
                "Điểm được tính trên tổng doanh số"
        });
        cbCachTinhDiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCachTinhDiem.setBounds(225, startY, 320, fieldH);
        panel.add(cbCachTinhDiem);

        // 3. Tự động nâng cấp thành viên khi đạt hạn mức (Default: true)
        startY += rowGap + 4;
        chkTuDongNangCapThanhVien = new JCheckBox("Tự động nâng cấp thành viên khi đạt hạn mức", true);
        chkTuDongNangCapThanhVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongNangCapThanhVien.setOpaque(false);
        chkTuDongNangCapThanhVien.setBounds(225, startY, 350, fieldH);
        panel.add(chkTuDongNangCapThanhVien);

        // 4. Hiển thị điểm của khách hàng trên hóa đơn (Default: true)
        startY += rowGap;
        chkHienThiDiemKhachHang = new JCheckBox("Hiển thị điểm của khách hàng trên hóa đơn", true);
        chkHienThiDiemKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiDiemKhachHang.setOpaque(false);
        chkHienThiDiemKhachHang.setBounds(225, startY, 350, fieldH);
        panel.add(chkHienThiDiemKhachHang);

        // 5. Không tích điểm nếu có khuyến mại (Default: false)
        startY += rowGap;
        chkKhongTichDiemNeuCoKhuyenMai = new JCheckBox("Không tích điểm nếu có khuyến mại", false);
        chkKhongTichDiemNeuCoKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhongTichDiemNeuCoKhuyenMai.setOpaque(false);
        chkKhongTichDiemNeuCoKhuyenMai.setBounds(225, startY, 350, fieldH);
        panel.add(chkKhongTichDiemNeuCoKhuyenMai);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 7: KHO HÀNG ====================
    private JPanel createTabKhoHang() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 22;
        int rowGap = 28;

        // 1. Cho phép nhập một mặt hàng nhiều lần trong phiếu (Default: false)
        chkChoPhepNhapMotMatHangNhieuLan = new JCheckBox("Cho phép nhập một mặt hàng nhiều lần trong phiếu", false);
        chkChoPhepNhapMotMatHangNhieuLan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepNhapMotMatHangNhieuLan.setOpaque(false);
        chkChoPhepNhapMotMatHangNhieuLan.setBounds(20, startY, 380, fieldH);
        panel.add(chkChoPhepNhapMotMatHangNhieuLan);

        // 2. Bắt buộc chọn nhân viên trong nhập kho (Default: false)
        startY += rowGap;
        chkBatBuocChonNhanVienNhapKho = new JCheckBox("Bắt buộc chọn nhân viên trong nhập kho", false);
        chkBatBuocChonNhanVienNhapKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBatBuocChonNhanVienNhapKho.setOpaque(false);
        chkBatBuocChonNhanVienNhapKho.setBounds(20, startY, 380, fieldH);
        panel.add(chkBatBuocChonNhanVienNhapKho);

        // 3. Bắt buộc chọn nhà cung cấp trong nhập kho (Default: false)
        startY += rowGap;
        chkBatBuocChonNhaCungCapNhapKho = new JCheckBox("Bắt buộc chọn nhà cung cấp trong nhập kho", false);
        chkBatBuocChonNhaCungCapNhapKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkBatBuocChonNhaCungCapNhapKho.setOpaque(false);
        chkBatBuocChonNhaCungCapNhapKho.setBounds(20, startY, 380, fieldH);
        panel.add(chkBatBuocChonNhaCungCapNhapKho);

        // 4. Sử dụng 2 đơn vị tính (Default: false)
        startY += rowGap;
        chkSuDung2DonViTinh = new JCheckBox("Sử dụng 2 đơn vị tính", false);
        chkSuDung2DonViTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDung2DonViTinh.setOpaque(false);
        chkSuDung2DonViTinh.setBounds(20, startY, 380, fieldH);
        panel.add(chkSuDung2DonViTinh);

        // 5. Tự động tính giá vốn (Default: true)
        startY += rowGap;
        chkTuDongTinhGiaVon = new JCheckBox("Tự động tính giá vốn", true);
        chkTuDongTinhGiaVon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongTinhGiaVon.setOpaque(false);
        chkTuDongTinhGiaVon.setBounds(20, startY, 380, fieldH);
        panel.add(chkTuDongTinhGiaVon);

        // 6. Nhập kho bằng đầu đọc mã vạch (Default: true)
        startY += rowGap;
        chkNhapKhoBangDauDocMaVach = new JCheckBox("Nhập kho bằng đầu đọc mã vạch", true);
        chkNhapKhoBangDauDocMaVach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkNhapKhoBangDauDocMaVach.setOpaque(false);
        chkNhapKhoBangDauDocMaVach.setBounds(20, startY, 380, fieldH);
        panel.add(chkNhapKhoBangDauDocMaVach);

        // 7. Giá sử dụng khi chuyển xuất kho
        startY += rowGap;
        JLabel lblGiaXuat = new JLabel("Giá sử dụng khi chuyển xuất kho");
        lblGiaXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaXuat.setBounds(25, startY, 200, fieldH);
        panel.add(lblGiaXuat);

        cbGiaSuDungKhiChuyenXuatKho = new JComboBox<>(new String[]{
                "Giá bán",
                "Giá nhập",
                "Giá vốn"
        });
        cbGiaSuDungKhiChuyenXuatKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbGiaSuDungKhiChuyenXuatKho.setSelectedItem("Giá vốn");
        cbGiaSuDungKhiChuyenXuatKho.setBounds(230, startY, 300, fieldH);
        panel.add(cbGiaSuDungKhiChuyenXuatKho);

        // 8. Sử dụng chức năng sản xuất (Default: false)
        startY += rowGap;
        chkSuDungChucNangSanXuat = new JCheckBox("Sử dụng chức năng sản xuất", false);
        chkSuDungChucNangSanXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungChucNangSanXuat.setOpaque(false);
        chkSuDungChucNangSanXuat.setBounds(20, startY, 380, fieldH);
        panel.add(chkSuDungChucNangSanXuat);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 8: MẶT HÀNG ====================
    private JPanel createTabMatHang() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 12;
        int fieldH = 22;
        int rowGap = 26;

        // Row 1: Sử dụng mã sẵn có & Sử dụng mã hàng (Default: false / true)
        chkSuDungMaSanCo = new JCheckBox("Sử dụng mã sẵn có", false);
        chkSuDungMaSanCo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungMaSanCo.setOpaque(false);
        chkSuDungMaSanCo.setBounds(20, startY, 200, fieldH);
        panel.add(chkSuDungMaSanCo);

        chkSuDungMaHang = new JCheckBox("Sử dụng mã hàng", true);
        chkSuDungMaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungMaHang.setOpaque(false);
        chkSuDungMaHang.setBounds(330, startY, 200, fieldH);
        panel.add(chkSuDungMaHang);

        // Row 2: Tự động sinh mã vạch (Default: true)
        startY += rowGap;
        chkTuDongSinhMaVach = new JCheckBox("Tự động sinh mã vạch", true);
        chkTuDongSinhMaVach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongSinhMaVach.setOpaque(false);
        chkTuDongSinhMaVach.setBounds(20, startY, 250, fieldH);
        panel.add(chkTuDongSinhMaVach);

        // Row 3: Chiều dài mã vạch & Có ký tự kiểm tra (Default: 7 / false)
        startY += rowGap;
        JLabel lblChieuDai = new JLabel("Chiều dài mã vạch");
        lblChieuDai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChieuDai.setBounds(45, startY, 120, fieldH);
        panel.add(lblChieuDai);

        txtChieuDaiMaVach = new JTextField("7");
        txtChieuDaiMaVach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtChieuDaiMaVach.setBounds(175, startY, 55, fieldH);
        panel.add(txtChieuDaiMaVach);

        chkCoKyTuKiemTra = new JCheckBox("Có ký tự kiểm tra", false);
        chkCoKyTuKiemTra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoKyTuKiemTra.setOpaque(false);
        chkCoKyTuKiemTra.setBounds(270, startY, 200, fieldH);
        panel.add(chkCoKyTuKiemTra);

        // Row 4: Sử dụng tem in bằng cân điện tử & Chuỗi bắt đầu (Default: true / "29")
        startY += rowGap;
        chkSuDungTemInBangCanDienTu = new JCheckBox("Sử dụng tem in bằng cân điện tử", true);
        chkSuDungTemInBangCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungTemInBangCanDienTu.setOpaque(false);
        chkSuDungTemInBangCanDienTu.setBounds(20, startY, 240, fieldH);
        panel.add(chkSuDungTemInBangCanDienTu);

        JLabel lblChuoiBD = new JLabel("Chuỗi bắt đầu");
        lblChuoiBD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChuoiBD.setBounds(280, startY, 90, fieldH);
        panel.add(lblChuoiBD);

        txtChuoiBatDauCanDienTu = new JTextField("29");
        txtChuoiBatDauCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtChuoiBatDauCanDienTu.setBounds(375, startY, 155, fieldH);
        panel.add(txtChuoiBatDauCanDienTu);

        // Row 5: Tìm theo & Cách tìm
        startY += rowGap;
        JLabel lblTimTheo = new JLabel("Tìm theo");
        lblTimTheo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTimTheo.setBounds(25, startY, 70, fieldH);
        panel.add(lblTimTheo);

        cbTimTheo = new JComboBox<>(new String[]{
                "Cả tên hàng và mã hàng",
                "Tên hàng",
                "Mã hàng"
        });
        cbTimTheo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTimTheo.setBounds(100, startY, 160, fieldH);
        panel.add(cbTimTheo);

        JLabel lblCachTim = new JLabel("Cách tìm");
        lblCachTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCachTim.setBounds(280, startY, 70, fieldH);
        panel.add(lblCachTim);

        cbCachTim = new JComboBox<>(new String[]{
                "Tìm có chứa",
                "Tìm bắt đầu",
                "Tìm chính xác"
        });
        cbCachTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbCachTim.setBounds(375, startY, 155, fieldH);
        panel.add(cbCachTim);

        // Row 6: Cho phép trùng tên mặt hàng & Hàng có bảo hành (Default: true / true)
        startY += rowGap;
        chkChoPhepTrungTenMatHang = new JCheckBox("Cho phép trùng tên mặt hàng", true);
        chkChoPhepTrungTenMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChoPhepTrungTenMatHang.setOpaque(false);
        chkChoPhepTrungTenMatHang.setBounds(20, startY, 240, fieldH);
        panel.add(chkChoPhepTrungTenMatHang);

        chkHangCoBaoHanh = new JCheckBox("Hàng có bảo hành", true);
        chkHangCoBaoHanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHangCoBaoHanh.setOpaque(false);
        chkHangCoBaoHanh.setBounds(330, startY, 200, fieldH);
        panel.add(chkHangCoBaoHanh);

        // Row 7: Sử dụng hãng sản xuất (Default: true)
        startY += rowGap;
        chkSuDungHangSanXuat = new JCheckBox("Sử dụng hãng sản xuất", true);
        chkSuDungHangSanXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungHangSanXuat.setOpaque(false);
        chkSuDungHangSanXuat.setBounds(20, startY, 250, fieldH);
        panel.add(chkSuDungHangSanXuat);

        // Row 8: Tiêu đề Cấu hình sử dụng nhiều giá bán
        startY += rowGap + 4;
        JLabel lblCauHinhGia = new JLabel("Cấu hình sử dụng nhiều giá bán trên mặt hàng hay không");
        lblCauHinhGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCauHinhGia.setBounds(25, startY, 400, fieldH);
        panel.add(lblCauHinhGia);

        // Row 9: Sử dụng giá 2 & Diễn giải giá 2 (Default: false / "Đơn giá 2")
        startY += rowGap;
        chkSuDungGia2 = new JCheckBox("Sử dụng giá 2", false);
        chkSuDungGia2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungGia2.setOpaque(false);
        chkSuDungGia2.setBounds(20, startY, 150, fieldH);
        panel.add(chkSuDungGia2);

        JLabel lblDienGiai2 = new JLabel("Diễn giải giá 2");
        lblDienGiai2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDienGiai2.setBounds(280, startY, 90, fieldH);
        panel.add(lblDienGiai2);

        txtDienGiaiGia2 = new JTextField("Đơn giá 2");
        txtDienGiaiGia2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiaiGia2.setBounds(375, startY, 155, fieldH);
        panel.add(txtDienGiaiGia2);

        // Row 10: Sử dụng giá 3 & Diễn giải giá 3 (Default: false / "Giá bán 3")
        startY += rowGap;
        chkSuDungGia3 = new JCheckBox("Sử dụng giá 3", false);
        chkSuDungGia3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungGia3.setOpaque(false);
        chkSuDungGia3.setBounds(20, startY, 150, fieldH);
        panel.add(chkSuDungGia3);

        JLabel lblDienGiai3 = new JLabel("Diễn giải giá 3");
        lblDienGiai3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDienGiai3.setBounds(280, startY, 90, fieldH);
        panel.add(lblDienGiai3);

        txtDienGiaiGia3 = new JTextField("Giá bán 3");
        txtDienGiaiGia3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiaiGia3.setBounds(375, startY, 155, fieldH);
        panel.add(txtDienGiaiGia3);

        // Row 11: Sử dụng giá 4 & Diễn giải giá 4 (Default: false / "Giá bán 4")
        startY += rowGap;
        chkSuDungGia4 = new JCheckBox("Sử dụng giá 4", false);
        chkSuDungGia4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkSuDungGia4.setOpaque(false);
        chkSuDungGia4.setBounds(20, startY, 150, fieldH);
        panel.add(chkSuDungGia4);

        JLabel lblDienGiai4 = new JLabel("Diễn giải giá 4");
        lblDienGiai4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDienGiai4.setBounds(280, startY, 90, fieldH);
        panel.add(lblDienGiai4);

        txtDienGiaiGia4 = new JTextField("Giá bán 4");
        txtDienGiaiGia4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiaiGia4.setBounds(375, startY, 155, fieldH);
        panel.add(txtDienGiaiGia4);

        // Row 12: Hiển thị để lựa chọn nếu có nhiều giá (Default: true)
        startY += rowGap;
        chkHienThiDeLuaChonNeuCoNhieuGia = new JCheckBox("Hiển thị để lựa chọn nếu có nhiều giá", true);
        chkHienThiDeLuaChonNeuCoNhieuGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiDeLuaChonNeuCoNhieuGia.setOpaque(false);
        chkHienThiDeLuaChonNeuCoNhieuGia.setBounds(20, startY, 320, fieldH);
        panel.add(chkHienThiDeLuaChonNeuCoNhieuGia);

        // Row 13: Sắp xếp thứ tự theo (Default: "Mã hàng")
        startY += rowGap;
        JLabel lblSapXep = new JLabel("Sắp xếp thứ tự theo");
        lblSapXep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSapXep.setBounds(25, startY, 130, fieldH);
        panel.add(lblSapXep);

        cbSapXepThuTuTheo = new JComboBox<>(new String[]{
                "Mã hàng",
                "Tên hàng"
        });
        cbSapXepThuTuTheo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbSapXepThuTuTheo.setBounds(165, startY, 365, fieldH);
        panel.add(cbSapXepThuTuTheo);

        // Row 14: Mặt hàng có hạn sử dụng (Default: false)
        startY += rowGap;
        chkMatHangCoHanSuDung = new JCheckBox("Mặt hàng có hạn sử dụng", false);
        chkMatHangCoHanSuDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkMatHangCoHanSuDung.setOpaque(false);
        chkMatHangCoHanSuDung.setBounds(20, startY, 300, fieldH);
        panel.add(chkMatHangCoHanSuDung);

        // Row 15: Mặt hàng có kích thước (Default: false)
        startY += rowGap;
        chkMatHangCoKichThuoc = new JCheckBox("Mặt hàng có kích thước", false);
        chkMatHangCoKichThuoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkMatHangCoKichThuoc.setOpaque(false);
        chkMatHangCoKichThuoc.setBounds(20, startY, 300, fieldH);
        panel.add(chkMatHangCoKichThuoc);

        // Row 16: Mặt hàng có vật tư định lượng (Default: true)
        startY += rowGap;
        chkMatHangCoVatTuDinhLuong = new JCheckBox("Mặt hàng có vật tư định lượng", true);
        chkMatHangCoVatTuDinhLuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkMatHangCoVatTuDinhLuong.setOpaque(false);
        chkMatHangCoVatTuDinhLuong.setBounds(20, startY, 300, fieldH);
        panel.add(chkMatHangCoVatTuDinhLuong);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB: CÂY HIỂN THỊ GIÁ ====================
    private JPanel createTabCayHienThiGia() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        Font normalFont = new Font("Segoe UI", Font.PLAIN, 12);
        int fieldH = 24;

        chkCoCayHienThiGia = new JCheckBox("Có cây hiển thị giá", false);
        chkCoCayHienThiGia.setFont(normalFont);
        chkCoCayHienThiGia.setOpaque(false);
        chkCoCayHienThiGia.setBounds(10, 10, 230, fieldH);
        panel.add(chkCoCayHienThiGia);

        chkCayHienThiGiaMotDong = new JCheckBox("Cây hiển thị giá 1 dòng", false);
        chkCayHienThiGiaMotDong.setFont(normalFont);
        chkCayHienThiGiaMotDong.setOpaque(false);
        chkCayHienThiGiaMotDong.setBounds(310, 10, 230, fieldH);
        panel.add(chkCayHienThiGiaMotDong);

        JLabel lblCongSuDung = new JLabel("Cổng sử dụng");
        lblCongSuDung.setFont(normalFont);
        lblCongSuDung.setBounds(15, 48, 100, fieldH);
        panel.add(lblCongSuDung);

        txtCongCayHienThiGia = new JTextField();
        txtCongCayHienThiGia.setFont(normalFont);
        txtCongCayHienThiGia.setBounds(115, 48, 455, fieldH);
        panel.add(txtCongCayHienThiGia);

        JLabel lblViDuCong = new JLabel("Ví dụ: COM1");
        lblViDuCong.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblViDuCong.setBounds(120, 74, 130, 20);
        panel.add(lblViDuCong);

        chkCoManHinhPhu = new JCheckBox("Có màn hình phụ", false);
        chkCoManHinhPhu.setFont(normalFont);
        chkCoManHinhPhu.setOpaque(false);
        chkCoManHinhPhu.setBounds(10, 104, 200, fieldH);
        panel.add(chkCoManHinhPhu);

        JLabel lblLogoManHinhPhu = new JLabel("Logo màn hình phụ");
        lblLogoManHinhPhu.setFont(normalFont);
        lblLogoManHinhPhu.setBounds(120, 140, 130, fieldH);
        panel.add(lblLogoManHinhPhu);

        lblLogoManHinhPhuPreview = new JLabel("", SwingConstants.CENTER);
        lblLogoManHinhPhuPreview.setOpaque(true);
        lblLogoManHinhPhuPreview.setBackground(Color.WHITE);
        lblLogoManHinhPhuPreview.setBorder(BorderFactory.createLineBorder(new Color(120, 120, 120)));
        lblLogoManHinhPhuPreview.setBounds(185, 140, 80, 80);
        panel.add(lblLogoManHinhPhuPreview);

        JButton btnChonLogoManHinhPhu = new JButton("Chọn");
        btnChonLogoManHinhPhu.setFont(normalFont);
        btnChonLogoManHinhPhu.setBackground(Color.WHITE);
        btnChonLogoManHinhPhu.setBounds(186, 228, 78, 26);
        btnChonLogoManHinhPhu.addActionListener(e -> chooseLogoManHinhPhu());
        panel.add(btnChonLogoManHinhPhu);

        chkManHinhPhuSuDungGiaoDienThuong = new JCheckBox("Màn hình phụ sử dụng cả giao diện thường", true);
        chkManHinhPhuSuDungGiaoDienThuong.setFont(normalFont);
        chkManHinhPhuSuDungGiaoDienThuong.setOpaque(false);
        chkManHinhPhuSuDungGiaoDienThuong.setBounds(10, 272, 350, fieldH);
        panel.add(chkManHinhPhuSuDungGiaoDienThuong);

        return panel;
    }

    // ==================== TAB: CÂN ĐIỆN TỬ ====================
    private JPanel createTabCanDienTu() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 22;
        int rowGap = 28;
        int lblW = 120;
        int fieldX = 150;
        int fieldW = 380;
        int smallFieldW = 75;

        // Row 1: Kích hoạt sử dụng cân điện tử
        chkKichHoatSuDungCanDienTu = new JCheckBox("Kích hoạt sử dụng cân điện tử", false);
        chkKichHoatSuDungCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKichHoatSuDungCanDienTu.setOpaque(false);
        chkKichHoatSuDungCanDienTu.setBounds(20, startY, 300, fieldH);
        panel.add(chkKichHoatSuDungCanDienTu);

        // Row 2: Cổng com
        startY += rowGap;
        JLabel lblCongCom = new JLabel("Cổng com");
        lblCongCom.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCongCom.setBounds(25, startY, lblW, fieldH);
        panel.add(lblCongCom);

        txtCongComCanDienTu = new JTextField();
        txtCongComCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCongComCanDienTu.setBounds(fieldX, startY, fieldW, fieldH);
        panel.add(txtCongComCanDienTu);

        // Row 3: Baud rate (1200, 2400, 4800, 9600)
        startY += rowGap;
        JLabel lblBaudRate = new JLabel("Baud rate");
        lblBaudRate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBaudRate.setBounds(25, startY, lblW, fieldH);
        panel.add(lblBaudRate);

        cbBaudRateCanDienTu = new JComboBox<>(new String[]{"1200", "2400", "4800", "9600"});
        cbBaudRateCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbBaudRateCanDienTu.setBackground(Color.WHITE);
        cbBaudRateCanDienTu.setBounds(fieldX, startY, fieldW, fieldH);
        panel.add(cbBaudRateCanDienTu);

        // Row 4: Databit (8)
        startY += rowGap;
        JLabel lblDatabit = new JLabel("Databit (8)");
        lblDatabit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDatabit.setBounds(25, startY, lblW, fieldH);
        panel.add(lblDatabit);

        txtDatabitCanDienTu = new JTextField("0");
        txtDatabitCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDatabitCanDienTu.setBounds(fieldX, startY, smallFieldW, fieldH);
        panel.add(txtDatabitCanDienTu);

        // Row 5: Parity (None, Odd, Even, Mark, Space)
        startY += rowGap;
        JLabel lblParity = new JLabel("Parity");
        lblParity.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblParity.setBounds(25, startY, lblW, fieldH);
        panel.add(lblParity);

        cbParityCanDienTu = new JComboBox<>(new String[]{"None", "Odd", "Even", "Mark", "Space"});
        cbParityCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbParityCanDienTu.setBackground(Color.WHITE);
        cbParityCanDienTu.setBounds(fieldX, startY, fieldW, fieldH);
        panel.add(cbParityCanDienTu);

        // Row 6: Stop bits (để trống, 1, 2, 1.5)
        startY += rowGap;
        JLabel lblStopBits = new JLabel("Stop bits");
        lblStopBits.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStopBits.setBounds(25, startY, lblW, fieldH);
        panel.add(lblStopBits);

        cbStopBitsCanDienTu = new JComboBox<>(new String[]{"", "1", "2", "1.5"});
        cbStopBitsCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbStopBitsCanDienTu.setBackground(Color.WHITE);
        cbStopBitsCanDienTu.setBounds(fieldX, startY, fieldW, fieldH);
        panel.add(cbStopBitsCanDienTu);

        // Row 7: Timeout
        startY += rowGap;
        JLabel lblTimeout = new JLabel("Timeout");
        lblTimeout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTimeout.setBounds(25, startY, lblW, fieldH);
        panel.add(lblTimeout);

        txtTimeoutCanDienTu = new JTextField("0");
        txtTimeoutCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimeoutCanDienTu.setBounds(fieldX, startY, smallFieldW, fieldH);
        panel.add(txtTimeoutCanDienTu);

        // Row 8: Vị trí
        startY += rowGap;
        JLabel lblViTri = new JLabel("Vị trí");
        lblViTri.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblViTri.setBounds(25, startY, lblW, fieldH);
        panel.add(lblViTri);

        txtViTriCanDienTu = new JTextField("0");
        txtViTriCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtViTriCanDienTu.setBounds(fieldX, startY, smallFieldW, fieldH);
        panel.add(txtViTriCanDienTu);

        // Row 9: Độ dài
        startY += rowGap;
        JLabel lblDoDai = new JLabel("Độ dài");
        lblDoDai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDoDai.setBounds(25, startY, lblW, fieldH);
        panel.add(lblDoDai);

        txtDoDaiCanDienTu = new JTextField("0");
        txtDoDaiCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDoDaiCanDienTu.setBounds(fieldX, startY, smallFieldW, fieldH);
        panel.add(txtDoDaiCanDienTu);

        // Row 10: Tự động nhận dạng
        startY += rowGap;
        chkTuDongNhanDangCanDienTu = new JCheckBox("Tự động nhận dạng", false);
        chkTuDongNhanDangCanDienTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongNhanDangCanDienTu.setOpaque(false);
        chkTuDongNhanDangCanDienTu.setBounds(20, startY, 300, fieldH);
        panel.add(chkTuDongNhanDangCanDienTu);

        // Row 11: Đơn vị trên cân (Gr, Kg, Gram)
        startY += rowGap;
        JLabel lblDonVi = new JLabel("Đơn vị trên cân");
        lblDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDonVi.setBounds(25, startY, lblW, fieldH);
        panel.add(lblDonVi);

        cbDonViTrenCan = new JComboBox<>(new String[]{"Gram", "Kg", "Gr"});
        cbDonViTrenCan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDonViTrenCan.setBackground(Color.WHITE);
        cbDonViTrenCan.setBounds(fieldX, startY, fieldW, fieldH);
        panel.add(cbDonViTrenCan);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 9: CẢNH BÁO ====================
    private JPanel createTabCanhBao() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 22;
        int rowGap = 28;

        // 1. Hiển thị cảnh báo (Default: true)
        chkHienThiCanhBao = new JCheckBox("Hiển thị cảnh báo", true);
        chkHienThiCanhBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiCanhBao.setOpaque(false);
        chkHienThiCanhBao.setBounds(20, startY, 300, fieldH);
        panel.add(chkHienThiCanhBao);

        // 2. Thông báo hàng dưới mức an toàn (Default: false)
        startY += rowGap;
        chkThongBaoHangDuoiMucAnToan = new JCheckBox("Thông báo hàng dưới mức an toàn", false);
        chkThongBaoHangDuoiMucAnToan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkThongBaoHangDuoiMucAnToan.setOpaque(false);
        chkThongBaoHangDuoiMucAnToan.setBounds(20, startY, 350, fieldH);
        panel.add(chkThongBaoHangDuoiMucAnToan);

        // 3. Thông báo khách đến ngày sinh nhật (Default: false)
        startY += rowGap;
        chkThongBaoKhachDenNgaySinhNhat = new JCheckBox("Thông báo khách đến ngày sinh nhật", false);
        chkThongBaoKhachDenNgaySinhNhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkThongBaoKhachDenNgaySinhNhat.setOpaque(false);
        chkThongBaoKhachDenNgaySinhNhat.setBounds(20, startY, 350, fieldH);
        panel.add(chkThongBaoKhachDenNgaySinhNhat);

        // 4. Thông báo hàng hết hạn dùng (Default: false)
        startY += rowGap;
        chkThongBaoHangHetHanDung = new JCheckBox("Thông báo hàng hết hạn dùng", false);
        chkThongBaoHangHetHanDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkThongBaoHangHetHanDung.setOpaque(false);
        chkThongBaoHangHetHanDung.setBounds(20, startY, 350, fieldH);
        panel.add(chkThongBaoHangHetHanDung);

        // 5. Thông báo hàng sắp hết hạn dùng (Default: false)
        startY += rowGap;
        chkThongBaoHangSapHetHanDung = new JCheckBox("Thông báo hàng sắp hết hạn dùng", false);
        chkThongBaoHangSapHetHanDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkThongBaoHangSapHetHanDung.setOpaque(false);
        chkThongBaoHangSapHetHanDung.setBounds(20, startY, 350, fieldH);
        panel.add(chkThongBaoHangSapHetHanDung);

        // 6. Thông báo trước (ngày) (Default: 3)
        startY += rowGap;
        JLabel lblThongBaoTruoc = new JLabel("Thông báo trước (ngày)");
        lblThongBaoTruoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThongBaoTruoc.setBounds(25, startY, 150, fieldH);
        panel.add(lblThongBaoTruoc);

        txtThongBaoTruocNgay = new JTextField("3");
        txtThongBaoTruocNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtThongBaoTruocNgay.setBounds(180, startY, 75, fieldH);
        panel.add(txtThongBaoTruocNgay);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 10: CẢM ỨNG ====================
    private JPanel createTabCamUng() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 15;
        int fieldH = 24;
        int rowGap = 32;

        String[] detectedPrinters = getAvailablePrinters();

        // Row 1: In pha chế khi in hóa đơn (Default: false)
        chkInPhaCheKhiInHoaDon = new JCheckBox("In pha chế khi in hóa đơn", false);
        chkInPhaCheKhiInHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkInPhaCheKhiInHoaDon.setOpaque(false);
        chkInPhaCheKhiInHoaDon.setBounds(20, startY, 300, 22);
        panel.add(chkInPhaCheKhiInHoaDon);

        // Row 2: Mẫu in pha chế (Default: "Mẫu in chế biến")
        startY += 28;
        JLabel lblMauIn = new JLabel("Mẫu in pha chế");
        lblMauIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMauIn.setBounds(25, startY, 150, fieldH);
        panel.add(lblMauIn);

        cbMauInPhaChe = new JComboBox<>();
        cbMauInPhaChe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMauInPhaChe.setBackground(Color.WHITE);
        cbMauInPhaChe.setBounds(180, startY, 380, fieldH);
        panel.add(cbMauInPhaChe);

        // Row 3: Máy in pha chế (Default: "Microsoft XPS Document Writer")
        startY += rowGap;
        JLabel lblMayIn = new JLabel("Máy in pha chế");
        lblMayIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMayIn.setBounds(25, startY, 150, fieldH);
        panel.add(lblMayIn);

        cbMayInPhaChe = new JComboBox<>(detectedPrinters);
        cbMayInPhaChe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMayInPhaChe.setBackground(Color.WHITE);
        cbMayInPhaChe.setSelectedItem("Microsoft XPS Document Writer");
        cbMayInPhaChe.setBounds(180, startY, 380, fieldH);
        panel.add(cbMayInPhaChe);

        // Row 4: In pha chế khi lưu tạm (Default: false)
        startY += rowGap;
        chkInPhaCheKhiLuuTam = new JCheckBox("In pha chế khi lưu tạm", false);
        chkInPhaCheKhiLuuTam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkInPhaCheKhiLuuTam.setOpaque(false);
        chkInPhaCheKhiLuuTam.setBounds(20, startY, 300, 22);
        panel.add(chkInPhaCheKhiLuuTam);

        // Row 5: Mẫu in pha chế lưu tạm (Default: "")
        startY += 28;
        JLabel lblMauInTam = new JLabel("Mẫu in pha chế lưu tạm");
        lblMauInTam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMauInTam.setBounds(25, startY, 150, fieldH);
        panel.add(lblMauInTam);

        cbMauInPhaCheLuuTam = new JComboBox<>();
        cbMauInPhaCheLuuTam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbMauInPhaCheLuuTam.setBackground(Color.WHITE);
        cbMauInPhaCheLuuTam.setBounds(180, startY, 380, fieldH);
        panel.add(cbMauInPhaCheLuuTam);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    // ==================== TAB 11: QUẢN TRỊ (Ảnh 1 & 2) ====================
    private JPanel createTabQuanTri() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int startY = 12;
        int fieldH = 22;
        int rowGap = 26;

        // Row 1: Phiếu gần nhất nằm phía dưới (Default: false)
        chkPhieuGanNhatNamPhiaDuoi = new JCheckBox("Phiếu gần nhất nằm phía dưới", false);
        chkPhieuGanNhatNamPhiaDuoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkPhieuGanNhatNamPhiaDuoi.setOpaque(false);
        chkPhieuGanNhatNamPhiaDuoi.setBounds(20, startY, 300, fieldH);
        panel.add(chkPhieuGanNhatNamPhiaDuoi);

        // Row 2: Phím nhập liệu trên lưới
        startY += rowGap;
        JLabel lblPhimNhap = new JLabel("Phím nhập liệu trên lưới");
        lblPhimNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhimNhap.setBounds(25, startY, 150, fieldH);
        panel.add(lblPhimNhap);

        cbPhimNhapLieuTrenLuoi = new JComboBox<>(new String[]{
                "Tab để sang ngang, Enter để xuống dòng",
                "Enter để sang ngang, Tab để xuống dòng",
                "Enter và Tab đều sang ngang"
        });
        cbPhimNhapLieuTrenLuoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbPhimNhapLieuTrenLuoi.setBackground(Color.WHITE);
        cbPhimNhapLieuTrenLuoi.setBounds(180, startY, 380, fieldH);
        panel.add(cbPhimNhapLieuTrenLuoi);

        // Row 3: Dòng mô tả Thiết lập tùy chọn tự động sao lưu dữ liệu...
        startY += rowGap + 4;
        JLabel lblNoteBackup = new JLabel("Thiết lập tùy chọn tự động sao lưu dữ liệu để đảm bảo an toàn trong tình huống xấu nhất");
        lblNoteBackup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNoteBackup.setBounds(25, startY, 530, fieldH);
        panel.add(lblNoteBackup);

        // Row 4: Tự động sao lưu dữ liệu (Default: true)
        startY += rowGap;
        chkTuDongSaoLuuDuLieu = new JCheckBox("Tự động sao lưu dữ liệu", true);
        chkTuDongSaoLuuDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongSaoLuuDuLieu.setOpaque(false);
        chkTuDongSaoLuuDuLieu.setBounds(20, startY, 300, fieldH);
        panel.add(chkTuDongSaoLuuDuLieu);

        // Row 5: Đường dẫn sao lưu & Số ngày (Default: "" / "1")
        startY += rowGap;
        JLabel lblDuongDan = new JLabel("Đường dẫn sao lưu:");
        lblDuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDuongDan.setBounds(45, startY, 125, fieldH);
        panel.add(lblDuongDan);

        txtDuongDanSaoLuu = new JTextField();
        txtDuongDanSaoLuu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDuongDanSaoLuu.setBounds(180, startY, 140, fieldH);
        panel.add(txtDuongDanSaoLuu);

        JLabel lblSoNgay = new JLabel("Số ngày:");
        lblSoNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoNgay.setBounds(330, startY, 60, fieldH);
        panel.add(lblSoNgay);

        txtSoNgaySaoLuu = new JTextField("1");
        txtSoNgaySaoLuu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoNgaySaoLuu.setBounds(395, startY, 55, fieldH);
        panel.add(txtSoNgaySaoLuu);

        // Row 6: Chức năng mặc định
        startY += rowGap;
        JLabel lblChucNang = new JLabel("Chức năng mặc định");
        lblChucNang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblChucNang.setBounds(25, startY, 150, fieldH);
        panel.add(lblChucNang);

        cbChucNangMacDinh = new JComboBox<>(new String[]{
                "Hóa đơn bán hàng",
                "Báo giá",
                "Đơn hàng",
                "Phiếu nhập kho",
                "Phiếu xuất kho",
                "Danh mục mặt hàng",
                "Khách hàng"
        });
        cbChucNangMacDinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbChucNangMacDinh.setBackground(Color.WHITE);
        cbChucNangMacDinh.setBounds(180, startY, 380, fieldH);
        panel.add(cbChucNangMacDinh);

        // Row 7: Lọc dữ liệu bỏ khoảng trống (Default: false)
        startY += rowGap;
        chkLocDuLieuBoKhoangTrong = new JCheckBox("Lọc dữ liệu bỏ khoảng trống", false);
        chkLocDuLieuBoKhoangTrong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkLocDuLieuBoKhoangTrong.setOpaque(false);
        chkLocDuLieuBoKhoangTrong.setBounds(20, startY, 300, fieldH);
        panel.add(chkLocDuLieuBoKhoangTrong);

        startY += 20;
        JLabel lblNoteLoc = new JLabel("(Ví dụ bạn tìm \"cafe da\" sẽ ra cả \"cafe đá\" và \"cafeda\")");
        lblNoteLoc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNoteLoc.setForeground(new Color(100, 100, 100));
        lblNoteLoc.setBounds(25, startY, 450, 18);
        panel.add(lblNoteLoc);

        // Row 8: Hiển thị xuống dòng nếu độ rộng cột nhỏ (Default: false)
        startY += 24;
        chkHienThiXuongDongNeuDoRongCotNho = new JCheckBox("Hiển thị xuống dòng nếu độ rộng cột nhỏ", false);
        chkHienThiXuongDongNeuDoRongCotNho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiXuongDongNeuDoRongCotNho.setOpaque(false);
        chkHienThiXuongDongNeuDoRongCotNho.setBounds(20, startY, 350, fieldH);
        panel.add(chkHienThiXuongDongNeuDoRongCotNho);

        startY += 20;
        JLabel lblNoteXuongDong = new JLabel("(Tùy chọn này sẽ làm chương trình chạy chậm hơn một chút)");
        lblNoteXuongDong.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNoteXuongDong.setForeground(new Color(100, 100, 100));
        lblNoteXuongDong.setBounds(25, startY, 450, 18);
        panel.add(lblNoteXuongDong);

        // Row 9: Độ to chữ hiển thị
        startY += 24;
        JLabel lblDoToChu = new JLabel("Độ to chữ hiển thị");
        lblDoToChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDoToChu.setBounds(25, startY, 150, fieldH);
        panel.add(lblDoToChu);

        cbDoToChuHienThi = new JComboBox<>(new String[]{
                "Nhỏ (110%)",
                "Mặc định (100%)",
                "Vừa (120%)",
                "Lớn (130%)"
        });
        cbDoToChuHienThi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDoToChuHienThi.setBackground(Color.WHITE);
        cbDoToChuHienThi.setBounds(180, startY, 380, fieldH);
        panel.add(cbDoToChuHienThi);

        startY += 20;
        JLabel lblNoteFont = new JLabel("(Bạn cần thoát chương trình ra vào lại để áp dụng)");
        lblNoteFont.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblNoteFont.setForeground(new Color(100, 100, 100));
        lblNoteFont.setBounds(180, startY, 380, 18);
        panel.add(lblNoteFont);

        // Row 10: Giao diện chương trình
        startY += 24;
        JLabel lblGiaoDien = new JLabel("Giao diện chương trình");
        lblGiaoDien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaoDien.setBounds(25, startY, 150, fieldH);
        panel.add(lblGiaoDien);

        cbGiaoDienChuongTrinh = new JComboBox<>(new String[]{
                "Office 2010 - Blue",
                "Office 2010 - Black",
                "Office 2010 - Silver",
                "Windows Standard",
                "FlatLaf Light"
        });
        cbGiaoDienChuongTrinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbGiaoDienChuongTrinh.setBackground(Color.WHITE);
        cbGiaoDienChuongTrinh.setBounds(180, startY, 380, fieldH);
        panel.add(cbGiaoDienChuongTrinh);

        // Row 11: Tự động tải lại dữ liệu khi chuyển tab ở giao diện chính (Default: true)
        startY += rowGap;
        chkTuDongTaiLaiDuLieuKhiChuyenTab = new JCheckBox("Tự động tải lại dữ liệu khi chuyển tab ở giao diện chính", true);
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setOpaque(false);
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setBounds(20, startY, 450, fieldH);
        panel.add(chkTuDongTaiLaiDuLieuKhiChuyenTab);

        // Row 12: Kích hoạt lưu vết hoạt động (Default: true)
        startY += rowGap;
        chkKichHoatLuuVetHoatDong = new JCheckBox("Kích hoạt lưu vết hoạt động", true);
        chkKichHoatLuuVetHoatDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKichHoatLuuVetHoatDong.setOpaque(false);
        chkKichHoatLuuVetHoatDong.setBounds(20, startY, 350, fieldH);
        panel.add(chkKichHoatLuuVetHoatDong);

        // Row 13: Hệ thống chạy nhiều máy trạm (Default: true)
        startY += rowGap;
        chkHeThongChayNhieuMayTram = new JCheckBox("Hệ thống chạy nhiều máy trạm", true);
        chkHeThongChayNhieuMayTram.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHeThongChayNhieuMayTram.setOpaque(false);
        chkHeThongChayNhieuMayTram.setBounds(20, startY, 350, fieldH);
        panel.add(chkHeThongChayNhieuMayTram);

        panel.setPreferredSize(new Dimension(580, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    private JPanel createPlaceholderCard(String title) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("Cấu hình " + title + " đang được phát triển.");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
    }

    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn hình ảnh logo công ty");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
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
        if (path == null || path.trim().isEmpty()) {
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
            return;
        }
        try {
            String resolved = SystemConfigDAO.resolveLogoPath(path);
            File f = (resolved != null) ? new File(resolved) : new File(path);
            if (f.exists() && f.isFile()) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(90, 65, Image.SCALE_SMOOTH);
                    lblLogoPreview.setIcon(new ImageIcon(scaled));
                    lblLogoPreview.setText("");
                    return;
                }
            }
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
        } catch (Exception ignored) {
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
        }
    }

    private void chooseLogoManHinhPhu() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn logo màn hình phụ");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
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
        lblLogoManHinhPhuPreview.setIcon(null);
        lblLogoManHinhPhuPreview.setText("");
        if (path == null || path.trim().isEmpty()) {
            return;
        }
        try {
            String resolved = SystemConfigDAO.resolveLogoPath(path);
            File f = resolved != null ? new File(resolved) : new File(path);
            if (f.exists() && f.isFile()) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    Image scaled = img.getScaledInstance(76, 76, Image.SCALE_SMOOTH);
                    lblLogoManHinhPhuPreview.setIcon(new ImageIcon(scaled));
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadTemplatesToComboBoxes() {
        List<String> templateNames = templateDAO.getInvoiceTemplateNames();
        if (templateNames == null || templateNames.isEmpty()) {
            templateNames = Arrays.asList(
                    "BIÊN BẢN GIAO NHẬN HÀNG HÓA",
                    "Mẫu 54 mm x 2 dòng",
                    "Mẫu in A5 cho máy in kim",
                    "Mẫu in bill 54mm",
                    "Mẫu in bill 80 chiết khấu mặt hàng",
                    "Mẫu in bill 80mm",
                    "Mẫu in bill A4",
                    "Mẫu in bill A5",
                    "Mẫu in chế biến",
                    "Phiếu giao hàng"
            );
        }

        ListCellRenderer<Object> templateRenderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null && !value.toString().trim().isEmpty()) {
                    String valStr = value.toString();
                    label.setText(valStr);
                    label.setIcon(getTemplateIcon(valStr));
                }
                return label;
            }
        };

        if (cbMauHoaDon != null) {
            cbMauHoaDon.setRenderer(templateRenderer);
            cbMauHoaDon.removeAllItems();
            for (String t : templateNames) {
                cbMauHoaDon.addItem(t);
            }
        }

        if (cbMauInPhaChe != null) {
            cbMauInPhaChe.setRenderer(templateRenderer);
            cbMauInPhaChe.removeAllItems();
            for (String t : templateNames) {
                cbMauInPhaChe.addItem(t);
            }
        }

        if (cbMauInPhaCheLuuTam != null) {
            cbMauInPhaCheLuuTam.setRenderer(templateRenderer);
            cbMauInPhaCheLuuTam.removeAllItems();
            cbMauInPhaCheLuuTam.addItem("");
            for (String t : templateNames) {
                cbMauInPhaCheLuuTam.addItem(t);
            }
        }
    }

    private Icon getTemplateIcon(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        if (lower.contains("biên bản") || lower.contains("giao nhận") || lower.contains("phiếu giao")) {
            return IconHelper.getIcon("document_valid.png", 16, 16);
        } else if (lower.contains("2 dòng") || lower.contains("54 mm")) {
            return IconHelper.getIcon("Statistic16.png", 16, 16);
        } else if (lower.contains("chế biến") || lower.contains("pha chế")) {
            return IconHelper.getIcon("settings.png", 16, 16);
        } else if (lower.contains("bill") || lower.contains("80") || lower.contains("54")) {
            return IconHelper.getIcon("receipt--pencil.png", 16, 16);
        } else {
            return IconHelper.getIcon("Report.png", 16, 16);
        }
    }

    private void selectOrAddItem(JComboBox<String> cbo, String value) {
        if (cbo == null) return;
        if (value == null || value.trim().isEmpty()) {
            if (cbo.getItemCount() > 0) cbo.setSelectedIndex(0);
            return;
        }
        String target = value.trim();
        for (int i = 0; i < cbo.getItemCount(); i++) {
            String item = cbo.getItemAt(i);
            if (item != null && (item.equalsIgnoreCase(target) || item.contains(target))) {
                cbo.setSelectedIndex(i);
                return;
            }
        }
        cbo.addItem(target);
        cbo.setSelectedItem(target);
    }

    private void loadConfigToUI() {
        if (currentConfig == null) return;

        // Load dynamic templates from database
        loadTemplatesToComboBoxes();

        // Tab 1
        txtTenCongTy.setText(currentConfig.getTenCongTy());
        txtDiaChi.setText(currentConfig.getDiaChi());
        selectedLogoPath = currentConfig.getLogoPath();
        if (selectedLogoPath != null && !selectedLogoPath.trim().isEmpty()) {
            displayLogo(selectedLogoPath);
        } else {
            lblLogoPreview.setIcon(null);
            lblLogoPreview.setText("Chưa có");
        }
        txtSoDienThoai.setText(currentConfig.getSoDienThoai());
        txtEmail.setText(currentConfig.getEmail());
        txtLoiCamOn.setText(currentConfig.getLoiCamOn());
        txtFax.setText(currentConfig.getFax());
        selectOrAddItem(cbMauHoaDon, currentConfig.getMauHoaDon());
        chkLuaChonMauKhiIn.setSelected(currentConfig.isLuaChonMauKhiIn());
        chkHienThiTruocKhiIn.setSelected(currentConfig.isHienThiTruocKhiIn());

        // Tab 2: Máy in (ComboBoxes)
        for (var entry : printerFields.entrySet()) {
            String val = currentConfig.getPrinterSettings().get(entry.getKey());
            if (val != null && !val.trim().isEmpty()) {
                entry.getValue().setSelectedItem(val.trim());
            } else {
                entry.getValue().setSelectedIndex(0);
            }
        }

        // Tab 3: Số phiếu (Readonly fields)
        for (var entry : voucherFields.entrySet()) {
            String val = currentConfig.getVoucherRules().get(entry.getKey());
            entry.getValue().setText(val != null ? val : "");
        }

        // Tab 4: Bán hàng
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
        txtLamTronTien.setText(currentConfig.getLamTronTien());

        // Tab 5: Thanh toán
        chkCoThanhToanVoucher.setSelected(currentConfig.isCoThanhToanVoucher());
        chkChoPhepKhachNo.setSelected(currentConfig.isChoPhepKhachNo());
        chkLuaChonVoucherTuDanhSach.setSelected(currentConfig.isLuaChonVoucherTuDanhSach());
        chkSuDungTheTraTruoc.setSelected(currentConfig.isSuDungTheTraTruoc());
        chkSuDungDiemTichLuyDeThanhToan.setSelected(currentConfig.isSuDungDiemTichLuyDeThanhToan());
        txtQuyDoi1DiemSangTien.setText(currentConfig.getQuyDoi1DiemSangTien());
        chkCoThanhToanThe.setSelected(currentConfig.isCoThanhToanThe());
        chkCoThanhToanChuyenKhoan.setSelected(currentConfig.isCoThanhToanChuyenKhoan());

        // Tab 6: Tích điểm
        txtDoanhSoTuongUng1Diem.setText(currentConfig.getDoanhSoTuongUng1Diem());
        cbCachTinhDiem.setSelectedItem(currentConfig.getCachTinhDiem());
        chkTuDongNangCapThanhVien.setSelected(currentConfig.isTuDongNangCapThanhVien());
        chkHienThiDiemKhachHang.setSelected(currentConfig.isHienThiDiemKhachHangTrenHoaDon());
        chkKhongTichDiemNeuCoKhuyenMai.setSelected(currentConfig.isKhongTichDiemNeuCoKhuyenMai());

        // Tab 7: Kho hàng
        chkChoPhepNhapMotMatHangNhieuLan.setSelected(currentConfig.isChoPhepNhapMotMatHangNhieuLan());
        chkBatBuocChonNhanVienNhapKho.setSelected(currentConfig.isBatBuocChonNhanVienNhapKho());
        chkBatBuocChonNhaCungCapNhapKho.setSelected(currentConfig.isBatBuocChonNhaCungCapNhapKho());
        chkSuDung2DonViTinh.setSelected(currentConfig.isSuDung2DonViTinh());
        chkTuDongTinhGiaVon.setSelected(currentConfig.isTuDongTinhGiaVon());
        chkNhapKhoBangDauDocMaVach.setSelected(currentConfig.isNhapKhoBangDauDocMaVach());
        cbGiaSuDungKhiChuyenXuatKho.setSelectedItem(currentConfig.getGiaSuDungKhiChuyenXuatKho());
        chkSuDungChucNangSanXuat.setSelected(currentConfig.isSuDungChucNangSanXuat());

        // Tab 8: Mặt hàng
        chkSuDungMaSanCo.setSelected(currentConfig.isSuDungMaSanCo());
        chkSuDungMaHang.setSelected(currentConfig.isSuDungMaHang());
        chkTuDongSinhMaVach.setSelected(currentConfig.isTuDongSinhMaVach());
        txtChieuDaiMaVach.setText(String.valueOf(currentConfig.getChieuDaiMaVach()));
        chkCoKyTuKiemTra.setSelected(currentConfig.isCoKyTuKiemTra());
        chkSuDungTemInBangCanDienTu.setSelected(currentConfig.isSuDungTemInBangCanDienTu());
        txtChuoiBatDauCanDienTu.setText(currentConfig.getChuoiBatDauCanDienTu());
        cbTimTheo.setSelectedItem(currentConfig.getTimTheo());
        cbCachTim.setSelectedItem(currentConfig.getCachTim());
        chkChoPhepTrungTenMatHang.setSelected(currentConfig.isChoPhepTrungTenMatHang());
        chkHangCoBaoHanh.setSelected(currentConfig.isHangCoBaoHanh());
        chkSuDungHangSanXuat.setSelected(currentConfig.isSuDungHangSanXuat());
        chkSuDungGia2.setSelected(currentConfig.isSuDungGia2());
        txtDienGiaiGia2.setText(currentConfig.getDienGiaiGia2());
        chkSuDungGia3.setSelected(currentConfig.isSuDungGia3());
        txtDienGiaiGia3.setText(currentConfig.getDienGiaiGia3());
        chkSuDungGia4.setSelected(currentConfig.isSuDungGia4());
        txtDienGiaiGia4.setText(currentConfig.getDienGiaiGia4());
        chkHienThiDeLuaChonNeuCoNhieuGia.setSelected(currentConfig.isHienThiDeLuaChonNeuCoNhieuGia());
        cbSapXepThuTuTheo.setSelectedItem(currentConfig.getSapXepThuTuTheo());
        chkMatHangCoHanSuDung.setSelected(currentConfig.isMatHangCoHanSuDung());
        chkMatHangCoKichThuoc.setSelected(currentConfig.isMatHangCoKichThuoc());
        chkMatHangCoVatTuDinhLuong.setSelected(currentConfig.isMatHangCoVatTuDinhLuong());

        // Tab Cây hiển thị giá
        chkCoCayHienThiGia.setSelected(currentConfig.isCoCayHienThiGia());
        chkCayHienThiGiaMotDong.setSelected(currentConfig.isCayHienThiGiaMotDong());
        txtCongCayHienThiGia.setText(currentConfig.getCongCayHienThiGia() != null ? currentConfig.getCongCayHienThiGia() : "");
        chkCoManHinhPhu.setSelected(currentConfig.isCoManHinhPhu());
        selectedLogoManHinhPhuPath = currentConfig.getLogoManHinhPhuPath();
        displayLogoManHinhPhu(selectedLogoManHinhPhuPath);
        chkManHinhPhuSuDungGiaoDienThuong.setSelected(currentConfig.isManHinhPhuSuDungGiaoDienThuong());

        // Tab Cân điện tử
        chkKichHoatSuDungCanDienTu.setSelected(currentConfig.isKichHoatSuDungCanDienTu());
        txtCongComCanDienTu.setText(currentConfig.getCongComCanDienTu() != null ? currentConfig.getCongComCanDienTu() : "");
        selectOrAddItem(cbBaudRateCanDienTu, currentConfig.getBaudRateCanDienTu());
        txtDatabitCanDienTu.setText(currentConfig.getDatabitCanDienTu() != null ? currentConfig.getDatabitCanDienTu() : "0");
        selectOrAddItem(cbParityCanDienTu, currentConfig.getParityCanDienTu());
        selectOrAddItem(cbStopBitsCanDienTu, currentConfig.getStopBitsCanDienTu());
        txtTimeoutCanDienTu.setText(String.valueOf(currentConfig.getTimeoutCanDienTu()));
        txtViTriCanDienTu.setText(String.valueOf(currentConfig.getViTriCanDienTu()));
        txtDoDaiCanDienTu.setText(String.valueOf(currentConfig.getDoDaiCanDienTu()));
        chkTuDongNhanDangCanDienTu.setSelected(currentConfig.isTuDongNhanDangCanDienTu());
        selectOrAddItem(cbDonViTrenCan, currentConfig.getDonViTrenCan());

        // Tab 9: Cảnh báo
        chkHienThiCanhBao.setSelected(currentConfig.isHienThiCanhBao());
        chkThongBaoHangDuoiMucAnToan.setSelected(currentConfig.isThongBaoHangDuoiMucAnToan());
        chkThongBaoKhachDenNgaySinhNhat.setSelected(currentConfig.isThongBaoKhachDenNgaySinhNhat());
        chkThongBaoHangHetHanDung.setSelected(currentConfig.isThongBaoHangHetHanDung());
        chkThongBaoHangSapHetHanDung.setSelected(currentConfig.isThongBaoHangSapHetHanDung());
        txtThongBaoTruocNgay.setText(String.valueOf(currentConfig.getThongBaoTruocNgay()));

        // Tab 10: Cảm ứng
        chkInPhaCheKhiInHoaDon.setSelected(currentConfig.isInPhaCheKhiInHoaDon());
        selectOrAddItem(cbMauInPhaChe, currentConfig.getMauInPhaChe());
        if (currentConfig.getMayInPhaChe() != null && !currentConfig.getMayInPhaChe().isEmpty()) {
            cbMayInPhaChe.setSelectedItem(currentConfig.getMayInPhaChe());
        }
        chkInPhaCheKhiLuuTam.setSelected(currentConfig.isInPhaCheKhiLuuTam());
        selectOrAddItem(cbMauInPhaCheLuuTam, currentConfig.getMauInPhaCheLuuTam());

        // Tab 11: Quản trị
        chkPhieuGanNhatNamPhiaDuoi.setSelected(currentConfig.isPhieuGanNhatNamPhiaDuoi());
        cbPhimNhapLieuTrenLuoi.setSelectedItem(currentConfig.getPhimNhapLieuTrenLuoi());
        chkTuDongSaoLuuDuLieu.setSelected(currentConfig.isTuDongSaoLuuDuLieu());
        txtDuongDanSaoLuu.setText(currentConfig.getDuongDanSaoLuu());
        txtSoNgaySaoLuu.setText(String.valueOf(currentConfig.getSoNgaySaoLuu()));
        cbChucNangMacDinh.setSelectedItem(currentConfig.getChucNangMacDinh());
        chkLocDuLieuBoKhoangTrong.setSelected(currentConfig.isLocDuLieuBoKhoangTrong());
        chkHienThiXuongDongNeuDoRongCotNho.setSelected(currentConfig.isHienThiXuongDongNeuDoRongCotNho());
        cbDoToChuHienThi.setSelectedItem(currentConfig.getDoToChuHienThi());
        cbGiaoDienChuongTrinh.setSelectedItem(currentConfig.getGiaoDienChuongTrinh());
        chkTuDongTaiLaiDuLieuKhiChuyenTab.setSelected(currentConfig.isTuDongTaiLaiDuLieuKhiChuyenTab());
        chkKichHoatLuuVetHoatDong.setSelected(currentConfig.isKichHoatLuuVetHoatDong());
        chkHeThongChayNhieuMayTram.setSelected(currentConfig.isHeThongChayNhieuMayTram());
    }

    private void onSave() {
        // Tab 1
        currentConfig.setTenCongTy(txtTenCongTy.getText().trim());
        currentConfig.setDiaChi(txtDiaChi.getText().trim());
        currentConfig.setLogoPath(selectedLogoPath != null && !selectedLogoPath.trim().isEmpty() ? selectedLogoPath.trim() : null);
        currentConfig.setSoDienThoai(txtSoDienThoai.getText().trim());
        currentConfig.setEmail(txtEmail.getText().trim());
        currentConfig.setLoiCamOn(txtLoiCamOn.getText().trim());
        currentConfig.setFax(txtFax.getText().trim());
        currentConfig.setMauHoaDon(cbMauHoaDon.getSelectedItem() != null ? cbMauHoaDon.getSelectedItem().toString() : "");
        currentConfig.setLuaChonMauKhiIn(chkLuaChonMauKhiIn.isSelected());
        currentConfig.setHienThiTruocKhiIn(chkHienThiTruocKhiIn.isSelected());

        // Tab 2: Máy in (ComboBox selected values)
        for (var entry : printerFields.entrySet()) {
            Object sel = entry.getValue().getSelectedItem();
            currentConfig.getPrinterSettings().put(entry.getKey(), sel != null ? sel.toString().trim() : "");
        }

        // Tab 3: Số phiếu
        for (var entry : voucherFields.entrySet()) {
            currentConfig.getVoucherRules().put(entry.getKey(), entry.getValue().getText().trim());
        }

        // Tab 4: Bán hàng
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

        // Tab 5: Thanh toán
        currentConfig.setCoThanhToanVoucher(chkCoThanhToanVoucher.isSelected());
        currentConfig.setChoPhepKhachNo(chkChoPhepKhachNo.isSelected());
        currentConfig.setLuaChonVoucherTuDanhSach(chkLuaChonVoucherTuDanhSach.isSelected());
        currentConfig.setSuDungTheTraTruoc(chkSuDungTheTraTruoc.isSelected());
        currentConfig.setSuDungDiemTichLuyDeThanhToan(chkSuDungDiemTichLuyDeThanhToan.isSelected());
        currentConfig.setQuyDoi1DiemSangTien(txtQuyDoi1DiemSangTien.getText().trim());
        currentConfig.setCoThanhToanThe(chkCoThanhToanThe.isSelected());
        currentConfig.setCoThanhToanChuyenKhoan(chkCoThanhToanChuyenKhoan.isSelected());

        // Tab 6: Tích điểm
        currentConfig.setDoanhSoTuongUng1Diem(txtDoanhSoTuongUng1Diem.getText().trim());
        currentConfig.setCachTinhDiem(cbCachTinhDiem.getSelectedItem() != null ? cbCachTinhDiem.getSelectedItem().toString() : "Điểm được tính trên từng hóa đơn");
        currentConfig.setTuDongNangCapThanhVien(chkTuDongNangCapThanhVien.isSelected());
        currentConfig.setHienThiDiemKhachHangTrenHoaDon(chkHienThiDiemKhachHang.isSelected());
        currentConfig.setKhongTichDiemNeuCoKhuyenMai(chkKhongTichDiemNeuCoKhuyenMai.isSelected());

        // Tab 7: Kho hàng
        currentConfig.setChoPhepNhapMotMatHangNhieuLan(chkChoPhepNhapMotMatHangNhieuLan.isSelected());
        currentConfig.setBatBuocChonNhanVienNhapKho(chkBatBuocChonNhanVienNhapKho.isSelected());
        currentConfig.setBatBuocChonNhaCungCapNhapKho(chkBatBuocChonNhaCungCapNhapKho.isSelected());
        currentConfig.setSuDung2DonViTinh(chkSuDung2DonViTinh.isSelected());
        currentConfig.setTuDongTinhGiaVon(chkTuDongTinhGiaVon.isSelected());
        currentConfig.setNhapKhoBangDauDocMaVach(chkNhapKhoBangDauDocMaVach.isSelected());
        currentConfig.setGiaSuDungKhiChuyenXuatKho(cbGiaSuDungKhiChuyenXuatKho.getSelectedItem() != null ? cbGiaSuDungKhiChuyenXuatKho.getSelectedItem().toString() : "Giá vốn");
        currentConfig.setSuDungChucNangSanXuat(chkSuDungChucNangSanXuat.isSelected());

        // Tab 8: Mặt hàng
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

        // Tab Cây hiển thị giá
        currentConfig.setCoCayHienThiGia(chkCoCayHienThiGia.isSelected());
        currentConfig.setCayHienThiGiaMotDong(chkCayHienThiGiaMotDong.isSelected());
        currentConfig.setCongCayHienThiGia(txtCongCayHienThiGia.getText().trim());
        currentConfig.setCoManHinhPhu(chkCoManHinhPhu.isSelected());
        currentConfig.setLogoManHinhPhuPath(selectedLogoManHinhPhuPath != null && !selectedLogoManHinhPhuPath.trim().isEmpty()
                ? selectedLogoManHinhPhuPath.trim() : null);
        currentConfig.setManHinhPhuSuDungGiaoDienThuong(chkManHinhPhuSuDungGiaoDienThuong.isSelected());

        // Tab Cân điện tử
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

        // Tab 9: Cảnh báo
        currentConfig.setHienThiCanhBao(chkHienThiCanhBao.isSelected());
        currentConfig.setThongBaoHangDuoiMucAnToan(chkThongBaoHangDuoiMucAnToan.isSelected());
        currentConfig.setThongBaoKhachDenNgaySinhNhat(chkThongBaoKhachDenNgaySinhNhat.isSelected());
        currentConfig.setThongBaoHangHetHanDung(chkThongBaoHangHetHanDung.isSelected());
        currentConfig.setThongBaoHangSapHetHanDung(chkThongBaoHangSapHetHanDung.isSelected());
        try { currentConfig.setThongBaoTruocNgay(Integer.parseInt(txtThongBaoTruocNgay.getText().trim())); } catch (Exception ignored) {}

        // Tab 10: Cảm ứng
        currentConfig.setInPhaCheKhiInHoaDon(chkInPhaCheKhiInHoaDon.isSelected());
        currentConfig.setMauInPhaChe(cbMauInPhaChe.getSelectedItem() != null ? cbMauInPhaChe.getSelectedItem().toString() : "Mẫu in chế biến");
        currentConfig.setMayInPhaChe(cbMayInPhaChe.getSelectedItem() != null ? cbMayInPhaChe.getSelectedItem().toString() : "Microsoft XPS Document Writer");
        currentConfig.setInPhaCheKhiLuuTam(chkInPhaCheKhiLuuTam.isSelected());
        currentConfig.setMauInPhaCheLuuTam(cbMauInPhaCheLuuTam.getSelectedItem() != null ? cbMauInPhaCheLuuTam.getSelectedItem().toString() : "");

        // Tab 11: Quản trị
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

        boolean ok = configDAO.saveConfig(currentConfig);
        if (ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu thông tin cấu hình toàn bộ hệ thống thành công!\n"
                            + "Vui lòng khởi động lại ứng dụng để áp dụng giao diện và cỡ chữ mới.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu cấu hình hệ thống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class MenuItem {
        String name;
        String iconName;

        public MenuItem(String name, String iconName) {
            this.name = name;
            this.iconName = iconName;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
