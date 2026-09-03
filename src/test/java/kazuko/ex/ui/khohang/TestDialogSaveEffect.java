package kazuko.ex.ui.khohang;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.model.danhmuc.KhoHang;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.lang.reflect.Method;

public class TestDialogSaveEffect {
    @Test
    public void testDialogSave() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame frame = new JFrame();
            QuanLyXuatKhoPanel panel = new QuanLyXuatKhoPanel();
            frame.add(panel);
            frame.pack();

            System.out.println("Panel before dialog, rowCount: " + panel.getMasterModel().getRowCount());

            // Open dialog
            PhieuXuatKhoDialog dlg = new PhieuXuatKhoDialog(frame, panel::loadData);

            // Add 1 item from catalog
            try {
                Method mAdd = PhieuXuatKhoDialog.class.getDeclaredMethod("addSelectedCatalogItem");
                mAdd.setAccessible(true);
                mAdd.invoke(dlg);

                // Save voucher
                Method mSave = PhieuXuatKhoDialog.class.getDeclaredMethod("saveVoucher", boolean.class, boolean.class);
                mSave.setAccessible(true);
                // saveVoucher(createNewAfter = false, closeAfter = true)
                boolean saved = (boolean) mSave.invoke(dlg, false, true);
                System.out.println("Dialog saved: " + saved);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Panel after dialog save, rowCount: " + panel.getMasterModel().getRowCount());
            for (int i = 0; i < panel.getMasterModel().getRowCount(); i++) {
                System.out.println("  Row " + i + ": " + panel.getMasterModel().getValueAt(i, 5)); // Số phiếu
            }

            dlg.dispose();
            frame.dispose();
        });
    }
}
