package kazuko.ex.dao;

import kazuko.ex.model.nhansu.BangLuongTongHop;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BangLuongTongHopDAO extends BaseDAO {

    private static final Map<String, List<BangLuongTongHop>> MEMORY_CACHE = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<BangLuongTongHop> getByBangLuongId(String bangLuongId) {
        if (bangLuongId == null) return Collections.emptyList();

        List<BangLuongTongHop> list = new ArrayList<>();
        String sql = "SELECT th.*, nv.NAME as TENNHANVIEN " +
                     "FROM TBANGLUONGTONGHOP th " +
                     "LEFT JOIN DNHANVIEN nv ON th.DNHANVIENID = nv.ID " +
                     "WHERE th.TBANGLUONGID = ? AND (th.STATUS IS NULL OR th.STATUS >= 0)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bangLuongId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BangLuongTongHop th = new BangLuongTongHop();
                    th.setId(rs.getString("ID"));
                    th.setBangLuongId(rs.getString("TBANGLUONGID"));
                    th.setNhanVienId(rs.getString("DNHANVIENID"));
                    th.setTenNhanVien(rs.getString("TENNHANVIEN"));
                    th.setCachTinhLuong(rs.getInt("CACHTINHLUONG"));
                    th.setLuongCa(rs.getDouble("LUONGCA"));
                    th.setLuongThang(rs.getDouble("LUONGTHANG"));
                    th.setTongLuong(rs.getDouble("TONGLUONG"));
                    th.setThuong(rs.getDouble("THUONG"));
                    th.setPhat(rs.getDouble("PHAT"));
                    th.setTamUng(rs.getDouble("TAMUNG"));
                    th.setThucNhan(rs.getDouble("THUCNHAN"));
                    int st = rs.getInt("STATUS");
                    th.setStatus(st >= 0 ? 1 : -1);
                    list.add(th);
                }
            }
            if (!list.isEmpty()) {
                MEMORY_CACHE.put(bangLuongId, new ArrayList<>(list));
                return list;
            }
        } catch (Exception e) {
            System.err.println("Lỗi đọc TBANGLUONGTONGHOP: " + e.getMessage());
        }

        return MEMORY_CACHE.getOrDefault(bangLuongId, new ArrayList<>());
    }

    public boolean saveAll(String bangLuongId, List<BangLuongTongHop> list) {
        if (bangLuongId == null || list == null) return false;

        MEMORY_CACHE.put(bangLuongId, new ArrayList<>(list));
        String userId = getEffectiveUserId();

        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM TBANGLUONGTONGHOP WHERE TBANGLUONGID = ?")) {
                psDel.setString(1, bangLuongId);
                psDel.executeUpdate();
            }

            String sqlIns = "INSERT INTO TBANGLUONGTONGHOP (" +
                            "ID, TBANGLUONGID, DNHANVIENID, CACHTINHLUONG, LUONGCA, LUONGTHANG, " +
                            "TONGLUONG, THUONG, PHAT, TAMUNG, THUCNHAN, STATUS, TIMECREATED, USERCREATEDID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";

            try (PreparedStatement psIns = conn.prepareStatement(sqlIns)) {
                for (BangLuongTongHop th : list) {
                    if (th.getId() == null || th.getId().trim().isEmpty()) {
                        th.setId(UUID.randomUUID().toString());
                    }
                    psIns.setString(1, th.getId());
                    psIns.setString(2, bangLuongId);
                    psIns.setString(3, th.getNhanVienId());
                    psIns.setInt(4, th.getCachTinhLuong());
                    psIns.setDouble(5, th.getLuongCa());
                    psIns.setDouble(6, th.getLuongThang());
                    psIns.setDouble(7, th.getTongLuong());
                    psIns.setDouble(8, th.getThuong());
                    psIns.setDouble(9, th.getPhat());
                    psIns.setDouble(10, th.getTamUng());
                    psIns.setDouble(11, th.getThucNhan());
                    psIns.setString(12, userId);
                    psIns.addBatch();
                }
                psIns.executeBatch();
            }

            conn.commit();
            conn.setAutoCommit(autoCommit);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi ghi TBANGLUONGTONGHOP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
