package kazuko.ex.danhmuc;

import kazuko.ex.dao.DatHangNCCDAO;
import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.dathang.DatHangNCC;
import kazuko.ex.ui.danhmuc.DanhMucMatHangPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatHangDetailTabsFeatureTest {

    @BeforeAll
    public static void setupHeadless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    public void testDatHangNCCDAOGetByMatHang() {
        DatHangNCCDAO dao = new DatHangNCCDAO();
        MatHangDAO mhDao = new MatHangDAO();
        List<MatHang> allMH = mhDao.getAll();
        assertNotNull(allMH);

        if (!allMH.isEmpty()) {
            MatHang first = allMH.get(0);
            List<DatHangNCC> orders = dao.getByMatHang(first.getId(), first.getCode());
            assertNotNull(orders);
        }

        // Test with null and empty
        List<DatHangNCC> emptyList = dao.getByMatHang(null, null);
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testDonHangDAOGetPhieuSanXuatByMatHang() {
        DonHangDAO dao = new DonHangDAO();
        MatHangDAO mhDao = new MatHangDAO();
        List<MatHang> allMH = mhDao.getAll();
        assertNotNull(allMH);

        if (!allMH.isEmpty()) {
            MatHang first = allMH.get(0);
            List<DonHang> phieus = dao.getPhieuSanXuatByMatHang(first.getId(), first.getCode());
            assertNotNull(phieus);
        }

        // Test with null and empty
        List<DonHang> emptyList = dao.getPhieuSanXuatByMatHang(null, null);
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testDanhMucMatHangPanelBottomTabsStructure() throws Exception {
        DanhMucMatHangPanel panel = new DanhMucMatHangPanel();
        assertNotNull(panel);

        // Reflect bottom tabs
        Field tableDatHangNCCField = DanhMucMatHangPanel.class.getDeclaredField("tableDatHangNCC");
        tableDatHangNCCField.setAccessible(true);
        JTable tableDatHangNCC = (JTable) tableDatHangNCCField.get(panel);
        assertNotNull(tableDatHangNCC, "tableDatHangNCC phải được khởi tạo");

        Field tableSanXuatField = DanhMucMatHangPanel.class.getDeclaredField("tableSanXuat");
        tableSanXuatField.setAccessible(true);
        JTable tableSanXuat = (JTable) tableSanXuatField.get(panel);
        assertNotNull(tableSanXuat, "tableSanXuat phải được khởi tạo");

        // Verify DatHangNCC columns
        List<String> expectedDatHangCols = List.of(
                "Số phiếu", "Ngày", "Nhà cung cấp", "Tổng cộng", "Phí vận chuyển",
                "Tiền giảm giá", "Tỉ lệ giảm giá", "Tiền thuế", "Tỉ lệ thuế", "Tiền hàng", "Ghi chú"
        );
        for (String col : expectedDatHangCols) {
            boolean found = false;
            for (int i = 0; i < tableDatHangNCC.getColumnCount(); i++) {
                if (col.equals(tableDatHangNCC.getColumnName(i))) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Bảng Đặt hàng NCC phải có cột: " + col);
        }

        // Verify SanXuat columns
        List<String> expectedSanXuatCols = List.of(
                "Số phiếu", "Ngày", "Tổng cộng", "Kho thành phẩm", "Kho vật tư",
                "Bàn", "Số khách", "Đặt hàng nhà cung cấp"
        );
        for (String col : expectedSanXuatCols) {
            boolean found = false;
            for (int i = 0; i < tableSanXuat.getColumnCount(); i++) {
                if (col.equals(tableSanXuat.getColumnName(i))) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Bảng Sản xuất phải có cột: " + col);
        }
    }
}
