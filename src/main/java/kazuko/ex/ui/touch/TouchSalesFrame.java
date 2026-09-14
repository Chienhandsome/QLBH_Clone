package kazuko.ex.ui.touch;

import kazuko.ex.model.system.SUser;
import kazuko.ex.ui.hoadon.HoaDonBanHangPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Full-screen touch shell for the existing sales invoice workflow.
 *
 * The screen only supplies a touch-oriented presentation. All invoice state,
 * validation, pricing, persistence and reporting remain in
 * {@link HoaDonBanHangPanel} and its existing DAO collaborators.
 */
public final class TouchSalesFrame extends JFrame {

    public TouchSalesFrame(SUser user) {
        String username = user != null && user.getUsername() != null ? user.getUsername() : "";
        setTitle("TAP Shop TS" + (username.isBlank() ? "" : " - " + username));
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 720));
        setContentPane(new HoaDonBanHangPanel(true));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                int choice = JOptionPane.showConfirmDialog(TouchSalesFrame.this,
                        "Thoát màn hình bán hàng cảm ứng?", "TAP Shop TS",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) dispose();
            }
        });

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }
}
