package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;

import java.awt.*;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình hiển thị cột và định dạng thẻ khách hàng thân thiết cảm ứng.
 */
public class TouchKhachHangThanThietColumnConfig implements Serializable, Cloneable {
    public static final String DEFAULT_CARD_BG = "#004F4C"; // Teal đậm mặc định

    private static final DecimalFormat DF_CURRENCY = new DecimalFormat("#,##0");
    private static final DecimalFormat DF_POINTS = new DecimalFormat("#,##0.##");

    public static class ColumnItem implements Serializable, Cloneable {
        private String key;
        private String title;
        private String prefix;
        private String colorHex;
        private boolean bold;
        private boolean italic;
        private boolean rightAligned;
        private int fontSize;

        public ColumnItem() {}

        public ColumnItem(String key, String title, String prefix, String colorHex, boolean bold, boolean italic, boolean rightAligned, int fontSize) {
            this.key = key;
            this.title = title;
            this.prefix = prefix;
            this.colorHex = colorHex;
            this.bold = bold;
            this.italic = italic;
            this.rightAligned = rightAligned;
            this.fontSize = fontSize;
        }

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getPrefix() { return prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }

        public String getColorHex() { return colorHex == null || colorHex.isBlank() ? "#FFFFFF" : colorHex; }
        public void setColorHex(String colorHex) { this.colorHex = colorHex; }

        public Color getColor() {
            try {
                return Color.decode(getColorHex());
            } catch (Exception e) {
                return Color.WHITE;
            }
        }

        public void setColor(Color color) {
            if (color != null) {
                this.colorHex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        public boolean isBold() { return bold; }
        public void setBold(boolean bold) { this.bold = bold; }

        public boolean isItalic() { return italic; }
        public void setItalic(boolean italic) { this.italic = italic; }

        public boolean isRightAligned() { return rightAligned; }
        public void setRightAligned(boolean rightAligned) { this.rightAligned = rightAligned; }

        public int getFontSize() { return fontSize <= 0 ? 14 : fontSize; }
        public void setFontSize(int fontSize) { this.fontSize = fontSize; }

        public Font getFont() {
            int style = Font.PLAIN;
            if (bold) style |= Font.BOLD;
            if (italic) style |= Font.ITALIC;
            return new Font("Segoe UI", style, getFontSize());
        }

        @Override
        public ColumnItem clone() {
            try {
                return (ColumnItem) super.clone();
            } catch (CloneNotSupportedException e) {
                return new ColumnItem(key, title, prefix, colorHex, bold, italic, rightAligned, fontSize);
            }
        }
    }

    private List<ColumnItem> usedColumns = new ArrayList<>();
    private List<ColumnItem> unusedColumns = new ArrayList<>();
    private String cardBgColorHex = DEFAULT_CARD_BG;
    private int gridColumns = 1;
    private int gridRows = 8;
    private boolean showTitle = true;

    public TouchKhachHangThanThietColumnConfig() {}

    public static TouchKhachHangThanThietColumnConfig createDefault() {
        TouchKhachHangThanThietColumnConfig cfg = new TouchKhachHangThanThietColumnConfig();
        // Cột sử dụng mặc định (Hình 1 & Hình 2)
        cfg.usedColumns.add(new ColumnItem("NAME", "TÊN KHÁCH HÀNG", "KH: ", "#FFFFFF", false, false, false, 15));
        cfg.usedColumns.add(new ColumnItem("DIEM_TICH_LUY", "ĐIỂM TÍCH LŨY", "ĐIỂM: ", "#FFFFFF", false, false, true, 15));

        // Cột chưa sử dụng mặc định (Hình 2)
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 14));
        cfg.unusedColumns.add(new ColumnItem("DIA_CHI", "ĐỊA CHỈ", "ĐC: ", "#FFFFFF", false, false, false, 14));
        cfg.unusedColumns.add(new ColumnItem("DIEN_THOAI", "ĐIỆN THOẠI", "ĐT: ", "#FFFFFF", false, false, false, 14));
        cfg.unusedColumns.add(new ColumnItem("EMAIL", "EMAIL", "Email: ", "#FFFFFF", false, false, false, 14));
        cfg.unusedColumns.add(new ColumnItem("DOANH_SO", "DOANH SỐ", "DS: ", "#FFFFFF", false, false, true, 14));
        cfg.unusedColumns.add(new ColumnItem("SO_HOA_DON", "SỐ HÓA ĐƠN", "Số HĐ: ", "#FFFFFF", false, false, true, 14));

        return cfg;
    }

    public List<ColumnItem> getUsedColumns() { return usedColumns; }
    public void setUsedColumns(List<ColumnItem> usedColumns) { this.usedColumns = usedColumns; }

    public List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    public void setUnusedColumns(List<ColumnItem> unusedColumns) { this.unusedColumns = unusedColumns; }

    public String getCardBgColorHex() { return cardBgColorHex == null ? DEFAULT_CARD_BG : cardBgColorHex; }
    public void setCardBgColorHex(String cardBgColorHex) { this.cardBgColorHex = cardBgColorHex; }

    public Color getCardBgColor() {
        try {
            return Color.decode(getCardBgColorHex());
        } catch (Exception e) {
            return new Color(0, 79, 76);
        }
    }

    public void setCardBgColor(Color color) {
        if (color != null) {
            this.cardBgColorHex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    public int getGridColumns() { return gridColumns <= 0 ? 1 : gridColumns; }
    public void setGridColumns(int gridColumns) { this.gridColumns = gridColumns; }

    public int getGridRows() { return gridRows <= 0 ? 8 : gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }

    public boolean isShowTitle() { return showTitle; }
    public void setShowTitle(boolean showTitle) { this.showTitle = showTitle; }

    public static String getRawValue(KhachHangLoyaltySummary summary, String key) {
        if (summary == null || key == null) return "";
        var kh = summary.getKhachHang();
        return switch (key) {
            case "NAME" -> kh != null && kh.getName() != null ? kh.getName().trim() : "";
            case "DIEM_TICH_LUY" -> {
                double pt = summary.getDiemTichLuy();
                yield pt == (long) pt ? String.valueOf((long) pt) : DF_POINTS.format(pt);
            }
            case "GHI_CHU" -> kh != null && kh.getGhiChu() != null ? kh.getGhiChu().trim() : "";
            case "DIA_CHI" -> kh != null && kh.getDiaChi() != null ? kh.getDiaChi().trim() : "";
            case "DIEN_THOAI" -> kh != null && kh.getDienThoai() != null ? kh.getDienThoai().trim() : "";
            case "EMAIL" -> kh != null && kh.getEmail() != null ? kh.getEmail().trim() : "";
            case "DOANH_SO" -> DF_CURRENCY.format(summary.getDoanhSo());
            case "SO_HOA_DON" -> String.valueOf(summary.getSoHoaDon());
            default -> "";
        };
    }

    public static String getFormattedText(KhachHangLoyaltySummary summary, ColumnItem col, boolean showPrefix) {
        String raw = getRawValue(summary, col.getKey());
        if (raw.isEmpty()) return "";
        if (showPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    @Override
    public TouchKhachHangThanThietColumnConfig clone() {
        TouchKhachHangThanThietColumnConfig copy = new TouchKhachHangThanThietColumnConfig();
        copy.cardBgColorHex = this.cardBgColorHex;
        copy.gridColumns = this.gridColumns;
        copy.gridRows = this.gridRows;
        copy.showTitle = this.showTitle;
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        return copy;
    }
}
