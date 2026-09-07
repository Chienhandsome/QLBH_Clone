package kazuko.ex.ui.danhmuc;

import kazuko.ex.util.IconHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ThemTuExcelKhachHangDialog extends JDialog {
    private final File excelFile;
    private final List<String> excelHeaders;
    private final Consumer<List<Map<String, Object>>> onAcceptCallback;

    private static final String[] TARGET_FIELDS = {
            "",
            "Mã khách",
            "Tên khách hàng",
            "Địa chỉ",
            "Điện thoại",
            "Email",
            "Nhóm khách hàng",
            "Mã số thuế",
            "Nhân viên",
            "Ngày thành lập/sinh nhật",
            "Tỉnh thành",
            "Facebook",
            "Ghi chú",
            "Điểm tích lũy ban đầu"
    };

    private JTable mappingTable;
    private DefaultTableModel tableModel;

    public ThemTuExcelKhachHangDialog(Window parent, File excelFile, List<String> excelHeaders, Consumer<List<Map<String, Object>>> onAcceptCallback) {
        super(parent, "Thêm từ excel", ModalityType.APPLICATION_MODAL);
        this.excelFile = excelFile;
        this.excelHeaders = excelHeaders != null ? excelHeaders : new ArrayList<>();
        this.onAcceptCallback = onAcceptCallback;

        initComponents();
        setSize(480, 520);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 244, 250));

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(225, 235, 248));
        headerPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("page_white_excel.png", 32, 32));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("table_column.png", 28, 28));
        }

        JPanel pnlHeaderText = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlHeaderText.setOpaque(false);

        JLabel lblGuide1 = new JLabel("Hướng dẫn: Cột bên trái là cột sẵn có trong file excel của bạn.");
        lblGuide1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGuide1.setForeground(new Color(30, 40, 55));

        JLabel lblGuide2 = new JLabel("Bạn chọn cột bên phải để hệ thống hiểu được cột trong file excel là dữ liệu nào");
        lblGuide2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGuide2.setForeground(new Color(30, 40, 55));

        pnlHeaderText.add(lblGuide1);
        pnlHeaderText.add(lblGuide2);

        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(pnlHeaderText, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Table
        String[] colNames = {"Cột excel", "Dữ liệu"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Chỉ sửa cột "Dữ liệu"
            }
        };

        for (String header : excelHeaders) {
            tableModel.addRow(new Object[]{header, ""});
        }

        mappingTable = new JTable(tableModel);
        mappingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mappingTable.setRowHeight(24);
        mappingTable.setSelectionBackground(new Color(205, 230, 255));
        mappingTable.setSelectionForeground(Color.BLACK);
        mappingTable.setGridColor(new Color(220, 230, 242));
        mappingTable.setShowGrid(true);

        mappingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        mappingTable.getTableHeader().setBackground(new Color(240, 245, 252));
        mappingTable.getTableHeader().setPreferredSize(new Dimension(0, 28));

        // Editor cho cột "Dữ liệu": JComboBox với 13 trường
        JComboBox<String> cmbFields = new JComboBox<>(TARGET_FIELDS);
        cmbFields.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mappingTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cmbFields));

        // Renderer cho cột "Cột excel"
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        mappingTable.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        JScrollPane scrollPane = new JScrollPane(mappingTable);
        scrollPane.setBorder(new LineBorder(new Color(190, 210, 235), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBorder(new EmptyBorder(8, 8, 8, 8));
        pnlCenter.setOpaque(false);
        pnlCenter.add(scrollPane, BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Panel
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(new Color(230, 238, 248));
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 215, 230)));
        pnlBottom.setPreferredSize(new Dimension(480, 44));

        // Left button: Tự động chọn
        JPanel pnlBottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlBottomLeft.setOpaque(false);

        JButton btnAuto = new JButton("Tự động chọn");
        btnAuto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAuto.setBackground(Color.WHITE);
        btnAuto.setFocusPainted(false);
        btnAuto.setIcon(IconHelper.getIcon("accept.png", 14, 14));
        if (btnAuto.getIcon() == null) btnAuto.setIcon(IconHelper.getIcon("1282529873_objects.png", 14, 14));
        btnAuto.addActionListener(e -> onAutoSelect());
        pnlBottomLeft.add(btnAuto);

        // Right buttons: Chấp nhận, Hủy bỏ
        JPanel pnlBottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottomRight.setOpaque(false);

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAccept.setPreferredSize(new Dimension(95, 28));
        btnAccept.setBackground(Color.WHITE);
        btnAccept.setFocusPainted(false);
        btnAccept.setIcon(IconHelper.getIcon("accept.png", 14, 14));
        btnAccept.addActionListener(e -> onAccept());

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(85, 28));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.setIcon(IconHelper.getIcon("cross.png", 14, 14));
        btnCancel.addActionListener(e -> dispose());

        pnlBottomRight.add(btnAccept);
        pnlBottomRight.add(btnCancel);

        pnlBottom.add(pnlBottomLeft, BorderLayout.WEST);
        pnlBottom.add(pnlBottomRight, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnAccept);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Tự động chọn mapping ngay khi mở
        onAutoSelect();
    }

    private void onAutoSelect() {
        if (mappingTable.isEditing()) {
            TableCellEditor editor = mappingTable.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        Set<String> assignedFields = new HashSet<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String excelCol = tableModel.getValueAt(i, 0).toString().trim();
            String matched = findBestMatchingField(excelCol, assignedFields);
            if (!matched.isEmpty()) {
                tableModel.setValueAt(matched, i, 1);
                assignedFields.add(matched);
            } else {
                tableModel.setValueAt("", i, 1);
            }
        }
    }

    private String findBestMatchingField(String excelColName, Set<String> assignedFields) {
        String lowerCol = excelColName.toLowerCase().trim();
        String normalized = removeAccents(lowerCol);

        if (matchesPattern(lowerCol, normalized, new String[]{"mã khách", "mã khách hàng", "mã kh", "makhach", "makh", "customer code", "cust code", "customer id"})) {
            if (!assignedFields.contains("Mã khách")) return "Mã khách";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"tên khách hàng", "tên khách", "tên kh", "họ và tên", "họ tên", "khách hàng", "ten khach hang", "ten khach", "ten kh", "customer name", "name"})) {
            if (!assignedFields.contains("Tên khách hàng")) return "Tên khách hàng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"địa chỉ", "địa chỉ giao hàng", "dia chi", "address"})) {
            if (!assignedFields.contains("Địa chỉ")) return "Địa chỉ";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"điện thoại", "sđt", "số điện thoại", "dien thoai", "sdt", "phone", "telephone", "mobile"})) {
            if (!assignedFields.contains("Điện thoại")) return "Điện thoại";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"email", "thư điện tử", "e-mail", "mail"})) {
            if (!assignedFields.contains("Email")) return "Email";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"nhóm khách hàng", "nhóm khách", "nhóm kh", "nhom khach hang", "nhom khach", "nhom kh", "group", "customer group"})) {
            if (!assignedFields.contains("Nhóm khách hàng")) return "Nhóm khách hàng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"mã số thuế", "mst", "tax code", "tax", "ma so thue"})) {
            if (!assignedFields.contains("Mã số thuế")) return "Mã số thuế";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"nhân viên", "nhân viên phụ trách", "nhan vien", "staff", "employee"})) {
            if (!assignedFields.contains("Nhân viên")) return "Nhân viên";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"ngày thành lập/sinh nhật", "ngày sinh nhật", "ngày sinh", "sinh nhật", "ngày thành lập", "ngay sinh", "sinh nhat", "ngay thanh lap", "birthday", "dob"})) {
            if (!assignedFields.contains("Ngày thành lập/sinh nhật")) return "Ngày thành lập/sinh nhật";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"tỉnh thành", "thành phố", "tỉnh / thành phố", "tinh thanh", "thanh pho", "city", "province"})) {
            if (!assignedFields.contains("Tỉnh thành")) return "Tỉnh thành";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"facebook", "fb", "link facebook"})) {
            if (!assignedFields.contains("Facebook")) return "Facebook";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"ghi chú", "mô tả", "note", "description", "ghi chu", "mo ta"})) {
            if (!assignedFields.contains("Ghi chú")) return "Ghi chú";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"điểm tích lũy ban đầu", "điểm ban đầu", "điểm tích lũy", "điểm", "diem tich luy ban dau", "diem ban dau", "diem tich luy", "points"})) {
            if (!assignedFields.contains("Điểm tích lũy ban đầu")) return "Điểm tích lũy ban đầu";
        }

        return "";
    }

    private boolean matchesPattern(String lower, String normalized, String[] patterns) {
        for (String p : patterns) {
            if (lower.equals(p) || normalized.equals(removeAccents(p))) return true;
        }
        return false;
    }

    private String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D').trim();
    }

    private void onAccept() {
        if (mappingTable.isEditing()) {
            TableCellEditor editor = mappingTable.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        Map<String, Integer> fieldCounts = new HashMap<>();
        Map<Integer, String> colIndexToTargetField = new HashMap<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 1);
            String field = val != null ? val.toString().trim() : "";
            if (!field.isEmpty()) {
                fieldCounts.put(field, fieldCounts.getOrDefault(field, 0) + 1);
                colIndexToTargetField.put(i, field);
            }
        }

        // Check duplicate mapping
        for (Map.Entry<String, Integer> entry : fieldCounts.entrySet()) {
            if (entry.getValue() > 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Trường '" + entry.getKey() + "' đã được chọn " + entry.getValue() + " lần!\nMỗi trường chỉ được ánh xạ tối đa một lần.",
                        "Lỗi ánh xạ",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        if (colIndexToTargetField.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn ít nhất một trường dữ liệu!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Read Excel data
        try (InputStream in = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                JOptionPane.showMessageDialog(this, "File Excel không có dòng dữ liệu nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DataFormatter formatter = new DataFormatter();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            List<Map<String, Object>> parsedRows = new ArrayList<>();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, Object> rowMap = new HashMap<>();
                boolean hasData = false;

                for (Map.Entry<Integer, String> entry : colIndexToTargetField.entrySet()) {
                    int colIdx = entry.getKey();
                    String targetField = entry.getValue();

                    Cell cell = row.getCell(colIdx);
                    String cellVal = cell != null ? formatter.formatCellValue(cell).trim() : "";
                    if (!cellVal.isEmpty()) hasData = true;

                    if ("Ngày thành lập/sinh nhật".equals(targetField)) {
                        if (cell != null && DateUtil.isCellDateFormatted(cell)) {
                            Date d = cell.getDateCellValue();
                            if (d != null) {
                                rowMap.put(targetField, sdf.format(d));
                            } else {
                                rowMap.put(targetField, cellVal);
                            }
                        } else {
                            rowMap.put(targetField, cellVal);
                        }
                    } else if ("Điểm tích lũy ban đầu".equals(targetField)) {
                        double val = 0;
                        try {
                            if (!cellVal.isEmpty()) {
                                String clean = cellVal.replace(",", "").replace(" ", "").trim();
                                val = Double.parseDouble(clean);
                            }
                        } catch (Exception ignored) {}
                        rowMap.put(targetField, val);
                    } else {
                        rowMap.put(targetField, cellVal);
                    }
                }

                if (hasData) {
                    parsedRows.add(rowMap);
                }
            }

            if (parsedRows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dòng dữ liệu nào hợp lệ trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (onAcceptCallback != null) {
                onAcceptCallback.accept(parsedRows);
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi đọc dữ liệu Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
