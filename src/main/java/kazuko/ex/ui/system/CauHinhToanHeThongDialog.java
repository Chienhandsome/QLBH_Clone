package kazuko.ex.ui.system;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
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
    private SystemConfig currentConfig;

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
    private JCheckBox chkHienThiMatHangKhuyenMai;
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
    private JCheckBox chkGiaBanLeUSD;

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
        pnlCardContainer.add(createTabMayIn(), "Máy in");
        pnlCardContainer.add(createTabSoPhieu(), "Số phiếu");
        pnlCardContainer.add(createTabBanHang(), "Bán hàng");
        pnlCardContainer.add(createTabThanhToan(), "Thanh toán");
        pnlCardContainer.add(createTabTichDiem(), "Tích điểm");
        pnlCardContainer.add(createTabKhoHang(), "Kho hàng");
        pnlCardContainer.add(createTabMatHang(), "Mặt hàng");

        // Placeholder cards for other categories
        for (MenuItem item : allMenuItems) {
            if (!"Thông tin chung".equals(item.name) && !"Máy in".equals(item.name) && !"Số phiếu".equals(item.name)
                    && !"Bán hàng".equals(item.name) && !"Thanh toán".equals(item.name) && !"Tích điểm".equals(item.name)
                    && !"Kho hàng".equals(item.name) && !"Mặt hàng".equals(item.name)) {
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
        btnChooseLogo.setBounds(510, startY + 80, 100, 22);
        btnChooseLogo.setBackground(Color.WHITE);
        btnChooseLogo.addActionListener(e -> chooseLogo());
        panel.add(btnChooseLogo);

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

        String[] templates = {
                "Mẫu in bill 54mm",
                "Mẫu in bill 80mm",
                "Mẫu in bill 80 chiết khấu mặt hàng",
                "Mẫu 54 mm x 2 dòng",
                "Mẫu in bill A4",
                "Mẫu in bill A5",
                "Mẫu in A5 cho máy in kim",
                "BIÊN BẢN GIAO NHẬN HÀNG HÓA",
                "Mẫu in chế biến"
        };
        cbMauHoaDon = new JComboBox<>(templates);
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
        } else if (!list.contains("Microsoft Print to PDF")) {
            list.add("Microsoft Print to PDF");
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
        chkHienThiMatHangKhuyenMai = new JCheckBox("Hiển thị mặt hàng khuyến mại khi thanh toán", true);
        chkHienThiMatHangKhuyenMai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkHienThiMatHangKhuyenMai.setOpaque(false);
        chkHienThiMatHangKhuyenMai.setBounds(20, startY, 350, fieldH);
        panel.add(chkHienThiMatHangKhuyenMai);

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
        chkChoPhepKhachNo.setOpaque(false);
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

    // ==================== TAB 8: MẶT HÀNG (Ảnh 1 -> 5) ====================
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

        // Row 17: Giá bán lẻ (USD) (Default: true)
        startY += rowGap;
        chkGiaBanLeUSD = new JCheckBox("Giá bán lẻ (USD)", true);
        chkGiaBanLeUSD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkGiaBanLeUSD.setOpaque(false);
        chkGiaBanLeUSD.setBounds(20, startY, 300, fieldH);
        panel.add(chkGiaBanLeUSD);

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
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            selectedLogoPath = f.getAbsolutePath();
            displayLogo(selectedLogoPath);
        }
    }

    private void displayLogo(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaled = icon.getImage().getScaledInstance(90, 65, Image.SCALE_SMOOTH);
            lblLogoPreview.setIcon(new ImageIcon(scaled));
        } catch (Exception ignored) {}
    }

    private void loadConfigToUI() {
        if (currentConfig == null) return;

        // Tab 1
        txtTenCongTy.setText(currentConfig.getTenCongTy());
        txtDiaChi.setText(currentConfig.getDiaChi());
        selectedLogoPath = currentConfig.getLogoPath();
        if (selectedLogoPath != null && !selectedLogoPath.isEmpty()) {
            displayLogo(selectedLogoPath);
        }
        txtSoDienThoai.setText(currentConfig.getSoDienThoai());
        txtEmail.setText(currentConfig.getEmail());
        txtLoiCamOn.setText(currentConfig.getLoiCamOn());
        txtFax.setText(currentConfig.getFax());
        cbMauHoaDon.setSelectedItem(currentConfig.getMauHoaDon());
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
        chkHienThiMatHangKhuyenMai.setSelected(currentConfig.isHienThiMatHangKhuyenMaiKhiThanhToan());
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
        chkGiaBanLeUSD.setSelected(currentConfig.isGiaBanLeUSD());
    }

    private void onSave() {
        // Tab 1
        currentConfig.setTenCongTy(txtTenCongTy.getText().trim());
        currentConfig.setDiaChi(txtDiaChi.getText().trim());
        currentConfig.setLogoPath(selectedLogoPath);
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
        currentConfig.setHienThiMatHangKhuyenMaiKhiThanhToan(chkHienThiMatHangKhuyenMai.isSelected());
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
        currentConfig.setGiaBanLeUSD(chkGiaBanLeUSD.isSelected());

        boolean ok = configDAO.saveConfig(currentConfig);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã lưu thông tin cấu hình toàn bộ hệ thống thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
