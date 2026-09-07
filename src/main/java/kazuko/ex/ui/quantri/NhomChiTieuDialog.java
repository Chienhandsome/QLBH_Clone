package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

final class NhomChiTieuDialog extends JDialog {
    private String savedId;

    static String show(Component owner, ChiTieuDoanhThuDAO dao, Group group) {
        NhomChiTieuDialog dialog = new NhomChiTieuDialog(SwingUtilities.getWindowAncestor(owner), dao, group);
        dialog.setVisible(true);
        return dialog.savedId;
    }

    private record ImageChoice(String id, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    private NhomChiTieuDialog(Window owner, ChiTieuDoanhThuDAO dao, Group g) {
        super(owner, "Nhóm chỉ tiêu doanh thu", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        setResizable(false);

        boolean isEditing = (g.id() != null);

        // 1. Header Banner (Matching Screenshot 3)
        JPanel banner = new JPanel(new BorderLayout(10, 0));
        banner.setBackground(Color.WHITE);
        banner.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(205, 215, 228)),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel iconLabel = new JLabel(IconHelper.getIcon("icon_note_edit.png", 32, 32));
        if (iconLabel.getIcon() == null) {
            iconLabel.setIcon(IconHelper.getIcon("pencil.png", 28, 28));
        }

        JLabel titleLabel = new JLabel(isEditing ? "Chỉnh sửa nhóm chỉ tiêu doanh thu" : "Thêm mới nhóm chỉ tiêu doanh thu");
        titleLabel.setFont(ChiTieuUI.FONT_TITLE);
        titleLabel.setForeground(new Color(20, 45, 85));

        banner.add(iconLabel, BorderLayout.WEST);
        banner.add(titleLabel, BorderLayout.CENTER);
        add(banner, BorderLayout.NORTH);

        // 2. Form Body (Matching Screenshot 3)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(ChiTieuUI.COLOR_FORM_BG);
        form.setBorder(new EmptyBorder(12, 14, 12, 14));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Tên nhóm chỉ tiêu doanh thu + Ảnh
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.0;
        JLabel lblName = new JLabel("Tên nhóm chỉ tiêu doa...:");
        lblName.setFont(ChiTieuUI.FONT);
        form.add(lblName, c);

        c.gridx = 1;
        c.weightx = 1.0;
        JTextField name = new JTextField(g.name(), 18);
        name.setFont(ChiTieuUI.FONT);
        name.setBackground(Color.WHITE);
        name.setBorder(new CompoundBorder(new LineBorder(new Color(0, 102, 204), 1), new EmptyBorder(2, 4, 2, 4)));
        form.add(name, c);

        c.gridx = 2;
        c.weightx = 0.0;
        JLabel lblImg = new JLabel("Ảnh:");
        lblImg.setFont(ChiTieuUI.FONT);
        form.add(lblImg, c);

        c.gridx = 3;
        c.weightx = 0.0;
        JComboBox<ImageChoice> image = new JComboBox<>();
        image.setFont(ChiTieuUI.FONT);
        image.setBackground(Color.WHITE);
        image.setPreferredSize(new Dimension(90, 24));
        image.addItem(new ImageChoice("", ""));
        try {
            for (Map.Entry<String, String> entry : dao.images().entrySet()) {
                image.addItem(new ImageChoice(entry.getKey(), entry.getValue()));
                if (Objects.equals(entry.getKey(), g.imageId())) {
                    image.setSelectedIndex(image.getItemCount() - 1);
                }
            }
        } catch (Exception ignored) {}
        form.add(image, c);

        // Row 2: Ghi chú
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(ChiTieuUI.FONT);
        form.add(lblNote, c);

        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        JTextArea note = new JTextArea(g.note(), 4, 20);
        note.setFont(ChiTieuUI.FONT);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane scrollNote = new JScrollPane(note);
        scrollNote.setBorder(new LineBorder(new Color(0, 102, 204), 1));
        scrollNote.setPreferredSize(new Dimension(340, 85));
        form.add(scrollNote, c);

        add(form, BorderLayout.CENTER);

        // 3. Footer Buttons (Matching Screenshot 3)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        footer.setBackground(ChiTieuUI.COLOR_FOOTER_BG);
        footer.setBorder(new MatteBorder(1, 0, 0, 0, ChiTieuUI.COLOR_FOOTER_BORDER));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(ChiTieuUI.FONT_BOLD);
        btnSave.setFocusPainted(false);
        btnSave.setBackground(Color.WHITE);
        btnSave.setPreferredSize(new Dimension(95, 26));

        JButton btnCancel = new JButton("Thoát");
        btnCancel.setFont(ChiTieuUI.FONT_BOLD);
        btnCancel.setFocusPainted(false);
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(80, 26));

        btnSave.addActionListener(e -> {
            String n = name.getText().trim();
            if (n.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên nhóm chỉ tiêu doanh thu.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                name.requestFocusInWindow();
                return;
            }
            try {
                ImageChoice img = (ImageChoice) image.getSelectedItem();
                savedId = dao.saveGroup(new Group(
                        g.id(),
                        n,
                        note.getText().trim(),
                        g.parentId(),
                        g.folder(),
                        img == null ? null : img.id(),
                        g.status()
                ));
                dispose();
            } catch (Exception ex) {
                ChiTieuUI.error(this, ex);
            }
        });

        btnCancel.addActionListener(e -> dispose());

        footer.add(btnSave);
        footer.add(btnCancel);
        add(footer, BorderLayout.SOUTH);

        // Phím tắt Enter & Escape
        getRootPane().setDefaultButton(btnSave);
        ChiTieuUI.bind(getRootPane(), "ESCAPE", this::dispose);

        pack();
        setSize(480, 240);
        setLocationRelativeTo(owner);
    }
}
