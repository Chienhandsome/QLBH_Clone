package kazuko.ex.ui.baocao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.lang.reflect.Field;

/**
 * Khung giao diện dùng chung cho mọi màn hình báo cáo.
 *
 * <p>Panel báo cáo cũ vẫn chịu trách nhiệm tạo tham số và sinh JasperPrint.
 * Lớp này chỉ tổ chức lại phần nhìn: tiêu đề, sidebar tham số và vùng preview.
 * Cách bọc này cho phép chuyển đổi toàn bộ báo cáo mà không thay đổi truy vấn
 * nghiệp vụ của từng báo cáo.</p>
 */
public final class ReportScreenPanel extends JPanel {
    static final Color HEADER_BACKGROUND = new Color(82, 143, 174);
    static final Color FILTER_BACKGROUND = new Color(4, 67, 86);
    static final Color FILTER_ON_BACKGROUND = new Color(204, 62, 0);
    static final Color LOAD_BACKGROUND = new Color(5, 92, 20);
    private static final int FILTER_WIDTH = 360;

    private final JPanel filterSidebar;
    private final JButton filterToggle;
    private boolean filterVisible = true;

    public ReportScreenPanel(String title, JPanel reportContent) {
        super(new BorderLayout(6, 6));
        setBackground(FILTER_BACKGROUND);

        JPanel legacyFilter = detachLegacyFilter(reportContent);
        filterSidebar = createFilterSidebar(legacyFilter);
        filterToggle = createFilterToggle();

        add(createHeader(title), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(6, 0));
        body.setOpaque(false);
        body.add(filterSidebar, BorderLayout.WEST);
        body.add(reportContent, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        // Các panel cũ lắng nghe thay đổi tham số và tự chạy báo cáo sau 250-300 ms.
        // Sau khi được bọc, chỉ nút TẢI DỮ LIỆU/F5 được phép chạy lại báo cáo.
        suppressLegacyAutoReload(reportContent);
        updateFilterToggleStyle();
    }

    private JPanel createHeader(String title) {
        JPanel header = new JPanel(new BorderLayout(6, 0));
        header.setBackground(FILTER_BACKGROUND);
        header.setBorder(new EmptyBorder(6, 6, 0, 0));

        filterToggle.setPreferredSize(new Dimension(FILTER_WIDTH, 62));
        header.add(filterToggle, BorderLayout.WEST);

        String displayTitle = title == null ? "BÁO CÁO" : title;
        JLabel titleLabel = new JLabel(displayTitle, SwingConstants.CENTER);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(HEADER_BACKGROUND);
        titleLabel.setForeground(Color.WHITE);
        int titleFontSize = displayTitle.length() > 60 ? 18 : displayTitle.length() > 42 ? 22 : 27;
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize));
        titleLabel.setBorder(new EmptyBorder(0, 12, 0, 12));
        header.add(titleLabel, BorderLayout.CENTER);
        return header;
    }

    private JButton createFilterToggle() {
        JButton button = new JButton("LỌC  THAM SỐ");
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 25));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.getAccessibleContext().setAccessibleName("Bật hoặc tắt vùng lọc tham số");
        button.addActionListener(e -> setFilterVisible(!filterVisible));
        return button;
    }

    private JPanel createFilterSidebar(JPanel legacyFilter) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(FILTER_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(FILTER_WIDTH, 0));
        sidebar.setMinimumSize(new Dimension(FILTER_WIDTH, 0));

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBackground(FILTER_BACKGROUND);
        fields.setBorder(new EmptyBorder(10, 12, 14, 12));

        if (legacyFilter != null) {
            Component[] components = legacyFilter.getComponents();
            legacyFilter.removeAll();
            for (Component component : components) {
                prepareFilterComponent(component);
                fields.add(component);
                fields.add(Box.createVerticalStrut(component instanceof JLabel ? 4 : 10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(fields);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(FILTER_BACKGROUND);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        sidebar.add(scrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private void prepareFilterComponent(Component component) {
        if (component instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            int preferredHeight = Math.max(30, jc.getPreferredSize().height);
            jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
            jc.setPreferredSize(new Dimension(FILTER_WIDTH - 24, preferredHeight));
        }

        if (component instanceof JLabel label) {
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        } else if (component instanceof JButton button) {
            String text = button.getText() == null ? "" : button.getText().toLowerCase();
            if (text.contains("tải dữ liệu")) {
                button.setBackground(LOAD_BACKGROUND);
                button.setForeground(Color.WHITE);
                button.setFont(new Font("Segoe UI", Font.BOLD, 19));
                button.setBorderPainted(false);
                button.setPreferredSize(new Dimension(FILTER_WIDTH - 24, 62));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            }
        } else if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                prepareNestedFilterComponent(child);
            }
        }
    }

    private void prepareNestedFilterComponent(Component component) {
        if (component instanceof JLabel label) {
            label.setForeground(Color.WHITE);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                prepareNestedFilterComponent(child);
            }
        }
    }

    private JPanel detachLegacyFilter(JPanel reportContent) {
        LayoutManager layout = reportContent.getLayout();
        if (layout instanceof BorderLayout borderLayout) {
            Component north = borderLayout.getLayoutComponent(BorderLayout.NORTH);
            if (north instanceof JPanel panel) {
                reportContent.remove(panel);
                reportContent.revalidate();
                return panel;
            }
        }
        return null;
    }

    private void suppressLegacyAutoReload(JPanel reportContent) {
        Class<?> type = reportContent.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("isInitializing");
                if (field.getType() == boolean.class) {
                    field.setAccessible(true);
                    field.setBoolean(reportContent, true);
                }
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return;
            }
        }
    }

    public void setFilterVisible(boolean visible) {
        if (filterVisible == visible) return;
        filterVisible = visible;
        filterSidebar.setVisible(visible);
        updateFilterToggleStyle();
        revalidate();
        repaint();
    }

    public boolean isFilterVisible() {
        return filterVisible;
    }

    private void updateFilterToggleStyle() {
        filterToggle.setBackground(filterVisible ? FILTER_ON_BACKGROUND : HEADER_BACKGROUND);
        filterToggle.setToolTipText(filterVisible ? "Ẩn bộ lọc tham số" : "Hiện bộ lọc tham số");
    }

    /** Đóng tab đang chứa báo cáo, không đóng ứng dụng. */
    public void closeReport() {
        Component child = this;
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JTabbedPane tabs) {
                int index = tabs.indexOfComponent(child);
                if (index >= 0) {
                    tabs.removeTabAt(index);
                    return;
                }
            }
            child = parent;
            parent = parent.getParent();
        }
    }
}
