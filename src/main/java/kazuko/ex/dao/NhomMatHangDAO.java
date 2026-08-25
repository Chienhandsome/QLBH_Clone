package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class NhomMatHangDAO extends BaseDAO {
    private static final List<NhomMatHang> MEMORY_CATEGORIES = new CopyOnWriteArrayList<>();

    static {
        // Mock dữ liệu nhóm hàng phong phú chuẩn theo giao diện
        String[][] defaultData = {
                {"N1", "am", "ẫm sắc thuốc", "asterisk-yellow.png"},
                {"N2", "ANO", "Anova", "asterisk-yellow.png"},
                {"N3", "AC", "ANY COOK", "asterisk-yellow.png"},
                {"N4", "BAI", "Bài", "Folder.png"},
                {"N5", "BCDR", "Bàn chải đánh răng", "asterisk-yellow.png"},
                {"N6", "BNZ", "Benza", "Folder.png"},
                {"N7", "BG", "Bếp Ga", "Folder.png"},
                {"N8", "BST", "Besuto", "Folder.png"},
                {"N9", "SVT", "Bơm SaViTex", "Folder.png"},
                {"N10", "TP", "Bơm Tiền Phát", "Folder.png"},
                {"N11", "BG2", "Bột giặt", "asterisk-yellow.png"},
                {"N12", "CLN", "Cây lau nhà", "asterisk-yellow.png"},
                {"N13", "CHI", "Chì", "Folder.png"},
                {"N14", "CMT", "COMET", "Folder.png"},
                {"N15", "CTY", "Công ty", "asterisk-yellow.png"},
                {"N16", "CKC", "cookchen", "Folder.png"},
                {"N17", "CCK", "Cuckoo", "Folder.png"},
                {"N18", "DLX", "Daelux", "Folder.png"},
                {"N19", "DCR", "Dao cạo râu", "asterisk-yellow.png"},
                {"N20", "DD", "Dầu dây", "Folder.png"},
                {"N21", "DWO", "Dawo", "Folder.png"},
                {"N22", "DD2", "Dây Điện", "Folder.png"},
                {"N23", "DM", "Dũa móng", "asterisk-yellow.png"},
                {"N24", "DT", "Đạt Tường", "Folder.png"},
                {"N25", "DBV", "Đèn Bích Vân", "Folder.png"},
                {"N26", "DC", "Đèn chùa", "Folder.png"},
                {"N27", "DPS", "Đèn Pasuco", "Folder.png"},
                {"N28", "FTM", "Fantom", "Folder.png"},
                {"N29", "FRL", "Feroli", "Folder.png"},
                {"N30", "FJK", "Fujika", "Folder.png"},
                {"N31", "FSB", "Fushibo", "Folder.png"},
                {"N32", "GAD", "GA Dynamic", "Folder.png"},
                {"N33", "GVS", "Giấy vệ sinh + Băng vệ sinh", "Folder.png"},
                {"N34", "HNL", "Hanel", "Folder.png"},
                {"N35", "HRK", "Haraki", "Folder.png"},
                {"N36", "HST", "Hestia", "Folder.png"},
                {"N37", "SNK", "Sanaky", "Folder.png"},
                {"N38", "KLK", "Khaluck", "Folder.png"},
                {"N39", "NSD", "Nissei + Dura", "Folder.png"},
                {"N40", "STD", "Standa", "Folder.png"},
                {"N41", "VN", "Việt Nhật", "Folder.png"},
                {"N42", "HTG", "Hoatung", "Folder.png"}
        };

        for (String[] row : defaultData) {
            NhomMatHang nh = new NhomMatHang(row[0], row[1], row[2], row[3]);
            if ("N1".equals(row[0])) {
                nh.setKhongChoPhepGiamGia(true);
            }
            MEMORY_CATEGORIES.add(nh);
        }
    }

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<NhomMatHang> getAll() {
        List<NhomMatHang> dbList = tryLoadFromDb();
        if (dbList != null && !dbList.isEmpty()) {
            // Đồng bộ thêm các nhóm trong MEMORY_CATEGORIES nếu chưa có trong DB
            for (NhomMatHang memNh : MEMORY_CATEGORIES) {
                boolean found = false;
                for (NhomMatHang dbNh : dbList) {
                    if (memNh.getId().equals(dbNh.getId()) || (memNh.getName() != null && memNh.getName().equalsIgnoreCase(dbNh.getName()))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    dbList.add(memNh);
                }
            }
            return dbList;
        }
        return new ArrayList<>(MEMORY_CATEGORIES);
    }

    public NhomMatHang findById(String id) {
        if (id == null) return null;
        for (NhomMatHang nh : getAll()) {
            if (id.equals(nh.getId())) return nh;
        }
        return null;
    }

    public boolean insert(NhomMatHang nh) {
        if (nh == null) return false;
        if (nh.getId() == null || nh.getId().trim().isEmpty()) {
            nh.setId(UUID.randomUUID().toString());
        }
        if (nh.getCode() == null || nh.getCode().trim().isEmpty()) {
            nh.setCode(nh.getName() != null && nh.getName().length() >= 2 ? nh.getName().substring(0, 2).toUpperCase() : "NH");
        }

        // Cập nhật bộ nhớ cache
        boolean exists = false;
        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (nh.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, nh);
                exists = true;
                break;
            }
        }
        if (!exists) {
            MEMORY_CATEGORIES.add(0, nh);
        }

        tryInsertToDb(nh);
        return true;
    }

    public boolean update(NhomMatHang nh) {
        if (nh == null || nh.getId() == null) return false;
        for (int i = 0; i < MEMORY_CATEGORIES.size(); i++) {
            if (nh.getId().equals(MEMORY_CATEGORIES.get(i).getId())) {
                MEMORY_CATEGORIES.set(i, nh);
                break;
            }
        }
        tryUpdateToDb(nh);
        return true;
    }

    public boolean delete(String id) {
        if (id == null) return false;
        MEMORY_CATEGORIES.removeIf(n -> id.equals(n.getId()));
        tryDeleteFromDb(id);
        return true;
    }

    private List<NhomMatHang> tryLoadFromDb() {
        List<NhomMatHang> list = new ArrayList<>();
        String sql = "SELECT ID, CODE, NAME, NOTE FROM DNHOMMATHANG ORDER BY NAME";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                NhomMatHang nh = new NhomMatHang();
                nh.setId(rs.getString("ID"));
                nh.setCode(rs.getString("CODE"));
                nh.setName(rs.getString("NAME"));
                nh.setGhiChu(rs.getString("NOTE"));
                nh.setIconName("Folder.png");
                list.add(nh);
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean tryInsertToDb(NhomMatHang nh) {
        // Chiến lược 1: Thử insert với đầy đủ các cột chuẩn Firebird (STATUS, USERCREATEDID, TIMECREATED)
        String sqlFull = "INSERT INTO DNHOMMATHANG (ID, CODE, NAME, NOTE, STATUS, USERCREATEDID, TIMECREATED) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlFull)) {
            ps.setString(1, nh.getId());
            ps.setString(2, nh.getCode());
            ps.setString(3, nh.getName());
            ps.setString(4, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setInt(5, 30);
            ps.setString(6, getEffectiveUserId());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate() > 0;
        } catch (Exception e1) {
            // Chiến lược 2: Thử insert với STATUS
            String sqlStatus = "INSERT INTO DNHOMMATHANG (ID, CODE, NAME, NOTE, STATUS) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlStatus)) {
                ps.setString(1, nh.getId());
                ps.setString(2, nh.getCode());
                ps.setString(3, nh.getName());
                ps.setString(4, nh.getGhiChu() != null ? nh.getGhiChu() : "");
                ps.setInt(5, 30);
                return ps.executeUpdate() > 0;
            } catch (Exception e2) {
                // Chiến lược 3: Thử insert cơ bản
                String sqlBasic = "INSERT INTO DNHOMMATHANG (ID, CODE, NAME, NOTE) VALUES (?, ?, ?, ?)";
                try (Connection conn = getConnection();
                     PreparedStatement ps = conn.prepareStatement(sqlBasic)) {
                    ps.setString(1, nh.getId());
                    ps.setString(2, nh.getCode());
                    ps.setString(3, nh.getName());
                    ps.setString(4, nh.getGhiChu() != null ? nh.getGhiChu() : "");
                    return ps.executeUpdate() > 0;
                } catch (Exception e3) {
                    System.err.println("Lỗi insert DNHOMMATHANG vào CSDL: " + e3.getMessage());
                    return false;
                }
            }
        }
    }

    private boolean tryUpdateToDb(NhomMatHang nh) {
        String sql = "UPDATE DNHOMMATHANG SET CODE=?, NAME=?, NOTE=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nh.getCode());
            ps.setString(2, nh.getName());
            ps.setString(3, nh.getGhiChu() != null ? nh.getGhiChu() : "");
            ps.setString(4, nh.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi update DNHOMMATHANG vào CSDL: " + e.getMessage());
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DNHOMMATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
