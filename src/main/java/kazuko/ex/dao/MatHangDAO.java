package kazuko.ex.dao;

import kazuko.ex.model.danhmuc.MatHang;
import kazuko.ex.model.danhmuc.TieuChiPhanLoai;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.service.AuthService;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MatHangDAO extends BaseDAO {
    private static final List<MatHang> MEMORY_PRODUCTS = new CopyOnWriteArrayList<>();

    static {
        // Dữ liệu mẫu phong phú chuẩn 100% theo ảnh giao diện của người dùng
        Object[][] sampleData = {
                {"am00002", "2900001", "ấm sắc thuốc", "bao", 0.0, 0.0, false, false, "Việt Nhật", "Công ty ABC", "Bao"},
                {"3k0", "3K0", "Sopoka", "Chiếc", 20900.0, 30000.0, false, false, "Sopoka", "Đại lý phân phối", "Hộp"},
                {"4k0", "4K0", "Sopoka", "Chiếc", 33000.0, 40000.0, false, false, "Sopoka", "Đại lý phân phối", "Hộp"},
                {"4k9", "4K9", "Sopoka", "Chiếc", 108900.0, 87120.0, false, false, "Sopoka", "Đại lý phân phối", "Thùng"},
                {"1900001", "ALYNA Bấm móng lớn", "Kiềm", "Cây", 18000.0, 0.0, false, false, "Kiềm", "Nhà cung cấp chính", "Hộp"},
                {"1900002", "ALYNA Bấm móng nhỏ", "Kiềm", "Cây", 10000.0, 0.0, false, false, "Kiềm", "Nhà cung cấp chính", "Hộp"},
                {"1300002", "ALYNA BB Cream 2 màu kem pu...", "Công ty", "Hộp", 120000.0, 0.0, false, false, "Công ty", "Công ty ABC", "Thùng"},
                {"1300003", "ALYNA Đắp mặt nạ đủ loại", "Công ty", "Bịch", 3000.0, 0.0, false, false, "Công ty", "Công ty ABC", "Hộp"},
                {"1300004", "ALYNA Gòn tẩy trang 100 miếng", "Công ty", "Bịch", 20000.0, 0.0, false, false, "Công ty", "Công ty ABC", "Hộp"},
                {"1300005", "ALYNA Hair Coat 80ml", "Công ty", "chai", 35000.0, 0.0, false, false, "Công ty", "Công ty ABC", "Thùng"},
                {"2600001", "ALYNA Kéo tỉa mày", "Nhíp", "Cây", 18000.0, 0.0, false, false, "Nhíp", "Đại lý phân phối", "Hộp"},
                {"2600002", "ALYNA Nhíp", "Nhíp", "Cây", 10000.0, 0.0, false, false, "Nhíp", "Đại lý phân phối", "Hộp"},
                {"528", "Ấm đun 528", "Dawo", "Chiếc", 385000.0, 308000.0, false, false, "Dawo", "Nhà cung cấp chính", "Thùng"},
                {"538", "Ấm đun 538", "Dawo", "Chiếc", 429000.0, 343200.0, false, false, "Dawo", "Nhà cung cấp chính", "Thùng"},
                {"539", "Ấm đun 539", "Dawo", "Chiếc", 579700.0, 463760.0, false, false, "Dawo", "Nhà cung cấp chính", "Thùng"},
                {"stv", "Ấm Sắc Thuốc", "Livingcook", "Chiếc", 214500.0, 171600.0, false, false, "Livingcook", "Đại lý phân phối", "Thùng"},
                {"TTBT", "Ấm sắc thuốc Bát Tiên", "Trường Thọ", "Chiếc", 192500.0, 154000.0, false, false, "Bát Tiên", "Công ty ABC", "Thùng"},
                {"lorente", "Ấm sắc thuốc Lorente", "ấm sắc thuốc", "Chiếc", 198000.0, 158400.0, false, false, "Lorente", "Công ty ABC", "Thùng"},
                {"amstlg", "Ấm sắc thuốc lục giác", "ấm sắc thuốc", "Chiếc", 308000.0, 246400.0, false, false, "ấm sắc thuốc", "Công ty ABC", "Thùng"},
                {"stt", "Ấm Sắc Thuốc Tròn", "Livingcook", "Chiếc", 275000.0, 220000.0, false, false, "Livingcook", "Đại lý phân phối", "Thùng"},
                {"TT86", "Ấm sắc thuốc Trường Thọ", "Trường Thọ", "Chiếc", 203500.0, 162800.0, false, false, "Trường Thọ", "Đại lý phân phối", "Thùng"},
                {"TTBA2086", "Ấm sắc thuốc Trường Thọ BA2086", "Trường Thọ", "Chiếc", 203500.0, 162800.0, false, false, "Trường Thọ", "Đại lý phân phối", "Thùng"},
                {"02B", "Bếp ga 02B", "Bếp Ga", "Chiếc", 638000.02, 170000.0, false, false, "Bếp Ga", "Công ty ABC", "Thùng"},
                {"02E", "Bếp ga 02E", "Bếp Ga", "Chiếc", 649000.0, 519200.0, false, false, "Bếp Ga", "Công ty ABC", "Thùng"},
                {"05IC-A", "Bếp từ Sato 05IC", "Việt Nhật", "Chiếc", 1083500.0, 866800.0, true, false, "Việt Nhật", "Nhà cung cấp chính", "Thùng"},
                {"1", "Nồi 01 ANYCOOK", "ANY COOK", "Chiếc", 489500.0, 391600.0, false, false, "ANY COOK", "Công ty ABC", "Thùng"},
                {"1000001", "Bài NI LONG", "Bài", "Hộp", 45000.0, 0.0, false, false, "Bài", "Đại lý phân phối", "Lốc"},
                {"1000081", "Lẩu cá", "Khaluck", "Chiếc", 682000.0, 545600.0, false, false, "Khaluck", "Công ty ABC", "Thùng"},
                {"1011", "Bếp từ SANAKY 1011BT", "Sanaky", "Chiếc", 970200.0, 776160.0, false, false, "Sanaky", "Đại lý phân phối", "Thùng"},
                {"1012", "Bếp từ SANAKY 1012", "Sanaky", "Chiếc", 970200.0, 776160.0, false, false, "Sanaky", "Đại lý phân phối", "Thùng"},
                {"1014", "Bếp từ sanaky AT1014", "Sanaky", "Chiếc", 946000.0, 756800.0, false, false, "Sanaky", "Đại lý phân phối", "Thùng"},
                {"1015", "Bếp từ sanaky AT1015", "Sanaky", "Chiếc", 946000.0, 756800.0, false, false, "Sanaky", "Đại lý phân phối", "Thùng"},
                {"1016", "Bếp từ sanaky AT1016", "Sanaky", "Chiếc", 572000.0, 457600.0, false, false, "Sanaky", "Đại lý phân phối", "Thùng"},
                {"108", "Siêu tốc KL 108", "Khaluck", "Chiếc", 280500.0, 224400.0, false, false, "Khaluck", "Công ty ABC", "Thùng"},
                {"1080", "Sinh tố Hàn Quốc 1080", "Nissei + Dura", "Chiếc", 770000.0, 616000.0, false, false, "Nissei + Dura", "Công ty ABC", "Thùng"},
                {"108cl", "Lọc feroli 108 cường lực", "Feroli", "Chiếc", 3520000.0, 2816000.0, false, false, "Feroli", "Công ty ABC", "Thùng"},
                {"109CL", "Lọc Feroli 109 Kính cường lực", "Feroli", "Chiếc", 3630000.0, 2904000.0, false, false, "Feroli", "Công ty ABC", "Thùng"},
                {"10A", "Quạt Sưởi longer 10A", "Nissei + Dura", "Chiếc", 39600.0, 31680.0, false, false, "Nissei + Dura", "Công ty ABC", "Thùng"},
                {"10KVA-50", "Máy ổn áp STANDA 10KVA-50", "Standa", "Chiếc", 4565000.0, 3652000.0, false, false, "Standa", "Đại lý phân phối", "Thùng"},
                {"10KVA-90", "Máy ổn áp STANDA 10KVA-90", "Standa", "Chiếc", 4235000.0, 3388000.0, false, false, "Standa", "Đại lý phân phối", "Thùng"},
                {"10L", "Nồi shap 10L", "Chi", "Chiếc", 2530000.0, 2024000.0, false, false, "Chi", "Công ty ABC", "Thùng"},
                {"11", "Dây 1*1.0 ht", "Hoatung", "m", 3630.0, 2904.0, false, false, "Hoatung", "Đại lý phân phối", "Cuộn"},
                {"1100001", "Bàn chải mủ hình chân", "Bàn chải đánh răng", "Cái", 7000.0, 0.0, false, false, "Bàn chải đánh răng", "Công ty ABC", "Hộp"},
                {"1100002", "Bàn chải mủ rẻ", "Bàn chải đánh răng", "Cái", 4000.0, 0.0, false, false, "Bàn chải đánh răng", "Công ty ABC", "Hộp"}
        };

        int idx = 1;
        for (Object[] row : sampleData) {
            MatHang mh = new MatHang();
            mh.setId("MH_" + (idx++));
            mh.setCode((String) row[0]);
            mh.setName((String) row[1]);
            mh.setTenNhom((String) row[2]);
            mh.setTenDonViTinh((String) row[3]);
            mh.setGiaBan((Double) row[4]);
            mh.setGiaNhap((Double) row[5]);
            mh.setGiaVon((Double) row[5]);
            mh.setKhongInCheBien((Boolean) row[6]);
            mh.setBanNhanh((Boolean) row[7]);
            if (row.length > 8) mh.setHangSanXuat((String) row[8]);
            if (row.length > 9) mh.setDoiTacKyGui((String) row[9]);
            if (row.length > 10) mh.setTenDonViTinhChan((String) row[10]);
            mh.setTimeCreated(new Timestamp(System.currentTimeMillis() - 86400000L * 300));
            mh.setUserCreatedName("Administrator");
            mh.setTimeModified(new Timestamp(System.currentTimeMillis() - 86400000L * 50));
            mh.setUserModifiedName("Administrator");
            MEMORY_PRODUCTS.add(mh);
        }
    }

    public List<MatHang> getAll() {
        List<MatHang> dbList = tryLoadFromDb(null, null);
        if (dbList != null) {
            return dbList;
        }
        return new ArrayList<>(MEMORY_PRODUCTS);
    }

    public List<MatHang> getByNhom(String tenNhom, String nhomId) {
        if (tenNhom == null || "Tất cả".equalsIgnoreCase(tenNhom.trim())) {
            return getAll();
        }
        List<MatHang> dbList = tryLoadFromDb(tenNhom, nhomId);
        if (dbList != null) {
            return dbList;
        }
        List<MatHang> filtered = new ArrayList<>();
        for (MatHang mh : MEMORY_PRODUCTS) {
            if ("Chưa thiết lập".equalsIgnoreCase(tenNhom)) {
                if ((mh.getTenNhom() == null || mh.getTenNhom().trim().isEmpty()) &&
                    (mh.getNhomMatHangId() == null || mh.getNhomMatHangId().trim().isEmpty())) {
                    filtered.add(mh);
                }
            } else {
                boolean matchId = nhomId != null && nhomId.equals(mh.getNhomMatHangId());
                boolean matchName = tenNhom.equalsIgnoreCase(mh.getTenNhom());
                if (matchId || matchName) {
                    filtered.add(mh);
                }
            }
        }
        return filtered;
    }

    public List<MatHang> getByNhomIds(Set<String> nhomIds, Set<String> nhomNames) {
        if (nhomIds == null || nhomIds.isEmpty()) {
            return getAll();
        }
        List<MatHang> all = getAll();
        List<MatHang> filtered = new ArrayList<>();
        for (MatHang mh : all) {
            boolean matchId = mh.getNhomMatHangId() != null && nhomIds.contains(mh.getNhomMatHangId());
            boolean matchName = mh.getTenNhom() != null && nhomNames != null && nhomNames.contains(mh.getTenNhom());
            if (matchId || matchName) {
                filtered.add(mh);
            }
        }
        return filtered;
    }

    public void updateNhomName(String nhomId, String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) return;
        for (MatHang mh : MEMORY_PRODUCTS) {
            boolean matchId = nhomId != null && nhomId.equals(mh.getNhomMatHangId());
            boolean matchOldName = oldName != null && oldName.equalsIgnoreCase(mh.getTenNhom());
            if (matchId || matchOldName) {
                mh.setTenNhom(newName);
                if (nhomId != null) {
                    mh.setNhomMatHangId(nhomId);
                }
            }
        }
    }

    public List<MatHang> search(String keyword, String tenNhom) {
        return search(keyword, tenNhom, null);
    }

    public List<MatHang> search(String keyword, String tenNhom, String nhomId) {
        SystemConfig config = new SystemConfigDAO().loadConfig();
        return searchWithConfig(keyword, tenNhom, nhomId, config);
    }

    public List<MatHang> searchWithConfig(String keyword, String tenNhom, String nhomId, SystemConfig config) {
        List<MatHang> baseList = getByNhom(tenNhom, nhomId);
        return filterListWithConfig(baseList, keyword, config);
    }

    public List<MatHang> searchWithConfigByGroupSet(String keyword, Set<String> ids, Set<String> names, SystemConfig config) {
        List<MatHang> baseList = getByNhomIds(ids, names);
        return filterListWithConfig(baseList, keyword, config);
    }

    public List<MatHang> getByCriteria(TieuChiPhanLoai tieuChi, String selectedName, String selectedId, Set<String> groupIds, Set<String> groupNames) {
        if (tieuChi == null || tieuChi == TieuChiPhanLoai.NHOM_MAT_HANG) {
            if (groupIds != null && !groupIds.isEmpty()) {
                return getByNhomIds(groupIds, groupNames);
            }
            return getByNhom(selectedName, selectedId);
        }

        List<MatHang> all = getAll();
        if (selectedName == null || "Tất cả".equalsIgnoreCase(selectedName.trim())) {
            return all;
        }

        List<MatHang> filtered = new ArrayList<>();
        boolean isUnassigned = "Chưa thiết lập".equalsIgnoreCase(selectedName.trim());

        for (MatHang mh : all) {
            switch (tieuChi) {
                case DON_VI_TINH:
                    if (isUnassigned) {
                        boolean emptyId = mh.getDonViTinhId() == null || mh.getDonViTinhId().trim().isEmpty();
                        boolean emptyName = mh.getTenDonViTinh() == null || mh.getTenDonViTinh().trim().isEmpty();
                        if (emptyId && emptyName) filtered.add(mh);
                    } else {
                        boolean matchId = selectedId != null && selectedId.equalsIgnoreCase(mh.getDonViTinhId());
                        boolean matchName = mh.getTenDonViTinh() != null && selectedName.equalsIgnoreCase(mh.getTenDonViTinh().trim());
                        if (matchId || matchName) filtered.add(mh);
                    }
                    break;

                case HANG_SAN_XUAT:
                    if (isUnassigned) {
                        if (mh.getHangSanXuat() == null || mh.getHangSanXuat().trim().isEmpty()) {
                            filtered.add(mh);
                        }
                    } else {
                        if (mh.getHangSanXuat() != null && selectedName.equalsIgnoreCase(mh.getHangSanXuat().trim())) {
                            filtered.add(mh);
                        }
                    }
                    break;

                case DON_VI_TINH_CHAN:
                    if (isUnassigned) {
                        boolean emptyId = mh.getDonViTinhChanId() == null || mh.getDonViTinhChanId().trim().isEmpty();
                        boolean emptyName = mh.getTenDonViTinhChan() == null || mh.getTenDonViTinhChan().trim().isEmpty();
                        if (emptyId && emptyName) filtered.add(mh);
                    } else {
                        boolean matchId = selectedId != null && selectedId.equalsIgnoreCase(mh.getDonViTinhChanId());
                        boolean matchName = mh.getTenDonViTinhChan() != null && selectedName.equalsIgnoreCase(mh.getTenDonViTinhChan().trim());
                        if (matchId || matchName) filtered.add(mh);
                    }
                    break;

                case DOI_TAC_KY_GUI:
                    if (isUnassigned) {
                        if (mh.getDoiTacKyGui() == null || mh.getDoiTacKyGui().trim().isEmpty()) {
                            filtered.add(mh);
                        }
                    } else {
                        if (mh.getDoiTacKyGui() != null && selectedName.equalsIgnoreCase(mh.getDoiTacKyGui().trim())) {
                            filtered.add(mh);
                        }
                    }
                    break;

                default:
                    filtered.add(mh);
                    break;
            }
        }
        return filtered;
    }

    public List<MatHang> searchWithConfigByCriteria(String keyword, TieuChiPhanLoai tieuChi, String selectedName, String selectedId, Set<String> groupIds, Set<String> groupNames, SystemConfig config) {
        List<MatHang> baseList = getByCriteria(tieuChi, selectedName, selectedId, groupIds, groupNames);
        return filterListWithConfig(baseList, keyword, config);
    }

    public List<String> getDistinctHangSanXuat() {
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        String[] preset = {"ANY COOK", "Bát Tiên", "Benza", "Bếp Ga", "Besuto", "Chi", "Công ty", "Daelux", "Dawo", "Feroli", "Hoatung", "Khaluck", "Kiềm", "Livingcook", "Lorente", "Nhíp", "Nissei + Dura", "Panasonic", "Samsung", "Sanaky", "Sopoka", "Standa", "Sunhouse", "Trường Thọ", "Việt Nhật", "VinaKip"};
        for (String p : preset) {
            set.add(p);
        }
        for (MatHang mh : getAll()) {
            if (mh.getHangSanXuat() != null && !mh.getHangSanXuat().trim().isEmpty()) {
                set.add(mh.getHangSanXuat().trim());
            }
        }
        return new ArrayList<>(set);
    }

    public List<String> getDistinctDoiTacKyGui() {
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        String[] preset = {"Công ty ABC", "Đại lý phân phối", "Nhà cung cấp chính", "Công ty TNHH Song Long", "Nhà phân phối Miền Bắc", "Đối tác Tân Á", "Ký gửi Hải Hà"};
        for (String p : preset) {
            set.add(p);
        }
        for (MatHang mh : getAll()) {
            if (mh.getDoiTacKyGui() != null && !mh.getDoiTacKyGui().trim().isEmpty()) {
                set.add(mh.getDoiTacKyGui().trim());
            }
        }
        return new ArrayList<>(set);
    }

    public List<String> getDistinctDonViTinhChan() {
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        String[] preset = {"Bao", "Bịch", "Bộ", "Cây", "Chục", "Gói", "Hộp", "Két", "Lốc", "Thùng"};
        for (String p : preset) {
            set.add(p);
        }
        for (MatHang mh : getAll()) {
            if (mh.getTenDonViTinhChan() != null && !mh.getTenDonViTinhChan().trim().isEmpty()) {
                set.add(mh.getTenDonViTinhChan().trim());
            }
        }
        return new ArrayList<>(set);
    }

    public void updateCriteriaValue(TieuChiPhanLoai tieuChi, String oldValue, String newValue) {
        if (oldValue == null || newValue == null || oldValue.equalsIgnoreCase(newValue)) return;
        for (MatHang mh : MEMORY_PRODUCTS) {
            if (tieuChi == TieuChiPhanLoai.HANG_SAN_XUAT) {
                if (oldValue.equalsIgnoreCase(mh.getHangSanXuat())) {
                    mh.setHangSanXuat(newValue);
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DOI_TAC_KY_GUI) {
                if (oldValue.equalsIgnoreCase(mh.getDoiTacKyGui())) {
                    mh.setDoiTacKyGui(newValue);
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DON_VI_TINH_CHAN) {
                if (oldValue.equalsIgnoreCase(mh.getTenDonViTinhChan())) {
                    mh.setTenDonViTinhChan(newValue);
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DON_VI_TINH) {
                if (oldValue.equalsIgnoreCase(mh.getTenDonViTinh())) {
                    mh.setTenDonViTinh(newValue);
                    update(mh);
                }
            }
        }
    }

    public void deleteCriteriaValue(TieuChiPhanLoai tieuChi, String value) {
        if (value == null) return;
        for (MatHang mh : MEMORY_PRODUCTS) {
            if (tieuChi == TieuChiPhanLoai.HANG_SAN_XUAT) {
                if (value.equalsIgnoreCase(mh.getHangSanXuat())) {
                    mh.setHangSanXuat("");
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DOI_TAC_KY_GUI) {
                if (value.equalsIgnoreCase(mh.getDoiTacKyGui())) {
                    mh.setDoiTacKyGui("");
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DON_VI_TINH_CHAN) {
                if (value.equalsIgnoreCase(mh.getTenDonViTinhChan())) {
                    mh.setTenDonViTinhChan("");
                    mh.setDonViTinhChanId("");
                    update(mh);
                }
            } else if (tieuChi == TieuChiPhanLoai.DON_VI_TINH) {
                if (value.equalsIgnoreCase(mh.getTenDonViTinh())) {
                    mh.setTenDonViTinh("");
                    mh.setDonViTinhId("");
                    update(mh);
                }
            }
        }
    }

    private List<MatHang> filterListWithConfig(List<MatHang> list, String keyword, SystemConfig config) {
        String timTheo = config != null && config.getTimTheo() != null ? config.getTimTheo() : "Cả tên hàng và mã hàng";
        String cachTim = config != null && config.getCachTim() != null ? config.getCachTim() : "Tìm có chứa";

        List<MatHang> result = new ArrayList<>();
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String kw = hasKeyword ? keyword.trim() : "";

        for (MatHang mh : list) {
            if (!hasKeyword) {
                result.add(mh);
                continue;
            }
            boolean match = false;
            if ("Tên hàng".equalsIgnoreCase(timTheo)) {
                match = matchText(mh.getName(), kw, cachTim);
            } else if ("Mã hàng".equalsIgnoreCase(timTheo)) {
                match = matchText(mh.getCode(), kw, cachTim);
            } else {
                // "Cả tên hàng và mã hàng" (kèm cả nhóm hàng)
                match = matchText(mh.getName(), kw, cachTim)
                        || matchText(mh.getCode(), kw, cachTim)
                        || matchText(mh.getTenNhom(), kw, cachTim);
            }
            if (match) {
                result.add(mh);
            }
        }

        // Áp dụng cấu hình Sắp xếp thứ tự theo: Mã hàng hoặc Tên hàng
        if (config != null && "Tên hàng".equalsIgnoreCase(config.getSapXepThuTuTheo())) {
            result.sort((a, b) -> {
                String na = a.getName() != null ? a.getName() : "";
                String nb = b.getName() != null ? b.getName() : "";
                int cmp = na.compareToIgnoreCase(nb);
                if (cmp != 0) return cmp;
                String ca = a.getCode() != null ? a.getCode() : "";
                String cb = b.getCode() != null ? b.getCode() : "";
                return ca.compareToIgnoreCase(cb);
            });
        } else {
            result.sort((a, b) -> {
                String ca = a.getCode() != null ? a.getCode() : "";
                String cb = b.getCode() != null ? b.getCode() : "";
                int cmp = ca.compareToIgnoreCase(cb);
                if (cmp != 0) return cmp;
                String na = a.getName() != null ? a.getName() : "";
                String nb = b.getName() != null ? b.getName() : "";
                return na.compareToIgnoreCase(nb);
            });
        }

        return result;
    }

    private boolean matchText(String source, String keyword, String cachTim) {
        if (source == null || keyword == null) return false;
        String s = source.trim().toLowerCase();
        String k = keyword.trim().toLowerCase();

        if ("Tìm bắt đầu".equalsIgnoreCase(cachTim)) {
            return s.startsWith(k);
        } else if ("Tìm chính xác".equalsIgnoreCase(cachTim)) {
            return s.equals(k);
        } else {
            // "Tìm có chứa"
            return s.contains(k);
        }
    }

    public boolean existsByName(String name, String excludeId) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmed = name.trim().toLowerCase();
        for (MatHang mh : getAll()) {
            if (excludeId != null && excludeId.equals(mh.getId())) continue;
            if (mh.getName() != null && mh.getName().trim().equalsIgnoreCase(trimmed)) {
                return true;
            }
        }
        return false;
    }

    public MatHang findById(String id) {
        if (id == null) return null;
        for (MatHang mh : getAll()) {
            if (id.equals(mh.getId())) return mh;
        }
        return null;
    }

    public MatHang getById(String id) {
        return findById(id);
    }

    public boolean insert(MatHang mh) {
        if (mh == null) return false;
        if (mh.getId() == null || mh.getId().trim().isEmpty()) {
            mh.setId(UUID.randomUUID().toString());
        }
        mh.setTimeCreated(new Timestamp(System.currentTimeMillis()));
        mh.setUserCreatedName(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getName() : "Administrator");
        MEMORY_PRODUCTS.add(0, mh);
        return tryInsertToDb(mh);
    }

    public static char calculateChecksumChar(String numStr) {
        if (numStr == null || numStr.isEmpty()) return '0';
        int sum = 0;
        for (int i = numStr.length() - 1; i >= 0; i--) {
            char c = numStr.charAt(i);
            if (Character.isDigit(c)) {
                int digit = c - '0';
                int weight = ((numStr.length() - 1 - i) % 2 == 0) ? 3 : 1;
                sum += digit * weight;
            }
        }
        int check = (10 - (sum % 10)) % 10;
        return (char) ('0' + check);
    }

    public String generateProductCodeOrBarcode(SystemConfig config, String prefix, boolean isByWeight) {
        if (config == null) {
            return generateNextProductCode(prefix);
        }

        if (config.isTuDongSinhMaVach()) {
            int len = config.getChieuDaiMaVach();
            if (len <= 0) len = 7;
            boolean hasCheck = config.isCoKyTuKiemTra();
            int baseLen = hasCheck ? Math.max(1, len - 1) : len;

            String codePrefix = "";
            if (config.isSuDungTemInBangCanDienTu() && isByWeight) {
                codePrefix = (config.getChuoiBatDauCanDienTu() != null && !config.getChuoiBatDauCanDienTu().trim().isEmpty())
                        ? config.getChuoiBatDauCanDienTu().trim() : "29";
            }

            int numSlots = Math.max(1, baseLen - codePrefix.length());
            long maxNum = 0;
            for (MatHang mh : getAll()) {
                if (mh.getCode() != null) {
                    String c = mh.getCode().trim();
                    if (c.startsWith(codePrefix) && c.matches("\\d+")) {
                        try {
                            String sub = hasCheck && c.length() > codePrefix.length() + 1
                                    ? c.substring(codePrefix.length(), c.length() - 1)
                                    : c.substring(codePrefix.length());
                            long val = Long.parseLong(sub);
                            if (val > maxNum) maxNum = val;
                        } catch (Exception ignored) {}
                    }
                }
            }

            long nextNum = maxNum + 1;
            String formatPattern = "%0" + numSlots + "d";
            String baseCode = codePrefix + String.format(formatPattern, nextNum);
            if (baseCode.length() > baseLen) {
                baseCode = baseCode.substring(baseCode.length() - baseLen);
            }

            if (hasCheck) {
                return baseCode + calculateChecksumChar(baseCode);
            } else {
                return baseCode;
            }
        } else {
            return generateNextProductCode(prefix);
        }
    }

    public String generateNextProductCode(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "MH";
        }
        prefix = prefix.trim();
        String prefixUpper = prefix.toUpperCase();
        int maxIndex = 0;

        for (MatHang mh : getAll()) {
            if (mh.getCode() != null) {
                String code = mh.getCode().trim();
                if (code.toUpperCase().startsWith(prefixUpper)) {
                    String suffix = code.substring(prefix.length()).trim();
                    try {
                        int num = Integer.parseInt(suffix);
                        if (num > maxIndex) {
                            maxIndex = num;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        int nextIndex = maxIndex + 1;
        return prefix + String.format("%03d", nextIndex);
    }

    public boolean update(MatHang mh) {
        if (mh == null || mh.getId() == null) return false;
        mh.setTimeModified(new Timestamp(System.currentTimeMillis()));
        mh.setUserModifiedName(AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getName() : "Administrator");

        boolean memFound = false;
        for (int i = 0; i < MEMORY_PRODUCTS.size(); i++) {
            if (mh.getId().equals(MEMORY_PRODUCTS.get(i).getId())) {
                MEMORY_PRODUCTS.set(i, mh);
                memFound = true;
                break;
            }
        }
        boolean dbOk = tryUpdateToDb(mh);
        return dbOk || memFound;
    }

    private static final java.util.Map<String, MatHang> TRASH_PRODUCTS = new java.util.concurrent.ConcurrentHashMap<>();

    public boolean moveToTrash(String id) {
        if (id == null) return false;
        MatHang found = findById(id);
        if (found != null) {
            found.setStatus(-1);
            TRASH_PRODUCTS.put(id, found);
        }
        MEMORY_PRODUCTS.removeIf(m -> id.equals(m.getId()));

        String sql = "UPDATE DMATHANG SET STATUS = -1 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    public boolean restoreFromTrash(String id) {
        if (id == null) return false;
        MatHang restored = TRASH_PRODUCTS.remove(id);
        if (restored == null) {
            restored = findByIdInTrash(id);
        }
        if (restored != null) {
            restored.setStatus(1);
            MEMORY_PRODUCTS.add(0, restored);
        }

        String sql = "UPDATE DMATHANG SET STATUS = 30 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    public boolean deletePermanently(String id) {
        if (id == null) return false;
        TRASH_PRODUCTS.remove(id);
        MEMORY_PRODUCTS.removeIf(m -> id.equals(m.getId()));

        String sql = "DELETE FROM DMATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public List<MatHang> getTrashProducts() {
        List<MatHang> dbList = tryLoadTrashFromDb();
        java.util.Map<String, MatHang> map = new java.util.LinkedHashMap<>();
        if (dbList != null) {
            for (MatHang mh : dbList) {
                if (mh.getId() != null) {
                    map.put(mh.getId(), mh);
                }
            }
        }
        for (java.util.Map.Entry<String, MatHang> entry : TRASH_PRODUCTS.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return new ArrayList<>(map.values());
    }

    public MatHang findByIdInTrash(String id) {
        if (id == null) return null;
        if (TRASH_PRODUCTS.containsKey(id)) return TRASH_PRODUCTS.get(id);
        for (MatHang mh : getTrashProducts()) {
            if (id.equals(mh.getId())) return mh;
        }
        return null;
    }

    public boolean delete(String id) {
        return moveToTrash(id);
    }

    public boolean deleteByNhom(String tenNhom, String nhomId) {
        if (tenNhom == null || tenNhom.trim().isEmpty()) return false;
        for (MatHang m : MEMORY_PRODUCTS) {
            boolean matchId = nhomId != null && nhomId.equals(m.getNhomMatHangId());
            boolean matchName = tenNhom.equalsIgnoreCase(m.getTenNhom());
            if (matchId || matchName) {
                m.setStatus(-1);
                if (m.getId() != null) {
                    TRASH_PRODUCTS.put(m.getId(), m);
                }
            }
        }
        MEMORY_PRODUCTS.removeIf(m -> {
            boolean matchId = nhomId != null && nhomId.equals(m.getNhomMatHangId());
            boolean matchName = tenNhom.equalsIgnoreCase(m.getTenNhom());
            return matchId || matchName;
        });

        String sqlSoft = "UPDATE DMATHANG SET STATUS = -1 WHERE DNHOMMATHANGID = ? OR DNHOMMATHANGID IN (SELECT ID FROM DNHOMMATHANG WHERE NAME = ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSoft)) {
            ps.setString(1, nhomId != null ? nhomId : "");
            ps.setString(2, tenNhom);
            ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
        } catch (Exception ignored) {}
        return true;
    }

    static {
        ensureDatabaseColumns();
    }

    private static void ensureDatabaseColumns() {
    }

    private MatHang mapResultSetToMatHang(ResultSet rs) throws SQLException {
        MatHang mh = new MatHang();
        mh.setId(rs.getString("ID"));
        mh.setCode(rs.getString("CODE"));
        mh.setName(rs.getString("NAME"));
        mh.setMaSanCo(rs.getString("MASANCO"));
        mh.setGiaNhap(rs.getDouble("GIANHAP"));
        mh.setGiaBan(rs.getDouble("GIABAN"));
        mh.setGiaVon(rs.getDouble("GIAVON"));
        mh.setGiaBan2(rs.getDouble("GIABAN2"));
        mh.setGiaBan3(rs.getDouble("GIABAN3"));
        mh.setGiaBan4(rs.getDouble("GIABAN4"));
        mh.setGiaBanChan(rs.getDouble("GIABANCHAN"));
        mh.setQuyDoi(rs.getDouble("QUYDOI"));
        mh.setGiamGiaPt(rs.getDouble("MACDINHGIAMGIA"));
        mh.setGiamGiaTien(rs.getDouble("MACDINHGIAMTIEN"));
        mh.setNhomMatHangId(rs.getString("DNHOMMATHANGID"));
        mh.setDonViTinhId(rs.getString("DDONVITINHID"));
        mh.setDonViTinhChanId(rs.getString("DDONVITINHCHANID"));
        mh.setTenNhom(rs.getString("TENNHOM"));
        mh.setTenDonViTinh(rs.getString("TENDONVITINH"));
        mh.setTenDonViTinhChan(rs.getString("TENDONVITINHCHAN"));
        mh.setGhiChu(rs.getString("NOTE"));
        mh.setStatus(rs.getInt("STATUS"));
        mh.setTimeCreated(rs.getTimestamp("TIMECREATED"));
        mh.setUserCreatedId(rs.getString("USERCREATEDID"));
        mh.setTimeModified(rs.getTimestamp("TIMEMODIFIED"));
        mh.setUserModifiedId(rs.getString("USERMODIFIEDID"));
        mh.setTonToiThieu(rs.getDouble("TONTOITHIEU"));
        mh.setTonToiDa(rs.getDouble("TONTOIDA"));
        mh.setBaoHanh(rs.getString("BAOHANH"));
        mh.setHoaHong(rs.getDouble("HOAHONG"));
        mh.setKhoiLuongTheoCan(rs.getInt("THEOCAN") == 1);
        mh.setCoHanSuDung(rs.getInt("COHANSUDUNG") == 1);
        mh.setPhuongThucTruKho(rs.getInt("LOAIDINHLUONG"));
        mh.setKichThuoc(rs.getString("SIZE"));
        mh.setKhongInCheBien(rs.getInt("KHONGINCHEBIEN") == 1);
        mh.setHangSanXuat(rs.getString("DHANGSANXUATID"));
        mh.setDoiTacKyGui(rs.getString("DDOITACKYGUIID"));
        return mh;
    }

    private List<MatHang> tryLoadTrashFromDb() {
        List<MatHang> list = new ArrayList<>();
        String sql = "SELECT m.ID, m.CODE, m.NAME, m.MASANCO, m.GIANHAP, m.GIABAN, m.GIAVON, " +
                     "m.GIABAN2, m.GIABAN3, m.GIABAN4, m.GIABANCHAN, m.QUYDOI, " +
                     "m.MACDINHGIAMGIA, m.MACDINHGIAMTIEN, m.DNHOMMATHANGID, m.DDONVITINHID, m.DDONVITINHCHANID, " +
                     "m.NOTE, m.STATUS, m.USERCREATEDID, m.TIMECREATED, m.USERMODIFIEDID, m.TIMEMODIFIED, " +
                     "m.TONTOITHIEU, m.TONTOIDA, m.BAOHANH, m.DHANGSANXUATID, m.DDOITACKYGUIID, m.HOAHONG, " +
                     "m.THEOCAN, m.COHANSUDUNG, m.LOAIDINHLUONG, m.SIZE, m.KHONGINCHEBIEN, " +
                     "n.NAME as TENNHOM, d.NAME as TENDONVITINH, dc.NAME as TENDONVITINHCHAN " +
                     "FROM DMATHANG m " +
                     "LEFT JOIN DNHOMMATHANG n ON m.DNHOMMATHANGID = n.ID " +
                     "LEFT JOIN DDONVITINH d ON m.DDONVITINHID = d.ID " +
                     "LEFT JOIN DDONVITINH dc ON m.DDONVITINHCHANID = dc.ID " +
                     "WHERE m.STATUS < 0 " +
                     "ORDER BY m.CODE, m.NAME";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToMatHang(rs));
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private List<MatHang> tryLoadFromDb(String tenNhomFilter, String nhomIdFilter) {
        List<MatHang> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.ID, m.CODE, m.NAME, m.MASANCO, m.GIANHAP, m.GIABAN, m.GIAVON, " +
                "m.GIABAN2, m.GIABAN3, m.GIABAN4, m.GIABANCHAN, m.QUYDOI, " +
                "m.MACDINHGIAMGIA, m.MACDINHGIAMTIEN, m.DNHOMMATHANGID, m.DDONVITINHID, m.DDONVITINHCHANID, " +
                "m.NOTE, m.STATUS, m.USERCREATEDID, m.TIMECREATED, m.USERMODIFIEDID, m.TIMEMODIFIED, " +
                "m.TONTOITHIEU, m.TONTOIDA, m.BAOHANH, m.DHANGSANXUATID, m.DDOITACKYGUIID, m.HOAHONG, " +
                "m.THEOCAN, m.COHANSUDUNG, m.LOAIDINHLUONG, m.SIZE, m.KHONGINCHEBIEN, " +
                "n.NAME as TENNHOM, d.NAME as TENDONVITINH, dc.NAME as TENDONVITINHCHAN " +
                "FROM DMATHANG m " +
                "LEFT JOIN DNHOMMATHANG n ON m.DNHOMMATHANGID = n.ID " +
                "LEFT JOIN DDONVITINH d ON m.DDONVITINHID = d.ID " +
                "LEFT JOIN DDONVITINH dc ON m.DDONVITINHCHANID = dc.ID " +
                "WHERE (m.STATUS IS NULL OR m.STATUS >= 0) "
        );

        if (nhomIdFilter != null && !nhomIdFilter.trim().isEmpty()) {
            sql.append("AND m.DNHOMMATHANGID = ? ");
        } else if (tenNhomFilter != null && !tenNhomFilter.trim().isEmpty()) {
            if ("Chưa thiết lập".equalsIgnoreCase(tenNhomFilter)) {
                sql.append("AND (m.DNHOMMATHANGID IS NULL OR n.NAME IS NULL) ");
            } else if (!"Tất cả".equalsIgnoreCase(tenNhomFilter)) {
                sql.append("AND n.NAME = ? ");
            }
        }
        SystemConfig sysConfig = new SystemConfigDAO().loadConfig();
        if (sysConfig != null && "Tên hàng".equalsIgnoreCase(sysConfig.getSapXepThuTuTheo())) {
            sql.append("ORDER BY m.NAME, m.CODE");
        } else {
            sql.append("ORDER BY m.CODE, m.NAME");
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int pIdx = 1;
            if (nhomIdFilter != null && !nhomIdFilter.trim().isEmpty()) {
                ps.setString(pIdx++, nhomIdFilter);
            } else if (tenNhomFilter != null && !tenNhomFilter.trim().isEmpty() && !"Chưa thiết lập".equalsIgnoreCase(tenNhomFilter) && !"Tất cả".equalsIgnoreCase(tenNhomFilter)) {
                ps.setString(pIdx++, tenNhomFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMatHang(rs));
                }
            }
            return list;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean tryInsertToDb(MatHang mh) {
        String sql = "INSERT INTO DMATHANG (" +
                "ID, CODE, NAME, MASANCO, GIANHAP, GIABAN, GIAVON, " +
                "GIABAN2, GIABAN3, GIABAN4, GIABANCHAN, QUYDOI, " +
                "MACDINHGIAMGIA, MACDINHGIAMTIEN, DNHOMMATHANGID, DDONVITINHID, DDONVITINHCHANID, " +
                "NOTE, STATUS, TIMECREATED, USERCREATEDID, TONTOITHIEU, TONTOIDA, " +
                "BAOHANH, DHANGSANXUATID, DDOITACKYGUIID, HOAHONG, THEOCAN, " +
                "COHANSUDUNG, LOAIDINHLUONG, SIZE, KHONGINCHEBIEN" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, 30, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, ?, ?" +
                ")";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, mh.getId());
            ps.setString(i++, mh.getCode());
            ps.setString(i++, mh.getName());
            ps.setString(i++, mh.getMaSanCo() != null ? mh.getMaSanCo() : "");
            ps.setDouble(i++, mh.getGiaNhap());
            ps.setDouble(i++, mh.getGiaBan());
            ps.setDouble(i++, mh.getGiaVon());
            ps.setDouble(i++, mh.getGiaBan2());
            ps.setDouble(i++, mh.getGiaBan3());
            ps.setDouble(i++, mh.getGiaBan4());
            ps.setDouble(i++, mh.getGiaBanChan());
            ps.setDouble(i++, mh.getQuyDoi());
            ps.setDouble(i++, mh.getGiamGiaPt());
            ps.setDouble(i++, mh.getGiamGiaTien());
            ps.setString(i++, mh.getNhomMatHangId());
            ps.setString(i++, mh.getDonViTinhId());
            ps.setString(i++, mh.getDonViTinhChanId());
            ps.setString(i++, mh.getGhiChu() != null ? mh.getGhiChu() : "");
            ps.setTimestamp(i++, mh.getTimeCreated() != null ? mh.getTimeCreated() : new Timestamp(System.currentTimeMillis()));
            String userCreated = mh.getUserCreatedId();
            if (userCreated == null || userCreated.trim().isEmpty()) {
                userCreated = (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null)
                        ? AuthService.getCurrentUser().getId() : "1000001";
            }
            ps.setString(i++, userCreated);
            ps.setDouble(i++, mh.getTonToiThieu());
            ps.setDouble(i++, mh.getTonToiDa());
            ps.setString(i++, mh.getBaoHanh() != null ? mh.getBaoHanh() : "");
            ps.setString(i++, mh.getHangSanXuat() != null ? mh.getHangSanXuat() : "");
            ps.setString(i++, mh.getDoiTacKyGui() != null ? mh.getDoiTacKyGui() : "");
            ps.setDouble(i++, mh.getHoaHong());
            ps.setInt(i++, mh.isKhoiLuongTheoCan() ? 1 : 0);
            ps.setInt(i++, mh.isCoHanSuDung() ? 1 : 0);
            ps.setInt(i++, mh.getPhuongThucTruKho());
            ps.setString(i++, mh.getKichThuoc() != null ? mh.getKichThuoc() : "");
            ps.setInt(i++, mh.isKhongInCheBien() ? 1 : 0);
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryUpdateToDb(MatHang mh) {
        String sql = "UPDATE DMATHANG SET " +
                "CODE=?, NAME=?, MASANCO=?, GIANHAP=?, GIABAN=?, GIAVON=?, " +
                "GIABAN2=?, GIABAN3=?, GIABAN4=?, GIABANCHAN=?, QUYDOI=?, " +
                "MACDINHGIAMGIA=?, MACDINHGIAMTIEN=?, DNHOMMATHANGID=?, DDONVITINHID=?, DDONVITINHCHANID=?, " +
                "NOTE=?, TIMEMODIFIED=?, USERMODIFIEDID=?, TONTOITHIEU=?, TONTOIDA=?, " +
                "BAOHANH=?, DHANGSANXUATID=?, DDOITACKYGUIID=?, HOAHONG=?, THEOCAN=?, " +
                "COHANSUDUNG=?, LOAIDINHLUONG=?, SIZE=?, KHONGINCHEBIEN=? " +
                "WHERE ID=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, mh.getCode());
            ps.setString(i++, mh.getName());
            ps.setString(i++, mh.getMaSanCo() != null ? mh.getMaSanCo() : "");
            ps.setDouble(i++, mh.getGiaNhap());
            ps.setDouble(i++, mh.getGiaBan());
            ps.setDouble(i++, mh.getGiaVon());
            ps.setDouble(i++, mh.getGiaBan2());
            ps.setDouble(i++, mh.getGiaBan3());
            ps.setDouble(i++, mh.getGiaBan4());
            ps.setDouble(i++, mh.getGiaBanChan());
            ps.setDouble(i++, mh.getQuyDoi());
            ps.setDouble(i++, mh.getGiamGiaPt());
            ps.setDouble(i++, mh.getGiamGiaTien());
            ps.setString(i++, mh.getNhomMatHangId());
            ps.setString(i++, mh.getDonViTinhId());
            ps.setString(i++, mh.getDonViTinhChanId());
            ps.setString(i++, mh.getGhiChu() != null ? mh.getGhiChu() : "");
            ps.setTimestamp(i++, mh.getTimeModified() != null ? mh.getTimeModified() : new Timestamp(System.currentTimeMillis()));
            String userModified = mh.getUserModifiedId();
            if (userModified == null || userModified.trim().isEmpty()) {
                userModified = (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null)
                        ? AuthService.getCurrentUser().getId() : "1000001";
            }
            ps.setString(i++, userModified);
            ps.setDouble(i++, mh.getTonToiThieu());
            ps.setDouble(i++, mh.getTonToiDa());
            ps.setString(i++, mh.getBaoHanh() != null ? mh.getBaoHanh() : "");
            ps.setString(i++, mh.getHangSanXuat() != null ? mh.getHangSanXuat() : "");
            ps.setString(i++, mh.getDoiTacKyGui() != null ? mh.getDoiTacKyGui() : "");
            ps.setDouble(i++, mh.getHoaHong());
            ps.setInt(i++, mh.isKhoiLuongTheoCan() ? 1 : 0);
            ps.setInt(i++, mh.isCoHanSuDung() ? 1 : 0);
            ps.setInt(i++, mh.getPhuongThucTruKho());
            ps.setString(i++, mh.getKichThuoc() != null ? mh.getKichThuoc() : "");
            ps.setInt(i++, mh.isKhongInCheBien() ? 1 : 0);
            ps.setString(i++, mh.getId());
            int rows = ps.executeUpdate();
            if (!conn.getAutoCommit()) conn.commit();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean tryDeleteFromDb(String id) {
        String sql = "DELETE FROM DMATHANG WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) return true;
        } catch (Exception ignored) {}

        // Fallback to soft delete
        String sqlSoft = "UPDATE DMATHANG SET STATUS = -1 WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlSoft)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public double getTonKhoHienTai(String matHangId) {
        if (matHangId == null || matHangId.trim().isEmpty()) return 0.0;
        String sql = "SELECT COALESCE(SUM(SLNHAP), 0) - COALESCE(SUM(SLXUAT), 0) AS TONKHO " +
                "FROM TDONHANGCHITIET c " +
                "JOIN TDONHANG d ON c.TDONHANGID = d.ID " +
                "WHERE c.DMATHANGID = ? AND (d.STATUS IS NULL OR d.STATUS >= 0)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matHangId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("TONKHO");
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getTonKhoHienTai: " + e.getMessage());
        }
        return 0.0;
    }

    public boolean updateGiaVon(String matHangId, double giaVonMoi) {
        if (matHangId == null || matHangId.trim().isEmpty()) return false;
        String sql = "UPDATE DMATHANG SET GIAVON = ? WHERE ID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, giaVonMoi);
            ps.setString(2, matHangId.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updateGiaVon: " + e.getMessage());
            return false;
        }
    }
}

