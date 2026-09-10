package kazuko.ex.ui.hoadon;

import kazuko.ex.model.banhang.GiaBanGanDayKhachHang;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChonLoaiGiaDialogTest {

    @Test
    void locToiDaBaGiaLichSuKhongTrungNhau() {
        List<GiaBanGanDayKhachHang> history = Arrays.asList(
                price(100_000.02, "HD07"),
                price(150_000, "HD06"),
                price(150_000, "HD05"),
                price(175_000, "HD04"),
                price(200_000, "HD03"),
                price(190_000, "HD02"),
                price(210_000, "HD01")
        );

        List<GiaBanGanDayKhachHang> result = ChonLoaiGiaDialog.filterRecentPrices(history);

        assertEquals(3, result.size());
        assertEquals(100_000.02d, result.get(0).getDonGia());
        assertEquals(150_000d, result.get(1).getDonGia());
        assertEquals(175_000d, result.get(2).getDonGia());
    }

    private GiaBanGanDayKhachHang price(double value, String invoice) {
        return new GiaBanGanDayKhachHang(value, new Date(), invoice);
    }
}
