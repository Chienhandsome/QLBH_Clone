package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.congno.ChiTietCongNoNhaCungCapEntry;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import kazuko.ex.ui.common.ChonCotHienThiDialog;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuanLyCongNoNhaCungCapPanel extends JPanel {

    private final CongNoNhaCungCapDAO congNoDAO = new CongNoNhaCungCapDAO();
    private final NhomNhaCungCapDAO nhomNCCDAO = new NhomNhaCungCapDAO();

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Top Toolbar
    private JTextField txtTimKiem;
    private JButton btnRefresh;
    private JButton btnHienThiTatCa;
    private JButton btnThanhToan;
    private JButton btnInTongHop;
    private JLabel lblTongCongNo;

    private boolean showAll = true; // true: hiển thị tất cả NCC; false: chỉ hiện còn nợ <> 0

    // Left Category Tree & Bottom ComboBox
    private JTree treeNhomNCC;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private JComboBox<NhomNCCComboItem> cbNhomNCCBottom;
    private String selectedNhomNCCId = null;

    // Upper Table: Danh sách NCC & công nợ
    private JTable tableNCC;
    private DefaultTableModel modelNCC;
    private final List<CongNoNhaCungCapItem> listAllNCC = new ArrayList<>();
    private final List<CongNoNhaCungCapItem> listFilteredNCC = new ArrayList<>();
    private CongNoNhaCungCapItem currentSelectedNCC = null;

    // Lower Table: Sổ chi tiết công nợ NCC
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;

    public QuanLyCongNoNhaCungCapPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initComponents();
        setupEvents();
        loadTreeAndComboNhomNCC();
        loadDataCongNo();
    }

    private void initComponents() {
        // 1. Top Toolbar (Khớp ảnh Công nợ nhà cung cấp)
        JPanel pnlTop = new JPanel(new BorderLayout(8, 0));
        pnlTop.setBackground(new Color(235, 242, 252));
        pnlTop.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(180, 205, 235)),
                new EmptyBorder(5, 8, 5, 8)
        ));

        JPanel pnlTopLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTopLeft.setOpaque(false);

        JLabel lblTim = new JLabel("Lọc dữ liệu (F3)");
        lblTim.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTimKiem.setPreferredSize(new Dimension(160, 24));

        btnRefresh = createToolbarButton("Refresh", "arrow_refresh.png");
        btnHienThiTatCa = createToolbarButton("Hiển thị tất cả", "table.png");
        btnThanhToan = createToolbarButton("Thanh toán", "coins.png");
        btnInTongHop = createToolbarButton("In tổng hợp nợ", "printer.png");

        pnlTopLeft.add(lblTim);
        pnlTopLeft.add(txtTimKiem);
        pnlTopLeft.add(btnRefresh);
        pnlTopLeft.add(btnHienThiTatCa);
        pnlTopLeft.add(btnThanhToan);
        pnlTopLeft.add(btnInTongHop);

        JPanel pnlTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTopRight.setOpaque(false);

        JLabel lblTongTitle = new JLabel("TỔNG:");
        lblTongTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTongTitle.setForeground(new Color(20, 50, 95));

        lblTongCongNo = new JLabel("0 đ");
        lblTongCongNo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTongCongNo.setForeground(new Color(180, 20, 20));
        lblTongCongNo.setBackground(Color.WHITE);
        lblTongCongNo.setOpaque(true);
        lblTongCongNo.setBorder(new CompoundBorder(new LineBorder(new Color(185, 205, 230), 1), new EmptyBorder(2, 10, 2, 10)));

        pnlTopRight.add(lblTongTitle);
        pnlTopRight.add(lblTongCongNo);

        pnlTop.add(pnlTopLeft, BorderLayout.CENTER);
        pnlTop.add(pnlTopRight, BorderLayout.EAST);
        add(pnlTop, BorderLayout.NORTH);

        // 2. Main Split: Left (Category Tree) | Right (Upper & Lower Tables)
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(180);
        splitMain.setDividerSize(4);
        splitMain.setBorder(null);

        // Left Panel: Lọc dữ liệu (Tree & Bottom Combo)
        JPanel pnlLeft = createLeftFilterPanel();
        splitMain.setLeftComponent(pnlLeft);

        // Right Panel: Upper (Supplier Debt Table) & Lower (Ledger Table)
        JSplitPane splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitRight.setDividerLocation(380);
        splitRight.setDividerSize(5);
        splitRight.setBorder(null);

        JPanel pnlUpper = createUpperTablePanel();
        JPanel pnlLower = createLowerTablePanel();

        splitRight.setTopComponent(pnlUpper);
        splitRight.setBottomComponent(pnlLower);

        splitMain.setRightComponent(splitRight);
        add(splitMain, BorderLayout.CENTER);
    }

    private JPanel createLeftFilterPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new MatteBorder(0, 0, 0, 1, new Color(210, 225, 240)));

        // Header
        JPanel pnlHdr = new JPanel(new BorderLayout());
        pnlHdr.setBackground(new Color(240, 245, 252));
        pnlHdr.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(210, 225, 240)),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblHdr = new JLabel("Lọc dữ liệu");
        lblHdr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHdr.setForeground(new Color(20, 50, 95));

        JLabel lblGear = new JLabel(IconHelper.getIcon("cog.png", 14, 14));
        pnlHdr.add(lblHdr, BorderLayout.WEST);
        pnlHdr.add(lblGear, BorderLayout.EAST);
        pnl.add(pnlHdr, BorderLayout.NORTH);

        // Tree
        rootNode = new DefaultMutableTreeNode("Tất cả");
        treeModel = new DefaultTreeModel(rootNode);
        treeNhomNCC = new JTree(treeModel);
        treeNhomNCC.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomNCC.setRowHeight(20);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("bullet_blue.png", 12, 12));
        renderer.setOpenIcon(IconHelper.getIcon("folder_page.png", 14, 14));
        renderer.setClosedIcon(IconHelper.getIcon("folder.png", 14, 14));
        treeNhomNCC.setCellRenderer(renderer);

        JScrollPane sp = new JScrollPane(treeNhomNCC);
        sp.setBorder(null);
        pnl.add(sp, BorderLayout.CENTER);

        // Bottom Combo Box: Nhóm nhà cung cấp (Khớp góc trái dưới ảnh 1)
        JPanel pnlBottomCombo = new JPanel(new BorderLayout(4, 2));
        pnlBottomCombo.setBackground(new Color(245, 248, 253));
        pnlBottomCombo.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)),
                new EmptyBorder(4, 6, 4, 6)
        ));

        JLabel lblCombo = new JLabel("Nhóm nhà cung cấp");
        lblCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCombo.setForeground(new Color(60, 80, 110));

        cbNhomNCCBottom = new JComboBox<>();
        cbNhomNCCBottom.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        pnlBottomCombo.add(lblCombo, BorderLayout.NORTH);
        pnlBottomCombo.add(cbNhomNCCBottom, BorderLayout.CENTER);
        pnl.add(pnlBottomCombo, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createUpperTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Khớp thứ tự các cột trong ảnh: Mã nhà cung cấp, Tên nhà cung cấp, Địa chỉ, Điện thoại, Email, Còn nợ
        String[] cols = {
                "Mã nhà cung cấp", "Tên nhà cung cấp", "Địa chỉ", "Điện thoại", "Email", "Còn nợ"
        };

        modelNCC = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableNCC = new JTable(modelNCC);
        styleTable(tableNCC, true);
        ChonCotHienThiDialog.attachToTableHeader(tableNCC, List.of(cols));

        pnl.add(new JScrollPane(tableNCC), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createLowerTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        // Khớp thứ tự các cột bảng dưới trong ảnh: Số phiếu, Ngày, Tổng cộng, Diễn giải, Tiền thanh toán, Lũy kế
        String[] cols = {
                "Số phiếu", "Ngày", "Tổng cộng", "Diễn giải", "Tiền thanh toán", "Lũy kế"
        };

        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableChiTiet = new JTable(modelChiTiet);
        styleTable(tableChiTiet, false);
        ChonCotHienThiDialog.attachToTableHeader(tableChiTiet, List.of(cols));

        pnl.add(new JScrollPane(tableChiTiet), BorderLayout.CENTER);
        return pnl;
    }

    private void styleTable(JTable table, boolean isUpperTable) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 238, 248));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (isUpperTable) {
            table.setSelectionBackground(new Color(0, 120, 215));
            table.setSelectionForeground(Color.WHITE);
        } else {
            table.setSelectionBackground(new Color(210, 230, 252));
            table.setSelectionForeground(Color.BLACK);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(235, 242, 252));
        header.setForeground(new Color(40, 50, 70));
        header.setPreferredSize(new Dimension(100, 24));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        if (isUpperTable) {
            table.getColumnModel().getColumn(0).setPreferredWidth(120);
            table.getColumnModel().getColumn(1).setPreferredWidth(210);
            table.getColumnModel().getColumn(2).setPreferredWidth(180);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(110);
            table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        } else {
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(110);
            table.getColumnModel().getColumn(3).setPreferredWidth(220);
            table.getColumnModel().getColumn(4).setPreferredWidth(110);
            table.getColumnModel().getColumn(5).setPreferredWidth(110);
            table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        }
    }

    private JButton createToolbarButton(String text, String iconName) {
        JButton btn = new JButton(text, IconHelper.getIcon(iconName, 14, 14));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(180, 205, 235), 1), new EmptyBorder(3, 10, 3, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupEvents() {
        btnRefresh.addActionListener(e -> loadDataCongNo());

        btnHienThiTatCa.addActionListener(e -> {
            showAll = !showAll;
            btnHienThiTatCa.setText(showAll ? "Hiển thị tất cả" : "Chỉ NCC còn nợ");
            applyFilters();
        });

        btnThanhToan.addActionListener(e -> openThanhToanDialog());

        btnInTongHop.addActionListener(e -> exportOrPrintReport());

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilters(); }
        });

        treeNhomNCC.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) treeNhomNCC.getLastSelectedPathComponent();
            if (selected == null || selected.isRoot()) {
                selectedNhomNCCId = null;
                cbNhomNCCBottom.setSelectedIndex(0);
            } else {
                Object uo = selected.getUserObject();
                if (uo instanceof NhomNCCNode) {
                    selectedNhomNCCId = ((NhomNCCNode) uo).getId();
                    for (int i = 0; i < cbNhomNCCBottom.getItemCount(); i++) {
                        NhomNCCComboItem ci = cbNhomNCCBottom.getItemAt(i);
                        if (ci != null && selectedNhomNCCId.equals(ci.getId())) {
                            cbNhomNCCBottom.setSelectedIndex(i);
                            break;
                        }
                    }
                } else {
                    selectedNhomNCCId = null;
                }
            }
            applyFilters();
        });

        cbNhomNCCBottom.addActionListener(e -> {
            NhomNCCComboItem item = (NhomNCCComboItem) cbNhomNCCBottom.getSelectedItem();
            if (item != null) {
                selectedNhomNCCId = item.getId();
                applyFilters();
            }
        });

        tableNCC.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableNCC.getSelectedRow();
                if (row >= 0 && row < listFilteredNCC.size()) {
                    currentSelectedNCC = listFilteredNCC.get(row);
                    loadChiTietData();
                } else {
                    currentSelectedNCC = null;
                    modelChiTiet.setRowCount(0);
                }
            }
        });

        tableNCC.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openThanhToanDialog();
                }
            }
        });
    }

    private static class NhomNCCNode {
        private final String id;
        private final String name;
        public NhomNCCNode(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    private static class NhomNCCComboItem {
        private final String id;
        private final String name;
        public NhomNCCComboItem(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        @Override public String toString() { return name; }
    }

    private void loadTreeAndComboNhomNCC() {
        rootNode.removeAllChildren();
        cbNhomNCCBottom.removeAllItems();
        cbNhomNCCBottom.addItem(new NhomNCCComboItem(null, "-- Tất cả --"));

        try {
            List<NhomNhaCungCap> list = nhomNCCDAO.getAll();
            for (NhomNhaCungCap n : list) {
                rootNode.add(new DefaultMutableTreeNode(new NhomNCCNode(n.getId(), n.getName())));
                cbNhomNCCBottom.addItem(new NhomNCCComboItem(n.getId(), n.getName()));
            }
            treeModel.reload();
            treeNhomNCC.expandRow(0);
        } catch (Exception ex) {
            System.err.println("Lỗi loadTreeAndComboNhomNCC: " + ex.getMessage());
        }
    }

    public void loadDataCongNo() {
        listAllNCC.clear();
        listAllNCC.addAll(congNoDAO.getDanhSachCongNoNCC(null, null, false));
        applyFilters();
    }

    private void applyFilters() {
        listFilteredNCC.clear();
        String kw = txtTimKiem.getText().trim().toLowerCase();

        double totalNo = 0.0;
        for (CongNoNhaCungCapItem item : listAllNCC) {
            if (!showAll && Math.abs(item.getConNo()) < 0.0001) {
                continue;
            }

            if (selectedNhomNCCId != null && !selectedNhomNCCId.trim().isEmpty()) {
                if (item.getNhomNCCId() == null || !item.getNhomNCCId().trim().equalsIgnoreCase(selectedNhomNCCId.trim())) {
                    continue;
                }
            }

            if (!kw.isEmpty()) {
                boolean matchTen = item.getTenNCC() != null && item.getTenNCC().toLowerCase().contains(kw);
                boolean matchMa = item.getMaNCC() != null && item.getMaNCC().toLowerCase().contains(kw);
                boolean matchDt = item.getDienThoai() != null && item.getDienThoai().toLowerCase().contains(kw);
                boolean matchDc = item.getDiaChi() != null && item.getDiaChi().toLowerCase().contains(kw);
                if (!matchTen && !matchMa && !matchDt && !matchDc) continue;
            }

            listFilteredNCC.add(item);
            totalNo += item.getConNo();
        }

        modelNCC.setRowCount(0);
        for (CongNoNhaCungCapItem item : listFilteredNCC) {
            modelNCC.addRow(new Object[]{
                    item.getMaNCC() != null ? item.getMaNCC() : "",
                    item.getTenNCC() != null ? item.getTenNCC() : "",
                    item.getDiaChi() != null ? item.getDiaChi() : "",
                    item.getDienThoai() != null ? item.getDienThoai() : "",
                    item.getEmail() != null ? item.getEmail() : "",
                    formatNum(item.getConNo())
            });
        }

        lblTongCongNo.setText(formatNum(totalNo) + " đ");

        if (!listFilteredNCC.isEmpty()) {
            tableNCC.setRowSelectionInterval(0, 0);
        } else {
            currentSelectedNCC = null;
            modelChiTiet.setRowCount(0);
        }
    }

    private void loadChiTietData() {
        if (currentSelectedNCC == null) {
            modelChiTiet.setRowCount(0);
            return;
        }

        List<ChiTietCongNoNhaCungCapEntry> entries = congNoDAO.getChiTietCongNoNCC(currentSelectedNCC.getNhaCungCapId(), null, null);
        modelChiTiet.setRowCount(0);

        for (ChiTietCongNoNhaCungCapEntry e : entries) {
            modelChiTiet.addRow(new Object[]{
                    e.getSoPhieu(),
                    e.getNgay() != null ? sdfDate.format(e.getNgay()) : "",
                    formatNum(e.getTongCong()),
                    e.getDienGiai(),
                    formatNum(e.getTienThanhToan()),
                    formatNum(e.getLuyKe())
            });
        }
    }

    private void openThanhToanDialog() {
        if (currentSelectedNCC == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần trả nợ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        ThanhToanCongNoNCCDialog dlg = new ThanhToanCongNoNCCDialog(win, currentSelectedNCC);
        dlg.setVisible(true);

        if (dlg.isSuccess()) {
            loadDataCongNo();
        }
    }

    private void exportOrPrintReport() {
        if (listFilteredNCC.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu công nợ để xuất báo cáo!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu Báo cáo tổng hợp công nợ nhà cung cấp");
        fc.setSelectedFile(new File("BaoCaoCongNoNhaCungCap_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                pw.write('\ufeff'); // BOM cho tiếng Việt Unicode
                pw.println("STT,Mã NCC,Tên nhà cung cấp,Địa chỉ,Điện thoại,Email,Tổng tiền nhập,Tổng đã trả,Còn nợ");
                for (int i = 0; i < listFilteredNCC.size(); i++) {
                    CongNoNhaCungCapItem item = listFilteredNCC.get(i);
                    pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f\n",
                            i + 1,
                            item.getMaNCC() != null ? item.getMaNCC() : "",
                            item.getTenNCC() != null ? item.getTenNCC().replace("\"", "\"\"") : "",
                            item.getDiaChi() != null ? item.getDiaChi().replace("\"", "\"\"") : "",
                            item.getDienThoai() != null ? item.getDienThoai() : "",
                            item.getEmail() != null ? item.getEmail() : "",
                            item.getTongNhap(),
                            item.getTongDaTra(),
                            item.getConNo()
                    );
                }
                JOptionPane.showMessageDialog(this, "Xuất báo cáo công nợ NCC thành công vào file:\n" + f.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String formatNum(double val) {
        if (Math.abs(val - (long) val) < 0.0001) {
            return String.format("%,d", (long) val);
        }
        return dfMoney.format(val);
    }
}
