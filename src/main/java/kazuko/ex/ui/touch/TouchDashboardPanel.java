package kazuko.ex.ui.touch;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.system.SUser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Main touch navigation screen modelled after TAP Shop TS v5.1. */
final class TouchDashboardPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color BUTTON_TOP = new Color(0, 145, 145);
    private static final Color BUTTON_BOTTOM = new Color(35, 184, 184);
    private static final Color SELECTED_TOP = new Color(151, 51, 4);
    private static final Color SELECTED_BOTTOM = new Color(244, 92, 22);
    private static final Color BACK_TOP = new Color(20, 48, 85);
    private static final Color BACK_BOTTOM = new Color(42, 82, 135);
    private static final Color STATUS = new Color(74, 133, 196);
    private static final Map<String, String[]> SUB_MENUS = createSubMenus();
    private static final Map<String, String> PARENT_MENUS = createParentMenus();

    private final Consumer<String> openModule;
    private final JPanel submenuHost = new JPanel(new BorderLayout());
    private final Map<String, GradientButton> mainButtons = new LinkedHashMap<>();
    private GradientButton invoiceButton;
    private String activeMainMenu = "HOẠT ĐỘNG";

    TouchDashboardPanel(SUser user, Runnable openSales, Runnable logout,
                        Consumer<String> openModule) {
        this.openModule = openModule;
        setLayout(new BorderLayout(0, 8));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 3, 5));

        JLabel title = new JLabel(
                "PHẦN MỀM QUẢN LÝ BÁN LẺ, CỬA HÀNG, SIÊU THỊ V5.1 TÂN AN PHÁT - HOTLINE: 0967041111",
                SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(HEADER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setBorder(new LineBorder(new Color(45, 103, 130), 2, true));
        title.setPreferredSize(new Dimension(0, 65));
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(8, 0));
        body.setBackground(BACKGROUND);
        body.add(createMainMenuColumn(), BorderLayout.WEST);

        submenuHost.setOpaque(false);
        submenuHost.setPreferredSize(new Dimension(440, 0));
        JPanel centerArea = new JPanel(new BorderLayout());
        centerArea.setOpaque(false);
        centerArea.add(submenuHost, BorderLayout.WEST);
        body.add(centerArea, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(9, 1, 0, 7));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(400, 0));
        invoiceButton = createMenuButton("HÓA ĐƠN", e -> openSales.run());
        right.add(invoiceButton);
        for (String item : new String[]{
                "QUẢN LÝ BÁN HÀNG", "THỐNG KÊ DOANH THU", "THỐNG KÊ MẶT HÀNG BÁN",
                "CHI TIẾT HOẠT ĐỘNG", "TỔNG HỢP KQKD", "TỒN KHO", "GHI CHÚ"
        }) {
            right.add(createMenuButton(item, e -> openModule.accept(item)));
        }
        right.add(createMenuButton("ĐĂNG XUẤT", e -> logout.run()));
        body.add(right, BorderLayout.EAST);
        add(body, BorderLayout.CENTER);
        add(createStatusBar(user), BorderLayout.SOUTH);

        selectMainMenu("HOẠT ĐỘNG");
    }

    private JPanel createMainMenuColumn() {
        String[] labels = {
                "HỆ THỐNG", "HOẠT ĐỘNG", "KHO HÀNG", "QUỸ", "CÔNG NỢ",
                "NHÂN SỰ", "QUẢN TRỊ", "BÁO CÁO", "TRỢ GIÚP"
        };
        JPanel column = new JPanel(new GridLayout(labels.length, 1, 0, 7));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(400, 0));
        for (String label : labels) {
            GradientButton button = createMenuButton(label, e -> selectMainMenu(label));
            mainButtons.put(label, button);
            column.add(button);
        }
        return column;
    }

    private void selectMainMenu(String menu) {
        this.activeMainMenu = menu;
        mainButtons.forEach((label, button) -> button.setSelectedStyle(label.equals(menu)));
        invoiceButton.setSelectedStyle(true);
        renderSubmenu(menu, null);
    }

    private void renderSubmenu(String menuKey, String parentKey) {
        String[] items = SUB_MENUS.get(menuKey);
        if (items == null) {
            openModule.accept(menuKey);
            return;
        }

        submenuHost.removeAll();
        int totalItems = items.length + (parentKey != null ? 1 : 0);

        JPanel list = new TouchMenuList(new GridLayout(totalItems, 1, 0, 7));
        list.setBackground(new Color(83, 132, 154));
        list.setBorder(new EmptyBorder(5, 5, 5, 5));
        int rowHeight = totalItems > 8 ? 72 : 100;
        list.setPreferredSize(new Dimension(440, totalItems * rowHeight));

        if (parentKey != null) {
            String backText = "CƠ SỞ DỮ LIỆU...".equals(menuKey) ? "..." : "◀  " + parentKey;
            GradientButton backBtn = new GradientButton(backText);
            backBtn.setFont(new Font("Segoe UI", Font.BOLD, parentKey.length() > 20 ? 17 : 20));
            backBtn.setForeground(Color.WHITE);
            backBtn.setFocusPainted(false);
            backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            backBtn.setBorder(new CompoundBorder(
                    new LineBorder(new Color(220, 240, 255), 2, true),
                    new EmptyBorder(5, 12, 5, 12)));
            backBtn.setBackStyle(true);
            backBtn.addActionListener(e -> {
                String grandParent = PARENT_MENUS.get(parentKey);
                renderSubmenu(parentKey, grandParent);
            });
            list.add(backBtn);
        }

        for (String item : items) {
            if (SUB_MENUS.containsKey(item)) {
                GradientButton groupBtn = createMenuButton(item, null);
                groupBtn.addActionListener(e -> {
                    selectSubmenuButton(list, groupBtn);
                    renderSubmenu(item, menuKey);
                });
                groupBtn.setFont(new Font("Segoe UI", Font.BOLD, item.length() > 22 ? 17 : 21));
                list.add(groupBtn);
            } else {
                GradientButton leafBtn = createMenuButton(item, null);
                leafBtn.addActionListener(e -> {
                    selectSubmenuButton(list, leafBtn);
                    openModule.accept(item);
                });
                leafBtn.setFont(new Font("Segoe UI", Font.BOLD, item.length() > 24 ? 16 : (item.length() > 18 ? 18 : 22)));
                list.add(leafBtn);
            }
        }

        JScrollPane scroll = new JScrollPane(list,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(45);
        submenuHost.add(scroll, BorderLayout.CENTER);
        submenuHost.revalidate();
        submenuHost.repaint();
    }

    private void selectSubmenuButton(JPanel list, GradientButton selected) {
        for (Component component : list.getComponents()) {
            if (component instanceof GradientButton button) button.setSelectedStyle(button == selected);
        }
    }

    private GradientButton createMenuButton(String text, ActionListener action) {
        GradientButton button = new GradientButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, text.length() > 24 ? 17 : (text.length() > 18 ? 19 : 23)));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 218, 231), 2, true),
                new EmptyBorder(5, 12, 5, 12)));
        if (action != null) button.addActionListener(action);
        return button;
    }

    private JPanel createStatusBar(SUser user) {
        JPanel status = new JPanel(new BorderLayout(8, 0));
        status.setOpaque(false);
        status.add(createStatusLabel("Phiên bản dùng thử, hết hạn trong 20 ngày."), BorderLayout.CENTER);
        String username = user != null && user.getUsername() != null ? user.getUsername() : "Administrator";
        JLabel userLabel = createStatusLabel(username);
        userLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userLabel.setPreferredSize(new Dimension(190, 36));
        JLabel database = createStatusLabel(databaseName());
        database.setHorizontalAlignment(SwingConstants.CENTER);
        database.setPreferredSize(new Dimension(190, 36));
        JPanel identity = new JPanel(new BorderLayout(8, 0));
        identity.setOpaque(false);
        identity.add(userLabel, BorderLayout.CENTER);
        identity.add(database, BorderLayout.EAST);
        status.add(identity, BorderLayout.EAST);
        return status;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(STATUS);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setBorder(new CompoundBorder(
                new LineBorder(new Color(140, 170, 205), 2, true),
                new EmptyBorder(2, 8, 2, 8)));
        return label;
    }

    private String databaseName() {
        try {
            String configured = DatabaseConnection.getConfig().getDatabasePath();
            if (configured == null || configured.isBlank()) return "DEMO";
            String fileName = Path.of(configured).getFileName().toString();
            int extension = fileName.lastIndexOf('.');
            return extension > 0 ? fileName.substring(0, extension) : fileName;
        } catch (Exception ignored) {
            return "DEMO";
        }
    }

    private static Map<String, String[]> createSubMenus() {
        Map<String, String[]> menus = new LinkedHashMap<>();

        // 1. HỆ THỐNG
        menus.put("HỆ THỐNG", new String[]{
                "CƠ SỞ DỮ LIỆU...", "ĐỔI MẬT KHẨU ĐĂNG NHẬP",
                "ĐĂNG XUẤT KHỎI HỆ THỐNG", "THOÁT KHỎI HỆ THỐNG"
        });
        menus.put("CƠ SỞ DỮ LIỆU...", new String[]{
                "TẠO MỚI CƠ SỞ DỮ LIỆU",
                "SAO LƯU CƠ SỞ DỮ LIỆU",
                "PHỤC HỒI CƠ SỞ DỮ LIỆU"
        });

        // 2. HOẠT ĐỘNG
        menus.put("HOẠT ĐỘNG", new String[]{
                "DANH MỤC CỬA HÀNG", "DANH MỤC MẶT HÀNG", "DANH MỤC KHÁCH HÀNG",
                "KHÁCH HÀNG THÂN THIẾT", "KHÁCH ĐẶT HÀNG", "HÓA ĐƠN BÁN HÀNG",
                "QUẢN LÝ BÁN HÀNG", "LƯU VẾT HOẠT ĐỘNG", "THỐNG KÊ DOANH THU",
                "THỐNG KÊ MẶT HÀNG BÁN", "TỔNG HỢP KẾT QUẢ KINH DOANH",
                "CHI TIẾT HOẠT ĐỘNG NGÀY"
        });

        // 3. KHO HÀNG
        menus.put("KHO HÀNG", new String[]{
                "DANH MỤC KHO HÀNG", "DANH MỤC NHÀ CUNG CẤP", "NHẬP HÀNG VÀO KHO", "TỒN KHO"
        });

        // 4. QUỸ
        menus.put("QUỸ", new String[]{
                "DANH MỤC LÝ DO THU CHI", "TẠO PHIẾU THU", "TẠO PHIẾU CHI",
                "DANH MỤC PHIẾU THU", "DANH MỤC PHIẾU CHI", "TỒN QUỸ"
        });

        // 5. CÔNG NỢ
        menus.put("CÔNG NỢ", new String[]{"CÔNG NỢ KHÁCH HÀNG", "CÔNG NỢ NHÀ CUNG CẤP"});

        // 6. NHÂN SỰ
        menus.put("NHÂN SỰ", new String[]{
                "DANH MỤC NHÂN VIÊN", "DANH MỤC CA LÀM VIỆC"
        });

        // 7. QUẢN TRỊ
        menus.put("QUẢN TRỊ", new String[]{
                "XÓA DỮ LIỆU", "CÔNG CỤ NHÀ PHÁT TRIỂN",
                "NGƯỜI DÙNG VÀ PHÂN QUYỀN", "CẤU HÌNH TOÀN HỆ THỐNG"
        });

        // 8. BÁO CÁO (Cấp 1)
        menus.put("BÁO CÁO", new String[]{
                "BÁO CÁO QUỸ", "BÁO CÁO DANH MỤC", "BÁO CÁO BÁN HÀNG",
                "BÁO CÁO ĐẶT HÀNG", "BÁO CÁO KHO HÀNG", "BÁO CÁO CÔNG NỢ",
                "BÁO CÁO QUẢN TRỊ", "BÁO CÁO BIỂU ĐỒ"
        });

        // 8.1 BÁO CÁO QUỸ (Cấp 2)
        menus.put("BÁO CÁO QUỸ", new String[]{
                "DANH SÁCH PHIẾU THU THEO NGÀY",
                "DANH SÁCH PHIẾU THU THEO LÝ DO THU CHI",
                "DANH SÁCH PHIẾU CHI THEO NGÀY",
                "DANH SÁCH PHIẾU CHI THEO LÝ DO THU CHI",
                "TỔNG HỢP THU CHI THEO NGÀY",
                "TỔNG HỢP THU CHI THEO LÝ DO",
                "BÁO CÁO TỒN QUỸ"
        });

        // 8.2 BÁO CÁO DANH MỤC (Cấp 2)
        menus.put("BÁO CÁO DANH MỤC", new String[]{
                "DANH SÁCH KHÁCH HÀNG THEO NHÓM",
                "DANH SÁCH KHÁCH HÀNG THEO NHÂN VIÊN",
                "DANH SÁCH NHÀ CUNG CẤP THEO NHÓM",
                "DANH SÁCH ĐỢT KHUYẾN MẠI",
                "DANH SÁCH MẶT HÀNG THEO NHÓM",
                "DANH SÁCH MẶT HÀNG THEO HÃNG SẢN XUẤT",
                "KHÁCH HÀNG ĐẾN NGÀY SINH NHẬT"
        });

        // 8.3 BÁO CÁO BÁN HÀNG (Cấp 2)
        menus.put("BÁO CÁO BÁN HÀNG", new String[]{
                "KHÁCH HÀNG THÂN THIẾT",
                "BÁO CÁO BÁN HÀNG THEO GIỜ",
                "BÁO CÁO BÁN HÀNG THEO KÍCH THƯỚC",
                "BÁO CÁO TỔNG HỢP GIÁ TRỊ BÁN THEO THÁNG",
                "BÁO CÁO TỔNG HỢP SỐ LƯỢNG BÁN THEO THÁNG",
                "BÁO CÁO BÁN HÀNG THEO NGÀY",
                "BÁO CÁO CHI TIẾT BÁN HÀNG THEO NGÀY",
                "BÁO CÁO BÁN HÀNG THEO KHÁCH HÀNG",
                "BÁO CÁO BÁN HÀNG THEO NHÂN VIÊN KINH DOANH",
                "TỔNG HỢP MẶT HÀNG BÁN THEO KHÁCH HÀNG",
                "TỔNG HỢP MẶT HÀNG BÁN THEO NGÀY",
                "TỔNG HỢP MẶT HÀNG BÁN THEO NHÂN VIÊN KINH DOANH",
                "TỔNG HỢP BÁN HÀNG THEO NGÀY",
                "TỔNG HỢP BÁN THEO KHÁCH HÀNG",
                "TỔNG HỢP BÁN THEO NHÂN VIÊN BÁN",
                "TỔNG HỢP BÁN THEO THU NGÂN",
                "TỔNG HỢP MẶT HÀNG BÁN THEO THU NGÂN",
                "BÁO CÁO CHI TIẾT BÁN HÀNG THEO THU NGÂN",
                "BÁO CÁO BÁN HÀNG THEO THU NGÂN",
                "TỔNG HỢP TIỀN VẬN CHUYỂN THEO NHÂN VIÊN",
                "CHI TIẾT BÁN HÀNG THEO ĐỐI TÁC",
                "TỔNG HỢP HOA HỒNG THEO NVKD",
                "CHI TIẾT BÁN HÀNG THEO HÓA ĐƠN",
                "BÁO CÁO CHI TIẾT HÀNG KHUYẾN MẠI"
        });

        // 8.4 BÁO CÁO ĐẶT HÀNG (Cấp 2)
        menus.put("BÁO CÁO ĐẶT HÀNG", new String[]{
                "DANH SÁCH ĐẶT HÀNG THEO NGÀY",
                "DANH SÁCH ĐẶT HÀNG THEO KHÁCH HÀNG",
                "TỔNG HỢP ĐẶT HÀNG THEO NGÀY",
                "TỔNG HỢP ĐẶT HÀNG THEO KHÁCH HÀNG",
                "TỔNG HỢP MẶT HÀNG ĐẶT THEO KHÁCH HÀNG",
                "TỔNG HỢP MẶT HÀNG ĐẶT THEO NGÀY"
        });

        // 8.5 BÁO CÁO KHO HÀNG (Cấp 2)
        menus.put("BÁO CÁO KHO HÀNG", new String[]{
                "BÁO CÁO TỒN KHO THEO KÍCH THƯỚC",
                "BÁO CÁO KIỂM KÊ",
                "BÁO CÁO XUẤT KHÁC",
                "BÁO CÁO NHẬP HÀNG",
                "BÁO CÁO CHUYỂN KHO",
                "BÁO CÁO THEO HSD",
                "BÁO CÁO HÀNG TỒN KHO",
                "BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN",
                "THẺ KHO"
        });

        // 8.5.1 BÁO CÁO KIỂM KÊ (Cấp 3)
        menus.put("BÁO CÁO KIỂM KÊ", new String[]{
                "DANH SÁCH PHIẾU KIỂM KÊ THEO NGÀY",
                "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NGÀY",
                "TỔNG HỢP MẶT HÀNG PHIẾU KIỂM KÊ THEO NHÂN VIÊN",
                "DANH SÁCH PHIẾU KIỂM KÊ THEO NHÂN VIÊN"
        });

        // 8.5.2 BÁO CÁO XUẤT KHÁC (Cấp 3)
        menus.put("BÁO CÁO XUẤT KHÁC", new String[]{
                "TỔNG HỢP MẶT HÀNG XUẤT THEO NGÀY",
                "TỔNG HỢP MẶT HÀNG XUẤT THEO NHÂN VIÊN XUẤT",
                "DANH SÁCH PHIẾU XUẤT KHO THEO NHÂN VIÊN XUẤT",
                "TỔNG HỢP XUẤT KHÁC THEO NGÀY",
                "TỔNG HỢP XUẤT KHÁC THEO NHÂN VIÊN XUẤT",
                "DANH SÁCH PHIẾU XUẤT KHO THEO NGÀY"
        });

        // 8.5.3 BÁO CÁO NHẬP HÀNG (Cấp 3)
        menus.put("BÁO CÁO NHẬP HÀNG", new String[]{
                "DANH SÁCH PHIẾU NHẬP KHO THEO NGÀY",
                "DANH SÁCH PHIẾU NHẬP KHO THEO NHÀ CUNG CẤP",
                "DANH SÁCH PHIẾU NHẬP KHO THEO NHÂN VIÊN NHẬP",
                "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÀ CUNG CẤP",
                "TỔNG HỢP MẶT HÀNG PHIẾU NHẬP THEO NHÂN VIÊN NHẬP",
                "TỔNG HỢP NHẬP THEO NGÀY",
                "TỔNG HỢP NHẬP THEO NHÀ CUNG CẤP",
                "TỔNG HỢP NHẬP THEO NHÂN VIÊN NHẬP",
                "TỔNG HỢP MẶT HÀNG NHẬP KHO THEO NGÀY"
        });

        // 8.5.4 BÁO CÁO CHUYỂN KHO (Cấp 3)
        menus.put("BÁO CÁO CHUYỂN KHO", new String[]{
                "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN NHẬN",
                "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NHÂN VIÊN CHUYỂN",
                "TỔNG HỢP MẶT HÀNG CHUYỂN KHO THEO NGÀY",
                "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN XUẤT",
                "DANH SÁCH PHIẾU CHUYỂN KHO THEO NHÂN VIÊN NHẬP",
                "DANH SÁCH PHIẾU CHUYỂN KHO THEO NGÀY"
        });

        // 8.5.5 BÁO CÁO THEO HSD (Cấp 3)
        menus.put("BÁO CÁO THEO HSD", new String[]{
                "BÁO CÁO THEO HẠN DÙNG",
                "BÁO CÁO HÀNG ĐÃ HẾT HẠN",
                "BÁO CÁO HÀNG TỒN KHO CÓ HẠN DÙNG"
        });

        // 8.6 BÁO CÁO CÔNG NỢ (Cấp 2)
        menus.put("BÁO CÁO CÔNG NỢ", new String[]{
                "ĐỐI CHIẾU CÔNG NỢ NHÀ CUNG CẤP",
                "BÁO CÁO CÔNG NỢ NHÀ CUNG CẤP",
                "ĐỐI CHIẾU CÔNG NỢ KHÁCH HÀNG",
                "TỔNG HỢP CÔNG NỢ KHÁCH HÀNG",
                "TỔNG HỢP CÔNG NỢ NHÀ CUNG CẤP",
                "BÁO CÁO CÔNG NỢ KHÁCH HÀNG"
        });

        // 8.7 BÁO CÁO QUẢN TRỊ (Cấp 2)
        menus.put("BÁO CÁO QUẢN TRỊ", new String[]{
                "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ VỐN)",
                "TỔNG HỢP LÃI GỘP THEO MẶT HÀNG (GIÁ NHẬP)",
                "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ NHẬP)",
                "CHI TIẾT LÃI THEO HÓA ĐƠN (GIÁ VỐN)",
                "BÁO CÁO KẾT QUẢ KINH DOANH",
                "CHI TIẾT HOẠT ĐỘNG TRONG NGÀY",
                "BÁO CÁO 20 MẶT HÀNG BÁN CHẠY NHẤT",
                "BẢNG TỔNG HỢP THỊ PHẦN",
                "PHÂN TÍCH TÌNH HÌNH BÁN HÀNG"
        });

        // 8.8 BÁO CÁO BIỂU ĐỒ (Cấp 2)
        menus.put("BÁO CÁO BIỂU ĐỒ", new String[]{
                "BIỂU ĐỒ DOANH SỐ THEO NHÓM",
                "BIỂU ĐỒ DOANH THU NGÀY TRONG THÁNG",
                "BIỂU ĐỒ DOANH THU THÁNG TRONG NĂM",
                "BIỂU ĐỒ THEO NHÂN VIÊN KINH DOANH"
        });

        // 9. TRỢ GIÚP
        menus.put("TRỢ GIÚP", new String[]{
                "HƯỚNG DẪN SỬ DỤNG", "HỖ TRỢ TỪ XA TEAMVIEWER",
                "ĐĂNG KÝ BẢN QUYỀN...", "THÔNG TIN PHẦN MỀM"
        });

        return menus;
    }

    private static Map<String, String> createParentMenus() {
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put("CƠ SỞ DỮ LIỆU...", "HỆ THỐNG");
        parents.put("BÁO CÁO QUỸ", "BÁO CÁO");
        parents.put("BÁO CÁO DANH MỤC", "BÁO CÁO");
        parents.put("BÁO CÁO BÁN HÀNG", "BÁO CÁO");
        parents.put("BÁO CÁO ĐẶT HÀNG", "BÁO CÁO");
        parents.put("BÁO CÁO KHO HÀNG", "BÁO CÁO");
        parents.put("BÁO CÁO CÔNG NỢ", "BÁO CÁO");
        parents.put("BÁO CÁO QUẢN TRỊ", "BÁO CÁO");
        parents.put("BÁO CÁO BIỂU ĐỒ", "BÁO CÁO");

        parents.put("BÁO CÁO KIỂM KÊ", "BÁO CÁO KHO HÀNG");
        parents.put("BÁO CÁO XUẤT KHÁC", "BÁO CÁO KHO HÀNG");
        parents.put("BÁO CÁO NHẬP HÀNG", "BÁO CÁO KHO HÀNG");
        parents.put("BÁO CÁO CHUYỂN KHO", "BÁO CÁO KHO HÀNG");
        parents.put("BÁO CÁO THEO HSD", "BÁO CÁO KHO HÀNG");
        return parents;
    }

    private static final class GradientButton extends JButton {
        private boolean selectedStyle;
        private boolean backStyle;

        private GradientButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        private void setSelectedStyle(boolean selectedStyle) {
            this.selectedStyle = selectedStyle;
            repaint();
        }

        private void setBackStyle(boolean backStyle) {
            this.backStyle = backStyle;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color normalTop;
                Color normalBottom;
                if (backStyle) {
                    normalTop = BACK_TOP;
                    normalBottom = BACK_BOTTOM;
                } else if (selectedStyle) {
                    normalTop = SELECTED_TOP;
                    normalBottom = SELECTED_BOTTOM;
                } else {
                    normalTop = BUTTON_TOP;
                    normalBottom = BUTTON_BOTTOM;
                }
                Color top = getModel().isPressed() ? normalBottom.darker() : normalTop;
                Color bottom = getModel().isRollover() ? normalBottom.brighter() : normalBottom;
                g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
            } finally {
                g2.dispose();
            }
            super.paintComponent(graphics);
        }
    }

    /** Keeps short menus top-aligned while still allowing touch scrolling on small displays. */
    private static final class TouchMenuList extends JPanel implements Scrollable {
        private TouchMenuList(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 45;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(90, visibleRect.height - 90);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
