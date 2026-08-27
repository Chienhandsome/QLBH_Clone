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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KhoHangDialog extends JDialog {
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();

    private JTree treeKhoHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JComboBox<KhoHang> cbDefaultWarehouse;
    private boolean isUpdatingDefaultCb = false;

    public KhoHangDialog(Frame parent) {
        super(parent, "Kho hàng", true);
        initDialog(parent);
    }

    public KhoHangDialog(Dialog parent) {
        super(parent, "Kho hàng", true);
        initDialog(parent);
    }

    private void initDialog(Window parent) {
        initComponents();
        loadTreeData();
        loadDefaultWarehouseComboBox();
        setSize(480, 560);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Header Panel: Nền trắng + Icon report--pencil + Tiêu đề (Khớp 100% Ảnh 1)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("report--pencil.png", 32, 32));
        JLabel lblTitle = new JLabel("Cho phép bạn quản lý và chỉnh sửa danh mục kho hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 20, 20));

        headerPanel.add(lblIcon);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Panel: Toolbar + ComboBox Giá trị mặc định + JTree
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(4, 8, 8, 8));

        // Top Controls: Toolbar + Default Value
        JPanel topControls = new JPanel(new BorderLayout());
        topControls.setOpaque(false);

        // Toolbar: Thêm mới ▾, Xóa, Chỉnh sửa, Icon sort/tree, Thùng rác
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        toolBar.setBackground(new Color(218, 230, 245));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnAddMenu = createToolButton("Thêm mới ▾", "plus.png");
        JButton btnDelete = createToolButton("Xóa", "cross.png");
        JButton btnEdit = createToolButton("Chỉnh sửa", "pencil.png");
        JButton btnOrganize = createToolButton("", "folder-tree.png");
        JButton btnTrash = createToolButton("Thùng rác", "RecycleBin.png");

        btnAddMenu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddMenu.setForeground(new Color(0, 120, 50));
        btnDelete.setForeground(new Color(180, 20, 20));

        // Dropdown Popup Menu cho "Thêm mới ▾"
        JPopupMenu popupAdd = new JPopupMenu();
        JMenuItem miAddWarehouse = new JMenuItem("Thêm kho hàng", IconHelper.getIcon("Home.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miAddSeparator = new JMenuItem("Thêm phân cách", IconHelper.getIcon("ui-separator.png", 16, 16));

        miAddWarehouse.addActionListener(e -> onAddNewWarehouse());
        miAddFolder.addActionListener(e -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(e -> onAddNewFolder(true));
        miAddSeparator.addActionListener(e -> onAddSeparator());

        popupAdd.add(miAddWarehouse);
        popupAdd.add(miAddFolder);
        popupAdd.add(miAddSubFolder);
        popupAdd.add(miAddSeparator);

        btnAddMenu.addActionListener(e -> popupAdd.show(btnAddMenu, 0, btnAddMenu.getHeight()));

        btnDelete.addActionListener(e -> onDeleteSelected());
        btnEdit.addActionListener(e -> onEditSelected());
        btnTrash.addActionListener(e -> onOpenTrash());

        toolBar.add(btnAddMenu);
        toolBar.add(btnDelete);
        toolBar.add(btnEdit);
        toolBar.add(btnOrganize);
        toolBar.add(btnTrash);
        topControls.add(toolBar, BorderLayout.NORTH);

        // Sub Bar: Giá trị mặc định: [ K02 ▾ ]
        JPanel defaultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        defaultPanel.setOpaque(false);
        JLabel lblDefault = new JLabel("Giá trị mặc định:");
        lblDefault.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        defaultPanel.add(lblDefault);

        cbDefaultWarehouse = new JComboBox<>();
        cbDefaultWarehouse.setPreferredSize(new Dimension(220, 24));
        cbDefaultWarehouse.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDefaultWarehouse.setBackground(Color.WHITE);
        cbDefaultWarehouse.addActionListener(e -> onDefaultWarehouseChanged());
        defaultPanel.add(cbDefaultWarehouse);

        topControls.add(defaultPanel, BorderLayout.SOUTH);
        centerPanel.add(topControls, BorderLayout.NORTH);

        // Center View: JTree
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        treeKhoHang = new JTree(treeModel);
        treeKhoHang.setRootVisible(false);
        treeKhoHang.setShowsRootHandles(true);
        treeKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        treeKhoHang.setRowHeight(24);

        // Custom Cell Renderer hiển thị kho hàng khớp ảnh 1
        treeKhoHang.setCellRenderer(new DefaultTreeCellRenderer() {
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
                            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        } else if ("red_house".equals(k.getAvatarIcon())) {
                            label.setText(k.getName());
                            label.setIcon(KhoHangEditDialog.getStoreColoredIcon(new Color(220, 50, 50), 16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        } else if ("purple_house".equals(k.getAvatarIcon())) {
                            label.setText(k.getName());
                            label.setIcon(KhoHangEditDialog.getStoreColoredIcon(new Color(110, 80, 180), 16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        } else if ("lightbulb".equals(k.getAvatarIcon())) {
                            label.setText(k.getName());
                            label.setIcon(KhoHangEditDialog.getLightbulbIcon(16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        } else {
                            label.setText(k.getName());
                            label.setIcon(null);
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        }
                    }
                }
                return label;
            }
        });

        // Double click để mở form sửa
        treeKhoHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelected();
                }
            }
        });

        // Phím tắt bàn phím
        treeKhoHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onDeleteSelected();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    onAddNewWarehouse();
                } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                    onEditSelected();
                }
            }
        });

        // Bật tính năng Kéo & Thả (Drag & Drop)
        treeKhoHang.setDragEnabled(true);
        treeKhoHang.setDropMode(DropMode.ON);
        treeKhoHang.setTransferHandler(new TreeTransferHandler(khoHangDAO, () -> {
            loadTreeData();
            loadDefaultWarehouseComboBox();
        }));

        JScrollPane scrollPane = new JScrollPane(treeKhoHang);
        scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, new Color(180, 200, 225)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom: Nút Thoát
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton btnExit = new JButton("Thoát");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExit.setPreferredSize(new Dimension(85, 28));
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

    public void loadTreeData() {
        rootNode.removeAllChildren();
        List<KhoHang> warehouses = khoHangDAO.getAllActive();

        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Pass 1: Tạo TreeNode cho tất cả các phần tử
        for (KhoHang k : warehouses) {
            if (k.getId() != null) {
                nodeMap.put(k.getId(), new DefaultMutableTreeNode(k));
            }
        }

        // Pass 2: Gắn vào cây phân cấp theo parentId
        for (KhoHang k : warehouses) {
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
        for (int i = 0; i < treeKhoHang.getRowCount(); i++) {
            treeKhoHang.expandRow(i);
        }
    }

    private void loadDefaultWarehouseComboBox() {
        isUpdatingDefaultCb = true;
        cbDefaultWarehouse.removeAllItems();
        List<KhoHang> warehouses = khoHangDAO.getAllActive();
        String defaultId = khoHangDAO.getDefaultWarehouseId();
        KhoHang selectedItem = null;

        for (KhoHang k : warehouses) {
            if (!k.isFolder() && !k.isSeparator()) {
                cbDefaultWarehouse.addItem(k);
                if (k.getId().equals(defaultId)) {
                    selectedItem = k;
                }
            }
        }

        if (selectedItem != null) {
            cbDefaultWarehouse.setSelectedItem(selectedItem);
        }
        isUpdatingDefaultCb = false;
    }

    private void onDefaultWarehouseChanged() {
        if (isUpdatingDefaultCb) return;
        KhoHang selected = (KhoHang) cbDefaultWarehouse.getSelectedItem();
        if (selected != null) {
            khoHangDAO.setDefaultWarehouseId(selected.getId());
        }
    }

    private KhoHang getSelectedWarehouse() {
        TreePath path = treeKhoHang.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof KhoHang) {
                return (KhoHang) node.getUserObject();
            }
        }
        return null;
    }

    private void onAddNewWarehouse() {
        KhoHang selected = getSelectedWarehouse();
        KhoHang newWarehouse = new KhoHang();
        if (selected != null && selected.isFolder()) {
            newWarehouse.setParentId(selected.getId());
        }

        List<KhoHang> warehouses = khoHangDAO.getAllActive();
        KhoHangEditDialog editDialog = new KhoHangEditDialog(this, newWarehouse, warehouses, () -> {
            loadTreeData();
            loadDefaultWarehouseComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onAddNewFolder(boolean isSub) {
        String title = isSub ? "Thêm thư mục con" : "Thêm thư mục";
        String folderName = JOptionPane.showInputDialog(this, "Nhập tên thư mục:", title, JOptionPane.PLAIN_MESSAGE);
        if (folderName != null && !folderName.trim().isEmpty()) {
            KhoHang folder = new KhoHang();
            folder.setName(folderName.trim());
            folder.setFolder(true);
            folder.setAvatarIcon("Folder.png");

            KhoHang selected = getSelectedWarehouse();
            if (isSub && selected != null && selected.isFolder()) {
                folder.setParentId(selected.getId());
            }

            khoHangDAO.insert(folder);
            loadTreeData();
        }
    }

    private void onAddSeparator() {
        KhoHang sep = new KhoHang();
        sep.setName("----------------------------------------");
        sep.setSeparator(true);
        khoHangDAO.insert(sep);
        loadTreeData();
    }

    private void onEditSelected() {
        KhoHang selected = getSelectedWarehouse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kho hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.isSeparator()) return;

        if (selected.isFolder()) {
            String newName = JOptionPane.showInputDialog(this, "Đổi tên thư mục:", selected.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                selected.setName(newName.trim());
                khoHangDAO.update(selected);
                loadTreeData();
            }
            return;
        }

        List<KhoHang> warehouses = new ArrayList<>();
        for (KhoHang k : khoHangDAO.getAllActive()) {
            if (!k.isFolder() && !k.isSeparator()) {
                warehouses.add(k);
            }
        }

        KhoHangEditDialog editDialog = new KhoHangEditDialog(this, selected, warehouses, () -> {
            loadTreeData();
            loadDefaultWarehouseComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onDeleteSelected() {
        KhoHang selected = getSelectedWarehouse();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn kho hàng hoặc thư mục cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.isSeparator()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn chuyển '" + selected.getName() + "' vào Thùng rác không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (selected.getId() != null && selected.getId().equals(khoHangDAO.getDefaultWarehouseId())) {
                khoHangDAO.setDefaultWarehouseId(null);
            }
            boolean success = khoHangDAO.moveToTrash(selected.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã chuyển '" + selected.getName() + "' vào Thùng rác!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadTreeData();
            loadDefaultWarehouseComboBox();
        }
    }

    private void onOpenTrash() {
        KhoHangTrashDialog trashDialog = new KhoHangTrashDialog(this, () -> {
            loadTreeData();
            loadDefaultWarehouseComboBox();
        });
        trashDialog.setVisible(true);
    }

    private static class TreeTransferHandler extends TransferHandler {
        private final KhoHangDAO khoHangDAO;
        private final Runnable onDataChanged;
        private DefaultMutableTreeNode draggedNode;

        public TreeTransferHandler(KhoHangDAO khoHangDAO, Runnable onDataChanged) {
            this.khoHangDAO = khoHangDAO;
            this.onDataChanged = onDataChanged;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath path = tree.getSelectionPath();
            if (path == null) {
                return null;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node == null || !(node.getUserObject() instanceof KhoHang)) {
                return null;
            }
            draggedNode = node;
            KhoHang k = (KhoHang) node.getUserObject();
            return new StringSelection(k.getId());
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return false;
            }

            support.setShowDropLocation(true);
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            TreePath dropPath = dl.getPath();

            if (dropPath == null) {
                return true;
            }

            DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
            if (draggedNode == null || draggedNode == targetNode) {
                return false;
            }

            if (draggedNode.isNodeDescendant(targetNode)) {
                return false;
            }

            return true;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                String draggedId = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                if (draggedId == null || draggedId.trim().isEmpty()) {
                    return false;
                }

                JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
                TreePath dropPath = dl.getPath();

                String newParentId = "";
                if (dropPath != null) {
                    DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
                    Object targetUserObj = targetNode.getUserObject();
                    if (targetUserObj instanceof KhoHang) {
                        KhoHang targetItem = (KhoHang) targetUserObj;
                        if (targetItem.isFolder()) {
                            newParentId = targetItem.getId();
                        } else {
                            newParentId = targetItem.getParentId() != null ? targetItem.getParentId() : "";
                        }
                    }
                }

                boolean ok = khoHangDAO.updateParentId(draggedId, newParentId);
                if (ok) {
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                    JTree tree = (JTree) support.getComponent();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tree.getRowCount(); i++) {
                            TreePath p = tree.getPathForRow(i);
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
                            if (n.getUserObject() instanceof KhoHang) {
                                KhoHang k = (KhoHang) n.getUserObject();
                                if (draggedId.equals(k.getId())) {
                                    tree.setSelectionPath(p);
                                    tree.scrollPathToVisible(p);
                                    break;
                                }
                            }
                        }
                    });
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}
