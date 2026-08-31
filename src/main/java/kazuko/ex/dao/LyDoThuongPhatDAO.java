package kazuko.ex.dao;

import kazuko.ex.model.nhansu.LyDoThuongPhat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LyDoThuongPhatDAO {
    private static final List<LyDoThuongPhat> inMemoryList = new ArrayList<>();

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        LyDoThuongPhat ld1 = new LyDoThuongPhat("ldtp-1", "Doanh số");
        ld1.setDefault(true);
        inMemoryList.add(ld1);

        LyDoThuongPhat ld2 = new LyDoThuongPhat("ldtp-2", "Tính sai cho khách");
        inMemoryList.add(ld2);
    }

    public List<LyDoThuongPhat> getAllActive() {
        return inMemoryList.stream()
                .filter(ld -> ld.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<LyDoThuongPhat> getTrashItems() {
        return inMemoryList.stream()
                .filter(ld -> ld.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public LyDoThuongPhat findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(ld -> id.equals(ld.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(LyDoThuongPhat ld) {
        if (ld == null) return false;
        if (ld.getId() == null || ld.getId().trim().isEmpty()) {
            ld.setId("ldtp-" + UUID.randomUUID().toString().substring(0, 8));
        }
        ld.setStatus(1);
        inMemoryList.add(ld);
        return true;
    }

    public boolean update(LyDoThuongPhat ld) {
        if (ld == null || ld.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (ld.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, ld);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        LyDoThuongPhat ld = findById(id);
        if (ld != null) {
            ld.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        LyDoThuongPhat ld = findById(id);
        if (ld != null) {
            ld.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return inMemoryList.removeIf(ld -> id != null && id.equals(ld.getId()));
    }
}
