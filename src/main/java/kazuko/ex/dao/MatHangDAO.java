package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        return new ArrayList<>(MEMORY_PRODUCTS);
    }

    public List<MatHang> getByNhom(String tenNhom, String nhomId) {
        if (tenNhom == null || "Tất cả".equalsIgnoreCase(tenNhom.trim())) {
            return getAll();
        }
        List<MatHang> dbList = tryLoadFromDb(tenNhom, nhomId);
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        List<MatHang> filtered = new ArrayList<>();
        for (MatHang mh : MEMORY_PRODUCTS) {
            if ("Chưa thiết lập".equalsIgnoreCase(tenNhom)) {
                if (mh.getTenNhom() == null || mh.getTenNhom().trim().isEmpty()) {
                    filtered.add(mh);
                }
            } else if (tenNhom.equalsIgnoreCase(mh.getTenNhom())) {
                filtered.add(mh);
            }
        }
        return filtered;
    }

    public List<MatHang> search(String keyword, String tenNhom) {
        List<MatHang> baseList = getByNhom(tenNhom, null);
        if (keyword == null || keyword.trim().isEmpty()) {
            return baseList;
        }
        String kw = keyword.trim().toLowerCase();
        List<MatHang> result = new ArrayList<>();
        for (MatHang mh : baseList) {
            boolean matchCode = mh.getCode() != null && mh.getCode().toLowerCase().contains(kw);
            boolean matchName = mh.getName() != null && mh.getName().toLowerCase().contains(kw);
            boolean matchGroup = mh.getTenNhom() != null && mh.getTenNhom().toLowerCase().contains(kw);
            if (matchCode || matchName || matchGroup) {
                result.add(mh);
            }
        }
        return result;
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

    public boolean delete(String id) {
        if (id == null) return false;
        MEMORY_PRODUCTS.removeIf(m -> id.equals(m.getId()));
        return tryDeleteFromDb(id);
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
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

