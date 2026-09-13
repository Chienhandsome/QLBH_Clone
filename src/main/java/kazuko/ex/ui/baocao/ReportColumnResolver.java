package kazuko.ex.ui.baocao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** Đọc các cột dữ liệu thật từ JRXML của báo cáo được chọn. */
final class ReportColumnResolver {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private ReportColumnResolver() {
    }

    static List<String> resolve(String reportName) {
        List<String> displayNames = new ArrayList<>();
        for (ColumnDefinition column : resolveReport(reportName).columns()) {
            displayNames.add(column.displayName());
        }
        return displayNames;
    }

    static ResolvedReport resolveReport(String reportName) {
        for (String candidate : resourceCandidates(reportName)) {
            List<ColumnDefinition> columns = readFields(candidate);
            if (!columns.isEmpty()) {
                String reportKey = candidate.substring(candidate.lastIndexOf('/') + 1,
                        candidate.length() - ".jrxml".length());
                return new ResolvedReport(reportKey, columns);
            }
        }
        return new ResolvedReport(toResourceName(reportName), List.of());
    }

    private static Set<String> resourceCandidates(String reportName) {
        String base = toResourceName(reportName);
        Set<String> candidates = new LinkedHashSet<>();
        addCandidate(candidates, base);
        if (base.startsWith("bao_cao_")) {
            addCandidate(candidates, base.substring("bao_cao_".length()));
        } else {
            addCandidate(candidates, "bao_cao_" + base);
        }

        addCandidate(candidates, base
                .replace("phieu_nhap_kho", "phieu_nhap")
                .replace("phieu_xuat_kho", "phieu_xuat")
                .replace("nhan_vien_nhap", "nhan_vien")
                .replace("ly_do_thu_chi", "ly_do"));

        if ("bieu_do_theo_nhan_vien_kinh_doanh".equals(base)) {
            addCandidate(candidates, "bieu_do_doanh_so_theo_nvkd");
        }
        if ("bao_cao_theo_han_dung".equals(base)) {
            addCandidate(candidates, "bao_cao_hang_theo_han_dung");
        }
        return candidates;
    }

    private static void addCandidate(Set<String> candidates, String baseName) {
        if (baseName != null && !baseName.isBlank()) {
            candidates.add("/reports/" + baseName + ".jrxml");
        }
    }

    private static List<ColumnDefinition> readFields(String resourcePath) {
        List<ColumnDefinition> columns = new ArrayList<>();
        try (InputStream input = ReportColumnResolver.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return columns;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(input);
            NodeList fields = document.getElementsByTagName("field");
            for (int i = 0; i < fields.getLength(); i++) {
                Element field = (Element) fields.item(i);
                String fieldName = field.getAttribute("name");
                if (fieldName == null || fieldName.isBlank() || "stt".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                columns.add(new ColumnDefinition(
                        fieldName, toDisplayName(fieldName), field.getAttribute("class")));
            }
        } catch (Exception e) {
            System.err.println("Không đọc được cột báo cáo " + resourcePath + ": " + e.getMessage());
        }
        return columns;
    }

    private static String toResourceName(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static String toDisplayName(String fieldName) {
        switch (fieldName) {
            case "soPhieu": return "Số phiếu";
            case "ngay":
            case "ngayGroup": return "Ngày";
            case "tenDoiTuong": return "Tên đối tượng";
            case "diaChi": return "Địa chỉ";
            case "loaiPhieu": return "Loại phiếu";
            case "lyDoThuChi": return "Lý do thu chi";
            case "dienGiai": return "Diễn giải";
            case "chungTuGoc": return "Chứng từ gốc";
            case "soTien": return "Số tiền";
            default:
                break;
        }
        String spaced = fieldName.replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replace('_', ' ').trim().toLowerCase(Locale.ROOT);
        List<String> words = new ArrayList<>(Arrays.asList(spaced.split("\\s+")));
        if (!words.isEmpty() && "group".equals(words.get(words.size() - 1))) {
            words.remove(words.size() - 1);
        }

        StringBuilder display = new StringBuilder();
        for (String word : words) {
            if (display.length() > 0) display.append(' ');
            display.append(translateWord(word));
        }
        if (display.length() > 0) {
            display.setCharAt(0, Character.toUpperCase(display.charAt(0)));
        }
        return display.toString();
    }

    private static String translateWord(String word) {
        switch (word) {
            case "so": return "số";
            case "ngay": return "ngày";
            case "ten": return "tên";
            case "doi": return "đối";
            case "tuong": return "tượng";
            case "dia": return "địa";
            case "chi": return "chi";
            case "ly": return "lý";
            case "do": return "do";
            case "thu": return "thu";
            case "dien": return "diễn";
            case "giai": return "giải";
            case "chung": return "chứng";
            case "goc": return "gốc";
            case "tien": return "tiền";
            case "ma": return "mã";
            case "luong": return "lượng";
            case "gia": return "giá";
            case "von": return "vốn";
            case "nhap": return "nhập";
            case "xuat": return "xuất";
            case "hang": return "hàng";
            case "khach": return "khách";
            case "nhan": return "nhân";
            case "vien": return "viên";
            case "thanh": return "thành";
            default: return word;
        }
    }

    record ColumnDefinition(String fieldName, String displayName, String valueClassName) {
    }

    record ResolvedReport(String reportKey, List<ColumnDefinition> columns) {
    }
}
