package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.BangLuongTongHopDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.nhansu.BangLuong;
import kazuko.ex.model.nhansu.BangLuongTongHop;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.ui.quy.TaoPhieuChiDialog;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ChiLuongDialog extends JDialog {

    private final BangLuong currentBangLuong;
    private final List<BangLuongTongHop> salaryList;
    private final Runnable onSalaryUpdated;

    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblActionCreateSlip;

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0");

    public ChiLuongDialog(Window parent, BangLuong bangLuong, List<BangLuongTongHop> salaryList, Runnable onSalaryUpdated) {
        super(parent, "CHI LƯƠNG", ModalityType.APPLICATION_MODAL);
        this.currentBangLuong = bangLuong;
        this.salaryList = salaryList != null ? new ArrayList<>(salaryList) : new ArrayList<>();
        this.onSalaryUpdated = onSalaryUpdated;

        initComponents();
        loadDataToTable();

        setSize(560, 420);
        setMinimumSize(new Dimension(500, 360));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 245, 252));

        // 1. Top Panel: Banner & Action Link
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // Banner
        JPanel pnlBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        pnlBanner.setBackground(new Color(230, 245, 230));
        pnlBanner.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 220, 190)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("money.png", 26, 26));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("btnCash.Image.png", 26, 26));
        }
        int th = currentBangLuong != null ? currentBangLuong.getThang() : 7;
        int nam = currentBangLuong != null ? currentBangLuong.getNam() : 2018;

        JLabel lblTitle = new JLabel("Chi tiết chi lương tháng " + th + " năm " + nam);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(20, 45, 30));

        pnlBanner.add(lblIcon);
        pnlBanner.add(lblTitle);
        pnlNorth.add(pnlBanner, BorderLayout.NORTH);

        // Sub Link: Tạo phiếu chi lương cho...
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlAction.setBackground(new Color(225, 238, 252));
        pnlAction.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JLabel lblSlipIcon = new JLabel(IconHelper.getIcon("report--pencil.png", 16, 16));
        lblActionCreateSlip = new JLabel("Tạo phiếu chi lương");
        lblActionCreateSlip.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblActionCreateSlip.setForeground(new Color(0, 70, 175));
        lblActionCreateSlip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblActionCreateSlip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openTaoPhieuChiForSelectedEmployee();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lblActionCreateSlip.setText("<html><u>" + getActionText() + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lblActionCreateSlip.setText(getActionText());
            }
        });

        pnlAction.add(lblSlipIcon);
        pnlAction.add(lblActionCreateSlip);
        pnlNorth.add(pnlAction, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center Table
        String[] cols = {"", "Nhân viên", "Tiền lương", "Đã nhận", "Số phiếu chi"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(22);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(215, 225, 235));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (isSelected) {
                        setText("▶ " + (row + 1));
                    } else {
                        setText(String.valueOf(row + 1));
                    }
                } else if (column == 2 || column == 3) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    c.setBackground(new Color(188, 104, 52)); // Cam đất
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionLinkText();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openTaoPhieuChiForSelectedEmployee();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new MatteBorder(0, 1, 1, 1, new Color(185, 205, 230)));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Panel: Thoát Button
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setOpaque(false);

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225), 1), new EmptyBorder(4, 16, 4, 16)));
        btnThoat.setFocusPainted(false);
        btnThoat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnThoat);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private String getActionText() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < salaryList.size()) {
            return "Tạo phiếu chi lương cho '" + salaryList.get(row).getTenNhanVien() + "'";
        }
        return "Tạo phiếu chi lương";
    }

    private void updateActionLinkText() {
        lblActionCreateSlip.setText(getActionText());
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);
        for (BangLuongTongHop th : salaryList) {
            tableModel.addRow(new Object[]{
                    "",
                    th.getTenNhanVien(),
                    CURRENCY_FMT.format(th.getThucNhan()),
                    CURRENCY_FMT.format(th.getDaNhan()),
                    th.getSoPhieuChi() != null ? th.getSoPhieuChi() : ""
            });
        }

        if (!salaryList.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
            updateActionLinkText();
        }
    }

    private void openTaoPhieuChiForSelectedEmployee() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= salaryList.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để tạo phiếu chi lương!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BangLuongTongHop th = salaryList.get(row);
        int m = currentBangLuong != null ? currentBangLuong.getThang() : 7;
        int y = currentBangLuong != null ? currentBangLuong.getNam() : 2018;

        double remaining = Math.max(0, th.getThucNhan() - th.getDaNhan());

        PhieuChi pc = new PhieuChi();
        pc.setLoaiDoiTuong("Nhân viên");
        pc.setNhanVienId(th.getNhanVienId());
        pc.setTenDoiTuong(th.getTenNhanVien());
        pc.setDiaChi("");
        pc.setLyDoChi("Chi lương tháng " + m + "/" + y);
        pc.setSoTien(remaining > 0 ? remaining : th.getThucNhan());

        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(this, pc, null, "Nhân viên", () -> {
            // Sau khi lưu phiếu chi thành công
            th.setDaNhan(th.getDaNhan() + pc.getSoTien());
            th.setSoPhieuChi(pc.getSoPhieu());
            loadDataToTable();
            if (row < table.getRowCount()) {
                table.setRowSelectionInterval(row, row);
            }
            if (onSalaryUpdated != null) {
                onSalaryUpdated.run();
            }
        });
        dlg.setVisible(true);
    }
}
