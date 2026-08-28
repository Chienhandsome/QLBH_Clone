package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class DanhMucBillHuyDialog extends JDialog {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;

    private JTable tableMaster;
    private DefaultTableModel masterModel;
    private JTable tableDetail;
    private DefaultTableModel detailModel;

    private List<DonHang> listCancelled = new ArrayList<>();

    public DanhMucBillHuyDialog(Window parent) {
        super(parent, "DANH MỤC BILL HỦY", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(940, 560);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Top Filter Bar
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        pnlTop.setBackground(new Color(215, 230, 248));
        pnlTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 205, 235)));

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTop.add(lblTu);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setDate(cal.getTime());
        dpkTuNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadData(); }
            @Override public void removeUpdate(DocumentEvent e) { loadData(); }
            @Override public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        pnlTop.add(dpkTuNgay);

        JLabel lblDen = new JLabel("Đến:");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTop.add(lblDen);

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadData(); }
            @Override public void removeUpdate(DocumentEvent e) { loadData(); }
            @Override public void changedUpdate(DocumentEvent e) { loadData(); }
        });
        pnlTop.add(dpkDenNgay);

        JButton btnRefresh = new JButton("Nạp lại", IconHelper.getIcon("Refresh.png", 16, 16));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        pnlTop.add(btnRefresh);

        add(pnlTop, BorderLayout.NORTH);

        // Center Split: Master Table vs Detail Table
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(260);
        split.setDividerSize(5);

        // 1. Master Table (Top)
        String[] masterHeaders = {
                "STT", "Số phiếu", "Ghi chú", "Ngày", "Khách hàng", "Nhân viên",
                "Thu ngân hủy", "Giờ hủy", "Ngày hủy", "Đổi trả", "Đã thanh toán"
        };
        masterModel = new DefaultTableModel(masterHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableMaster = new JTable(masterModel);
        tableMaster.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableMaster.setRowHeight(22);
        tableMaster.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMaster.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableMaster.getTableHeader().setBackground(new Color(230, 240, 252));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        tableMaster.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableMaster.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableMaster.getColumnModel().getColumn(1).setPreferredWidth(95);
        tableMaster.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableMaster.getColumnModel().getColumn(3).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableMaster.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableMaster.getColumnModel().getColumn(5).setPreferredWidth(110);
        tableMaster.getColumnModel().getColumn(6).setPreferredWidth(100);
        tableMaster.getColumnModel().getColumn(7).setPreferredWidth(70);
        tableMaster.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        tableMaster.getColumnModel().getColumn(8).setPreferredWidth(85);
        tableMaster.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);
        tableMaster.getColumnModel().getColumn(9).setPreferredWidth(80);
        tableMaster.getColumnModel().getColumn(9).setCellRenderer(rightRenderer);
        tableMaster.getColumnModel().getColumn(10).setPreferredWidth(90);
        tableMaster.getColumnModel().getColumn(10).setCellRenderer(rightRenderer);

        tableMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMasterSelectionChanged();
            }
        });

        JScrollPane spMaster = new JScrollPane(tableMaster);
        split.setTopComponent(spMaster);

        // 2. Detail Table (Bottom)
        String[] detailHeaders = {"STT", "Ghi chú", "Mã hàng", "Tên hàng", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền"};
        detailModel = new DefaultTableModel(detailHeaders, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableDetail = new JTable(detailModel);
        tableDetail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableDetail.setRowHeight(22);
        tableDetail.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableDetail.getTableHeader().setBackground(new Color(230, 240, 252));

        tableDetail.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableDetail.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableDetail.getColumnModel().getColumn(1).setPreferredWidth(90);
        tableDetail.getColumnModel().getColumn(2).setPreferredWidth(80);
        tableDetail.getColumnModel().getColumn(3).setPreferredWidth(180);
        tableDetail.getColumnModel().getColumn(4).setPreferredWidth(60);
        tableDetail.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableDetail.getColumnModel().getColumn(5).setPreferredWidth(60);
        tableDetail.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(6).setPreferredWidth(85);
        tableDetail.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tableDetail.getColumnModel().getColumn(7).setPreferredWidth(95);
        tableDetail.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);

        JScrollPane spDetail = new JScrollPane(tableDetail);
        split.setBottomComponent(spDetail);

        add(split, BorderLayout.CENTER);

        // Key bindings
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadData() {
        Date tuNgay = dpkTuNgay != null ? dpkTuNgay.getDate() : null;
        Date denNgay = dpkDenNgay != null ? dpkDenNgay.getDate() : null;
        listCancelled = hoaDonDAO.getDanhSachBillHuy(tuNgay, denNgay, null);

        masterModel.setRowCount(0);
        int stt = 1;
        for (DonHang dh : listCancelled) {
            String ngayStr = dh.getNgay() != null ? DateUtil.formatDate(dh.getNgay()) : "";
            String ngayHuyStr = dh.getNgayHuy() != null ? DateUtil.formatDate(dh.getNgayHuy()) : ngayStr;
            String gioHuyStr = dh.getGioHuy() != null ? dh.getGioHuy() : "12:00";

            masterModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    dh.getName() != null ? dh.getName() : "",
                    dh.getLyDoHuy() != null ? dh.getLyDoHuy() : (dh.getGhiChu() != null ? dh.getGhiChu() : ""),
                    ngayStr,
                    dh.getTenKhach() != null ? dh.getTenKhach() : "",
                    dh.getTenNhanVien() != null ? dh.getTenNhanVien() : "",
                    dh.getThuNganHuy() != null ? dh.getThuNganHuy() : "Administrator",
                    gioHuyStr,
                    ngayHuyStr,
                    CurrencyUtil.format(dh.getTienDoiTra()),
                    CurrencyUtil.format(dh.getTienThanhToan())
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
        if (row < 0 || row >= listCancelled.size()) {
            detailModel.setRowCount(0);
            return;
        }

        DonHang dh = listCancelled.get(row);
        detailModel.setRowCount(0);
        int stt = 1;

        List<DonHangChiTiet> allDetails = new ArrayList<>();
        if (dh.getChiTietLayHang() != null) allDetails.addAll(dh.getChiTietLayHang());
        if (dh.getChiTietTraHang() != null) allDetails.addAll(dh.getChiTietTraHang());

        for (DonHangChiTiet ct : allDetails) {
            detailModel.addRow(new Object[]{
                    String.valueOf(stt++),
                    ct.getGhiChu() != null ? ct.getGhiChu() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    CurrencyUtil.format(ct.getThanhTien())
            });
        }
    }
}
