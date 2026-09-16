package kazuko.ex.ui.touch;

import kazuko.ex.util.CurrencyUtil;

import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Cấu hình hiển thị lưới thẻ cho màn hình Tồn Quỹ Touch. */
final class TouchTonQuyColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {

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

    /** Bản ghi dữ liệu một dòng/thẻ tồn quỹ */
    static final class TonQuyRecord {
        private String id;
        private String soPhieu;
        private Date ngay;
        private String dienGiai;
        private double thu;
        private double chi;
        private String loaiNguonTien;
        private String type; // "HOA_DON", "PHIEU_THU", "PHIEU_CHI"
        private Object rawObject;

        TonQuyRecord() {}

        TonQuyRecord(String id, String soPhieu, Date ngay, String dienGiai, double thu, double chi,
                     String loaiNguonTien, String type, Object rawObject) {
            this.id = id;
            this.soPhieu = soPhieu;
            this.ngay = ngay;
            this.dienGiai = dienGiai;
            this.thu = thu;
            this.chi = chi;
            this.loaiNguonTien = loaiNguonTien;
            this.type = type;
            this.rawObject = rawObject;
        }

        String getId() { return id; }
        String getSoPhieu() { return soPhieu; }
        Date getNgay() { return ngay; }
        String getDienGiai() { return dienGiai; }
        double getThu() { return thu; }
        double getChi() { return chi; }
        String getLoaiNguonTien() { return loaiNguonTien; }
        String getType() { return type; }
        Object getRawObject() { return rawObject; }
    }

    private List<ColumnItem> usedColumns = new ArrayList<>();
    private List<ColumnItem> unusedColumns = new ArrayList<>();
    private String cardBgColorHex = "#E24F05"; // Màu cam đặc trưng như ảnh mẫu
    private int gridColumns = 2, gridRows = 6;
    private boolean showTitle = true;

    static TouchTonQuyColumnConfig createDefault() {
        TouchTonQuyColumnConfig c = new TouchTonQuyColumnConfig();
        // Cột hiển thị: Cột 1 (Ngày, Số phiếu, Diễn giải) - Cột 2 (Thu, Chi)
        c.usedColumns.add(col("DATE", "NGÀY", "Ngày: "));
        c.usedColumns.add(colRight("THU", "THU", "Thu: ", "#FFFFFF"));
        c.usedColumns.add(col("NUMBER", "SỐ PHIẾU", "Số phiếu: "));
        c.usedColumns.add(colRight("CHI", "CHI", "Chi: ", "#FFFFFF"));
        c.usedColumns.add(col("DIEN_GIAI", "DIỄN GIẢI", "Diễn giải: "));

        c.unusedColumns.add(col("NGUON_TIEN", "NGUỒN TIỀN", "Nguồn: "));
        c.unusedColumns.add(col("LOAI_PHIEU", "LOẠI PHIẾU", "Loại: "));
        return c;
    }

    private static ColumnItem col(String key, String title, String prefix) {
        return new ColumnItem(key, title, prefix, "#FFFFFF", true, false, false, 16);
    }

    private static ColumnItem colRight(String key, String title, String prefix, String color) {
        return new ColumnItem(key, title, prefix, color, true, false, true, 16);
    }

    List<ColumnItem> getUsedColumns() { return usedColumns; }
    List<ColumnItem> getUnusedColumns() { return unusedColumns; }

    Color getCardBgColor() {
        try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(226, 79, 5); }
    }
    void setCardBgColor(Color c) { if (c != null) cardBgColorHex = hex(c); }

    public int getGridColumns() { return Math.max(1, gridColumns); }
    public void setGridColumns(int v) { gridColumns = Math.max(1, v); }
    public int getGridRows() { return Math.max(1, gridRows); }
    public void setGridRows(int v) { gridRows = Math.max(1, v); }

    boolean isShowTitle() { return showTitle; }
    void setShowTitle(boolean v) { showTitle = v; }

    static String value(TonQuyRecord r, ColumnItem item, boolean title) {
        if (r == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "DATE" -> r.getNgay() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(r.getNgay());
            case "NUMBER" -> safe(r.getSoPhieu());
            case "DIEN_GIAI" -> safe(r.getDienGiai());
            case "THU" -> r.getThu() > 0 ? CurrencyUtil.format(r.getThu()) : "";
            case "CHI" -> r.getChi() > 0 ? CurrencyUtil.format(r.getChi()) : "";
            case "NGUON_TIEN" -> safe(r.getLoaiNguonTien());
            case "LOAI_PHIEU" -> safe(r.getType());
            default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }

    @Override
    public TouchTonQuyColumnConfig clone() {
        TouchTonQuyColumnConfig c = new TouchTonQuyColumnConfig();
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
