package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.BangLuongDAO;
import kazuko.ex.model.nhansu.BangLuong;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.List;

public class TaoBangLuongDialog extends JDialog {

    private final BangLuongDAO bangLuongDAO = new BangLuongDAO();
    private BangLuong currentBangLuong;
    private final Runnable onDataSaved;

    private JTextField txtSoPhieu;
    private JSpinner spThang;
    private JSpinner spNam;
    private JTextArea txtGhiChu;

    private JButton btnLuuVaIn;
    private JButton btnLuuVaXemIn;
    private JButton btnLuu;
    private JButton btnLuuVaMoi;
    private JButton btnLuuVaThoat;
    private JButton btnThoat;

    private JButton btnTruoc;
    private JButton btnSau;
    private JButton btnTaoMoi;
    private JButton btnSaoChep;
    private JButton btnXoa;

    private static final Color BG_CYAN = new Color(230, 248, 255);
    private static final Color BG_FORM = new Color(215, 230, 248);

    public TaoBangLuongDialog(Window parent) {
        this(parent, null, null);
    }

    public TaoBangLuongDialog(Window parent, BangLuong bl, Runnable onDataSaved) {
        super(parent, bl != null && bl.getId() != null ? "BẢNG LƯƠNG - CẬP NHẬT" : "BẢNG LƯƠNG - THÊM MỚI", ModalityType.APPLICATION_MODAL);
        this.currentBangLuong = bl;
        this.onDataSaved = onDataSaved;

        initComponents();
        if (currentBangLuong != null) {
            loadToForm(currentBangLuong);
        } else {
            resetForNew();
        }

        setupHotkeys();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(480, 360);
        setPreferredSize(new Dimension(480, 360));
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

        JLabel lblIcon = new JLabel(IconHelper.getIcon("btnCard.Icon.png", 28, 28));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("report--pencil.png", 28, 28));
        }
        JLabel lblTitle = new JLabel("Bảng lương");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(20, 35, 65));

        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitle);
        pnlNorth.add(pnlTitle, BorderLayout.NORTH);

        // Sub Toolbar: Phím tắt, Trước, Sau, Tạo mới, Sao chép, Xóa
        JPanel pnlNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        pnlNav.setBackground(new Color(225, 238, 252));
        pnlNav.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnPhimTat = createTopNavButton("Phím tắt ▼", null);
        btnTruoc = createTopNavButton("Trước (F10)", IconHelper.getIcon("arrow-180.png", 14, 14));
        btnSau = createTopNavButton("Sau (F11)", IconHelper.getIcon("arrow.png", 14, 14));
        btnTaoMoi = createTopNavButton("Tạo mới", IconHelper.getIcon("plus.png", 14, 14));
        btnSaoChep = createTopNavButton("Sao chép", IconHelper.getIcon("report--pencil.png", 14, 14));
        btnXoa = createTopNavButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));

        btnPhimTat.addActionListener(e -> showShortcutHelp());
        btnTruoc.addActionListener(e -> onNavigatePrevious());
        btnSau.addActionListener(e -> onNavigateNext());
        btnTaoMoi.addActionListener(e -> resetForNew());
        btnSaoChep.addActionListener(e -> onCopy());
        btnXoa.addActionListener(e -> onDelete());

        pnlNav.add(btnPhimTat);
        pnlNav.add(btnTruoc);
        pnlNav.add(btnSau);
        pnlNav.add(btnTaoMoi);
        pnlNav.add(btnSaoChep);
        pnlNav.add(btnXoa);
        pnlNorth.add(pnlNav, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center Form Body
        JPanel pnlCenter = new JPanel(null);
        pnlCenter.setOpaque(false);
        pnlCenter.setPreferredSize(new Dimension(480, 200));

        int y = 15;
        int rowH = 24;
        int gapY = 32;

        // Row 1: Số phiếu
        JLabel lblSoPhieu = createLabel("Số phiếu", 15, y, 65, rowH);
        txtSoPhieu = new JTextField();
        txtSoPhieu.setBounds(85, y, 365, rowH);
        txtSoPhieu.setBackground(BG_CYAN);
        txtSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblSoPhieu);
        pnlCenter.add(txtSoPhieu);

        // Row 2: Tháng + Năm
        y += gapY;
        JLabel lblThang = createLabel("Tháng", 15, y, 65, rowH);

        Calendar now = Calendar.getInstance();
        spThang = new JSpinner(new SpinnerNumberModel(now.get(Calendar.MONTH) + 1, 1, 12, 1));
        spThang.setBounds(85, y, 70, rowH);
        spThang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spThang.addChangeListener(e -> updateAutoSoPhieu());

        JLabel lblNam = createLabel("Năm", 185, y, 40, rowH);
        spNam = new JSpinner(new SpinnerNumberModel(now.get(Calendar.YEAR), 2000, 2100, 1));
        spNam.setBounds(230, y, 80, rowH);
        spNam.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spNam.addChangeListener(e -> updateAutoSoPhieu());

        pnlCenter.add(lblThang);
        pnlCenter.add(spThang);
        pnlCenter.add(lblNam);
        pnlCenter.add(spNam);

        // Row 3: Ghi chú
        y += gapY;
        JLabel lblGhiChu = createLabel("Ghi chú", 15, y, 65, rowH);
        txtGhiChu = new JTextArea();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBounds(85, y, 365, 80);
        scrollGhiChu.setBorder(new LineBorder(new Color(180, 200, 225), 1));

        pnlCenter.add(lblGhiChu);
        pnlCenter.add(scrollGhiChu);

        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 235)));

        btnLuuVaIn = createBottomButton("Lưu & In", IconHelper.getIcon("printer.png", 14, 14));
        btnLuuVaXemIn = createBottomButton("Lưu & Xem in", null);
        btnLuu = createBottomButton("Lưu", IconHelper.getIcon("save.png", 14, 14));
        btnLuuVaMoi = createBottomButton("Lưu & Mới", null);
        btnLuuVaThoat = createBottomButton("Lưu & thoát", null);
        btnThoat = createBottomButton("Thoát", null);

        btnLuuVaIn.addActionListener(e -> onSave(false, true));
        btnLuuVaXemIn.addActionListener(e -> onSave(false, false));
        btnLuu.addActionListener(e -> onSave(false, false));
        btnLuuVaMoi.addActionListener(e -> onSave(true, false));
        btnLuuVaThoat.addActionListener(e -> onSave(false, true));
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnLuuVaIn);
        pnlBottom.add(btnLuuVaXemIn);
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
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(185, 205, 230), 1), new EmptyBorder(4, 10, 4, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateAutoSoPhieu() {
        int th = (Integer) spThang.getValue();
        int nam = (Integer) spNam.getValue();
        txtSoPhieu.setText("Tháng " + th + "/" + nam);
    }

    private void resetForNew() {
        currentBangLuong = new BangLuong();
        Calendar cal = Calendar.getInstance();
        spThang.setValue(cal.get(Calendar.MONTH) + 1);
        spNam.setValue(cal.get(Calendar.YEAR));
        updateAutoSoPhieu();
        txtGhiChu.setText("");
        setTitle("BẢNG LƯƠNG - THÊM MỚI");
    }

    private void loadToForm(BangLuong bl) {
        if (bl == null) return;
        this.currentBangLuong = bl;
        setTitle(bl.getId() != null ? "BẢNG LƯƠNG - CẬP NHẬT" : "BẢNG LƯƠNG - THÊM MỚI");

        txtSoPhieu.setText(bl.getSoPhieu() != null ? bl.getSoPhieu() : "");
        spThang.setValue(bl.getThang() > 0 ? bl.getThang() : 1);
        spNam.setValue(bl.getNam() > 0 ? bl.getNam() : 2026);
        txtGhiChu.setText(bl.getGhiChu() != null ? bl.getGhiChu() : "");
    }

    private void onSave(boolean isNewAfter, boolean isCloseAfter) {
        String soPhieu = txtSoPhieu.getText().trim();
        if (soPhieu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số phiếu bảng lương!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtSoPhieu.requestFocus();
            return;
        }

        if (currentBangLuong == null) currentBangLuong = new BangLuong();

        currentBangLuong.setSoPhieu(soPhieu);
        currentBangLuong.setThang((Integer) spThang.getValue());
        currentBangLuong.setNam((Integer) spNam.getValue());
        currentBangLuong.setGhiChu(txtGhiChu.getText().trim());

        if (currentBangLuong.getId() == null || currentBangLuong.getId().trim().isEmpty()) {
            bangLuongDAO.insert(currentBangLuong);
        } else {
            bangLuongDAO.update(currentBangLuong);
        }

        if (onDataSaved != null) onDataSaved.run();

        if (isCloseAfter) {
            dispose();
        } else if (isNewAfter) {
            resetForNew();
        } else {
            JOptionPane.showMessageDialog(this, "Lưu thông tin bảng lương thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onCopy() {
        if (currentBangLuong == null) return;
        currentBangLuong = new BangLuong();
        int th = (Integer) spThang.getValue();
        int nam = (Integer) spNam.getValue();
        th++;
        if (th > 12) {
            th = 1;
            nam++;
        }
        spThang.setValue(th);
        spNam.setValue(nam);
        updateAutoSoPhieu();
        setTitle("BẢNG LƯƠNG - THÊM MỚI");
    }

    private void onDelete() {
        if (currentBangLuong == null || currentBangLuong.getId() == null) return;
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa bảng lương [" + currentBangLuong.getSoPhieu() + "]?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            bangLuongDAO.delete(currentBangLuong.getId());
            if (onDataSaved != null) onDataSaved.run();
            dispose();
        }
    }

    private void onNavigatePrevious() {
        List<BangLuong> list = bangLuongDAO.getAll();
        if (list.isEmpty() || currentBangLuong == null || currentBangLuong.getId() == null) return;
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (currentBangLuong.getId().equals(list.get(i).getId())) {
                idx = i;
                break;
            }
        }
        if (idx > 0) {
            loadToForm(list.get(idx - 1));
        }
    }

    private void onNavigateNext() {
        List<BangLuong> list = bangLuongDAO.getAll();
        if (list.isEmpty() || currentBangLuong == null || currentBangLuong.getId() == null) return;
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (currentBangLuong.getId().equals(list.get(i).getId())) {
                idx = i;
                break;
            }
        }
        if (idx >= 0 && idx < list.size() - 1) {
            loadToForm(list.get(idx + 1));
        }
    }

    private void showShortcutHelp() {
        JOptionPane.showMessageDialog(this,
                "<html><b>CÁC PHÍM TẮT:</b><br>" +
                        "- <b>Alt + L</b>: Lưu dữ liệu<br>" +
                        "- <b>Alt + M</b>: Lưu và thêm mới<br>" +
                        "- <b>Alt + T</b>: Lưu và thoát<br>" +
                        "- <b>F10</b>: Bảng lương trước<br>" +
                        "- <b>F11</b>: Bảng lương sau<br>" +
                        "- <b>Esc</b>: Thoát</html>",
                "Phím tắt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupHotkeys() {
        getRootPane().registerKeyboardAction(e -> onSave(false, false),
                KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> onSave(true, false),
                KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> onSave(false, true),
                KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> onNavigatePrevious(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> onNavigateNext(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
