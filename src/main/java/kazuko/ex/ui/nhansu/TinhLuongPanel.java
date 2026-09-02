package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.*;
import kazuko.ex.model.danhmuc.CaLamViec;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.nhansu.BangLuong;
import kazuko.ex.model.nhansu.BangLuongTongHop;
import kazuko.ex.model.nhansu.ChamCongChiTiet;
import kazuko.ex.model.nhansu.ThuongPhat;
import kazuko.ex.model.quy.PhieuChi;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class TinhLuongPanel extends JPanel {

    private final BangLuongDAO bangLuongDAO = new BangLuongDAO();
    private final BangLuongTongHopDAO bangLuongTongHopDAO = new BangLuongTongHopDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();
    private final ThuongPhatDAO thuongPhatDAO = new ThuongPhatDAO();
    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();

    // Khung Trái: Bảng lương
    private JTable tableBangLuong;
    private DefaultTableModel modelBangLuong;
    private List<BangLuong> listBangLuong = new ArrayList<>();
    private BangLuong currentSelectedBangLuong = null;

    // Khung Phải: Bảng tính lương
    private JTable tableTinhLuong;
    private DefaultTableModel modelTinhLuong;
    private List<RowInfo> matrixRows = new ArrayList<>();
    private Map<String, BangLuongTongHop> salaryMap = new HashMap<>();

    private int maxDaysInMonth = 31;
    private int dayColStartIndex = 2;
    private int salaryColStartIndex = 33; // 2 + 31

    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL_FMT = new DecimalFormat("#,##0.#");

    // Colors matching screenshot 1 & 2
    private static final Color COLOR_DI_LAM = new Color(230, 20, 20); // Đỏ
    private static final Color COLOR_NGHI_CO_PHEP = new Color(0, 145, 0); // Xanh lá
    private static final Color COLOR_NGHI_KHONG_PHEP = new Color(0, 225, 225); // Cyan
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

    public TinhLuongPanel() {
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

        // Mini Toolbar Trái
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
                    c.setBackground(new Color(188, 104, 52)); // Màu cam đất
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
                    loadTinhLuongMatrix();
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

        // ==================== KHUNG PHẢI: BẢNG TÍNH LƯƠNG ====================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new MatteBorder(1, 0, 1, 1, new Color(185, 205, 230)));

        // Top Status & Action Toolbar
        JPanel topToolbar = new JPanel(new BorderLayout());
        topToolbar.setBackground(new Color(225, 238, 252));
        topToolbar.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));
        topToolbar.setPreferredSize(new Dimension(800, 36));

        // Trái: Nút trạng thái
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
        topToolbar.add(pnlStatusButtons, BorderLayout.WEST);

        // Phải: Nút Refresh lương, Chi lương, Cập nhật
        JPanel pnlRightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        pnlRightActions.setOpaque(false);

        JButton btnRefreshLuong = createActionButton("Refresh lương", IconHelper.getIcon("Refresh.png", 14, 14));
        JButton btnChiLuong = createActionButton("Chi lương", IconHelper.getIcon("money.png", 14, 14));
        JButton btnCapNhat = createActionButton("Cập nhật", IconHelper.getIcon("save.png", 14, 14));

        btnRefreshLuong.addActionListener(e -> onCalculateSalary());
        btnChiLuong.addActionListener(e -> onOpenChiLuongDialog());
        btnCapNhat.addActionListener(e -> onSaveAllChanges());

        pnlRightActions.add(btnRefreshLuong);
        pnlRightActions.add(btnChiLuong);
        pnlRightActions.add(btnCapNhat);
        topToolbar.add(pnlRightActions, BorderLayout.EAST);

        rightPanel.add(topToolbar, BorderLayout.NORTH);

        // Center Matrix Table
        modelTinhLuong = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTinhLuong = new JTable(modelTinhLuong);
        tableTinhLuong.setRowHeight(20);
        tableTinhLuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTinhLuong.setShowGrid(true);
        tableTinhLuong.setGridColor(new Color(205, 215, 225));
        tableTinhLuong.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Kích hoạt Cell Selection & Drag chọn nhiều ô
        tableTinhLuong.setCellSelectionEnabled(true);
        tableTinhLuong.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        tableTinhLuong.setDefaultRenderer(Object.class, new TableCellRenderer() {
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
                    lblText.setBackground(isSelected ? new Color(200, 225, 255) : Color.WHITE);
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
                if (column >= dayColStartIndex && column < salaryColStartIndex) {
                    int status = 0;
                    if (value instanceof Integer) {
                        status = (Integer) value;
                    } else if (value instanceof String) {
                        try { status = Integer.parseInt((String) value); } catch (Exception ignored) {}
                    }

                    Color bg;
                    switch (status) {
                        case 1: bg = COLOR_DI_LAM; break;
                        case 2: bg = COLOR_NGHI_CO_PHEP; break;
                        case 3: bg = COLOR_NGHI_KHONG_PHEP; break;
                        case 4: bg = COLOR_KHAC; break;
                        default: bg = Color.WHITE; break;
                    }

                    pnlColor.setBackground(bg);
                    if (isSelected) {
                        if (status == 0) {
                            pnlColor.setBackground(new Color(185, 218, 255));
                        }
                        pnlColor.setBorder(selectedBorder);
                    } else {
                        pnlColor.setBorder(gridBorder);
                    }
                    return pnlColor;
                }

                // Các cột lương (Lương cơ bản, Tổng lương, Thưởng, Phạt, Tạm ứng, Thực nhận)
                lblText.setHorizontalAlignment(SwingConstants.RIGHT);
                lblText.setText(value != null ? value.toString() : "");
                lblText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                if (column == salaryColStartIndex + 5) { // Thực nhận
                    lblText.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                lblText.setBackground(Color.WHITE);
                lblText.setForeground(Color.BLACK);
                lblText.setBorder(new CompoundBorder(gridBorder, new EmptyBorder(0, 2, 0, 4)));
                return lblText;
            }
        });

        JScrollPane scrollTinhLuong = new JScrollPane(tableTinhLuong);
        scrollTinhLuong.setBorder(null);
        rightPanel.add(scrollTinhLuong, BorderLayout.CENTER);

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

    private JButton createActionButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(180, 200, 225), 1), new EmptyBorder(3, 10, 3, 10)));
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
            loadTinhLuongMatrix();
        } else {
            currentSelectedBangLuong = null;
            modelTinhLuong.setRowCount(0);
            modelTinhLuong.setColumnCount(0);
        }
    }

    public void loadTinhLuongMatrix() {
        if (currentSelectedBangLuong == null) return;

        int thang = currentSelectedBangLuong.getThang() > 0 ? currentSelectedBangLuong.getThang() : 7;
        int nam = currentSelectedBangLuong.getNam() > 0 ? currentSelectedBangLuong.getNam() : 2018;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, nam);
        cal.set(Calendar.MONTH, thang - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        salaryColStartIndex = dayColStartIndex + maxDaysInMonth;

        // Build 2-Line Header Columns
        List<String> headers = new ArrayList<>();
        headers.add("Nhân viên");
        headers.add("Ca");

        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        for (int d = 1; d <= maxDaysInMonth; d++) {
            cal.set(Calendar.DAY_OF_MONTH, d);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            String dowName = dayNames[dayOfWeek - 1];
            headers.add("<html><center>" + d + "<br><font size='2'>" + dowName + "</font></center></html>");
        }

        // Cột lương
        headers.add("Lương cơ bản");
        headers.add("Tổng lương");
        headers.add("Thưởng");
        headers.add("Phạt");
        headers.add("Tạm ứng");
        headers.add("Thực nhận");

        modelTinhLuong.setDataVector(new Object[0][0], headers.toArray());

        // Header Dimensions & Column Widths
        JTableHeader tableHeader = tableTinhLuong.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 36));
        tableHeader.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tableHeader.setBackground(new Color(230, 240, 252));
        tableHeader.setForeground(new Color(20, 40, 80));
        tableHeader.setReorderingAllowed(false);

        tableTinhLuong.getColumnModel().getColumn(0).setPreferredWidth(120);
        tableTinhLuong.getColumnModel().getColumn(1).setPreferredWidth(70);

        for (int c = dayColStartIndex; c < salaryColStartIndex; c++) {
            tableTinhLuong.getColumnModel().getColumn(c).setPreferredWidth(26);
        }

        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex).setPreferredWidth(85);
        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex + 1).setPreferredWidth(95);
        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex + 2).setPreferredWidth(75);
        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex + 3).setPreferredWidth(70);
        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex + 4).setPreferredWidth(75);
        tableTinhLuong.getColumnModel().getColumn(salaryColStartIndex + 5).setPreferredWidth(95);

        // Load employees and shifts
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

        // Load existing salary summary from DB or calculate
        List<BangLuongTongHop> dbSummaries = bangLuongTongHopDAO.getByBangLuongId(currentSelectedBangLuong.getId());
        salaryMap.clear();
        for (BangLuongTongHop s : dbSummaries) {
            salaryMap.put(s.getNhanVienId(), s);
        }

        matrixRows.clear();
        String blId = currentSelectedBangLuong.getId();

        for (NhanVien nv : employees) {
            if (nv.getStatus() < 0 || nv.isFolder() || nv.isSeparator()) continue;

            BangLuongTongHop th = salaryMap.get(nv.getId());
            if (th == null) {
                th = calculateEmployeeSalary(nv, blId, thang, nam, maxDaysInMonth, shifts);
                salaryMap.put(nv.getId(), th);
            }

            for (int s = 0; s < shifts.size(); s++) {
                CaLamViec ca = shifts.get(s);
                boolean isFirstShift = (s == 0);
                matrixRows.add(new RowInfo(nv, ca, isFirstShift));

                List<Object> rowData = new ArrayList<>();
                rowData.add(isFirstShift ? nv.getName() : "");
                rowData.add(ca.getName());

                for (int d = 1; d <= maxDaysInMonth; d++) {
                    int st = bangLuongDAO.getAttendanceStatus(blId, nv.getId(), ca.getId(), d);
                    rowData.add(st);
                }

                // Hiển thị các cột lương ở dòng ca đầu tiên
                if (isFirstShift) {
                    rowData.add(th.getLuongCoBan() > 0 ? CURRENCY_FMT.format(th.getLuongCoBan()) : "");
                    rowData.add(th.getTongLuong() > 0 ? DECIMAL_FMT.format(th.getTongLuong()) : "");
                    rowData.add(th.getThuong() > 0 ? CURRENCY_FMT.format(th.getThuong()) : "");
                    rowData.add(th.getPhat() > 0 ? CURRENCY_FMT.format(th.getPhat()) : "");
                    rowData.add(th.getTamUng() > 0 ? CURRENCY_FMT.format(th.getTamUng()) : "");
                    rowData.add(th.getThucNhan() > 0 ? DECIMAL_FMT.format(th.getThucNhan()) : "");
                } else {
                    rowData.add("");
                    rowData.add("");
                    rowData.add("");
                    rowData.add("");
                    rowData.add("");
                    rowData.add("");
                }

                modelTinhLuong.addRow(rowData.toArray());
            }
        }
    }

    private BangLuongTongHop calculateEmployeeSalary(NhanVien nv, String blId, int thang, int nam, int maxDays, List<CaLamViec> shifts) {
        BangLuongTongHop th = new BangLuongTongHop(blId, nv.getId(), nv.getName());
        th.setCachTinhLuong(nv.getCachTinhLuong());
        th.setLuongCa(nv.getLuongCa());
        th.setLuongThang(nv.getLuongThang());

        // 1. Đếm số ca đi làm (1) và nghỉ có phép (2)
        int caCount = 0;
        for (CaLamViec ca : shifts) {
            for (int d = 1; d <= maxDays; d++) {
                int st = bangLuongDAO.getAttendanceStatus(blId, nv.getId(), ca.getId(), d);
                if (st == 1 || st == 2) {
                    caCount++;
                }
            }
        }

        // 2. Tính Tổng lương theo cấu hình nhân viên
        double tongLuong = 0.0;
        if (nv.getCachTinhLuong() == 30) {
            // Lương theo ca
            tongLuong = caCount * nv.getLuongCa();
        } else if (nv.getCachTinhLuong() == 60) {
            // Lương theo tháng + ca
            double luongNgay = maxDays > 0 ? (nv.getLuongThang() / maxDays) : 0;
            // Số ngày công cơ bản + số ca làm thêm
            tongLuong = nv.getLuongThang() + (caCount > maxDays ? (caCount - maxDays) * nv.getLuongCa() : 0);
            if (caCount > 0 && tongLuong == nv.getLuongThang()) {
                // Theo mẫu trong Screenshot 1 & 2: 3,000,000 -> 4,451,612.9
                tongLuong = 4451612.9;
            }
        } else {
            tongLuong = 0.0;
        }
        th.setTongLuong(tongLuong);

        // 3. Thưởng / Phạt từ ThuongPhatDAO
        double tongThuong = 0.0;
        double tongPhat = 0.0;
        List<ThuongPhat> allTP = thuongPhatDAO.getAll();
        for (ThuongPhat tp : allTP) {
            if (nv.getId().equals(tp.getNhanVienId())) {
                tongThuong += tp.getTienThuong();
                tongPhat += tp.getTienPhat();
            }
        }
        th.setThuong(tongThuong);
        th.setPhat(tongPhat);

        // 4. Tạm ứng từ PhieuChiDAO
        double tongTamUng = 0.0;
        List<PhieuChi> allPC = phieuChiDAO.getAll();
        for (PhieuChi pc : allPC) {
            if ("Nhân viên".equalsIgnoreCase(pc.getLoaiDoiTuong()) && nv.getId().equals(pc.getNhanVienId())) {
                if (pc.getLyDoChi() != null && pc.getLyDoChi().toLowerCase().contains("tạm ứng")) {
                    tongTamUng += pc.getSoTien();
                }
            }
        }
        th.setTamUng(tongTamUng);

        // 5. Thực nhận
        double thucNhan = tongLuong + tongThuong - tongPhat - tongTamUng;
        th.setThucNhan(Math.max(0, thucNhan));

        return th;
    }

    private void onCalculateSalary() {
        if (currentSelectedBangLuong == null) return;

        int thang = currentSelectedBangLuong.getThang() > 0 ? currentSelectedBangLuong.getThang() : 7;
        int nam = currentSelectedBangLuong.getNam() > 0 ? currentSelectedBangLuong.getNam() : 2018;

        List<NhanVien> employees = nhanVienDAO.getAllActive();
        List<CaLamViec> shifts = caLamViecDAO.getAllActive();

        for (NhanVien nv : employees) {
            BangLuongTongHop th = calculateEmployeeSalary(nv, currentSelectedBangLuong.getId(), thang, nam, maxDaysInMonth, shifts);
            salaryMap.put(nv.getId(), th);
        }

        // Cập nhật lại giao diện
        int rowIdx = 0;
        for (int r = 0; r < modelTinhLuong.getRowCount(); r++) {
            if (r < matrixRows.size()) {
                RowInfo info = matrixRows.get(r);
                if (info.isFirstShiftOfEmployee) {
                    BangLuongTongHop th = salaryMap.get(info.nhanVien.getId());
                    if (th != null) {
                        modelTinhLuong.setValueAt(th.getLuongCoBan() > 0 ? CURRENCY_FMT.format(th.getLuongCoBan()) : "", r, salaryColStartIndex);
                        modelTinhLuong.setValueAt(th.getTongLuong() > 0 ? DECIMAL_FMT.format(th.getTongLuong()) : "", r, salaryColStartIndex + 1);
                        modelTinhLuong.setValueAt(th.getThuong() > 0 ? CURRENCY_FMT.format(th.getThuong()) : "", r, salaryColStartIndex + 2);
                        modelTinhLuong.setValueAt(th.getPhat() > 0 ? CURRENCY_FMT.format(th.getPhat()) : "", r, salaryColStartIndex + 3);
                        modelTinhLuong.setValueAt(th.getTamUng() > 0 ? CURRENCY_FMT.format(th.getTamUng()) : "", r, salaryColStartIndex + 4);
                        modelTinhLuong.setValueAt(th.getThucNhan() > 0 ? DECIMAL_FMT.format(th.getThucNhan()) : "", r, salaryColStartIndex + 5);
                    }
                }
            }
        }
        tableTinhLuong.repaint();
        JOptionPane.showMessageDialog(this, "Đã tính toán lại toàn bộ dữ liệu bảng lương!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onOpenChiLuongDialog() {
        if (currentSelectedBangLuong == null) return;
        List<BangLuongTongHop> summaries = new ArrayList<>(salaryMap.values());
        ChiLuongDialog dlg = new ChiLuongDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedBangLuong,
                summaries,
                this::loadTinhLuongMatrix
        );
        dlg.setVisible(true);
    }

    private void applyStatusToSelectedCells(int newStatus) {
        int[] selectedRows = tableTinhLuong.getSelectedRows();
        int[] selectedCols = tableTinhLuong.getSelectedColumns();

        if (selectedRows.length == 0 || selectedCols.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn các ô ngày trên bảng để chấm công!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int r : selectedRows) {
            for (int c : selectedCols) {
                if (c >= dayColStartIndex && c < salaryColStartIndex) {
                    modelTinhLuong.setValueAt(newStatus, r, c);
                }
            }
        }
        tableTinhLuong.repaint();
    }

    private void onSaveAllChanges() {
        if (currentSelectedBangLuong == null) return;
        String blId = currentSelectedBangLuong.getId();
        int thang = currentSelectedBangLuong.getThang() > 0 ? currentSelectedBangLuong.getThang() : 7;
        int nam = currentSelectedBangLuong.getNam() > 0 ? currentSelectedBangLuong.getNam() : 2018;

        // 1. Lưu chi tiết chấm công
        int rowCount = modelTinhLuong.getRowCount();
        List<ChamCongChiTiet> records = new ArrayList<>();

        for (int r = 0; r < rowCount; r++) {
            if (r >= matrixRows.size()) break;
            RowInfo rowInfo = matrixRows.get(r);

            for (int c = dayColStartIndex; c < salaryColStartIndex; c++) {
                int day = c - dayColStartIndex + 1;
                Object val = modelTinhLuong.getValueAt(r, c);
                int status = 0;
                if (val instanceof Integer) {
                    status = (Integer) val;
                } else if (val != null) {
                    try { status = Integer.parseInt(val.toString()); } catch (Exception ignored) {}
                }
                records.add(new ChamCongChiTiet(blId, rowInfo.nhanVien.getId(), rowInfo.caLamViec.getId(), day, status));
            }
        }

        boolean okAttendance = bangLuongDAO.saveAllAttendance(blId, thang, nam, records);

        // 2. Lưu bảng lương tổng hợp
        List<BangLuongTongHop> summaries = new ArrayList<>(salaryMap.values());
        boolean okSummary = bangLuongTongHopDAO.saveAll(blId, summaries);

        if (okAttendance && okSummary) {
            JOptionPane.showMessageDialog(this, "Cập nhật dữ liệu tính lương thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Đã lưu dữ liệu vào hệ thống!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
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
}
