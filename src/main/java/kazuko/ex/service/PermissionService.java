package kazuko.ex.service;

import kazuko.ex.dao.FunctionDAO;
import kazuko.ex.dao.GroupRoleDAO;
import kazuko.ex.dao.ReportDAO;
import kazuko.ex.dao.ReportRoleDAO;
import kazuko.ex.model.system.*;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

public class PermissionService {
    private static PermissionService instance;

    private final FunctionDAO functionDAO = new FunctionDAO();
    private final GroupRoleDAO groupRoleDAO = new GroupRoleDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final ReportRoleDAO reportRoleDAO = new ReportRoleDAO();

    private SUser currentUser;
    private final Map<String, Integer> functionRolesById = new HashMap<>();
    private final Map<String, Integer> functionRolesByNormalizedName = new HashMap<>();
    private final Map<String, Integer> reportRolesById = new HashMap<>();
    private final Map<String, Integer> reportRolesByNormalizedName = new HashMap<>();

    public static synchronized PermissionService getInstance() {
        if (instance == null) {
            instance = new PermissionService();
        }
        return instance;
    }

    public static void reload() {
        if (instance != null) {
            instance.initPermissions(AuthService.getCurrentUser());
        }
    }

    public void initPermissions(SUser user) {
        this.currentUser = user;
        functionRolesById.clear();
        functionRolesByNormalizedName.clear();
        reportRolesById.clear();
        reportRolesByNormalizedName.clear();

        if (user == null || user.isAdminUser()) {
            return; // Admin has full access to everything
        }

        String groupId = user.getGroupUserId();
        if (groupId == null || groupId.trim().isEmpty()) {
            return;
        }

        // 1. Load Functions & Roles
        List<SFunction> allFunctions = functionDAO.getAllFunctions();
        Map<String, SGroupRole> groupRoles = groupRoleDAO.getRolesMapByGroupId(groupId);

        for (SFunction f : allFunctions) {
            SGroupRole r = groupRoles.get(f.getId());
            int mode = (r != null) ? r.getMode() : 0;
            functionRolesById.put(f.getId(), mode);

            String normName = normalize(f.getName());
            if (!normName.isEmpty()) {
                functionRolesByNormalizedName.put(normName, mode);
            }
        }

        // 2. Load Reports & Roles
        List<SReport> allReports = reportDAO.getAllReports();
        Map<String, SReportRole> repRoles = reportRoleDAO.getReportRolesMapByGroupId(groupId);

        for (SReport rep : allReports) {
            SReportRole rr = repRoles.get(rep.getId());
            int mode = (rr != null) ? rr.getMode() : SReportRole.MODE_ALLOWED; // Default allowed unless specified
            reportRolesById.put(rep.getId(), mode);

            String normName = normalize(rep.getName());
            if (!normName.isEmpty()) {
                reportRolesByNormalizedName.put(normName, mode);
            }
        }
    }

