package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.TangGiamDiemDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.TangGiamDiem;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TangGiamDiemEditDialog extends JDialog {
    private final TangGiamDiemDAO tangGiamDiemDAO = new TangGiamDiemDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private TangGiamDiem currentItem;
    private final String initialKhachHangId;
    private final Runnable onSaveCallback;
    private List<TangGiamDiem> allItems;
    private int currentIndex = -1;

    // Components
    private DatePickerField dpNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhachHang> cbKhachHang;
    private JTextField txtDiemTang;
    private JTextField txtDiemGiam;
    private JTextField txtLyDo;
    private JTextArea txtGhiChu;

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230);
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_TOOLBAR_BG = new Color(210, 225, 244);
    private static final Color COLOR_TOOLBAR_BORDER = new Color(175, 198, 226);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);
    private static final Color COLOR_REQUIRED_FIELD = new Color(255, 255, 204);

    public TangGiamDiemEditDialog(Component parent, TangGiamDiem item, String defaultKhachHangId, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "", ModalityType.APPLICATION_MODAL);
        this.currentItem = item != null ? item : new TangGiamDiem();
        this.initialKhachHangId = defaultKhachHangId;
        this.onSaveCallback = onSaveCallback;

        this.allItems = tangGiamDiemDAO.getAll();
        findCurrentIndex();

        initUI();
        loadFormData();
        setupShortcuts();
        pack();
        setLocationRelativeTo(parent);
    }

    private void findCurrentIndex() {
        if (currentItem != null && currentItem.getId() != null) {
            for (int i = 0; i < allItems.size(); i++) {
                if (currentItem.getId().equals(allItems.get(i).getId())) {
                    currentIndex = i;
                    break;
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(530, 450));
        setResizable(false);

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 48));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        leftHeader.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("1282529873_objects.png", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Tăng giảm điểm");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle: Sub Ribbon Toolbar + Form Body
        JPanel centerContainer = new JPanel(new BorderLayout());

        // 2.1 Ribbon Bar
        JPanel ribbonBar = new JPanel(new BorderLayout());
        ribbonBar.setBackground(COLOR_TOOLBAR_BG);
        ribbonBar.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_TOOLBAR_BORDER));

        JPanel leftRibbon = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        leftRibbon.setOpaque(false);

        JButton btnShortcuts = createRibbonButton("Phím tắt ▾", "btnConfig.Icon.png");
        JButton btnPrev = createRibbonButton("Trước (F10)", "arrow-180-medium.png");
        JButton btnNext = createRibbonButton("Sau (F11)", "arrow-000-medium.png");
        JButton btnCreateNew = createRibbonButton("Tạo mới", "btnNew.Image.png");

        btnShortcuts.addActionListener(e -> showShortcutMenu(btnShortcuts));
        btnPrev.addActionListener(e -> navigatePrevious());
        btnNext.addActionListener(e -> navigateNext());
        btnCreateNew.addActionListener(e -> onNewItem());

        leftRibbon.add(btnShortcuts);
        leftRibbon.add(btnPrev);
        leftRibbon.add(btnNext);
        leftRibbon.add(btnCreateNew);

        ribbonBar.add(leftRibbon, BorderLayout.WEST);
        centerContainer.add(ribbonBar, BorderLayout.NORTH);

        // 2.2 Form Body
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        int rowH = 25;
        int gap = 8;
        int y = 14;

        // Row 1: Ngày | Số phiếu
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgay.setBounds(16, y, 75, rowH);
        formPanel.add(lblNgay);

        dpNgay = new DatePickerField();
        dpNgay.setBounds(95, y, 115, rowH);
        formPanel.add(dpNgay);

        JLabel lblSoPhieu = new JLabel("Số phiếu");
        lblSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoPhieu.setBounds(225, y, 60, rowH);
        formPanel.add(lblSoPhieu);

        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoPhieu.setBounds(290, y, 195, rowH);
        txtSoPhieu.setBackground(new Color(225, 238, 255));
        formPanel.add(txtSoPhieu);

        // Row 2: Khách hàng
        y += rowH + gap;
        JLabel lblKhach = new JLabel("Khách hàng");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhach.setBounds(16, y, 75, rowH);
        formPanel.add(lblKhach);

        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.setBackground(COLOR_REQUIRED_FIELD);
        cbKhachHang.setBounds(95, y, 390, rowH);
        populateKhachHangComboBox();
        formPanel.add(cbKhachHang);

        // Row 3: Điểm tăng | Điểm giảm
        y += rowH + gap;
        JLabel lblDiemTang = new JLabel("Điểm tăng");
        lblDiemTang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiemTang.setBounds(16, y, 75, rowH);
        formPanel.add(lblDiemTang);

        txtDiemTang = new JTextField("0");
        txtDiemTang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiemTang.setHorizontalAlignment(JTextField.RIGHT);
        txtDiemTang.setBounds(95, y, 115, rowH);
        formPanel.add(txtDiemTang);

        JLabel lblDiemGiam = new JLabel("Điểm giảm");
        lblDiemGiam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiemGiam.setBounds(225, y, 65, rowH);
        formPanel.add(lblDiemGiam);

        txtDiemGiam = new JTextField("0");
        txtDiemGiam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiemGiam.setHorizontalAlignment(JTextField.RIGHT);
        txtDiemGiam.setBounds(290, y, 195, rowH);
        formPanel.add(txtDiemGiam);

        // Row 4: Lý do
        y += rowH + gap;
        JLabel lblLyDo = new JLabel("Lý do");
        lblLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLyDo.setBounds(16, y, 75, rowH);
        formPanel.add(lblLyDo);

        txtLyDo = new JTextField();
        txtLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLyDo.setBackground(COLOR_REQUIRED_FIELD);
        txtLyDo.setBounds(95, y, 390, rowH);
        formPanel.add(txtLyDo);

        // Row 5: Ghi chú
        y += rowH + gap;
        JLabel lblGhiChu = new JLabel("Ghi chú");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(16, y, 75, rowH);
        formPanel.add(lblGhiChu);

        txtGhiChu = new JTextArea();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBounds(95, y, 390, 85);
        scrollGhiChu.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        formPanel.add(scrollGhiChu);

        centerContainer.add(formPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // 3. Footer Action Buttons
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

    private void populateKhachHangComboBox() {
        cbKhachHang.removeAllItems();
        List<KhachHang> list = khachHangDAO.getAll();
        for (KhachHang k : list) {
            cbKhachHang.addItem(k);
        }
        cbKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    KhachHang kh = (KhachHang) value;
                    lbl.setText(kh.getName() != null ? kh.getName() : "");
                }
                return lbl;
            }
        });
    }

    private void loadFormData() {
        if (currentItem != null && currentItem.getId() != null) {
            setTitle("TĂNG GIẢM ĐIỂM - SỬA");
            dpNgay.setDate(currentItem.getNgay() != null ? currentItem.getNgay() : new Date());
            txtSoPhieu.setText(currentItem.getSoPhieu() != null ? currentItem.getSoPhieu() : "");
            txtDiemTang.setText(formatPoint(currentItem.getDiemTang()));
            txtDiemGiam.setText(formatPoint(currentItem.getDiemGiam()));
            txtLyDo.setText(currentItem.getLyDo() != null ? currentItem.getLyDo() : "");
            txtGhiChu.setText(currentItem.getNote() != null ? currentItem.getNote() : "");

            if (currentItem.getKhachHangId() != null) {
                for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                    KhachHang kh = cbKhachHang.getItemAt(i);
                    if (currentItem.getKhachHangId().equals(kh.getId())) {
                        cbKhachHang.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } else {
            setTitle("TĂNG GIẢM ĐIỂM - THÊM MỚI");
            dpNgay.setDate(new Date());
            txtSoPhieu.setText(tangGiamDiemDAO.generateNextSoPhieu());
            txtDiemTang.setText("0");
            txtDiemGiam.setText("0");
            txtLyDo.setText("");
            txtGhiChu.setText("");

            if (initialKhachHangId != null) {
                for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                    KhachHang kh = cbKhachHang.getItemAt(i);
                    if (initialKhachHangId.equals(kh.getId())) {
                        cbKhachHang.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }

    private String formatPoint(double pt) {
        if (pt == (long) pt) {
            return String.valueOf((long) pt);
        }
        return String.valueOf(pt);
    }

    private void onSave(boolean isSaveAndNew, boolean isSaveAndExit) {
        KhachHang selectedKh = (KhachHang) cbKhachHang.getSelectedItem();
        if (selectedKh == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cbKhachHang.requestFocus();
            return;
        }

        String lyDo = txtLyDo.getText().trim();
        if (lyDo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lý do không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtLyDo.requestFocus();
            return;
        }

        double diemTang = 0.0;
        double diemGiam = 0.0;
        try {
            String dtStr = txtDiemTang.getText().trim().replace(",", ".");
            if (!dtStr.isEmpty()) diemTang = Double.parseDouble(dtStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm tăng không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiemTang.requestFocus();
            return;
        }

        try {
            String dgStr = txtDiemGiam.getText().trim().replace(",", ".");
            if (!dgStr.isEmpty()) diemGiam = Double.parseDouble(dgStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Điểm giảm không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            txtDiemGiam.requestFocus();
            return;
        }

        if (diemTang <= 0 && diemGiam <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập điểm tăng hoặc điểm giảm lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtDiemTang.requestFocus();
            return;
        }

        Date dNgay = dpNgay.getDate();
        if (dNgay == null) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            dpNgay.requestFocus();
            return;
        }

        currentItem.setNgay(new Timestamp(dNgay.getTime()));
        currentItem.setSoPhieu(txtSoPhieu.getText().trim());
        currentItem.setKhachHangId(selectedKh.getId());
        currentItem.setTenKhachHang(selectedKh.getName());
        currentItem.setDiemTang(diemTang);
        currentItem.setDiemGiam(diemGiam);
        currentItem.setLyDo(lyDo);
        currentItem.setNote(txtGhiChu.getText().trim());

        boolean success;
        if (currentItem.getId() == null) {
            success = tangGiamDiemDAO.insert(currentItem);
        } else {
            success = tangGiamDiemDAO.update(currentItem);
        }

        if (success) {
            this.allItems = tangGiamDiemDAO.getAll();
            findCurrentIndex();
            if (onSaveCallback != null) onSaveCallback.run();

            if (isSaveAndNew) {
                onNewItem();
            } else if (isSaveAndExit) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lưu dữ liệu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadFormData();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Lưu dữ liệu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onNewItem() {
        this.currentItem = new TangGiamDiem();
        this.currentIndex = -1;
        loadFormData();
        txtDiemTang.requestFocus();
    }

    private void navigatePrevious() {
        if (allItems == null || allItems.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
            this.currentItem = allItems.get(currentIndex);
            loadFormData();
        } else if (currentIndex == -1 && !allItems.isEmpty()) {
            currentIndex = 0;
            this.currentItem = allItems.get(0);
            loadFormData();
        } else {
            JOptionPane.showMessageDialog(this, "Đang ở bản ghi đầu tiên!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void navigateNext() {
        if (allItems == null || allItems.isEmpty()) return;
        if (currentIndex < allItems.size() - 1 && currentIndex >= 0) {
            currentIndex++;
            this.currentItem = allItems.get(currentIndex);
            loadFormData();
        } else {
            JOptionPane.showMessageDialog(this, "Đang ở bản ghi cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setupShortcuts() {
        JRootPane rp = getRootPane();
        InputMap im = rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rp.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        am.put("exit", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { dispose(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveAndNew");
        am.put("saveAndNew", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { onSave(true, false); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK), "save");
        am.put("save", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { onSave(false, false); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "newItem");
        am.put("newItem", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { onNewItem(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "prev");
        am.put("prev", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { navigatePrevious(); }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "next");
        am.put("next", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { navigateNext(); }
        });
    }

    private void showShortcutMenu(Component invoker) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem("Ctrl + L: Lưu"));
        popup.add(new JMenuItem("Ctrl + S: Lưu & Mới"));
        popup.add(new JMenuItem("Ctrl + N: Tạo mới"));
        popup.add(new JMenuItem("F10: Xem trước"));
        popup.add(new JMenuItem("F11: Xem sau"));
        popup.add(new JMenuItem("ESC: Thoát"));
        popup.show(invoker, 0, invoker.getHeight());
    }

    private JButton createRibbonButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setBackground(COLOR_TOOLBAR_BG);
        btn.setBorder(new EmptyBorder(3, 6, 3, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createActionButton(String text, String iconName) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 26));
        return btn;
    }
}
