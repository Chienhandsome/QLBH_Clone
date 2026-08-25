package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatHangDAO extends BaseDAO {
    private static final List<MatHang> MEMORY_PRODUCTS = new CopyOnWriteArrayList<>();

    static {
        // Dữ liệu mẫu phong phú chuẩn 100% theo ảnh giao diện của người dùng
        Object[][] sampleData = {
                {"02B", "Bếp ga 02B", "Bếp Ga", "Chiếc", 638000.02, 170000.0, false, false},
                {"02E", "Bếp ga 02E", "Bếp Ga", "Chiếc", 649000.0, 519200.0, false, false},
                {"05IC-A", "Bếp từ Sato 05IC", "Việt Nhật", "Chiếc", 1083500.0, 866800.0, true, false},
                {"1", "Nồi 01 ANYCOOK", "ANY COOK", "Chiếc", 489500.0, 391600.0, false, false},
                {"1000001", "Bài NI LONG", "Bài", "Hộp", 45000.0, 0.0, false, false},
                {"1000081", "Lẩu cá", "Khaluck", "Chiếc", 682000.0, 545600.0, false, false},
                {"1011", "Bếp từ SANAKY 1011BT", "Sanaky", "Chiếc", 970200.0, 776160.0, false, false},
                {"1012", "Bếp từ SANAKY 1012", "Sanaky", "Chiếc", 970200.0, 776160.0, false, false},
                {"1014", "Bếp từ sanaky AT1014", "Sanaky", "Chiếc", 946000.0, 756800.0, false, false},
                {"1015", "Bếp từ sanaky AT1015", "Sanaky", "Chiếc", 946000.0, 756800.0, false, false},
                {"1016", "Bếp từ sanaky AT1016", "Sanaky", "Chiếc", 572000.0, 457600.0, false, false},
                {"108", "Siêu tốc KL 108", "Khaluck", "Chiếc", 280500.0, 224400.0, false, false},
                {"1080", "Sinh tố Hàn Quốc 1080", "Nissei + Dura", "Chiếc", 770000.0, 616000.0, false, false},
                {"108cl", "Lọc feroli 108 cường lực", "Feroli", "Chiếc", 3520000.0, 2816000.0, false, false},
                {"109CL", "Lọc Feroli 109 Kính cường lực", "Feroli", "Chiếc", 3630000.0, 2904000.0, false, false},
                {"10A", "Quạt Sưởi longer 10A", "Nissei + Dura", "Chiếc", 39600.0, 31680.0, false, false},
                {"10KVA-50", "Máy ổn áp STANDA 10KVA-50", "Standa", "Chiếc", 4565000.0, 3652000.0, false, false},
                {"10KVA-90", "Máy ổn áp STANDA 10KVA-90", "Standa", "Chiếc", 4235000.0, 3388000.0, false, false},
                {"10L", "Nồi shap 10L", "Chi", "Chiếc", 2530000.0, 2024000.0, false, false},
                {"11", "Dây 1*1.0 ht", "Hoatung", "m", 3630.0, 2904.0, false, false},
                {"1100001", "Bàn chải mủ hình chân", "Bàn chải đánh răng", "Cái", 7000.0, 0.0, false, false},
                {"1100002", "Bàn chải mủ rẻ", "Bàn chải đánh răng", "Cái", 4000.0, 0.0, false, false}
        };

        int idx = 1;
        for (Object[] row : sampleData) {
            MatHang mh = new MatHang();
            mh.setId("MH_" + (idx++));
            mh.setCode((String) row[0]);
            mh.setName((String) row[1]);
            mh.setTenNhom((String) row[2]);
            mh.setTenDonViTinh((String) row[3]);
            mh.setGiaBan((Double) row[4]);
            mh.setGiaNhap((Double) row[5]);
            mh.setGiaVon((Double) row[5]);
            mh.setKhongInCheBien((Boolean) row[6]);
            mh.setBanNhanh((Boolean) row[7]);
            mh.setTimeCreated(new Timestamp(System.currentTimeMillis() - 86400000L * 300));
            mh.setUserCreatedName("Administrator");
            mh.setTimeModified(new Timestamp(System.currentTimeMillis() - 86400000L * 50));
            mh.setUserModifiedName("Administrator");
            MEMORY_PRODUCTS.add(mh);
        }
    }

    public List<MatHang> getAll() {
        List<MatHang> dbList = tryLoadFromDb(null, null);
        if (dbList != null) {
            return dbList;
        }
        return new ArrayList<>(MEMORY_PRODUCTS);
    }

    public List<MatHang> getByNhom(String tenNhom, String nhomId) {
        if (tenNhom == null || "Tất cả".equalsIgnoreCase(tenNhom.trim())) {
            return getAll();
        }
        List<MatHang> dbList = tryLoadFromDb(tenNhom, nhomId);
        if (dbList != null) {
            return dbList;
        }
        List<MatHang> filtered = new ArrayList<>();
        for (MatHang mh : MEMORY_PRODUCTS) {
            if ("Chưa thiết lập".equalsIgnoreCase(tenNhom)) {
                if ((mh.getTenNhom() == null || mh.getTenNhom().trim().isEmpty()) &&
                    (mh.getNhomMatHangId() == null || mh.getNhomMatHangId().trim().isEmpty())) {
                    filtered.add(mh);
                }
            } else {
                boolean matchId = nhomId != null && nhomId.equals(mh.getNhomMatHangId());
                boolean matchName = tenNhom.equalsIgnoreCase(mh.getTenNhom());
                if (matchId || matchName) {
                    filtered.add(mh);
                }
            }
        }
        return filtered;
    }

    public List<MatHang> getByNhomIds(Set<String> nhomIds, Set<String> nhomNames) {
        if (nhomIds == null || nhomIds.isEmpty()) {
            return getAll();
        }
        List<MatHang> all = getAll();
        List<MatHang> filtered = new ArrayList<>();
        for (MatHang mh : all) {
            boolean matchId = mh.getNhomMatHangId() != null && nhomIds.contains(mh.getNhomMatHangId());
            boolean matchName = mh.getTenNhom() != null && nhomNames != null && nhomNames.contains(mh.getTenNhom());
            if (matchId || matchName) {
                filtered.add(mh);
            }
        }
        return filtered;
    }

    public void updateNhomName(String nhomId, String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) return;
        for (MatHang mh : MEMORY_PRODUCTS) {
            boolean matchId = nhomId != null && nhomId.equals(mh.getNhomMatHangId());
            boolean matchOldName = oldName != null && oldName.equalsIgnoreCase(mh.getTenNhom());
            if (matchId || matchOldName) {
                mh.setTenNhom(newName);
                if (nhomId != null) {
                    mh.setNhomMatHangId(nhomId);
                }
            }
        }
    }

    public List<MatHang> search(String keyword, String tenNhom) {
        return search(keyword, tenNhom, null);
    }

    public List<MatHang> search(String keyword, String tenNhom, String nhomId) {
        SystemConfig config = new SystemConfigDAO().loadConfig();
        return searchWithConfig(keyword, tenNhom, nhomId, config);
    }

    public List<MatHang> searchWithConfig(String keyword, String tenNhom, String nhomId, SystemConfig config) {
        List<MatHang> baseList = getByNhom(tenNhom, nhomId);
        if (keyword == null || keyword.trim().isEmpty()) {
            return baseList;
        }
        return filterListWithConfig(baseList, keyword.trim(), config);
    }

    public List<MatHang> searchWithConfigByGroupSet(String keyword, Set<String> ids, Set<String> names, SystemConfig config) {
        List<MatHang> baseList = getByNhomIds(ids, names);
        if (keyword == null || keyword.trim().isEmpty()) {
            return baseList;
        }
        return filterListWithConfig(baseList, keyword.trim(), config);
    }

    private List<MatHang> filterListWithConfig(List<MatHang> list, String keyword, SystemConfig config) {
        String timTheo = config != null && config.getTimTheo() != null ? config.getTimTheo() : "Cả tên hàng và mã hàng";
        String cachTim = config != null && config.getCachTim() != null ? config.getCachTim() : "Tìm có chứa";

        List<MatHang> result = new ArrayList<>();
        for (MatHang mh : list) {
            boolean match = false;
            if ("Tên hàng".equalsIgnoreCase(timTheo)) {
                match = matchText(mh.getName(), keyword, cachTim);
            } else if ("Mã hàng".equalsIgnoreCase(timTheo)) {
                match = matchText(mh.getCode(), keyword, cachTim);
            } else {
                // "Cả tên hàng và mã hàng" (kèm cả nhóm hàng)
                match = matchText(mh.getName(), keyword, cachTim)
                        || matchText(mh.getCode(), keyword, cachTim)
                        || matchText(mh.getTenNhom(), keyword, cachTim);
            }
            if (match) {
                result.add(mh);
            }
        }
        return result;
    }

    private boolean matchText(String source, String keyword, String cachTim) {
        if (source == null || keyword == null) return false;
        String s = source.trim().toLowerCase();
        String k = keyword.trim().toLowerCase();

        if ("Tìm bắt đầu".equalsIgnoreCase(cachTim)) {
            return s.startsWith(k);
        } else if ("Tìm chính xác".equalsIgnoreCase(cachTim)) {
            return s.equals(k);
        } else {
            // "Tìm có chứa"
            return s.contains(k);
        }
    }

    public boolean existsByName(String name, String excludeId) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim().toLowerCase();
        for (MatHang mh : getAll()) {
            if (excludeId != null && excludeId.equals(mh.getId())) continue;
            if (mh.getName() != null && mh.getName().trim().equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public MatHang findById(String id) {
        if (id == null) return null;
        for (MatHang mh : getAll()) {
            if (id.equals(mh.getId())) return mh;
        }
        return null;
    }

    public boolean insert(MatHang mh) {
        if (mh == null) return false;
        if (mh.getId() == null || mh.getId().trim().isEmpty()) {
            mh.setId(UUID.randomUUID().toString());
        }
        mh.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        mh.setUserCreatedName(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getName() : "Administrator");
        MEMORY_PRODUCTS.add(0, mh);
        return tryInsertToDb(mh);
    }

    public static char calculateChecksumChar(String numStr) {
        if (numStr == null || numStr.isEmpty()) return '0';
        int sum = 0;
        for (int i = numStr.length() - 1; i >= 0; i--) {
            char c = numStr.charAt(i);
            if (Character.isDigit(c)) {
                int digit = c - '0';
                int weight = ((numStr.length() - 1 - i) % 2 == 0) ? 3 : 1;
                sum += digit * weight;
            }
        }
        int check = (10 - (sum % 10)) % 10;
        return (char) ('0' + check);
    }

    public String generateProductCodeOrBarcode(SystemConfig config, String prefix, boolean isByWeight) {
        if (config == null) {
            return generateNextProductCode(prefix);
        }

        if (config.isTuDongSinhMaVach()) {
            int len = config.getChieuDaiMaVach();
            if (len <= 0) len = 7;
            boolean hasCheck = config.isCoKyTuKiemTra();
            int baseLen = hasCheck ? Math.max(1, len - 1) : len;

            String codePrefix = "";
            if (config.isSuDungTemInBangCanDienTu() && isByWeight) {
                codePrefix = (config.getChuoiBatDauCanDienTu() != null && !config.getChuoiBatDauCanDienTu().trim().isEmpty())
                        ? config.getChuoiBatDauCanDienTu().trim() : "29";
            }

            int numSlots = Math.max(1, baseLen - codePrefix.length());
            long maxNum = 0;
            for (MatHang mh : getAll()) {
                if (mh.getCode() != null) {
                    String c = mh.getCode().trim();
                    if (c.startsWith(codePrefix) && c.matches("\\d+")) {
                        try {
                            String sub = hasCheck && c.length() > codePrefix.length() + 1
                                    ? c.substring(codePrefix.length(), c.length() - 1)
                                    : c.substring(codePrefix.length());
                            long val = Long.parseLong(sub);
                            if (val > maxNum) maxNum = val;
                        } catch (Exception ignored) {}
                    }
                }
            }

            long nextNum = maxNum + 1;
            String formatPattern = "%0" + numSlots + "d";
            String baseCode = codePrefix + String.format(formatPattern, nextNum);
            if (baseCode.length() > baseLen) {
                baseCode = baseCode.substring(baseCode.length() - baseLen);
            }

            if (hasCheck) {
                return baseCode + calculateChecksumChar(baseCode);
            } else {
                return baseCode;
            }
        } else {
            return generateNextProductCode(prefix);
        }
    }

    public String generateNextProductCode(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "MH";
        }
        prefix = prefix.trim();
        String prefixUpper = prefix.toUpperCase();
        int maxIndex = 0;

        for (MatHang mh : getAll()) {
            if (mh.getCode() != null) {
                String code = mh.getCode().trim();
                if (code.toUpperCase().startsWith(prefixUpper)) {
                    String suffix = code.substring(prefix.length()).trim();
                    try {
                        int num = Integer.parseInt(suffix);
                        if (num > maxIndex) {
                            maxIndex = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        int nextIndex = maxIndex + 1;
        return prefix + String.format("%03d", nextIndex);
    }

    public boolean update(MatHang mh) {
        if (mh == null || mh.getId() == null) return false;
        mh.setTimeModified(new Timestamp(System.currentTimeMillis()));
        mh.setUserModifiedName(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getName() : "Administrator");

        boolean memFound = false;
        for (int i = 0; i < MEMORY_PRODUCTS.size(); i++) {
            if (mh.getId().equals(MEMORY_PRODUCTS.get(i).getId())) {
                MEMORY_PRODUCTS.set(i, mh);
                memFound = true;
                break;
            }
        }
        boolean dbOk = tryUpdateToDb(mh);
        return dbOk || memFound;
    }

    private static final java.util.Map<String, MatHang> TRASH_PRODUCTS = new java.util.concurrent.ConcurrentHashMap<>();

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        MatHang found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_PRODUCTS.put(id, found);
        }
        MEMORY_PRODUCTS.removeIf(m -> id.equals(m.getId()));

        String sql = "UPDATE DMATHANG SET STATUS = -1 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        MatHang restored = TRASH_PRODUCTS.remove(id);
        if (restored == null) {
            restored = findByIdInTrash(id);
        }
        if (restored != null) {
            restored.setStatus(1);
            MEMORY_PRODUCTS.add(0, restored);
        }

        String sql = "UPDATE DMATHANG SET STATUS = 30 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        TRASH_PRODUCTS.remove(id);
        MEMORY_PRODUCTS.removeIf(m -> id.equals(m.getId()));

        String sql = "DELETE FROM DMATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public List<MatHang> getTrashProducts() {
        List<MatHang> dbList = tryLoadTrashFromDb();
        java.util.Map<String, MatHang> map = new java.util.LinkedHashMap<>();
        if (dbList != null) {
            for (MatHang mh : dbList) {
                if (mh.getId() != null) {
                    map.put(mh.getId(), mh);
                }
            }
        }
        for (java.util.Map.Entry<String, MatHang> entry : TRASH_PRODUCTS.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return new ArrayList<>(map.values());
    }

    public MatHang findByIdInTrash(String id) {
        if (id == null) return null;
        if (TRASH_PRODUCTS.containsKey(id)) return TRASH_PRODUCTS.get(id);
        for (MatHang mh : getTrashProducts()) {
            if (id.equals(mh.getId())) return mh;
        }
        return null;
    }

    public boolean delete(String id) {
        return moveToTrash(id);
    }

    public boolean deleteByNhom(String tenNhom, String nhomId) {
        if (tenNhom == null || tenNhom.trim().isEmpty()) return false;
        for (MatHang m : MEMORY_PRODUCTS) {
            boolean matchId = nhomId != null && nhomId.equals(m.getNhomMatHangId());
            boolean matchName = tenNhom.equalsIgnoreCase(m.getTenNhom());
            if (matchId || matchName) {
                m.setStatus(-1);
                if (m.getId() != null) {
                    TRASH_PRODUCTS.put(m.getId(), m);
                }
            }
        }
        MEMORY_PRODUCTS.removeIf(m -> {
            boolean matchId = nhomId != null && nhomId.equals(m.getNhomMatHangId());
            boolean matchName = tenNhom.equalsIgnoreCase(m.getTenNhom());
            return matchId || matchName;
        });

        String sqlSoft = "UPDATE DMATHANG SET STATUS = -1 WHERE DNHOMMATHANGID = ? OR DNHOMMATHANGID IN (SELECT ID FROM DNHOMMATHANG WHERE NAME = ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSoft)) {
            ps.setString(1, nhomId != null ? nhomId : "");
            ps.setString(2, tenNhom);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    private List<MatHang> tryLoadTrashFromDb() {
        List<MatHang> list = new ArrayList<>();
        String sql = "SELECT m.ID, m.CODE, m.NAME, m.GIANHAP, m.GIABAN, m.GIAVON, m.DNHOMMATHANGID, m.DDONVITINHID, " +
                     "m.NOTE, m.STATUS, m.TIMECREATED, m.TIMEMODIFIED, " +
                     "n.NAME as TENNHOM, d.NAME as TENDONVITINH " +
                     "FROM DMATHANG m " +
                     "LEFT JOIN DNHOMMATHANG n ON m.DNHOMMATHANGID = n.ID " +
                     "LEFT JOIN DDONVITINH d ON m.DDONVITINHID = d.ID " +
                     "WHERE m.STATUS < 0 " +
                     "ORDER BY m.CODE, m.NAME";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MatHang mh = new MatHang();
                mh.setId(rs.getString("ID"));
                mh.setCode(rs.getString("CODE"));
                mh.setName(rs.getString("NAME"));
                mh.setGiaNhap(rs.getDouble("GIANHAP"));
                mh.setGiaBan(rs.getDouble("GIABAN"));
                mh.setGiaVon(rs.getDouble("GIAVON"));
                mh.setNhomMatHangId(rs.getString("DNHOMMATHANGID"));
                mh.setDonViTinhId(rs.getString("DDONVITINHID"));
                mh.setTenNhom(rs.getString("TENNHOM"));
                mh.setTenDonViTinh(rs.getString("TENDONVITINH"));
                mh.setGhiChu(rs.getString("NOTE"));
                mh.setStatus(-1);
                mh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                mh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                list.add(mh);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private List<MatHang> tryLoadFromDb(String tenNhomFilter, String nhomIdFilter) {
        List<MatHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.ID, m.CODE, m.NAME, m.GIANHAP, m.GIABAN, m.GIAVON, m.DNHOMMATHANGID, m.DDONVITINHID, " +
                "m.NOTE, m.STATUS, m.TIMECREATED, m.TIMEMODIFIED, " +
                "n.NAME as TENNHOM, d.NAME as TENDONVITINH " +
                "FROM DMATHANG m " +
                "LEFT JOIN DNHOMMATHANG n ON m.DNHOMMATHANGID = n.ID " +
                "LEFT JOIN DDONVITINH d ON m.DDONVITINHID = d.ID " +
                "WHERE (m.STATUS IS NULL OR m.STATUS >= 0) "
        );

        if (nhomIdFilter != null && !nhomIdFilter.trim().isEmpty()) {
            sql.append("AND m.DNHOMMATHANGID = ? ");
        } else if (tenNhomFilter != null && !tenNhomFilter.trim().isEmpty()) {
            if ("Chưa thiết lập".equalsIgnoreCase(tenNhomFilter)) {
                sql.append("AND (m.DNHOMMATHANGID IS NULL OR n.NAME IS NULL) ");
            } else if (!"Tất cả".equalsIgnoreCase(tenNhomFilter)) {
                sql.append("AND n.NAME = ? ");
            }
        }
        sql.append("ORDER BY m.CODE, m.NAME");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int pIdx = 1;
            if (nhomIdFilter != null && !nhomIdFilter.trim().isEmpty()) {
                ps.setString(pIdx++, nhomIdFilter);
            } else if (tenNhomFilter != null && !tenNhomFilter.trim().isEmpty() && !"Chưa thiết lập".equalsIgnoreCase(tenNhomFilter) && !"Tất cả".equalsIgnoreCase(tenNhomFilter)) {
                ps.setString(pIdx++, tenNhomFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MatHang mh = new MatHang();
                    mh.setId(rs.getString("ID"));
                    mh.setCode(rs.getString("CODE"));
                    mh.setName(rs.getString("NAME"));
                    mh.setGiaNhap(rs.getDouble("GIANHAP"));
                    mh.setGiaBan(rs.getDouble("GIABAN"));
                    mh.setGiaVon(rs.getDouble("GIAVON"));
                    mh.setNhomMatHangId(rs.getString("DNHOMMATHANGID"));
                    mh.setDonViTinhId(rs.getString("DDONVITINHID"));
                    mh.setTenNhom(rs.getString("TENNHOM"));
                    mh.setTenDonViTinh(rs.getString("TENDONVITINH"));
                    mh.setGhiChu(rs.getString("NOTE"));
                    mh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    mh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    list.add(mh);
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean tryInsertToDb(MatHang mh) {
        String sql = "INSERT INTO DMATHANG (ID, CODE, NAME, GIANHAP, GIABAN, GIAVON, DNHOMMATHANGID, DDONVITINHID, NOTE, STATUS, TIMECREATED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 30, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mh.getId());
            ps.setString(2, mh.getCode());
            ps.setString(3, mh.getName());
            ps.setDouble(4, mh.getGiaNhap());
            ps.setDouble(5, mh.getGiaBan());
            ps.setDouble(6, mh.getGiaVon());
            ps.setString(7, mh.getNhomMatHangId());
            ps.setString(8, mh.getDonViTinhId());
            ps.setString(9, mh.getGhiChu() != null ? mh.getGhiChu() : "");
            ps.setTimestamp(10, mh.getTimeCreated() != null ? mh.getTimeCreated() : new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryUpdateToDb(MatHang mh) {
        String sql = "UPDATE DMATHANG SET CODE=?, NAME=?, GIANHAP=?, GIABAN=?, GIAVON=?, DNHOMMATHANGID=?, DDONVITINHID=?, NOTE=?, TIMEMODIFIED=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mh.getCode());
            ps.setString(2, mh.getName());
            ps.setDouble(3, mh.getGiaNhap());
            ps.setDouble(4, mh.getGiaBan());
            ps.setDouble(5, mh.getGiaVon());
            ps.setString(6, mh.getNhomMatHangId());
            ps.setString(7, mh.getDonViTinhId());
            ps.setString(8, mh.getGhiChu());
            ps.setTimestamp(9, mh.getTimeModified() != null ? mh.getTimeModified() : new Timestamp(System.currentTimeMillis()));
            ps.setString(10, mh.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DMATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
        } catch (Exception ignored) {}

        // Fallback to soft delete
        String sqlSoft = "UPDATE DMATHANG SET STATUS = -1 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSoft)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

