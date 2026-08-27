package kazuko.ex.ui.baogia;

import kazuko.ex.dao.BaoGiaDAO;
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

public class InLuoiBaoGiaDialog extends JDialog {

    private final BaoGiaDAO baoGiaDAO = new BaoGiaDAO();
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

    public InLuoiBaoGiaDialog(Window parent) {
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
        JLabel lblHeaderTitle = new JLabel("Cấu hình trang in");
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
                new TitledBorder(new LineBorder(new Color(255, 180, 80), 1), "Thiết lập", TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12), new Color(180, 90, 0)),
                new EmptyBorder(8, 8, 8, 8)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // 1. Tiêu đề
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblTieuDe = new JLabel("Tiêu đề:");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblTieuDe, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtTieuDe = new JTextField("Báo giá");
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(txtTieuDe, gbc);

        // 2. Ghi chú
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblGhiChu = new JLabel("Ghi chú:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblGhiChu, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtGhiChu = new JTextField("");
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(txtGhiChu, gbc);

        // 3. Mẫu in
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblMauIn = new JLabel("Mẫu in:");
        lblMauIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblMauIn, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.weighty = 0.4; gbc.fill = GridBagConstraints.BOTH;
        listMauInModel = new DefaultListModel<>();
        listMauIn = new JList<>(listMauInModel);
        listMauIn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        listMauIn.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof STemplate) {
                    STemplate t = (STemplate) value;
                    l.setText(t.getName());
                    if (t.getName() != null && t.getName().contains("80")) {
                        l.setIcon(IconHelper.getIcon("table_excel.png", 16, 16));
                    } else {
                        l.setIcon(IconHelper.getIcon("page_white_text.png", 16, 16));
                    }
                }
                return l;
            }
        });
        JScrollPane scrollMauIn = new JScrollPane(listMauIn);
        scrollMauIn.setPreferredSize(new Dimension(240, 85));
        pnlThietLap.add(scrollMauIn, gbc);

        // 4. Cột hiển thị
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.weighty = 0; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.fill = GridBagConstraints.NONE;
        JLabel lblCot = new JLabel("Cột hiển thị");
        lblCot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlThietLap.add(lblCot, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; gbc.weighty = 0.6; gbc.fill = GridBagConstraints.BOTH;
        JPanel pnlCheckColumns = new JPanel(new GridLayout(8, 1, 2, 2));
        pnlCheckColumns.setBackground(Color.WHITE);
        pnlCheckColumns.setBorder(new LineBorder(new Color(210, 220, 235), 1));

        chkNgay = createColumnCheckbox("Ngày", true);
        chkSoPhieu = createColumnCheckbox("Số phiếu", true);
        chkTenKhach = createColumnCheckbox("Tên khách", true);
        chkDiaChi = createColumnCheckbox("Địa chỉ", true);
        chkDienThoai = createColumnCheckbox("Điện thoại", true);
        chkTienHang = createColumnCheckbox("Tiền hàng", true);
        chkTienGiamGia = createColumnCheckbox("Tiền giảm giá", true);
        chkTongCong = createColumnCheckbox("Tổng cộng", true);

        pnlCheckColumns.add(chkNgay);
        pnlCheckColumns.add(chkSoPhieu);
        pnlCheckColumns.add(chkTenKhach);
        pnlCheckColumns.add(chkDiaChi);
        pnlCheckColumns.add(chkDienThoai);
        pnlCheckColumns.add(chkTienHang);
        pnlCheckColumns.add(chkTienGiamGia);
        pnlCheckColumns.add(chkTongCong);

        JScrollPane scrollCols = new JScrollPane(pnlCheckColumns);
        scrollCols.setPreferredSize(new Dimension(240, 130));
        pnlThietLap.add(scrollCols, gbc);

        // 5. In cột số thứ tự
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        chkInCotSTT = new JCheckBox("In cột số thứ tự", true);
        chkInCotSTT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkInCotSTT.setOpaque(false);
        pnlThietLap.add(chkInCotSTT, gbc);

        pnlMain.add(pnlThietLap, BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);

        // Bottom Button Bar
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlFooter.setBackground(new Color(240, 245, 252));
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 225, 240)));

        JButton btnHienThi = new JButton("Hiển thị");
        btnHienThi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHienThi.setPreferredSize(new Dimension(85, 28));
        btnHienThi.addActionListener(e -> {
            STemplate sel = listMauIn.getSelectedValue();
            String templateName = (sel != null) ? sel.getName() : "Mặc định";
            JOptionPane.showMessageDialog(this,
                    "Đang nạp cấu hình trang in báo giá:\n- Tiêu đề: " + txtTieuDe.getText() +
                            "\n- Mẫu in: " + templateName +
                            "\n- In STT: " + (chkInCotSTT.isSelected() ? "Có" : "Không") +
                            "\n(Chức năng in FastReport/Jasper sẽ được nạp theo mẫu đã chọn)",
                    "Xem trước trang in", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnThoat = new JButton("Thoát");
        btnThoat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThoat.setPreferredSize(new Dimension(85, 28));
        btnThoat.addActionListener(e -> dispose());

        pnlFooter.add(btnHienThi);
        pnlFooter.add(btnThoat);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JCheckBox createColumnCheckbox(String text, boolean selected) {
        JCheckBox chk = new JCheckBox(text, selected);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chk.setBackground(Color.WHITE);
        return chk;
    }

    private void loadTemplates() {
        listMauInModel.clear();
        List<STemplate> templates = baoGiaDAO.getBaoGiaPrintTemplates();
        for (STemplate t : templates) {
            listMauInModel.addElement(t);
        }
        if (!templates.isEmpty()) {
            listMauIn.setSelectedIndex(0);
        }
    }
}
