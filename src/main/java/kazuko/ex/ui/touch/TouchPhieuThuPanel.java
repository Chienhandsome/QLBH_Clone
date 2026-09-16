package kazuko.ex.ui.touch;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Màn hình tạo và cập nhật phiếu thu dành cho giao diện cảm ứng. */
public final class TouchPhieuThuPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(105, 143, 31);
    private static final Color SELECT_HEADER = new Color(79, 139, 169);
    private static final Color CARD = new Color(0, 79, 76);
    private static final Color CARD_SELECTED = new Color(211, 84, 0);
    private static final Color GREEN = new Color(0, 100, 25);
    private static final Color RED = new Color(165, 0, 0);
    private static final Color GRAY = new Color(145, 145, 145);
    private static final Color TEAL = new Color(0, 82, 91);
    private static final Color DISABLED_FIELD = new Color(238, 238, 238);
    private static final Color YELLOW_FIELD = new Color(245, 255, 188);
    private static final Color CYAN_FIELD = new Color(220, 250, 252);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final LyDoThuChiDAO reasonDAO = new LyDoThuChiDAO();
    private final KhachHangDAO customerDAO = new KhachHangDAO();
    private final NhanVienDAO employeeDAO = new NhanVienDAO();
    private final NhaCungCapDAO supplierDAO = new NhaCungCapDAO();
    private final CuaHangDAO storeDAO = new CuaHangDAO();
    private final Runnable exit;

    private JLabel titleLabel;
    private final CardLayout navigation = new CardLayout();
    private final JPanel screens = new JPanel(navigation);
    private final JTextField dateField = field(YELLOW_FIELD);
    private final JTextField voucherField = field(CYAN_FIELD);
    private final JTextField reasonField = field(YELLOW_FIELD);
    private final JTextField purposeField = field(YELLOW_FIELD);
    private final JTextField subjectNameField = field(YELLOW_FIELD);
    private final JTextField addressField = field(Color.WHITE);
    private final JTextField employeeField = field(Color.WHITE);
    private final JTextField customerField = field(Color.WHITE);
    private final JTextField supplierField = field(Color.WHITE);
    private final JTextField originalDocumentField = field(Color.WHITE);
    private final JTextField amountField = field(YELLOW_FIELD);
    private final JTextField noteField = field(Color.WHITE);
    private final JCheckBox transferCheck = new JCheckBox("CHUYỂN KHOẢN");
    private final JButton subjectTypeButton = button("ĐỐI TƯỢNG KHÁC", Color.WHITE, Color.BLACK, 360, 50);

    private final JButton employeeSearch = iconButton("⌕");
    private final JButton customerSearch = iconButton("⌕");
    private final JButton supplierSearch = iconButton("⌕");
    private JButton subjectNameKb;
    private JButton addressKb;

    private final JLabel searchTitle = headerLabel("TÌM KIẾM DỮ LIỆU", SELECT_HEADER, 65);
    private final JLabel searchBadge = headerLabel("", GREEN, 45);
    private final TouchScrollableRowsPanel searchCards = new TouchScrollableRowsPanel();
    private final StringBuilder searchBuffer = new StringBuilder();

    private PhieuThu currentReceipt;
    private LyDoThuChi selectedReason;
    private NhanVien selectedEmployee;
    private KhachHang selectedCustomer;
    private NhaCungCap selectedSupplier;
    private CuaHang selectedStore;
    private SearchKind searchKind = SearchKind.REASON;
    private SearchEntry selectedSearchEntry;

    private List<LyDoThuChi> reasons = new ArrayList<>();
    private List<NhanVien> employees = new ArrayList<>();
    private List<KhachHang> customers = new ArrayList<>();
    private List<NhaCungCap> suppliers = new ArrayList<>();
    private List<CuaHang> stores = new ArrayList<>();

    public TouchPhieuThuPanel(Runnable exit) {
        this(exit, null);
    }

    public TouchPhieuThuPanel(Runnable exit, String customerId, String customerName, String address, Double debtAmount) {
        this(exit, null);
        setPreselectedCustomer(customerId, customerName, address, debtAmount);
    }

    public TouchPhieuThuPanel(Runnable exit, PhieuThu receiptToEdit) {
        this.exit = exit;
        DATE_FORMAT.setLenient(false);
        setLayout(new BorderLayout());
        screens.add(createMainScreen(), "main");
        screens.add(createSubjectTypeScreen(), "subjectType");
        screens.add(createSearchScreen(), "search");
        add(screens, BorderLayout.CENTER);
        loadLookups();
        setupFieldSync();
        if (receiptToEdit != null) {
            loadReceipt(receiptToEdit);
        } else {
            resetForNew();
        }
    }

    private JPanel createMainScreen() {
        JPanel root = basePanel();
        root.add(createMainHeader(), BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(16, 10, 8, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addLabel(form, gbc, "NGÀY", 0, 0);
        addComponent(form, gbc, dateField, 1, 0, 1, 0.28);
        JButton dateButton = iconButton("▼");
        dateButton.addActionListener(e -> chooseDate());
        addComponent(form, gbc, dateButton, 2, 0, 1, 0);
        addLabel(form, gbc, "SỐ PHIẾU", 3, 0);
        addComponent(form, gbc, voucherField, 4, 0, 1, 0.72);
        addComponent(form, gbc, keyboardButton(voucherField, "NHẬP SỐ PHIẾU"), 5, 0, 1, 0);

        addLabel(form, gbc, "PHÂN LOẠI", 0, 1);
        addComponent(form, gbc, reasonField, 1, 1, 4, 1);
        JButton reasonSearch = iconButton("⌕");
        reasonSearch.addActionListener(e -> openSearch(SearchKind.REASON));
        addComponent(form, gbc, reasonSearch, 5, 1, 1, 0);

        addLabel(form, gbc, "LÝ DO", 0, 2);
        addComponent(form, gbc, purposeField, 1, 2, 4, 1);
        addComponent(form, gbc, keyboardButton(purposeField, "NHẬP LÝ DO THU"), 5, 2, 1, 0);

        addLabel(form, gbc, "TÊN ĐỐI TƯỢNG", 0, 3);
        addComponent(form, gbc, subjectNameField, 1, 3, 1, 0.5);
        subjectNameKb = keyboardButton(subjectNameField, "NHẬP TÊN ĐỐI TƯỢNG");
        addComponent(form, gbc, subjectNameKb, 2, 3, 1, 0);
        addLabel(form, gbc, "LOẠI ĐỐI TƯỢNG", 3, 3);
        subjectTypeButton.addActionListener(e -> navigation.show(screens, "subjectType"));
        addComponent(form, gbc, subjectTypeButton, 4, 3, 1, 0.5);
        JButton subjectTypeDrop = iconButton("▼");
        subjectTypeDrop.addActionListener(e -> navigation.show(screens, "subjectType"));
        addComponent(form, gbc, subjectTypeDrop, 5, 3, 1, 0);

        addLabel(form, gbc, "ĐỊA CHỈ", 0, 4);
        addComponent(form, gbc, addressField, 1, 4, 1, 0.5);
        addressKb = keyboardButton(addressField, "NHẬP ĐỊA CHỈ");
        addComponent(form, gbc, addressKb, 2, 4, 1, 0);

        addLabel(form, gbc, "NHÂN VIÊN", 0, 5);
        addComponent(form, gbc, employeeField, 1, 5, 1, 0.5);
        employeeSearch.addActionListener(e -> openSearch(SearchKind.EMPLOYEE));
        addComponent(form, gbc, employeeSearch, 2, 5, 1, 0);

        addLabel(form, gbc, "KHÁCH HÀNG", 0, 6);
        addComponent(form, gbc, customerField, 1, 6, 1, 0.5);
        customerSearch.addActionListener(e -> openSearch(SearchKind.CUSTOMER));
        addComponent(form, gbc, customerSearch, 2, 6, 1, 0);

        addLabel(form, gbc, "NHÀ CUNG CẤP", 0, 7);
        addComponent(form, gbc, supplierField, 1, 7, 1, 0.5);
        supplierSearch.addActionListener(e -> openSearch(SearchKind.SUPPLIER));
        addComponent(form, gbc, supplierSearch, 2, 7, 1, 0);

        addLabel(form, gbc, "CHỨNG TỪ GỐC", 0, 8);
        addComponent(form, gbc, originalDocumentField, 1, 8, 1, 0.5);
        addComponent(form, gbc, keyboardButton(originalDocumentField, "NHẬP CHỨNG TỪ GỐC"), 2, 8, 1, 0);

        addLabel(form, gbc, "SỐ TIỀN THU", 0, 9);
        amountField.setHorizontalAlignment(SwingConstants.RIGHT);
        addComponent(form, gbc, amountField, 1, 9, 1, 0.5);
        JButton amountButton = iconButton("▦");
        amountButton.addActionListener(e -> chooseAmount());
        addComponent(form, gbc, amountButton, 2, 9, 1, 0);
        transferCheck.setOpaque(false);
        transferCheck.setForeground(Color.WHITE);
        transferCheck.setFont(new Font("Segoe UI", Font.BOLD, 20));
        transferCheck.setFocusPainted(false);
        transferCheck.setIcon(new LargeCheckBoxIcon());
        transferCheck.setSelectedIcon(new LargeCheckBoxIcon());
        addComponent(form, gbc, transferCheck, 3, 9, 2, 0.5);

        addLabel(form, gbc, "GHI CHÚ", 0, 10);
        addComponent(form, gbc, noteField, 1, 10, 4, 1);
        addComponent(form, gbc, keyboardButton(noteField, "NHẬP GHI CHÚ"), 5, 10, 1, 0);

        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 6;
        gbc.weighty = 1;
        form.add(Box.createVerticalGlue(), gbc);
        root.add(form, BorderLayout.CENTER);
        root.add(createMainFooter(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel createMainHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(HEADER);
        header.setBorder(new LineBorder(new Color(70, 110, 20), 2, true));
        header.setPreferredSize(new Dimension(0, 80));

        titleLabel = new JLabel("PHIẾU THU");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setBorder(new EmptyBorder(0, 8, 0, 0));
        header.add(titleLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 4));
        actions.setOpaque(false);
        JButton previous = button("TRƯỚC", GREEN, Color.WHITE, 120, 68);
        previous.addActionListener(e -> navigateReceipt(-1));
        JButton next = button("SAU", GRAY, Color.WHITE, 120, 68);
        next.addActionListener(e -> navigateReceipt(1));
        JButton create = button("TẠO MỚI", GREEN, Color.WHITE, 135, 68);
        create.addActionListener(e -> resetForNew());
        JButton copy = button("SAO CHÉP", GRAY, Color.WHITE, 135, 68);
        copy.addActionListener(e -> copyReceipt());
        JButton delete = button("XÓA", GRAY, Color.WHITE, 120, 68);
        delete.addActionListener(e -> deleteReceipt());
        actions.add(previous);
        actions.add(next);
        actions.add(create);
        actions.add(copy);
        actions.add(delete);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createMainFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 5, 0, 5));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton savePrint = button("LƯU & IN", GREEN, Color.WHITE, 160, 68);
        savePrint.addActionListener(e -> saveAndPrint(true));
        JButton savePreview = button("LƯU & XEM IN", GREEN, Color.WHITE, 225, 68);
        savePreview.addActionListener(e -> saveAndPrint(false));
        left.add(savePrint);
        left.add(savePreview);
        footer.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton save = button("LƯU", GRAY, Color.WHITE, 160, 68);
        save.addActionListener(e -> saveReceipt(false, false));
        JButton saveNew = button("LƯU & MỚI", GRAY, Color.WHITE, 185, 68);
        saveNew.addActionListener(e -> saveReceipt(true, false));
        JButton saveExit = button("LƯU & THOÁT", GRAY, Color.WHITE, 210, 68);
        saveExit.addActionListener(e -> saveReceipt(false, true));
        JButton close = button("THOÁT", RED, Color.WHITE, 150, 68);
        close.addActionListener(e -> exit.run());
        right.add(save);
        right.add(saveNew);
        right.add(saveExit);
        right.add(close);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private JPanel createSubjectTypeScreen() {
        JPanel root = basePanel();
        root.add(headerLabel("CHỌN DỮ LIỆU", SELECT_HEADER, 65), BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(4, 1, 0, 8));
        choices.setOpaque(false);
        choices.setBorder(new EmptyBorder(8, 3, 0, 3));
        addSubjectTypeButton(choices, "ĐỐI TƯỢNG KHÁC", "Đối tượng khác");
        addSubjectTypeButton(choices, "NHÂN VIÊN", "Nhân viên");
        addSubjectTypeButton(choices, "KHÁCH HÀNG", "Khách hàng");
        addSubjectTypeButton(choices, "NHÀ CUNG CẤP", "Nhà cung cấp");

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(choices, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        JButton close = button("THOÁT", RED, Color.WHITE, 310, 72);
        close.addActionListener(e -> navigation.show(screens, "main"));
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void addSubjectTypeButton(JPanel panel, String text, String value) {
        JButton choice = button(text, CARD, Color.WHITE, 1000, 105);
        choice.addActionListener(e -> {
            chooseSubjectType(value);
            navigation.show(screens, "main");
        });
        panel.add(choice);
    }

    private JPanel createSearchScreen() {
        JPanel root = basePanel();
        root.add(searchTitle, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        searchCards.configureGrid(2, 4, 8, 8);
        searchCards.setBackground(Color.WHITE);
        searchCards.setBorder(new EmptyBorder(7, 7, 7, 7));
        JScrollPane scroll = new JScrollPane(searchCards,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 560));
        scroll.setBorder(new EmptyBorder(5, 3, 0, 3));
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        center.add(scroll, BorderLayout.CENTER);

        JPanel keyboardArea = new JPanel(new BorderLayout(0, 3));
        keyboardArea.setOpaque(false);
        searchBadge.setOpaque(true);
        searchBadge.setBackground(new Color(255, 235, 0));
        searchBadge.setForeground(Color.RED);
        searchBadge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        searchBadge.setBorder(new LineBorder(Color.ORANGE, 1, true));
        searchBadge.setVisible(false);
        JPanel badgeBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badgeBox.setOpaque(false);
        badgeBox.add(searchBadge);
        keyboardArea.add(badgeBox, BorderLayout.NORTH);
        keyboardArea.add(new TouchEmbeddedKeyboardPanel(
                this::appendSearch,
                this::backspaceSearch,
                () -> appendSearch(" "),
                true
        ), BorderLayout.CENTER);
        keyboardArea.setPreferredSize(new Dimension(0, 345));
        center.add(keyboardArea, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(5, 3, 0, 3));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        JButton add = button("THÊM MỚI", GREEN, Color.WHITE, 180, 65);
        add.addActionListener(e -> addSearchItem());
        JButton edit = button("CHỈNH SỬA", GREEN, Color.WHITE, 195, 65);
        edit.addActionListener(e -> editSearchItem());
        left.add(add);
        left.add(edit);
        footer.add(left, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        JButton clear = button("XÓA TRỐNG", new Color(150, 10, 135), Color.WHITE, 180, 65);
        clear.addActionListener(e -> clearSearch());
        JButton close = button("THOÁT", RED, Color.WHITE, 150, 65);
        close.addActionListener(e -> navigation.show(screens, "main"));
        right.add(clear);
        right.add(close);
        footer.add(right, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private void loadLookups() {
        reasons = reasonDAO.getAllActive().stream()
                .filter(reason -> !reason.isFolder() && !reason.isSeparator() && reason.isLaLyDoThu())
                .toList();
        employees = employeeDAO.getAll().stream()
                .filter(employee -> employee.getStatus() >= 0 && !employee.isFolder() && !employee.isSeparator())
                .toList();
        customers = customerDAO.getAll().stream()
                .filter(customer -> customer.getStatus() >= 0)
                .toList();
        suppliers = supplierDAO.getAll().stream()
                .filter(supplier -> supplier.getStatus() >= 0)
                .toList();
        stores = storeDAO.getRealStores();
        if (!stores.isEmpty() && selectedStore == null) selectedStore = stores.get(0);
    }

    public void resetForNew() {
        if (titleLabel != null) {
            titleLabel.setText("PHIẾU THU");
        }
        currentReceipt = new PhieuThu();
        dateField.setText(DATE_FORMAT.format(new Date()));
        voucherField.setText(phieuThuDAO.generateNextSoPhieu(new Date()));
        selectedReason = findDefaultReason();
        reasonField.setText(selectedReason != null ? safe(selectedReason.getName()) : "");
        purposeField.setText(selectedReason != null ? safe(selectedReason.getName()) : "");
        selectedEmployee = null;
        selectedCustomer = null;
        selectedSupplier = null;
        employeeField.setText("");
        customerField.setText("");
        supplierField.setText("");
        subjectNameField.setText("");
        addressField.setText("");
        originalDocumentField.setText("");
        amountField.setText("0");
        noteField.setText("");
        transferCheck.setSelected(false);
        chooseSubjectType("Đối tượng khác");
        navigation.show(screens, "main");
    }

    /**
     * Chọn sẵn khách hàng cho phiếu thu, cập nhật loại đối tượng thành "Khách hàng"
     * và điền tên, địa chỉ, số tiền nếu có.
     */
    public void setPreselectedCustomer(String customerId, String customerName, String address, Double debtAmount) {
        KhachHang customer = findCustomer(customerId);
        if (customer == null && customerName != null) {
            customer = customers.stream()
                    .filter(c -> customerName.equalsIgnoreCase(c.getName()))
                    .findFirst().orElse(null);
        }
        if (customer != null) {
            selectedCustomer = customer;
            customerField.setText(safe(customer.getName()));
        } else {
            selectedCustomer = new KhachHang();
            selectedCustomer.setId(customerId);
            selectedCustomer.setName(customerName);
            selectedCustomer.setDiaChi(address);
            customerField.setText(safe(customerName));
        }

        chooseSubjectType("Khách hàng");

        if (address != null && !address.isBlank()) {
            addressField.setText(address.trim());
        }

        // Ưu tiên chọn lý do thu nợ khách hàng nếu có
        for (LyDoThuChi r : reasons) {
            if (r.getName() != null) {
                String rName = r.getName().toLowerCase(Locale.ROOT);
                if (rName.contains("thu nợ") || rName.contains("nợ khách")) {
                    selectedReason = r;
                    reasonField.setText(r.getName());
                    purposeField.setText(r.getName());
                    break;
                }
            }
        }

        if (debtAmount != null && debtAmount > 0) {
            amountField.setText(CurrencyUtil.format(debtAmount));
        }
    }

    private LyDoThuChi findDefaultReason() {
        String defaultId = reasonDAO.getDefaultReasonId();
        for (LyDoThuChi reason : reasons) {
            if (reason.getId() != null && reason.getId().equals(defaultId)) return reason;
        }
        return reasons.isEmpty() ? null : reasons.get(0);
    }

    private void chooseDate() {
        String selected = TouchDatePickerDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), dateField.getText().trim());
        if (selected != null) {
            dateField.setText(selected);
            if (currentReceipt == null || currentReceipt.getId() == null) {
                voucherField.setText(phieuThuDAO.generateNextSoPhieu(parseDate(selected)));
            }
        }
    }

    private void chooseAmount() {
        Double selected = TouchNumberInputDialog.showDoubleDialog(
                SwingUtilities.getWindowAncestor(this), "SỐ TIỀN THU", parseAmount(), 0, 999_999_999_999d);
        if (selected != null) amountField.setText(CurrencyUtil.format(selected));
    }

    private JButton keyboardButton(JTextField target, String title) {
        JButton keyboard = iconButton("⌨");
        keyboard.addActionListener(e -> {
            if (!target.isEditable() || !keyboard.isEnabled()) return;
            String value = TouchTextKeyboardDialog.showDialog(
                    SwingUtilities.getWindowAncestor(this), title, target.getText());
            if (value != null) target.setText(value);
        });
        return keyboard;
    }

    private void chooseSubjectType(String type) {
        subjectTypeButton.setText(type.toUpperCase(Locale.ROOT));
        updateSubjectFieldsState(type);
        applySelectedSubject(type);
    }

    private void updateSubjectFieldsState(String type) {
        boolean isCustomer = "Khách hàng".equalsIgnoreCase(type);
        boolean isEmployee = "Nhân viên".equalsIgnoreCase(type);
        boolean isSupplier = "Nhà cung cấp".equalsIgnoreCase(type);
        boolean isOther = !isCustomer && !isEmployee && !isSupplier;

        // 1. Customer field & search button
        customerField.setEditable(isCustomer);
        customerField.setBackground(isCustomer ? Color.WHITE : DISABLED_FIELD);
        updateButtonState(customerSearch, isCustomer);

        // 2. Employee field & search button
        employeeField.setEditable(isEmployee);
        employeeField.setBackground(isEmployee ? Color.WHITE : DISABLED_FIELD);
        updateButtonState(employeeSearch, isEmployee);

        // 3. Supplier field & search button
        supplierField.setEditable(isSupplier);
        supplierField.setBackground(isSupplier ? Color.WHITE : DISABLED_FIELD);
        updateButtonState(supplierSearch, isSupplier);

        // 4. Subject name field & keyboard button
        subjectNameField.setEditable(isOther);
        subjectNameField.setBackground(isOther ? YELLOW_FIELD : DISABLED_FIELD);
        if (subjectNameKb != null) {
            updateButtonState(subjectNameKb, isOther);
        }

        // 5. Address field & keyboard button
        addressField.setEditable(isOther);
        addressField.setBackground(isOther ? Color.WHITE : DISABLED_FIELD);
        if (addressKb != null) {
            updateButtonState(addressKb, isOther);
        }

        // Clear non-active fields when switching type
        if (isCustomer) {
            employeeField.setText("");
            supplierField.setText("");
            selectedEmployee = null;
            selectedSupplier = null;
        } else if (isEmployee) {
            customerField.setText("");
            supplierField.setText("");
            selectedCustomer = null;
            selectedSupplier = null;
        } else if (isSupplier) {
            customerField.setText("");
            employeeField.setText("");
            selectedCustomer = null;
            selectedEmployee = null;
        } else {
            customerField.setText("");
            employeeField.setText("");
            supplierField.setText("");
            selectedCustomer = null;
            selectedEmployee = null;
            selectedSupplier = null;
        }
    }

    private void updateButtonState(JButton button, boolean active) {
        button.setEnabled(active);
        button.setBackground(active ? TEAL : GRAY);
    }

    private void setupFieldSync() {
        customerField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if ("Khách hàng".equalsIgnoreCase(selectedSubjectType()) && customerField.isEditable()) {
                subjectNameField.setText(customerField.getText());
            }
        }));
        employeeField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if ("Nhân viên".equalsIgnoreCase(selectedSubjectType()) && employeeField.isEditable()) {
                subjectNameField.setText(employeeField.getText());
            }
        }));
        supplierField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            if ("Nhà cung cấp".equalsIgnoreCase(selectedSubjectType()) && supplierField.isEditable()) {
                subjectNameField.setText(supplierField.getText());
            }
        }));
    }

    private void applySelectedSubject(String type) {
        switch (type) {
            case "Nhân viên" -> {
                if (selectedEmployee != null) {
                    subjectNameField.setText(safe(selectedEmployee.getName()));
                    addressField.setText(safe(selectedEmployee.getDiaChi()));
                } else if (!employeeField.getText().isBlank()) {
                    if (subjectNameField.getText().isBlank()) {
                        subjectNameField.setText(employeeField.getText().trim());
                    }
                }
            }
            case "Khách hàng" -> {
                if (selectedCustomer != null) {
                    subjectNameField.setText(safe(selectedCustomer.getName()));
                    addressField.setText(safe(selectedCustomer.getDiaChi()));
                } else if (!customerField.getText().isBlank()) {
                    if (subjectNameField.getText().isBlank()) {
                        subjectNameField.setText(customerField.getText().trim());
                    }
                }
            }
            case "Nhà cung cấp" -> {
                if (selectedSupplier != null) {
                    subjectNameField.setText(safe(selectedSupplier.getName()));
                    addressField.setText(safe(selectedSupplier.getDiaChi()));
                } else if (!supplierField.getText().isBlank()) {
                    if (subjectNameField.getText().isBlank()) {
                        subjectNameField.setText(supplierField.getText().trim());
                    }
                }
            }
            default -> {
                // Đối tượng khác được nhập trực tiếp.
            }
        }
    }

    private String selectedSubjectType() {
        String text = subjectTypeButton.getText();
        if ("NHÂN VIÊN".equals(text)) return "Nhân viên";
        if ("KHÁCH HÀNG".equals(text)) return "Khách hàng";
        if ("NHÀ CUNG CẤP".equals(text)) return "Nhà cung cấp";
        return "Đối tượng khác";
    }

    private void openSearch(SearchKind kind) {
        searchKind = kind;
        selectedSearchEntry = null;
        searchBuffer.setLength(0);
        searchTitle.setText(kind.title);
        updateSearchBadge();
        renderSearchCards();
        navigation.show(screens, "search");
    }

    private void appendSearch(String value) {
        searchBuffer.append(value);
        updateSearchBadge();
        renderSearchCards();
    }

    private void backspaceSearch() {
        if (!searchBuffer.isEmpty()) searchBuffer.deleteCharAt(searchBuffer.length() - 1);
        updateSearchBadge();
        renderSearchCards();
    }

    private void clearSearch() {
        searchBuffer.setLength(0);
        selectedSearchEntry = null;
        updateSearchBadge();
        renderSearchCards();
    }

    private void updateSearchBadge() {
        String keyword = searchBuffer.toString();
        searchBadge.setText("  " + keyword + "  ");
        searchBadge.setVisible(!keyword.isBlank());
    }

    private void renderSearchCards() {
        searchCards.removeAll();
        searchCards.configureGrid(2, 4, 8, 8);
        String keyword = normalize(searchBuffer.toString());
        for (SearchEntry entry : getSearchEntries()) {
            if (!keyword.isBlank() && !normalize(entry.name).contains(keyword)) continue;
            JButton card = button(entry.name, CARD, Color.WHITE, 300, 110);
            if (selectedSearchEntry != null && selectedSearchEntry.value == entry.value) {
                card.setBackground(CARD_SELECTED);
            }
            card.addActionListener(e -> selectSearchEntry(entry));
            searchCards.add(card);
        }
        searchCards.completeGrid();
        searchCards.revalidate();
        searchCards.repaint();
    }

    private List<SearchEntry> getSearchEntries() {
        List<SearchEntry> entries = new ArrayList<>();
        switch (searchKind) {
            case REASON -> reasons.forEach(item -> entries.add(
                    new SearchEntry(item, safe(item.getName()), "")));
            case EMPLOYEE -> employees.forEach(item -> entries.add(
                    new SearchEntry(item, safe(item.getName()), safe(item.getDiaChi()))));
            case CUSTOMER -> customers.forEach(item -> entries.add(
                    new SearchEntry(item, safe(item.getName()), safe(item.getDiaChi()))));
            case SUPPLIER -> suppliers.forEach(item -> entries.add(
                    new SearchEntry(item, safe(item.getName()), safe(item.getDiaChi()))));
        }
        return entries;
    }

    private void selectSearchEntry(SearchEntry entry) {
        selectedSearchEntry = entry;
        switch (searchKind) {
            case REASON -> {
                selectedReason = (LyDoThuChi) entry.value;
                reasonField.setText(entry.name);
                purposeField.setText(entry.name);
            }
            case EMPLOYEE -> {
                selectedEmployee = (NhanVien) entry.value;
                employeeField.setText(entry.name);
                chooseSubjectType("Nhân viên");
            }
            case CUSTOMER -> {
                selectedCustomer = (KhachHang) entry.value;
                customerField.setText(entry.name);
                chooseSubjectType("Khách hàng");
            }
            case SUPPLIER -> {
                selectedSupplier = (NhaCungCap) entry.value;
                supplierField.setText(entry.name);
                chooseSubjectType("Nhà cung cấp");
            }
        }
        navigation.show(screens, "main");
    }

    private void addSearchItem() {
        String name = TouchTextKeyboardDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), "THÊM MỚI " + searchKind.label, searchBuffer.toString());
        if (name == null || name.trim().isEmpty()) return;
        boolean saved = false;
        switch (searchKind) {
            case REASON -> {
                LyDoThuChi reason = new LyDoThuChi();
                reason.setName(name.trim());
                reason.setLaLyDoThu(true);
                reason.setLaLyDoChi(false);
                saved = reasonDAO.insert(reason);
            }
            case CUSTOMER -> {
                KhachHang customer = new KhachHang();
                customer.setName(name.trim());
                saved = customerDAO.insert(customer);
            }
            case EMPLOYEE, SUPPLIER -> JOptionPane.showMessageDialog(this,
                    "Vui lòng thêm mới trong màn hình danh mục " + searchKind.label.toLowerCase(Locale.ROOT) + ".",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
        if (saved) {
            loadLookups();
            searchBuffer.setLength(0);
            searchBuffer.append(name.trim());
            updateSearchBadge();
            renderSearchCards();
        }
    }

    private void editSearchItem() {
        if (selectedSearchEntry == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dữ liệu cần chỉnh sửa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String name = TouchTextKeyboardDialog.showDialog(
                SwingUtilities.getWindowAncestor(this), "CHỈNH SỬA " + searchKind.label,
                selectedSearchEntry.name);
        if (name == null || name.trim().isEmpty()) return;

        boolean saved = switch (searchKind) {
            case REASON -> {
                LyDoThuChi value = (LyDoThuChi) selectedSearchEntry.value;
                value.setName(name.trim());
                yield reasonDAO.update(value);
            }
            case CUSTOMER -> {
                KhachHang value = (KhachHang) selectedSearchEntry.value;
                value.setName(name.trim());
                yield customerDAO.update(value);
            }
            case EMPLOYEE -> {
                NhanVien value = (NhanVien) selectedSearchEntry.value;
                value.setName(name.trim());
                yield employeeDAO.update(value);
            }
            case SUPPLIER -> {
                NhaCungCap value = (NhaCungCap) selectedSearchEntry.value;
                value.setName(name.trim());
                yield supplierDAO.update(value);
            }
        };
        if (saved) {
            loadLookups();
            selectedSearchEntry = null;
            renderSearchCards();
        }
    }

    private boolean collectFormToModel() {
        if (voucherField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số phiếu không được để trống!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (currentReceipt == null) currentReceipt = new PhieuThu();
        currentReceipt.setNgay(parseDate(dateField.getText()));
        currentReceipt.setSoPhieu(voucherField.getText().trim());
        currentReceipt.setPhanLoaiId(selectedReason != null ? selectedReason.getId() : null);
        currentReceipt.setTenPhanLoai(reasonField.getText().trim());
        currentReceipt.setLyDoThu(purposeField.getText().trim());
        currentReceipt.setChungTuGoc(originalDocumentField.getText().trim());
        currentReceipt.setLoaiDoiTuong(selectedSubjectType());
        currentReceipt.setTenDoiTuong(subjectNameField.getText().trim());
        currentReceipt.setDiaChi(addressField.getText().trim());

        if ("Nhân viên".equals(selectedSubjectType())) {
            String empName = employeeField.getText().trim();
            currentReceipt.setTenNhanVien(empName.isEmpty() ? null : empName);
            currentReceipt.setNhanVienId(selectedEmployee != null && empName.equalsIgnoreCase(safe(selectedEmployee.getName()))
                    ? selectedEmployee.getId() : null);
            if (currentReceipt.getTenDoiTuong().isEmpty()) {
                currentReceipt.setTenDoiTuong(empName);
            }
        } else {
            currentReceipt.setNhanVienId(null);
            currentReceipt.setTenNhanVien(null);
        }

        if ("Khách hàng".equals(selectedSubjectType())) {
            String custName = customerField.getText().trim();
            currentReceipt.setTenKhachHang(custName.isEmpty() ? null : custName);
            currentReceipt.setKhachHangId(selectedCustomer != null && custName.equalsIgnoreCase(safe(selectedCustomer.getName()))
                    ? selectedCustomer.getId() : null);
            if (currentReceipt.getTenDoiTuong().isEmpty()) {
                currentReceipt.setTenDoiTuong(custName);
            }
        } else {
            currentReceipt.setKhachHangId(null);
            currentReceipt.setTenKhachHang(null);
        }

        if ("Nhà cung cấp".equals(selectedSubjectType())) {
            String supName = supplierField.getText().trim();
            currentReceipt.setTenNhaCungCap(supName.isEmpty() ? null : supName);
            currentReceipt.setNhaCungCapId(selectedSupplier != null && supName.equalsIgnoreCase(safe(selectedSupplier.getName()))
                    ? selectedSupplier.getId() : null);
            if (currentReceipt.getTenDoiTuong().isEmpty()) {
                currentReceipt.setTenDoiTuong(supName);
            }
        } else {
            currentReceipt.setNhaCungCapId(null);
            currentReceipt.setTenNhaCungCap(null);
        }
        currentReceipt.setSoTien(parseAmount());
        currentReceipt.setChuyenVaoTaiKhoan(transferCheck.isSelected());
        currentReceipt.setTaiKhoanNganHangId(null);
        currentReceipt.setTenTaiKhoanNganHang(null);
        if (selectedStore != null) {
            currentReceipt.setCuaHangId(selectedStore.getId());
            currentReceipt.setTenCuaHang(selectedStore.getName());
        }
        currentReceipt.setKhongThayDoiCongNo(false);
        currentReceipt.setGhiChu(noteField.getText().trim());
        return true;
    }

    private boolean saveReceipt(boolean createNewAfter, boolean closeAfter) {
        if (!collectFormToModel()) return false;
        boolean isNew = currentReceipt.getId() == null || currentReceipt.getId().isBlank();
        boolean saved = isNew ? phieuThuDAO.insert(currentReceipt) : phieuThuDAO.update(currentReceipt);
        if (!saved) {
            JOptionPane.showMessageDialog(this, "Không thể lưu phiếu thu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        JOptionPane.showMessageDialog(this,
                "Đã lưu phiếu thu " + currentReceipt.getSoPhieu() + " thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        if (createNewAfter) resetForNew();
        else if (closeAfter) exit.run();
        return true;
    }

    private void saveAndPrint(boolean directPrint) {
        if (saveReceipt(false, false)) {
            String action = directPrint ? "Đang gửi lệnh in phiếu thu " : "Mở bản xem trước in phiếu thu ";
            JOptionPane.showMessageDialog(this, action + currentReceipt.getSoPhieu(),
                "In phiếu thu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyReceipt() {
        if (!collectFormToModel()) return;
        currentReceipt.setId(null);
        currentReceipt.setSoPhieu(phieuThuDAO.generateNextSoPhieu(parseDate(dateField.getText())));
        voucherField.setText(currentReceipt.getSoPhieu());
        JOptionPane.showMessageDialog(this,
                "Đã tạo bản sao mới với số phiếu: " + currentReceipt.getSoPhieu(),
                "Sao chép phiếu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteReceipt() {
        if (currentReceipt == null || currentReceipt.getId() == null) {
            JOptionPane.showMessageDialog(this, "Phiếu thu chưa được lưu nên không thể xóa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),
                "BẠN CÓ MUỐN XÓA PHIẾU THU<br>" + safe(currentReceipt.getSoPhieu()) + " KHÔNG?")) return;
        if (phieuThuDAO.delete(currentReceipt.getId())) resetForNew();
    }

    private void navigateReceipt(int direction) {
        String currentId = currentReceipt != null ? currentReceipt.getId() : null;
        PhieuThu target = phieuThuDAO.getPhieuThuByOffset(currentId, direction);
        if (target == null) {
            JOptionPane.showMessageDialog(this,
                    direction < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        loadReceipt(target);
    }

    public void loadReceipt(PhieuThu receipt) {
        if (titleLabel != null && receipt != null) {
            titleLabel.setText(receipt.getId() != null ? "CHỈNH SỬA PHIẾU THU" : "PHIẾU THU");
        }
        currentReceipt = receipt;
        dateField.setText(DATE_FORMAT.format(receipt.getNgay() != null ? receipt.getNgay() : new Date()));
        voucherField.setText(safe(receipt.getSoPhieu()));
        selectedReason = findReason(receipt.getPhanLoaiId());
        reasonField.setText(!safe(receipt.getTenPhanLoai()).isBlank()
                ? receipt.getTenPhanLoai() : selectedReason != null ? safe(selectedReason.getName()) : "");
        purposeField.setText(safe(receipt.getLyDoThu()));
        originalDocumentField.setText(safe(receipt.getChungTuGoc()));
        selectedEmployee = findEmployee(receipt.getNhanVienId());
        selectedCustomer = findCustomer(receipt.getKhachHangId());
        selectedSupplier = findSupplier(receipt.getNhaCungCapId());
        String type = receipt.getLoaiDoiTuong() != null ? receipt.getLoaiDoiTuong() : "Đối tượng khác";
        chooseSubjectType(type);
        employeeField.setText(selectedEmployee != null ? safe(selectedEmployee.getName()) : safe(receipt.getTenNhanVien()));
        customerField.setText(selectedCustomer != null ? safe(selectedCustomer.getName()) : safe(receipt.getTenKhachHang()));
        supplierField.setText(selectedSupplier != null ? safe(selectedSupplier.getName()) : safe(receipt.getTenNhaCungCap()));
        subjectNameField.setText(safe(receipt.getTenDoiTuong()));
        addressField.setText(safe(receipt.getDiaChi()));
        amountField.setText(CurrencyUtil.format(receipt.getSoTien()));
        transferCheck.setSelected(receipt.isChuyenVaoTaiKhoan());
        noteField.setText(safe(receipt.getGhiChu()));
        selectedStore = findStore(receipt.getCuaHangId());
        navigation.show(screens, "main");
    }

    private LyDoThuChi findReason(String id) {
        return reasons.stream().filter(item -> id != null && id.equals(item.getId())).findFirst().orElse(null);
    }

    private NhanVien findEmployee(String id) {
        return employees.stream().filter(item -> id != null && id.equals(item.getId())).findFirst().orElse(null);
    }

    private KhachHang findCustomer(String id) {
        return customers.stream().filter(item -> id != null && id.equals(item.getId())).findFirst().orElse(null);
    }

    private NhaCungCap findSupplier(String id) {
        return suppliers.stream().filter(item -> id != null && id.equals(item.getId())).findFirst().orElse(null);
    }

    private CuaHang findStore(String id) {
        return stores.stream().filter(item -> id != null && id.equals(item.getId())).findFirst()
                .orElse(stores.isEmpty() ? null : stores.get(0));
    }

    private Date parseDate(String value) {
        try {
            return DATE_FORMAT.parse(value != null ? value.trim() : "");
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    private double parseAmount() {
        String digits = amountField.getText().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return 0;
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private JPanel basePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(5, 5, 15, 5));
        return panel;
    }

    private static JTextField field(Color background) {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 27));
        field.setBackground(background);
        field.setPreferredSize(new Dimension(400, 50));
        field.setBorder(new LineBorder(new Color(190, 215, 220), 1));
        return field;
    }

    private static JLabel headerLabel(String text, Color background, int height) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 28));
        label.setPreferredSize(new Dimension(0, height));
        label.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        return label;
    }

    private static JButton iconButton(String text) {
        JButton button = button(text, new Color(0, 82, 91), Color.WHITE, 75, 50);
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 32));
        return button;
    }

    private static JButton button(String text, Color background, Color foreground, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 21));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new LineBorder(new Color(190, 220, 225), 2, true));
        return button;
    }

    private static void addLabel(JPanel panel, GridBagConstraints gbc, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 20));
        label.setPreferredSize(new Dimension(x == 3 ? 185 : 190, 50));
        addComponent(panel, gbc, label, x, y, 1, 0);
    }

    private static void addComponent(JPanel panel, GridBagConstraints gbc, Component component,
                                     int x, int y, int width, double weightX) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.weightx = weightX;
        gbc.weighty = 0;
        panel.add(component, gbc);
        gbc.gridwidth = 1;
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(safe(value), Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized).replaceAll("").toLowerCase(Locale.ROOT).trim();
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }

    private enum SearchKind {
        REASON("CHỌN LÝ DO THU", "LÝ DO THU CHI"),
        EMPLOYEE("TÌM KIẾM NHÂN VIÊN", "NHÂN VIÊN"),
        CUSTOMER("TÌM KIẾM KHÁCH HÀNG", "KHÁCH HÀNG"),
        SUPPLIER("TÌM KIẾM NHÀ CUNG CẤP", "NHÀ CUNG CẤP");

        private final String title;
        private final String label;

        SearchKind(String title, String label) {
            this.title = title;
            this.label = label;
        }
    }

    private record SearchEntry(Object value, String name, String address) {}

    private static final class LargeCheckBoxIcon implements Icon {
        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRect(x + 2, y + 2, 33, 33);
            if (component instanceof AbstractButton button && button.isSelected()) {
                g2.setColor(new Color(30, 210, 80));
                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 8, y + 19, x + 16, y + 28);
                g2.drawLine(x + 16, y + 28, x + 31, y + 9);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 39;
        }

        @Override
        public int getIconHeight() {
            return 39;
        }
    }

    private static final class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable action;

        SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            action.run();
        }
    }
}
