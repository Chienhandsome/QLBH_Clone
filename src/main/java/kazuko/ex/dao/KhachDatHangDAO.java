package kazuko.ex.dao;

import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.model.system.STemplate;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.VoucherNumberGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class KhachDatHangDAO extends BaseDAO {

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    /**
     * Lấy danh sách phiếu đặt hàng theo bộ lọc
     */
    public List<KhachDatHang> getDanhSachKhachDatHang(Date tuNgay, Date denNgay, String soPhieu, String khachHangId) {
        return getDanhSachKhachDatHang(tuNgay, denNgay, soPhieu, khachHangId, null);
    }

    /**
     * Lấy danh sách phiếu đặt hàng theo bộ lọc nâng cao gồm từ khóa tên khách
     */
    public List<KhachDatHang> getDanhSachKhachDatHang(Date tuNgay, Date denNgay, String soPhieu, String khachHangId, String tenKhachKeyword) {
        List<KhachDatHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ID, NAME, NGAY, DKHACHHANGID, TENKHACH, DIACHI, DIENTHOAI, EMAIL, " +
                "NOTE, TIENHANG, TILEGIAMGIA, TIENGIAMGIA, TILETHUE, TIENTHUE, PHIVANCHUYEN, TONGCONG, LOAI, " +
                "GIAMTHEOTIEN, STATUS, TIMECREATED, USERCREATEDID, TIMEMODIFIED, USERMODIFIEDID " +
                "FROM TDATHANG WHERE (STATUS >= 0 OR STATUS IS NULL) ");

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
        } else if (tenKhachKeyword != null && !tenKhachKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(TENKHACH) LIKE ? OR LOWER(DIENTHOAI) LIKE ? OR LOWER(DIACHI) LIKE ?) ");
        }

        sql.append("ORDER BY NGAY DESC, TIMECREATED DESC, NAME DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (tuNgay != null) {
                ps.setTimestamp(idx++, new Timestamp(tuNgay.getTime()));
            }
            if (denNgay != null) {
                long endOfDay = denNgay.getTime() + 86399999L;
                ps.setTimestamp(idx++, new Timestamp(endOfDay));
            }
            if (soPhieu != null && !soPhieu.trim().isEmpty()) {
                ps.setString(idx++, "%" + soPhieu.trim().toLowerCase() + "%");
            }
            if (khachHangId != null && !khachHangId.trim().isEmpty()) {
                ps.setString(idx++, khachHangId.trim());
            } else if (tenKhachKeyword != null && !tenKhachKeyword.trim().isEmpty()) {
                String kw = "%" + tenKhachKeyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachDatHang dh = mapResultSetToKhachDatHang(rs);
                    list.add(dh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn danh sách đặt hàng: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chi tiết một phiếu đặt hàng theo ID
     */
    public KhachDatHang getKhachDatHangById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT * FROM TDATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KhachDatHang dh = mapResultSetToKhachDatHang(rs);
                    dh.setChiTietList(getChiTietByKhachDatHangId(id, dh.getName()));
                    return dh;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy đặt hàng theo ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách hàng hóa chi tiết của một phiếu đặt hàng
     */
    public List<KhachDatHangChiTiet> getChiTietByKhachDatHangId(String khachDatHangId, String soPhieu) {
        List<KhachDatHangChiTiet> list = new ArrayList<>();
        if (khachDatHangId == null || khachDatHangId.trim().isEmpty()) return list;

        String sql = "SELECT c.ID, c.TDATHANGID, c.DMATHANGID, c.DDONVITINHID, c.SOLUONG, c.DONGIA, " +
                "c.TILEGIAMGIA, c.THANHTIEN, c.NOTE, c.STATUS, c.TIMECREATED, c.USERCREATEDID, " +
                "m.CODE AS MAHANG, m.NAME AS TENHANG, dv.NAME AS TENDONVITINH " +
                "FROM TDATHANGCHITIET c " +
                "LEFT JOIN DMATHANG m ON c.DMATHANGID = m.ID " +
                "LEFT JOIN DDONVITINH dv ON COALESCE(c.DDONVITINHID, m.DDONVITINHID) = dv.ID " +
                "WHERE c.TDATHANGID = ? AND (c.STATUS >= 0 OR c.STATUS IS NULL) " +
                "ORDER BY c.TIMECREATED ASC, c.ID ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachDatHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhachDatHangChiTiet ct = new KhachDatHangChiTiet();
                    ct.setId(rs.getString("ID"));
                    ct.settKhachDatHangId(rs.getString("TDATHANGID"));
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
                    ct.setSoPhieuDatHang(soPhieu);
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn chi tiết đặt hàng: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới một phiếu đặt hàng và toàn bộ chi tiết
     */
    public boolean insertKhachDatHang(KhachDatHang dh, List<KhachDatHangChiTiet> chiTietList) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getTimeCreated() == null) {
            dh.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        }

        if (dh.getUserCreatedId() == null || dh.getUserCreatedId().trim().isEmpty()) {
            dh.setUserCreatedId(getEffectiveUserId());
        }
        if (dh.getUserModifiedId() == null || dh.getUserModifiedId().trim().isEmpty()) {
            dh.setUserModifiedId(getEffectiveUserId());
        }

        String sqlMaster = "INSERT INTO TDATHANG (ID, NAME, NGAY, DKHACHHANGID, TENKHACH, DIACHI, DIENTHOAI, EMAIL, " +
                "NOTE, TIENHANG, TILEGIAMGIA, TIENGIAMGIA, TILETHUE, TIENTHUE, PHIVANCHUYEN, TONGCONG, LOAI, " +
                "GIAMTHEOTIEN, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlDetail = "INSERT INTO TDATHANGCHITIET (ID, TDATHANGID, DMATHANGID, DDONVITINHID, SOLUONG, DONGIA, " +
                "TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay());
                psMaster.setString(4, dh.getdKhachHangId());
                psMaster.setString(5, dh.getTenKhach());
                psMaster.setString(6, dh.getDiaChi());
                psMaster.setString(7, dh.getDienThoai());
                psMaster.setString(8, dh.getEmail());
                psMaster.setString(9, dh.getNote());
                psMaster.setDouble(10, dh.getTienHang());
                psMaster.setDouble(11, dh.getTiLeGiamGia());
                psMaster.setDouble(12, dh.getTienGiamGia());
                psMaster.setDouble(13, dh.getTiLeThue());
                psMaster.setDouble(14, dh.getTienThue());
                psMaster.setDouble(15, dh.getPhiVanChuyen());
                psMaster.setDouble(16, dh.getTongCong());
                psMaster.setInt(17, dh.getLoai());
                psMaster.setInt(18, dh.getGiamTheoTien());
                psMaster.setInt(19, dh.getStatus());
                psMaster.setTimestamp(20, dh.getTimeCreated());
                psMaster.setString(21, dh.getUserCreatedId());
                psMaster.executeUpdate();
            }

            if (chiTietList != null && !chiTietList.isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (KhachDatHangChiTiet ct : chiTietList) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        ct.settKhachDatHangId(dh.getId());
                        if (ct.getTimeCreated() == null) {
                            ct.setTimeCreated(new Timestamp(System.currentTimeMillis()));
                        }
                        if (ct.getUserCreatedId() == null || ct.getUserCreatedId().trim().isEmpty()) {
                            ct.setUserCreatedId(getEffectiveUserId());
                        }

                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, ct.gettKhachDatHangId());
                        psDetail.setString(3, ct.getdMatHangId());
                        psDetail.setString(4, ct.getdDonViTinhId());
                        psDetail.setDouble(5, ct.getSoLuong());
                        psDetail.setDouble(6, ct.getDonGia());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getNote());
                        psDetail.setInt(10, ct.getStatus());
                        psDetail.setTimestamp(11, ct.getTimeCreated());
                        psDetail.setString(12, ct.getUserCreatedId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            auditOrder("THÊM", dh, chiTietList);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm mới phiếu đặt hàng: " + e.getMessage());
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
     * Cập nhật một phiếu đặt hàng và toàn bộ chi tiết của nó
     */
    public boolean updateKhachDatHang(KhachDatHang dh, List<KhachDatHangChiTiet> chiTietList) {
        if (dh == null || dh.getId() == null) return false;

        dh.setTimeModified(new Timestamp(System.currentTimeMillis()));
        dh.setUserModifiedId(getEffectiveUserId());

        String sqlMaster = "UPDATE TDATHANG SET NAME=?, NGAY=?, DKHACHHANGID=?, TENKHACH=?, DIACHI=?, DIENTHOAI=?, " +
                "EMAIL=?, NOTE=?, TIENHANG=?, TILEGIAMGIA=?, TIENGIAMGIA=?, TILETHUE=?, TIENTHUE=?, PHIVANCHUYEN=?, " +
                "TONGCONG=?, LOAI=?, GIAMTHEOTIEN=?, STATUS=?, TIMEMODIFIED=?, USERMODIFIEDID=? " +
                "WHERE ID=?";

        String sqlDeleteDetails = "DELETE FROM TDATHANGCHITIET WHERE TDATHANGID = ?";
        String sqlDetail = "INSERT INTO TDATHANGCHITIET (ID, TDATHANGID, DMATHANGID, DDONVITINHID, SOLUONG, DONGIA, " +
                "TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getName());
                psMaster.setTimestamp(2, dh.getNgay());
                psMaster.setString(3, dh.getdKhachHangId());
                psMaster.setString(4, dh.getTenKhach());
                psMaster.setString(5, dh.getDiaChi());
                psMaster.setString(6, dh.getDienThoai());
                psMaster.setString(7, dh.getEmail());
                psMaster.setString(8, dh.getNote());
                psMaster.setDouble(9, dh.getTienHang());
                psMaster.setDouble(10, dh.getTiLeGiamGia());
                psMaster.setDouble(11, dh.getTienGiamGia());
                psMaster.setDouble(12, dh.getTiLeThue());
                psMaster.setDouble(13, dh.getTienThue());
                psMaster.setDouble(14, dh.getPhiVanChuyen());
                psMaster.setDouble(15, dh.getTongCong());
                psMaster.setInt(16, dh.getLoai());
                psMaster.setInt(17, dh.getGiamTheoTien());
                psMaster.setInt(18, dh.getStatus());
                psMaster.setTimestamp(19, dh.getTimeModified());
                psMaster.setString(20, dh.getUserModifiedId());
                psMaster.setString(21, dh.getId());
                psMaster.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteDetails)) {
                psDel.setString(1, dh.getId());
                psDel.executeUpdate();
            }

            if (chiTietList != null && !chiTietList.isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (KhachDatHangChiTiet ct : chiTietList) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        ct.settKhachDatHangId(dh.getId());
                        if (ct.getTimeCreated() == null) {
                            ct.setTimeCreated(new Timestamp(System.currentTimeMillis()));
                        }
                        if (ct.getUserCreatedId() == null || ct.getUserCreatedId().trim().isEmpty()) {
                            ct.setUserCreatedId(getEffectiveUserId());
                        }

                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, ct.gettKhachDatHangId());
                        psDetail.setString(3, ct.getdMatHangId());
                        psDetail.setString(4, ct.getdDonViTinhId());
                        psDetail.setDouble(5, ct.getSoLuong());
                        psDetail.setDouble(6, ct.getDonGia());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getNote());
                        psDetail.setInt(10, ct.getStatus());
                        psDetail.setTimestamp(11, ct.getTimeCreated());
                        psDetail.setString(12, ct.getUserCreatedId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            auditOrder("SỬA", dh, chiTietList);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật phiếu đặt hàng: " + e.getMessage());
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
     * Xóa phiếu đặt hàng (và các dòng chi tiết)
     */
    public boolean deleteKhachDatHang(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        String sqlDeleteDetails = "DELETE FROM TDATHANGCHITIET WHERE TDATHANGID = ?";
        String sqlDeleteMaster = "DELETE FROM TDATHANG WHERE ID = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteDetails)) {
                ps.setString(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlDeleteMaster)) {
                ps.setString(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    audit("Đặt hàng của khách", "XÓA", id, "Xóa phiếu đặt hàng của khách");
                    return true;
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa phiếu đặt hàng: " + e.getMessage());
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

    private void auditOrder(String action, KhachDatHang order, List<KhachDatHangChiTiet> details) {
        int itemCount = details == null ? 0 : details.size();
        String reference = order.getName() != null && !order.getName().trim().isEmpty()
                ? order.getName() : order.getId();
        audit("Đặt hàng của khách", action, reference,
                action + " phiếu đặt hàng; khách: " + valueOrEmpty(order.getTenKhach())
                        + "; số mặt hàng: " + itemCount
                        + "; tổng cộng: " + Math.round(order.getTongCong()));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Tự sinh số phiếu kế tiếp: DH{YY}/{00000} (vd: DH26/00001)
     */
    public String generateNextSoPhieu(Date ngay) {
        if (ngay == null) ngay = new Date();
        VoucherNumberGenerator.CompiledPattern numberPattern = VoucherNumberGenerator.fromConfig(
                "Đặt hàng", "DH(yy)/(*****)", ngay);

        String sql = "SELECT NAME FROM TDATHANG WHERE NAME LIKE ? ORDER BY NAME DESC";
        long maxIndex = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberPattern.getSqlLikePattern());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    maxIndex = Math.max(maxIndex, numberPattern.extractSequence(name));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi sinh số phiếu đặt hàng: " + e.getMessage());
        }

        return numberPattern.format(maxIndex + 1);
    }

    /**
     * Lấy danh sách mẫu in đặt hàng từ bảng STEMPLATE
     */
    public List<STemplate> getKhachDatHangPrintTemplates() {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE FROM STEMPLATE " +
                     "WHERE (STATUS = 30 OR STATUS IS NULL OR STATUS > 0) " +
                     "AND (LOWER(NAME) LIKE '%đặt hàng%' OR LOWER(NAME) LIKE '%dat hang%' " +
                     "     OR LOWER(NAME) LIKE '%báo giá%' OR LOWER(NAME) LIKE '%bao gia%' " +
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
            System.err.println("Lỗi lấy mẫu in đặt hàng: " + e.getMessage());
        }

        if (list.isEmpty()) {
            list.add(new STemplate("1", "Mẫu 80"));
            list.add(new STemplate("2", "Mẫu A4 nằm ngang"));
            list.add(new STemplate("3", "Mẫu A4 thẳng đứng"));
        }
        return list;
    }

    private KhachDatHang mapResultSetToKhachDatHang(ResultSet rs) throws SQLException {
        KhachDatHang dh = new KhachDatHang();
        dh.setId(rs.getString("ID"));
        dh.setName(rs.getString("NAME"));
        dh.setNgay(rs.getTimestamp("NGAY"));
        dh.setdKhachHangId(rs.getString("DKHACHHANGID"));
        dh.setTenKhach(rs.getString("TENKHACH"));
        dh.setDiaChi(rs.getString("DIACHI"));
        dh.setDienThoai(rs.getString("DIENTHOAI"));
        dh.setEmail(rs.getString("EMAIL"));
        dh.setNote(rs.getString("NOTE"));
        dh.setTienHang(rs.getDouble("TIENHANG"));
        dh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        dh.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        dh.setTiLeThue(rs.getDouble("TILETHUE"));
        dh.setTienThue(rs.getDouble("TIENTHUE"));
        dh.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN"));
        dh.setTongCong(rs.getDouble("TONGCONG"));
        dh.setLoai(rs.getInt("LOAI"));
        dh.setGiamTheoTien(rs.getInt("GIAMTHEOTIEN"));
        dh.setStatus(rs.getInt("STATUS"));
        dh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        dh.setUserCreatedId(rs.getString("USERCREATEDID"));
        dh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        dh.setUserModifiedId(rs.getString("USERMODIFIEDID"));
        return dh;
    }
}
