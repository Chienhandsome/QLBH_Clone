package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình hiển thị cột cho thẻ hóa đơn Quản lý bán hàng cảm ứng (Hình 2).
 */
public class TouchQuanLyBanHangBillColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
    public static final String DEFAULT_CARD_BG = "#004F4C"; // Xanh teal như Hình 1

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
    private int gridColumns = 2; // Hình 2: SỐ CỘT: 2
    private int gridRows = 6;    // Hình 2: SỐ DÒNG: 6
    private String cardBgHex = DEFAULT_CARD_BG;
    private boolean showTitle = false;

    public TouchQuanLyBanHangBillColumnConfig() {}

    public static TouchQuanLyBanHangBillColumnConfig createDefault() {
        TouchQuanLyBanHangBillColumnConfig cfg = new TouchQuanLyBanHangBillColumnConfig();

        // 4 Cột sử dụng mặc định như Hình 2:
        // SỐ PHIẾU (trên trái), TỔNG CỘNG (trên phải), KHÁCH HÀNG (dưới trái), THANH TOÁN BỞI (dưới phải)
        cfg.usedColumns.add(new ColumnItem("SO_PHIEU", "SỐ PHIẾU", "Số phiếu: ", "#FFFFFF", true, false, false, 14));
        cfg.usedColumns.add(new ColumnItem("TONG_CONG", "TỔNG CỘNG", "Tổng cộng: ", "#FFFFFF", true, false, true, 14));
        cfg.usedColumns.add(new ColumnItem("KHACH_HANG", "KHÁCH HÀNG", "Khách hàng: ", "#FFFFFF", false, false, false, 13));
        cfg.usedColumns.add(new ColumnItem("THANH_TOAN_BOI", "THANH TOÁN BỞI", "Thanh toán bởi: ", "#FFFFFF", false, false, true, 13));

        // 8 Cột không sử dụng đúng như Hình 2:
        cfg.unusedColumns.add(new ColumnItem("GHI_CHU", "GHI CHÚ", "Ghi chú: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("NGAY", "NGÀY", "Ngày: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("MA_KHACH", "MÃ KHÁCH", "Mã khách: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("DIA_CHI", "ĐỊA CHỈ", "Địa chỉ: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("DIEN_THOAI", "ĐIỆN THOẠI", "Điện thoại: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("GIAM_GIA", "GIẢM GIÁ", "Giảm giá: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("PT_GIAM_GIA", "% GIẢM GIÁ", "% Giảm giá: ", "#FFFFFF", false, false, false, 13));
        cfg.unusedColumns.add(new ColumnItem("TIEN_HANG", "TIỀN HÀNG", "Tiền hàng: ", "#FFFFFF", false, false, false, 13));

        return cfg;
    }

    public List<ColumnItem> getUsedColumns() { return usedColumns; }
    public void setUsedColumns(List<ColumnItem> usedColumns) { this.usedColumns = usedColumns; }

    public List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    public void setUnusedColumns(List<ColumnItem> unusedColumns) { this.unusedColumns = unusedColumns; }

    public int getGridColumns() { return gridColumns <= 0 ? 2 : gridColumns; }
    public void setGridColumns(int gridColumns) { this.gridColumns = gridColumns; }

    public int getGridRows() { return gridRows <= 0 ? 6 : gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }

    public String getCardBgHex() { return cardBgHex == null || cardBgHex.isBlank() ? DEFAULT_CARD_BG : cardBgHex; }
    public void setCardBgHex(String cardBgHex) { this.cardBgHex = cardBgHex; }

    public Color getCardBgColor() {
        try {
            return Color.decode(getCardBgHex());
        } catch (Exception e) {
            return Color.decode(DEFAULT_CARD_BG);
        }
    }

    public void setCardBgColor(Color color) {
        if (color != null) {
            this.cardBgHex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        }
    }

    public boolean isShowTitle() { return showTitle; }
    public void setShowTitle(boolean showTitle) { this.showTitle = showTitle; }

    public static String getRawValue(DonHang dh, String key) {
        if (dh == null || key == null) return "";
        return switch (key) {
            case "SO_PHIEU" -> dh.getName() != null ? dh.getName() : "";
            case "TONG_CONG" -> CurrencyUtil.format(dh.getTongCong());
            case "KHACH_HANG" -> dh.getTenKhach() != null ? dh.getTenKhach() : "Khách lẻ";
            case "THANH_TOAN_BOI" -> dh.getNguoiThanhToan() != null ? dh.getNguoiThanhToan() : "Administrator";
            case "GHI_CHU" -> dh.getGhiChu() != null ? dh.getGhiChu() : "";
            case "NGAY" -> dh.getNgay() != null ? DateUtil.formatDate(dh.getNgay()) : "";
            case "MA_KHACH" -> dh.getMaKhach() != null ? dh.getMaKhach() : "";
            case "DIA_CHI" -> dh.getDiaChiKhach() != null ? dh.getDiaChiKhach() : "";
            case "DIEN_THOAI" -> dh.getDienThoaiKhach() != null ? dh.getDienThoaiKhach() : "";
            case "GIAM_GIA" -> CurrencyUtil.format(dh.getTienGiamGia());
            case "PT_GIAM_GIA" -> String.format("%.0f%%", dh.getTiLeGiamGia());
            case "TIEN_HANG" -> CurrencyUtil.format(dh.getTienHang());
            default -> "";
        };
    }

    public static String getFormattedText(DonHang dh, ColumnItem col, boolean forceShowPrefix) {
        String raw = getRawValue(dh, col.getKey());
        if (raw.isEmpty()) return "";
        if (forceShowPrefix && col.getPrefix() != null && !col.getPrefix().isEmpty()) {
            return col.getPrefix() + raw;
        }
        return raw;
    }

    @Override
    public TouchQuanLyBanHangBillColumnConfig clone() {
        TouchQuanLyBanHangBillColumnConfig copy = new TouchQuanLyBanHangBillColumnConfig();
        for (ColumnItem item : this.usedColumns) copy.usedColumns.add(item.clone());
        for (ColumnItem item : this.unusedColumns) copy.unusedColumns.add(item.clone());
        copy.gridColumns = this.gridColumns;
        copy.gridRows = this.gridRows;
        copy.cardBgHex = this.cardBgHex;
        copy.showTitle = this.showTitle;
        return copy;
    }
}
