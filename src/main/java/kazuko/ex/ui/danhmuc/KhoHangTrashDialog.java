package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
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

public class KhoHangTrashDialog extends JDialog {
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final Runnable onDataChanged;

    private JTree treeTrash;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    public KhoHangTrashDialog(Dialog parent, Runnable onDataChanged) {
        super(parent, "Thùng rác", true);
        this.onDataChanged = onDataChanged;

        initComponents();
        loadTrashTree();
        setSize(460, 480);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));
        headerPanel.setPreferredSize(new Dimension(460, 60));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        leftHeader.setOpaque(false);
        JLabel lblTrashIcon = new JLabel(IconHelper.getIcon("RecycleBin.png", 32, 32));
        JLabel lblHeaderTitle = new JLabel("Dữ liệu Kho hàng bị xóa hoặc không dùng tới.");
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
        JButton btnViewDetail = createToolButton("Xem chi tiết", "pencil.png");
        JButton btnDeletePerm = createToolButton("Xóa vĩnh viễn", "cross.png");
        JButton btnProperties = createToolButton("Thuộc tính", "settings.png");

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
                    if (userObj instanceof KhoHang) {
                        KhoHang k = (KhoHang) userObj;
                        if (k.isSeparator()) {
                            label.setText("----------------------------------------------");
                            label.setForeground(Color.LIGHT_GRAY);
                            label.setIcon(null);
                        } else if (k.isFolder()) {
                            label.setText(k.getName());
                            label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                        } else {
                            label.setText(k.getName());
                            label.setIcon(null);
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
        List<KhoHang> trashList = khoHangDAO.getTrashItems();

        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Pass 1: Tạo TreeNode cho tất cả các phần tử
        for (KhoHang k : trashList) {
            if (k.getId() != null) {
                nodeMap.put(k.getId(), new DefaultMutableTreeNode(k));
            }
        }

        // Pass 2: Gắn vào cây phân cấp theo parentId
        for (KhoHang k : trashList) {
            DefaultMutableTreeNode currentNode = nodeMap.get(k.getId());
            if (currentNode == null) continue;

            String parentId = k.getParentId();
            DefaultMutableTreeNode parentNode = null;
            if (parentId != null && !parentId.trim().isEmpty()) {
                parentNode = nodeMap.get(parentId.trim());
            }

            if (parentNode != null && parentNode != currentNode) {
                parentNode.add(currentNode);
            } else {
                rootNode.add(currentNode);
            }
        }

        treeModel.reload();
        for (int i = 0; i < treeTrash.getRowCount(); i++) {
            treeTrash.expandRow(i);
        }
    }

    private KhoHang getSelectedWarehouse() {
        TreePath path = treeTrash.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof KhoHang) {
                return (KhoHang) node.getUserObject();
            }
        }
        return null;
    }

    private void onRestoreSelected() {
        KhoHang selected = getSelectedWarehouse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mục cần khôi phục!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = khoHangDAO.restoreFromTrash(selected.getId());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã khôi phục '" + selected.getName() + "' về danh mục kho hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadTrashTree();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        }
    }

    private void onViewDetail() {
        KhoHang selected = getSelectedWarehouse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn mục cần xem chi tiết!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KhoHangEditDialog dialog = new KhoHangEditDialog(this, selected, null, () -> {
            loadTrashTree();
            if (onDataChanged != null) onDataChanged.run();
        });
        dialog.setVisible(true);
    }

    private void onDeletePermanently() {
        KhoHang selected = getSelectedWarehouse();
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
            boolean ok = khoHangDAO.deletePermanently(selected.getId());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa vĩnh viễn '" + selected.getName() + "' thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadTrashTree();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        }
    }
}
