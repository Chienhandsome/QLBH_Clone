package kazuko.ex.ui.touch;

import kazuko.ex.model.system.ActivityLog;

import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/** Persisted presentation settings for the touch activity-log cards. */
public class TouchLuuVetColumnConfig implements Serializable, Cloneable, TouchGridSettingsModel {
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
        public ColumnItem(String key, String title, String prefix, String colorHex,
                          boolean bold, boolean italic, boolean rightAligned, int fontSize) {
            this.key = key; this.title = title; this.prefix = prefix; this.colorHex = colorHex;
            this.bold = bold; this.italic = italic; this.rightAligned = rightAligned; this.fontSize = fontSize;
        }
        public String getKey() { return key; }
        public String getTitle() { return title; }
        public String getPrefix() { return prefix == null ? "" : prefix; }
        public void setPrefix(String prefix) { this.prefix = prefix; }
        public Color getColor() { try { return Color.decode(colorHex); } catch (Exception e) { return Color.WHITE; } }
        public void setColor(Color value) { if (value != null) colorHex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue()); }
        public boolean isBold() { return bold; }
        public void setBold(boolean bold) { this.bold = bold; }
        public boolean isItalic() { return italic; }
        public void setItalic(boolean italic) { this.italic = italic; }
        public boolean isRightAligned() { return rightAligned; }
        public void setRightAligned(boolean rightAligned) { this.rightAligned = rightAligned; }
        public int getFontSize() { return fontSize <= 0 ? 18 : fontSize; }
        public void setFontSize(int fontSize) { this.fontSize = fontSize; }
        public Font getFont() { return new Font("Segoe UI", (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0), getFontSize()); }
        @Override public ColumnItem clone() { return new ColumnItem(key, title, prefix, colorHex, bold, italic, rightAligned, fontSize); }
    }

    private List<ColumnItem> usedColumns = new ArrayList<>();
    private List<ColumnItem> unusedColumns = new ArrayList<>();
    private String cardBgColorHex = "#004F4C";
    private int gridColumns = 1;
    private int gridRows = 10;
    private boolean showTitle = false;

    public static TouchLuuVetColumnConfig createDefault() {
        TouchLuuVetColumnConfig c = new TouchLuuVetColumnConfig();
        c.usedColumns.add(new ColumnItem("REFERENCE", "SỐ ĐƠN HÀNG", "", "#FFFFFF", false, false, false, 18));
        c.usedColumns.add(new ColumnItem("USER", "TÀI KHOẢN", "", "#FFFFFF", true, false, true, 18));
        c.usedColumns.add(new ColumnItem("CONTENT", "NỘI DUNG", "", "#FFFFFF", false, false, false, 18));
        c.usedColumns.add(new ColumnItem("TIME", "GIỜ", "", "#FFFFFF", true, false, true, 18));
        c.unusedColumns.add(new ColumnItem("DATE", "NGÀY", "", "#FFFFFF", true, false, true, 18));
        c.unusedColumns.add(new ColumnItem("ACTION", "HÀNH ĐỘNG", "", "#FFFFFF", false, false, false, 18));
        c.unusedColumns.add(new ColumnItem("MODULE", "CHỨC NĂNG", "", "#FFFFFF", false, false, false, 18));
        c.unusedColumns.add(new ColumnItem("DEVICE", "THIẾT BỊ", "", "#FFFFFF", false, false, false, 18));
        return c;
    }

    public List<ColumnItem> getUsedColumns() { return usedColumns; }
    public List<ColumnItem> getUnusedColumns() { return unusedColumns; }
    public Color getCardBgColor() { try { return Color.decode(cardBgColorHex); } catch (Exception e) { return new Color(0, 79, 76); } }
    public void setCardBgColor(Color value) { if (value != null) cardBgColorHex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue()); }
    public int getGridColumns() { return gridColumns <= 0 ? 1 : gridColumns; }
    public void setGridColumns(int value) { gridColumns = value; }
    public int getGridRows() { return gridRows <= 0 ? 10 : gridRows; }
    public void setGridRows(int value) { gridRows = value; }
    public boolean isShowTitle() { return showTitle; }
    public void setShowTitle(boolean value) { showTitle = value; }

    public static String value(ActivityLog log, ColumnItem item, boolean title) {
        if (log == null || item == null) return "";
        String raw = switch (item.getKey()) {
            case "REFERENCE" -> safe(log.getReferenceNo());
            case "USER" -> safe(log.getUserName());
            case "CONTENT" -> safe(log.getContent());
            case "TIME" -> log.getEventTime() == null ? "" : new SimpleDateFormat("HH:mm:ss").format(log.getEventTime());
            case "DATE" -> log.getEventTime() == null ? "" : new SimpleDateFormat("dd/MM/yyyy").format(log.getEventTime());
            case "ACTION" -> safe(log.getAction());
            case "MODULE" -> safe(log.getModule());
            case "DEVICE" -> safe(log.getDeviceName());
            default -> "";
        };
        return title && !raw.isEmpty() ? item.getPrefix() + raw : raw;
    }

    private static String safe(String value) { return value == null ? "" : value; }
    @Override public TouchLuuVetColumnConfig clone() {
        TouchLuuVetColumnConfig c = new TouchLuuVetColumnConfig();
        c.cardBgColorHex = cardBgColorHex; c.gridColumns = gridColumns; c.gridRows = gridRows; c.showTitle = showTitle;
        for (ColumnItem i : usedColumns) c.usedColumns.add(i.clone());
        for (ColumnItem i : unusedColumns) c.unusedColumns.add(i.clone());
        return c;
    }
}
