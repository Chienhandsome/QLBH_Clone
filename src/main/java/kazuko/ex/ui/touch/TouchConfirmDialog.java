package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Touch-sized replacement for confirmation option panes. */
final class TouchConfirmDialog extends JDialog {
    private boolean accepted;

    private TouchConfirmDialog(Window owner, String message) {
        super(owner, "Xác nhận", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(48, 115, 165));
        root.setBorder(new LineBorder(new Color(225, 238, 247), 2));

        JLabel title = new JLabel("XÁC NHẬN", SwingConstants.CENTER);
        title.setOpaque(true);
        title.setBackground(new Color(116, 158, 190));
        title.setForeground(Color.YELLOW);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setPreferredSize(new Dimension(0, 55));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(25, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(45, 42, 20, 42));
        JLabel question = new JLabel("?", SwingConstants.CENTER);
        question.setForeground(Color.WHITE);
        question.setFont(new Font("Segoe UI", Font.BOLD, 40));
        question.setBorder(new LineBorder(Color.WHITE, 3, true));
        question.setPreferredSize(new Dimension(70, 70));
        center.add(question, BorderLayout.WEST);
        JLabel text = new JLabel("<html>" + message.replace("\n", "<br>") + "</html>");
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 23));
        center.add(text, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(0, 0, 0, 14));
        JButton yes = actionButton("YES", new Color(0, 145, 0));
        yes.addActionListener(e -> {
            accepted = true;
            dispose();
        });
        JButton no = actionButton("NO", new Color(165, 5, 12));
        no.addActionListener(e -> dispose());
        actions.add(yes);
        actions.add(no);
        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(no);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(700, 315);
        setLocationRelativeTo(owner);
    }

    private JButton actionButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 21));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(165, 72));
        button.setBorder(new LineBorder(new Color(210, 230, 240), 2, true));
        return button;
    }

    static boolean show(Window owner, String message) {
        TouchConfirmDialog dialog = new TouchConfirmDialog(owner, message);
        dialog.setVisible(true);
        return dialog.accepted;
    }
}
