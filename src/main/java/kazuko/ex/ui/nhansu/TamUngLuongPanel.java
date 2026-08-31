package kazuko.ex.ui.nhansu;

import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.quy.TaoPhieuChiDialog;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.ExcelExporter;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TamUngLuongPanel extends JPanel {

    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private DatePickerField dpkTuNgay;
    private DatePickerField dpkDenNgay;
    private JButton btnTaoMoi;
    private JButton btnChinhSua;
    private JButton btnXoa;

    private JTable tableTamUng;
    private DefaultTableModel tableModel;
    private List<PhieuChi> currentList = new ArrayList<>();
    private PhieuChi currentSelectedPhieuChi = null;

    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");

    public TamUngLuongPanel() {
        initComponents();
        loadTableData();
        setupShortcuts();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(230, 240, 252));

        // ==================== 1. TOP TOOLBAR & FILTER ====================
        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.setOpaque(false);

        // Row 1: Filter Date (Từ: __/__/____ Đến __/__/____)
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlFilter.setBackground(new Color(225, 238, 252));
        pnlFilter.setBorder(new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)));

        JLabel lblTu = new JLabel("Từ:");
        lblTu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTu.setForeground(new Color(30, 40, 60));

        dpkTuNgay = new DatePickerField();
        dpkTuNgay.setDate(null); // Trống như hình
        dpkTuNgay.setPreferredSize(new Dimension(115, 24));
        dpkTuNgay.addPropertyChangeListener("date", evt -> loadTableData());

        JLabel lblDen = new JLabel("Đến");
        lblDen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDen.setForeground(new Color(30, 40, 60));

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(null); // Trống như hình
        dpkDenNgay.setPreferredSize(new Dimension(115, 24));
        dpkDenNgay.addPropertyChangeListener("date", evt -> loadTableData());

        pnlFilter.add(lblTu);
        pnlFilter.add(dpkTuNgay);
        pnlFilter.add(lblDen);
        pnlFilter.add(dpkDenNgay);
        pnlNorth.add(pnlFilter);

        // Row 2: Action Buttons (+ Tạo mới, Chỉnh sửa, Xóa)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlActions.setBackground(new Color(225, 238, 252));
        pnlActions.setBorder(new MatteBorder(0, 0, 1, 0, new Color(180, 200, 225)));

        btnTaoMoi = createActionButton("Tạo mới", IconHelper.getIcon("plus.png", 14, 14));
        btnChinhSua = createActionButton("Chỉnh sửa", IconHelper.getIcon("pencil.png", 14, 14));
        btnXoa = createActionButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));

        btnTaoMoi.addActionListener(e -> onAddNew());
        btnChinhSua.addActionListener(e -> onEditSelected());
        btnXoa.addActionListener(e -> onDeleteSelected());

        pnlActions.add(btnTaoMoi);
        pnlActions.add(btnChinhSua);
        pnlActions.add(btnXoa);
        pnlNorth.add(pnlActions);

        add(pnlNorth, BorderLayout.NORTH);

        // ==================== 2. CENTER TABLE ====================
        String[] columns = {"", "Số phiếu", "Ngày", "Người nhận", "Số tiền"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTamUng = new JTable(tableModel);
        tableTamUng.setRowHeight(22);
        tableTamUng.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTamUng.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTamUng.setShowGrid(true);
        tableTamUng.setGridColor(new Color(225, 232, 240));
        tableTamUng.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        int[] widths = {35, 110, 100, 200, 120};
        for (int i = 0; i < widths.length; i++) {
            if (i < tableTamUng.getColumnModel().getColumnCount()) {
                tableTamUng.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
        }

        // Table Header Styling & Quick Menu
        JTableHeader header = tableTamUng.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(230, 240, 252));
        header.setForeground(new Color(30, 60, 110));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 26));

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        // Custom Cell Renderer
        tableTamUng.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    // STT kèm con trỏ ▶
                    JLabel lbl = (JLabel) c;
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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

                if (column == 1 && isSelected) {
                    // Cột Số phiếu khi được chọn có màu cam đất đặc trưng
                    c.setBackground(new Color(188, 104, 52));
                    c.setForeground(Color.WHITE);
                    return c;
                }

                if (column == 4) {
                    // Cột Số tiền căn phải
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.RIGHT);
                    c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                } else if (column == 2) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (isSelected) {
                    // Soft green / highlight background
                    c.setBackground(new Color(225, 245, 230));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 254));
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });

        tableTamUng.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tableTamUng.getSelectedRow();
                if (selectedRow >= 0 && selectedRow < currentList.size()) {
                    currentSelectedPhieuChi = currentList.get(selectedRow);
                } else {
                    currentSelectedPhieuChi = null;
                }
            }
        });

        tableTamUng.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onEditSelected();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showTablePopup(e);
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableTamUng);
        tableScroll.setBorder(null);
        add(tableScroll, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 8, 3, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void loadTableData() {
        List<PhieuChi> all = phieuChiDAO.getAll();

        Date tuNgay = dpkTuNgay.getDate() != null ? getStartOfDay(dpkTuNgay.getDate()) : null;
        Date denNgay = dpkDenNgay.getDate() != null ? getEndOfDay(dpkDenNgay.getDate()) : null;

        List<PhieuChi> filtered = all.stream().filter(pc -> {
            // Lọc loại đối tượng là Nhân viên hoặc lý do tạm ứng lương
            boolean isNhanVien = "Nhân viên".equalsIgnoreCase(pc.getLoaiDoiTuong())
                    || (pc.getNhanVienId() != null && !pc.getNhanVienId().trim().isEmpty())
                    || (pc.getTenPhanLoai() != null && pc.getTenPhanLoai().toLowerCase().contains("tạm ứng"))
                    || (pc.getLyDoChi() != null && pc.getLyDoChi().toLowerCase().contains("tạm ứng"));

            if (!isNhanVien) return false;

            // Lọc ngày
            if (tuNgay != null && pc.getNgay() != null && pc.getNgay().before(tuNgay)) return false;
            if (denNgay != null && pc.getNgay() != null && pc.getNgay().after(denNgay)) return false;

            return true;
        }).collect(Collectors.toList());

        currentList = filtered;

        tableModel.setRowCount(0);
        for (PhieuChi pc : currentList) {
            String ngayStr = pc.getNgay() != null ? sdfDate.format(pc.getNgay()) : "";
            String nguoiNhan = (pc.getTenNhanVien() != null && !pc.getTenNhanVien().trim().isEmpty()) ?
                    pc.getTenNhanVien() : (pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : "");

            // Format raw integer/amount like 134 or 10000 matching screenshot
            String soTienStr = pc.getSoTien() == (long) pc.getSoTien() ?
                    String.valueOf((long) pc.getSoTien()) : CurrencyUtil.format(pc.getSoTien());

            tableModel.addRow(new Object[]{
                    "",
                    pc.getSoPhieu() != null ? pc.getSoPhieu() : "",
                    ngayStr,
                    nguoiNhan,
                    soTienStr
            });
        }

        if (!currentList.isEmpty()) {
            tableTamUng.setRowSelectionInterval(0, 0);
        }
    }

    private void onAddNew() {
        LyDoThuChi defaultTamUng = null;
        for (LyDoThuChi tc : lyDoThuChiDAO.getAllActive()) {
            if (tc.isLaLyDoChi() && tc.getName() != null && tc.getName().toLowerCase().contains("tạm ứng")) {
                defaultTamUng = tc;
                break;
            }
        }

        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(
                SwingUtilities.getWindowAncestor(this),
                null,
                defaultTamUng,
                "Nhân viên",
                this::loadTableData
        );
        dlg.setVisible(true);
    }

    private void onEditSelected() {
        if (currentSelectedPhieuChi == null) {
            int row = tableTamUng.getSelectedRow();
            if (row >= 0 && row < currentList.size()) {
                currentSelectedPhieuChi = currentList.get(row);
            }
        }
        if (currentSelectedPhieuChi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tạm ứng trên bảng để chỉnh sửa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaoPhieuChiDialog dlg = new TaoPhieuChiDialog(
                SwingUtilities.getWindowAncestor(this),
                currentSelectedPhieuChi,
                null,
                "Nhân viên",
                this::loadTableData
        );
        dlg.setVisible(true);
    }

    private void onDeleteSelected() {
        if (currentSelectedPhieuChi == null) {
            int row = tableTamUng.getSelectedRow();
            if (row >= 0 && row < currentList.size()) {
                currentSelectedPhieuChi = currentList.get(row);
            }
        }
        if (currentSelectedPhieuChi == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phiếu tạm ứng trên bảng để xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu tạm ứng [" + currentSelectedPhieuChi.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            phieuChiDAO.delete(currentSelectedPhieuChi.getId());
            loadTableData();
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu tạm ứng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void autoResizeColumnWidths() {
        for (int column = 0; column < tableTamUng.getColumnCount(); column++) {
            TableColumn tableColumn = tableTamUng.getColumnModel().getColumn(column);
            int preferredWidth = 40;

            // Header width
            TableCellRenderer headerRenderer = tableTamUng.getTableHeader().getDefaultRenderer();
            if (headerRenderer != null) {
                Component headerComp = headerRenderer.getTableCellRendererComponent(
                        tableTamUng, tableColumn.getHeaderValue(), false, false, 0, column);
                if (headerComp != null) {
                    preferredWidth = Math.max(preferredWidth, headerComp.getPreferredSize().width + 20);
                }
            }

            // Sample rows width
            int rowCount = Math.min(tableTamUng.getRowCount(), 100);
            for (int row = 0; row < rowCount; row++) {
                TableCellRenderer cellRenderer = tableTamUng.getCellRenderer(row, column);
                if (cellRenderer != null) {
                    Component c = tableTamUng.prepareRenderer(cellRenderer, row, column);
                    if (c != null) {
                        int width = c.getPreferredSize().width + tableTamUng.getIntercellSpacing().width + 16;
                        preferredWidth = Math.max(preferredWidth, width);
                    }
                }
            }

            tableColumn.setPreferredWidth(Math.min(preferredWidth, 350));
        }
        tableTamUng.getTableHeader().repaint();
    }

    private void showTablePopup(MouseEvent e) {
        int row = tableTamUng.rowAtPoint(e.getPoint());
        if (row >= 0 && row < tableTamUng.getRowCount()) {
            tableTamUng.setRowSelectionInterval(row, row);
        }

        JPopupMenu popup = new JPopupMenu();

        JMenuItem miAdd = new JMenuItem("Tạo mới (Insert)", IconHelper.getIcon("plus.png", 16, 16));
        miAdd.addActionListener(ev -> onAddNew());
        popup.add(miAdd);

        JMenuItem miEdit = new JMenuItem("Chỉnh sửa (F4)", IconHelper.getIcon("pencil.png", 16, 16));
        miEdit.addActionListener(ev -> onEditSelected());
        popup.add(miEdit);

        JMenuItem miDel = new JMenuItem("Xóa (Del)", IconHelper.getIcon("cross.png", 16, 16));
        miDel.addActionListener(ev -> onDeleteSelected());
        popup.add(miDel);

        popup.addSeparator();

        JMenuItem miAutoResize = new JMenuItem("Tự động dãn cột");
        miAutoResize.addActionListener(ev -> autoResizeColumnWidths());
        popup.add(miAutoResize);

        JMenuItem miExcel = new JMenuItem("Xuất Excel", IconHelper.getIcon("export-excel.png", 16, 16));
        miExcel.addActionListener(ev -> ExcelExporter.exportTable(tableTamUng, "Tam_Ung_Luong", "Tạm ứng lương", this));
        popup.add(miExcel);

        JMenuItem miPrint = new JMenuItem("In danh sách", IconHelper.getIcon("printer.png", 16, 16));
        miPrint.addActionListener(ev -> {
            try {
                tableTamUng.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi in: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        popup.add(miPrint);

        popup.addSeparator();

        JMenuItem miRefresh = new JMenuItem("Làm mới (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        miRefresh.addActionListener(ev -> loadTableData());
        popup.add(miRefresh);

        popup.show(tableTamUng, e.getX(), e.getY());
    }

    private void setupShortcuts() {
        // Insert: Thêm mới
        registerKeyboardAction(e -> onAddNew(), KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F4: Chỉnh sửa
        registerKeyboardAction(e -> onEditSelected(), KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Delete: Xóa
        registerKeyboardAction(e -> onDeleteSelected(), KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // F5: Tải lại
        registerKeyboardAction(e -> loadTableData(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
}
