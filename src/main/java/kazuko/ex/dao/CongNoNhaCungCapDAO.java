package kazuko.ex.dao;

import kazuko.ex.model.congno.ChiTietCongNoNhaCungCapEntry;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class CongNoNhaCungCapDAO extends BaseDAO {

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<CongNoNhaCungCapItem> getDanhSachCongNoNCC(String nhomNCCId, String keyword, boolean onlyHasDebt) {
        List<CongNoNhaCungCapItem> list = new ArrayList<>();

        // 1. Map tổng tiền nhập và tiền thanh toán từ đơn hàng nhập (TDONHANG LOAI = 1 hoặc NAME LIKE 'PN%')
        Map<String, double[]> purchaseMap = new HashMap<>(); // nccId -> [tongNhap, daThanhToan]
        String sqlPurchase = "SELECT d.DNHACUNGCAPID, SUM(d.TONGCONG) AS TONGNHAP, SUM(d.TIENTHANHTOAN) AS TONGDATT " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DNHACUNGCAPID IS NOT NULL AND (d.LOAI = 1 OR d.NAME LIKE 'PN%') " +
                "GROUP BY d.DNHACUNGCAPID";

        // 2. Map tổng tiền từ phiếu chi trả nợ (TPHIEUCHI)
        Map<String, Double> paymentMap = new HashMap<>(); // nccId -> tongChi
        String sqlPayment = "SELECT pc.DNHACUNGCAPID, SUM(pc.SOTIEN) AS TONGCHI " +
                "FROM TPHIEUCHI pc " +
                "WHERE (pc.STATUS IS NULL OR pc.STATUS >= 0) AND pc.DNHACUNGCAPID IS NOT NULL " +
                "AND (pc.KHONGTHAYDOICONGNO IS NULL OR pc.KHONGTHAYDOICONGNO = 0) " +
                "GROUP BY pc.DNHACUNGCAPID";

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlPurchase);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nccId = rs.getString("DNHACUNGCAPID");
                    if (nccId != null) {
                        purchaseMap.put(nccId.trim(), new double[]{rs.getDouble("TONGNHAP"), rs.getDouble("TONGDATT")});
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlPayment);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nccId = rs.getString("DNHACUNGCAPID");
                    if (nccId != null) {
                        paymentMap.put(nccId.trim(), rs.getDouble("TONGCHI"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính toán tổng nợ nhà cung cấp: " + e.getMessage());
        }

        // 3. Lấy danh sách toàn bộ nhà cung cấp
        String sqlNCC = "SELECT n.ID, n.MANHACUNGCAP AS MANCC, n.NAME AS TENNCC, n.DNHOMNHACUNGCAPID, nh.NAME AS TENNHOM, " +
                "n.DIACHI, n.DIENTHOAI, n.EMAIL " +
                "FROM DNHACUNGCAP n " +
                "LEFT JOIN DNHOMNHACUNGCAP nh ON n.DNHOMNHACUNGCAPID = nh.ID " +
                "WHERE (n.STATUS IS NULL OR n.STATUS >= 0) " +
                "ORDER BY n.NAME ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlNCC);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nccId = rs.getString("ID");
                String ma = rs.getString("MANCC");
                String ten = rs.getString("TENNCC");
                String nhomId = rs.getString("DNHOMNHACUNGCAPID");
                String tenNhom = rs.getString("TENNHOM");
                String diaChi = rs.getString("DIACHI");
                String dienThoai = rs.getString("DIENTHOAI");
                String email = rs.getString("EMAIL");

                double[] pData = purchaseMap.getOrDefault(nccId != null ? nccId.trim() : "", new double[]{0.0, 0.0});
                double tongNhap = pData[0];
                double daTraDon = pData[1];
                double daChiPhieu = paymentMap.getOrDefault(nccId != null ? nccId.trim() : "", 0.0);

                double tongDaTra = daTraDon + daChiPhieu;
                double conNo = tongNhap - tongDaTra;

                // Lọc theo nhóm
                if (nhomNCCId != null && !nhomNCCId.trim().isEmpty()) {
                    if (nhomId == null || !nhomId.trim().equalsIgnoreCase(nhomNCCId.trim())) {
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

                CongNoNhaCungCapItem item = new CongNoNhaCungCapItem();
                item.setNhaCungCapId(nccId);
                item.setMaNCC(ma);
                item.setTenNCC(ten);
                item.setNhomNCCId(nhomId);
                item.setTenNhomNCC(tenNhom);
                item.setDiaChi(diaChi);
                item.setDienThoai(dienThoai);
                item.setEmail(email);
                item.setTongNhap(tongNhap);
                item.setTongDaTra(tongDaTra);
                item.setConNo(conNo);

                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getDanhSachCongNoNCC: " + e.getMessage());
        }

        return list;
    }

    public List<ChiTietCongNoNhaCungCapEntry> getChiTietCongNoNCC(String nhaCungCapId, Date tuNgay, Date denNgay) {
        List<ChiTietCongNoNhaCungCapEntry> list = new ArrayList<>();
        if (nhaCungCapId == null || nhaCungCapId.trim().isEmpty()) return list;

        // 1. Đơn nhập hàng
        StringBuilder sqlPurchases = new StringBuilder(
                "SELECT d.NAME AS SOPHIEU, d.NGAY, d.TONGCONG, d.TIENTHANHTOAN, d.DIENGIAI, d.TIMECREATED " +
                "FROM TDONHANG d " +
                "WHERE d.DNHACUNGCAPID = ? AND (d.LOAI = 1 OR d.NAME LIKE 'PN%') AND (d.STATUS IS NULL OR d.STATUS >= 0) "
        );
        if (tuNgay != null) sqlPurchases.append("AND d.NGAY >= ? ");
        if (denNgay != null) sqlPurchases.append("AND d.NGAY <= ? ");
        sqlPurchases.append("ORDER BY d.NGAY ASC, d.TIMECREATED ASC");

        // 2. Phiếu chi trả nợ
        StringBuilder sqlPayments = new StringBuilder(
                "SELECT pc.SOPHIEU, pc.NGAY, pc.SOTIEN, pc.LYDOCHI, pc.TIMECREATED " +
                "FROM TPHIEUCHI pc " +
                "WHERE pc.DNHACUNGCAPID = ? AND (pc.STATUS IS NULL OR pc.STATUS >= 0) " +
                "AND (pc.KHONGTHAYDOICONGNO IS NULL OR pc.KHONGTHAYDOICONGNO = 0) "
        );
        if (tuNgay != null) sqlPayments.append("AND pc.NGAY >= ? ");
        if (denNgay != null) sqlPayments.append("AND pc.NGAY <= ? ");
        sqlPayments.append("ORDER BY pc.NGAY ASC, pc.TIMECREATED ASC");

        try (Connection conn = getConnection()) {
            // Read purchases
            try (PreparedStatement ps = conn.prepareStatement(sqlPurchases.toString())) {
                int pIdx = 1;
                ps.setString(pIdx++, nhaCungCapId.trim());
                if (tuNgay != null) ps.setTimestamp(pIdx++, new Timestamp(tuNgay.getTime()));
                if (denNgay != null) {
                    long endOfDay = denNgay.getTime() + 86399999L;
                    ps.setTimestamp(pIdx++, new Timestamp(endOfDay));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietCongNoNhaCungCapEntry e = new ChiTietCongNoNhaCungCapEntry();
                        e.setSoPhieu(rs.getString("SOPHIEU"));
                        e.setNgay(rs.getTimestamp("NGAY"));
                        e.setTongCong(rs.getDouble("TONGCONG"));
                        e.setTienThanhToan(rs.getDouble("TIENTHANHTOAN"));
                        String dg = rs.getString("DIENGIAI");
                        e.setDienGiai(dg != null && !dg.trim().isEmpty() ? dg : "Nhập mua hàng");
                        e.setLoaiChungTu("NHAPKHO");
                        list.add(e);
                    }
                }
            }

            // Read payments
            try (PreparedStatement ps = conn.prepareStatement(sqlPayments.toString())) {
                int pIdx = 1;
                ps.setString(pIdx++, nhaCungCapId.trim());
                if (tuNgay != null) ps.setTimestamp(pIdx++, new Timestamp(tuNgay.getTime()));
                if (denNgay != null) {
                    long endOfDay = denNgay.getTime() + 86399999L;
                    ps.setTimestamp(pIdx++, new Timestamp(endOfDay));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ChiTietCongNoNhaCungCapEntry e = new ChiTietCongNoNhaCungCapEntry();
                        e.setSoPhieu(rs.getString("SOPHIEU"));
                        e.setNgay(rs.getTimestamp("NGAY"));
                        e.setTongCong(0.0);
                        e.setTienThanhToan(rs.getDouble("SOTIEN"));
                        String lyDo = rs.getString("LYDOCHI");
                        e.setDienGiai(lyDo != null && !lyDo.trim().isEmpty() ? lyDo : "Chi trả nợ NCC");
                        e.setLoaiChungTu("PHIEUCHI");
                        list.add(e);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getChiTietCongNoNCC: " + e.getMessage());
        }

        // Sort all chronologically
        list.sort(Comparator.comparing(ChiTietCongNoNhaCungCapEntry::getNgay, Comparator.nullsLast(Comparator.naturalOrder())));

        // Calculate running debt balance
        double running = 0.0;
        for (ChiTietCongNoNhaCungCapEntry e : list) {
            if ("NHAPKHO".equals(e.getLoaiChungTu())) {
                running += (e.getTongCong() - e.getTienThanhToan());
            } else {
                running -= e.getTienThanhToan();
            }
            e.setLuyKe(running);
        }

        return list;
    }

    public String generateNextSoPhieuChi(Date ngay) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy");
        String yearCode = sdf.format(ngay != null ? ngay : new Date());
        String prefix = "PC" + yearCode + "/";

        String sql = "SELECT SOPHIEU FROM TPHIEUCHI WHERE SOPHIEU LIKE ? ORDER BY SOPHIEU DESC";
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
            System.err.println("Lỗi generateNextSoPhieuChi: " + e.getMessage());
        }
        return String.format("%s%05d", prefix, maxSeq + 1);
    }

    public boolean insertPhieuChi(String nhaCungCapId, String tenNCC, String diaChi,
                                  double soTien, String nhanVienId, String lyDoChi,
                                  String ghiChu, String taiKhoanNganHangId, Date ngay) {
        if (soTien <= 0) return false;

        String id = UUID.randomUUID().toString();
        Date dateVal = (ngay != null) ? ngay : new Date();
        String soPhieu = generateNextSoPhieuChi(dateVal);

        String sqlPC = "INSERT INTO TPHIEUCHI (" +
                "ID, SOPHIEU, NGAY, DLYDOTHUCHIID, TENPHANLOAI, LYDOCHI, CHUNGTUGOC, " +
                "LOAIDOITUONG, TENDOITUONG, DIACHI, DNHANVIENID, DNHACUNGCAPID, SOTIEN, " +
                "CHUYENTUTAIKHOAN, DTAIKHOANNGANHANGID, KHONGTHAYDOICONGNO, NOTE, STATUS, " +
                "TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, 1, CURRENT_TIMESTAMP, ?)";

        String sqlTC = "INSERT INTO TTHUCHI (" +
                "ID, NAME, NGAY, TENDOITUONG, DIACHI, LOAI, LOAIDOITUONG, DIENGIAI, " +
                "THU, CHI, DNHACUNGCAPID, DNHANVIENID, DTAIKHOANNGANHANGID, LAPHIEUTHUCONGNO, " +
                "KHONGTHAYDOICONGNO, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, 2, 1, ?, 0, ?, ?, ?, ?, 0, 0, 1, CURRENT_TIMESTAMP, ?)";

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlPC)) {
                ps.setString(1, id);
                ps.setString(2, soPhieu);
                ps.setTimestamp(3, new Timestamp(dateVal.getTime()));
                ps.setString(4, "lydo-trano-ncc");
                ps.setString(5, "Chi trả nợ NCC");
                ps.setString(6, lyDoChi != null ? lyDoChi : "Chi trả nợ nhà cung cấp");
                ps.setString(7, "");
                ps.setInt(8, 1); // NCC
                ps.setString(9, tenNCC);
                ps.setString(10, diaChi);
                ps.setString(11, nhanVienId);
                ps.setString(12, nhaCungCapId);
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
                ps.setString(4, tenNCC);
                ps.setString(5, diaChi);
                ps.setString(6, lyDoChi != null ? lyDoChi : "Chi trả nợ nhà cung cấp");
                ps.setDouble(7, soTien);
                ps.setString(8, nhaCungCapId);
                ps.setString(9, nhanVienId);
                ps.setString(10, taiKhoanNganHangId);
                ps.setString(11, getEffectiveUserId());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi insertPhieuChi: " + e.getMessage());
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

    private static final Map<String, Double> MEMORY_INITIAL_DEBTS = new ConcurrentHashMap<>();

    /**
     * Lấy danh sách công nợ ban đầu của toàn bộ nhà cung cấp
     * @return Map với key là NhaCungCapId và value là số tiền nợ ban đầu
     */
    public Map<String, Double> getCongNoBanDauMap() {
        Map<String, Double> map = new HashMap<>(MEMORY_INITIAL_DEBTS);
        String sql = "SELECT d.DNHACUNGCAPID, d.TONGCONG " +
                "FROM TDONHANG d " +
                "WHERE (d.STATUS IS NULL OR d.STATUS >= 0) AND d.DNHACUNGCAPID IS NOT NULL " +
                "AND d.LOAI = 1 AND (d.DIENGIAI = 'Công nợ ban đầu' OR d.NAME LIKE 'CNBD_NCC_%')";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String nccId = rs.getString("DNHACUNGCAPID");
                if (nccId != null) {
                    double amount = rs.getDouble("TONGCONG");
                    map.put(nccId.trim(), amount);
                    MEMORY_INITIAL_DEBTS.put(nccId.trim(), amount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getCongNoBanDauMap từ DB: " + e.getMessage());
        }
        return map;
    }

    /**
     * Lưu công nợ ban đầu cho danh sách nhà cung cấp
     * @param ngayChot Ngày chốt công nợ
     * @param mapCongNo Map chứa {nhaCungCapId: soTien}
     * @return true nếu lưu thành công
     */
    public boolean saveCongNoBanDau(Date ngayChot, Map<String, Double> mapCongNo) {
        if (mapCongNo == null) return false;
        Date dateVal = (ngayChot != null) ? ngayChot : new Date();
        Timestamp tsNgay = new Timestamp(dateVal.getTime());
        String userId = getEffectiveUserId();

        // Cập nhật bộ nhớ tạm
        MEMORY_INITIAL_DEBTS.putAll(mapCongNo);

        // Lấy danh sách NCC để lấy Mã NCC
        NhaCungCapDAO nccDAO = new NhaCungCapDAO();
        Map<String, String> nccMaMap = new HashMap<>();
        for (kazuko.ex.model.danhmuc.NhaCungCap ncc : nccDAO.getAll()) {
            if (ncc.getId() != null) {
                nccMaMap.put(ncc.getId().trim(), ncc.getMaNhaCungCap() != null ? ncc.getMaNhaCungCap().trim() : ncc.getId().trim());
            }
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String sqlCheck = "SELECT ID FROM TDONHANG WHERE DNHACUNGCAPID = ? AND LOAI = 1 AND (DIENGIAI = 'Công nợ ban đầu' OR NAME LIKE 'CNBD_NCC_%')";
            String sqlUpdate = "UPDATE TDONHANG SET TONGCONG = ?, NGAY = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, USERMODIFIEDID = ?, STATUS = ? WHERE ID = ?";
            String sqlInsert = "INSERT INTO TDONHANG (" +
                    "ID, NAME, DNHACUNGCAPID, NGAY, LOAI, TONGCONG, TIENTHANHTOAN, DIENGIAI, STATUS, TIMECREATED, USERCREATEDID) " +
                    "VALUES (?, ?, ?, ?, 1, ?, 0, 'Công nợ ban đầu', 1, CURRENT_TIMESTAMP, ?)";

            for (Map.Entry<String, Double> entry : mapCongNo.entrySet()) {
                String nccId = entry.getKey();
                double soTien = (entry.getValue() != null) ? entry.getValue() : 0.0;
                String maNCC = nccMaMap.getOrDefault(nccId, "NCC");
                String soPhieu = "CNBD_NCC_" + maNCC;

                String existingOrderId = null;
                try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                    psCheck.setString(1, nccId);
                    try (ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next()) {
                            existingOrderId = rsCheck.getString("ID");
                        }
                    }
                }

                if (existingOrderId != null) {
                    try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                        psUpdate.setDouble(1, soTien);
                        psUpdate.setTimestamp(2, tsNgay);
                        psUpdate.setString(3, userId);
                        psUpdate.setInt(4, (soTien > 0) ? 1 : 0);
                        psUpdate.setString(5, existingOrderId);
                        psUpdate.executeUpdate();
                    }
                } else if (soTien > 0) {
                    try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, UUID.randomUUID().toString());
                        psInsert.setString(2, soPhieu);
                        psInsert.setString(3, nccId);
                        psInsert.setTimestamp(4, tsNgay);
                        psInsert.setDouble(5, soTien);
                        psInsert.setString(6, userId);
                        psInsert.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi saveCongNoBanDau cho NCC: " + e.getMessage());
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
