package kazuko.ex.dao;

import kazuko.ex.model.nhansu.ThuongPhat;
import kazuko.ex.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ThuongPhatDAO extends BaseDAO {
    private static final List<ThuongPhat> MEMORY_LIST = new CopyOnWriteArrayList<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_LIST.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            ThuongPhat tp1 = new ThuongPhat();
            tp1.setId("tp-001");
            tp1.setSoPhieu("TP26/00001");
            tp1.setNgay(sdf.parse("31/08/2026 09:00"));
            tp1.setTimeCreated(sdf.parse("31/08/2026 09:00"));
            tp1.setUserCreatedId("Administrator");
            tp1.setNhanVienId("223f6d45-ce2d-411f-8263-9b4ca8052ea9");
            tp1.setTenNhanVien("Hoàng Giang");
            tp1.setTienThuong(10000.0);
            tp1.setTienPhat(100.0);
            tp1.setLyDoId("bbcac5ef-e944-41b6-ac5a-dd6533067419");
            tp1.setTenLyDo("Doanh số");
            tp1.setGhiChu("note");
            tp1.setStatus(1);
            MEMORY_LIST.add(tp1);
        } catch (Exception ignored) {}
    }

    private List<ThuongPhat> tryLoadFromDb(int statusFilter) {
        List<ThuongPhat> list = new ArrayList<>();
        String sql = "SELECT tp.*, nv.NAME as TENNHANVIEN, ld.NAME as TENLYDO " +
                     "FROM TTHUONGPHAT tp " +
                     "LEFT JOIN DNHANVIEN nv ON tp.DNHANVIENID = nv.ID " +
                     "LEFT JOIN DLYDOTHUONGPHAT ld ON tp.DLYDOTHUONGPHATID = ld.ID " +
                     "ORDER BY tp.NGAY DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ThuongPhat tp = new ThuongPhat();
                tp.setId(rs.getString("ID"));
                tp.setSoPhieu(rs.getString("NAME"));
                tp.setGhiChu(rs.getString("NOTE"));
                int st = rs.getInt("STATUS");
                tp.setStatus(st >= 0 ? 1 : -1);

                tp.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                tp.setNgay(rs.getTimestamp("NGAY"));
                tp.setNhanVienId(rs.getString("DNHANVIENID"));
                tp.setTenNhanVien(rs.getString("TENNHANVIEN"));
                tp.setTienThuong(rs.getDouble("THUONG"));
                tp.setTienPhat(rs.getDouble("PHAT"));
                tp.setLyDoId(rs.getString("DLYDOTHUONGPHATID"));
                tp.setTenLyDo(rs.getString("TENLYDO"));

                if ((statusFilter >= 0 && tp.getStatus() >= 0) || (statusFilter < 0 && tp.getStatus() < 0)) {
                    list.add(tp);
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    public List<ThuongPhat> getAll() {
        List<ThuongPhat> dbList = tryLoadFromDb(1);
        if (dbList != null && !dbList.isEmpty()) {
            return dbList;
        }
        return MEMORY_LIST.stream()
                .filter(tp -> tp.getStatus() == 1)
                .sorted(Comparator.comparing(ThuongPhat::getNgay, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<ThuongPhat> getTrashItems() {
        List<ThuongPhat> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }
        return MEMORY_LIST.stream()
                .filter(tp -> tp.getStatus() == -1)
                .sorted(Comparator.comparing(ThuongPhat::getNgay, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public ThuongPhat findById(String id) {
        if (id == null) return null;
        List<ThuongPhat> all = getAll();
        for (ThuongPhat tp : all) {
            if (id.equals(tp.getId())) return tp;
        }
        return MEMORY_LIST.stream()
                .filter(tp -> id.equals(tp.getId()))
                .findFirst()
                .orElse(null);
    }

    public boolean insert(ThuongPhat tp) {
        if (tp == null) return false;
        if (tp.getId() == null || tp.getId().trim().isEmpty()) {
            tp.setId(UUID.randomUUID().toString());
        }
        if (tp.getNgay() == null) {
            tp.setNgay(new Date());
        }
        tp.setStatus(1);

        String userId = getEffectiveUserId();
        String sql = "INSERT INTO TTHUONGPHAT (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, NGAY, DNHANVIENID, THUONG, PHAT, DLYDOTHUONGPHATID) " +
                     "VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tp.getId());
            ps.setString(2, tp.getSoPhieu());
            ps.setString(3, tp.getGhiChu());
            ps.setString(4, userId);
            ps.setTimestamp(5, new Timestamp(tp.getNgay().getTime()));
            ps.setString(6, tp.getNhanVienId());
            ps.setDouble(7, tp.getTienThuong());
            ps.setDouble(8, tp.getTienPhat());
            ps.setString(9, tp.getLyDoId());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        MEMORY_LIST.add(0, tp);
        return true;
    }

    public boolean update(ThuongPhat tp) {
        if (tp == null || tp.getId() == null) return false;

        String sql = "UPDATE TTHUONGPHAT SET NAME = ?, NOTE = ?, NGAY = ?, DNHANVIENID = ?, THUONG = ?, PHAT = ?, DLYDOTHUONGPHATID = ?, TIMEMODIFIED = CURRENT_TIMESTAMP WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tp.getSoPhieu());
            ps.setString(2, tp.getGhiChu());
            ps.setTimestamp(3, tp.getNgay() != null ? new Timestamp(tp.getNgay().getTime()) : new Timestamp(System.currentTimeMillis()));
            ps.setString(4, tp.getNhanVienId());
            ps.setDouble(5, tp.getTienThuong());
            ps.setDouble(6, tp.getTienPhat());
            ps.setString(7, tp.getLyDoId());
            ps.setString(8, tp.getId());
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        for (int i = 0; i < MEMORY_LIST.size(); i++) {
            if (tp.getId().equals(MEMORY_LIST.get(i).getId())) {
                MEMORY_LIST.set(i, tp);
                return true;
            }
        }
        return true;
    }

    public boolean moveToTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE TTHUONGPHAT SET STATUS = -1 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        ThuongPhat tp = findById(id);
        if (tp != null) {
            tp.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE TTHUONGPHAT SET STATUS = 30 WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        ThuongPhat tp = findById(id);
        if (tp != null) {
            tp.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean delete(String id) {
        return deletePermanently(id);
    }

    public boolean deletePermanently(String id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM TTHUONGPHAT WHERE ID = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}

        return MEMORY_LIST.removeIf(tp -> id != null && id.equals(tp.getId()));
    }

    public String generateNextSoPhieu() {
        return generateNextSoPhieu(new Date());
    }

    public String generateNextSoPhieu(Date date) {
        Calendar cal = Calendar.getInstance();
        if (date != null) cal.setTime(date);
        int year2Digits = cal.get(Calendar.YEAR) % 100;
        String prefix = String.format("TP%02d/", year2Digits);

        int maxNum = 0;
        List<ThuongPhat> all = getAll();
        for (ThuongPhat tp : all) {
            if (tp.getSoPhieu() != null && tp.getSoPhieu().startsWith(prefix)) {
                try {
                    String numPart = tp.getSoPhieu().substring(prefix.length());
                    int num = Integer.parseInt(numPart);
                    if (num > maxNum) maxNum = num;
                } catch (Exception ignored) {}
            }
        }
        return String.format("%s%05d", prefix, maxNum + 1);
    }
}
