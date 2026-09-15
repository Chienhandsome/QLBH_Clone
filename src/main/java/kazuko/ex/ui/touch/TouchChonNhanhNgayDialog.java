package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.BiConsumer;

/**
 * Hộp thoại "CHỌN NHANH NGÀY" (Hình 3).
 * Cho phép chọn nhanh khoảng thời gian: Hôm nay, Hôm qua, Tuần này, Tuần trước,
 * Tháng này, Tháng trước, Quý này, Quý trước, Năm nay, Năm trước, Xóa trống.
 */
public class TouchChonNhanhNgayDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color OLIVE = new Color(125, 133, 30);
    private static final Color BROWN = new Color(153, 82, 23);
    private static final Color DARK_GREEN = new Color(11, 97, 33);
    private static final Color TEAL = new Color(8, 84, 91);
    private static final Color NAVY = new Color(20, 20, 75);
    private static final Color PURPLE = new Color(128, 0, 128);
    private static final Color RED = new Color(155, 0, 0);

    private static final SimpleDateFormat SDF_TITLE = new SimpleDateFormat("dd/MM/yyyy");

    private final BiConsumer<Date, Date> onSelected;

    public TouchChonNhanhNgayDialog(Window owner, BiConsumer<Date, Date> onSelected) {
        super(owner, "Chọn nhanh ngày", ModalityType.APPLICATION_MODAL);
        this.onSelected = onSelected;

        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 3));

        // 1. Header: "CHỌN NHANH NGÀY - 15/09/2026"
        JLabel title = new JLabel("CHỌN NHANH NGÀY - " + SDF_TITLE.format(new Date()), SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(HEADER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setPreferredSize(new Dimension(0, 48));
        root.add(title, BorderLayout.NORTH);

        // 2. Main Grid: 2 columns x 5 rows
        JPanel grid = new JPanel(new GridLayout(5, 2, 8, 8));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 12, 10, 12));

        // Row 1: HÔM NAY | HÔM QUA
        grid.add(createDateBtn("HÔM NAY", OLIVE, () -> applyToday()));
        grid.add(createDateBtn("HÔM QUA", OLIVE, () -> applyYesterday()));

        // Row 2: TUẦN NÀY | TUẦN TRƯỚC
        grid.add(createDateBtn("TUẦN NÀY", BROWN, () -> applyThisWeek()));
        grid.add(createDateBtn("TUẦN TRƯỚC", BROWN, () -> applyLastWeek()));

        // Row 3: THÁNG NÀY | THÁNG TRƯỚC
        grid.add(createDateBtn("THÁNG NÀY", DARK_GREEN, () -> applyThisMonth()));
        grid.add(createDateBtn("THÁNG TRƯỚC", DARK_GREEN, () -> applyLastMonth()));

        // Row 4: QUÝ NÀY | QUÝ TRƯỚC
        grid.add(createDateBtn("QUÝ NÀY", TEAL, () -> applyThisQuarter()));
        grid.add(createDateBtn("QUÝ TRƯỚC", TEAL, () -> applyLastQuarter()));

        // Row 5: NĂM NAY | NĂM TRƯỚC
        grid.add(createDateBtn("NĂM NAY", NAVY, () -> applyThisYear()));
        grid.add(createDateBtn("NĂM TRƯỚC", NAVY, () -> applyLastYear()));

        root.add(grid, BorderLayout.CENTER);

        // 3. Bottom Row: XÓA TRỐNG | HỦY BỎ
        JPanel bottomBar = new JPanel(new GridLayout(1, 2, 8, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(0, 12, 12, 12));

        JButton btnClear = button("XÓA TRỐNG", PURPLE);
        btnClear.setPreferredSize(new Dimension(0, 52));
        btnClear.addActionListener(e -> {
            if (onSelected != null) onSelected.accept(null, null);
            dispose();
        });

        JButton btnCancel = button("HỦY BỎ", RED);
        btnCancel.setPreferredSize(new Dimension(0, 52));
        btnCancel.addActionListener(e -> dispose());

        bottomBar.add(btnClear);
        bottomBar.add(btnCancel);
        root.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(root);
        setSize(850, 480);
        setLocationRelativeTo(owner);
    }

    private JButton createDateBtn(String text, Color bg, Runnable action) {
        JButton btn = button(text, bg);
        btn.setPreferredSize(new Dimension(0, 52));
        btn.addActionListener(e -> {
            action.run();
            dispose();
        });
        return btn;
    }

    void applyClear() {
        if (onSelected != null) onSelected.accept(null, null);
    }

    void applyPreset(String label) {
        if (label == null) return;
        switch (label.trim().toUpperCase()) {
            case "HÔM NAY" -> applyToday();
            case "HÔM QUA" -> applyYesterday();
            case "TUẦN NÀY" -> applyThisWeek();
            case "TUẦN TRƯỚC" -> applyLastWeek();
            case "THÁNG NÀY" -> applyThisMonth();
            case "THÁNG TRƯỚC" -> applyLastMonth();
            case "QUÝ NÀY" -> applyThisQuarter();
            case "QUÝ TRƯỚC" -> applyLastQuarter();
            case "NĂM NAY" -> applyThisYear();
            case "NĂM TRƯỚC" -> applyLastYear();
        }
    }

    private void applyToday() {
        Calendar cal = Calendar.getInstance();
        Date start = startOfDay(cal.getTime());
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyYesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        Date start = startOfDay(cal.getTime());
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyThisWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date start = startOfDay(cal.getTime());

        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date start = startOfDay(cal.getTime());

        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyThisMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyLastMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyThisQuarter() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int quarterStartMonth = (month / 3) * 3;

        cal.set(Calendar.MONTH, quarterStartMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.MONTH, quarterStartMonth + 2);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyLastQuarter() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int quarterStartMonth = (month / 3) * 3 - 3;
        if (quarterStartMonth < 0) {
            quarterStartMonth += 12;
            cal.add(Calendar.YEAR, -1);
        }

        cal.set(Calendar.MONTH, quarterStartMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.MONTH, quarterStartMonth + 2);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyThisYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private void applyLastYear() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date start = startOfDay(cal.getTime());

        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        Date end = endOfDay(cal.getTime());
        if (onSelected != null) onSelected.accept(start, end);
    }

    private static Date startOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date endOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        return btn;
    }

    public static void showDialog(Window owner, BiConsumer<Date, Date> onSelected) {
        new TouchChonNhanhNgayDialog(owner, onSelected).setVisible(true);
    }
}
