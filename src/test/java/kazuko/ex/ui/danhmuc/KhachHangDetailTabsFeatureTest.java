package kazuko.ex.ui.danhmuc;

import org.junit.jupiter.api.Test;

import javax.swing.JTable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KhachHangDetailTabsFeatureTest {

    @Test
    void createsAllBusinessTablesWithReferenceDefaults() throws Exception {
        DanhMucKhachHangPanel panel = new DanhMucKhachHangPanel();

        for (String fieldName : List.of(
                "tableBaoGia", "tableDatHang", "tableDonHang", "tableSanXuat",
                "tableTangGiamDiem", "tablePhieuThu", "tablePhieuChi", "tablePhieuThuCongNo")) {
            assertNotNull(readTable(panel, fieldName), fieldName + " phải được khởi tạo");
        }

        assertEquals(
                List.of("", "Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại",
                        "Tiền hàng", "Tiền giảm giá", "Tổng cộng", "Tỉ lệ thuế", "Tiền thuế",
                        "Phí vận chuyển", "Email", "Ghi chú", "Khách hàng", "Tỉ lệ giảm giá"),
                columnNames(readTable(panel, "tableBaoGia")));

        assertEquals(
                List.of("", "Ngày", "Số phiếu", "Tên khách", "Địa chỉ", "Điện thoại",
                        "Email", "Tổng cộng", "Tỉ lệ thuế", "Tiền thuế", "Phí vận chuyển"),
                columnNames(readTable(panel, "tableDatHang")));
    }

    private JTable readTable(DanhMucKhachHangPanel panel, String fieldName) throws Exception {
        Field field = DanhMucKhachHangPanel.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (JTable) field.get(panel);
    }

    private List<String> columnNames(JTable table) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            result.add(table.getColumnName(i));
        }
        return result;
    }
}
