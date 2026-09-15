package kazuko.ex.ui.touch;

import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.danhmuc.NhanVien;

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
 * Hộp thoại cảm ứng "CHỌN NHÂN VIÊN" theo đúng hình 2 của người dùng.
 * Danh sách các nhân viên dạng nút bấm màu xanh lá.
 * Thanh dưới gồm: THÊM, SỬA, XÓA TRỐNG, THOÁT.
 */
public class TouchChonNhanVienDialog extends JDialog {
    private static final Color BG_HEADER = new Color(79, 139, 169);
    private static final Color BG_BODY = new Color(0, 67, 86);
    private static final Color LIST_BG = new Color(225, 225, 225);
    private static final Color GREEN_BTN = new Color(0, 95, 0);
    private static final Color SELECTED_COLOR = new Color(226, 79, 5);
    private static final Color PURPLE_BTN = new Color(106, 13, 84);
    private static final Color RED_BTN = new Color(155, 0, 0);

    private final NhanVienDAO nhanVienDao = new NhanVienDAO();
    private final JPanel listContainer = new JPanel();
    private NhanVien selectedEmployee = null;
    private boolean confirmed = false;
    private boolean cleared = false;

    public TouchChonNhanVienDialog(Window owner, NhanVien currentSelected) {
        super(owner, "CHỌN NHÂN VIÊN", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        this.selectedEmployee = currentSelected;
        initComponents();
        loadEmployees();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_BODY);
        root.setBorder(new LineBorder(new Color(0, 110, 140), 3));
        root.setPreferredSize(new Dimension(680, 420));

        // 1. Header: CHỌN NHÂN VIÊN
        JLabel lblTitle = new JLabel("CHỌN NHÂN VIÊN", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(BG_HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setPreferredSize(new Dimension(680, 46));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Center: List of employees
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(LIST_BG);
        listContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(LIST_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(35);
        root.add(scroll, BorderLayout.CENTER);

        // 3. Bottom Toolbar: THÊM | SỬA  ...  XÓA TRỐNG | THOÁT
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 10, 10, 10));

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftBtns.setOpaque(false);

        JButton btnThem = actionButton("THÊM", GREEN_BTN, new Dimension(100, 44));
        btnThem.addActionListener(e -> handleAddEmployee());

        JButton btnSua = actionButton("SỬA", GREEN_BTN, new Dimension(100, 44));
        btnSua.addActionListener(e -> handleEditEmployee());

        leftBtns.add(btnThem);
        leftBtns.add(btnSua);
        bottom.add(leftBtns, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);

        JButton btnClear = actionButton("XÓA TRỐNG", PURPLE_BTN, new Dimension(135, 44));
        btnClear.addActionListener(e -> {
            selectedEmployee = null;
            cleared = true;
            confirmed = true;
            dispose();
        });

        JButton btnExit = actionButton("THOÁT", RED_BTN, new Dimension(100, 44));
        btnExit.addActionListener(e -> {
            if (selectedEmployee != null) {
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

    private void loadEmployees() {
        listContainer.removeAll();
        List<NhanVien> employees = nhanVienDao.getAll();
        for (NhanVien nv : employees) {
            JButton btn = new JButton(nv.getName());
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(new EmptyBorder(0, 16, 0, 16));

            boolean isSelected = selectedEmployee != null && selectedEmployee.getId() != null && selectedEmployee.getId().equals(nv.getId());
            btn.setBackground(isSelected ? SELECTED_COLOR : GREEN_BTN);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 || (selectedEmployee != null && selectedEmployee.getId() != null && selectedEmployee.getId().equals(nv.getId()))) {
                        selectedEmployee = nv;
                        confirmed = true;
                        dispose();
                    } else {
                        selectedEmployee = nv;
                        loadEmployees();
                    }
                }
            });

            listContainer.add(btn);
            listContainer.add(Box.createVerticalStrut(4));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private void handleAddEmployee() {
        String newName = TouchTextKeyboardDialog.showDialog(this, "THÊM NHÂN VIÊN MỚI", "");
        if (newName != null && !newName.trim().isEmpty()) {
            NhanVien nv = new NhanVien(UUID.randomUUID().toString(), newName.trim(), "user_female_1");
            nhanVienDao.insert(nv);
            selectedEmployee = nv;
            loadEmployees();
        }
    }

    private void handleEditEmployee() {
        if (selectedEmployee == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String updated = TouchTextKeyboardDialog.showDialog(this, "SỬA TÊN NHÂN VIÊN", selectedEmployee.getName());
        if (updated != null && !updated.trim().isEmpty()) {
            selectedEmployee.setName(updated.trim());
            nhanVienDao.update(selectedEmployee);
            loadEmployees();
        }
    }

    private JButton actionButton(String text, Color bg, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

    public NhanVien getSelectedEmployee() {
        return selectedEmployee;
    }

    /**
     * Mở hộp thoại chọn nhân viên cảm ứng.
     * @return NhanVien đã chọn, hoặc null nếu chọn Xóa trống hoặc Hủy.
     */
    public static NhanVien showDialog(Window owner, NhanVien currentSelected) {
        TouchChonNhanVienDialog dlg = new TouchChonNhanVienDialog(owner, currentSelected);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            return dlg.getSelectedEmployee();
        }
        return currentSelected;
    }
}
