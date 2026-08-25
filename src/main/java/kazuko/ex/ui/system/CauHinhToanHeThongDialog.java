package kazuko.ex.ui.system;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.IconHelper;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
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

        // Placeholder cards for other categories
        for (MenuItem item : allMenuItems) {
            if (!"Thông tin chung".equals(item.name) && !"Máy in".equals(item.name) && !"Số phiếu".equals(item.name)) {
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

    // ==================== TAB 1: THÔNG TIN CHÍNH (Ảnh 1) ====================
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

    // ==================== TAB 2: MÁY IN (Ảnh 1 - Dropdown & Nhận diện máy in) ====================
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

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        JPanel container = new JPanel(new BorderLayout());
        container.add(scrollPane, BorderLayout.CENTER);
        return container;
    }

    /**
     * Nhận diện và lấy danh sách tất cả các máy in đang kết nối với thiết bị (OS)
     */
    private String[] getAvailablePrinters() {
        List<String> list = new ArrayList<>();
        list.add(""); // Lựa chọn rỗng / mặc định đầu tiên

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

        // Fallback options phổ biến nếu hệ thống chưa nhận diện được
        if (list.size() <= 1) {
            list.add("Microsoft Print to PDF");
            list.add("Fax");
        } else if (!list.contains("Microsoft Print to PDF")) {
            list.add("Microsoft Print to PDF");
        }

        return list.toArray(new String[0]);
    }

    // ==================== TAB 3: SỐ PHIẾU (Ảnh 2 - Readonly Field + Dialog) ====================
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
            txt.setEditable(false); // Không thể edit trực tiếp
            txt.setBackground(new Color(250, 252, 255)); // Nền sáng chỉ đọc
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
