package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DotKhuyenMai;
import kazuko.ex.model.danhmuc.DotKhuyenMaiMuaTangChiTiet;
import kazuko.ex.model.danhmuc.LoaiHinhKhuyenMai;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

public class DotKhuyenMaiDAOTest {

    @Test
    public void testLoaiHinhKhuyenMaiDAO() {
        LoaiHinhKhuyenMaiDAO dao = new LoaiHinhKhuyenMaiDAO();
        List<LoaiHinhKhuyenMai> list = dao.getAll();
        Assertions.assertNotNull(list);
        Assertions.assertFalse(list.isEmpty(), "LoaiHinhKhuyenMai list should not be empty");

        // Verify there is no 'Chưa thiết lập' in DB list
        boolean hasChuaThietLap = list.stream().anyMatch(l -> "Chưa thiết lập".equalsIgnoreCase(l.getName()));
        Assertions.assertFalse(hasChuaThietLap, "Should not contain 'Chưa thiết lập'");

        // Test update
        LoaiHinhKhuyenMai first = list.get(0);
        String oldName = first.getName();
        first.setName(oldName + " Test");
        dao.update(first);

        LoaiHinhKhuyenMai updated = dao.findById(first.getId());
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(oldName + " Test", updated.getName());

        // Restore name
        first.setName(oldName);
        dao.update(first);
    }

    @Test
    public void testDotKhuyenMaiCRUD() {
        DotKhuyenMaiDAO dao = new DotKhuyenMaiDAO();
        LoaiHinhKhuyenMaiDAO loaiDao = new LoaiHinhKhuyenMaiDAO();

        List<LoaiHinhKhuyenMai> loaiList = loaiDao.getAll();
        String loaiId = loaiList.isEmpty() ? "TEST_LOAI" : loaiList.get(0).getId();
        String loaiName = loaiList.isEmpty() ? "Loại Test" : loaiList.get(0).getName();

        DotKhuyenMai km = new DotKhuyenMai();
        km.setName("KM Tết Tri Ân " + System.currentTimeMillis());
        km.setdLoaiHinhKhuyenMaiId(loaiId);
        km.setTenLoaiHinhKhuyenMai(loaiName);
        km.setTuNgay(new Timestamp(System.currentTimeMillis()));
        km.setDenNgay(new Timestamp(System.currentTimeMillis() + 86400000L * 7));
        km.setTiLeGiamGia(15.0);
        km.setNgungApDung(false);
        km.setNote("Ghi chú test khuyến mãi");

        // 1. Insert
        boolean insertOk = dao.insert(km);
        Assertions.assertTrue(insertOk);
        Assertions.assertNotNull(km.getId());

        // 2. Find
        DotKhuyenMai loaded = dao.findById(km.getId());
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(km.getName(), loaded.getName());
        Assertions.assertEquals(15.0, loaded.getTiLeGiamGia());

        // 3. Update
        loaded.setName(km.getName() + " (Đã sửa)");
        loaded.setTiLeGiamGia(20.0);
        boolean updateOk = dao.update(loaded);
        Assertions.assertTrue(updateOk);

        DotKhuyenMai reloaded = dao.findById(km.getId());
        Assertions.assertEquals(km.getName() + " (Đã sửa)", reloaded.getName());
        Assertions.assertEquals(20.0, reloaded.getTiLeGiamGia());

        // 4. Move to Trash & Restore
        dao.moveToTrash(km.getId());
        Assertions.assertNull(dao.findActiveById(km.getId()));
        Assertions.assertNotNull(dao.findTrashById(km.getId()));
        Assertions.assertTrue(dao.getTrashItems().stream().anyMatch(t -> km.getId().equals(t.getId())));

        dao.restoreFromTrash(km.getId());
        Assertions.assertNotNull(dao.findActiveById(km.getId()));

        // 5. Delete permanently
        dao.deletePermanently(km.getId());
        Assertions.assertNull(dao.findById(km.getId()));
    }

