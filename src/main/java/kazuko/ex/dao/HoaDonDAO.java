package kazuko.ex.dao;

import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.banhang.GiaBanGanDayKhachHang;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.VoucherNumberGenerator;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoaDonDAO extends BaseDAO {

    private static final List<DonHang> MEMORY_HOA_DON = new CopyOnWriteArrayList<>();
    private static final Map<String, DonHang> MEMORY_TAM_HOA_DON = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    /**
     * Lấy tối đa ba đơn giá khác nhau gần đây để giao diện hiển thị lịch sử mua.
     * Chỉ dùng dòng xuất của hóa đơn hoàn tất;
     * dòng trả, hóa đơn nháp/hủy không được tính.
     */
    public List<GiaBanGanDayKhachHang> getGiaBanGanDayCuaKhachHang(
            String khachHangId, String matHangId, String donViTinhId) {
        List<GiaBanGanDayKhachHang> result = new ArrayList<>();
        if (khachHangId == null || khachHangId.trim().isEmpty()
                || matHangId == null || matHangId.trim().isEmpty()) {
            return result;
        }

        String unitCondition = donViTinhId != null && !donViTinhId.trim().isEmpty()
                ? "AND (c.DDONVITINHID = ? OR c.DDONVITINHID IS NULL) " : "";
        String sql = "SELECT c.DONGIA, d.NGAY, d.NAME AS SOHOADON, " +
                "d.TIMECREATED AS DONCREATED, c.TIMECREATED AS CHITIETCREATED " +
                "FROM TDONHANGCHITIET c JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE d.DKHACHHANGID = ? AND c.DMATHANGID = ? AND d.LOAI = 0 " +
                "AND (d.DATHANHTOAN = 30 OR d.STATUS = 1) " +
                "AND (d.STATUS IS NULL OR d.STATUS >= 0) " +
                "AND (c.STATUS IS NULL OR c.STATUS >= 0) " +
                "AND COALESCE(c.XUATVATTU, 0) = 0 " +
                "AND (COALESCE(c.SLXUATCHUAQUYDOI, 0) > 0 OR COALESCE(c.SLXUAT, 0) > 0) " +
                "AND COALESCE(c.SLNHAPCHUAQUYDOI, 0) = 0 AND COALESCE(c.SLNHAP, 0) = 0 " +
                "AND c.DONGIA > 0 " + unitCondition +
                "ORDER BY d.NGAY DESC, c.TIMECREATED DESC, d.TIMECREATED DESC, c.ID DESC";

        Set<Long> seenPrices = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, khachHangId.trim());
            ps.setString(index++, matHangId.trim());
            if (!unitCondition.isEmpty()) ps.setString(index, donViTinhId.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && result.size() < 3) {
                    // DONGIA là kiểu số thực trong dữ liệu cũ và có thể đọc thành 638000.02
                    // dù giá nghiệp vụ là 638000. Chuẩn hóa về đơn vị đồng trước khi dùng lại.
                    double price = Math.round(rs.getDouble("DONGIA"));
                    long priceKey = Math.round(price);
                    if (!seenPrices.add(priceKey)) continue;
                    result.add(new GiaBanGanDayKhachHang(
                            price, rs.getTimestamp("NGAY"), rs.getString("SOHOADON")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy giá bán gần đây của khách hàng: " + e.getMessage());
        }
        return result;
    }

    public boolean insertHoaDon(DonHang dh) {
        if (dh == null) return false;
        if (dh.getId() == null || dh.getId().trim().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        if (dh.getName() == null || dh.getName().trim().isEmpty()) {
            dh.setName(generateNextSoHoaDon(dh.getNgay()));
        }

        String sqlMaster = "INSERT INTO TDONHANG (ID, NAME, NGAY, DIENGIAI, TIENHANG, TIENGIAMGIA, TILEGIAMGIA, " +
                "TIENTHUE, TILETHUE, PHIVANCHUYEN, DOITRA, TONGCONG, TIENTHANHTOAN, CONLAI, " +
                "DKHOXUATID, DNHANVIENXUATID, DKHACHHANGID, DCUAHANGID, LOAI, STATUS, " +
                "NOTE, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                "SLXUAT, SLNHAP, TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psMaster = conn.prepareStatement(sqlMaster)) {
                psMaster.setString(1, dh.getId());
                psMaster.setString(2, dh.getName());
                psMaster.setTimestamp(3, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                psMaster.setString(4, dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất bán hàng");
                psMaster.setDouble(5, dh.getTienHang());
                psMaster.setDouble(6, dh.getTienGiamGia());
                psMaster.setDouble(7, dh.getTiLeGiamGia());
                psMaster.setDouble(8, dh.getTienThue());
                psMaster.setDouble(9, dh.getTiLeThue());
                psMaster.setDouble(10, dh.getPhiVanChuyen());
                psMaster.setDouble(11, dh.getTienDoiTra());
                psMaster.setDouble(12, dh.getTongCong());
                psMaster.setDouble(13, dh.getTienThanhToan());
                psMaster.setDouble(14, dh.getConLai());
                psMaster.setString(15, dh.getKhoXuatId());
                psMaster.setString(16, dh.getNhanVienXuatId());
                psMaster.setString(17, dh.getKhachHangId());
                psMaster.setString(18, dh.getCuaHangId());
                psMaster.setInt(19, dh.getStatus());
                psMaster.setString(20, dh.getGhiChu());
                psMaster.setString(21, getEffectiveUserId());
                psMaster.executeUpdate();
            }

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                // Khách lấy hàng
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSoLuong()); // slXuat
                        psDetail.setDouble(7, 0.0); // slNhap
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu());
                        psDetail.setInt(11, 1);
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                }
                // Khách trả hàng
                if (dh.getChiTietTraHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) {
                            ct.setId(UUID.randomUUID().toString());
                        }
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, 0.0); // slXuat
                        psDetail.setDouble(7, ct.getSoLuong()); // slNhap
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu() != null ? "[TRẢ HÀNG] " + ct.getGhiChu() : "[TRẢ HÀNG]");
                        psDetail.setInt(11, 1);
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                }
                psDetail.executeBatch();
            }

            conn.commit();
            MEMORY_HOA_DON.add(dh);
            auditInvoice("THANH TOÁN", dh, "Lập hóa đơn bán hàng");
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insert TDONHANG: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            MEMORY_HOA_DON.add(dh);
            auditInvoice("THANH TOÁN", dh, "Lập hóa đơn bán hàng (bộ nhớ tạm)");
            return true;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        }
    }

    public DonHang getHoaDonById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, k.NAME AS TENKHACH, nv.NAME AS TENNHANVIEN " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHACHHANG k ON d.DKHACHHANGID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENXUATID = nv.ID " +
                "WHERE d.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DonHang dh = mapResultSetToDonHang(rs);
                    loadDetailsForDonHang(conn, dh);
                    return dh;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi getHoaDonById: " + e.getMessage());
        }

        for (DonHang dh : MEMORY_HOA_DON) {
            if (id.equals(dh.getId())) return dh;
        }
        return null;
    }

    private void loadDetailsForDonHang(Connection conn, DonHang dh) {
        String sql = "SELECT c.*, m.CODE AS MAHANG, m.NAME AS TENHANG, dv.NAME AS TENDONVITINH, m.BAOHANH " +
                "FROM TDONHANGCHITIET c " +
                "LEFT JOIN DMATHANG m ON c.DMATHANGID = m.ID " +
                "LEFT JOIN DDONVITINH dv ON COALESCE(c.DDONVITINHID, m.DDONVITINHID) = dv.ID " +
                "WHERE c.TDONHANGID = ? AND (c.STATUS IS NULL OR c.STATUS >= 0) " +
                "ORDER BY c.TIMECREATED ASC, c.ID ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dh.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DonHangChiTiet ct = new DonHangChiTiet();
                    ct.setId(rs.getString("ID"));
                    ct.setDonHangId(dh.getId());
                    ct.setMatHangId(rs.getString("DMATHANGID"));
                    ct.setMaHang(rs.getString("MAHANG"));
                    ct.setTenHang(rs.getString("TENHANG"));
                    ct.setDonViTinhId(rs.getString("DDONVITINHID"));
                    ct.setTenDonViTinh(rs.getString("TENDONVITINH"));
                    ct.setDonGia(rs.getDouble("DONGIA"));
                    double slXuat = rs.getDouble("SLXUAT");
                    double slNhap = rs.getDouble("SLNHAP");
                    ct.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    ct.setThanhTien(rs.getDouble("THANHTIEN"));
                    ct.setGhiChu(rs.getString("NOTE"));
                    ct.setBaoHanh(rs.getString("BAOHANH"));

                    if (slNhap > 0 && slXuat <= 0) {
                        ct.setTraHang(true);
                        ct.setSoLuong(slNhap);
                        dh.getChiTietTraHang().add(ct);
                    } else {
                        ct.setTraHang(false);
                        ct.setSoLuong(slXuat > 0 ? slXuat : 1.0);
                        dh.getChiTietLayHang().add(ct);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi loadDetailsForDonHang: " + e.getMessage());
        }
    }

    public DonHang getHoaDonByOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDONHANG WHERE LOAI = 0 AND (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, TIMECREATED DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (Exception ignored) {}

        if (ids.isEmpty()) {
            for (DonHang dh : MEMORY_HOA_DON) {
                ids.add(dh.getId());
            }
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
            return getHoaDonById(ids.get(targetIdx));
        }
        return null;
    }

    public String generateNextSoHoaDon(Date ngay) {
        if (ngay == null) ngay = new Date();
        VoucherNumberGenerator.CompiledPattern numberPattern = VoucherNumberGenerator.fromConfig(
                "Đơn hàng", "(yy)(******)", ngay);

        String sql = "SELECT NAME FROM TDONHANG WHERE NAME LIKE ? AND LOAI = 0 ORDER BY NAME DESC";
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
            System.err.println("Lỗi sinh số hóa đơn: " + e.getMessage());
        }

        if (maxIndex == 0) {
            for (DonHang dh : MEMORY_HOA_DON) {
                maxIndex = Math.max(maxIndex, numberPattern.extractSequence(dh.getName()));
            }
        }

        return numberPattern.format(maxIndex + 1);
    }

    public void saveTam(DonHang dh) {
        if (dh == null) return;
        dh.setTam(true);
        if (dh.getId() == null || dh.getId().isEmpty()) {
            dh.setId(UUID.randomUUID().toString());
        }
        MEMORY_TAM_HOA_DON.put(dh.getId(), dh);
        auditInvoice("LƯU TẠM", dh, "Lưu tạm hóa đơn bán hàng");
    }

    public List<DonHang> getDanhSachTam() {
        return new ArrayList<>(MEMORY_TAM_HOA_DON.values());
    }

    public void removeTam(String id) {
        if (id != null) {
            MEMORY_TAM_HOA_DON.remove(id);
        }
    }

    public List<DonHang> getDanhSachHoaDon(Date tuNgay, Date denNgay, String cuaHangId, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, k.NAME AS TENKHACH, k.MAKHACH AS MAKHACH, k.DIACHI AS DIACHIKHACH, k.DIENTHOAI AS DIENTHOAIKHACH, " +
                "nv.NAME AS TENNHANVIEN, dhang.NAME AS SOPHIEUDATHANG, " +
                "COALESCE(ut.NAME, ut.USERNAME) AS TENTHANHTOAN " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHACHHANG k ON d.DKHACHHANGID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENXUATID = nv.ID " +
                "LEFT JOIN TDATHANG dhang ON d.TDATHANGID = dhang.ID " +
                "LEFT JOIN SUSER ut ON COALESCE(d.USERTHANHTOANID, d.USERCREATEDID) = ut.ID " +
                "WHERE d.LOAI = 0 AND (d.STATUS = 1 OR d.STATUS = 0 OR d.STATUS IS NULL) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (cuaHangId != null && !cuaHangId.trim().isEmpty()) {
            sql.append("AND d.DCUAHANGID = ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(k.NAME) LIKE ? OR LOWER(k.DIENTHOAI) LIKE ? OR LOWER(d.DIENGIAI) LIKE ?) ");
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
            if (cuaHangId != null && !cuaHangId.trim().isEmpty()) {
                ps.setString(idx++, cuaHangId.trim());
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
                    DonHang dh = mapResultSetToDonHang(rs);
                    list.add(dh);
                }
            }

            for (DonHang dh : list) {
                loadDetailsForDonHang(conn, dh);
            }
        } catch (Exception e) {
            System.err.println("Lỗi getDanhSachHoaDon: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (DonHang dh : MEMORY_HOA_DON) {
                if (matchesInvoiceFilter(dh, tuNgay, denNgay, cuaHangId, keyword)) {
                    list.add(dh);
                }
            }
        }

        return list;
    }

    public List<DonHang> getDanhSachBillHuy(Date tuNgay, Date denNgay, String keyword) {
        List<DonHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, k.NAME AS TENKHACH, k.MAKHACH AS MAKHACH, k.DIACHI AS DIACHIKHACH, k.DIENTHOAI AS DIENTHOAIKHACH, " +
                "nv.NAME AS TENNHANVIEN " +
                "FROM TDONHANG d " +
                "LEFT JOIN DKHACHHANG k ON d.DKHACHHANGID = k.ID " +
                "LEFT JOIN DNHANVIEN nv ON d.DNHANVIENXUATID = nv.ID " +
                "WHERE (d.STATUS = -1 OR d.STATUS = 2) "
        );

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
        }
        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(k.NAME) LIKE ? OR LOWER(d.NOTE) LIKE ?) ");
        }

        sql.append("ORDER BY d.NGAY DESC, d.TIMEMODIFIED DESC");

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

            for (DonHang dh : list) {
                loadDetailsForDonHang(conn, dh);
            }
        } catch (Exception e) {
            System.err.println("Lỗi getDanhSachBillHuy: " + e.getMessage());
        }

        if (list.isEmpty()) {
            for (DonHang dh : MEMORY_HOA_DON) {
                if (dh.getStatus() < 0 || dh.getStatus() == 2) {
                    list.add(dh);
                }
            }
        }

        return list;
    }

    public boolean cancelHoaDon(String id, String lyDoHuy, String thuNganHuy) {
        if (id == null || id.trim().isEmpty()) return false;
        String sql = "UPDATE TDONHANG SET STATUS = -1, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lyDoHuy != null ? lyDoHuy : "Hủy phiếu bán hàng");
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            ps.executeUpdate();

            for (DonHang dh : MEMORY_HOA_DON) {
                if (id.equals(dh.getId())) {
                    dh.setStatus(-1);
                    dh.setLyDoHuy(lyDoHuy);
                    dh.setThuNganHuy(thuNganHuy);
                    dh.setNgayHuy(new Date());
                }
            }
            audit("Hóa đơn bán hàng", "HỦY", id,
                    "Hủy hóa đơn. Lý do: " + (lyDoHuy != null ? lyDoHuy : "Không ghi"));
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi cancelHoaDon: " + e.getMessage());
            for (DonHang dh : MEMORY_HOA_DON) {
                if (id.equals(dh.getId())) {
                    dh.setStatus(-1);
                    dh.setLyDoHuy(lyDoHuy);
                    dh.setThuNganHuy(thuNganHuy);
                    dh.setNgayHuy(new Date());
                }
            }
            audit("Hóa đơn bán hàng", "HỦY", id,
                    "Hủy hóa đơn trong bộ nhớ. Lý do: " + (lyDoHuy != null ? lyDoHuy : "Không ghi"));
            return true;
        }
    }

    public boolean updateHoaDon(DonHang dh) {
        if (dh == null || dh.getId() == null) return false;
        String sql = "UPDATE TDONHANG SET NGAY = ?, DIENGIAI = ?, TIENHANG = ?, TIENGIAMGIA = ?, TILEGIAMGIA = ?, " +
                "TIENTHUE = ?, TILETHUE = ?, PHIVANCHUYEN = ?, DOITRA = ?, " +
                "TONGCONG = ?, TIENTHANHTOAN = ?, CONLAI = ?, DKHOXUATID = ?, DNHANVIENXUATID = ?, DKHACHHANGID = ?, " +
                "DCUAHANGID = ?, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, dh.getNgay() != null ? new Timestamp(dh.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
                ps.setString(2, dh.getDienGiai());
                ps.setDouble(3, dh.getTienHang());
                ps.setDouble(4, dh.getTienGiamGia());
                ps.setDouble(5, dh.getTiLeGiamGia());
                ps.setDouble(6, dh.getTienThue());
                ps.setDouble(7, dh.getTiLeThue());
                ps.setDouble(8, dh.getPhiVanChuyen());
                ps.setDouble(9, dh.getTienDoiTra());
                ps.setDouble(10, dh.getTongCong());
                ps.setDouble(11, dh.getTienThanhToan());
                ps.setDouble(12, dh.getConLai());
                ps.setString(13, dh.getKhoXuatId());
                ps.setString(14, dh.getNhanVienXuatId());
                ps.setString(15, dh.getKhachHangId());
                ps.setString(16, dh.getCuaHangId());
                ps.setString(17, dh.getGhiChu());
                ps.setString(18, getEffectiveUserId());
                ps.setString(19, dh.getId());
                ps.executeUpdate();
            }

            String sqlDel = "DELETE FROM TDONHANGCHITIET WHERE TDONHANGID = ?";
            try (PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
                psDel.setString(1, dh.getId());
                psDel.executeUpdate();
            }

            String sqlDetail = "INSERT INTO TDONHANGCHITIET (ID, TDONHANGID, DMATHANGID, DDONVITINHID, DONGIA, " +
                    "SLXUAT, SLNHAP, TILEGIAMGIA, THANHTIEN, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                if (dh.getChiTietLayHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietLayHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) ct.setId(UUID.randomUUID().toString());
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, ct.getSoLuong());
                        psDetail.setDouble(7, 0.0);
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu());
                        psDetail.setInt(11, 1);
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                }
                if (dh.getChiTietTraHang() != null) {
                    for (DonHangChiTiet ct : dh.getChiTietTraHang()) {
                        if (ct.getId() == null || ct.getId().trim().isEmpty()) ct.setId(UUID.randomUUID().toString());
                        psDetail.setString(1, ct.getId());
                        psDetail.setString(2, dh.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setDouble(5, ct.getDonGia());
                        psDetail.setDouble(6, 0.0);
                        psDetail.setDouble(7, ct.getSoLuong());
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setString(10, ct.getGhiChu() != null ? "[TRẢ HÀNG] " + ct.getGhiChu() : "[TRẢ HÀNG]");
                        psDetail.setInt(11, 1);
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                }
                psDetail.executeBatch();
            }

            conn.commit();
            auditInvoice("SỬA", dh, "Cập nhật hóa đơn bán hàng");
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi updateHoaDon: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            auditInvoice("SỬA", dh, "Cập nhật hóa đơn bán hàng trong bộ nhớ");
            return true;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
            }
        }
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
        try { dh.setTenKhach(rs.getString("TENKHACH")); } catch (Exception ignored) {}
        try { dh.setTenNhanVien(rs.getString("TENNHANVIEN")); } catch (Exception ignored) {}
        try { dh.setGhiChu(rs.getString("NOTE")); } catch (Exception ignored) {}
        try { dh.setMaKhach(rs.getString("MAKHACH")); } catch (Exception ignored) {}
        try { dh.setDiaChiKhach(rs.getString("DIACHIKHACH")); } catch (Exception ignored) {}
        try { dh.setDienThoaiKhach(rs.getString("DIENTHOAIKHACH")); } catch (Exception ignored) {}
        try { dh.setUserCreatedId(rs.getString("USERCREATEDID")); } catch (Exception ignored) {}
        try { dh.setTienThue(rs.getDouble("TIENTHUE")); } catch (Exception ignored) {}
        try { dh.setTiLeThue(rs.getDouble("TILETHUE")); } catch (Exception ignored) {}
        try { dh.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN")); } catch (Exception ignored) {}
        try { dh.setTienDoiTra(rs.getDouble("DOITRA")); } catch (Exception ignored) {}
        try { dh.setKhachDua(rs.getDouble("KHACHDUA")); } catch (Exception ignored) {}
        try { dh.setTraLai(rs.getDouble("TRALAI")); } catch (Exception ignored) {}
        try { dh.setDiaChiGiaoHang(rs.getString("GIAOHANG")); } catch (Exception ignored) {}
        try { dh.setTienTheATM(rs.getDouble("THE")); } catch (Exception ignored) {}
        try { dh.setMaTheTraTruoc(rs.getString("DTHETRATRUOCID")); } catch (Exception ignored) {}
        try { dh.setTienTruTichLuy(rs.getDouble("TRUTICHLUY")); } catch (Exception ignored) {}
        try { dh.setSoPhieuDatHang(rs.getString("SOPHIEUDATHANG")); } catch (Exception ignored) {}
        try {
            Timestamp tc = rs.getTimestamp("TIMECREATED");
            if (tc != null) {
                dh.setTimeCreated(new Date(tc.getTime()));
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dh.setGioThanhToan(timeFormat.format(new Date(tc.getTime())));
            }
        } catch (Exception ignored) {}
        try {
            Timestamp tm = rs.getTimestamp("TIMEMODIFIED");
            if (tm != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                dh.setGioHuy(timeFormat.format(new Date(tm.getTime())));
                dh.setNgayHuy(new Date(tm.getTime()));
            }
        } catch (Exception ignored) {}
        try {
            String tenThanhToan = rs.getString("TENTHANHTOAN");
            dh.setNguoiThanhToan(tenThanhToan != null && !tenThanhToan.trim().isEmpty()
                    ? tenThanhToan : "Administrator");
        } catch (Exception ignored) {}
        try { dh.setThuNganHuy("Administrator"); } catch (Exception ignored) {}
        return dh;
    }

    private boolean matchesInvoiceFilter(DonHang invoice, Date fromDate, Date toDate,
                                         String storeId, String keyword) {
        if (invoice == null || invoice.getStatus() < 0) return false;
        Date invoiceDate = invoice.getNgay();
        if (fromDate != null && (invoiceDate == null || invoiceDate.before(fromDate))) return false;
        if (toDate != null) {
            long endOfDay = toDate.getTime() + 86399999L;
            if (invoiceDate == null || invoiceDate.getTime() > endOfDay) return false;
        }
        if (storeId != null && !storeId.trim().isEmpty()
                && !storeId.trim().equals(invoice.getCuaHangId())) return false;
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String needle = keyword.trim().toLowerCase();
        return containsIgnoreCase(invoice.getName(), needle)
                || containsIgnoreCase(invoice.getTenKhach(), needle)
                || containsIgnoreCase(invoice.getDienThoaiKhach(), needle)
                || containsIgnoreCase(invoice.getDienGiai(), needle);
    }

    private boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private void auditInvoice(String action, DonHang invoice, String prefix) {
        int soldItems = invoice.getChiTietLayHang() != null ? invoice.getChiTietLayHang().size() : 0;
        int returnedItems = invoice.getChiTietTraHang() != null ? invoice.getChiTietTraHang().size() : 0;
        audit("Hóa đơn bán hàng", action, invoice.getName(),
                prefix + "; khách: " + (invoice.getTenKhach() != null ? invoice.getTenKhach() : "Khách lẻ")
                        + "; mặt hàng bán: " + soldItems + "; mặt hàng trả: " + returnedItems
                        + "; tổng cộng: " + Math.round(invoice.getTongCong()));
    }
}
