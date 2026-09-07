package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoNhaCungCapDAO;
import kazuko.ex.model.congno.ChiTietCongNoNhaCungCapEntry;
import kazuko.ex.model.congno.CongNoNhaCungCapItem;
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
public class CongNoNhaCungCapFeatureTest {

    private final CongNoNhaCungCapDAO dao = new CongNoNhaCungCapDAO();

    @Test
    @Order(1)
    public void testGetDanhSachCongNoNCC() {
        List<CongNoNhaCungCapItem> list = dao.getDanhSachCongNoNCC(null, null, false);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Danh sách nhà cung cấp không được rỗng");

        System.out.println("Tổng số NCC lấy được: " + list.size());
        CongNoNhaCungCapItem first = list.get(0);
        assertNotNull(first.getNhaCungCapId());
        assertNotNull(first.getTenNCC());
        System.out.println("NCC 1: " + first.getTenNCC() + " (Mã: " + first.getMaNCC() + "), Còn nợ: " + first.getConNo());
    }

    @Test
    @Order(2)
    public void testGetChiTietCongNoNCC() {
        List<CongNoNhaCungCapItem> list = dao.getDanhSachCongNoNCC(null, null, false);
        if (!list.isEmpty()) {
            String nccId = list.get(0).getNhaCungCapId();
            List<ChiTietCongNoNhaCungCapEntry> chiTiet = dao.getChiTietCongNoNCC(nccId, null, null);
            assertNotNull(chiTiet);
            System.out.println("Số dòng chi tiết công nợ cho NCC [" + list.get(0).getTenNCC() + "]: " + chiTiet.size());
        }
    }

    @Test
    @Order(3)
    public void testGenerateNextSoPhieuChi() {
        String soPhieu = dao.generateNextSoPhieuChi(new Date());
        assertNotNull(soPhieu);
        assertTrue(soPhieu.startsWith("PC"), "Số phiếu chi phải có tiền tố PC");
        System.out.println("Số phiếu chi tạo tiếp theo: " + soPhieu);
    }

    @Test
    @Order(4)
    public void testInsertPhieuChi() {
        List<CongNoNhaCungCapItem> list = dao.getDanhSachCongNoNCC(null, null, false);
        if (!list.isEmpty()) {
            CongNoNhaCungCapItem ncc = list.get(0);
            boolean ok = dao.insertPhieuChi(
                    ncc.getNhaCungCapId(),
                    ncc.getTenNCC(),
                    ncc.getDiaChi(),
                    100000.0,
                    null,
                    "Chi trả nợ NCC test JUnit",
                    "Ghi chú test",
                    null,
                    new Date()
            );
            assertTrue(ok, "Thao tác tạo phiếu chi trả nợ phải thành công");
            System.out.println("Tạo thành công phiếu chi trả nợ 100,000 đ cho NCC: " + ncc.getTenNCC());
        }
    }

    @Test
    @Order(5)
    public void testQuanLyCongNoNhaCungCapPanel() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            QuanLyCongNoNhaCungCapPanel panel = new QuanLyCongNoNhaCungCapPanel();
            assertNotNull(panel);
        });
    }
}
