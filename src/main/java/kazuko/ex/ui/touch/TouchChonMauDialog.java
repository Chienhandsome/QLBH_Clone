package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Hộp thoại chọn màu cảm ứng gồm 48 màu chuẩn (8 cột x 6 dòng). */
public class TouchChonMauDialog extends JDialog {
    private static final Color BG = new Color(0, 67, 86);
    private static final Color HEADER = new Color(79, 139, 169);
    private static final Color RED = new Color(155, 0, 0);

    private static final String[][] PALETTE_HEX = {
            {"#FFFFFF", "#FFD2D2", "#FFE6C7", "#FFFFD0", "#D6FCD2", "#D2FAFA", "#D6DCFE", "#FED3FC"},
            {"#EBEBEB", "#FFA0A0", "#FFCE85", "#FFFF99", "#A0FEA0", "#9EFAFE", "#A0B0FE", "#FE9EFE"},
            {"#C8C8C8", "#FF0000", "#FF8C00", "#FFFF00", "#00FF00", "#00FFFF", "#0000FF", "#FF00FF"},
            {"#888888", "#DC143C", "#D35400", "#CCCC00", "#00D400", "#00CED1", "#0000CD", "#CD00CD"},
            {"#484848", "#8B0000", "#994C00", "#8B8B00", "#008B00", "#008B8B", "#00008B", "#8B008B"},
            {"#1A1A1A", "#4A0D0D", "#663333", "#4A4A0D", "#0D4A0D", "#0D3A3A", "#0D0D4A", "#3A0D3A"}
    };

    private Color selectedColor = null;

    public TouchChonMauDialog(Window owner, Color currentColor) {
        super(owner, "Chọn màu", ModalityType.APPLICATION_MODAL);
        this.selectedColor = currentColor;
        setUndecorated(true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BG);
        root.setBorder(new LineBorder(HEADER, 4));

        // Header
        JLabel header = new JLabel("CHỌN MÀU", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(HEADER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setPreferredSize(new Dimension(0, 48));
        root.add(header, BorderLayout.NORTH);

        // Palette 8x6
        JPanel grid = new JPanel(new GridLayout(6, 8, 12, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 15, 10, 15));

        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 8; c++) {
                Color col = Color.decode(PALETTE_HEX[r][c]);
                JButton swatch = new JButton();
                swatch.setBackground(col);
                swatch.setOpaque(true);
                swatch.setFocusPainted(false);
                swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                swatch.setBorder(new LineBorder(new Color(220, 230, 235), 2, true));
                swatch.addActionListener(e -> {
                    selectedColor = col;
                    dispose();
                });
                grid.add(swatch);
            }
        }
        root.add(grid, BorderLayout.CENTER);

        // Footer với nút THOÁT
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        footer.setOpaque(false);
        JButton close = new JButton("THOÁT");
        close.setFont(new Font("Segoe UI", Font.BOLD, 20));
        close.setForeground(Color.WHITE);
        close.setBackground(RED);
        close.setFocusPainted(false);
        close.setBorder(new LineBorder(new Color(220, 230, 235), 2, true));
        close.setPreferredSize(new Dimension(170, 52));
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dispose());
        footer.add(close);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(880, 560);
        setLocationRelativeTo(owner);
    }

    public static Color show(Window owner, Color currentColor) {
        TouchChonMauDialog dialog = new TouchChonMauDialog(owner, currentColor);
        dialog.setVisible(true);
        return dialog.selectedColor;
    }

    public static Color showDialog(Window owner, Color currentColor) {
        return show(owner, currentColor);
    }

    public static void show(Window owner, Color currentColor, Consumer<Color> onSelected) {
        TouchChonMauDialog dialog = new TouchChonMauDialog(owner, currentColor);
        dialog.setVisible(true);
        if (dialog.selectedColor != null && onSelected != null) {
            onSelected.accept(dialog.selectedColor);
        }
    }
}
