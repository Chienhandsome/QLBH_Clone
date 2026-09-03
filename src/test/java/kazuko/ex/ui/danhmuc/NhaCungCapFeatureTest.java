package kazuko.ex.ui.danhmuc;

import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhomNhaCungCapDAO;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhomNhaCungCap;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NhaCungCapFeatureTest {

    private final NhaCungCapDAO nhaCungCapDAO = new NhaCungCapDAO();
    private final NhomNhaCungCapDAO nhomNhaCungCapDAO = new NhomNhaCungCapDAO();

    @Test
    @Order(1)
    public void testGetAllSuppliersFromDb() {
        List<NhaCungCap> list = nhaCungCapDAO.getAll();
        assertNotNull(list, "Danh sách nhà cung cấp không được null");
        assertFalse(list.isEmpty(), "Cơ sở dữ liệu mẫu phải có ít nhất 1 nhà cung cấp");
        System.out.println("Tìm thấy " + list.size() + " nhà cung cấp trong CSDL.");

        boolean foundDemo = false;
        for (NhaCungCap ncc : list) {
            assertNotNull(ncc.getName(), "Tên nhà cung cấp không được null");
            if ("thebao".equalsIgnoreCase(ncc.getMaNhaCungCap()) ||
                "thgantk".equalsIgnoreCase(ncc.getMaNhaCungCap()) ||
                "thkoten".equalsIgnoreCase(ncc.getMaNhaCungCap())) {
                foundDemo = true;
            }
        }
        assertTrue(foundDemo, "Phải tìm thấy các nhà cung cấp mẫu trong CSDL như thebao, thgantk, thkoten");
    }

    @Test
    @Order(2)
    public void testNhomNhaCungCapFromDb() {
        List<NhomNhaCungCap> nhomList = nhomNhaCungCapDAO.getAll();
        assertNotNull(nhomList);
        System.out.println("Tìm thấy " + nhomList.size() + " nhóm nhà cung cấp.");

        // Kiểm thử insert nhóm mới (đặc biệt kiểm tra USERCREATEDID NOT NULL)
        NhomNhaCungCap newNhom = new NhomNhaCungCap();
        String testId = UUID.randomUUID().toString();
        newNhom.setId(testId);
        newNhom.setName("Nhóm Test Tự Động");
        newNhom.setNote("Ghi chú nhóm test");

        boolean inserted = nhomNhaCungCapDAO.insert(newNhom);
        assertTrue(inserted, "Phải insert thành công nhóm nhà cung cấp (không được lỗi USERCREATEDID)");

        newNhom.setName("Nhóm Test Đã Sửa");
        boolean updated = nhomNhaCungCapDAO.update(newNhom);
        assertTrue(updated, "Phải update thành công nhóm nhà cung cấp");

        boolean deleted = nhomNhaCungCapDAO.delete(testId);
        assertTrue(deleted, "Phải delete thành công nhóm nhà cung cấp");
    }

    @Test
    @Order(3)
    public void testInsertUpdateDeleteSupplier() {
        String testId = UUID.randomUUID().toString();
        NhaCungCap ncc = new NhaCungCap();
        ncc.setId(testId);
        ncc.setMaNhaCungCap("test_ncc_auto");
        ncc.setName("Nhà Cung Cấp Tự Động Test");
        ncc.setDiaChi("123 Đường Kiểm Thử");
        ncc.setDienThoai("0987654321");
        ncc.setEmail("test@supplier.com");
        ncc.setWebsite("https://supplier-test.com");
        ncc.setNote("Ghi chú thử nghiệm");

        // 1. Insert
        boolean inserted = nhaCungCapDAO.insert(ncc);
        assertTrue(inserted, "Phải insert thành công nhà cung cấp");

        // 2. Retrieve
        NhaCungCap retrieved = nhaCungCapDAO.getById(testId);
        assertNotNull(retrieved, "Phải tìm thấy nhà cung cấp vừa thêm");
        assertEquals("test_ncc_auto", retrieved.getMaNhaCungCap());
        assertEquals("Nhà Cung Cấp Tự Động Test", retrieved.getName());
        assertEquals("123 Đường Kiểm Thử", retrieved.getDiaChi());

        // 3. Update
        retrieved.setName("Nhà Cung Cấp Đã Cập Nhật");
        retrieved.setDiaChi("456 Đường Mới");
        boolean updated = nhaCungCapDAO.update(retrieved);
        assertTrue(updated, "Phải update thành công");

        NhaCungCap updatedObj = nhaCungCapDAO.getById(testId);
        assertNotNull(updatedObj);
        assertEquals("Nhà Cung Cấp Đã Cập Nhật", updatedObj.getName());
        assertEquals("456 Đường Mới", updatedObj.getDiaChi());

        // 4. Delete (Soft delete)
        boolean deleted = nhaCungCapDAO.delete(testId);
        assertTrue(deleted, "Phải xóa thành công");

        NhaCungCap afterDelete = nhaCungCapDAO.getById(testId);
        assertNotNull(afterDelete);
        assertEquals(-1, afterDelete.getStatus(), "Status sau khi xóa phải là -1");

        List<NhaCungCap> trashList = nhaCungCapDAO.getTrash();
        boolean foundInTrash = trashList.stream().anyMatch(n -> testId.equals(n.getId()));
        assertTrue(foundInTrash, "Nhà cung cấp sau khi xóa phải nằm trong thùng rác");
    }

    @Test
    @Order(4)
    public void testDanhMucNhaCungCapPanelInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DanhMucNhaCungCapPanel panel = new DanhMucNhaCungCapPanel();
            assertNotNull(panel.getTableMaster(), "Bảng master phải được khởi tạo");
            assertNotNull(panel.getMasterModel(), "masterModel phải được khởi tạo");
            assertTrue(panel.getTableMaster().getRowCount() > 0, "Bảng danh mục nhà cung cấp phải tải được dữ liệu CSDL");
        });
    }

    @Test
    @Order(5)
    public void testNhaCungCapDialogInstantiation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            NhaCungCapDialog dlg = new NhaCungCapDialog(frame, null);
            assertNotNull(dlg);
            assertTrue(dlg.getTitle().contains("THÊM MỚI"));
            dlg.dispose();
            frame.dispose();
        });
    }
}
