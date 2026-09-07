package kazuko.ex.ui.congno;

import kazuko.ex.dao.CongNoKhachHangDAO;
import kazuko.ex.model.congno.CongNoKhachHangItem;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.Date;

public class ThanhToanCongNoKhachDialog extends JDialog {

    private final CongNoKhachHangDAO congNoDAO = new CongNoKhachHangDAO();
    private final CongNoKhachHangItem customer;
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private DatePickerField dpkNgayThu;
    private JTextField txtSoTien;
    private JTextField txtLyDoThu;
    private JComboBox<String> cbHinhThuc;
    private JTextField txtGhiChu;

    private JButton btnSave;
    private JButton btnCancel;
    private boolean isSuccess = false;

    public ThanhToanCongNoKhachDialog(Window parent, CongNoKhachHangItem customer) {
        super(parent, "PHIẾU THU TIỀN NỢ KHÁCH HÀNG", ModalityType.APPLICATION_MODAL);
        this.customer = customer;

        initComponents();
        setupEvents();
        setupKeyBindings();

        setSize(520, 380);
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(0, 12));
        contentPane.setBackground(new Color(235, 242, 252));
        contentPane.setBorder(new EmptyBorder(14, 16, 14, 16));
        setContentPane(contentPane);

        // Header Panel
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlHeader.setOpaque(false);
        JLabel lblIcon = new JLabel(IconHelper.getIcon("money_add.png", 28, 28));
        JLabel lblTitle = new JLabel("Thu tiền nợ khách hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(15, 50, 100));
        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);

        // Form Center
        JPanel pnlCenter = new JPanel(new GridBagLayout());
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new CompoundBorder(
                new LineBorder(new Color(185, 205, 230), 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Khách hàng
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.25;
        JLabel lblKh = new JLabel("Khách hàng:");
        lblKh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblKh, gbc);

        gbc.gridx = 1; gbc.weightx = 0.75;
        String khInfo = customer.getTenKhach() + " (" + (customer.getMaKhach() != null ? customer.getMaKhach() : "") + ")";
        JLabel lblKhVal = new JLabel(khInfo);
        lblKhVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKhVal.setForeground(new Color(20, 40, 80));
        pnlCenter.add(lblKhVal, gbc);

        // Row 1: Số tiền còn nợ
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblNo = new JLabel("Số tiền còn nợ:");
        lblNo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblNo, gbc);

        gbc.gridx = 1;
        JLabel lblNoVal = new JLabel(df.format(customer.getConNo()) + " đ");
        lblNoVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNoVal.setForeground(new Color(180, 20, 20));
        pnlCenter.add(lblNoVal, gbc);

        // Row 2: Ngày thu
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblNgay = new JLabel("Ngày thu:");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblNgay, gbc);

        gbc.gridx = 1;
        dpkNgayThu = new DatePickerField();
        dpkNgayThu.setDate(new Date());
        dpkNgayThu.setPreferredSize(new Dimension(140, 24));
        pnlCenter.add(dpkNgayThu, gbc);

        // Row 3: Số tiền thu
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblTien = new JLabel("Số tiền thu (*):");
        lblTien.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTien.setForeground(new Color(15, 60, 130));
        pnlCenter.add(lblTien, gbc);

        gbc.gridx = 1;
        double defaultAmount = Math.max(0, customer.getConNo());
        txtSoTien = new JTextField(String.format("%.0f", defaultAmount));
        txtSoTien.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtSoTien.setForeground(new Color(0, 100, 40));
        txtSoTien.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(160, 190, 225), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        pnlCenter.add(txtSoTien, gbc);

        // Row 4: Hình thức
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblHt = new JLabel("Hình thức:");
        lblHt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblHt, gbc);

        gbc.gridx = 1;
        cbHinhThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản ngân hàng"});
        cbHinhThuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(cbHinhThuc, gbc);

        // Row 5: Lý do thu
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel lblLyDo = new JLabel("Lý do thu:");
        lblLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblLyDo, gbc);

        gbc.gridx = 1;
        txtLyDoThu = new JTextField("Thu nợ khách hàng " + customer.getTenKhach());
        txtLyDoThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtLyDoThu.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(160, 190, 225), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        pnlCenter.add(txtLyDoThu, gbc);

        // Row 6: Ghi chú
        gbc.gridx = 0; gbc.gridy = 6;
        JLabel lblNote = new JLabel("Ghi chú:");
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblNote, gbc);

        gbc.gridx = 1;
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(160, 190, 225), 1),
                new EmptyBorder(2, 6, 2, 6)
        ));
        pnlCenter.add(txtGhiChu, gbc);

        // Bottom Bar
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBottom.setOpaque(false);

        btnSave = new JButton("Lưu & Thu tiền", IconHelper.getIcon("money.png", 16, 16));
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(230, 245, 230));
        btnSave.setBorder(new CompoundBorder(new LineBorder(new Color(120, 180, 130), 1), new EmptyBorder(5, 14, 5, 14)));
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnCancel = new JButton("Bỏ qua");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setBorder(new CompoundBorder(new LineBorder(new Color(160, 190, 225), 1), new EmptyBorder(5, 14, 5, 14)));
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        pnlBottom.add(btnSave);
        pnlBottom.add(btnCancel);

        contentPane.add(pnlHeader, BorderLayout.NORTH);
        contentPane.add(pnlCenter, BorderLayout.CENTER);
        contentPane.add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        btnSave.addActionListener(e -> executeSave());
        btnCancel.addActionListener(e -> dispose());
    }

    private void executeSave() {
        double soTien;
        try {
            soTien = Double.parseDouble(txtSoTien.getText().trim().replace(",", ""));
            if (soTien <= 0) {
                JOptionPane.showMessageDialog(this, "Số tiền thu phải lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền thu không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date ngay = dpkNgayThu.getDate();
        String lyDo = txtLyDoThu.getText().trim();
        String ghiChu = txtGhiChu.getText().trim();
        String tkId = (cbHinhThuc.getSelectedIndex() == 1) ? "bank-default" : null;

        boolean ok = congNoDAO.insertPhieuThu(
                customer.getKhachHangId(),
                customer.getTenKhach(),
                customer.getDiaChi(),
                soTien,
                null,
                lyDo,
                ghiChu,
                tkId,
                ngay
        );

        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Thu tiền nợ thành công số tiền: " + df.format(soTien) + " đ cho khách hàng [" + customer.getTenKhach() + "]!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            isSuccess = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu thu nợ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupKeyBindings() {
        JRootPane rp = getRootPane();
        rp.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rp.registerKeyboardAction(e -> executeSave(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
