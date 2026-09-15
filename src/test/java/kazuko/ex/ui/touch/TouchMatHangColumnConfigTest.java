package kazuko.ex.ui.touch;

import kazuko.ex.model.danhmuc.MatHang;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class TouchMatHangColumnConfigTest {

    @Test
    void testDefaultConfig() {
        TouchMatHangColumnConfig config = TouchMatHangColumnConfig.createDefault();
        assertNotNull(config);
        assertEquals(4, config.getUsedColumns().size());
        assertEquals("NAME", config.getUsedColumns().get(0).getKey());
        assertEquals("HANG_SAN_XUAT", config.getUsedColumns().get(1).getKey());
        assertEquals("CODE", config.getUsedColumns().get(2).getKey());
        assertEquals("GIA_BAN", config.getUsedColumns().get(3).getKey());

        assertFalse(config.getUnusedColumns().isEmpty());
        assertEquals(2, config.getGridColumns());
        assertEquals(8, config.getGridRows());
        assertTrue(config.isShowTitle());
        assertEquals("#004F4C", config.getCardBgColorHex());
    }

    @Test
    void testColumnFormattingAndCloning() {
        TouchMatHangColumnConfig config = TouchMatHangColumnConfig.createDefault();
        TouchMatHangColumnConfig.ColumnItem nameCol = config.getUsedColumns().get(0);

        nameCol.setBold(true);
        nameCol.setItalic(true);
        nameCol.setFontSize(28);
        nameCol.setRightAligned(true);
        nameCol.setColor(Color.RED);

        assertEquals("#FF0000", nameCol.getColorHex());
        assertTrue(nameCol.isBold());
        assertTrue(nameCol.isItalic());
        assertTrue(nameCol.isRightAligned());
        assertEquals(28, nameCol.getFontSize());

        TouchMatHangColumnConfig copy = config.clone();
        assertEquals(copy.getUsedColumns().size(), config.getUsedColumns().size());
        assertEquals("#FF0000", copy.getUsedColumns().get(0).getColorHex());
    }

    @Test
    void testFormattedTextFromProduct() {
        MatHang item = new MatHang();
        item.setName("Sữa tươi");
        item.setHangSanXuat("Vinamilk");
        item.setCode("SP001");
        item.setGiaBan(25000);

        TouchMatHangColumnConfig config = TouchMatHangColumnConfig.createDefault();
        TouchMatHangColumnConfig.ColumnItem nameCol = config.getUsedColumns().get(0);
        TouchMatHangColumnConfig.ColumnItem priceCol = config.getUsedColumns().get(3);

        assertEquals("TÊN: Sữa tươi", TouchMatHangColumnConfig.getFormattedText(item, nameCol, true));
        assertEquals("Sữa tươi", TouchMatHangColumnConfig.getFormattedText(item, nameCol, false));

        assertEquals("GIÁ: 25.000", TouchMatHangColumnConfig.getFormattedText(item, priceCol, true));
        assertEquals("25.000", TouchMatHangColumnConfig.getFormattedText(item, priceCol, false));
    }

    @Test
    void testDaoSaveAndLoad() {
        TouchMatHangColumnConfig config = TouchMatHangColumnConfig.createDefault();
        config.setGridColumns(3);
        config.setGridRows(6);
        config.setCardBgColor(new Color(15, 75, 120));
        config.setShowTitle(false);

        boolean saved = TouchMatHangConfigDAO.saveConfig(config);
        assertTrue(saved, "Lưu cấu hình phải thành công");

        File cfgFile = TouchMatHangConfigDAO.getConfigFile();
        assertTrue(cfgFile.exists(), "File cấu hình phải được tạo");

        TouchMatHangColumnConfig loaded = TouchMatHangConfigDAO.loadConfig();
        assertNotNull(loaded);
        assertEquals(3, loaded.getGridColumns());
        assertEquals(6, loaded.getGridRows());
        assertFalse(loaded.isShowTitle());
        assertEquals(String.format("#%02X%02X%02X", 15, 75, 120), loaded.getCardBgColorHex());
    }
}
