package kazuko.ex.model.quantri;

import java.math.BigDecimal;
import java.time.Year;
import java.sql.Timestamp;
import java.util.*;

/** Annual plan; province allocations are independent of the monthly amounts. */
public class ChiTieuDoanhThu {
    public String id;
    public int year = Year.now().getValue();
    public String groupId;
    public String groupName = "";
    public String note = "";
    public int status = 30;
    public Timestamp createdAt, modifiedAt;
    public String createdBy = "", modifiedBy = "";
    public final BigDecimal[] months = new BigDecimal[12];
    public final List<ProvinceTarget> provinces = new ArrayList<>();

    public ChiTieuDoanhThu() { Arrays.fill(months, BigDecimal.ZERO); }

    public record ProvinceTarget(String provinceId, String provinceName, BigDecimal amount, String note) {}

    public record Group(String id, String name, String note, String parentId, boolean folder,
                        String imageId, int status) {
        @Override public String toString() { return name; }
    }

    public ChiTieuDoanhThu copy(boolean asNew) {
        ChiTieuDoanhThu result = new ChiTieuDoanhThu();
        result.id = asNew ? null : id;
        result.year = year;
        result.groupId = groupId;
        result.groupName = groupName;
        result.note = note;
        result.status = asNew ? 30 : status;
        result.createdAt = createdAt;
        result.modifiedAt = modifiedAt;
        result.createdBy = createdBy;
        result.modifiedBy = modifiedBy;
        System.arraycopy(months, 0, result.months, 0, 12);
        result.provinces.addAll(provinces);
        return result;
    }

    public BigDecimal total() { return Arrays.stream(months).reduce(BigDecimal.ZERO, BigDecimal::add); }

    public void validate() {
        if (year < 1900 || year > 9999) throw new IllegalArgumentException("Năm phải từ 1900 đến 9999.");
        checkNote(note);
        for (int i = 0; i < 12; i++) checkAmount(months[i], "Tháng " + (i + 1));
        Set<String> ids = new HashSet<>();
        for (ProvinceTarget p : provinces) {
            if (p.provinceId() == null || p.provinceId().isBlank())
                throw new IllegalArgumentException("Vui lòng chọn tỉnh thành cho từng dòng chỉ tiêu.");
            if (!ids.add(p.provinceId())) throw new IllegalArgumentException("Tỉnh thành bị trùng: " + p.provinceName());
            checkAmount(p.amount(), "Chỉ tiêu " + p.provinceName());
            checkNote(p.note());
        }
    }

    private static void checkNote(String note) {
        if (note != null && note.length() > 255) throw new IllegalArgumentException("Ghi chú tối đa 255 ký tự.");
    }

    private static void checkAmount(BigDecimal value, String label) {
        if (value == null || value.signum() < 0 || value.compareTo(new BigDecimal("9999999999999999.99")) > 0
                || value.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException(label + ": nhập số không âm, tối đa 16 chữ số phần nguyên và 2 số thập phân.");
    }
}
