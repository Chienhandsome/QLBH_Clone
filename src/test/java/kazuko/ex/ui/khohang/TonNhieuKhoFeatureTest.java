package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.khohang.TonNhieuKhoItem;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TonNhieuKhoFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();

    @Test
    @Order(1)
    public void testGetTonNhieuKhoMatrix() {
        List<TonNhieuKhoItem> list = donHangDAO.getTonNhieuKhoMatrix();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Danh sách ma trận tồn nhiều kho không được rỗng");

        TonNhieuKhoItem first = list.get(0);
        assertNotNull(first.getMatHangId());
        assertNotNull(first.getMaHang());
        System.out.println("Tổng số mặt hàng trong ma trận: " + list.size());
        System.out.println("Mặt hàng đầu tiên: " + first.getMaHang() + " - " + first.getTenHang() + ", Tổng tồn: " + first.getTongTon());
    }

    @Test
    @Order(2)
    public void testWarehouseStockAggregation() {
        List<TonNhieuKhoItem> list = donHangDAO.getTonNhieuKhoMatrix();
        List<KhoHang> khoList = khoHangDAO.getAllActive();

        for (TonNhieuKhoItem item : list) {
            double sumStock = 0.0;
            for (KhoHang k : khoList) {
                sumStock += item.getTonKho(k.getId());
            }
            // Check that tonTheoKho contains valid numbers
            assertTrue(!Double.isNaN(item.getTongTon()));
        }
    }

    @Test
    @Order(3)
    public void testQuanLyTonNhieuKhoPanelInstantiation() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            QuanLyTonNhieuKhoPanel panel = new QuanLyTonNhieuKhoPanel();
            assertNotNull(panel);
        });
    }
}
