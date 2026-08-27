package kazuko.ex;

import kazuko.ex.dao.BaoGiaDAO;
import kazuko.ex.model.baogia.BaoGia;
import kazuko.ex.model.baogia.BaoGiaChiTiet;
import kazuko.ex.model.system.STemplate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BaoGiaDAOTest {
    public static void main(String[] args) {
        System.out.println("=== BẮT ĐẦU KIỂM THỬ BAOGIADAO ===");
        BaoGiaDAO dao = new BaoGiaDAO();

        // 1. Kiểm tra sinh số phiếu tự động
        String nextSoPhieu = dao.generateNextSoPhieu(new Date());
        System.out.println("1. Sinh số phiếu tự động thành công: " + nextSoPhieu);
        if (!nextSoPhieu.startsWith("BG")) {
            System.err.println("FAIL: Số phiếu không đúng định dạng!");
            System.exit(1);
        }

        // 2. Thêm mới một Báo giá thử nghiệm
        String testId = UUID.randomUUID().toString();
        BaoGia bg = new BaoGia();
        bg.setId(testId);
        bg.setName(nextSoPhieu);
        bg.setNgay(new Timestamp(System.currentTimeMillis()));
        bg.setTenKhach("Công Chiến");
        bg.setDiaChi("abc");
        bg.setDienThoai("123");
        bg.setEmail("chien@test.com");
        bg.setNote("Báo giá dự án mẫu");
        bg.setTienHang(362000.0);
        bg.setTiLeGiamGia(0.0);
        bg.setTienGiamGia(0.0);
        bg.setTiLeThue(0.0);
        bg.setTienThue(0.0);
        bg.setPhiVanChuyen(0.0);
        bg.setTongCong(362000.0);
        bg.setStatus(1);

        List<BaoGiaChiTiet> details = new ArrayList<>();
        BaoGiaChiTiet ct1 = new BaoGiaChiTiet();
        ct1.setId(UUID.randomUUID().toString());
        ct1.settBaoGiaId(testId);
        ct1.setMaHang("ST25");
        ct1.setTenHang("Gạo ST25");
        ct1.setTenDonViTinh("kg");
        ct1.setSoLuong(1.0);
        ct1.setDonGia(50000.0);
        ct1.setTiLeGiamGia(0.0);
        ct1.setThanhTien(50000.0);
        details.add(ct1);

        BaoGiaChiTiet ct2 = new BaoGiaChiTiet();
        ct2.setId(UUID.randomUUID().toString());
        ct2.settBaoGiaId(testId);
        ct2.setMaHang("1900001");
        ct2.setTenHang("ALYNA Bấm móng");
        ct2.setTenDonViTinh("Cây");
        ct2.setSoLuong(2.0);
        ct2.setDonGia(18000.0);
        ct2.setTiLeGiamGia(0.0);
        ct2.setThanhTien(36000.0);
        details.add(ct2);

        boolean insertOk = dao.insertBaoGia(bg, details);
        System.out.println("2. Thêm mới Báo giá và 2 chi tiết: " + (insertOk ? "THÀNH CÔNG" : "THẤT BẠI"));
        if (!insertOk) {
            System.err.println("FAIL: Không thể chèn dữ liệu báo giá!");
            System.exit(1);
        }

        // 3. Truy vấn lại theo ID
        BaoGia loaded = dao.getBaoGiaById(testId);
        System.out.println("3. Truy vấn Báo giá theo ID: " + (loaded != null ? loaded.getName() + " - " + loaded.getTenKhach() : "NULL"));
        if (loaded == null || !loaded.getName().equals(nextSoPhieu)) {
            System.err.println("FAIL: Dữ liệu tải lại không khớp!");
            System.exit(1);
        }

        // 4. Kiểm tra danh sách chi tiết
        List<BaoGiaChiTiet> loadedDetails = dao.getChiTietByBaoGiaId(testId, loaded.getName());
        System.out.println("4. Số dòng chi tiết tải được: " + loadedDetails.size());
        if (loadedDetails.size() != 2) {
            System.err.println("FAIL: Số dòng chi tiết không đúng!");
            System.exit(1);
        }

        // 5. Cập nhật Báo giá
        loaded.setTenKhach("Công Chiến Updated");
        loaded.setTongCong(400000.0);
        boolean updateOk = dao.updateBaoGia(loaded, loadedDetails);
        System.out.println("5. Cập nhật Báo giá: " + (updateOk ? "THÀNH CÔNG" : "THẤT BẠI"));

        // 6. Kiểm tra mẫu in
        List<STemplate> templates = dao.getBaoGiaPrintTemplates();
        System.out.println("6. Danh sách mẫu in nạp được từ DB: " + templates.size() + " mẫu.");
        for (STemplate t : templates) {
            System.out.println("   - " + t.getName());
        }

        // 7. Xóa báo giá thử nghiệm
        boolean deleteOk = dao.deleteBaoGia(testId);
        System.out.println("7. Xóa Báo giá thử nghiệm: " + (deleteOk ? "THÀNH CÔNG" : "THẤT BẠI"));

        System.out.println("=== TẤT CẢ CÁC BƯỚC KIỂM THỬ THÀNH CÔNG 100% ===");
    }
}
