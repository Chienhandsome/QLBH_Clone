package kazuko.ex.model.bill;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JSON contract ổn định của bill editor. Layout được giới hạn theo block để
 * đảm bảo bill 58/80mm không lệch khi nội dung dài.
 */
public class BillTemplateDefinition {
    public enum BlockType { TEXT, FIELD, ITEMS, SEPARATOR }
    public enum Section { HEADER, SUMMARY, FOOTER }

    public static class Block {
        private String id = UUID.randomUUID().toString();
        private BlockType type = BlockType.TEXT;
        private Section section = Section.HEADER;
        private String text = "";
        private String label = "";
        private String binding = "";
        private int fontSize = 9;
        private boolean bold;
        private String alignment = "LEFT";
        private boolean visible = true;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public BlockType getType() { return type; }
        public void setType(BlockType type) { this.type = type; }
        public Section getSection() { return section; }
        public void setSection(Section section) { this.section = section; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getBinding() { return binding; }
        public void setBinding(String binding) { this.binding = binding; }
        public int getFontSize() { return fontSize; }
        public void setFontSize(int fontSize) { this.fontSize = fontSize; }
        public boolean isBold() { return bold; }
        public void setBold(boolean bold) { this.bold = bold; }
        public String getAlignment() { return alignment; }
        public void setAlignment(String alignment) { this.alignment = alignment; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }

        @Override
        public String toString() {
            return switch (type) {
                case TEXT -> "Văn bản: " + text;
                case FIELD -> "Dữ liệu: " + label + binding;
                case ITEMS -> "Bảng mặt hàng";
                case SEPARATOR -> "Đường phân cách";
            };
        }
    }

    private int schemaVersion = 1;
    private String fontName = "Tahoma";
    private double marginMm = 3.0;
    private List<Block> blocks = new ArrayList<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }
    public String getFontName() { return fontName; }
    public void setFontName(String fontName) { this.fontName = fontName; }
    public double getMarginMm() { return marginMm; }
    public void setMarginMm(double marginMm) { this.marginMm = marginMm; }
    public List<Block> getBlocks() { return blocks; }
    public void setBlocks(List<Block> blocks) { this.blocks = blocks != null ? blocks : new ArrayList<>(); }

    public static BillTemplateDefinition createDefault80mm() {
        BillTemplateDefinition d = new BillTemplateDefinition();
        d.blocks.add(field(Section.HEADER, "", "companyName", 13, true, "CENTER"));
        d.blocks.add(field(Section.HEADER, "", "companyAddress", 8, false, "CENTER"));
        d.blocks.add(field(Section.HEADER, "ĐT: ", "companyPhone", 8, false, "CENTER"));
        d.blocks.add(text(Section.HEADER, "HÓA ĐƠN BÁN HÀNG", 13, true, "CENTER"));
        d.blocks.add(separator(Section.HEADER));
        d.blocks.add(field(Section.HEADER, "Số HĐ: ", "invoiceNumber", 9, false, "LEFT"));
        d.blocks.add(field(Section.HEADER, "Ngày: ", "invoiceDate", 9, false, "LEFT"));
        d.blocks.add(field(Section.HEADER, "Khách hàng: ", "customerName", 9, false, "LEFT"));
        d.blocks.add(items());
        d.blocks.add(separator(Section.SUMMARY));
        d.blocks.add(field(Section.SUMMARY, "Tiền hàng: ", "subtotal", 9, false, "RIGHT"));
        d.blocks.add(field(Section.SUMMARY, "Giảm giá: ", "discount", 9, false, "RIGHT"));
        d.blocks.add(field(Section.SUMMARY, "VAT: ", "tax", 9, false, "RIGHT"));
        d.blocks.add(field(Section.SUMMARY, "TỔNG: ", "total", 12, true, "RIGHT"));
        d.blocks.add(field(Section.SUMMARY, "Đã thanh toán: ", "paid", 9, false, "RIGHT"));
        d.blocks.add(text(Section.FOOTER, "Cảm ơn quý khách và hẹn gặp lại!", 9, false, "CENTER"));
        return d;
    }

    public static Block text(Section section, String value, int size, boolean bold, String alignment) {
        Block b = new Block();
        b.type = BlockType.TEXT;
        b.section = section;
        b.text = value;
        b.fontSize = size;
        b.bold = bold;
        b.alignment = alignment;
        return b;
    }

    public static Block field(Section section, String label, String binding, int size, boolean bold, String alignment) {
        Block b = new Block();
        b.type = BlockType.FIELD;
        b.section = section;
        b.label = label;
        b.binding = binding;
        b.fontSize = size;
        b.bold = bold;
        b.alignment = alignment;
        return b;
    }

    public static Block separator(Section section) {
        Block b = new Block();
        b.type = BlockType.SEPARATOR;
        b.section = section;
        return b;
    }

    public static Block items() {
        Block b = new Block();
        b.type = BlockType.ITEMS;
        b.section = Section.HEADER;
        return b;
    }
}