    private static final Map<String, String> FUNCTION_ALIASES = new HashMap<>();
    static {
        FUNCTION_ALIASES.put("nguoidungvaphanquyen", "quanlynguoidung");
        FUNCTION_ALIASES.put("phanquyen", "quanlynguoidung");
        FUNCTION_ALIASES.put("chitieudoanhthu", "danhmucchitieudoanhthu");
        FUNCTION_ALIASES.put("taophieuthu", "thuchi");
        FUNCTION_ALIASES.put("taophieuchi", "thuchi");
        FUNCTION_ALIASES.put("phieuthu", "danhmucphieuthuchi");
        FUNCTION_ALIASES.put("phieuchi", "danhmucphieuthuchi");
        FUNCTION_ALIASES.put("danhmucphieuthu", "danhmucphieuthuchi");
        FUNCTION_ALIASES.put("danhmucphieuchi", "danhmucphieuthuchi");
        FUNCTION_ALIASES.put("nhaphangvaokho", "nhapkho");
        FUNCTION_ALIASES.put("quanlynhapkho", "nhapkho");
        FUNCTION_ALIASES.put("dathangnhacungcap", "quanlydathangnhacungcap");
        FUNCTION_ALIASES.put("dathangncc", "quanlydathangnhacungcap");
        FUNCTION_ALIASES.put("quanlydathangncc", "quanlydathangnhacungcap");
        FUNCTION_ALIASES.put("thuongphat", "quanlythuongphat");
        FUNCTION_ALIASES.put("congngokhachhangbandau", "dulieubandau");
        FUNCTION_ALIASES.put("congngonhacungcapbandau", "dulieubandau");
        FUNCTION_ALIASES.put("tonkhobandau", "dulieubandau");
        FUNCTION_ALIASES.put("taomoi", "taomoicosodulieu");
        FUNCTION_ALIASES.put("saoluu", "saoluucosodulieu");
        FUNCTION_ALIASES.put("phuchoi", "phuchoicosodulieu");
        FUNCTION_ALIASES.put("khoiphuc", "phuchoicosodulieu");
        FUNCTION_ALIASES.put("taomoicsdl", "taomoicosodulieu");
        FUNCTION_ALIASES.put("saoluucsdl", "saoluucosodulieu");
        FUNCTION_ALIASES.put("khoiphuccsdl", "phuchoicosodulieu");
        FUNCTION_ALIASES.put("phuchoicsdl", "phuchoicosodulieu");
        FUNCTION_ALIASES.put("baogiabanhang", "baogia");
        FUNCTION_ALIASES.put("baogiakhachhang", "baogia");
        FUNCTION_ALIASES.put("khachdathang", "dathang");
        FUNCTION_ALIASES.put("guitinnhankhachhang", "guitinnhantoikhachhang");
        FUNCTION_ALIASES.put("quanlyxuatkho", "xuatkho");
        FUNCTION_ALIASES.put("quanlychuyenkho", "chuyenkho");
        FUNCTION_ALIASES.put("quanlykiemke", "kiemkekho");
        FUNCTION_ALIASES.put("quanlytonkho", "tonkho");
        FUNCTION_ALIASES.put("tonnhieukho", "tonkho");
        FUNCTION_ALIASES.put("quanlytonnhieukho", "tonkho");
        FUNCTION_ALIASES.put("danhmuclydothuchi", "danhmucthudochi");
        FUNCTION_ALIASES.put("lydothuchi", "danhmucthudochi");
        FUNCTION_ALIASES.put("taikhoannganhang", "danhmuctaikhoannganhang");
        FUNCTION_ALIASES.put("nhanvien", "danhmucnhanvien");
        FUNCTION_ALIASES.put("calamviec", "danhmuccalamviec");
        FUNCTION_ALIASES.put("cuahang", "danhmuccuahang");
        FUNCTION_ALIASES.put("khohang", "danhmuckhohang");
        FUNCTION_ALIASES.put("khachhang", "danhmuckhachhang");
        FUNCTION_ALIASES.put("nhacungcap", "danhmucnhacungcap");
        FUNCTION_ALIASES.put("mathang", "danhmucmathang");
        FUNCTION_ALIASES.put("dotkhuyenmai", "danhmucdotkhuyenmai");
        FUNCTION_ALIASES.put("cauhinhhethong", "cauhintoanhethong");
        FUNCTION_ALIASES.put("cauhinhtoanhethong", "cauhintoanhethong");
    }

