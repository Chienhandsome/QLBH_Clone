package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.dathang.DatHangNCC;
import kazuko.ex.model.dathang.DatHangNCCChiTiet;
import kazuko.ex.service.AuthService;
import kazuko.ex.service.ActivityLogService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DatHangNCCDAO {

    private void audit(String module, String action, String referenceNo, String content) {
        ActivityLogService.log(module, action, referenceNo, content);
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null) {
            return AuthService.getCurrentUser().getId();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public synchronized String generateNextSoPhieu(Date date) {
        if (date == null) date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String yy = sdf.format(date);
        String prefix = "DMH" + yy + "/";

        String sql = "SELECT NAME FROM TDATHANGNCC WHERE NAME LIKE ? ORDER BY NAME DESC";
        int maxSeq = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    if (name != null && name.startsWith(prefix)) {
                        String numPart = name.substring(prefix.length()).trim();
                        try {
                            int seq = Integer.parseInt(numPart);
                            if (seq > maxSeq) maxSeq = seq;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi generateNextSoPhieu DatHangNCC: " + e.getMessage());
        }

        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public List<DatHangNCC> getAll(Date tuNgay, Date denNgay, String nhaCungCapId, String keyword) {
        List<DatHangNCC> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, ncc.NAME AS TENNHACUNGCAP, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM TDATHANGNCC d " +
                "LEFT JOIN DNHACUNGCAP ncc ON d.DNHACUNGCAPID = ncc.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) "
        );

        List<Object> params = new ArrayList<>();

        if (tuNgay != null) {
            sql.append("AND d.NGAY >= ? ");
            Calendar cal = Calendar.getInstance();
            cal.setTime(tuNgay);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            params.add(new Timestamp(cal.getTimeInMillis()));
        }

        if (denNgay != null) {
            sql.append("AND d.NGAY <= ? ");
            Calendar cal = Calendar.getInstance();
            cal.setTime(denNgay);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            params.add(new Timestamp(cal.getTimeInMillis()));
        }

        if (nhaCungCapId != null && !nhaCungCapId.trim().isEmpty()) {
            sql.append("AND d.DNHACUNGCAPID = ? ");
            params.add(nhaCungCapId.trim());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(d.NAME) LIKE ? OR LOWER(ncc.NAME) LIKE ? OR LOWER(d.NOTE) LIKE ?) ");
            String kwPattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kwPattern);
            params.add(kwPattern);
            params.add(kwPattern);
        }

        sql.append("ORDER BY d.NGAY DESC, d.NAME DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDatHangNCC(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách đặt hàng nhà cung cấp: " + e.getMessage());
        }

        return list;
    }

    public List<DatHangNCC> getByMatHang(String matHangId, String matHangCode) {
        List<DatHangNCC> list = new ArrayList<>();
        if ((matHangId == null || matHangId.trim().isEmpty()) && (matHangCode == null || matHangCode.trim().isEmpty())) {
            return list;
        }

        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT d.*, ncc.NAME AS TENNHACUNGCAP, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM TDATHANGNCC d " +
                "INNER JOIN TDATHANGNCCCHITIET ct ON ct.TDATHANGNCCID = d.ID " +
                "LEFT JOIN DMATHANG m ON ct.DMATHANGID = m.ID " +
                "LEFT JOIN DNHACUNGCAP ncc ON d.DNHACUNGCAPID = ncc.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) " +
                "  AND (ct.STATUS IS NULL OR ct.STATUS >= 0) AND ("
        );

        List<Object> params = new ArrayList<>();
        boolean hasParam = false;
        if (matHangId != null && !matHangId.trim().isEmpty()) {
            sql.append("ct.DMATHANGID = ? ");
            params.add(matHangId.trim());
            hasParam = true;
        }
        if (matHangCode != null && !matHangCode.trim().isEmpty()) {
            if (hasParam) sql.append("OR ");
            sql.append("LOWER(m.CODE) = LOWER(?) ");
            params.add(matHangCode.trim());
        }
        sql.append(") ORDER BY d.NGAY DESC, d.NAME DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDatHangNCC(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getByMatHang DatHangNCC: " + e.getMessage());
        }
        return list;
    }

    public DatHangNCC getById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT d.*, ncc.NAME AS TENNHACUNGCAP, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM TDATHANGNCC d " +
                "LEFT JOIN DNHACUNGCAP ncc ON d.DNHACUNGCAPID = ncc.ID " +
                "LEFT JOIN SUSER u ON d.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON d.USERMODIFIEDID = um.ID " +
                "WHERE d.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DatHangNCC order = mapResultSetToDatHangNCC(rs);
                    order.setChiTiet(getChiTiet(order.getId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getById DatHangNCC: " + e.getMessage());
        }
        return null;
    }

    public List<DatHangNCCChiTiet> getChiTiet(String datHangNCCId) {
        List<DatHangNCCChiTiet> list = new ArrayList<>();
        if (datHangNCCId == null || datHangNCCId.trim().isEmpty()) return list;

        String sql = "SELECT ct.*, m.CODE AS MAHANG, m.NAME AS TENHANG, dvt.NAME AS TENDONVITINH " +
                "FROM TDATHANGNCCCHITIET ct " +
                "LEFT JOIN DMATHANG m ON ct.DMATHANGID = m.ID " +
                "LEFT JOIN DDONVITINH dvt ON ct.DDONVITINHID = dvt.ID " +
                "WHERE ct.TDATHANGNCCID = ? AND (ct.STATUS IS NULL OR ct.STATUS >= 0) " +
                "ORDER BY ct.ID ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, datHangNCCId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DatHangNCCChiTiet item = new DatHangNCCChiTiet();
                    item.setId(rs.getString("ID"));
                    item.setDatHangNCCId(rs.getString("TDATHANGNCCID"));
                    item.setMatHangId(rs.getString("DMATHANGID"));
                    item.setMaHang(rs.getString("MAHANG"));
                    item.setTenHang(rs.getString("TENHANG"));
                    item.setDonViTinhId(rs.getString("DDONVITINHID"));
                    item.setTenDonViTinh(rs.getString("TENDONVITINH"));
                    item.setBaoHanh(rs.getString("BAOHANH"));
                    item.setSoLuong(rs.getDouble("SOLUONG"));
                    item.setDonGia(rs.getDouble("DONGIA"));
                    item.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    item.setThanhTien(rs.getDouble("THANHTIEN"));
                    item.setGiaMuaGanNhat(rs.getDouble("GIAMUAGANNHAT"));
                    item.setNote(rs.getString("NOTE"));
                    item.setStatus(rs.getInt("STATUS"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTiet DatHangNCC: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(DatHangNCC order) {
        if (order == null) return false;
        if (order.getId() == null || order.getId().trim().isEmpty()) {
            order.setId(UUID.randomUUID().toString());
        }
        if (order.getName() == null || order.getName().trim().isEmpty()) {
            order.setName(generateNextSoPhieu(order.getNgay()));
        }

        String sqlHeader = "INSERT INTO TDATHANGNCC (ID, NAME, NGAY, DNHACUNGCAPID, GIAMTHEOTIEN, LOAI, " +
                "TIENHANG, TILEGIAMGIA, TIENGIAMGIA, TILETHUE, TIENTHUE, PHIVANCHUYEN, TONGCONG, NOTE, " +
                "STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        String sqlDetail = "INSERT INTO TDATHANGNCCCHITIET (ID, TDATHANGNCCID, DMATHANGID, DDONVITINHID, " +
                "BAOHANH, SOLUONG, DONGIA, TILEGIAMGIA, THANHTIEN, GIAMUAGANNHAT, NOTE, " +
                "STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, order.getId());
                ps.setString(2, order.getName());
                ps.setTimestamp(3, new Timestamp(order.getNgay() != null ? order.getNgay().getTime() : System.currentTimeMillis()));
                ps.setString(4, order.getNhaCungCapId());
                ps.setInt(5, order.getGiamTheoTien());
                ps.setInt(6, order.getLoai());
                ps.setDouble(7, order.getTienHang());
                ps.setDouble(8, order.getTiLeGiamGia());
                ps.setDouble(9, order.getTienGiamGia());
                ps.setDouble(10, order.getTiLeThue());
                ps.setDouble(11, order.getTienThue());
                ps.setDouble(12, order.getPhiVanChuyen());
                ps.setDouble(13, order.getTongCong());
                ps.setString(14, order.getNote());
                ps.setString(15, getEffectiveUserId());
                ps.executeUpdate();
            }

            if (order.getChiTiet() != null && !order.getChiTiet().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DatHangNCCChiTiet ct : order.getChiTiet()) {
                        String detailId = ct.getId();
                        if (detailId == null || detailId.trim().isEmpty()) {
                            detailId = UUID.randomUUID().toString();
                            ct.setId(detailId);
                        }
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, order.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setString(5, ct.getBaoHanh());
                        psDetail.setDouble(6, ct.getSoLuong());
                        psDetail.setDouble(7, ct.getDonGia());
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setDouble(10, ct.getGiaMuaGanNhat());
                        psDetail.setString(11, ct.getNote());
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            auditOrder("THÊM", order);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insert DatHangNCC: " + e.getMessage());
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

    public boolean update(DatHangNCC order) {
        if (order == null || order.getId() == null) return false;

        String sqlHeader = "UPDATE TDATHANGNCC SET NAME = ?, NGAY = ?, DNHACUNGCAPID = ?, GIAMTHEOTIEN = ?, " +
                "LOAI = ?, TIENHANG = ?, TILEGIAMGIA = ?, TIENGIAMGIA = ?, TILETHUE = ?, TIENTHUE = ?, " +
                "PHIVANCHUYEN = ?, TONGCONG = ?, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? " +
                "WHERE ID = ?";

        String sqlDeleteOldDetails = "DELETE FROM TDATHANGNCCCHITIET WHERE TDATHANGNCCID = ?";

        String sqlDetail = "INSERT INTO TDATHANGNCCCHITIET (ID, TDATHANGNCCID, DMATHANGID, DDONVITINHID, " +
                "BAOHANH, SOLUONG, DONGIA, TILEGIAMGIA, THANHTIEN, GIAMUAGANNHAT, NOTE, " +
                "STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                ps.setString(1, order.getName());
                ps.setTimestamp(2, new Timestamp(order.getNgay() != null ? order.getNgay().getTime() : System.currentTimeMillis()));
                ps.setString(3, order.getNhaCungCapId());
                ps.setInt(4, order.getGiamTheoTien());
                ps.setInt(5, order.getLoai());
                ps.setDouble(6, order.getTienHang());
                ps.setDouble(7, order.getTiLeGiamGia());
                ps.setDouble(8, order.getTienGiamGia());
                ps.setDouble(9, order.getTiLeThue());
                ps.setDouble(10, order.getTienThue());
                ps.setDouble(11, order.getPhiVanChuyen());
                ps.setDouble(12, order.getTongCong());
                ps.setString(13, order.getNote());
                ps.setString(14, getEffectiveUserId());
                ps.setString(15, order.getId());
                ps.executeUpdate();
            }

            try (PreparedStatement psDel = conn.prepareStatement(sqlDeleteOldDetails)) {
                psDel.setString(1, order.getId());
                psDel.executeUpdate();
            }

            if (order.getChiTiet() != null && !order.getChiTiet().isEmpty()) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (DatHangNCCChiTiet ct : order.getChiTiet()) {
                        String detailId = UUID.randomUUID().toString();
                        ct.setId(detailId);
                        psDetail.setString(1, detailId);
                        psDetail.setString(2, order.getId());
                        psDetail.setString(3, ct.getMatHangId());
                        psDetail.setString(4, ct.getDonViTinhId());
                        psDetail.setString(5, ct.getBaoHanh());
                        psDetail.setDouble(6, ct.getSoLuong());
                        psDetail.setDouble(7, ct.getDonGia());
                        psDetail.setDouble(8, ct.getTiLeGiamGia());
                        psDetail.setDouble(9, ct.getThanhTien());
                        psDetail.setDouble(10, ct.getGiaMuaGanNhat());
                        psDetail.setString(11, ct.getNote());
                        psDetail.setString(12, getEffectiveUserId());
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            auditOrder("SỬA", order);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi update DatHangNCC: " + e.getMessage());
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

    public boolean delete(String id) {
        if (id == null) return false;
        String sql = "UPDATE TDATHANGNCC SET STATUS = -1, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, getEffectiveUserId());
            ps.setString(2, id.trim());
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) audit("Đặt hàng nhà cung cấp", "XÓA", id, "Xóa phiếu đặt hàng nhà cung cấp");
            return deleted;
        } catch (SQLException e) {
            System.err.println("Lỗi delete DatHangNCC: " + e.getMessage());
            return false;
        }
    }

    private void auditOrder(String action, DatHangNCC order) {
        int itemCount = order.getChiTiet() == null ? 0 : order.getChiTiet().size();
        String reference = order.getName() != null && !order.getName().trim().isEmpty()
                ? order.getName() : order.getId();
        audit("Đặt hàng nhà cung cấp", action, reference,
                action + " phiếu đặt hàng nhà cung cấp; số mặt hàng: " + itemCount
                        + "; tổng cộng: " + Math.round(order.getTongCong()));
    }

    public DatHangNCC getOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM TDATHANGNCC WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NGAY DESC, NAME DESC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getOffset DatHangNCC: " + e.getMessage());
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
            return getById(ids.get(targetIdx));
        }
        return null;
    }

    private DatHangNCC mapResultSetToDatHangNCC(ResultSet rs) throws SQLException {
        DatHangNCC o = new DatHangNCC();
        o.setId(rs.getString("ID"));
        o.setName(rs.getString("NAME"));
        o.setNgay(rs.getTimestamp("NGAY"));
        o.setNhaCungCapId(rs.getString("DNHACUNGCAPID"));
        o.setGiamTheoTien(rs.getInt("GIAMTHEOTIEN"));
        o.setLoai(rs.getInt("LOAI"));
        o.setTienHang(rs.getDouble("TIENHANG"));
        o.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
        o.setTienGiamGia(rs.getDouble("TIENGIAMGIA"));
        o.setTiLeThue(rs.getDouble("TILETHUE"));
        o.setTienThue(rs.getDouble("TIENTHUE"));
        o.setPhiVanChuyen(rs.getDouble("PHIVANCHUYEN"));
        o.setTongCong(rs.getDouble("TONGCONG"));
        o.setNote(rs.getString("NOTE"));
        o.setStatus(rs.getInt("STATUS"));
        o.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        o.setUserCreatedId(rs.getString("USERCREATEDID"));
        o.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        o.setUserModifiedId(rs.getString("USERMODIFIEDID"));

        try {
            o.setTenNhaCungCap(rs.getString("TENNHACUNGCAP"));
        } catch (SQLException ignored) {}
        try {
            o.setUserCreatedName(rs.getString("TENNGUOITAO"));
            if (o.getUserCreatedName() == null) o.setUserCreatedName("Administrator");
        } catch (SQLException ignored) {}
        try {
            o.setUserModifiedName(rs.getString("TENNGUOISUA"));
        } catch (SQLException ignored) {}

        return o;
    }
}
