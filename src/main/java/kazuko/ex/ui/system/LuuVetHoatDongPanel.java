package kazuko.ex.ui.system;

import kazuko.ex.dao.ActivityLogDAO;
import kazuko.ex.model.system.ActivityLog;
import kazuko.ex.service.AuthService;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class LuuVetHoatDongPanel extends JPanel {
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private DatePickerField datePicker;
    private JComboBox<String> userCombo;
    private JTextField referenceField;
    private JTextField contentField;
    private DefaultTableModel tableModel;
    private JTable table;
    private List<ActivityLog> currentLogs = new ArrayList<>();
    private boolean loadingUsers;

    public LuuVetHoatDongPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(createFilterBar(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        registerKeyboardAction(e -> refreshAll(), KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        refreshAll();
    }

    private JComponent createFilterBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        panel.setBackground(new Color(215, 230, 248));

        panel.add(new JLabel("Ngày:"));
        datePicker = new DatePickerField();
        datePicker.setDate(new Date());
        panel.add(datePicker);

        panel.add(new JLabel("Tài khoản:"));
        userCombo = new JComboBox<>();
        userCombo.setPreferredSize(new Dimension(180, 24));
        panel.add(userCombo);

        panel.add(new JLabel("Số phiếu/Mã:"));
        referenceField = new JTextField(10);
        panel.add(referenceField);

        panel.add(new JLabel("Nội dung:"));
        contentField = new JTextField(14);
        panel.add(contentField);

        JButton refreshButton = new JButton("Refresh (F5)", IconHelper.getIcon("Refresh.png", 16, 16));
        refreshButton.setBackground(Color.WHITE);
        refreshButton.addActionListener(e -> refreshAll());
        panel.add(refreshButton);

        JButton deleteButton = new JButton("Xóa lưu vết", IconHelper.getIcon("Cancelled.png", 16, 16));
        deleteButton.setForeground(new Color(180, 25, 25));
        deleteButton.setBackground(Color.WHITE);
        boolean isAdmin = AuthService.getCurrentUser() != null && AuthService.getCurrentUser().isAdminUser();
        deleteButton.setEnabled(isAdmin);
        if (!isAdmin) deleteButton.setToolTipText("Chỉ tài khoản quản trị được phép xóa lưu vết");
        deleteButton.addActionListener(e -> deleteSelectedLogs());
        panel.add(deleteButton);

        referenceField.addActionListener(e -> refreshData());
        contentField.addActionListener(e -> refreshData());
        userCombo.addActionListener(e -> {
            if (!loadingUsers && userCombo.getItemCount() > 0) refreshData();
        });
        return panel;
    }

    private JComponent createTable() {
        String[] columns = {"", "Giờ", "Ngày", "Hành động", "Chức năng", "Số phiếu/Mã",
                "Nội dung", "Tài khoản", "Thiết bị"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(23);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(225, 238, 252));
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(38);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(85);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(145);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(430);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(130);
        return new JScrollPane(table);
    }

    private void loadUsers() {
        loadingUsers = true;
        try {
            String selected = userCombo.getSelectedItem() != null ? userCombo.getSelectedItem().toString() : null;
            userCombo.removeAllItems();
            userCombo.addItem("Tất cả tài khoản");
            for (String userName : activityLogDAO.getUserNames()) userCombo.addItem(userName);
            if (selected != null) userCombo.setSelectedItem(selected);
        } finally {
            loadingUsers = false;
        }
    }

    private void refreshAll() {
        loadUsers();
        refreshData();
    }

    public void refreshData() {
        Date selectedDate = datePicker.getDate();
        Date from = null;
        Date to = null;
        if (selectedDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(selectedDate);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            from = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            to = calendar.getTime();
        }

        String selectedUser = userCombo.getSelectedIndex() > 0
                ? String.valueOf(userCombo.getSelectedItem()) : null;
        currentLogs = activityLogDAO.search(from, to, selectedUser,
                referenceField.getText(), contentField.getText());

        tableModel.setRowCount(0);
        int index = 1;
        for (ActivityLog log : currentLogs) {
            tableModel.addRow(new Object[]{index++, timeFormat.format(log.getEventTime()),
                    dateFormat.format(log.getEventTime()), log.getAction(), log.getModule(),
                    log.getReferenceNo(), log.getContent(), log.getUserName(), log.getDeviceName()});
        }
    }

    private void deleteSelectedLogs() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một dòng lưu vết cần xóa!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa " + selectedRows.length + " dòng lưu vết đã chọn không?",
                "Xác nhận xóa lưu vết", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        List<String> ids = new ArrayList<>();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < currentLogs.size()) ids.add(currentLogs.get(modelRow).getId());
        }
        int deleted = activityLogDAO.deleteByIds(ids);
        JOptionPane.showMessageDialog(this, "Đã xóa " + deleted + " dòng lưu vết.",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        refreshData();
    }
}
