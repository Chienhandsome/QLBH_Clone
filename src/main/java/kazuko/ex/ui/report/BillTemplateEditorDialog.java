package kazuko.ex.ui.report;

import kazuko.ex.dao.BillTemplateDAO;
import kazuko.ex.model.bill.BillTemplate;
import kazuko.ex.model.bill.BillTemplateDefinition;
import kazuko.ex.model.bill.BillTemplateDefinition.Block;
import kazuko.ex.model.bill.BillTemplateDefinition.BlockType;
import kazuko.ex.model.bill.BillTemplateDefinition.Section;
import kazuko.ex.report.bill.BillReportRenderer;
import kazuko.ex.report.bill.JrxmlEditorServer;
import kazuko.ex.util.BillTemplateJson;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Bill editor dạng block thuần Swing. Đây là MVP cố ý giới hạn, không phải một
 * report designer tổng quát như FastReport.
 */
public class BillTemplateEditorDialog extends JDialog {
    private final BillTemplateDAO dao;
    private final BillTemplate workingTemplate;
    private final BillReportRenderer renderer = new BillReportRenderer();

    private final DefaultListModel<Block> blockModel = new DefaultListModel<>();
    private final JList<Block> blockList = new JList<>(blockModel);
    private final JPanel previewHolder = new JPanel(new BorderLayout());
    private final JTextField txtTemplateName = new JTextField();
    private final JSpinner spPaperWidth = new JSpinner(new SpinnerNumberModel(80, 48, 210, 1));
    private final JComboBox<Section> cbSection = new JComboBox<>(Section.values());
    private final JLabel lblType = new JLabel("-");
    private final JTextField txtText = new JTextField();
    private final JTextField txtLabel = new JTextField();
    private final JComboBox<String> cbBinding = new JComboBox<>(BillReportRenderer.SUPPORTED_BINDINGS.toArray(String[]::new));
    private final JSpinner spFontSize = new JSpinner(new SpinnerNumberModel(9, 6, 36, 1));
    private final JCheckBox chkBold = new JCheckBox("In đậm");
    private final JComboBox<String> cbAlignment = new JComboBox<>(new String[]{"LEFT", "CENTER", "RIGHT"});
    private final JCheckBox chkVisible = new JCheckBox("Hiển thị", true);
    private final JLabel lblPreviewStatus = new JLabel(" ");

    private boolean loadingProperties;
    private int previewGeneration;

    public BillTemplateEditorDialog(Window owner, BillTemplate template, BillTemplateDAO dao) {
        super(owner, "THIẾT KẾ MẪU BILL - " + template.getName(), ModalityType.APPLICATION_MODAL);
        this.dao = dao;
        this.workingTemplate = copyTemplate(template);
        initializeUi();
        loadTemplate();
        refreshPreview();
    }

    public static void open(Window owner, BillTemplate template, BillTemplateDAO dao) {
        BillTemplateEditorDialog dialog = new BillTemplateEditorDialog(owner, template, dao);
        dialog.setVisible(true);
    }

