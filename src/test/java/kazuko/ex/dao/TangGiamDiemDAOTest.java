package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.DiemHoaDon;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.KhachHangLoyaltySummary;
import kazuko.ex.model.danhmuc.TangGiamDiem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TangGiamDiemDAOTest {
    private TangGiamDiemDAO tangGiamDiemDAO;
    private KhachHangDAO khachHangDAO;

    @BeforeEach
    public void setUp() {
        tangGiamDiemDAO = new TangGiamDiemDAO();
        khachHangDAO = new KhachHangDAO();
    }

    @Test
    public void testGenerateNextSoPhieu() {
        String soPhieu = tangGiamDiemDAO.generateNextSoPhieu();
        Assertions.assertNotNull(soPhieu);
        String year2Digits = new SimpleDateFormat("yy").format(new Date());
        Assertions.assertTrue(soPhieu.startsWith("TG" + year2Digits + "/"), "SoPhieu should start with TGyy/: " + soPhieu);
    }

    @Test
    public void testTangGiamDiemCRUD() {
        // 1. Get or create a test customer
        List<KhachHang> khList = khachHangDAO.getAll();
        Assertions.assertFalse(khList.isEmpty(), "Should have customers in DB");
        KhachHang testKh = khList.get(0);

        // 2. Insert new TangGiamDiem
        TangGiamDiem tg = new TangGiamDiem();
        tg.setId(UUID.randomUUID().toString());
        tg.setKhachHangId(testKh.getId());
        tg.setNgay(new Timestamp(System.currentTimeMillis()));
        tg.setSoPhieu(tangGiamDiemDAO.generateNextSoPhieu());
        tg.setDiemTang(15.0);
        tg.setDiemGiam(5.0);
        tg.setLyDo("Tặng điểm sinh nhật");
        tg.setNote("Ghi chú kiểm thử JUnit");

        boolean inserted = tangGiamDiemDAO.insert(tg);
        Assertions.assertTrue(inserted, "Insert TangGiamDiem should succeed");

        // 3. Query
        List<TangGiamDiem> byKh = tangGiamDiemDAO.getByKhachHangId(testKh.getId());
        Assertions.assertFalse(byKh.isEmpty(), "Should find items for customer");
        TangGiamDiem found = byKh.stream().filter(item -> tg.getId().equals(item.getId())).findFirst().orElse(null);
        Assertions.assertNotNull(found, "Should find inserted item");
        Assertions.assertEquals(15.0, found.getDiemTang());
        Assertions.assertEquals(5.0, found.getDiemGiam());
        Assertions.assertEquals("Tặng điểm sinh nhật", found.getLyDo());

        // 4. Update
        found.setDiemTang(20.0);
        found.setLyDo("Tặng điểm sự kiện đặc biệt");
        boolean updated = tangGiamDiemDAO.update(found);
        Assertions.assertTrue(updated, "Update TangGiamDiem should succeed");

        TangGiamDiem reloaded = tangGiamDiemDAO.findById(tg.getId());
        Assertions.assertNotNull(reloaded);
        Assertions.assertEquals(20.0, reloaded.getDiemTang());
        Assertions.assertEquals("Tặng điểm sự kiện đặc biệt", reloaded.getLyDo());

        // 5. Delete
        boolean deleted = tangGiamDiemDAO.delete(tg.getId());
        Assertions.assertTrue(deleted, "Delete TangGiamDiem should succeed");
    }

    @Test
    public void testLoyaltySummaryListAndResetPoints() {
        List<KhachHangLoyaltySummary> summaries = tangGiamDiemDAO.getLoyaltySummaryList(20000.0);
        Assertions.assertNotNull(summaries);
        Assertions.assertFalse(summaries.isEmpty(), "Should return loyalty summaries for all customers");

        // Test Reset Points
        KhachHangLoyaltySummary first = summaries.get(0);
        String khId = first.getKhachHang().getId();

        // Add 50 points first
        TangGiamDiem tg = new TangGiamDiem();
        tg.setId(UUID.randomUUID().toString());
        tg.setKhachHangId(khId);
        tg.setNgay(new Timestamp(System.currentTimeMillis()));
        tg.setSoPhieu(tangGiamDiemDAO.generateNextSoPhieu());
        tg.setDiemTang(50.0);
        tg.setDiemGiam(0.0);
        tg.setLyDo("Tặng thử nghiệm");
        tangGiamDiemDAO.insert(tg);

        // Reset points
        boolean resetOk = tangGiamDiemDAO.resetDiem(khId, 50.0);
        Assertions.assertTrue(resetOk, "Reset points should succeed");

        List<TangGiamDiem> history = tangGiamDiemDAO.getByKhachHangId(khId);
        boolean hasResetRecord = history.stream().anyMatch(h -> "Reset điểm".equals(h.getLyDo()) && h.getDiemGiam() == 50.0);
        Assertions.assertTrue(hasResetRecord, "Should contain Reset điểm record in history");

        // Cleanup
        tangGiamDiemDAO.delete(tg.getId());
        history.stream().filter(h -> "Reset điểm".equals(h.getLyDo())).forEach(h -> tangGiamDiemDAO.delete(h.getId()));
    }
}
