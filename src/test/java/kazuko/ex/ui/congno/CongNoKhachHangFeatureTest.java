package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.model.congno.ChiTietCongNoKhachHangEntry;
import kazuko.ex.model.congno.CongNoKhachHangItem;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CongNoKhachHangFeatureTest {

    private final CongNoKhachHangDAO dao = new CongNoKhachHangDAO();

    @Test
    @Order(1)
    public void testGetDanhSachCongNoKhachHang() {
        List<CongNoKhachHangItem> list = dao.getDanhSachCongNoKhachHang(null, null, false);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Danh sách khách hàng không được rỗng");

        System.out.println("Tổng số khách hàng lấy được: " + list.size());
        CongNoKhachHangItem first = list.get(0);
        assertNotNull(first.getKhachHangId());
        assertNotNull(first.getTenKhach());
        System.out.println("Khách hàng 1: " + first.getTenKhach() + " (Mã: " + first.getMaKhach() + "), Còn nợ: " + first.getConNo());
    }

    @Test
    @Order(2)
    public void testGetChiTietCongNo() {
        List<CongNoKhachHangItem> list = dao.getDanhSachCongNoKhachHang(null, null, false);
        if (!list.isEmpty()) {
            String khId = list.get(0).getKhachHangId();
            List<ChiTietCongNoKhachHangEntry> chiTiet = dao.getChiTietCongNo(khId, null, null);
            assertNotNull(chiTiet);
            System.out.println("Số dòng chi tiết công nợ cho khách [" + list.get(0).getTenKhach() + "]: " + chiTiet.size());
        }
    }

    @Test
    @Order(3)
    public void testGenerateNextSoPhieuThu() {
        String soPhieu = dao.generateNextSoPhieuThu(new Date());
        assertNotNull(soPhieu);
        assertTrue(soPhieu.startsWith("PTCN"), "Phiếu thu công nợ phải có tiền tố PTCN theo cấu hình mặc định");
        System.out.println("Số phiếu thu tạo tiếp theo: " + soPhieu);
    }

    @Test
    @Order(4)
    public void testInsertPhieuThu() {
        List<CongNoKhachHangItem> list = dao.getDanhSachCongNoKhachHang(null, null, false);
        if (!list.isEmpty()) {
            CongNoKhachHangItem kh = list.get(0);
            boolean ok = dao.insertPhieuThu(
                    kh.getKhachHangId(),
                    kh.getTenKhach(),
                    kh.getDiaChi(),
                    50000.0,
                    null,
                    "Thu nợ test JUnit",
                    "Ghi chú test",
                    null,
                    new Date()
            );
            assertTrue(ok, "Thao tác tạo phiếu thu nợ phải thành công");
            System.out.println("Tạo thành công phiếu thu nợ 50,000 đ cho khách: " + kh.getTenKhach());
        }
    }

    @Test
    @Order(5)
    public void testQuanLyCongNoKhachHangPanel() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            QuanLyCongNoKhachHangPanel panel = new QuanLyCongNoKhachHangPanel();
            assertNotNull(panel);
        });
    }
}
