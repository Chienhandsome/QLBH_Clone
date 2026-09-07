package kazuko.ex.ui.danhmuc;

import kazuko.ex.util.MatHangExcelUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class XuatMauVaChonExcelDialog extends JDialog {
    private final Consumer<List<Map<String, Object>>> onImportSuccess;

    public XuatMauVaChonExcelDialog(Window owner, Consumer<List<Map<String, Object>>> onImportSuccess) {
        super(owner, "Chọn file excel hoặc xuất file mẫu", ModalityType.APPLICATION_MODAL);
        this.onImportSuccess = onImportSuccess;

        initUI();
        setSize(460, 260);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    // Constructor backward-compatibility
    public XuatMauVaChonExcelDialog(Window owner, List<String> visibleColumns, List<String> allColumns, Consumer<List<Map<String, Object>>> onImportSuccess) {
        this(owner, onImportSuccess);
    }

    private void initUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(205, 222, 245));
        contentPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // 1. Phần xuất mẫu
        JLabel lblExport = new JLabel("Nếu bạn chưa có file excel, click vào \"Xuất file mẫu\"");
        lblExport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblExport);
        contentPanel.add(Box.createVerticalStrut(8));

        JButton btnExportVisible = new JButton("Xuất file mẫu các cột đang hiển thị trong danh sách");
        btnExportVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportVisible.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExportVisible.setMaximumSize(new Dimension(360, 26));
        btnExportVisible.setPreferredSize(new Dimension(360, 26));
        btnExportVisible.setBackground(Color.WHITE);
        btnExportVisible.addActionListener(e -> MatHangExcelUtil.exportSampleTemplate(this, "Mau_MatHang_HienThi.xlsx"));
        contentPanel.add(btnExportVisible);
        contentPanel.add(Box.createVerticalStrut(6));

        JButton btnExportAll = new JButton("Xuất file mẫu tất cả các cột (bao gồm cả cột ẩn)");
        btnExportAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnExportAll.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExportAll.setMaximumSize(new Dimension(360, 26));
        btnExportAll.setPreferredSize(new Dimension(360, 26));
        btnExportAll.setBackground(Color.WHITE);
        btnExportAll.addActionListener(e -> MatHangExcelUtil.exportSampleTemplate(this, "Mau_MatHang_TatCa.xlsx"));
        contentPanel.add(btnExportAll);
        contentPanel.add(Box.createVerticalStrut(14));

        // 2. Phần chọn file excel
        JLabel lblImport = new JLabel("Nếu đã có mẫu, click \"Chọn file excel\"");
        lblImport.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblImport.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblImport);
        contentPanel.add(Box.createVerticalStrut(8));

        JButton btnChooseExcel = new JButton("Chọn file excel");
        btnChooseExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChooseExcel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnChooseExcel.setMaximumSize(new Dimension(360, 26));
        btnChooseExcel.setPreferredSize(new Dimension(360, 26));
        btnChooseExcel.setBackground(Color.WHITE);
        btnChooseExcel.addActionListener(e -> onChooseExcelFile());
        contentPanel.add(btnChooseExcel);
        contentPanel.add(Box.createVerticalStrut(6));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(360, 26));
        btnCancel.setPreferredSize(new Dimension(360, 26));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.addActionListener(e -> dispose());
        contentPanel.add(btnCancel);

        setContentPane(contentPanel);
    }

    private void onChooseExcelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file Excel chứa danh sách mặt hàng");
        chooser.setFileFilter(new FileNameExtensionFilter("File Excel (*.xlsx, *.xls)", "xlsx", "xls"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (InputStream in = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 0) {
                JOptionPane.showMessageDialog(this, "File Excel trống hoặc không đúng định dạng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Đọc Header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tiêu đề cột trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<String> fileHeaders = new ArrayList<>();
            DataFormatter formatter = new DataFormatter();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                String h = cell != null ? formatter.formatCellValue(cell).trim() : "";
                if (!h.isEmpty()) {
                    fileHeaders.add(h);
                }
            }

            if (fileHeaders.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy tiêu đề cột hợp lệ trong file Excel!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Mở hộp thoại ánh xạ Thêm từ excel
            ThemTuExcelMatHangDialog mappingDialog = new ThemTuExcelMatHangDialog(
                    this,
                    file,
                    fileHeaders,
                    parsedRows -> {
                        if (onImportSuccess != null) {
                            onImportSuccess.accept(parsedRows);
                        }
                        dispose();
                    }
            );
            mappingDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc file Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
