package kazuko.ex.ui.common;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DatePickerField extends JPanel {
    private final JTextField txtDate;
    private final JButton btnCalendar;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private final JPopupMenu popupCalendar = new JPopupMenu();
    private final Calendar calendarView = Calendar.getInstance();
    private boolean isUpdating = false;

    public DatePickerField() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        txtDate = new JTextField();
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDate.setPreferredSize(new Dimension(80, 24));
        txtDate.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(175, 195, 220), 1),
                BorderFactory.createEmptyBorder(1, 4, 1, 2)
        ));

        txtDate.getDocument().addDocumentListener(new DocumentListener() {
            private void notifyChange() {
                if (!isUpdating) {
                    firePropertyChange("date", null, getDate());
                }
            }
            @Override public void insertUpdate(DocumentEvent e) { notifyChange(); }
            @Override public void removeUpdate(DocumentEvent e) { notifyChange(); }
            @Override public void changedUpdate(DocumentEvent e) { notifyChange(); }
        });

        btnCalendar = new JButton();
        btnCalendar.setPreferredSize(new Dimension(24, 24));
        btnCalendar.setFocusPainted(false);
        btnCalendar.setBackground(new Color(240, 245, 252));
        btnCalendar.setBorder(new LineBorder(new Color(175, 195, 220), 1));
        btnCalendar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCalendar.setToolTipText("Chọn ngày");

        // Custom calendar icon drawn with Graphics2D
        btnCalendar.setIcon(new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = 13;
                int h = 13;
                int startX = x + 1;
                int startY = y + 1;

                // Calendar background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(startX, startY, w, h, 2, 2);

                // Calendar top bar (blue)
                g2.setColor(new Color(40, 110, 200));
                g2.fillRoundRect(startX, startY, w, 4, 2, 2);

                // Calendar border
                g2.setColor(new Color(80, 110, 150));
                g2.drawRoundRect(startX, startY, w, h, 2, 2);

                // Calendar binder rings
                g2.setColor(new Color(40, 40, 40));
                g2.fillRect(startX + 3, startY - 1, 2, 2);
                g2.fillRect(startX + 8, startY - 1, 2, 2);

                // Little grid lines
                g2.setColor(new Color(180, 195, 215));
                g2.drawLine(startX + 2, startY + 7, startX + w - 2, startY + 7);
                g2.drawLine(startX + 2, startY + 10, startX + w - 2, startY + 10);
                g2.drawLine(startX + 5, startY + 5, startX + 5, startY + h - 2);
                g2.drawLine(startX + 9, startY + 5, startX + 9, startY + h - 2);

                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        });

        add(txtDate, BorderLayout.CENTER);
        add(btnCalendar, BorderLayout.EAST);

        btnCalendar.addActionListener(e -> toggleCalendarPopup());
        txtDate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleCalendarPopup();
                }
            }
        });
    }

    public void setDate(Date date) {
        Date old = getDate();
        isUpdating = true;
        try {
            if (date != null) {
                txtDate.setText(sdf.format(date));
                calendarView.setTime(date);
            } else {
                txtDate.setText("");
            }
        } finally {
            isUpdating = false;
        }
        firePropertyChange("date", old, date);
    }

    public void setDate(Timestamp timestamp) {
        if (timestamp != null) {
            setDate(new Date(timestamp.getTime()));
        } else {
            txtDate.setText("");
        }
    }

    public Date getDate() {
        String text = txtDate.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return sdf.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }

    public Timestamp getTimestamp() {
        Date d = getDate();
        return d != null ? new Timestamp(d.getTime()) : null;
    }

    public String getText() {
        return txtDate.getText().trim();
    }

    public void setText(String text) {
        txtDate.setText(text != null ? text.trim() : "");
        Date d = getDate();
        if (d != null) {
            calendarView.setTime(d);
        }
    }

    public JTextField getTextField() {
        return txtDate;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtDate.setEnabled(enabled);
        btnCalendar.setEnabled(enabled);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (txtDate != null) {
            txtDate.setFont(font);
        }
    }

    private void toggleCalendarPopup() {
        if (popupCalendar.isVisible()) {
            popupCalendar.setVisible(false);
            return;
        }

        Date d = getDate();
        if (d != null) {
            calendarView.setTime(d);
        } else {
            calendarView.setTime(new Date());
        }

        rebuildCalendarPopup();
        popupCalendar.show(this, 0, getHeight());
    }

    private void rebuildCalendarPopup() {
        popupCalendar.removeAll();
        popupCalendar.setLayout(new BorderLayout());
        popupCalendar.setBorder(new LineBorder(new Color(150, 180, 220), 1));

        JPanel calPanel = new JPanel(new BorderLayout(0, 4));
        calPanel.setBackground(Color.WHITE);
        calPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        // 1. Month/Year Navigation Header
        JPanel navHeader = new JPanel(new BorderLayout(4, 0));
        navHeader.setBackground(new Color(230, 240, 252));
        navHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));
        navHeader.setPreferredSize(new Dimension(220, 30));

        JButton btnPrevMonth = createNavButton("‹", "Tháng trước");
        JButton btnNextMonth = createNavButton("›", "Tháng sau");
        JButton btnPrevYear = createNavButton("«", "Năm trước");
        JButton btnNextYear = createNavButton("»", "Năm sau");

        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 2));
        leftBtns.setOpaque(false);
        leftBtns.add(btnPrevYear);
        leftBtns.add(btnPrevMonth);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 2));
        rightBtns.setOpaque(false);
        rightBtns.add(btnNextMonth);
        rightBtns.add(btnNextYear);

        int currentMonth = calendarView.get(Calendar.MONTH);
        int currentYear = calendarView.get(Calendar.YEAR);
        JLabel lblMonthYear = new JLabel(String.format("Tháng %02d / %d", currentMonth + 1, currentYear), SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMonthYear.setForeground(new Color(25, 55, 105));

        btnPrevYear.addActionListener(e -> {
            calendarView.add(Calendar.YEAR, -1);
            rebuildCalendarPopup();
        });
        btnNextYear.addActionListener(e -> {
            calendarView.add(Calendar.YEAR, 1);
            rebuildCalendarPopup();
        });
        btnPrevMonth.addActionListener(e -> {
            calendarView.add(Calendar.MONTH, -1);
            rebuildCalendarPopup();
        });
        btnNextMonth.addActionListener(e -> {
            calendarView.add(Calendar.MONTH, 1);
            rebuildCalendarPopup();
        });

        navHeader.add(leftBtns, BorderLayout.WEST);
        navHeader.add(lblMonthYear, BorderLayout.CENTER);
        navHeader.add(rightBtns, BorderLayout.EAST);
        calPanel.add(navHeader, BorderLayout.NORTH);

        // 2. Day Grid
        JPanel gridPanel = new JPanel(new GridLayout(7, 7, 2, 2));
        gridPanel.setBackground(Color.WHITE);

        String[] weekDays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (int i = 0; i < weekDays.length; i++) {
            JLabel lblDay = new JLabel(weekDays[i], SwingConstants.CENTER);
            lblDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDay.setForeground(i == 6 ? new Color(200, 30, 30) : new Color(60, 90, 130));
            gridPanel.add(lblDay);
        }

        Calendar cal = (Calendar) calendarView.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1, Monday=2
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        Calendar prevMonthCal = (Calendar) cal.clone();
        prevMonthCal.add(Calendar.MONTH, -1);
        int daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = offset - 1; i >= 0; i--) {
            int dNum = daysInPrevMonth - i;
            JLabel lblPrev = new JLabel(String.valueOf(dNum), SwingConstants.CENTER);
            lblPrev.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblPrev.setForeground(new Color(185, 195, 205));
            gridPanel.add(lblPrev);
        }

        int maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar todayCal = Calendar.getInstance();
        Date selectedDate = getDate();
        Calendar selectedCal = Calendar.getInstance();
        if (selectedDate != null) {
            selectedCal.setTime(selectedDate);
        }

        for (int day = 1; day <= maxDaysInMonth; day++) {
            final int dayVal = day;
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnDay.setMargin(new Insets(1, 1, 1, 1));
            btnDay.setFocusPainted(false);
            btnDay.setContentAreaFilled(true);
            btnDay.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            boolean isToday = (todayCal.get(Calendar.YEAR) == currentYear &&
                    todayCal.get(Calendar.MONTH) == currentMonth &&
                    todayCal.get(Calendar.DAY_OF_MONTH) == dayVal);

            boolean isSelected = (selectedDate != null &&
                    selectedCal.get(Calendar.YEAR) == currentYear &&
                    selectedCal.get(Calendar.MONTH) == currentMonth &&
                    selectedCal.get(Calendar.DAY_OF_MONTH) == dayVal);

            if (isSelected) {
                btnDay.setBackground(new Color(50, 120, 220));
                btnDay.setForeground(Color.WHITE);
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else if (isToday) {
                btnDay.setBackground(new Color(255, 240, 180));
                btnDay.setForeground(new Color(180, 40, 40));
                btnDay.setBorder(new LineBorder(new Color(230, 160, 30), 1));
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
            } else {
                btnDay.setBackground(Color.WHITE);
                btnDay.setForeground(new Color(30, 30, 30));
            }

            btnDay.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) {
                        btnDay.setBackground(new Color(215, 235, 255));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) {
                        btnDay.setBackground(isToday ? new Color(255, 240, 180) : Color.WHITE);
                    }
                }
            });

            btnDay.addActionListener(e -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(currentYear, currentMonth, dayVal);
                setDate(chosen.getTime());
                popupCalendar.setVisible(false);
            });

            gridPanel.add(btnDay);
        }

        int totalCells = offset + maxDaysInMonth;
        int remaining = (totalCells <= 35) ? 35 - totalCells : 42 - totalCells;
        for (int i = 1; i <= remaining; i++) {
            JLabel lblNext = new JLabel(String.valueOf(i), SwingConstants.CENTER);
            lblNext.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblNext.setForeground(new Color(185, 195, 205));
            gridPanel.add(lblNext);
        }

        calPanel.add(gridPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons (Hôm nay, Xóa, Đóng)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        footer.setBackground(new Color(245, 248, 252));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JButton btnToday = createActionButton("Hôm nay");
        JButton btnClear = createActionButton("Xóa");
        JButton btnClose = createActionButton("Đóng");

        btnToday.addActionListener(e -> {
            setDate(new Date());
            popupCalendar.setVisible(false);
        });

        btnClear.addActionListener(e -> {
            setDate(null);
            popupCalendar.setVisible(false);
        });

        btnClose.addActionListener(e -> popupCalendar.setVisible(false));

        footer.add(btnToday);
        footer.add(btnClear);
        footer.add(btnClose);
        calPanel.add(footer, BorderLayout.SOUTH);

        popupCalendar.add(calPanel);
        popupCalendar.pack();
    }

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(20, 22));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setToolTipText(tooltip);
        return btn;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setPreferredSize(new Dimension(65, 22));
        btn.setMargin(new Insets(1, 2, 1, 2));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        return btn;
    }
}
