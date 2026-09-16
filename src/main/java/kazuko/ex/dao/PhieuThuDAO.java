package kazuko.ex.dao;

import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.VoucherNumberGenerator;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class PhieuThuDAO extends BaseDAO {

    private static final List<PhieuThu> MEMORY_PHIEU_THU = new CopyOnWriteArrayList<>();
    private static boolean tableChecked = false;

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_PHIEU_THU.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            PhieuThu pt1 = new PhieuThu();
            pt1.setId("pt-001");
            pt1.setSoPhieu("PT26/00001");
            pt1.setNgay(sdf.parse("28/08/2026 14:40"));
            pt1.setTimeCreated(sdf.parse("28/08/2026 14:40"));
            pt1.setUserCreatedId("Administrator");
            pt1.setTenDoiTuong("Nguyễn Tú Oanh");
            pt1.setDiaChi("");
            pt1.setPhanLoaiId("tc-7");
            pt1.setTenPhanLoai("Nạp thẻ trả trước");
            pt1.setLyDoThu("Lí do thu hah...");
            pt1.setChungTuGoc("ok");
            pt1.setSoTien(10000.0);
            pt1.setGhiChu("note");
            pt1.setLoaiDoiTuong("Đối tượng khác");
            pt1.setStatus(1);
            PhieuThu pt2 = new PhieuThu();
            pt2.setId("pt-002");
            pt2.setSoPhieu("PT26/00002");
            pt2.setNgay(sdf.parse("07/09/2026 09:15"));
            pt2.setTimeCreated(sdf.parse("07/09/2026 09:15"));
            pt2.setUserCreatedId("Administrator");
            pt2.setTenDoiTuong("Ánh");
            pt2.setDiaChi("Lâm Hà gần TH hường nhung");
            pt2.setPhanLoaiId("tc-11");
            pt2.setTenPhanLoai("Thu công nợ");
            pt2.setLyDoThu("Thu công nợ");
            pt2.setChungTuGoc("");
            pt2.setSoTien(500000.0);
            pt2.setGhiChu("thu");
            pt2.setLoaiDoiTuong("Khách hàng");
            pt2.setStatus(1);
            MEMORY_PHIEU_THU.add(pt2);
        } catch (Exception ignored) {}
    }


    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    private synchronized void ensureTableExists(Connection conn) {
        if (tableChecked) return;
        tableChecked = true;
        try {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getTables(null, null, "TPHIEUTHU", new String[]{"TABLE"})) {
                boolean exists = rs.next();
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
            }
        } catch (Exception e) {
            // Fallback gracefully to memory mode
        }
    }

    public synchronized String generateNextSoPhieu(Date date) {
        if (date == null) date = new Date();
        VoucherNumberGenerator.CompiledPattern numberPattern = VoucherNumberGenerator.fromConfig(
                "Phiếu thu", "PT(yy)/(*****)", date);

        long maxSeq = 0;

        // Check DB
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String sql = "SELECT SOPHIEU FROM TPHIEUTHU WHERE SOPHIEU LIKE ? ORDER BY SOPHIEU DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, numberPattern.getSqlLikePattern());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String sp = rs.getString(1);
                        maxSeq = Math.max(maxSeq, numberPattern.extractSequence(sp));
                    }
                }
            }
        } catch (Exception ignored) {}

        // Check Memory
        for (PhieuThu pt : MEMORY_PHIEU_THU) {
            maxSeq = Math.max(maxSeq, numberPattern.extractSequence(pt.getSoPhieu()));
        }

        try {
            return numberPattern.format(maxSeq + 1);
        } catch (IllegalStateException e) {
            return "PT" + new SimpleDateFormat("yy").format(date) + "/" + (maxSeq + 1);
        }
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
            ensureTableExists(conn);
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
        audit("Phiếu thu", "THÊM", pt.getSoPhieu(),
                "Lập phiếu thu; đối tượng: " + pt.getTenDoiTuong() + "; số tiền: " + Math.round(pt.getSoTien()));
        return true;
    }

    public boolean update(PhieuThu pt) {
        if (pt == null || pt.getId() == null) return false;

        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
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
                audit("Phiếu thu", "SỬA", pt.getSoPhieu(),
                        "Cập nhật phiếu thu; số tiền: " + Math.round(pt.getSoTien()));
                return true;
            }
        }
        MEMORY_PHIEU_THU.add(pt);
        audit("Phiếu thu", "SỬA", pt.getSoPhieu(),
                "Cập nhật phiếu thu; số tiền: " + Math.round(pt.getSoTien()));
        return true;
    }

    public boolean delete(String id) {
        if (id == null) return false;
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String sql = "UPDATE TPHIEUTHU SET STATUS = -1 WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (PhieuThu pt : MEMORY_PHIEU_THU) {
            if (id.equals(pt.getId())) {
                pt.setStatus(-1);
                audit("Phiếu thu", "XÓA", pt.getSoPhieu(), "Xóa phiếu thu: " + pt.getSoPhieu());
                return true;
            }
        }
        audit("Phiếu thu", "XÓA", id, "Xóa phiếu thu ID: " + id);
        return true;
    }

    public boolean restore(String id) {
        if (id == null) return false;
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String sql = "UPDATE TPHIEUTHU SET STATUS = 1 WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (PhieuThu pt : MEMORY_PHIEU_THU) {
            if (id.equals(pt.getId())) {
                pt.setStatus(1);
                audit("Phiếu thu", "KHÔI PHỤC", pt.getSoPhieu(), "Khôi phục phiếu thu: " + pt.getSoPhieu());
                return true;
            }
        }
        audit("Phiếu thu", "KHÔI PHỤC", id, "Khôi phục phiếu thu ID: " + id);
        return true;
    }

    public List<PhieuThu> getTrashItems() {
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String sql = "SELECT * FROM TPHIEUTHU WHERE STATUS = -1 ORDER BY NGAY DESC";
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

        for (PhieuThu m : MEMORY_PHIEU_THU) {
            if (m.getStatus() == -1 && list.stream().noneMatch(x -> x.getId().equals(m.getId()))) {
                list.add(m);
            }
        }
        return list;
    }

    public List<PhieuThu> getAll() {
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
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

    public List<PhieuThu> getByKhachHangId(String khachHangId) {
        if (khachHangId == null || khachHangId.trim().isEmpty()) return new ArrayList<>();
        List<PhieuThu> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            String sql = "SELECT * FROM TPHIEUTHU WHERE DKHACHHANGID = ? AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, khachHangId.trim());
                try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (Exception ignored) {}

        for (PhieuThu m : MEMORY_PHIEU_THU) {
            if (khachHangId.trim().equals(m.getKhachHangId()) && m.getStatus() >= 0
                    && list.stream().noneMatch(x -> Objects.equals(x.getId(), m.getId()))) {
                list.add(m);
            }
        }
        return list;
    }

    public List<PhieuThu> getReportPhieuThuTheoNgay(Date fromDate, Date toDate, String lyDoThuChiId, String cuaHangId) {
        List<PhieuThu> list = new ArrayList<>();

        Calendar calFrom = Calendar.getInstance();
        if (fromDate != null) {
            calFrom.setTime(fromDate);
            calFrom.set(Calendar.HOUR_OF_DAY, 0);
            calFrom.set(Calendar.MINUTE, 0);
            calFrom.set(Calendar.SECOND, 0);
            calFrom.set(Calendar.MILLISECOND, 0);
        } else {
            calFrom.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        }

        Calendar calTo = Calendar.getInstance();
        if (toDate != null) {
            calTo.setTime(toDate);
            calTo.set(Calendar.HOUR_OF_DAY, 23);
            calTo.set(Calendar.MINUTE, 59);
            calTo.set(Calendar.SECOND, 59);
            calTo.set(Calendar.MILLISECOND, 999);
        } else {
            calTo.set(2099, Calendar.DECEMBER, 31, 23, 59, 59);
        }

        Timestamp tsFrom = new Timestamp(calFrom.getTimeInMillis());
        Timestamp tsTo = new Timestamp(calTo.getTimeInMillis());

        try (Connection conn = getConnection()) {
            ensureTableExists(conn);
            StringBuilder sql = new StringBuilder("SELECT * FROM TPHIEUTHU WHERE STATUS >= 0 AND NGAY >= ? AND NGAY <= ?");
            List<Object> params = new ArrayList<>();
            params.add(tsFrom);
            params.add(tsTo);

            if (lyDoThuChiId != null && !lyDoThuChiId.trim().isEmpty() && !lyDoThuChiId.equalsIgnoreCase("ALL")) {
                sql.append(" AND (DLYDOTHUCHIID = ? OR TENPHANLOAI = ? OR LYDOTHU = ?)");
                params.add(lyDoThuChiId.trim());
                params.add(lyDoThuChiId.trim());
                params.add(lyDoThuChiId.trim());
            }

            if (cuaHangId != null && !cuaHangId.trim().isEmpty() && !cuaHangId.equalsIgnoreCase("ALL")) {
                sql.append(" AND DCUAHANGID = ?");
                params.add(cuaHangId.trim());
            }

            sql.append(" ORDER BY NGAY ASC, SOPHIEU ASC");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (Exception ignored) {}

        // Memory merge
        for (PhieuThu m : MEMORY_PHIEU_THU) {
            if (m.getStatus() >= 0 && list.stream().noneMatch(x -> Objects.equals(x.getId(), m.getId()))) {
                Date d = m.getNgay();
                if (d != null && d.after(calFrom.getTime()) && d.before(calTo.getTime())) {
                    boolean matchLyDo = (lyDoThuChiId == null || lyDoThuChiId.trim().isEmpty() || lyDoThuChiId.equalsIgnoreCase("ALL")
                            || Objects.equals(lyDoThuChiId, m.getPhanLoaiId()) || Objects.equals(lyDoThuChiId, m.getTenPhanLoai()) || Objects.equals(lyDoThuChiId, m.getLyDoThu()));
                    boolean matchCuaHang = (cuaHangId == null || cuaHangId.trim().isEmpty() || cuaHangId.equalsIgnoreCase("ALL")
                            || Objects.equals(cuaHangId, m.getCuaHangId()));
                    if (matchLyDo && matchCuaHang) {
                        list.add(m);
                    }
                }
            }
        }

        list.sort(Comparator.comparing((PhieuThu p) -> p.getNgay() != null ? p.getNgay() : new Date())
                .thenComparing(p -> p.getSoPhieu() != null ? p.getSoPhieu() : ""));

        return list;
    }
}

