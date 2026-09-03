package kazuko.ex.dao;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DonHangDAO extends BaseDAO {

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<DonHang> getPhieuXuatKho(Date tuNgay, Date denNgay, String khoXuatId) {
        return getPhieuXuatKho(tuNgay, denNgay, khoXuatId, null, null);
    }

    public List<DonHang> getPhieuXuatKho(Date tuNgay, Date denNgay, String khoXuatId, String nhanVienId, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, " +
                "k.NAME AS TENKHO, " +
                "nv.NAME AS TENNHANVIEN, " +
                "kh.NAME AS TENKHACH, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON d.DKHOXUATID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENXUATID = nv.ID " +
                "LEFT JOIN DKHACHHANG kh ON d.DKHACHHANGID = kh.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE (d.LOAI = 2 OR d.NAME LIKE 'PX%') AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (khoXuatId != null && !khoXuatId.trim().isEmpty()) {
            sql.append("AND d.DKHOXUATID = ? ");
        }
        if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
            sql.append("AND d.DNHANVIENXUATID = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(d.DIENGIAI) LIKE ? OR LOWER(k.NAME) LIKE ?) ");
        }

        sql.append("ORDER BY d.NGAY DESC, d.TIMECREATED DESC, d.NAME DESC");

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
            if (khoXuatId != null && !khoXuatId.trim().isEmpty()) {
                ps.setString(idx++, khoXuatId.trim());
            }
            if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
                ps.setString(idx++, nhanVienId.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHang dh = mapResultSetToDonHang(rs);
                    list.add(dh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu xuất kho: " + e.getMessage());
        }
        return list;
    }

    public DonHang getPhieuXuatById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, " +
                "k.NAME AS TENKHO, " +
                "nv.NAME AS TENNHANVIEN, " +
                "kh.NAME AS TENKHACH, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON d.DKHOXUATID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENXUATID = nv.ID " +
                "LEFT JOIN DKHACHHANG kh ON d.DKHACHHANGID = kh.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonHang dh = mapResultSetToDonHang(rs);
                    dh.setChiTietLayHang(getChiTietPhieuXuat(conn, dh.getId()));
                    return dh;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuXuatById: " + e.getMessage());
        }
        return null;
    }

    public List<DonHangChiTiet> getChiTietPhieuXuat(String donHangId) {
        try (Connection conn = getConnection()) {
            return getChiTietPhieuXuat(conn, donHangId);
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietPhieuXuat: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<DonHangChiTiet> getChiTietPhieuXuat(Connection conn, String donHangId) {
        List<DonHangChiTiet> list = new ArrayList<>();
        String sql = "SELECT c.*, m.CODE AS MAHANG, m.NAME AS TENHANG, " +
                "dv.NAME AS TENDONVITINH, COALESCE(c.BAOHANH, m.BAOHANH) AS BAOHANH_FINAL " +
                "FROM TDONHANGCHITIET c " +
                "LEFT JOIN DMATHANG m ON c.DMATHANGID = m.ID " +
                "LEFT JOIN DDONVITINH dv ON COALESCE(c.DDONVITINHID, m.DDONVITINHID) = dv.ID " +
                "WHERE c.TDONHANGID = ? AND (c.STATUS IS NULL OR c.STATUS >= 0) " +
                "ORDER BY c.TIMECREATED ASC, c.ID ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, donHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHangChiTiet ct = new DonHangChiTiet();
                    ct.setId(rs.getString("ID"));
                    ct.setDonHangId(donHangId);
                    ct.setMatHangId(rs.getString("DMATHANGID"));
                    ct.setMaHang(rs.getString("MAHANG"));
                    ct.setTenHang(rs.getString("TENHANG"));
                    ct.setDonViTinhId(rs.getString("DDONVITINHID"));
                    ct.setTenDonViTinh(rs.getString("TENDONVITINH"));
                    ct.setDonGia(rs.getDouble("DONGIA"));
                    double slXuat = rs.getDouble("SLXUAT");
                    ct.setSoLuong(slXuat > 0 ? slXuat : 1.0);
                    ct.setSlXuat(ct.getSoLuong());
                    ct.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    ct.setTienGiam(rs.getDouble("TIENGIAMGIA"));
                    ct.setThanhTien(rs.getDouble("THANHTIEN"));
                    ct.setGhiChu(rs.getString("NOTE"));
                    ct.setBaoHanh(rs.getString("BAOHANH_FINAL"));
                    ct.setKichThuoc(rs.getString("KICHTHUOC"));
                    ct.setHanSuDung(rs.getString("HANSUDUNG"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi load chi tiết xuất kho: " + e.getMessage());
        }
        return list;
    }

    public String generateNextSoPhieuXuat(Date ngay) {
        if (ngay == null) ngay = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String prefix = "PX" + sdf.format(ngay) + "/";

        String sql = "SELECT NAME FROM TDONHANG WHERE NAME LIKE ? AND (LOAI = 2 OR LOAI IS NULL) ORDER BY NAME DESC";
        int maxIndex = 0;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("NAME");
                    if (name != null && name.startsWith(prefix)) {
                        try {
                            String numPart = name.substring(prefix.length()).trim();
                            int val = Integer.parseInt(numPart);
                            if (val > maxIndex) maxIndex = val;
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi generateNextSoPhieuXuat: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxIndex + 1);
    }

    public boolean insertPhieuXuatKho(DonHang dh) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getName() == null || dh.getName().trim().isEmpty()) {
            dh.setName(generateNextSoPhieuXuat(dh.getNgay()));
        }
        dh.setLoai(2); // Xuất kho

        String sqlMaster = "INSERT INTO TDONHANG (ID, NAME, NGAY, DIENGIAI, TIENHANG, TIENGIAMGIA, TILEGIAMGIA, " +
                "TONGCONG, TIENTHANHTOAN, CONLAI, DKHOXUATID, DNHANVIENXUATID, DKHACHHANGID, DCUAHANGID, LOAI, STATUS, " +
                "NOTE, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 2, 1, ?, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLXUAT, SLNHAP, TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(4, dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất khác");
                psMaster.setDouble(5, dh.getTienHang());
                psMaster.setDouble(6, dh.getTienGiamGia());
                psMaster.setDouble(7, dh.getTiLeGiamGia());
                psMaster.setDouble(8, dh.getTongCong());
                psMaster.setDouble(9, dh.getTienThanhToan());
                psMaster.setDouble(10, dh.getConLai());
                psMaster.setString(11, dh.getKhoXuatId());
                psMaster.setString(12, dh.getNhanVienXuatId());
                psMaster.setString(13, dh.getKhachHangId());
                psMaster.setString(14, dh.getCuaHangId());
                psMaster.setString(15, dh.getGhiChu());
                psMaster.setString(16, getEffectiveUserId());
                psMaster.executeUpdate();
            }

            if (dh.getChiTietLayHang() != null && !dh.getChiTietLayHang().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSoLuong());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getGhiChu());
                        psDetail.setString(10, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insert phiếu xuất kho: " + e.getMessage());
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

    public boolean updatePhieuXuatKho(DonHang dh) {
        if (dh == null || dh.getId() == null) return false;

        String sqlMaster = "UPDATE TDONHANG SET NGAY = ?, DIENGIAI = ?, TIENHANG = ?, TIENGIAMGIA = ?, " +
                "TILEGIAMGIA = ?, TONGCONG = ?, TIENTHANHTOAN = ?, CONLAI = ?, DKHOXUATID = ?, " +
                "DNHANVIENXUATID = ?, DKHACHHANGID = ?, DCUAHANGID = ?, NOTE = ?, " +
                "TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";

        String sqlDeleteDetails = "DELETE FROM TDONHANGCHITIET WHERE TDONHANGID = ?";
        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLXUAT, SLNHAP, TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setTimestamp(1, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(2, dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất khác");
                psMaster.setDouble(3, dh.getTienHang());
                psMaster.setDouble(4, dh.getTienGiamGia());
                psMaster.setDouble(5, dh.getTiLeGiamGia());
                psMaster.setDouble(6, dh.getTongCong());
                psMaster.setDouble(7, dh.getTienThanhToan());
                psMaster.setDouble(8, dh.getConLai());
                psMaster.setString(9, dh.getKhoXuatId());
                psMaster.setString(10, dh.getNhanVienXuatId());
                psMaster.setString(11, dh.getKhachHangId());
                psMaster.setString(12, dh.getCuaHangId());
                psMaster.setString(13, dh.getGhiChu());
                psMaster.setString(14, getEffectiveUserId());
                psMaster.setString(15, dh.getId());
                psMaster.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteDetails)) {
                psDel.setString(1, dh.getId());
                psDel.executeUpdate();
            }

            if (dh.getChiTietLayHang() != null && !dh.getChiTietLayHang().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSoLuong());
                        psDetail.setDouble(7, ct.getTiLeGiamGia());
                        psDetail.setDouble(8, ct.getThanhTien());
                        psDetail.setString(9, ct.getGhiChu());
                        psDetail.setString(10, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi update phiếu xuất kho: " + e.getMessage());
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

    public boolean deletePhieuXuatKho(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        String sql = "UPDATE TDONHANG SET STATUS = -1, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getEffectiveUserId());
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi delete phiếu xuất kho: " + e.getMessage());
            return false;
        }
    }

    public DonHang getPhieuXuatOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDONHANG WHERE (LOAI = 2 OR NAME LIKE 'PX%') AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuXuatOffset: " + e.getMessage());
        }

        if (ids.isEmpty()) return null;

        int curIdx = ids.indexOf(currentId);
        int targetIdx;
        if (curIdx < 0) {
            targetIdx = 0;
        } else {
            targetIdx = curIdx + offset;
        }

        if (targetIdx >= 0 && targetIdx < ids.size()) {
            return getPhieuXuatById(ids.get(targetIdx));
        }
        return null;
    }

    private DonHang mapResultSetToDonHang(ResultSet rs) throws SQLException {
        DonHang dh = new DonHang();
        dh.setId(rs.getString("ID"));
        dh.setName(rs.getString("NAME"));
        dh.setNgay(rs.getTimestamp("NGAY"));
        dh.setDienGiai(rs.getString("DIENGIAI"));
        dh.setTienHang(rs.getDouble("TIENHANG"));
        dh.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        dh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        dh.setTongCong(rs.getDouble("TONGCONG"));
        dh.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
        dh.setConLai(rs.getDouble("CONLAI"));
        dh.setKhoXuatId(rs.getString("DKHOXUATID"));
        dh.setNhanVienXuatId(rs.getString("DNHANVIENXUATID"));
        dh.setKhachHangId(rs.getString("DKHACHHANGID"));
        dh.setCuaHangId(rs.getString("DCUAHANGID"));
        dh.setLoai(rs.getInt("LOAI"));
        dh.setStatus(rs.getInt("STATUS"));
        dh.setGhiChu(rs.getString("NOTE"));

        try {
            dh.setTenKho(rs.getString("TENKHO"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNhanVien(rs.getString("TENNHANVIEN"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenKhach(rs.getString("TENKHACH"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenCuaHang(rs.getString("TENCUAHANG"));
        } catch (SQLException ignored) {}
        try {
            dh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        } catch (SQLException ignored) {}
        try {
            dh.setUserCreatedId(rs.getString("USERCREATEDID"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNguoiTao(rs.getString("TENNGUOITAO"));
            if (dh.getTenNguoiTao() == null) dh.setTenNguoiTao("Administrator");
        } catch (SQLException ignored) {}
        try {
            dh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        } catch (SQLException ignored) {}
        try {
            dh.setUserModifiedId(rs.getString("USERMODIFIEDID"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNguoiSua(rs.getString("TENNGUOISUA"));
        } catch (SQLException ignored) {}

        return dh;
    }
}
