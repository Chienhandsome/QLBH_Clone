package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.URL;
import java.util.List;

public class NhomKhachHangTrashDialog extends JDialog {
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();
    private final Runnable onDataChanged;

    private JTree treeTrash;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public NhomKhachHangTrashDialog(Component parentComponent, Runnable onDataChanged) {
        super(SwingUtilities.getWindowAncestor(parentComponent), "Thùng rác - Nhóm khách hàng", ModalityType.APPLICATION_MODAL);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashTree();
        setSize(480, 500);
        setLocationRelativeTo(parentComponent);
    }

    public NhomKhachHangTrashDialog(Dialog parent, Runnable onDataChanged) {
        super(parent, "Thùng rác - Nhóm khách hàng", true);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashTree();
        setSize(480, 500);
        setLocationRelativeTo(parent);
    }

    public NhomKhachHangTrashDialog(Frame parent, Runnable onDataChanged) {
        super(parent, "Thùng rác - Nhóm khách hàng", true);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashTree();
        setSize(480, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));
        headerPanel.setPreferredSize(new Dimension(480, 60));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftHeader.setOpaque(false);
        JLabel lblTrashIcon = new JLabel(IconHelper.getIcon("RecycleBin.png", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Dữ liệu Nhóm khách hàng bị xóa hoặc không dùng tới.");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        leftHeader.add(lblTrashIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(new Color(218, 230, 245));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnRestore = createToolButton("Khôi phục", "Refresh.png");
        JButton btnDeletePerm = createToolButton("Xóa vĩnh viễn", "cross.png");

        btnRestore.addActionListener(e -> onRestoreSelected());
        btnDeletePerm.addActionListener(e -> onDeletePermanently());

        toolBar.add(btnRestore);
        toolBar.add(btnDeletePerm);
        centerPanel.add(toolBar, BorderLayout.NORTH);

        rootNode = new DefaultMutableTreeNode("Thùng rác");
        treeModel = new DefaultTreeModel(rootNode);
        treeTrash = new JTree(treeModel);
        treeTrash.setRootVisible(false);
        treeTrash.setShowsRootHandles(true);
        treeTrash.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeTrash.setRowHeight(22);

        treeTrash.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof NhomKhachHang) {
                        NhomKhachHang nh = (NhomKhachHang) userObj;
                        lbl.setText(nh.getName());
                        lbl.setIcon(IconHelper.getIcon(nh.getIconName() != null ? nh.getIconName() : "Folder.png", 16, 16));
                    }
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(treeTrash);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        JButton btnClose = new JButton("Thoát");
        btnClose.setPreferredSize(new Dimension(85, 26));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createToolButton(String text, String iconName) {
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

    private void loadTrashTree() {
        rootNode.removeAllChildren();
        List<NhomKhachHang> trashList = nhomKhachHangDAO.getTrashItems();
        for (NhomKhachHang nh : trashList) {
            rootNode.add(new DefaultMutableTreeNode(nh));
        }
        treeModel.reload();
        for (int i = 0; i < treeTrash.getRowCount(); i++) {
            treeTrash.expandRow(i);
        }
    }

    private void onRestoreSelected() {
        TreePath path = treeTrash.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomKhachHang) {
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            nhomKhachHangDAO.restoreFromTrash(nh.getId());
            loadTrashTree();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void onDeletePermanently() {
        TreePath path = treeTrash.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof NhomKhachHang) {
            NhomKhachHang nh = (NhomKhachHang) node.getUserObject();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc muốn xóa vĩnh viễn nhóm '" + nh.getName() + "'?\nHành động này không thể hoàn tác!",
                    "Xác nhận xóa vĩnh viễn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                nhomKhachHangDAO.deletePermanently(nh.getId());
                loadTrashTree();
                if (onDataChanged != null) onDataChanged.run();
            }
        }
    }
}
