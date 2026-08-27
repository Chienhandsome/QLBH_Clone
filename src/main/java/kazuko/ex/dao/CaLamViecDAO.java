package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.CaLamViec;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CaLamViecDAO {
    private static final List<CaLamViec> inMemoryList = new ArrayList<>();
    private static String defaultShiftId = "ca-1";

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        // Active shifts matching Screenshot 1
        CaLamViec ca1 = new CaLamViec("ca-1", "Ca chiều", 100.0);
        ca1.setDefault(true);
        inMemoryList.add(ca1);

        CaLamViec ca2 = new CaLamViec("ca-2", "ca đêm", 120.0);
        inMemoryList.add(ca2);

        CaLamViec ca3 = new CaLamViec("ca-3", "Ca sáng", 100.0);
        inMemoryList.add(ca3);

        // Deleted items for trash testing
        CaLamViec caTrash = new CaLamViec("ca-4", "Ca tăng cường", 150.0);
        caTrash.setStatus(-1);
        inMemoryList.add(caTrash);
    }

    public List<CaLamViec> getAllActive() {
        return inMemoryList.stream()
                .filter(ca -> ca.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<CaLamViec> getTrashItems() {
        return inMemoryList.stream()
                .filter(ca -> ca.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public CaLamViec findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(ca -> id.equals(ca.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(CaLamViec ca) {
        if (ca == null) return false;
        if (ca.getId() == null || ca.getId().trim().isEmpty()) {
            ca.setId("ca-" + UUID.randomUUID().toString().substring(0, 8));
        }
        ca.setStatus(1);
        inMemoryList.add(ca);
        return true;
    }

    public boolean update(CaLamViec ca) {
        if (ca == null || ca.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (ca.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, ca);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        CaLamViec ca = findById(id);
        if (ca != null) {
            ca.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        CaLamViec ca = findById(id);
        if (ca != null) {
            ca.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return inMemoryList.removeIf(ca -> id != null && id.equals(ca.getId()));
    }

    public boolean updateParentId(String id, String newParentId) {
        CaLamViec ca = findById(id);
        if (ca != null) {
            ca.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultShiftId() {
        return defaultShiftId;
    }

    public void setDefaultShiftId(String id) {
        defaultShiftId = id;
        for (CaLamViec ca : inMemoryList) {
            ca.setDefault(ca.getId() != null && ca.getId().equals(id));
        }
    }
}
