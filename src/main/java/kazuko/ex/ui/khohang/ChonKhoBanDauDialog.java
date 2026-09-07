package kazuko.ex.ui.khohang;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ChonKhoBanDauDialog extends JDialog {

    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private JTree treeKho;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    private boolean confirmed = false;
    private KhoHang selectedKho = null;

    public ChonKhoBanDauDialog(Window parent) {
        super(parent, "Chọn dữ liệu", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadWarehouses();
        setSize(390, 420);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Top Header Label: MỜI BẠN CHỌN KHO
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlHeader.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("MỜI BẠN CHỌN KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(20, 30, 50));
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center: Tree Kho Hàng
        rootNode = new DefaultMutableTreeNode("ROOT");
        treeModel = new DefaultTreeModel(rootNode);
        treeKho = new JTree(treeModel);
        treeKho.setRootVisible(false);
        treeKho.setShowsRootHandles(true);
        treeKho.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        treeKho.setRowHeight(26);

        // Custom Tree Cell Renderer matching Screenshot 1
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObj instanceof KhoNode) {
                        KhoNode node = (KhoNode) userObj;
                        lbl.setText(node.getName());
                        if (node.getIcon() != null) {
                            lbl.setIcon(node.getIcon());
                        }
                    }
                }
                if (sel) {
                    lbl.setBackground(new Color(0, 120, 215));
                    lbl.setForeground(Color.WHITE);
                    lbl.setOpaque(true);
                } else {
                    lbl.setOpaque(false);
                    lbl.setForeground(new Color(30, 30, 30));
                }
                return lbl;
            }
        };
        treeKho.setCellRenderer(renderer);

        // Double click to confirm
        treeKho.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onAccept();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(treeKho);
        scrollPane.setBorder(new CompoundBorder(
                new EmptyBorder(0, 12, 8, 12),
                new LineBorder(new Color(190, 210, 235), 1)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Panel: Chấp nhận + Hủy bỏ
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setBackground(new Color(218, 232, 247));
        pnlBottom.setBorder(new LineBorder(new Color(195, 215, 238), 1));

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAccept.setIcon(IconHelper.getIcon("accept.png", 16, 16));
        btnAccept.setBackground(Color.WHITE);
        btnAccept.setFocusPainted(false);
        btnAccept.setPreferredSize(new Dimension(110, 28));
        btnAccept.addActionListener(e -> onAccept());

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setIcon(IconHelper.getIcon("cancel.png", 16, 16));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(95, 28));
        btnCancel.addActionListener(e -> dispose());

        pnlBottom.add(btnAccept);
        pnlBottom.add(btnCancel);
        add(pnlBottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnAccept);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public static class KhoNode {
        private final KhoHang kho;
        private final Icon icon;

        public KhoNode(KhoHang kho, Icon icon) {
            this.kho = kho;
            this.icon = icon;
        }

        public KhoHang getKho() { return kho; }
        public String getName() { return (kho != null) ? kho.getName() : ""; }
        public Icon getIcon() { return icon; }

        @Override
        public String toString() { return getName(); }
    }

    private void loadWarehouses() {
        rootNode.removeAllChildren();
        List<KhoHang> list = khoHangDAO.getAllActive();

        DefaultMutableTreeNode firstNode = null;
        for (KhoHang k : list) {
            Icon icon;
            String nameLower = (k.getName() != null) ? k.getName().toLowerCase() : "";
            if (nameLower.contains("tổng")) {
                icon = IconHelper.getIcon("lightbulb.png", 16, 16);
            } else if (nameLower.contains("chi nhánh") || nameLower.contains("bán hàng")) {
                icon = IconHelper.getIcon("house.png", 16, 16);
            } else {
                icon = IconHelper.getIcon("building.png", 16, 16);
            }
            if (icon == null) {
                icon = IconHelper.getIcon("bullet_blue.png", 16, 16);
            }

            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new KhoNode(k, icon));
            rootNode.add(node);
            if (firstNode == null) {
                firstNode = node;
            }
        }

        treeModel.reload();
        for (int i = 0; i < treeKho.getRowCount(); i++) {
            treeKho.expandRow(i);
        }

        if (firstNode != null) {
            treeKho.setSelectionPath(new TreePath(firstNode.getPath()));
        }
    }

    private void onAccept() {
        TreePath path = treeKho.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node != null && node.getUserObject() instanceof KhoNode) {
                selectedKho = ((KhoNode) node.getUserObject()).getKho();
            }
        }

        if (selectedKho == null) {
            List<KhoHang> list = khoHangDAO.getAllActive();
            if (!list.isEmpty()) {
                selectedKho = list.get(0);
            }
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public KhoHang getSelectedKho() {
        return selectedKho;
    }
}
