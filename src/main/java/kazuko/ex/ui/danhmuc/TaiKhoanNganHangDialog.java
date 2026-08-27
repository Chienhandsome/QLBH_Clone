package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
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

public class TaiKhoanNganHangDialog extends JDialog {
    private final TaiKhoanNganHangDAO taiKhoanNganHangDAO = new TaiKhoanNganHangDAO();

    private JTree treeTaiKhoan;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JComboBox<TaiKhoanNganHang> cbDefaultAccount;
    private boolean isUpdatingDefaultCb = false;

    public TaiKhoanNganHangDialog(Frame parent) {
        super(parent, "Tài khoản ngân hàng", true);
        initDialog(parent);
    }

    public TaiKhoanNganHangDialog(Dialog parent) {
        super(parent, "Tài khoản ngân hàng", true);
        initDialog(parent);
    }

    private void initDialog(Window parent) {
        initComponents();
        loadTreeData();
        loadDefaultAccountComboBox();
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
        JLabel lblTitle = new JLabel("Cho phép bạn quản lý và chỉnh sửa danh mục tài khoản ngân hàng");
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
        JMenuItem miAddAccount = new JMenuItem("Thêm tài khoản ngân hàng", IconHelper.getIcon("database1.png", 16, 16));
        JMenuItem miAddFolder = new JMenuItem("Thêm thư mục", IconHelper.getIcon("Folder.png", 16, 16));
        JMenuItem miAddSubFolder = new JMenuItem("Thêm thư mục con", IconHelper.getIcon("folder-tree.png", 16, 16));
        JMenuItem miAddSeparator = new JMenuItem("Thêm phân cách", IconHelper.getIcon("ui-separator.png", 16, 16));

        miAddAccount.addActionListener(e -> onAddNewAccount());
        miAddFolder.addActionListener(e -> onAddNewFolder(false));
        miAddSubFolder.addActionListener(e -> onAddNewFolder(true));
        miAddSeparator.addActionListener(e -> onAddSeparator());

        popupAdd.add(miAddAccount);
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

        // Sub Bar: Giá trị mặc định: [ tk 1 ▾ ]
        JPanel defaultPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        defaultPanel.setOpaque(false);
        JLabel lblDefault = new JLabel("Giá trị mặc định:");
        lblDefault.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        defaultPanel.add(lblDefault);

        cbDefaultAccount = new JComboBox<>();
        cbDefaultAccount.setPreferredSize(new Dimension(220, 24));
        cbDefaultAccount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbDefaultAccount.setBackground(Color.WHITE);
        cbDefaultAccount.addActionListener(e -> onDefaultAccountChanged());
        defaultPanel.add(cbDefaultAccount);

        topControls.add(defaultPanel, BorderLayout.SOUTH);
        centerPanel.add(topControls, BorderLayout.NORTH);

        // Center View: JTree
        rootNode = new DefaultMutableTreeNode("Root");
        treeModel = new DefaultTreeModel(rootNode);
        treeTaiKhoan = new JTree(treeModel);
        treeTaiKhoan.setRootVisible(false);
        treeTaiKhoan.setShowsRootHandles(true);
        treeTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        treeTaiKhoan.setRowHeight(24);

        // Custom Cell Renderer hiển thị tài khoản ngân hàng khớp ảnh 1
        treeTaiKhoan.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof TaiKhoanNganHang) {
                        TaiKhoanNganHang tk = (TaiKhoanNganHang) userObj;
                        if (tk.isSeparator()) {
                            label.setText("----------------------------------------------");
                            label.setForeground(Color.LIGHT_GRAY);
                            label.setIcon(null);
                        } else if (tk.isFolder()) {
                            label.setText(tk.getName());
                            label.setIcon(IconHelper.getIcon("Folder.png", 16, 16));
                            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                        } else {
                            label.setText(tk.getName());
                            label.setIcon(null);
                            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        }
                    }
                }
                return label;
            }
        });

        // Double click để mở form sửa
        treeTaiKhoan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelected();
                }
            }
        });

        // Phím tắt bàn phím
        treeTaiKhoan.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    onDeleteSelected();
                } else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
                    onAddNewAccount();
                } else if (e.getKeyCode() == KeyEvent.VK_F4) {
                    onEditSelected();
                }
            }
        });

        // Bật tính năng Kéo & Thả (Drag & Drop)
        treeTaiKhoan.setDragEnabled(true);
        treeTaiKhoan.setDropMode(DropMode.ON);
        treeTaiKhoan.setTransferHandler(new TreeTransferHandler(taiKhoanNganHangDAO, () -> {
            loadTreeData();
            loadDefaultAccountComboBox();
        }));

        JScrollPane scrollPane = new JScrollPane(treeTaiKhoan);
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
        List<TaiKhoanNganHang> accounts = taiKhoanNganHangDAO.getAllActive();

        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();

        // Pass 1: Tạo TreeNode cho tất cả các phần tử
        for (TaiKhoanNganHang tk : accounts) {
            if (tk.getId() != null) {
                nodeMap.put(tk.getId(), new DefaultMutableTreeNode(tk));
            }
        }

        // Pass 2: Gắn vào cây phân cấp theo parentId
        for (TaiKhoanNganHang tk : accounts) {
            DefaultMutableTreeNode currentNode = nodeMap.get(tk.getId());
            if (currentNode == null) continue;

            String parentId = tk.getParentId();
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
        for (int i = 0; i < treeTaiKhoan.getRowCount(); i++) {
            treeTaiKhoan.expandRow(i);
        }
    }

    private void loadDefaultAccountComboBox() {
        isUpdatingDefaultCb = true;
        cbDefaultAccount.removeAllItems();
        List<TaiKhoanNganHang> accounts = taiKhoanNganHangDAO.getAllActive();
        String defaultId = taiKhoanNganHangDAO.getDefaultAccountId();
        TaiKhoanNganHang selectedItem = null;

        for (TaiKhoanNganHang tk : accounts) {
            if (!tk.isFolder() && !tk.isSeparator()) {
                cbDefaultAccount.addItem(tk);
                if (tk.getId().equals(defaultId)) {
                    selectedItem = tk;
                }
            }
        }

        if (selectedItem != null) {
            cbDefaultAccount.setSelectedItem(selectedItem);
        }
        isUpdatingDefaultCb = false;
    }

    private void onDefaultAccountChanged() {
        if (isUpdatingDefaultCb) return;
        TaiKhoanNganHang selected = (TaiKhoanNganHang) cbDefaultAccount.getSelectedItem();
        if (selected != null) {
            taiKhoanNganHangDAO.setDefaultAccountId(selected.getId());
        }
    }

    private TaiKhoanNganHang getSelectedAccount() {
        TreePath path = treeTaiKhoan.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof TaiKhoanNganHang) {
                return (TaiKhoanNganHang) node.getUserObject();
            }
        }
        return null;
    }

    private void onAddNewAccount() {
        TaiKhoanNganHang selected = getSelectedAccount();
        TaiKhoanNganHang newAccount = new TaiKhoanNganHang();
        if (selected != null && selected.isFolder()) {
            newAccount.setParentId(selected.getId());
        }

        List<TaiKhoanNganHang> accounts = taiKhoanNganHangDAO.getAllActive();
        TaiKhoanNganHangEditDialog editDialog = new TaiKhoanNganHangEditDialog(this, newAccount, accounts, () -> {
            loadTreeData();
            loadDefaultAccountComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onAddNewFolder(boolean isSub) {
        String title = isSub ? "Thêm thư mục con" : "Thêm thư mục";
        String folderName = JOptionPane.showInputDialog(this, "Nhập tên thư mục:", title, JOptionPane.PLAIN_MESSAGE);
        if (folderName != null && !folderName.trim().isEmpty()) {
            TaiKhoanNganHang folder = new TaiKhoanNganHang();
            folder.setName(folderName.trim());
            folder.setFolder(true);
            folder.setAvatarIcon("Folder.png");

            TaiKhoanNganHang selected = getSelectedAccount();
            if (isSub && selected != null && selected.isFolder()) {
                folder.setParentId(selected.getId());
            }

            taiKhoanNganHangDAO.insert(folder);
            loadTreeData();
        }
    }

    private void onAddSeparator() {
        TaiKhoanNganHang sep = new TaiKhoanNganHang();
        sep.setName("----------------------------------------");
        sep.setSeparator(true);
        taiKhoanNganHangDAO.insert(sep);
        loadTreeData();
    }

    private void onEditSelected() {
        TaiKhoanNganHang selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản ngân hàng cần chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.isSeparator()) return;

        if (selected.isFolder()) {
            String newName = JOptionPane.showInputDialog(this, "Đổi tên thư mục:", selected.getName());
            if (newName != null && !newName.trim().isEmpty()) {
                selected.setName(newName.trim());
                taiKhoanNganHangDAO.update(selected);
                loadTreeData();
            }
            return;
        }

        List<TaiKhoanNganHang> accounts = new ArrayList<>();
        for (TaiKhoanNganHang tk : taiKhoanNganHangDAO.getAllActive()) {
            if (!tk.isFolder() && !tk.isSeparator()) {
                accounts.add(tk);
            }
        }

        TaiKhoanNganHangEditDialog editDialog = new TaiKhoanNganHangEditDialog(this, selected, accounts, () -> {
            loadTreeData();
            loadDefaultAccountComboBox();
        });
        editDialog.setVisible(true);
    }

    private void onDeleteSelected() {
        TaiKhoanNganHang selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản ngân hàng hoặc thư mục cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
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
            if (selected.getId() != null && selected.getId().equals(taiKhoanNganHangDAO.getDefaultAccountId())) {
                taiKhoanNganHangDAO.setDefaultAccountId(null);
            }
            boolean success = taiKhoanNganHangDAO.moveToTrash(selected.getId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã chuyển '" + selected.getName() + "' vào Thùng rác!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            loadTreeData();
            loadDefaultAccountComboBox();
        }
    }

    private void onOpenTrash() {
        TaiKhoanNganHangTrashDialog trashDialog = new TaiKhoanNganHangTrashDialog(this, () -> {
            loadTreeData();
            loadDefaultAccountComboBox();
        });
        trashDialog.setVisible(true);
    }

    private static class TreeTransferHandler extends TransferHandler {
        private final TaiKhoanNganHangDAO taiKhoanNganHangDAO;
        private final Runnable onDataChanged;
        private DefaultMutableTreeNode draggedNode;

        public TreeTransferHandler(TaiKhoanNganHangDAO taiKhoanNganHangDAO, Runnable onDataChanged) {
            this.taiKhoanNganHangDAO = taiKhoanNganHangDAO;
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
            if (node == null || !(node.getUserObject() instanceof TaiKhoanNganHang)) {
                return null;
            }
            draggedNode = node;
            TaiKhoanNganHang tk = (TaiKhoanNganHang) node.getUserObject();
            return new StringSelection(tk.getId());
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
                    if (targetUserObj instanceof TaiKhoanNganHang) {
                        TaiKhoanNganHang targetItem = (TaiKhoanNganHang) targetUserObj;
                        if (targetItem.isFolder()) {
                            newParentId = targetItem.getId();
                        } else {
                            newParentId = targetItem.getParentId() != null ? targetItem.getParentId() : "";
                        }
                    }
                }

                boolean ok = taiKhoanNganHangDAO.updateParentId(draggedId, newParentId);
                if (ok) {
                    if (onDataChanged != null) {
                        onDataChanged.run();
                    }
                    JTree tree = (JTree) support.getComponent();
                    SwingUtilities.invokeLater(() -> {
                        for (int i = 0; i < tree.getRowCount(); i++) {
                            TreePath p = tree.getPathForRow(i);
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p.getLastPathComponent();
                            if (n.getUserObject() instanceof TaiKhoanNganHang) {
                                TaiKhoanNganHang tk = (TaiKhoanNganHang) n.getUserObject();
                                if (draggedId.equals(tk.getId())) {
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
