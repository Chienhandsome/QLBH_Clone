package kazuko.ex.ui.common;

import kazuko.ex.config.DatabaseConnection;
import kazuko.ex.model.system.SUser;
import kazuko.ex.util.AppColors;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.File;

public class StatusPanel extends JPanel {
    private final JLabel lblTrial;
    private final JLabel lblDatabase;
    private final JLabel lblUser;
    private final JLabel lblInputMethod;
    private final JComboBox<String> cbInputMethod;

    public StatusPanel(SUser user) {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, AppColors.BORDER_GRAY),
                new EmptyBorder(2, 10, 2, 10)
        ));
        setBackground(AppColors.STATUS_BAR_BG);

        // Phía trái: Bản quyền / Thông báo
        lblTrial = new JLabel("Phiên bản dùng thử, hết hạn trong 20 ngày.");
        lblTrial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblTrial, BorderLayout.WEST);

        // Phía phải: Thông tin CSDL, Tài khoản, Bộ gõ tiếng Việt (JComboBox)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        String dbPath = DatabaseConnection.getConfig().getDatabasePath();
        String dbName = "DEMO.FDB";
        if (dbPath != null && !dbPath.isEmpty()) {
            File f = new File(dbPath);
            dbName = f.getName().toUpperCase();
        }

        lblDatabase = new JLabel("CSDL: " + dbName);
        lblUser = new JLabel("Tài khoản: " + (user != null ? user.getName() : "Administrator"));
        lblInputMethod = new JLabel("Gõ tiếng Việt");
        cbInputMethod = new JComboBox<>(new String[]{"Telex", "VNI"});

        lblDatabase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInputMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbInputMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbInputMethod.setBackground(Color.WHITE);
        cbInputMethod.setPreferredSize(new Dimension(75, 22));
        cbInputMethod.setFocusable(false);

        rightPanel.add(lblDatabase);
        rightPanel.add(new JSeparator(JSeparator.VERTICAL));
        rightPanel.add(lblUser);
        rightPanel.add(new JSeparator(JSeparator.VERTICAL));
        rightPanel.add(lblInputMethod);
        rightPanel.add(cbInputMethod);

        add(rightPanel, BorderLayout.EAST);
    }
}
