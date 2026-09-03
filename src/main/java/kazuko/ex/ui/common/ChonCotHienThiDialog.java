package kazuko.ex.ui.common;

import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ChonCotHienThiDialog extends JDialog {

    private final JTable targetTable;
    private final List<ColumnInfo> allAvailableColumns;

    private DefaultListModel<ColumnInfo> modelHidden;
    private JList<ColumnInfo> listHidden;

    private DefaultListModel<ColumnInfo> modelVisible;
    private JList<ColumnInfo> listVisible;

    private JButton btnAdd;
    private JButton btnRemove;
    private JButton btnMoveUp;
    private JButton btnMoveDown;

    private JButton btnRestore;
    private JButton btnAccept;
    private JButton btnCancel;

    public static class ColumnInfo {
        private final String name;
        private final TableColumn tableColumn;
        private final int defaultIndex;

        public ColumnInfo(String name, TableColumn tableColumn, int defaultIndex) {
            this.name = name;
            this.tableColumn = tableColumn;
            this.defaultIndex = defaultIndex;
        }

        public String getName() { return name; }
        public TableColumn getTableColumn() { return tableColumn; }
        public int getDefaultIndex() { return defaultIndex; }

        @Override
        public String toString() {
            return name;
        }
    }

    public ChonCotHienThiDialog(Window parent, JTable table, List<ColumnInfo> allColumns) {
        super(parent, "Chọn cột hiển thị", ModalityType.APPLICATION_MODAL);
        this.targetTable = table;
        this.allAvailableColumns = new ArrayList<>(allColumns);

        initComponents();
        populateLists();
        setupEvents();

        setSize(560, 420);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 10));
        contentPane.setBackground(new Color(214, 230, 248)); // Nền xanh nhạt chuẩn theo ảnh
        contentPane.setBorder(new EmptyBorder(12, 14, 12, 14));
        setContentPane(contentPane);

        // Header Panel: Icon tài liệu xanh + hướng dẫn
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlHeader.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("page_white_add.png", 28, 28));
        JPanel pnlHeaderText = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlHeaderText.setOpaque(false);

        JLabel lblTitle = new JLabel("Mời bạn chọn các cột muốn hiển thị trên lưới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(15, 45, 90));

        JLabel lblHint = new JLabel("Chú ý: Bạn có thể nháy đúp vào danh sách cột để thêm hoặc xóa cột");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(60, 70, 85));

        pnlHeaderText.add(lblTitle);
        pnlHeaderText.add(lblHint);

        pnlHeader.add(lblIcon);
        pnlHeader.add(pnlHeaderText);

        // Center: Dual List with Buttons (Images 3, 4, 5)
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.BOTH;

        // Left List: Danh sách các cột ẩn
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 4));
        pnlLeft.setOpaque(false);
        JLabel lblHidden = new JLabel("Danh sách các cột ẩn");
        lblHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlLeft.add(lblHidden, BorderLayout.NORTH);

        modelHidden = new DefaultListModel<>();
        listHidden = new JList<>(modelHidden);
        listHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listHidden.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        JScrollPane spHidden = new JScrollPane(listHidden);
        spHidden.setPreferredSize(new Dimension(170, 240));
        pnlLeft.add(spHidden, BorderLayout.CENTER);

        // Middle Buttons: Thêm >> , << Xóa
        JPanel pnlMiddleBtns = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlMiddleBtns.setOpaque(false);
        btnAdd = createActionButton("Thêm >>");
        btnRemove = createActionButton("<< Xóa");
        pnlMiddleBtns.add(btnAdd);
        pnlMiddleBtns.add(btnRemove);

        // Right List: Danh sách các cột hiển thị trên lưới
        JPanel pnlRight = new JPanel(new BorderLayout(0, 4));
        pnlRight.setOpaque(false);
        JLabel lblVisible = new JLabel("Danh sách các cột hiển thị trên lưới");
        lblVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlRight.add(lblVisible, BorderLayout.NORTH);

        modelVisible = new DefaultListModel<>();
        listVisible = new JList<>(modelVisible);
        listVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listVisible.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        JScrollPane spVisible = new JScrollPane(listVisible);
        spVisible.setPreferredSize(new Dimension(185, 240));
        pnlRight.add(spVisible, BorderLayout.CENTER);

        // Rightmost Buttons: Chuyển lên, Chuyển xuống
        JPanel pnlRightBtns = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlRightBtns.setOpaque(false);
        btnMoveUp = createActionButton("Chuyển lên");
        btnMoveDown = createActionButton("Chuyển xuống");
        pnlRightBtns.add(btnMoveUp);
        pnlRightBtns.add(btnMoveDown);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.4; gbc.weighty = 1.0;
        pnlCenter.add(pnlLeft, gbc);

        gbc.gridx = 1; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.NONE;
        pnlCenter.add(pnlMiddleBtns, gbc);

        gbc.gridx = 2; gbc.weightx = 0.4; gbc.fill = GridBagConstraints.BOTH;
        pnlCenter.add(pnlRight, gbc);

        gbc.gridx = 3; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.NONE;
        pnlCenter.add(pnlRightBtns, gbc);

        // Bottom Bar: Khôi phục, Chấp nhận, Hủy bỏ
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setOpaque(false);

        btnRestore = createActionButton("Khôi phục");
        btnAccept = createActionButton("Chấp nhận");
        btnCancel = createActionButton("Hủy bỏ");

        pnlBottom.add(btnRestore);
        pnlBottom.add(btnAccept);
        pnlBottom.add(btnCancel);

        contentPane.add(pnlHeader, BorderLayout.NORTH);
        contentPane.add(pnlCenter, BorderLayout.CENTER);
        contentPane.add(pnlBottom, BorderLayout.SOUTH);
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 225), 1), new EmptyBorder(4, 12, 4, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void populateLists() {
        modelHidden.clear();
        modelVisible.clear();

        TableColumnModel colModel = targetTable.getColumnModel();
        List<String> visibleHeaders = new ArrayList<>();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            Object headerVal = colModel.getColumn(i).getHeaderValue();
            if (headerVal != null) {
                visibleHeaders.add(headerVal.toString().trim());
            }
        }

        for (ColumnInfo info : allAvailableColumns) {
            if (visibleHeaders.contains(info.getName())) {
                modelVisible.addElement(info);
            } else {
                modelHidden.addElement(info);
            }
        }
    }

    private void setupEvents() {
        // Double-click to move
        listHidden.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) moveHiddenToVisible();
            }
        });

        listVisible.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) moveVisibleToHidden();
            }
        });

        btnAdd.addActionListener(e -> moveHiddenToVisible());
        btnRemove.addActionListener(e -> moveVisibleToHidden());

        btnMoveUp.addActionListener(e -> moveVisibleItem(-1));
        btnMoveDown.addActionListener(e -> moveVisibleItem(1));

        btnRestore.addActionListener(e -> restoreDefaults());
        btnAccept.addActionListener(e -> applyColumnChanges());
        btnCancel.addActionListener(e -> dispose());
    }

    private void moveHiddenToVisible() {
        List<ColumnInfo> selected = listHidden.getSelectedValuesList();
        for (ColumnInfo item : selected) {
            modelHidden.removeElement(item);
            modelVisible.addElement(item);
        }
    }

    private void moveVisibleToHidden() {
        List<ColumnInfo> selected = listVisible.getSelectedValuesList();
        for (ColumnInfo item : selected) {
            modelVisible.removeElement(item);
            modelHidden.addElement(item);
        }
    }

    private void moveVisibleItem(int direction) {
        int idx = listVisible.getSelectedIndex();
        if (idx < 0) return;
        int targetIdx = idx + direction;
        if (targetIdx >= 0 && targetIdx < modelVisible.size()) {
            ColumnInfo item = modelVisible.remove(idx);
            modelVisible.add(targetIdx, item);
            listVisible.setSelectedIndex(targetIdx);
        }
    }

    private void restoreDefaults() {
        modelHidden.clear();
        modelVisible.clear();
        for (ColumnInfo info : allAvailableColumns) {
            modelVisible.addElement(info);
        }
    }

    private void applyColumnChanges() {
        TableColumnModel colModel = targetTable.getColumnModel();

        // Xóa hết cột hiện tại trên model
        while (colModel.getColumnCount() > 0) {
            colModel.removeColumn(colModel.getColumn(0));
        }

        // Thêm lại các cột theo đúng thứ tự người dùng vừa chọn
        for (int i = 0; i < modelVisible.size(); i++) {
            ColumnInfo info = modelVisible.getElementAt(i);
            colModel.addColumn(info.getTableColumn());
        }

        targetTable.revalidate();
        targetTable.repaint();
        dispose();
    }

    public static void attachToTableHeader(JTable table, List<String> columnNames) {
        List<ColumnInfo> allCols = new ArrayList<>();
        TableColumnModel colModel = table.getColumnModel();
        for (int i = 0; i < colModel.getColumnCount(); i++) {
            TableColumn col = colModel.getColumn(i);
            String name = col.getHeaderValue() != null ? col.getHeaderValue().toString().trim() : ("Cột " + (i + 1));
            if (!name.isEmpty()) {
                allCols.add(new ColumnInfo(name, col, i));
            }
        }

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miChooseCols = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table_column.png", 14, 14));
        miChooseCols.addActionListener(e -> {
            Window win = SwingUtilities.getWindowAncestor(table);
            new ChonCotHienThiDialog(win, table, allCols).setVisible(true);
        });
        popup.add(miChooseCols);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }
            private void showPopup(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }
}
