package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.util.CurrencyUtil;

import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Persisted card layout for the touch warehouse-receipt list. */
final class TouchNhapKhoColumnConfig implements Serializable, Cloneable {
    static final class ColumnItem implements Serializable, Cloneable {
        private String key, title, prefix, colorHex;
        private boolean bold, italic, rightAligned;
        private int fontSize;
        ColumnItem() {}
        ColumnItem(String key, String title, String prefix, String color, boolean bold, boolean italic, boolean right, int size) {
            this.key = key; this.title = title; this.prefix = prefix; this.colorHex = color; this.bold = bold; this.italic = italic; this.rightAligned = right; this.fontSize = size;
        }
        String getKey() { return key; } String getTitle() { return title; } String getPrefix() { return prefix == null ? "" : prefix; } void setPrefix(String v) { prefix = v; }
        Color getColor() { try { return Color.decode(colorHex); } catch (Exception e) { return Color.WHITE; } } void setColor(Color c) { if (c != null) colorHex = hex(c); }
        boolean isBold() { return bold; } void setBold(boolean v) { bold = v; } boolean isItalic() { return italic; } void setItalic(boolean v) { italic = v; }
        boolean isRightAligned() { return rightAligned; } void setRightAligned(boolean v) { rightAligned = v; } int getFontSize() { return fontSize <= 0 ? 16 : fontSize; } void setFontSize(int v) { fontSize = v; }
        Font getFont() { return new Font("Segoe UI", (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), getFontSize()); }
        @Override public ColumnItem clone() { return new ColumnItem(key, title, prefix, colorHex, bold, italic, rightAligned, fontSize); }
    }
    private List<ColumnItem> usedColumns = new ArrayList<>(), unusedColumns = new ArrayList<>();
    private String cardBgColorHex = "#004F4C";
    private int gridColumns = 3, gridRows = 6;
    private boolean showTitle = true;

    static TouchNhapKhoColumnConfig createDefault() {
        TouchNhapKhoColumnConfig c = new TouchNhapKhoColumnConfig();
        c.usedColumns.add(col("NUMBER", "SỐ PHIẾU", "Số phiếu: "));
        c.usedColumns.add(col("DATE", "NGÀY", "Ngày: "));
        c.usedColumns.add(col("SUPPLIER", "NHÀ CUNG CẤP", "Nhà CC: "));
        c.usedColumns.add(col("TOTAL", "TỔNG CỘNG", "Tổng cộng: "));
        c.usedColumns.add(col("PAID", "THANH TOÁN", "Thanh toán: "));
        c.usedColumns.add(col("CARD_PAYMENT", "THẺ TT", "Thẻ TT: "));
        c.usedColumns.add(col("PREPAID", "THẺ TRẢ TRƯỚC", "Thẻ trả trước: "));
        c.usedColumns.add(col("REWARD", "TRỪ TÍCH LŨY", "Trừ tích lũy: "));
        c.usedColumns.add(col("BANK", "TÀI KHOẢN NGÂN HÀNG", "Tài khoản NH: "));
        c.usedColumns.add(col("VOUCHER", "MÃ VOUCHER", "Mã voucher: "));
        c.usedColumns.add(col("CARD", "THẺ", "Thẻ: "));
        c.usedColumns.add(col("TABLE", "BÀN", "Bàn: "));
        c.unusedColumns.add(col("NOTE", "GHI CHÚ", "Ghi chú: "));
        c.unusedColumns.add(col("DISCOUNT", "TIỀN GIẢM GIÁ", "Tiền giảm giá: "));
        c.unusedColumns.add(col("DISCOUNT_RATE", "TỈ LỆ GIẢM GIÁ", "Tỉ lệ giảm giá: "));
        c.unusedColumns.add(col("ITEM_AMOUNT", "TIỀN HÀNG", "Tiền hàng: "));
        c.unusedColumns.add(col("WAREHOUSE", "KHO NHẬP", "Kho nhập: "));
        c.unusedColumns.add(col("EMPLOYEE", "NHÂN VIÊN NHẬP", "NV nhập: "));
        c.unusedColumns.add(col("DESCRIPTION", "DIỄN GIẢI", "Diễn giải: "));
        c.unusedColumns.add(col("STORE", "CỬA HÀNG", "Cửa hàng: "));
        return c;
    }
    private static ColumnItem col(String key, String title, String prefix) { return new ColumnItem(key, title, prefix, "#FFFFFF", true, false, false, 16); }
    List<ColumnItem> getUsedColumns() { return usedColumns; } List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    Color getCardBgColor() { try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(0, 79, 76); } } void setCardBgColor(Color c) { if (c != null) cardBgColorHex = hex(c); }
    int getGridColumns() { return Math.max(1, gridColumns); } void setGridColumns(int v) { gridColumns = Math.max(1, v); } int getGridRows() { return Math.max(1, gridRows); } void setGridRows(int v) { gridRows = Math.max(1, v); }
    boolean isShowTitle() { return showTitle; } void setShowTitle(boolean v) { showTitle = v; }
    static String value(DonHang d, ColumnItem item, boolean title) {
        if (d == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "NUMBER" -> safe(d.getName()); case "DATE" -> d.getNgay() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(d.getNgay());
            case "SUPPLIER" -> safe(d.getTenNhaCungCap()); case "TOTAL" -> money(d.getTongCong()); case "PAID" -> money(d.getTienThanhToan());
            case "CARD_PAYMENT", "CARD" -> optionalMoney(d.getTienTheATM()); case "PREPAID" -> optionalMoney(d.getTienTheTraTruoc()); case "REWARD" -> optionalMoney(d.getTienTruTichLuy());
            case "BANK" -> safe(d.getTaiKhoanNganHangId()); case "VOUCHER" -> safe(d.getMaVoucher()); case "TABLE" -> safe(d.getBan());
            case "NOTE" -> safe(d.getGhiChu()); case "DISCOUNT" -> optionalMoney(d.getTienGiamGia()); case "DISCOUNT_RATE" -> d.getTiLeGiamGia() == 0 ? "" : d.getTiLeGiamGia() + "%";
            case "ITEM_AMOUNT" -> money(d.getTienHang()); case "WAREHOUSE" -> safe(d.getTenKhoNhap()); case "EMPLOYEE" -> safe(d.getTenNhanVienNhap());
            case "DESCRIPTION" -> safe(d.getDienGiai()); case "STORE" -> safe(d.getTenCuaHang()); default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }
    @Override public TouchNhapKhoColumnConfig clone() { TouchNhapKhoColumnConfig c = new TouchNhapKhoColumnConfig(); c.cardBgColorHex = cardBgColorHex; c.gridColumns = gridColumns; c.gridRows = gridRows; c.showTitle = showTitle; for (var x : usedColumns) c.usedColumns.add(x.clone()); for (var x : unusedColumns) c.unusedColumns.add(x.clone()); return c; }
    private static String money(double value) { return CurrencyUtil.format(value); } private static String optionalMoney(double value) { return value == 0 ? "" : money(value); } private static String safe(String v) { return v == null ? "" : v; } private static String hex(Color c) { return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()); }
}
