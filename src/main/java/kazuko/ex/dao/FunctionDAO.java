package kazuko.ex.dao;

import kazuko.ex.model.system.SFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FunctionDAO extends BaseDAO {

    public List<SFunction> getAllFunctions() {
        List<SFunction> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, GROUPNAME, PARENTID, PARENTDIR, ITEMTYPE, SIMAGEID " +
                     "FROM SFUNCTION WHERE STATUS >= 0 ORDER BY GROUPNAME, SORTORDER, NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SFunction f = new SFunction();
                f.setId(rs.getString("ID"));
                f.setName(rs.getString("NAME"));
                f.setNote(rs.getString("NOTE"));
                f.setStatus(rs.getInt("STATUS"));
                f.setSortOrder(rs.getObject("SORTORDER") != null ? rs.getInt("SORTORDER") : 0);
                f.setGroupName(rs.getString("GROUPNAME"));
                f.setParentId(rs.getString("PARENTID"));
                f.setParentDir(rs.getString("PARENTDIR"));
                f.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                f.setSimageId(rs.getString("SIMAGEID"));
                list.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách chức năng SFUNCTION: " + e.getMessage());
        }
        return list;
    }

    public List<String> getDistinctGroupNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT GROUPNAME FROM SFUNCTION WHERE GROUPNAME IS NOT NULL AND STATUS >= 0 ORDER BY GROUPNAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String g = rs.getString(1);
                if (g != null && !g.trim().isEmpty()) {
                    list.add(g.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy nhóm chức năng: " + e.getMessage());
        }
        return list;
    }

    public List<SFunction> getFunctionsByGroupName(String groupName) {
        List<SFunction> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, GROUPNAME, PARENTID, PARENTDIR, ITEMTYPE, SIMAGEID " +
                     "FROM SFUNCTION WHERE GROUPNAME = ? AND STATUS >= 0 ORDER BY SORTORDER, NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SFunction f = new SFunction();
                    f.setId(rs.getString("ID"));
                    f.setName(rs.getString("NAME"));
                    f.setNote(rs.getString("NOTE"));
                    f.setStatus(rs.getInt("STATUS"));
                    f.setSortOrder(rs.getObject("SORTORDER") != null ? rs.getInt("SORTORDER") : 0);
                    f.setGroupName(rs.getString("GROUPNAME"));
                    f.setParentId(rs.getString("PARENTID"));
                    f.setParentDir(rs.getString("PARENTDIR"));
                    f.setItemType(rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : 0);
                    f.setSimageId(rs.getString("SIMAGEID"));
                    list.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy chức năng theo nhóm: " + e.getMessage());
        }
        return list;
    }
}
