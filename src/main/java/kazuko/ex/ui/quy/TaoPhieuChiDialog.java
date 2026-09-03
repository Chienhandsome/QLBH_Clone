package kazuko.ex.ui.quy;

import kazuko.ex.dao.CuaHangDAO;
import kazuko.ex.dao.KhachHangDAO;
import kazuko.ex.dao.LyDoThuChiDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.dao.PhieuChiDAO;
import kazuko.ex.dao.TaiKhoanNganHangDAO;
import kazuko.ex.model.danhmuc.CuaHang;
import kazuko.ex.model.danhmuc.KhachHang;
import kazuko.ex.model.danhmuc.LyDoThuChi;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.model.danhmuc.TaiKhoanNganHang;
import kazuko.ex.model.quy.PhieuChi;
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

public class TaoPhieuChiDialog extends JDialog {

    private final PhieuChiDAO phieuChiDAO = new PhieuChiDAO();
    private final LyDoThuChiDAO lyDoThuChiDAO = new LyDoThuChiDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final CuaHangDAO cuaHangDAO = new CuaHangDAO();
    private final TaiKhoanNganHangDAO taiKhoanNganHangDAO = new TaiKhoanNganHangDAO();

    private PhieuChi currentPhieuChi;
    private LyDoThuChi defaultReason;
    private String fixedLoaiDoiTuong = null;
    private final Runnable onDataSaved;

    // Form Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoPhieu;
    private DropdownWithActionBar<LyDoThuChi> cbPhanLoai;
    private JTextField txtLyDoChi;
    private JTextField txtChungTuGoc;
    private JComboBox<String> cbLoaiDoiTuong;
    private JTextField txtTenDoiTuong;
    private JTextField txtDiaChi;
    private JComboBox<NhanVien> cbNhanVien;
    private JComboBox<KhachHang> cbKhachHang;
    private JComboBox<String> cbNhaCungCap;
    private JTextField txtSoTien;
    private JCheckBox chkChuyenTuTaiKhoan;
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

    public TaoPhieuChiDialog(Window parent) {
        this(parent, null, null, null, null);
    }

    public TaoPhieuChiDialog(Window parent, PhieuChi phieuChi, Runnable onDataSaved) {
        this(parent, phieuChi, null, null, onDataSaved);
    }

    public TaoPhieuChiDialog(Window parent, PhieuChi phieuChi, LyDoThuChi defaultReason, Runnable onDataSaved) {
        this(parent, phieuChi, defaultReason, null, onDataSaved);
    }

