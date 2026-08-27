package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.TaiKhoanNganHang;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaiKhoanNganHangDAO {
    private static final List<TaiKhoanNganHang> inMemoryList = new ArrayList<>();
    private static String defaultAccountId = "tk-1";

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        // Active accounts matching Screenshot 1
        TaiKhoanNganHang tk1 = new TaiKhoanNganHang("tk-1", "tk 1");
        tk1.setDefault(true);
        inMemoryList.add(tk1);

        TaiKhoanNganHang tk2 = new TaiKhoanNganHang("tk-2", "tk 2");
        inMemoryList.add(tk2);

        TaiKhoanNganHang tk3 = new TaiKhoanNganHang("tk-3", "tk 3");
        inMemoryList.add(tk3);

        // Deleted item for trash
        TaiKhoanNganHang tkTrash = new TaiKhoanNganHang("tk-4", "tk cũ");
        tkTrash.setStatus(-1);
        inMemoryList.add(tkTrash);
    }

    public List<TaiKhoanNganHang> getAllActive() {
        return inMemoryList.stream()
                .filter(tk -> tk.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<TaiKhoanNganHang> getTrashItems() {
        return inMemoryList.stream()
                .filter(tk -> tk.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public TaiKhoanNganHang findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(tk -> id.equals(tk.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(TaiKhoanNganHang tk) {
        if (tk == null) return false;
        if (tk.getId() == null || tk.getId().trim().isEmpty()) {
            tk.setId("tk-" + UUID.randomUUID().toString().substring(0, 8));
        }
        tk.setStatus(1);
        inMemoryList.add(tk);
        return true;
    }

    public boolean update(TaiKhoanNganHang tk) {
        if (tk == null || tk.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (tk.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, tk);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        TaiKhoanNganHang tk = findById(id);
        if (tk != null) {
            tk.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        TaiKhoanNganHang tk = findById(id);
        if (tk != null) {
            tk.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return inMemoryList.removeIf(tk -> id != null && id.equals(tk.getId()));
    }

    public boolean updateParentId(String id, String newParentId) {
        TaiKhoanNganHang tk = findById(id);
        if (tk != null) {
            tk.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultAccountId() {
        return defaultAccountId;
    }

    public void setDefaultAccountId(String id) {
        defaultAccountId = id;
        for (TaiKhoanNganHang tk : inMemoryList) {
            tk.setDefault(tk.getId() != null && tk.getId().equals(id));
        }
    }
}
