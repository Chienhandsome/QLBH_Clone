package kazuko.ex.ui.baocao;

import kazuko.ex.dao.ReportColumnSettingDAO;
import kazuko.ex.model.system.ReportColumnSetting;
import kazuko.ex.report.service.ReportCacheInvalidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dialog cấu hình các cột hiển thị của một mẫu báo cáo. */
public class MauInReportDialog extends JDialog {
    private static final String[] ALIGNMENTS = {"Trái", "Giữa", "Phải"};

    private final String reportKey;
    private final List<ReportColumnResolver.ColumnDefinition> reportColumns;
    private final ReportColumnSettingDAO settingDAO = new ReportColumnSettingDAO();
    private final DefaultTableModel columnModel;
    private JTable columnTable;
    private final JTable detailPreview = new JTable();
    private final JTable group1Preview = new JTable();
    private final JTable group2Preview = new JTable();
    private final JTextField templateNameField = new JTextField("Mẫu A4");
    private boolean updatingGroupSelection;

    public MauInReportDialog(Window owner, String reportName, String reportKey,
                             List<ReportColumnResolver.ColumnDefinition> reportColumns) {
        super(owner, "MẪU IN - " + reportName, ModalityType.APPLICATION_MODAL);
        this.reportKey = reportKey;
        this.reportColumns = reportColumns;
        this.columnModel = createColumnModel(reportColumns, loadSavedSettings());
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 520));
        setSize(1150, 630);
        setLocationRelativeTo(getOwner());

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(new EmptyBorder(10, 12, 10, 12));
        setContentPane(content);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        JLabel title = new JLabel("Chi tiết mẫu in");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        top.add(title, BorderLayout.NORTH);

        JPanel nameRow = new JPanel(new BorderLayout(8, 0));
        nameRow.setBorder(new EmptyBorder(10, 0, 2, 0));
        nameRow.add(new JLabel("Tên mẫu:"), BorderLayout.WEST);
        nameRow.add(templateNameField, BorderLayout.CENTER);
        top.add(nameRow, BorderLayout.CENTER);
        content.add(top, BorderLayout.NORTH);

        columnTable = new JTable(columnModel);
        columnTable.setRowHeight(25);
        columnTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnTable.getTableHeader().setReorderingAllowed(false);
        columnTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnTable.getColumnModel().getColumn(0).setPreferredWidth(210);
        for (int i = 1; i <= 6; i++) {
            columnTable.getColumnModel().getColumn(i).setPreferredWidth(i == 1 ? 80 : 105);
        }
        columnTable.getColumnModel().getColumn(7).setPreferredWidth(115);
        columnTable.getColumnModel().getColumn(7)
                .setCellEditor(new DefaultCellEditor(new JComboBox<>(ALIGNMENTS)));

        JScrollPane columnScroll = new JScrollPane(columnTable);
        columnScroll.setBorder(BorderFactory.createTitledBorder("Các cột hiển thị trên báo cáo"));

        configurePreviewTable(detailPreview);
        configurePreviewTable(group1Preview);
        configurePreviewTable(group2Preview);
        JTabbedPane previewTabs = new JTabbedPane();
        previewTabs.addTab("Chi tiết", new JScrollPane(detailPreview));
        previewTabs.addTab("Nhóm 1", new JScrollPane(group1Preview));
        previewTabs.addTab("Nhóm 2", new JScrollPane(group2Preview));
        previewTabs.setPreferredSize(new Dimension(0, 150));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, columnScroll, previewTabs);
        centerSplit.setResizeWeight(0.70);
        centerSplit.setDividerLocation(330);
        centerSplit.setDividerSize(6);
        centerSplit.setContinuousLayout(true);
        centerSplit.setBorder(BorderFactory.createEmptyBorder());
        content.add(centerSplit, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton saveButton = new JButton("Ghi dữ liệu");
        JButton closeButton = new JButton("Thoát");
        saveButton.addActionListener(e -> saveSettings());
        closeButton.addActionListener(e -> dispose());
        actions.add(saveButton);
        actions.add(closeButton);
        content.add(actions, BorderLayout.SOUTH);

        columnModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                enforceSingleGroupSelection(e.getFirstRow(), e.getColumn());
                refreshPreviews();
            }
        });
        refreshPreviews();
    }

    private DefaultTableModel createColumnModel(
            List<ReportColumnResolver.ColumnDefinition> columns,
            List<ReportColumnSetting> savedSettings) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{
                "Cột", "Hiển thị", "Nhóm 1", "Tổng nhóm 1",
                "Nhóm 2", "Tổng nhóm 2", "Tổng cộng", "Căn lề"
        }, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex >= 1 && columnIndex <= 6 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
        };
        Map<String, ReportColumnSetting> savedByField = new HashMap<>();
        for (ReportColumnSetting setting : savedSettings) {
            savedByField.put(setting.getFieldName(), setting);
        }
        if (!savedSettings.isEmpty() && savedSettings.get(0).getTemplateName() != null) {
            templateNameField.setText(savedSettings.get(0).getTemplateName());
        }
        for (ReportColumnResolver.ColumnDefinition column : columns) {
            ReportColumnSetting saved = savedByField.get(column.fieldName());
            model.addRow(new Object[]{
                    column.displayName(),
                    saved == null || saved.isVisible(),
                    saved != null && saved.getGroupLevel() == 1,
                    saved != null && saved.isTotalGroup1(),
                    saved != null && saved.getGroupLevel() == 2,
                    saved != null && saved.isTotalGroup2(),
                    saved != null && saved.isGrandTotal(),
                    saved == null ? defaultAlignment(column) : toDisplayAlignment(saved.getAlignment())
            });
        }
        return model;
    }

    private List<ReportColumnSetting> loadSavedSettings() {
        try {
            return settingDAO.findByReportKey(reportKey);
        } catch (SQLException e) {
            System.err.println("Không tải được thiết lập cột " + reportKey + ": " + e.getMessage());
            return List.of();
        }
    }

    private void saveSettings() {
        if (columnTable.isEditing() && !columnTable.getCellEditor().stopCellEditing()) {
            return;
        }
        if (templateNameField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên mẫu không được để trống.",
                    "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
            templateNameField.requestFocusInWindow();
            return;
        }
        int group1Row = selectedGroupRow(2);
        int group2Row = selectedGroupRow(4);
        if (group2Row >= 0 && group1Row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Phải chọn Nhóm 1 trước khi chọn Nhóm 2.",
                    "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasCheckedValue(3) && group1Row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Tổng nhóm 1 chỉ dùng được khi đã chọn Nhóm 1.",
                    "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasCheckedValue(5) && group2Row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Tổng nhóm 2 chỉ dùng được khi đã chọn Nhóm 2.",
                    "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            boolean hasTotal = Boolean.TRUE.equals(columnModel.getValueAt(row, 3))
                    || Boolean.TRUE.equals(columnModel.getValueAt(row, 5))
                    || Boolean.TRUE.equals(columnModel.getValueAt(row, 6));
            if (hasTotal && !isNumeric(reportColumns.get(row).valueClassName())) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ có thể tính tổng cho cột số: “" + reportColumns.get(row).displayName() + "”.",
                        "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        List<ReportColumnSetting> settings = new ArrayList<>();
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            ReportColumnResolver.ColumnDefinition column = reportColumns.get(row);
            settings.add(new ReportColumnSetting(
                    reportKey,
                    templateNameField.getText().trim(),
                    column.fieldName(),
                    String.valueOf(columnModel.getValueAt(row, 0)),
                    Boolean.TRUE.equals(columnModel.getValueAt(row, 1)),
                    Boolean.TRUE.equals(columnModel.getValueAt(row, 2)) ? 1
                            : Boolean.TRUE.equals(columnModel.getValueAt(row, 4)) ? 2 : 0,
                    Boolean.TRUE.equals(columnModel.getValueAt(row, 3)),
                    Boolean.TRUE.equals(columnModel.getValueAt(row, 5)),
                    Boolean.TRUE.equals(columnModel.getValueAt(row, 6)),
                    String.valueOf(columnModel.getValueAt(row, 7)),
                    row));
        }

        try {
            settingDAO.save(reportKey, templateNameField.getText(), settings);
            ReportCacheInvalidator.clearAll();
            JOptionPane.showMessageDialog(this,
                    "Đã lưu thiết lập mẫu báo cáo.",
                    "Thiết kế mẫu", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Không lưu được thiết lập: " + e.getMessage(),
                    "Thiết kế mẫu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int selectedGroupRow(int groupColumn) {
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(columnModel.getValueAt(row, groupColumn))) return row;
        }
        return -1;
    }

    private boolean hasCheckedValue(int column) {
        return selectedGroupRow(column) >= 0;
    }

    private String defaultAlignment(ReportColumnResolver.ColumnDefinition column) {
        return isNumeric(column.valueClassName()) ? "Phải" : "Trái";
    }

    private boolean isNumeric(String className) {
        return className != null && (className.startsWith("java.lang.Byte")
                || className.startsWith("java.lang.Short")
                || className.startsWith("java.lang.Integer")
                || className.startsWith("java.lang.Long")
                || className.startsWith("java.lang.Float")
                || className.startsWith("java.lang.Double")
                || className.startsWith("java.math.BigDecimal")
                || className.startsWith("java.math.BigInteger"));
    }

    private void configurePreviewTable(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(24);
        table.setEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void refreshPreviews() {
        DefaultTableModel previewModel = new DefaultTableModel();
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            boolean isGroup = Boolean.TRUE.equals(columnModel.getValueAt(row, 2))
                    || Boolean.TRUE.equals(columnModel.getValueAt(row, 4));
            if (Boolean.TRUE.equals(columnModel.getValueAt(row, 1)) && !isGroup) {
                previewModel.addColumn(columnModel.getValueAt(row, 0));
            }
        }
        detailPreview.setModel(previewModel);

        int visibleIndex = 0;
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            boolean isGroup = Boolean.TRUE.equals(columnModel.getValueAt(row, 2))
                    || Boolean.TRUE.equals(columnModel.getValueAt(row, 4));
            if (!Boolean.TRUE.equals(columnModel.getValueAt(row, 1)) || isGroup) continue;
            TableColumn previewColumn = detailPreview.getColumnModel().getColumn(visibleIndex++);
            previewColumn.setPreferredWidth(125);
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(toSwingAlignment(String.valueOf(columnModel.getValueAt(row, 2))));
            previewColumn.setCellRenderer(renderer);
        }
        refreshGroupPreview(group1Preview, 2);
        refreshGroupPreview(group2Preview, 4);
    }

    private void refreshGroupPreview(JTable preview, int groupColumn) {
        DefaultTableModel model = new DefaultTableModel();
        for (int row = 0; row < columnModel.getRowCount(); row++) {
            if (Boolean.TRUE.equals(columnModel.getValueAt(row, groupColumn))) {
                model.addColumn(columnModel.getValueAt(row, 0));
            }
        }
        preview.setModel(model);
        if (preview.getColumnCount() > 0) {
            preview.getColumnModel().getColumn(0).setPreferredWidth(160);
        }
    }

    private void enforceSingleGroupSelection(int changedRow, int changedColumn) {
        if (updatingGroupSelection || changedRow < 0 || (changedColumn != 2 && changedColumn != 4)) return;
        if (!Boolean.TRUE.equals(columnModel.getValueAt(changedRow, changedColumn))) return;
        updatingGroupSelection = true;
        try {
            int otherGroupColumn = changedColumn == 2 ? 4 : 2;
            columnModel.setValueAt(Boolean.FALSE, changedRow, otherGroupColumn);
            for (int row = 0; row < columnModel.getRowCount(); row++) {
                if (row != changedRow) {
                    columnModel.setValueAt(Boolean.FALSE, row, changedColumn);
                }
            }
        } finally {
            updatingGroupSelection = false;
        }
    }

    private int toSwingAlignment(String value) {
        if ("Giữa".equals(value)) return SwingConstants.CENTER;
        if ("Phải".equals(value)) return SwingConstants.RIGHT;
        return SwingConstants.LEFT;
    }

    private String toDisplayAlignment(String value) {
        if ("CENTER".equalsIgnoreCase(value)) return "Giữa";
        if ("RIGHT".equalsIgnoreCase(value)) return "Phải";
        return "Trái";
    }
}
