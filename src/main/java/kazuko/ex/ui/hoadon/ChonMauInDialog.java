package kazuko.ex.ui.hoadon;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.report.bill.BillPrintService;
import kazuko.ex.util.IconHelper;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChonMauInDialog extends JDialog {

    public enum PrintAction {
        PREVIEW_SCREEN,
        PRINT_PRINTER,
        EXPORT_EXCEL,
        EXPORT_PDF,
        DESIGN_TEMPLATE
    }

    private final DonHang donHang;
    private JList<PrintTemplateItem> listTemplates;
    private DefaultListModel<PrintTemplateItem> templateModel;

    private JRadioButton rbXemManHinh;
    private JRadioButton rbInMayIn;
    private JComboBox<String> cbPrinters;
    private JRadioButton rbXuatExcel;
    private JRadioButton rbXuatPdf;
    private JRadioButton rbThietKe;

    private boolean confirmed = false;
    private PrintAction selectedAction = PrintAction.PREVIEW_SCREEN;
    private String selectedTemplateName = "Mẫu in bill A4";
    private String selectedPrinterName = "";
    private final BillPrintService billPrintService = new BillPrintService();

    public static class PrintTemplateItem {
        public String name;
        public String iconName;

        public PrintTemplateItem(String name, String iconName) {
            this.name = name;
            this.iconName = iconName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public ChonMauInDialog(Window parent, DonHang donHang) {
        super(parent, "CHỌN MẪU IN", ModalityType.APPLICATION_MODAL);
        this.donHang = donHang;
        initComponents();
    }

    private void initComponents() {
        setSize(480, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel pnlContent = new JPanel(new BorderLayout(0, 8));
        pnlContent.setBackground(new Color(230, 240, 252));
        pnlContent.setBorder(new EmptyBorder(8, 8, 8, 8));

        // 1. Template List (Top)
        templateModel = new DefaultListModel<>();
        templateModel.addElement(new PrintTemplateItem("Mẫu in A5 cho máy in kim", "asterisk-yellow.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in bill 54mm", "asterisk-yellow.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in bill 80 chiết khấu mặt hàng", "asterisk-yellow.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in bill 80mm", "asterisk-yellow.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in bill A4", "Report.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in bill A5", "Report.png"));
        templateModel.addElement(new PrintTemplateItem("Mẫu in chế biến", "mail-send.png"));
        templateModel.addElement(new PrintTemplateItem("Phiếu giao hàng", "Find.png"));

        listTemplates = new JList<>(templateModel);
        listTemplates.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listTemplates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTemplates.setSelectedIndex(4); // Default "Mẫu in bill A4"
        listTemplates.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PrintTemplateItem) {
                    PrintTemplateItem item = (PrintTemplateItem) value;
                    lbl.setText(item.name);
                    lbl.setIcon(IconHelper.getIcon(item.iconName, 16, 16));
                    lbl.setBorder(new EmptyBorder(2, 4, 2, 4));
                }
                return lbl;
            }
        });

        listTemplates.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onConfirm();
                }
            }
        });

        JScrollPane spList = new JScrollPane(listTemplates);
        spList.setPreferredSize(new Dimension(460, 160));
        spList.setBorder(new LineBorder(new Color(180, 205, 235), 1));
        pnlContent.add(spList, BorderLayout.NORTH);

        // 2. Options Panel (Bottom)
        JPanel pnlOptions = new JPanel(null);
        pnlOptions.setBackground(new Color(215, 232, 250));
        pnlOptions.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        new LineBorder(new Color(165, 195, 230), 1),
                        "Tùy chọn",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        new Font("Segoe UI", Font.PLAIN, 12),
                        new Color(20, 50, 100)
                ),
                new EmptyBorder(4, 8, 4, 8)
        ));
        pnlOptions.setPreferredSize(new Dimension(460, 150));

        rbXemManHinh = new JRadioButton("Xem trên màn hình", true);
        rbXemManHinh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rbXemManHinh.setOpaque(false);
        rbXemManHinh.setBounds(15, 20, 150, 22);

        rbInMayIn = new JRadioButton("In ra máy in");
        rbInMayIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbInMayIn.setOpaque(false);
        rbInMayIn.setBounds(15, 46, 100, 22);

        cbPrinters = new JComboBox<>();
        cbPrinters.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cbPrinters.setBounds(120, 46, 320, 22);
        loadPrinters();

        rbXuatExcel = new JRadioButton("Xuất excel");
        rbXuatExcel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbXuatExcel.setOpaque(false);
        rbXuatExcel.setBounds(15, 74, 100, 22);

        rbXuatPdf = new JRadioButton("Xuất pdf");
        rbXuatPdf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbXuatPdf.setOpaque(false);
        rbXuatPdf.setBounds(140, 74, 100, 22);

        rbThietKe = new JRadioButton("Thiết kế mẫu in");
        rbThietKe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbThietKe.setOpaque(false);
        rbThietKe.setBounds(15, 102, 140, 22);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbXemManHinh);
        bg.add(rbInMayIn);
        bg.add(rbXuatExcel);
        bg.add(rbXuatPdf);
        bg.add(rbThietKe);

        pnlOptions.add(rbXemManHinh);
        pnlOptions.add(rbInMayIn);
        pnlOptions.add(cbPrinters);
        pnlOptions.add(rbXuatExcel);
        pnlOptions.add(rbXuatPdf);
        pnlOptions.add(rbThietKe);

        pnlContent.add(pnlOptions, BorderLayout.CENTER);
        add(pnlContent, BorderLayout.CENTER);

        // 3. Footer buttons (OK, Cancel)
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pnlFooter.setBackground(new Color(230, 240, 252));

        JButton btnOk = new JButton("OK");
        btnOk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnOk.setBackground(Color.WHITE);
        btnOk.setPreferredSize(new Dimension(75, 24));
        btnOk.addActionListener(e -> onConfirm());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(75, 24));
        btnCancel.addActionListener(e -> dispose());

        pnlFooter.add(btnOk);
        pnlFooter.add(btnCancel);
        add(pnlFooter, BorderLayout.SOUTH);

        // Key bindings
        KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKey, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getRootPane().registerKeyboardAction(e -> onConfirm(), enterKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadPrinters() {
        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            for (PrintService ps : services) {
                cbPrinters.addItem(ps.getName());
            }
        } catch (Exception ignored) {}

        if (cbPrinters.getItemCount() == 0) {
            cbPrinters.addItem("Máy in mặc định (Default Printer)");
            cbPrinters.addItem("Microsoft Print to PDF");
        }
    }

    private void onConfirm() {
        PrintTemplateItem item = listTemplates.getSelectedValue();
        if (item != null) {
            selectedTemplateName = item.name;
        }

        if (rbXemManHinh.isSelected()) {
            selectedAction = PrintAction.PREVIEW_SCREEN;
        } else if (rbInMayIn.isSelected()) {
            selectedAction = PrintAction.PRINT_PRINTER;
            selectedPrinterName = (String) cbPrinters.getSelectedItem();
        } else if (rbXuatExcel.isSelected()) {
            selectedAction = PrintAction.EXPORT_EXCEL;
        } else if (rbXuatPdf.isSelected()) {
            selectedAction = PrintAction.EXPORT_PDF;
        } else if (rbThietKe.isSelected()) {
            selectedAction = PrintAction.DESIGN_TEMPLATE;
        }

        confirmed = true;
        dispose();
        executeSelectedAction();
    }

    private void executeSelectedAction() {
        Window owner = getOwner();
        try {
            switch (selectedAction) {
                case PREVIEW_SCREEN -> billPrintService.preview(owner, donHang, selectedTemplateName);
                case PRINT_PRINTER -> billPrintService.print(donHang, selectedTemplateName, selectedPrinterName);
                case EXPORT_PDF -> billPrintService.exportPdf(owner, donHang, selectedTemplateName);
                case DESIGN_TEMPLATE -> billPrintService.design(owner, selectedTemplateName, donHang);
                case EXPORT_EXCEL -> JOptionPane.showMessageDialog(owner,
                        "Mẫu bill Java hiện hỗ trợ Preview, In và PDF. Xuất Excel sẽ được bổ sung sau.",
                        "Chưa hỗ trợ", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(owner, "Không thể xử lý mẫu in:\n" + rootMessage(ex),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }

    public boolean isConfirmed() { return confirmed; }
    public PrintAction getSelectedAction() { return selectedAction; }
    public String getSelectedTemplateName() { return selectedTemplateName; }
    public String getSelectedPrinterName() { return selectedPrinterName; }
}
