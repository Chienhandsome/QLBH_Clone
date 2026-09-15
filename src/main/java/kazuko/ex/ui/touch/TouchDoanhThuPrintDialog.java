package kazuko.ex.ui.touch;

import kazuko.ex.report.service.BaoCaoBanHangReportService;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.swing.JRViewer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Touch report output picker for the revenue report. */
final class TouchDoanhThuPrintDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86), HEADER = new Color(79, 139, 169), TEAL = new Color(0, 79, 76);
    private static final Color GREEN = new Color(0, 85, 20), OLIVE = new Color(88, 82, 0), ORANGE = new Color(255, 130, 0), RED = new Color(165, 15, 15);
    private final ReportProvider reportProvider;
    private final JPanel templates = new JPanel(), printers = new JPanel();
    private String selectedTemplate = "58mm";
    private JasperPrint cachedPrint;

    TouchDoanhThuPrintDialog(Window owner, BaoCaoBanHangReportService service, Date from, Date to, String storeId) {
        this(owner, () -> service.generateBanHangTheoNgay(from, to, null, null, storeId));
    }

    TouchDoanhThuPrintDialog(Window owner, ReportProvider reportProvider) {
        super(owner, "CHỌN MẪU IN", ModalityType.APPLICATION_MODAL);
        this.reportProvider = reportProvider;
        setUndecorated(true); setLayout(new BorderLayout(8, 8)); getContentPane().setBackground(BG);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));
        add(header(), BorderLayout.NORTH); add(body(), BorderLayout.CENTER);
        JButton cancel = button("HỦY BỎ", RED); cancel.setPreferredSize(new Dimension(0, 78)); cancel.addActionListener(e -> dispose()); add(cancel, BorderLayout.SOUTH);
        setSize(owner == null ? Toolkit.getDefaultToolkit().getScreenSize() : owner.getSize()); setLocationRelativeTo(owner);
    }

    private JPanel header() { JPanel p = new JPanel(new BorderLayout()); p.setBackground(HEADER); p.setPreferredSize(new Dimension(0, 64)); JLabel l = new JLabel("CHỌN MẪU IN", SwingConstants.CENTER); l.setForeground(Color.WHITE); l.setFont(TouchThongKeDoanhThuPanel.font(25)); p.add(l); return p; }

    private JPanel body() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        templates.setLayout(new BoxLayout(templates, BoxLayout.Y_AXIS)); templates.setBackground(HEADER); templates.setBorder(new EmptyBorder(7, 7, 7, 7)); renderTemplates();
        c.gridx = 0; c.weightx = .37; p.add(new JScrollPane(templates), c);
        JPanel right = new JPanel(new BorderLayout(0, 7)); right.setOpaque(false);
        JPanel exports = new JPanel(new GridLayout(2, 2, 7, 7)); exports.setOpaque(false);
        JButton preview = button("XEM TRÊN MÀN HÌNH", GREEN); preview.addActionListener(e -> withPrint(this::preview)); exports.add(preview); exports.add(new JLabel());
        JButton excel = button("XUẤT EXCEL", OLIVE), pdf = button("XUẤT PDF", OLIVE); excel.addActionListener(e -> withPrint(this::exportExcel)); pdf.addActionListener(e -> withPrint(this::exportPdf)); exports.add(excel); exports.add(pdf);
        exports.setPreferredSize(new Dimension(0, 172)); right.add(exports, BorderLayout.NORTH);
        JPanel printerBox = new JPanel(new BorderLayout()); printerBox.setBackground(new Color(232, 232, 232));
        JLabel caption = new JLabel("HOẶC LỰA CHỌN MÁY IN ĐỂ IN"); caption.setForeground(Color.WHITE); caption.setFont(TouchThongKeDoanhThuPanel.font(17)); caption.setOpaque(true); caption.setBackground(BG); caption.setBorder(new EmptyBorder(7, 3, 7, 3)); printerBox.add(caption, BorderLayout.NORTH);
        printers.setLayout(new BoxLayout(printers, BoxLayout.Y_AXIS)); printers.setBackground(new Color(232, 232, 232)); printers.setBorder(new EmptyBorder(7, 7, 7, 7)); loadPrinters();
        JScrollPane printerScroll = new JScrollPane(printers); printerScroll.setBorder(null); printerBox.add(printerScroll, BorderLayout.CENTER); right.add(printerBox, BorderLayout.CENTER);
        c.gridx = 1; c.weightx = .63; c.insets = new Insets(0, 7, 0, 0); p.add(right, c); return p;
    }

    private void renderTemplates() {
        templates.removeAll(); for (String name : new String[]{"58mm", "80mm", "A4"}) {
            JButton b = button(name, name.equals(selectedTemplate) ? ORANGE : GREEN); b.setHorizontalAlignment(SwingConstants.LEFT); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105)); b.setPreferredSize(new Dimension(0, 105));
            b.addActionListener(e -> { selectedTemplate = name; renderTemplates(); }); templates.add(b); templates.add(Box.createVerticalStrut(7));
        } templates.revalidate(); templates.repaint();
    }

    private void loadPrinters() {
        printers.removeAll(); PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        if (services.length == 0) { JLabel empty = new JLabel("Không tìm thấy máy in", SwingConstants.CENTER); empty.setFont(TouchThongKeDoanhThuPanel.font(18)); printers.add(empty); }
        for (PrintService ps : services) { JButton b = button(ps.getName().toUpperCase(), TEAL); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); b.setPreferredSize(new Dimension(0, 100)); b.addActionListener(e -> withPrint(print -> printTo(print, ps.getName()))); printers.add(b); printers.add(Box.createVerticalStrut(7)); }
    }

    private void withPrint(PrintAction action) {
        setEnabled(false); setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<JasperPrint, Void>() {
            @Override protected JasperPrint doInBackground() throws Exception { return cachedPrint != null ? cachedPrint : reportProvider.create(); }
            @Override protected void done() { try { cachedPrint = get(); action.run(cachedPrint); } catch (Exception ex) { JOptionPane.showMessageDialog(TouchDoanhThuPrintDialog.this, "Không thể tạo báo cáo: " + rootMessage(ex), "IN BÁO CÁO", JOptionPane.ERROR_MESSAGE); } finally { setEnabled(true); setCursor(Cursor.getDefaultCursor()); } }
        }.execute();
    }

    private void preview(JasperPrint print) { JDialog d = new JDialog(this, "XEM TRƯỚC DOANH THU", ModalityType.MODELESS); d.setLayout(new BorderLayout()); d.add(new JRViewer(print)); d.setSize(1100, 760); d.setLocationRelativeTo(this); d.setVisible(true); }
    private void exportPdf(JasperPrint print) throws Exception { File file = choose("Xuất báo cáo PDF", "pdf"); if (file == null) return; JasperExportManager.exportReportToPdfFile(print, file.getAbsolutePath()); done(file); }
    private void exportExcel(JasperPrint print) throws Exception { File file = choose("Xuất báo cáo Excel", "xlsx"); if (file == null) return; JRXlsxExporter exporter = new JRXlsxExporter(); exporter.setExporterInput(new SimpleExporterInput(print)); exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(file)); SimpleXlsxReportConfiguration cfg = new SimpleXlsxReportConfiguration(); cfg.setDetectCellType(true); cfg.setCollapseRowSpan(false); exporter.setConfiguration(cfg); exporter.exportReport(); done(file); }
    private void printTo(JasperPrint print, String printer) throws Exception { JRPrintServiceExporter exporter = new JRPrintServiceExporter(); exporter.setExporterInput(new SimpleExporterInput(print)); SimplePrintServiceExporterConfiguration cfg = new SimplePrintServiceExporterConfiguration(); HashPrintServiceAttributeSet serviceAttrs = new HashPrintServiceAttributeSet(); serviceAttrs.add(new PrinterName(printer, null)); cfg.setPrintServiceAttributeSet(serviceAttrs); cfg.setPrintRequestAttributeSet(new HashPrintRequestAttributeSet()); cfg.setDisplayPageDialog(false); cfg.setDisplayPrintDialog(false); exporter.setConfiguration(cfg); exporter.exportReport(); JOptionPane.showMessageDialog(this, "Đã gửi báo cáo tới máy in " + printer + "."); }
    private File choose(String title, String extension) { JFileChooser c = new JFileChooser(); c.setDialogTitle(title); c.setFileFilter(new FileNameExtensionFilter(extension.toUpperCase() + " (*." + extension + ")", extension)); c.setSelectedFile(new File("bao-cao-" + new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date()) + "." + extension)); if (c.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return null; File f = c.getSelectedFile(); return f.getName().toLowerCase().endsWith("." + extension) ? f : new File(f.getParentFile(), f.getName() + "." + extension); }
    private void done(File file) { JOptionPane.showMessageDialog(this, "Đã xuất báo cáo:\n" + file.getAbsolutePath(), "IN BÁO CÁO", JOptionPane.INFORMATION_MESSAGE); }
    private static JButton button(String text, Color bg) { JButton b = TouchThongKeDoanhThuPanel.button(text, bg); b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true)); return b; }
    private static String rootMessage(Throwable t) { Throwable c = t; while (c.getCause() != null) c = c.getCause(); return c.getMessage(); }
    @FunctionalInterface private interface PrintAction { void run(JasperPrint print) throws Exception; }
    @FunctionalInterface interface ReportProvider { JasperPrint create() throws Exception; }
}
