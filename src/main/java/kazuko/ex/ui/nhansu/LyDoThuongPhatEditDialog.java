package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.LyDoThuongPhatDAO;
import kazuko.ex.model.nhansu.LyDoThuongPhat;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

public class LyDoThuongPhatEditDialog extends JDialog {

    private final LyDoThuongPhatDAO dao = new LyDoThuongPhatDAO();
    private LyDoThuongPhat currentReason;
    private final Runnable onDataSaved;

    private JTextField txtName;
    private JComboBox<String> cbAvatar;
    private JTextArea txtGhiChu;

    private static final String[] AVATAR_OPTIONS = {
            "", "asterisk-yellow.png", "gift.png", "plus.png", "pencil.png", "btnCard.Icon.png"
    };

    public LyDoThuongPhatEditDialog(Window parent, LyDoThuongPhat reason, Runnable onDataSaved) {
        super(parent, "Lý do thưởng phạt", ModalityType.APPLICATION_MODAL);
        this.currentReason = reason;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDataToForm();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(215, 230, 248));

        // 1. Top Header Banner
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("pencil.png", 22, 22));
        JLabel lblTitle = new JLabel(currentReason != null ? "Chỉnh sửa lý do thưởng phạt" : "Thêm mới lý do thưởng phạt");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 35, 65));

        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Form Body
        JPanel pnlBody = new JPanel(null);
        pnlBody.setOpaque(false);
        pnlBody.setPreferredSize(new Dimension(460, 220));

        int y = 15;
        int rowH = 24;

        // Row 1: Tên lý do & Ảnh
        JLabel lblName = new JLabel("Tên lý do thưởng phạt");
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblName.setBounds(15, y, 130, rowH);
        pnlBody.add(lblName);

        txtName = new JTextField();
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtName.setBounds(150, y, 185, rowH);
        pnlBody.add(txtName);

        JLabel lblAnh = new JLabel("Ảnh:");
        lblAnh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAnh.setBounds(345, y, 30, rowH);
        pnlBody.add(lblAnh);

        cbAvatar = new JComboBox<>(AVATAR_OPTIONS);
        cbAvatar.setBounds(380, y, 65, rowH);
        cbAvatar.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String val = (String) value;
                if (val != null && !val.isEmpty()) {
                    lbl.setIcon(IconHelper.getIcon(val, 16, 16));
                    lbl.setText("");
                } else {
                    lbl.setIcon(null);
                    lbl.setText("--");
                }
                return lbl;
            }
        });
        pnlBody.add(cbAvatar);

        // Row 2: Ghi chú
        y += 32;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setBounds(15, y, 130, rowH);
        pnlBody.add(lblGhiChu);

        txtGhiChu = new JTextArea();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);

        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setBounds(150, y, 295, 125);
        scrollGhiChu.setBorder(new LineBorder(new Color(180, 200, 225), 1));
        pnlBody.add(scrollGhiChu);

        add(pnlBody, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(185, 205, 235)));

        JButton btnGhiDuLieu = new JButton("Ghi dữ liệu");
        btnGhiDuLieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnGhiDuLieu.setBackground(Color.WHITE);
        btnGhiDuLieu.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225), 1), new EmptyBorder(4, 12, 4, 12)));
        btnGhiDuLieu.setFocusPainted(false);
        btnGhiDuLieu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGhiDuLieu.addActionListener(e -> onSave());

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225), 1), new EmptyBorder(4, 16, 4, 16)));
        btnThoat.setFocusPainted(false);
        btnThoat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnGhiDuLieu);
        pnlBottom.add(btnThoat);
        add(pnlBottom, BorderLayout.SOUTH);

        // ESC key to close
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadDataToForm() {
        if (currentReason != null) {
            txtName.setText(currentReason.getName() != null ? currentReason.getName() : "");
            txtGhiChu.setText(currentReason.getGhiChu() != null ? currentReason.getGhiChu() : "");
            if (currentReason.getAvatarIcon() != null) {
                cbAvatar.setSelectedItem(currentReason.getAvatarIcon());
            }
        }
    }

    private void onSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên lý do thưởng phạt!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return;
        }

        if (currentReason == null) {
            currentReason = new LyDoThuongPhat();
        }

        currentReason.setName(name);
        currentReason.setGhiChu(txtGhiChu.getText().trim());
        currentReason.setAvatarIcon((String) cbAvatar.getSelectedItem());

        if (currentReason.getId() == null || currentReason.getId().trim().isEmpty()) {
            dao.insert(currentReason);
        } else {
            dao.update(currentReason);
        }

        if (onDataSaved != null) onDataSaved.run();
        dispose();
    }
}
