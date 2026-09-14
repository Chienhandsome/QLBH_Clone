package kazuko.ex.ui.system;

import kazuko.ex.util.IconHelper;
import kazuko.ex.util.VoucherNumberGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

public class CauHinhCachSinhDialog extends JDialog {
    private final String voucherType;
    private final String initialPattern;
    private final Consumer<String> onSaveCallback;

    private JTextField txtPattern;
    private JLabel lblExplanation;

    public CauHinhCachSinhDialog(Dialog parent, String voucherType, String initialPattern, Consumer<String> onSaveCallback) {
        super(parent, "Cấu hình cách sinh " + voucherType, true);
        this.voucherType = voucherType;
        this.initialPattern = initialPattern != null ? initialPattern : "";
        this.onSaveCallback = onSaveCallback;

        initUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(580, 420));
        setResizable(false);
        getContentPane().setBackground(new Color(240, 245, 252));

        // 1. Top Header
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new MatteBorder(0, 0, 1, 0, new Color(205, 220, 240)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("Tools.png", 24, 24));
        JLabel lblTitle = new JLabel("Thiết lập cách sinh " + voucherType);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(25, 45, 85));
        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Center Content
        JPanel pnlContent = new JPanel(null);
        pnlContent.setOpaque(false);

        JLabel lblMau = new JLabel("Mẫu:");
        lblMau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMau.setBounds(20, 15, 45, 24);
        pnlContent.add(lblMau);

        txtPattern = new JTextField(initialPattern);
        txtPattern.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtPattern.setBounds(65, 15, 180, 24);
        pnlContent.add(txtPattern);

        lblExplanation = new JLabel();
        lblExplanation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblExplanation.setForeground(new Color(40, 40, 40));
        lblExplanation.setBounds(255, 15, 310, 24);
        pnlContent.add(lblExplanation);

        // Instructions text area
        JTextArea txtGuide = new JTextArea();
        txtGuide.setEditable(false);
        txtGuide.setOpaque(false);
        txtGuide.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGuide.setLineWrap(true);
        txtGuide.setWrapStyleWord(true);
        txtGuide.setText(
                "Sử dụng (*) dành cho phần số tự động tăng do hệ thống sinh ra, độ dài do số lượng dấu sao quy định, tối đa là 9.\n" +
                "Ví dụ:\n" +
                "HD(**) gồm 99 số chạy từ HD01, HD02 đến HD99\n" +
                "HD(***) gồm 999 số chạy từ HD001, HD002 đến HD999\n\n" +
                "Sử dụng (MM) để lấy tháng hiện tại\n" +
                "Sử dụng (yy) để lấy năm hiện tại (2 số cuối của năm)\n" +
                "Sử dụng (yyyy) để lấy năm hiện tại (cả 4 số)\n" +
                "Sử dụng (dd) để lấy ngày hiện tại\n\n" +
                "Nếu số có chứa năm, (*) sẽ chạy lại từ 01 ở đầu năm mới\n" +
                "Nếu số có chứa tháng, (*) sẽ chạy lại từ 01 ở đầu tháng mới\n" +
                "Nếu số có chứa ngày, (*) sẽ chạy lại từ 01 ở đầu ngày mới"
        );
        txtGuide.setBounds(65, 55, 490, 250);
        pnlContent.add(txtGuide);

        add(pnlContent, BorderLayout.CENTER);

        // Dynamic explanation listener
        txtPattern.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateExplanation(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateExplanation(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateExplanation(); }
        });
        updateExplanation();

        // 3. Bottom Footer
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlFooter.setBackground(new Color(235, 242, 250));
        pnlFooter.setBorder(new MatteBorder(1, 0, 0, 0, new Color(205, 220, 240)));

        JButton btnSave = new JButton("Ghi dữ liệu");
        btnSave.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSave.setPreferredSize(new Dimension(95, 26));
        btnSave.setBackground(Color.WHITE);
        btnSave.addActionListener(e -> onSave());

        JButton btnCancel = new JButton("Thoát");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.setPreferredSize(new Dimension(85, 26));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.addActionListener(e -> dispose());

        pnlFooter.add(btnSave);
        pnlFooter.add(btnCancel);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void updateExplanation() {
        String pattern = txtPattern.getText().trim();
        if (pattern.isEmpty()) {
            lblExplanation.setText("(Chưa nhập mẫu)");
            return;
        }

        String error = VoucherNumberGenerator.validate(pattern);
        if (error != null) {
            lblExplanation.setText(error);
            return;
        }

        int starCount = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '*') starCount++;
        }
        long total = (long) Math.pow(10, starCount) - 1;
        VoucherNumberGenerator.CompiledPattern compiled = VoucherNumberGenerator.compile(pattern, new java.util.Date());

        lblExplanation.setText("Có " + total + " chuỗi từ " + compiled.format(1) + " đến " + compiled.format(total));
    }

    private void onSave() {
        String pattern = txtPattern.getText().trim();
        if (!("Bảng lương".equals(voucherType) && pattern.isEmpty())) {
            String error = VoucherNumberGenerator.validate(pattern);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Mẫu số phiếu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                txtPattern.requestFocus();
                return;
            }
        }
        if (onSaveCallback != null) {
            onSaveCallback.accept(pattern);
        }
        dispose();
    }
}
