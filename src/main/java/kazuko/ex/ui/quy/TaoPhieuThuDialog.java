package kazuko.ex.ui.quy;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.PhieuThuDAO;
import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
import kazuko.ex.model.quy.PhieuThu;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.common.DropdownWithActionBar;
import kazuko.ex.ui.danhmuc.LyDoThuChiDialog;
import kazuko.ex.ui.danhmuc.LyDoThuChiEditDialog;
import kazuko.ex.ui.danhmuc.TaiKhoanNganHangDialog;
import kazuko.ex.ui.danhmuc.TaiKhoanNganHangEditDialog;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TaoPhieuThuDialog extends JDialog {

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final TaiKhoanNganHangDAO taiKhoanNganHangDAO = new TaiKhoanNganHangDAO();

    private PhieuThu currentPhieuThu;
    private LyDoThuChi defaultReason;
    private final Runnable onDataSaved;

    // Form Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private DropdownWithActionBar<LyDoThuChi> cbPhanLoai;
    private JTextField txtLyDoThu;
    private JTextField txtChungTuGoc;
    private JComboBox<String> cbLoaiDoiTuong;
    private JTextField txtTenDoiTuong;
    private JTextField txtDiaChi;
    private JComboBox<NhanVien> cbNhanVien;
    private JComboBox<KhachHang> cbKhachHang;
    private JComboBox<String> cbNhaCungCap;
    private JTextField txtSoTien;
    private JCheckBox chkChuyenVaoTaiKhoan;
    private DropdownWithActionBar<TaiKhoanNganHang> cbTaiKhoanNganHang;
    private JComboBox<CuaHang> cbCuaHang;
    private JCheckBox chkKhongThayDoiCongNo;
    private JTextField txtGhiChu;

    // Action Buttons
    private JButton btnLuuVaIn;
    private JButton btnLuuVaXemIn;
    private JButton btnLuu;
    private JButton btnLuuVaMoi;
    private JButton btnLuuVaThoat;
    private JButton btnThoat;

    // Top Navigation Buttons
    private JButton btnTruoc;
    private JButton btnSau;
    private JButton btnTaoMoi;
    private JButton btnSaoChep;
    private JButton btnXoa;

    private static final Color BG_YELLOW = new Color(254, 253, 225);
    private static final Color BG_CYAN = new Color(230, 248, 255);
    private static final Color BG_FORM = new Color(215, 230, 248);

    public TaoPhieuThuDialog(Window parent) {
        this(parent, null, null, null);
    }

    public TaoPhieuThuDialog(Window parent, PhieuThu phieuThu, Runnable onDataSaved) {
        this(parent, phieuThu, null, onDataSaved);
    }

    public TaoPhieuThuDialog(Window parent, PhieuThu phieuThu, LyDoThuChi defaultReason, Runnable onDataSaved) {
        super(parent, phieuThu != null && phieuThu.getId() != null ? "PHIẾU THU - CẬP NHẬT" : "PHIẾU THU - THÊM MỚI", ModalityType.APPLICATION_MODAL);
        this.currentPhieuThu = phieuThu;
        this.defaultReason = defaultReason;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDropdownData();

        if (currentPhieuThu != null) {
            loadPhieuThuToForm(currentPhieuThu);
        } else {
            resetForNew();
        }

        setupHotkeys();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(650, 560);
        setPreferredSize(new Dimension(650, 560));
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_FORM);

        // 1. Top Header Banner & Navigation Toolbar
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setOpaque(false);

        // Header Title Banner
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnlHeader.setOpaque(false);
        JLabel lblIcon = new JLabel(IconHelper.getIcon("arrow-000-medium.png", 24, 24));
        JLabel lblTitle = new JLabel("Phiếu thu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(20, 50, 110));
        pnlHeader.add(lblIcon);
        pnlHeader.add(lblTitle);
        pnlNorth.add(pnlHeader, BorderLayout.NORTH);

        // Navigation Toolbar
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
        pnlToolbar.setBackground(new Color(225, 238, 252));
        pnlToolbar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(180, 205, 235)),
                new EmptyBorder(1, 4, 1, 4)
        ));

        JButton btnPhimTat = createToolbarButton("Phím tắt ▾", null);
        btnPhimTat.addActionListener(e -> showShortcutsPopup(btnPhimTat));

        btnTruoc = createToolbarButton("Trước (F10)", IconHelper.getIcon("navigation-180-frame.png", 14, 14));
        btnTruoc.addActionListener(e -> onNavigate(-1));

        btnSau = createToolbarButton("Sau (F11)", IconHelper.getIcon("navigation-000-frame.png", 14, 14));
        btnSau.addActionListener(e -> onNavigate(1));

        btnTaoMoi = createToolbarButton("Tạo mới", IconHelper.getIcon("btnNew.Image.png", 14, 14));
        btnTaoMoi.addActionListener(e -> resetForNew());

        btnSaoChep = createToolbarButton("Sao chép", IconHelper.getIcon("bookmark-import.png", 14, 14));
        btnSaoChep.addActionListener(e -> onSaoChep());

        btnXoa = createToolbarButton("Xóa", IconHelper.getIcon("cross.png", 14, 14));
        btnXoa.setForeground(new Color(180, 20, 20));
        btnXoa.addActionListener(e -> onDeleteCurrentPhieuThu());

        pnlToolbar.add(btnPhimTat);
        pnlToolbar.add(btnTruoc);
        pnlToolbar.add(btnSau);
        pnlToolbar.add(btnTaoMoi);
        pnlToolbar.add(btnSaoChep);
        pnlToolbar.add(btnXoa);

        pnlNorth.add(pnlToolbar, BorderLayout.SOUTH);
        add(pnlNorth, BorderLayout.NORTH);

        // 2. Center Absolute Layout Form Matching Screenshots
        JPanel pnlCenter = new JPanel(null);
        pnlCenter.setBackground(BG_FORM);
        pnlCenter.setBorder(new EmptyBorder(6, 10, 6, 10));

        int y = 8;
        int rowH = 24;
        int gapY = 28;

        // Row 1: Ngày, Số phiếu
        JLabel lblNgay = createLabel("Ngày", 10, y, 65, rowH);
        dpkNgay = new DatePickerField();
        dpkNgay.setBounds(80, y, 120, rowH);
        pnlCenter.add(lblNgay);
        pnlCenter.add(dpkNgay);

        JLabel lblSoPhieu = createLabel("Số phiếu", 215, y, 60, rowH);
        txtSoPhieu = new JTextField();
        txtSoPhieu.setBounds(280, y, 145, rowH);
        txtSoPhieu.setBackground(BG_CYAN);
        txtSoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlCenter.add(lblSoPhieu);
        pnlCenter.add(txtSoPhieu);

        // Row 2: Phân loại
        y += gapY;
        JLabel lblPhanLoai = createLabel("Phân loại", 10, y, 65, rowH);
        cbPhanLoai = new DropdownWithActionBar<>();
        cbPhanLoai.setBounds(80, y, 540, rowH);
        cbPhanLoai.setBackground(BG_YELLOW);
        cbPhanLoai.setDisplayConverter(LyDoThuChi::getName);
        cbPhanLoai.addActionListener(e -> {
            LyDoThuChi selected = cbPhanLoai.getSelectedItem();
            if (selected != null) {
                txtLyDoThu.setText(selected.getName());
            }
        });
        cbPhanLoai.setOnThem(() -> {
            LyDoThuChiEditDialog dlg = new LyDoThuChiEditDialog(this, null, lyDoThuChiDAO.getAllActive(), () -> loadPhanLoaiData());
            dlg.setVisible(true);
        });
        cbPhanLoai.setOnTai(this::loadPhanLoaiData);
        cbPhanLoai.setOnDanhMuc(() -> {
            LyDoThuChiDialog dlg = new LyDoThuChiDialog(this);
            dlg.setVisible(true);
            loadPhanLoaiData();
        });
        pnlCenter.add(lblPhanLoai);
        pnlCenter.add(cbPhanLoai);

        // Row 3: Lý do thu
        y += gapY;
        JLabel lblLyDoThu = createLabel("Lý do thu", 10, y, 65, rowH);
        txtLyDoThu = new JTextField();
        txtLyDoThu.setBounds(80, y, 540, rowH);
        txtLyDoThu.setBackground(BG_YELLOW);
        txtLyDoThu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblLyDoThu);
        pnlCenter.add(txtLyDoThu);

        // Row 4: Chứng từ gốc, Loại đối tượng
        y += gapY;
        JLabel lblChungTuGoc = createLabel("Chứng từ gốc", 10, y, 75, rowH);
        txtChungTuGoc = new JTextField();
        txtChungTuGoc.setBounds(80, y, 320, rowH);
        txtChungTuGoc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblChungTuGoc);
        pnlCenter.add(txtChungTuGoc);

        JLabel lblLoaiDT = createLabel("Loại đối tượng", 415, y, 80, rowH);
        cbLoaiDoiTuong = new JComboBox<>(new String[]{"Đối tượng khác", "Khách hàng", "Nhân viên", "Nhà cung cấp"});
        cbLoaiDoiTuong.setBounds(500, y, 120, rowH);
        cbLoaiDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLoaiDoiTuong.addActionListener(e -> updateDoiTuongComponentsState());
        pnlCenter.add(lblLoaiDT);
        pnlCenter.add(cbLoaiDoiTuong);

        // Row 5: Tên đối tượng
        y += gapY;
        JLabel lblTenDT = createLabel("Tên đối tượng", 10, y, 75, rowH);
        txtTenDoiTuong = new JTextField();
        txtTenDoiTuong.setBounds(80, y, 540, rowH);
        txtTenDoiTuong.setBackground(BG_YELLOW);
        txtTenDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblTenDT);
        pnlCenter.add(txtTenDoiTuong);

        // Row 6: Địa chỉ
        y += gapY;
        JLabel lblDiaChi = createLabel("Địa chỉ", 10, y, 65, rowH);
        txtDiaChi = new JTextField();
        txtDiaChi.setBounds(80, y, 540, rowH);
        txtDiaChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblDiaChi);
        pnlCenter.add(txtDiaChi);

        // Row 7: Nhân viên
        y += gapY;
        JLabel lblNhanVien = createLabel("Nhân viên", 10, y, 65, rowH);
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setBounds(80, y, 540, rowH);
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.addActionListener(e -> {
            if ("Nhân viên".equals(cbLoaiDoiTuong.getSelectedItem())) {
                NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
                if (nv != null) {
                    txtTenDoiTuong.setText(nv.getName() != null ? nv.getName() : "");
                    txtDiaChi.setText(nv.getDiaChi() != null ? nv.getDiaChi() : "");
                }
            }
        });
        pnlCenter.add(lblNhanVien);
        pnlCenter.add(cbNhanVien);

        // Row 8: Khách hàng
        y += gapY;
        JLabel lblKhachHang = createLabel("Khách hàng", 10, y, 65, rowH);
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setBounds(80, y, 540, rowH);
        cbKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhachHang.addActionListener(e -> {
            if ("Khách hàng".equals(cbLoaiDoiTuong.getSelectedItem())) {
                KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
                if (kh != null) {
                    txtTenDoiTuong.setText(kh.getName() != null ? kh.getName() : "");
                    txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                }
            }
        });
        pnlCenter.add(lblKhachHang);
        pnlCenter.add(cbKhachHang);

        // Row 9: Nhà cung cấp
        y += gapY;
        JLabel lblNCC = createLabel("Nhà cung cấp", 10, y, 75, rowH);
        cbNhaCungCap = new JComboBox<>(new String[]{"-- Chọn nhà cung cấp --", "Công ty TNHH Ánh Dương", "Nhà phân phối Sài Gòn", "Công ty CP Thiết Bị Số"});
        cbNhaCungCap.setBounds(80, y, 540, rowH);
        cbNhaCungCap.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhaCungCap.addActionListener(e -> {
            if ("Nhà cung cấp".equals(cbLoaiDoiTuong.getSelectedItem()) && cbNhaCungCap.getSelectedIndex() > 0) {
                txtTenDoiTuong.setText((String) cbNhaCungCap.getSelectedItem());
            }
        });
        pnlCenter.add(lblNCC);
        pnlCenter.add(cbNhaCungCap);

        // Row 10: Số tiền
        y += gapY;
        JLabel lblSoTien = createLabel("Số tiền:", 10, y, 65, rowH);
        txtSoTien = new JTextField("0");
        txtSoTien.setBounds(80, y, 140, rowH);
        txtSoTien.setBackground(BG_YELLOW);
        txtSoTien.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoTien.setHorizontalAlignment(SwingConstants.RIGHT);
        txtSoTien.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                formatMoneyInput();
            }
        });
        pnlCenter.add(lblSoTien);
        pnlCenter.add(txtSoTien);

        // Row 11: [ ] Chuyển vào tài khoản + Tài khoản ngân hàng Dropdown
        y += gapY;
        chkChuyenVaoTaiKhoan = new JCheckBox("Chuyển vào tài khoản");
        chkChuyenVaoTaiKhoan.setOpaque(false);
        chkChuyenVaoTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChuyenVaoTaiKhoan.setBounds(80, y, 145, rowH);
        chkChuyenVaoTaiKhoan.addActionListener(e -> updateTaiKhoanNHState());
        pnlCenter.add(chkChuyenVaoTaiKhoan);

        cbTaiKhoanNganHang = new DropdownWithActionBar<>();
        cbTaiKhoanNganHang.setBounds(230, y, 390, rowH);
        cbTaiKhoanNganHang.setDisplayConverter(TaiKhoanNganHang::getName);
        cbTaiKhoanNganHang.setOnThem(() -> {
            TaiKhoanNganHangEditDialog dlg = new TaiKhoanNganHangEditDialog(this, null, taiKhoanNganHangDAO.getAllActive(), () -> loadTaiKhoanNHData());
            dlg.setVisible(true);
        });
        cbTaiKhoanNganHang.setOnTai(this::loadTaiKhoanNHData);
        cbTaiKhoanNganHang.setOnDanhMuc(() -> {
            TaiKhoanNganHangDialog dlg = new TaiKhoanNganHangDialog(this);
            dlg.setVisible(true);
            loadTaiKhoanNHData();
        });
        pnlCenter.add(cbTaiKhoanNganHang);

        // Row 12: Cửa hàng
        y += gapY;
        JLabel lblCuaHang = createLabel("Cửa hàng", 10, y, 65, rowH);
        cbCuaHang = new JComboBox<>();
        cbCuaHang.setBounds(80, y, 540, rowH);
        cbCuaHang.setBackground(BG_YELLOW);
        cbCuaHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblCuaHang);
        pnlCenter.add(cbCuaHang);

        // Row 13: [ ] Không thay đổi công nợ
        y += gapY;
        chkKhongThayDoiCongNo = new JCheckBox("Không thay đổi công nợ");
        chkKhongThayDoiCongNo.setOpaque(false);
        chkKhongThayDoiCongNo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkKhongThayDoiCongNo.setBounds(80, y, 200, rowH);
        pnlCenter.add(chkKhongThayDoiCongNo);

        // Row 14: Ghi chú
        y += gapY;
        JLabel lblGhiChu = createLabel("Ghi chú", 10, y, 65, rowH);
        txtGhiChu = new JTextField();
        txtGhiChu.setBounds(80, y, 540, rowH);
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblGhiChu);
        pnlCenter.add(txtGhiChu);

        add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom Action Buttons Bar
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        pnlBottom.setBackground(new Color(230, 240, 252));
        pnlBottom.setBorder(new MatteBorder(1, 0, 0, 0, new Color(190, 210, 235)));

        btnLuuVaIn = createActionButton("Lưu & In", IconHelper.getIcon("Preview.png", 16, 16));
        btnLuuVaIn.addActionListener(e -> onSaveAndPrint(true));

        btnLuuVaXemIn = createActionButton("Lưu & Xem in", IconHelper.getIcon("report--pencil.png", 16, 16));
        btnLuuVaXemIn.addActionListener(e -> onSaveAndPrint(false));

        btnLuu = createActionButton("Lưu", IconHelper.getIcon("btnSave.Image.png", 16, 16));
        btnLuu.addActionListener(e -> onSave(false, false));

        btnLuuVaMoi = createActionButton("Lưu & Mới", null);
        btnLuuVaMoi.addActionListener(e -> onSave(true, false));

        btnLuuVaThoat = createActionButton("Lưu & thoát", null);
        btnLuuVaThoat.addActionListener(e -> onSave(false, true));

        btnThoat = createActionButton("Thoát", null);
        btnThoat.addActionListener(e -> dispose());

        pnlBottom.add(btnLuuVaIn);
        pnlBottom.add(btnLuuVaXemIn);
        pnlBottom.add(btnLuu);
        pnlBottom.add(btnLuuVaMoi);
        pnlBottom.add(btnLuuVaThoat);
        pnlBottom.add(btnThoat);

        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, int x, int y, int w, int h) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setBounds(x, y, w, h);
        return lbl;
    }

    private JButton createToolbarButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(190, 210, 235), 1), new EmptyBorder(3, 8, 3, 8)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createActionButton(String text, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(180, 205, 235), 1), new EmptyBorder(4, 12, 4, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadDropdownData() {
        // Cửa hàng
        cbCuaHang.removeAllItems();
        List<CuaHang> cuaHangList = cuaHangDAO.getAll();
        for (CuaHang ch : cuaHangList) {
            cbCuaHang.addItem(ch);
        }
        if (cbCuaHang.getItemCount() > 0) {
            cbCuaHang.setSelectedIndex(0);
        }

        // Khách hàng
        cbKhachHang.removeAllItems();
        List<KhachHang> khList = khachHangDAO.getAll();
        for (KhachHang kh : khList) {
            if (kh.getStatus() >= 0) cbKhachHang.addItem(kh);
        }

        // Nhân viên
        cbNhanVien.removeAllItems();
        List<NhanVien> nvList = nhanVienDAO.getAll();
        for (NhanVien nv : nvList) {
            if (nv.getStatus() >= 0) cbNhanVien.addItem(nv);
        }

        // Phân loại
        loadPhanLoaiData();

        // Tài khoản ngân hàng
        loadTaiKhoanNHData();

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
    }

    private void loadPhanLoaiData() {
        List<LyDoThuChi> thuList = lyDoThuChiDAO.getAllActive().stream()
                .filter(LyDoThuChi::isLaLyDoThu)
                .collect(Collectors.toList());
        cbPhanLoai.setItems(thuList);
        if (defaultReason != null) {
            for (LyDoThuChi r : thuList) {
                if (defaultReason.getId() != null && defaultReason.getId().equals(r.getId())) {
                    cbPhanLoai.setSelectedItem(r);
                    if (txtLyDoThu != null) txtLyDoThu.setText(r.getName());
                    return;
                }
            }
        }
        if (!thuList.isEmpty() && cbPhanLoai.getSelectedItem() == null) {
            LyDoThuChi selected = null;
            for (LyDoThuChi r : thuList) {
                if ("Thanh toán công nợ".equalsIgnoreCase(r.getName())) {
                    selected = r;
                    break;
                }
            }
            if (selected == null) selected = thuList.get(0);
            cbPhanLoai.setSelectedItem(selected);
            if (txtLyDoThu != null && (txtLyDoThu.getText() == null || txtLyDoThu.getText().trim().isEmpty())) {
                txtLyDoThu.setText(selected.getName());
            }
        }
    }

    private void loadTaiKhoanNHData() {
        List<TaiKhoanNganHang> tkList = taiKhoanNganHangDAO.getAllActive();
        cbTaiKhoanNganHang.setItems(tkList);
        if (!tkList.isEmpty() && cbTaiKhoanNganHang.getSelectedItem() == null) {
            cbTaiKhoanNganHang.setSelectedItem(tkList.get(0));
        }
    }

    private void updateDoiTuongComponentsState() {
        String loai = (String) cbLoaiDoiTuong.getSelectedItem();
        if (loai == null) loai = "Đối tượng khác";

        boolean isOther = "Đối tượng khác".equals(loai);
        txtTenDoiTuong.setEditable(isOther);
        txtTenDoiTuong.setBackground(isOther ? BG_YELLOW : new Color(240, 240, 240));

        switch (loai) {
            case "Khách hàng":
                cbKhachHang.setEnabled(true);
                cbNhanVien.setEnabled(false);
                cbNhaCungCap.setEnabled(false);
                if (cbKhachHang.getSelectedItem() != null) {
                    KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
                    txtTenDoiTuong.setText(kh.getName() != null ? kh.getName() : "");
                    txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
                }
                break;
            case "Nhân viên":
                cbKhachHang.setEnabled(false);
                cbNhanVien.setEnabled(true);
                cbNhaCungCap.setEnabled(false);
                if (cbNhanVien.getSelectedItem() != null) {
                    NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
                    txtTenDoiTuong.setText(nv.getName() != null ? nv.getName() : "");
                    txtDiaChi.setText(nv.getDiaChi() != null ? nv.getDiaChi() : "");
                }
                break;
            case "Nhà cung cấp":
                cbKhachHang.setEnabled(false);
                cbNhanVien.setEnabled(false);
                cbNhaCungCap.setEnabled(true);
                if (cbNhaCungCap.getSelectedIndex() > 0) {
                    txtTenDoiTuong.setText((String) cbNhaCungCap.getSelectedItem());
                }
                break;
            default: // "Đối tượng khác"
                cbKhachHang.setEnabled(false);
                cbNhanVien.setEnabled(false);
                cbNhaCungCap.setEnabled(false);
                break;
        }
    }

    private void updateTaiKhoanNHState() {
        boolean isChuyenKhoan = chkChuyenVaoTaiKhoan.isSelected();
        cbTaiKhoanNganHang.setEnabled(isChuyenKhoan);
    }

    private void formatMoneyInput() {
        String s = txtSoTien.getText().replaceAll("[^0-9.]", "");
        if (s.isEmpty()) s = "0";
        try {
            double v = Double.parseDouble(s);
            txtSoTien.setText(CurrencyUtil.format(v));
        } catch (Exception ignored) {}
    }

    private double parseMoneyInput() {
        String s = txtSoTien.getText().replaceAll("[^0-9.]", "");
        if (s.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void resetForNew() {
        currentPhieuThu = new PhieuThu();
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(phieuThuDAO.generateNextSoPhieu(dpkNgay.getDate()));

        if (defaultReason != null) {
            cbPhanLoai.setSelectedItem(defaultReason);
            txtLyDoThu.setText(defaultReason.getName());
        } else if (cbPhanLoai.getItems() != null && !cbPhanLoai.getItems().isEmpty()) {
            LyDoThuChi selected = null;
            for (LyDoThuChi r : cbPhanLoai.getItems()) {
                if ("Thanh toán công nợ".equalsIgnoreCase(r.getName())) {
                    selected = r;
                    break;
                }
            }
            if (selected == null) selected = cbPhanLoai.getItems().get(0);
            cbPhanLoai.setSelectedItem(selected);
            txtLyDoThu.setText(selected.getName());
        } else {
            txtLyDoThu.setText("");
        }

        txtChungTuGoc.setText("");
        cbLoaiDoiTuong.setSelectedItem("Đối tượng khác");
        txtTenDoiTuong.setText("");
        txtDiaChi.setText("");
        txtSoTien.setText("0");
        chkChuyenVaoTaiKhoan.setSelected(false);
        chkKhongThayDoiCongNo.setSelected(false);
        txtGhiChu.setText("");

        if (cbCuaHang.getItemCount() > 0) cbCuaHang.setSelectedIndex(0);

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
        setTitle("PHIẾU THU - THÊM MỚI");
    }

    private void onDeleteCurrentPhieuThu() {
        if (currentPhieuThu == null || currentPhieuThu.getId() == null) {
            JOptionPane.showMessageDialog(this, "Phiếu thu chưa được lưu nên không thể xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu thu [" + currentPhieuThu.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            phieuThuDAO.delete(currentPhieuThu.getId());
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu thu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();
            dispose();
        }
    }

    private void loadPhieuThuToForm(PhieuThu pt) {
        if (pt == null) return;
        this.currentPhieuThu = pt;
        setTitle(pt.getId() != null ? "PHIẾU THU - CẬP NHẬT" : "PHIẾU THU - THÊM MỚI");

        dpkNgay.setDate(pt.getNgay() != null ? pt.getNgay() : new Date());
        txtSoPhieu.setText(pt.getSoPhieu() != null ? pt.getSoPhieu() : "");

        if (pt.getPhanLoaiId() != null) {
            for (LyDoThuChi r : cbPhanLoai.getItems()) {
                if (pt.getPhanLoaiId().equals(r.getId())) {
                    cbPhanLoai.setSelectedItem(r);
                    break;
                }
            }
        }
        txtLyDoThu.setText(pt.getLyDoThu() != null ? pt.getLyDoThu() : "");
        txtChungTuGoc.setText(pt.getChungTuGoc() != null ? pt.getChungTuGoc() : "");

        cbLoaiDoiTuong.setSelectedItem(pt.getLoaiDoiTuong() != null ? pt.getLoaiDoiTuong() : "Đối tượng khác");
        txtTenDoiTuong.setText(pt.getTenDoiTuong() != null ? pt.getTenDoiTuong() : "");
        txtDiaChi.setText(pt.getDiaChi() != null ? pt.getDiaChi() : "");

        if (pt.getNhanVienId() != null) {
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                NhanVien nv = cbNhanVien.getItemAt(i);
                if (nv != null && pt.getNhanVienId().equals(nv.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (pt.getKhachHangId() != null) {
            for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                KhachHang kh = cbKhachHang.getItemAt(i);
                if (kh != null && pt.getKhachHangId().equals(kh.getId())) {
                    cbKhachHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtSoTien.setText(CurrencyUtil.format(pt.getSoTien()));
        chkChuyenVaoTaiKhoan.setSelected(pt.isChuyenVaoTaiKhoan());

        if (pt.getTaiKhoanNganHangId() != null) {
            for (TaiKhoanNganHang tk : cbTaiKhoanNganHang.getItems()) {
                if (pt.getTaiKhoanNganHangId().equals(tk.getId())) {
                    cbTaiKhoanNganHang.setSelectedItem(tk);
                    break;
                }
            }
        }

        if (pt.getCuaHangId() != null) {
            for (int i = 0; i < cbCuaHang.getItemCount(); i++) {
                CuaHang ch = cbCuaHang.getItemAt(i);
                if (ch != null && pt.getCuaHangId().equals(ch.getId())) {
                    cbCuaHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        chkKhongThayDoiCongNo.setSelected(pt.isKhongThayDoiCongNo());
        txtGhiChu.setText(pt.getGhiChu() != null ? pt.getGhiChu() : "");

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
    }

    private boolean collectFormToModel() {
        if (txtSoPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số phiếu không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (currentPhieuThu == null) currentPhieuThu = new PhieuThu();

        currentPhieuThu.setNgay(dpkNgay.getDate());
        currentPhieuThu.setSoPhieu(txtSoPhieu.getText().trim());

        LyDoThuChi r = cbPhanLoai.getSelectedItem();
        if (r != null) {
            currentPhieuThu.setPhanLoaiId(r.getId());
            currentPhieuThu.setTenPhanLoai(r.getName());
        }
        currentPhieuThu.setLyDoThu(txtLyDoThu.getText().trim());
        currentPhieuThu.setChungTuGoc(txtChungTuGoc.getText().trim());

        String loaiDT = (String) cbLoaiDoiTuong.getSelectedItem();
        currentPhieuThu.setLoaiDoiTuong(loaiDT);
        currentPhieuThu.setTenDoiTuong(txtTenDoiTuong.getText().trim());
        currentPhieuThu.setDiaChi(txtDiaChi.getText().trim());

        if ("Nhân viên".equals(loaiDT) && cbNhanVien.getSelectedItem() != null) {
            NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
            currentPhieuThu.setNhanVienId(nv.getId());
            currentPhieuThu.setTenNhanVien(nv.getName());
        } else {
            currentPhieuThu.setNhanVienId(null);
            currentPhieuThu.setTenNhanVien(null);
        }

        if ("Khách hàng".equals(loaiDT) && cbKhachHang.getSelectedItem() != null) {
            KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
            currentPhieuThu.setKhachHangId(kh.getId());
            currentPhieuThu.setTenKhachHang(kh.getName());
        } else {
            currentPhieuThu.setKhachHangId(null);
            currentPhieuThu.setTenKhachHang(null);
        }

        if ("Nhà cung cấp".equals(loaiDT) && cbNhaCungCap.getSelectedIndex() > 0) {
            currentPhieuThu.setTenNhaCungCap((String) cbNhaCungCap.getSelectedItem());
        } else {
            currentPhieuThu.setTenNhaCungCap(null);
        }

        currentPhieuThu.setSoTien(parseMoneyInput());
        currentPhieuThu.setChuyenVaoTaiKhoan(chkChuyenVaoTaiKhoan.isSelected());

        if (chkChuyenVaoTaiKhoan.isSelected() && cbTaiKhoanNganHang.getSelectedItem() != null) {
            TaiKhoanNganHang tk = cbTaiKhoanNganHang.getSelectedItem();
            currentPhieuThu.setTaiKhoanNganHangId(tk.getId());
            currentPhieuThu.setTenTaiKhoanNganHang(tk.getName());
        } else {
            currentPhieuThu.setTaiKhoanNganHangId(null);
            currentPhieuThu.setTenTaiKhoanNganHang(null);
        }

        if (cbCuaHang.getSelectedItem() != null) {
            CuaHang ch = (CuaHang) cbCuaHang.getSelectedItem();
            currentPhieuThu.setCuaHangId(ch.getId());
            currentPhieuThu.setTenCuaHang(ch.getName());
        }

        currentPhieuThu.setKhongThayDoiCongNo(chkKhongThayDoiCongNo.isSelected());
        currentPhieuThu.setGhiChu(txtGhiChu.getText().trim());

        return true;
    }

    private boolean onSave(boolean createNewAfter, boolean closeAfter) {
        if (!collectFormToModel()) return false;

        boolean ok;
        if (currentPhieuThu.getId() == null || currentPhieuThu.getId().trim().isEmpty()) {
            ok = phieuThuDAO.insert(currentPhieuThu);
        } else {
            ok = phieuThuDAO.update(currentPhieuThu);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã lưu phiếu thu " + currentPhieuThu.getSoPhieu() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu thu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void onSaveAndPrint(boolean directPrint) {
        if (onSave(false, false)) {
            String msg = directPrint ? "Đang gửi lệnh in phiếu thu " + currentPhieuThu.getSoPhieu() + "..." : "Mở bản xem trước in phiếu thu " + currentPhieuThu.getSoPhieu();
            JOptionPane.showMessageDialog(this, msg, "In phiếu thu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onSaoChep() {
        if (currentPhieuThu == null) return;
        collectFormToModel();
        currentPhieuThu.setId(null);
        currentPhieuThu.setSoPhieu(phieuThuDAO.generateNextSoPhieu(dpkNgay.getDate()));
        txtSoPhieu.setText(currentPhieuThu.getSoPhieu());
        setTitle("PHIẾU THU - THÊM MỚI (SAO CHÉP)");
        JOptionPane.showMessageDialog(this, "Đã tạo bản sao mới với số phiếu: " + currentPhieuThu.getSoPhieu(), "Sao chép phiếu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onNavigate(int direction) {
        String curId = (currentPhieuThu != null) ? currentPhieuThu.getId() : "";
        PhieuThu target = phieuThuDAO.getPhieuThuByOffset(curId, direction);
        if (target != null) {
            loadPhieuThuToForm(target);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showShortcutsPopup(Component comp) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem("F10: Xem phiếu trước"));
        popup.add(new JMenuItem("F11: Xem phiếu sau"));
        popup.add(new JMenuItem("F8: Lưu & In"));
        popup.add(new JMenuItem("F12: Lưu phiếu"));
        popup.add(new JMenuItem("Esc: Thoát"));
        popup.show(comp, 0, comp.getHeight());
    }

    private void setupHotkeys() {
        // F10: Trước
        getRootPane().registerKeyboardAction(e -> onNavigate(-1), KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F11: Sau
        getRootPane().registerKeyboardAction(e -> onNavigate(1), KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F12: Lưu
        getRootPane().registerKeyboardAction(e -> onSave(false, false), KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // F8: Lưu & In
        getRootPane().registerKeyboardAction(e -> onSaveAndPrint(true), KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Esc: Thoát
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
}
