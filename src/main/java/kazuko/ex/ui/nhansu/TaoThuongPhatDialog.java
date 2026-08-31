package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.LyDoThuongPhatDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.ThuongPhatDAO;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.nhansu.LyDoThuongPhat;
import kazuko.ex.model.nhansu.ThuongPhat;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.common.DropdownWithActionBar;
import kazuko.ex.ui.danhmuc.NhanVienDialog;
import kazuko.ex.ui.danhmuc.NhanVienEditDialog;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.List;

public class TaoThuongPhatDialog extends JDialog {

    private final ThuongPhatDAO thuongPhatDAO = new ThuongPhatDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final LyDoThuongPhatDAO lyDoThuongPhatDAO = new LyDoThuongPhatDAO();

    private ThuongPhat currentThuongPhat;
    private NhanVien defaultEmployee;
    private final Runnable onDataSaved;

    // Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private DropdownWithActionBar<NhanVien> cbNhanVien;
    private JTextField txtTienThuong;
    private JTextField txtTienPhat;
    private DropdownWithActionBar<LyDoThuongPhat> cbLyDo;
    private JTextArea txtGhiChu;

    // Bottom action buttons
    private JButton btnLuu;
    private JButton btnLuuVaMoi;
    private JButton btnLuuVaThoat;
    private JButton btnThoat;

    // Nav
    private JButton btnTruoc;
    private JButton btnSau;
    private JButton btnTaoMoi;

    private static final Color BG_YELLOW = new Color(254, 253, 225);
    private static final Color BG_CYAN = new Color(230, 248, 255);
    private static final Color BG_FORM = new Color(215, 230, 248);

    public TaoThuongPhatDialog(Window parent) {
        this(parent, null, null, null);
    }

    public TaoThuongPhatDialog(Window parent, ThuongPhat tp, Runnable onDataSaved) {
        this(parent, tp, null, onDataSaved);
    }

    public TaoThuongPhatDialog(Window parent, ThuongPhat tp, NhanVien defaultEmployee, Runnable onDataSaved) {
        super(parent, tp != null && tp.getId() != null ? "THƯỞNG PHẠT - CẬP NHẬT" : "THƯỞNG PHẠT - THÊM MỚI", ModalityType.APPLICATION_MODAL);
        this.currentThuongPhat = tp;
        this.defaultEmployee = defaultEmployee;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDropdowns();

        if (currentThuongPhat != null) {
            loadToForm(currentThuongPhat);
        } else {
            resetForNew();
        }

        setupHotkeys();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(480, 420);
        setPreferredSize(new Dimension(480, 420));
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_FORM);

