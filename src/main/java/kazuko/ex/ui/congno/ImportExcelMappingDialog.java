package kazuko.ex.ui.congno;

import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportExcelMappingDialog extends JDialog {
    public static final String FIELD_MA_DOI_TAC = "Mã đối tác";
    public static final String FIELD_CONG_NO_DAU = "Công nợ đầu";
    public static final String FIELD_TEN_KHACH = "Tên khách";
    public static final String FIELD_DIA_CHI = "Địa chỉ";
    public static final String FIELD_DIEN_THOAI = "Điện thoại";

    public static final String[] TARGET_FIELDS = {
            "",
            FIELD_MA_DOI_TAC,
            FIELD_CONG_NO_DAU,
            FIELD_TEN_KHACH,
            FIELD_DIA_CHI,
            FIELD_DIEN_THOAI
    };

    private final List<String> excelHeaders;
    private JTable mappingTable;
    private DefaultTableModel tableModel;
    private boolean confirmed = false;
    private final Map<String, String> resultMap = new HashMap<>();

    public ImportExcelMappingDialog(Window parent, List<String> excelHeaders) {
        super(parent, "Thêm từ excel", ModalityType.APPLICATION_MODAL);
        this.excelHeaders = excelHeaders;
        initComponents();
        setSize(460, 480);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Model
        String[] colNames = {"Cột excel", "Dữ liệu"};
        tableModel = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Chỉ cho phép sửa cột Dữ liệu (chọn ComboBox)
            }
        };

        if (excelHeaders != null) {
            for (String h : excelHeaders) {
                tableModel.addRow(new Object[]{h, ""});
            }
        }

        mappingTable = new JTable(tableModel);
        mappingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mappingTable.setRowHeight(24);
        mappingTable.setSelectionBackground(new Color(205, 230, 255));
        mappingTable.setSelectionForeground(Color.BLACK);
        mappingTable.setGridColor(new Color(220, 230, 242));
        mappingTable.setShowGrid(true);

        // Header style
        mappingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        mappingTable.getTableHeader().setBackground(new Color(240, 245, 252));
        mappingTable.getTableHeader().setPreferredSize(new Dimension(0, 28));

        // Editor cho cột Dữ liệu: JComboBox
        JComboBox<String> cmbFields = new JComboBox<>(TARGET_FIELDS);
        cmbFields.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mappingTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(cmbFields));

        // Renderer
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

        // Bottom panel
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlBottom.setBackground(new Color(240, 245, 252));
        pnlBottom.setBorder(new LineBorder(new Color(210, 225, 240), 1, true));

        JButton btnOK = new JButton("OK");
        btnOK.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnOK.setPreferredSize(new Dimension(85, 28));
        btnOK.setBackground(AppColors.WHITE);
        btnOK.setFocusPainted(false);
        btnOK.addActionListener(e -> onOK());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(85, 28));
        btnCancel.setBackground(AppColors.WHITE);
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> dispose());

        pnlBottom.add(btnOK);
        pnlBottom.add(btnCancel);
        add(pnlBottom, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOK);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void onOK() {
        if (mappingTable.isEditing()) {
            TableCellEditor editor = mappingTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }

        resultMap.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String excelCol = (String) tableModel.getValueAt(i, 0);
            String targetField = (String) tableModel.getValueAt(i, 1);
            if (excelCol != null && targetField != null && !targetField.trim().isEmpty()) {
                resultMap.put(excelCol.trim(), targetField.trim());
            }
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Map<String, String> getResultMap() {
        return resultMap;
    }
}
