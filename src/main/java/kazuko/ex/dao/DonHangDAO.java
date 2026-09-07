package kazuko.ex.dao;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public boolean delete(String id) {
        return deletePhieuXuatKho(id);
    }

    public boolean deletePhieuChuyenKho(String id) {
        return deletePhieuXuatKho(id);
    }

    public DonHang getPhieuChuyenKhoOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDONHANG WHERE LOAI = 3 AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuChuyenKhoOffset: " + e.getMessage());
        }

        if (ids.isEmpty()) return null;

        int curIdx = ids.indexOf(currentId);
        int targetIdx = curIdx < 0 ? 0 : curIdx + offset;

        if (targetIdx >= 0 && targetIdx < ids.size()) {
            return getPhieuChuyenKhoById(ids.get(targetIdx));
        }
        return null;
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

    // =========================================================================
    // =========================== PHIẾU NHẬP KHO ==============================
    // =========================================================================

    public List<DonHang> getPhieuNhapKho(Date tuNgay, Date denNgay, String khoNhapId, String nhanVienId, String nhaCungCapId, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, " +
                "k.NAME AS TENKHONHAP, " +
                "nv.NAME AS TENNHANVIENNHAP, " +
                "ncc.NAME AS TENNHACUNGCAP, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON d.DKHONHAPID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENNHAPID = nv.ID " +
                "LEFT JOIN DKHACHHANG ncc ON COALESCE(d.DNHACUNGCAPID, d.DKHACHHANGID) = ncc.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE (d.LOAI = 1 OR d.NAME LIKE 'PN%') AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (khoNhapId != null && !khoNhapId.trim().isEmpty()) {
            sql.append("AND d.DKHONHAPID = ? ");
        }
        if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
            sql.append("AND d.DNHANVIENNHAPID = ? ");
        }
        if (nhaCungCapId != null && !nhaCungCapId.trim().isEmpty()) {
            sql.append("AND (d.DNHACUNGCAPID = ? OR d.DKHACHHANGID = ?) ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(d.DIENGIAI) LIKE ? OR LOWER(k.NAME) LIKE ? OR LOWER(ncc.NAME) LIKE ?) ");
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
            if (khoNhapId != null && !khoNhapId.trim().isEmpty()) {
                ps.setString(idx++, khoNhapId.trim());
            }
            if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
                ps.setString(idx++, nhanVienId.trim());
            }
            if (nhaCungCapId != null && !nhaCungCapId.trim().isEmpty()) {
                ps.setString(idx++, nhaCungCapId.trim());
                ps.setString(idx++, nhaCungCapId.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPhieuNhap(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu nhập kho: " + e.getMessage());
        }
        return list;
    }

    public DonHang getPhieuNhapById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, " +
                "k.NAME AS TENKHONHAP, " +
                "nv.NAME AS TENNHANVIENNHAP, " +
                "ncc.NAME AS TENNHACUNGCAP, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON d.DKHONHAPID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENNHAPID = nv.ID " +
                "LEFT JOIN DKHACHHANG ncc ON COALESCE(d.DNHACUNGCAPID, d.DKHACHHANGID) = ncc.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonHang dh = mapResultSetToPhieuNhap(rs);
                    dh.setChiTietLayHang(getChiTietPhieuNhap(conn, dh.getId()));
                    return dh;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuNhapById: " + e.getMessage());
        }
        return null;
    }

    public List<DonHangChiTiet> getChiTietPhieuNhap(String donHangId) {
        try (Connection conn = getConnection()) {
            return getChiTietPhieuNhap(conn, donHangId);
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietPhieuNhap: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<DonHangChiTiet> getChiTietPhieuNhap(Connection conn, String donHangId) {
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
                    double slNhap = rs.getDouble("SLNHAP");
                    double slXuat = rs.getDouble("SLXUAT");
                    double qty = slNhap > 0 ? slNhap : (slXuat > 0 ? slXuat : 1.0);
                    ct.setSoLuong(qty);
                    ct.setSlNhap(qty);
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
            System.err.println("Lỗi load chi tiết nhập kho: " + e.getMessage());
        }
        return list;
    }

    public String generateNextSoPhieuNhap(Date ngay) {
        if (ngay == null) ngay = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String prefix = "PN" + sdf.format(ngay) + "/";

        String sql = "SELECT NAME FROM TDONHANG WHERE NAME LIKE ? AND (LOAI = 1 OR LOAI IS NULL) ORDER BY NAME DESC";
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
            System.err.println("Lỗi generateNextSoPhieuNhap: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxIndex + 1);
    }

    public boolean insertPhieuNhapKho(DonHang dh) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getName() == null || dh.getName().trim().isEmpty()) {
            dh.setName(generateNextSoPhieuNhap(dh.getNgay()));
        }
        dh.setLoai(1); // 1: Nhập kho

        String sqlMaster = "INSERT INTO TDONHANG (ID, NAME, NGAY, DIENGIAI, TIENHANG, TIENGIAMGIA, TILEGIAMGIA, " +
                "TIENTHUE, TILETHUE, PHIVANCHUYEN, TONGCONG, TIENTHANHTOAN, CONLAI, DKHONHAPID, DNHANVIENNHAPID, " +
                "DNHACUNGCAPID, DCUAHANGID, LOAI, STATUS, NOTE, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1, ?, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, TILEGIAMGIA, TIENGIAMGIA, THANHTIEN, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(4, dh.getDienGiai() != null ? dh.getDienGiai() : "Nhập mua hàng");
                psMaster.setDouble(5, dh.getTienHang());
                psMaster.setDouble(6, dh.getTienGiamGia());
                psMaster.setDouble(7, dh.getTiLeGiamGia());
                psMaster.setDouble(8, dh.getTienThue());
                psMaster.setDouble(9, dh.getTiLeThue());
                psMaster.setDouble(10, dh.getPhiVanChuyen());
                psMaster.setDouble(11, dh.getTongCong());
                psMaster.setDouble(12, dh.getTienThanhToan());
                psMaster.setDouble(13, dh.getConLai());
                psMaster.setString(14, dh.getKhoNhapId());
                psMaster.setString(15, dh.getNhanVienNhapId());
                psMaster.setString(16, dh.getNhaCungCapId());
                psMaster.setString(17, dh.getCuaHangId());
                psMaster.setString(18, dh.getGhiChu());
                psMaster.setString(19, getEffectiveUserId());
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
                        psDetail.setDouble(8, ct.getTienGiam());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu());
                        psDetail.setString(11, ct.getBaoHanh());
                        psDetail.setString(12, ct.getKichThuoc());
                        psDetail.setString(13, ct.getHanSuDung());
                        psDetail.setString(14, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insert phiếu nhập kho: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updatePhieuNhapKho(DonHang dh) {
        if (dh == null || dh.getId() == null) return false;

        String sqlMaster = "UPDATE TDONHANG SET NGAY = ?, DIENGIAI = ?, TIENHANG = ?, TIENGIAMGIA = ?, " +
                "TILEGIAMGIA = ?, TIENTHUE = ?, TILETHUE = ?, PHIVANCHUYEN = ?, TONGCONG = ?, TIENTHANHTOAN = ?, " +
                "CONLAI = ?, DKHONHAPID = ?, DNHANVIENNHAPID = ?, DNHACUNGCAPID = ?, DCUAHANGID = ?, NOTE = ?, " +
                "TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";

        String sqlDeleteDetails = "DELETE FROM TDONHANGCHITIET WHERE TDONHANGID = ?";
        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, TILEGIAMGIA, TIENGIAMGIA, THANHTIEN, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setTimestamp(1, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(2, dh.getDienGiai() != null ? dh.getDienGiai() : "Nhập mua hàng");
                psMaster.setDouble(3, dh.getTienHang());
                psMaster.setDouble(4, dh.getTienGiamGia());
                psMaster.setDouble(5, dh.getTiLeGiamGia());
                psMaster.setDouble(6, dh.getTienThue());
                psMaster.setDouble(7, dh.getTiLeThue());
                psMaster.setDouble(8, dh.getPhiVanChuyen());
                psMaster.setDouble(9, dh.getTongCong());
                psMaster.setDouble(10, dh.getTienThanhToan());
                psMaster.setDouble(11, dh.getConLai());
                psMaster.setString(12, dh.getKhoNhapId());
                psMaster.setString(13, dh.getNhanVienNhapId());
                psMaster.setString(14, dh.getNhaCungCapId());
                psMaster.setString(15, dh.getCuaHangId());
                psMaster.setString(16, dh.getGhiChu());
                psMaster.setString(17, getEffectiveUserId());
                psMaster.setString(18, dh.getId());
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
                        psDetail.setDouble(8, ct.getTienGiam());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu());
                        psDetail.setString(11, ct.getBaoHanh());
                        psDetail.setString(12, ct.getKichThuoc());
                        psDetail.setString(13, ct.getHanSuDung());
                        psDetail.setString(14, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi update phiếu nhập kho: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deletePhieuNhapKho(String id) {
        return deletePhieuXuatKho(id);
    }

    public DonHang getPhieuNhapOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDONHANG WHERE (LOAI = 1 OR NAME LIKE 'PN%') AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuNhapOffset: " + e.getMessage());
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
            return getPhieuNhapById(ids.get(targetIdx));
        }
        return null;
    }

    private DonHang mapResultSetToPhieuNhap(ResultSet rs) throws SQLException {
        DonHang dh = new DonHang();
        dh.setId(rs.getString("ID"));
        dh.setName(rs.getString("NAME"));
        dh.setNgay(rs.getTimestamp("NGAY"));
        dh.setDienGiai(rs.getString("DIENGIAI"));
        dh.setTienHang(rs.getDouble("TIENHANG"));
        dh.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        dh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        dh.setTienThue(rs.getDouble("TIENTHUE"));
        dh.setTiLeThue(rs.getDouble("TILETHUE"));
        dh.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN"));
        dh.setTongCong(rs.getDouble("TONGCONG"));
        dh.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
        dh.setConLai(rs.getDouble("CONLAI"));
        dh.setKhoNhapId(rs.getString("DKHONHAPID"));
        dh.setNhanVienNhapId(rs.getString("DNHANVIENNHAPID"));
        dh.setNhaCungCapId(rs.getString("DNHACUNGCAPID"));
        dh.setKhachHangId(rs.getString("DKHACHHANGID"));
        dh.setCuaHangId(rs.getString("DCUAHANGID"));
        dh.setLoai(rs.getInt("LOAI"));
        dh.setStatus(rs.getInt("STATUS"));
        dh.setGhiChu(rs.getString("NOTE"));

        try {
            dh.setTenKhoNhap(rs.getString("TENKHONHAP"));
            if (dh.getTenKhoNhap() == null) dh.setTenKhoNhap(rs.getString("TENKHO"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNhanVienNhap(rs.getString("TENNHANVIENNHAP"));
            if (dh.getTenNhanVienNhap() == null) dh.setTenNhanVienNhap(rs.getString("TENNHANVIEN"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNhaCungCap(rs.getString("TENNHACUNGCAP"));
            if (dh.getTenNhaCungCap() == null) dh.setTenNhaCungCap(rs.getString("TENKHACH"));
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

    public String generateNextSoPhieuChuyenKho(Date ngay) {
        if (ngay == null) ngay = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String prefix = "PCK" + sdf.format(ngay) + "/";

        String sql = "SELECT NAME FROM TDONHANG WHERE NAME LIKE ? AND LOAI = 3 ORDER BY NAME DESC";
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
            System.err.println("Lỗi generateNextSoPhieuChuyenKho: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxIndex + 1);
    }

    public boolean insertPhieuChuyenKho(DonHang dh) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getName() == null || dh.getName().trim().isEmpty()) {
            dh.setName(generateNextSoPhieuChuyenKho(dh.getNgay()));
        }
        dh.setLoai(3); // 3: Chuyển kho nội bộ

        String sqlMaster = "INSERT INTO TDONHANG (ID, NAME, NGAY, DIENGIAI, TIENHANG, TIENGIAMGIA, TILEGIAMGIA, " +
                "TIENTHUE, TILETHUE, PHIVANCHUYEN, TONGCONG, TIENTHANHTOAN, CONLAI, DKHOXUATID, DKHONHAPID, " +
                "DNHANVIENXUATID, DNHANVIENNHAPID, DCUAHANGID, LOAI, STATUS, NOTE, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 3, 1, ?, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, TILEGIAMGIA, TIENGIAMGIA, THANHTIEN, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(4, dh.getDienGiai() != null ? dh.getDienGiai() : "Chuyển kho nội bộ");
                psMaster.setDouble(5, dh.getTienHang());
                psMaster.setDouble(6, dh.getTienGiamGia());
                psMaster.setDouble(7, dh.getTiLeGiamGia());
                psMaster.setDouble(8, dh.getTienThue());
                psMaster.setDouble(9, dh.getTiLeThue());
                psMaster.setDouble(10, dh.getPhiVanChuyen());
                psMaster.setDouble(11, dh.getTongCong());
                psMaster.setDouble(12, dh.getTienThanhToan());
                psMaster.setDouble(13, dh.getConLai());
                psMaster.setString(14, dh.getKhoXuatId());
                psMaster.setString(15, dh.getKhoNhapId());
                psMaster.setString(16, dh.getNhanVienXuatId());
                psMaster.setString(17, dh.getNhanVienNhapId());
                psMaster.setString(18, dh.getCuaHangId());
                psMaster.setString(19, dh.getGhiChu());
                psMaster.setString(20, getEffectiveUserId());
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
                        psDetail.setDouble(6, ct.getSoLuong()); // SLNHAP
                        psDetail.setDouble(7, ct.getSoLuong()); // SLXUAT
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getTienGiam());
                        psDetail.setDouble(10, ct.getThanhTien());
                        psDetail.setString(11, ct.getGhiChu());
                        psDetail.setString(12, ct.getBaoHanh());
                        psDetail.setString(13, ct.getKichThuoc());
                        psDetail.setString(14, ct.getHanSuDung());
                        psDetail.setString(15, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insertPhieuChuyenKho: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updatePhieuChuyenKho(DonHang dh) {
        if (dh == null || dh.getId() == null) return false;

        String sqlMaster = "UPDATE TDONHANG SET NAME = ?, NGAY = ?, DIENGIAI = ?, TIENHANG = ?, TIENGIAMGIA = ?, " +
                "TILEGIAMGIA = ?, TIENTHUE = ?, TILETHUE = ?, PHIVANCHUYEN = ?, TONGCONG = ?, TIENTHANHTOAN = ?, " +
                "CONLAI = ?, DKHOXUATID = ?, DKHONHAPID = ?, DNHANVIENXUATID = ?, DNHANVIENNHAPID = ?, " +
                "NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";

        String sqlDeleteOld = "DELETE FROM TDONHANGCHITIET WHERE TDONHANGID = ?";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, TILEGIAMGIA, TIENGIAMGIA, THANHTIEN, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlMaster)) {
                ps.setString(1, dh.getName());
                ps.setTimestamp(2, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(3, dh.getDienGiai());
                ps.setDouble(4, dh.getTienHang());
                ps.setDouble(5, dh.getTienGiamGia());
                ps.setDouble(6, dh.getTiLeGiamGia());
                ps.setDouble(7, dh.getTienThue());
                ps.setDouble(8, dh.getTiLeThue());
                ps.setDouble(9, dh.getPhiVanChuyen());
                ps.setDouble(10, dh.getTongCong());
                ps.setDouble(11, dh.getTienThanhToan());
                ps.setDouble(12, dh.getConLai());
                ps.setString(13, dh.getKhoXuatId());
                ps.setString(14, dh.getKhoNhapId());
                ps.setString(15, dh.getNhanVienXuatId());
                ps.setString(16, dh.getNhanVienNhapId());
                ps.setString(17, dh.getGhiChu());
                ps.setString(18, getEffectiveUserId());
                ps.setString(19, dh.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteOld)) {
                psDel.setString(1, dh.getId());
                psDel.executeUpdate();
            }

            if (dh.getChiTietLayHang() != null && !dh.getChiTietLayHang().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        String detailId = UUID.randomUUID().toString();
                        ct.setId(detailId);
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSoLuong()); // SLNHAP
                        psDetail.setDouble(7, ct.getSoLuong()); // SLXUAT
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getTienGiam());
                        psDetail.setDouble(10, ct.getThanhTien());
                        psDetail.setString(11, ct.getGhiChu());
                        psDetail.setString(12, ct.getBaoHanh());
                        psDetail.setString(13, ct.getKichThuoc());
                        psDetail.setString(14, ct.getHanSuDung());
                        psDetail.setString(15, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePhieuChuyenKho: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<DonHang> getAllPhieuChuyenKho(Date tuNgay, Date denNgay, String khoNhapId, String khoXuatId, String nvNhapId, String nvXuatId, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, " +
                "kx.NAME AS TENKHOXUAT, " +
                "kn.NAME AS TENKHONHAP, " +
                "nvx.NAME AS TENNHANVIENXUAT, " +
                "nvn.NAME AS TENNHANVIENNHAP, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG kx ON d.DKHOXUATID = kx.ID " +
                "LEFT JOIN DKHOHANG kn ON d.DKHONHAPID = kn.ID " +
                "LEFT JOIN DNHANVIEN nvx ON d.DNHANVIENXUATID = nvx.ID " +
                "LEFT JOIN DNHANVIEN nvn ON d.DNHANVIENNHAPID = nvn.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.LOAI = 3 AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (khoNhapId != null && !khoNhapId.trim().isEmpty()) {
            sql.append("AND d.DKHONHAPID = ? ");
        }
        if (khoXuatId != null && !khoXuatId.trim().isEmpty()) {
            sql.append("AND d.DKHOXUATID = ? ");
        }
        if (nvNhapId != null && !nvNhapId.trim().isEmpty()) {
            sql.append("AND d.DNHANVIENNHAPID = ? ");
        }
        if (nvXuatId != null && !nvXuatId.trim().isEmpty()) {
            sql.append("AND d.DNHANVIENXUATID = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(d.DIENGIAI) LIKE ? OR LOWER(kx.NAME) LIKE ? OR LOWER(kn.NAME) LIKE ? OR LOWER(d.NOTE) LIKE ?) ");
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
            if (khoNhapId != null && !khoNhapId.trim().isEmpty()) {
                ps.setString(idx++, khoNhapId.trim());
            }
            if (khoXuatId != null && !khoXuatId.trim().isEmpty()) {
                ps.setString(idx++, khoXuatId.trim());
            }
            if (nvNhapId != null && !nvNhapId.trim().isEmpty()) {
                ps.setString(idx++, nvNhapId.trim());
            }
            if (nvXuatId != null && !nvXuatId.trim().isEmpty()) {
                ps.setString(idx++, nvXuatId.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPhieuChuyenKho(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu chuyển kho: " + e.getMessage());
        }
        return list;
    }

    public DonHang getPhieuChuyenKhoById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, " +
                "kx.NAME AS TENKHOXUAT, " +
                "kn.NAME AS TENKHONHAP, " +
                "nvx.NAME AS TENNHANVIENXUAT, " +
                "nvn.NAME AS TENNHANVIENNHAP, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG kx ON d.DKHOXUATID = kx.ID " +
                "LEFT JOIN DKHOHANG kn ON d.DKHONHAPID = kn.ID " +
                "LEFT JOIN DNHANVIEN nvx ON d.DNHANVIENXUATID = nvx.ID " +
                "LEFT JOIN DNHANVIEN nvn ON d.DNHANVIENNHAPID = nvn.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.ID = ? AND d.LOAI = 3";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonHang dh = mapResultSetToPhieuChuyenKho(rs);
                    dh.setChiTietLayHang(getChiTietPhieuNhap(conn, dh.getId()));
                    return dh;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuChuyenKhoById: " + e.getMessage());
        }
        return null;
    }

    private DonHang mapResultSetToPhieuChuyenKho(ResultSet rs) throws SQLException {
        DonHang dh = new DonHang();
        dh.setId(rs.getString("ID"));
        dh.setName(rs.getString("NAME"));
        dh.setNgay(rs.getTimestamp("NGAY"));
        dh.setDienGiai(rs.getString("DIENGIAI"));
        dh.setTienHang(rs.getDouble("TIENHANG"));
        dh.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        dh.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        dh.setTienThue(rs.getDouble("TIENTHUE"));
        dh.setTiLeThue(rs.getDouble("TILETHUE"));
        dh.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN"));
        dh.setTongCong(rs.getDouble("TONGCONG"));
        dh.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
        dh.setConLai(rs.getDouble("CONLAI"));
        dh.setKhoXuatId(rs.getString("DKHOXUATID"));
        dh.setKhoNhapId(rs.getString("DKHONHAPID"));
        dh.setNhanVienXuatId(rs.getString("DNHANVIENXUATID"));
        dh.setNhanVienNhapId(rs.getString("DNHANVIENNHAPID"));
        dh.setCuaHangId(rs.getString("DCUAHANGID"));
        dh.setLoai(rs.getInt("LOAI"));
        dh.setStatus(rs.getInt("STATUS"));
        dh.setGhiChu(rs.getString("NOTE"));

        try {
            dh.setTenKhoXuat(rs.getString("TENKHOXUAT"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenKhoNhap(rs.getString("TENKHONHAP"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNhanVienXuat(rs.getString("TENNHANVIENXUAT"));
        } catch (SQLException ignored) {}
        try {
            dh.setTenNhanVienNhap(rs.getString("TENNHANVIENNHAP"));
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

    public String generateNextSoPhieuKiemKe(Date ngay) {
        if (ngay == null) ngay = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String prefix = "PKK" + sdf.format(ngay) + "/";

        String sql = "SELECT NAME FROM TDONHANG WHERE NAME LIKE ? AND LOAI = 4 ORDER BY NAME DESC";
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
            System.err.println("Lỗi generateNextSoPhieuKiemKe: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxIndex + 1);
    }

    public double getTonKhoTheoKho(String khoId, String matHangId) {
        if (matHangId == null || matHangId.trim().isEmpty()) return 0.0;
        if (khoId == null || khoId.trim().isEmpty()) {
            return new MatHangDAO().getTonKhoHienTai(matHangId);
        }
        String sql = "SELECT COALESCE(SUM( " +
                "    CASE WHEN d.DKHONHAPID = ? THEN c.SLNHAP ELSE 0 END " +
                "    - " +
                "    CASE WHEN d.DKHOXUATID = ? THEN c.SLXUAT ELSE 0 END " +
                "), 0) AS TONKHO " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE c.DMATHANGID = ? AND (d.STATUS IS NULL OR d.STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khoId.trim());
            ps.setString(2, khoId.trim());
            ps.setString(3, matHangId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TONKHO");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonKhoTheoKho: " + e.getMessage());
        }
        return 0.0;
    }

    public boolean insertPhieuKiemKe(DonHang dh) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getName() == null || dh.getName().trim().isEmpty()) {
            dh.setName(generateNextSoPhieuKiemKe(dh.getNgay()));
        }
        dh.setLoai(4); // 4: Kiểm kê kho

        String sqlMaster = "INSERT INTO TDONHANG (ID, NAME, NGAY, DIENGIAI, NOTE, LOAI, STATUS, " +
                "DKHOXUATID, DKHONHAPID, DNHANVIENXUATID, DNHANVIENNHAPID, DCUAHANGID, " +
                "TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, 4, 1, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, SLTHUCTE, SLHETHONG, DKHOHANGID, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, " +
                "STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(4, dh.getDienGiai() != null ? dh.getDienGiai() : "Kiểm kê");
                psMaster.setString(5, dh.getGhiChu());
                psMaster.setString(6, dh.getKhoXuatId());
                psMaster.setString(7, dh.getKhoXuatId());
                psMaster.setString(8, dh.getNhanVienXuatId());
                psMaster.setString(9, dh.getNhanVienXuatId());
                psMaster.setString(10, dh.getCuaHangId());
                psMaster.setString(11, getEffectiveUserId());
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
                        psDetail.setDouble(6, ct.getSlNhap()); // Điều chỉnh nhập
                        psDetail.setDouble(7, ct.getSlXuat()); // Điều chỉnh xuất
                        psDetail.setDouble(8, ct.getSlThucTe()); // Số lượng thực tế
                        psDetail.setDouble(9, ct.getSlHeThong()); // Số lượng hệ thống
                        psDetail.setString(10, dh.getKhoXuatId());
                        psDetail.setString(11, ct.getGhiChu());
                        psDetail.setString(12, ct.getBaoHanh());
                        psDetail.setString(13, ct.getKichThuoc());
                        psDetail.setString(14, ct.getHanSuDung());
                        psDetail.setString(15, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insertPhieuKiemKe: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updatePhieuKiemKe(DonHang dh) {
        if (dh == null || dh.getId() == null) return false;

        String sqlMaster = "UPDATE TDONHANG SET NAME = ?, NGAY = ?, DIENGIAI = ?, NOTE = ?, " +
                "DKHOXUATID = ?, DKHONHAPID = ?, DNHANVIENXUATID = ?, DNHANVIENNHAPID = ?, " +
                "TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";

        String sqlDeleteOld = "DELETE FROM TDONHANGCHITIET WHERE TDONHANGID = ?";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLNHAP, SLXUAT, SLTHUCTE, SLHETHONG, DKHOHANGID, NOTE, BAOHANH, KICHTHUOC, HANSUDUNG, " +
                "STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlMaster)) {
                ps.setString(1, dh.getName());
                ps.setTimestamp(2, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(3, dh.getDienGiai());
                ps.setString(4, dh.getGhiChu());
                ps.setString(5, dh.getKhoXuatId());
                ps.setString(6, dh.getKhoXuatId());
                ps.setString(7, dh.getNhanVienXuatId());
                ps.setString(8, dh.getNhanVienXuatId());
                ps.setString(9, getEffectiveUserId());
                ps.setString(10, dh.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteOld)) {
                psDel.setString(1, dh.getId());
                psDel.executeUpdate();
            }

            if (dh.getChiTietLayHang() != null && !dh.getChiTietLayHang().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        String detailId = UUID.randomUUID().toString();
                        ct.setId(detailId);
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSlNhap());
                        psDetail.setDouble(7, ct.getSlXuat());
                        psDetail.setDouble(8, ct.getSlThucTe());
                        psDetail.setDouble(9, ct.getSlHeThong());
                        psDetail.setString(10, dh.getKhoXuatId());
                        psDetail.setString(11, ct.getGhiChu());
                        psDetail.setString(12, ct.getBaoHanh());
                        psDetail.setString(13, ct.getKichThuoc());
                        psDetail.setString(14, ct.getHanSuDung());
                        psDetail.setString(15, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePhieuKiemKe: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deletePhieuKiemKe(String id) {
        return deletePhieuXuatKho(id);
    }

    public List<DonHang> getAllPhieuKiemKe(Date tuNgay, Date denNgay, String khoId, String nhanVienId, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, " +
                "k.NAME AS TENKHO, " +
                "nv.NAME AS TENNHANVIEN, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON COALESCE(d.DKHONHAPID, d.DKHOXUATID) = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON COALESCE(d.DNHANVIENNHAPID, d.DNHANVIENXUATID) = nv.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.LOAI = 4 AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (khoId != null && !khoId.trim().isEmpty()) {
            sql.append("AND (d.DKHONHAPID = ? OR d.DKHOXUATID = ?) ");
        }
        if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
            sql.append("AND (d.DNHANVIENNHAPID = ? OR d.DNHANVIENXUATID = ?) ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(d.DIENGIAI) LIKE ? OR LOWER(k.NAME) LIKE ? OR LOWER(d.NOTE) LIKE ?) ");
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
            if (khoId != null && !khoId.trim().isEmpty()) {
                ps.setString(idx++, khoId.trim());
                ps.setString(idx++, khoId.trim());
            }
            if (nhanVienId != null && !nhanVienId.trim().isEmpty()) {
                ps.setString(idx++, nhanVienId.trim());
                ps.setString(idx++, nhanVienId.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim().toLowerCase() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHang dh = new DonHang();
                    dh.setId(rs.getString("ID"));
                    dh.setName(rs.getString("NAME"));
                    dh.setNgay(rs.getTimestamp("NGAY"));
                    dh.setDienGiai(rs.getString("DIENGIAI"));
                    dh.setGhiChu(rs.getString("NOTE"));
                    dh.setLoai(rs.getInt("LOAI"));
                    dh.setStatus(rs.getInt("STATUS"));
                    dh.setKhoXuatId(rs.getString("DKHOXUATID"));
                    dh.setKhoNhapId(rs.getString("DKHONHAPID"));
                    dh.setNhanVienXuatId(rs.getString("DNHANVIENXUATID"));
                    dh.setNhanVienNhapId(rs.getString("DNHANVIENNHAPID"));
                    dh.setCuaHangId(rs.getString("DCUAHANGID"));
                    dh.setTenKhoXuat(rs.getString("TENKHO"));
                    dh.setTenKhoNhap(rs.getString("TENKHO"));
                    dh.setTenNhanVien(rs.getString("TENNHANVIEN"));
                    dh.setTenCuaHang(rs.getString("TENCUAHANG"));
                    dh.setTenNguoiTao(rs.getString("TENNGUOITAO"));
                    dh.setTenNguoiSua(rs.getString("TENNGUOISUA"));
                    dh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    dh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    list.add(dh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllPhieuKiemKe: " + e.getMessage());
        }
        return list;
    }

    public DonHang getPhieuKiemKeById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, " +
                "k.NAME AS TENKHO, " +
                "nv.NAME AS TENNHANVIEN, " +
                "ch.NAME AS TENCUAHANG, " +
                "u.NAME AS TENNGUOITAO, " +
                "um.NAME AS TENNGUOISUA " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHOHANG k ON COALESCE(d.DKHONHAPID, d.DKHOXUATID) = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON COALESCE(d.DNHANVIENNHAPID, d.DNHANVIENXUATID) = nv.ID " +
                "LEFT JOIN DCUAHANG ch ON d.DCUAHANGID = ch.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.ID = ? AND d.LOAI = 4";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonHang dh = new DonHang();
                    dh.setId(rs.getString("ID"));
                    dh.setName(rs.getString("NAME"));
                    dh.setNgay(rs.getTimestamp("NGAY"));
                    dh.setDienGiai(rs.getString("DIENGIAI"));
                    dh.setGhiChu(rs.getString("NOTE"));
                    dh.setLoai(rs.getInt("LOAI"));
                    dh.setStatus(rs.getInt("STATUS"));
                    dh.setKhoXuatId(rs.getString("DKHOXUATID"));
                    dh.setKhoNhapId(rs.getString("DKHONHAPID"));
                    dh.setNhanVienXuatId(rs.getString("DNHANVIENXUATID"));
                    dh.setNhanVienNhapId(rs.getString("DNHANVIENNHAPID"));
                    dh.setCuaHangId(rs.getString("DCUAHANGID"));
                    dh.setTenKhoXuat(rs.getString("TENKHO"));
                    dh.setTenKhoNhap(rs.getString("TENKHO"));
                    dh.setTenNhanVien(rs.getString("TENNHANVIEN"));
                    dh.setTenCuaHang(rs.getString("TENCUAHANG"));
                    dh.setTenNguoiTao(rs.getString("TENNGUOITAO"));
                    dh.setTenNguoiSua(rs.getString("TENNGUOISUA"));
                    dh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    dh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    dh.setChiTietLayHang(getChiTietPhieuKiemKe(conn, dh.getId()));
                    return dh;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuKiemKeById: " + e.getMessage());
        }
        return null;
    }

    public List<DonHangChiTiet> getChiTietPhieuKiemKe(String donHangId) {
        try (Connection conn = getConnection()) {
            return getChiTietPhieuKiemKe(conn, donHangId);
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietPhieuKiemKe: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<DonHangChiTiet> getChiTietPhieuKiemKe(Connection conn, String donHangId) {
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
                    ct.setSlHeThong(rs.getDouble("SLHETHONG"));
                    ct.setSlThucTe(rs.getDouble("SLTHUCTE"));
                    ct.setSoLuong(rs.getDouble("SLTHUCTE"));
                    ct.setSlNhap(rs.getDouble("SLNHAP"));
                    ct.setSlXuat(rs.getDouble("SLXUAT"));
                    ct.setGhiChu(rs.getString("NOTE"));
                    ct.setBaoHanh(rs.getString("BAOHANH_FINAL"));
                    ct.setKichThuoc(rs.getString("KICHTHUOC"));
                    ct.setHanSuDung(rs.getString("HANSUDUNG"));
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietPhieuKiemKe: " + e.getMessage());
        }
        return list;
    }

    public DonHang getPhieuKiemKeOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDONHANG WHERE LOAI = 4 AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPhieuKiemKeOffset: " + e.getMessage());
        }

        if (ids.isEmpty()) return null;

        int curIdx = ids.indexOf(currentId);
        int targetIdx = curIdx < 0 ? 0 : curIdx + offset;

        if (targetIdx >= 0 && targetIdx < ids.size()) {
            return getPhieuKiemKeById(ids.get(targetIdx));
        }
        return null;
    }

    public List<kazuko.ex.model.khohang.TonKhoItem> getTonKhoDanhSach(String khoId) {
        List<kazuko.ex.model.khohang.TonKhoItem> list = new ArrayList<>();
        Map<String, Double> stockMap = new HashMap<>();

        // 1. Tính tồn kho nhanh bằng 1 câu aggregate
        String stockSql;
        if (khoId != null && !khoId.trim().isEmpty()) {
            stockSql = "SELECT c.DMATHANGID, " +
                    "SUM(CASE WHEN d.DKHONHAPID = ? THEN c.SLNHAP ELSE 0 END) AS NHAP, " +
                    "SUM(CASE WHEN d.DKHOXUATID = ? THEN c.SLXUAT ELSE 0 END) AS XUAT " +
                    "FROM TDONHANGCHITIET c " +
                    "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                    "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND (d.DKHONHAPID = ? OR d.DKHOXUATID = ?) " +
                    "GROUP BY c.DMATHANGID";
        } else {
            stockSql = "SELECT c.DMATHANGID, " +
                    "SUM(c.SLNHAP) AS NHAP, " +
                    "SUM(c.SLXUAT) AS XUAT " +
                    "FROM TDONHANGCHITIET c " +
                    "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                    "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) " +
                    "GROUP BY c.DMATHANGID";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(stockSql)) {
            if (khoId != null && !khoId.trim().isEmpty()) {
                ps.setString(1, khoId.trim());
                ps.setString(2, khoId.trim());
                ps.setString(3, khoId.trim());
                ps.setString(4, khoId.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mhId = rs.getString("DMATHANGID");
                    double nhap = rs.getDouble("NHAP");
                    double xuat = rs.getDouble("XUAT");
                    if (mhId != null) {
                        stockMap.put(mhId.trim(), nhap - xuat);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính stockMap: " + e.getMessage());
        }

        // 2. Lấy toàn bộ danh mục mặt hàng kèm thông tin chi tiết
        String itemSql = "SELECT m.ID, m.CODE AS MA, m.NAME AS TENHANG, m.DNHOMMATHANGID, nh.NAME AS TENNHOM, " +
                "m.GIABAN, m.GIAVON, m.QUYDOI, m.BAOHANH, m.TONTOITHIEU, " +
                "dv1.NAME AS TENDVT1, dv2.NAME AS TENDVT2, hsx.NAME AS TENHANGSX " +
                "FROM DMATHANG m " +
                "LEFT JOIN DNHOMMATHANG nh ON m.DNHOMMATHANGID = nh.ID " +
                "LEFT JOIN DDONVITINH dv1 ON m.DDONVITINHID = dv1.ID " +
                "LEFT JOIN DDONVITINH dv2 ON m.DDONVITINHCHANID = dv2.ID " +
                "LEFT JOIN DHANGSANXUAT hsx ON m.DHANGSANXUATID = hsx.ID " +
                "WHERE (m.STATUS IS NULL OR m.STATUS >= 0) " +
                "ORDER BY m.CODE ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(itemSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                kazuko.ex.model.khohang.TonKhoItem item = new kazuko.ex.model.khohang.TonKhoItem();
                String mhId = rs.getString("ID");
                item.setMatHangId(mhId);
                item.setMaHang(rs.getString("MA"));
                item.setTenHang(rs.getString("TENHANG"));
                item.setNhomHangId(rs.getString("DNHOMMATHANGID"));
                item.setNhomHangName(rs.getString("TENNHOM"));
                item.setDonViTinh(rs.getString("TENDVT1"));
                item.setDonViTinh2(rs.getString("TENDVT2"));
                item.setGiaBan(rs.getDouble("GIABAN"));
                item.setGiaVon(rs.getDouble("GIAVON"));
                item.setBaoHanh(rs.getString("BAOHANH"));
                item.setHangSx(rs.getString("TENHANGSX"));
                item.setTonToiThieu(rs.getDouble("TONTOITHIEU"));
                double quyDoi = rs.getDouble("QUYDOI");
                item.setQuyDoi(quyDoi > 0 ? quyDoi : 1.0);

                double ton = stockMap.getOrDefault(mhId != null ? mhId.trim() : "", 0.0);
                item.setTon(ton);

                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonKhoDanhSach: " + e.getMessage());
        }

        return list;
    }

    public List<kazuko.ex.model.khohang.TheKhoEntry> getTheKho(String matHangId, String khoId, java.util.Date tuNgay, java.util.Date denNgay) {
        List<kazuko.ex.model.khohang.TheKhoEntry> list = new ArrayList<>();
        if (matHangId == null || matHangId.trim().isEmpty()) return list;

        StringBuilder sql = new StringBuilder(
                "SELECT d.ID, d.NAME AS SOPHIEU, d.NGAY, d.LOAI, d.DIENGIAI, " +
                "kh.NAME AS TENKHACH, ncc.NAME AS TENNHACUNGCAP, " +
                "d.DKHOXUATID, d.DKHONHAPID, kx.NAME AS TENKHOXUAT, kn.NAME AS TENKHONHAP, " +
                "c.DONGIA, c.THANHTIEN, c.TILEGIAMGIA, c.SLNHAP, c.SLXUAT, " +
                "dv.NAME AS TENDVT " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "LEFT JOIN DKHACHHANG kh ON d.DKHACHHANGID = kh.ID " +
                "LEFT JOIN DNHACUNGCAP ncc ON d.DNHACUNGCAPID = ncc.ID " +
                "LEFT JOIN DDONVITINH dv ON c.DDONVITINHID = dv.ID " +
                "LEFT JOIN DKHOHANG kx ON d.DKHOXUATID = kx.ID " +
                "LEFT JOIN DKHOHANG kn ON d.DKHONHAPID = kn.ID " +
                "WHERE c.DMATHANGID = ? AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (khoId != null && !khoId.trim().isEmpty()) {
            sql.append("AND (d.DKHOXUATID = ? OR d.DKHONHAPID = ?) ");
        }
        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        sql.append("ORDER BY d.NGAY ASC, d.TIMECREATED ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int pIdx = 1;
            ps.setString(pIdx++, matHangId.trim());
            if (khoId != null && !khoId.trim().isEmpty()) {
                ps.setString(pIdx++, khoId.trim());
                ps.setString(pIdx++, khoId.trim());
            }
            if (tuNgay != null) {
                ps.setTimestamp(pIdx++, new Timestamp(tuNgay.getTime()));
            }
            if (denNgay != null) {
                long endOfDay = denNgay.getTime() + 86399999L;
                ps.setTimestamp(pIdx++, new Timestamp(endOfDay));
            }

            double runningBalance = 0.0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    kazuko.ex.model.khohang.TheKhoEntry entry = new kazuko.ex.model.khohang.TheKhoEntry();
                    entry.setSoPhieu(rs.getString("SOPHIEU"));
                    entry.setNgay(rs.getTimestamp("NGAY"));
                    entry.setDonGia(rs.getDouble("DONGIA"));
                    entry.setThanhTien(rs.getDouble("THANHTIEN"));
                    entry.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    entry.setTenDonViTinh(rs.getString("TENDVT"));

                    int loai = rs.getInt("LOAI");
                    double slNhap = rs.getDouble("SLNHAP");
                    double slXuat = rs.getDouble("SLXUAT");
                    String kxId = rs.getString("DKHOXUATID");
                    String knId = rs.getString("DKHONHAPID");
                    String tenKx = rs.getString("TENKHOXUAT");
                    String tenKn = rs.getString("TENKHONHAP");

                    String doiTuong = "";
                    if (loai == 3) { // Chuyển kho
                        if (khoId != null && !khoId.trim().isEmpty()) {
                            if (khoId.trim().equalsIgnoreCase(kxId != null ? kxId.trim() : "")) {
                                slNhap = 0;
                                doiTuong = "Chuyển tới: " + (tenKn != null ? tenKn : "");
                            } else {
                                slXuat = 0;
                                doiTuong = "Chuyển từ: " + (tenKx != null ? tenKx : "");
                            }
                        } else {
                            doiTuong = (tenKx != null ? tenKx : "") + " -> " + (tenKn != null ? tenKn : "");
                        }
                    } else if (loai == 1) { // Nhập kho
                        doiTuong = rs.getString("TENNHACUNGCAP");
                    } else if (loai == 4) { // Kiểm kê
                        doiTuong = "Kiểm kê kho";
                    } else { // Bán lẻ, xuất kho
                        doiTuong = rs.getString("TENKHACH");
                    }

                    entry.setSlNhap(slNhap);
                    entry.setSlXuat(slXuat);
                    entry.setDoiTuong(doiTuong != null ? doiTuong : "");

                    String dienGiai = rs.getString("DIENGIAI");
                    if (dienGiai == null || dienGiai.trim().isEmpty()) {
                        if (loai == 0) dienGiai = "Bán hàng";
                        else if (loai == 1) dienGiai = "Nhập kho";
                        else if (loai == 2) dienGiai = "Xuất kho";
                        else if (loai == 3) dienGiai = "Chuyển kho";
                        else if (loai == 4) dienGiai = "Kiểm kê";
                    }
                    entry.setDienGiai(dienGiai);

                    runningBalance += (slNhap - slXuat);
                    entry.setTonLuyKe(runningBalance);

                    list.add(entry);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTheKho: " + e.getMessage());
        }

        return list;
    }

    public List<Object[]> getTonTheoHanDung(String matHangId, String khoId) {
        List<Object[]> list = new ArrayList<>();
        if (matHangId == null || matHangId.trim().isEmpty()) return list;

        StringBuilder sql = new StringBuilder(
                "SELECT c.HANSUDUNG, SUM(c.SLNHAP - c.SLXUAT) AS TON " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE c.DMATHANGID = ? AND c.HANSUDUNG IS NOT NULL AND TRIM(c.HANSUDUNG) <> '' " +
                "AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        if (khoId != null && !khoId.trim().isEmpty()) {
            sql.append("AND (d.DKHONHAPID = ? OR d.DKHOXUATID = ?) ");
        }
        sql.append("GROUP BY c.HANSUDUNG HAVING SUM(c.SLNHAP - c.SLXUAT) <> 0 ORDER BY c.HANSUDUNG ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int pIdx = 1;
            ps.setString(pIdx++, matHangId.trim());
            if (khoId != null && !khoId.trim().isEmpty()) {
                ps.setString(pIdx++, khoId.trim());
                ps.setString(pIdx++, khoId.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                            rs.getString("HANSUDUNG"),
                            rs.getDouble("TON")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonTheoHanDung: " + e.getMessage());
        }

        return list;
    }

    public List<kazuko.ex.model.khohang.TonNhieuKhoItem> getTonNhieuKhoMatrix() {
        List<kazuko.ex.model.khohang.TonNhieuKhoItem> list = new ArrayList<>();
        Map<String, Map<String, Double>> matrixStock = new HashMap<>();

        // 1. Quét toàn bộ chi tiết xuất nhập phân nhóm theo mặt hàng và kho
        String sqlNhap = "SELECT c.DMATHANGID, d.DKHONHAPID, SUM(c.SLNHAP) AS TONGNHAP " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DKHONHAPID IS NOT NULL AND c.SLNHAP > 0 " +
                "GROUP BY c.DMATHANGID, d.DKHONHAPID";

        String sqlXuat = "SELECT c.DMATHANGID, d.DKHOXUATID, SUM(c.SLXUAT) AS TONGXUAT " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DKHOXUATID IS NOT NULL AND c.SLXUAT > 0 " +
                "GROUP BY c.DMATHANGID, d.DKHOXUATID";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlNhap);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mhId = rs.getString("DMATHANGID");
                    String kId = rs.getString("DKHONHAPID");
                    double sl = rs.getDouble("TONGNHAP");
                    if (mhId != null && kId != null) {
                        matrixStock.computeIfAbsent(mhId.trim(), k -> new HashMap<>())
                                .merge(kId.trim(), sl, Double::sum);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlXuat);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mhId = rs.getString("DMATHANGID");
                    String kId = rs.getString("DKHOXUATID");
                    double sl = rs.getDouble("TONGXUAT");
                    if (mhId != null && kId != null) {
                        matrixStock.computeIfAbsent(mhId.trim(), k -> new HashMap<>())
                                .merge(kId.trim(), -sl, Double::sum);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonNhieuKhoMatrix stock: " + e.getMessage());
        }

        // 2. Lấy danh mục mặt hàng
        String itemSql = "SELECT m.ID, m.CODE AS MA, m.NAME AS TENHANG, m.DNHOMMATHANGID, nh.NAME AS TENNHOM, " +
                "m.QUYDOI, m.BAOHANH, m.TONTOITHIEU, " +
                "dv1.NAME AS TENDVT1, dv2.NAME AS TENDVT2, hsx.NAME AS TENHANGSX " +
                "FROM DMATHANG m " +
                "LEFT JOIN DNHOMMATHANG nh ON m.DNHOMMATHANGID = nh.ID " +
                "LEFT JOIN DDONVITINH dv1 ON m.DDONVITINHID = dv1.ID " +
                "LEFT JOIN DDONVITINH dv2 ON m.DDONVITINHCHANID = dv2.ID " +
                "LEFT JOIN DHANGSANXUAT hsx ON m.DHANGSANXUATID = hsx.ID " +
                "WHERE (m.STATUS IS NULL OR m.STATUS >= 0) " +
                "ORDER BY m.CODE ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(itemSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                kazuko.ex.model.khohang.TonNhieuKhoItem item = new kazuko.ex.model.khohang.TonNhieuKhoItem();
                String mhId = rs.getString("ID");
                item.setMatHangId(mhId);
                item.setMaHang(rs.getString("MA"));
                item.setTenHang(rs.getString("TENHANG"));
                item.setNhomHangId(rs.getString("DNHOMMATHANGID"));
                item.setNhomHangName(rs.getString("TENNHOM"));
                item.setDonViTinh(rs.getString("TENDVT1"));
                item.setDonViTinh2(rs.getString("TENDVT2"));
                item.setBaoHanh(rs.getString("BAOHANH"));
                item.setHangSx(rs.getString("TENHANGSX"));
                item.setTonToiThieu(rs.getDouble("TONTOITHIEU"));
                double quyDoi = rs.getDouble("QUYDOI");
                item.setQuyDoi(quyDoi > 0 ? quyDoi : 1.0);

                Map<String, Double> khoStockMap = matrixStock.getOrDefault(mhId != null ? mhId.trim() : "", new HashMap<>());
                double totalTon = 0.0;
                for (Map.Entry<String, Double> entry : khoStockMap.entrySet()) {
                    item.setTonKho(entry.getKey(), entry.getValue());
                    totalTon += entry.getValue();
                }
                item.setTongTon(totalTon);

                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonNhieuKhoMatrix itemSql: " + e.getMessage());
        }

        return list;
    }
}
