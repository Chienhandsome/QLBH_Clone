package kazuko.ex.ui.touch;

import kazuko.ex.dao.KhachDatHangDAO;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Hộp thoại cảm ứng "MỞ ĐẶT HÀNG" chuẩn 100% Hình 3.
 * - Header: Slate Blue với tiêu đề "MỞ ĐẶT HÀNG"
 * - Vùng giữa: Lưới danh sách các phiếu đặt hàng (hàng chọn màu baby blue #D4EBF8) + nút bánh răng [⚙]
 * - Thanh đáy: CHỌN ĐẶT HÀNG (xám) bên trái, THOÁT (đỏ) bên phải.
 */
public class TouchMoDatHangDialog extends JDialog {

    private static final Color BG_DARK = new Color(8, 61, 86);         // #083D56
    private static final Color HEADER_BG = new Color(74, 130, 158);     // #4A829E
    private static final Color CENTER_CONTAINER_BG = new Color(227, 230, 232); // #E3E6E8
    private static final Color BTN_CHOOSE_BG = new Color(127, 148, 163);       // #7F94A3
    private static final Color BTN_EXIT_BG = new Color(153, 0, 0);             // #990000
    private static final Color SELECTION_BG = new Color(212, 235, 248);        // #D4EBF8

    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();
    private List<KhachDatHang> orderList = new ArrayList<>();
    private KhachDatHang selectedOrder = null;
    private boolean confirmed = false;

    private JTable tableOrders;
    private DefaultTableModel modelOrders;

    public TouchMoDatHangDialog(Window parent) {
        super(parent, "MỞ ĐẶT HÀNG", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        setSize(660, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(BG_DARK);
        root.setBorder(new LineBorder(HEADER_BG, 2));

        // 1. Header: MỞ ĐẶT HÀNG
        JLabel lblHeader = new JLabel("MỞ ĐẶT HÀNG", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(HEADER_BG);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHeader.setPreferredSize(new Dimension(660, 38));
        root.add(lblHeader, BorderLayout.NORTH);

        // 2. Vùng danh sách trung tâm
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(CENTER_CONTAINER_BG);
        pnlCenter.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 190, 200), 1, false),
                new EmptyBorder(1, 1, 1, 1)
        ));

        String[] cols = {"STT", "Số phiếu", "Ngày đặt", "Khách hàng", "Điện thoại", "Địa chỉ giao", "Tổng cộng", "Ghi chú"};
        modelOrders = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tableOrders = new JTable(modelOrders);
        setupTouchTableStyle(tableOrders);

        tableOrders.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableOrders.getSelectedRow();
                if (row >= 0 && row < orderList.size()) {
                    selectedOrder = orderList.get(row);
                    if (e.getClickCount() == 2) {
                        onConfirmChoose();
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tableOrders);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        pnlCenter.add(scroll, BorderLayout.CENTER);

        // Thanh công cụ nhỏ đáy lưới chứa nút bánh răng [⚙] góc dưới phải
        JPanel pnlGear = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 3));
        pnlGear.setBackground(CENTER_CONTAINER_BG);
        pnlGear.setPreferredSize(new Dimension(0, 32));

        JButton btnGear = createGearButton();
        btnGear.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Cài đặt bộ lọc và cột danh sách đặt hàng cảm ứng.",
                "Cấu hình", JOptionPane.INFORMATION_MESSAGE));
        pnlGear.add(btnGear);

        pnlCenter.add(pnlGear, BorderLayout.SOUTH);

        JPanel pnlBodyWrapper = new JPanel(new BorderLayout());
        pnlBodyWrapper.setOpaque(false);
        pnlBodyWrapper.setBorder(new EmptyBorder(0, 8, 2, 8));
        pnlBodyWrapper.add(pnlCenter, BorderLayout.CENTER);

        root.add(pnlBodyWrapper, BorderLayout.CENTER);

        // 3. Thanh đáy: CHỌN ĐẶT HÀNG vs THOÁT
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(4, 10, 8, 10));

        JButton btnChon = createActionButton("CHỌN ĐẶT HÀNG", BTN_CHOOSE_BG, new Dimension(135, 32), e -> onConfirmChoose());
        pnlBottom.add(btnChon, BorderLayout.WEST);

        JButton btnThoat = createActionButton("THOÁT", BTN_EXIT_BG, new Dimension(85, 32), e -> dispose());
        pnlBottom.add(btnThoat, BorderLayout.EAST);

        root.add(pnlBottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JButton createGearButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(50, 70, 90));
                int numTeeth = 8;
                int rOuter = 8;
                int rInner = 6;
                int hole = 3;
                for (int i = 0; i < numTeeth; i++) {
                    double angle = i * 2 * Math.PI / numTeeth;
                    int tx = cx + (int) (rOuter * Math.cos(angle));
                    int ty = cy + (int) (rOuter * Math.sin(angle));
                    g2.fillRect(tx - 2, ty - 2, 4, 4);
                }
                g2.fillOval(cx - rInner, cy - rInner, rInner * 2, rInner * 2);
                g2.setColor(getBackground());
                g2.fillOval(cx - hole, cy - hole, hole * 2, hole * 2);
                g2.dispose();
            }
        };
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(new Color(190, 200, 210), 1, true));
        btn.setPreferredSize(new Dimension(26, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupTouchTableStyle(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 234, 238));
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(242, 244, 247));
        header.setForeground(new Color(50, 50, 50));
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        // Căn chỉnh lề từng cột
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel().getColumn(0).setPreferredWidth(42);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(1).setPreferredWidth(95);

        table.getColumnModel().getColumn(2).setPreferredWidth(88);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        table.getColumnModel().getColumn(3).setPreferredWidth(115);
        table.getColumnModel().getColumn(4).setPreferredWidth(75);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);

        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        table.getColumnModel().getColumn(7).setPreferredWidth(70);
    }

    private JButton createActionButton(String text, Color bg, Dimension size, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.setPreferredSize(size);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private void loadOrders() {
        orderList = khachDatHangDAO.getDanhSachKhachDatHang(null, null, null, null);
        if (orderList == null) orderList = new ArrayList<>();

        modelOrders.setRowCount(0);
        int stt = 1;
        for (KhachDatHang o : orderList) {
            modelOrders.addRow(new Object[]{
                    stt++,
                    o.getName(),
                    o.getNgay() != null ? DateUtil.formatDate(o.getNgay()) : "",
                    o.getTenKhach() != null ? o.getTenKhach() : "",
                    o.getDienThoai() != null ? o.getDienThoai() : "",
                    o.getDiaChi() != null ? o.getDiaChi() : "",
                    CurrencyUtil.formatMoney(o.getTongCong()),
                    o.getNote() != null ? o.getNote() : ""
            });
        }

        if (tableOrders.getRowCount() > 0) {
            tableOrders.setRowSelectionInterval(0, 0);
            selectedOrder = orderList.get(0);
        }
    }

    private void onConfirmChoose() {
        int row = tableOrders.getSelectedRow();
        if (row < 0 || row >= orderList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn đặt hàng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedOrder = orderList.get(row);
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public KhachDatHang getSelectedOrder() {
        return selectedOrder;
    }
}