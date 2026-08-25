package kazuko.ex.dao;

import kazuko.ex.model.system.STemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class STemplateDAO extends BaseDAO {

    // Danh sách 10 mẫu hóa đơn bán hàng chuẩn theo ứng dụng mẫu
    private static final List<STemplate> DEFAULT_INVOICE_TEMPLATES = new ArrayList<>();

    static {
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("1", "BIÊN BẢN GIAO NHẬN HÀNG HÓA"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("2", "Mẫu 54 mm x 2 dòng"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("3", "Mẫu in A5 cho máy in kim"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("4", "Mẫu in bill 54mm"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("5", "Mẫu in bill 80 chiết khấu mặt hàng"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("6", "Mẫu in bill 80mm"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("7", "Mẫu in bill A4"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("8", "Mẫu in bill A5"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("9", "Mẫu in chế biến"));
        DEFAULT_INVOICE_TEMPLATES.add(new STemplate("10", "Phiếu giao hàng"));
    }

    /**
     * Lấy danh sách mẫu hóa đơn bán hàng (được lọc riêng cho phân hệ Hóa đơn bán hàng / Cửa hàng)
     */
    public List<STemplate> getInvoiceTemplates() {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, SIMAGEID " +
                     "FROM STEMPLATE " +
                     "WHERE (STATUS = 30 OR STATUS IS NULL OR STATUS > 0) " +
                     "AND (REPORTBASE = 1 OR REPORTBASE = 0 " +
                     "     OR SFORMID LIKE '%BanHang%' OR SFORMID LIKE '%HoaDon%' " +
                     "     OR LOWER(NAME) LIKE '%bill%' OR LOWER(NAME) LIKE '%giao nhận%' " +
                     "     OR LOWER(NAME) LIKE '%chế biến%' OR LOWER(NAME) LIKE '%máy in kim%' " +
                     "     OR LOWER(NAME) LIKE '%phiếu giao%' OR LOWER(NAME) LIKE '%54 mm%') " +
                     "ORDER BY SORTORDER, NAME, AUTOID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                STemplate t = new STemplate();
                t.setId(rs.getString("ID"));
                t.setName(rs.getString("NAME"));
                t.setNote(rs.getString("NOTE"));
                if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                t.setSortOrder(rs.getString("SORTORDER"));
                if (rs.getObject("REPORTBASE") != null) t.setReportBase(rs.getInt("REPORTBASE"));
                if (rs.getObject("AUTOID") != null) t.setAutoId(rs.getInt("AUTOID"));
                if (rs.getObject("ITEMTYPE") != null) t.setItemType(rs.getInt("ITEMTYPE"));
                t.setSImageId(rs.getString("SIMAGEID"));
                if (t.getName() != null && !t.getName().trim().isEmpty()) {
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn getInvoiceTemplates từ STEMPLATE: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>(DEFAULT_INVOICE_TEMPLATES);
        }
        return list;
    }

    /**
     * Lấy danh sách tên các mẫu hóa đơn bán lẻ (10 mẫu)
     */
    public List<String> getInvoiceTemplateNames() {
        List<String> names = new ArrayList<>();
        List<STemplate> templates = getInvoiceTemplates();
        for (STemplate t : templates) {
            if (t.getName() != null && !t.getName().trim().isEmpty()) {
                names.add(t.getName().trim());
            }
        }
        return names;
    }

    /**
     * Lấy danh sách toàn bộ các Thư mục (Folder) mẫu in
     */
    public List<STemplate> getFolderList() {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, SIMAGEID " +
                     "FROM STEMPLATE " +
                     "WHERE ITEMTYPE = 1 AND (STATUS >= 0 OR STATUS IS NULL) " +
                     "ORDER BY SORTORDER, NAME, AUTOID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                STemplate t = new STemplate();
                t.setId(rs.getString("ID"));
                t.setName(rs.getString("NAME"));
                t.setNote(rs.getString("NOTE"));
                if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                t.setSortOrder(rs.getString("SORTORDER"));
                t.setItemType(1);
                t.setSImageId(rs.getString("SIMAGEID"));
                if (t.getName() != null && !t.getName().trim().isEmpty()) {
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy getFolderList: " + e.getMessage());
        }

        if (list.isEmpty()) {
            String[] folderNames = {
                    "Mẫu cơ bản", "Phiếu xuất kho", "Thống kê", "Phiếu nhập kho",
                    "Hóa đơn bán hàng", "Thống kê mặt hàng bán", "Phiếu thu",
                    "Phiếu kiểm kê", "Phiếu chi", "Sản xuất", "Phiếu chuyển kho",
                    "Thống kê doanh thu", "Đặt hàng nhà cung cấp", "Đặt hàng",
                    "Mặt hàng", "Báo giá", "Bảng lương"
            };
            for (int i = 0; i < folderNames.length; i++) {
                STemplate f = new STemplate("F" + (i + 1), folderNames[i]);
                f.setItemType(1);
                list.add(f);
            }
        }
        return list;
    }

    /**
     * Lấy danh sách mẫu in thuộc một Thư mục cụ thể
     */
    public List<STemplate> getTemplatesByFolder(String folderId, String folderName) {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, SIMAGEID, TEMPLATE, STYLE " +
                     "FROM STEMPLATE " +
                     "WHERE (PARENTID = ? OR PARENTDIR = ? OR PARENTID = (SELECT ID FROM STEMPLATE WHERE NAME = ? AND ITEMTYPE = 1)) " +
                     "AND ITEMTYPE = 0 AND (STATUS >= 0 OR STATUS IS NULL) " +
                     "ORDER BY SORTORDER, NAME, AUTOID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, folderId != null ? folderId : "");
            ps.setString(2, folderName != null ? folderName : "");
            ps.setString(3, folderName != null ? folderName : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    STemplate t = new STemplate();
                    t.setId(rs.getString("ID"));
                    t.setName(rs.getString("NAME"));
                    t.setNote(rs.getString("NOTE"));
                    if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                    t.setSortOrder(rs.getString("SORTORDER"));
                    t.setItemType(0);
                    t.setSImageId(rs.getString("SIMAGEID"));
                    t.setTemplate(rs.getString("TEMPLATE"));
                    t.setStyle(rs.getString("STYLE"));
                    if (t.getName() != null && !t.getName().trim().isEmpty()) {
                        list.add(t);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy getTemplatesByFolder: " + e.getMessage());
        }

        if (list.isEmpty() && folderName != null) {
            String fLower = folderName.trim().toLowerCase();
            if (fLower.contains("cơ bản")) {
                list.add(new STemplate("T1", "Mẫu 80"));
                list.add(new STemplate("T2", "Mẫu A4 nằm ngang"));
                list.add(new STemplate("T3", "Mẫu A4 thẳng đứng"));
            } else if (fLower.contains("bán hàng")) {
                list.addAll(getInvoiceTemplates());
            } else if (fLower.contains("xuất kho")) {
                list.add(new STemplate("XK1", "Phiếu xuất kho A4"));
                list.add(new STemplate("XK2", "Phiếu xuất kho A5"));
                list.add(new STemplate("XK3", "Phiếu xuất kho 80mm"));
            } else if (fLower.contains("nhập kho")) {
                list.add(new STemplate("NK1", "Phiếu nhập kho A4"));
                list.add(new STemplate("NK2", "Phiếu nhập kho A5"));
            } else if (fLower.contains("thu")) {
                list.add(new STemplate("PT1", "Phiếu thu A4"));
                list.add(new STemplate("PT2", "Phiếu thu A5"));
            } else if (fLower.contains("chi")) {
                list.add(new STemplate("PC1", "Phiếu chi A4"));
                list.add(new STemplate("PC2", "Phiếu chi A5"));
            } else if (fLower.contains("kiểm kê")) {
                list.add(new STemplate("KK1", "Phiếu kiểm kê A4"));
            } else if (fLower.contains("báo giá")) {
                list.add(new STemplate("BG1", "Báo giá 54mm"));
                list.add(new STemplate("BG2", "Báo giá A4 chuẩn"));
            } else if (fLower.contains("lương")) {
                list.add(new STemplate("BL1", "Bảng thanh toán tiền lương"));
            } else if (fLower.contains("doanh thu") || fLower.contains("thống kê")) {
                list.add(new STemplate("TK1", "Báo cáo kết ca 80mm"));
                list.add(new STemplate("TK2", "Báo cáo doanh thu A4"));
            } else {
                list.add(new STemplate("DEF1", "Mẫu in chuẩn A4"));
                list.add(new STemplate("DEF2", "Mẫu in 80mm"));
            }
        }
        return list;
    }

    /**
     * Lấy toàn bộ danh sách mẫu in trong hệ thống
     */
    public List<STemplate> getAllActive() {
        List<STemplate> list = new ArrayList<>();
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, SIMAGEID " +
                     "FROM STEMPLATE " +
                     "WHERE (STATUS >= 0 OR STATUS IS NULL) " +
                     "ORDER BY SORTORDER, NAME, AUTOID";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                STemplate t = new STemplate();
                t.setId(rs.getString("ID"));
                t.setName(rs.getString("NAME"));
                t.setNote(rs.getString("NOTE"));
                if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                t.setSortOrder(rs.getString("SORTORDER"));
                if (rs.getObject("REPORTBASE") != null) t.setReportBase(rs.getInt("REPORTBASE"));
                if (rs.getObject("AUTOID") != null) t.setAutoId(rs.getInt("AUTOID"));
                if (rs.getObject("ITEMTYPE") != null) t.setItemType(rs.getInt("ITEMTYPE"));
                t.setSImageId(rs.getString("SIMAGEID"));
                if (t.getName() != null && !t.getName().trim().isEmpty()) {
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn getAllActive từ STEMPLATE: " + e.getMessage());
        }

        if (list.isEmpty()) {
            return new ArrayList<>(DEFAULT_INVOICE_TEMPLATES);
        }
        return list;
    }

    /**
     * Lấy danh sách tên tất cả các mẫu in
     */
    public List<String> getAllActiveNames() {
        List<String> names = new ArrayList<>();
        List<STemplate> templates = getAllActive();
        for (STemplate t : templates) {
            if (t.getName() != null && !t.getName().trim().isEmpty()) {
                names.add(t.getName().trim());
            }
        }
        return names;
    }

    /**
     * Tìm mẫu in theo ID
     */
    public STemplate findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, TEMPLATE, STYLE " +
                     "FROM STEMPLATE WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    STemplate t = new STemplate();
                    t.setId(rs.getString("ID"));
                    t.setName(rs.getString("NAME"));
                    t.setNote(rs.getString("NOTE"));
                    if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                    t.setSortOrder(rs.getString("SORTORDER"));
                    if (rs.getObject("REPORTBASE") != null) t.setReportBase(rs.getInt("REPORTBASE"));
                    if (rs.getObject("AUTOID") != null) t.setAutoId(rs.getInt("AUTOID"));
                    if (rs.getObject("ITEMTYPE") != null) t.setItemType(rs.getInt("ITEMTYPE"));
                    t.setTemplate(rs.getString("TEMPLATE"));
                    t.setStyle(rs.getString("STYLE"));
                    return t;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm STEMPLATE theo ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm mẫu in theo Tên
     */
    public STemplate findByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String sql = "SELECT ID, NAME, NOTE, STATUS, SORTORDER, REPORTBASE, AUTOID, ITEMTYPE, TEMPLATE, STYLE " +
                     "FROM STEMPLATE WHERE NAME = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    STemplate t = new STemplate();
                    t.setId(rs.getString("ID"));
                    t.setName(rs.getString("NAME"));
                    t.setNote(rs.getString("NOTE"));
                    if (rs.getObject("STATUS") != null) t.setStatus(rs.getInt("STATUS"));
                    t.setSortOrder(rs.getString("SORTORDER"));
                    if (rs.getObject("REPORTBASE") != null) t.setReportBase(rs.getInt("REPORTBASE"));
                    if (rs.getObject("AUTOID") != null) t.setAutoId(rs.getInt("AUTOID"));
                    if (rs.getObject("ITEMTYPE") != null) t.setItemType(rs.getInt("ITEMTYPE"));
                    t.setTemplate(rs.getString("TEMPLATE"));
                    t.setStyle(rs.getString("STYLE"));
                    return t;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm STEMPLATE theo Name: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chuỗi nội dung XML mẫu in (cột TEMPLATE) theo ID
     */
    public String getTemplateXmlById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT TEMPLATE FROM STEMPLATE WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TEMPLATE");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy XML TEMPLATE theo ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy chuỗi nội dung XML mẫu in (cột TEMPLATE) theo Tên
     */
    public String getTemplateXmlByName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String sql = "SELECT TEMPLATE FROM STEMPLATE WHERE NAME = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TEMPLATE");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy XML TEMPLATE theo Name: " + e.getMessage());
        }
        return null;
    }
}
