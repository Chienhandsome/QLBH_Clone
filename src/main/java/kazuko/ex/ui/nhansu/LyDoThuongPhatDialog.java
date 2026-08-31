package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.LyDoThuongPhatDAO;
import kazuko.ex.model.nhansu.LyDoThuongPhat;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LyDoThuongPhatDialog extends JDialog {

    private final LyDoThuongPhatDAO dao = new LyDoThuongPhatDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private List<LyDoThuongPhat> currentList;

    public LyDoThuongPhatDialog(Window parent) {
        super(parent, "Danh mục lý do thưởng phạt", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadData();
        setSize(520, 420);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(230, 240, 252));

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("asterisk-yellow.png", 22, 22));
        JLabel lblTitle = new JLabel("Danh mục lý do thưởng phạt");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 35, 65));
        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlToolbar.setBackground(new Color(225, 238, 252));
        pnlToolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnAdd = new JButton("Thêm", IconHelper.getIcon("plus.png", 14, 14));
        JButton btnEdit = new JButton("Sửa", IconHelper.getIcon("pencil.png", 14, 14));
        JButton btnDelete = new JButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));
        JButton btnRefresh = new JButton("Nạp lại", IconHelper.getIcon("Refresh.png", 14, 14));

        styleButton(btnAdd);
        styleButton(btnEdit);
        styleButton(btnDelete);
        styleButton(btnRefresh);

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnRefresh.addActionListener(e -> loadData());

        pnlToolbar.add(btnAdd);
        pnlToolbar.add(btnEdit);
        pnlToolbar.add(btnDelete);
        pnlToolbar.add(btnRefresh);
        add(pnlToolbar, BorderLayout.CENTER);

        // Table
        String[] cols = {"STT", "Tên lý do", "Ghi chú"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEdit();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlToolbar, BorderLayout.NORTH);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // Bottom close
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setOpaque(false);
        JButton btnClose = new JButton("Đóng");
        styleButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        pnlBottom.add(btnClose);
        add(pnlBottom, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadData() {
        currentList = dao.getAllActive();
        tableModel.setRowCount(0);
        int stt = 1;
        for (LyDoThuongPhat ld : currentList) {
            tableModel.addRow(new Object[]{
                    stt++,
                    ld.getName() != null ? ld.getName() : "",
                    ld.getGhiChu() != null ? ld.getGhiChu() : ""
            });
        }
        if (!currentList.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void onAdd() {
        LyDoThuongPhatEditDialog dlg = new LyDoThuongPhatEditDialog(this, null, this::loadData);
        dlg.setVisible(true);
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lý do để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LyDoThuongPhat ld = currentList.get(row);
        LyDoThuongPhatEditDialog dlg = new LyDoThuongPhatEditDialog(this, ld, this::loadData);
        dlg.setVisible(true);
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lý do để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LyDoThuongPhat ld = currentList.get(row);
        int cf = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa lý do [" + ld.getName() + "]?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (cf == JOptionPane.YES_OPTION) {
            dao.moveToTrash(ld.getId());
            loadData();
        }
    }
}
