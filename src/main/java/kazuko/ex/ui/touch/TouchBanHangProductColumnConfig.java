package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình hiển thị cột cho thẻ mặt hàng trên màn hình bán hàng (Hình 4).
 */
public class TouchBanHangProductColumnConfig implements Serializable, Cloneable {
    public static final String DEFAULT_CARD_BG = "#004F4C"; // Teal

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

    public TouchBanHangProductColumnConfig() {}

    public static TouchBanHangProductColumnConfig createDefault() {
        TouchBanHangProductColumnConfig cfg = new TouchBanHangProductColumnConfig();

        // 2 cột sử dụng mặc định đúng như Hình 4: TÊN HÀNG, GIÁ BÁN
        cfg.usedColumns.add(new ColumnItem("TEN_HANG", "TÊN HÀNG", "", "#FFFFFF", true, false, false, 14));
        cfg.usedColumns.add(new ColumnItem("GIA_BAN", "GIÁ BÁN", "", "#FFFFFF", true, false, true, 14));

        // Cột chưa sử dụng đúng như Hình 4: ĐVT, MÃ HÀNG, BẢO HÀNH
        cfg.unusedColumns.add(new ColumnItem("DVT", "ĐVT", "", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("MA_HANG", "MÃ HÀNG", "", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("BAO_HANH", "BẢO HÀNH", "", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "", "#FFFFFF", false, false, false, 13));

        return cfg;
    }

    public List<ColumnItem> getUsedColumns() { return usedColumns; }
    public void setUsedColumns(List<ColumnItem> usedColumns) { this.usedColumns = usedColumns; }

    public List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    public void setUnusedColumns(List<ColumnItem> unusedColumns) { this.unusedColumns = unusedColumns; }

    public static String getRawValue(MatHang mh, String key) {
        if (mh == null || key == null) return "";
        return switch (key) {
            case "TEN_HANG" -> mh.getName() != null ? mh.getName() : (mh.getCode() != null ? mh.getCode() : "");
            case "GIA_BAN" -> String.format("%,.0f", mh.getGiaBan());
            case "DVT" -> mh.getTenDonViTinh() != null ? mh.getTenDonViTinh() : "";
            case "MA_HANG" -> mh.getCode() != null ? mh.getCode() : "";
            case "BAO_HANH" -> mh.getBaoHanh() != null ? mh.getBaoHanh() : "";
            case "GHI_CHU" -> mh.getGhiChu() != null ? mh.getGhiChu() : "";
            default -> "";
        };
    }

    public static String getFormattedText(MatHang mh, ColumnItem col, boolean showPrefix) {
        String raw = getRawValue(mh, col.getKey());
        if (raw.isEmpty()) return "";
        if (showPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    @Override
    public TouchBanHangProductColumnConfig clone() {
        TouchBanHangProductColumnConfig copy = new TouchBanHangProductColumnConfig();
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        return copy;
    }
}
