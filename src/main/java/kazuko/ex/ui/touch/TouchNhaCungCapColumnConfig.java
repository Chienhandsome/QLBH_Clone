package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.NhaCungCap;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Persisted layout and typography of supplier cards. */
final class TouchNhaCungCapColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
    static final class ColumnItem implements Serializable, Cloneable {
        private String key, title, prefix, colorHex;
        private boolean bold, italic, rightAligned;
        private int fontSize;
        ColumnItem() {}
        ColumnItem(String key, String title, String prefix, String colorHex, boolean bold, boolean italic, boolean rightAligned, int fontSize) {
            this.key = key; this.title = title; this.prefix = prefix; this.colorHex = colorHex;
            this.bold = bold; this.italic = italic; this.rightAligned = rightAligned; this.fontSize = fontSize;
        }
        String getKey() { return key; }
        String getTitle() { return title; }
        String getPrefix() { return prefix == null ? "" : prefix; }
        void setPrefix(String value) { prefix = value; }
        Color getColor() { try { return Color.decode(colorHex); } catch (Exception e) { return Color.WHITE; } }
        void setColor(Color value) { if (value != null) colorHex = hex(value); }
        boolean isBold() { return bold; } void setBold(boolean value) { bold = value; }
        boolean isItalic() { return italic; } void setItalic(boolean value) { italic = value; }
        boolean isRightAligned() { return rightAligned; } void setRightAligned(boolean value) { rightAligned = value; }
        int getFontSize() { return fontSize <= 0 ? 18 : fontSize; } void setFontSize(int value) { fontSize = value; }
        Font getFont() { return new Font("Segoe UI", (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), getFontSize()); }
        @Override public ColumnItem clone() { return new ColumnItem(key, title, prefix, colorHex, bold, italic, rightAligned, fontSize); }
    }

    private List<ColumnItem> usedColumns = new ArrayList<>(), unusedColumns = new ArrayList<>();
    private String cardBgColorHex = "#004F4C";
    private int gridColumns = 2, gridRows = 7;
    private boolean showTitle;

    static TouchNhaCungCapColumnConfig createDefault() {
        TouchNhaCungCapColumnConfig c = new TouchNhaCungCapColumnConfig();
        c.usedColumns.add(new ColumnItem("NAME", "TÊN NHÀ CUNG CẤP", "", "#FFFFFF", true, false, false, 18));
        c.unusedColumns.add(new ColumnItem("NOTE", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("GROUP", "NHÓM NHÀ CUNG CẤP", "Nhóm: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("CODE", "MÃ NHÀ CUNG CẤP", "Mã: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("ADDRESS", "ĐỊA CHỈ", "Địa chỉ: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("PHONE", "ĐIỆN THOẠI", "Điện thoại: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("EMAIL", "EMAIL", "Email: ", "#FFFFFF", false, false, false, 16));
        c.unusedColumns.add(new ColumnItem("WEBSITE", "WEBSITE", "Website: ", "#FFFFFF", false, false, false, 16));
        return c;
    }

    List<ColumnItem> getUsedColumns() { return usedColumns; }
    List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    Color getCardBgColor() { try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(0, 79, 76); } }
    void setCardBgColor(Color value) { if (value != null) cardBgColorHex = hex(value); }
    public int getGridColumns() { return Math.max(1, gridColumns); } public void setGridColumns(int value) { gridColumns = Math.max(1, value); }
    public int getGridRows() { return Math.max(1, gridRows); } public void setGridRows(int value) { gridRows = Math.max(1, value); }
    boolean isShowTitle() { return showTitle; } void setShowTitle(boolean value) { showTitle = value; }

    static String value(NhaCungCap supplier, ColumnItem item, boolean title) {
        if (supplier == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "NAME" -> safe(supplier.getName());
            case "NOTE" -> safe(supplier.getNote());
            case "GROUP" -> safe(supplier.getTenNhomNhaCungCap());
            case "CODE" -> safe(supplier.getMaNhaCungCap());
            case "ADDRESS" -> safe(supplier.getDiaChi());
            case "PHONE" -> safe(supplier.getDienThoai());
            case "EMAIL" -> safe(supplier.getEmail());
            case "WEBSITE" -> safe(supplier.getWebsite());
            default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }

    @Override public TouchNhaCungCapColumnConfig clone() {
        TouchNhaCungCapColumnConfig c = new TouchNhaCungCapColumnConfig();
        c.cardBgColorHex = cardBgColorHex; c.gridColumns = gridColumns; c.gridRows = gridRows; c.showTitle = showTitle;
        for (ColumnItem i : usedColumns) c.usedColumns.add(i.clone());
        for (ColumnItem i : unusedColumns) c.unusedColumns.add(i.clone());
        return c;
    }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String hex(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}
