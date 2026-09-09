package kazuko.ex.report.bill;

public class BillItemRow {
    private String itemCode;
    private String itemName;
    private String unit;
    private Double quantity;
    private Double unitPrice;
    private Double discountRate;
    private Double discountAmount;
    private Double amount;
    private String note;

    public BillItemRow() {}

    public BillItemRow(String itemName, double quantity, double unitPrice, double amount) {
        this(null, itemName, null, quantity, unitPrice, 0, 0, amount, null);
    }

    public BillItemRow(String itemCode, String itemName, String unit, double quantity,
                       double unitPrice, double discountRate, double discountAmount,
                       double amount, String note) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.amount = amount;
        this.note = note;
    }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public Double getDiscountRate() { return discountRate; }
    public void setDiscountRate(Double discountRate) { this.discountRate = discountRate; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
