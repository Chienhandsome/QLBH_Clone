package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.khohang.TheKhoEntry;
import kazuko.ex.model.khohang.TonKhoItem;
import kazuko.ex.ui.common.ChonCotHienThiDialog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TonKhoFeatureTest {

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();

    @Test
    @Order(1)
    public void testGetTonKhoDanhSachAllWarehouses() {
        List<TonKhoItem> list = donHangDAO.getTonKhoDanhSach(null);
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Danh mục mặt hàng tồn kho không được rỗng");

        TonKhoItem first = list.get(0);
        assertNotNull(first.getMatHangId());
        assertNotNull(first.getMaHang());
        System.out.println("Tổng số mặt hàng lấy được: " + list.size());
        System.out.println("Mặt hàng đầu tiên: " + first.getMaHang() + " - " + first.getTenHang() + ", Tồn: " + first.getTon());
    }

    @Test
    @Order(2)
    public void testGetTonKhoDanhSachSpecificWarehouse() {
        List<KhoHang> khoList = khoHangDAO.getAll();
        if (!khoList.isEmpty()) {
            String khoId = khoList.get(0).getId();
            List<TonKhoItem> list = donHangDAO.getTonKhoDanhSach(khoId);
            assertNotNull(list);
            System.out.println("Số mặt hàng trong kho [" + khoList.get(0).getName() + "]: " + list.size());
        }
    }

    @Test
    @Order(3)
    public void testGetTheKhoRunningBalance() {
        List<TonKhoItem> list = donHangDAO.getTonKhoDanhSach(null);
        assertFalse(list.isEmpty());

        // Tìm một mặt hàng có giao dịch hoặc lấy mặt hàng đầu tiên
        String testMhId = list.get(0).getMatHangId();
        for (TonKhoItem item : list) {
            if (Math.abs(item.getTon()) > 0) {
                testMhId = item.getMatHangId();
                break;
            }
        }

        List<TheKhoEntry> theKho = donHangDAO.getTheKho(testMhId, null, null, null);
        assertNotNull(theKho);
        System.out.println("Số dòng thẻ kho cho mặt hàng " + testMhId + ": " + theKho.size());

        // Verify cumulative running balance logic
        double running = 0.0;
        for (TheKhoEntry e : theKho) {
            running += (e.getSlNhap() - e.getSlXuat());
            assertEquals(running, e.getTonLuyKe(), 0.001, "Tồn lũy kế phải bằng tổng nhập trừ tổng xuất qua từng phiếu");
        }
    }

    @Test
    @Order(4)
    public void testGetTonTheoHanDung() {
        List<TonKhoItem> list = donHangDAO.getTonKhoDanhSach(null);
        assertFalse(list.isEmpty());

        String testMhId = list.get(0).getMatHangId();
        List<Object[]> hanDungList = donHangDAO.getTonTheoHanDung(testMhId, null);
        assertNotNull(hanDungList);
        System.out.println("Số lô hạn dùng: " + hanDungList.size());
    }

    @Test
    @Order(5)
    public void testChonCotHienThiDialogLogic() {
        String[] colNames = {"Cột 1", "Cột 2", "Cột 3", "Cột 4"};
        DefaultTableModel model = new DefaultTableModel(colNames, 0);
        JTable table = new JTable(model);

        assertEquals(4, table.getColumnCount());

        List<ChonCotHienThiDialog.ColumnInfo> colInfos = new ArrayList<>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);
            colInfos.add(new ChonCotHienThiDialog.ColumnInfo(colNames[i], tc, i));
        }

        assertEquals(4, colInfos.size());
    }

    @Test
    @Order(6)
    public void testQuanLyTonKhoPanelInstantiation() throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        SwingUtilities.invokeAndWait(() -> {
            QuanLyTonKhoPanel panel = new QuanLyTonKhoPanel();
            assertNotNull(panel);
        });
    }
}
