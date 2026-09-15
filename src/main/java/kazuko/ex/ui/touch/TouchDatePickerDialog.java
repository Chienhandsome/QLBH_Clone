package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Hộp thoại cảm ứng "NHẬP NGÀY" (Touch Date Picker) theo Hình 4.
 * Bao gồm:
 * - Ô hiển thị ngày lớn: DD/MM/YYYY
 * - Cột Ngày (1-31), cột Tháng (1-12), cột Năm (▲, 4 năm, ▼)
 * - Nút XÓA TRỐNG (tím), CHẤP NHẬN (xanh), HỦY BỎ (đỏ)
 */
public class TouchDatePickerDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER_BG = new Color(79, 139, 169);
    private static final Color TEAL_BTN = new Color(0, 79, 76);
    private static final Color OLIVE_BTN = new Color(55, 75, 30);
    private static final Color GREEN_BTN = new Color(0, 105, 0);
    private static final Color RED_BTN = new Color(175, 20, 20);
    private static final Color PURPLE_BTN = new Color(106, 13, 84);
    private static final Color DARK_RED = new Color(90, 15, 15);
    private static final Color SELECTED_RED = new Color(211, 47, 47);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private int startYear;

    private boolean confirmed = false;
    private String resultDate = null;

    private final JLabel lblDisplay = new JLabel("", SwingConstants.CENTER);
    private final JPanel pnlDays = new JPanel(new GridLayout(7, 5, 4, 4));
    private final JPanel pnlMonths = new JPanel(new GridLayout(6, 2, 4, 4));
    private final JPanel pnlYears = new JPanel(new GridLayout(6, 1, 4, 4));

    public TouchDatePickerDialog(Window owner, String initialDate) {
        super(owner, "NHẬP NGÀY", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        initDate(initialDate);
        initComponents();
        updateDisplay();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initDate(String initialDate) {
        LocalDate now = LocalDate.now();
        if (initialDate != null && !initialDate.trim().isEmpty()) {
            try {
                LocalDate parsed = LocalDate.parse(initialDate.trim(), FORMATTER);
                selectedDay = parsed.getDayOfMonth();
                selectedMonth = parsed.getMonthValue();
                selectedYear = parsed.getYear();
            } catch (Exception e) {
                selectedDay = now.getDayOfMonth();
                selectedMonth = now.getMonthValue();
                selectedYear = now.getYear();
            }
        } else {
            selectedDay = now.getDayOfMonth();
            selectedMonth = now.getMonthValue();
            selectedYear = now.getYear();
        }
        startYear = selectedYear - 2;
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(BG);
        content.setBorder(new LineBorder(HEADER_BG, 2));

        // 1. Header & Display
        JPanel topPanel = new JPanel(new BorderLayout(0, 6));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(8, 12, 4, 12));

        JLabel title = new JLabel("NHẬP NGÀY", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        topPanel.add(title, BorderLayout.NORTH);

        lblDisplay.setOpaque(true);
        lblDisplay.setBackground(Color.WHITE);
        lblDisplay.setForeground(Color.BLACK);
        lblDisplay.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDisplay.setPreferredSize(new Dimension(500, 44));
        lblDisplay.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        topPanel.add(lblDisplay, BorderLayout.SOUTH);

        content.add(topPanel, BorderLayout.NORTH);

        // 2. Center: 3 columns (Ngày, Tháng, Năm)
        JPanel centerPanel = new JPanel(new BorderLayout(14, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(4, 14, 8, 14));

        // Column 1: Ngày (1..31)
        JPanel dayCol = new JPanel(new BorderLayout(0, 4));
        dayCol.setOpaque(false);
        JLabel lblDayTitle = new JLabel("Ngày");
        lblDayTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDayTitle.setForeground(Color.WHITE);
        dayCol.add(lblDayTitle, BorderLayout.NORTH);

        pnlDays.setOpaque(false);
        dayCol.add(pnlDays, BorderLayout.CENTER);
        centerPanel.add(dayCol, BorderLayout.CENTER);

        // Right container for Tháng + Năm
        JPanel monthYearPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        monthYearPanel.setOpaque(false);

        // Column 2: Tháng (1..12)
        JPanel monthCol = new JPanel(new BorderLayout(0, 4));
        monthCol.setOpaque(false);
        JLabel lblMonthTitle = new JLabel("Tháng");
        lblMonthTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMonthTitle.setForeground(Color.WHITE);
        monthCol.add(lblMonthTitle, BorderLayout.NORTH);

        pnlMonths.setOpaque(false);
        monthCol.add(pnlMonths, BorderLayout.CENTER);
        monthYearPanel.add(monthCol);

        // Column 3: Năm (▲, 4 năm, ▼)
        JPanel yearCol = new JPanel(new BorderLayout(0, 4));
        yearCol.setOpaque(false);
        JLabel lblYearTitle = new JLabel("Năm");
        lblYearTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblYearTitle.setForeground(Color.WHITE);
        yearCol.add(lblYearTitle, BorderLayout.NORTH);

        pnlYears.setOpaque(false);
        yearCol.add(pnlYears, BorderLayout.CENTER);
        monthYearPanel.add(yearCol);

        centerPanel.add(monthYearPanel, BorderLayout.EAST);
        content.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Toolbar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(6, 14, 12, 14));

        JButton btnClear = actionButton("XÓA TRỐNG", PURPLE_BTN, new Dimension(130, 42));
        btnClear.addActionListener(e -> {
            confirmed = true;
            resultDate = "";
            dispose();
        });
        bottomBar.add(btnClear, BorderLayout.WEST);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton btnAccept = actionButton("CHẤP NHẬN", GREEN_BTN, new Dimension(130, 42));
        btnAccept.addActionListener(e -> {
            confirmed = true;
            resultDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth, selectedYear);
            dispose();
        });

        JButton btnCancel = actionButton("HỦY BỎ", RED_BTN, new Dimension(110, 42));
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        rightBtns.add(btnAccept);
        rightBtns.add(btnCancel);
        bottomBar.add(rightBtns, BorderLayout.EAST);

        content.add(bottomBar, BorderLayout.SOUTH);

        setContentPane(content);

        // ESC to cancel
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        refreshDayButtons();
        refreshMonthButtons();
        refreshYearButtons();
    }

    private void updateDisplay() {
        lblDisplay.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth, selectedYear));
    }

    private void refreshDayButtons() {
        pnlDays.removeAll();
        int maxDays = YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();
        if (selectedDay > maxDays) {
            selectedDay = maxDays;
            updateDisplay();
        }

        for (int d = 1; d <= 35; d++) {
            if (d <= 31) {
                final int dayVal = d;
                JButton btn = new JButton(String.valueOf(d));
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setFocusPainted(false);
                btn.setBorder(new LineBorder(new Color(60, 110, 120), 1));
                if (dayVal > maxDays) {
                    btn.setEnabled(false);
                    btn.setBackground(new Color(0, 45, 55));
                    btn.setForeground(Color.GRAY);
                } else if (dayVal == selectedDay) {
                    btn.setBackground(SELECTED_RED);
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(TEAL_BTN);
                    btn.setForeground(Color.WHITE);
                }
                btn.addActionListener(e -> {
                    selectedDay = dayVal;
                    updateDisplay();
                    refreshDayButtons();
                });
                pnlDays.add(btn);
            } else {
                JPanel placeholder = new JPanel();
                placeholder.setOpaque(false);
                pnlDays.add(placeholder);
            }
        }
        pnlDays.revalidate();
        pnlDays.repaint();
    }

    private void refreshMonthButtons() {
        pnlMonths.removeAll();
        for (int m = 1; m <= 12; m++) {
            final int monthVal = m;
            JButton btn = new JButton(String.valueOf(m));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorder(new LineBorder(new Color(60, 110, 120), 1));
            if (monthVal == selectedMonth) {
                btn.setBackground(SELECTED_RED);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(OLIVE_BTN);
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(e -> {
                selectedMonth = monthVal;
                updateDisplay();
                refreshDayButtons();
                refreshMonthButtons();
            });
            pnlMonths.add(btn);
        }
        pnlMonths.revalidate();
        pnlMonths.repaint();
    }

    private void refreshYearButtons() {
        pnlYears.removeAll();

        // 1. Up arrow button
        JButton btnUp = new JButton("▲");
        btnUp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnUp.setBackground(DARK_RED);
        btnUp.setForeground(Color.WHITE);
        btnUp.setFocusPainted(false);
        btnUp.setBorder(new LineBorder(new Color(80, 20, 20), 1));
        btnUp.addActionListener(e -> {
            startYear--;
            refreshYearButtons();
        });
        pnlYears.add(btnUp);

        // 2. 4 year buttons
        for (int i = 0; i < 4; i++) {
            final int y = startYear + i;
            JButton btn = new JButton(String.valueOf(y));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorder(new LineBorder(new Color(60, 110, 120), 1));
            if (y == selectedYear) {
                btn.setBackground(SELECTED_RED);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(GREEN_BTN);
                btn.setForeground(Color.WHITE);
            }
            btn.addActionListener(e -> {
                selectedYear = y;
                updateDisplay();
                refreshDayButtons();
                refreshYearButtons();
            });
            pnlYears.add(btn);
        }

        // 3. Down arrow button
        JButton btnDown = new JButton("▼");
        btnDown.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDown.setBackground(DARK_RED);
        btnDown.setForeground(Color.WHITE);
        btnDown.setFocusPainted(false);
        btnDown.setBorder(new LineBorder(new Color(80, 20, 20), 1));
        btnDown.addActionListener(e -> {
            startYear++;
            refreshYearButtons();
        });
        pnlYears.add(btnDown);

        pnlYears.revalidate();
        pnlYears.repaint();
    }

    private JButton actionButton(String text, Color bg, Dimension size) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(bg.brighter(), 1));
        btn.setPreferredSize(size);
        return btn;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getResultDate() {
        return resultDate;
    }

    /**
     * Hiển thị hộp thoại chọn ngày cảm ứng.
     * @return Chuỗi ngày dd/MM/yyyy hoặc chuỗi rỗng nếu xóa trắng, hoặc null nếu bấm Hủy bỏ.
     */
    public static String showDialog(Window owner, String initialDate) {
        TouchDatePickerDialog dlg = new TouchDatePickerDialog(owner, initialDate);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            return dlg.getResultDate();
        }
        return null;
    }
}
