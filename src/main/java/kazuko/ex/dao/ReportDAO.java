package kazuko.ex.dao;

import kazuko.ex.model.system.SReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO extends BaseDAO {

    public List<SReport> getAllReports() {
        List<SReport> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, PARENTID, PARENTDIR, ITEMTYPE, SIMAGEID " +
                     "FROM SREPORT WHERE STATUS >= 0 ORDER BY SORTORDER, NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SReport r = new SReport();
                r.setId(rs.getString("ID"));
                r.setName(rs.getString("NAME"));
                r.setNote(rs.getString("NOTE"));
                r.setStatus(rs.getInt("STATUS"));
                r.setSortOrder(rs.getString("SORTORDER"));
                r.setParentId(rs.getString("PARENTID"));
                r.setParentDir(rs.getString("PARENTDIR"));
                r.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                r.setSimageId(rs.getString("SIMAGEID"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách báo cáo SREPORT: " + e.getMessage());
        }
        return list;
    }

    public List<SReport> getReportsByParentId(String parentId) {
        List<SReport> list = new ArrayList<>();
        String sql;
        if (parentId == null || parentId.trim().isEmpty()) {
            sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, PARENTID, PARENTDIR, ITEMTYPE, SIMAGEID FROM SREPORT WHERE (PARENTID IS NULL OR TRIM(PARENTID) = '') AND STATUS >= 0 ORDER BY SORTORDER, NAME";
        } else {
            sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, PARENTID, PARENTDIR, ITEMTYPE, SIMAGEID FROM SREPORT WHERE PARENTID = ? AND STATUS >= 0 ORDER BY SORTORDER, NAME";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (parentId != null && !parentId.trim().isEmpty()) {
                ps.setString(1, parentId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SReport r = new SReport();
                    r.setId(rs.getString("ID"));
                    r.setName(rs.getString("NAME"));
                    r.setNote(rs.getString("NOTE"));
                    r.setStatus(rs.getInt("STATUS"));
                    r.setSortOrder(rs.getString("SORTORDER"));
                    r.setParentId(rs.getString("PARENTID"));
                    r.setParentDir(rs.getString("PARENTDIR"));
                    r.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                    r.setSimageId(rs.getString("SIMAGEID"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy báo cáo con: " + e.getMessage());
        }
        return list;
    }
}
