package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class TimKiemKhachDatHangDialog extends JDialog {

    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JTextField txtSoPhieu;
    private JComboBox<KhachHang> cbKhachHang;
    private JTextField txtLocF3;

    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private JTable tableDetail;
    private DefaultTableModel detailModel;
    private JTabbedPane tabbedDetails;

    private List<KhachDatHang> allOrdersList = new ArrayList<>();
    private List<KhachDatHang> filteredOrdersList = new ArrayList<>();
    private KhachDatHang selectedOrder = null;
    private boolean confirmed = false;

    public TimKiemKhachDatHangDialog(Window parent) {
        super(parent, "TÌM KIẾM", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadKhachHangComboBox();
        searchDataFromDb();
    }

    private void initComponents() {
        setSize(960, 580);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Center SplitPane: Left Sidebar (Lọc dữ liệu) vs Right Data Area (Dữ liệu)
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitMain.setDividerLocation(230);
        splitMain.setDividerSize(5);

        splitMain.setLeftComponent(createLeftFilterPanel());
        splitMain.setRightComponent(createRightDataPanel());

        add(splitMain, BorderLayout.CENTER);

        // Bottom Footer Bar
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(new Color(210, 228, 248));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 205, 235)));

        JButton btnChon = new JButton("Chọn");
        btnChon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChon.setBackground(Color.WHITE);
        btnChon.setPreferredSize(new Dimension(80, 26));
        btnChon.addActionListener(e -> onConfirmSelection());

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setPreferredSize(new Dimension(80, 26));
        btnThoat.addActionListener(e -> dispose());

        pnlFooter.add(btnChon);
        pnlFooter.add(btnThoat);
        add(pnlFooter, BorderLayout.SOUTH);

        // Key bindings
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f3Key = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        getRootPane().registerKeyboardAction(e -> txtLocF3.requestFocusInWindow(), f3Key, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JPanel createLeftFilterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(215, 230, 245));

        // Header Tab
        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlTab.setBackground(Color.WHITE);
        pnlTab.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JLabel lblTitle = new JLabel("Lọc dữ liệu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlTab.add(lblTitle);

        panel.add(pnlTab, BorderLayout.NORTH);

        // Filter Form
        JPanel pnlForm = new JPanel(null);
        pnlForm.setBackground(new Color(215, 230, 245));
        pnlForm.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgay.setBounds(8, 8, 50, 20);
        pnlForm.add(lblNgay);

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTu.setBounds(8, 32, 30, 22);
        pnlForm.add(lblTu);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setDate(cal.getTime());
        dpkTuNgay.setBounds(40, 32, 165, 24);
        dpkTuNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDataFromDb(); }
        });
        pnlForm.add(dpkTuNgay);

        JLabel lblDen = new JLabel("Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDen.setBounds(8, 62, 30, 22);
        pnlForm.add(lblDen);

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.setBounds(40, 62, 165, 24);
        dpkDenNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDataFromDb(); }
        });
        pnlForm.add(dpkDenNgay);

        JLabel lblSoPhieu = new JLabel("Số phiếu");
        lblSoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSoPhieu.setBounds(8, 96, 70, 20);
        pnlForm.add(lblSoPhieu);

        txtSoPhieu = new JTextField();
        txtSoPhieu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSoPhieu.setBounds(8, 118, 197, 24);
        txtSoPhieu.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void removeUpdate(DocumentEvent e) { searchDataFromDb(); }
            @Override public void changedUpdate(DocumentEvent e) { searchDataFromDb(); }
        });
        pnlForm.add(txtSoPhieu);

        JLabel lblKhach = new JLabel("Khách hàng");
        lblKhach.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKhach.setBounds(8, 150, 90, 20);
        pnlForm.add(lblKhach);

        cbKhachHang = new JComboBox<>();
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.setBounds(8, 172, 197, 24);
        cbKhachHang.addActionListener(e -> searchDataFromDb());
        pnlForm.add(cbKhachHang);

        panel.add(pnlForm, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightDataPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Top bar: "Dữ liệu" tab & "Lọc (F3):"
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(230, 240, 252));
        pnlTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JPanel pnlTabTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTabTitle.setBackground(Color.WHITE);
        pnlTabTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(190, 210, 235)));
        JLabel lblDuLieu = new JLabel("Dữ liệu");
        lblDuLieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlTabTitle.add(lblDuLieu);
        pnlTop.add(pnlTabTitle, BorderLayout.WEST);

        JPanel pnlSearchF3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        pnlSearchF3.setOpaque(false);
        JLabel lblLoc = new JLabel("Lọc (F3):");
        lblLoc.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtLocF3 = new JTextField(18);
        txtLocF3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLocF3.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterTableData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterTableData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterTableData(); }
        });
        pnlSearchF3.add(lblLoc);
        pnlSearchF3.add(txtLocF3);
        pnlTop.add(pnlSearchF3, BorderLayout.CENTER);

        panel.add(pnlTop, BorderLayout.NORTH);

        // Center Split: Master Table vs Tabbed Details
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(200);
        split.setDividerSize(5);

        // 1. Master Table
        String[] masterHeaders = {"STT", "Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại", "Email", "Tổng cộng"};
        masterModel = new DefaultTableModel(masterHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableMaster = new JTable(masterModel);
        tableMaster.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMaster.setRowHeight(22);
        tableMaster.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMaster.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableMaster.getTableHeader().setBackground(new Color(230, 240, 252));

        tableMaster.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableMaster.getColumnModel().getColumn(1).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(2).setPreferredWidth(95);
        tableMaster.getColumnModel().getColumn(3).setPreferredWidth(140);
        tableMaster.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableMaster.getColumnModel().getColumn(5).setPreferredWidth(90);
        tableMaster.getColumnModel().getColumn(6).setPreferredWidth(90);
        tableMaster.getColumnModel().getColumn(7).setPreferredWidth(95);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableMaster.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMasterSelectionChanged();
            }
        });

        tableMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onConfirmSelection();
                }
            }
        });

        tableMaster.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    onConfirmSelection();
                }
            }
        });

        JScrollPane spMaster = new JScrollPane(tableMaster);
        split.setTopComponent(spMaster);

        // 2. Tabbed Details Pane
        tabbedDetails = new JTabbedPane();
        tabbedDetails.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab: Đặt hàng chi tiết
        JPanel pnlDetail = new JPanel(new BorderLayout());
        pnlDetail.setBackground(Color.WHITE);

        String[] detailHeaders = {"STT", "Mặt hàng", "Đơn vị tính", "Số lượng", "Đơn giá", "Giảm giá %", "Thành tiền", "Ghi chú", "Mã hàng"};
        detailModel = new DefaultTableModel(detailHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableDetail = new JTable(detailModel);
        tableDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDetail.setRowHeight(22);
        tableDetail.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDetail.getTableHeader().setBackground(new Color(230, 240, 252));

        tableDetail.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableDetail.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableDetail.getColumnModel().getColumn(2).setPreferredWidth(60);
        tableDetail.getColumnModel().getColumn(3).setPreferredWidth(60);
        tableDetail.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(5).setPreferredWidth(70);
        tableDetail.getColumnModel().getColumn(6).setPreferredWidth(85);
        tableDetail.getColumnModel().getColumn(7).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(8).setPreferredWidth(75);

        tableDetail.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane spDetail = new JScrollPane(tableDetail);
        pnlDetail.add(spDetail, BorderLayout.CENTER);

        tabbedDetails.addTab("Thông tin", createPlaceholderPanel("Thông tin đơn đặt hàng"));
        tabbedDetails.addTab("Đặt hàng chi tiết", pnlDetail);
        tabbedDetails.addTab("Đặt hàng nhà cung cấp", createPlaceholderPanel("Đặt hàng nhà cung cấp"));
        tabbedDetails.addTab("Đơn hàng", createPlaceholderPanel("Đơn hàng"));
        tabbedDetails.addTab("Sản xuất", createPlaceholderPanel("Sản xuất"));
        tabbedDetails.addTab("Phiếu thu", createPlaceholderPanel("Phiếu thu"));
        tabbedDetails.addTab("Phiếu thu công nợ", createPlaceholderPanel("Phiếu thu công nợ"));

        tabbedDetails.setSelectedIndex(1); // Mặc định mở tab "Đặt hàng chi tiết"

        split.setBottomComponent(tabbedDetails);
        panel.add(split, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.add(new JLabel("Không có dữ liệu " + title));
        return pnl;
    }

    private void loadKhachHangComboBox() {
        cbKhachHang.removeAllItems();
        KhachHang allKh = new KhachHang();
        allKh.setName("-- Tất cả khách hàng --");
        cbKhachHang.addItem(allKh);

        List<KhachHang> list = khachHangDAO.getAll();
        for (KhachHang kh : list) {
            if (kh.getStatus() >= 0) {
                cbKhachHang.addItem(kh);
            }
        }
    }

    private void searchDataFromDb() {
        Date tuNgay = dpkTuNgay != null ? dpkTuNgay.getDate() : null;
        Date denNgay = dpkDenNgay != null ? dpkDenNgay.getDate() : null;
        String soPhieu = (txtSoPhieu != null && !txtSoPhieu.getText().trim().isEmpty()) ? txtSoPhieu.getText().trim() : null;

        String khachId = null;
        if (cbKhachHang != null && cbKhachHang.getSelectedIndex() > 0) {
            KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
            if (kh != null) khachId = kh.getId();
        }

        allOrdersList = khachDatHangDAO.getDanhSachKhachDatHang(tuNgay, denNgay, soPhieu, khachId, null);
        filterTableData();
    }

    private void filterTableData() {
        String kw = txtLocF3 != null ? txtLocF3.getText().trim().toLowerCase() : "";
        filteredOrdersList = new ArrayList<>();
        for (KhachDatHang o : allOrdersList) {
            if (kw.isEmpty()) {
                filteredOrdersList.add(o);
                continue;
            }
            String name = o.getName() != null ? o.getName().toLowerCase() : "";
            String khach = o.getTenKhach() != null ? o.getTenKhach().toLowerCase() : "";
            String sdt = o.getDienThoai() != null ? o.getDienThoai().toLowerCase() : "";
            String email = o.getEmail() != null ? o.getEmail().toLowerCase() : "";
            String dc = o.getDiaChi() != null ? o.getDiaChi().toLowerCase() : "";
            if (name.contains(kw) || khach.contains(kw) || sdt.contains(kw) || email.contains(kw) || dc.contains(kw)) {
                filteredOrdersList.add(o);
            }
        }
        populateMasterTable();
    }

    private void populateMasterTable() {
        masterModel.setRowCount(0);
        int stt = 1;
        for (KhachDatHang o : filteredOrdersList) {
            String ngayStr = o.getNgay() != null ? DateUtil.formatDate(o.getNgay()) : "";
            masterModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ngayStr,
                    o.getName() != null ? o.getName() : "",
                    o.getTenKhach() != null ? o.getTenKhach() : "",
                    o.getDiaChi() != null ? o.getDiaChi() : "",
                    o.getDienThoai() != null ? o.getDienThoai() : "",
                    o.getEmail() != null ? o.getEmail() : "",
                    CurrencyUtil.format(o.getTongCong())
            });
        }

        if (tableMaster.getRowCount() > 0) {
            tableMaster.setRowSelectionInterval(0, 0);
            onMasterSelectionChanged();
        } else {
            detailModel.setRowCount(0);
        }
    }

    private void onMasterSelectionChanged() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= filteredOrdersList.size()) {
            detailModel.setRowCount(0);
            return;
        }

        KhachDatHang o = filteredOrdersList.get(row);
        List<KhachDatHangChiTiet> details = khachDatHangDAO.getChiTietByKhachDatHangId(o.getId(), o.getName());

        detailModel.setRowCount(0);
        int stt = 1;
        for (KhachDatHangChiTiet ct : details) {
            detailModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getNote() != null ? ct.getNote() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : ""
            });
        }
    }

    private void onConfirmSelection() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= filteredOrdersList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu đặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedOrder = filteredOrdersList.get(row);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public KhachDatHang getSelectedOrder() { return selectedOrder; }
}