    public TaoPhieuChiDialog(Window parent, PhieuChi phieuChi, LyDoThuChi defaultReason, String fixedLoaiDoiTuong, Runnable onDataSaved) {
        super(parent, phieuChi != null && phieuChi.getId() != null ? "PHIẾU CHI - CẬP NHẬT" : "PHIẾU CHI - THÊM MỚI", ModalityType.APPLICATION_MODAL);
        this.currentPhieuChi = phieuChi;
        this.defaultReason = defaultReason;
        this.fixedLoaiDoiTuong = fixedLoaiDoiTuong;
        this.onDataSaved = onDataSaved;

        initComponents();
        loadDropdownData();

        if (currentPhieuChi != null) {
            loadPhieuChiToForm(currentPhieuChi);
        } else {
            resetForNew();
        }

        if (fixedLoaiDoiTuong != null && !fixedLoaiDoiTuong.trim().isEmpty()) {
            cbLoaiDoiTuong.setSelectedItem(fixedLoaiDoiTuong);
            cbLoaiDoiTuong.setEnabled(false);
            updateDoiTuongComponentsState();
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
        JLabel lblIcon = new JLabel(IconHelper.getIcon("arrow-180-medium.png", 24, 24));
        JLabel lblTitle = new JLabel("Phiếu chi");
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
        btnXoa.addActionListener(e -> onDeleteCurrentPhieuChi());

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
                txtLyDoChi.setText(selected.getName());
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

        // Row 3: Lý do chi
        y += gapY;
        JLabel lblLyDoChi = createLabel("Lý do chi", 10, y, 65, rowH);
        txtLyDoChi = new JTextField();
        txtLyDoChi.setBounds(80, y, 540, rowH);
        txtLyDoChi.setBackground(BG_YELLOW);
        txtLyDoChi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlCenter.add(lblLyDoChi);
        pnlCenter.add(txtLyDoChi);

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

        // Row 11: [ ] Chuyển từ tài khoản + Tài khoản ngân hàng Dropdown
        y += gapY;
        chkChuyenTuTaiKhoan = new JCheckBox("Chuyển từ tài khoản");
        chkChuyenTuTaiKhoan.setOpaque(false);
        chkChuyenTuTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkChuyenTuTaiKhoan.setBounds(80, y, 145, rowH);
        chkChuyenTuTaiKhoan.addActionListener(e -> updateTaiKhoanNHState());
        pnlCenter.add(chkChuyenTuTaiKhoan);

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

        // Nhà cung cấp
        cbNhaCungCap.removeAllItems();
        cbNhaCungCap.addItem("-- Chọn nhà cung cấp --");
        List<NhaCungCap> nccList = new NhaCungCapDAO().getAll();
        for (NhaCungCap ncc : nccList) {
            if (ncc.getStatus() >= 0) cbNhaCungCap.addItem(ncc.getName());
        }

        // Phân loại
        loadPhanLoaiData();

        // Tài khoản ngân hàng
        loadTaiKhoanNHData();

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
    }

    private void loadPhanLoaiData() {
        List<LyDoThuChi> chiList = lyDoThuChiDAO.getAllActive().stream()
                .filter(LyDoThuChi::isLaLyDoChi)
                .collect(Collectors.toList());
        cbPhanLoai.setItems(chiList);

        LyDoThuChi toSelect = null;
        if (defaultReason != null && defaultReason.isLaLyDoChi()) {
            for (LyDoThuChi r : chiList) {
                if (defaultReason.getId().equals(r.getId())) {
                    toSelect = r;
                    break;
                }
            }
        }
        if (toSelect == null && !chiList.isEmpty()) {
            for (LyDoThuChi r : chiList) {
                if (r.isDefault() || "Thanh toán công nợ NCC".equalsIgnoreCase(r.getName()) || "Chi mua hàng".equalsIgnoreCase(r.getName()) || "Chi tiền hàng".equalsIgnoreCase(r.getName())) {
                    toSelect = r;
                    break;
                }
            }
            if (toSelect == null) toSelect = chiList.get(0);
        }

        if (toSelect != null) {
            cbPhanLoai.setSelectedItem(toSelect);
            if (txtLyDoChi != null && (txtLyDoChi.getText() == null || txtLyDoChi.getText().trim().isEmpty())) {
                txtLyDoChi.setText(toSelect.getName());
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
        boolean isChuyenKhoan = chkChuyenTuTaiKhoan.isSelected();
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
        currentPhieuChi = new PhieuChi();
        dpkNgay.setDate(new Date());
        txtSoPhieu.setText(phieuChiDAO.generateNextSoPhieu(dpkNgay.getDate()));

        if (cbPhanLoai.getItems() != null && !cbPhanLoai.getItems().isEmpty()) {
            cbPhanLoai.setSelectedItem(cbPhanLoai.getItems().get(0));
            txtLyDoChi.setText(cbPhanLoai.getItems().get(0).getName());
        } else {
            txtLyDoChi.setText("");
        }

        txtChungTuGoc.setText("");
        if (fixedLoaiDoiTuong != null && !fixedLoaiDoiTuong.trim().isEmpty()) {
            cbLoaiDoiTuong.setSelectedItem(fixedLoaiDoiTuong);
            cbLoaiDoiTuong.setEnabled(false);
        } else {
            cbLoaiDoiTuong.setSelectedItem("Đối tượng khác");
            cbLoaiDoiTuong.setEnabled(true);
        }
        txtTenDoiTuong.setText("");
        txtDiaChi.setText("");
        txtSoTien.setText("0");
        chkChuyenTuTaiKhoan.setSelected(false);
        chkKhongThayDoiCongNo.setSelected(false);
        txtGhiChu.setText("");

        if (cbCuaHang.getItemCount() > 0) cbCuaHang.setSelectedIndex(0);

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
        setTitle("PHIẾU CHI - THÊM MỚI");
    }

    private void loadPhieuChiToForm(PhieuChi pc) {
        if (pc == null) return;
        this.currentPhieuChi = pc;
        setTitle(pc.getId() != null ? "PHIẾU CHI - CẬP NHẬT" : "PHIẾU CHI - THÊM MỚI");

        dpkNgay.setDate(pc.getNgay() != null ? pc.getNgay() : new Date());
        String sp = pc.getSoPhieu();
        if (sp == null || sp.trim().isEmpty()) {
            sp = phieuChiDAO.generateNextSoPhieu(dpkNgay.getDate());
        }
        txtSoPhieu.setText(sp);

        if (pc.getPhanLoaiId() != null) {
            for (LyDoThuChi r : cbPhanLoai.getItems()) {
                if (pc.getPhanLoaiId().equals(r.getId())) {
                    cbPhanLoai.setSelectedItem(r);
                    break;
                }
            }
        }
        txtLyDoChi.setText(pc.getLyDoChi() != null ? pc.getLyDoChi() : "");
        txtChungTuGoc.setText(pc.getChungTuGoc() != null ? pc.getChungTuGoc() : "");

        if (fixedLoaiDoiTuong != null && !fixedLoaiDoiTuong.trim().isEmpty()) {
            cbLoaiDoiTuong.setSelectedItem(fixedLoaiDoiTuong);
            cbLoaiDoiTuong.setEnabled(false);
        } else {
            cbLoaiDoiTuong.setSelectedItem(pc.getLoaiDoiTuong() != null ? pc.getLoaiDoiTuong() : "Đối tượng khác");
            cbLoaiDoiTuong.setEnabled(true);
        }
        txtTenDoiTuong.setText(pc.getTenDoiTuong() != null ? pc.getTenDoiTuong() : "");
        txtDiaChi.setText(pc.getDiaChi() != null ? pc.getDiaChi() : "");

        if (pc.getNhanVienId() != null) {
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                NhanVien nv = cbNhanVien.getItemAt(i);
                if (nv != null && pc.getNhanVienId().equals(nv.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (pc.getKhachHangId() != null) {
            for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
                KhachHang kh = cbKhachHang.getItemAt(i);
                if (kh != null && pc.getKhachHangId().equals(kh.getId())) {
                    cbKhachHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (pc.getTenNhaCungCap() != null || pc.getTenDoiTuong() != null) {
            String targetNcc = pc.getTenNhaCungCap() != null ? pc.getTenNhaCungCap() : pc.getTenDoiTuong();
            for (int i = 0; i < cbNhaCungCap.getItemCount(); i++) {
                if (targetNcc.equalsIgnoreCase(cbNhaCungCap.getItemAt(i))) {
                    cbNhaCungCap.setSelectedIndex(i);
                    break;
                }
            }
        }

        txtSoTien.setText(CurrencyUtil.format(pc.getSoTien()));
        chkChuyenTuTaiKhoan.setSelected(pc.isChuyenTuTaiKhoan());

        if (pc.getTaiKhoanNganHangId() != null) {
            for (TaiKhoanNganHang tk : cbTaiKhoanNganHang.getItems()) {
                if (pc.getTaiKhoanNganHangId().equals(tk.getId())) {
                    cbTaiKhoanNganHang.setSelectedItem(tk);
                    break;
                }
            }
        }

        if (pc.getCuaHangId() != null) {
            for (int i = 0; i < cbCuaHang.getItemCount(); i++) {
                CuaHang ch = cbCuaHang.getItemAt(i);
                if (ch != null && pc.getCuaHangId().equals(ch.getId())) {
                    cbCuaHang.setSelectedIndex(i);
                    break;
                }
            }
        }

        chkKhongThayDoiCongNo.setSelected(pc.isKhongThayDoiCongNo());
        txtGhiChu.setText(pc.getGhiChu() != null ? pc.getGhiChu() : "");

        updateDoiTuongComponentsState();
        updateTaiKhoanNHState();
    }

    private boolean collectFormToModel() {
        if (txtSoPhieu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số phiếu không được để trống!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (currentPhieuChi == null) currentPhieuChi = new PhieuChi();

        currentPhieuChi.setNgay(dpkNgay.getDate());
        currentPhieuChi.setSoPhieu(txtSoPhieu.getText().trim());

        LyDoThuChi r = cbPhanLoai.getSelectedItem();
        if (r != null) {
            currentPhieuChi.setPhanLoaiId(r.getId());
            currentPhieuChi.setTenPhanLoai(r.getName());
        }
        currentPhieuChi.setLyDoChi(txtLyDoChi.getText().trim());
        currentPhieuChi.setChungTuGoc(txtChungTuGoc.getText().trim());

        String loaiDT = (String) cbLoaiDoiTuong.getSelectedItem();
        currentPhieuChi.setLoaiDoiTuong(loaiDT);
        currentPhieuChi.setTenDoiTuong(txtTenDoiTuong.getText().trim());
        currentPhieuChi.setDiaChi(txtDiaChi.getText().trim());

        if ("Nhân viên".equals(loaiDT) && cbNhanVien.getSelectedItem() != null) {
            NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
            currentPhieuChi.setNhanVienId(nv.getId());
            currentPhieuChi.setTenNhanVien(nv.getName());
        } else {
            currentPhieuChi.setNhanVienId(null);
            currentPhieuChi.setTenNhanVien(null);
        }

        if ("Khách hàng".equals(loaiDT) && cbKhachHang.getSelectedItem() != null) {
            KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
            currentPhieuChi.setKhachHangId(kh.getId());
            currentPhieuChi.setTenKhachHang(kh.getName());
        } else {
            currentPhieuChi.setKhachHangId(null);
            currentPhieuChi.setTenKhachHang(null);
        }

        if ("Nhà cung cấp".equals(loaiDT) && cbNhaCungCap.getSelectedIndex() > 0) {
            currentPhieuChi.setTenNhaCungCap((String) cbNhaCungCap.getSelectedItem());
        } else {
            currentPhieuChi.setTenNhaCungCap(null);
        }

        currentPhieuChi.setSoTien(parseMoneyInput());
        currentPhieuChi.setChuyenTuTaiKhoan(chkChuyenTuTaiKhoan.isSelected());

        if (chkChuyenTuTaiKhoan.isSelected() && cbTaiKhoanNganHang.getSelectedItem() != null) {
            TaiKhoanNganHang tk = cbTaiKhoanNganHang.getSelectedItem();
            currentPhieuChi.setTaiKhoanNganHangId(tk.getId());
            currentPhieuChi.setTenTaiKhoanNganHang(tk.getName());
        } else {
            currentPhieuChi.setTaiKhoanNganHangId(null);
            currentPhieuChi.setTenTaiKhoanNganHang(null);
        }

        if (cbCuaHang.getSelectedItem() != null) {
            CuaHang ch = (CuaHang) cbCuaHang.getSelectedItem();
            currentPhieuChi.setCuaHangId(ch.getId());
            currentPhieuChi.setTenCuaHang(ch.getName());
        }

        currentPhieuChi.setKhongThayDoiCongNo(chkKhongThayDoiCongNo.isSelected());
        currentPhieuChi.setGhiChu(txtGhiChu.getText().trim());

        return true;
    }

    private boolean onSave(boolean createNewAfter, boolean closeAfter) {
        if (!collectFormToModel()) return false;

        boolean ok;
        if (currentPhieuChi.getId() == null || currentPhieuChi.getId().trim().isEmpty()) {
            ok = phieuChiDAO.insert(currentPhieuChi);
        } else {
            ok = phieuChiDAO.update(currentPhieuChi);
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã lưu phiếu chi " + currentPhieuChi.getSoPhieu() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            if (onDataSaved != null) onDataSaved.run();

            if (createNewAfter) {
                resetForNew();
            } else if (closeAfter) {
                dispose();
            }
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu phiếu chi!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void onSaveAndPrint(boolean directPrint) {
        if (onSave(false, false)) {
            String msg = directPrint ? "Đang gửi lệnh in phiếu chi " + currentPhieuChi.getSoPhieu() + "..." : "Mở bản xem trước in phiếu chi " + currentPhieuChi.getSoPhieu();
            JOptionPane.showMessageDialog(this, msg, "In phiếu chi", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onSaoChep() {
        if (currentPhieuChi == null) return;
        collectFormToModel();
        currentPhieuChi.setId(null);
        currentPhieuChi.setSoPhieu(phieuChiDAO.generateNextSoPhieu(dpkNgay.getDate()));
        txtSoPhieu.setText(currentPhieuChi.getSoPhieu());
        setTitle("PHIẾU CHI - THÊM MỚI (SAO CHÉP)");
        JOptionPane.showMessageDialog(this, "Đã tạo bản sao mới với số phiếu: " + currentPhieuChi.getSoPhieu(), "Sao chép phiếu", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onNavigate(int direction) {
        String curId = (currentPhieuChi != null) ? currentPhieuChi.getId() : "";
        PhieuChi target = phieuChiDAO.getPhieuChiByOffset(curId, direction);
        if (target != null) {
            loadPhieuChiToForm(target);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở phiếu đầu tiên!" : "Đã ở phiếu cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void onDeleteCurrentPhieuChi() {
        if (currentPhieuChi == null || currentPhieuChi.getId() == null) {
            JOptionPane.showMessageDialog(this, "Phiếu chi chưa được lưu, không thể xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa phiếu chi [" + currentPhieuChi.getSoPhieu() + "] không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            phieuChiDAO.delete(currentPhieuChi.getId());
            if (onDataSaved != null) onDataSaved.run();
            JOptionPane.showMessageDialog(this, "Đã xóa phiếu chi thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
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
