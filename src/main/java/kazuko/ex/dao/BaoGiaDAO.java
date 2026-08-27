package kazuko.ex.dao;

import kazuko.ex.model.baogia.BaoGia;
import kazuko.ex.model.baogia.BaoGiaChiTiet;
import kazuko.ex.model.system.STemplate;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BaoGiaDAO extends BaseDAO {

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    /**
     * Lấy danh sách phiếu báo giá theo bộ lọc
     */
    public List<BaoGia> getDanhSachBaoGia(Date tuNgay, Date denNgay, String soPhieu, String khachHangId) {
        List<BaoGia> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID, NAME, NGAY, DKHACHHANGID, TENKHACH, DIACHI, DIENTHOAI, EMAIL, " +
                "NOTE, TIENHANG, TILEGIAMGIA, TIENGIAMGIA, TILETHUE, TIENTHUE, PHIVANCHUYEN, TONGCONG, LOAI, " +
                "GIAMTHEOTIEN, STATUS, TIMECREATED, USERCREATEDID, TIMEMODIFIED, USERMODIFIEDID " +
                "FROM TBAOGIA WHERE (STATUS >= 0 OR STATUS IS NULL) ");

        if (tuNgay != null) {
            sql.append("AND NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND NGAY <= ? ");
        }
        if (soPhieu != null && !soPhieu.trim().isEmpty()) {
            sql.append("AND LOWER(NAME) LIKE ? ");
        }
        if (khachHangId != null && !khachHangId.trim().isEmpty()) {
            sql.append("AND DKHACHHANGID = ? ");
        }

        sql.append("ORDER BY NGAY DESC, TIMECREATED DESC, NAME DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (tuNgay != null) {
                ps.setTimestamp(idx++, new Timestamp(tuNgay.getTime()));
            }
            if (denNgay != null) {
                // Set to end of day
                long endOfDay = denNgay.getTime() + 86399999L;
                ps.setTimestamp(idx++, new Timestamp(endOfDay));
            }
            if (soPhieu != null && !soPhieu.trim().isEmpty()) {
                ps.setString(idx++, "%" + soPhieu.trim().toLowerCase() + "%");
            }
            if (khachHangId != null && !khachHangId.trim().isEmpty()) {
                ps.setString(idx++, khachHangId.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BaoGia bg = mapResultSetToBaoGia(rs);
                    list.add(bg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách báo giá: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chi tiết một phiếu báo giá theo ID
     */
    public BaoGia getBaoGiaById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT * FROM TBAOGIA WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BaoGia bg = mapResultSetToBaoGia(rs);
                    bg.setChiTietList(getChiTietByBaoGiaId(id, bg.getName()));
                    return bg;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy báo giá theo ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách hàng hóa chi tiết của một phiếu báo giá
     */
    public List<BaoGiaChiTiet> getChiTietByBaoGiaId(String baoGiaId, String soPhieu) {
        List<BaoGiaChiTiet> list = new ArrayList<>();
        if (baoGiaId == null || baoGiaId.trim().isEmpty()) return list;

        String sql = "SELECT c.ID, c.TBAOGIAID, c.DMATHANGID, c.DDONVITINHID, c.SOLUONG, c.DONGIA, " +
                "c.TILEGIAMGIA, c.THANHTIEN, c.NOTE, c.STATUS, c.TIMECREATED, c.USERCREATEDID, " +
                "m.CODE AS MAHANG, m.NAME AS TENHANG, dv.NAME AS TENDONVITINH " +
                "FROM TBAOGIACHITIET c " +
                "LEFT JOIN DMATHANG m ON c.DMATHANGID = m.ID " +
                "LEFT JOIN DDONVITINH dv ON COALESCE(c.DDONVITINHID, m.DDONVITINHID) = dv.ID " +
                "WHERE c.TBAOGIAID = ? AND (c.STATUS >= 0 OR c.STATUS IS NULL) " +
                "ORDER BY c.TIMECREATED ASC, c.ID ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, baoGiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BaoGiaChiTiet ct = new BaoGiaChiTiet();
                    ct.setId(rs.getString("ID"));
                    ct.settBaoGiaId(rs.getString("TBAOGIAID"));
                    ct.setdMatHangId(rs.getString("DMATHANGID"));
                    ct.setdDonViTinhId(rs.getString("DDONVITINHID"));
                    ct.setMaHang(rs.getString("MAHANG"));
                    ct.setTenHang(rs.getString("TENHANG"));
                    ct.setTenDonViTinh(rs.getString("TENDONVITINH"));
                    ct.setSoLuong(rs.getDouble("SOLUONG"));
                    ct.setDonGia(rs.getDouble("DONGIA"));
                    ct.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    ct.setThanhTien(rs.getDouble("THANHTIEN"));
                    ct.setNote(rs.getString("NOTE"));
                    ct.setStatus(rs.getInt("STATUS"));
                    ct.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    ct.setUserCreatedId(rs.getString("USERCREATEDID"));
                    ct.setSoPhieuBaoGia(soPhieu);
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn chi tiết báo giá: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới một phiếu báo giá kèm các dòng chi tiết
     */
    public boolean insertBaoGia(BaoGia bg, List<BaoGiaChiTiet> chiTietList) {
        if (bg == null) return false;
        if (bg.getId() == null || bg.getId().trim().isEmpty()) {
            bg.setId(UUID.randomUUID().toString());
        }
        if (bg.getTimeCreated() == null) {
            bg.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        }

        if (bg.getUserCreatedId() == null || bg.getUserCreatedId().trim().isEmpty()) {
            bg.setUserCreatedId(getEffectiveUserId());
        }
        if (bg.getUserModifiedId() == null || bg.getUserModifiedId().trim().isEmpty()) {
            bg.setUserModifiedId(getEffectiveUserId());
        }

        String sqlMaster = "INSERT INTO TBAOGIA (ID, NAME, NGAY, DKHACHHANGID, TENKHACH, DIACHI, DIENTHOAI, EMAIL, " +
                "NOTE, TIENHANG, TILEGIAMGIA, TIENGIAMGIA, TILETHUE, TIENTHUE, PHIVANCHUYEN, TONGCONG, LOAI, " +
                "GIAMTHEOTIEN, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO TBAOGIACHITIET (ID, TBAOGIAID, DMATHANGID, DDONVITINHID, SOLUONG, DONGIA, " +
                "TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, bg.getId());
                psMaster.setString(2, bg.getName());
                psMaster.setTimestamp(3, bg.getNgay());
                psMaster.setString(4, bg.getdKhachHangId());
                psMaster.setString(5, bg.getTenKhach());
                psMaster.setString(6, bg.getDiaChi());
                psMaster.setString(7, bg.getDienThoai());
                psMaster.setString(8, bg.getEmail());
                psMaster.setString(9, bg.getNote());
                psMaster.setDouble(10, bg.getTienHang());
                psMaster.setDouble(11, bg.getTiLeGiamGia());
                psMaster.setDouble(12, bg.getTienGiamGia());
                psMaster.setDouble(13, bg.getTiLeThue());
                psMaster.setDouble(14, bg.getTienThue());
                psMaster.setDouble(15, bg.getPhiVanChuyen());
                psMaster.setDouble(16, bg.getTongCong());
                psMaster.setInt(17, bg.getLoai());
                psMaster.setInt(18, bg.getGiamTheoTien());
                psMaster.setInt(19, bg.getStatus());
                psMaster.setTimestamp(20, bg.getTimeCreated());
                psMaster.setString(21, bg.getUserCreatedId());
                psMaster.executeUpdate();
            }

            if (chiTietList != null && !chiTietList.isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (BaoGiaChiTiet ct : chiTietList) {
                        String detailId = (ct.getId() != null && !ct.getId().isEmpty()) ? ct.getId() : UUID.randomUUID().toString();
                        ct.setId(detailId);
                        ct.settBaoGiaId(bg.getId());

                        psDetail.setString(1, detailId);
                        psDetail.setString(2, bg.getId());
                        psDetail.setString(3, ct.getdMatHangId());
                        psDetail.setString(4, ct.getdDonViTinhId());
                        psDetail.setDouble(5, ct.getSoLuong());
                        psDetail.setDouble(6, ct.getDonGia());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getNote());
                        psDetail.setInt(10, ct.getStatus());
                        psDetail.setTimestamp(11, ct.getTimeCreated() != null ? ct.getTimeCreated() : bg.getTimeCreated());
                        psDetail.setString(12, ct.getUserCreatedId() != null ? ct.getUserCreatedId() : bg.getUserCreatedId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm mới phiếu báo giá: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật phiếu báo giá và cập nhật lại danh sách chi tiết
     */
    public boolean updateBaoGia(BaoGia bg, List<BaoGiaChiTiet> chiTietList) {
        if (bg == null || bg.getId() == null) return false;
        bg.setTimeModified(new Timestamp(System.currentTimeMillis()));
        if (bg.getUserModifiedId() == null || bg.getUserModifiedId().trim().isEmpty()) {
            bg.setUserModifiedId(getEffectiveUserId());
        }

        String sqlMaster = "UPDATE TBAOGIA SET NAME = ?, NGAY = ?, DKHACHHANGID = ?, TENKHACH = ?, DIACHI = ?, " +
                "DIENTHOAI = ?, EMAIL = ?, NOTE = ?, TIENHANG = ?, TILEGIAMGIA = ?, TIENGIAMGIA = ?, " +
                "TILETHUE = ?, TIENTHUE = ?, PHIVANCHUYEN = ?, TONGCONG = ?, LOAI = ?, GIAMTHEOTIEN = ?, " +
                "TIMEMODIFIED = ?, USERMODIFIEDID = ? WHERE ID = ?";

        String sqlDeleteDetails = "DELETE FROM TBAOGIACHITIET WHERE TBAOGIAID = ?";
        String sqlDetail = "INSERT INTO TBAOGIACHITIET (ID, TBAOGIAID, DMATHANGID, DDONVITINHID, SOLUONG, DONGIA, " +
                "TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, bg.getName());
                psMaster.setTimestamp(2, bg.getNgay());
                psMaster.setString(3, bg.getdKhachHangId());
                psMaster.setString(4, bg.getTenKhach());
                psMaster.setString(5, bg.getDiaChi());
                psMaster.setString(6, bg.getDienThoai());
                psMaster.setString(7, bg.getEmail());
                psMaster.setString(8, bg.getNote());
                psMaster.setDouble(9, bg.getTienHang());
                psMaster.setDouble(10, bg.getTiLeGiamGia());
                psMaster.setDouble(11, bg.getTienGiamGia());
                psMaster.setDouble(12, bg.getTiLeThue());
                psMaster.setDouble(13, bg.getTienThue());
                psMaster.setDouble(14, bg.getPhiVanChuyen());
                psMaster.setDouble(15, bg.getTongCong());
                psMaster.setInt(16, bg.getLoai());
                psMaster.setInt(17, bg.getGiamTheoTien());
                psMaster.setTimestamp(18, bg.getTimeModified());
                psMaster.setString(19, bg.getUserModifiedId());
                psMaster.setString(20, bg.getId());
                psMaster.executeUpdate();
            }

            // Xóa chi tiết cũ và chèn chi tiết mới
            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteDetails)) {
                psDel.setString(1, bg.getId());
                psDel.executeUpdate();
            }

            if (chiTietList != null && !chiTietList.isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (BaoGiaChiTiet ct : chiTietList) {
                        String detailId = (ct.getId() != null && !ct.getId().isEmpty()) ? ct.getId() : UUID.randomUUID().toString();
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, bg.getId());
                        psDetail.setString(3, ct.getdMatHangId());
                        psDetail.setString(4, ct.getdDonViTinhId());
                        psDetail.setDouble(5, ct.getSoLuong());
                        psDetail.setDouble(6, ct.getDonGia());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getNote());
                        psDetail.setInt(10, ct.getStatus());
                        psDetail.setTimestamp(11, ct.getTimeCreated() != null ? ct.getTimeCreated() : bg.getTimeModified());
                        psDetail.setString(12, ct.getUserCreatedId() != null ? ct.getUserCreatedId() : bg.getUserModifiedId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật phiếu báo giá: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Xóa phiếu báo giá
     */
    public boolean deleteBaoGia(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psDelDetails = conn.prepareStatement("DELETE FROM TBAOGIACHITIET WHERE TBAOGIAID = ?")) {
                psDelDetails.setString(1, id);
                psDelDetails.executeUpdate();
            }

            try (PreparedStatement psDelMaster = conn.prepareStatement("DELETE FROM TBAOGIA WHERE ID = ?")) {
                psDelMaster.setString(1, id);
                psDelMaster.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa phiếu báo giá: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Tự sinh số phiếu kế tiếp: BG{YY}/{00000} (vd: BG26/00001)
     */
    public String generateNextSoPhieu(Date ngay) {
        if (ngay == null) ngay = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String year2Digits = sdf.format(ngay);
        String prefix = "BG" + year2Digits + "/";

        String sql = "SELECT NAME FROM TBAOGIA WHERE NAME LIKE ? ORDER BY NAME DESC";
        int maxIndex = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    if (name != null && name.startsWith(prefix)) {
                        String numPart = name.substring(prefix.length()).trim();
                        try {
                            int val = Integer.parseInt(numPart);
                            if (val > maxIndex) {
                                maxIndex = val;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sinh số phiếu báo giá: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxIndex + 1);
    }

    /**
     * Lấy danh sách mẫu in báo giá từ bảng STEMPLATE
     */
    public List<STemplate> getBaoGiaPrintTemplates() {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE FROM STEMPLATE " +
                     "WHERE (STATUS = 30 OR STATUS IS NULL OR STATUS > 0) " +
                     "AND (LOWER(NAME) LIKE '%báo giá%' OR LOWER(NAME) LIKE '%bao gia%' " +
                     "     OR LOWER(NAME) LIKE '%mẫu 80%' OR LOWER(NAME) LIKE '%mẫu a4%' " +
                     "     OR LOWER(NAME) LIKE '%mẫu a5%' OR LOWER(NAME) LIKE '%80mm%' OR LOWER(NAME) LIKE '%a4%') " +
                     "ORDER BY SORTORDER, NAME, AUTOID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                STemplate t = new STemplate();
                t.setId(rs.getString("ID"));
                t.setName(rs.getString("NAME"));
                t.setNote(rs.getString("NOTE"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn mẫu in báo giá: " + e.getMessage());
        }

        // Mẫu mặc định nếu DB chưa có
        if (list.isEmpty()) {
            list.add(new STemplate("1", "Mẫu 80"));
            list.add(new STemplate("2", "Mẫu A4 nằm ngang"));
            list.add(new STemplate("3", "Mẫu A4 thẳng đứng"));
        }
        return list;
    }

    private BaoGia mapResultSetToBaoGia(ResultSet rs) throws SQLException {
        BaoGia bg = new BaoGia();
        bg.setId(rs.getString("ID"));
        bg.setName(rs.getString("NAME"));
        bg.setNgay(rs.getTimestamp("NGAY"));
        bg.setdKhachHangId(rs.getString("DKHACHHANGID"));
        bg.setTenKhach(rs.getString("TENKHACH"));
        bg.setDiaChi(rs.getString("DIACHI"));
        bg.setDienThoai(rs.getString("DIENTHOAI"));
        bg.setEmail(rs.getString("EMAIL"));
        bg.setNote(rs.getString("NOTE"));
        bg.setTienHang(rs.getDouble("TIENHANG"));
        bg.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        bg.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        bg.setTiLeThue(rs.getDouble("TILETHUE"));
        bg.setTienThue(rs.getDouble("TIENTHUE"));
        bg.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN"));
        bg.setTongCong(rs.getDouble("TONGCONG"));
        bg.setLoai(rs.getInt("LOAI"));
        bg.setGiamTheoTien(rs.getInt("GIAMTHEOTIEN"));
        bg.setStatus(rs.getInt("STATUS"));
        bg.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        bg.setUserCreatedId(rs.getString("USERCREATEDID"));
        bg.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        bg.setUserModifiedId(rs.getString("USERMODIFIEDID"));
        return bg;
    }
}
