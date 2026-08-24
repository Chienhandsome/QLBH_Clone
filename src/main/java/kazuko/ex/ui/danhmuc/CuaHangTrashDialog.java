package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuaHangTrashDialog extends JDialog {
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final Runnable onDataChanged;

    private JTree treeTrash;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public CuaHangTrashDialog(Dialog parent, Runnable onDataChanged) {
        super(parent, "Thùng rác", true);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashTree();
        setSize(460, 480);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Header Panel: Nền trắng + Icon Thùng rác RecycleBin.png + Tiêu đề
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));
        headerPanel.setPreferredSize(new Dimension(460, 60));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftHeader.setOpaque(false);
        JLabel lblTrashIcon = new JLabel(IconHelper.getIcon("RecycleBin.png", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Dữ liệu Cửa hàng bị xóa hoặc không dùng tới.");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderTitle.setForeground(new Color(30, 30, 30));

        leftHeader.add(lblTrashIcon);
        leftHeader.add(lblHeaderTitle);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        JLabel lblRightNote = new JLabel(IconHelper.getIcon("report--pencil.png", 24, 24));
        lblRightNote.setBorder(new EmptyBorder(0, 0, 0, 14));
        headerPanel.add(lblRightNote, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: Toolbar + JTree
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        // Toolbar: Khôi phục, Xem chi tiết, Xóa vĩnh viễn, Thuộc tính
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(new Color(218, 230, 245));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnRestore = createToolButton("Khôi phục", "Refresh.png");
        JButton btnViewDetail = createToolButton("Xem chi tiết", "Preview.png");
        JButton btnDeletePerm = createToolButton("Xóa vĩnh viễn", "cross.png");
        JButton btnProperties = createToolButton("Thuộc tính", "properties.png");

        btnRestore.addActionListener(e -> onRestoreSelected());
        btnViewDetail.addActionListener(e -> onViewDetail());
        btnDeletePerm.addActionListener(e -> onDeletePermanently());
        btnProperties.addActionListener(e -> onViewDetail());

        toolBar.add(btnRestore);
        toolBar.add(btnViewDetail);
        toolBar.add(btnDeletePerm);
        toolBar.add(btnProperties);
        centerPanel.add(toolBar, BorderLayout.NORTH);

        // Tree
        rootNode = new DefaultMutableTreeNode("Thùng rác");
        treeModel = new DefaultTreeModel(rootNode);
        treeTrash = new JTree(treeModel);
        treeTrash.setRootVisible(false);
        treeTrash.setShowsRootHandles(true);
        treeTrash.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        treeTrash.setRowHeight(22);

        treeTrash.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof CuaHang) {
                        CuaHang ch = (CuaHang) userObj;
                        if (ch.isFolder()) {
                            label.setText(ch.getName());
                            label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        } else {
                            label.setText(ch.getName());
                            label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                        }
                    }
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(treeTrash);
        scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, new Color(180, 200, 225)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom: Nút Thoát
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(80, 26));
        btnExit.setBackground(Color.WHITE);
        btnExit.addActionListener(e -> dispose());
        bottomPanel.add(btnExit);
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
        List<CuaHang> trashList = cuaHangDAO.getTrashItems();

        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Add folder nodes first
        for (CuaHang ch : trashList) {
            if (ch.isFolder() || ch.getParentId() == null) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(ch);
                nodeMap.put(ch.getId(), node);
                rootNode.add(node);
            }
        }

        // Add child items
        for (CuaHang ch : trashList) {
            if (ch.getParentId() != null) {
                DefaultMutableTreeNode parentNode = nodeMap.get(ch.getParentId());
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(ch);
                nodeMap.put(ch.getId(), childNode);
                if (parentNode != null) {
                    parentNode.add(childNode);
                } else {
                    rootNode.add(childNode);
                }
            }
        }

        treeModel.reload();
        for (int i = 0; i < treeTrash.getRowCount(); i++) {
            treeTrash.expandRow(i);
        }
    }

    private CuaHang getSelectedStore() {
        TreePath path = treeTrash.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof CuaHang) {
                return (CuaHang) node.getUserObject();
            }
        }
        return null;
    }

    private void onRestoreSelected() {
        CuaHang selected = getSelectedStore();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mục cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = cuaHangDAO.restoreFromTrash(selected.getId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã khôi phục '" + selected.getName() + "' về danh mục cửa hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadTrashTree();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        }
    }

    private void onViewDetail() {
        CuaHang selected = getSelectedStore();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mục cần xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CuaHangEditDialog dialog = new CuaHangEditDialog(this, selected, null, () -> {
            loadTrashTree();
            if (onDataChanged != null) onDataChanged.run();
        });
        dialog.setVisible(true);
    }

    private void onDeletePermanently() {
        CuaHang selected = getSelectedStore();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mục cần xóa vĩnh viễn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn XÓA VĨNH VIỄN '" + selected.getName() + "'?\nDữ liệu sẽ không thể khôi phục lại!",
                "Xác nhận xóa vĩnh viễn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            cuaHangDAO.deletePermanently(selected.getId());
            loadTrashTree();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        }
    }
}
