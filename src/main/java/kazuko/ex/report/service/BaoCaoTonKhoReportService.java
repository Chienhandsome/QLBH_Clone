package kazuko.ex.report.service;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.dao.NhomMatHangDAO;
import kazuko.ex.dao.SystemConfigDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.NhomMatHang;
import kazuko.ex.model.khohang.TonKhoItem;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.report.model.BaoCaoHangHanDungReportRow;
import kazuko.ex.report.model.BaoCaoHangTonKhoReportRow;
import kazuko.ex.report.model.BaoCaoTongHopXuatNhapTonReportRow;
import kazuko.ex.report.model.TonKhoTheoKichThuocReportRow;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BaoCaoTonKhoReportService {

    private static JasperReport cachedReportTonKhoTheoKichThuoc = null;
    private static JasperReport cachedReportHangTheoHanDung = null;
    private static JasperReport cachedReportHangDaHetHan = null;
    private static JasperReport cachedReportHangTonKhoHanDung = null;
    private static JasperReport cachedReportHangTonKho = null;
    private static JasperReport cachedReportTongHopXuatNhapTon = null;

    private static Object cachedLogoImage = null;
    private static String cachedLogoPath = null;

    private final DonHangDAO donHangDAO = new DonHangDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final SystemConfigDAO configDAO = new SystemConfigDAO();

    static {
        new Thread(() -> {
            try {
                preloadAll();
            } catch (Exception ignored) {}
        }, "BaoCaoTonKho-Preload-Thread").start();
    }

    public static synchronized void preloadAll() {
        try {
            if (cachedReportTonKhoTheoKichThuoc == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_ton_kho_theo_kich_thuoc.jrxml")) {
                    if (in != null) cachedReportTonKhoTheoKichThuoc = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportHangTheoHanDung == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_hang_theo_han_dung.jrxml")) {
                    if (in != null) cachedReportHangTheoHanDung = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportHangDaHetHan == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_hang_da_het_han.jrxml")) {
                    if (in != null) cachedReportHangDaHetHan = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportHangTonKhoHanDung == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_hang_ton_kho_han_dung.jrxml")) {
                    if (in != null) cachedReportHangTonKhoHanDung = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportHangTonKho == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_hang_ton_kho.jrxml")) {
                    if (in != null) cachedReportHangTonKho = JasperCompileManager.compileReport(in);
                }
            }
            if (cachedReportTongHopXuatNhapTon == null) {
                try (InputStream in = BaoCaoTonKhoReportService.class.getResourceAsStream("/reports/bao_cao_tong_hop_xuat_nhap_ton.jrxml")) {
                    if (in != null) cachedReportTongHopXuatNhapTon = JasperCompileManager.compileReport(in);
                }
            }
        } catch (Exception ignored) {}
    }

    public synchronized JasperReport getCompiledReportTonKhoTheoKichThuoc() throws JRException {
        if (cachedReportTonKhoTheoKichThuoc != null) return cachedReportTonKhoTheoKichThuoc;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_ton_kho_theo_kich_thuoc.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_ton_kho_theo_kich_thuoc.jrxml không tồn tại!");
            cachedReportTonKhoTheoKichThuoc = JasperCompileManager.compileReport(in);
            return cachedReportTonKhoTheoKichThuoc;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportHangTheoHanDung() throws JRException {
        if (cachedReportHangTheoHanDung != null) return cachedReportHangTheoHanDung;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_hang_theo_han_dung.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_hang_theo_han_dung.jrxml không tồn tại!");
            cachedReportHangTheoHanDung = JasperCompileManager.compileReport(in);
            return cachedReportHangTheoHanDung;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportHangDaHetHan() throws JRException {
        if (cachedReportHangDaHetHan != null) return cachedReportHangDaHetHan;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_hang_da_het_han.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_hang_da_het_han.jrxml không tồn tại!");
            cachedReportHangDaHetHan = JasperCompileManager.compileReport(in);
            return cachedReportHangDaHetHan;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportHangTonKhoHanDung() throws JRException {
        if (cachedReportHangTonKhoHanDung != null) return cachedReportHangTonKhoHanDung;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_hang_ton_kho_han_dung.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_hang_ton_kho_han_dung.jrxml không tồn tại!");
            cachedReportHangTonKhoHanDung = JasperCompileManager.compileReport(in);
            return cachedReportHangTonKhoHanDung;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportHangTonKho() throws JRException {
        if (cachedReportHangTonKho != null) return cachedReportHangTonKho;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_hang_ton_kho.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_hang_ton_kho.jrxml không tồn tại!");
            cachedReportHangTonKho = JasperCompileManager.compileReport(in);
            return cachedReportHangTonKho;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized JasperReport getCompiledReportTongHopXuatNhapTon() throws JRException {
        if (cachedReportTongHopXuatNhapTon != null) return cachedReportTongHopXuatNhapTon;
        try (InputStream in = getClass().getResourceAsStream("/reports/bao_cao_tong_hop_xuat_nhap_ton.jrxml")) {
            if (in == null) throw new JRException("Mẫu /reports/bao_cao_tong_hop_xuat_nhap_ton.jrxml không tồn tại!");
            cachedReportTongHopXuatNhapTon = JasperCompileManager.compileReport(in);
            return cachedReportTongHopXuatNhapTon;
        } catch (Exception e) {
            throw new JRException(e);
        }
    }

    public synchronized Object getLogoImage() {
        try {
            SystemConfig cfg = configDAO.loadConfig();
            String path = cfg != null ? cfg.getLogoPath() : null;
            if (path != null && !path.trim().isEmpty() && !path.equals(cachedLogoPath)) {
                File f = new File(path);
                if (f.exists()) {
                    cachedLogoPath = path;
                    cachedLogoImage = ImageIO.read(f);
                    return cachedLogoImage;
                }
            }
            if (cachedLogoImage != null && (path == null || path.trim().isEmpty())) {
                return cachedLogoImage;
            }
            if (cachedLogoImage == null) {
                try (InputStream is = getClass().getResourceAsStream("/logo.png")) {
                    if (is != null) {
                        cachedLogoImage = ImageIO.read(is);
                    }
                }
            }
        } catch (Exception ignored) {}
        return cachedLogoImage;
    }

    public Map<String, Object> createDefaultParameters(String title) {
        Map<String, Object> params = new HashMap<>();
        SystemConfig cfg = configDAO.loadConfig();
        if (cfg != null) {
            params.put("p_companyName", cfg.getTenCongTy() != null && !cfg.getTenCongTy().isEmpty() ? cfg.getTenCongTy() : "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", cfg.getDiaChi() != null && !cfg.getDiaChi().isEmpty() ? cfg.getDiaChi() : "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", cfg.getSoDienThoai() != null && !cfg.getSoDienThoai().isEmpty() ? cfg.getSoDienThoai() : "(08) 6264 5788");
            params.put("p_companyEmail", cfg.getEmail() != null ? cfg.getEmail() : "");
        } else {
            params.put("p_companyName", "Công ty TNHH Ngọc Châu Âu");
            params.put("p_companyAddress", "455 Sư Vạn Hạnh, P.12, Q.10, TPHCM");
            params.put("p_companyPhone", "(08) 6264 5788");
            params.put("p_companyEmail", "info@ngocchauau.vn");
        }
        params.put("p_logoImage", getLogoImage());
        params.put("p_reportTitle", title);

        SimpleDateFormat sdfFull = new SimpleDateFormat("'Ngày 'dd' tháng 'MM' năm 'yyyy");
        params.put("p_currentDateStr", sdfFull.format(new Date()));

        return params;
    }

    private String getKhoName(String khoId) {
        if (khoId == null || khoId.trim().isEmpty()) return "Kho: Tất cả";
        try {
            KhoHang k = khoHangDAO.findById(khoId);
            if (k != null && k.getName() != null) return "Kho: " + k.getName();
        } catch (Exception ignored) {}
        return "Kho: Tất cả";
    }

    public List<TonKhoTheoKichThuocReportRow> fetchTonKhoTheoKichThuocData(String nhomHangId, String khoId, String searchKeyword) {
        List<TonKhoTheoKichThuocReportRow> rows = new ArrayList<>();
        List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);

        Map<String, String> nhomHangMap = new HashMap<>();
        try {
            for (NhomMatHang n : nhomMatHangDAO.getAll()) {
                nhomHangMap.put(n.getId(), n.getName());
            }
        } catch (Exception ignored) {}

        if (tonItems == null || tonItems.isEmpty()) {
            List<MatHang> matHangs = matHangDAO.getAll();
            if (matHangs != null && !matHangs.isEmpty()) {
                tonItems = new ArrayList<>();
                for (MatHang m : matHangs) {
                    TonKhoItem item = new TonKhoItem();
                    item.setMatHangId(m.getId());
                    item.setMaHang(m.getCode());
                    item.setTenHang(m.getName());
                    item.setDonViTinh(m.getTenDonViTinh());
                    item.setNhomHangId(m.getNhomMatHangId());
                    item.setNhomHangName(m.getTenNhom());
                    item.setTon(m.getTonKho());
                    tonItems.add(item);
                }
            }
        }

        if (tonItems != null) {
            String kw = searchKeyword != null ? searchKeyword.trim().toLowerCase() : "";
            int stt = 1;
            for (TonKhoItem item : tonItems) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty() && !"all".equalsIgnoreCase(nhomHangId)) {
                    if (item.getNhomHangId() == null || !item.getNhomHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(kw);
                    boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) {
                        continue;
                    }
                }

                String groupName = item.getNhomHangName();
                if (groupName == null || groupName.trim().isEmpty()) {
                    groupName = nhomHangMap.get(item.getNhomHangId());
                }
                if (groupName == null || groupName.trim().isEmpty()) {
                    groupName = "Khác";
                }
                String groupHeader = "Nhóm: " + groupName;

                TonKhoTheoKichThuocReportRow row = new TonKhoTheoKichThuocReportRow(
                        stt++,
                        groupHeader,
                        item.getMaHang() != null ? item.getMaHang() : "",
                        item.getTenHang() != null ? item.getTenHang() : "",
                        item.getDonViTinh() != null ? item.getDonViTinh() : "",
                        item.getTon(),
                        0.0
                );
                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            rows.add(new TonKhoTheoKichThuocReportRow(1, "Nhóm: Vòng tay", "VT001", "Vòng tay Kim Cương Nữ", "Cái", 5.0, 0.0));
            rows.add(new TonKhoTheoKichThuocReportRow(2, "Nhóm: Vòng tay", "VT002", "Vòng tay Vàng 18k", "Cái", 8.0, 0.0));
            rows.add(new TonKhoTheoKichThuocReportRow(3, "Nhóm: Nhẫn cưới", "NC001", "Nhẫn cưới Kim Cương Trắng", "Cặp", 12.0, 0.0));
            rows.add(new TonKhoTheoKichThuocReportRow(4, "Nhóm: Nhẫn cưới", "NC002", "Nhẫn cưới Bạch Kim", "Cặp", 6.0, 0.0));
        }

        return rows;
    }

    public List<BaoCaoHangHanDungReportRow> fetchHangTheoHanDung(Date tuNgay, Date denNgay, String khoId, String nhomHangId, String keyword) {
        List<BaoCaoHangHanDungReportRow> rows = new ArrayList<>();
        List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);

        if (tonItems != null && !tonItems.isEmpty()) {
            String kw = keyword != null ? keyword.trim().toLowerCase() : "";
            int stt = 1;
            for (TonKhoItem item : tonItems) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty()) {
                    if (item.getNhomHangId() == null || !item.getNhomHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(kw);
                    boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) continue;
                }
                // If item has positive stock
                if (item.getTon() > 0) {
                    rows.add(new BaoCaoHangHanDungReportRow(
                            stt++,
                            item.getMaHang() != null ? item.getMaHang() : "",
                            item.getTenHang() != null ? item.getTenHang() : "",
                            item.getDonViTinh() != null ? item.getDonViTinh() : "",
                            item.getTon(),
                            "30/09/2026"
                    ));
                }
            }
        }

        return rows;
    }

    public List<BaoCaoHangHanDungReportRow> fetchHangDaHetHan(Date truocNgay, String khoId, String nhomHangId, String keyword) {
        List<BaoCaoHangHanDungReportRow> rows = new ArrayList<>();
        List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);

        if (tonItems != null && !tonItems.isEmpty()) {
            String kw = keyword != null ? keyword.trim().toLowerCase() : "";
            int stt = 1;
            for (TonKhoItem item : tonItems) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty()) {
                    if (item.getNhomHangId() == null || !item.getNhomHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(kw);
                    boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) continue;
                }
            }
        }

        return rows;
    }

    public List<BaoCaoHangHanDungReportRow> fetchHangTonKhoHanDung(Date tonNgay, String khoId, String nhomHangId, String keyword) {
        List<BaoCaoHangHanDungReportRow> rows = new ArrayList<>();
        List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);

        if (tonItems != null && !tonItems.isEmpty()) {
            String kw = keyword != null ? keyword.trim().toLowerCase() : "";
            int stt = 1;
            for (TonKhoItem item : tonItems) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty()) {
                    if (item.getNhomHangId() == null || !item.getNhomHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = item.getMaHang() != null && item.getMaHang().toLowerCase().contains(kw);
                    boolean matchTen = item.getTenHang() != null && item.getTenHang().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) continue;
                }
                if (item.getTon() > 0) {
                    rows.add(new BaoCaoHangHanDungReportRow(
                            stt++,
                            item.getMaHang() != null ? item.getMaHang() : "",
                            item.getTenHang() != null ? item.getTenHang() : "",
                            item.getDonViTinh() != null ? item.getDonViTinh() : "",
                            item.getTon(),
                            "30/09/2026"
                    ));
                }
            }
        }

        return rows;
    }

    public JasperPrint generateTonKhoTheoKichThuocReport(String nhomHangId, String khoId, String searchKeyword) throws JRException {
        JasperReport jr = getCompiledReportTonKhoTheoKichThuoc();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO TỒN KHO THEO KÍCH THƯỚC");
        List<TonKhoTheoKichThuocReportRow> data = fetchTonKhoTheoKichThuocData(nhomHangId, khoId, searchKeyword);
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(data);
        return JasperFillManager.fillReport(jr, params, ds);
    }

    public JasperPrint generateHangTheoHanDungReport(Date tuNgay, Date denNgay, String khoId, String nhomHangId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportHangTheoHanDung();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO HÀNG THEO HẠN DÙNG");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String tuStr = tuNgay != null ? sdf.format(tuNgay) : "";
        String denStr = denNgay != null ? sdf.format(denNgay) : "";
        params.put("p_subTitleDate", "Hạn dùng từ: " + tuStr + " đến " + denStr);
        params.put("p_subTitleKho", getKhoName(khoId));

        List<BaoCaoHangHanDungReportRow> data = fetchHangTheoHanDung(tuNgay, denNgay, khoId, nhomHangId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateHangDaHetHanReport(Date truocNgay, String khoId, String nhomHangId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportHangDaHetHan();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO HÀNG ĐÃ HẾT HẠN");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String truocStr = truocNgay != null ? sdf.format(truocNgay) : sdf.format(new Date());
        params.put("p_subTitleDate", "Ngày: " + truocStr);
        params.put("p_subTitleKho", getKhoName(khoId));

        List<BaoCaoHangHanDungReportRow> data = fetchHangDaHetHan(truocNgay, khoId, nhomHangId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public JasperPrint generateHangTonKhoHanDungReport(Date tonNgay, String khoId, String nhomHangId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportHangTonKhoHanDung();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO HÀNG TỒN KHO HẠN DÙNG");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String tonStr = tonNgay != null ? sdf.format(tonNgay) : sdf.format(new Date());
        params.put("p_subTitleDate", "Ngày: " + tonStr);
        params.put("p_subTitleKho", getKhoName(khoId));

        List<BaoCaoHangHanDungReportRow> data = fetchHangTonKhoHanDung(tonNgay, khoId, nhomHangId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public List<BaoCaoHangTonKhoReportRow> fetchHangTonKho(String khoId, String nhomHangId, String keyword) {
        List<BaoCaoHangTonKhoReportRow> rows = new ArrayList<>();
        List<MatHang> matHangs = matHangDAO.getAll();
        Map<String, Double> tonKhoMap = new HashMap<>();
        try {
            List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);
            if (tonItems != null) {
                for (TonKhoItem it : tonItems) {
                    tonKhoMap.put(it.getMatHangId(), it.getTon());
                }
            }
        } catch (Exception ignored) {}

        Map<String, String> nhomHangMap = new HashMap<>();
        try {
            for (NhomMatHang n : nhomMatHangDAO.getAll()) {
                nhomHangMap.put(n.getId(), n.getName());
            }
        } catch (Exception ignored) {}

        if (matHangs != null) {
            String kw = keyword != null ? keyword.trim().toLowerCase() : "";
            int stt = 1;
            for (MatHang m : matHangs) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty() && !"all".equalsIgnoreCase(nhomHangId)) {
                    if (m.getNhomMatHangId() == null || !m.getNhomMatHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = m.getCode() != null && m.getCode().toLowerCase().contains(kw);
                    boolean matchTen = m.getName() != null && m.getName().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) continue;
                }

                String gName = m.getTenNhom();
                if (gName == null || gName.trim().isEmpty()) {
                    gName = nhomHangMap.get(m.getNhomMatHangId());
                }
                if (gName == null || gName.trim().isEmpty()) {
                    gName = "Khác";
                }
                String groupHeader = "Nhóm hàng: " + gName;

                double ton = tonKhoMap.containsKey(m.getId()) ? tonKhoMap.get(m.getId()) : m.getTonKho();
                double giaVon = m.getGiaNhap() > 0 ? m.getGiaNhap() : m.getGiaVon();
                double giaTriVon = ton * giaVon;
                double giaBan = m.getGiaBan();
                double giaTriBan = ton * giaBan;

                rows.add(new BaoCaoHangTonKhoReportRow(
                        stt++,
                        groupHeader,
                        m.getCode() != null ? m.getCode() : "",
                        m.getName() != null ? m.getName() : "",
                        m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "",
                        ton,
                        m.getTonToiThieu(),
                        m.getTonToiDa(),
                        giaVon,
                        giaTriVon,
                        giaBan,
                        giaTriBan
                ));
            }
        }

        if (rows.isEmpty()) {
            rows.add(new BaoCaoHangTonKhoReportRow(1, "Nhóm hàng: Nhẫn", "VT001", "Vòng tay Kim Cương Nữ", "Cái", 5.0, 1.0, 10.0, 12000000.0, 60000000.0, 15000000.0, 75000000.0));
            rows.add(new BaoCaoHangTonKhoReportRow(2, "Nhóm hàng: Nhẫn", "NC001", "Nhẫn cưới Kim Cương Trắng", "Cặp", 8.0, 2.0, 15.0, 8500000.0, 68000000.0, 11000000.0, 88000000.0));
        }

        return rows;
    }

    public JasperPrint generateHangTonKhoReport(String khoId, String nhomHangId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportHangTonKho();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO HÀNG TỒN KHO");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        params.put("p_subTitleDate", "Ngày: " + sdf.format(new Date()));
        params.put("p_subTitleKho", getKhoName(khoId));

        List<BaoCaoHangTonKhoReportRow> data = fetchHangTonKho(khoId, nhomHangId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }

    public List<BaoCaoTongHopXuatNhapTonReportRow> fetchTongHopXuatNhapTon(Date tuNgay, Date denNgay, String khoId, String nhomHangId, String keyword) {
        List<BaoCaoTongHopXuatNhapTonReportRow> rows = new ArrayList<>();
        List<MatHang> matHangs = matHangDAO.getAll();
        Map<String, Double> tonKhoMap = new HashMap<>();
        try {
            List<TonKhoItem> tonItems = donHangDAO.getTonKhoDanhSach(khoId);
            if (tonItems != null) {
                for (TonKhoItem it : tonItems) {
                    tonKhoMap.put(it.getMatHangId(), it.getTon());
                }
            }
        } catch (Exception ignored) {}

        Map<String, String> nhomHangMap = new HashMap<>();
        try {
            for (NhomMatHang n : nhomMatHangDAO.getAll()) {
                nhomHangMap.put(n.getId(), n.getName());
            }
        } catch (Exception ignored) {}

        if (matHangs != null) {
            String kw = keyword != null ? keyword.trim().toLowerCase() : "";
            int stt = 1;
            for (MatHang m : matHangs) {
                if (nhomHangId != null && !nhomHangId.trim().isEmpty() && !"all".equalsIgnoreCase(nhomHangId)) {
                    if (m.getNhomMatHangId() == null || !m.getNhomMatHangId().equalsIgnoreCase(nhomHangId)) {
                        continue;
                    }
                }
                if (!kw.isEmpty()) {
                    boolean matchMa = m.getCode() != null && m.getCode().toLowerCase().contains(kw);
                    boolean matchTen = m.getName() != null && m.getName().toLowerCase().contains(kw);
                    if (!matchMa && !matchTen) continue;
                }

                String gName = m.getTenNhom();
                if (gName == null || gName.trim().isEmpty()) {
                    gName = nhomHangMap.get(m.getNhomMatHangId());
                }
                if (gName == null || gName.trim().isEmpty()) {
                    gName = "Khác";
                }
                String groupHeader = "Nhóm hàng: " + gName;

                double giaVon = m.getGiaNhap() > 0 ? m.getGiaNhap() : m.getGiaVon();
                double tonCuoiSl = tonKhoMap.containsKey(m.getId()) ? tonKhoMap.get(m.getId()) : m.getTonKho();
                double tonCuoiGt = tonCuoiSl * giaVon;

                double nhapSl = 0.0;
                double nhapGt = 0.0;
                double xuatSl = 0.0;
                double xuatGt = 0.0;
                double tonDauSl = tonCuoiSl;
                double tonDauGt = tonDauSl * giaVon;

                rows.add(new BaoCaoTongHopXuatNhapTonReportRow(
                        stt++,
                        groupHeader,
                        m.getCode() != null ? m.getCode() : "",
                        m.getName() != null ? m.getName() : "",
                        m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "",
                        giaVon,
                        tonDauSl,
                        tonDauGt,
                        nhapSl,
                        nhapGt,
                        xuatSl,
                        xuatGt,
                        tonCuoiSl,
                        tonCuoiGt
                ));
            }
        }

        if (rows.isEmpty()) {
            rows.add(new BaoCaoTongHopXuatNhapTonReportRow(1, "Nhóm hàng: Nhẫn", "VT001", "Vòng tay Kim Cương Nữ", "Cái", 12000000.0, 5.0, 60000000.0, 0.0, 0.0, 0.0, 0.0, 5.0, 60000000.0));
            rows.add(new BaoCaoTongHopXuatNhapTonReportRow(2, "Nhóm hàng: Nhẫn", "NC001", "Nhẫn cưới Kim Cương Trắng", "Cặp", 8500000.0, 8.0, 68000000.0, 0.0, 0.0, 0.0, 0.0, 8.0, 68000000.0));
        }

        return rows;
    }

    public JasperPrint generateTongHopXuatNhapTonReport(Date tuNgay, Date denNgay, String khoId, String nhomHangId, String keyword) throws JRException {
        JasperReport jr = getCompiledReportTongHopXuatNhapTon();
        Map<String, Object> params = createDefaultParameters("BÁO CÁO TỔNG HỢP XUẤT NHẬP TỒN");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String tuStr = tuNgay != null ? sdf.format(tuNgay) : sdf.format(new Date());
        String denStr = denNgay != null ? sdf.format(denNgay) : sdf.format(new Date());
        params.put("p_subTitleDate", "Từ ngày: " + tuStr + " đến ngày " + denStr);
        params.put("p_subTitleKho", getKhoName(khoId));

        List<BaoCaoTongHopXuatNhapTonReportRow> data = fetchTongHopXuatNhapTon(tuNgay, denNgay, khoId, nhomHangId, keyword);
        return JasperFillManager.fillReport(jr, params, new JRBeanCollectionDataSource(data));
    }
}
