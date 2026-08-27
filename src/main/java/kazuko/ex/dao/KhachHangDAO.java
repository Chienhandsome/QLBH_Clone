package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class KhachHangDAO extends BaseDAO {
    private static final List<KhachHang> MEMORY_CUSTOMERS = new CopyOnWriteArrayList<>();
    private static final Map<String, KhachHang> TRASH_CUSTOMERS = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<KhachHang> getAll() {
        List<KhachHang> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            return dbList;
        }

        List<KhachHang> result = new ArrayList<>();
        for (KhachHang kh : MEMORY_CUSTOMERS) {
            if (kh.getStatus() >= 0 && !TRASH_CUSTOMERS.containsKey(kh.getId())) {
                result.add(kh);
            }
        }
        return result;
    }

    public List<KhachHang> getTrashItems() {
        List<KhachHang> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }

        List<KhachHang> list = new ArrayList<>();
        for (KhachHang kh : MEMORY_CUSTOMERS) {
            if (kh.getStatus() < 0 && kh.getId() != null) {
                list.add(kh);
            }
        }
        for (KhachHang tr : TRASH_CUSTOMERS.values()) {
            if (!list.contains(tr)) {
                list.add(tr);
            }
        }
        return list;
    }

    public KhachHang findActiveById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (KhachHang kh : getAll()) {
            if (id.equals(kh.getId())) return kh;
        }
        return null;
    }

    public KhachHang findTrashById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (KhachHang kh : getTrashItems()) {
            if (id.equals(kh.getId())) return kh;
        }
        return null;
    }

    public KhachHang findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        KhachHang active = findActiveById(id);
        if (active != null) return active;
        return findTrashById(id);
    }

    public boolean insert(KhachHang kh) {
        if (kh == null) return false;
        if (kh.getId() == null || kh.getId().trim().isEmpty()) {
            kh.setId(UUID.randomUUID().toString());
        }
        if (kh.getMaKhach() == null || kh.getMaKhach().trim().isEmpty()) {
            kh.setMaKhach(getNextMaKhach());
        }
        kh.setStatus(30);
        kh.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        kh.setUserCreatedId(getEffectiveUserId());

        boolean dbSuccess = tryInsertToDb(kh);
        MEMORY_CUSTOMERS.add(kh);
        TRASH_CUSTOMERS.remove(kh.getId());
        return dbSuccess;
    }

    public boolean update(KhachHang kh) {
        if (kh == null || kh.getId() == null) return false;
        kh.setTimeModified(new Timestamp(System.currentTimeMillis()));
        kh.setUserModifiedId(getEffectiveUserId());

        boolean dbSuccess = tryUpdateToDb(kh);
        for (int i = 0; i < MEMORY_CUSTOMERS.size(); i++) {
            if (kh.getId().equals(MEMORY_CUSTOMERS.get(i).getId())) {
                MEMORY_CUSTOMERS.set(i, kh);
                break;
            }
        }
        return dbSuccess;
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, -1);
        KhachHang kh = findById(id);
        if (kh != null) {
            kh.setStatus(-1);
            TRASH_CUSTOMERS.put(id, kh);
        }
        return dbSuccess;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, 30);
        KhachHang kh = findById(id);
        if (kh != null) {
            kh.setStatus(30);
            TRASH_CUSTOMERS.remove(id);
        }
        return dbSuccess;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        boolean dbSuccess = tryDeleteFromDb(id);
        TRASH_CUSTOMERS.remove(id);
        MEMORY_CUSTOMERS.removeIf(k -> id.equals(k.getId()));
        return dbSuccess;
    }

    public List<KhachHang> search(String keyword, String tenNhom, String nhomId) {
        List<KhachHang> list = getAll();
        List<KhachHang> result = new ArrayList<>();
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";

        for (KhachHang kh : list) {
            if (tenNhom != null && !tenNhom.isEmpty() && !"Tất cả".equals(tenNhom)) {
                if ("Chưa thiết lập".equals(tenNhom)) {
                    if (kh.getNhomKhachHangId() != null && !kh.getNhomKhachHangId().trim().isEmpty()) {
                        continue;
                    }
                } else if (nhomId != null && !nhomId.trim().isEmpty()) {
                    if (!nhomId.equalsIgnoreCase(kh.getNhomKhachHangId())) {
                        continue;
                    }
                } else {
                    if (kh.getTenNhomKhachHang() == null || !kh.getTenNhomKhachHang().equalsIgnoreCase(tenNhom)) {
                        continue;
                    }
                }
            }

            if (!kw.isEmpty()) {
                boolean match = (kh.getMaKhach() != null && kh.getMaKhach().toLowerCase().contains(kw)) ||
                        (kh.getName() != null && kh.getName().toLowerCase().contains(kw)) ||
                        (kh.getDiaChi() != null && kh.getDiaChi().toLowerCase().contains(kw)) ||
                        (kh.getDienThoai() != null && kh.getDienThoai().toLowerCase().contains(kw)) ||
                        (kh.getEmail() != null && kh.getEmail().toLowerCase().contains(kw)) ||
                        (kh.getMaSoThue() != null && kh.getMaSoThue().toLowerCase().contains(kw)) ||
                        (kh.getTenNhanVien() != null && kh.getTenNhanVien().toLowerCase().contains(kw));
                if (!match) continue;
            }

            result.add(kh);
        }
        return result;
    }

    public List<KhachHang> searchByGroupSet(String keyword, Set<String> groupIds, Set<String> groupNames) {
        List<KhachHang> list = getAll();
        List<KhachHang> result = new ArrayList<>();
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";

        for (KhachHang kh : list) {
            boolean groupMatch = false;
            if (groupIds != null && kh.getNhomKhachHangId() != null && groupIds.contains(kh.getNhomKhachHangId())) {
                groupMatch = true;
            }
            if (!groupMatch && groupNames != null && kh.getTenNhomKhachHang() != null && groupNames.contains(kh.getTenNhomKhachHang())) {
                groupMatch = true;
            }
            if (!groupMatch) continue;

            if (!kw.isEmpty()) {
                boolean match = (kh.getMaKhach() != null && kh.getMaKhach().toLowerCase().contains(kw)) ||
                        (kh.getName() != null && kh.getName().toLowerCase().contains(kw)) ||
                        (kh.getDiaChi() != null && kh.getDiaChi().toLowerCase().contains(kw)) ||
                        (kh.getDienThoai() != null && kh.getDienThoai().toLowerCase().contains(kw)) ||
                        (kh.getEmail() != null && kh.getEmail().toLowerCase().contains(kw)) ||
                        (kh.getMaSoThue() != null && kh.getMaSoThue().toLowerCase().contains(kw)) ||
                        (kh.getTenNhanVien() != null && kh.getTenNhanVien().toLowerCase().contains(kw));
                if (!match) continue;
            }

            result.add(kh);
        }
        return result;
    }

    public String getNextMaKhach() {
        int max = 0;
        for (KhachHang kh : getAll()) {
            if (kh.getMaKhach() != null && kh.getMaKhach().toUpperCase().startsWith("KH")) {
                try {
                    int num = Integer.parseInt(kh.getMaKhach().substring(2).trim());
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("KH%05d", max + 1);
    }

    public boolean existsByMaKhach(String maKhach, String excludeId) {
        if (maKhach == null || maKhach.trim().isEmpty()) return false;
        String check = maKhach.trim().toLowerCase();
        for (KhachHang kh : getAll()) {
            if (excludeId != null && excludeId.equals(kh.getId())) continue;
            if (kh.getMaKhach() != null && kh.getMaKhach().trim().equalsIgnoreCase(check)) {
                return true;
            }
        }
        return false;
    }

    public KhachHang findByMaKhach(String maKhach) {
        if (maKhach == null || maKhach.trim().isEmpty()) return null;
        String check = maKhach.trim().toLowerCase();
        for (KhachHang kh : getAll()) {
            if (kh.getMaKhach() != null && kh.getMaKhach().trim().equalsIgnoreCase(check)) {
                return kh;
            }
        }
        return null;
    }

    public Map<String, String> getAllNhanVienMap() {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            NhanVienDAO nvDAO = new NhanVienDAO();
            for (kazuko.ex.model.danhmuc.NhanVien nv : nvDAO.getAllActive()) {
                if (!nv.isFolder() && !nv.isSeparator() && nv.getName() != null && !nv.getName().trim().isEmpty()) {
                    map.put(nv.getId(), nv.getName().trim());
                }
            }
            if (!map.isEmpty()) {
                return map;
            }
        } catch (Exception ignored) {}

        String sql = "SELECT ID, NAME, ITEMTYPE FROM DNHANVIEN WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Integer itemType = rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : null;
                if (itemType != null && (itemType == 1 || itemType == 2)) {
                    continue;
                }
                String name = rs.getString("NAME");
                if (name != null && !name.trim().isEmpty()) {
                    map.put(rs.getString("ID"), name.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getAllNhanVienMap: " + e.getMessage());
        }
        return map;
    }

    public Map<String, String> getAllTinhThanhMap() {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT ID, NAME FROM DTINHTHANH WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("ID"), rs.getString("NAME"));
            }
        } catch (Exception e) {
            System.err.println("Lỗi getAllTinhThanhMap: " + e.getMessage());
        }
        return map;
    }

    private List<KhachHang> tryLoadFromDb(int statusFilter) {
        List<KhachHang> list = new ArrayList<>();
        String sql = "SELECT k.ID, k.MAKHACH, k.NAME, k.DIACHI, k.DIENTHOAI, k.EMAIL, k.NGAYSINH, " +
                     "k.DIEMTICHLUYBANDAU, k.GIABAN, k.MASOTHUE, k.DNHANVIENID, nv.NAME as TENNHANVIEN, " +
                     "k.DNHOMKHACHHANGID, n.NAME as TENNHOM, k.DTINHTHANHID, tt.NAME as TENTINHTHANH, " +
                     "k.FACEBOOK, k.NOTE, k.STATUS, k.TIMECREATED, k.USERCREATEDID, k.TIMEMODIFIED, k.USERMODIFIEDID, " +
                     "u1.NAME as USERCREATEDNAME, u2.NAME as USERMODIFIEDNAME " +
                     "FROM DKHACHHANG k " +
                     "LEFT JOIN DNHOMKHACHHANG n ON k.DNHOMKHACHHANGID = n.ID " +
                     "LEFT JOIN DNHANVIEN nv ON k.DNHANVIENID = nv.ID " +
                     "LEFT JOIN DTINHTHANH tt ON k.DTINHTHANHID = tt.ID " +
                     "LEFT JOIN SUSER u1 ON k.USERCREATEDID = u1.ID " +
                     "LEFT JOIN SUSER u2 ON k.USERMODIFIEDID = u2.ID " +
                     "WHERE " + (statusFilter >= 0 ? "(k.STATUS IS NULL OR k.STATUS >= 0)" : "k.STATUS < 0") + " " +
                     "ORDER BY k.MAKHACH, k.NAME";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setId(rs.getString("ID"));
                kh.setMaKhach(rs.getString("MAKHACH"));
                kh.setName(rs.getString("NAME"));
                kh.setDiaChi(rs.getString("DIACHI"));
                kh.setDienThoai(rs.getString("DIENTHOAI"));
                kh.setEmail(rs.getString("EMAIL"));
                kh.setNgaySinhNhat(rs.getTimestamp("NGAYSINH"));
                kh.setDiemTichLuyBanDau(rs.getInt("DIEMTICHLUYBANDAU"));
                kh.setDiemTichLuy(kh.getDiemTichLuyBanDau());
                kh.setGiaBanIndex(rs.getInt("GIABAN"));
                kh.setMaSoThue(rs.getString("MASOTHUE"));
                kh.setNhanVienId(rs.getString("DNHANVIENID"));
                kh.setTenNhanVien(rs.getString("TENNHANVIEN"));
                kh.setNhomKhachHangId(rs.getString("DNHOMKHACHHANGID"));
                kh.setTenNhomKhachHang(rs.getString("TENNHOM"));
                kh.setTinhThanh(rs.getString("TENTINHTHANH") != null ? rs.getString("TENTINHTHANH") : rs.getString("DTINHTHANHID"));
                kh.setFacebook(rs.getString("FACEBOOK"));
                kh.setGhiChu(rs.getString("NOTE"));
                kh.setStatus(rs.getInt("STATUS"));
                kh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                kh.setUserCreatedId(rs.getString("USERCREATEDID"));
                kh.setUserCreatedName(rs.getString("USERCREATEDNAME") != null ? rs.getString("USERCREATEDNAME") : rs.getString("USERCREATEDID"));
                kh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                kh.setUserModifiedId(rs.getString("USERMODIFIEDID"));
                kh.setUserModifiedName(rs.getString("USERMODIFIEDNAME") != null ? rs.getString("USERMODIFIEDNAME") : rs.getString("USERMODIFIEDID"));

                list.add(kh);
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb DKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private boolean tryInsertToDb(KhachHang kh) {
        String sql = "INSERT INTO DKHACHHANG (" +
                     "ID, MAKHACH, NAME, DIACHI, DIENTHOAI, EMAIL, NGAYSINH, " +
                     "DIEMTICHLUYBANDAU, GIABAN, MASOTHUE, DNHANVIENID, DNHOMKHACHHANGID, " +
                     "DTINHTHANHID, FACEBOOK, NOTE, STATUS, TIMECREATED, USERCREATEDID" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getId());
            ps.setString(2, kh.getMaKhach());
            ps.setString(3, kh.getName());
            ps.setString(4, kh.getDiaChi());
            ps.setString(5, kh.getDienThoai());
            ps.setString(6, kh.getEmail());
            ps.setTimestamp(7, kh.getNgaySinhNhat() != null ? new Timestamp(kh.getNgaySinhNhat().getTime()) : null);
            ps.setInt(8, (int) kh.getDiemTichLuyBanDau());
            ps.setInt(9, kh.getGiaBanIndex());
            ps.setString(10, kh.getMaSoThue());
            ps.setString(11, kh.getNhanVienId());
            ps.setString(12, kh.getNhomKhachHangId());
            ps.setString(13, resolveTinhThanhId(conn, kh.getTinhThanh()));
            ps.setString(14, kh.getFacebook());
            ps.setString(15, kh.getGhiChu());
            ps.setString(16, getEffectiveUserId());

            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryInsertToDb DKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryUpdateToDb(KhachHang kh) {
        String sql = "UPDATE DKHACHHANG SET " +
                     "MAKHACH=?, NAME=?, DIACHI=?, DIENTHOAI=?, EMAIL=?, NGAYSINH=?, " +
                     "DIEMTICHLUYBANDAU=?, GIABAN=?, MASOTHUE=?, DNHANVIENID=?, DNHOMKHACHHANGID=?, " +
                     "DTINHTHANHID=?, FACEBOOK=?, NOTE=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? " +
                     "WHERE ID=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getMaKhach());
            ps.setString(2, kh.getName());
            ps.setString(3, kh.getDiaChi());
            ps.setString(4, kh.getDienThoai());
            ps.setString(5, kh.getEmail());
            ps.setTimestamp(6, kh.getNgaySinhNhat() != null ? new Timestamp(kh.getNgaySinhNhat().getTime()) : null);
            ps.setInt(7, (int) kh.getDiemTichLuyBanDau());
            ps.setInt(8, kh.getGiaBanIndex());
            ps.setString(9, kh.getMaSoThue());
            ps.setString(10, kh.getNhanVienId());
            ps.setString(11, kh.getNhomKhachHangId());
            ps.setString(12, resolveTinhThanhId(conn, kh.getTinhThanh()));
            ps.setString(13, kh.getFacebook());
            ps.setString(14, kh.getGhiChu());
            ps.setString(15, getEffectiveUserId());
            ps.setString(16, kh.getId());

            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryUpdateToDb DKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String resolveTinhThanhId(Connection conn, String tinhThanhNameOrId) {
        if (tinhThanhNameOrId == null || tinhThanhNameOrId.trim().isEmpty()) return null;
        String val = tinhThanhNameOrId.trim();
        String sql = "SELECT ID FROM DTINHTHANH WHERE ID = ? OR UPPER(NAME) = UPPER(?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, val);
            ps.setString(2, val);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ID");
                }
            }
        } catch (Exception ignored) {}
        return val;
    }

    private boolean trySetStatusDb(String id, int status) {
        String sql = "UPDATE DKHACHHANG SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi trySetStatusDb DKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DKHACHHANG WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryDeleteFromDb DKHACHHANG: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