    private void initializeUi() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setSize(1320, 820);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(8, 8, 8, 8));
        setContentPane(root);
        root.add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane leftCenter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildBlockPanel(), previewHolder);
        leftCenter.setResizeWeight(0.22);
        leftCenter.setDividerLocation(280);
        JSplitPane all = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCenter, buildPropertyPanel());
        all.setResizeWeight(0.78);
        all.setDividerLocation(980);
        root.add(all, BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JComponent buildTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.add(new JLabel("Tên mẫu:"));
        txtTemplateName.setPreferredSize(new Dimension(280, 26));
        panel.add(txtTemplateName);
        panel.add(new JLabel("Khổ giấy (mm):"));
        spPaperWidth.setPreferredSize(new Dimension(70, 26));
        panel.add(spPaperWidth);
        JButton btnRefresh = new JButton("Làm mới preview");
        btnRefresh.addActionListener(e -> {
            applyCurrentProperties();
            applyTemplateProperties();
            refreshPreview();
        });
        panel.add(btnRefresh);
        JButton btnCanvas = new JButton("Thử canvas JRXML");
        btnCanvas.setToolTipText("Mở ngx-jrxml-editor trong trình duyệt mặc định (PoC)");
        btnCanvas.addActionListener(e -> openJrxmlCanvas());
        panel.add(btnCanvas);
        return panel;
    }

    private void openJrxmlCanvas() {
        applyCurrentProperties();
        applyTemplateProperties();
        try {
            JrxmlEditorServer.open(workingTemplate);
            JOptionPane.showMessageDialog(this,
                    "Canvas JRXML đã mở trong trình duyệt.\n" +
                            "Bấm 'Lưu và kiểm tra' tại đó để JasperReports compile rồi lưu vào Firebird.",
                    "JRXML Canvas (PoC)", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể mở canvas JRXML:\n" + rootMessage(ex),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JComponent buildBlockPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Thành phần"));
        blockList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blockList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Block block) {
                    label.setText("[" + sectionName(block.getSection()) + "] " + block);
                    if (!block.isVisible()) label.setForeground(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
                }
                label.setBorder(new EmptyBorder(4, 5, 4, 5));
                return label;
            }
        });
        blockList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedBlock();
        });
        panel.add(new JScrollPane(blockList), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(0, 2, 4, 4));
        JButton addText = new JButton("+ Văn bản");
        addText.addActionListener(e -> addBlock(BillTemplateDefinition.text(Section.HEADER, "Văn bản mới", 9, false, "LEFT")));
        JButton addField = new JButton("+ Dữ liệu");
        addField.addActionListener(e -> addBlock(BillTemplateDefinition.field(Section.SUMMARY, "Nhãn: ", "total", 9, false, "RIGHT")));
        JButton addLine = new JButton("+ Đường kẻ");
        addLine.addActionListener(e -> addBlock(BillTemplateDefinition.separator(Section.SUMMARY)));
        JButton delete = new JButton("Xóa");
        delete.addActionListener(e -> deleteSelected());
        JButton up = new JButton("Lên");
        up.addActionListener(e -> moveSelected(-1));
        JButton down = new JButton("Xuống");
        down.addActionListener(e -> moveSelected(1));
        actions.add(addText); actions.add(addField);
        actions.add(addLine); actions.add(delete);
        actions.add(up); actions.add(down);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildPropertyPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createTitledBorder("Thuộc tính"));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 5, 4, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0; c.gridy = 0;
        addRow(form, c, "Loại", lblType);
        addRow(form, c, "Khu vực", cbSection);
        addRow(form, c, "Nội dung", txtText);
        addRow(form, c, "Nhãn", txtLabel);
        addRow(form, c, "Binding", cbBinding);
        addRow(form, c, "Cỡ chữ", spFontSize);
        addRow(form, c, "Căn lề", cbAlignment);
        c.gridx = 1; c.gridy++;
        form.add(chkBold, c);
        c.gridy++;
        form.add(chkVisible, c);
        c.gridy++;
        JButton apply = new JButton("Áp dụng thuộc tính");
        apply.addActionListener(e -> {
            applyCurrentProperties();
            refreshPreview();
        });
        form.add(apply, c);
        c.gridy++; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        form.add(Box.createVerticalGlue(), c);
        wrapper.add(form, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout());
        lblPreviewStatus.setForeground(new Color(80, 80, 80));
        panel.add(lblPreviewStatus, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton cancel = new JButton("Đóng");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Lưu mẫu");
        save.addActionListener(e -> saveTemplate());
        buttons.add(cancel);
        buttons.add(save);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    private static void addRow(JPanel form, GridBagConstraints c, String label, Component component) {
        c.gridx = 0; c.weightx = 0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(new JLabel(label + ":"), c);
        c.gridx = 1; c.weightx = 1;
        form.add(component, c);
        c.gridy++;
    }

    private void loadTemplate() {
        txtTemplateName.setText(workingTemplate.getName());
        spPaperWidth.setValue(workingTemplate.getPaperWidth());
        blockModel.clear();
        for (Block block : workingTemplate.getDefinition().getBlocks()) blockModel.addElement(block);
        if (!blockModel.isEmpty()) blockList.setSelectedIndex(0);
    }

    private void loadSelectedBlock() {
        Block block = blockList.getSelectedValue();
        loadingProperties = true;
        try {
            boolean selected = block != null;
            lblType.setText(selected ? typeName(block.getType()) : "-");
            cbSection.setEnabled(selected && block.getType() != BlockType.ITEMS);
            txtText.setEnabled(selected && block.getType() == BlockType.TEXT);
            txtLabel.setEnabled(selected && block.getType() == BlockType.FIELD);
            cbBinding.setEnabled(selected && block.getType() == BlockType.FIELD);
            spFontSize.setEnabled(selected && (block.getType() == BlockType.TEXT || block.getType() == BlockType.FIELD));
            chkBold.setEnabled(spFontSize.isEnabled());
            cbAlignment.setEnabled(spFontSize.isEnabled());
            chkVisible.setEnabled(selected);
            if (!selected) return;
            cbSection.setSelectedItem(block.getSection());
            txtText.setText(block.getText());
            txtLabel.setText(block.getLabel());
            cbBinding.setSelectedItem(block.getBinding());
            spFontSize.setValue(block.getFontSize());
            chkBold.setSelected(block.isBold());
            cbAlignment.setSelectedItem(block.getAlignment());
            chkVisible.setSelected(block.isVisible());
        } finally {
            loadingProperties = false;
        }
    }

    private void applyCurrentProperties() {
        if (loadingProperties) return;
        Block block = blockList.getSelectedValue();
        if (block == null) return;
        if (block.getType() != BlockType.ITEMS) block.setSection((Section) cbSection.getSelectedItem());
        if (block.getType() == BlockType.TEXT) block.setText(txtText.getText());
        if (block.getType() == BlockType.FIELD) {
            block.setLabel(txtLabel.getText());
            block.setBinding((String) cbBinding.getSelectedItem());
        }
        block.setFontSize((Integer) spFontSize.getValue());
        block.setBold(chkBold.isSelected());
        block.setAlignment((String) cbAlignment.getSelectedItem());
        block.setVisible(chkVisible.isSelected());
        blockList.repaint();
    }

    private void applyTemplateProperties() {
        workingTemplate.setName(txtTemplateName.getText().trim());
        workingTemplate.setPaperWidth((Integer) spPaperWidth.getValue());
    }

    private void addBlock(Block block) {
        applyCurrentProperties();
        workingTemplate.getDefinition().getBlocks().add(block);
        blockModel.addElement(block);
        blockList.setSelectedIndex(blockModel.size() - 1);
        refreshPreview();
    }

    private void deleteSelected() {
        int index = blockList.getSelectedIndex();
        if (index < 0) return;
        Block block = blockModel.get(index);
        if (block.getType() == BlockType.ITEMS) {
            JOptionPane.showMessageDialog(this, "Bảng mặt hàng là thành phần bắt buộc. Bạn có thể bỏ chọn Hiển thị.",
                    "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        workingTemplate.getDefinition().getBlocks().remove(block);
        blockModel.remove(index);
        if (!blockModel.isEmpty()) blockList.setSelectedIndex(Math.min(index, blockModel.size() - 1));
        refreshPreview();
    }

    private void moveSelected(int delta) {
        applyCurrentProperties();
        int from = blockList.getSelectedIndex();
        int to = from + delta;
        if (from < 0 || to < 0 || to >= blockModel.size()) return;
        Block selected = blockModel.get(from);
        blockModel.remove(from);
        blockModel.add(to, selected);
        List<Block> blocks = workingTemplate.getDefinition().getBlocks();
        blocks.remove(from);
        blocks.add(to, selected);
        blockList.setSelectedIndex(to);
        refreshPreview();
    }

    private void refreshPreview() {
        applyTemplateProperties();
        int generation = ++previewGeneration;
        lblPreviewStatus.setText("Đang tạo preview...");
        new SwingWorker<JasperPrint, Void>() {
            @Override
            protected JasperPrint doInBackground() throws Exception {
                return renderer.renderSample(workingTemplate);
            }

            @Override
            protected void done() {
                if (generation != previewGeneration) return;
                try {
                    JasperPrint print = get();
                    previewHolder.removeAll();
                    previewHolder.add(new JRViewer(print), BorderLayout.CENTER);
                    lblPreviewStatus.setText("Preview dữ liệu mẫu • version " + workingTemplate.getVersionNo());
                } catch (Exception ex) {
                    previewHolder.removeAll();
                    JTextArea error = new JTextArea("Không thể tạo preview:\n" + rootMessage(ex));
                    error.setEditable(false);
                    error.setForeground(new Color(170, 30, 30));
                    error.setBorder(new EmptyBorder(15, 15, 15, 15));
                    previewHolder.add(error, BorderLayout.CENTER);
                    lblPreviewStatus.setText("Preview lỗi");
                }
                previewHolder.revalidate();
                previewHolder.repaint();
            }
        }.execute();
    }

    private void saveTemplate() {
        applyCurrentProperties();
        applyTemplateProperties();
        if (workingTemplate.getName().isBlank()) {
            JOptionPane.showMessageDialog(this, "Tên mẫu không được để trống.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            renderer.createDesign(workingTemplate); // validate trước khi ghi DB
            dao.save(workingTemplate);
            JOptionPane.showMessageDialog(this, "Đã lưu mẫu bill Java thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể lưu mẫu:\n" + rootMessage(ex), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static BillTemplate copyTemplate(BillTemplate source) {
        BillTemplate copy = new BillTemplate();
        copy.setId(source.getId());
        copy.setLegacyTemplateId(source.getLegacyTemplateId());
        copy.setName(source.getName());
        copy.setPaperWidth(source.getPaperWidth());
        copy.setDefinition(BillTemplateJson.copy(source.getDefinition()));
        copy.setStatus(source.getStatus());
        copy.setVersionNo(source.getVersionNo());
        copy.setTimeCreated(source.getTimeCreated());
        copy.setTimeModified(source.getTimeModified());
        return copy;
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }

    private static String sectionName(Section section) {
        return switch (section) {
            case HEADER -> "Đầu bill";
            case SUMMARY -> "Tổng cộng";
            case FOOTER -> "Cuối bill";
        };
    }

    private static String typeName(BlockType type) {
        return switch (type) {
            case TEXT -> "Văn bản";
            case FIELD -> "Dữ liệu";
            case ITEMS -> "Bảng mặt hàng";
            case SEPARATOR -> "Đường phân cách";
        };
    }
}
