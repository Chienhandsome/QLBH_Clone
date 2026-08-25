package kazuko.ex.ui.danhmuc;

import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;

public class LichSuBanHangDialog extends JDialog {
    private final MatHang currentItem;
    private JTextField txtSearch;
    private JTable tableHistory;
    private DefaultTableModel tableModel;

    public LichSuBanHangDialog(Dialog parent, MatHang item) {
        super(parent, "LỊCH SỬ BÁN HÀNG", true);
        this.currentItem = item;
        initUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(650, 480));
        getContentPane().setBackground(new Color(240, 245, 252));

        // Top Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(12, 15, 8, 15));

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlTitle.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Statistic16.png", 18, 18));
        JLabel lblTitle = new JLabel("LỊCH SỬ BÁN HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 50, 95));
        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitle);

        JLabel lblProductName = new JLabel(currentItem != null && currentItem.getName() != null ? currentItem.getName() : "");
        lblProductName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProductName.setForeground(new Color(20, 20, 20));
        lblProductName.setBorder(new EmptyBorder(6, 4, 6, 0));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlSearch.setOpaque(false);
        JLabel lblSearch = new JLabel("Lọc dữ liệu:");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(160, 24));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearch);

        JPanel pnlTop = new JPanel();
        pnlTop.setLayout(new BoxLayout(pnlTop, BoxLayout.Y_AXIS));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlTitle);
        pnlTop.add(lblProductName);
        pnlTop.add(pnlSearch);

        pnlHeader.add(pnlTop, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"", "Ngày", "Giờ", "Khách hàng", "Đơn giá"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableHistory = new JTable(tableModel);
        tableHistory.setRowHeight(22);
        tableHistory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableHistory.setShowGrid(true);
        tableHistory.setGridColor(new Color(225, 235, 245));

        JTableHeader th = tableHistory.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        th.setBackground(new Color(235, 242, 252));
        th.setPreferredSize(new Dimension(100, 26));

        tableHistory.getColumnModel().getColumn(0).setPreferredWidth(26);
        tableHistory.getColumnModel().getColumn(0).setMaxWidth(30);
        tableHistory.getColumnModel().getColumn(1).setPreferredWidth(90);
        tableHistory.getColumnModel().getColumn(2).setPreferredWidth(70);
        tableHistory.getColumnModel().getColumn(3).setPreferredWidth(260);
        tableHistory.getColumnModel().getColumn(4).setPreferredWidth(120);

        DecimalFormat df = new DecimalFormat("#,##0.##");
        tableHistory.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (value instanceof Number) {
                    lbl.setText(df.format(((Number) value).doubleValue()));
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableHistory);
        scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, new Color(185, 205, 230)));

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(4, 15, 10, 15));
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // Bottom Footer
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        pnlFooter.setOpaque(false);

        JButton btnClose = new JButton("Thoát");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setPreferredSize(new Dimension(85, 26));
        btnClose.setBackground(Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        pnlFooter.add(btnClose);

        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void filterData() {
        // Mock / placeholder filtering
    }
}
