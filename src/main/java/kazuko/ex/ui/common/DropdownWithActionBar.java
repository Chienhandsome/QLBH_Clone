package kazuko.ex.ui.common;

import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DropdownWithActionBar<T> extends JPanel {

    private final JTextField txtDisplay;
    private final JButton btnArrow;
    private final JPopupMenu popupMenu;
    private final JList<T> listItems;
    private final DefaultListModel<T> listModel;

    private List<T> items = new ArrayList<>();
    private T selectedItem = null;
    private Function<T, String> displayConverter = (item) -> item != null ? item.toString() : "";
    private final List<ActionListener> actionListeners = new ArrayList<>();

    private Runnable onThemAction;
    private Runnable onTaiAction;
    private Runnable onDanhMucAction;

    public DropdownWithActionBar() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(180, 200, 225), 1));

        txtDisplay = new JTextField();
        txtDisplay.setEditable(false);
        txtDisplay.setBorder(new EmptyBorder(0, 5, 0, 2));
        txtDisplay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDisplay.setOpaque(false);
        txtDisplay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnArrow = new JButton("▾");
        btnArrow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnArrow.setPreferredSize(new Dimension(20, 20));
        btnArrow.setFocusPainted(false);
        btnArrow.setBorder(new MatteBorder(0, 1, 0, 0, new Color(200, 215, 235)));
        btnArrow.setBackground(new Color(245, 248, 253));
        btnArrow.setMargin(new Insets(0, 0, 0, 0));
        btnArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        add(txtDisplay, BorderLayout.CENTER);
        add(btnArrow, BorderLayout.EAST);

        // Setup Popup Menu
        popupMenu = new JPopupMenu();
        popupMenu.setBorder(new LineBorder(new Color(160, 185, 215), 1));
        popupMenu.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        listItems = new JList<>(listModel);
        listItems.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listItems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listItems.setFixedCellHeight(24);
        listItems.setSelectionBackground(new Color(200, 225, 255));
        listItems.setSelectionForeground(Color.BLACK);

        listItems.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
                if (value != null) {
                    @SuppressWarnings("unchecked")
                    T item = (T) value;
                    lbl.setText(displayConverter.apply(item));
                }
                return lbl;
            }
        });

        JScrollPane scrollPane = new JScrollPane(listItems);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(220, 140));

        // Bottom Action Bar matching Screenshots 3 & 4
        JPanel pnlBottomBar = new JPanel(new GridLayout(1, 3, 2, 0));
        pnlBottomBar.setBackground(new Color(245, 248, 253));
        pnlBottomBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(200, 215, 235)),
                new EmptyBorder(3, 4, 3, 4)
        ));

        JButton btnThem = createSmallButton("+ Thêm", IconHelper.getIcon("btnNew.Image.png", 14, 14));
        btnThem.setForeground(new Color(0, 120, 0));
        btnThem.addActionListener(e -> {
            popupMenu.setVisible(false);
            if (onThemAction != null) onThemAction.run();
        });

        JButton btnTai = createSmallButton("Tải", IconHelper.getIcon("Refresh.png", 14, 14));
        btnTai.setForeground(new Color(0, 70, 180));
        btnTai.addActionListener(e -> {
            if (onTaiAction != null) onTaiAction.run();
        });

        JButton btnDanhMuc = createSmallButton("Danh mục", null);
        btnDanhMuc.addActionListener(e -> {
            popupMenu.setVisible(false);
            if (onDanhMucAction != null) onDanhMucAction.run();
        });

        pnlBottomBar.add(btnThem);
        pnlBottomBar.add(btnTai);
        pnlBottomBar.add(btnDanhMuc);

        JPanel pnlPopupContent = new JPanel(new BorderLayout());
        pnlPopupContent.add(scrollPane, BorderLayout.CENTER);
        pnlPopupContent.add(pnlBottomBar, BorderLayout.SOUTH);

        popupMenu.add(pnlPopupContent);

        // Click listeners to show popup
        MouseAdapter clickToShow = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                togglePopup();
            }
        };
        txtDisplay.addMouseListener(clickToShow);
        btnArrow.addMouseListener(clickToShow);
        addMouseListener(clickToShow);

        // Selection listener on list
        listItems.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listItems.getSelectedValue() != null) {
                    setSelectedItem(listItems.getSelectedValue());
                    popupMenu.setVisible(false);
                    fireActionEvent();
                }
            }
        });

        listItems.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && listItems.getSelectedValue() != null) {
                    setSelectedItem(listItems.getSelectedValue());
                    popupMenu.setVisible(false);
                    fireActionEvent();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupMenu.setVisible(false);
                }
            }
        });
    }

    private JButton createSmallButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(200, 215, 235), 1), new EmptyBorder(2, 4, 2, 4)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void togglePopup() {
        if (popupMenu.isVisible()) {
            popupMenu.setVisible(false);
        } else {
            showPopup();
        }
    }

    public void showPopup() {
        int width = Math.max(getWidth(), 230);
        popupMenu.setPreferredSize(new Dimension(width, 175));
        popupMenu.show(this, 0, getHeight());
        if (selectedItem != null) {
            listItems.setSelectedValue(selectedItem, true);
        }
        listItems.requestFocusInWindow();
    }

    public void setItems(List<T> newItems) {
        this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
        listModel.clear();
        for (T item : items) {
            listModel.addElement(item);
        }
        if (selectedItem != null && !items.contains(selectedItem)) {
            selectedItem = null;
            txtDisplay.setText("");
        } else if (selectedItem != null) {
            txtDisplay.setText(displayConverter.apply(selectedItem));
        }
    }

    public List<T> getItems() {
        return items;
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T item) {
        this.selectedItem = item;
        txtDisplay.setText(displayConverter.apply(item));
        if (item != null) {
            listItems.setSelectedValue(item, true);
        } else {
            listItems.clearSelection();
        }
    }

    public void setDisplayConverter(Function<T, String> displayConverter) {
        this.displayConverter = displayConverter != null ? displayConverter : (item -> item != null ? item.toString() : "");
        if (selectedItem != null) {
            txtDisplay.setText(this.displayConverter.apply(selectedItem));
        }
    }

    public void setListCellRenderer(ListCellRenderer<? super T> renderer) {
        listItems.setCellRenderer(renderer);
    }

    public void setOnThem(Runnable action) {
        this.onThemAction = action;
    }

    public void setOnTai(Runnable action) {
        this.onTaiAction = action;
    }

    public void setOnDanhMuc(Runnable action) {
        this.onDanhMucAction = action;
    }

    public void addActionListener(ActionListener l) {
        actionListeners.add(l);
    }

    private void fireActionEvent() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "selected");
        for (ActionListener l : actionListeners) {
            l.actionPerformed(evt);
        }
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (txtDisplay != null) {
            txtDisplay.setBackground(bg);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtDisplay.setEnabled(enabled);
        btnArrow.setEnabled(enabled);
        if (!enabled) {
            setBackground(new Color(240, 240, 240));
        }
    }
}