package kazuko.ex.ui.dathang;

import kazuko.ex.model.system.STemplate;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class InLuoiDatHangDialog extends JDialog {

    private JTextField txtTieuDe;
    private JTextField txtGhiChu;
    private JList<STemplate> listMauIn;
    private DefaultListModel<STemplate> listMauInModel;

    private JCheckBox chkNgay;
    private JCheckBox chkSoPhieu;
    private JCheckBox chkTenKhach;
    private JCheckBox chkDiaChi;
    private JCheckBox chkDienThoai;
    private JCheckBox chkTienHang;
    private JCheckBox chkTienGiamGia;
    private JCheckBox chkTongCong;
    private JCheckBox chkInCotSTT;

    public InLuoiDatHangDialog(Window parent) {
        super(parent, "In lưới", ModalityType.APPLICATION_MODAL);
        initComponents();
        loadTemplates();
    }

    private void initComponents() {
        setSize(430, 520);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        setResizable(false);

        // Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlHeader.setBackground(new Color(240, 245, 252));
        pnlHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 235)),
                new EmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblHeaderIcon = new JLabel(IconHelper.getIcon("Printer.png", 28, 28));
        JLabel lblHeaderTitle = new JLabel("Cấu hình trang in đơn đặt hàng");
        lblHeaderTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderTitle.setForeground(new Color(30, 60, 110));

        pnlHeader.add(lblHeaderIcon);
        pnlHeader.add(lblHeaderTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Main Content Panel
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(new Color(245, 248, 253));
        pnlMain.setBorder(new EmptyBorder(8, 10, 8, 10));

        JPanel pnlThietLap = new JPanel(new GridBagLayout());
        pnlThietLap.setBackground(Color.WHITE);
        pnlThietLap.setBorder(new CompoundBorder(
                new TitledBorder(new LineBorder(new Color(185, 205, 230), 1), "Thiết lập in", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(40, 70, 130)),
                new EmptyBorder(6, 8, 6, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Row 0: Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTieuDe = new JLabel("Tiêu đề:");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblTieuDe, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtTieuDe = new JTextField("DANH SÁCH KHÁCH ĐẶT HÀNG");
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(txtTieuDe, gbc);

        // Row 1: Ghi chú
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblGhiChu, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtGhiChu = new JTextField("");
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(txtGhiChu, gbc);

        // Row 2: Chọn mẫu in
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel lblMauIn = new JLabel("Chọn mẫu in:");
        lblMauIn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlThietLap.add(lblMauIn, gbc);

        // Row 3: List mẫu in
        gbc.gridy = 3; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.6;
        listMauInModel = new DefaultListModel<>();
        listMauIn = new JList<>(listMauInModel);
        listMauIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listMauIn.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spMauIn = new JScrollPane(listMauIn);
        spMauIn.setPreferredSize(new Dimension(380, 100));
        pnlThietLap.add(spMauIn, gbc);

        // Row 4: Checkboxes chọn cột in
        gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        JPanel pnlCotIn = new JPanel(new GridLayout(3, 3, 4, 4));
        pnlCotIn.setOpaque(false);
        pnlCotIn.setBorder(new TitledBorder(new LineBorder(new Color(200, 215, 235), 1), "Các cột in"));

        chkInCotSTT = new JCheckBox("In cột STT", true);
        chkNgay = new JCheckBox("Ngày", true);
        chkSoPhieu = new JCheckBox("Số phiếu", true);
        chkTenKhach = new JCheckBox("Tên khách", true);
        chkDiaChi = new JCheckBox("Địa chỉ", true);
        chkDienThoai = new JCheckBox("Điện thoại", true);
        chkTienHang = new JCheckBox("Tiền hàng", true);
        chkTienGiamGia = new JCheckBox("Tiền giảm", true);
        chkTongCong = new JCheckBox("Tổng cộng", true);

        pnlCotIn.add(chkInCotSTT);
        pnlCotIn.add(chkNgay);
        pnlCotIn.add(chkSoPhieu);
        pnlCotIn.add(chkTenKhach);
        pnlCotIn.add(chkDiaChi);
        pnlCotIn.add(chkDienThoai);
        pnlCotIn.add(chkTienHang);
        pnlCotIn.add(chkTienGiamGia);
        pnlCotIn.add(chkTongCong);
        pnlThietLap.add(pnlCotIn, gbc);

        pnlMain.add(pnlThietLap, BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);

        // Footer Actions
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        pnlFooter.setBackground(new Color(240, 245, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 215, 235)));

        JButton btnXem = new JButton("Xem", IconHelper.getIcon("preview.png", 16, 16));
        JButton btnIn = new JButton("In", IconHelper.getIcon("Printer.png", 16, 16));
        JButton btnThoat = new JButton("Thoát", IconHelper.getIcon("Exit.png", 16, 16));

        btnXem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnXem.addActionListener(e -> onPreview());
        btnIn.addActionListener(e -> onPrint());
        btnThoat.addActionListener(e -> dispose());

        pnlFooter.add(btnXem);
        pnlFooter.add(btnIn);
        pnlFooter.add(btnThoat);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void loadTemplates() {
        listMauInModel.clear();
        STemplate t1 = new STemplate();
        t1.setName("Mẫu chuẩn A4 - Khách đặt hàng");
        listMauInModel.addElement(t1);

        STemplate t2 = new STemplate();
        t2.setName("Mẫu rút gọn - Khách đặt hàng");
        listMauInModel.addElement(t2);

        listMauIn.setSelectedIndex(0);
    }

    private void onPreview() {
        JOptionPane.showMessageDialog(this, "Đang hiển thị bản xem trước mẫu in đơn đặt hàng...", "Xem trước", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onPrint() {
        JOptionPane.showMessageDialog(this, "Đang gửi lệnh in tới máy in mặc định...", "In", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
