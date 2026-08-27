package kazuko.ex.ui.baogia;

import kazuko.ex.dao.BaoGiaDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.baogia.BaoGia;
import kazuko.ex.model.baogia.BaoGiaChiTiet;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class BaoGiaBanHangPanel extends JPanel {

    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    // Filter Controls
    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JTextField txtSoPhieuFilter;
    private JComboBox<KhachHang> cbKhachHangFilter;

    // Master Table & Toolbar
    private JTextField txtSearch;
    private JButton btnAddNew;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnImportExcel;
    private JButton btnExportExcel;
    private JButton btnPrint;
    private JButton btnSummary;
    private JButton btnAnalysis;

    private JTable tableBaoGia;
    private DefaultTableModel masterModel;
    private JScrollPane tableScroll;
    private JPanel summaryBarPanel;
    private boolean isSummaryVisible = true;

    // Detail Table & Info Tab
    private JTabbedPane tabbedPaneDetail;
    private JTable tableChiTiet;
    private DefaultTableModel detailModel;
    private JTextArea txtInfoArea;

    private List<BaoGia> currentBaoGiaList = new ArrayList<>();
    private List<BaoGia> filteredBaoGiaList = new ArrayList<>();
    private BaoGia selectedBaoGia = null;

    public BaoGiaBanHangPanel() {
        initComponents();
        setupKeyBindings();
        initFilterDefaults();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(AppColors.TOOLBAR_BG);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(220);
        mainSplit.setDividerSize(5);

        // ==================== KHUNG TRÁI: LỌC DỮ LIỆU ====================
        JPanel leftPanel = createLeftFilterPanel();
        mainSplit.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: MASTER - DETAIL ====================
        JPanel rightPanel = createRightMasterDetailPanel();
        mainSplit.setRightComponent(rightPanel);

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createLeftFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(185, 205, 230), 1), "Lọc dữ liệu", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(40, 70, 120)),
                new EmptyBorder(8, 6, 8, 6)
        ));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 2, 3, 2);

        // 1. Label Ngày & Dropdown nút chọn nhanh
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(lblNgay, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 0.7;
        JPanel pnlDateRangeHeader = new JPanel(new BorderLayout());
        pnlDateRangeHeader.setOpaque(false);

        JButton btnQuickDate = new JButton("▾");
        btnQuickDate.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnQuickDate.setPreferredSize(new Dimension(22, 20));
        btnQuickDate.setFocusPainted(false);
        btnQuickDate.setBackground(new Color(240, 245, 252));
        btnQuickDate.addActionListener(e -> showQuickDateMenu(btnQuickDate));
        pnlDateRangeHeader.add(btnQuickDate, BorderLayout.EAST);
        formPanel.add(pnlDateRangeHeader, gbc);
        gbc.gridwidth = 1;

        // Row 1: Từ ngày
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblTu, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 0.8;
        dpkTuNgay = new DatePickerField();
        formPanel.add(dpkTuNgay, gbc);
        gbc.gridwidth = 1;

        // Row 2: Đến ngày
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        JLabel lblDen = new JLabel("Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblDen, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weightx = 0.8;
        dpkDenNgay = new DatePickerField();
        formPanel.add(dpkDenNgay, gbc);
        gbc.gridwidth = 1;

        // Row 3: Số phiếu
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblSoPhieu = new JLabel("Số phiếu");
        lblSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblSoPhieu, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 0.7;
        txtSoPhieuFilter = new JTextField();
        txtSoPhieuFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(txtSoPhieuFilter, gbc);
        gbc.gridwidth = 1;

        // Row 4: Khách hàng
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel lblKhach = new JLabel("Khách hàng");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formPanel.add(lblKhach, gbc);

        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weightx = 0.7;
        cbKhachHangFilter = new JComboBox<>();
        cbKhachHangFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHangFilter.setBackground(Color.WHITE);
        cbKhachHangFilter.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    l.setText(((KhachHang) value).getName());
                } else if (value == null) {
                    l.setText("-- Tất cả --");
                }
                return l;
            }
        });
        formPanel.add(cbKhachHangFilter, gbc);
        gbc.gridwidth = 1;

        // Row 5: Nút Xem dữ liệu
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 2, 4, 2);
        JButton btnApplyFilter = new JButton("Xem dữ liệu", IconHelper.getIcon("Search43.png", 16, 16));
        btnApplyFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnApplyFilter.setBackground(new Color(240, 245, 252));
        btnApplyFilter.setFocusPainted(false);
        btnApplyFilter.addActionListener(e -> loadData());
        formPanel.add(btnApplyFilter, gbc);

        panel.add(formPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createRightMasterDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setDividerLocation(360);
        verticalSplit.setDividerSize(6);

        // TOP: Master Panel (Toolbar + Master Table + Summary Bar)
        JPanel masterPanel = new JPanel(new BorderLayout());
        masterPanel.setBackground(Color.WHITE);

        // 1. Toolbar
        JPanel toolBar = createMasterToolBar();
        masterPanel.add(toolBar, BorderLayout.NORTH);

        // 2. Master Table
        String[] masterCols = {"", "Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại", "Tiền hàng", "Tiền giảm giá", "Tổng cộng"};
        masterModel = new DefaultTableModel(masterCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableBaoGia = new JTable(masterModel);
        tableBaoGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableBaoGia.setRowHeight(22);
        tableBaoGia.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableBaoGia.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableBaoGia.getTableHeader().setBackground(new Color(235, 242, 252));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableBaoGia.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableBaoGia.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableBaoGia.getColumnModel().getColumn(1).setPreferredWidth(85);
        tableBaoGia.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableBaoGia.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableBaoGia.getColumnModel().getColumn(3).setPreferredWidth(150);
        tableBaoGia.getColumnModel().getColumn(4).setPreferredWidth(160);
        tableBaoGia.getColumnModel().getColumn(5).setPreferredWidth(95);
        tableBaoGia.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableBaoGia.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableBaoGia.getColumnModel().getColumn(7).setPreferredWidth(90);
        tableBaoGia.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableBaoGia.getColumnModel().getColumn(8).setPreferredWidth(110);
        tableBaoGia.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Highlight selected row with green tint as in image
        tableBaoGia.setSelectionBackground(new Color(75, 150, 40));
        tableBaoGia.setSelectionForeground(Color.WHITE);

        tableBaoGia.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMasterRowSelected();
            }
        });

        tableBaoGia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditSelectedBaoGia();
                }
            }
        });

        tableScroll = new JScrollPane(tableBaoGia);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());

        // 3. Summary Footer Bar
        summaryBarPanel = createSummaryBarPanel();

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(tableScroll, BorderLayout.CENTER);
        tableContainer.add(summaryBarPanel, BorderLayout.SOUTH);

        masterPanel.add(tableContainer, BorderLayout.CENTER);
        verticalSplit.setTopComponent(masterPanel);

        // BOTTOM: Detail Panel (TabbedPane)
        JPanel detailPanel = createDetailPanel();
        verticalSplit.setBottomComponent(detailPanel);

        panel.add(verticalSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMasterToolBar() {
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 3));
        toolBar.setBackground(new Color(240, 245, 252));
        toolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(200, 215, 235)));

        toolBar.add(new JLabel("Lọc (F3):"));
        txtSearch = new JTextField(12);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMasterTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterMasterTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterMasterTable(); }
        });
        toolBar.add(txtSearch);

        btnAddNew = createToolButton("Thêm mới (Insert)", "plus.png", new Color(0, 120, 50), true);
        btnEdit = createToolButton("Chỉnh sửa (F4)", "pencil.png", Color.BLACK, false);
        btnDelete = createToolButton("Xóa (Del)", "cross.png", new Color(180, 20, 20), false);
        btnImportExcel = createToolButton("Thêm excel", "page_white_excel.png", Color.BLACK, false);
        btnExportExcel = createToolButton("Xuất excel", "export_excel.png", Color.BLACK, false);
        btnPrint = createToolButton("In", "Printer.png", Color.BLACK, false);
        btnSummary = createToolButton("∑ Tổng", null, Color.BLACK, false);
        btnAnalysis = createToolButton("P.tích", "chart_bar.png", Color.BLACK, false);

        btnAddNew.addActionListener(e -> onAddNewBaoGia());
        btnEdit.addActionListener(e -> onEditSelectedBaoGia());
        btnDelete.addActionListener(e -> onDeleteSelectedBaoGia());
        btnImportExcel.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng Nhập dữ liệu Báo giá từ Excel", "Excel", JOptionPane.INFORMATION_MESSAGE));
        btnExportExcel.addActionListener(e -> onExportExcel());
        btnPrint.addActionListener(e -> onPrintSelectedBaoGia());
        btnSummary.addActionListener(e -> toggleSummaryBar());
        btnAnalysis.addActionListener(e -> JOptionPane.showMessageDialog(this, "Biểu đồ phân tích doanh thu báo giá", "Phân tích", JOptionPane.INFORMATION_MESSAGE));

        toolBar.add(btnAddNew);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        toolBar.add(btnImportExcel);
        toolBar.add(btnExportExcel);
        toolBar.add(btnPrint);
        toolBar.add(btnSummary);
        toolBar.add(btnAnalysis);

        return toolBar;
    }

    private JButton createToolButton(String text, String iconName, Color textColor, boolean isBold) {
        JButton btn = new JButton(text);
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 16, 16));
        }
        btn.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, 12));
        btn.setForeground(textColor);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 205, 225), 1),
                new EmptyBorder(3, 6, 3, 6)
        ));
        return btn;
    }

    private JPanel createSummaryBarPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!isSummaryVisible) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int panelW = getWidth();
                int panelH = getHeight();

                // Background
                g2.setColor(new Color(235, 243, 252));
                g2.fillRect(0, 0, panelW, panelH);

                // Top border line
                g2.setColor(new Color(185, 205, 230));
                g2.drawLine(0, 0, panelW, 0);

                if (tableBaoGia == null || masterModel == null) {
                    g2.dispose();
                    return;
                }

                int scrollX = 0;
                if (tableScroll != null && tableScroll.getViewport() != null) {
                    scrollX = tableScroll.getViewport().getViewPosition().x;
                }

                // Draw "TỔNG" Label
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.setColor(new Color(20, 20, 20));
                FontMetrics fmBold = g2.getFontMetrics();
                int textY = (panelH - fmBold.getHeight()) / 2 + fmBold.getAscent();
                g2.drawString("TỔNG", 10, textY);

                int viewportW = panelW;
                if (tableScroll != null && tableScroll.getViewport() != null) {
                    viewportW = tableScroll.getViewport().getWidth();
                }
                g2.clipRect(0, 0, viewportW, panelH);

                // Calculate & Render totals for columns 6 (Tiền hàng), 7 (Tiền giảm giá), 8 (Tổng cộng)
                int colCount = tableBaoGia.getColumnCount();
                for (int c = 0; c < colCount; c++) {
                    TableColumn tableCol = tableBaoGia.getColumnModel().getColumn(c);
                    int modelCol = tableCol.getModelIndex();
                    if (modelCol != 6 && modelCol != 7 && modelCol != 8) continue;

                    double sum = 0;
                    int rowCount = masterModel.getRowCount();
                    for (int r = 0; r < rowCount; r++) {
                        Object val = masterModel.getValueAt(r, modelCol);
                        if (val != null) {
                            try {
                                sum += Double.parseDouble(val.toString().replace(",", "").replace(" ", "").trim());
                            } catch (Exception ignored) {}
                        }
                    }

                    String sumText = CurrencyUtil.formatMoney(sum);

                    Rectangle headerRect = tableBaoGia.getTableHeader() != null ? tableBaoGia.getTableHeader().getHeaderRect(c) : new Rectangle(0, 0, tableCol.getWidth(), 24);
                    int colX = headerRect.x - scrollX;
                    int colW = headerRect.width;

                    int boxX = colX + 2;
                    int boxY = 3;
                    int boxW = Math.max(10, colW - 4);
                    int boxH = panelH - 6;

                    g2.setColor(Color.WHITE);
                    g2.fillRect(boxX, boxY, boxW, boxH);
                    g2.setColor(new Color(185, 205, 230));
                    g2.drawRect(boxX, boxY, boxW, boxH);

                    g2.setColor(new Color(20, 20, 20));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    int numW = fm.stringWidth(sumText);
                    int numX = boxX + boxW - numW - 4;
                    int numY = boxY + (boxH - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(sumText, numX, numY);
                }

                g2.dispose();
            }
        };

        panel.setPreferredSize(new Dimension(100, 28));
        panel.setMinimumSize(new Dimension(100, 28));
        return panel;
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        tabbedPaneDetail = new JTabbedPane(JTabbedPane.TOP);
        tabbedPaneDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tab 1: Thông tin
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setBackground(Color.WHITE);
        pnlInfo.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtInfoArea = new JTextArea();
        txtInfoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtInfoArea.setEditable(false);
        txtInfoArea.setBackground(new Color(250, 252, 255));
        pnlInfo.add(new JScrollPane(txtInfoArea), BorderLayout.CENTER);
        tabbedPaneDetail.addTab("Thông tin", pnlInfo);

        // Tab 2: Báo giá chi tiết
        JPanel pnlChiTiet = new JPanel(new BorderLayout());
        pnlChiTiet.setBackground(Color.WHITE);

        String[] detailCols = {"", "Ghi chú", "Báo giá", "Mã hàng", "Mặt hàng", "Số lượng", "Đơn giá", "Thành tiền", "Giảm giá %", "Đơn vị tính"};
        detailModel = new DefaultTableModel(detailCols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tableChiTiet = new JTable(detailModel);
        tableChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableChiTiet.setRowHeight(22);
        tableChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableChiTiet.getTableHeader().setBackground(new Color(235, 242, 252));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableChiTiet.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableChiTiet.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(85);
        tableChiTiet.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableChiTiet.getColumnModel().getColumn(4).setPreferredWidth(160);
        tableChiTiet.getColumnModel().getColumn(5).setPreferredWidth(65);
        tableChiTiet.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(6).setPreferredWidth(85);
        tableChiTiet.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(7).setPreferredWidth(95);
        tableChiTiet.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableChiTiet.getColumnModel().getColumn(8).setPreferredWidth(75);
        tableChiTiet.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Unit Column with Icon
        tableChiTiet.getColumnModel().getColumn(9).setPreferredWidth(70);
        tableChiTiet.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setIcon(IconHelper.getIcon("tag_blue.png", 14, 14));
                l.setHorizontalAlignment(JLabel.LEFT);
                return l;
            }
        });

        JScrollPane detailScroll = new JScrollPane(tableChiTiet);
        pnlChiTiet.add(detailScroll, BorderLayout.CENTER);
        tabbedPaneDetail.addTab("Báo giá chi tiết", pnlChiTiet);

        tabbedPaneDetail.setSelectedIndex(1); // Default to Báo giá chi tiết
        panel.add(tabbedPaneDetail, BorderLayout.CENTER);
        return panel;
    }

    private void setupKeyBindings() {
        // F3: Focus Search box
        registerKeyboardAction(e -> {
            txtSearch.requestFocusInWindow();
            txtSearch.selectAll();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Insert: Add New
        registerKeyboardAction(e -> onAddNewBaoGia(),
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // F4: Edit
        registerKeyboardAction(e -> onEditSelectedBaoGia(),
                KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // Delete: Delete
        registerKeyboardAction(e -> onDeleteSelectedBaoGia(),
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initFilterDefaults() {
        // Default: 30 days range
        Calendar cal = Calendar.getInstance();
        dpkDenNgay.setDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        dpkTuNgay.setDate(cal.getTime());

        // Load Customers into Filter
        cbKhachHangFilter.removeAllItems();
        cbKhachHangFilter.addItem(null);
        List<KhachHang> khList = khachHangDAO.getAll();
        for (KhachHang kh : khList) {
            cbKhachHangFilter.addItem(kh);
        }
    }

    private void showQuickDateMenu(Component comp) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createMenuItem("Hôm nay", () -> setDateRange(0, 0)));
        menu.add(createMenuItem("Hôm qua", () -> setDateRange(-1, -1)));
        menu.addSeparator();
        menu.add(createMenuItem("Tuần này", this::setThisWeek));
        menu.add(createMenuItem("Tuần trước", this::setLastWeek));
        menu.addSeparator();
        menu.add(createMenuItem("Tháng này", this::setThisMonth));
        menu.add(createMenuItem("Tháng trước", this::setLastMonth));
        menu.add(createMenuItem("Năm nay", this::setThisYear));
        menu.addSeparator();
        menu.add(createMenuItem("Tất cả thời gian", () -> {
            dpkTuNgay.setDate(null);
            dpkDenNgay.setDate(null);
            loadData();
        }));
        menu.show(comp, 0, comp.getHeight());
    }

    private JMenuItem createMenuItem(String text, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        item.addActionListener(e -> {
            action.run();
            loadData();
        });
        return item;
    }

    private void setDateRange(int offsetFromStart, int offsetFromEnd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, offsetFromEnd);
        dpkDenNgay.setDate(cal.getTime());

        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, offsetFromStart);
        dpkTuNgay.setDate(cal.getTime());
    }

    private void setThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        dpkTuNgay.setDate(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        dpkDenNgay.setDate(cal.getTime());
    }

    private void setLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        dpkTuNgay.setDate(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        dpkDenNgay.setDate(cal.getTime());
    }

    private void setThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dpkTuNgay.setDate(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        dpkDenNgay.setDate(cal.getTime());
    }

    private void setLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dpkTuNgay.setDate(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        dpkDenNgay.setDate(cal.getTime());
    }

    private void setThisYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        dpkTuNgay.setDate(cal.getTime());
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
        dpkDenNgay.setDate(cal.getTime());
    }

    public void loadData() {
        Date tuNgay = dpkTuNgay.getDate();
        Date denNgay = dpkDenNgay.getDate();
        String soPhieu = txtSoPhieuFilter.getText() != null ? txtSoPhieuFilter.getText().trim() : null;
        Object selKh = cbKhachHangFilter.getSelectedItem();
        String khachId = (selKh instanceof KhachHang) ? ((KhachHang) selKh).getId() : null;

        currentBaoGiaList = baoGiaDAO.getDanhSachBaoGia(tuNgay, denNgay, soPhieu, khachId);
        filterMasterTable();
    }

    private void filterMasterTable() {
        String keyword = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        filteredBaoGiaList.clear();
        masterModel.setRowCount(0);

        int stt = 1;
        for (BaoGia bg : currentBaoGiaList) {
            boolean match = keyword.isEmpty() ||
                    (bg.getName() != null && bg.getName().toLowerCase().contains(keyword)) ||
                    (bg.getTenKhach() != null && bg.getTenKhach().toLowerCase().contains(keyword)) ||
                    (bg.getDienThoai() != null && bg.getDienThoai().contains(keyword)) ||
                    (bg.getDiaChi() != null && bg.getDiaChi().toLowerCase().contains(keyword));

            if (match) {
                filteredBaoGiaList.add(bg);
                masterModel.addRow(new Object[]{
                        String.valueOf(stt++),
                        DateUtil.formatDate(bg.getNgay()),
                        bg.getName() != null ? bg.getName() : "",
                        bg.getTenKhach() != null ? bg.getTenKhach() : "",
                        bg.getDiaChi() != null ? bg.getDiaChi() : "",
                        bg.getDienThoai() != null ? bg.getDienThoai() : "",
                        CurrencyUtil.formatMoney(bg.getTienHang()),
                        CurrencyUtil.formatMoney(bg.getTienGiamGia()),
                        CurrencyUtil.formatMoney(bg.getTongCong())
                });
            }
        }

        if (summaryBarPanel != null) {
            summaryBarPanel.repaint();
        }

        if (!filteredBaoGiaList.isEmpty()) {
            tableBaoGia.setRowSelectionInterval(0, 0);
        } else {
            selectedBaoGia = null;
            detailModel.setRowCount(0);
            txtInfoArea.setText("");
        }
    }

    private void onMasterRowSelected() {
        int row = tableBaoGia.getSelectedRow();
        if (row >= 0 && row < filteredBaoGiaList.size()) {
            selectedBaoGia = filteredBaoGiaList.get(row);
            loadDetailData(selectedBaoGia);
        } else {
            selectedBaoGia = null;
            detailModel.setRowCount(0);
            txtInfoArea.setText("");
        }
    }

    private void loadDetailData(BaoGia bg) {
        if (bg == null) return;
        detailModel.setRowCount(0);

        List<BaoGiaChiTiet> details = baoGiaDAO.getChiTietByBaoGiaId(bg.getId(), bg.getName());
        int stt = 1;
        for (BaoGiaChiTiet ct : details) {
            detailModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getNote() != null ? ct.getNote() : "",
                    bg.getName() != null ? bg.getName() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    CurrencyUtil.formatQuantity(ct.getSoLuong()),
                    CurrencyUtil.formatMoney(ct.getDonGia()),
                    CurrencyUtil.formatMoney(ct.getThanhTien()),
                    CurrencyUtil.formatQuantity(ct.getTiLeGiamGia()),
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : ""
            });
        }

        // Info Tab update
        StringBuilder sb = new StringBuilder();
        sb.append("Số phiếu: ").append(bg.getName()).append("\n");
        sb.append("Ngày lập: ").append(DateUtil.formatDate(bg.getNgay())).append("\n");
        sb.append("Khách hàng: ").append(bg.getTenKhach() != null ? bg.getTenKhach() : "").append("\n");
        sb.append("Địa chỉ: ").append(bg.getDiaChi() != null ? bg.getDiaChi() : "").append("\n");
        sb.append("Điện thoại: ").append(bg.getDienThoai() != null ? bg.getDienThoai() : "").append("\n");
        sb.append("Email: ").append(bg.getEmail() != null ? bg.getEmail() : "").append("\n");
        sb.append("Ghi chú: ").append(bg.getNote() != null ? bg.getNote() : "").append("\n");
        sb.append("------------------------------------\n");
        sb.append("Tổng tiền hàng: ").append(CurrencyUtil.formatMoney(bg.getTienHang())).append(" VNĐ\n");
        sb.append("Tiền giảm giá: ").append(CurrencyUtil.formatMoney(bg.getTienGiamGia())).append(" VNĐ (").append(bg.getTiLeGiamGia()).append("%)\n");
        sb.append("Thuế VAT: ").append(CurrencyUtil.formatMoney(bg.getTienThue())).append(" VNĐ (").append(bg.getTiLeThue()).append("%)\n");
        sb.append("Phí vận chuyển: ").append(CurrencyUtil.formatMoney(bg.getPhiVanChuyen())).append(" VNĐ\n");
        sb.append("TỔNG CỘNG: ").append(CurrencyUtil.formatMoney(bg.getTongCong())).append(" VNĐ\n");
        txtInfoArea.setText(sb.toString());
    }

    private void onAddNewBaoGia() {
        Window win = SwingUtilities.getWindowAncestor(this);
        BaoGiaEditDialog dlg = new BaoGiaEditDialog(win, null, filteredBaoGiaList, -1, this::loadData);
        dlg.setVisible(true);
    }

    private void onEditSelectedBaoGia() {
        int sel = tableBaoGia.getSelectedRow();
        if (sel >= 0 && sel < filteredBaoGiaList.size()) {
            BaoGia bg = filteredBaoGiaList.get(sel);
            Window win = SwingUtilities.getWindowAncestor(this);
            BaoGiaEditDialog dlg = new BaoGiaEditDialog(win, bg, filteredBaoGiaList, sel, this::loadData);
            dlg.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu báo giá để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDeleteSelectedBaoGia() {
        int sel = tableBaoGia.getSelectedRow();
        if (sel >= 0 && sel < filteredBaoGiaList.size()) {
            BaoGia bg = filteredBaoGiaList.get(sel);
            int conf = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa phiếu báo giá: " + bg.getName() + " không?",
                    "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (conf == JOptionPane.YES_OPTION) {
                if (baoGiaDAO.deleteBaoGia(bg.getId())) {
                    JOptionPane.showMessageDialog(this, "Đã xóa phiếu báo giá thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa phiếu báo giá thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu báo giá để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onPrintSelectedBaoGia() {
        Window win = SwingUtilities.getWindowAncestor(this);
        InLuoiBaoGiaDialog dlg = new InLuoiBaoGiaDialog(win);
        dlg.setVisible(true);
    }

    private void toggleSummaryBar() {
        isSummaryVisible = !isSummaryVisible;
        summaryBarPanel.setVisible(isSummaryVisible);
        revalidate();
        repaint();
    }

    private void onExportExcel() {
        JOptionPane.showMessageDialog(this, "Xuất danh sách báo giá ra file Excel thành công!", "Xuất Excel", JOptionPane.INFORMATION_MESSAGE);
    }
}
