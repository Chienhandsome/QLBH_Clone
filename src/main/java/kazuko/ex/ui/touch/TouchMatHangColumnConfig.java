package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;

import java.awt.*;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Cấu hình hiển thị cột và định dạng thẻ mặt hàng trên màn hình cảm ứng. */
public class TouchMatHangColumnConfig implements Serializable, Cloneable {
    private static final NumberFormat MONEY = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    public static final String DEFAULT_CARD_BG = "#004F4C"; // Teal mặc định

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

        public int getFontSize() { return fontSize <= 0 ? 20 : fontSize; }
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
    private int gridColumns = 2;
    private int gridRows = 8;
    private boolean showTitle = true;

    public TouchMatHangColumnConfig() {}

    public static TouchMatHangColumnConfig createDefault() {
        TouchMatHangColumnConfig cfg = new TouchMatHangColumnConfig();
        // Các cột sử dụng mặc định
        cfg.usedColumns.add(new ColumnItem("NAME", "TÊN MẶT HÀNG", "TÊN: ", "#FF50DC", true, false, false, 26));
        cfg.usedColumns.add(new ColumnItem("HANG_SAN_XUAT", "HÃNG SẢN XUẤT", "Hãng sản xuất: ", "#FFFFFF", false, false, false, 20));
        cfg.usedColumns.add(new ColumnItem("CODE", "MÃ HÀNG", "MÃ: ", "#FFFFFF", true, false, false, 20));
        cfg.usedColumns.add(new ColumnItem("GIA_BAN", "GIÁ BÁN", "GIÁ: ", "#FFFF00", true, false, true, 23));

        // Các cột chưa sử dụng mặc định
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("NHOM_MAT_HANG", "NHÓM MẶT HÀNG", "Nhóm: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("DON_VI_TINH", "ĐƠN VỊ TÍNH", "ĐVT: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("GIA_NHAP", "GIÁ NHẬP", "Giá nhập: ", "#00FFFF", false, false, true, 18));
        cfg.unusedColumns.add(new ColumnItem("TON_TOI_THIEU", "TỒN TỐI THIỂU", "Tồn min: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("TON_TOI_DA", "TỒN TỐI ĐA", "Tồn max: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("BAO_HANH", "BẢO HÀNH", "Bảo hành: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("HOA_HONG", "HOA HỒNG", "Hoa hồng: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("GIA_BAN_2", "ĐƠN GIÁ 2", "Giá 2: ", "#FFFF00", false, false, true, 20));
        cfg.unusedColumns.add(new ColumnItem("GIA_BAN_3", "ĐƠN GIÁ 3", "Giá 3: ", "#FFFF00", false, false, true, 20));
        cfg.unusedColumns.add(new ColumnItem("GIA_BAN_4", "ĐƠN GIÁ 4", "Giá 4: ", "#FFFF00", false, false, true, 20));
        cfg.unusedColumns.add(new ColumnItem("MA_SAN_CO", "MÃ SẴN CÓ", "Mã phụ: ", "#FFFFFF", false, false, false, 18));
        cfg.unusedColumns.add(new ColumnItem("TON_KHO", "TỒN KHO", "Tồn kho: ", "#FFFFFF", false, false, false, 18));

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

    public int getGridColumns() { return gridColumns <= 0 ? 2 : gridColumns; }
    public void setGridColumns(int gridColumns) { this.gridColumns = gridColumns; }

    public int getGridRows() { return gridRows <= 0 ? 8 : gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }

    public boolean isShowTitle() { return showTitle; }
    public void setShowTitle(boolean showTitle) { this.showTitle = showTitle; }

    public static String getRawValue(MatHang product, String key) {
        if (product == null || key == null) return "";
        return switch (key) {
            case "NAME" -> safe(product.getName());
            case "HANG_SAN_XUAT" -> safe(product.getHangSanXuat());
            case "CODE" -> safe(product.getCode());
            case "GIA_BAN" -> MONEY.format(product.getGiaBan());
            case "GHI_CHU" -> safe(product.getGhiChu());
            case "NHOM_MAT_HANG" -> safe(product.getTenNhom());
            case "DON_VI_TINH" -> safe(product.getTenDonViTinh());
            case "GIA_NHAP" -> MONEY.format(product.getGiaNhap());
            case "TON_TOI_THIEU" -> String.valueOf((long) product.getTonToiThieu());
            case "TON_TOI_DA" -> String.valueOf((long) product.getTonToiDa());
            case "BAO_HANH" -> safe(product.getBaoHanh());
            case "HOA_HONG" -> MONEY.format(product.getHoaHong());
            case "GIA_BAN_2" -> MONEY.format(product.getGiaBan2());
            case "GIA_BAN_3" -> MONEY.format(product.getGiaBan3());
            case "GIA_BAN_4" -> MONEY.format(product.getGiaBan4());
            case "MA_SAN_CO" -> safe(product.getMaSanCo());
            case "TON_KHO" -> String.valueOf((long) product.getTonKho());
            default -> "";
        };
    }

    public static String getFormattedText(MatHang product, ColumnItem col, boolean showPrefix) {
        String raw = getRawValue(product, col.getKey());
        if (raw.isEmpty()) return "";
        if (showPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public TouchMatHangColumnConfig clone() {
        TouchMatHangColumnConfig copy = new TouchMatHangColumnConfig();
        copy.cardBgColorHex = this.cardBgColorHex;
        copy.gridColumns = this.gridColumns;
        copy.gridRows = this.gridRows;
        copy.showTitle = this.showTitle;
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        return copy;
    }
}
