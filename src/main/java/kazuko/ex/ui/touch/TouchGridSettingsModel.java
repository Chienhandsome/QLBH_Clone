package kazuko.ex.ui.touch;

/** Shared row/column contract used by every touch-card settings screen. */
interface TouchGridSettingsModel {
    int getGridColumns();
    void setGridColumns(int columns);
    int getGridRows();
    void setGridRows(int rows);
}