    public boolean isActionAllowed(String actionNameOrId) {
        if (currentUser == null || currentUser.isAdminUser()) {
            return true;
        }
        if (actionNameOrId == null || actionNameOrId.trim().isEmpty()) {
            return true;
        }

        String norm = normalize(actionNameOrId);
        if (norm.isEmpty()) return true;

        // Utilities always allowed
        if ("dangxuat".equals(norm) || "thoat".equals(norm) || "doimatkhau".equals(norm)
                || "thongtinphanmem".equals(norm) || "dangkibanquyen".equals(norm)
                || "ghichu".equals(norm) || "ghichunhanh".equals(norm)) {
            return true;
        }

        // 1. Check ID directly
        if (functionRolesById.containsKey(actionNameOrId)) {
            int mode = functionRolesById.get(actionNameOrId);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }
        if (reportRolesById.containsKey(actionNameOrId)) {
            return reportRolesById.get(actionNameOrId) != SReportRole.MODE_LOCKED;
        }

        // 2. Exact normalized match
        if (functionRolesByNormalizedName.containsKey(norm)) {
            int mode = functionRolesByNormalizedName.get(norm);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }
        if (reportRolesByNormalizedName.containsKey(norm)) {
            return reportRolesByNormalizedName.get(norm) != SReportRole.MODE_LOCKED;
        }

        // 3. Alias mapping
        String alias = FUNCTION_ALIASES.get(norm);
        if (alias != null && functionRolesByNormalizedName.containsKey(alias)) {
            int mode = functionRolesByNormalizedName.get(alias);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        // 4. Best partial match for Function
        String bestFuncKey = null;
        for (String funcKey : functionRolesByNormalizedName.keySet()) {
            if (norm.equals(funcKey) || norm.contains(funcKey) || funcKey.contains(norm)) {
                if (bestFuncKey == null || funcKey.length() > bestFuncKey.length()) {
                    bestFuncKey = funcKey;
                }
            }
        }

        // 5. Best partial match for Report
        String bestReportKey = null;
        for (String repKey : reportRolesByNormalizedName.keySet()) {
            if (norm.equals(repKey) || norm.contains(repKey) || repKey.contains(norm)) {
                if (bestReportKey == null || repKey.length() > bestReportKey.length()) {
                    bestReportKey = repKey;
                }
            }
        }

        if (bestReportKey != null && bestFuncKey != null) {
            if (Math.abs(bestReportKey.length() - norm.length()) <= Math.abs(bestFuncKey.length() - norm.length())) {
                return reportRolesByNormalizedName.get(bestReportKey) != SReportRole.MODE_LOCKED;
            } else {
                int mode = functionRolesByNormalizedName.get(bestFuncKey);
                return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
            }
        } else if (bestReportKey != null) {
            return reportRolesByNormalizedName.get(bestReportKey) != SReportRole.MODE_LOCKED;
        } else if (bestFuncKey != null) {
            int mode = functionRolesByNormalizedName.get(bestFuncKey);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        return true;
    }

    public boolean isFunctionAllowed(String functionNameOrId) {
        if (currentUser == null || currentUser.isAdminUser()) {
            return true;
        }
        if (functionNameOrId == null || functionNameOrId.trim().isEmpty()) {
            return true;
        }

        // Check by ID directly
        if (functionRolesById.containsKey(functionNameOrId)) {
            int mode = functionRolesById.get(functionNameOrId);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        // Check by normalized name
        String norm = normalize(functionNameOrId);
        if (functionRolesByNormalizedName.containsKey(norm)) {
            int mode = functionRolesByNormalizedName.get(norm);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        // Check alias mapping
        String alias = FUNCTION_ALIASES.get(norm);
        if (alias != null && functionRolesByNormalizedName.containsKey(alias)) {
            int mode = functionRolesByNormalizedName.get(alias);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        // Check best partial match
        String bestFuncKey = null;
        for (String key : functionRolesByNormalizedName.keySet()) {
            if (norm.equals(key) || norm.contains(key) || key.contains(norm)) {
                if (bestFuncKey == null || key.length() > bestFuncKey.length()) {
                    bestFuncKey = key;
                }
            }
        }
        if (bestFuncKey != null) {
            int mode = functionRolesByNormalizedName.get(bestFuncKey);
            return mode > 0 && (mode & SGroupRole.MASK_VIEW) != 0;
        }

        return true;
    }

    public boolean isReportAllowed(String reportNameOrId) {
        if (currentUser == null || currentUser.isAdminUser()) {
            return true;
        }
        if (reportNameOrId == null || reportNameOrId.trim().isEmpty()) {
            return true;
        }

        if (reportRolesById.containsKey(reportNameOrId)) {
            return reportRolesById.get(reportNameOrId) != SReportRole.MODE_LOCKED;
        }

        String norm = normalize(reportNameOrId);
        if (reportRolesByNormalizedName.containsKey(norm)) {
            return reportRolesByNormalizedName.get(norm) != SReportRole.MODE_LOCKED;
        }

        String bestReportKey = null;
        for (String key : reportRolesByNormalizedName.keySet()) {
            if (norm.equals(key) || norm.contains(key) || key.contains(norm)) {
                if (bestReportKey == null || key.length() > bestReportKey.length()) {
                    bestReportKey = key;
                }
            }
        }
        if (bestReportKey != null) {
            return reportRolesByNormalizedName.get(bestReportKey) != SReportRole.MODE_LOCKED;
        }

        return true;
    }

    public boolean hasAction(String functionNameOrId, int actionMask) {
        if (currentUser == null || currentUser.isAdminUser()) {
            return true;
        }
        if (functionNameOrId == null || functionNameOrId.trim().isEmpty()) {
            return true;
        }

        Integer mode = functionRolesById.get(functionNameOrId);
        if (mode == null) {
            String norm = normalize(functionNameOrId);
            mode = functionRolesByNormalizedName.get(norm);
        }
        if (mode == null) return true;

        return (mode & actionMask) != 0;
    }

    public static String normalize(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccents = pattern.matcher(temp).replaceAll("");
        return noAccents.replace('đ', 'd').replaceAll("[^a-z0-9]", "");
    }
}
