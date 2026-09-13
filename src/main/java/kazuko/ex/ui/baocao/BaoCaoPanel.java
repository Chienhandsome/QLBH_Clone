package kazuko.ex.ui.baocao;

import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/** Màn hình điều hướng và làm việc tập trung cho toàn bộ báo cáo. */
public class BaoCaoPanel extends JPanel {
    private static final int NAVIGATION_WIDTH = 375;

    private final JMenu reportMenu;
    private final Function<String, JComponent> reportPanelFactory;
    private final DefaultListModel<ReportEntry> reportListModel = new DefaultListModel<>();
    private final JList<ReportEntry> reportList = new JList<>(reportListModel);
    private final List<ReportEntry> allReports = new ArrayList<>();
    private final JButton designTemplateButton = new JButton("Thiết kế mẫu");
    private final JPanel reportWorkspace = new JPanel(new BorderLayout());
    private String displayedReportName;

    public BaoCaoPanel(JMenu reportMenu, Function<String, JComponent> reportPanelFactory) {
        this.reportMenu = reportMenu;
        this.reportPanelFactory = reportPanelFactory;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 205, 222)));
        designTemplateButton.setIcon(IconHelper.getIcon("design-bump.png", 16, 16));
        designTemplateButton.setFocusable(false);
        designTemplateButton.setEnabled(false);
        designTemplateButton.addActionListener(e -> openTemplateDesigner());
        toolbar.add(designTemplateButton);
        add(toolbar, BorderLayout.NORTH);

        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setMinimumSize(new Dimension(280, 0));
        navigationPanel.setPreferredSize(new Dimension(NAVIGATION_WIDTH, 0));

        DefaultMutableTreeNode hiddenRoot = new DefaultMutableTreeNode();
        if (reportMenu != null) {
            addMenuFolders(hiddenRoot, reportMenu);
        }

        JTree folderTree = createFolderTree(hiddenRoot);
        JTabbedPane navigationTabs = new JTabbedPane(JTabbedPane.TOP);
        navigationTabs.addTab("Tất cả", new JScrollPane(folderTree));
        navigationTabs.addTab("Thường dùng", createFrequentlyUsedPanel());
        navigationTabs.addTab("Tìm kiếm", createSearchPanel());

        configureReportList();
        JScrollPane reportScroll = new JScrollPane(reportList);
        reportScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                UIManager.getColor("Component.borderColor")));

        JSplitPane navigationSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, navigationTabs, reportScroll);
        navigationSplit.setResizeWeight(0.43);
        navigationSplit.setDividerLocation(285);
        navigationSplit.setDividerSize(5);
        navigationSplit.setContinuousLayout(true);
        navigationSplit.setBorder(BorderFactory.createEmptyBorder());
        navigationPanel.add(navigationSplit, BorderLayout.CENTER);

        reportWorkspace.setBackground(Color.WHITE);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, navigationPanel, reportWorkspace);
        mainSplit.setDividerLocation(NAVIGATION_WIDTH);
        mainSplit.setDividerSize(5);
        mainSplit.setContinuousLayout(true);
        mainSplit.setResizeWeight(0.0);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());
        add(mainSplit, BorderLayout.CENTER);

        if (hiddenRoot.getChildCount() > 0) {
            folderTree.setSelectionRow(0);
        }
    }

    private JTree createFolderTree(DefaultMutableTreeNode hiddenRoot) {
        JTree tree = new JTree(new DefaultTreeModel(hiddenRoot));
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(23);
        tree.setBorder(new EmptyBorder(5, 5, 5, 5));
        tree.setCellRenderer(new FolderTreeRenderer());
        tree.addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path == null) {
                reportListModel.clear();
                return;
            }
            Object selected = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
            if (selected instanceof FolderEntry) {
                showReports(((FolderEntry) selected).reports);
            }
        });

        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
        return tree;
    }

    private void addMenuFolders(DefaultMutableTreeNode parentNode, JMenu parentMenu) {
        for (Component component : parentMenu.getMenuComponents()) {
            if (!(component instanceof JMenu) || !component.isVisible()) {
                continue;
            }

            JMenu childMenu = (JMenu) component;
            FolderEntry folder = new FolderEntry(childMenu.getText());
            DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folder);
            parentNode.add(folderNode);

            for (Component child : childMenu.getMenuComponents()) {
                if (child instanceof JMenuItem && !(child instanceof JMenu) && child.isVisible()) {
                    JMenuItem item = (JMenuItem) child;
                    ReportEntry report = new ReportEntry(item.getText(), item.getIcon());
                    folder.reports.add(report);
                    allReports.add(report);
                }
            }
            addMenuFolders(folderNode, childMenu);
        }
    }

    private void configureReportList() {
        reportList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportList.setFixedCellHeight(23);
        reportList.setBorder(new EmptyBorder(5, 5, 5, 5));
        reportList.setCellRenderer(new ReportListRenderer());
        reportList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                designTemplateButton.setEnabled(reportList.getSelectedValue() != null);
                showSelectedReport();
            }
        });
        reportList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedReport();
                }
            }
        });
        reportList.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "openReport");
        reportList.getActionMap().put("openReport", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openSelectedReport();
            }
        });
    }

    private JPanel createFrequentlyUsedPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel emptyLabel = new JLabel("Chưa có báo cáo thường dùng", SwingConstants.CENTER);
        emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        panel.add(emptyLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(7, 7, 7, 7));

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Nhập tên báo cáo...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterReports(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterReports(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterReports(searchField.getText());
            }
        });
        panel.add(searchField, BorderLayout.NORTH);
        return panel;
    }

    private void filterReports(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ReportEntry> matches = new ArrayList<>();
        for (ReportEntry report : allReports) {
            if (normalizedKeyword.isEmpty()
                    || report.name.toLowerCase(Locale.ROOT).contains(normalizedKeyword)) {
                matches.add(report);
            }
        }
        showReports(matches);
    }

    private void showReports(List<ReportEntry> reports) {
        reportListModel.clear();
        for (ReportEntry report : reports) {
            reportListModel.addElement(report);
        }
        if (!reportListModel.isEmpty()) {
            reportList.setSelectedIndex(0);
        }
    }

    private void openSelectedReport() {
        showSelectedReport();
    }

    private void showSelectedReport() {
        ReportEntry selected = reportList.getSelectedValue();
        if (selected == null || reportPanelFactory == null) return;
        if (selected.name.equals(displayedReportName) && reportWorkspace.getComponentCount() > 0) return;

        reportWorkspace.removeAll();
        try {
            JComponent reportPanel = reportPanelFactory.apply(selected.name);
            if (reportPanel != null) {
                reportWorkspace.add(reportPanel, BorderLayout.CENTER);
                displayedReportName = selected.name;
            } else {
                reportWorkspace.add(createUnavailableReportMessage(selected.name), BorderLayout.CENTER);
                displayedReportName = null;
            }
        } catch (RuntimeException ex) {
            JLabel error = new JLabel("Không thể mở báo cáo: " + ex.getMessage(), SwingConstants.CENTER);
            error.setForeground(new Color(170, 35, 35));
            reportWorkspace.add(error, BorderLayout.CENTER);
            displayedReportName = null;
        }
        reportWorkspace.revalidate();
        reportWorkspace.repaint();
    }

    private JComponent createUnavailableReportMessage(String reportName) {
        JLabel message = new JLabel(
                "<html><center><b>" + reportName + "</b><br><br>"
                        + "Báo cáo này chưa có màn hình tham số.</center></html>",
                SwingConstants.CENTER);
        message.setForeground(UIManager.getColor("Label.disabledForeground"));
        return message;
    }

    private void openTemplateDesigner() {
        ReportEntry selected = reportList.getSelectedValue();
        if (selected == null) return;

        ReportColumnResolver.ResolvedReport resolved = ReportColumnResolver.resolveReport(selected.name);
        if (resolved.columns().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy định nghĩa cột của báo cáo “" + selected.name + "”.",
                    "Thiết kế mẫu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        new MauInReportDialog(owner, selected.name, resolved.reportKey(), resolved.columns()).setVisible(true);
    }

    private static class FolderEntry {
        private final String name;
        private final List<ReportEntry> reports = new ArrayList<>();

        private FolderEntry(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class ReportEntry {
        private final String name;
        private final Icon icon;

        private ReportEntry(String name, Icon icon) {
            this.name = name;
            this.icon = icon != null ? icon : IconHelper.getIcon("Report.png", 16, 16);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class FolderTreeRenderer extends DefaultTreeCellRenderer {
        private final Icon closedFolderIcon = IconHelper.getIcon("Folder.png", 16, 16);
        private final Icon openFolderIcon = IconHelper.getIcon("btnCloseFolder.Values.Image.png", 16, 16);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                       boolean expanded, boolean leaf, int row,
                                                       boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(
                    tree, value, selected, expanded, leaf, row, hasFocus);
            label.setIcon(expanded && openFolderIcon != null ? openFolderIcon : closedFolderIcon);
            return label;
        }
    }

    private static class ReportListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean selected, boolean hasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, selected, hasFocus);
            if (value instanceof ReportEntry) {
                ReportEntry report = (ReportEntry) value;
                label.setText(report.name);
                label.setIcon(report.icon);
            }
            label.setBorder(new EmptyBorder(1, 5, 1, 3));
            return label;
        }
    }
}
