package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.service.ActivityLogService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class KhoHangDAO {
    private void audit(String action, String reference, String content) {
        ActivityLogService.log("Kho hàng", action, reference, content);
    }
    private static final List<KhoHang> inMemoryList = new ArrayList<>();
    private static String defaultWarehouseId = "k-1";

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        // Active warehouses matching Screenshot 1
        KhoHang k1 = new KhoHang("k-1", "K02", "1", "");
        k1.setDefault(true);
        inMemoryList.add(k1);

        KhoHang k2 = new KhoHang("k-2", "KHO BÁN HÀNG", "1", "red_house");
        inMemoryList.add(k2);

        KhoHang k3 = new KhoHang("k-3", "Kho chi nhánh 1", "chi-nhanh-1", "purple_house");
        inMemoryList.add(k3);

        KhoHang k4 = new KhoHang("k-4", "KHO TỔNG", "headquarter", "lightbulb");
        inMemoryList.add(k4);

        KhoHang k5 = new KhoHang("k-5", "TEST", "1", "");
        inMemoryList.add(k5);

        // Deleted item for trash
        KhoHang kTrash = new KhoHang("k-6", "Kho thanh lý", "1", "");
        kTrash.setStatus(-1);
        inMemoryList.add(kTrash);
    }

    public List<KhoHang> getAll() {
        return getAllActive();
    }

    public List<KhoHang> getAllActive() {
        return inMemoryList.stream()
                .filter(k -> k.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<KhoHang> getTrashItems() {
        return inMemoryList.stream()
                .filter(k -> k.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public KhoHang findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(k -> id.equals(k.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(KhoHang k) {
        if (k == null) return false;
        if (k.getId() == null || k.getId().trim().isEmpty()) {
            k.setId("k-" + UUID.randomUUID().toString().substring(0, 8));
        }
        k.setStatus(1);
        inMemoryList.add(k);
        audit("THÊM", k.getName(), "Thêm kho hàng");
        return true;
    }

    public boolean update(KhoHang k) {
        if (k == null || k.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (k.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, k);
                audit("SỬA", k.getName(), "Cập nhật kho hàng");
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        KhoHang k = findById(id);
        if (k != null) {
            k.setStatus(-1);
            audit("XÓA", k.getName(), "Chuyển kho hàng vào thùng rác");
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        KhoHang k = findById(id);
        if (k != null) {
            k.setStatus(1);
            audit("KHÔI PHỤC", k.getName(), "Khôi phục kho hàng");
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        boolean removed = inMemoryList.removeIf(k -> id != null && id.equals(k.getId()));
        if (removed) audit("XÓA VĨNH VIỄN", id, "Xóa vĩnh viễn kho hàng");
        return removed;
    }

    public boolean updateParentId(String id, String newParentId) {
        KhoHang k = findById(id);
        if (k != null) {
            k.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultWarehouseId() {
        return defaultWarehouseId;
    }

    public void setDefaultWarehouseId(String id) {
        defaultWarehouseId = id;
        for (KhoHang k : inMemoryList) {
            k.setDefault(k.getId() != null && k.getId().equals(id));
        }
    }
}
