package kazuko.ex.dao;

import kazuko.ex.model.nhansu.LyDoThuongPhat;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LyDoThuongPhatDAO extends BaseDAO {
    private static final List<LyDoThuongPhat> inMemoryList = new ArrayList<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!inMemoryList.isEmpty()) return;

        LyDoThuongPhat ld1 = new LyDoThuongPhat("bbcac5ef-e944-41b6-ac5a-dd6533067419", "Doanh số");
        ld1.setDefault(true);
        inMemoryList.add(ld1);

        LyDoThuongPhat ld2 = new LyDoThuongPhat("41239a51-42a3-496d-a1c1-cf0e266f7db2", "Tính sai cho khách");
        inMemoryList.add(ld2);
    }

    private List<LyDoThuongPhat> tryLoadFromDb(int statusFilter) {
        List<LyDoThuongPhat> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM DLYDOTHUONGPHAT")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID");
                    String name = rs.getString("NAME");
                    String note = rs.getString("NOTE");
                    int st = rs.getInt("STATUS");

                    LyDoThuongPhat ld = new LyDoThuongPhat(id, name);
                    ld.setGhiChu(note);
                    ld.setStatus(st >= 0 ? 1 : -1);

                    if ((statusFilter >= 0 && ld.getStatus() >= 0) || (statusFilter < 0 && ld.getStatus() < 0)) {
                        list.add(ld);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public List<LyDoThuongPhat> getAllActive() {
        List<LyDoThuongPhat> dbList = tryLoadFromDb(1);
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        return inMemoryList.stream()
                .filter(ld -> ld.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<LyDoThuongPhat> getTrashItems() {
        List<LyDoThuongPhat> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }
        return inMemoryList.stream()
                .filter(ld -> ld.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public LyDoThuongPhat findById(String id) {
        if (id == null) return null;
        List<LyDoThuongPhat> all = getAllActive();
        for (LyDoThuongPhat ld : all) {
            if (id.equals(ld.getId())) return ld;
        }
        return inMemoryList.stream()
                .filter(ld -> id.equals(ld.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(LyDoThuongPhat ld) {
        if (ld == null) return false;
        if (ld.getId() == null || ld.getId().trim().isEmpty()) {
            ld.setId(UUID.randomUUID().toString());
        }
        ld.setStatus(1);

        String userId = getEffectiveUserId();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO DLYDOTHUONGPHAT (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?)")) {
            ps.setString(1, ld.getId());
            ps.setString(2, ld.getName());
            ps.setString(3, ld.getGhiChu());
            ps.setString(4, userId);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        inMemoryList.add(ld);
        audit("Lý do thưởng phạt", "THÊM", ld.getName(), "Thêm lý do thưởng phạt");
        return true;
    }

    public boolean update(LyDoThuongPhat ld) {
        if (ld == null || ld.getId() == null) return false;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DLYDOTHUONGPHAT SET NAME = ?, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP WHERE ID = ?")) {
            ps.setString(1, ld.getName());
            ps.setString(2, ld.getGhiChu());
            ps.setString(3, ld.getId());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        for (int i = 0; i < inMemoryList.size(); i++) {
            if (ld.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, ld);
                audit("Lý do thưởng phạt", "SỬA", ld.getName(), "Cập nhật lý do thưởng phạt");
                return true;
            }
        }
        audit("Lý do thưởng phạt", "SỬA", ld.getName(), "Cập nhật lý do thưởng phạt");
        return true;
    }

    public boolean moveToTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DLYDOTHUONGPHAT SET STATUS = -1 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        LyDoThuongPhat ld = findById(id);
        if (ld != null) {
            ld.setStatus(-1);
            audit("Lý do thưởng phạt", "XÓA", ld.getName(), "Chuyển lý do thưởng phạt vào thùng rác");
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DLYDOTHUONGPHAT SET STATUS = 30 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        LyDoThuongPhat ld = findById(id);
        if (ld != null) {
            ld.setStatus(1);
            audit("Lý do thưởng phạt", "KHÔI PHỤC", ld.getName(), "Khôi phục lý do thưởng phạt");
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM DLYDOTHUONGPHAT WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        boolean removed = inMemoryList.removeIf(ld -> id != null && id.equals(ld.getId()));
        if (removed) audit("Lý do thưởng phạt", "XÓA VĨNH VIỄN", id, "Xóa vĩnh viễn lý do thưởng phạt");
        return removed;
    }
}
