package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import static org.junit.jupiter.api.Assertions.*;

class ChiTieuDoanhThuUITest {
    static class FakeDAO extends ChiTieuDoanhThuDAO {
        final ChiTieuDoanhThu plan=new ChiTieuDoanhThu();
        FakeDAO(){plan.id="plan";plan.groupId="g";plan.groupName="SALE";plan.year=2026;plan.months[0]=new BigDecimal("500000000");}
        @Override public List<Group> groups(){return List.of(new Group("g","SALE","",null,false,null,30));}
        @Override public List<ChiTieuDoanhThu> list(boolean trash){return trash?List.of():List.of(plan.copy(false));}
        @Override public List<ProvinceTarget> details(String id){return List.of(new ProvinceTarget("p","Hà Nội",new BigDecimal("100"),""));}
        @Override public Map<String,String> provinces(){return Map.of("p","Hà Nội");}
    }

    @Test void filteringClearsStaleProvinceDetailsAndReloadsSelection() throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                ChiTieuDoanhThuPanel panel=new ChiTieuDoanhThuPanel(new FakeDAO());
                JTable table=(JTable)field(panel,"table"),details=(JTable)field(panel,"details");
                JTextField search=(JTextField)field(panel,"search");
                assertEquals(1,table.getRowCount());assertEquals(1,details.getRowCount());
                search.setText("no match");assertEquals(0,table.getRowCount());assertEquals(0,details.getRowCount());
                search.setText("2026");assertEquals(1,table.getRowCount());assertEquals(1,details.getRowCount());
                assertFalse(table.isCellEditable(0,3));
            } catch(Exception e){throw new RuntimeException(e);}
        });
    }
    private static Object field(Object object,String name) throws Exception {
        Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
    }
}
