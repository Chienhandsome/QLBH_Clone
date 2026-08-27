package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.LyDoThuChi;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LyDoThuChiDAO {
    private static final List<LyDoThuChi> inMemoryList = new ArrayList<>();
    private static String defaultReasonId = "tc-7";

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        // Active reasons matching Screenshot 1
        inMemoryList.add(new LyDoThuChi("tc-1", "Chi khác", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-2", "Chi lương nhân viên", false, true, "user_female_1"));
        inMemoryList.add(new LyDoThuChi("tc-3", "Đặt trước", true, false, ""));
        inMemoryList.add(new LyDoThuChi("tc-4", "Đồ dùng, dụng cụ", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-5", "Lương nhân viên", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-6", "Lương vệ sỹ", false, true, ""));

        LyDoThuChi tc7 = new LyDoThuChi("tc-7", "Nạp thẻ trả trước", true, false, "");
        tc7.setDefault(true);
        inMemoryList.add(tc7);

        inMemoryList.add(new LyDoThuChi("tc-8", "Ngoại giao", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-9", "Tạm ứng", false, true, "user_female_5"));
        inMemoryList.add(new LyDoThuChi("tc-10", "Thanh toán công nợ", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-11", "Thu công nợ", true, false, ""));
        inMemoryList.add(new LyDoThuChi("tc-12", "Thu đặt trước tiền mua hàng", true, false, "receipt_red"));
        inMemoryList.add(new LyDoThuChi("tc-13", "Thu tiền hoàn ứng", true, false, ""));
        inMemoryList.add(new LyDoThuChi("tc-14", "Thưởng nhân viên", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-15", "Tiền điện", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-16", "Tiền điện thoại", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-17", "Tiền nhà", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-18", "Tiền nước", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-19", "Trả trước tiền mua hàng", false, true, ""));
        inMemoryList.add(new LyDoThuChi("tc-20", "Vận chuyển", false, true, ""));

        // Deleted item for trash
        LyDoThuChi tcTrash = new LyDoThuChi("tc-21", "Chi phí sửa chữa", false, true, "");
        tcTrash.setStatus(-1);
        inMemoryList.add(tcTrash);
    }

    public List<LyDoThuChi> getAllActive() {
        return inMemoryList.stream()
                .filter(tc -> tc.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<LyDoThuChi> getTrashItems() {
        return inMemoryList.stream()
                .filter(tc -> tc.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public LyDoThuChi findById(String id) {
        if (id == null) return null;
        return inMemoryList.stream()
                .filter(tc -> id.equals(tc.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(LyDoThuChi tc) {
        if (tc == null) return false;
        if (tc.getId() == null || tc.getId().trim().isEmpty()) {
            tc.setId("tc-" + UUID.randomUUID().toString().substring(0, 8));
        }
        tc.setStatus(1);
        inMemoryList.add(tc);
        return true;
    }

    public boolean update(LyDoThuChi tc) {
        if (tc == null || tc.getId() == null) return false;
        for (int i = 0; i < inMemoryList.size(); i++) {
            if (tc.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, tc);
                return true;
            }
        }
        return false;
    }

    public boolean moveToTrash(String id) {
        LyDoThuChi tc = findById(id);
        if (tc != null) {
            tc.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        LyDoThuChi tc = findById(id);
        if (tc != null) {
            tc.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        return inMemoryList.removeIf(tc -> id != null && id.equals(tc.getId()));
    }

    public boolean updateParentId(String id, String newParentId) {
        LyDoThuChi tc = findById(id);
        if (tc != null) {
            tc.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultReasonId() {
        return defaultReasonId;
    }

    public void setDefaultReasonId(String id) {
        defaultReasonId = id;
        for (LyDoThuChi tc : inMemoryList) {
            tc.setDefault(tc.getId() != null && tc.getId().equals(id));
        }
    }
}
