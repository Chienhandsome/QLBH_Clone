package kazuko.ex.ui.touch;

import kazuko.ex.model.khohang.TonKhoItem;

import java.awt.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/** Persisted layout and typography of touch stock cards. */
final class TouchTonKhoColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
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
    private int gridColumns = 3, gridRows = 8;
    private boolean showTitle;

    static TouchTonKhoColumnConfig createDefault() {
        TouchTonKhoColumnConfig c = new TouchTonKhoColumnConfig();
        c.usedColumns.add(new ColumnItem("WARRANTY", "BẢO HÀNH", "Bảo hành: ", "#FFFFFF", true, false, false, 15));
        c.usedColumns.add(new ColumnItem("NAME", "MẶT HÀNG", "Mặt hàng: ", "#FFFFFF", true, false, false, 18));
        c.usedColumns.add(new ColumnItem("STOCK", "TỒN", "Tồn: ", "#FFFF00", true, false, true, 18));
        c.unusedColumns.add(new ColumnItem("STOCK2", "TỒN 2 ĐVT", "Tồn 2 ĐVT: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("SALE_PRICE", "GIÁ BÁN", "Giá bán: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("UNIT", "ĐVT", "ĐVT: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("CODE", "MÃ HÀNG", "Mã hàng: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("MIN_STOCK", "TỒN TỐI THIỂU", "Tồn tối thiểu: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("MANUFACTURER", "HÃNG SX", "Hãng SX: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("COST_PRICE", "GIÁ VỐN", "Giá vốn: ", "#FFFFFF", true, false, false, 16));
        c.unusedColumns.add(new ColumnItem("UNIT2", "ĐVT 2", "ĐVT 2: ", "#FFFFFF", true, false, false, 16));
        return c;
    }

    List<ColumnItem> getUsedColumns() { return usedColumns; }
    List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    Color getCardBgColor() { try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(0, 79, 76); } }
    void setCardBgColor(Color value) { if (value != null) cardBgColorHex = hex(value); }
    public int getGridColumns() { return Math.max(1, gridColumns); } public void setGridColumns(int value) { gridColumns = Math.max(1, value); }
    public int getGridRows() { return Math.max(1, gridRows); } public void setGridRows(int value) { gridRows = Math.max(1, value); }
    boolean isShowTitle() { return showTitle; } void setShowTitle(boolean value) { showTitle = value; }

    static String value(TonKhoItem stock, ColumnItem item, boolean title) {
        if (stock == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "WARRANTY" -> safe(stock.getBaoHanh());
            case "NAME" -> safe(stock.getTenHang());
            case "STOCK" -> number(stock.getTon());
            case "STOCK2" -> number(stock.getTon2Dvt());
            case "SALE_PRICE" -> number(stock.getGiaBan());
            case "UNIT" -> safe(stock.getDonViTinh());
            case "CODE" -> safe(stock.getMaHang());
            case "MIN_STOCK" -> number(stock.getTonToiThieu());
            case "MANUFACTURER" -> safe(stock.getHangSx());
            case "COST_PRICE" -> number(stock.getGiaVon());
            case "UNIT2" -> safe(stock.getDonViTinh2());
            default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }

    @Override public TouchTonKhoColumnConfig clone() {
        TouchTonKhoColumnConfig c = new TouchTonKhoColumnConfig();
        c.cardBgColorHex = cardBgColorHex; c.gridColumns = gridColumns; c.gridRows = gridRows; c.showTitle = showTitle;
        for (ColumnItem i : usedColumns) c.usedColumns.add(i.clone());
        for (ColumnItem i : unusedColumns) c.unusedColumns.add(i.clone());
        return c;
    }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String number(double value) { return new DecimalFormat("#,##0.##").format(value); }
    private static String hex(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}
