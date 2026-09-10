package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.dao.NhomKhachHangDAO;
import kazuko.ex.model.congno.ChiTietCongNoKhachHangEntry;
import kazuko.ex.model.congno.CongNoKhachHangItem;
import kazuko.ex.model.danhmuc.NhomKhachHang;
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

public class QuanLyCongNoKhachHangPanel extends JPanel {

    private final CongNoKhachHangDAO congNoDAO = new CongNoKhachHangDAO();
    private final NhomKhachHangDAO nhomKhachHangDAO = new NhomKhachHangDAO();

    private final DecimalFormat dfMoney = new DecimalFormat("#,##0.##");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Top Toolbar
    private JTextField txtTimKiem;
    private JButton btnRefresh;
    private JButton btnHienThiTatCa;
    private JButton btnThanhToan;
    private JButton btnInTongHop;
    private JLabel lblTongCongNo;

    private boolean showAll = true; // true: hiển thị tất cả khách; false: chỉ hiện còn nợ <> 0

    // Left Category Tree
    private JTree treeNhomKhach;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private String selectedNhomKhachId = null;

    // Upper Table: Danh sách khách hàng & công nợ
    private JTable tableKhachHang;
    private DefaultTableModel modelKhachHang;
    private final List<CongNoKhachHangItem> listAllKhach = new ArrayList<>();
    private final List<CongNoKhachHangItem> listFilteredKhach = new ArrayList<>();
    private CongNoKhachHangItem currentSelectedKhach = null;

    // Lower Table: Sổ chi tiết công nợ
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;
    private final List<ChiTietCongNoKhachHangEntry> listChiTiet = new ArrayList<>();

    public QuanLyCongNoKhachHangPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        initComponents();
        setupEvents();
        loadTreeNhomKhach();
        loadDataCongNo();
    }

    private void initComponents() {
        // 1. Top Toolbar (Khớp ảnh Công nợ khách hàng)
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

        // Left Panel: Lọc dữ liệu (Tree)
        JPanel pnlLeft = createLeftFilterPanel();
        splitMain.setLeftComponent(pnlLeft);

        // Right Panel: Upper (Customer Debt Table) & Lower (Ledger Table)
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
        treeNhomKhach = new JTree(treeModel);
        treeNhomKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        treeNhomKhach.setRowHeight(20);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("bullet_blue.png", 12, 12));
        renderer.setOpenIcon(IconHelper.getIcon("folder_page.png", 14, 14));
        renderer.setClosedIcon(IconHelper.getIcon("folder.png", 14, 14));
        treeNhomKhach.setCellRenderer(renderer);

        JScrollPane sp = new JScrollPane(treeNhomKhach);
        sp.setBorder(null);
        pnl.add(sp, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel createUpperTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        String[] cols = {
                "Tên khách hàng", "Mã khách", "Địa chỉ", "Điện thoại",
                "Email", "Mã số thuế", "Còn nợ"
        };

        modelKhachHang = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tableKhachHang = new JTable(modelKhachHang);
        styleTable(tableKhachHang, true);
        ChonCotHienThiDialog.attachToTableHeader(tableKhachHang, List.of(cols));

        pnl.add(new JScrollPane(tableKhachHang), BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createLowerTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        String[] cols = {
                "Số phiếu", "Ngày", "Tổng cộng", "Tiền thanh toán", "Diễn giải", "Lũy kế"
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
            table.getColumnModel().getColumn(0).setPreferredWidth(180);
            table.getColumnModel().getColumn(1).setPreferredWidth(90);
            table.getColumnModel().getColumn(2).setPreferredWidth(160);
            table.getColumnModel().getColumn(3).setPreferredWidth(100);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);
            table.getColumnModel().getColumn(5).setPreferredWidth(90);
            table.getColumnModel().getColumn(6).setPreferredWidth(110);
            table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        } else {
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(100);
            table.getColumnModel().getColumn(3).setPreferredWidth(110);
            table.getColumnModel().getColumn(4).setPreferredWidth(220);
            table.getColumnModel().getColumn(5).setPreferredWidth(110);
            table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
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
            btnHienThiTatCa.setText(showAll ? "Hiển thị tất cả" : "Chỉ khách còn nợ");
            applyFilters();
        });

        btnThanhToan.addActionListener(e -> openThanhToanDialog());

        btnInTongHop.addActionListener(e -> exportOrPrintReport());

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilters(); }
        });

        treeNhomKhach.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) treeNhomKhach.getLastSelectedPathComponent();
            if (selected == null || selected.isRoot()) {
                selectedNhomKhachId = null;
            } else {
                Object uo = selected.getUserObject();
                if (uo instanceof NhomKhachNode) {
                    selectedNhomKhachId = ((NhomKhachNode) uo).getId();
                } else {
                    selectedNhomKhachId = null;
                }
            }
            applyFilters();
        });

        tableKhachHang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableKhachHang.getSelectedRow();
                if (row >= 0 && row < listFilteredKhach.size()) {
                    currentSelectedKhach = listFilteredKhach.get(row);
                    loadChiTietData();
                } else {
                    currentSelectedKhach = null;
                    listChiTiet.clear();
                    modelChiTiet.setRowCount(0);
                }
            }
        });

        tableKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openThanhToanDialog();
                }
            }
        });

        JMenuItem miThanhToanDong = new JMenuItem(
                "Thanh toán dòng này", IconHelper.getIcon("coins.png", 14, 14));
        miThanhToanDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miThanhToanDong.addActionListener(e -> openThanhToanSelectedDetail());
        JPopupMenu quickPaymentMenu = new JPopupMenu();
        quickPaymentMenu.add(miThanhToanDong);

        tableChiTiet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = tableChiTiet.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        tableChiTiet.setRowSelectionInterval(row, row);
                        openThanhToanSelectedDetail();
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowQuickPaymentMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowQuickPaymentMenu(e);
            }

            private void maybeShowQuickPaymentMenu(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = tableChiTiet.rowAtPoint(e.getPoint());
                if (row >= 0 && row < tableChiTiet.getRowCount()) {
                    tableChiTiet.setRowSelectionInterval(row, row);
                } else {
                    tableChiTiet.clearSelection();
                }
                miThanhToanDong.setEnabled(isSelectedDetailPayable());
                quickPaymentMenu.show(tableChiTiet, e.getX(), e.getY());
            }
        });
    }

    private static class NhomKhachNode {
        private final String id;
        private final String name;
        public NhomKhachNode(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    private void loadTreeNhomKhach() {
        rootNode.removeAllChildren();
        try {
            List<NhomKhachHang> list = nhomKhachHangDAO.getAll();
            for (NhomKhachHang n : list) {
                rootNode.add(new DefaultMutableTreeNode(new NhomKhachNode(n.getId(), n.getName())));
            }
            treeModel.reload();
            treeNhomKhach.expandRow(0);
        } catch (Exception ex) {
            System.err.println("Lỗi loadTreeNhomKhach: " + ex.getMessage());
        }
    }

    public void loadDataCongNo() {
        listAllKhach.clear();
        listAllKhach.addAll(congNoDAO.getDanhSachCongNoKhachHang(null, null, false));
        applyFilters();
    }

    private void applyFilters() {
        listFilteredKhach.clear();
        String kw = txtTimKiem.getText().trim().toLowerCase();

        double totalNo = 0.0;
        for (CongNoKhachHangItem item : listAllKhach) {
            if (!showAll && Math.abs(item.getConNo()) < 0.0001) {
                continue;
            }

            if (selectedNhomKhachId != null && !selectedNhomKhachId.trim().isEmpty()) {
                if (item.getNhomKhachId() == null || !item.getNhomKhachId().trim().equalsIgnoreCase(selectedNhomKhachId.trim())) {
                    continue;
                }
            }

            if (!kw.isEmpty()) {
                boolean matchTen = item.getTenKhach() != null && item.getTenKhach().toLowerCase().contains(kw);
                boolean matchMa = item.getMaKhach() != null && item.getMaKhach().toLowerCase().contains(kw);
                boolean matchDt = item.getDienThoai() != null && item.getDienThoai().toLowerCase().contains(kw);
                boolean matchDc = item.getDiaChi() != null && item.getDiaChi().toLowerCase().contains(kw);
                if (!matchTen && !matchMa && !matchDt && !matchDc) continue;
            }

            listFilteredKhach.add(item);
            totalNo += item.getConNo();
        }

        modelKhachHang.setRowCount(0);
        for (CongNoKhachHangItem item : listFilteredKhach) {
            modelKhachHang.addRow(new Object[]{
                    item.getTenKhach() != null ? item.getTenKhach() : "",
                    item.getMaKhach() != null ? item.getMaKhach() : "",
                    item.getDiaChi() != null ? item.getDiaChi() : "",
                    item.getDienThoai() != null ? item.getDienThoai() : "",
                    item.getEmail() != null ? item.getEmail() : "",
                    item.getMaSoThue() != null ? item.getMaSoThue() : "",
                    formatNum(item.getConNo())
            });
        }

        lblTongCongNo.setText(formatNum(totalNo) + " đ");

        if (!listFilteredKhach.isEmpty()) {
            tableKhachHang.setRowSelectionInterval(0, 0);
        } else {
            currentSelectedKhach = null;
            listChiTiet.clear();
            modelChiTiet.setRowCount(0);
        }
    }

    private void loadChiTietData() {
        if (currentSelectedKhach == null) {
            listChiTiet.clear();
            modelChiTiet.setRowCount(0);
            return;
        }

        List<ChiTietCongNoKhachHangEntry> entries = congNoDAO.getChiTietCongNo(currentSelectedKhach.getKhachHangId(), null, null);
        listChiTiet.clear();
        listChiTiet.addAll(entries);
        modelChiTiet.setRowCount(0);

        for (ChiTietCongNoKhachHangEntry e : entries) {
            modelChiTiet.addRow(new Object[]{
                    e.getSoPhieu(),
                    e.getNgay() != null ? sdfDate.format(e.getNgay()) : "",
                    formatNum(e.getTongCong()),
                    formatNum(e.getTienThanhToan()),
                    e.getDienGiai(),
                    formatNum(e.getLuyKe())
            });
        }
    }

    private void openThanhToanDialog() {
        if (currentSelectedKhach == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần thu nợ!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Window win = SwingUtilities.getWindowAncestor(this);
        ThanhToanCongNoKhachDialog dlg = new ThanhToanCongNoKhachDialog(win, currentSelectedKhach);
        dlg.setVisible(true);

        if (dlg.isSuccess()) {
            loadDataCongNo();
        }
    }

    private boolean isSelectedDetailPayable() {
        int row = tableChiTiet != null ? tableChiTiet.getSelectedRow() : -1;
        if (row < 0 || row >= listChiTiet.size()) return false;
        ChiTietCongNoKhachHangEntry entry = listChiTiet.get(row);
        return "DONHANG".equals(entry.getLoaiChungTu()) && entry.getTongCong() > 0.0001;
    }

    private void openThanhToanSelectedDetail() {
        if (currentSelectedKhach == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần thu nợ!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = tableChiTiet.getSelectedRow();
        if (row < 0 || row >= listChiTiet.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng hóa đơn cần thanh toán!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChiTietCongNoKhachHangEntry entry = listChiTiet.get(row);
        if (!"DONHANG".equals(entry.getLoaiChungTu()) || entry.getTongCong() <= 0.0001) {
            JOptionPane.showMessageDialog(this,
                    "Dòng đã chọn không phải hóa đơn có giá trị cần thanh toán!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedCustomerId = currentSelectedKhach.getKhachHangId();
        Window win = SwingUtilities.getWindowAncestor(this);
        ThanhToanCongNoKhachDialog dlg = new ThanhToanCongNoKhachDialog(
                win, currentSelectedKhach, entry.getTongCong(), entry.getSoPhieu());
        dlg.setVisible(true);

        if (dlg.isSuccess()) {
            loadDataCongNo();
            restoreSelectedCustomer(selectedCustomerId);
        }
    }

    private void restoreSelectedCustomer(String customerId) {
        if (customerId == null) return;
        for (int i = 0; i < listFilteredKhach.size(); i++) {
            if (customerId.equals(listFilteredKhach.get(i).getKhachHangId())) {
                tableKhachHang.setRowSelectionInterval(i, i);
                tableKhachHang.scrollRectToVisible(tableKhachHang.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void exportOrPrintReport() {
        if (listFilteredKhach.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu công nợ để xuất báo cáo!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu Báo cáo tổng hợp công nợ khách hàng");
        fc.setSelectedFile(new File("BaoCaoCongNoKhachHang_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_8)) {
                pw.write('\ufeff'); // BOM cho tiếng Việt Unicode
                pw.println("STT,Mã khách,Tên khách hàng,Địa chỉ,Điện thoại,Email,Mã số thuế,Tổng tiền mua,Tổng đã trả,Còn nợ");
                for (int i = 0; i < listFilteredKhach.size(); i++) {
                    CongNoKhachHangItem item = listFilteredKhach.get(i);
                    pw.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f\n",
                            i + 1,
                            item.getMaKhach() != null ? item.getMaKhach() : "",
                            item.getTenKhach() != null ? item.getTenKhach().replace("\"", "\"\"") : "",
                            item.getDiaChi() != null ? item.getDiaChi().replace("\"", "\"\"") : "",
                            item.getDienThoai() != null ? item.getDienThoai() : "",
                            item.getEmail() != null ? item.getEmail() : "",
                            item.getMaSoThue() != null ? item.getMaSoThue() : "",
                            item.getTongMua(),
                            item.getTongDaTra(),
                            item.getConNo()
                    );
                }
                JOptionPane.showMessageDialog(this, "Xuất báo cáo công nợ thành công vào file:\n" + f.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
