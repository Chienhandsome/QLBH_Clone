package kazuko.ex.ui.touch;

import kazuko.ex.model.dathang.KhachDatHangChiTiet;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình hiển thị cột cho thẻ mặt hàng đã chọn trong đơn đặt hàng (Hình bổ sung 1).
 */
public class TouchDatHangItemColumnConfig implements Serializable, Cloneable {
    public static final String DEFAULT_CARD_BG = "#E6E6E6"; // Nền xám nhạt như Hình 4

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

    public TouchDatHangItemColumnConfig() {}

    public static TouchDatHangItemColumnConfig createDefault() {
        TouchDatHangItemColumnConfig cfg = new TouchDatHangItemColumnConfig();

        // 5 cột sử dụng mặc định như Hình bổ sung 1
        cfg.usedColumns.add(new ColumnItem("MAT_HANG", "MẶT HÀNG", "", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("SO_LUONG", "SỐ LƯỢNG", "", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("DVT", "ĐVT", "", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("GIAM_GIA", "GIẢM GIÁ %", "", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("THANH_TIEN", "THÀNH TIỀN", "", "#FFFFFF", false, false, false, 13));

        // 3 cột không sử dụng mặc định như Hình bổ sung 1
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("MA_HANG", "MÃ HÀNG", "Mã: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("DON_GIA", "ĐƠN GIÁ", "Đơn giá: ", "#FFFFFF", false, false, false, 13));

        return cfg;
    }

    public List<ColumnItem> getUsedColumns() { return usedColumns; }
    public void setUsedColumns(List<ColumnItem> usedColumns) { this.usedColumns = usedColumns; }

    public List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    public void setUnusedColumns(List<ColumnItem> unusedColumns) { this.unusedColumns = unusedColumns; }

    public static String getRawValue(KhachDatHangChiTiet ct, String key) {
        if (ct == null || key == null) return "";
        return switch (key) {
            case "MAT_HANG" -> {
                String th = ct.getTenHang();
                yield th != null && !th.isBlank() ? th : (ct.getMaHang() != null ? ct.getMaHang() : "");
            }
            case "SO_LUONG" -> {
                double sl = ct.getSoLuong();
                if (sl == (long) sl) yield String.valueOf((long) sl);
                yield String.format("%,.2f", sl);
            }
            case "DVT" -> ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "";
            case "GIAM_GIA" -> {
                double gg = ct.getTiLeGiamGia();
                if (gg > 0) yield String.format("%,.0f%%", gg);
                yield "";
            }
            case "THANH_TIEN" -> String.format("%,.0f", ct.getThanhTien());
            case "GHI_CHU" -> ct.getNote() != null ? ct.getNote() : "";
            case "MA_HANG" -> ct.getMaHang() != null ? ct.getMaHang() : "";
            case "DON_GIA" -> String.format("%,.0f", ct.getDonGia());
            default -> "";
        };
    }

    public static String getFormattedText(KhachDatHangChiTiet ct, ColumnItem col, boolean showPrefix) {
        String raw = getRawValue(ct, col.getKey());
        if (raw.isEmpty()) return "";
        if (showPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    @Override
    public TouchDatHangItemColumnConfig clone() {
        TouchDatHangItemColumnConfig copy = new TouchDatHangItemColumnConfig();
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        return copy;
    }
}
