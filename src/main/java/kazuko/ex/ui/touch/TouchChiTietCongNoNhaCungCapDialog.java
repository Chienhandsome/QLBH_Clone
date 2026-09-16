package kazuko.ex.ui.touch;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.model.congno.ChiTietCongNoNhaCungCapEntry;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
import kazuko.ex.util.CurrencyUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

/** Dialog cảm ứng xem sổ chi tiết công nợ của nhà cung cấp. */
public final class TouchChiTietCongNoNhaCungCapDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color RED = new Color(165, 10, 10);

    private final CongNoNhaCungCapDAO congNoDAO = new CongNoNhaCungCapDAO();
    private final CongNoNhaCungCapItem supplier;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TouchChiTietCongNoNhaCungCapDialog(Window parent, CongNoNhaCungCapItem supplier) {
        super(parent, "SỔ CHI TIẾT CÔNG NỢ", ModalityType.APPLICATION_MODAL);
        this.supplier = supplier;
        setUndecorated(true);
        setSize(1080, 650);
        setLocationRelativeTo(parent);
        setContentPane(createContent());
    }

    private JPanel createContent() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(BG);
        root.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));

        // Header
        String name = supplier != null && supplier.getTenNCC() != null ? supplier.getTenNCC().toUpperCase() : "";
        JLabel title = new JLabel("SỔ CHI TIẾT CÔNG NỢ - " + name, SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(HEADER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setPreferredSize(new Dimension(0, 60));
        root.add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"Số phiếu", "Ngày", "Nhập hàng", "Thanh toán", "Diễn giải", "Nợ lũy kế"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        if (supplier != null && supplier.getNhaCungCapId() != null) {
            List<ChiTietCongNoNhaCungCapEntry> entries = congNoDAO.getChiTietCongNoNCC(supplier.getNhaCungCapId(), null, null);
            for (ChiTietCongNoNhaCungCapEntry e : entries) {
                model.addRow(new Object[]{
                        e.getSoPhieu() != null ? e.getSoPhieu() : "",
                        e.getNgay() != null ? dateFormat.format(e.getNgay()) : "",
                        e.getTongCong() > 0 ? CurrencyUtil.format(e.getTongCong()) : "",
                        e.getTienThanhToan() > 0 ? CurrencyUtil.format(e.getTienThanhToan()) : "",
                        e.getDienGiai() != null ? e.getDienGiai() : "",
                        CurrencyUtil.format(e.getLuyKe())
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(215, 225, 235));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 17));
        th.setPreferredSize(new Dimension(0, 48));
        th.setBackground(new Color(220, 235, 245));
        th.setForeground(new Color(20, 50, 80));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(4, 10, 4, 10));
        root.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(6, 12, 10, 12));

        double debt = supplier != null ? supplier.getConNo() : 0.0;
        JLabel lblDebt = new JLabel("TỔNG CÒN NỢ: " + CurrencyUtil.format(debt) + " đ");
        lblDebt.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDebt.setForeground(new Color(255, 235, 100));
        footer.add(lblDebt, BorderLayout.WEST);

        JButton close = new JButton("ĐÓNG");
        close.setFont(new Font("Segoe UI", Font.BOLD, 18));
        close.setBackground(RED);
        close.setForeground(Color.WHITE);
        close.setPreferredSize(new Dimension(140, 48));
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
        close.addActionListener(e -> dispose());
        footer.add(close, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);
        return root;
    }
}
