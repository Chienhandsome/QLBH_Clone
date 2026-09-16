package kazuko.ex.ui.touch;

import javax.swing.*;
import java.awt.Window;

/** Installs the shared column/row controls used by touch settings dialogs. */
final class TouchGridSettingsSupport {
    static final int MIN_COLUMNS = 1;
    static final int MAX_COLUMNS = 10;
    static final int MIN_ROWS = 1;
    static final int MAX_ROWS = 50;

    private TouchGridSettingsSupport() {
    }

    static void bind(Window owner, JButton columnsButton, JButton rowsButton,
                     TouchGridSettingsModel model, Runnable changed) {
        columnsButton.addActionListener(e -> {
            Integer value = TouchNumberInputDialog.showDialog(owner,
                    "NHẬP SỐ CỘT", model.getGridColumns(), MIN_COLUMNS, MAX_COLUMNS);
            if (value != null) {
                model.setGridColumns(value);
                refreshLabels(columnsButton, rowsButton, model);
                if (changed != null) changed.run();
            }
        });
        rowsButton.addActionListener(e -> {
            Integer value = TouchNumberInputDialog.showDialog(owner,
                    "NHẬP SỐ DÒNG", model.getGridRows(), MIN_ROWS, MAX_ROWS);
            if (value != null) {
                model.setGridRows(value);
                refreshLabels(columnsButton, rowsButton, model);
                if (changed != null) changed.run();
            }
        });
        refreshLabels(columnsButton, rowsButton, model);
    }

    static void refreshLabels(JButton columnsButton, JButton rowsButton,
                              TouchGridSettingsModel model) {
        columnsButton.setText("SỐ CỘT: " + model.getGridColumns());
        rowsButton.setText("SỐ DÒNG: " + model.getGridRows());
    }
}
