package kazuko.ex.dao;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.service.AuthService;
import kazuko.ex.service.ActivityLogService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NhaCungCapDAO {

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

    public List<NhaCungCap> getAll() {
        return getByNhom(null);
    }

    public List<NhaCungCap> getByNhom(String nhomId) {
        List<NhaCungCap> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT n.*, nh.NAME AS TENNHOM, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM DNHACUNGCAP n " +
                "LEFT JOIN DNHOMNHACUNGCAP nh ON n.DNHOMNHACUNGCAPID = nh.ID " +
                "LEFT JOIN SUSER u ON n.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON n.USERMODIFIEDID = um.ID " +
                "WHERE (n.STATUS IS NULL OR n.STATUS >= 0) "
        );

        if ("UNSET".equalsIgnoreCase(nhomId)) {
            sql.append("AND (n.DNHOMNHACUNGCAPID IS NULL OR TRIM(n.DNHOMNHACUNGCAPID) = '') ");
        } else if (nhomId != null && !nhomId.trim().isEmpty()) {
            sql.append("AND n.DNHOMNHACUNGCAPID = ? ");
        }

        sql.append("ORDER BY n.NAME ASC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (nhomId != null && !nhomId.trim().isEmpty() && !"UNSET".equalsIgnoreCase(nhomId)) {
                ps.setString(1, nhomId.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNhaCungCap(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách nhà cung cấp: " + e.getMessage());
        }
        return list;
    }

    public List<NhaCungCap> getTrash() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT n.*, nh.NAME AS TENNHOM, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM DNHACUNGCAP n " +
                "LEFT JOIN DNHOMNHACUNGCAP nh ON n.DNHOMNHACUNGCAPID = nh.ID " +
                "LEFT JOIN SUSER u ON n.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON n.USERMODIFIEDID = um.ID " +
                "WHERE n.STATUS = -1 ORDER BY n.NAME ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToNhaCungCap(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách thùng rác nhà cung cấp: " + e.getMessage());
        }
        return list;
    }

    public NhaCungCap getById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT n.*, nh.NAME AS TENNHOM, u.NAME AS TENNGUOITAO, um.NAME AS TENNGUOISUA " +
                "FROM DNHACUNGCAP n " +
                "LEFT JOIN DNHOMNHACUNGCAP nh ON n.DNHOMNHACUNGCAPID = nh.ID " +
                "LEFT JOIN SUSER u ON n.USERCREATEDID = u.ID " +
                "LEFT JOIN SUSER um ON n.USERMODIFIEDID = um.ID " +
                "WHERE n.ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNhaCungCap(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getById NhaCungCap: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(NhaCungCap ncc) {
        if (ncc == null) return false;
        if (ncc.getId() == null || ncc.getId().trim().isEmpty()) {
            ncc.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO DNHACUNGCAP (ID, MANHACUNGCAP, NAME, DIACHI, DIENTHOAI, EMAIL, WEBSITE, " +
                "DNHOMNHACUNGCAPID, NOTE, STATUS, TIMECREATED, USERCREATEDID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getId());
            ps.setString(2, ncc.getMaNhaCungCap());
            ps.setString(3, ncc.getName());
            ps.setString(4, ncc.getDiaChi());
            ps.setString(5, ncc.getDienThoai());
            ps.setString(6, ncc.getEmail());
            ps.setString(7, ncc.getWebsite());
            ps.setString(8, ncc.getNhomNhaCungCapId());
            ps.setString(9, ncc.getNote());
            ps.setString(10, getEffectiveUserId());
            boolean success = ps.executeUpdate() > 0;
            if (success) audit("Nhà cung cấp", "THÊM", ncc.getMaNhaCungCap(), "Thêm nhà cung cấp: " + ncc.getName());
            return success;
        } catch (SQLException e) {
            System.err.println("Lỗi insert NhaCungCap: " + e.getMessage());
            return false;
        }
    }

    public boolean update(NhaCungCap ncc) {
        if (ncc == null || ncc.getId() == null) return false;

        String sql = "UPDATE DNHACUNGCAP SET MANHACUNGCAP = ?, NAME = ?, DIACHI = ?, DIENTHOAI = ?, " +
                "EMAIL = ?, WEBSITE = ?, DNHOMNHACUNGCAPID = ?, NOTE = ?, TIMEMODIFIED = CURRENT_TIMESTAMP, " +
                "USERMODIFIEDID = ? WHERE ID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ncc.getMaNhaCungCap());
            ps.setString(2, ncc.getName());
            ps.setString(3, ncc.getDiaChi());
            ps.setString(4, ncc.getDienThoai());
            ps.setString(5, ncc.getEmail());
            ps.setString(6, ncc.getWebsite());
            ps.setString(7, ncc.getNhomNhaCungCapId());
            ps.setString(8, ncc.getNote());
            ps.setString(9, getEffectiveUserId());
            ps.setString(10, ncc.getId());
            boolean success = ps.executeUpdate() > 0;
            if (success) audit("Nhà cung cấp", "SỬA", ncc.getMaNhaCungCap(), "Cập nhật nhà cung cấp: " + ncc.getName());
            return success;
        } catch (SQLException e) {
            System.err.println("Lỗi update NhaCungCap: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        if (id == null) return false;
        String sql = "UPDATE DNHACUNGCAP SET STATUS = -1, TIMEMODIFIED = CURRENT_TIMESTAMP WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            boolean success = ps.executeUpdate() > 0;
            if (success) audit("Nhà cung cấp", "XÓA", id, "Xóa nhà cung cấp ID: " + id);
            return success;
        } catch (SQLException e) {
            System.err.println("Lỗi delete NhaCungCap: " + e.getMessage());
            return false;
        }
    }

    public NhaCungCap getOffset(String currentId, int offset) {
        String sql = "SELECT ID FROM DNHACUNGCAP WHERE (STATUS IS NULL OR STATUS >= 0) ORDER BY NAME ASC";
        List<String> ids = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ids.add(rs.getString("ID"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getOffset NhaCungCap: " + e.getMessage());
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

    private NhaCungCap mapResultSetToNhaCungCap(ResultSet rs) throws SQLException {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setId(rs.getString("ID"));
        ncc.setMaNhaCungCap(rs.getString("MANHACUNGCAP"));
        ncc.setName(rs.getString("NAME"));
        ncc.setDiaChi(rs.getString("DIACHI"));
        ncc.setDienThoai(rs.getString("DIENTHOAI"));
        ncc.setEmail(rs.getString("EMAIL"));
        ncc.setWebsite(rs.getString("WEBSITE"));
        ncc.setNhomNhaCungCapId(rs.getString("DNHOMNHACUNGCAPID"));
        ncc.setNote(rs.getString("NOTE"));
        ncc.setStatus(rs.getInt("STATUS"));
        ncc.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        ncc.setUserCreatedId(rs.getString("USERCREATEDID"));
        ncc.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        ncc.setUserModifiedId(rs.getString("USERMODIFIEDID"));

        try {
            ncc.setTenNhomNhaCungCap(rs.getString("TENNHOM"));
        } catch (SQLException ignored) {}
        try {
            ncc.setUserCreatedName(rs.getString("TENNGUOITAO"));
            if (ncc.getUserCreatedName() == null) ncc.setUserCreatedName("Administrator");
        } catch (SQLException ignored) {}
        try {
            ncc.setUserModifiedName(rs.getString("TENNGUOISUA"));
        } catch (SQLException ignored) {}

        return ncc;
    }
}
