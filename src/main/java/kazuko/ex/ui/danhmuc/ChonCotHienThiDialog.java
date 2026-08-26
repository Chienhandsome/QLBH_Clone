package kazuko.ex.ui.danhmuc;

import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ChonCotHienThiDialog extends JDialog {
    private final DefaultListModel<String> modelHidden = new DefaultListModel<>();
    private final DefaultListModel<String> modelVisible = new DefaultListModel<>();
    private final JList<String> listHidden = new JList<>(modelHidden);
    private final JList<String> listVisible = new JList<>(modelVisible);

    private final List<String> defaultVisibleList;
    private final List<String> allColumnsList;
    private final Consumer<List<String>> onAcceptCallback;

    public ChonCotHienThiDialog(Window owner, List<String> currentVisible, List<String> allColumns, List<String> defaultVisible, Consumer<List<String>> onAccept) {
        super(owner, "Chọn cột hiển thị", ModalityType.APPLICATION_MODAL);
        this.defaultVisibleList = new ArrayList<>(defaultVisible);
        this.allColumnsList = new ArrayList<>(allColumns);
        this.onAcceptCallback = onAccept;

        initData(currentVisible);
        initUI();
        setSize(650, 480);
        setLocationRelativeTo(owner);
    }

    private void initData(List<String> currentVisible) {
        modelVisible.clear();
        modelHidden.clear();

        for (String col : currentVisible) {
            if (!col.isEmpty()) {
                modelVisible.addElement(col);
            }
        }

        for (String col : allColumnsList) {
            if (!col.isEmpty() && !currentVisible.contains(col)) {
                modelHidden.addElement(col);
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 244, 250));

        // 1. Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(new Color(245, 248, 253));
        headerPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(200, 215, 230)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("table_column.png", 32, 32));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("1282529873_objects.png", 28, 28));
        }

        JPanel textHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        textHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Mời bạn chọn các cột muốn hiển thị trên lưới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(new Color(20, 30, 45));

        JLabel lblSub = new JLabel("Chú ý: Bạn có thể nháy đúp vào danh sách cột để thêm hoặc xóa cột");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSub.setForeground(new Color(60, 70, 85));

        textHeader.add(lblTitle);
        textHeader.add(lblSub);

        headerPanel.add(lblIcon, BorderLayout.WEST);
        headerPanel.add(textHeader, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Center Body Panel
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(12, 15, 12, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        // Column 1: Hidden Columns List
        JPanel leftPanel = new JPanel(new BorderLayout(0, 6));
        leftPanel.setOpaque(false);
        JLabel lblHidden = new JLabel("Danh sách các cột ẩn");
        lblHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftPanel.add(lblHidden, BorderLayout.NORTH);

        listHidden.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listHidden.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollHidden = new JScrollPane(listHidden);
        scrollHidden.setPreferredSize(new Dimension(210, 280));
        scrollHidden.setBorder(new LineBorder(new Color(180, 195, 215)));
        leftPanel.add(scrollHidden, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.42;
        gbc.weighty = 1.0;
        centerPanel.add(leftPanel, gbc);

        // Column 2: Add / Remove Buttons
        JPanel midButtons = new JPanel(new GridBagLayout());
        midButtons.setOpaque(false);
        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.gridx = 0;
        gbcBtn.fill = GridBagConstraints.HORIZONTAL;
        gbcBtn.insets = new Insets(6, 4, 6, 4);

        JButton btnAdd = new JButton("Thêm >>");
        btnAdd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAdd.setPreferredSize(new Dimension(85, 26));
        btnAdd.addActionListener(e -> onAddSelected());
        midButtons.add(btnAdd, gbcBtn);

        JButton btnRemove = new JButton("<< Xóa");
        btnRemove.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRemove.setPreferredSize(new Dimension(85, 26));
        btnRemove.addActionListener(e -> onRemoveSelected());
        midButtons.add(btnRemove, gbcBtn);

        gbc.gridx = 1;
        gbc.weightx = 0.1;
        centerPanel.add(midButtons, gbc);

        // Column 3: Visible Columns List
        JPanel rightPanel = new JPanel(new BorderLayout(0, 6));
        rightPanel.setOpaque(false);
        JLabel lblVisible = new JLabel("Danh sách các cột hiển thị trên lưới");
        lblVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rightPanel.add(lblVisible, BorderLayout.NORTH);

        listVisible.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listVisible.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollVisible = new JScrollPane(listVisible);
        scrollVisible.setPreferredSize(new Dimension(210, 280));
        scrollVisible.setBorder(new LineBorder(new Color(180, 195, 215)));
        rightPanel.add(scrollVisible, BorderLayout.CENTER);

        gbc.gridx = 2;
        gbc.weightx = 0.42;
        centerPanel.add(rightPanel, gbc);

        // Column 4: Move Up / Down Buttons
        JPanel farRightButtons = new JPanel(new GridBagLayout());
        farRightButtons.setOpaque(false);
        GridBagConstraints gbcMove = new GridBagConstraints();
        gbcMove.gridx = 0;
        gbcMove.fill = GridBagConstraints.HORIZONTAL;
        gbcMove.insets = new Insets(6, 4, 6, 0);

        JButton btnMoveUp = new JButton("Chuyển lên");
        btnMoveUp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMoveUp.setPreferredSize(new Dimension(100, 26));
        btnMoveUp.addActionListener(e -> onMoveUp());
        farRightButtons.add(btnMoveUp, gbcMove);

        JButton btnMoveDown = new JButton("Chuyển xuống");
        btnMoveDown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMoveDown.setPreferredSize(new Dimension(100, 26));
        btnMoveDown.addActionListener(e -> onMoveDown());
        farRightButtons.add(btnMoveDown, gbcMove);

        gbc.gridx = 3;
        gbc.weightx = 0.1;
        centerPanel.add(farRightButtons, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Double click handlers
        listHidden.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onAddSelected();
                }
            }
        });

        listVisible.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onRemoveSelected();
                }
            }
        });

        // 3. Footer Buttons Panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footerPanel.setBackground(new Color(235, 240, 248));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 235)));

        JButton btnRestore = new JButton("Khôi phục");
        btnRestore.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRestore.setPreferredSize(new Dimension(95, 26));
        btnRestore.addActionListener(e -> onRestoreDefaults());
        footerPanel.add(btnRestore);

        JButton btnAccept = new JButton("Chấp nhận");
        btnAccept.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAccept.setPreferredSize(new Dimension(95, 26));
        btnAccept.addActionListener(e -> onAccept());
        footerPanel.add(btnAccept);

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(85, 26));
        btnCancel.addActionListener(e -> dispose());
        footerPanel.add(btnCancel);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void onAddSelected() {
        List<String> selected = listHidden.getSelectedValuesList();
        if (selected == null || selected.isEmpty()) return;

        for (String item : selected) {
            modelHidden.removeElement(item);
            modelVisible.addElement(item);
        }
        listVisible.setSelectedValue(selected.get(selected.size() - 1), true);
    }

    private void onRemoveSelected() {
        List<String> selected = listVisible.getSelectedValuesList();
        if (selected == null || selected.isEmpty()) return;

        for (String item : selected) {
            modelVisible.removeElement(item);
            modelHidden.addElement(item);
        }
        listHidden.setSelectedValue(selected.get(selected.size() - 1), true);
    }

    private void onMoveUp() {
        int idx = listVisible.getSelectedIndex();
        if (idx > 0) {
            String item = modelVisible.remove(idx);
            modelVisible.add(idx - 1, item);
            listVisible.setSelectedIndex(idx - 1);
        }
    }

    private void onMoveDown() {
        int idx = listVisible.getSelectedIndex();
        if (idx >= 0 && idx < modelVisible.size() - 1) {
            String item = modelVisible.remove(idx);
            modelVisible.add(idx + 1, item);
            listVisible.setSelectedIndex(idx + 1);
        }
    }

    private void onRestoreDefaults() {
        initData(defaultVisibleList);
    }

    private void onAccept() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < modelVisible.size(); i++) {
            result.add(modelVisible.getElementAt(i));
        }
        if (onAcceptCallback != null) {
            onAcceptCallback.accept(result);
        }
        dispose();
    }
}