        // 1. Top Header Banner & Nav Toolbar
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // Header Title Banner
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlTitle.setBackground(Color.WHITE);
        pnlTitle.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblGiftIcon = new JLabel(IconHelper.getIcon("gift.png", 28, 28));
        if (lblGiftIcon.getIcon() == null) {
            lblGiftIcon.setIcon(IconHelper.getIcon("asterisk-yellow.png", 28, 28));
        }
        JLabel lblTitle = new JLabel("Thưởng phạt");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(20, 35, 65));

        pnlTitle.add(lblGiftIcon);
        pnlTitle.add(lblTitle);
        pnlNorth.add(pnlTitle, BorderLayout.NORTH);

        // Sub Toolbar: Phím tắt, Trước, Sau, Tạo mới
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlNav.setBackground(new Color(225, 238, 252));
        pnlNav.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnPhimTat = createTopNavButton("Phím tắt ▼", null);
        btnTruoc = createTopNavButton("Trước (F10)", IconHelper.getIcon("arrow-180.png", 14, 14));
        btnSau = createTopNavButton("Sau (F11)", IconHelper.getIcon("arrow.png", 14, 14));
        btnTaoMoi = createTopNavButton("Tạo mới", IconHelper.getIcon("plus.png", 14, 14));

        btnPhimTat.addActionListener(e -> showShortcutHelp());
        btnTruoc.addActionListener(e -> onNavigatePrevious());
        btnSau.addActionListener(e -> onNavigateNext());
        btnTaoMoi.addActionListener(e -> resetForNew());

        pnlNav.add(btnPhimTat);
        pnlNav.add(btnTruoc);
        pnlNav.add(btnSau);
        pnlNav.add(btnTaoMoi);
        pnlNorth.add(pnlNav, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center Form
        JPanel pnlCenter = new JPanel(null);
        pnlCenter.setOpaque(false);
        pnlCenter.setPreferredSize(new Dimension(480, 260));

        int y = 12;
        int rowH = 24;
        int gapY = 30;

        // Row 1: Ngày + Số phiếu
        JLabel lblNgay = createLabel("Ngày", 12, y, 65, rowH);
        dpkNgay = new DatePickerField();
        dpkNgay.setDate(new Date());
        dpkNgay.setBackground(BG_YELLOW);
        dpkNgay.setBounds(80, y, 130, rowH);
        dpkNgay.addPropertyChangeListener("date", evt -> {
            if (currentThuongPhat == null || currentThuongPhat.getId() == null) {
                txtSoPhieu.setText(thuongPhatDAO.generateNextSoPhieu(dpkNgay.getDate()));
            }
        });

        JLabel lblSoPhieu = createLabel("Số phiếu", 220, y, 55, rowH);
        txtSoPhieu = new JTextField();
        txtSoPhieu.setBounds(280, y, 175, rowH);
        txtSoPhieu.setBackground(BG_CYAN);
        txtSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlCenter.add(lblNgay);
        pnlCenter.add(dpkNgay);
        pnlCenter.add(lblSoPhieu);
        pnlCenter.add(txtSoPhieu);

        // Row 2: Nhân viên
        y += gapY;
        JLabel lblNV = createLabel("Nhân viên", 12, y, 65, rowH);
        cbNhanVien = new DropdownWithActionBar<>();
        cbNhanVien.setBackground(BG_YELLOW);
        cbNhanVien.setBounds(80, y, 375, rowH);
        cbNhanVien.setOnThem(() -> {
            NhanVienEditDialog dlg = new NhanVienEditDialog(this, null, nhanVienDAO.getAllActive(), this::loadDropdowns);
            dlg.setVisible(true);
        });
        cbNhanVien.setOnTai(this::loadDropdowns);
        cbNhanVien.setOnDanhMuc(() -> {
            NhanVienDialog dlg = new NhanVienDialog(this);
            dlg.setVisible(true);
            loadDropdowns();
        });

        pnlCenter.add(lblNV);
        pnlCenter.add(cbNhanVien);

        // Row 3: Thưởng + Phạt
        y += gapY;
        JLabel lblThuong = createLabel("Thưởng", 12, y, 65, rowH);
        txtTienThuong = new JTextField("0");
        txtTienThuong.setBounds(80, y, 130, rowH);
        txtTienThuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTienThuong.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                formatMoneyField(txtTienThuong);
            }
        });

        JLabel lblPhat = createLabel("Phạt", 220, y, 55, rowH);
        txtTienPhat = new JTextField("0");
        txtTienPhat.setBounds(280, y, 175, rowH);
        txtTienPhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTienPhat.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                formatMoneyField(txtTienPhat);
            }
        });

        pnlCenter.add(lblThuong);
        pnlCenter.add(txtTienThuong);
        pnlCenter.add(lblPhat);
        pnlCenter.add(txtTienPhat);

        // Row 4: Lý do
        y += gapY;
        JLabel lblLyDo = createLabel("Lý do", 12, y, 65, rowH);
        cbLyDo = new DropdownWithActionBar<>();
        cbLyDo.setBackground(BG_YELLOW);
        cbLyDo.setBounds(80, y, 375, rowH);
        cbLyDo.setOnThem(() -> {
            LyDoThuongPhatEditDialog dlg = new LyDoThuongPhatEditDialog(this, null, this::loadDropdowns);
            dlg.setVisible(true);
        });
        cbLyDo.setOnTai(this::loadDropdowns);
        cbLyDo.setOnDanhMuc(() -> {
            LyDoThuongPhatDialog dlg = new LyDoThuongPhatDialog(this);
            dlg.setVisible(true);
            loadDropdowns();
        });

        pnlCenter.add(lblLyDo);
        pnlCenter.add(cbLyDo);

        // Row 5: Ghi chú
        y += gapY;
        JLabel lblGhiChu = createLabel("Ghi chú", 12, y, 65, rowH);
        txtGhiChu = new JTextArea();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBounds(80, y, 375, 115);
        scrollGhiChu.setBorder(new LineBorder(new Color(180, 200, 225), 1));

        pnlCenter.add(lblGhiChu);
        pnlCenter.add(scrollGhiChu);

        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 235)));

        btnLuu = createBottomButton("Lưu", IconHelper.getIcon("save.png", 14, 14));
        btnLuuVaMoi = createBottomButton("Lưu & Mới", null);
        btnLuuVaThoat = createBottomButton("Lưu & thoát", null);
        btnThoat = createBottomButton("Thoát", null);

        btnLuu.addActionListener(e -> onSave(false, false));
        btnLuuVaMoi.addActionListener(e -> onSave(true, false));
        btnLuuVaThoat.addActionListener(e -> onSave(false, true));
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnLuu);
        pnlBottom.add(btnLuuVaMoi);
        pnlBottom.add(btnLuuVaThoat);
        pnlBottom.add(btnThoat);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(new Color(30, 45, 70));
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private JButton createTopNavButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 7, 3, 7)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createBottomButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(185, 205, 230), 1), new EmptyBorder(4, 12, 4, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDropdowns() {
        // Load NhanVien
        List<NhanVien> employees = nhanVienDAO.getAllActive();
        cbNhanVien.setItems(employees);
        if (defaultEmployee != null) {
            for (NhanVien nv : employees) {
                if (defaultEmployee.getId().equals(nv.getId())) {
                    cbNhanVien.setSelectedItem(nv);
                    break;
                }
            }
        } else if (cbNhanVien.getSelectedItem() == null && !employees.isEmpty()) {
            cbNhanVien.setSelectedItem(employees.get(0));
        }

        // Load LyDo
        List<LyDoThuongPhat> reasons = lyDoThuongPhatDAO.getAllActive();
        cbLyDo.setItems(reasons);
        if (cbLyDo.getSelectedItem() == null && !reasons.isEmpty()) {
            cbLyDo.setSelectedItem(reasons.get(0));
        }
    }

    private void formatMoneyField(JTextField tf) {
        String s = tf.getText().replaceAll("[^0-9.]", "");
        if (s.isEmpty()) s = "0";
        try {
            double v = Double.parseDouble(s);
            tf.setText(CurrencyUtil.format(v));
        } catch (Exception ignored) {}
    }

    private double parseMoneyField(JTextField tf) {
        String s = tf.getText().replaceAll("[^0-9.]", "");
        if (s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void resetForNew() {
        currentThuongPhat = new ThuongPhat();
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(thuongPhatDAO.generateNextSoPhieu(dpkNgay.getDate()));

        if (defaultEmployee != null) {
            cbNhanVien.setSelectedItem(defaultEmployee);
        } else if (cbNhanVien.getItems() != null && !cbNhanVien.getItems().isEmpty()) {
            cbNhanVien.setSelectedItem(cbNhanVien.getItems().get(0));
        }

        txtTienThuong.setText("0");
        txtTienPhat.setText("0");

        if (cbLyDo.getItems() != null && !cbLyDo.getItems().isEmpty()) {
            cbLyDo.setSelectedItem(cbLyDo.getItems().get(0));
        }
        txtGhiChu.setText("");
        setTitle("THƯỞNG PHẠT - THÊM MỚI");
    }

    private void loadToForm(ThuongPhat tp) {
        if (tp == null) return;
        this.currentThuongPhat = tp;
        setTitle(tp.getId() != null ? "THƯỞNG PHẠT - CẬP NHẬT" : "THƯỞNG PHẠT - THÊM MỚI");

        dpkNgay.setDate(tp.getNgay() != null ? tp.getNgay() : new Date());
        txtSoPhieu.setText(tp.getSoPhieu() != null ? tp.getSoPhieu() : "");

        if (tp.getNhanVienId() != null) {
            for (NhanVien nv : cbNhanVien.getItems()) {
                if (tp.getNhanVienId().equals(nv.getId())) {
                    cbNhanVien.setSelectedItem(nv);
                    break;
                }
            }
        }

        txtTienThuong.setText(CurrencyUtil.format(tp.getTienThuong()));
        txtTienPhat.setText(CurrencyUtil.format(tp.getTienPhat()));

        if (tp.getLyDoId() != null) {
            for (LyDoThuongPhat ld : cbLyDo.getItems()) {
                if (tp.getLyDoId().equals(ld.getId())) {
                    cbLyDo.setSelectedItem(ld);
                    break;
                }
            }
        } else if (tp.getTenLyDo() != null) {
            for (LyDoThuongPhat ld : cbLyDo.getItems()) {
                if (tp.getTenLyDo().equalsIgnoreCase(ld.getName())) {
                    cbLyDo.setSelectedItem(ld);
                    break;
                }
            }
        }

        txtGhiChu.setText(tp.getGhiChu() != null ? tp.getGhiChu() : "");
    }

    private void onSave(boolean isNewAfter, boolean isCloseAfter) {
        NhanVien selectedNV = cbNhanVien.getSelectedItem();
        if (selectedNV == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            cbNhanVien.requestFocus();
            return;
        }

        if (currentThuongPhat == null) currentThuongPhat = new ThuongPhat();

        currentThuongPhat.setNgay(dpkNgay.getDate());
        currentThuongPhat.setSoPhieu(txtSoPhieu.getText().trim());
        currentThuongPhat.setNhanVienId(selectedNV.getId());
        currentThuongPhat.setTenNhanVien(selectedNV.getName());
        currentThuongPhat.setTienThuong(parseMoneyField(txtTienThuong));
        currentThuongPhat.setTienPhat(parseMoneyField(txtTienPhat));

        LyDoThuongPhat selectedReason = cbLyDo.getSelectedItem();
        if (selectedReason != null) {
            currentThuongPhat.setLyDoId(selectedReason.getId());
            currentThuongPhat.setTenLyDo(selectedReason.getName());
        } else {
            currentThuongPhat.setLyDoId(null);
            currentThuongPhat.setTenLyDo("");
        }

        currentThuongPhat.setGhiChu(txtGhiChu.getText().trim());

        if (currentThuongPhat.getId() == null || currentThuongPhat.getId().trim().isEmpty()) {
            thuongPhatDAO.insert(currentThuongPhat);
        } else {
            thuongPhatDAO.update(currentThuongPhat);
        }

        if (onDataSaved != null) onDataSaved.run();

        if (isCloseAfter) {
            dispose();
        } else if (isNewAfter) {
            resetForNew();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thưởng phạt thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onNavigatePrevious() {
        List<ThuongPhat> list = thuongPhatDAO.getAll();
        if (list.isEmpty() || currentThuongPhat == null || currentThuongPhat.getId() == null) return;
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (currentThuongPhat.getId().equals(list.get(i).getId())) {
                idx = i;
                break;
            }
        }
        if (idx > 0) {
            loadToForm(list.get(idx - 1));
        } else {
            JOptionPane.showMessageDialog(this, "Đây là phiếu thưởng phạt đầu tiên!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onNavigateNext() {
        List<ThuongPhat> list = thuongPhatDAO.getAll();
        if (list.isEmpty() || currentThuongPhat == null || currentThuongPhat.getId() == null) return;
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (currentThuongPhat.getId().equals(list.get(i).getId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0 && idx < list.size() - 1) {
            loadToForm(list.get(idx + 1));
        } else {
            JOptionPane.showMessageDialog(this, "Đây là phiếu thưởng phạt cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showShortcutHelp() {
        JOptionPane.showMessageDialog(this,
                "<html><b>CÁC PHÍM TẮT THAO TÁC:</b><br>" +
                        "- <b>Alt + L</b>: Lưu dữ liệu<br>" +
                        "- <b>Alt + M</b>: Lưu và thêm mới<br>" +
                        "- <b>Alt + T</b>: Lưu và thoát<br>" +
                        "- <b>F10</b>: Chuyển đến phiếu trước<br>" +
                        "- <b>F11</b>: Chuyển đến phiếu sau<br>" +
                        "- <b>Esc</b>: Thoát cửa sổ</html>",
                "Phím tắt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupHotkeys() {
        // Alt + L: Lưu
        getRootPane().registerKeyboardAction(e -> onSave(false, false),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Alt + M: Lưu & Mới
        getRootPane().registerKeyboardAction(e -> onSave(true, false),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Alt + T: Lưu & Thoát
        getRootPane().registerKeyboardAction(e -> onSave(false, true),
                KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F10: Trước
        getRootPane().registerKeyboardAction(e -> onNavigatePrevious(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F11: Sau
        getRootPane().registerKeyboardAction(e -> onNavigateNext(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Esc: Thoát
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
