package kazuko.ex.model.banhang;

import java.util.Date;

/** Một mức đơn giá gần đây mà khách hàng đã mua mặt hàng. */
public class GiaBanGanDayKhachHang {
    private final double donGia;
    private final Date ngayHoaDon;
    private final String soHoaDon;

    public GiaBanGanDayKhachHang(double donGia, Date ngayHoaDon, String soHoaDon) {
        this.donGia = donGia;
        this.ngayHoaDon = ngayHoaDon;
        this.soHoaDon = soHoaDon;
    }

    public double getDonGia() {
        return donGia;
    }

    public Date getNgayHoaDon() {
        return ngayHoaDon;
    }

    public String getSoHoaDon() {
        return soHoaDon;
    }
}
