package kazuko.ex.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in test: creates one randomly named group and removes only its own records in finally. */
@EnabledIfSystemProperty(named="chitieu.integration",matches="true")
class ChiTieuDoanhThuDatabaseTest {
    static class TestDAO extends ChiTieuDoanhThuDAO {
        @Override protected String userId() {
            try(Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT FIRST 1 ID FROM SUSER ORDER BY ID")) {
                if(r.next()) return r.getString(1);
                throw new IllegalStateException("CSDL kiểm thử cần có tài khoản.");
            } catch(SQLException e) { throw new IllegalStateException(e); }
        }
        void cleanup(String groupId) throws SQLException {
            try(Connection c=connect()) {
                c.setAutoCommit(false);
                try {
                    for(String sql:List.of("DELETE FROM DCHITIEUCHITIET WHERE DCHITIEUDOANHTHUID IN (SELECT ID FROM DCHITIEUDOANHTHU WHERE DNHOMCHITIEUDOANHTHUID=?)",
                            "DELETE FROM DCHITIEUDOANHTHU WHERE DNHOMCHITIEUDOANHTHUID=?", "DELETE FROM DNHOMCHITIEUDOANHTHU WHERE ID=?")) {
                        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,groupId);s.executeUpdate();}
                    }
                    c.commit();
                }catch(SQLException e){c.rollback();throw e;}
            }
        }
    }
    @Test void savesReloadsUpdatesRollsBackAndRestoresWithRealSchema() throws Exception {
        TestDAO dao=new TestDAO();String group=dao.saveGroup(new Group(null,"QA-"+UUID.randomUUID(),"",null,false,null,30));
        try {
            var province=dao.provinces().entrySet().stream().findFirst().orElseThrow();
            ChiTieuDoanhThu target=new ChiTieuDoanhThu();target.groupId=group;target.year=2091;
            target.months[0]=new BigDecimal("1234567.89");
            target.provinces.add(new ProvinceTarget(province.getKey(),province.getValue(),new BigDecimal("42.50"),"QA"));
            dao.save(target);assertNotNull(target.id);
            ChiTieuDoanhThu loaded=dao.list(false).stream().filter(t->t.id.equals(target.id)).findFirst().orElseThrow();
            assertEquals(0,target.months[0].compareTo(loaded.months[0]));assertEquals(1,dao.details(target.id).size());
            target.months[0]=new BigDecimal("17.25");target.provinces.clear();dao.save(target);
            assertTrue(dao.details(target.id).isEmpty());
            assertThrows(SQLException.class,()->dao.setTrash(group,true,true));
            ChiTieuDoanhThu first=new ChiTieuDoanhThu();first.groupId=group;first.year=2092;
            ChiTieuDoanhThu duplicate=first.copy(true);
            assertThrows(SQLException.class,()->dao.saveAll(List.of(first,duplicate)));
            assertNull(first.id);
            assertTrue(dao.list(false).stream().noneMatch(t->group.equals(t.groupId)&&t.year==2092));
            dao.setTrash(target.id,false,true);assertTrue(dao.list(true).stream().anyMatch(t->t.id.equals(target.id)));
            dao.setTrash(target.id,false,false);assertTrue(dao.list(false).stream().anyMatch(t->t.id.equals(target.id)));
        } finally {dao.cleanup(group);}
    }
}
