package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.KhachHang;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Cấu hình hiển thị cột và định dạng thẻ khách hàng trên màn hình cảm ứng theo Hình 1. */
public class TouchKhachHangColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
    public static final String DEFAULT_CARD_BG = "#004F4C"; // Màu Teal mặc định

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
    private int gridColumns = 4;
    private int gridRows = 6;
    private boolean showTitle = true;

    public TouchKhachHangColumnConfig() {}

    public static TouchKhachHangColumnConfig createDefault() {
        TouchKhachHangColumnConfig cfg = new TouchKhachHangColumnConfig();
        // 3 dòng hiển thị mặc định đúng như Hình 1
        cfg.usedColumns.add(new ColumnItem("TINH_THANH", "TỈNH THÀNH", "", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("MA_KHACH", "MÃ KHÁCH", "", "#FFFFFF", true, false, false, 15));
        cfg.usedColumns.add(new ColumnItem("NAME", "TÊN KHÁCH HÀNG", "", "#FFFFFF", false, false, false, 13));

        // Cột chưa dùng
        cfg.unusedColumns.add(new ColumnItem("DIEN_THOAI", "ĐIỆN THOẠI", "ĐT: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("DIA_CHI", "ĐỊA CHỈ", "ĐC: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("EMAIL", "EMAIL", "Email: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("NHOM_KHACH", "NHÓM KHÁCH", "Nhóm: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("MA_SO_THUE", "MÃ SỐ THUẾ", "MST: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("DIEM_TICH_LUY", "ĐIỂM TÍCH LŨY", "Điểm: ", "#FFFF00", false, false, true, 13));
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 13));

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

    public int getGridColumns() { return gridColumns <= 0 ? 4 : gridColumns; }
    public void setGridColumns(int gridColumns) { this.gridColumns = gridColumns; }

    public int getGridRows() { return gridRows <= 0 ? 6 : gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }

    public boolean isShowTitle() { return showTitle; }
    public void setShowTitle(boolean showTitle) { this.showTitle = showTitle; }

    public static String getRawValue(KhachHang kh, String key) {
        if (kh == null || key == null) return "";
        return switch (key) {
            case "TINH_THANH" -> {
                String tt = safe(kh.getTinhThanh());
                if (tt.isEmpty()) tt = safe(kh.getDiaChi());
                yield tt;
            }
            case "MA_KHACH" -> safe(kh.getMaKhach());
            case "NAME" -> safe(kh.getName());
            case "DIEN_THOAI" -> safe(kh.getDienThoai());
            case "DIA_CHI" -> safe(kh.getDiaChi());
            case "EMAIL" -> safe(kh.getEmail());
            case "NHOM_KHACH" -> safe(kh.getTenNhomKhachHang());
            case "MA_SO_THUE" -> safe(kh.getMaSoThue());
            case "DIEM_TICH_LUY" -> String.valueOf((long) kh.getDiemTichLuy());
            case "GHI_CHU" -> safe(kh.getGhiChu());
            default -> "";
        };
    }

    public static String getFormattedText(KhachHang kh, ColumnItem col, boolean showPrefix) {
        String raw = getRawValue(kh, col.getKey());
        if (raw.isEmpty()) return "";
        if (showPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public TouchKhachHangColumnConfig clone() {
        TouchKhachHangColumnConfig copy = new TouchKhachHangColumnConfig();
        copy.cardBgColorHex = this.cardBgColorHex;
        copy.gridColumns = this.gridColumns;
        copy.gridRows = this.gridRows;
        copy.showTitle = this.showTitle;
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        return copy;
    }
}
