package kazuko.ex.report.bill;

import kazuko.ex.dao.BillTemplateDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.bill.BillTemplate;

import java.util.Date;
import java.util.List;

/** Launcher thủ công cho browser smoke test; không được Surefire tự chạy. */
public final class JrxmlEditorPocMain {
    private JrxmlEditorPocMain() {}

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "render".equalsIgnoreCase(args[0])) {
            DonHang order = new DonHang();
            order.setName("POC/0001");
            order.setNgay(new Date());
            order.setTenKhach("Khách thử nghiệm");
            order.setTienHang(20_000);
            order.setTongCong(20_000);
            order.setTienThanhToan(20_000);
            DonHangChiTiet item = new DonHangChiTiet();
            item.setTenHang("Sản phẩm thử nghiệm");
            item.setSoLuong(2);
            item.setDonGia(10_000);
            order.setChiTietLayHang(List.of(item));
            var print = new BillPrintService().createPrint(order, "Mẫu in bill 80mm");
            System.out.println("JRXML_DB_RENDER_PAGES=" + print.getPages().size());
            return;
        }
        BillTemplate template = new BillTemplateDAO().newForLegacyTemplate(
                "190263eb-bc79-4ad2-9cee-ecd5d0470426", "Mẫu in bill 80mm");
        System.out.println(JrxmlEditorServer.start(template, false));
        Thread.currentThread().join();
    }
}
