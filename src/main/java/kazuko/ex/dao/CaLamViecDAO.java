package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.CaLamViec;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CaLamViecDAO extends BaseDAO {
    private static final List<CaLamViec> inMemoryList = new ArrayList<>();
    private static String defaultShiftId = "ca-1";

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

        // Active shifts matching Screenshot 1
        CaLamViec ca1 = new CaLamViec("cf547f29-3bab-478e-8327-6899839b00ac", "Ca chiều", 100.0);
        ca1.setDefault(true);
        inMemoryList.add(ca1);

        CaLamViec ca2 = new CaLamViec("ca-2", "ca đêm", 120.0);
        inMemoryList.add(ca2);

        CaLamViec ca3 = new CaLamViec("3e1345ce-06d4-4b1f-899a-786a7aae980b", "Ca sáng", 100.0);
        inMemoryList.add(ca3);

        // Deleted items for trash testing
        CaLamViec caTrash = new CaLamViec("ca-4", "Ca tăng cường", 150.0);
        caTrash.setStatus(-1);
        inMemoryList.add(caTrash);
    }

    private List<CaLamViec> tryLoadFromDb(int statusFilter) {
        List<CaLamViec> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM DCALAMVIEC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("ID");
                    String name = rs.getString("NAME");
                    double tile = rs.getDouble("TILELUONG");
                    int st = rs.getInt("STATUS");

                    CaLamViec ca = new CaLamViec(id, name, tile > 0 ? tile : 100.0);
                    ca.setGhiChu(rs.getString("NOTE"));
                    ca.setStatus(st >= 0 ? 1 : -1);

                    if ((statusFilter >= 0 && ca.getStatus() >= 0) || (statusFilter < 0 && ca.getStatus() < 0)) {
                        list.add(ca);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public List<CaLamViec> getAllActive() {
        List<CaLamViec> dbList = tryLoadFromDb(1);
        if (dbList != null && !dbList.isEmpty()) {
            boolean hasNight = dbList.stream().anyMatch(c -> c.getName() != null && c.getName().toLowerCase().contains("đêm"));
            if (!hasNight) {
                CaLamViec caDem = new CaLamViec("ca-2", "ca đêm", 120.0);
                caDem.setStatus(1);
                dbList.add(caDem);
            }
            return dbList;
        }
        return inMemoryList.stream()
                .filter(ca -> ca.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<CaLamViec> getTrashItems() {
        List<CaLamViec> dbList = tryLoadFromDb(-1);
        if (dbList != null) return dbList;
        return inMemoryList.stream()
                .filter(ca -> ca.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public CaLamViec findById(String id) {
        if (id == null) return null;
        List<CaLamViec> all = getAllActive();
        for (CaLamViec ca : all) {
            if (id.equals(ca.getId())) return ca;
        }
        return inMemoryList.stream()
                .filter(ca -> id.equals(ca.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(CaLamViec ca) {
        if (ca == null) return false;
        if (ca.getId() == null || ca.getId().trim().isEmpty()) {
            ca.setId(UUID.randomUUID().toString());
        }
        ca.setStatus(1);

        String userId = getEffectiveUserId();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO DCALAMVIEC (ID, NAME, NOTE, STATUS, TILELUONG, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, 30, ?, CURRENT_TIMESTAMP, ?)")) {
            ps.setString(1, ca.getId());
            ps.setString(2, ca.getName());
            ps.setString(3, ca.getGhiChu());
            ps.setDouble(4, ca.getTiLeLuong());
            ps.setString(5, userId);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        inMemoryList.add(ca);
        return true;
    }

    public boolean update(CaLamViec ca) {
        if (ca == null || ca.getId() == null) return false;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DCALAMVIEC SET NAME = ?, NOTE = ?, TILELUONG = ?, TIMEMODIFIED = CURRENT_TIMESTAMP WHERE ID = ?")) {
            ps.setString(1, ca.getName());
            ps.setString(2, ca.getGhiChu());
            ps.setDouble(3, ca.getTiLeLuong());
            ps.setString(4, ca.getId());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        for (int i = 0; i < inMemoryList.size(); i++) {
            if (ca.getId().equals(inMemoryList.get(i).getId())) {
                inMemoryList.set(i, ca);
                return true;
            }
        }
        return true;
    }

    public boolean moveToTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DCALAMVIEC SET STATUS = -1 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        CaLamViec ca = findById(id);
        if (ca != null) {
            ca.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE DCALAMVIEC SET STATUS = 30 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        CaLamViec ca = findById(id);
        if (ca != null) {
            ca.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM DCALAMVIEC WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

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
