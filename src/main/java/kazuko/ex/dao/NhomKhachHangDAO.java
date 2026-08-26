package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.NhomKhachHang;
import kazuko.ex.model.danhmuc.NhomKhachHangGiamGia;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NhomKhachHangDAO extends BaseDAO {
    private static final List<NhomKhachHang> MEMORY_CATEGORIES = new CopyOnWriteArrayList<>();
    private static final Map<String, NhomKhachHang> TRASH_CATEGORIES = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<NhomKhachHang> getAll() {
        List<NhomKhachHang> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            return dbList;
        }

        List<NhomKhachHang> result = new ArrayList<>();
        for (NhomKhachHang memNh : MEMORY_CATEGORIES) {
            if (memNh.getStatus() >= 0 && !TRASH_CATEGORIES.containsKey(memNh.getId())) {
                result.add(memNh);
            }
        }
        return result;
    }

    public List<NhomKhachHang> getTrashItems() {
        List<NhomKhachHang> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }

        List<NhomKhachHang> trashList = new ArrayList<>();
        for (NhomKhachHang memNh : MEMORY_CATEGORIES) {
            if (memNh.getStatus() < 0 && memNh.getId() != null) {
                trashList.add(memNh);
            }
        }
        for (NhomKhachHang tr : TRASH_CATEGORIES.values()) {
            if (!trashList.contains(tr)) {
                trashList.add(tr);
            }
        }
        return trashList;
    }

    public NhomKhachHang findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (NhomKhachHang nh : getAll()) {
            if (id.equals(nh.getId())) return nh;
        }
        for (NhomKhachHang nh : getTrashItems()) {
            if (id.equals(nh.getId())) return nh;
        }
        return null;
    }

    public NhomKhachHang findByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String clean = name.trim().toLowerCase();
        for (NhomKhachHang nh : getAll()) {
            if (nh.getName() != null && nh.getName().trim().equalsIgnoreCase(clean)) {
                return nh;
            }
        }
        return null;
    }

    public Set<String> getAllDescendantIds(String rootId) {
        Set<String> allIds = new LinkedHashSet<>();
        if (rootId == null || rootId.trim().isEmpty()) return allIds;
        allIds.add(rootId.trim());

        List<NhomKhachHang> allItems = new ArrayList<>(getAll());
        allItems.addAll(getTrashItems());

        boolean added;
        do {
            added = false;
            for (NhomKhachHang nh : allItems) {
                if (nh.getParentId() != null && !nh.getParentId().trim().isEmpty() && nh.getId() != null) {
                    if (allIds.contains(nh.getParentId().trim())) {
                        if (allIds.add(nh.getId().trim())) {
                            added = true;
                        }
                    }
                }
            }
        } while (added);

        return allIds;
    }

    public boolean insert(NhomKhachHang nh) {
        if (nh == null) return false;
        if (nh.getId() == null || nh.getId().trim().isEmpty()) {
            nh.setId(UUID.randomUUID().toString());
        }
        nh.setStatus(30);

        boolean dbSuccess = tryInsertToDb(nh);
        MEMORY_CATEGORIES.add(nh);
        TRASH_CATEGORIES.remove(nh.getId());
        return dbSuccess;
    }

    public boolean update(NhomKhachHang nh) {
        if (nh == null || nh.getId() == null) return false;
        boolean dbSuccess = tryUpdateToDb(nh);

        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (nh.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, nh);
                break;
            }
        }
        return dbSuccess;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, -1);
        NhomKhachHang found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_CATEGORIES.put(id, found);
        }
        return dbSuccess;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, 30);
        NhomKhachHang found = findById(id);
        if (found != null) {
            found.setStatus(30);
            TRASH_CATEGORIES.remove(id);
        }
        return dbSuccess;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        boolean dbSuccess = tryDeleteFromDb(id);
        TRASH_CATEGORIES.remove(id);
        MEMORY_CATEGORIES.removeIf(n -> id.equals(n.getId()));
        return dbSuccess;
    }

    private List<NhomKhachHang> tryLoadFromDb(int statusFilter) {
        List<NhomKhachHang> list = new ArrayList<>();
        String sql = "SELECT n.ID, n.NAME, n.NOTE, n.STATUS, n.USERMODIFIEDID, n.TIMEMODIFIED, " +
                     "n.TIMECREATED, n.SORTORDER, n.USERCREATEDID, n.PARENTID, n.PARENTDIR, " +
                     "n.ITEMTYPE, n.AUTOID, n.SIMAGEID, n.TILEGIAMGIA, n.DIEMTICHLUY " +
                     "FROM DNHOMKHACHHANG n " +
                     "WHERE " + (statusFilter >= 0 ? "(n.STATUS IS NULL OR n.STATUS >= 0)" : "n.STATUS < 0") + " " +
                     "ORDER BY n.SORTORDER, n.NAME";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhomKhachHang nh = new NhomKhachHang();
                    nh.setId(rs.getString("ID"));
                    nh.setName(rs.getString("NAME"));
                    nh.setGhiChu(rs.getString("NOTE"));
                    nh.setStatus(rs.getInt("STATUS"));
                    nh.setParentId(rs.getString("PARENTID"));
                    nh.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                    nh.setIconName(rs.getString("SIMAGEID") != null ? rs.getString("SIMAGEID") : "Folder.png");
                    nh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    nh.setDiemTichLuy(rs.getDouble("DIEMTICHLUY"));
                    list.add(nh);
                }
            }

            Map<String, List<NhomKhachHangGiamGia>> discountMap = loadAllDiscountsMap(conn);
            for (NhomKhachHang nh : list) {
                if (nh.getId() != null && discountMap.containsKey(nh.getId())) {
                    nh.setListGiamGiaNhomHang(discountMap.get(nh.getId()));
                } else {
                    nh.setListGiamGiaNhomHang(new ArrayList<>());
                }
            }

            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb DNHOMKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, List<NhomKhachHangGiamGia>> loadAllDiscountsMap(Connection conn) {
        Map<String, List<NhomKhachHangGiamGia>> map = new HashMap<>();
        String sql = "SELECT g.ID, g.DNHOMKHACHHANGID, g.DNHOMMATHANGID, g.TILEGIAM, m.NAME as TENNHOMMATHANG " +
                     "FROM DGIAMTHEONHOM g " +
                     "LEFT JOIN DNHOMMATHANG m ON g.DNHOMMATHANGID = m.ID " +
                     "WHERE (g.STATUS IS NULL OR g.STATUS >= 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nhomKhachId = rs.getString("DNHOMKHACHHANGID");
                if (nhomKhachId == null) continue;
                NhomKhachHangGiamGia gg = new NhomKhachHangGiamGia();
                gg.setNhomMatHangId(rs.getString("DNHOMMATHANGID"));
                gg.setTenNhomMatHang(rs.getString("TENNHOMMATHANG"));
                gg.setTiLeGiam(rs.getDouble("TILEGIAM"));
                map.computeIfAbsent(nhomKhachId, k -> new ArrayList<>()).add(gg);
            }
        } catch (Exception e) {
            System.err.println("Lỗi loadAllDiscountsMap: " + e.getMessage());
        }
        return map;
    }

    private boolean tryInsertToDb(NhomKhachHang nh) {
        String sql = "INSERT INTO DNHOMKHACHHANG (" +
                     "ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, SORTORDER, ITEMTYPE, SIMAGEID, PARENTID, TILEGIAMGIA, DIEMTICHLUY" +
                     ") VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nh.getId());
            ps.setString(2, nh.getName());
            ps.setString(3, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setString(4, getEffectiveUserId());
            ps.setString(5, String.valueOf(nh.getSortOrder()));
            ps.setInt(6, nh.getItemType() != null ? nh.getItemType() : 0);
            ps.setString(7, nh.getIconName() != null ? nh.getIconName() : "Folder.png");
            ps.setString(8, nh.getParentId());
            ps.setDouble(9, nh.getTiLeGiamGia());
            ps.setDouble(10, nh.getDiemTichLuy());
            int rows = ps.executeUpdate();

            // Save discounts in DGIAMTHEONHOM
            saveDiscountsForGroup(conn, nh);

            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryInsertToDb DNHOMKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryUpdateToDb(NhomKhachHang nh) {
        String sql = "UPDATE DNHOMKHACHHANG SET " +
                     "NAME=?, NOTE=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=?, SORTORDER=?, ITEMTYPE=?, SIMAGEID=?, PARENTID=?, TILEGIAMGIA=?, DIEMTICHLUY=? " +
                     "WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nh.getName());
            ps.setString(2, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setString(3, getEffectiveUserId());
            ps.setString(4, String.valueOf(nh.getSortOrder()));
            ps.setInt(5, nh.getItemType() != null ? nh.getItemType() : 0);
            ps.setString(6, nh.getIconName() != null ? nh.getIconName() : "Folder.png");
            ps.setString(7, nh.getParentId());
            ps.setDouble(8, nh.getTiLeGiamGia());
            ps.setDouble(9, nh.getDiemTichLuy());
            ps.setString(10, nh.getId());
            int rows = ps.executeUpdate();

            // Update discounts
            saveDiscountsForGroup(conn, nh);

            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryUpdateToDb DNHOMKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void saveDiscountsForGroup(Connection conn, NhomKhachHang nh) {
        try {
            // Delete old
            String delSql = "DELETE FROM DGIAMTHEONHOM WHERE DNHOMKHACHHANGID = ?";
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) {
                psDel.setString(1, nh.getId());
                psDel.executeUpdate();
            }

            // Insert new
            if (nh.getListGiamGiaNhomHang() != null && !nh.getListGiamGiaNhomHang().isEmpty()) {
                String insSql = "INSERT INTO DGIAMTHEONHOM (ID, DNHOMKHACHHANGID, DNHOMMATHANGID, TILEGIAM, STATUS, USERCREATEDID, TIMECREATED) " +
                                "VALUES (?, ?, ?, ?, 30, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement psIns = conn.prepareStatement(insSql)) {
                    for (NhomKhachHangGiamGia gg : nh.getListGiamGiaNhomHang()) {
                        String nmhId = gg.getNhomMatHangId();
                        if (nmhId == null || nmhId.trim().isEmpty()) {
                            // Find NMH by name if needed
                            NhomMatHangDAO nmhDAO = new NhomMatHangDAO();
                            for (var nmh : nmhDAO.getAll()) {
                                if (nmh.getName() != null && nmh.getName().equalsIgnoreCase(gg.getTenNhomMatHang())) {
                                    nmhId = nmh.getId();
                                    break;
                                }
                            }
                        }
                        if (nmhId != null && !nmhId.trim().isEmpty()) {
                            psIns.setString(1, UUID.randomUUID().toString());
                            psIns.setString(2, nh.getId());
                            psIns.setString(3, nmhId);
                            psIns.setDouble(4, gg.getTiLeGiam());
                            psIns.setString(5, getEffectiveUserId());
                            psIns.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi saveDiscountsForGroup: " + e.getMessage());
        }
    }

    private boolean trySetStatusDb(String id, int status) {
        String sql = "UPDATE DNHOMKHACHHANG SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi trySetStatusDb DNHOMKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement psGg = conn.prepareStatement("DELETE FROM DGIAMTHEONHOM WHERE DNHOMKHACHHANGID = ?")) {
                psGg.setString(1, id);
                psGg.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM DNHOMKHACHHANG WHERE ID=?")) {
                ps.setString(1, id);
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi tryDeleteFromDb DNHOMKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

