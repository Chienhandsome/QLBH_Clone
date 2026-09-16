package kazuko.ex.ui.touch;

import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.util.CurrencyUtil;

import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Cấu hình hiển thị lưới thẻ cho danh mục phiếu chi Touch. */
final class TouchPhieuChiColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
    static final class ColumnItem implements Serializable, Cloneable {
        private String key, title, prefix, colorHex;
        private boolean bold, italic, rightAligned;
        private int fontSize;

        ColumnItem() {}

        ColumnItem(String key, String title, String prefix, String color, boolean bold, boolean italic, boolean right, int size) {
            this.key = key;
            this.title = title;
            this.prefix = prefix;
            this.colorHex = color;
            this.bold = bold;
            this.italic = italic;
            this.rightAligned = right;
            this.fontSize = size;
        }

        String getKey() { return key; }
        String getTitle() { return title; }
        String getPrefix() { return prefix == null ? "" : prefix; }
        void setPrefix(String v) { prefix = v; }
        Color getColor() {
            try { return Color.decode(colorHex); } catch (Exception e) { return Color.WHITE; }
        }
        void setColor(Color c) { if (c != null) colorHex = hex(c); }
        boolean isBold() { return bold; }
        void setBold(boolean v) { bold = v; }
        boolean isItalic() { return italic; }
        void setItalic(boolean v) { italic = v; }
        boolean isRightAligned() { return rightAligned; }
        void setRightAligned(boolean v) { rightAligned = v; }
        int getFontSize() { return fontSize <= 0 ? 16 : fontSize; }
        void setFontSize(int v) { fontSize = v; }
        Font getFont() {
            return new Font("Segoe UI", (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), getFontSize());
        }

        @Override
        public ColumnItem clone() {
            return new ColumnItem(key, title, prefix, colorHex, bold, italic, rightAligned, fontSize);
        }
    }

    private List<ColumnItem> usedColumns = new ArrayList<>();
    private List<ColumnItem> unusedColumns = new ArrayList<>();
    private String cardBgColorHex = "#004F4C";
    private int gridColumns = 3, gridRows = 6;
    private boolean showTitle = true;

    static TouchPhieuChiColumnConfig createDefault() {
        TouchPhieuChiColumnConfig c = new TouchPhieuChiColumnConfig();
        c.usedColumns.add(col("NUMBER", "SỐ PHIẾU", "Số phiếu: "));
        c.usedColumns.add(col("DATE", "NGÀY", "Ngày: "));
        c.usedColumns.add(col("SUBJECT_TYPE", "LOẠI ĐỐI TƯỢNG", "Loại ĐT: "));
        c.usedColumns.add(col("SUBJECT_NAME", "TÊN ĐỐI TƯỢNG", "Đối tượng: "));
        c.usedColumns.add(col("REASON", "LÝ DO CHI", "Lý do: "));
        c.usedColumns.add(colRight("AMOUNT", "SỐ TIỀN", "Số tiền: ", "#FFE600"));
        c.usedColumns.add(col("STORE", "CỬA HÀNG", "Cửa hàng: "));
        c.usedColumns.add(col("NOTE", "GHI CHÚ", "Ghi chú: "));

        c.unusedColumns.add(col("ADDRESS", "ĐỊA CHỈ", "Địa chỉ: "));
        c.unusedColumns.add(col("EMPLOYEE", "NHÂN VIÊN", "Nhân viên: "));
        c.unusedColumns.add(col("CUSTOMER", "KHÁCH HÀNG", "Khách hàng: "));
        c.unusedColumns.add(col("SUPPLIER", "NHÀ CUNG CẤP", "Nhà CC: "));
        c.unusedColumns.add(col("ORIGINAL_DOC", "CHỨNG TỪ GỐC", "Chứng từ: "));
        c.unusedColumns.add(col("TRANSFER", "CHUYỂN KHOẢN", "Chuyển khoản: "));
        c.unusedColumns.add(col("CREATOR", "NGƯỜI TẠO", "Người tạo: "));
        c.unusedColumns.add(col("TIME_CREATED", "THỜI GIAN TẠO", "Tạo lúc: "));
        return c;
    }

    private static ColumnItem col(String key, String title, String prefix) {
        return new ColumnItem(key, title, prefix, "#FFFFFF", true, false, false, 16);
    }

    private static ColumnItem colRight(String key, String title, String prefix, String color) {
        return new ColumnItem(key, title, prefix, color, true, false, true, 17);
    }

    List<ColumnItem> getUsedColumns() { return usedColumns; }
    List<ColumnItem> getUnusedColumns() { return unusedColumns; }

    Color getCardBgColor() {
        try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(0, 79, 76); }
    }
    void setCardBgColor(Color c) { if (c != null) cardBgColorHex = hex(c); }

    public int getGridColumns() { return Math.max(1, gridColumns); }
    public void setGridColumns(int v) { gridColumns = Math.max(1, v); }
    public int getGridRows() { return Math.max(1, gridRows); }
    public void setGridRows(int v) { gridRows = Math.max(1, v); }

    boolean isShowTitle() { return showTitle; }
    void setShowTitle(boolean v) { showTitle = v; }

    static String value(PhieuChi pc, ColumnItem item, boolean title) {
        if (pc == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "NUMBER" -> safe(pc.getSoPhieu());
            case "DATE" -> pc.getNgay() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(pc.getNgay());
            case "SUBJECT_TYPE" -> safe(pc.getLoaiDoiTuong());
            case "SUBJECT_NAME" -> safe(pc.getTenDoiTuong());
            case "REASON" -> !safe(pc.getTenPhanLoai()).isBlank() ? pc.getTenPhanLoai() : safe(pc.getLyDoChi());
            case "AMOUNT" -> CurrencyUtil.format(pc.getSoTien());
            case "STORE" -> safe(pc.getTenCuaHang());
            case "NOTE" -> safe(pc.getGhiChu());
            case "ADDRESS" -> safe(pc.getDiaChi());
            case "EMPLOYEE" -> safe(pc.getTenNhanVien());
            case "CUSTOMER" -> safe(pc.getTenKhachHang());
            case "SUPPLIER" -> safe(pc.getTenNhaCungCap());
            case "ORIGINAL_DOC" -> safe(pc.getChungTuGoc());
            case "TRANSFER" -> pc.isChuyenTuTaiKhoan() ? "Có" : "Không";
            case "CREATOR" -> safe(pc.getUserCreatedId());
            case "TIME_CREATED" -> pc.getTimeCreated() == null ? "" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(pc.getTimeCreated());
            default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }

    @Override
    public TouchPhieuChiColumnConfig clone() {
        TouchPhieuChiColumnConfig c = new TouchPhieuChiColumnConfig();
        c.cardBgColorHex = cardBgColorHex;
        c.gridColumns = gridColumns;
        c.gridRows = gridRows;
        c.showTitle = showTitle;
        for (var x : usedColumns) c.usedColumns.add(x.clone());
        for (var x : unusedColumns) c.unusedColumns.add(x.clone());
        return c;
    }

    private static String safe(String v) { return v == null ? "" : v; }
    private static String hex(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}
