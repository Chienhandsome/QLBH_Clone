package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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

public class DanhSachHoaDonLuuTamDialog extends JDialog {

    private final List<DonHang> allTamList;
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private List<DonHang> filteredTamList = new ArrayList<>();
    private DonHang selectedHoaDon = null;
    private boolean confirmed = false;

    private JTextField txtFilter;
    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private JTable tableDetail;
    private DefaultTableModel detailModel;

    public DanhSachHoaDonLuuTamDialog(Window parent, List<DonHang> tamList) {
        super(parent, "DANH SÁCH HÓA ĐƠN LƯU TẠM", ModalityType.APPLICATION_MODAL);
        this.allTamList = tamList != null ? new ArrayList<>(tamList) : new ArrayList<>();
        this.filteredTamList = new ArrayList<>(this.allTamList);

        initComponents();
        loadMasterData();
    }

    private void initComponents() {
        setSize(860, 560);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header Panel
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 6));
        pnlHeader.setBackground(new Color(245, 248, 253));
        pnlHeader.setBorder(new EmptyBorder(8, 12, 6, 12));

        JLabel lblTitle = new JLabel("CHỌN LẠI HÓA ĐƠN LƯU TẠM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 40, 75));
        pnlHeader.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilter.setOpaque(false);
        JLabel lblLoc = new JLabel("Lọc dữ liệu:");
        lblLoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter = new JTextField(22);
        txtFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        pnlFilter.add(lblLoc);
        pnlFilter.add(txtFilter);
        pnlHeader.add(pnlFilter, BorderLayout.CENTER);

        add(pnlHeader, BorderLayout.NORTH);

        // Center SplitPane with Master Table (Top) and Detail Table (Bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(230);
        splitPane.setDividerSize(6);

        // 1. Master Table
        String[] masterHeaders = {"STT", "Ngày", "Số phiếu", "Khách hàng", "Nhân viên", "Kho xuất", "Tiền hàng", "Tiền giảm giá", "Phí vận chuyển", "Tổng cộng"};
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
        tableMaster.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableMaster.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableMaster.getColumnModel().getColumn(4).setPreferredWidth(90);
        tableMaster.getColumnModel().getColumn(5).setPreferredWidth(110);
        tableMaster.getColumnModel().getColumn(6).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(7).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(8).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(9).setPreferredWidth(95);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableMaster.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMasterSelectionChanged();
            }
        });

        tableMaster.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onAccept();
                }
            }
        });

        tableMaster.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    onAccept();
                }
            }
        });

        JScrollPane spMaster = new JScrollPane(tableMaster);
        splitPane.setTopComponent(spMaster);

        // 2. Detail Table
        String[] detailHeaders = {"STT", "Mã hàng", "Ghi chú", "Mặt hàng", "ĐVT", "Số lượng", "Đơn giá", "Giảm giá %", "Thành tiền"};
        detailModel = new DefaultTableModel(detailHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableDetail = new JTable(detailModel);
        tableDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDetail.setRowHeight(22);
        tableDetail.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDetail.getTableHeader().setBackground(new Color(230, 240, 252));

        tableDetail.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableDetail.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(3).setPreferredWidth(160);
        tableDetail.getColumnModel().getColumn(4).setPreferredWidth(50);
        tableDetail.getColumnModel().getColumn(5).setPreferredWidth(60);
        tableDetail.getColumnModel().getColumn(6).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(7).setPreferredWidth(70);
        tableDetail.getColumnModel().getColumn(8).setPreferredWidth(85);

        tableDetail.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        JScrollPane spDetail = new JScrollPane(tableDetail);
        splitPane.setBottomComponent(spDetail);

        add(splitPane, BorderLayout.CENTER);

        // Footer Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(new Color(210, 228, 248)); // Light blue footer matching screenshot
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 205, 235)));

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAccept.setBackground(Color.WHITE);
        btnAccept.setPreferredSize(new Dimension(90, 26));
        btnAccept.addActionListener(e -> onAccept());

        JButton btnCancel = new JButton("Thoát");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(80, 26));
        btnCancel.addActionListener(e -> dispose());

        pnlFooter.add(btnAccept);
        pnlFooter.add(btnCancel);
        add(pnlFooter, BorderLayout.SOUTH);

        // Key bindings: ESC to cancel
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void applyFilter() {
        String kw = txtFilter.getText().trim().toLowerCase();
        filteredTamList.clear();
        for (DonHang dh : allTamList) {
            if (kw.isEmpty()) {
                filteredTamList.add(dh);
                continue;
            }
            String name = dh.getName() != null ? dh.getName().toLowerCase() : "";
            String khach = dh.getTenKhach() != null ? dh.getTenKhach().toLowerCase() : "";
            String nv = dh.getTenNhanVien() != null ? dh.getTenNhanVien().toLowerCase() : "";
            if (name.contains(kw) || khach.contains(kw) || nv.contains(kw)) {
                filteredTamList.add(dh);
            }
        }
        loadMasterData();
    }

    private void loadMasterData() {
        masterModel.setRowCount(0);
        int stt = 1;
        for (DonHang dh : filteredTamList) {
            String ngayStr = dh.getNgay() != null ? DateUtil.formatDate(dh.getNgay()) : "";
            String khoName = "";
            if (dh.getKhoXuatId() != null) {
                KhoHang k = khoHangDAO.findById(dh.getKhoXuatId());
                khoName = k != null ? k.getName() : dh.getKhoXuatId();
            }
            masterModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ngayStr,
                    dh.getName() != null ? dh.getName() : "",
                    dh.getTenKhach() != null ? dh.getTenKhach() : "",
                    dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "",
                    khoName,
                    CurrencyUtil.format(dh.getTienHang()),
                    CurrencyUtil.format(dh.getTienGiamGia()),
                    "0",
                    CurrencyUtil.format(dh.getTongCong())
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
        if (row < 0 || row >= filteredTamList.size()) {
            detailModel.setRowCount(0);
            return;
        }

        DonHang dh = filteredTamList.get(row);
        detailModel.setRowCount(0);
        int stt = 1;

        List<DonHangChiTiet> items = new ArrayList<>();
        if (dh.getChiTietLayHang() != null) items.addAll(dh.getChiTietLayHang());
        if (dh.getChiTietTraHang() != null) items.addAll(dh.getChiTietTraHang());

        for (DonHangChiTiet ct : items) {
            detailModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getThanhTien())
            });
        }
    }

    private void onAccept() {
        int row = tableMaster.getSelectedRow();
        if (row < 0 || row >= filteredTamList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn lưu tạm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedHoaDon = filteredTamList.get(row);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() { return confirmed; }
    public DonHang getSelectedHoaDon() { return selectedHoaDon; }
}
