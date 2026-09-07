package kazuko.ex.quantri;

import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import kazuko.ex.util.ChiTieuDoanhThuExcel;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ChiTieuDoanhThuTest {
    @Test void rejectsNegativeAmountsDuplicateProvincesAndExcessPrecision() {
        ChiTieuDoanhThu t=new ChiTieuDoanhThu();t.months[0]=new BigDecimal("-1");
        assertThrows(IllegalArgumentException.class,t::validate);
        t.months[0]=new BigDecimal("1.234");assertThrows(IllegalArgumentException.class,t::validate);
        t.months[0]=new BigDecimal("10000000000000000");assertThrows(IllegalArgumentException.class,t::validate);
        t.months[0]=BigDecimal.TEN;
        t.provinces.add(new ProvinceTarget("p1","Hà Nội",BigDecimal.TEN,""));
        t.provinces.add(new ProvinceTarget("p1","Hà Nội",BigDecimal.TEN,""));
        assertThrows(IllegalArgumentException.class,t::validate);
    }

    @Test void excelRoundTripRetainsMonthlyAndProvinceAmountsExactly() throws Exception {
        ChiTieuDoanhThu t=new ChiTieuDoanhThu();t.year=2026;t.groupName="SALE";t.groupId="g";
        t.months[0]=new BigDecimal("9999999999999999.99");t.months[11]=new BigDecimal("250000.25");
        t.note="Kế hoạch năm";t.provinces.add(new ProvinceTarget("p","Hà Nội",new BigDecimal("100.05"),"Ghi chú tỉnh"));
        ByteArrayOutputStream out=new ByteArrayOutputStream();ChiTieuDoanhThuExcel.write(out,List.of(t));
        var imported=ChiTieuDoanhThuExcel.read(new ByteArrayInputStream(out.toByteArray()),
                List.of(new Group("g","SALE","",null,false,null,30)),Map.of("p","Hà Nội"));
        assertEquals(1,imported.size());ChiTieuDoanhThu copy=imported.get(0);
        assertArrayEquals(t.months,copy.months);assertEquals(t.provinces,copy.provinces);assertEquals(t.note,copy.note);
        assertNull(copy.id);assertEquals("g",copy.groupId);
    }

    @Test void excelRejectsUnknownGroupBeforeAnythingIsSaved() throws Exception {
        ChiTieuDoanhThu t=new ChiTieuDoanhThu();t.groupName="Missing";
        ByteArrayOutputStream out=new ByteArrayOutputStream();ChiTieuDoanhThuExcel.write(out,List.of(t));
        IllegalArgumentException error=assertThrows(IllegalArgumentException.class,()->ChiTieuDoanhThuExcel.read(
                new ByteArrayInputStream(out.toByteArray()),List.of(),Map.of()));
        assertTrue(error.getMessage().contains("dòng 2"));
    }
}
