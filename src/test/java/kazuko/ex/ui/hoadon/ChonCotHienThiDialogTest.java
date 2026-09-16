package kazuko.ex.ui.hoadon;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChonCotHienThiDialogTest {

    private static final List<String> INVOICE_CATALOG_COLUMNS = List.of(
            "Mã hàng", "Tên hàng", "ĐVT", "ĐVT chẵn", "Giá bán", "Bảo hành"
    );

    @Test
    void invoiceCatalogOnlyOffersSixRequestedColumns() {
        assertEquals(INVOICE_CATALOG_COLUMNS,
                ChonCotHienThiDialog.ALL_POSSIBLE_CATALOG_COLUMNS);
        assertEquals(INVOICE_CATALOG_COLUMNS,
                ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS);
    }
}
