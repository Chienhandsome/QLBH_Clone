package kazuko.ex.dao;

import kazuko.ex.model.quy.PhieuChi;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhieuChiDAO extends BaseDAO {

    private static final List<PhieuChi> MEMORY_PHIEU_CHI = new CopyOnWriteArrayList<>();
    private static boolean tableChecked = false;

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_PHIEU_CHI.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            PhieuChi pc1 = new PhieuChi();
            pc1.setId("pc-001");
            pc1.setSoPhieu("PC26/00001");
            pc1.setNgay(sdf.parse("28/08/2026 15:30"));
            pc1.setTimeCreated(sdf.parse("28/08/2026 15:30"));
            pc1.setUserCreatedId("Administrator");
            pc1.setTenDoiTuong("Công ty TNHH Tiếp Thị Số");
            pc1.setDiaChi("Hà Nội");
            pc1.setPhanLoaiId("tc-1");
            pc1.setTenPhanLoai("Chi phí quản lý");
            pc1.setLyDoChi("Chi tiền văn phòng phẩm và tiếp khách");
            pc1.setChungTuGoc("HĐ 0012");
            pc1.setSoTien(500000.0);
            pc1.setGhiChu("Đã duyệt");
            pc1.setLoaiDoiTuong("Nhà cung cấp");
            pc1.setStatus(1);
            MEMORY_PHIEU_CHI.add(pc1);
        } catch (Exception ignored) {}
    }

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
            ResultSet rs = md.getTables(null, null, "TPHIEUCHI", new String[]{"TABLE"});
            boolean exists = rs.next();
            rs.close();
            if (!exists) {
                String ddl = "CREATE TABLE TPHIEUCHI (" +
                        "ID VARCHAR(50) NOT NULL PRIMARY KEY, " +
                        "SOPHIEU VARCHAR(50), " +
                        "NGAY TIMESTAMP, " +
                        "DLYDOTHUCHIID VARCHAR(50), " +
                        "TENPHANLOAI VARCHAR(255), " +
                        "LYDOCHI VARCHAR(500), " +
                        "CHUNGTUGOC VARCHAR(255), " +
                        "LOAIDOITUONG VARCHAR(50), " +
                        "TENDOITUONG VARCHAR(255), " +
                        "DIACHI VARCHAR(500), " +
                        "DNHANVIENID VARCHAR(50), " +
                        "DKHACHHANGID VARCHAR(50), " +
                        "DNHACUNGCAPID VARCHAR(50), " +
                        "SOTIEN DOUBLE PRECISION, " +
                        "CHUYENTUTAIKHOAN SMALLINT DEFAULT 0, " +
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
        String prefix = "PC" + yearStr + "/";

        int maxSeq = 0;

        // Check DB
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "SELECT SOPHIEU FROM TPHIEUCHI WHERE SOPHIEU LIKE ? ORDER BY SOPHIEU DESC";
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
        for (PhieuChi pc : MEMORY_PHIEU_CHI) {
            if (pc.getSoPhieu() != null && pc.getSoPhieu().startsWith(prefix)) {
                try {
                    String numPart = pc.getSoPhieu().substring(prefix.length());
                    int val = Integer.parseInt(numPart);
                    if (val > maxSeq) maxSeq = val;
                } catch (Exception ignored) {}
            }
        }

        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public boolean insert(PhieuChi pc) {
        if (pc == null) return false;
        if (pc.getId() == null || pc.getId().trim().isEmpty()) {
            pc.setId(UUID.randomUUID().toString());
        }
        if (pc.getSoPhieu() == null || pc.getSoPhieu().trim().isEmpty()) {
            pc.setSoPhieu(generateNextSoPhieu(pc.getNgay()));
        }
        if (pc.getTimeCreated() == null) {
            pc.setTimeCreated(new Date());
        }
        pc.setUserCreatedId(getEffectiveUserId());
        pc.setStatus(1);

        // Try DB
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "INSERT INTO TPHIEUCHI (" +
                    "ID, SOPHIEU, NGAY, DLYDOTHUCHIID, TENPHANLOAI, LYDOCHI, CHUNGTUGOC, " +
                    "LOAIDOITUONG, TENDOITUONG, DIACHI, DNHANVIENID, DKHACHHANGID, DNHACUNGCAPID, " +
                    "SOTIEN, CHUYENTUTAIKHOAN, DTAIKHOANNGANHANGID, DCUAHANGID, KHONGTHAYDOICONGNO, " +
                    "NOTE, STATUS, TIMECREATED, USERCREATEDID" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pc.getId());
                ps.setString(2, pc.getSoPhieu());
                ps.setTimestamp(3, pc.getNgay() != null ? new Timestamp(pc.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(4, pc.getPhanLoaiId());
                ps.setString(5, pc.getTenPhanLoai());
                ps.setString(6, pc.getLyDoChi());
                ps.setString(7, pc.getChungTuGoc());
                ps.setString(8, pc.getLoaiDoiTuong());
                ps.setString(9, pc.getTenDoiTuong());
                ps.setString(10, pc.getDiaChi());
                ps.setString(11, pc.getNhanVienId());
                ps.setString(12, pc.getKhachHangId());
                ps.setString(13, pc.getNhaCungCapId());
                ps.setDouble(14, pc.getSoTien());
                ps.setInt(15, pc.isChuyenTuTaiKhoan() ? 1 : 0);
                ps.setString(16, pc.getTaiKhoanNganHangId());
                ps.setString(17, pc.getCuaHangId());
                ps.setInt(18, pc.isKhongThayDoiCongNo() ? 1 : 0);
                ps.setString(19, pc.getGhiChu());
                ps.setInt(20, pc.getStatus());
                ps.setTimestamp(21, new Timestamp(pc.getTimeCreated().getTime()));
                ps.setString(22, pc.getUserCreatedId());

                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception e) {
            System.err.println("Info: Lưu DB TPHIEUCHI không thành công, lưu vào bộ nhớ cache: " + e.getMessage());
        }

        // Memory cache
        MEMORY_PHIEU_CHI.removeIf(p -> p.getId().equals(pc.getId()));
        MEMORY_PHIEU_CHI.add(pc);
        return true;
    }

    public boolean update(PhieuChi pc) {
        if (pc == null || pc.getId() == null) return false;

        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "UPDATE TPHIEUCHI SET " +
                    "SOPHIEU=?, NGAY=?, DLYDOTHUCHIID=?, TENPHANLOAI=?, LYDOCHI=?, CHUNGTUGOC=?, " +
                    "LOAIDOITUONG=?, TENDOITUONG=?, DIACHI=?, DNHANVIENID=?, DKHACHHANGID=?, DNHACUNGCAPID=?, " +
                    "SOTIEN=?, CHUYENTUTAIKHOAN=?, DTAIKHOANNGANHANGID=?, DCUAHANGID=?, KHONGTHAYDOICONGNO=?, " +
                    "NOTE=?, STATUS=? WHERE ID=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pc.getSoPhieu());
                ps.setTimestamp(2, pc.getNgay() != null ? new Timestamp(pc.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(3, pc.getPhanLoaiId());
                ps.setString(4, pc.getTenPhanLoai());
                ps.setString(5, pc.getLyDoChi());
                ps.setString(6, pc.getChungTuGoc());
                ps.setString(7, pc.getLoaiDoiTuong());
                ps.setString(8, pc.getTenDoiTuong());
                ps.setString(9, pc.getDiaChi());
                ps.setString(10, pc.getNhanVienId());
                ps.setString(11, pc.getKhachHangId());
                ps.setString(12, pc.getNhaCungCapId());
                ps.setDouble(13, pc.getSoTien());
                ps.setInt(14, pc.isChuyenTuTaiKhoan() ? 1 : 0);
                ps.setString(15, pc.getTaiKhoanNganHangId());
                ps.setString(16, pc.getCuaHangId());
                ps.setInt(17, pc.isKhongThayDoiCongNo() ? 1 : 0);
                ps.setString(18, pc.getGhiChu());
                ps.setInt(19, pc.getStatus());
                ps.setString(20, pc.getId());

                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (int i = 0; i < MEMORY_PHIEU_CHI.size(); i++) {
            if (MEMORY_PHIEU_CHI.get(i).getId().equals(pc.getId())) {
                MEMORY_PHIEU_CHI.set(i, pc);
                return true;
            }
        }
        MEMORY_PHIEU_CHI.add(pc);
        return true;
    }

    public List<PhieuChi> getAll() {
        List<PhieuChi> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "SELECT * FROM TPHIEUCHI WHERE STATUS >= 0 ORDER BY NGAY DESC, SOPHIEU DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuChi pc = new PhieuChi();
                    pc.setId(rs.getString("ID"));
                    pc.setSoPhieu(rs.getString("SOPHIEU"));
                    pc.setNgay(rs.getTimestamp("NGAY"));
                    pc.setPhanLoaiId(rs.getString("DLYDOTHUCHIID"));
                    pc.setTenPhanLoai(rs.getString("TENPHANLOAI"));
                    pc.setLyDoChi(rs.getString("LYDOCHI"));
                    pc.setChungTuGoc(rs.getString("CHUNGTUGOC"));
                    pc.setLoaiDoiTuong(rs.getString("LOAIDOITUONG"));
                    pc.setTenDoiTuong(rs.getString("TENDOITUONG"));
                    pc.setDiaChi(rs.getString("DIACHI"));
                    pc.setNhanVienId(rs.getString("DNHANVIENID"));
                    pc.setKhachHangId(rs.getString("DKHACHHANGID"));
                    pc.setNhaCungCapId(rs.getString("DNHACUNGCAPID"));
                    pc.setSoTien(rs.getDouble("SOTIEN"));
                    pc.setChuyenTuTaiKhoan(rs.getInt("CHUYENTUTAIKHOAN") == 1);
                    pc.setTaiKhoanNganHangId(rs.getString("DTAIKHOANNGANHANGID"));
                    pc.setCuaHangId(rs.getString("DCUAHANGID"));
                    pc.setKhongThayDoiCongNo(rs.getInt("KHONGTHAYDOICONGNO") == 1);
                    pc.setGhiChu(rs.getString("NOTE"));
                    pc.setStatus(rs.getInt("STATUS"));
                    pc.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    pc.setUserCreatedId(rs.getString("USERCREATEDID"));
                    list.add(pc);
                }
            }
        } catch (Exception ignored) {}

        // Merge with memory
        for (PhieuChi m : MEMORY_PHIEU_CHI) {
            if (m.getStatus() >= 0 && list.stream().noneMatch(x -> x.getId().equals(m.getId()))) {
                list.add(m);
            }
        }
        return list;
    }

    public PhieuChi findById(String id) {
        if (id == null) return null;
        for (PhieuChi pc : getAll()) {
            if (id.equals(pc.getId())) return pc;
        }
        return null;
    }

    public boolean delete(String id) {
        if (id == null) return false;
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "UPDATE TPHIEUCHI SET STATUS = -1 WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (PhieuChi pc : MEMORY_PHIEU_CHI) {
            if (id.equals(pc.getId())) {
                pc.setStatus(-1);
                return true;
            }
        }
        return true;
    }

    public boolean restore(String id) {
        if (id == null) return false;
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "UPDATE TPHIEUCHI SET STATUS = 1 WHERE ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
            }
        } catch (Exception ignored) {}

        for (PhieuChi pc : MEMORY_PHIEU_CHI) {
            if (id.equals(pc.getId())) {
                pc.setStatus(1);
                return true;
            }
        }
        return true;
    }

    public List<PhieuChi> getTrashItems() {
        List<PhieuChi> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            ensureTableExists();
            String sql = "SELECT * FROM TPHIEUCHI WHERE STATUS = -1 ORDER BY NGAY DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuChi pc = new PhieuChi();
                    pc.setId(rs.getString("ID"));
                    pc.setSoPhieu(rs.getString("SOPHIEU"));
                    pc.setNgay(rs.getTimestamp("NGAY"));
                    pc.setPhanLoaiId(rs.getString("DLYDOTHUCHIID"));
                    pc.setTenPhanLoai(rs.getString("TENPHANLOAI"));
                    pc.setLyDoChi(rs.getString("LYDOCHI"));
                    pc.setChungTuGoc(rs.getString("CHUNGTUGOC"));
                    pc.setLoaiDoiTuong(rs.getString("LOAIDOITUONG"));
                    pc.setTenDoiTuong(rs.getString("TENDOITUONG"));
                    pc.setDiaChi(rs.getString("DIACHI"));
                    pc.setNhanVienId(rs.getString("DNHANVIENID"));
                    pc.setKhachHangId(rs.getString("DKHACHHANGID"));
                    pc.setNhaCungCapId(rs.getString("DNHACUNGCAPID"));
                    pc.setSoTien(rs.getDouble("SOTIEN"));
                    pc.setChuyenTuTaiKhoan(rs.getInt("CHUYENTUTAIKHOAN") == 1);
                    pc.setTaiKhoanNganHangId(rs.getString("DTAIKHOANNGANHANGID"));
                    pc.setCuaHangId(rs.getString("DCUAHANGID"));
                    pc.setKhongThayDoiCongNo(rs.getInt("KHONGTHAYDOICONGNO") == 1);
                    pc.setGhiChu(rs.getString("NOTE"));
                    pc.setStatus(rs.getInt("STATUS"));
                    pc.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    pc.setUserCreatedId(rs.getString("USERCREATEDID"));
                    list.add(pc);
                }
            }
        } catch (Exception ignored) {}

        for (PhieuChi m : MEMORY_PHIEU_CHI) {
            if (m.getStatus() == -1 && list.stream().noneMatch(x -> x.getId().equals(m.getId()))) {
                list.add(m);
            }
        }
        return list;
    }

    public PhieuChi getPhieuChiByOffset(String currentId, int direction) {
        List<PhieuChi> list = getAll();
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
