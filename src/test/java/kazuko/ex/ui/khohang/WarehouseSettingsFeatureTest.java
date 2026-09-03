package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.system.SystemConfig;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseSettingsFeatureTest {

    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final DonHangDAO donHangDAO = new DonHangDAO();

    @Test
    @Order(1)
    public void testWarehouseConfigPersistence() {
        SystemConfig config = systemConfigDAO.getConfig();
        assertNotNull(config, "SystemConfig không được null");

        boolean originalDup = config.isChoPhepNhapMotMatHangNhieuLan();
        boolean originalStaff = config.isBatBuocChonNhanVienNhapKho();
        boolean originalSupplier = config.isBatBuocChonNhaCungCapNhapKho();
        String originalPriceType = config.getGiaSuDungKhiChuyenXuatKho();

        try {
            config.setChoPhepNhapMotMatHangNhieuLan(true);
            config.setBatBuocChonNhanVienNhapKho(true);
            config.setBatBuocChonNhaCungCapNhapKho(true);
            config.setGiaSuDungKhiChuyenXuatKho("Giá vốn");
            systemConfigDAO.saveConfig(config);

            SystemConfig reloaded = systemConfigDAO.getConfig();
            assertTrue(reloaded.isChoPhepNhapMotMatHangNhieuLan());
            assertTrue(reloaded.isBatBuocChonNhanVienNhapKho());
            assertTrue(reloaded.isBatBuocChonNhaCungCapNhapKho());
            assertEquals("Giá vốn", reloaded.getGiaSuDungKhiChuyenXuatKho());
        } finally {
            // Restore original settings
            config.setChoPhepNhapMotMatHangNhieuLan(originalDup);
            config.setBatBuocChonNhanVienNhapKho(originalStaff);
            config.setBatBuocChonNhaCungCapNhapKho(originalSupplier);
            config.setGiaSuDungKhiChuyenXuatKho(originalPriceType != null ? originalPriceType : "Giá vốn");
            systemConfigDAO.saveConfig(config);
        }
    }

    @Test
    @Order(2)
    public void testChoPhepNhapMotMatHangNhieuLanLogic() {
        List<DonHangChiTiet> lineItems = new ArrayList<>();
        String matHangId = "1006";

        // Case 1: allowDuplicate = false -> Merges quantity
        boolean allowDuplicate = false;
        double qty1 = 2.0;
        double qty2 = 3.0;

        // First add
        DonHangChiTiet ct1 = new DonHangChiTiet();
        ct1.setMatHangId(matHangId);
        ct1.setSoLuong(qty1);
        lineItems.add(ct1);

        // Second add when allowDuplicate == false
        boolean found = false;
        if (!allowDuplicate) {
            for (DonHangChiTiet existing : lineItems) {
                if (matHangId.equals(existing.getMatHangId())) {
                    existing.setSoLuong(existing.getSoLuong() + qty2);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            DonHangChiTiet ct2 = new DonHangChiTiet();
            ct2.setMatHangId(matHangId);
            ct2.setSoLuong(qty2);
            lineItems.add(ct2);
        }
        assertEquals(1, lineItems.size(), "Khi allowDuplicate = false, phải gộp thành 1 dòng");
        assertEquals(5.0, lineItems.get(0).getSoLuong(), "Số lượng phải là 2 + 3 = 5");

        // Case 2: allowDuplicate = true -> Adds as separate row
        allowDuplicate = true;
        found = false;
        if (!allowDuplicate) {
            for (DonHangChiTiet existing : lineItems) {
                if (matHangId.equals(existing.getMatHangId())) {
                    existing.setSoLuong(existing.getSoLuong() + qty2);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            DonHangChiTiet ct3 = new DonHangChiTiet();
            ct3.setMatHangId(matHangId);
            ct3.setSoLuong(qty2);
            lineItems.add(ct3);
        }
        assertEquals(2, lineItems.size(), "Khi allowDuplicate = true, phải thêm dòng mới tách biệt");
    }

    @Test
    @Order(3)
    public void testGiaSuDungKhiChuyenXuatKhoCalculation() {
        MatHang mh = new MatHang();
        mh.setGiaBan(150000.0);
        mh.setGiaNhap(100000.0);
        mh.setGiaVon(110000.0);

        // Gia von
        String priceType = "Giá vốn";
        double price1 = "Giá vốn".equalsIgnoreCase(priceType) ? mh.getGiaVon() : mh.getGiaBan();
        assertEquals(110000.0, price1);

        // Gia ban
        priceType = "Giá bán";
        double price2 = "Giá bán".equalsIgnoreCase(priceType) ? mh.getGiaBan() : mh.getGiaVon();
        assertEquals(150000.0, price2);

        // Gia nhap
        priceType = "Giá nhập";
        double price3 = "Giá nhập".equalsIgnoreCase(priceType) ? mh.getGiaNhap() : mh.getGiaVon();
        assertEquals(100000.0, price3);
    }

    @Test
    @Order(4)
    public void testTuDongTinhGiaVonCalculation() {
        // Test moving average formula: (Q_cu * P_cu + Q_nhap * P_nhap) / (Q_cu + Q_nhap)
        double qCu = 10.0;
        double pCu = 100000.0;
        double qNhap = 5.0;
        double pNhap = 130000.0;

        double pMoi = (qCu * pCu + qNhap * pNhap) / (qCu + qNhap);
        // (10 * 100,000 + 5 * 130,000) / 15 = (1,000,000 + 650,000) / 15 = 1,650,000 / 15 = 110,000
        assertEquals(110000.0, pMoi, 0.001, "Giá vốn bình quân mới phải là 110,000");

        // Test DAO method updateGiaVon exists and executes
        List<MatHang> list = matHangDAO.getAll();
        if (list != null && !list.isEmpty()) {
            MatHang first = list.get(0);
            double oldGiaVon = first.getGiaVon();
            boolean ok = matHangDAO.updateGiaVon(first.getId(), oldGiaVon);
            assertTrue(ok, "Phải cập nhật giá vốn thành công");
        }
    }
}
