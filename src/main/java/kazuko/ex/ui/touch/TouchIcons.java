package kazuko.ex.ui.touch;

import javax.swing.*;
import java.awt.*;

/**
 * Các Icon vẽ vector chuẩn hiển thị sắc nét trên mọi màn hình cảm ứng,
 * không phụ thuộc vào font chữ hệ thống và không bao giờ bị lỗi ô vuông [] hoặc thiếu glyph.
 */
public class TouchIcons {

    public static Icon keyboard(int width, int height, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color != null ? color : Color.WHITE);

                int padX = 2;
                int padY = 4;
                int w = width - padX * 2;
                int h = height - padY * 2;
                int startX = x + padX;
                int startY = y + padY;

                // Khung bàn phím bên ngoài
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(startX, startY, w, h, 6, 6);

                // Các hàng phím nhỏ bên trong
                int keyW = (w - 12) / 4;
                int keyH = (h - 10) / 3;

                // Hàng 1
                for (int i = 0; i < 4; i++) {
                    g2.fillRect(startX + 3 + i * (keyW + 2), startY + 3, keyW, keyH);
                }
                // Hàng 2
                for (int i = 0; i < 4; i++) {
                    g2.fillRect(startX + 3 + i * (keyW + 2), startY + 4 + keyH, keyW, keyH);
                }
                // Hàng 3: Phím Space dài ở giữa
                int spaceW = w - 10;
                g2.fillRect(startX + 5, startY + 5 + keyH * 2, spaceW, keyH - 1);

                g2.dispose();
            }

            @Override public int getIconWidth() { return width; }
            @Override public int getIconHeight() { return height; }
        };
    }

    public static Icon calculator(int width, int height, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color != null ? color : Color.WHITE);

                int padX = 4;
                int padY = 2;
                int w = width - padX * 2;
                int h = height - padY * 2;
                int startX = x + padX;
                int startY = y + padY;

                // Khung máy tính
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(startX, startY, w, h, 6, 6);

                // Màn hình LCD nhỏ ở trên
                int lcdH = (h / 4);
                g2.drawRect(startX + 3, startY + 3, w - 6, lcdH);
                g2.fillRect(startX + 4, startY + 4, w - 8, lcdH - 1);

                // Lưới nút 3x3 ở dưới
                int gridTop = startY + lcdH + 6;
                int btnW = (w - 8) / 3;
                int btnH = (h - lcdH - 11) / 3;

                for (int r = 0; r < 3; r++) {
                    for (int col = 0; col < 3; col++) {
                        g2.fillRect(startX + 3 + col * (btnW + 1), gridTop + r * (btnH + 1), btnW, btnH);
                    }
                }

                g2.dispose();
            }

            @Override public int getIconWidth() { return width; }
            @Override public int getIconHeight() { return height; }
        };
    }

    public static Icon search(int width, int height, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color != null ? color : Color.WHITE);

                int d = Math.min(width, height) - 10;
                int cx = x + 4;
                int cy = y + 4;

                // Vòng tròn kính lúp
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(cx, cy, d, d);

                // Cán kính lúp
                int handleStartX = cx + (int) (d * 0.8);
                int handleStartY = cy + (int) (d * 0.8);
                int handleEndX = x + width - 4;
                int handleEndY = y + height - 4;
                g2.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(handleStartX, handleStartY, handleEndX, handleEndY);

                g2.dispose();
            }

            @Override public int getIconWidth() { return width; }
            @Override public int getIconHeight() { return height; }
        };
    }

    public static Icon downArrow(int width, int height, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color != null ? color : Color.WHITE);

                int size = Math.min(width, height) - 10;
                int cx = x + (width - size) / 2;
                int cy = y + (height - size) / 2;

                int[] xPoints = {cx, cx + size, cx + size / 2};
                int[] yPoints = {cy + 2, cy + 2, cy + size - 2};
                g2.fillPolygon(xPoints, yPoints, 3);

                g2.dispose();
            }

            @Override public int getIconWidth() { return width; }
            @Override public int getIconHeight() { return height; }
        };
    }

    public static Icon gear(int width, int height, Color color) {
        ImageIcon resIcon = kazuko.ex.util.IconHelper.getIcon("btnConfig.Icon.png", width, height);
        if (resIcon != null && resIcon.getIconWidth() > 0) {
            return resIcon;
        }
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color != null ? color : Color.WHITE);

                int rOut = Math.min(width, height) / 2 - 2;
                int rIn = rOut * 6 / 10;
                int rHole = rOut * 3 / 10;
                int cx = x + width / 2;
                int cy = y + height / 2;

                // 8 teeth
                g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = cx + (int) (rIn * Math.cos(angle));
                    int y1 = cy + (int) (rIn * Math.sin(angle));
                    int x2 = cx + (int) (rOut * Math.cos(angle));
                    int y2 = cy + (int) (rOut * Math.sin(angle));
                    g2.drawLine(x1, y1, x2, y2);
                }

                // Center wheel
                g2.fillOval(cx - rIn, cy - rIn, rIn * 2, rIn * 2);

                // Inner hole
                g2.setColor(new Color(0, 90, 115));
                g2.fillOval(cx - rHole, cy - rHole, rHole * 2, rHole * 2);

                g2.dispose();
            }

            @Override public int getIconWidth() { return width; }
            @Override public int getIconHeight() { return height; }
        };
    }
}
