package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Hosts an existing desktop business panel inside the full-screen touch shell. */
final class TouchModulePanel extends JPanel {
    TouchModulePanel(String title, JPanel content, Runnable back) {
        setLayout(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(new Color(79, 139, 169));
        header.setBorder(new EmptyBorder(7, 10, 7, 10));
        JButton backButton = new JButton("◀  TỔNG QUAN");
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(0, 120, 130));
        backButton.setFocusPainted(false);
        backButton.setPreferredSize(new Dimension(210, 48));
        backButton.addActionListener(e -> back.run());
        header.add(backButton, BorderLayout.WEST);
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(heading, BorderLayout.CENTER);
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(210, 1));
        header.add(spacer, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }
}