    @Test
    public void testSanPhamDiscountsCRUD() {
        DotKhuyenMaiDAO dao = new DotKhuyenMaiDAO();
        MatHangDAO mhDao = new MatHangDAO();
        LoaiHinhKhuyenMaiDAO loaiDao = new LoaiHinhKhuyenMaiDAO();
        List<LoaiHinhKhuyenMai> loaiList = loaiDao.getAll();
        String loaiSpId = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("sản phẩm") || l.getName().toLowerCase().contains("san pham")))
                .map(LoaiHinhKhuyenMai::getId)
                .findFirst().orElse(loaiList.isEmpty() ? "TEST_SP" : loaiList.get(0).getId());
        String loaiSpName = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("sản phẩm") || l.getName().toLowerCase().contains("san pham")))
                .map(LoaiHinhKhuyenMai::getName)
                .findFirst().orElse("Giảm giá theo sản phẩm");

        List<kazuko.ex.model.danhmuc.MatHang> products = mhDao.getAll();
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());

        String prodId1 = products.get(0).getId();
        String prodId2 = products.size() > 1 ? products.get(1).getId() : "PROD_2";

        DotKhuyenMai km = new DotKhuyenMai();
        km.setName("KM Giảm Giá Sản Phẩm Test " + System.currentTimeMillis());
        km.setdLoaiHinhKhuyenMaiId(loaiSpId);
        km.setTenLoaiHinhKhuyenMai(loaiSpName);
        km.setTuNgay(new Timestamp(System.currentTimeMillis()));
        km.setDenNgay(new Timestamp(System.currentTimeMillis() + 86400000L * 7));

        java.util.Map<String, Double> prodDiscounts = new java.util.HashMap<>();
        prodDiscounts.put(prodId1, 15.5);
        prodDiscounts.put(prodId2, 25.0);
        km.setMapSanPhamGiamGia(prodDiscounts);

        boolean insertOk = dao.insert(km);
        Assertions.assertTrue(insertOk);

        java.util.Map<String, Double> loadedDiscounts = dao.getProductDiscounts(km.getId());
        Assertions.assertNotNull(loadedDiscounts);
        Assertions.assertEquals(15.5, loadedDiscounts.get(prodId1));
        Assertions.assertEquals(25.0, loadedDiscounts.get(prodId2));

        // Update product discount
        prodDiscounts.put(prodId1, 30.0);
        dao.saveProductDiscounts(km.getId(), prodDiscounts);

        java.util.Map<String, Double> reloadedDiscounts = dao.getProductDiscounts(km.getId());
        Assertions.assertEquals(30.0, reloadedDiscounts.get(prodId1));

        // Clean up
        dao.deletePermanently(km.getId());
    }

    @Test
    public void testDongGiaDiscountsAndConflictValidation() {
        DotKhuyenMaiDAO dao = new DotKhuyenMaiDAO();
        MatHangDAO mhDao = new MatHangDAO();
        LoaiHinhKhuyenMaiDAO loaiDao = new LoaiHinhKhuyenMaiDAO();
        List<LoaiHinhKhuyenMai> loaiList = loaiDao.getAll();
        String loaiDgId = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("đồng giá") || l.getName().toLowerCase().contains("dong gia")))
                .map(LoaiHinhKhuyenMai::getId)
                .findFirst().orElse("8be106e5-37b5-4523-8eec-a7c6ea0d40f9");
        String loaiDgName = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("đồng giá") || l.getName().toLowerCase().contains("dong gia")))
                .map(LoaiHinhKhuyenMai::getName)
                .findFirst().orElse("Mặt hàng đồng giá");

        List<kazuko.ex.model.danhmuc.MatHang> products = mhDao.getAll();
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());

        String prodId1 = products.get(0).getId();

        // 1. Create S1: 10/10/2026 -> 15/10/2026
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        Timestamp tuNgayS1 = null;
        Timestamp denNgayS1 = null;
        Timestamp tuNgayOverlap = null;
        Timestamp denNgayOverlap = null;
        Timestamp tuNgayBefore = null;
        Timestamp denNgayBefore = null;
        Timestamp tuNgayAfter = null;
        Timestamp denNgayAfter = null;
        try {
            tuNgayS1 = new Timestamp(sdf.parse("2026-10-10").getTime());
            denNgayS1 = new Timestamp(sdf.parse("2026-10-15").getTime());

            tuNgayOverlap = new Timestamp(sdf.parse("2026-10-12").getTime());
            denNgayOverlap = new Timestamp(sdf.parse("2026-10-20").getTime());

            tuNgayBefore = new Timestamp(sdf.parse("2026-10-01").getTime());
            denNgayBefore = new Timestamp(sdf.parse("2026-10-09").getTime());

            tuNgayAfter = new Timestamp(sdf.parse("2026-10-16").getTime());
            denNgayAfter = new Timestamp(sdf.parse("2026-10-25").getTime());
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }

        DotKhuyenMai s1 = new DotKhuyenMai();
        s1.setName("Sale Đồng Giá 50k - S1");
        s1.setdLoaiHinhKhuyenMaiId(loaiDgId);
        s1.setTenLoaiHinhKhuyenMai(loaiDgName);
        s1.setTuNgay(tuNgayS1);
        s1.setDenNgay(denNgayS1);
        s1.setListCuaHangApDungIds(java.util.Arrays.asList("STORE_HN", "STORE_DN"));

        java.util.Map<String, Double> s1Prices = new java.util.HashMap<>();
        s1Prices.put(prodId1, 50000.0);
        s1.setMapDongGiaSanPham(s1Prices);

        boolean insertOk = dao.insert(s1);
        Assertions.assertTrue(insertOk);
        Assertions.assertNotNull(s1.getId());

        // Verify retrieval
        java.util.Map<String, Double> loadedPrices = dao.getDongGiaProductPrices(s1.getId());
        Assertions.assertNotNull(loadedPrices);
        Assertions.assertEquals(50000.0, loadedPrices.get(prodId1));

        // Test 1: S2 overlapping with S1 (12/10 - 20/10) with same store STORE_HN -> MUST CONFLICT
        List<DotKhuyenMaiDAO.ConflictPromotionInfo> conflictsOverlapSameStore = dao.checkDongGiaConflicts(
                null,
                tuNgayOverlap,
                denNgayOverlap,
                null,
                null,
                java.util.Collections.singletonList("STORE_HN"),
                java.util.Collections.singletonList(prodId1)
        );
        Assertions.assertFalse(conflictsOverlapSameStore.isEmpty(), "Should detect conflict when dates and stores overlap");
        Assertions.assertEquals(s1.getId(), conflictsOverlapSameStore.get(0).getDotKmId());
        Assertions.assertEquals(prodId1, conflictsOverlapSameStore.get(0).getMathangId());

        // Test 2: S2 overlapping with S1 (12/10 - 20/10) with DIFFERENT store STORE_HCM -> NO CONFLICT!
        List<DotKhuyenMaiDAO.ConflictPromotionInfo> conflictsOverlapDiffStore = dao.checkDongGiaConflicts(
                null,
                tuNgayOverlap,
                denNgayOverlap,
                null,
                null,
                java.util.Collections.singletonList("STORE_HCM"),
                java.util.Collections.singletonList(prodId1)
        );
        Assertions.assertTrue(conflictsOverlapDiffStore.isEmpty(), "Should NOT conflict when applied stores are different");

        // Test 3: S3 before S1 (01/10 - 09/10) with same store STORE_HN -> NO CONFLICT
        List<DotKhuyenMaiDAO.ConflictPromotionInfo> conflictsBefore = dao.checkDongGiaConflicts(
                null,
                tuNgayBefore,
                denNgayBefore,
                null,
                null,
                java.util.Collections.singletonList("STORE_HN"),
                java.util.Collections.singletonList(prodId1)
        );
        Assertions.assertTrue(conflictsBefore.isEmpty(), "Should NOT conflict when date is before S1");

        // Test 4: S4 after S1 (16/10 - 25/10) with same store STORE_HN -> NO CONFLICT
        List<DotKhuyenMaiDAO.ConflictPromotionInfo> conflictsAfter = dao.checkDongGiaConflicts(
                null,
                tuNgayAfter,
                denNgayAfter,
                null,
                null,
                java.util.Collections.singletonList("STORE_HN"),
                java.util.Collections.singletonList(prodId1)
        );
        Assertions.assertTrue(conflictsAfter.isEmpty(), "Should NOT conflict when date is after S1");

        // Test 5: S1 editing itself -> Excludes itself, NO CONFLICT
        List<DotKhuyenMaiDAO.ConflictPromotionInfo> conflictsSelf = dao.checkDongGiaConflicts(
                s1.getId(),
                tuNgayS1,
                denNgayS1,
                null,
                null,
                java.util.Collections.singletonList("STORE_HN"),
                java.util.Collections.singletonList(prodId1)
        );
        Assertions.assertTrue(conflictsSelf.isEmpty(), "Should NOT conflict with itself");

        // Clean up
        dao.deletePermanently(s1.getId());
    }

    @Test
    public void testMuaTangDiscountsCRUD() {
        DotKhuyenMaiDAO dao = new DotKhuyenMaiDAO();
        MatHangDAO mhDao = new MatHangDAO();
        LoaiHinhKhuyenMaiDAO loaiDao = new LoaiHinhKhuyenMaiDAO();
        List<LoaiHinhKhuyenMai> loaiList = loaiDao.getAll();
        String loaiMtId = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("mua") || l.getName().toLowerCase().contains("tặng") || l.getName().toLowerCase().contains("tang")))
                .map(LoaiHinhKhuyenMai::getId)
                .findFirst().orElse("e5d39bb6-41a4-43ff-bc4d-e170ae4aeba2");
        String loaiMtName = loaiList.stream()
                .filter(l -> l.getName() != null && (l.getName().toLowerCase().contains("mua") || l.getName().toLowerCase().contains("tặng") || l.getName().toLowerCase().contains("tang")))
                .map(LoaiHinhKhuyenMai::getName)
                .findFirst().orElse("Mua/Tặng");

        List<kazuko.ex.model.danhmuc.MatHang> products = mhDao.getAll();
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());

        String prodId1 = products.get(0).getId();
        String prodId2 = products.size() > 1 ? products.get(1).getId() : products.get(0).getId();

        DotKhuyenMai km = new DotKhuyenMai();
        km.setName("KM Mua 1 Tặng 1 Test " + System.currentTimeMillis());
        km.setdLoaiHinhKhuyenMaiId(loaiMtId);
        km.setTenLoaiHinhKhuyenMai(loaiMtName);
        km.setTuNgay(new Timestamp(System.currentTimeMillis()));
        km.setDenNgay(new Timestamp(System.currentTimeMillis() + 86400000L * 7));

        DotKhuyenMaiMuaTangChiTiet rule1 = new DotKhuyenMaiMuaTangChiTiet();
        rule1.setMathangMuaId(prodId1);
        rule1.setSoLuongMua(2.0);
        rule1.setTangCungLoai(true);
        rule1.setMathangTangId(prodId1);
        rule1.setSoLuongTang(1.0);

        DotKhuyenMaiMuaTangChiTiet rule2 = new DotKhuyenMaiMuaTangChiTiet();
        rule2.setMathangMuaId(prodId1);
        rule2.setSoLuongMua(3.0);
        rule2.setTangCungLoai(false);
        rule2.setMathangTangId(prodId2);
        rule2.setSoLuongTang(2.0);

        km.setListMuaTangChiTiet(java.util.Arrays.asList(rule1, rule2));

        // 1. Insert promotion
        boolean insertOk = dao.insert(km);
        Assertions.assertTrue(insertOk);
        Assertions.assertNotNull(km.getId());

        // 2. Retrieve details
        List<DotKhuyenMaiMuaTangChiTiet> loadedDetails = dao.getMuaTangDetails(km.getId());
        Assertions.assertNotNull(loadedDetails);
        Assertions.assertEquals(2, loadedDetails.size());

        DotKhuyenMaiMuaTangChiTiet loadedRule1 = loadedDetails.stream()
                .filter(DotKhuyenMaiMuaTangChiTiet::isTangCungLoai)
                .findFirst().orElse(null);
        Assertions.assertNotNull(loadedRule1);
        Assertions.assertEquals(prodId1, loadedRule1.getMathangMuaId());
        Assertions.assertEquals(2.0, loadedRule1.getSoLuongMua());
        Assertions.assertTrue(loadedRule1.isTangCungLoai());
        Assertions.assertEquals(prodId1, loadedRule1.getMathangTangId());
        Assertions.assertEquals(1.0, loadedRule1.getSoLuongTang());

        DotKhuyenMaiMuaTangChiTiet loadedRule2 = loadedDetails.stream()
                .filter(r -> !r.isTangCungLoai())
                .findFirst().orElse(null);
        Assertions.assertNotNull(loadedRule2);
        Assertions.assertEquals(prodId1, loadedRule2.getMathangMuaId());
        Assertions.assertEquals(3.0, loadedRule2.getSoLuongMua());
        Assertions.assertFalse(loadedRule2.isTangCungLoai());
        Assertions.assertEquals(prodId2, loadedRule2.getMathangTangId());
        Assertions.assertEquals(2.0, loadedRule2.getSoLuongTang());

        // 3. Update details
        rule1.setSoLuongMua(5.0);
        rule1.setSoLuongTang(3.0);
        dao.saveMuaTangDetails(km.getId(), java.util.Collections.singletonList(rule1));

        List<DotKhuyenMaiMuaTangChiTiet> reloaded = dao.getMuaTangDetails(km.getId());
        Assertions.assertEquals(1, reloaded.size());
        Assertions.assertEquals(5.0, reloaded.get(0).getSoLuongMua());
        Assertions.assertEquals(3.0, reloaded.get(0).getSoLuongTang());

        // 4. Clean up
        dao.deletePermanently(km.getId());
        Assertions.assertTrue(dao.getMuaTangDetails(km.getId()).isEmpty());
    }
}
