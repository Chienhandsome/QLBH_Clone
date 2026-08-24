package kazuko.ex.util;

import java.awt.Color;

public final class AppColors {
    // Ngăn chặn việc khởi tạo class (Utility class pattern)
    private AppColors() {}

    // ==========================================
    // 1. MÀU CHỦ ĐẠO (PRIMARY & THEME BLUE)
    // ==========================================
    /** Màu xanh dương đậm chủ đạo của hệ thống (Office Blue) */
    public static final Color PRIMARY_BLUE = new Color(0, 102, 204);

    /** Màu xanh dương đậm dùng cho header/tiêu đề nổi bật */
    public static final Color PRIMARY_DARK_BLUE = new Color(0, 71, 143);

    /** Màu xanh pastel nhạt làm nền Form Đăng nhập & Popup (#CADDF3) */
    public static final Color PRIMARY_LIGHT_BLUE = new Color(202, 221, 243);

    /** Màu xanh rất nhạt làm nền Toolbar / Thanh Ribbon (#EBF0F8) */
    public static final Color RIBBON_BG = new Color(235, 240, 248);

    /** Màu nền thanh công cụ phụ / Toolbar bảng dữ liệu (#F0F3F8) */
    public static final Color TOOLBAR_BG = new Color(240, 243, 248);

    // ==========================================
    // 2. MÀU VIỀN & ĐƯỜNG KẺ (BORDERS & LINES)
    // ==========================================
    /** Màu viền xanh nhạt của Toolbar và Panel */
    public static final Color BORDER_BLUE = new Color(180, 200, 225);

    /** Màu viền ngăn cách phân vùng dưới chân trang */
    public static final Color BORDER_SEPARATOR = new Color(175, 200, 230);

    /** Màu viền xám nhẹ tiêu chuẩn */
    public static final Color BORDER_GRAY = new Color(220, 220, 220);

    // ==========================================
    // 3. MÀU VĂN BẢN (TEXT COLORS)
    // ==========================================
    /** Màu chữ đen đậm tiêu chuẩn */
    public static final Color TEXT_DARK = new Color(30, 30, 30);

    /** Màu chữ liên kết xanh gạch chân (Hyperlink) */
    public static final Color TEXT_LINK = new Color(0, 51, 204);

    /** Màu chữ xám nhạt (ghi chú, placeholder) */
    public static final Color TEXT_MUTED = new Color(100, 100, 100);

    /** Màu chữ trắng */
    public static final Color TEXT_WHITE = Color.WHITE;

    // ==========================================
    // 4. MÀU HÀNH ĐỘNG & TRẠNG THÁI (ACTIONS)
    // ==========================================
    /** Màu xanh lá cây cho nút 'Thêm mới (Insert)', trạng thái thành công */
    public static final Color SUCCESS_GREEN = new Color(0, 128, 60);

    /** Màu đỏ cho nút 'Xóa (Del)', 'Hủy bỏ', 'Thoát' */
    public static final Color DANGER_RED = new Color(204, 40, 40);

    /** Màu cam nổi bật (Cảnh báo / Logo Tân An Phát) */
    public static final Color WARNING_ORANGE = new Color(230, 90, 0);

    // ==========================================
    // 5. MÀU NỀN CHUNG (BACKGROUNDS)
    // ==========================================
    /** Màu nền thanh trạng thái dưới cùng (StatusPanel) */
    public static final Color STATUS_BAR_BG = new Color(245, 247, 250);

    /** Màu nền trắng */
    public static final Color WHITE = Color.WHITE;
}
