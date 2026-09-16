package kazuko.ex.ui.touch;

import kazuko.ex.model.congno.CongNoKhachHangItem;
import kazuko.ex.util.CurrencyUtil;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Cấu hình hiển thị lưới thẻ cho màn hình Công nợ khách hàng Touch. */
final class TouchCongNoKhachHangColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {

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
    private String cardBgColorHex = "#004F4C"; // Màu xanh teal đặc trưng như ảnh mẫu
    private int gridColumns = 2, gridRows = 7;
    private boolean showTitle = true;

    static TouchCongNoKhachHangColumnConfig createDefault() {
        TouchCongNoKhachHangColumnConfig c = new TouchCongNoKhachHangColumnConfig();
        c.usedColumns.add(new ColumnItem("NAME", "TÊN KHÁCH HÀNG", "", "#FFFFFF", true, false, false, 18));
        c.usedColumns.add(new ColumnItem("DEBT", "CÒN NỢ", "Nợ: ", "#FFE600", true, false, true, 17));
        c.usedColumns.add(new ColumnItem("PHONE", "SỐ ĐIỆN THOẠI", "", "#FFFFFF", false, false, false, 16));

        c.unusedColumns.add(col("CODE", "MÃ KHÁCH", "Mã: "));
        c.unusedColumns.add(col("ADDRESS", "ĐỊA CHỈ", "ĐC: "));
        c.unusedColumns.add(col("GROUP", "NHÓM KHÁCH", "Nhóm: "));
        c.unusedColumns.add(col("TOTAL_BUY", "TỔNG MUA", "Mua: "));
        c.unusedColumns.add(col("PAID", "ĐÃ TRẢ", "Đã trả: "));
        return c;
    }

    private static ColumnItem col(String key, String title, String prefix) {
        return new ColumnItem(key, title, prefix, "#FFFFFF", true, false, false, 16);
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

    static String value(CongNoKhachHangItem item, ColumnItem col, boolean showTitle) {
        if (item == null || col == null) return "";
        String raw = switch (col.getKey()) {
            case "NAME" -> safe(item.getTenKhach());
            case "PHONE" -> safe(item.getDienThoai());
            case "DEBT" -> item.getConNo() != 0 ? CurrencyUtil.format(item.getConNo()) : "";
            case "CODE" -> safe(item.getMaKhach());
            case "ADDRESS" -> safe(item.getDiaChi());
            case "GROUP" -> safe(item.getTenNhomKhach());
            case "TOTAL_BUY" -> CurrencyUtil.format(item.getTongMua());
            case "PAID" -> CurrencyUtil.format(item.getTongDaTra());
            default -> "";
        };
        return showTitle && !raw.isEmpty() ? col.getPrefix() + raw : raw;
    }

    @Override
    public TouchCongNoKhachHangColumnConfig clone() {
        TouchCongNoKhachHangColumnConfig c = new TouchCongNoKhachHangColumnConfig();
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
