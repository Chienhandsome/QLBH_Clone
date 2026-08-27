package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class NhanVienDAO extends BaseDAO {
    private static final List<NhanVien> MEMORY_EMPLOYEES = new CopyOnWriteArrayList<>();
    private static String defaultEmployeeId = null;
    private static Set<String> cachedColumns = null;

    static {
        initSampleData();
    }

    private static void initSampleData() {
        if (!MEMORY_EMPLOYEES.isEmpty()) return;

        // Active employees matching Screenshot 1
        NhanVien nv1 = new NhanVien("nv-1", "Hoàng Giang", "user_female_1");
        nv1.setCachTinhLuong(2);
        nv1.setDefault(true);
        MEMORY_EMPLOYEES.add(nv1);

        NhanVien nv2 = new NhanVien("nv-2", "Nguyễn Minh Trang", "user_female_2");
        nv2.setCachTinhLuong(2);
        MEMORY_EMPLOYEES.add(nv2);

        NhanVien nv3 = new NhanVien("nv-3", "Nguyễn Tú Oanh", "user_female_3");
        nv3.setCachTinhLuong(2);
        MEMORY_EMPLOYEES.add(nv3);

        NhanVien nv4 = new NhanVien("nv-4", "Phạm Huyền Trang", "user_female_4");
        nv4.setCachTinhLuong(2);
        MEMORY_EMPLOYEES.add(nv4);

        NhanVien nv5 = new NhanVien("nv-5", "Phạm Quế Chi", "user_female_5");
        nv5.setCachTinhLuong(2);
        MEMORY_EMPLOYEES.add(nv5);

        NhanVien nv6 = new NhanVien("nv-6", "Trần Thúy Hằng", "user_female_6");
        nv6.setCachTinhLuong(2);
        MEMORY_EMPLOYEES.add(nv6);

        // Deleted items matching Screenshot 4
        NhanVien nvTrash1 = new NhanVien("nv-7", "Hoàng Văn Tuấn", "user_male_1");
        nvTrash1.setStatus(-1);
        MEMORY_EMPLOYEES.add(nvTrash1);

        NhanVien nvTrashFolder = new NhanVien("nv-8", "Thư mục 1", "Folder.png");
        nvTrashFolder.setFolder(true);
        nvTrashFolder.setStatus(-1);
        MEMORY_EMPLOYEES.add(nvTrashFolder);
    }

    private String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    private synchronized Set<String> getTableColumns(Connection conn) {
        if (cachedColumns != null) return cachedColumns;
        Set<String> columns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM DNHANVIEN WHERE 1=0")) {
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(meta.getColumnName(i));
            }
            cachedColumns = columns;
        } catch (Exception e) {
            columns.add("ID");
            columns.add("NAME");
            columns.add("DIACHI");
            columns.add("DIENTHOAI");
            columns.add("NOTE");
            columns.add("STATUS");
            columns.add("PARENTID");
            columns.add("ITEMTYPE");
            cachedColumns = columns;
        }
        return cachedColumns;
    }

    private List<NhanVien> tryLoadFromDb(int statusFilter) {
        List<NhanVien> list = new ArrayList<>();
        try (Connection conn = getConnection()) {
            Set<String> cols = getTableColumns(conn);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM DNHANVIEN")) {

                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    if (cols.contains("ID")) nv.setId(rs.getString("ID"));
                    if (cols.contains("NAME")) nv.setName(rs.getString("NAME"));
                    if (cols.contains("DIACHI")) nv.setDiaChi(rs.getString("DIACHI"));
                    if (cols.contains("DIENTHOAI")) nv.setDienThoai(rs.getString("DIENTHOAI"));
                    if (cols.contains("AVATAR")) {
                        String av = rs.getString("AVATAR");
                        if (av != null && !av.trim().isEmpty()) nv.setAvatarIcon(av);
                    }
                    if (cols.contains("CACHTINHLUONG") && rs.getObject("CACHTINHLUONG") != null) {
                        nv.setCachTinhLuong(rs.getInt("CACHTINHLUONG"));
                    }
                    if (cols.contains("LUONGCA") && rs.getObject("LUONGCA") != null) {
                        nv.setLuongCa(rs.getDouble("LUONGCA"));
                    }
                    if (cols.contains("LUONGTHANG") && rs.getObject("LUONGTHANG") != null) {
                        nv.setLuongThang(rs.getDouble("LUONGTHANG"));
                    }
                    if (cols.contains("NGHITHU7") && rs.getObject("NGHITHU7") != null) {
                        nv.setNghiThu7(rs.getInt("NGHITHU7") == 1);
                    }
                    if (cols.contains("NGHICHUNHAT") && rs.getObject("NGHICHUNHAT") != null) {
                        nv.setNghiChuNhat(rs.getInt("NGHICHUNHAT") == 1);
                    }
                    if (cols.contains("NOTE")) nv.setGhiChu(rs.getString("NOTE"));
                    if (cols.contains("PARENTID")) nv.setParentId(rs.getString("PARENTID"));

                    if (cols.contains("ITEMTYPE")) {
                        Integer itemType = rs.getObject("ITEMTYPE") != null ? rs.getInt("ITEMTYPE") : null;
                        if (itemType != null && itemType == 1) {
                            nv.setFolder(true);
                            nv.setAvatarIcon("Folder.png");
                        } else if (itemType != null && itemType == 2) {
                            nv.setSeparator(true);
                        }
                    }

                    if (cols.contains("ISDEFAULT") && rs.getObject("ISDEFAULT") != null) {
                        nv.setDefault(rs.getInt("ISDEFAULT") == 1);
                    }

                    int st = 1;
                    if (cols.contains("STATUS") && rs.getObject("STATUS") != null) {
                        st = rs.getInt("STATUS");
                    }
                    nv.setStatus(st >= 0 ? 1 : -1);

                    if ((statusFilter >= 0 && nv.getStatus() >= 0) || (statusFilter < 0 && nv.getStatus() < 0)) {
                        list.add(nv);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb NhanVien: " + e.getMessage());
            return null;
        }
    }

    private boolean tryInsertToDb(NhanVien nv) {
        try (Connection conn = getConnection()) {
            Set<String> cols = getTableColumns(conn);
            List<String> insertCols = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            if (cols.contains("ID")) {
                insertCols.add("ID");
                values.add(nv.getId());
            }
            if (cols.contains("CODE")) {
                insertCols.add("CODE");
                values.add(nv.getId());
            }
            if (cols.contains("NAME")) {
                insertCols.add("NAME");
                values.add(nv.getName() != null ? nv.getName() : "");
            }
            if (cols.contains("DIACHI")) {
                insertCols.add("DIACHI");
                values.add(nv.getDiaChi());
            }
            if (cols.contains("DIENTHOAI")) {
                insertCols.add("DIENTHOAI");
                values.add(nv.getDienThoai());
            }
            if (cols.contains("AVATAR")) {
                insertCols.add("AVATAR");
                values.add(nv.getAvatarIcon());
            }
            if (cols.contains("CACHTINHLUONG")) {
                insertCols.add("CACHTINHLUONG");
                values.add(nv.getCachTinhLuong());
            }
            if (cols.contains("LUONGCA")) {
                insertCols.add("LUONGCA");
                values.add(nv.getLuongCa());
            }
            if (cols.contains("LUONGTHANG")) {
                insertCols.add("LUONGTHANG");
                values.add(nv.getLuongThang());
            }
            if (cols.contains("NGHITHU7")) {
                insertCols.add("NGHITHU7");
                values.add(nv.isNghiThu7() ? 1 : 0);
            }
            if (cols.contains("NGHICHUNHAT")) {
                insertCols.add("NGHICHUNHAT");
                values.add(nv.isNghiChuNhat() ? 1 : 0);
            }
            if (cols.contains("NOTE")) {
                insertCols.add("NOTE");
                values.add(nv.getGhiChu());
            }
            if (cols.contains("PARENTID")) {
                insertCols.add("PARENTID");
                values.add(nv.getParentId() != null ? nv.getParentId() : "");
            }
            if (cols.contains("ITEMTYPE")) {
                insertCols.add("ITEMTYPE");
                values.add(nv.isFolder() ? 1 : (nv.isSeparator() ? 2 : 0));
            }
            if (cols.contains("ISDEFAULT")) {
                insertCols.add("ISDEFAULT");
                values.add(nv.isDefault() ? 1 : 0);
            }
            if (cols.contains("STATUS")) {
                insertCols.add("STATUS");
                values.add(nv.getStatus() >= 0 ? 30 : -1);
            }
            if (cols.contains("USERCREATEDID")) {
                insertCols.add("USERCREATEDID");
                values.add(getEffectiveUserId());
            }
            if (cols.contains("TIMECREATED")) {
                insertCols.add("TIMECREATED");
                values.add(new Timestamp(System.currentTimeMillis()));
            }

            StringBuilder sql = new StringBuilder("INSERT INTO DNHANVIEN (");
            sql.append(String.join(", ", insertCols));
            sql.append(") VALUES (");
            for (int i = 0; i < insertCols.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                int affected = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return affected > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi insert DNHANVIEN: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateToDb(NhanVien nv) {
        try (Connection conn = getConnection()) {
            Set<String> cols = getTableColumns(conn);
            List<String> setClauses = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            if (cols.contains("CODE")) {
                setClauses.add("CODE = ?");
                values.add(nv.getId());
            }
            if (cols.contains("NAME")) {
                setClauses.add("NAME = ?");
                values.add(nv.getName() != null ? nv.getName() : "");
            }
            if (cols.contains("DIACHI")) {
                setClauses.add("DIACHI = ?");
                values.add(nv.getDiaChi());
            }
            if (cols.contains("DIENTHOAI")) {
                setClauses.add("DIENTHOAI = ?");
                values.add(nv.getDienThoai());
            }
            if (cols.contains("AVATAR")) {
                setClauses.add("AVATAR = ?");
                values.add(nv.getAvatarIcon());
            }
            if (cols.contains("CACHTINHLUONG")) {
                setClauses.add("CACHTINHLUONG = ?");
                values.add(nv.getCachTinhLuong());
            }
            if (cols.contains("LUONGCA")) {
                setClauses.add("LUONGCA = ?");
                values.add(nv.getLuongCa());
            }
            if (cols.contains("LUONGTHANG")) {
                setClauses.add("LUONGTHANG = ?");
                values.add(nv.getLuongThang());
            }
            if (cols.contains("NGHITHU7")) {
                setClauses.add("NGHITHU7 = ?");
                values.add(nv.isNghiThu7() ? 1 : 0);
            }
            if (cols.contains("NGHICHUNHAT")) {
                setClauses.add("NGHICHUNHAT = ?");
                values.add(nv.isNghiChuNhat() ? 1 : 0);
            }
            if (cols.contains("NOTE")) {
                setClauses.add("NOTE = ?");
                values.add(nv.getGhiChu());
            }
            if (cols.contains("PARENTID")) {
                setClauses.add("PARENTID = ?");
                values.add(nv.getParentId() != null ? nv.getParentId() : "");
            }
            if (cols.contains("ITEMTYPE")) {
                setClauses.add("ITEMTYPE = ?");
                values.add(nv.isFolder() ? 1 : (nv.isSeparator() ? 2 : 0));
            }
            if (cols.contains("ISDEFAULT")) {
                setClauses.add("ISDEFAULT = ?");
                values.add(nv.isDefault() ? 1 : 0);
            }
            if (cols.contains("STATUS")) {
                setClauses.add("STATUS = ?");
                values.add(nv.getStatus() >= 0 ? 30 : -1);
            }
            if (cols.contains("USERMODIFIEDID")) {
                setClauses.add("USERMODIFIEDID = ?");
                values.add(getEffectiveUserId());
            }
            if (cols.contains("TIMEMODIFIED")) {
                setClauses.add("TIMEMODIFIED = ?");
                values.add(new Timestamp(System.currentTimeMillis()));
            }

            StringBuilder sql = new StringBuilder("UPDATE DNHANVIEN SET ");
            sql.append(String.join(", ", setClauses));
            sql.append(" WHERE ID = ?");
            values.add(nv.getId());

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                int affected = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return affected > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi update DNHANVIEN: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateStatusInDb(String id, int status) {
        try (Connection conn = getConnection()) {
            Set<String> cols = getTableColumns(conn);
            List<String> setClauses = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            if (cols.contains("STATUS")) {
                setClauses.add("STATUS = ?");
                values.add(status >= 0 ? 30 : -1);
            }
            if (cols.contains("USERMODIFIEDID")) {
                setClauses.add("USERMODIFIEDID = ?");
                values.add(getEffectiveUserId());
            }
            if (cols.contains("TIMEMODIFIED")) {
                setClauses.add("TIMEMODIFIED = ?");
                values.add(new Timestamp(System.currentTimeMillis()));
            }

            if (setClauses.isEmpty()) return true;

            StringBuilder sql = new StringBuilder("UPDATE DNHANVIEN SET ");
            sql.append(String.join(", ", setClauses));
            sql.append(" WHERE ID = ?");
            values.add(id);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi update status DNHANVIEN: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateParentIdInDb(String id, String newParentId) {
        try (Connection conn = getConnection()) {
            Set<String> cols = getTableColumns(conn);
            if (!cols.contains("PARENTID")) return true;

            List<String> setClauses = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            setClauses.add("PARENTID = ?");
            values.add(newParentId != null ? newParentId : "");

            if (cols.contains("USERMODIFIEDID")) {
                setClauses.add("USERMODIFIEDID = ?");
                values.add(getEffectiveUserId());
            }
            if (cols.contains("TIMEMODIFIED")) {
                setClauses.add("TIMEMODIFIED = ?");
                values.add(new Timestamp(System.currentTimeMillis()));
            }

            StringBuilder sql = new StringBuilder("UPDATE DNHANVIEN SET ");
            sql.append(String.join(", ", setClauses));
            sql.append(" WHERE ID = ?");
            values.add(id);

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                int rows = ps.executeUpdate();
                if (!conn.getAutoCommit()) conn.commit();
                return rows > 0;
            }
        } catch (Exception e) {
            System.err.println("Lỗi update parentId DNHANVIEN: " + e.getMessage());
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DNHANVIEN WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            System.err.println("Lỗi delete DNHANVIEN: " + e.getMessage());
            return false;
        }
    }

    public List<NhanVien> getAllActive() {
        List<NhanVien> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            return dbList;
        }
        return MEMORY_EMPLOYEES.stream()
                .filter(nv -> nv.getStatus() == 1)
                .collect(Collectors.toList());
    }

    public List<NhanVien> getTrashItems() {
        List<NhanVien> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }
        return MEMORY_EMPLOYEES.stream()
                .filter(nv -> nv.getStatus() == -1)
                .collect(Collectors.toList());
    }

    public NhanVien findById(String id) {
        if (id == null) return null;
        for (NhanVien nv : getAllActive()) {
            if (id.equals(nv.getId())) return nv;
        }
        for (NhanVien nv : getTrashItems()) {
            if (id.equals(nv.getId())) return nv;
        }
        return null;
    }

    public boolean insert(NhanVien nv) {
        if (nv == null) return false;
        if (nv.getId() == null || nv.getId().trim().isEmpty()) {
            nv.setId("nv-" + UUID.randomUUID().toString().substring(0, 8));
        }
        nv.setStatus(1);
        boolean dbOk = tryInsertToDb(nv);

        MEMORY_EMPLOYEES.removeIf(item -> item.getId().equals(nv.getId()));
        MEMORY_EMPLOYEES.add(nv);
        return true;
    }

    public boolean update(NhanVien nv) {
        if (nv == null || nv.getId() == null) return false;
        boolean dbOk = tryUpdateToDb(nv);

        for (int i = 0; i < MEMORY_EMPLOYEES.size(); i++) {
            if (nv.getId().equals(MEMORY_EMPLOYEES.get(i).getId())) {
                MEMORY_EMPLOYEES.set(i, nv);
                return true;
            }
        }
        MEMORY_EMPLOYEES.add(nv);
        return true;
    }

    public boolean moveToTrash(String id) {
        tryUpdateStatusInDb(id, -1);
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setStatus(-1);
            return true;
        }
        return false;
    }

    public boolean restoreFromTrash(String id) {
        tryUpdateStatusInDb(id, 1);
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setStatus(1);
            return true;
        }
        return false;
    }

    public boolean deletePermanently(String id) {
        tryDeleteFromDb(id);
        return MEMORY_EMPLOYEES.removeIf(nv -> id != null && id.equals(nv.getId()));
    }

    public boolean updateParentId(String id, String newParentId) {
        tryUpdateParentIdInDb(id, newParentId);
        NhanVien nv = findById(id);
        if (nv != null) {
            nv.setParentId(newParentId != null && !newParentId.trim().isEmpty() ? newParentId : null);
            return true;
        }
        return false;
    }

    public String getDefaultEmployeeId() {
        List<NhanVien> active = getAllActive();
        for (NhanVien nv : active) {
            if (nv.isDefault()) {
                return nv.getId();
            }
        }
        if (defaultEmployeeId != null) {
            return defaultEmployeeId;
        }
        if (!active.isEmpty()) {
            return active.get(0).getId();
        }
        return null;
    }

    public void setDefaultEmployeeId(String id) {
        defaultEmployeeId = id;
        for (NhanVien nv : MEMORY_EMPLOYEES) {
            nv.setDefault(nv.getId() != null && nv.getId().equals(id));
        }
    }
}
