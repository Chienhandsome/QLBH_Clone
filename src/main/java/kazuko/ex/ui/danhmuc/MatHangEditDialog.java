package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.DonViTinhDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.DonViTinh;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class MatHangEditDialog extends JDialog {
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final DonViTinhDAO donViTinhDAO = new DonViTinhDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private SystemConfig systemConfig;

    private MatHang currentItem;
    private final Runnable onSaveCallback;

    // Tab 1: Thông tin chính
    private JLabel lblMaSanCo;
    private JTextField txtMaSanCo;
    private JLabel lblMaTuSinh;
    private JTextField txtCode;
    private JLabel lblTenHang;
    private JTextField txtName;
    private JComboBox<DonViTinh> cbDonViTinh;
    private JComboBox<DonViTinh> cbDonViTinhChan;
    private JLabel lblGiaBan;
    private JTextField txtGiaBan;
    private JTextField txtQuyDoi;
    private JLabel lblQuyDoiGiaiThich;
    private JLabel lblDonGia2;
    private JTextField txtGiaBan2;
    private JLabel lblGiaBanChan;
    private JTextField txtGiaBanChan;
    private JLabel lblGiaBan3;
    private JTextField txtGiaBan3;
    private JLabel lblGiaBan4;
    private JTextField txtGiaBan4;
    private JLabel lblGiaNhap;
    private JTextField txtGiaNhap;
    private JComboBox<NhomMatHang> cbNhomHang;
    private JTextField txtGiamGiaPt;
    private JTextField txtGiamGiaTien;
    private JLabel lblKichThuoc;
    private JTextField txtKichThuoc;
    private JTextField txtNote;

    // Tab 2: Thông tin khác
    private JComboBox<String> cbDoiTacKyGui;
    private JTextField txtTonToiThieu;
    private JTextField txtTonToiDa;
    private JLabel lblHangSX;
    private JComboBox<String> cbHangSanXuat;
    private JLabel lblBaoHanh;
    private JTextField txtBaoHanh;
    private JTextField txtHoaHong;
    private JCheckBox chkKhoiLuongTheoCan;
    private JCheckBox chkCoHanSuDung;
    private JCheckBox chkKhongInCheBien;

    // Tab 3: Định lượng
    private JRadioButton rbKiemVatTu;
    private JRadioButton rbDinhLuongCombo;
    private JRadioButton rbBanKem;
    private JRadioButton rbDichVu;
    private JRadioButton rbVatTuNguyenLieu;
    private JTable tableDinhLuong;
    private DefaultTableModel modelDinhLuong;

    // Right Side: Ảnh & Lịch sử
    private JLabel lblImagePreview;
    private String currentImagePath;

    public MatHangEditDialog(Component parent, MatHang item, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), item.getId() == null ? "MẶT HÀNG - THÊM MỚI" : "MẶT HÀNG - SỬA", ModalityType.APPLICATION_MODAL);
        this.currentItem = item;
        this.onSaveCallback = onSaveCallback;

        initUI();
        loadFormData();
        setupShortcuts();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        this.systemConfig = systemConfigDAO.loadConfig();
        if (this.systemConfig == null) {
            this.systemConfig = new SystemConfig();
        }

        setLayout(new BorderLayout());
        setResizable(true);
        setPreferredSize(new Dimension(860, 600));
        setMinimumSize(new Dimension(800, 540));

        // 1. Top Title Header
        JPanel pnlHeader = createHeaderPanel();
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center Content: Left Tabs + Right Photo & History
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(new Color(240, 245, 252));

        // Left Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPane.addTab("Thông tin chính", createTabThongTinChinh());
        tabbedPane.addTab("Thông tin khác", createTabThongTinKhac());
        
        JPanel pnlDinhLuong = createTabDinhLuong();
        if (systemConfig.isMatHangCoVatTuDinhLuong()) {
            tabbedPane.addTab("Định lượng", pnlDinhLuong);
        }

        pnlCenter.add(tabbedPane, BorderLayout.CENTER);

        // Right Photo & Links Panel
        JPanel pnlRight = createRightSidePanel();
        pnlCenter.add(pnlRight, BorderLayout.EAST);

        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Buttons (Lưu, Lưu&Mới, Lưu&thoát, Thoát)
        JPanel pnlFooter = createFooterPanel();
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());

        // Header Strip
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(210, 225, 245)));
        header.setPreferredSize(new Dimension(800, 38));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        leftHeader.setOpaque(false);
        JLabel lblIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 22, 22));
        JLabel lblTitle = new JLabel("Mặt hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 45, 90));
        leftHeader.add(lblIcon);
        leftHeader.add(lblTitle);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightHeader.setOpaque(false);
        JLabel lblHelp = new JLabel("(Ctrl+S: Lưu&Mới, Ctrl+L: Lưu, Ctrl+N: Mới, Ctrl+P: In, ESC: Thoát)");
        lblHelp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHelp.setForeground(new Color(60, 60, 60));
        rightHeader.add(lblHelp);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);
        pnlTop.add(header, BorderLayout.NORTH);

        // Toolbar Strip
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(new Color(225, 236, 250));
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));
        toolbar.setPreferredSize(new Dimension(800, 28));

        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        leftTools.setOpaque(false);

        JButton btnShortcuts = createToolbarButton("Phím tắt ▾", "tsbQuickOpen.Image.png");
        JButton btnPrev = createToolbarButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createToolbarButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnNew = createToolbarButton("Tạo mới", "plus.png");
        JButton btnCopy = createToolbarButton("Sao chép", "Report.png");

        JPopupMenu popupShortcuts = new JPopupMenu();
        JMenuItem miSave = new JMenuItem("Lưu (Ctrl+L)");
        JMenuItem miSaveNew = new JMenuItem("Lưu & Mới (Ctrl+S)");
        JMenuItem miSaveExit = new JMenuItem("Lưu & Thoát");
        JMenuItem miExit = new JMenuItem("Thoát (ESC)");
        miSave.addActionListener(e -> onSave(false, false));
        miSaveNew.addActionListener(e -> onSave(true, false));
        miSaveExit.addActionListener(e -> onSave(false, true));
        miExit.addActionListener(e -> dispose());
        popupShortcuts.add(miSave);
        popupShortcuts.add(miSaveNew);
        popupShortcuts.add(miSaveExit);
        popupShortcuts.addSeparator();
        popupShortcuts.add(miExit);

        btnShortcuts.addActionListener(e -> popupShortcuts.show(btnShortcuts, 0, btnShortcuts.getHeight()));
        btnNew.addActionListener(e -> onClearForNew());
        btnCopy.addActionListener(e -> onCloneCurrent());

        leftTools.add(btnShortcuts);
        leftTools.add(btnPrev);
        leftTools.add(btnNext);
        leftTools.add(btnNew);
        leftTools.add(btnCopy);

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        rightTools.setOpaque(false);
        JButton btnDelete = createToolbarButton("Xóa", "cross.png");
        btnDelete.setForeground(new Color(180, 20, 20));
        btnDelete.addActionListener(e -> onDelete());
        rightTools.add(btnDelete);

        toolbar.add(leftTools, BorderLayout.WEST);
        toolbar.add(rightTools, BorderLayout.EAST);
        pnlTop.add(toolbar, BorderLayout.SOUTH);

        return pnlTop;
    }

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(245, 248, 253));
        btn.setBorder(new CompoundBorder(new MatteBorder(1, 1, 1, 1, new Color(190, 210, 235)), new EmptyBorder(2, 6, 2, 6)));
        return btn;
    }

    // ==================== TAB 1: THÔNG TIN CHÍNH ====================
    private JComponent createTabThongTinChinh() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int lblW = 100;
        int fieldH = 23;
        int rowGap = 27;
        int startY = 12;

        // Row 1: Mã sẵn có
        lblMaSanCo = new JLabel("Mã sẵn có");
        lblMaSanCo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMaSanCo.setBounds(15, startY, lblW, fieldH);
        panel.add(lblMaSanCo);

        txtMaSanCo = new JTextField();
        txtMaSanCo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        boolean allowEditMaSanCo = systemConfig.isSuDungMaSanCo();
        txtMaSanCo.setEditable(allowEditMaSanCo);
        txtMaSanCo.setBackground(allowEditMaSanCo ? Color.WHITE : new Color(245, 248, 252));
        txtMaSanCo.setBounds(120, startY, 160, fieldH);
        panel.add(txtMaSanCo);

        // Row 2: Mã tự sinh (Mã hàng)
        startY += rowGap;
        lblMaTuSinh = new JLabel("Mã tự sinh");
        lblMaTuSinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMaTuSinh.setBounds(15, startY, lblW, fieldH);
        panel.add(lblMaTuSinh);

        txtCode = new JTextField();
        txtCode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCode.setBounds(120, startY, 160, fieldH);
        panel.add(txtCode);

        if (!systemConfig.isSuDungMaHang()) {
            lblMaTuSinh.setEnabled(false);
            txtCode.setEditable(false);
            txtCode.setBackground(new Color(245, 248, 252));
        }

        // Row 3: Tên hàng
        startY += rowGap;
        lblTenHang = new JLabel("Tên hàng");
        lblTenHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTenHang.setBounds(15, startY, lblW, fieldH);
        panel.add(lblTenHang);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(new Color(255, 255, 225)); // Vàng nhạt nổi bật
        txtName.setBounds(120, startY, 340, fieldH);
        panel.add(txtName);

        // Row 4: Đơn vị tính & ĐVT chẵn
        startY += rowGap;
        JLabel lblDVT = new JLabel("Đơn vị tính");
        lblDVT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDVT.setBounds(15, startY, lblW, fieldH);
        panel.add(lblDVT);

        cbDonViTinh = new JComboBox<>();
        cbDonViTinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDonViTinh.setBounds(120, startY, 95, fieldH);
        cbDonViTinh.addActionListener(e -> updateUnitLabelsAndExplanation());
        panel.add(cbDonViTinh);

        JLabel lblDVTChan = new JLabel("Đơn vị tính chẵn");
        lblDVTChan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDVTChan.setBounds(230, startY, 100, fieldH);
        panel.add(lblDVTChan);

        cbDonViTinhChan = new JComboBox<>();
        cbDonViTinhChan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDonViTinhChan.setBounds(335, startY, 125, fieldH);
        cbDonViTinhChan.setEnabled(systemConfig.isSuDung2DonViTinh());
        cbDonViTinhChan.addActionListener(e -> updateUnitLabelsAndExplanation());
        panel.add(cbDonViTinhChan);

        // Row 5: Giá bán & Quy đổi
        startY += rowGap;
        lblGiaBan = new JLabel("Giá bán");
        lblGiaBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaBan.setBounds(15, startY, lblW, fieldH);
        panel.add(lblGiaBan);

        txtGiaBan = new JTextField("0");
        txtGiaBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaBan.setBounds(120, startY, 95, fieldH);
        panel.add(txtGiaBan);

        JLabel lblQuyDoi = new JLabel("Quy đổi");
        lblQuyDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblQuyDoi.setBounds(230, startY, 100, fieldH);
        panel.add(lblQuyDoi);

        txtQuyDoi = new JTextField("0");
        txtQuyDoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtQuyDoi.setBounds(335, startY, 40, fieldH);
        txtQuyDoi.setEditable(systemConfig.isSuDung2DonViTinh());
        txtQuyDoi.setBackground(systemConfig.isSuDung2DonViTinh() ? Color.WHITE : new Color(240, 240, 240));
        txtQuyDoi.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateUnitLabelsAndExplanation(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateUnitLabelsAndExplanation(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateUnitLabelsAndExplanation(); }
        });
        panel.add(txtQuyDoi);

        lblQuyDoiGiaiThich = new JLabel("");
        lblQuyDoiGiaiThich.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblQuyDoiGiaiThich.setForeground(new Color(60, 60, 60));
        lblQuyDoiGiaiThich.setBounds(380, startY, 140, fieldH);
        panel.add(lblQuyDoiGiaiThich);

        // Row 6: Đơn giá 2 (Diễn giải giá 2) & Giá bán chẵn
        startY += rowGap;
        String dg2Title = (systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty())
                ? systemConfig.getDienGiaiGia2().trim() : "Đơn giá 2";
        lblDonGia2 = new JLabel(dg2Title);
        lblDonGia2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDonGia2.setBounds(15, startY, lblW, fieldH);
        panel.add(lblDonGia2);

        txtGiaBan2 = new JTextField("0");
        txtGiaBan2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaBan2.setBounds(120, startY, 95, fieldH);
        txtGiaBan2.setEditable(systemConfig.isSuDungGia2());
        txtGiaBan2.setBackground(systemConfig.isSuDungGia2() ? Color.WHITE : new Color(240, 240, 240));
        panel.add(txtGiaBan2);

        lblGiaBanChan = new JLabel("Giá bán chẵn");
        lblGiaBanChan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaBanChan.setBounds(230, startY, 100, fieldH);
        panel.add(lblGiaBanChan);

        txtGiaBanChan = new JTextField("0");
        txtGiaBanChan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaBanChan.setBounds(335, startY, 125, fieldH);
        txtGiaBanChan.setEditable(systemConfig.isSuDung2DonViTinh());
        txtGiaBanChan.setBackground(systemConfig.isSuDung2DonViTinh() ? Color.WHITE : new Color(240, 240, 240));
        panel.add(txtGiaBanChan);

        // Row 7: Giá bán 3 (Diễn giải giá 3)
        startY += rowGap;
        String dg3Title = (systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty())
                ? systemConfig.getDienGiaiGia3().trim() : "Giá bán 3";
        lblGiaBan3 = new JLabel(dg3Title);
        lblGiaBan3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaBan3.setBounds(15, startY, lblW, fieldH);
        panel.add(lblGiaBan3);

        txtGiaBan3 = new JTextField("0");
        txtGiaBan3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaBan3.setBounds(120, startY, 95, fieldH);
        txtGiaBan3.setEditable(systemConfig.isSuDungGia3());
        txtGiaBan3.setBackground(systemConfig.isSuDungGia3() ? Color.WHITE : new Color(240, 240, 240));
        panel.add(txtGiaBan3);

        // Row 8: Giá bán 4 (Diễn giải giá 4) & Giá nhập
        startY += rowGap;
        String dg4Title = (systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().trim().isEmpty())
                ? systemConfig.getDienGiaiGia4().trim() : "Giá bán 4";
        lblGiaBan4 = new JLabel(dg4Title);
        lblGiaBan4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaBan4.setBounds(15, startY, lblW, fieldH);
        panel.add(lblGiaBan4);

        txtGiaBan4 = new JTextField("0");
        txtGiaBan4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaBan4.setBounds(120, startY, 95, fieldH);
        txtGiaBan4.setEditable(systemConfig.isSuDungGia4());
        txtGiaBan4.setBackground(systemConfig.isSuDungGia4() ? Color.WHITE : new Color(240, 240, 240));
        panel.add(txtGiaBan4);

        lblGiaNhap = new JLabel("Giá nhập");
        lblGiaNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaNhap.setBounds(230, startY, 100, fieldH);
        panel.add(lblGiaNhap);

        txtGiaNhap = new JTextField("0");
        txtGiaNhap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiaNhap.setBounds(335, startY, 125, fieldH);
        panel.add(txtGiaNhap);

        // Row: Nhóm mặt hàng
        startY += rowGap;
        JLabel lblNhomHang = new JLabel("Nhóm mặt hàng");
        lblNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhomHang.setBounds(15, startY, lblW, fieldH);
        panel.add(lblNhomHang);

        cbNhomHang = new JComboBox<>();
        cbNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhomHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhomMatHang) {
                    NhomMatHang nh = (NhomMatHang) value;
                    lbl.setText(nh.getName());
                    lbl.setIcon(IconHelper.getIcon(nh.isFolder() ? "Folder.png" : "1282529873_objects.png", 14, 14));
                }
                return lbl;
            }
        });
        cbNhomHang.setBounds(120, startY, 340, fieldH);
        panel.add(cbNhomHang);

        // Row: Mặc định giảm giá % & Mặc định giảm tiền
        startY += rowGap;
        JLabel lblGiamGiaPt = new JLabel("Mặc định giảm giá %");
        lblGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiamGiaPt.setBounds(15, startY, 115, fieldH);
        panel.add(lblGiamGiaPt);

        txtGiamGiaPt = new JTextField("0");
        txtGiamGiaPt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiamGiaPt.setBounds(135, startY, 80, fieldH);
        panel.add(txtGiamGiaPt);

        JLabel lblGiamTien = new JLabel("Mặc định giảm tiền");
        lblGiamTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiamTien.setBounds(230, startY, 110, fieldH);
        panel.add(lblGiamTien);

        txtGiamGiaTien = new JTextField("0");
        txtGiamGiaTien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGiamGiaTien.setBounds(345, startY, 115, fieldH);
        panel.add(txtGiamGiaTien);

        // Row: Size (Ví dụ: M, L,S)
        startY += rowGap;
        lblKichThuoc = new JLabel("Size (Ví dụ: M, L,S)");
        lblKichThuoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKichThuoc.setBounds(15, startY, 105, fieldH);
        panel.add(lblKichThuoc);

        txtKichThuoc = new JTextField();
        txtKichThuoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtKichThuoc.setBounds(120, startY, 340, fieldH);
        txtKichThuoc.setEditable(systemConfig.isMatHangCoKichThuoc());
        txtKichThuoc.setBackground(systemConfig.isMatHangCoKichThuoc() ? Color.WHITE : new Color(240, 240, 240));
        panel.add(txtKichThuoc);

        // Row: Ghi chú
        startY += rowGap;
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(15, startY, lblW, fieldH);
        panel.add(lblGhiChu);

        txtNote = new JTextField();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setBounds(120, startY, 340, fieldH);
        panel.add(txtNote);

        panel.setPreferredSize(new Dimension(480, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    // ==================== TAB 2: THÔNG TIN KHÁC ====================
    private JComponent createTabThongTinKhac() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        int lblW = 100;
        int fieldH = 23;
        int rowGap = 28;
        int startY = 12;

        // Row 1: Đối tác ký gửi
        JLabel lblDoiTac = new JLabel("Đối tác ký gửi");
        lblDoiTac.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDoiTac.setBounds(15, startY, lblW, fieldH);
        panel.add(lblDoiTac);

        cbDoiTacKyGui = new JComboBox<>(new String[]{"", "Công ty ABC", "Đại lý phân phối", "Nhà cung cấp chính"});
        cbDoiTacKyGui.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDoiTacKyGui.setBounds(120, startY, 335, fieldH);
        panel.add(cbDoiTacKyGui);

        // Row 2: Tồn tối thiểu & Tồn tối đa
        startY += rowGap;
        JLabel lblTonMin = new JLabel("Tồn tối thiểu");
        lblTonMin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTonMin.setBounds(15, startY, lblW, fieldH);
        panel.add(lblTonMin);

        txtTonToiThieu = new JTextField("0");
        txtTonToiThieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTonToiThieu.setBounds(120, startY, 95, fieldH);
        panel.add(txtTonToiThieu);

        JLabel lblTonMax = new JLabel("Tồn tối đa");
        lblTonMax.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTonMax.setBounds(230, startY, 80, fieldH);
        panel.add(lblTonMax);

        txtTonToiDa = new JTextField("0");
        txtTonToiDa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTonToiDa.setBounds(315, startY, 140, fieldH);
        panel.add(txtTonToiDa);

        // Row 3: Hãng sản xuất
        if (systemConfig.isSuDungHangSanXuat()) {
            startY += rowGap;
            lblHangSX = new JLabel("Hãng sản xuất");
            lblHangSX.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblHangSX.setBounds(15, startY, lblW, fieldH);
            panel.add(lblHangSX);

            cbHangSanXuat = new JComboBox<>(new String[]{"", "Việt Nhật", "Sunhouse", "Samsung", "Panasonic", "VinaKip"});
            cbHangSanXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cbHangSanXuat.setBounds(120, startY, 335, fieldH);
            panel.add(cbHangSanXuat);
        } else {
            lblHangSX = new JLabel("Hãng sản xuất");
            cbHangSanXuat = new JComboBox<>(new String[]{""});
        }

        // Row 4: Bảo hành & Hoa hồng
        startY += rowGap;
        if (systemConfig.isHangCoBaoHanh()) {
            lblBaoHanh = new JLabel("Bảo hành");
            lblBaoHanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblBaoHanh.setBounds(15, startY, lblW, fieldH);
            panel.add(lblBaoHanh);

            txtBaoHanh = new JTextField();
            txtBaoHanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txtBaoHanh.setBounds(120, startY, 65, fieldH);
            panel.add(txtBaoHanh);
        } else {
            lblBaoHanh = new JLabel("Bảo hành");
            txtBaoHanh = new JTextField();
        }

        JLabel lblHoaHong = new JLabel("Hoa hồng cho nhân viên kd");
        lblHoaHong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHoaHong.setBounds(systemConfig.isHangCoBaoHanh() ? 195 : 15, startY, systemConfig.isHangCoBaoHanh() ? 150 : lblW, fieldH);
        panel.add(lblHoaHong);

        txtHoaHong = new JTextField("0");
        txtHoaHong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtHoaHong.setBounds(systemConfig.isHangCoBaoHanh() ? 350 : 120, startY, systemConfig.isHangCoBaoHanh() ? 105 : 140, fieldH);
        panel.add(txtHoaHong);

        // Row 5: Checkboxes
        startY += rowGap + 5;
        chkKhoiLuongTheoCan = new JCheckBox("Khối lượng theo cân");
        chkKhoiLuongTheoCan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhoiLuongTheoCan.setOpaque(false);
        chkKhoiLuongTheoCan.setBounds(120, startY, 200, 20);
        chkKhoiLuongTheoCan.setEnabled(systemConfig.isKichHoatSuDungCanDienTu());
        panel.add(chkKhoiLuongTheoCan);

        startY += 24;
        chkCoHanSuDung = new JCheckBox("Có hạn sử dụng");
        chkCoHanSuDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkCoHanSuDung.setOpaque(false);
        chkCoHanSuDung.setBounds(120, startY, 200, 20);
        chkCoHanSuDung.setEnabled(systemConfig.isMatHangCoHanSuDung());
        panel.add(chkCoHanSuDung);

        startY += 24;
        chkKhongInCheBien = new JCheckBox("Không in chế biến");
        chkKhongInCheBien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhongInCheBien.setOpaque(false);
        chkKhongInCheBien.setBounds(120, startY, 200, 20);
        panel.add(chkKhongInCheBien);

        panel.setPreferredSize(new Dimension(480, startY + 40));
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    // ==================== TAB 3: ĐỊNH LƯỢNG ====================
    private JPanel createTabDinhLuong() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Top Radio Buttons: Phương thức trừ kho
        JPanel pnlRadio = new JPanel();
        pnlRadio.setLayout(new BoxLayout(pnlRadio, BoxLayout.Y_AXIS));
        pnlRadio.setOpaque(false);

        JLabel lblTitlePT = new JLabel("Phương thức trừ kho");
        lblTitlePT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitlePT.setForeground(new Color(25, 50, 95));
        pnlRadio.add(lblTitlePT);
        pnlRadio.add(Box.createVerticalStrut(4));

        rbKiemVatTu = new JRadioButton("Mặt hàng kiêm vật tư (Định lượng nếu sản xuất, có bán, có tồn)", true);
        rbDinhLuongCombo = new JRadioButton("Định lượng, Combo (Mặt hàng chính không có tồn kho, Vật tư xuất khi bán hàng)");
        rbBanKem = new JRadioButton("Mặt hàng bán kèm (Mặt hàng chính có tồn kho, Vật tư xuất cùng khi bán hàng)");
        rbDichVu = new JRadioButton("Dịch vụ (Có bán, không nhập, có tồn)");
        rbVatTuNguyenLieu = new JRadioButton("Vật tư nguyên liệu (Không bán, Có tồn)");

        Font rFont = new Font("Segoe UI", Font.PLAIN, 12);
        rbKiemVatTu.setFont(rFont); rbKiemVatTu.setOpaque(false);
        rbDinhLuongCombo.setFont(rFont); rbDinhLuongCombo.setOpaque(false);
        rbBanKem.setFont(rFont); rbBanKem.setOpaque(false);
        rbDichVu.setFont(rFont); rbDichVu.setOpaque(false);
        rbVatTuNguyenLieu.setFont(rFont); rbVatTuNguyenLieu.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbKiemVatTu);
        bg.add(rbDinhLuongCombo);
        bg.add(rbBanKem);
        bg.add(rbDichVu);
        bg.add(rbVatTuNguyenLieu);

        pnlRadio.add(rbKiemVatTu);
        pnlRadio.add(rbDinhLuongCombo);
        pnlRadio.add(rbBanKem);
        pnlRadio.add(rbDichVu);
        pnlRadio.add(rbVatTuNguyenLieu);

        panel.add(pnlRadio, BorderLayout.NORTH);

        // Center Table: Vật tư định lượng
        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setOpaque(false);
        pnlTableContainer.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Sub Toolbar
        JPanel subToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        subToolbar.setBackground(new Color(230, 240, 252));
        subToolbar.setBorder(new MatteBorder(1, 1, 0, 1, new Color(185, 205, 230)));

        JButton btnAddRow = new JButton("Thêm dòng (F4)");
        btnAddRow.setIcon(IconHelper.getIcon("plus.png", 14, 14));
        btnAddRow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAddRow.setBackground(Color.WHITE);

        JButton btnDelRow = new JButton("Xóa dòng (F8)");
        btnDelRow.setIcon(IconHelper.getIcon("cross.png", 14, 14));
        btnDelRow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDelRow.setBackground(Color.WHITE);

        JButton btnExportExcel = new JButton("Xuất excel");
        btnExportExcel.setIcon(IconHelper.getIcon("Report.png", 14, 14));
        btnExportExcel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnExportExcel.setBackground(Color.WHITE);

        btnAddRow.addActionListener(e -> modelDinhLuong.addRow(new Object[]{"", "", "", "1"}));
        btnDelRow.addActionListener(e -> {
            int sel = tableDinhLuong.getSelectedRow();
            if (sel >= 0) modelDinhLuong.removeRow(sel);
        });

        subToolbar.add(btnAddRow);
        subToolbar.add(btnDelRow);
        subToolbar.add(btnExportExcel);
        pnlTableContainer.add(subToolbar, BorderLayout.NORTH);

        String[] columns = {"", "Vật tư", "ĐVT", "Số lượng"};
        modelDinhLuong = new DefaultTableModel(columns, 0);
        tableDinhLuong = new JTable(modelDinhLuong);
        tableDinhLuong.setRowHeight(22);
        tableDinhLuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDinhLuong.setShowGrid(true);
        tableDinhLuong.setGridColor(new Color(225, 235, 245));

        JTableHeader th = tableDinhLuong.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(240, 245, 252));
        th.setPreferredSize(new Dimension(100, 24));

        tableDinhLuong.getColumnModel().getColumn(0).setPreferredWidth(26);
        tableDinhLuong.getColumnModel().getColumn(0).setMaxWidth(30);
        tableDinhLuong.getColumnModel().getColumn(1).setPreferredWidth(260);
        tableDinhLuong.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableDinhLuong.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(tableDinhLuong);
        scrollPane.setBorder(new MatteBorder(0, 1, 1, 1, new Color(185, 205, 230)));
        pnlTableContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(pnlTableContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==================== RIGHT SIDE PANEL: ẢNH & LỊCH SỬ ====================
    private JPanel createRightSidePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 400));
        panel.setBackground(new Color(240, 245, 252));
        panel.setBorder(new EmptyBorder(10, 10, 10, 15));

        // Top: Label Ảnh
        JPanel pnlPhotoContainer = new JPanel();
        pnlPhotoContainer.setLayout(new BoxLayout(pnlPhotoContainer, BoxLayout.Y_AXIS));
        pnlPhotoContainer.setOpaque(false);

        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAnh.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPhotoContainer.add(lblAnh);
        pnlPhotoContainer.add(Box.createVerticalStrut(4));

        // Photo Preview Box
        JPanel pnlBox = new JPanel(new BorderLayout());
        pnlBox.setPreferredSize(new Dimension(250, 190));
        pnlBox.setMaximumSize(new Dimension(250, 190));
        pnlBox.setBackground(new Color(210, 222, 238));
        pnlBox.setBorder(new MatteBorder(1, 1, 1, 1, new Color(150, 175, 205)));

        lblImagePreview = new JLabel();
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        pnlBox.add(lblImagePreview, BorderLayout.CENTER);
        pnlPhotoContainer.add(pnlBox);
        pnlPhotoContainer.add(Box.createVerticalStrut(6));

        // 3 Action Buttons: Paste, Browse, Delete
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnPaste = createMiniPhotoButton("Report.png", "Dán ảnh từ Clipboard");
        JButton btnBrowse = createMiniPhotoButton("Folder.png", "Chọn ảnh từ máy tính");
        JButton btnDelete = createMiniPhotoButton("cross.png", "Xóa ảnh");

        btnPaste.addActionListener(e -> pasteImageFromClipboard());
        btnBrowse.addActionListener(e -> browseImageFile());
        btnDelete.addActionListener(e -> removeImage());

        pnlButtons.add(btnPaste);
        pnlButtons.add(btnBrowse);
        pnlButtons.add(btnDelete);
        pnlPhotoContainer.add(pnlButtons);
        pnlPhotoContainer.add(Box.createVerticalStrut(15));

        // 2 Hyperlinks: Lịch sử nhập mua & Lịch sử xuất bán
        JPanel pnlLinks = new JPanel();
        pnlLinks.setLayout(new BoxLayout(pnlLinks, BoxLayout.Y_AXIS));
        pnlLinks.setOpaque(false);
        pnlLinks.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLSMua = createHyperlinkLabel("Lịch sử nhập mua hàng", "arrow-000-medium.png");
        JLabel lblLSBan = createHyperlinkLabel("Lịch sử xuất bán hàng", "arrow-180-medium.png");

        lblLSMua.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LichSuMuaHangDialog(MatHangEditDialog.this, currentItem).setVisible(true);
            }
        });

        lblLSBan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new LichSuBanHangDialog(MatHangEditDialog.this, currentItem).setVisible(true);
            }
        });

        pnlLinks.add(lblLSMua);
        pnlLinks.add(Box.createVerticalStrut(8));
        pnlLinks.add(lblLSBan);
        pnlPhotoContainer.add(pnlLinks);

        panel.add(pnlPhotoContainer, BorderLayout.NORTH);
        return panel;
    }

    private JButton createMiniPhotoButton(String iconName, String tooltip) {
        JButton btn = new JButton();
        btn.setIcon(IconHelper.getIcon(iconName, 14, 14));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));
        return btn;
    }

    private JLabel createHyperlinkLabel(String text, String iconName) {
        JLabel lbl = new JLabel(text);
        if (iconName != null) {
            lbl.setIcon(IconHelper.getIcon(iconName, 14, 14));
        }
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(0, 80, 190));
        lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lbl.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lbl.setText(text);
            }
        });
        return lbl;
    }

    // ==================== BOTTOM FOOTER PANEL ====================
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setBackground(new Color(236, 242, 250));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        JButton btnSave = new JButton("Lưu");
        btnSave.setIcon(IconHelper.getIcon("btnSave.Image.png", 14, 14));
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.setPreferredSize(new Dimension(85, 26));
        btnSave.setBackground(Color.WHITE);
        btnSave.addActionListener(e -> onSave(false, false));

        JButton btnSaveNew = new JButton("Lưu & Mới");
        btnSaveNew.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSaveNew.setPreferredSize(new Dimension(95, 26));
        btnSaveNew.setBackground(Color.WHITE);
        btnSaveNew.addActionListener(e -> onSave(true, false));

        JButton btnSaveExit = new JButton("Lưu & thoát");
        btnSaveExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSaveExit.setPreferredSize(new Dimension(95, 26));
        btnSaveExit.setBackground(Color.WHITE);
        btnSaveExit.addActionListener(e -> onSave(false, true));

        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(85, 26));
        btnExit.setBackground(Color.WHITE);
        btnExit.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnSaveNew);
        footer.add(btnSaveExit);
        footer.add(btnExit);
        return footer;
    }

    // ==================== LOAD & SAVE DATA ====================
    private void loadFormData() {
        // 1. Nhóm hàng
        cbNhomHang.removeAllItems();
        List<NhomMatHang> nhomList = nhomMatHangDAO.getAll();
        NhomMatHang selNhom = null;
        for (NhomMatHang nh : nhomList) {
            cbNhomHang.addItem(nh);
            if (currentItem.getNhomMatHangId() != null && currentItem.getNhomMatHangId().equals(nh.getId())) {
                selNhom = nh;
            } else if (currentItem.getTenNhom() != null && currentItem.getTenNhom().equalsIgnoreCase(nh.getName())) {
                selNhom = nh;
            }
        }
        if (selNhom != null) cbNhomHang.setSelectedItem(selNhom);

        // 2. Đơn vị tính
        cbDonViTinh.removeAllItems();
        cbDonViTinhChan.removeAllItems();
        cbDonViTinhChan.addItem(new DonViTinh("", "", "")); // Blank option
        List<DonViTinh> dvtList = donViTinhDAO.getAll();
        DonViTinh selDvt = null;
        DonViTinh selDvtChan = null;
        for (DonViTinh dvt : dvtList) {
            cbDonViTinh.addItem(dvt);
            cbDonViTinhChan.addItem(dvt);
            if (currentItem.getDonViTinhId() != null && currentItem.getDonViTinhId().equals(dvt.getId())) {
                selDvt = dvt;
            } else if (currentItem.getTenDonViTinh() != null && currentItem.getTenDonViTinh().equalsIgnoreCase(dvt.getName())) {
                selDvt = dvt;
            }
            if (currentItem.getDonViTinhChanId() != null && currentItem.getDonViTinhChanId().equals(dvt.getId())) {
                selDvtChan = dvt;
            } else if (currentItem.getTenDonViTinhChan() != null && currentItem.getTenDonViTinhChan().equalsIgnoreCase(dvt.getName())) {
                selDvtChan = dvt;
            }
        }
        if (selDvt != null) {
            cbDonViTinh.setSelectedItem(selDvt);
        } else if (currentItem.getTenDonViTinh() != null && !currentItem.getTenDonViTinh().trim().isEmpty()) {
            DonViTinh custom = new DonViTinh(currentItem.getDonViTinhId() != null ? currentItem.getDonViTinhId() : "", "", currentItem.getTenDonViTinh().trim());
            cbDonViTinh.addItem(custom);
            cbDonViTinh.setSelectedItem(custom);
        }

        if (selDvtChan != null) {
            cbDonViTinhChan.setSelectedItem(selDvtChan);
        } else if (currentItem.getTenDonViTinhChan() != null && !currentItem.getTenDonViTinhChan().trim().isEmpty()) {
            DonViTinh customChan = new DonViTinh(currentItem.getDonViTinhChanId() != null ? currentItem.getDonViTinhChanId() : "", "", currentItem.getTenDonViTinhChan().trim());
            cbDonViTinhChan.addItem(customChan);
            cbDonViTinhChan.setSelectedItem(customChan);
        } else {
            cbDonViTinhChan.setSelectedIndex(0);
        }

        // 3. Tab 1 Values
        this.systemConfig = systemConfigDAO.loadConfig();
        if (this.systemConfig == null) {
            this.systemConfig = new SystemConfig();
        }

        boolean allow2Units = systemConfig.isSuDung2DonViTinh();
        cbDonViTinhChan.setEnabled(allow2Units);
        txtQuyDoi.setEditable(allow2Units);
        txtQuyDoi.setBackground(allow2Units ? Color.WHITE : new Color(240, 240, 240));
        txtGiaBanChan.setEditable(allow2Units);
        txtGiaBanChan.setBackground(allow2Units ? Color.WHITE : new Color(240, 240, 240));

        String dg2Title = (systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty())
                ? systemConfig.getDienGiaiGia2().trim() : "Đơn giá 2";
        lblDonGia2.setText(dg2Title);
        txtGiaBan2.setEditable(systemConfig.isSuDungGia2());
        txtGiaBan2.setBackground(systemConfig.isSuDungGia2() ? Color.WHITE : new Color(240, 240, 240));

        String dg3Title = (systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty())
                ? systemConfig.getDienGiaiGia3().trim() : "Giá bán 3";
        lblGiaBan3.setText(dg3Title);
        txtGiaBan3.setEditable(systemConfig.isSuDungGia3());
        txtGiaBan3.setBackground(systemConfig.isSuDungGia3() ? Color.WHITE : new Color(240, 240, 240));

        String dg4Title = (systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().trim().isEmpty())
                ? systemConfig.getDienGiaiGia4().trim() : "Giá bán 4";
        lblGiaBan4.setText(dg4Title);
        txtGiaBan4.setEditable(systemConfig.isSuDungGia4());
        txtGiaBan4.setBackground(systemConfig.isSuDungGia4() ? Color.WHITE : new Color(240, 240, 240));

        txtMaSanCo.setText(currentItem.getMaSanCo() != null ? currentItem.getMaSanCo() : (currentItem.getId() != null ? currentItem.getId() : "1000073"));
        if (currentItem.getCode() != null && !currentItem.getCode().isEmpty()) {
            txtCode.setText(currentItem.getCode());
        } else {
            String prefix = selNhom != null && selNhom.getCode() != null ? selNhom.getCode() : "MH";
            txtCode.setText(matHangDAO.generateProductCodeOrBarcode(systemConfig, prefix, currentItem.isKhoiLuongTheoCan()));
        }

        txtName.setText(currentItem.getName() != null ? currentItem.getName() : "");

        DecimalFormat df = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.US));
        txtGiaBan.setText(df.format(currentItem.getGiaBan()));
        txtGiaNhap.setText(df.format(currentItem.getGiaNhap()));
        txtGiaBan2.setText(df.format(currentItem.getGiaBan2()));
        txtGiaBan3.setText(df.format(currentItem.getGiaBan3()));
        txtGiaBan4.setText(df.format(currentItem.getGiaBan4()));
        txtGiaBanChan.setText(df.format(currentItem.getGiaBanChan()));
        txtQuyDoi.setText(df.format(currentItem.getQuyDoi()));
        txtGiamGiaPt.setText(df.format(currentItem.getGiamGiaPt()));
        txtGiamGiaTien.setText(df.format(currentItem.getGiamGiaTien()));
        txtKichThuoc.setEditable(systemConfig.isMatHangCoKichThuoc());
        txtKichThuoc.setBackground(systemConfig.isMatHangCoKichThuoc() ? Color.WHITE : new Color(240, 240, 240));
        txtKichThuoc.setText(currentItem.getKichThuoc() != null ? currentItem.getKichThuoc() : "");
        txtNote.setText(currentItem.getGhiChu() != null ? currentItem.getGhiChu() : "");

        updateUnitLabelsAndExplanation();

        // 4. Tab 2 Values
        if (currentItem.getDoiTacKyGui() != null) cbDoiTacKyGui.setSelectedItem(currentItem.getDoiTacKyGui());
        if (currentItem.getHangSanXuat() != null) cbHangSanXuat.setSelectedItem(currentItem.getHangSanXuat());
        txtTonToiThieu.setText(df.format(currentItem.getTonToiThieu()));
        txtTonToiDa.setText(df.format(currentItem.getTonToiDa()));
        txtBaoHanh.setText(currentItem.getBaoHanh() != null ? currentItem.getBaoHanh() : "");
        txtHoaHong.setText(df.format(currentItem.getHoaHong()));
        chkKhoiLuongTheoCan.setEnabled(systemConfig.isKichHoatSuDungCanDienTu());
        chkKhoiLuongTheoCan.setSelected(currentItem.isKhoiLuongTheoCan());
        chkCoHanSuDung.setEnabled(systemConfig.isMatHangCoHanSuDung());
        chkCoHanSuDung.setSelected(currentItem.isCoHanSuDung());
        chkKhongInCheBien.setSelected(currentItem.isKhongInCheBien());

        // 5. Tab 3 Values
        if (rbKiemVatTu != null) {
            switch (currentItem.getPhuongThucTruKho()) {
                case 1: if (rbDinhLuongCombo != null) rbDinhLuongCombo.setSelected(true); break;
                case 2: if (rbBanKem != null) rbBanKem.setSelected(true); break;
                case 3: if (rbDichVu != null) rbDichVu.setSelected(true); break;
                case 4: if (rbVatTuNguyenLieu != null) rbVatTuNguyenLieu.setSelected(true); break;
                default: rbKiemVatTu.setSelected(true); break;
            }
        }

        // 6. Image
        if (currentItem.getImagePath() != null && !currentItem.getImagePath().isEmpty()) {
            displayImage(currentItem.getImagePath());
        }
    }

    private void onSave(boolean isSaveAndNew, boolean isSaveAndExit) {
        String code = txtCode.getText().trim();
        String name = txtName.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        // Kiểm tra cấu hình cho phép / không cho phép trùng tên mặt hàng
        if (systemConfig != null && !systemConfig.isChoPhepTrungTenMatHang()) {
            if (matHangDAO.existsByName(name, currentItem.getId())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Tên mặt hàng '" + name + "' đã tồn tại trong danh mục!\n(Hệ thống đang cấu hình Không cho phép trùng tên mặt hàng)",
                        "Trùng tên mặt hàng",
                        JOptionPane.WARNING_MESSAGE
                );
                txtName.requestFocus();
                return;
            }
        }

        if (code.isEmpty()) {
            NhomMatHang selNhom = (NhomMatHang) cbNhomHang.getSelectedItem();
            String prefix = selNhom != null && selNhom.getCode() != null ? selNhom.getCode() : "MH";
            code = matHangDAO.generateProductCodeOrBarcode(systemConfig, prefix, chkKhoiLuongTheoCan.isSelected());
        }

        currentItem.setCode(code);
        currentItem.setName(name);
        currentItem.setMaSanCo(txtMaSanCo.getText().trim());

        currentItem.setGiaBan(parseNumber(txtGiaBan.getText()));
        currentItem.setGiaNhap(parseNumber(txtGiaNhap.getText()));
        currentItem.setGiaBan2(parseNumber(txtGiaBan2.getText()));
        currentItem.setGiaBan3(parseNumber(txtGiaBan3.getText()));
        currentItem.setGiaBan4(parseNumber(txtGiaBan4.getText()));
        currentItem.setGiaBanChan(parseNumber(txtGiaBanChan.getText()));
        currentItem.setQuyDoi(parseNumber(txtQuyDoi.getText()));
        currentItem.setGiamGiaPt(parseNumber(txtGiamGiaPt.getText()));
        currentItem.setGiamGiaTien(parseNumber(txtGiamGiaTien.getText()));
        currentItem.setKichThuoc(txtKichThuoc.getText().trim());
        currentItem.setGhiChu(txtNote.getText().trim());

        NhomMatHang selNhom = (NhomMatHang) cbNhomHang.getSelectedItem();
        if (selNhom != null) {
            currentItem.setNhomMatHangId(selNhom.getId());
            currentItem.setTenNhom(selNhom.getName());
        }

        DonViTinh selDvt = (DonViTinh) cbDonViTinh.getSelectedItem();
        if (selDvt != null && selDvt.getId() != null && !selDvt.getId().trim().isEmpty()) {
            currentItem.setDonViTinhId(selDvt.getId());
            currentItem.setTenDonViTinh(selDvt.getName());
        } else {
            currentItem.setDonViTinhId(null);
            currentItem.setTenDonViTinh(null);
        }

        DonViTinh selDvtChan = (DonViTinh) cbDonViTinhChan.getSelectedItem();
        if (selDvtChan != null && selDvtChan.getId() != null && !selDvtChan.getId().trim().isEmpty()) {
            currentItem.setDonViTinhChanId(selDvtChan.getId());
            currentItem.setTenDonViTinhChan(selDvtChan.getName());
        } else {
            currentItem.setDonViTinhChanId(null);
            currentItem.setTenDonViTinhChan(null);
        }

        // Tab 2
        currentItem.setDoiTacKyGui(cbDoiTacKyGui.getSelectedItem() != null ? cbDoiTacKyGui.getSelectedItem().toString() : "");
        currentItem.setHangSanXuat(cbHangSanXuat.getSelectedItem() != null ? cbHangSanXuat.getSelectedItem().toString() : "");
        currentItem.setTonToiThieu(parseNumber(txtTonToiThieu.getText()));
        currentItem.setTonToiDa(parseNumber(txtTonToiDa.getText()));
        currentItem.setBaoHanh(txtBaoHanh.getText().trim());
        currentItem.setHoaHong(parseNumber(txtHoaHong.getText()));
        currentItem.setKhoiLuongTheoCan(chkKhoiLuongTheoCan.isSelected());
        currentItem.setCoHanSuDung(chkCoHanSuDung.isSelected());
        currentItem.setKhongInCheBien(chkKhongInCheBien.isSelected());

        // Tab 3
        if (rbDinhLuongCombo != null && rbBanKem != null && rbDichVu != null && rbVatTuNguyenLieu != null) {
            if (rbDinhLuongCombo.isSelected()) currentItem.setPhuongThucTruKho(1);
            else if (rbBanKem.isSelected()) currentItem.setPhuongThucTruKho(2);
            else if (rbDichVu.isSelected()) currentItem.setPhuongThucTruKho(3);
            else if (rbVatTuNguyenLieu.isSelected()) currentItem.setPhuongThucTruKho(4);
            else currentItem.setPhuongThucTruKho(0);
        }

        currentItem.setImagePath(currentImagePath);

        boolean success;
        if (currentItem.getId() == null) {
            success = matHangDAO.insert(currentItem);
        } else {
            success = matHangDAO.update(currentItem);
        }

        if (success) {
            if (onSaveCallback != null) onSaveCallback.run();

            if (isSaveAndNew) {
                onClearForNew();
                JOptionPane.showMessageDialog(this, "Đã lưu thành công! Mời nhập mặt hàng mới.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else if (isSaveAndExit) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu dữ liệu mặt hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lưu mặt hàng vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUnitLabelsAndExplanation() {
        if (lblGiaBan == null || lblGiaBanChan == null || cbDonViTinh == null || cbDonViTinhChan == null) {
            return;
        }

        DonViTinh selDvt = (DonViTinh) cbDonViTinh.getSelectedItem();
        String tenDvt = (selDvt != null && selDvt.getName() != null && !selDvt.getName().trim().isEmpty())
                ? selDvt.getName().trim() : "";

        DonViTinh selDvtChan = (DonViTinh) cbDonViTinhChan.getSelectedItem();
        String tenDvtChan = (selDvtChan != null && selDvtChan.getName() != null && !selDvtChan.getName().trim().isEmpty())
                ? selDvtChan.getName().trim() : "";

        // 1. Cập nhật nhãn Giá bán
        if (!tenDvt.isEmpty()) {
            lblGiaBan.setText("Giá bán " + tenDvt);
        } else {
            lblGiaBan.setText("Giá bán");
        }

        // 2. Cập nhật nhãn Giá bán chẵn
        if (!tenDvtChan.isEmpty()) {
            lblGiaBanChan.setText("Giá bán " + tenDvtChan);
        } else {
            lblGiaBanChan.setText("Giá bán chẵn");
        }

        // 3. Cập nhật diễn giải quy đổi: (1 bao = 10 Cây)
        if (lblQuyDoiGiaiThich != null) {
            String qdStr = txtQuyDoi != null ? txtQuyDoi.getText().trim() : "0";
            if (qdStr.isEmpty()) qdStr = "0";

            if (!tenDvtChan.isEmpty() && !tenDvt.isEmpty()) {
                lblQuyDoiGiaiThich.setText("(1 " + tenDvtChan + " = " + qdStr + " " + tenDvt + ")");
                lblQuyDoiGiaiThich.setVisible(true);
            } else {
                lblQuyDoiGiaiThich.setText("");
                lblQuyDoiGiaiThich.setVisible(false);
            }
        }
    }

    private void onClearForNew() {
        currentItem = new MatHang();
        String nextCode = matHangDAO.generateProductCodeOrBarcode(systemConfig, "MH", false);
        txtMaSanCo.setText("1000" + (int)(Math.random() * 900 + 100));
        txtCode.setText(nextCode);
        txtName.setText("");
        txtGiaBan.setText("0");
        txtGiaNhap.setText("0");
        txtGiaBan2.setText("0");
        txtGiaBan3.setText("0");
        txtGiaBan4.setText("0");
        txtGiaBanChan.setText("0");
        txtQuyDoi.setText("0");
        txtGiamGiaPt.setText("0");
        txtGiamGiaTien.setText("0");
        txtKichThuoc.setText("");
        txtNote.setText("");
        if (cbDonViTinhChan.getItemCount() > 0) cbDonViTinhChan.setSelectedIndex(0);
        if (rbKiemVatTu != null) rbKiemVatTu.setSelected(true);
        if (modelDinhLuong != null) modelDinhLuong.setRowCount(0);
        updateUnitLabelsAndExplanation();
        removeImage();
        txtName.requestFocus();
    }

    private void onCloneCurrent() {
        MatHang cloned = new MatHang();
        cloned.setName(txtName.getText().trim() + " (Sao chép)");
        String nextCode = matHangDAO.generateProductCodeOrBarcode(systemConfig, "MH", chkKhoiLuongTheoCan.isSelected());
        cloned.setCode(nextCode);
        cloned.setGiaBan(parseNumber(txtGiaBan.getText()));
        cloned.setGiaNhap(parseNumber(txtGiaNhap.getText()));
        cloned.setGiaBan2(parseNumber(txtGiaBan2.getText()));
        cloned.setGiaBan3(parseNumber(txtGiaBan3.getText()));
        cloned.setGiaBan4(parseNumber(txtGiaBan4.getText()));
        cloned.setGiaBanChan(parseNumber(txtGiaBanChan.getText()));
        cloned.setQuyDoi(parseNumber(txtQuyDoi.getText()));
        cloned.setGiamGiaPt(parseNumber(txtGiamGiaPt.getText()));
        cloned.setGiamGiaTien(parseNumber(txtGiamGiaTien.getText()));
        cloned.setKichThuoc(txtKichThuoc.getText().trim());
        DonViTinh selDvt = (DonViTinh) cbDonViTinh.getSelectedItem();
        if (selDvt != null && selDvt.getId() != null && !selDvt.getId().trim().isEmpty()) {
            cloned.setDonViTinhId(selDvt.getId());
            cloned.setTenDonViTinh(selDvt.getName());
        }
        DonViTinh selDvtChan = (DonViTinh) cbDonViTinhChan.getSelectedItem();
        if (selDvtChan != null && selDvtChan.getId() != null && !selDvtChan.getId().trim().isEmpty()) {
            cloned.setDonViTinhChanId(selDvtChan.getId());
            cloned.setTenDonViTinhChan(selDvtChan.getName());
        }
        cloned.setPhuongThucTruKho(currentItem.getPhuongThucTruKho());
        this.currentItem = cloned;
        txtName.setText(cloned.getName());
        txtCode.setText(nextCode);
        txtName.requestFocus();
    }

    private void onDelete() {
        if (currentItem.getId() == null) return;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa mặt hàng '" + currentItem.getName() + "'?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            matHangDAO.delete(currentItem.getId());
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        }
    }

    private void pasteImageFromClipboard() {
        try {
            Transferable tr = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (tr != null && tr.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                Image img = (Image) tr.getTransferData(DataFlavor.imageFlavor);
                if (img != null) {
                    Image scaled = img.getScaledInstance(230, 180, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaled));
                    currentImagePath = "clipboard_image";
                }
            }
        } catch (Exception ignored) {}
    }

    private void browseImageFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            displayImage(f.getAbsolutePath());
        }
    }

    private void displayImage(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaled = icon.getImage().getScaledInstance(230, 180, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(scaled));
            currentImagePath = path;
        } catch (Exception ignored) {}
    }

    private void removeImage() {
        lblImagePreview.setIcon(null);
        currentImagePath = null;
    }

    private double parseNumber(String val) {
        if (val == null) return 0.0;
        String s = val.trim();
        if (s.isEmpty()) return 0.0;
        try {
            s = s.replaceAll("\\s+", "");
            if (s.contains(".") && s.contains(",")) {
                if (s.lastIndexOf('.') > s.lastIndexOf(',')) {
                    s = s.replace(",", "");
                } else {
                    s = s.replace(".", "").replace(",", ".");
                }
            } else if (s.contains(",")) {
                int commaIndex = s.lastIndexOf(',');
                int decimals = s.length() - commaIndex - 1;
                if (s.indexOf(',') != commaIndex || decimals == 3) {
                    s = s.replace(",", "");
                } else {
                    s = s.replace(",", ".");
                }
            } else if (s.contains(".")) {
                int dotIndex = s.lastIndexOf('.');
                int decimals = s.length() - dotIndex - 1;
                if (s.indexOf('.') != dotIndex) {
                    s = s.replace(".", "");
                } else if (decimals == 3 && s.length() > 4) {
                    s = s.replace(".", "");
                }
            }
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void setupShortcuts() {
        // Ctrl + S -> Lưu & Mới
        getRootPane().registerKeyboardAction(e -> onSave(true, false), KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl + L -> Lưu
        getRootPane().registerKeyboardAction(e -> onSave(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Ctrl + N -> Mới
        getRootPane().registerKeyboardAction(e -> onClearForNew(), KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // ESC -> Thoát
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
