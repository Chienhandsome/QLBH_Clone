package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DotKhuyenMai;
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
        List<kazuko.ex.model.danhmuc.MatHang> products = mhDao.getAll();
        Assertions.assertNotNull(products);
        Assertions.assertFalse(products.isEmpty());

        String prodId1 = products.get(0).getId();
        String prodId2 = products.size() > 1 ? products.get(1).getId() : "PROD_2";

        DotKhuyenMai km = new DotKhuyenMai();
        km.setName("KM Giảm Giá Sản Phẩm Test " + System.currentTimeMillis());
        km.setTenLoaiHinhKhuyenMai("Giảm giá theo sản phẩm");
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
}
