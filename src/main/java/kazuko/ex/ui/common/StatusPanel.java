package kazuko.ex.ui.common;

import kazuko.ex.model.system.SUser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class StatusPanel extends JPanel {
    private final JLabel lblTrial;
    private final JLabel lblDatabase;
    private final JLabel lblUser;
    private final JLabel lblInputMethod;

    public StatusPanel(SUser user) {
        setLayout(new BorderLayout());
        setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
                new EmptyBorder(4, 10, 4, 10)
        ));
        setBackground(new Color(245, 247, 250));

        // Phía trái: Bản quyền / Thông báo
        lblTrial = new JLabel("⚠️ Phiên bản đầy đủ - Đang hoạt động");
        lblTrial.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(lblTrial, BorderLayout.WEST);

        // Phía phải: Thông tin CSDL, Tài khoản, Bộ gõ tiếng Việt (giống ảnh 1)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);

        lblDatabase = new JLabel("🗄️ CSDL: DEMO.FDB");
        lblUser = new JLabel("👤 Tài khoản: " + (user != null ? user.getName() : "Admin"));
        lblInputMethod = new JLabel("☑️ Gõ tiếng Việt: Telex");

        lblDatabase.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInputMethod.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        rightPanel.add(lblDatabase);
        rightPanel.add(new JSeparator(JSeparator.VERTICAL));
        rightPanel.add(lblUser);
        rightPanel.add(new JSeparator(JSeparator.VERTICAL));
        rightPanel.add(lblInputMethod);

        add(rightPanel, BorderLayout.EAST);
    }
}
