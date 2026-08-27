package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DotKhuyenMaiDAO extends BaseDAO {
    private static final List<DotKhuyenMai> MEMORY_PROMOTIONS = new CopyOnWriteArrayList<>();
    private static final Map<String, DotKhuyenMai> TRASH_PROMOTIONS = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> PROMOTION_STORES = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> PROMOTION_GROUP_DISCOUNTS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> PROMOTION_PRODUCT_DISCOUNTS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Double>> PROMOTION_DONG_GIA_PRICES = new ConcurrentHashMap<>();

    private static String getEffectiveUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null && !AuthService.getCurrentUser().getId().trim().isEmpty()) {
            return AuthService.getCurrentUser().getId().trim();
        }
        return "4f1466a0-0756-4ba9-afa8-053b96ca7569";
    }

    public List<DotKhuyenMai> getAll() {
        List<DotKhuyenMai> dbList = tryLoadFromDb(1);
        if (dbList != null) {
            return dbList;
        }

        List<DotKhuyenMai> result = new ArrayList<>();
        for (DotKhuyenMai p : MEMORY_PROMOTIONS) {
            if (p.getStatus() >= 0 && !TRASH_PROMOTIONS.containsKey(p.getId())) {
                result.add(p);
            }
        }
        return result;
    }

    public List<DotKhuyenMai> getTrashItems() {
        List<DotKhuyenMai> dbList = tryLoadFromDb(-1);
        if (dbList != null) {
            return dbList;
        }

        List<DotKhuyenMai> trashList = new ArrayList<>();
        for (DotKhuyenMai p : MEMORY_PROMOTIONS) {
            if (p.getStatus() < 0 && p.getId() != null) {
                trashList.add(p);
            }
        }
        for (DotKhuyenMai tr : TRASH_PROMOTIONS.values()) {
            if (!trashList.contains(tr)) {
                trashList.add(tr);
            }
        }
        return trashList;
    }

    public List<DotKhuyenMai> search(String keyword, String loaiKmName, String loaiKmId) {
        List<DotKhuyenMai> all = getAll();
        List<DotKhuyenMai> result = new ArrayList<>();
        String kw = keyword != null ? keyword.trim().toLowerCase() : "";

        for (DotKhuyenMai p : all) {
            boolean matchGroup = true;
            if (loaiKmId != null && !loaiKmId.trim().isEmpty()) {
                matchGroup = loaiKmId.equalsIgnoreCase(p.getdLoaiHinhKhuyenMaiId());
            } else if (loaiKmName != null && !loaiKmName.trim().isEmpty() && !"Tất cả".equalsIgnoreCase(loaiKmName.trim())) {
                matchGroup = loaiKmName.equalsIgnoreCase(p.getTenLoaiHinhKhuyenMai());
            }

            if (!matchGroup) continue;

            if (kw.isEmpty()) {
                result.add(p);
            } else {
                boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(kw);
                boolean matchLoai = p.getTenLoaiHinhKhuyenMai() != null && p.getTenLoaiHinhKhuyenMai().toLowerCase().contains(kw);
                boolean matchNote = p.getNote() != null && p.getNote().toLowerCase().contains(kw);
                if (matchName || matchLoai || matchNote) {
                    result.add(p);
                }
            }
        }
        return result;
    }

    public DotKhuyenMai findActiveById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (DotKhuyenMai p : getAll()) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public DotKhuyenMai findTrashById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        for (DotKhuyenMai p : getTrashItems()) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    public DotKhuyenMai findById(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        DotKhuyenMai active = findActiveById(id);
        if (active != null) return active;
        return findTrashById(id);
    }

    public boolean insert(DotKhuyenMai km) {
        if (km == null) return false;
        if (km.getId() == null || km.getId().trim().isEmpty()) {
            km.setId(UUID.randomUUID().toString());
        }
        km.setStatus(30);
        km.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        km.setUserCreatedId(getEffectiveUserId());

        boolean dbSuccess = tryInsertToDb(km);
        MEMORY_PROMOTIONS.add(km);
        if (km.getListCuaHangApDungIds() != null) {
            saveStoresForPromotion(km.getId(), km.getListCuaHangApDungIds());
        }
        if (isDongGia(km)) {
            saveDongGiaProducts(km.getId(), km.getMapDongGiaSanPham());
        } else if (isSanPham(km)) {
            saveProductDiscounts(km.getId(), km.getMapSanPhamGiamGia());
        } else if (isNhom(km)) {
            saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
        } else {
            if (km.getMapNhomGiamGia() != null && !km.getMapNhomGiamGia().isEmpty()) {
                saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
            }
            if (km.getMapSanPhamGiamGia() != null && !km.getMapSanPhamGiamGia().isEmpty()) {
                saveProductDiscounts(km.getId(), km.getMapSanPhamGiamGia());
            }
            if (km.getMapDongGiaSanPham() != null && !km.getMapDongGiaSanPham().isEmpty()) {
                saveDongGiaProducts(km.getId(), km.getMapDongGiaSanPham());
            }
        }
        return dbSuccess || true;
    }

    public boolean update(DotKhuyenMai km) {
        if (km == null || km.getId() == null) return false;
        km.setTimeModified(new Timestamp(System.currentTimeMillis()));
        km.setUserModifiedId(getEffectiveUserId());

        boolean dbSuccess = tryUpdateToDb(km);
        for (int i = 0; i < MEMORY_PROMOTIONS.size(); i++) {
            if (km.getId().equals(MEMORY_PROMOTIONS.get(i).getId())) {
                MEMORY_PROMOTIONS.set(i, km);
                break;
            }
        }
        if (km.getListCuaHangApDungIds() != null) {
            saveStoresForPromotion(km.getId(), km.getListCuaHangApDungIds());
        }
        if (isDongGia(km)) {
            saveDongGiaProducts(km.getId(), km.getMapDongGiaSanPham());
        } else if (isSanPham(km)) {
            saveProductDiscounts(km.getId(), km.getMapSanPhamGiamGia());
        } else if (isNhom(km)) {
            saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
        } else {
            if (km.getMapNhomGiamGia() != null && !km.getMapNhomGiamGia().isEmpty()) {
                saveGroupDiscounts(km.getId(), km.getMapNhomGiamGia());
            }
            if (km.getMapSanPhamGiamGia() != null && !km.getMapSanPhamGiamGia().isEmpty()) {
                saveProductDiscounts(km.getId(), km.getMapSanPhamGiamGia());
            }
            if (km.getMapDongGiaSanPham() != null && !km.getMapDongGiaSanPham().isEmpty()) {
                saveDongGiaProducts(km.getId(), km.getMapDongGiaSanPham());
            }
        }
        return dbSuccess || true;
    }

    private boolean isDongGia(DotKhuyenMai km) {
        if (km == null) return false;
        String typeName = km.getTenLoaiHinhKhuyenMai();
        if (typeName != null && (typeName.toLowerCase().contains("đồng giá") || typeName.toLowerCase().contains("dong gia"))) return true;
        return "8be106e5-37b5-4523-8eec-a7c6ea0d40f9".equals(km.getdLoaiHinhKhuyenMaiId());
    }

    private boolean isSanPham(DotKhuyenMai km) {
        if (km == null) return false;
        String typeName = km.getTenLoaiHinhKhuyenMai();
        if (typeName != null && (typeName.toLowerCase().contains("sản phẩm") || typeName.toLowerCase().contains("san pham"))) return true;
        return "8402c931-8a13-4d19-a693-53f9f683647e".equals(km.getdLoaiHinhKhuyenMaiId()) || "ef644cf0-f1db-4966-9dc4-83921868449c".equals(km.getdLoaiHinhKhuyenMaiId());
    }

    private boolean isNhom(DotKhuyenMai km) {
        if (km == null) return false;
        String typeName = km.getTenLoaiHinhKhuyenMai();
        if (typeName != null && (typeName.toLowerCase().contains("nhóm") || typeName.toLowerCase().contains("nhom"))) return true;
        return "7c566d6f-6386-4e93-a01c-3645e1aba5e7".equals(km.getdLoaiHinhKhuyenMaiId());
    }

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, -1);
        DotKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_PROMOTIONS.put(id, found);
        }
        return dbSuccess;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        boolean dbSuccess = trySetStatusDb(id, 30);
        DotKhuyenMai found = findById(id);
        if (found != null) {
            found.setStatus(30);
            TRASH_PROMOTIONS.remove(id);
        }
        return dbSuccess;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        boolean dbSuccess = tryDeleteFromDb(id);
        TRASH_PROMOTIONS.remove(id);
        MEMORY_PROMOTIONS.removeIf(p -> id.equals(p.getId()));
        PROMOTION_STORES.remove(id);
        PROMOTION_GROUP_DISCOUNTS.remove(id);
        PROMOTION_PRODUCT_DISCOUNTS.remove(id);
        PROMOTION_DONG_GIA_PRICES.remove(id);
        return dbSuccess;
    }

    public List<String> getStoresForPromotion(String dotKmId) {
        if (dotKmId == null) return new ArrayList<>();
        List<String> list = new ArrayList<>();
        String sql = "SELECT DCUAHANGID FROM DDOTKHUYENMAIAPDUNG WHERE DDOTKHUYENMAIID=? AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String chId = rs.getString("DCUAHANGID");
                    if (chId != null) list.add(chId);
                }
            }
            return list;
        } catch (Exception e) {
            if (PROMOTION_STORES.containsKey(dotKmId)) {
                return PROMOTION_STORES.get(dotKmId);
            }
            return new ArrayList<>();
        }
    }

    public boolean saveStoresForPromotion(String dotKmId, List<String> cuaHangIds) {
        if (dotKmId == null) return false;
        PROMOTION_STORES.put(dotKmId, new ArrayList<>(cuaHangIds != null ? cuaHangIds : Collections.emptyList()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAIAPDUNG WHERE DDOTKHUYENMAIID=?";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAIAPDUNG (ID, DDOTKHUYENMAIID, DCUAHANGID, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (cuaHangIds != null && !cuaHangIds.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (String chId : cuaHangIds) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, chId);
                        psIns.setString(4, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveStoresForPromotion: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getGroupDiscounts(String dotKmId) {
        if (dotKmId == null) return new HashMap<>();
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT DNHOMMATHANGID, TILEGIAMGIA FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DNHOMMATHANGID IS NOT NULL AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nhomId = rs.getString("DNHOMMATHANGID");
                    double tile = rs.getDouble("TILEGIAMGIA");
                    if (nhomId != null) {
                        map.put(nhomId, tile);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            if (PROMOTION_GROUP_DISCOUNTS.containsKey(dotKmId)) {
                return new HashMap<>(PROMOTION_GROUP_DISCOUNTS.get(dotKmId));
            }
            return map;
        }
    }

    public boolean saveGroupDiscounts(String dotKmId, Map<String, Double> groupDiscounts) {
        if (dotKmId == null) return false;
        PROMOTION_GROUP_DISCOUNTS.put(dotKmId, new HashMap<>(groupDiscounts != null ? groupDiscounts : Collections.emptyMap()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DNHOMMATHANGID IS NOT NULL";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAICHITIET (ID, DDOTKHUYENMAIID, DNHOMMATHANGID, TILEGIAMGIA, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (groupDiscounts != null && !groupDiscounts.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (Map.Entry<String, Double> entry : groupDiscounts.entrySet()) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, entry.getKey());
                        psIns.setDouble(4, entry.getValue() != null ? entry.getValue() : 0.0);
                        psIns.setString(5, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveGroupDiscounts: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getProductDiscounts(String dotKmId) {
        if (dotKmId == null) return Collections.emptyMap();
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT DMATHANGID, TILEGIAMGIA FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DMATHANGID IS NOT NULL AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mhId = rs.getString("DMATHANGID");
                    double tile = rs.getDouble("TILEGIAMGIA");
                    if (mhId != null) {
                        map.put(mhId, tile);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            if (PROMOTION_PRODUCT_DISCOUNTS.containsKey(dotKmId)) {
                return new HashMap<>(PROMOTION_PRODUCT_DISCOUNTS.get(dotKmId));
            }
            return map;
        }
    }

    public boolean saveProductDiscounts(String dotKmId, Map<String, Double> productDiscounts) {
        if (dotKmId == null) return false;
        PROMOTION_PRODUCT_DISCOUNTS.put(dotKmId, new HashMap<>(productDiscounts != null ? productDiscounts : Collections.emptyMap()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DMATHANGID IS NOT NULL";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAICHITIET (ID, DDOTKHUYENMAIID, DMATHANGID, TILEGIAMGIA, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (productDiscounts != null && !productDiscounts.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (Map.Entry<String, Double> entry : productDiscounts.entrySet()) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, entry.getKey());
                        psIns.setDouble(4, entry.getValue() != null ? entry.getValue() : 0.0);
                        psIns.setString(5, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveProductDiscounts: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getDongGiaProductPrices(String dotKmId) {
        if (dotKmId == null) return Collections.emptyMap();
        Map<String, Double> map = new HashMap<>();
        String sql = "SELECT DMATHANGID, GIABAN FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DMATHANGID IS NOT NULL AND (STATUS IS NULL OR STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dotKmId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mhId = rs.getString("DMATHANGID");
                    double giaBan = rs.getDouble("GIABAN");
                    if (mhId != null) {
                        map.put(mhId, giaBan);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            if (PROMOTION_DONG_GIA_PRICES.containsKey(dotKmId)) {
                return new HashMap<>(PROMOTION_DONG_GIA_PRICES.get(dotKmId));
            }
            return map;
        }
    }

    public boolean saveDongGiaProducts(String dotKmId, Map<String, Double> productPrices) {
        if (dotKmId == null) return false;
        PROMOTION_DONG_GIA_PRICES.put(dotKmId, new HashMap<>(productPrices != null ? productPrices : Collections.emptyMap()));

        String sqlDelete = "DELETE FROM DDOTKHUYENMAICHITIET WHERE DDOTKHUYENMAIID=? AND DMATHANGID IS NOT NULL";
        String sqlInsert = "INSERT INTO DDOTKHUYENMAICHITIET (ID, DDOTKHUYENMAIID, DMATHANGID, GIABAN, STATUS, TIMECREATED, USERCREATEDID) VALUES (?, ?, ?, ?, 30, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(sqlDelete)) {
                psDel.setString(1, dotKmId);
                psDel.executeUpdate();
            }
            if (productPrices != null && !productPrices.isEmpty()) {
                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (Map.Entry<String, Double> entry : productPrices.entrySet()) {
                        psIns.setString(1, UUID.randomUUID().toString());
                        psIns.setString(2, dotKmId);
                        psIns.setString(3, entry.getKey());
                        psIns.setDouble(4, entry.getValue() != null ? entry.getValue() : 0.0);
                        psIns.setString(5, getEffectiveUserId());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            System.err.println("Lỗi saveDongGiaProducts: " + e.getMessage());
            return false;
        }
    }

    public static class ConflictPromotionInfo {
        private String dotKmId;
        private String dotKmName;
        private Timestamp tuNgay;
        private Timestamp denNgay;
        private Timestamp tuGio;
        private Timestamp denGio;
        private String mathangId;
        private List<String> conflictingStoreIds = new ArrayList<>();

        public ConflictPromotionInfo(String dotKmId, String dotKmName, Timestamp tuNgay, Timestamp denNgay, Timestamp tuGio, Timestamp denGio, String mathangId, List<String> conflictingStoreIds) {
            this.dotKmId = dotKmId;
            this.dotKmName = dotKmName;
            this.tuNgay = tuNgay;
            this.denNgay = denNgay;
            this.tuGio = tuGio;
            this.denGio = denGio;
            this.mathangId = mathangId;
            this.conflictingStoreIds = conflictingStoreIds != null ? conflictingStoreIds : new ArrayList<>();
        }

        public String getDotKmId() { return dotKmId; }
        public String getDotKmName() { return dotKmName; }
        public Timestamp getTuNgay() { return tuNgay; }
        public Timestamp getDenNgay() { return denNgay; }
        public Timestamp getTuGio() { return tuGio; }
        public Timestamp getDenGio() { return denGio; }
        public String getMathangId() { return mathangId; }
        public List<String> getConflictingStoreIds() { return conflictingStoreIds; }
    }

    public static Timestamp combineDateTime(Timestamp date, Timestamp time, boolean isEndOfDayDefault) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        if (time != null) {
            Calendar tCal = Calendar.getInstance();
            tCal.setTimeInMillis(time.getTime());
            cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND, 0);
        } else {
            if (isEndOfDayDefault) {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
        }
        return new Timestamp(cal.getTimeInMillis());
    }

    public List<ConflictPromotionInfo> checkDongGiaConflicts(
            String currentDotKmId,
            Timestamp tuNgay,
            Timestamp denNgay,
            Timestamp tuGio,
            Timestamp denGio,
            Collection<String> currentStoreIds,
            Collection<String> productIds) {
        List<ConflictPromotionInfo> conflicts = new ArrayList<>();
        if (tuNgay == null || denNgay == null || productIds == null || productIds.isEmpty()) {
            return conflicts;
        }

        Timestamp targetStart = combineDateTime(tuNgay, tuGio, false);
        Timestamp targetEnd = combineDateTime(denNgay, denGio, true);
        if (targetStart == null || targetEnd == null) return conflicts;

        List<DotKhuyenMai> allKMs = getAll();
        for (DotKhuyenMai km : allKMs) {
            if (km.getId() != null && km.getId().equals(currentDotKmId)) {
                continue;
            }
            String loaiName = km.getTenLoaiHinhKhuyenMai();
            boolean isDongGia = (loaiName != null && (loaiName.toLowerCase().contains("đồng giá") || loaiName.toLowerCase().contains("dong gia")))
                    || "8be106e5-37b5-4523-8eec-a7c6ea0d40f9".equals(km.getdLoaiHinhKhuyenMaiId());
            if (!isDongGia) {
                continue;
            }

            Timestamp otherStart = combineDateTime(km.getTuNgay(), km.getTuGio(), false);
            Timestamp otherEnd = combineDateTime(km.getDenNgay(), km.getDenGio(), true);
            if (otherStart == null || otherEnd == null) continue;

            // 1. Time range overlap check: targetStart <= otherEnd && targetEnd >= otherStart
            if (targetStart.after(otherEnd) || targetEnd.before(otherStart)) {
                continue;
            }

            // 2. Store overlap check:
            // If stores are configured for both promotions and have NO intersection, they do NOT conflict!
            List<String> otherStoreIds = getStoresForPromotion(km.getId());
            List<String> overlappingStoreIds = new ArrayList<>();
            if (currentStoreIds != null && !currentStoreIds.isEmpty() && otherStoreIds != null && !otherStoreIds.isEmpty()) {
                for (String sId : currentStoreIds) {
                    if (otherStoreIds.contains(sId)) {
                        overlappingStoreIds.add(sId);
                    }
                }
                if (overlappingStoreIds.isEmpty()) {
                    continue; // Completely different stores -> NO CONFLICT!
                }
            } else {
                if (currentStoreIds != null) overlappingStoreIds.addAll(currentStoreIds);
            }

            // 3. Product check
            Map<String, Double> kmProducts = getDongGiaProductPrices(km.getId());
            for (String pId : productIds) {
                if (kmProducts.containsKey(pId)) {
                    conflicts.add(new ConflictPromotionInfo(
                            km.getId(),
                            km.getName(),
                            km.getTuNgay(),
                            km.getDenNgay(),
                            km.getTuGio(),
                            km.getDenGio(),
                            pId,
                            overlappingStoreIds
                    ));
                }
            }
        }
        return conflicts;
    }

    public List<ConflictPromotionInfo> checkDongGiaConflicts(
            String currentDotKmId,
            Timestamp tuNgay,
            Timestamp denNgay,
            Timestamp tuGio,
            Timestamp denGio,
            Collection<String> productIds) {
        return checkDongGiaConflicts(currentDotKmId, tuNgay, denNgay, tuGio, denGio, null, productIds);
    }

    private List<DotKhuyenMai> tryLoadFromDb(int statusFilter) {
        List<DotKhuyenMai> list = new ArrayList<>();
        String sql = "SELECT k.ID, k.NAME, k.NOTE, k.STATUS, k.USERMODIFIEDID, k.TIMEMODIFIED, k.TIMECREATED, k.USERCREATEDID, " +
                     "k.DLOAIHINHKHUYENMAIID, l.NAME as TENLOAIKM, k.TUNGAY, k.DENNGAY, k.NGUNGAPDUNG, k.TILEGIAMGIA, k.TUGIO, k.DENGIO " +
                     "FROM DDOTKHUYENMAI k " +
                     "LEFT JOIN DLOAIHINHKHUYENMAI l ON k.DLOAIHINHKHUYENMAIID = l.ID " +
                     "WHERE " + (statusFilter >= 0 ? "(k.STATUS IS NULL OR k.STATUS >= 0)" : "k.STATUS < 0") + " " +
                     "ORDER BY k.NAME";
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DotKhuyenMai km = new DotKhuyenMai();
                    km.setId(rs.getString("ID"));
                    km.setName(rs.getString("NAME"));
                    km.setNote(rs.getString("NOTE"));
                    km.setStatus(rs.getInt("STATUS"));
                    km.setUserModifiedId(rs.getString("USERMODIFIEDID"));
                    km.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
                    km.setTimeCreated(rs.getTimestamp("TIMECREATED"));
                    km.setUserCreatedId(rs.getString("USERCREATEDID"));
                    km.setdLoaiHinhKhuyenMaiId(rs.getString("DLOAIHINHKHUYENMAIID"));
                    km.setTenLoaiHinhKhuyenMai(rs.getString("TENLOAIKM"));
                    km.setTuNgay(rs.getTimestamp("TUNGAY"));
                    km.setDenNgay(rs.getTimestamp("DENNGAY"));
                    int ngung = rs.getInt("NGUNGAPDUNG");
                    km.setNgungApDung(ngung != 0);
                    km.setTiLeGiamGia(rs.getDouble("TILEGIAMGIA"));
                    km.setTuGio(rs.getTimestamp("TUGIO"));
                    km.setDenGio(rs.getTimestamp("DENGIO"));
                    list.add(km);
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Lỗi tryLoadFromDb DDOTKHUYENMAI: " + e.getMessage());
            return null;
        }
    }

    private boolean tryInsertToDb(DotKhuyenMai km) {
        String sql = "INSERT INTO DDOTKHUYENMAI (ID, NAME, NOTE, STATUS, TIMECREATED, USERCREATEDID, DLOAIHINHKHUYENMAIID, TUNGAY, DENNGAY, NGUNGAPDUNG, TILEGIAMGIA, TUGIO, DENGIO) " +
                     "VALUES (?, ?, ?, 30, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getId());
            ps.setString(2, km.getName());
            ps.setString(3, km.getNote() != null ? km.getNote() : "");
            ps.setString(4, getEffectiveUserId());
            ps.setString(5, km.getdLoaiHinhKhuyenMaiId());
            ps.setTimestamp(6, km.getTuNgay());
            ps.setTimestamp(7, km.getDenNgay());
            ps.setInt(8, km.isNgungApDung() ? 30 : 0);
            ps.setDouble(9, km.getTiLeGiamGia());
            ps.setTimestamp(10, km.getTuGio());
            ps.setTimestamp(11, km.getDenGio());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryInsertToDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean tryUpdateToDb(DotKhuyenMai km) {
        String sql = "UPDATE DDOTKHUYENMAI SET NAME=?, NOTE=?, DLOAIHINHKHUYENMAIID=?, TUNGAY=?, DENNGAY=?, NGUNGAPDUNG=?, TILEGIAMGIA=?, TUGIO=?, DENGIO=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, km.getName());
            ps.setString(2, km.getNote() != null ? km.getNote() : "");
            ps.setString(3, km.getdLoaiHinhKhuyenMaiId());
            ps.setTimestamp(4, km.getTuNgay());
            ps.setTimestamp(5, km.getDenNgay());
            ps.setInt(6, km.isNgungApDung() ? 30 : 0);
            ps.setDouble(7, km.getTiLeGiamGia());
            ps.setTimestamp(8, km.getTuGio());
            ps.setTimestamp(9, km.getDenGio());
            ps.setString(10, getEffectiveUserId());
            ps.setString(11, km.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryUpdateToDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean trySetStatusDb(String id, int status) {
        String sql = "UPDATE DDOTKHUYENMAI SET STATUS=?, TIMEMODIFIED=CURRENT_TIMESTAMP, USERMODIFIEDID=? WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, status);
            ps.setString(2, getEffectiveUserId());
            ps.setString(3, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi trySetStatusDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DDOTKHUYENMAI WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Lỗi tryDeleteFromDb DDOTKHUYENMAI: " + e.getMessage());
            return false;
        }
    }
}
