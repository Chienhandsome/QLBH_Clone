package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class ThemTuExcelMatHangDialog extends JDialog {
    private final File excelFile;
    private final List<String> excelHeaders;
    private final Consumer<List<Map<String, Object>>> onAcceptCallback;
    private final SystemConfig systemConfig;

    private String labelGia2 = "Đơn giá 2";
    private String labelGia3 = "Giá bán 3";
    private String labelGia4 = "Giá bán 4";
    private String[] targetFields;

    private JTable mappingTable;
    private DefaultTableModel tableModel;
    private boolean confirmed = false;

    public ThemTuExcelMatHangDialog(Window parent, File excelFile, List<String> excelHeaders, Consumer<List<Map<String, Object>>> onAcceptCallback) {
        super(parent, "Thêm từ excel", ModalityType.APPLICATION_MODAL);
        this.excelFile = excelFile;
        this.excelHeaders = excelHeaders != null ? excelHeaders : new ArrayList<>();
        this.onAcceptCallback = onAcceptCallback;
        this.systemConfig = new SystemConfigDAO().loadConfig();

        initTargetFields();
        initComponents();
        setSize(480, 520);
        setLocationRelativeTo(parent);
    }

    private void initTargetFields() {
        if (systemConfig != null) {
            if (systemConfig.getDienGiaiGia2() != null && !systemConfig.getDienGiaiGia2().trim().isEmpty()) {
                labelGia2 = systemConfig.getDienGiaiGia2().trim();
            }
            if (systemConfig.getDienGiaiGia3() != null && !systemConfig.getDienGiaiGia3().trim().isEmpty()) {
                labelGia3 = systemConfig.getDienGiaiGia3().trim();
            }
            if (systemConfig.getDienGiaiGia4() != null && !systemConfig.getDienGiaiGia4().trim().isEmpty()) {
                labelGia4 = systemConfig.getDienGiaiGia4().trim();
            }
        }

        List<String> list = new ArrayList<>();
        list.add("");
        list.add("Mã hàng");
        list.add("Tên mặt hàng");
        list.add("Nhóm mặt hàng");
        list.add("Đơn vị tính");
        list.add("Giá bán");
        list.add("Giá nhập");
        list.add("Mặc định giảm giá");
        list.add("Mặc định giảm tiền");
        list.add("Ghi chú");
        list.add("Không in chế biến");
        list.add("Bảo hành");
        list.add("Đơn vị tính chẵn");
        list.add("Quy đổi");
        list.add("Giá bán chẵn");
        list.add(labelGia2);
        list.add(labelGia3);
        if (systemConfig != null && systemConfig.isSuDungGia4()) {
            list.add(labelGia4);
        }
        list.add("Hãng sản xuất");
        list.add("Có hạn sử dụng");
        list.add("Tồn tối thiểu");
        list.add("Tồn tối đa");
        list.add("Ảnh");
        list.add("Hoa hồng");
        list.add("Giá vốn");
        list.add("Đối tác ký gửi");

        targetFields = list.toArray(new String[0]);
    }

    public String[] getTargetFields() {
        return targetFields;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(215, 230, 248));
        headerPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(185, 205, 230)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblIcon = new JLabel();
        Icon icon = IconHelper.getIcon("page_white_excel.png", 22, 22);
        if (icon == null) icon = IconHelper.getIcon("Bookmark.png", 22, 22);
        if (icon == null) icon = IconHelper.getIcon("1282529873_objects.png", 22, 22);
        lblIcon.setIcon(icon);

        JLabel lblGuide = new JLabel("<html><div style='font-family:Segoe UI;font-size:11px;color:#1e283c;line-height:1.4;'>"
                + "<b>Hướng dẫn:</b> Cột bên trái là cột sẵn có trong file excel của bạn.<br>"
                + "Bạn lựa chọn cột bên phải để hệ thống hiểu được cột trong file excel là dữ liệu nào"
                + "</div></html>");

        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(lblGuide, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Table Mapping
        String[] colNames = {"Cột excel", "Dữ liệu"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1;
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

        // Editor cho cột "Dữ liệu": JComboBox với targetFields động
        JComboBox<String> cmbFields = new JComboBox<>(targetFields);
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
    }

    private void onAutoSelect() {
        if (mappingTable.isEditing()) {
            TableCellEditor editor = mappingTable.getCellEditor();
            if (editor != null) editor.stopCellEditing();
        }

        Set<String> assignedFields = new HashSet<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String excelCol = (String) tableModel.getValueAt(i, 0);
            String match = findBestMatchField(excelCol, assignedFields);
            if (match != null && !match.isEmpty()) {
                tableModel.setValueAt(match, i, 1);
                assignedFields.add(match);
            } else {
                tableModel.setValueAt("", i, 1);
            }
        }
    }

    private String findBestMatchField(String rawCol, Set<String> assignedFields) {
        if (rawCol == null || rawCol.trim().isEmpty()) return "";
        String col = rawCol.trim();
        String lowerCol = col.toLowerCase();
        String normalized = removeAccents(lowerCol);

        // 1. Direct exact match
        for (String f : targetFields) {
            if (f.isEmpty()) continue;
            if (f.equalsIgnoreCase(col) && !assignedFields.contains(f)) {
                return f;
            }
        }

        // 2. Exact match on normalized
        for (String f : targetFields) {
            if (f.isEmpty()) continue;
            if (removeAccents(f.toLowerCase()).equals(normalized) && !assignedFields.contains(f)) {
                return f;
            }
        }

        // 3. Smart alias rules
        if (matchesPattern(lowerCol, normalized, new String[]{"mã", "mã hàng", "mã sp", "mã mặt hàng", "code", "ma", "ma hang", "ma sp"})) {
            if (!assignedFields.contains("Mã hàng")) return "Mã hàng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"tên", "tên hàng", "tên mặt hàng", "tên sp", "tên sản phẩm", "name", "ten", "ten hang", "ten mat hang", "ten sp"})) {
            if (!assignedFields.contains("Tên mặt hàng")) return "Tên mặt hàng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"nhóm", "nhóm hàng", "nhóm mặt hàng", "nhóm sp", "group", "category", "nhom", "nhom hang", "nhom mat hang"})) {
            if (!assignedFields.contains("Nhóm mặt hàng")) return "Nhóm mặt hàng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"đơn vị tính", "đvt", "dvt", "đơn vị", "unit", "don vi tinh", "don vi"})) {
            if (!assignedFields.contains("Đơn vị tính")) return "Đơn vị tính";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"giá bán", "đơn giá", "đơn giá bán", "giá", "price", "gia ban", "gia"})) {
            if (!assignedFields.contains("Giá bán")) return "Giá bán";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"giá nhập", "giá mua", "đơn giá nhập", "gia nhap", "gia mua"})) {
            if (!assignedFields.contains("Giá nhập")) return "Giá nhập";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"mặc định giảm giá", "giảm giá %", "giảm giá pt", "chiết khấu %", "giảm giá", "giam gia"})) {
            if (!assignedFields.contains("Mặc định giảm giá")) return "Mặc định giảm giá";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"mặc định giảm tiền", "giảm tiền", "chiết khấu tiền", "giam tien"})) {
            if (!assignedFields.contains("Mặc định giảm tiền")) return "Mặc định giảm tiền";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"ghi chú", "mô tả", "note", "description", "ghi chu", "mo ta"})) {
            if (!assignedFields.contains("Ghi chú")) return "Ghi chú";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"không in chế biến", "không in", "ko in chế biến", "ko in", "khong in che bien", "khong in"})) {
            if (!assignedFields.contains("Không in chế biến")) return "Không in chế biến";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"bảo hành", "thời hạn bảo hành", "warranty", "bao hanh"})) {
            if (!assignedFields.contains("Bảo hành")) return "Bảo hành";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"đơn vị tính chẵn", "đvt chẵn", "dvt chẵn", "dvt chan", "don vi tinh chan"})) {
            if (!assignedFields.contains("Đơn vị tính chẵn")) return "Đơn vị tính chẵn";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"quy đổi", "tỷ lệ quy đổi", "hệ số quy đổi", "quy doi"})) {
            if (!assignedFields.contains("Quy đổi")) return "Quy đổi";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"giá bán chẵn", "đơn giá chẵn", "gia ban chan"})) {
            if (!assignedFields.contains("Giá bán chẵn")) return "Giá bán chẵn";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"đơn giá 2", "giá bán 2", "giá 2", "don gia 2", "gia ban 2", "gia 2", labelGia2.toLowerCase()})) {
            if (!assignedFields.contains(labelGia2)) return labelGia2;
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"giá blabla", "giá bla bla", "giá bán 3", "giá 3", "đơn giá 3", "gia blabla", "gia 3", labelGia3.toLowerCase()})) {
            if (!assignedFields.contains(labelGia3)) return labelGia3;
        }
        if (systemConfig != null && systemConfig.isSuDungGia4()) {
            if (matchesPattern(lowerCol, normalized, new String[]{"giá bán 4", "giá 4", "đơn giá 4", "gia 4", labelGia4.toLowerCase()})) {
                if (!assignedFields.contains(labelGia4)) return labelGia4;
            }
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"hãng sản xuất", "hãng sx", "nhà sản xuất", "brand", "manufacturer", "hang san xuat", "hang sx"})) {
            if (!assignedFields.contains("Hãng sản xuất")) return "Hãng sản xuất";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"có hạn sử dụng", "hạn sử dụng", "hsd", "co han su dung", "han su dung"})) {
            if (!assignedFields.contains("Có hạn sử dụng")) return "Có hạn sử dụng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"tồn tối thiểu", "tồn min", "min stock", "ton toi thieu", "ton min"})) {
            if (!assignedFields.contains("Tồn tối thiểu")) return "Tồn tối thiểu";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"tồn tối đa", "tồn max", "max stock", "ton toi da", "ton max"})) {
            if (!assignedFields.contains("Tồn tối đa")) return "Tồn tối đa";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"ảnh", "hình ảnh", "hình", "image", "picture", "photo", "anh", "hinh anh"})) {
            if (!assignedFields.contains("Ảnh")) return "Ảnh";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"hoa hồng", "chiết khấu hoa hồng", "commission", "hoa hong"})) {
            if (!assignedFields.contains("Hoa hồng")) return "Hoa hồng";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"giá vốn", "cost", "cost price", "gia von"})) {
            if (!assignedFields.contains("Giá vốn")) return "Giá vốn";
        }
        if (matchesPattern(lowerCol, normalized, new String[]{"đối tác ký gửi", "đối tác", "ký gửi", "doi tac ky gui", "doi tac", "ky gui"})) {
            if (!assignedFields.contains("Đối tác ký gửi")) return "Đối tác ký gửi";
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
        Pattern pattern = Pattern.compile("\\\\p{InCombiningDiacriticalMarks}+");
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

        // Kiểm tra duplicate selection
        for (Map.Entry<String, Integer> entry : fieldCounts.entrySet()) {
            if (entry.getValue() > 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Trường '" + entry.getKey() + "' đã được chọn " + entry.getValue() + " lần!\\nMỗi trường chỉ được ánh xạ tối đa một lần.",
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

        // Đọc dữ liệu từ file Excel theo mapping
        try (InputStream in = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                JOptionPane.showMessageDialog(this, "File Excel không có dòng dữ liệu nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DataFormatter formatter = new DataFormatter();
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

                    if ("Không in chế biến".equals(targetField) || "Có hạn sử dụng".equals(targetField)) {
                        boolean boolVal = "true".equalsIgnoreCase(cellVal) || "1".equals(cellVal) || "x".equalsIgnoreCase(cellVal) || "có".equalsIgnoreCase(cellVal);
                        rowMap.put(targetField, boolVal);
                    } else {
                        rowMap.put(targetField, cellVal);
                    }
                }

                if (hasData) {
                    parsedRows.add(rowMap);
                }
            }

            if (parsedRows.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu hợp lệ trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (onAcceptCallback != null) {
                onAcceptCallback.accept(parsedRows);
            }

            confirmed = true;
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
