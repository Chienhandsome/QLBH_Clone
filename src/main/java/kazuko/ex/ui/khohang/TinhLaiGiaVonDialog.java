package kazuko.ex.ui.khohang;

import kazuko.ex.dao.MatHangDAO;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;

public class TinhLaiGiaVonDialog extends JDialog {

    private final MatHangDAO matHangDAO = new MatHangDAO();

    private JRadioButton rbBinhQuanCuoiKy;
    private JRadioButton rbBinhQuanGiaiDoan;
    private JRadioButton rbGiaNhapGanNhat;

    private JLabel lblTuNgay;
    private DatePickerField dpkTuNgay;
    private JLabel lblDenNgay;
    private DatePickerField dpkDenNgay;

    private JButton btnThucHien;
    private JButton btnThoat;

    public TinhLaiGiaVonDialog(Window parent) {
        super(parent, "TÍNH LẠI GIÁ VỐN", ModalityType.APPLICATION_MODAL);
        initComponents();
        setupEvents();
        setupKeyBindings();
        setSize(520, 310);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 10));
        contentPane.setBackground(new Color(214, 230, 248)); // Nền xanh nhạt chuẩn theo ảnh
        contentPane.setBorder(new EmptyBorder(16, 20, 16, 20));
        setContentPane(contentPane);

        // Header Panel: Icon $ + Tiêu đề
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlHeader.setOpaque(false);

        JLabel lblIcon = new JLabel(IconHelper.getIcon("money.png", 26, 26));
        JLabel lblTitle = new JLabel("Tính lại giá vốn hàng bán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 45, 90));
        lblTitle.setBorder(new EmptyBorder(0, 8, 0, 0));

        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);

        // Center Panel: Description + Group Title + Radio Options (Tất cả căn lề trái chuẩn xác)
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(1, 0, 1, 0);

        // Description label
        JLabel lblDesc1 = new JLabel("Chức năng này sử dụng để đánh giá lại hàng giá trị tồn kho của bạn và cho bạn biết một cách");
        JLabel lblDesc2 = new JLabel("tương đối thực tế nhất lợi nhuận của mình.");
        lblDesc1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc1.setForeground(new Color(30, 40, 55));
        lblDesc2.setForeground(new Color(30, 40, 55));

        gbc.gridy = 0;
        pnlCenter.add(lblDesc1, gbc);

        gbc.gridy = 1;
        pnlCenter.add(lblDesc2, gbc);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Lựa chọn phương pháp tính giá vốn");
        lblSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubtitle.setForeground(new Color(15, 45, 90));
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 0, 6, 0);
        pnlCenter.add(lblSubtitle, gbc);

        // Radios
        gbc.insets = new Insets(2, 0, 2, 0);

        rbBinhQuanCuoiKy = new JRadioButton("Bình quân cuối kỳ", true);
        rbBinhQuanCuoiKy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbBinhQuanCuoiKy.setOpaque(false);
        rbBinhQuanCuoiKy.setMargin(new Insets(0, 0, 0, 0));
        gbc.gridy = 3;
        pnlCenter.add(rbBinhQuanCuoiKy, gbc);

        // Panel chứa radio giai đoạn và các picker ngày (căn lề trái hoàn toàn)
        JPanel pnlGiaiDoan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlGiaiDoan.setOpaque(false);

        rbBinhQuanGiaiDoan = new JRadioButton("Bình quân lấy theo giai đoạn");
        rbBinhQuanGiaiDoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbBinhQuanGiaiDoan.setOpaque(false);
        rbBinhQuanGiaiDoan.setMargin(new Insets(0, 0, 0, 0));

        lblTuNgay = new JLabel("  Từ: ");
        lblTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTuNgay.setEnabled(false);

        dpkTuNgay = new DatePickerField();
        Calendar calTu = Calendar.getInstance();
        calTu.add(Calendar.DAY_OF_MONTH, -30);
        dpkTuNgay.setDate(calTu.getTime());
        dpkTuNgay.setEnabled(false);
        dpkTuNgay.setPreferredSize(new Dimension(110, 23));

        lblDenNgay = new JLabel("   Đến: ");
        lblDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDenNgay.setEnabled(false);

        dpkDenNgay = new DatePickerField();
        dpkDenNgay.setDate(new Date());
        dpkDenNgay.setEnabled(false);
        dpkDenNgay.setPreferredSize(new Dimension(110, 23));

        pnlGiaiDoan.add(rbBinhQuanGiaiDoan);
        pnlGiaiDoan.add(lblTuNgay);
        pnlGiaiDoan.add(dpkTuNgay);
        pnlGiaiDoan.add(lblDenNgay);
        pnlGiaiDoan.add(dpkDenNgay);

        gbc.gridy = 4;
        pnlCenter.add(pnlGiaiDoan, gbc);

        rbGiaNhapGanNhat = new JRadioButton("Giá nhập gần nhất");
        rbGiaNhapGanNhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rbGiaNhapGanNhat.setOpaque(false);
        rbGiaNhapGanNhat.setMargin(new Insets(0, 0, 0, 0));
        gbc.gridy = 5;
        pnlCenter.add(rbGiaNhapGanNhat, gbc);

        ButtonGroup group = new ButtonGroup();
        group.add(rbBinhQuanCuoiKy);
        group.add(rbBinhQuanGiaiDoan);
        group.add(rbGiaNhapGanNhat);

        // Bottom Button Bar
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setOpaque(false);

        btnThucHien = new JButton("Thực hiện");
        btnThucHien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThucHien.setBackground(Color.WHITE);
        btnThucHien.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 225), 1), new EmptyBorder(5, 16, 5, 16)));
        btnThucHien.setFocusPainted(false);
        btnThucHien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setBackground(Color.WHITE);
        btnThoat.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 225), 1), new EmptyBorder(5, 16, 5, 16)));
        btnThoat.setFocusPainted(false);
        btnThoat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlBottom.add(btnThucHien);
        pnlBottom.add(btnThoat);

        contentPane.add(pnlHeader, BorderLayout.NORTH);
        contentPane.add(pnlCenter, BorderLayout.CENTER);
        contentPane.add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        // Toggle date pickers based on radio selection
        rbBinhQuanCuoiKy.addActionListener(e -> updateDatePickersState());
        rbBinhQuanGiaiDoan.addActionListener(e -> updateDatePickersState());
        rbGiaNhapGanNhat.addActionListener(e -> updateDatePickersState());

        btnThucHien.addActionListener(e -> executeTinhGiaVon());
        btnThoat.addActionListener(e -> dispose());
    }

    private void updateDatePickersState() {
        boolean isGiaiDoan = rbBinhQuanGiaiDoan.isSelected();
        lblTuNgay.setEnabled(isGiaiDoan);
        dpkTuNgay.setEnabled(isGiaiDoan);
        lblDenNgay.setEnabled(isGiaiDoan);
        dpkDenNgay.setEnabled(isGiaiDoan);
    }

    private void executeTinhGiaVon() {
        int phuongPhap = 1;
        String tenPhuongPhap = "Bình quân cuối kỳ";
        Date tuNgay = null;
        Date denNgay = null;

        if (rbBinhQuanGiaiDoan.isSelected()) {
            phuongPhap = 2;
            tenPhuongPhap = "Bình quân theo giai đoạn";
            tuNgay = dpkTuNgay.getDate();
            denNgay = dpkDenNgay.getDate();
            if (tuNgay == null || denNgay == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khoảng thời gian hợp lệ (Từ ngày - Đến ngày)!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else if (rbGiaNhapGanNhat.isSelected()) {
            phuongPhap = 3;
            tenPhuongPhap = "Giá nhập gần nhất";
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn tính lại giá vốn hàng bán theo phương pháp:\n" +
                "[" + tenPhuongPhap + "] không?",
                "Xác nhận tính lại giá vốn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (conf != JOptionPane.YES_OPTION) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnThucHien.setEnabled(false);

        try {
            int count = matHangDAO.tinhLaiGiaVon(phuongPhap, tuNgay, denNgay);
            JOptionPane.showMessageDialog(this,
                    "Tính lại giá vốn thành công!\nĐã cập nhật giá vốn cho " + count + " mặt hàng.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi tính lại giá vốn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
            btnThucHien.setEnabled(true);
        }
    }

    private void setupKeyBindings() {
        JRootPane rp = getRootPane();
        rp.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> executeTinhGiaVon(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
