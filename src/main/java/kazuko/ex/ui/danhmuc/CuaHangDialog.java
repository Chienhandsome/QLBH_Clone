package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CuaHangDialog extends JDialog {
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();

    private JTree treeCuaHang;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JComboBox<CuaHang> cbDefaultStore;
    private boolean isUpdatingDefaultCb = false;

    public CuaHangDialog(Frame parent) {
        super(parent, "Cửa hàng", true);
        initComponents();
        loadTreeData();
        loadDefaultStoreComboBox();
        setSize(480, 560);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 1. Header Panel: Nền trắng + Icon report--pencil + Tiêu đề
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 235)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("report--pencil.png", 32, 32));
        JLabel lblTitle = new JLabel("Cho phép bạn quản lý và chỉnh sửa danh mục cửa hàng");
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

        // Toolbar: Thêm mới ▾, Xóa, Chỉnh sửa, Icon sort, Thùng rác
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

        // Dropdown Popup Menu cho "Thêm mới ▾" (theo ảnh 2)
        JPopupMenu popupAdd = new JPopupMenu();
        JMenuItem miAddStore = new JMenuItem("Thêm cửa hàng", IconHelper.getIcon("1282529873_objects.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miAddSeparator = new JMenuItem("Thêm phân cách", IconHelper.getIcon("ui-separator.png", 16, 16));

        miAddStore.addActionListener(e -> onAddNewStore());
        miAddFolder.addActionListener(e -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(e -> onAddNewFolder(true));
        miAddSeparator.addActionListener(e -> onAddSeparator());

        popupAdd.add(miAddStore);
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

        // Sub Bar: Giá trị mặc định: [ TEST ▾ ]
        JPanel defaultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        defaultPanel.setOpaque(false);
        JLabel lblDefault = new JLabel("Giá trị mặc định:");
        lblDefault.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        defaultPanel.add(lblDefault);

        cbDefaultStore = new JComboBox<>();
        cbDefaultStore.setPreferredSize(new Dimension(220, 24));
        cbDefaultStore.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDefaultStore.setBackground(Color.WHITE);
        cbDefaultStore.addActionListener(e -> onDefaultStoreChanged());
        defaultPanel.add(cbDefaultStore);

        topControls.add(defaultPanel, BorderLayout.SOUTH);
        centerPanel.add(topControls, BorderLayout.NORTH);

        // Center View: JTree
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        treeCuaHang = new JTree(treeModel);
        treeCuaHang.setRootVisible(false);
        treeCuaHang.setShowsRootHandles(true);
        treeCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        treeCuaHang.setRowHeight(24);

        // Custom Cell Renderer hiển thị icon chi nhánh, trụ sở, thư mục
        treeCuaHang.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof CuaHang) {
                        CuaHang ch = (CuaHang) userObj;
                        if (ch.isSeparator()) {
                            label.setText("----------------------------------------------");
                            label.setForeground(Color.LIGHT_GRAY);
                            label.setIcon(null);
                        } else if (ch.isFolder()) {
                            label.setText(ch.getName());
                            label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        } else if ("headquarter".equalsIgnoreCase(ch.getIconName())) {
                            label.setText(ch.getName());
                            label.setIcon(IconHelper.getIcon("globe-network.png", 16, 16));
                            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        } else {
                            label.setText(ch.getName());
                            label.setIcon(IconHelper.getIcon("1282529873_objects.png", 16, 16));
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        }
                    }
                }
                return label;
            }
        });

        // Double click để mở form sửa
        treeCuaHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(treeCuaHang);
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
        List<CuaHang> stores = cuaHangDAO.getAllActive();

        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Add root / folder nodes
        for (CuaHang ch : stores) {
            if (ch.getParentId() == null || ch.getParentId().isEmpty()) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(ch);
                nodeMap.put(ch.getId(), node);
                rootNode.add(node);
            }
        }

        // Add child nodes
        for (CuaHang ch : stores) {
            if (ch.getParentId() != null && !ch.getParentId().isEmpty()) {
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
        for (int i = 0; i < treeCuaHang.getRowCount(); i++) {
            treeCuaHang.expandRow(i);
        }
    }

    private void loadDefaultStoreComboBox() {
        isUpdatingDefaultCb = true;
        cbDefaultStore.removeAllItems();
        List<CuaHang> stores = cuaHangDAO.getAllActive();
        String defaultId = cuaHangDAO.getDefaultStoreId();
        CuaHang selectedItem = null;

        for (CuaHang ch : stores) {
            if (!ch.isFolder() && !ch.isSeparator()) {
                cbDefaultStore.addItem(ch);
                if (ch.getId().equals(defaultId)) {
                    selectedItem = ch;
                }
            }
        }

        if (selectedItem != null) {
            cbDefaultStore.setSelectedItem(selectedItem);
        }
        isUpdatingDefaultCb = false;
    }

    private void onDefaultStoreChanged() {
        if (isUpdatingDefaultCb) return;
        CuaHang selected = (CuaHang) cbDefaultStore.getSelectedItem();
        if (selected != null) {
            cuaHangDAO.setDefaultStoreId(selected.getId());
        }
    }

    private CuaHang getSelectedStore() {
        TreePath path = treeCuaHang.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof CuaHang) {
                return (CuaHang) node.getUserObject();
            }
        }
        return null;
    }

    private void onAddNewStore() {
        CuaHang selected = getSelectedStore();
        CuaHang newStore = new CuaHang();
        if (selected != null && selected.isFolder()) {
            newStore.setParentId(selected.getId());
        }
        newStore.setMauHoaDon("Mẫu 54 mm x 2 dòng");

        List<CuaHang> stores = cuaHangDAO.getAllActive();
        CuaHangEditDialog editDialog = new CuaHangEditDialog(this, newStore, stores, () -> {
            loadTreeData();
            loadDefaultStoreComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onAddNewFolder(boolean isSub) {
        String title = isSub ? "Thêm thư mục con" : "Thêm thư mục";
        String folderName = JOptionPane.showInputDialog(this, "Nhập tên thư mục:", title, JOptionPane.PLAIN_MESSAGE);
        if (folderName != null && !folderName.trim().isEmpty()) {
            CuaHang folder = new CuaHang();
            folder.setName(folderName.trim());
            folder.setFolder(true);
            folder.setIconName("folder");

            CuaHang selected = getSelectedStore();
            if (isSub && selected != null && selected.isFolder()) {
                folder.setParentId(selected.getId());
            }

            cuaHangDAO.insert(folder);
            loadTreeData();
        }
    }

    private void onAddSeparator() {
        CuaHang sep = new CuaHang();
        sep.setSeparator(true);
        cuaHangDAO.insert(sep);
        loadTreeData();
    }

    private void onEditSelected() {
        CuaHang selected = getSelectedStore();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn cửa hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.isSeparator()) return;

        if (selected.isFolder()) {
            String newName = JOptionPane.showInputDialog(this, "Đổi tên thư mục:", selected.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                selected.setName(newName.trim());
                cuaHangDAO.update(selected);
                loadTreeData();
            }
            return;
        }

        List<CuaHang> stores = new ArrayList<>();
        for (CuaHang ch : cuaHangDAO.getAllActive()) {
            if (!ch.isFolder() && !ch.isSeparator()) {
                stores.add(ch);
            }
        }

        CuaHangEditDialog editDialog = new CuaHangEditDialog(this, selected, stores, () -> {
            loadTreeData();
            loadDefaultStoreComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onDeleteSelected() {
        CuaHang selected = getSelectedStore();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn cửa hàng hoặc thư mục cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
            if (selected.getId() != null && selected.getId().equals(cuaHangDAO.getDefaultStoreId())) {
                cuaHangDAO.setDefaultStoreId(null);
            }
            boolean success = cuaHangDAO.moveToTrash(selected.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã chuyển '" + selected.getName() + "' vào Thùng rác!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadTreeData();
            loadDefaultStoreComboBox();
        }
    }

    private void onOpenTrash() {
        CuaHangTrashDialog trashDialog = new CuaHangTrashDialog(this, () -> {
            loadTreeData();
            loadDefaultStoreComboBox();
        });
        trashDialog.setVisible(true);
    }
}
