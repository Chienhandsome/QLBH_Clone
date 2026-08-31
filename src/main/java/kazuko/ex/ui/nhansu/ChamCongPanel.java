package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.BangLuongDAO;
import kazuko.ex.dao.CaLamViecDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.danhmuc.CaLamViec;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.nhansu.BangLuong;
import kazuko.ex.model.nhansu.ChamCongChiTiet;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChamCongPanel extends JPanel {

    private final BangLuongDAO bangLuongDAO = new BangLuongDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();

    // Khung Trái: Bảng lương
    private JTable tableBangLuong;
    private DefaultTableModel modelBangLuong;
    private List<BangLuong> listBangLuong = new ArrayList<>();
    private BangLuong currentSelectedBangLuong = null;

    // Khung Phải: Bảng chấm công
    private JTable tableChamCong;
    private DefaultTableModel modelChamCong;
    private List<RowInfo> matrixRows = new ArrayList<>();

    // Status colors matching screenshot 1
    private static final Color COLOR_DI_LAM = new Color(230, 20, 20); // Đỏ
    private static final Color COLOR_NGHI_CO_PHEP = new Color(0, 145, 0); // Xanh lá
    private static final Color COLOR_NGHI_KHONG_PHEP = new Color(0, 225, 225); // Xanh ngọc cyan
    private static final Color COLOR_KHAC = new Color(200, 225, 255); // Xanh nhạt

    private static class RowInfo {
        NhanVien nhanVien;
        CaLamViec caLamViec;
        boolean isFirstShiftOfEmployee;

        RowInfo(NhanVien nhanVien, CaLamViec caLamViec, boolean isFirstShiftOfEmployee) {
            this.nhanVien = nhanVien;
            this.caLamViec = caLamViec;
            this.isFirstShiftOfEmployee = isFirstShiftOfEmployee;
        }
    }

    public ChamCongPanel() {
        initComponents();
        loadBangLuongList();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 252));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(5);

        // ==================== KHUNG TRÁI: BẢNG LƯƠNG ====================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(new MatteBorder(1, 1, 1, 0, new Color(185, 205, 230)));

        // Mini Toolbar Trái (+ Thêm, Sửa, Xóa)
        JPanel leftToolBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 4));
        leftToolBar.setBackground(new Color(225, 238, 252));
        leftToolBar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        JButton btnThemBL = createLeftToolButton("Thêm", IconHelper.getIcon("plus.png", 14, 14));
        JButton btnSuaBL = createLeftToolButton("Sửa", IconHelper.getIcon("pencil.png", 14, 14));
        JButton btnXoaBL = createLeftToolButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));

        btnThemBL.addActionListener(e -> onAddNewBangLuong());
        btnSuaBL.addActionListener(e -> onEditSelectedBangLuong());
        btnXoaBL.addActionListener(e -> onDeleteSelectedBangLuong());

        leftToolBar.add(btnThemBL);
        leftToolBar.add(btnSuaBL);
        leftToolBar.add(btnXoaBL);
        leftPanel.add(leftToolBar, BorderLayout.NORTH);

        // Table Bảng Lương
        String[] colsBL = {"", "Bảng lương"};
        modelBangLuong = new DefaultTableModel(colsBL, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tableBangLuong = new JTable(modelBangLuong);
        tableBangLuong.setRowHeight(22);
        tableBangLuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableBangLuong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableBangLuong.setShowGrid(true);
        tableBangLuong.setGridColor(new Color(225, 232, 240));

        tableBangLuong.getColumnModel().getColumn(0).setPreferredWidth(35);
        tableBangLuong.getColumnModel().getColumn(1).setPreferredWidth(140);

        tableBangLuong.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0) {
                    JLabel lbl = (JLabel) c;
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    if (isSelected) {
                        lbl.setText("▶ " + (row + 1));
                        lbl.setBackground(new Color(220, 235, 255));
                        lbl.setForeground(new Color(20, 50, 120));
                    } else {
                        lbl.setText(String.valueOf(row + 1));
                        lbl.setBackground(new Color(245, 248, 252));
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                    return lbl;
                }

                if (isSelected) {
                    c.setBackground(new Color(188, 104, 52)); // Màu cam đất đặc trưng
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        tableBangLuong.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableBangLuong.getSelectedRow();
                if (row >= 0 && row < listBangLuong.size()) {
                    currentSelectedBangLuong = listBangLuong.get(row);
                    loadChamCongMatrix();
                }
            }
        });

        tableBangLuong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelectedBangLuong();
                }
            }
        });

        JScrollPane scrollBL = new JScrollPane(tableBangLuong);
        scrollBL.setBorder(null);
        leftPanel.add(scrollBL, BorderLayout.CENTER);
        splitPane.setLeftComponent(leftPanel);

        // ==================== KHUNG PHẢI: BẢNG CHẤM CÔNG ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // Top Status Toolbar
        JPanel topStatusToolbar = new JPanel(new BorderLayout());
        topStatusToolbar.setBackground(new Color(225, 238, 252));
        topStatusToolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        topStatusToolbar.setPreferredSize(new Dimension(800, 36));

        JPanel pnlStatusButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlStatusButtons.setOpaque(false);

        JButton btnKhongCoLich = createStatusButton("Không có lịch", Color.WHITE, Color.BLACK, new Color(190, 205, 225));
        JButton btnDiLam = createStatusButton("Đi làm", COLOR_DI_LAM, Color.WHITE, null);
        JButton btnNghiCoPhep = createStatusButton("Nghỉ có phép", COLOR_NGHI_CO_PHEP, Color.WHITE, null);
        JButton btnNghiKhongPhep = createStatusButton("Nghỉ không phép", COLOR_NGHI_KHONG_PHEP, Color.BLACK, null);

        btnKhongCoLich.addActionListener(e -> applyStatusToSelectedCells(0));
        btnDiLam.addActionListener(e -> applyStatusToSelectedCells(1));
        btnNghiCoPhep.addActionListener(e -> applyStatusToSelectedCells(2));
        btnNghiKhongPhep.addActionListener(e -> applyStatusToSelectedCells(3));

        pnlStatusButtons.add(btnKhongCoLich);
        pnlStatusButtons.add(btnDiLam);
        pnlStatusButtons.add(btnNghiCoPhep);
        pnlStatusButtons.add(btnNghiKhongPhep);
        topStatusToolbar.add(pnlStatusButtons, BorderLayout.WEST);

        JPanel pnlRightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        pnlRightActions.setOpaque(false);

        JButton btnCapNhat = new JButton("Cập nhật", IconHelper.getIcon("save.png", 14, 14));
        btnCapNhat.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCapNhat.setBackground(Color.WHITE);
        btnCapNhat.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225), 1), new EmptyBorder(3, 10, 3, 10)));
        btnCapNhat.setFocusPainted(false);
        btnCapNhat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCapNhat.addActionListener(e -> onSaveAttendanceChanges());

        pnlRightActions.add(btnCapNhat);
        topStatusToolbar.add(pnlRightActions, BorderLayout.EAST);

        rightPanel.add(topStatusToolbar, BorderLayout.NORTH);

        // Center Matrix Table
        modelChamCong = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableChamCong = new JTable(modelChamCong);
        tableChamCong.setRowHeight(20);
        tableChamCong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableChamCong.setShowGrid(true);
        tableChamCong.setGridColor(new Color(205, 215, 225));
        tableChamCong.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Kích hoạt Cell Selection & Drag chọn nhiều ô
        tableChamCong.setCellSelectionEnabled(true);
        tableChamCong.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        tableChamCong.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final JLabel lblText = new JLabel();
            private final JPanel pnlColor = new JPanel();
            private final MatteBorder gridBorder = new MatteBorder(0, 0, 1, 1, new Color(205, 215, 225));
            private final Border selectedBorder = new CompoundBorder(
                    new LineBorder(new Color(0, 100, 220), 1),
                    new MatteBorder(0, 0, 1, 1, new Color(205, 215, 225))
            );

            {
                lblText.setOpaque(true);
                lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (column == 0) {
                    // Cột Nhân viên
                    lblText.setHorizontalAlignment(SwingConstants.LEFT);
                    lblText.setText(value != null ? value.toString() : "");
                    lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lblText.setBackground(Color.WHITE);
                    lblText.setForeground(new Color(20, 30, 50));
                    lblText.setBorder(new CompoundBorder(gridBorder, new EmptyBorder(0, 4, 0, 2)));
                    return lblText;
                }

                if (column == 1) {
                    // Cột Ca
                    lblText.setHorizontalAlignment(SwingConstants.LEFT);
                    lblText.setText(value != null ? value.toString() : "");
                    lblText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lblText.setBackground(Color.WHITE);
                    lblText.setForeground(Color.DARK_GRAY);
                    lblText.setBorder(new CompoundBorder(gridBorder, new EmptyBorder(0, 4, 0, 2)));
                    return lblText;
                }

                // Cột ngày
                int status = 0;
                if (value instanceof Integer) {
                    status = (Integer) value;
                } else if (value instanceof String) {
                    try { status = Integer.parseInt((String) value); } catch (Exception ignored) {}
                }

                Color bg;
                switch (status) {
                    case 1:
                        bg = COLOR_DI_LAM;
                        break;
                    case 2:
                        bg = COLOR_NGHI_CO_PHEP;
                        break;
                    case 3:
                        bg = COLOR_NGHI_KHONG_PHEP;
                        break;
                    case 4:
                        bg = COLOR_KHAC;
                        break;
                    default:
                        bg = Color.WHITE;
                        break;
                }

                pnlColor.setBackground(bg);
                if (isSelected) {
                    if (status == 0) {
                        pnlColor.setBackground(new Color(185, 218, 255)); // Soft blue highlight for selected empty cells
                    }
                    pnlColor.setBorder(selectedBorder);
                } else {
                    pnlColor.setBorder(gridBorder);
                }
                return pnlColor;
            }
        });

        tableChamCong.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showMatrixPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showMatrixPopup(e);
            }
        });

        JScrollPane scrollChamCong = new JScrollPane(tableChamCong);
        scrollChamCong.setBorder(null);
        rightPanel.add(scrollChamCong, BorderLayout.CENTER);

        splitPane.setRightComponent(rightPanel);
        add(splitPane, BorderLayout.CENTER);
    }

    private JButton createLeftToolButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(2, 6, 2, 6)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createStatusButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(new CompoundBorder(
                new LineBorder(border != null ? border : bg.darker(), 1),
                new EmptyBorder(3, 12, 3, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadBangLuongList() {
        listBangLuong = bangLuongDAO.getAll();
        modelBangLuong.setRowCount(0);
        for (BangLuong bl : listBangLuong) {
            modelBangLuong.addRow(new Object[]{
                    "",
                    bl.getSoPhieu() != null ? bl.getSoPhieu() : ("Tháng " + bl.getThang() + "/" + bl.getNam())
            });
        }
        if (!listBangLuong.isEmpty()) {
            tableBangLuong.setRowSelectionInterval(0, 0);
            currentSelectedBangLuong = listBangLuong.get(0);
            loadChamCongMatrix();
        } else {
            currentSelectedBangLuong = null;
            modelChamCong.setRowCount(0);
            modelChamCong.setColumnCount(0);
        }
    }

    public void loadChamCongMatrix() {
        if (currentSelectedBangLuong == null) return;

        int thang = currentSelectedBangLuong.getThang() > 0 ? currentSelectedBangLuong.getThang() : 7;
        int nam = currentSelectedBangLuong.getNam() > 0 ? currentSelectedBangLuong.getNam() : 2018;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, nam);
        cal.set(Calendar.MONTH, thang - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Build 2-Line Header Columns
        List<String> headers = new ArrayList<>();
        headers.add("Nhân viên");
        headers.add("Ca");

        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int d = 1; d <= maxDays; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1 = Sunday (CN), 2 = Monday (T2)...
            String dowName = dayNames[dayOfWeek - 1];
            headers.add("<html><center>" + d + "<br><font size='2'>" + dowName + "</font></center></html>");
        }

        modelChamCong.setDataVector(new Object[0][0], headers.toArray());

        // Setup Column Widths & Custom Header Renderer
        JTableHeader tableHeader = tableChamCong.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 36));
        tableHeader.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tableHeader.setBackground(new Color(230, 240, 252));
        tableHeader.setForeground(new Color(20, 40, 80));
        tableHeader.setReorderingAllowed(false);

        tableChamCong.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableChamCong.getColumnModel().getColumn(1).setPreferredWidth(70);

        for (int c = 2; c < tableChamCong.getColumnCount(); c++) {
            tableChamCong.getColumnModel().getColumn(c).setPreferredWidth(26);
        }

        // Build Rows
        List<NhanVien> employees = nhanVienDAO.getAllActive();
        List<CaLamViec> shifts = caLamViecDAO.getAllActive();
        if (shifts.isEmpty()) {
            shifts.add(new CaLamViec("ca-3", "Ca sáng", 100));
            shifts.add(new CaLamViec("ca-1", "Ca chiều", 100));
            shifts.add(new CaLamViec("ca-2", "ca đêm", 120));
        }

        // Sort shifts: Ca sáng -> Ca chiều -> ca đêm
        shifts.sort((c1, c2) -> {
            String n1 = c1.getName().toLowerCase();
            String n2 = c2.getName().toLowerCase();
            int o1 = n1.contains("sáng") ? 1 : (n1.contains("chiều") ? 2 : (n1.contains("đêm") ? 3 : 4));
            int o2 = n2.contains("sáng") ? 1 : (n2.contains("chiều") ? 2 : (n2.contains("đêm") ? 3 : 4));
            return Integer.compare(o1, o2);
        });

        matrixRows.clear();
        String blId = currentSelectedBangLuong.getId();

        for (NhanVien nv : employees) {
            if (nv.getStatus() < 0 || nv.isFolder() || nv.isSeparator()) continue;

            for (int s = 0; s < shifts.size(); s++) {
                CaLamViec ca = shifts.get(s);
                boolean isFirstShift = (s == 0);
                matrixRows.add(new RowInfo(nv, ca, isFirstShift));

                List<Object> rowData = new ArrayList<>();
                rowData.add(isFirstShift ? nv.getName() : "");
                rowData.add(ca.getName());

                for (int d = 1; d <= maxDays; d++) {
                    int st = bangLuongDAO.getAttendanceStatus(blId, nv.getId(), ca.getId(), d);
                    rowData.add(st);
                }

                modelChamCong.addRow(rowData.toArray());
            }
        }
    }

    private void applyStatusToSelectedCells(int newStatus) {
        int[] selectedRows = tableChamCong.getSelectedRows();
        int[] selectedCols = tableChamCong.getSelectedColumns();

        if (selectedRows.length == 0 || selectedCols.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn (quét) các ô ngày trên bảng để chấm công!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int r : selectedRows) {
            for (int c : selectedCols) {
                if (c >= 2) { // Chỉ áp dụng cho các cột ngày
                    modelChamCong.setValueAt(newStatus, r, c);
                }
            }
        }
        tableChamCong.repaint();
    }

    private void onSaveAttendanceChanges() {
        if (currentSelectedBangLuong == null) return;
        String blId = currentSelectedBangLuong.getId();
        int thang = currentSelectedBangLuong.getThang() > 0 ? currentSelectedBangLuong.getThang() : 7;
        int nam = currentSelectedBangLuong.getNam() > 0 ? currentSelectedBangLuong.getNam() : 2018;

        int rowCount = modelChamCong.getRowCount();
        int colCount = modelChamCong.getColumnCount();

        List<ChamCongChiTiet> records = new ArrayList<>();
        for (int r = 0; r < rowCount; r++) {
            if (r >= matrixRows.size()) break;
            RowInfo rowInfo = matrixRows.get(r);

            for (int c = 2; c < colCount; c++) {
                int day = c - 1;
                Object val = modelChamCong.getValueAt(r, c);
                int status = 0;
                if (val instanceof Integer) {
                    status = (Integer) val;
                } else if (val != null) {
                    try { status = Integer.parseInt(val.toString()); } catch (Exception ignored) {}
                }
                records.add(new ChamCongChiTiet(blId, rowInfo.nhanVien.getId(), rowInfo.caLamViec.getId(), day, status));
            }
        }

        boolean ok = bangLuongDAO.saveAllAttendance(blId, thang, nam, records);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu chấm công thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Đã lưu dữ liệu chấm công vào hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onAddNewBangLuong() {
        TaoBangLuongDialog dlg = new TaoBangLuongDialog(SwingUtilities.getWindowAncestor(this), null, this::loadBangLuongList);
        dlg.setVisible(true);
    }

    private void onEditSelectedBangLuong() {
        if (currentSelectedBangLuong == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bảng lương bên trái để sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TaoBangLuongDialog dlg = new TaoBangLuongDialog(SwingUtilities.getWindowAncestor(this), currentSelectedBangLuong, this::loadBangLuongList);
        dlg.setVisible(true);
    }

    private void onDeleteSelectedBangLuong() {
        if (currentSelectedBangLuong == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bảng lương bên trái để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int cf = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa bảng lương [" + currentSelectedBangLuong.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (cf == JOptionPane.YES_OPTION) {
            bangLuongDAO.delete(currentSelectedBangLuong.getId());
            loadBangLuongList();
        }
    }

    private void showMatrixPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miKhongCoLich = new JMenuItem("Không có lịch (0)");
        miKhongCoLich.addActionListener(ev -> applyStatusToSelectedCells(0));
        popup.add(miKhongCoLich);

        JMenuItem miDiLam = new JMenuItem("Đi làm (Đỏ)");
        miDiLam.addActionListener(ev -> applyStatusToSelectedCells(1));
        popup.add(miDiLam);

        JMenuItem miNghiCoPhep = new JMenuItem("Nghỉ có phép (Xanh lá)");
        miNghiCoPhep.addActionListener(ev -> applyStatusToSelectedCells(2));
        popup.add(miNghiCoPhep);

        JMenuItem miNghiKhongPhep = new JMenuItem("Nghỉ không phép (Cyan)");
        miNghiKhongPhep.addActionListener(ev -> applyStatusToSelectedCells(3));
        popup.add(miNghiKhongPhep);

        popup.addSeparator();

        JMenuItem miSave = new JMenuItem("Cập nhật (Lưu thay đổi)", IconHelper.getIcon("save.png", 16, 16));
        miSave.addActionListener(ev -> onSaveAttendanceChanges());
        popup.add(miSave);

        JMenuItem miRefresh = new JMenuItem("Làm mới (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        miRefresh.addActionListener(ev -> loadChamCongMatrix());
        popup.add(miRefresh);

        popup.show(tableChamCong, e.getX(), e.getY());
    }
}
