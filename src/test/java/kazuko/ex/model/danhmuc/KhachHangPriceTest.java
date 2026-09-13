package kazuko.ex.model.danhmuc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KhachHangPriceTest {

    @Test
    void mapsStableDatabaseCodesToCustomerPriceTypes() {
        KhachHang customer = new KhachHang();

        assertMapping(customer, KhachHang.GIA_THEO_DANH_MUC, "Giá theo danh mục");
        assertMapping(customer, KhachHang.GIA_BAN_2, "Giá bán 2");
        assertMapping(customer, KhachHang.GIA_BAN_3, "Giá bán 3");
        assertMapping(customer, KhachHang.GIA_BAN_4, "Giá bán 4");
        assertMapping(customer, KhachHang.GIA_VON, "Giá vốn");
        assertMapping(customer, KhachHang.GIA_GAN_NHAT, "Giá gần nhất");
    }

    private void assertMapping(KhachHang customer, int code, String label) {
        customer.setGiaBanIndex(code);
        assertEquals(code, customer.getGiaBanIndex());
        assertEquals(label, customer.getGiaBanApDung());
    }
}
