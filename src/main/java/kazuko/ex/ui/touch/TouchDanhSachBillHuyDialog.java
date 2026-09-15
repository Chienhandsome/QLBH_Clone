package kazuko.ex.ui.touch;

import kazuko.ex.dao.HoaDonDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Hộp thoại "DANH SÁCH BILL HỦY" cho phân hệ Quản lý bán hàng cảm ứng (Hình 3).
 */
public class TouchDanhSachBillHuyDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color GREEN = new Color(0, 105, 0);
    private static final Color RED = new Color(155, 0, 0);
    private static final Color ORANGE = new Color(211, 84, 0);
    private static final Color YELLOW_PANEL = new Color(254, 255, 204); // Vàng nhạt như Hình 3
    private static final Color GRAY_PANEL = new Color(230, 230, 230);   // Xám nhạt như Hình 3

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private Date tuNgay;
    private Date denNgay;

    private final JTextField txtTuNgay = new JTextField();
    private final JTextField txtDenNgay = new JTextField();
    private final JPanel cardsListPanel = new JPanel();
    private final JTable tableDetails = new JTable();
    private DefaultTableModel tableModel;

    private final JLabel lblDetailHeader = new JLabel();
    private final JLabel lblDetailTotal = new JLabel();

    private List<DonHang> canceledList = new ArrayList<>();
    private DonHang selectedBill = null;

    public TouchDanhSachBillHuyDialog(Window owner) {
        super(owner, "Danh sách bill hủy", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Calendar cal = Calendar.getInstance();
        denNgay = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        tuNgay = cal.getTime();

        initComponents();

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        loadData();

        setSize(1080, 620);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 3));

        // 1. Tiêu đề hộp thoại
        JLabel lblTitle = new JLabel("DANH SÁCH BILL HỦY", SwingConstants.CENTER);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(HEADER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 44));
        root.add(lblTitle, BorderLayout.NORTH);

        // 2. Thân chính: Bên trái (vàng nhạt), Bên phải (xám nhạt)
        JPanel centerPanel = new JPanel(new BorderLayout(8, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Panel Trái: Danh sách bill hủy (chiều rộng 280px đúng Hình 3)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(280, 0));
        leftPanel.setBackground(YELLOW_PANEL);
        leftPanel.setBorder(new LineBorder(new Color(180, 200, 210), 1));

        cardsListPanel.setLayout(new BoxLayout(cardsListPanel, BoxLayout.Y_AXIS));
        cardsListPanel.setBackground(YELLOW_PANEL);
        cardsListPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        JScrollPane spLeft = new JScrollPane(cardsListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spLeft.setBorder(null);
        spLeft.getViewport().setBackground(YELLOW_PANEL);
        spLeft.getVerticalScrollBar().setUnitIncrement(35);
        leftPanel.add(spLeft, BorderLayout.CENTER);

        // Nút bánh răng ở góc dưới phải của panel trái (Hình 3)
        JPanel gearBarLeft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        gearBarLeft.setOpaque(false);
        JButton btnGearLeft = createGearButton();
        gearBarLeft.add(btnGearLeft);
        leftPanel.add(gearBarLeft, BorderLayout.SOUTH);

        centerPanel.add(leftPanel, BorderLayout.WEST);

        // Panel Phải: Chi tiết bill hủy
        JPanel rightPanel = new JPanel(new BorderLayout(0, 4));
        rightPanel.setBackground(GRAY_PANEL);
        rightPanel.setBorder(new LineBorder(new Color(180, 200, 210), 1));

        // Header thông tin chi tiết
        JPanel pnlDetailTop = new JPanel(new BorderLayout());
        pnlDetailTop.setOpaque(false);
        pnlDetailTop.setBorder(new EmptyBorder(8, 10, 4, 10));
        lblDetailHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDetailHeader.setForeground(new Color(0, 50, 70));
        pnlDetailTop.add(lblDetailHeader, BorderLayout.NORTH);
        rightPanel.add(pnlDetailTop, BorderLayout.NORTH);

        // Bảng chi tiết mặt hàng
        String[] cols = {"STT", "Mã hàng", "Tên mặt hàng", "ĐVT", "SL", "Đơn giá", "Thành tiền"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tableDetails.setModel(tableModel);
        tableDetails.setRowHeight(28);
        tableDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableDetails.getTableHeader().setBackground(new Color(210, 225, 235));
        tableDetails.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableDetails.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableDetails.getColumnModel().getColumn(2).setPreferredWidth(160);
        tableDetails.getColumnModel().getColumn(3).setPreferredWidth(50);
        tableDetails.getColumnModel().getColumn(4).setPreferredWidth(50);
        tableDetails.getColumnModel().getColumn(5).setPreferredWidth(85);
        tableDetails.getColumnModel().getColumn(6).setPreferredWidth(95);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tableDetails.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tableDetails.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        tableDetails.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane spTable = new JScrollPane(tableDetails);
        spTable.setBorder(new EmptyBorder(0, 6, 0, 6));
        spTable.getViewport().setBackground(Color.WHITE);
        rightPanel.add(spTable, BorderLayout.CENTER);

        // Footer panel phải
        JPanel pnlDetailBottom = new JPanel(new BorderLayout());
        pnlDetailBottom.setOpaque(false);
        pnlDetailBottom.setBorder(new EmptyBorder(4, 10, 4, 4));

        lblDetailTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetailTotal.setForeground(RED);
        pnlDetailBottom.add(lblDetailTotal, BorderLayout.WEST);

        JButton btnGearRight = createGearButton();
        pnlDetailBottom.add(btnGearRight, BorderLayout.EAST);
        rightPanel.add(pnlDetailBottom, BorderLayout.SOUTH);

        centerPanel.add(rightPanel);
        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Thanh nút đáy (Hình 3)
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setPreferredSize(new Dimension(0, 52));
        bottomBar.setBorder(new EmptyBorder(4, 8, 8, 8));

        // Bộ lọc ngày bên trái
        JPanel filterPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterPnl.setOpaque(false);

        JLabel lblTu = new JLabel("TỪ:");
        lblTu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTu.setForeground(Color.WHITE);
        filterPnl.add(lblTu);

        txtTuNgay.setPreferredSize(new Dimension(105, 38));
        txtTuNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTuNgay.setHorizontalAlignment(SwingConstants.CENTER);
        txtTuNgay.setEditable(false);
        txtTuNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtTuNgay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String picked = TouchDatePickerDialog.showDialog(TouchDanhSachBillHuyDialog.this, txtTuNgay.getText().trim());
                if (picked != null && !picked.isEmpty()) {
                    try {
                        tuNgay = SDF.parse(picked);
                        txtTuNgay.setText(picked);
                        loadData();
                    } catch (Exception ignored) {}
                }
            }
        });
        filterPnl.add(txtTuNgay);

        JLabel lblDen = new JLabel("ĐẾN:");
        lblDen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDen.setForeground(Color.WHITE);
        filterPnl.add(lblDen);

        txtDenNgay.setPreferredSize(new Dimension(105, 38));
        txtDenNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtDenNgay.setHorizontalAlignment(SwingConstants.CENTER);
        txtDenNgay.setEditable(false);
        txtDenNgay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtDenNgay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String picked = TouchDatePickerDialog.showDialog(TouchDanhSachBillHuyDialog.this, txtDenNgay.getText().trim());
                if (picked != null && !picked.isEmpty()) {
                    try {
                        denNgay = SDF.parse(picked);
                        txtDenNgay.setText(picked);
                        loadData();
                    } catch (Exception ignored) {}
                }
            }
        });
        filterPnl.add(txtDenNgay);

        // Nút mũi tên mở chọn nhanh ngày
        JButton btnQuickDate = new JButton("▼");
        btnQuickDate.setPreferredSize(new Dimension(38, 38));
        btnQuickDate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnQuickDate.setBackground(new Color(0, 67, 86));
        btnQuickDate.setForeground(Color.WHITE);
        btnQuickDate.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnQuickDate.setFocusPainted(false);
        btnQuickDate.addActionListener(e -> {
            TouchChonNhanhNgayDialog.showDialog(this, (start, end) -> {
                tuNgay = start;
                denNgay = end;
                txtTuNgay.setText(SDF.format(tuNgay));
                txtDenNgay.setText(SDF.format(denNgay));
                loadData();
            });
        });
        filterPnl.add(btnQuickDate);

        JButton btnRefresh = new JButton("REFRESH");
        btnRefresh.setPreferredSize(new Dimension(110, 42));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(GREEN);
        btnRefresh.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadData());
        filterPnl.add(btnRefresh);

        bottomBar.add(filterPnl, BorderLayout.WEST);

        // Nút THOÁT bên phải
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);

        JButton btnThoat = new JButton("THOÁT");
        btnThoat.setPreferredSize(new Dimension(110, 42));
        btnThoat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThoat.setForeground(Color.WHITE);
        btnThoat.setBackground(RED);
        btnThoat.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnThoat.setFocusPainted(false);
        btnThoat.addActionListener(e -> dispose());
        rightActions.add(btnThoat);

        bottomBar.add(rightActions, BorderLayout.EAST);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
        txtTuNgay.setText(SDF.format(tuNgay));
        txtDenNgay.setText(SDF.format(denNgay));
    }

    private JButton createGearButton() {
        JButton btn = new JButton("⚙");
        btn.setPreferredSize(new Dimension(36, 32));
        btn.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 20));
        btn.setForeground(Color.WHITE);
        btn.setBackground(HEADER);
        btn.setBorder(new LineBorder(Color.WHITE, 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData() {
        canceledList = hoaDonDAO.getDanhSachBillHuy(tuNgay, denNgay, null);
        if (!canceledList.isEmpty()) {
            if (selectedBill == null) {
                selectedBill = canceledList.get(0);
            } else {
                DonHang match = null;
                for (DonHang d : canceledList) {
                    if ((selectedBill.getId() != null && selectedBill.getId().equals(d.getId()))
                            || (selectedBill.getName() != null && selectedBill.getName().equals(d.getName()))) {
                        match = d;
                        break;
                    }
                }
                selectedBill = (match != null) ? match : canceledList.get(0);
            }
        } else {
            selectedBill = null;
        }

        renderCardsList();
        renderBillDetails(selectedBill);
    }

    private void selectBill(DonHang dh) {
        this.selectedBill = dh;
        renderCardsList();
        renderBillDetails(selectedBill);
    }

    void setSampleBillsForTest(List<DonHang> list) {
        this.canceledList = list;
        if (list != null && !list.isEmpty()) {
            this.selectedBill = list.get(0);
        } else {
            this.selectedBill = null;
        }
        renderCardsList();
        renderBillDetails(selectedBill);
    }

    DonHang getSelectedBillForTest() {
        return selectedBill;
    }

    JPanel getCardsListPanelForTest() {
        return cardsListPanel;
    }

    private void renderCardsList() {
        cardsListPanel.removeAll();
        if (canceledList.isEmpty()) {
            JLabel lblEmpty = new JLabel("(Không có bill hủy nào)", SwingConstants.CENTER);
            lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblEmpty.setForeground(Color.GRAY);
            cardsListPanel.add(lblEmpty);
        } else {
            for (DonHang dh : canceledList) {
                JPanel card = createBillCard(dh);
                cardsListPanel.add(card);
                cardsListPanel.add(Box.createVerticalStrut(6));
            }
        }
        cardsListPanel.revalidate();
        cardsListPanel.repaint();
    }

    private JPanel createBillCard(DonHang dh) {
        boolean isSelected = (selectedBill != null && (dh == selectedBill
                || (dh.getId() != null && dh.getId().equals(selectedBill.getId()))
                || (dh.getName() != null && dh.getName().equals(selectedBill.getName()))));
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setOpaque(true);
        card.setBackground(isSelected ? ORANGE : new Color(0, 79, 76));
        card.setBorder(new CompoundBorder(
                new LineBorder(isSelected ? Color.WHITE : new Color(140, 160, 170), isSelected ? 2 : 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Dòng trên: Số phiếu (trái) - Tổng tiền (phải)
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setOpaque(false);
        JLabel lblSo = new JLabel(dh.getName() != null ? dh.getName() : "");
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSo.setForeground(Color.WHITE);
        row1.add(lblSo, BorderLayout.WEST);

        JLabel lblTien = new JLabel(CurrencyUtil.format(dh.getTongCong()), SwingConstants.RIGHT);
        lblTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTien.setForeground(Color.WHITE);
        row1.add(lblTien, BorderLayout.EAST);
        card.add(row1, BorderLayout.NORTH);

        // Dòng dưới: Khách hàng / Lý do hủy
        JPanel row2 = new JPanel(new BorderLayout());
        row2.setOpaque(false);
        String sub = (dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ") +
                (dh.getLyDoHuy() != null && !dh.getLyDoHuy().isEmpty() ? " (" + dh.getLyDoHuy() + ")" : "");
        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(220, 235, 240));
        row2.add(lblSub, BorderLayout.WEST);

        JLabel lblUser = new JLabel(dh.getThuNganHuy() != null ? dh.getThuNganHuy() : "Admin", SwingConstants.RIGHT);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(220, 235, 240));
        row2.add(lblUser, BorderLayout.EAST);
        card.add(row2, BorderLayout.SOUTH);

        attachClickListener(card, () -> selectBill(dh));

        return card;
    }

    private void attachClickListener(Component comp, Runnable onClick) {
        comp.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onClick.run();
            }
        });
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                attachClickListener(child, onClick);
            }
        }
    }

    private void renderBillDetails(DonHang dh) {
        tableModel.setRowCount(0);
        if (dh == null) {
            lblDetailHeader.setText("Chi tiết: (Chưa chọn phiếu)");
            lblDetailTotal.setText("");
            return;
        }

        String hdr = String.format("<html><b>Số phiếu:</b> %s &nbsp;&nbsp;|&nbsp;&nbsp; <b>Ngày:</b> %s &nbsp;&nbsp;|&nbsp;&nbsp; <b>Khách:</b> %s<br><b>Lý do hủy:</b> <font color='red'>%s</font></html>",
                dh.getName() != null ? dh.getName() : "",
                dh.getNgay() != null ? DateUtil.formatDateTime(dh.getNgay()) : "",
                dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ",
                dh.getLyDoHuy() != null ? dh.getLyDoHuy() : (dh.getGhiChu() != null ? dh.getGhiChu() : "Hủy phiếu"));
        lblDetailHeader.setText(hdr);

        int stt = 1;
        if (dh.getChiTietLayHang() != null) {
            for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                tableModel.addRow(new Object[]{
                        stt++,
                        ct.getMaHang(),
                        ct.getTenHang(),
                        ct.getTenDonViTinh(),
                        String.format("%.1f", ct.getSoLuong()),
                        CurrencyUtil.format(ct.getDonGia()),
                        CurrencyUtil.format(ct.getThanhTien())
                });
            }
        }

        lblDetailTotal.setText("TỔNG CỘNG: " + CurrencyUtil.format(dh.getTongCong()) + " VNĐ");
    }
}