package kazuko.ex.ui.touch;

import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.NhomKhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.UUID;

/**
 * Hộp thoại cảm ứng "CHỌN NHÓM KHÁCH HÀNG" theo Hình 5.
 * Danh sách các nhóm dạng nút bấm màu xanh lá.
 * Thanh dưới gồm: THÊM, SỬA, XÓA TRỐNG, THOÁT.
 */
public class TouchChonNhomKhachHangDialog extends JDialog {
    private static final Color BG_HEADER = new Color(0, 67, 86);
    private static final Color LIST_BG = new Color(225, 225, 225);
    private static final Color GREEN_BTN = new Color(0, 105, 0);
    private static final Color SELECTED_COLOR = new Color(226, 79, 5);
    private static final Color PURPLE_BTN = new Color(106, 13, 84);
    private static final Color RED_BTN = new Color(155, 0, 0);

    private final NhomKhachHangDAO nhomDao = new NhomKhachHangDAO();
    private final JPanel listContainer = new JPanel();
    private NhomKhachHang selectedGroup = null;
    private boolean confirmed = false;
    private boolean cleared = false;

    public TouchChonNhomKhachHangDialog(Window owner, NhomKhachHang currentSelected) {
        super(owner, "CHỌN NHÓM KHÁCH HÀNG", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        this.selectedGroup = currentSelected;
        initComponents();
        loadGroups();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_HEADER);
        root.setBorder(new LineBorder(new Color(0, 110, 140), 2));
        root.setPreferredSize(new Dimension(650, 400));

        // 1. Header
        JLabel lblTitle = new JLabel("CHỌN NHÓM KHÁCH HÀNG", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(BG_HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setPreferredSize(new Dimension(650, 42));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Center: List of groups
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(LIST_BG);
        listContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(LIST_BG);
        root.add(scroll, BorderLayout.CENTER);

        // 3. Bottom Toolbar: THÊM | SỬA  ...  XÓA TRỐNG | THOÁT
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        JButton btnThem = actionButton("THÊM", GREEN_BTN, new Dimension(100, 42));
        btnThem.addActionListener(e -> handleAddGroup());

        JButton btnSua = actionButton("SỬA", GREEN_BTN, new Dimension(100, 42));
        btnSua.addActionListener(e -> handleEditGroup());

        leftBtns.add(btnThem);
        leftBtns.add(btnSua);
        bottom.add(leftBtns, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        JButton btnClear = actionButton("XÓA TRỐNG", PURPLE_BTN, new Dimension(130, 42));
        btnClear.addActionListener(e -> {
            selectedGroup = null;
            cleared = true;
            confirmed = true;
            dispose();
        });

        JButton btnExit = actionButton("THOÁT", RED_BTN, new Dimension(100, 42));
        btnExit.addActionListener(e -> {
            if (selectedGroup != null) {
                confirmed = true;
            }
            dispose();
        });

        rightBtns.add(btnClear);
        rightBtns.add(btnExit);
        bottom.add(rightBtns, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        // ESC to cancel
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadGroups() {
        listContainer.removeAll();
        List<NhomKhachHang> groups = nhomDao.getAll();
        for (NhomKhachHang grp : groups) {
            JButton btn = new JButton(grp.getName());
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(new EmptyBorder(0, 16, 0, 16));

            boolean isSelected = selectedGroup != null && selectedGroup.getId() != null && selectedGroup.getId().equals(grp.getId());
            btn.setBackground(isSelected ? SELECTED_COLOR : GREEN_BTN);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 || (selectedGroup != null && selectedGroup.getId() != null && selectedGroup.getId().equals(grp.getId()))) {
                        // Double click hoặc click lại group đang chọn -> Xác nhận luôn
                        selectedGroup = grp;
                        confirmed = true;
                        dispose();
                    } else {
                        selectedGroup = grp;
                        loadGroups();
                    }
                }
            });

            listContainer.add(btn);
            listContainer.add(Box.createVerticalStrut(4));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void handleAddGroup() {
        String newName = TouchTextKeyboardDialog.showDialog(this, "THÊM NHÓM KHÁCH HÀNG", "");
        if (newName != null && !newName.trim().isEmpty()) {
            NhomKhachHang nkh = new NhomKhachHang(UUID.randomUUID().toString(), newName.trim());
            nhomDao.insert(nkh);
            selectedGroup = nkh;
            loadGroups();
        }
    }

    private void handleEditGroup() {
        if (selectedGroup == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String updated = TouchTextKeyboardDialog.showDialog(this, "SỬA TÊN NHÓM", selectedGroup.getName());
        if (updated != null && !updated.trim().isEmpty()) {
            selectedGroup.setName(updated.trim());
            nhomDao.update(selectedGroup);
            loadGroups();
        }
    }

    private JButton actionButton(String text, Color bg, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        btn.setPreferredSize(size);
        return btn;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public boolean isCleared() {
        return cleared;
    }

    public NhomKhachHang getSelectedGroup() {
        return selectedGroup;
    }

    /**
     * Mở hộp thoại chọn nhóm khách hàng cảm ứng.
     * @return NhomKhachHang đã chọn, hoặc null nếu chọn Xóa trống hoặc Hủy.
     */
    public static NhomKhachHang showDialog(Window owner, NhomKhachHang currentSelected) {
        TouchChonNhomKhachHangDialog dlg = new TouchChonNhomKhachHangDialog(owner, currentSelected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            return dlg.getSelectedGroup();
        }
        return currentSelected; // Giữ nguyên nếu bấm Hủy
    }
}
