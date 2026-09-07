package kazuko.ex.util;

import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/** Two-sheet format: annual plans and their province allocations, joined by year and group. */
public final class ChiTieuDoanhThuExcel {
    private ChiTieuDoanhThuExcel() {}
    private static final String PLANS="Chỉ tiêu doanh thu", DETAILS="Chỉ tiêu theo tỉnh";
    private static String[] headers() {
        String[] h=new String[15]; h[0]="Năm"; h[1]="Phân nhóm";
        for(int i=0;i<12;i++) h[i+2]="Tháng "+(i+1);
        h[14]="Ghi chú"; return h;
    }
    private static final String[] DETAIL_HEADERS={"Năm","Phân nhóm","Tỉnh thành","Chỉ tiêu","Ghi chú"};

    public static void write(OutputStream out, List<ChiTieuDoanhThu> plans) throws IOException {
        try(Workbook workbook=new XSSFWorkbook()) {
            Sheet main=workbook.createSheet(PLANS), detail=workbook.createSheet(DETAILS);
            row(main,0,headers()); row(detail,0,DETAIL_HEADERS);
            int r=1,d=1;
            for(ChiTieuDoanhThu plan:plans) {
                Object[] cells=new Object[15]; cells[0]=plan.year; cells[1]=plan.groupName;
                System.arraycopy(plan.months,0,cells,2,12); cells[14]=plan.note;
                row(main,r++,cells);
                for(ProvinceTarget p:plan.provinces) row(detail,d++,new Object[]{plan.year,plan.groupName,p.provinceName(),p.amount(),p.note()});
            }
            CellStyle header=workbook.createCellStyle(); header.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font=workbook.createFont(); font.setBold(true); header.setFont(font);
            for(Sheet sheet:List.of(main,detail)) {
                sheet.createFreezePane(2,1);
                for(Cell c:sheet.getRow(0)) { c.setCellStyle(header); sheet.setColumnWidth(c.getColumnIndex(),c.getColumnIndex()==1?6500:4200); }
                sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0,Math.max(0,sheet.getLastRowNum()),0,sheet.getRow(0).getLastCellNum()-1));
            }
            workbook.write(out);
        }
    }

    private static void row(Sheet sheet,int index,Object[] values) {
        Row row=sheet.createRow(index);
        for(int i=0;i<values.length;i++) {
            Cell cell=row.createCell(i); Object v=values[i];
            // Decimal text preserves all 18 database digits through an export/import round trip.
            if(v instanceof BigDecimal n) cell.setCellValue(n.toPlainString());
            else if(v instanceof Number n) cell.setCellValue(n.doubleValue());
            else cell.setCellValue(v==null?"":v.toString());
        }
    }

    public static List<ChiTieuDoanhThu> read(InputStream in,List<Group> groups,Map<String,String> provinces) throws IOException {
        try(Workbook w=WorkbookFactory.create(in)) {
            Sheet main=w.getSheet(PLANS);
            requireHeaders(main,headers());
            Map<String,ChiTieuDoanhThu> plans=new LinkedHashMap<>();
            Map<String,String> groupIds=new HashMap<>(), provinceIds=new HashMap<>();
            for(Group g:groups) if(g.status()>=0 && !g.folder()) groupIds.put(normalize(g.name()),g.id());
            provinces.forEach((id,name)->provinceIds.put(normalize(name),id));
            for(int i=1;i<=main.getLastRowNum();i++) {
                Row row=main.getRow(i); if(blank(row)) continue;
                try {
                    ChiTieuDoanhThu t=new ChiTieuDoanhThu(); t.year=number(row,0).intValueExact();
                    String group=text(row,1);
                    if(!group.isBlank() && !normalize(group).equals(normalize("Chưa thiết lập"))) {
                        t.groupId=groupIds.get(normalize(group));
                        if(t.groupId==null) throw new IllegalArgumentException("Nhóm chưa có trong danh mục: "+group);
                        t.groupName=group;
                    }
                    for(int m=0;m<12;m++) t.months[m]=number(row,m+2);
                    t.note=text(row,14); t.validate();
                    if(plans.putIfAbsent(key(t.year,group),t)!=null) throw new IllegalArgumentException("Trùng năm và phân nhóm.");
                } catch(Exception e) { throw new IllegalArgumentException(PLANS+", dòng "+(i+1)+": "+e.getMessage(),e); }
            }
            Sheet detail=w.getSheet(DETAILS);
            if(detail!=null) {
                requireHeaders(detail,DETAIL_HEADERS);
                for(int i=1;i<=detail.getLastRowNum();i++) {
                    Row row=detail.getRow(i); if(blank(row)) continue;
                    try {
                        ChiTieuDoanhThu t=plans.get(key(number(row,0).intValueExact(),text(row,1)));
                        if(t==null) throw new IllegalArgumentException("Không tìm thấy kế hoạch tương ứng ở sheet "+PLANS);
                        String name=text(row,2), id=provinceIds.get(normalize(name));
                        if(id==null) throw new IllegalArgumentException("Tỉnh thành chưa có trong danh mục: "+name);
                        t.provinces.add(new ProvinceTarget(id,name,number(row,3),text(row,4))); t.validate();
                    } catch(Exception e) { throw new IllegalArgumentException(DETAILS+", dòng "+(i+1)+": "+e.getMessage(),e); }
                }
            }
            if(plans.isEmpty()) throw new IllegalArgumentException("File chưa có kế hoạch nào để nhập.");
            return new ArrayList<>(plans.values());
        }
    }
    private static String key(int year,String group) {
        String value=normalize(group);
        if(value.equals(normalize("Chưa thiết lập"))) value="";
        return year+"|"+value;
    }
    private static String normalize(String value) { return value==null?"":value.trim().toLowerCase(Locale.ROOT); }
    private static boolean blank(Row row) {
        if(row==null) return true;
        for(Cell c:row) if(c.getCellType()!=CellType.BLANK && !c.toString().isBlank()) return false;
        return true;
    }
    private static String text(Row row,int col) {
        Cell c=row.getCell(col); if(c==null) return "";
        if(c.getCellType()==CellType.FORMULA) throw new IllegalArgumentException("Ô công thức cần chuyển thành giá trị trước khi nhập.");
        return new DataFormatter(Locale.ROOT).formatCellValue(c).trim();
    }
    private static BigDecimal number(Row row,int col) {
        Cell c=row.getCell(col);
        if(c!=null && c.getCellType()==CellType.NUMERIC) return BigDecimal.valueOf(c.getNumericCellValue());
        String value=text(row,col); return value.isEmpty()?BigDecimal.ZERO:new BigDecimal(value);
    }
    private static void requireHeaders(Sheet s,String[] headers) {
        if(s==null || s.getRow(0)==null) throw new IllegalArgumentException("Thiếu sheet hoặc tiêu đề. Hãy dùng file mẫu từ màn hình Chỉ tiêu doanh thu.");
        for(int i=0;i<headers.length;i++) if(!headers[i].equals(text(s.getRow(0),i)))
            throw new IllegalArgumentException("Sheet "+s.getSheetName()+": cột "+(i+1)+" phải là '"+headers[i]+"'.");
    }
}
