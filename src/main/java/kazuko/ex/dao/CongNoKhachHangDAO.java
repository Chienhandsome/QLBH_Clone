package kazuko.ex.dao;

import kazuko.ex.model.congno.ChiTietCongNoKhachHangEntry;
import kazuko.ex.model.congno.CongNoKhachHangItem;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class CongNoKhachHangDAO extends BaseDAO {

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<CongNoKhachHangItem> getDanhSachCongNoKhachHang(String nhomKhachId, String keyword, boolean onlyHasDebt) {
        List<CongNoKhachHangItem> list = new ArrayList<>();

        // 1. Map tổng tiền mua và tiền thanh toán từ đơn hàng bán (TDONHANG LOAI = 0)
        Map<String, double[]> salesMap = new HashMap<>(); // khachId -> [tongMua, daThanhToan]
        String sqlSales = "SELECT d.DKHACHHANGID, SUM(d.TONGCONG) AS TONGMUA, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DKHACHHANGID IS NOT NULL AND d.LOAI = 0 " +
                "GROUP BY d.DKHACHHANGID";

        // 2. Map tổng tiền từ phiếu thu nợ (TPHIEUTHU)
        Map<String, Double> receiptMap = new HashMap<>(); // khachId -> tongThu
        String sqlReceipt = "SELECT pt.DKHACHHANGID, SUM(pt.SOTIEN) AS TONGTHU " +
                "FROM TPHIEUTHU pt " +
                "WHERE (pt.STATUS IS NULL OR pt.STATUS >= 0) AND pt.DKHACHHANGID IS NOT NULL " +
                "AND (pt.KHONGTHAYDOICONGNO IS NULL OR pt.KHONGTHAYDOICONGNO = 0) " +
                "GROUP BY pt.DKHACHHANGID";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlSales);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kId = rs.getString("DKHACHHANGID");
                    if (kId != null) {
                        salesMap.put(kId.trim(), new double[]{rs.getDouble("TONGMUA"), rs.getDouble("TONGDATT")});
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlReceipt);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kId = rs.getString("DKHACHHANGID");
                    if (kId != null) {
                        receiptMap.put(kId.trim(), rs.getDouble("TONGTHU"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính toán tổng nợ khách hàng: " + e.getMessage());
        }

        // 3. Lấy danh sách toàn bộ khách hàng
        String sqlKh = "SELECT k.ID, k.MAKHACH, k.NAME AS TENKHACH, k.DNHOMKHACHHANGID, n.NAME AS TENNHOM, " +
                "k.DIACHI, k.DIENTHOAI, k.EMAIL, k.MASOTHUE " +
                "FROM DKHACHHANG k " +
                "LEFT JOIN DNHOMKHACHHANG n ON k.DNHOMKHACHHANGID = n.ID " +
                "WHERE (k.STATUS IS NULL OR k.STATUS >= 0) " +
                "ORDER BY k.NAME ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlKh);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String kId = rs.getString("ID");
                String ma = rs.getString("MAKHACH");
                String ten = rs.getString("TENKHACH");
                String nhomId = rs.getString("DNHOMKHACHHANGID");
                String tenNhom = rs.getString("TENNHOM");
                String diaChi = rs.getString("DIACHI");
                String dienThoai = rs.getString("DIENTHOAI");
                String email = rs.getString("EMAIL");
                String mst = rs.getString("MASOTHUE");

                double[] sData = salesMap.getOrDefault(kId != null ? kId.trim() : "", new double[]{0.0, 0.0});
                double tongMua = sData[0];
                double daTraDon = sData[1];
                double daThuPhieu = receiptMap.getOrDefault(kId != null ? kId.trim() : "", 0.0);

                double tongDaTra = daTraDon + daThuPhieu;
                double conNo = tongMua - tongDaTra;

                // Lọc theo nhóm
                if (nhomKhachId != null && !nhomKhachId.trim().isEmpty()) {
                    if (nhomId == null || !nhomId.trim().equalsIgnoreCase(nhomKhachId.trim())) {
                        continue;
                    }
                }

                // Lọc theo keyword
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    boolean match = (ten != null && ten.toLowerCase().contains(kw)) ||
                            (ma != null && ma.toLowerCase().contains(kw)) ||
                            (dienThoai != null && dienThoai.toLowerCase().contains(kw)) ||
                            (diaChi != null && diaChi.toLowerCase().contains(kw));
                    if (!match) continue;
                }

                // Lọc còn nợ <> 0
                if (onlyHasDebt && Math.abs(conNo) < 0.0001) {
                    continue;
                }

                CongNoKhachHangItem item = new CongNoKhachHangItem();
                item.setKhachHangId(kId);
                item.setMaKhach(ma);
                item.setTenKhach(ten);
                item.setNhomKhachId(nhomId);
                item.setTenNhomKhach(tenNhom);
                item.setDiaChi(diaChi);
                item.setDienThoai(dienThoai);
                item.setEmail(email);
                item.setMaSoThue(mst);
                item.setTongMua(tongMua);
                item.setTongDaTra(tongDaTra);
                item.setConNo(conNo);

                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDanhSachCongNoKhachHang: " + e.getMessage());
        }

        return list;
    }

    public List<ChiTietCongNoKhachHangEntry> getChiTietCongNo(String khachHangId, Date tuNgay, Date denNgay) {
        List<ChiTietCongNoKhachHangEntry> list = new ArrayList<>();
        if (khachHangId == null || khachHangId.trim().isEmpty()) return list;

        // 1. Đơn hàng bán
        StringBuilder sqlOrders = new StringBuilder(
                "SELECT d.NAME AS SOPHIEU, d.NGAY, d.TONGCONG, d.TIENTHANHTOAN, d.DIENGIAI, d.TIMECREATED " +
                "FROM TDONHANG d " +
                "WHERE d.DKHACHHANGID = ? AND d.LOAI = 0 AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );
        if (tuNgay != null) sqlOrders.append("AND d.NGAY >= ? ");
        if (denNgay != null) sqlOrders.append("AND d.NGAY <= ? ");
        sqlOrders.append("ORDER BY d.NGAY ASC, d.TIMECREATED ASC");

        // 2. Phiếu thu
        StringBuilder sqlReceipts = new StringBuilder(
                "SELECT pt.SOPHIEU, pt.NGAY, pt.SOTIEN, pt.LYDOTHU, pt.TIMECREATED " +
                "FROM TPHIEUTHU pt " +
                "WHERE pt.DKHACHHANGID = ? AND (pt.STATUS IS NULL OR pt.STATUS >= 0) " +
                "AND (pt.KHONGTHAYDOICONGNO IS NULL OR pt.KHONGTHAYDOICONGNO = 0) "
        );
        if (tuNgay != null) sqlReceipts.append("AND pt.NGAY >= ? ");
        if (denNgay != null) sqlReceipts.append("AND pt.NGAY <= ? ");
        sqlReceipts.append("ORDER BY pt.NGAY ASC, pt.TIMECREATED ASC");

        try (Connection conn = getConnection()) {
            // Read orders
            try (PreparedStatement ps = conn.prepareStatement(sqlOrders.toString())) {
                int pIdx = 1;
                ps.setString(pIdx++, khachHangId.trim());
                if (tuNgay != null) ps.setTimestamp(pIdx++, new Timestamp(tuNgay.getTime()));
                if (denNgay != null) {
                    long endOfDay = denNgay.getTime() + 86399999L;
                    ps.setTimestamp(pIdx++, new Timestamp(endOfDay));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietCongNoKhachHangEntry e = new ChiTietCongNoKhachHangEntry();
                        e.setSoPhieu(rs.getString("SOPHIEU"));
                        e.setNgay(rs.getTimestamp("NGAY"));
                        e.setTongCong(rs.getDouble("TONGCONG"));
                        e.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
                        String dg = rs.getString("DIENGIAI");
                        e.setDienGiai(dg != null && !dg.trim().isEmpty() ? dg : "Bán lẻ");
                        e.setLoaiChungTu("DONHANG");
                        list.add(e);
                    }
                }
            }

            // Read receipts
            try (PreparedStatement ps = conn.prepareStatement(sqlReceipts.toString())) {
                int pIdx = 1;
                ps.setString(pIdx++, khachHangId.trim());
                if (tuNgay != null) ps.setTimestamp(pIdx++, new Timestamp(tuNgay.getTime()));
                if (denNgay != null) {
                    long endOfDay = denNgay.getTime() + 86399999L;
                    ps.setTimestamp(pIdx++, new Timestamp(endOfDay));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietCongNoKhachHangEntry e = new ChiTietCongNoKhachHangEntry();
                        e.setSoPhieu(rs.getString("SOPHIEU"));
                        e.setNgay(rs.getTimestamp("NGAY"));
                        e.setTongCong(0.0);
                        e.setTienThanhToan(rs.getDouble("SOTIEN"));
                        String lyDo = rs.getString("LYDOTHU");
                        e.setDienGiai(lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Thu nợ khách hàng");
                        e.setLoaiChungTu("PHIEUTHU");
                        list.add(e);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietCongNo: " + e.getMessage());
        }

        // Sort all chronologically
        list.sort(Comparator.comparing(ChiTietCongNoKhachHangEntry::getNgay, Comparator.nullsLast(Comparator.naturalOrder())));

        // Calculate running debt balance
        double running = 0.0;
        for (ChiTietCongNoKhachHangEntry e : list) {
            if ("DONHANG".equals(e.getLoaiChungTu())) {
                running += (e.getTongCong() - e.getTienThanhToan());
            } else {
                running -= e.getTienThanhToan();
            }
            e.setLuyKe(running);
        }

        return list;
    }

    public String generateNextSoPhieuThu(Date ngay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String yearCode = sdf.format(ngay != null ? ngay : new Date());
        String prefix = "PT" + yearCode + "/";

        String sql = "SELECT SOPHIEU FROM TPHIEUTHU WHERE SOPHIEU LIKE ? ORDER BY SOPHIEU DESC";
        int maxSeq = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("SOPHIEU");
                    if (name != null && name.startsWith(prefix)) {
                        String seqStr = name.substring(prefix.length());
                        try {
                            int seq = Integer.parseInt(seqStr);
                            if (seq > maxSeq) maxSeq = seq;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi generateNextSoPhieuThu: " + e.getMessage());
        }
        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public boolean insertPhieuThu(String khachHangId, String tenKhach, String diaChi,
                                  double soTien, String nhanVienId, String lyDoThu,
                                  String ghiChu, String taiKhoanNganHangId, Date ngay) {
        if (soTien <= 0) return false;

        String id = UUID.randomUUID().toString();
        Date dateVal = (ngay != null) ? ngay : new Date();
        String soPhieu = generateNextSoPhieuThu(dateVal);

        String sqlPT = "INSERT INTO TPHIEUTHU (" +
                "ID, SOPHIEU, NGAY, DLYDOTHUCHIID, TENPHANLOAI, LYDOTHU, CHUNGTUGOC, " +
                "LOAIDOITUONG, TENDOITUONG, DIACHI, DNHANVIENID, DKHACHHANGID, SOTIEN, " +
                "CHUYENVAOTAIKHOAN, DTAIKHOANNGANHANGID, KHONGTHAYDOICONGNO, NOTE, STATUS, " +
                "TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 1, CURRENT_TIMESTAMP, ?)";

        String sqlTC = "INSERT INTO TTHUCHI (" +
                "ID, NAME, NGAY, TENDOITUONG, DIACHI, LOAI, LOAIDOITUONG, DIENGIAI, " +
                "THU, CHI, DKHACHHANGID, DNHANVIENID, DTAIKHOANNGANHANGID, LAPHIEUTHUCONGNO, " +
                "KHONGTHAYDOICONGNO, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, 1, 0, ?, ?, 0, ?, ?, ?, 1, 0, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlPT)) {
                ps.setString(1, id);
                ps.setString(2, soPhieu);
                ps.setTimestamp(3, new Timestamp(dateVal.getTime()));
                ps.setString(4, "lydo-thuno");
                ps.setString(5, "Thu nợ");
                ps.setString(6, lyDoThu != null ? lyDoThu : "Thu nợ khách hàng");
                ps.setString(7, "");
                ps.setInt(8, 0); // Khách hàng
                ps.setString(9, tenKhach);
                ps.setString(10, diaChi);
                ps.setString(11, nhanVienId);
                ps.setString(12, khachHangId);
                ps.setDouble(13, soTien);
                ps.setBoolean(14, taiKhoanNganHangId != null && !taiKhoanNganHangId.trim().isEmpty());
                ps.setString(15, taiKhoanNganHangId);
                ps.setString(16, ghiChu);
                ps.setString(17, getEffectiveUserId());
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlTC)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, soPhieu);
                ps.setTimestamp(3, new Timestamp(dateVal.getTime()));
                ps.setString(4, tenKhach);
                ps.setString(5, diaChi);
                ps.setString(6, lyDoThu != null ? lyDoThu : "Thu nợ khách hàng");
                ps.setDouble(7, soTien);
                ps.setString(8, khachHangId);
                ps.setString(9, nhanVienId);
                ps.setString(10, taiKhoanNganHangId);
                ps.setString(11, getEffectiveUserId());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insertPhieuThu: " + e.getMessage());
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
}
