package kazuko.ex.ui.touch;

import kazuko.ex.model.banhang.ThongKeMatHangBanRow;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/** Persisted columns and order for sold-product touch cards. */
final class TouchMatHangBanColumnConfig implements Serializable, Cloneable {
    static final class Column implements Serializable, Cloneable {
        String key;
        String title;
        Column() {}
        Column(String key, String title) { this.key = key; this.title = title; }
        @Override public Column clone() { return new Column(key, title); }
    }

    private List<Column> used = new ArrayList<>();
    private List<Column> unused = new ArrayList<>();

    static TouchMatHangBanColumnConfig defaults() {
        TouchMatHangBanColumnConfig c = new TouchMatHangBanColumnConfig();
        c.used.add(new Column("NAME", "TÊN MẶT HÀNG"));
        c.used.add(new Column("QUANTITY", "SỐ LƯỢNG"));
        c.used.add(new Column("PRICE", "ĐƠN GIÁ"));
        c.used.add(new Column("SALE", "THÀNH TIỀN BÁN"));
        c.unused.add(new Column("CODE", "MÃ HÀNG"));
        c.unused.add(new Column("COST", "GIÁ VỐN"));
        c.unused.add(new Column("DISCOUNT", "GIẢM GIÁ %"));
        c.unused.add(new Column("UNIT", "ĐVT"));
        c.unused.add(new Column("COST_TOTAL", "THÀNH TIỀN NHẬP"));
        c.unused.add(new Column("PROFIT", "LÃI"));
        c.unused.add(new Column("MARGIN", "TỈ LỆ LÃI"));
        return c;
    }

    List<Column> getUsed() { return used; }
    List<Column> getUnused() { return unused; }

    String value(ThongKeMatHangBanRow row, Column column, DecimalFormat number) {
        return switch (column.key) {
            case "NAME" -> "TÊN: " + safe(row.getTenMatHang());
            case "QUANTITY" -> "SL: " + number.format(row.getSoLuong());
            case "PRICE" -> "Đ.GIÁ: " + number.format(row.getDonGia());
            case "SALE" -> "T.TIỀN: " + number.format(row.getThanhTienBan());
            case "CODE" -> "MÃ: " + safe(row.getMaHang());
            case "COST" -> "GIÁ VỐN: " + number.format(row.getGiaVon());
            case "DISCOUNT" -> "GIẢM GIÁ: " + number.format(row.getGiamGia()) + "%";
            case "UNIT" -> "ĐVT: " + safe(row.getDonViTinh());
            case "COST_TOTAL" -> "T.NHẬP: " + number.format(row.getThanhTienNhap());
            case "PROFIT" -> "LÃI: " + number.format(row.getLai());
            case "MARGIN" -> "TỈ LỆ LÃI: " + number.format(row.getTiLeLai()) + "%";
            default -> "";
        };
    }

    private static String safe(String s) { return s == null ? "" : s; }
    @Override public TouchMatHangBanColumnConfig clone() { TouchMatHangBanColumnConfig c = new TouchMatHangBanColumnConfig(); for (Column x : used) c.used.add(x.clone()); for (Column x : unused) c.unused.add(x.clone()); return c; }
}
