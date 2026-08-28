package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class TimKiemMatHangDialog extends JDialog {

    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();

    private JTextField txtTim;
    private JTree treeNhomHang;
    private JTable tableMatHang;
    private DefaultTableModel matHangModel;

    private List<MatHang> allProducts = new ArrayList<>();
    private List<MatHang> filteredProducts = new ArrayList<>();
    private MatHang selectedMatHang = null;
    private boolean confirmed = false;
    private String currentSelectedGroup = "Tất cả";

    public TimKiemMatHangDialog(Window parent) {
        super(parent, "TÌM KIẾM", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadTreeData();
        loadProductsFromDb();
    }

    private void initComponents() {
        setSize(780, 520);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // 1. Header Bar: Magnifying glass icon & "Tìm kiếm"
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 240)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Find.png", 22, 22));
        JLabel lblTitle = new JLabel("Tìm kiếm");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(20, 40, 80));

        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center SplitPane: Left (Tree + Search) vs Right (Table)
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(220);
        split.setDividerSize(5);

        // Left Panel
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 4));
        pnlLeft.setBackground(new Color(230, 240, 252));
        pnlLeft.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel pnlTim = new JPanel(new BorderLayout(4, 0));
        pnlTim.setOpaque(false);
        JLabel lblTim = new JLabel("Tìm:");
        lblTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTim = new JTextField();
        txtTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterProducts(); }
            @Override public void removeUpdate(DocumentEvent e) { filterProducts(); }
            @Override public void changedUpdate(DocumentEvent e) { filterProducts(); }
        });
        pnlTim.add(lblTim, BorderLayout.WEST);
        pnlTim.add(txtTim, BorderLayout.CENTER);
        pnlLeft.add(pnlTim, BorderLayout.NORTH);

        treeNhomHang = new JTree();
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomHang.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeNhomHang.setRowHeight(20);

        DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
        treeRenderer.setLeafIcon(IconHelper.getIcon("121.png", 14, 14));
        treeRenderer.setOpenIcon(IconHelper.getIcon("Folder2.png", 14, 14));
        treeRenderer.setClosedIcon(IconHelper.getIcon("Folder.png", 14, 14));
        treeNhomHang.setCellRenderer(treeRenderer);

        treeNhomHang.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeNhomHang.getLastSelectedPathComponent();
            if (node != null) {
                currentSelectedGroup = node.getUserObject().toString();
                filterProducts();
            }
        });

        JScrollPane spTree = new JScrollPane(treeNhomHang);
        spTree.setBorder(new LineBorder(new Color(180, 205, 235), 1));
        pnlLeft.add(spTree, BorderLayout.CENTER);
        split.setLeftComponent(pnlLeft);

        // Right Panel
        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.setBackground(Color.WHITE);

        String[] headers = {"STT", "Mã hàng", "Tên mặt hàng", "Bảo hành"};
        matHangModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableMatHang = new JTable(matHangModel);
        tableMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMatHang.setRowHeight(22);
        tableMatHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMatHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableMatHang.getTableHeader().setBackground(new Color(230, 240, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        tableMatHang.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableMatHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableMatHang.getColumnModel().getColumn(1).setPreferredWidth(85);
        tableMatHang.getColumnModel().getColumn(2).setPreferredWidth(260);
        tableMatHang.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableMatHang.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        tableMatHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onConfirm();
                }
            }
        });

        tableMatHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    onConfirm();
                }
            }
        });

        JScrollPane spTable = new JScrollPane(tableMatHang);
        pnlRight.add(spTable, BorderLayout.CENTER);
        split.setRightComponent(pnlRight);

        add(split, BorderLayout.CENTER);

        // 3. Bottom Footer (Chọn, Hủy bỏ)
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(new Color(215, 230, 248));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 205, 235)));

        JButton btnChon = new JButton("Chọn");
        btnChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChon.setBackground(Color.WHITE);
        btnChon.setPreferredSize(new Dimension(80, 26));
        btnChon.addActionListener(e -> onConfirm());

        JButton btnHuyBo = new JButton("Hủy bỏ");
        btnHuyBo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHuyBo.setBackground(Color.WHITE);
        btnHuyBo.setPreferredSize(new Dimension(80, 26));
        btnHuyBo.addActionListener(e -> dispose());

        pnlFooter.add(btnChon);
        pnlFooter.add(btnHuyBo);
        add(pnlFooter, BorderLayout.SOUTH);

        // Shortcuts
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadTreeData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<NhomMatHang> groups = nhomMatHangDAO.getAll();
        for (NhomMatHang nh : groups) {
            if (nh.getStatus() >= 0 && nh.getName() != null && !nh.getName().trim().isEmpty()) {
                root.add(new DefaultMutableTreeNode(nh.getName().trim()));
            }
        }
        treeNhomHang.setModel(new DefaultTreeModel(root));
        treeNhomHang.setSelectionRow(0);
    }

    private void loadProductsFromDb() {
        allProducts = matHangDAO.getAll();
        filterProducts();
    }

    private void filterProducts() {
        String kw = txtTim != null ? txtTim.getText().trim().toLowerCase() : "";
        filteredProducts = new ArrayList<>();

        for (MatHang m : allProducts) {
            if (m.getStatus() < 0) continue;

            if (currentSelectedGroup != null && !currentSelectedGroup.equals("Tất cả")) {
                String grp = m.getTenNhom() != null ? m.getTenNhom().trim() : "";
                if (!grp.equalsIgnoreCase(currentSelectedGroup)) {
                    continue;
                }
            }

            if (!kw.isEmpty()) {
                String code = m.getCode() != null ? m.getCode().toLowerCase() : "";
                String name = m.getName() != null ? m.getName().toLowerCase() : "";
                String dvt = m.getTenDonViTinh() != null ? m.getTenDonViTinh().toLowerCase() : "";
                if (!code.contains(kw) && !name.contains(kw) && !dvt.contains(kw)) {
                    continue;
                }
            }

            filteredProducts.add(m);
        }

        populateTable();
    }

    private void populateTable() {
        matHangModel.setRowCount(0);
        int stt = 1;
        for (MatHang m : filteredProducts) {
            matHangModel.addRow(new Object[]{
                    String.format("%04d", stt++),
                    m.getCode() != null ? m.getCode() : "",
                    m.getName() != null ? m.getName() : "",
                    m.getBaoHanh() != null ? m.getBaoHanh() : ""
            });
        }

        if (tableMatHang.getRowCount() > 0) {
            tableMatHang.setRowSelectionInterval(0, 0);
        }
    }

    private void onConfirm() {
        int row = tableMatHang.getSelectedRow();
        if (row < 0 || row >= filteredProducts.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedMatHang = filteredProducts.get(row);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public MatHang getSelectedMatHang() { return selectedMatHang; }
}
