package kazuko.ex.ui.baocao;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportColumnResolverTest {
    @Test
    void readsColumnsFromSelectedReportDefinition() {
        assertEquals(Arrays.asList(
                        "Số phiếu",
                        "Ngày",
                        "Tên đối tượng",
                        "Địa chỉ",
                        "Lý do thu chi",
                        "Diễn giải",
                        "Chứng từ gốc",
                        "Số tiền"),
                ReportColumnResolver.resolve("DANH SÁCH PHIẾU THU THEO NGÀY"));
    }
}
