package kazuko.ex.ui.touch;

import kazuko.ex.dao.LyDoThuChiDAO;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TouchLyDoThuChiPanelTest {

    @Test
    void listHasOneGlobalAddButtonAndOnlyEditDeleteActionsPerItem() {
        TouchLyDoThuChiPanel panel = new TouchLyDoThuChiPanel(() -> {});
        int visibleReasons = (int) new LyDoThuChiDAO().getAllActive().stream()
                .filter(reason -> !reason.isFolder() && !reason.isSeparator())
                .count();

        assertEquals(1, countButtons(panel, "THÊM"),
                "Nút THÊM chỉ được xuất hiện một lần ở chân danh sách");
        assertEquals(visibleReasons, countButtons(panel, "XÓA"));
        assertEquals(visibleReasons, countButtons(panel, "SỬA"));
    }

    private int countButtons(Component component, String text) {
        int count = component instanceof JButton button && text.equals(button.getText()) ? 1 : 0;
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                count += countButtons(child, text);
            }
        }
        return count;
    }
}
