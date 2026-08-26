package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.LoaiHinhKhuyenMaiDAO;
import kazuko.ex.model.danhmuc.LoaiHinhKhuyenMai;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class LoaiHinhKhuyenMaiDialog extends JDialog {
    private final LoaiHinhKhuyenMaiDAO loaiHinhKhuyenMaiDAO = new LoaiHinhKhuyenMaiDAO();
    private final LoaiHinhKhuyenMai currentItem;
    private final Runnable onSaveCallback;

    private JTextField txtName;
    private JComboBox<String> cbIcon;
    private JTextArea txtNote;

    private static final Color COLOR_HEADER_BG = new Color(255, 254, 230);
    private static final Color COLOR_HEADER_BORDER = new Color(225, 220, 185);
    private static final Color COLOR_FORM_BG = new Color(190, 211, 236);
    private static final Color COLOR_TEXT_DARK = new Color(20, 45, 85);
    private static final Color COLOR_REQUIRED_FIELD = new Color(255, 255, 204);

    public LoaiHinhKhuyenMaiDialog(Component parent, LoaiHinhKhuyenMai item, Runnable onSaveCallback) {
        super(SwingUtilities.getWindowAncestor(parent), "Loại hình khuyến mại", ModalityType.APPLICATION_MODAL);
        this.currentItem = item != null ? item : new LoaiHinhKhuyenMai();
        this.onSaveCallback = onSaveCallback;

        initUI();
        loadData();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        setPreferredSize(new Dimension(480, 260));

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, COLOR_HEADER_BORDER));
        headerPanel.setPreferredSize(new Dimension(480, 48));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        leftHeader.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("pencil.png", 28, 28));
        JLabel lblHeaderTitle = new JLabel("Chỉnh sửa loại hình khuyến mại");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTitle.setForeground(COLOR_TEXT_DARK);

        leftHeader.add(lblIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Middle Form Panel
        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(COLOR_FORM_BG);
        formPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Row 1: Tên loại hình khuyến mãi & Ảnh icon
        JLabel lblName = new JLabel("Tên loại hình khuyến...");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, 12, 130, 24);
        formPanel.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBounds(145, 12, 185, 24);
        formPanel.add(txtName);

        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAnh.setBounds(340, 12, 35, 24);
        formPanel.add(lblAnh);

        String[] iconOptions = {"asterisk-yellow.png", "1282529873_objects.png", "Folder.png", "Tree.png"};
        cbIcon = new JComboBox<>(iconOptions);
        cbIcon.setBounds(375, 12, 75, 24);
        cbIcon.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
                if (value != null) {
                    lbl.setIcon(IconHelper.getIcon(value.toString(), 16, 16));
                }
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                return lbl;
            }
        });
        formPanel.add(cbIcon);

        // Row 2: Ghi chú
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNote.setBounds(15, 44, 130, 24);
        formPanel.add(lblNote);

        txtNote = new JTextArea();
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);

        JScrollPane scrollNote = new JScrollPane(txtNote);
        scrollNote.setBounds(145, 44, 305, 90);
        scrollNote.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        formPanel.add(scrollNote);

        add(formPanel, BorderLayout.CENTER);

        // 3. Footer Action Buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footerPanel.setBackground(new Color(235, 242, 252));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 230)));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.setPreferredSize(new Dimension(100, 26));
        btnSave.setBackground(Color.WHITE);
        btnSave.setFocusPainted(false);

        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(80, 26));
        btnExit.setBackground(Color.WHITE);
        btnExit.setFocusPainted(false);

        btnSave.addActionListener(e -> onSave());
        btnExit.addActionListener(e -> dispose());

        footerPanel.add(btnSave);
        footerPanel.add(btnExit);
        add(footerPanel, BorderLayout.SOUTH);

        // Shortcuts
        getRootPane().registerKeyboardAction(e -> onSave(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadData() {
        if (currentItem != null) {
            txtName.setText(currentItem.getName() != null ? currentItem.getName() : "");
            txtNote.setText(currentItem.getNote() != null ? currentItem.getNote() : "");
            if (currentItem.getIconName() != null) {
                cbIcon.setSelectedItem(currentItem.getIconName());
            }
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên loại hình khuyến mãi không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        currentItem.setName(name);
        currentItem.setNote(txtNote.getText().trim());
        currentItem.setIconName((String) cbIcon.getSelectedItem());

        boolean ok = loaiHinhKhuyenMaiDAO.update(currentItem);
        if (ok) {
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật loại hình khuyến mãi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
