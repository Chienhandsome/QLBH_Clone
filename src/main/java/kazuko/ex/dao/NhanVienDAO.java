package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.NhanVien;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NhanVienDAO {
    private static final List<NhanVien> inMemoryList = new ArrayList<>();
    private static String defaultEmployeeId = "nv-1";

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        // Active employees matching Screenshot 1
        NhanVien nv1 = new NhanVien("nv-1", "Hoàng Giang", "user_female_1");
        nv1.setCachTinhLuong(2);
        nv1.setDefault(true);
        inMemoryList.add(nv1);

        NhanVien nv2 = new NhanVien("nv-2", "Nguyễn Minh Trang", "user_female_2");
        nv2.setCachTinhLuong(2);
        inMemoryList.add(nv2);

        NhanVien nv3 = new NhanVien("nv-3", "Nguyễn Tú Oanh", "user_female_3");
        nv3.setCachTinhLuong(2);
        inMemoryList.add(nv3);

        NhanVien nv4 = new NhanVien("nv-4", "Phạm Huyền Trang", "user_female_4");
        nv4.setCachTinhLuong(2);
        inMemoryList.add(nv4);

        NhanVien nv5 = new NhanVien("nv-5", "Phạm Quế Chi", "user_female_5");
        nv5.setCachTinhLuong(2);
        inMemoryList.add(nv5);

        NhanVien nv6 = new NhanVien("nv-6", "Trần Thúy Hằng", "user_female_6");
        nv6.setCachTinhLuong(2);
        inMemoryList.add(nv6);

        // Deleted items matching Screenshot 4
        NhanVien nvTrash1 = new NhanVien("nv-7", "Hoàng Văn Tuấn", "user_male_1");
        nvTrash1.setStatus(-1);
        inMemoryList.add(nvTrash1);

        NhanVien nvTrashFolder = new NhanVien("nv-8", "Thư mục 1", "Folder.png");
        nvTrashFolder.setFolder(true);
        nvTrashFolder.setStatus(-1);
        inMemoryList.add(nvTrashFolder);
    }

    public List<NhanVien> getAllActive() {
        return inMemoryList.stream()
                .filter(nv -> nv.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<NhanVien> getTrashItems() {
        return inMemoryList.stream()
                .filter(nv -> nv.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public NhanVien findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(nv -> id.equals(nv.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(NhanVien nv) {
        if (nv == null) return false;
        if (nv.getId() == null || nv.getId().trim().isEmpty()) {
            nv.setId("nv-" + UUID.randomUUID().toString().substring(0, 8));
        }
        nv.setStatus(1);
        inMemoryList.add(nv);
        return true;
    }

    public boolean update(NhanVien nv) {
        if (nv == null || nv.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (nv.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, nv);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return inMemoryList.removeIf(nv -> id != null && id.equals(nv.getId()));
    }

    public boolean updateParentId(String id, String newParentId) {
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultEmployeeId() {
        return defaultEmployeeId;
    }

    public void setDefaultEmployeeId(String id) {
        defaultEmployeeId = id;
        for (NhanVien nv : inMemoryList) {
            nv.setDefault(nv.getId() != null && nv.getId().equals(id));
        }
    }
}
