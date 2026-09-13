package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.system.SUser;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class KhachHangEditDialog extends JDialog {
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final UserDAO userDAO = new UserDAO();

    private KhachHang currentKhachHang;
    private final Runnable onSaveCallback;
    private List<KhachHang> allCustomers;
    private int currentIndex = -1;

    private JTextField txtMaKhach;
    private JTextField txtName;
    private JTextField txtDiaChi;
    private JTextField txtDienThoai;
    private JTextField txtEmail;
    private DatePickerField txtNgaySinhNhat;
    private JTextField txtDiemBanDau;
    private JComboBox<PriceOption> cbGiaBan;
    private JTextField txtMaSoThue;
    private JComboBox<SUser> cbNhanVien;
    private JComboBox<NhomKhachHang> cbNhomKhachHang;
    private JComboBox<String> cbTinhThanh;
    private JTextField txtFacebook;
    private JTextField txtGhiChu;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230);
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_TOOLBAR_BG = new Color(210, 225, 244);
    private static final Color COLOR_TOOLBAR_BORDER = new Color(175, 198, 226);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);
    private static final Color COLOR_REQUIRED_FIELD = new Color(255, 255, 204);

    public KhachHangEditDialog(Component parent, KhachHang khachHang, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "", ModalityType.APPLICATION_MODAL);
        this.currentKhachHang = khachHang != null ? khachHang : new KhachHang();
        this.onSaveCallback = onSaveCallback;

        this.allCustomers = khachHangDAO.getAll();
        findCurrentIndex();

        initUI();
        loadFormData();
        setupShortcuts();
        pack();
        setLocationRelativeTo(parent);
    }

    private void findCurrentIndex() {
        if (currentKhachHang != null && currentKhachHang.getId() != null) {
            for (int i = 0; i < allCustomers.size(); i++) {
                if (currentKhachHang.getId().equals(allCustomers.get(i).getId())) {
                    currentIndex = i;
                    break;
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setPreferredSize(new Dimension(530, 560));

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(530, 52));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setOpaque(false);

        JLabel lblCustomerIcon = new JLabel(IconHelper.getIcon("users.png", 34, 34));
        if (lblCustomerIcon.getIcon() == null) {
            lblCustomerIcon = new JLabel(IconHelper.getIcon("Folder.png", 34, 34));
        }

        JLabel lblHeaderTitle = new JLabel("Khách hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblCustomerIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle Container: Sub Toolbar + Form Body
        JPanel centerContainer = new JPanel(new BorderLayout());

        // 2.1 Sub Toolbar
        JPanel ribbonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        ribbonBar.setBackground(COLOR_TOOLBAR_BG);
        ribbonBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_TOOLBAR_BORDER));

        JButton btnShortcuts = createRibbonButton("Phím tắt ▾", "btnConfig.Icon.png");
        JButton btnPrev = createRibbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createRibbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnCreateNew = createRibbonButton("Tạo mới", "btnNew.Image.png");
        JButton btnCopy = createRibbonButton("Sao chép", "Copy.png");
        JButton btnDelete = createRibbonButton("Xóa", "cross.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigatePrevious());
        btnNext.addActionListener(e -> navigateNext());
        btnCreateNew.addActionListener(e -> onNewCustomer());
        btnCopy.addActionListener(e -> onCopyCustomer());
        btnDelete.addActionListener(e -> onDeleteCustomer());

        ribbonBar.add(btnShortcuts);
        ribbonBar.add(btnPrev);
        ribbonBar.add(btnNext);
        ribbonBar.add(btnCreateNew);
        ribbonBar.add(btnCopy);
        ribbonBar.add(btnDelete);
        centerContainer.add(ribbonBar, BorderLayout.NORTH);

        // 2.2 Form Panel
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        int y = 10;
        int labelWidth = 140;
        int fieldX = 155;
        int fieldWidth = 335;
        int rowHeight = 24;
        int gap = 6;

        // Row 1: Mã khách
        JLabel lblMaKhach = new JLabel("Mã khách");
        lblMaKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMaKhach.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblMaKhach);

        txtMaKhach = new JTextField();
        txtMaKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMaKhach.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtMaKhach);

        // Row 2: Tên khách hàng (Bắt buộc - màu nền vàng)
        y += rowHeight + gap;
        JLabel lblName = new JLabel("Tên khách hàng");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBackground(COLOR_REQUIRED_FIELD);
        txtName.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtName);

        // Row 3: Địa chỉ
        y += rowHeight + gap;
        JLabel lblDiaChi = new JLabel("Địa chỉ");
        lblDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiaChi.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblDiaChi);

        txtDiaChi = new JTextField();
        txtDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiaChi.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtDiaChi);

        // Row 4: Điện thoại
        y += rowHeight + gap;
        JLabel lblDienThoai = new JLabel("Điện thoại");
        lblDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDienThoai.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblDienThoai);

        txtDienThoai = new JTextField();
        txtDienThoai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienThoai.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtDienThoai);

        // Row 5: Email
        y += rowHeight + gap;
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEmail.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtEmail.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtEmail);

        // Row 6: Ngày thành lập/sinh nhật | Điểm tích lũy ban đầu
        y += rowHeight + gap;
        JLabel lblNgaySinh = new JLabel("Ngày thành lập/sinh nhật");
        lblNgaySinh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgaySinh.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblNgaySinh);

        txtNgaySinhNhat = new DatePickerField();
        txtNgaySinhNhat.setBounds(fieldX, y, 105, rowHeight);
        formPanel.add(txtNgaySinhNhat);

        JLabel lblDiemBanDau = new JLabel("Điểm tích lũy ban đầu");
        lblDiemBanDau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiemBanDau.setBounds(265, y, 130, rowHeight);
        formPanel.add(lblDiemBanDau);

        txtDiemBanDau = new JTextField("0");
        txtDiemBanDau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiemBanDau.setBounds(fieldX + 240, y, 95, rowHeight);
        formPanel.add(txtDiemBanDau);

        // Row 7: Giá bán
        y += rowHeight + gap;
        JLabel lblGiaBan = new JLabel("Giá bán");
        lblGiaBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGiaBan.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblGiaBan);

        SystemConfig priceConfig = new SystemConfigDAO().loadConfig();
        cbGiaBan = new JComboBox<>();
        cbGiaBan.addItem(new PriceOption(KhachHang.GIA_THEO_DANH_MUC, "Giá theo danh mục"));
        if (priceConfig.isSuDungGia2()) {
            cbGiaBan.addItem(new PriceOption(KhachHang.GIA_BAN_2,
                    configuredPriceName(priceConfig.getDienGiaiGia2(), "Đơn giá 2")));
        }
        if (priceConfig.isSuDungGia3()) {
            cbGiaBan.addItem(new PriceOption(KhachHang.GIA_BAN_3,
                    configuredPriceName(priceConfig.getDienGiaiGia3(), "Giá bán 3")));
        }
        if (priceConfig.isSuDungGia4()) {
            cbGiaBan.addItem(new PriceOption(KhachHang.GIA_BAN_4,
                    configuredPriceName(priceConfig.getDienGiaiGia4(), "Giá bán 4")));
        }
        cbGiaBan.addItem(new PriceOption(KhachHang.GIA_VON, "Giá vốn"));
        cbGiaBan.addItem(new PriceOption(KhachHang.GIA_GAN_NHAT, "Giá gần nhất"));
        cbGiaBan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbGiaBan.setBounds(fieldX, y, fieldWidth, rowHeight);
        cbGiaBan.setEnabled(priceConfig.isSuDungGiaTheoKhach());
        formPanel.add(cbGiaBan);

        // Row 8: Mã số thuế
        y += rowHeight + gap;
        JLabel lblMaSoThue = new JLabel("Mã số thuế");
        lblMaSoThue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMaSoThue.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblMaSoThue);

        txtMaSoThue = new JTextField();
        txtMaSoThue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtMaSoThue.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtMaSoThue);

        // Row 9: Nhân viên
        y += rowHeight + gap;
        JLabel lblNhanVien = new JLabel("Nhân viên");
        lblNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhanVien.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblNhanVien);

        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.setBounds(fieldX, y, fieldWidth, rowHeight);
        populateNhanVienComboBox();
        formPanel.add(cbNhanVien);

        // Row 10: Nhóm khách hàng
        y += rowHeight + gap;
        JLabel lblNhomKH = new JLabel("Nhóm khách hàng");
        lblNhomKH.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhomKH.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblNhomKH);

        cbNhomKhachHang = new JComboBox<>();
        cbNhomKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhomKhachHang.setBounds(fieldX, y, fieldWidth, rowHeight);
        populateNhomKhachHangComboBox();
        formPanel.add(cbNhomKhachHang);

        // Row 11: Tỉnh thành
        y += rowHeight + gap;
        JLabel lblTinhThanh = new JLabel("Tỉnh thành");
        lblTinhThanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTinhThanh.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblTinhThanh);

        cbTinhThanh = new JComboBox<>();
        cbTinhThanh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbTinhThanh.setBounds(fieldX, y, fieldWidth, rowHeight);
        populateTinhThanhComboBox();
        formPanel.add(cbTinhThanh);

        // Row 12: Facebook
        y += rowHeight + gap;
        JLabel lblFacebook = new JLabel("Facebook");
        lblFacebook.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFacebook.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblFacebook);

        txtFacebook = new JTextField();
        txtFacebook.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFacebook.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtFacebook);

        // Row 13: Ghi chú
        y += rowHeight + gap;
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(15, y, labelWidth, rowHeight);
        formPanel.add(lblGhiChu);

        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setBounds(fieldX, y, fieldWidth, rowHeight);
        formPanel.add(txtGhiChu);

        centerContainer.add(formPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footerPanel.setBackground(new Color(235, 242, 252));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JButton btnSave = createActionButton("Lưu", "Save1.png");
        JButton btnSaveNew = createActionButton("Lưu & Mới", null);
        JButton btnSaveExit = createActionButton("Lưu & thoát", null);
        JButton btnExit = createActionButton("Thoát", null);

        btnSave.addActionListener(e -> onSave(false, false));
        btnSaveNew.addActionListener(e -> onSave(true, false));
        btnSaveExit.addActionListener(e -> onSave(false, true));
        btnExit.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnSaveNew);
        footerPanel.add(btnSaveExit);
        footerPanel.add(btnExit);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createRibbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 6, 2, 6));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null && !iconName.isEmpty()) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(text.length() > 6 ? 100 : 75, 26));
        btn.setBackground(Color.WHITE);
        return btn;
    }

    private void populateNhanVienComboBox() {
        cbNhanVien.removeAllItems();
        SUser dummy = new SUser();
        dummy.setId("");
        dummy.setName("");
        cbNhanVien.addItem(dummy);

        List<NhanVien> activeEmployees = nhanVienDAO.getAllActive();
        if (activeEmployees != null && !activeEmployees.isEmpty()) {
            for (NhanVien nv : activeEmployees) {
                if (!nv.isFolder() && !nv.isSeparator() && nv.getName() != null && !nv.getName().trim().isEmpty()) {
                    SUser u = new SUser();
                    u.setId(nv.getId());
                    u.setName(nv.getName().trim());
                    cbNhanVien.addItem(u);
                }
            }
        } else {
            Map<String, String> nvMap = khachHangDAO.getAllNhanVienMap();
            if (nvMap != null && !nvMap.isEmpty()) {
                for (Map.Entry<String, String> entry : nvMap.entrySet()) {
                    SUser u = new SUser();
                    u.setId(entry.getKey());
                    u.setName(entry.getValue());
                    cbNhanVien.addItem(u);
                }
            }
        }

        cbNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SUser) {
                    SUser u = (SUser) value;
                    lbl.setText(u.getName() != null ? u.getName() : "");
                    if (u.getId() != null && !u.getId().isEmpty()) {
                        lbl.setIcon(IconHelper.getIcon("users.png", 14, 14));
                    } else {
                        lbl.setIcon(null);
                    }
                }
                return lbl;
            }
        });
    }

    private void populateTinhThanhComboBox() {
        cbTinhThanh.removeAllItems();
        cbTinhThanh.addItem("");
        Map<String, String> ttMap = khachHangDAO.getAllTinhThanhMap();
        if (ttMap != null && !ttMap.isEmpty()) {
            for (String ttName : ttMap.values()) {
                cbTinhThanh.addItem(ttName);
            }
        } else {
            String[] defaultList = {"DONG NAI", "TP. HÒ CHÍ MINH", "HÀ NỘI", "LÂM ĐỒNG", "BÌNH DƯƠNG", "BÀ RỊA - VŨNG TÀU", "ĐÀ NẴNG", "HẢI PHÒNG", "CẦN THƠ", "KHÁNH HÒA", "ĐẮK LẮK"};
            for (String tt : defaultList) {
                cbTinhThanh.addItem(tt);
            }
        }
    }

    private void populateNhomKhachHangComboBox() {
        cbNhomKhachHang.removeAllItems();
        NhomKhachHang dummy = new NhomKhachHang("", "Chưa thiết lập");
        cbNhomKhachHang.addItem(dummy);

        List<NhomKhachHang> groups = nhomKhachHangDAO.getAll();
        for (NhomKhachHang g : groups) {
            if (!g.isSeparator()) {
                cbNhomKhachHang.addItem(g);
            }
        }

        cbNhomKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhomKhachHang) {
                    NhomKhachHang n = (NhomKhachHang) value;
                    lbl.setText(n.getName());
                    lbl.setIcon(IconHelper.getIcon(n.getIconName() != null ? n.getIconName() : "Folder.png", 14, 14));
                }
                return lbl;
            }
        });
    }

    private void loadFormData() {
        if (currentKhachHang.getId() != null) {
            setTitle("KHÁCH HÀNG - SỬA");
            txtMaKhach.setText(currentKhachHang.getMaKhach() != null ? currentKhachHang.getMaKhach() : "");
            txtName.setText(currentKhachHang.getName() != null ? currentKhachHang.getName() : "");
            txtDiaChi.setText(currentKhachHang.getDiaChi() != null ? currentKhachHang.getDiaChi() : "");
            txtDienThoai.setText(currentKhachHang.getDienThoai() != null ? currentKhachHang.getDienThoai() : "");
            txtEmail.setText(currentKhachHang.getEmail() != null ? currentKhachHang.getEmail() : "");

            if (currentKhachHang.getNgaySinhNhat() != null) {
                txtNgaySinhNhat.setText(sdf.format(new Date(currentKhachHang.getNgaySinhNhat().getTime())));
            } else {
                txtNgaySinhNhat.setText("");
            }

            txtDiemBanDau.setText(String.valueOf((int) currentKhachHang.getDiemTichLuyBanDau()));

            selectPriceOption(currentKhachHang.getGiaBanIndex());

            txtMaSoThue.setText(currentKhachHang.getMaSoThue() != null ? currentKhachHang.getMaSoThue() : "");

            // Chọn nhân viên
            if (currentKhachHang.getTenNhanVien() != null && !currentKhachHang.getTenNhanVien().isEmpty()) {
                for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                    SUser u = cbNhanVien.getItemAt(i);
                    if (currentKhachHang.getTenNhanVien().equalsIgnoreCase(u.getName())) {
                        cbNhanVien.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Chọn nhóm khách hàng
            if (currentKhachHang.getTenNhomKhachHang() != null && !currentKhachHang.getTenNhomKhachHang().isEmpty()) {
                for (int i = 0; i < cbNhomKhachHang.getItemCount(); i++) {
                    NhomKhachHang n = cbNhomKhachHang.getItemAt(i);
                    if (currentKhachHang.getTenNhomKhachHang().equalsIgnoreCase(n.getName()) ||
                        (currentKhachHang.getNhomKhachHangId() != null && currentKhachHang.getNhomKhachHangId().equals(n.getId()))) {
                        cbNhomKhachHang.setSelectedIndex(i);
                        break;
                    }
                }
            }

            if (currentKhachHang.getTinhThanh() != null) {
                cbTinhThanh.setSelectedItem(currentKhachHang.getTinhThanh());
            }

            txtFacebook.setText(currentKhachHang.getFacebook() != null ? currentKhachHang.getFacebook() : "");
            txtGhiChu.setText(currentKhachHang.getGhiChu() != null ? currentKhachHang.getGhiChu() : "");
        } else {
            setTitle("KHÁCH HÀNG - THÊM MỚI");
            txtMaKhach.setText(khachHangDAO.getNextMaKhach());
            txtName.setText("");
            txtDiaChi.setText("");
            txtDienThoai.setText("");
            txtEmail.setText("");
            txtNgaySinhNhat.setText("");
            txtDiemBanDau.setText("0");
            cbGiaBan.setSelectedIndex(0);
            txtMaSoThue.setText("");
            cbNhanVien.setSelectedIndex(0);
            cbNhomKhachHang.setSelectedIndex(0);
            cbTinhThanh.setSelectedIndex(0);
            txtFacebook.setText("");
            txtGhiChu.setText("");
        }
    }

    private void onSave(boolean isSaveAndNew, boolean isSaveAndExit) {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khách hàng không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        String maKhach = txtMaKhach.getText().trim();
        if (maKhach.isEmpty()) {
            maKhach = khachHangDAO.getNextMaKhach();
        }

        if (khachHangDAO.existsByMaKhach(maKhach, currentKhachHang.getId())) {
            JOptionPane.showMessageDialog(this, "Mã khách '" + maKhach + "' đã tồn tại! Vui lòng chọn mã khác.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtMaKhach.requestFocus();
            return;
        }

        SystemConfig systemConfig = new SystemConfigDAO().loadConfig();
        if (!systemConfig.isChoPhepTrungTenKhachHang()
                && khachHangDAO.existsByName(name, currentKhachHang.getId())) {
            JOptionPane.showMessageDialog(this,
                    "Tên khách hàng '" + name + "' đã tồn tại!\nHệ thống đang cấu hình không cho phép trùng tên khách hàng.",
                    "Trùng tên khách hàng", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocusInWindow();
            txtName.selectAll();
            return;
        }

        currentKhachHang.setMaKhach(maKhach);
        currentKhachHang.setName(name);
        currentKhachHang.setDiaChi(txtDiaChi.getText().trim());
        currentKhachHang.setDienThoai(txtDienThoai.getText().trim());
        currentKhachHang.setEmail(txtEmail.getText().trim());

        String dobStr = txtNgaySinhNhat.getText().trim();
        if (!dobStr.isEmpty()) {
            Date d = txtNgaySinhNhat.getDate();
            if (d != null) {
                currentKhachHang.setNgaySinhNhat(new Timestamp(d.getTime()));
            } else {
                JOptionPane.showMessageDialog(this, "Định dạng ngày sinh không hợp lệ (đúng chuẩn dd/MM/yyyy)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                txtNgaySinhNhat.getTextField().requestFocus();
                return;
            }
        } else {
            currentKhachHang.setNgaySinhNhat(null);
        }

        try {
            double diemBanDau = Double.parseDouble(txtDiemBanDau.getText().trim());
            currentKhachHang.setDiemTichLuyBanDau(diemBanDau);
            if (currentKhachHang.getId() == null) {
                currentKhachHang.setDiemTichLuy(diemBanDau);
            }
        } catch (Exception e) {
            currentKhachHang.setDiemTichLuyBanDau(0);
        }

        if (systemConfig.isSuDungGiaTheoKhach() || currentKhachHang.getId() == null) {
            PriceOption selectedPrice = (PriceOption) cbGiaBan.getSelectedItem();
            currentKhachHang.setGiaBanIndex(selectedPrice != null
                    ? selectedPrice.code : KhachHang.GIA_THEO_DANH_MUC);
        }
        currentKhachHang.setMaSoThue(txtMaSoThue.getText().trim());

        SUser selectedUser = (SUser) cbNhanVien.getSelectedItem();
        if (selectedUser != null && selectedUser.getId() != null && !selectedUser.getId().isEmpty()) {
            currentKhachHang.setNhanVienId(selectedUser.getId());
            currentKhachHang.setTenNhanVien(selectedUser.getName());
        } else {
            currentKhachHang.setNhanVienId(null);
            currentKhachHang.setTenNhanVien(null);
        }

        NhomKhachHang selectedNhom = (NhomKhachHang) cbNhomKhachHang.getSelectedItem();
        if (selectedNhom != null && selectedNhom.getId() != null && !selectedNhom.getId().isEmpty()) {
            currentKhachHang.setNhomKhachHangId(selectedNhom.getId());
            currentKhachHang.setTenNhomKhachHang(selectedNhom.getName());
        } else {
            currentKhachHang.setNhomKhachHangId(null);
            currentKhachHang.setTenNhomKhachHang("Chưa thiết lập");
        }

        currentKhachHang.setTinhThanh((String) cbTinhThanh.getSelectedItem());
        currentKhachHang.setFacebook(txtFacebook.getText().trim());
        currentKhachHang.setGhiChu(txtGhiChu.getText().trim());

        boolean success;
        if (currentKhachHang.getId() == null) {
            success = khachHangDAO.insert(currentKhachHang);
        } else {
            success = khachHangDAO.update(currentKhachHang);
        }

        if (!success) {
            JOptionPane.showMessageDialog(this, "Không thể lưu dữ liệu vào cơ sở dữ liệu! Vui lòng kiểm tra lại kết nối.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            return;
        }

        allCustomers = khachHangDAO.getAll();
        findCurrentIndex();

        if (onSaveCallback != null) onSaveCallback.run();

        if (isSaveAndExit) {
            dispose();
        } else if (isSaveAndNew) {
            onNewCustomer();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu thông tin khách hàng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onNewCustomer() {
        this.currentKhachHang = new KhachHang();
        this.currentIndex = -1;
        loadFormData();
        txtName.requestFocus();
    }

    private void onCopyCustomer() {
        KhachHang copy = new KhachHang();
        copy.setMaKhach(khachHangDAO.getNextMaKhach());
        copy.setName(txtName.getText().trim() + " (Bản sao)");
        copy.setDiaChi(txtDiaChi.getText().trim());
        copy.setDienThoai(txtDienThoai.getText().trim());
        copy.setEmail(txtEmail.getText().trim());
        PriceOption selectedPrice = (PriceOption) cbGiaBan.getSelectedItem();
        copy.setGiaBanIndex(selectedPrice != null
                ? selectedPrice.code : KhachHang.GIA_THEO_DANH_MUC);
        copy.setMaSoThue(txtMaSoThue.getText().trim());
        copy.setTinhThanh((String) cbTinhThanh.getSelectedItem());
        copy.setFacebook(txtFacebook.getText().trim());
        copy.setGhiChu(txtGhiChu.getText().trim());

        this.currentKhachHang = copy;
        this.currentIndex = -1;
        loadFormData();
    }

    private void onDeleteCustomer() {
        if (currentKhachHang.getId() == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn chuyển khách hàng '" + currentKhachHang.getName() + "' vào thùng rác?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            khachHangDAO.moveToTrash(currentKhachHang.getId());
            if (onSaveCallback != null) onSaveCallback.run();
            dispose();
        }
    }

    private void navigatePrevious() {
        if (allCustomers.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            currentKhachHang = allCustomers.get(currentIndex);
            loadFormData();
        }
    }

    private void navigateNext() {
        if (allCustomers.isEmpty()) return;
        if (currentIndex < allCustomers.size() - 1) {
            currentIndex++;
            currentKhachHang = allCustomers.get(currentIndex);
            loadFormData();
        }
    }

    private void selectPriceOption(int code) {
        for (int i = 0; i < cbGiaBan.getItemCount(); i++) {
            PriceOption option = cbGiaBan.getItemAt(i);
            if (option.code == code) {
                cbGiaBan.setSelectedIndex(i);
                return;
            }
        }
        cbGiaBan.setSelectedIndex(0);
    }

    private static String configuredPriceName(String configuredName, String fallback) {
        return configuredName != null && !configuredName.trim().isEmpty()
                ? configuredName.trim() : fallback;
    }

    private static final class PriceOption {
        private final int code;
        private final String label;

        private PriceOption(int code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private void setupShortcuts() {
        getRootPane().registerKeyboardAction(e -> onSave(false, true), KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigatePrevious(), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> navigateNext(), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Ctrl + S: Lưu & thoát"));
        menu.add(new JMenuItem("F10: Xem khách hàng trước"));
        menu.add(new JMenuItem("F11: Xem khách hàng tiếp theo"));
        menu.add(new JMenuItem("Esc: Thoát"));
        menu.show(invoker, 0, invoker.getHeight());
    }
}
