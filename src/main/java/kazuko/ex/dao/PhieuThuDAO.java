package kazuko.ex.dao;

import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PhieuThuDAO extends BaseDAO {

    private static final List<PhieuThu> MEMORY_PHIEU_THU = new CopyOnWriteArrayList<>();
    private static boolean tableChecked = false;

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    private synchronized void ensureTableExists() {
        if (tableChecked) return;
        tableChecked = true;
        try (Connection conn = getConnection()) {
            DatabaseMetaData md = conn.getMetaData();
            ResultSet rs = md.getTables(null, null, "TPHIEUTHU", new String[]{"TABLE"});
            boolean exists = rs.next();
            rs.close();
            if (!exists) {
                String ddl = "CREATE TABLE TPHIEUTHU (" +
                        "ID VARCHAR(50) NOT NULL PRIMARY KEY, " +
                        "SOPHIEU VARCHAR(50), " +
                        "NGAY TIMESTAMP, " +
                        "DLYDOTHUCHIID VARCHAR(50), " +
                        "TENPHANLOAI VARCHAR(255), " +
                        "LYDOTHU VARCHAR(500), " +
                        "CHUNGTUGOC VARCHAR(255), " +
                        "LOAIDOITUONG VARCHAR(50), " +
                        "TENDOITUONG VARCHAR(255), " +
                        "DIACHI VARCHAR(500), " +
                        "DNHANVIENID VARCHAR(50), " +
                        "DKHACHHANGID VARCHAR(50), " +
                        "DNHACUNGCAPID VARCHAR(50), " +
                        "SOTIEN DOUBLE PRECISION, " +
                        "CHUYENVAOTAIKHOAN SMALLINT DEFAULT 0, " +
                        "DTAIKHOANNGANHANGID VARCHAR(50), " +
                        "DCUAHANGID VARCHAR(50), " +
                        "KHONGTHAYDOICONGNO SMALLINT DEFAULT 0, " +
                        "NOTE VARCHAR(500), " +
                        "STATUS SMALLINT DEFAULT 1, " +
                        "TIMECREATED TIMESTAMP, " +
                        "USERCREATEDID VARCHAR(50)" +
                        ")";
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(ddl);
                    if (!conn.getAutoCommit()) conn.commit();
                }
            }
        } catch (Exception e) {
            // Fallback gracefully to memory mode
        }
    }

    public synchronized String generateNextSoPhieu(Date date) {
        if (date == null) date = new Date();
        String yearStr = new SimpleDateFormat("yy").format(date);
        String prefix = "PT" + yearStr + "/";

        int maxSeq = 0;

        // Check DB
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "SELECT SOPHIEU FROM TPHIEUTHU WHERE SOPHIEU LIKE ? ORDER BY SOPHIEU DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, prefix + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sp = rs.getString(1);
                        if (sp != null && sp.startsWith(prefix)) {
                            try {
                                String numPart = sp.substring(prefix.length());
                                int val = Integer.parseInt(numPart);
                                if (val > maxSeq) maxSeq = val;
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // Check Memory
        for (PhieuThu pt : MEMORY_PHIEU_THU) {
            if (pt.getSoPhieu() != null && pt.getSoPhieu().startsWith(prefix)) {
                try {
                    String numPart = pt.getSoPhieu().substring(prefix.length());
                    int val = Integer.parseInt(numPart);
                    if (val > maxSeq) maxSeq = val;
                } catch (Exception ignored) {}
            }
        }

        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public boolean insert(PhieuThu pt) {
        if (pt == null) return false;
        if (pt.getId() == null || pt.getId().trim().isEmpty()) {
            pt.setId(UUID.randomUUID().toString());
        }
        if (pt.getSoPhieu() == null || pt.getSoPhieu().trim().isEmpty()) {
            pt.setSoPhieu(generateNextSoPhieu(pt.getNgay()));
        }
        if (pt.getTimeCreated() == null) {
            pt.setTimeCreated(new Date());
        }
        pt.setUserCreatedId(getEffectiveUserId());
        pt.setStatus(1);

        // Try DB
        boolean dbOk = false;
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "INSERT INTO TPHIEUTHU (" +
                    "ID, SOPHIEU, NGAY, DLYDOTHUCHIID, TENPHANLOAI, LYDOTHU, CHUNGTUGOC, " +
                    "LOAIDOITUONG, TENDOITUONG, DIACHI, DNHANVIENID, DKHACHHANGID, DNHACUNGCAPID, " +
                    "SOTIEN, CHUYENVAOTAIKHOAN, DTAIKHOANNGANHANGID, DCUAHANGID, KHONGTHAYDOICONGNO, " +
                    "NOTE, STATUS, TIMECREATED, USERCREATEDID" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pt.getId());
                ps.setString(2, pt.getSoPhieu());
                ps.setTimestamp(3, pt.getNgay() != null ? new Timestamp(pt.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(4, pt.getPhanLoaiId());
                ps.setString(5, pt.getTenPhanLoai());
                ps.setString(6, pt.getLyDoThu());
                ps.setString(7, pt.getChungTuGoc());
                ps.setString(8, pt.getLoaiDoiTuong());
                ps.setString(9, pt.getTenDoiTuong());
                ps.setString(10, pt.getDiaChi());
                ps.setString(11, pt.getNhanVienId());
                ps.setString(12, pt.getKhachHangId());
                ps.setString(13, pt.getNhaCungCapId());
                ps.setDouble(14, pt.getSoTien());
                ps.setInt(15, pt.isChuyenVaoTaiKhoan() ? 1 : 0);
                ps.setString(16, pt.getTaiKhoanNganHangId());
                ps.setString(17, pt.getCuaHangId());
                ps.setInt(18, pt.isKhongThayDoiCongNo() ? 1 : 0);
                ps.setString(19, pt.getGhiChu());
                ps.setInt(20, pt.getStatus());
                ps.setTimestamp(21, new Timestamp(pt.getTimeCreated().getTime()));
                ps.setString(22, pt.getUserCreatedId());

                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                dbOk = (rows > 0);
            }
        } catch (Exception e) {
            System.err.println("Info: Lưu DB TPHIEUTHU không thành công, lưu vào bộ nhớ cache: " + e.getMessage());
        }

        // Memory cache
        MEMORY_PHIEU_THU.removeIf(p -> p.getId().equals(pt.getId()));
        MEMORY_PHIEU_THU.add(pt);
        return true;
    }

    public boolean update(PhieuThu pt) {
        if (pt == null || pt.getId() == null) return false;

        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "UPDATE TPHIEUTHU SET " +
                    "SOPHIEU=?, NGAY=?, DLYDOTHUCHIID=?, TENPHANLOAI=?, LYDOTHU=?, CHUNGTUGOC=?, " +
                    "LOAIDOITUONG=?, TENDOITUONG=?, DIACHI=?, DNHANVIENID=?, DKHACHHANGID=?, DNHACUNGCAPID=?, " +
                    "SOTIEN=?, CHUYENVAOTAIKHOAN=?, DTAIKHOANNGANHANGID=?, DCUAHANGID=?, KHONGTHAYDOICONGNO=?, " +
                    "NOTE=?, STATUS=? WHERE ID=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pt.getSoPhieu());
                ps.setTimestamp(2, pt.getNgay() != null ? new Timestamp(pt.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(3, pt.getPhanLoaiId());
                ps.setString(4, pt.getTenPhanLoai());
                ps.setString(5, pt.getLyDoThu());
                ps.setString(6, pt.getChungTuGoc());
                ps.setString(7, pt.getLoaiDoiTuong());
                ps.setString(8, pt.getTenDoiTuong());
                ps.setString(9, pt.getDiaChi());
                ps.setString(10, pt.getNhanVienId());
                ps.setString(11, pt.getKhachHangId());
                ps.setString(12, pt.getNhaCungCapId());
                ps.setDouble(13, pt.getSoTien());
                ps.setInt(14, pt.isChuyenVaoTaiKhoan() ? 1 : 0);
                ps.setString(15, pt.getTaiKhoanNganHangId());
                ps.setString(16, pt.getCuaHangId());
                ps.setInt(17, pt.isKhongThayDoiCongNo() ? 1 : 0);
                ps.setString(18, pt.getGhiChu());
                ps.setInt(19, pt.getStatus());
                ps.setString(20, pt.getId());

                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (int i = 0; i < MEMORY_PHIEU_THU.size(); i++) {
            if (MEMORY_PHIEU_THU.get(i).getId().equals(pt.getId())) {
                MEMORY_PHIEU_THU.set(i, pt);
                return true;
            }
        }
        MEMORY_PHIEU_THU.add(pt);
        return true;
    }

    public List<PhieuThu> getAll() {
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "SELECT * FROM TPHIEUTHU WHERE STATUS >= 0 ORDER BY NGAY DESC, SOPHIEU DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuThu pt = new PhieuThu();
                    pt.setId(rs.getString("ID"));
                    pt.setSoPhieu(rs.getString("SOPHIEU"));
                    pt.setNgay(rs.getTimestamp("NGAY"));
                    pt.setPhanLoaiId(rs.getString("DLYDOTHUCHIID"));
                    pt.setTenPhanLoai(rs.getString("TENPHANLOAI"));
                    pt.setLyDoThu(rs.getString("LYDOTHU"));
                    pt.setChungTuGoc(rs.getString("CHUNGTUGOC"));
                    pt.setLoaiDoiTuong(rs.getString("LOAIDOITUONG"));
                    pt.setTenDoiTuong(rs.getString("TENDOITUONG"));
                    pt.setDiaChi(rs.getString("DIACHI"));
                    pt.setNhanVienId(rs.getString("DNHANVIENID"));
                    pt.setKhachHangId(rs.getString("DKHACHHANGID"));
                    pt.setNhaCungCapId(rs.getString("DNHACUNGCAPID"));
                    pt.setSoTien(rs.getDouble("SOTIEN"));
                    pt.setChuyenVaoTaiKhoan(rs.getInt("CHUYENVAOTAIKHOAN") == 1);
                    pt.setTaiKhoanNganHangId(rs.getString("DTAIKHOANNGANHANGID"));
                    pt.setCuaHangId(rs.getString("DCUAHANGID"));
                    pt.setKhongThayDoiCongNo(rs.getInt("KHONGTHAYDOICONGNO") == 1);
                    pt.setGhiChu(rs.getString("NOTE"));
                    pt.setStatus(rs.getInt("STATUS"));
                    pt.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    pt.setUserCreatedId(rs.getString("USERCREATEDID"));
                    list.add(pt);
                }
            }
        } catch (Exception ignored) {}

        // Merge with memory
        for (PhieuThu m : MEMORY_PHIEU_THU) {
            if (m.getStatus() >= 0 && list.stream().noneMatch(x -> x.getId().equals(m.getId()))) {
                list.add(m);
            }
        }
        return list;
    }

    public PhieuThu findById(String id) {
        if (id == null) return null;
        for (PhieuThu pt : getAll()) {
            if (id.equals(pt.getId())) return pt;
        }
        return null;
    }

    public PhieuThu getPhieuThuByOffset(String currentId, int direction) {
        List<PhieuThu> list = getAll();
        if (list.isEmpty()) return null;
        if (currentId == null || currentId.trim().isEmpty()) {
            return list.get(0);
        }
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(currentId)) {
                index = i;
                break;
            }
        }
        if (index == -1) return list.get(0);
        int target = index - direction; // direction: -1 is before, +1 is after
        if (target >= 0 && target < list.size()) {
            return list.get(target);
        }
        return null;
    }
}