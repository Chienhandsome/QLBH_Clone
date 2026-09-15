package kazuko.ex.ui.hoadon;

import kazuko.ex.dao.*;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.banhang.DonHangChiTiet;
import kazuko.ex.model.banhang.GiaBanGanDayKhachHang;
import kazuko.ex.model.danhmuc.*;
import kazuko.ex.model.dathang.KhachDatHang;
import kazuko.ex.model.dathang.KhachDatHangChiTiet;
import kazuko.ex.model.system.SystemConfig;
import kazuko.ex.ui.common.DatePickerField;
import kazuko.ex.ui.danhmuc.DanhMucKhachHangPanel;
import kazuko.ex.ui.danhmuc.KhachHangEditDialog;
import kazuko.ex.util.AppColors;
import kazuko.ex.util.CurrencyUtil;
import kazuko.ex.util.DateUtil;
import kazuko.ex.util.IconHelper;
import kazuko.ex.ui.touch.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class HoaDonBanHangPanel extends JPanel {

    private static final Color TOUCH_BLUE = new Color(70, 137, 184);
    private static final Color TOUCH_TEAL = new Color(0, 83, 82);
    private static final Color TOUCH_GREEN = new Color(0, 105, 12);
    private static final Color TOUCH_FIELD = new Color(190, 255, 190);

    private final boolean touchMode;
    private final Runnable touchExitAction;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final MatHangDAO matHangDAO = new MatHangDAO();
    private final NhomMatHangDAO nhomMatHangDAO = new NhomMatHangDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final KhoHangDAO khoHangDAO = new KhoHangDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final DotKhuyenMaiDAO dotKhuyenMaiDAO = new DotKhuyenMaiDAO();
    private final SystemConfigDAO systemConfigDAO = new SystemConfigDAO();
    private final KhachDatHangDAO khachDatHangDAO = new KhachDatHangDAO();

    private SystemConfig systemConfig;
    private DonHang currentHoaDon;
    private String currentLoaiGiaName = "Giá bán";

    // Top barcode & quick add bar
    private JTextField txtBarcode;
    private JTextField txtSoLuongBarcode;
    private JButton btnThemBarcode;

    // Left category tree & catalog table
    private JTree treeNhomHang;
    private JTable tableCatalog;
    private DefaultTableModel catalogModel;
    private List<String> visibleCatalogColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS);
    private JPopupMenu popupCatalog;
    private List<MatHang> allMatHangList = new ArrayList<>();
    private List<MatHang> filteredCatalogList = new ArrayList<>();

    // Master Info Controls
    private DatePickerField dpkNgay;
    private JTextField txtSoHD;
    private JComboBox<KhachHang> cbKhachHang;
    private JPanel customerLookupField;
    private JTextField txtKhachHangLookup;
    private JPopupMenu popupKhachHang;
    private JTable tableLookupKhachHang;
    private DefaultTableModel lookupKhachHangModel;
    private final List<KhachHang> allLookupKhachHangList = new ArrayList<>();
    private final List<KhachHang> filteredLookupKhachHangList = new ArrayList<>();
    private boolean isUpdatingCustomerLookup = false;
    private JComboBox<KhoHang> cbKhoXuat;
    private JComboBox<NhanVien> cbNhanVien;
    private JTextField txtDienGiai;
    private JTextField txtDiaChiGiaoHang;
    private JTextField txtGhiChu;

    // Mini Vertical Toolbar
    private JLabel lblTonKho;
    private JButton btnMoDatHang;
    private JButton btnLuuTam;
    private JButton btnMoLai;
    private JButton btnXoaDong;
    private JButton btnTangSL;
    private JButton btnGiamSL;
    private JButton btnDatSL;

    // Details Panel & Tabs
    private JTabbedPane tabbedPaneDetails;
    private JTable tableLayHang;
    private DefaultTableModel layHangModel;
    private JTable tableTraHang;
    private DefaultTableModel traHangModel;
    private List<String> visibleLayHangColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_COLUMNS);

    private final List<DonHangChiTiet> currentLayHangList = new ArrayList<>();
    private final List<DonHangChiTiet> currentTraHangList = new ArrayList<>();

    // Financial Summary Controls
    private JTextField txtTiLeGiamGia;
    private JTextField txtTienHang;
    private JTextField txtTongChietKhauMatHang;
    private JTextField txtTienGiamGia;
    private JTextField txtTiLeThue;
    private JTextField txtTienThue;
    private JTextField txtPhiVanChuyen;
    private JTextField txtTienDoiTra;
    private JTextField txtTongCong;
    private final List<Component> taxSummaryComponents = new ArrayList<>();
    private final List<Component> shippingSummaryComponents = new ArrayList<>();
    private boolean isUpdatingFinancials = false;
    private boolean isRefreshingLayHangTable = false;

    // Bottom Action Buttons
    private JButton btnTaoHoaDonMoi;
    private JButton btnThanhToan;
    private JButton btnThongKe;

    // Touch-screen presentation. The underlying invoice state and handlers are
    // deliberately shared with the desktop layout above.
    private JPanel touchCategoryPanel;
    private JPanel touchProductGrid;
    private JScrollPane touchProductScroll;
    private MatHang selectedTouchProduct;
    private DefaultListModel<DonHangChiTiet> touchCartListModel;
    private JList<DonHangChiTiet> touchCartList;
    private JPanel touchItemActionBar;
    private int selectedCartIndex = -1;

    // Touch Header Tabs & Cards
    private JButton btnTabHoaDon;
    private JButton btnTabGiaoHang;
    private JButton btnTabMini;
    private JPanel cardHeaderPanel;
    private CardLayout cardHeaderLayout;

    // Touch Master Fields (Invoice Tab - Hình 1)
    private JButton btnTouchNgay;
    private JButton btnTouchSoHD;
    private JButton btnTouchChonLoaiGia;
    private JButton btnTouchKhachHang;
    private JButton btnTouchDiem;
    private JButton btnTouchKho;
    private JButton btnTouchNhanVien;
    private JButton btnTouchSoKhach;
    private JButton btnTouchBan;

    // Touch Master Fields (Delivery Tab - Hình 5)
    private JButton btnTouchGiaoHang;
    private JButton btnTouchNhanVienGiao;
    private JButton btnTouchTrichNhanVien;
    private JButton btnTouchGhiChuGiao;
    private NhanVien deliveryNhanVien;
    private double deliveryTrichNhanVien = 0.0;
    private String deliveryGhiChu = "";
    private JTextField txtTouchTable;
    private JTextField txtTouchGuestCount;
    private JTextField txtTouchPoints;

    public HoaDonBanHangPanel() {
        this(false, null);
    }

    public HoaDonBanHangPanel(boolean touchMode) {
        this(touchMode, null);
    }

    public HoaDonBanHangPanel(boolean touchMode, Runnable touchExitAction) {
        this.touchMode = touchMode;
        this.touchExitAction = touchExitAction;
        if (touchMode) {
            initTouchComponents();
        } else {
            initComponents();
        }
        setupKeyBindings();
        loadInitialData();
        resetForNewInvoice();
    }

    private void initTouchComponents() {
        setLayout(new BorderLayout(6, 0));
        setBackground(TOUCH_BLUE);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        createTopBarcodePanel();
        createLeftCatalogPanel();
        createMasterHeaderForm();
        createDetailsTabbedPane();
        createFinancialSummaryPanel();
        lblTonKho = new JLabel("TỒN: 0");

        JPanel catalogArea = new JPanel(new BorderLayout(6, 6));
        catalogArea.setOpaque(false);
        catalogArea.add(createTouchBarcodePanel(), BorderLayout.NORTH);

        touchCategoryPanel = new JPanel();
        touchCategoryPanel.setLayout(new BoxLayout(touchCategoryPanel, BoxLayout.Y_AXIS));
        touchCategoryPanel.setBackground(TOUCH_BLUE);
        JScrollPane categoryScroll = new JScrollPane(touchCategoryPanel);
        categoryScroll.setPreferredSize(new Dimension(145, 0));
        categoryScroll.setBorder(BorderFactory.createEmptyBorder());
        categoryScroll.getVerticalScrollBar().setUnitIncrement(28);
        catalogArea.add(categoryScroll, BorderLayout.WEST);

        touchProductGrid = new JPanel(new GridLayout(0, 2, 7, 7));
        touchProductGrid.setBackground(TOUCH_BLUE);
        touchProductScroll = new JScrollPane(touchProductGrid);
        touchProductScroll.setBorder(BorderFactory.createEmptyBorder());
        touchProductScroll.getVerticalScrollBar().setUnitIncrement(52);
        touchProductScroll.getViewport().setBackground(TOUCH_BLUE);

        // Product grid wrapper with gear button [⚙] at bottom-right corner (Hình 1)
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(touchProductScroll, BorderLayout.CENTER);

        JPanel gearBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        gearBar.setOpaque(false);
        JButton btnGear = new JButton("⚙");
        btnGear.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnGear.setForeground(Color.WHITE);
        btnGear.setBackground(new Color(79, 139, 169));
        btnGear.setPreferredSize(new Dimension(42, 34));
        btnGear.setFocusPainted(false);
        btnGear.setBorder(new LineBorder(Color.WHITE, 1, true));
        btnGear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGear.setToolTipText("Thiết lập cột hiển thị mặt hàng");
        btnGear.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            TouchThietLapCotBanHangDialog dlg = new TouchThietLapCotBanHangDialog(owner, this::refreshTouchProductGrid);
            dlg.setVisible(true);
        });
        gearBar.add(btnGear);
        gridWrapper.add(gearBar, BorderLayout.SOUTH);

        catalogArea.add(gridWrapper, BorderLayout.CENTER);
        catalogArea.add(createTouchCatalogActionBar(), BorderLayout.SOUTH);

        JPanel invoiceArea = createTouchInvoicePanel();
        invoiceArea.setPreferredSize(new Dimension(595, 0));

        add(catalogArea, BorderLayout.CENTER);
        add(invoiceArea, BorderLayout.EAST);
    }

    private JPanel createTouchBarcodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        panel.setBackground(TOUCH_BLUE);
        panel.setPreferredSize(new Dimension(0, 58));

        JLabel barcodeLabel = createTouchLabel("MÃ VẠCH");
        txtBarcode.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtBarcode.setPreferredSize(new Dimension(240, 48));
        txtBarcode.addActionListener(e -> onBarcodeAddTouch());
        txtBarcode.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Window owner = SwingUtilities.getWindowAncestor(HoaDonBanHangPanel.this);
                    String val = TouchTextKeyboardDialog.showDialog(owner, "QUÉT / NHẬP MÃ VẠCH", txtBarcode.getText());
                    if (val != null) {
                        txtBarcode.setText(val);
                        onBarcodeAddTouch();
                    }
                }
            }
        });

        JLabel quantityLabel = createTouchLabel("SL");
        txtSoLuongBarcode.setFont(new Font("Segoe UI", Font.BOLD, 26));
        txtSoLuongBarcode.setHorizontalAlignment(SwingConstants.RIGHT);
        txtSoLuongBarcode.setPreferredSize(new Dimension(130, 48));
        txtSoLuongBarcode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtSoLuongBarcode.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window owner = SwingUtilities.getWindowAncestor(HoaDonBanHangPanel.this);
                double cur = 1.0;
                try {
                    cur = Double.parseDouble(txtSoLuongBarcode.getText().trim().replace(",", "."));
                } catch (Exception ignored) {}
                Double val = TouchNumberInputDialog.showDoubleDialog(owner, "SỐ LƯỢNG MUA", cur, 0.01, 999999);
                if (val != null && val > 0) {
                    txtSoLuongBarcode.setText(val == (long) (double) val ? String.format("%d", (long) (double) val) : String.valueOf(val));
                }
            }
        });

        btnThemBarcode.setText("THÊM");
        styleTouchButton(btnThemBarcode, TOUCH_GREEN, 110, 48);
        for (ActionListener al : btnThemBarcode.getActionListeners()) {
            btnThemBarcode.removeActionListener(al);
        }
        btnThemBarcode.addActionListener(e -> onBarcodeAddTouch());

        panel.add(barcodeLabel);
        panel.add(txtBarcode);
        panel.add(quantityLabel);
        panel.add(txtSoLuongBarcode);
        panel.add(btnThemBarcode);
        return panel;
    }

    private JPanel createTouchInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        panel.add(createTouchInvoiceHeader(), BorderLayout.NORTH);

        // Center Cart Area: Receipt List + Vertical Item Action Bar (Hình 1 & 2)
        JPanel cartContainer = new JPanel(new BorderLayout(4, 0));
        cartContainer.setBackground(Color.WHITE);

        touchCartListModel = new DefaultListModel<>();
        touchCartList = new JList<>(touchCartListModel);
        touchCartList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        touchCartList.setCellRenderer(new TouchReceiptListCellRenderer());
        touchCartList.setFixedCellHeight(54);
        touchCartList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedCartIndex = touchCartList.getSelectedIndex();
                if (touchItemActionBar != null) {
                    touchItemActionBar.setVisible(selectedCartIndex >= 0);
                }
            }
        });

        JScrollPane cartScroll = new JScrollPane(touchCartList);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.getViewport().setBackground(Color.WHITE);
        cartScroll.getVerticalScrollBar().setUnitIncrement(28);
        cartContainer.add(cartScroll, BorderLayout.CENTER);

        touchItemActionBar = createTouchItemActionBar();
        touchItemActionBar.setVisible(false);
        cartContainer.add(touchItemActionBar, BorderLayout.EAST);

        panel.add(cartContainer, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setBackground(Color.WHITE);
        bottom.add(createTouchSummaryPanel(), BorderLayout.CENTER);
        bottom.add(createTouchInvoiceActionBar(), BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTouchItemActionBar() {
        JPanel bar = new JPanel(new GridLayout(8, 1, 0, 4));
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(88, 0));
        bar.setBorder(new EmptyBorder(2, 2, 2, 2));

        JButton btnPlus = createTouchItemButton("+1", new Color(0, 80, 32), e -> onTouchItemPlus());
        JButton btnMinus = createTouchItemButton("-1", new Color(0, 80, 32), e -> onTouchItemMinus());
        JButton btnSetQty = createTouchItemButton("ĐẶT SL", new Color(0, 80, 32), e -> onTouchItemSetQty());
        JButton btnDelete = createTouchItemButton("XÓA", new Color(176, 0, 0), e -> onTouchItemDelete());
        JButton btnChangePrice = createTouchItemButton("ĐỔI GIÁ", new Color(0, 64, 128), e -> onTouchItemChangePrice());
        JButton btnDiscount = createTouchItemButton("CK%", new Color(0, 64, 128), e -> onTouchItemDiscount());
        JButton btnNote = createTouchItemButton("GHI CHÚ", new Color(0, 102, 102), e -> onTouchItemNote());
        JButton btnHide = createTouchItemButton("ẨN", new Color(139, 58, 0), e -> onTouchItemHide());

        bar.add(btnPlus);
        bar.add(btnMinus);
        bar.add(btnSetQty);
        bar.add(btnDelete);
        bar.add(btnChangePrice);
        bar.add(btnDiscount);
        bar.add(btnNote);
        bar.add(btnHide);

        return bar;
    }

    private JButton createTouchItemButton(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        btn.addActionListener(al);
        return btn;
    }

    private DonHangChiTiet getSelectedCartItem() {
        if (selectedCartIndex >= 0 && selectedCartIndex < currentLayHangList.size()) {
            return currentLayHangList.get(selectedCartIndex);
        }
        return null;
    }

    private void onTouchItemPlus() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        ct.setSoLuong(ct.getSoLuong() + 1);
        ct.tinhThanhTien();
        refreshLayHangTable(selectedCartIndex);
        refreshTouchCartList(selectedCartIndex);
        recalculateFinancials();
    }

    private void onTouchItemMinus() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        if (ct.getSoLuong() > 1) {
            ct.setSoLuong(ct.getSoLuong() - 1);
            ct.tinhThanhTien();
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
            recalculateFinancials();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa mặt hàng \"" + ct.getTenHang() + "\" khỏi hóa đơn?",
                    "Xóa mặt hàng", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentLayHangList.remove(selectedCartIndex);
                int next = Math.min(selectedCartIndex, currentLayHangList.size() - 1);
                refreshLayHangTable(next);
                refreshTouchCartList(next);
                recalculateFinancials();
            }
        }
    }

    private void onTouchItemSetQty() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        Double val = TouchNumberInputDialog.showDoubleDialog(owner,
                "ĐẶT SỐ LƯỢNG (" + ct.getTenHang() + ")", ct.getSoLuong(), 0.01, 999999);
        if (val != null && val > 0) {
            ct.setSoLuong(val);
            ct.tinhThanhTien();
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
            recalculateFinancials();
        }
    }

    private void onTouchItemDelete() {
        if (selectedCartIndex >= 0 && selectedCartIndex < currentLayHangList.size()) {
            DonHangChiTiet ct = currentLayHangList.get(selectedCartIndex);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Xóa mặt hàng \"" + ct.getTenHang() + "\" khỏi hóa đơn?",
                    "Xóa mặt hàng", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentLayHangList.remove(selectedCartIndex);
                int next = Math.min(selectedCartIndex, currentLayHangList.size() - 1);
                refreshLayHangTable(next);
                refreshTouchCartList(next);
                recalculateFinancials();
            }
        }
    }

    private void onTouchItemChangePrice() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        MatHang mh = matHangDAO.findById(ct.getMatHangId());
        Window owner = SwingUtilities.getWindowAncestor(this);
        Double newPrice = TouchChonGiaBanDialog.showDialog(owner, mh, ct.getDonGia());
        if (newPrice != null && newPrice > 0) {
            ct.setDonGia(newPrice);
            ct.tinhThanhTien();
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
            recalculateFinancials();
        }
    }

    private void onTouchItemDiscount() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        Double val = TouchNumberInputDialog.showDoubleDialog(owner,
                "CHIẾT KHẤU MẶT HÀNG (%)", ct.getTiLeGiamGia(), 0, 100);
        if (val != null) {
            ct.setTiLeGiamGia(val);
            ct.tinhThanhTien();
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
            recalculateFinancials();
        }
    }

    private void onTouchItemNote() {
        DonHangChiTiet ct = getSelectedCartItem();
        if (ct == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        String val = TouchTextKeyboardDialog.showDialog(owner,
                "GHI CHÚ MẶT HÀNG", ct.getGhiChu() != null ? ct.getGhiChu() : "");
        if (val != null) {
            ct.setGhiChu(val);
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
        }
    }

    private void onTouchItemHide() {
        if (touchCartList != null) touchCartList.clearSelection();
        selectedCartIndex = -1;
        if (touchItemActionBar != null) touchItemActionBar.setVisible(false);
    }

    private void refreshTouchCartList(int selectIndex) {
        if (!touchMode || touchCartListModel == null) return;
        touchCartListModel.clear();
        for (DonHangChiTiet ct : currentLayHangList) {
            touchCartListModel.addElement(ct);
        }
        if (selectIndex >= 0 && selectIndex < currentLayHangList.size()) {
            selectedCartIndex = selectIndex;
            touchCartList.setSelectedIndex(selectIndex);
            touchCartList.ensureIndexIsVisible(selectIndex);
            if (touchItemActionBar != null) touchItemActionBar.setVisible(true);
        } else if (currentLayHangList.isEmpty()) {
            selectedCartIndex = -1;
            if (touchItemActionBar != null) touchItemActionBar.setVisible(false);
        } else {
            selectedCartIndex = currentLayHangList.size() - 1;
            touchCartList.setSelectedIndex(selectedCartIndex);
            if (touchItemActionBar != null) touchItemActionBar.setVisible(true);
        }
        if (touchCartList != null) touchCartList.repaint();
    }

    private class TouchReceiptListCellRenderer extends JPanel implements ListCellRenderer<DonHangChiTiet> {
        private final JLabel lblLine1Name = new JLabel();
        private final JLabel lblLine1Total = new JLabel();
        private final JLabel lblLine2Details = new JLabel();

        public TouchReceiptListCellRenderer() {
            setLayout(new BorderLayout(4, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 225, 225)),
                    new EmptyBorder(6, 10, 6, 10)
            ));

            JPanel line1 = new JPanel(new BorderLayout());
            line1.setOpaque(false);
            lblLine1Name.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblLine1Name.setForeground(new Color(20, 20, 20));
            lblLine1Total.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblLine1Total.setForeground(new Color(204, 0, 0));
            line1.add(lblLine1Name, BorderLayout.CENTER);
            line1.add(lblLine1Total, BorderLayout.EAST);

            lblLine2Details.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblLine2Details.setForeground(new Color(0, 100, 20));

            add(line1, BorderLayout.NORTH);
            add(lblLine2Details, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends DonHangChiTiet> list,
                                                      DonHangChiTiet ct,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            if (ct == null) return this;
            int stt = index + 1;
            String name = ct.getTenHang() != null ? ct.getTenHang() : ct.getMaHang();
            lblLine1Name.setText(stt + ". " + name);
            lblLine1Total.setText(CurrencyUtil.format(ct.getThanhTien()));

            String qtyStr = (ct.getSoLuong() == (long) ct.getSoLuong())
                    ? String.format("%d", (long) ct.getSoLuong())
                    : String.valueOf(ct.getSoLuong());
            String line2 = qtyStr + " x " + CurrencyUtil.format(ct.getDonGia());
            if (ct.getTiLeGiamGia() > 0) {
                line2 += String.format("  (-%.0f%%)", ct.getTiLeGiamGia());
            }
            if (ct.getGhiChu() != null && !ct.getGhiChu().trim().isEmpty()) {
                line2 += "  [" + ct.getGhiChu().trim() + "]";
            }
            lblLine2Details.setText(line2);

            if (isSelected) {
                setBackground(new Color(218, 232, 245));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    private JPanel createTouchInvoiceHeader() {
        JPanel container = new JPanel(new BorderLayout(0, 4));
        container.setBackground(Color.WHITE);

        // Top 3 tabs: HÓA ĐƠN BÁN HÀNG, GIAO HÀNG, MINI
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        tabs.setBackground(Color.WHITE);

        btnTabHoaDon = new JButton("HÓA ĐƠN BÁN HÀNG");
        styleTouchButton(btnTabHoaDon, new Color(255, 153, 0), 245, 50);

        btnTabGiaoHang = new JButton("GIAO HÀNG");
        styleTouchButton(btnTabGiaoHang, new Color(110, 110, 110), 145, 50);

        btnTabMini = new JButton("MINI");
        styleTouchButton(btnTabMini, new Color(110, 110, 110), 90, 50);
        btnTabMini.addActionListener(e -> toggleTouchWindowSize());

        btnTabHoaDon.addActionListener(e -> switchTouchTab(true));
        btnTabGiaoHang.addActionListener(e -> switchTouchTab(false));

        tabs.add(btnTabHoaDon);
        tabs.add(btnTabGiaoHang);
        tabs.add(btnTabMini);
        container.add(tabs, BorderLayout.NORTH);

        // Card layout switching between Invoice Fields (Hình 1) and Delivery Fields (Hình 5)
        cardHeaderLayout = new CardLayout();
        cardHeaderPanel = new JPanel(cardHeaderLayout);
        cardHeaderPanel.setBackground(Color.WHITE);

        cardHeaderPanel.add(createTouchInvoiceFieldsPanel(), "INVOICE");
        cardHeaderPanel.add(createTouchDeliveryFieldsPanel(), "DELIVERY");

        container.add(cardHeaderPanel, BorderLayout.CENTER);
        return container;
    }

    private void switchTouchTab(boolean isInvoice) {
        if (isInvoice) {
            btnTabHoaDon.setBackground(new Color(255, 153, 0));
            btnTabGiaoHang.setBackground(new Color(110, 110, 110));
            cardHeaderLayout.show(cardHeaderPanel, "INVOICE");
        } else {
            btnTabHoaDon.setBackground(new Color(110, 110, 110));
            btnTabGiaoHang.setBackground(new Color(255, 153, 0));
            cardHeaderLayout.show(cardHeaderPanel, "DELIVERY");
        }
    }

    private JPanel createTouchInvoiceFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(2, 4, 2, 4));

        // Row 1: NGÀY, SỐ, Chọn loại giá
        btnTouchNgay = createMintPill(DateUtil.formatDate(dpkNgay.getDate()), 130, 38, e -> onTouchPickDate());
        btnTouchSoHD = createMintPill(txtSoHD.getText(), 130, 38, null);
        btnTouchChonLoaiGia = new JButton("Chọn loại giá");
        styleTouchButton(btnTouchChonLoaiGia, new Color(0, 80, 32), 125, 38);
        btnTouchChonLoaiGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTouchChonLoaiGia.addActionListener(e -> onTouchChonLoaiGiaGeneral());
        panel.add(createTouchInfoRow("NGÀY", btnTouchNgay, new JLabel("SỐ"), btnTouchSoHD, btnTouchChonLoaiGia));

        // Row 2: K.HÀNG, ĐIỂM
        btnTouchKhachHang = createMintPill("Khách lẻ", 280, 38, e -> onTouchPickCustomer());
        btnTouchDiem = createMintPill("0", 95, 38, null);
        panel.add(createTouchInfoRow("K.HÀNG", btnTouchKhachHang, new JLabel("ĐIỂM"), btnTouchDiem));

        // Row 3: KHO
        btnTouchKho = createMintPill("KHO BÁN HÀNG", 410, 38, e -> onTouchPickWarehouse());
        panel.add(createTouchInfoRow("KHO", btnTouchKho));

        // Row 4: N.VIÊN
        btnTouchNhanVien = createMintPill("Admin", 410, 38, e -> onTouchPickEmployee());
        panel.add(createTouchInfoRow("N.VIÊN", btnTouchNhanVien));

        // Row 5: SỐ KHÁCH, BÀN
        btnTouchSoKhach = createMintPill("0", 110, 38, e -> onTouchEditGuestCount());
        btnTouchBan = createMintPill("", 230, 38, e -> onTouchEditTable());
        panel.add(createTouchInfoRow("SỐ KHÁCH", btnTouchSoKhach, new JLabel("BÀN"), btnTouchBan));

        return panel;
    }

    private JPanel createTouchDeliveryFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(2, 4, 2, 4));

        btnTouchGiaoHang = createMintPill("Chọn địa chỉ giao hàng", 410, 38, e -> onTouchEditDeliveryAddress());
        panel.add(createTouchInfoRow("GIAO HÀNG", btnTouchGiaoHang));

        btnTouchNhanVienGiao = createMintPill("Chọn nhân viên giao", 410, 38, e -> onTouchPickDeliveryEmployee());
        panel.add(createTouchInfoRow("N.VIÊN GIAO", btnTouchNhanVienGiao));

        btnTouchTrichNhanVien = createMintPill("0", 410, 38, e -> onTouchEditDeliveryCommission());
        panel.add(createTouchInfoRow("TRÍCH N.VIÊN", btnTouchTrichNhanVien));

        btnTouchGhiChuGiao = createMintPill("Ghi chú giao hàng", 410, 38, e -> onTouchEditDeliveryNote());
        panel.add(createTouchInfoRow("GHI CHÚ", btnTouchGhiChuGiao));

        return panel;
    }

    private JButton createMintPill(String text, int width, int height, ActionListener action) {
        JButton btn = new JButton(text != null ? text : "");
        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(new Color(0, 45, 0));
        btn.setBackground(new Color(212, 248, 212)); // #D4F8D4
        btn.setBorder(new LineBorder(new Color(85, 170, 85), 1, true));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (action != null) {
            btn.addActionListener(action);
        }
        return btn;
    }

    private void onTouchPickDate() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TouchDatePickerDialog dlg = new TouchDatePickerDialog(owner, DateUtil.formatDate(dpkNgay.getDate()));
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getResultDate() != null) {
            Date d = DateUtil.parseDate(dlg.getResultDate());
            if (d != null) {
                dpkNgay.setDate(d);
                btnTouchNgay.setText(dlg.getResultDate());
            }
        }
    }

    private void onTouchChonLoaiGiaGeneral() {
        MatHang ref = null;
        double curPrice = 0;
        DonHangChiTiet selectedCt = getSelectedCartItem();
        if (selectedCt != null) {
            ref = matHangDAO.findById(selectedCt.getMatHangId());
            curPrice = selectedCt.getDonGia();
        } else if (!currentLayHangList.isEmpty()) {
            ref = matHangDAO.findById(currentLayHangList.get(0).getMatHangId());
            curPrice = currentLayHangList.get(0).getDonGia();
        }
        Window owner = SwingUtilities.getWindowAncestor(this);
        Double picked = TouchChonGiaBanDialog.showDialog(owner, ref, curPrice);
        if (picked != null && picked > 0) {
            if (selectedCt != null) {
                selectedCt.setDonGia(picked);
                selectedCt.tinhThanhTien();
            } else {
                for (DonHangChiTiet ct : currentLayHangList) {
                    ct.setDonGia(picked);
                    ct.tinhThanhTien();
                }
            }
            refreshLayHangTable(selectedCartIndex);
            refreshTouchCartList(selectedCartIndex);
            recalculateFinancials();
        }
    }

    private void onTouchPickCustomer() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TouchTimKiemKhachHangDialog.CustomerSearchResult res = TouchTimKiemKhachHangDialog.showDialog(owner);
        if (res.isConfirmed() && res.getCustomer() != null) {
            selectCustomerById(res.getCustomer().getId());
            updateTouchCustomerDisplay();
        } else if (res.isCleared()) {
            cbKhachHang.setSelectedIndex(0);
            updateTouchCustomerDisplay();
        }
    }

    private void updateTouchCustomerDisplay() {
        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (btnTouchKhachHang != null) {
            btnTouchKhachHang.setText(kh != null && kh.getName() != null ? kh.getName() : "Khách lẻ");
        }
        if (btnTouchDiem != null) {
            btnTouchDiem.setText(kh != null ? CurrencyUtil.format(kh.getDiemTichLuy()) : "0");
        }
        if (kh != null && kh.getDiaChi() != null && !kh.getDiaChi().isEmpty()) {
            txtDiaChiGiaoHang.setText(kh.getDiaChi());
            if (btnTouchGiaoHang != null) btnTouchGiaoHang.setText(kh.getDiaChi());
        }
    }

    private void onTouchPickWarehouse() {
        List<KhoHang> list = khoHangDAO.getAll();
        if (list.isEmpty()) return;
        KhoHang[] array = list.toArray(new KhoHang[0]);
        KhoHang selected = (KhoHang) JOptionPane.showInputDialog(this, "Chọn kho xuất hàng:", "KHO HÀNG",
                JOptionPane.PLAIN_MESSAGE, null, array, cbKhoXuat.getSelectedItem());
        if (selected != null) {
            cbKhoXuat.setSelectedItem(selected);
            if (btnTouchKho != null) btnTouchKho.setText(selected.getName());
        }
    }

    private void onTouchPickEmployee() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        NhanVien currentNv = (NhanVien) cbNhanVien.getSelectedItem();
        TouchChonNhanVienDialog dlg = new TouchChonNhanVienDialog(owner, currentNv);
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getSelectedEmployee() != null) {
            NhanVien nv = dlg.getSelectedEmployee();
            for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
                if (cbNhanVien.getItemAt(i).getId().equals(nv.getId())) {
                    cbNhanVien.setSelectedIndex(i);
                    break;
                }
            }
            if (btnTouchNhanVien != null) btnTouchNhanVien.setText(nv.getName());
        }
    }

    private void onTouchEditGuestCount() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        int cur = 0;
        try { cur = Integer.parseInt(btnTouchSoKhach.getText().trim()); } catch (Exception ignored) {}
        Integer val = TouchNumberInputDialog.showDialog(owner, "SỐ KHÁCH", cur, 0, 999);
        if (val != null) {
            btnTouchSoKhach.setText(String.valueOf(val));
            if (txtTouchGuestCount != null) txtTouchGuestCount.setText(String.valueOf(val));
        }
    }

    private void onTouchEditTable() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String val = TouchTextKeyboardDialog.showDialog(owner, "NHẬP BÀN / PHÒNG", btnTouchBan.getText().trim());
        if (val != null) {
            btnTouchBan.setText(val);
            if (txtTouchTable != null) txtTouchTable.setText(val);
        }
    }

    private void onTouchEditDeliveryAddress() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String val = TouchTextKeyboardDialog.showDialog(owner, "ĐỊA CHỈ / NGƯỜI NHẬN HÀNG", txtDiaChiGiaoHang.getText().trim());
        if (val != null) {
            txtDiaChiGiaoHang.setText(val);
            btnTouchGiaoHang.setText(val.isEmpty() ? "Chọn địa chỉ giao hàng" : val);
        }
    }

    private void onTouchPickDeliveryEmployee() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TouchChonNhanVienDialog dlg = new TouchChonNhanVienDialog(owner, deliveryNhanVien);
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getSelectedEmployee() != null) {
            deliveryNhanVien = dlg.getSelectedEmployee();
            btnTouchNhanVienGiao.setText(deliveryNhanVien.getName());
        }
    }

    private void onTouchEditDeliveryCommission() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        Double val = TouchNumberInputDialog.showDoubleDialog(owner, "TRÍCH HOA HỒNG NHÂN VIÊN GIAO", deliveryTrichNhanVien, 0, 999999999);
        if (val != null) {
            deliveryTrichNhanVien = val;
            btnTouchTrichNhanVien.setText(String.format("%,.0f", deliveryTrichNhanVien));
        }
    }

    private void onTouchEditDeliveryNote() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        String val = TouchTextKeyboardDialog.showDialog(owner, "GHI CHÚ GIAO HÀNG", deliveryGhiChu);
        if (val != null) {
            deliveryGhiChu = val;
            btnTouchGhiChuGiao.setText(deliveryGhiChu.isEmpty() ? "Ghi chú giao hàng" : deliveryGhiChu);
        }
    }

    private JPanel createTouchInfoRow(String label, Component... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        JLabel rowLabel = createTouchLabel(label);
        rowLabel.setForeground(Color.BLACK);
        rowLabel.setPreferredSize(new Dimension(96, 38));
        row.add(rowLabel);
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel inlineLabel = (JLabel) component;
                inlineLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                inlineLabel.setForeground(Color.BLACK);
            }
            row.add(component);
        }
        return row;
    }

    private JPanel createTouchSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(4, 12, 4, 12));

        addTouchSummaryRow(panel, 0, "TIỀN HÀNG", txtTienHang, false);

        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        discountPanel.setBackground(Color.WHITE);

        styleTouchInput(txtTiLeGiamGia, 85, 38, false);
        txtTiLeGiamGia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        txtTiLeGiamGia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window owner = SwingUtilities.getWindowAncestor(HoaDonBanHangPanel.this);
                Double val = TouchNumberInputDialog.showDoubleDialog(owner, "TỈ LỆ GIẢM GIÁ (%)", parsePercentField(txtTiLeGiamGia), 0, 100);
                if (val != null) {
                    txtTiLeGiamGia.setText(String.format("%.0f %%", val));
                    recalculateFinancials();
                }
            }
        });

        styleTouchInput(txtTienGiamGia, 160, 38, true);
        JLabel lblPercent = new JLabel("%");
        lblPercent.setFont(new Font("Segoe UI", Font.BOLD, 16));

        discountPanel.add(txtTiLeGiamGia);
        discountPanel.add(lblPercent);
        discountPanel.add(txtTienGiamGia);
        addTouchSummaryRow(panel, 1, "TỈ LỆ GIẢM GIÁ", discountPanel, false);

        addTouchSummaryRow(panel, 2, "TỔNG CỘNG", txtTongCong, true);
        return panel;
    }

    private void addTouchSummaryRow(JPanel panel, int rowIndex, String label, Component value, boolean total) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = rowIndex;
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel text = new JLabel(label);
        text.setFont(new Font("Segoe UI", Font.BOLD, total ? 22 : 16));
        panel.add(text, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        if (value instanceof JTextField) {
            JTextField field = (JTextField) value;
            styleTouchInput(field, 220, total ? 46 : 38, true);
            field.setFont(new Font("Segoe UI", Font.BOLD, total ? 26 : 18));
            if (total) field.setForeground(new Color(205, 20, 0));
        }
        panel.add(value, gbc);
    }

    private JPanel createTouchCatalogActionBar() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 7, 0));
        panel.setBackground(TOUCH_BLUE);
        panel.setPreferredSize(new Dimension(0, 72));
        panel.add(createTouchActionButton("TÌM MẶT HÀNG", new Color(17, 21, 116), e -> onTouchTimKiemMatHang()));

        btnThongKe = createTouchActionButton("THỐNG KÊ", TOUCH_GREEN, e -> onThongKe());
        panel.add(btnThongKe);
        btnMoLai = createTouchActionButton("MỞ LẠI", new Color(105, 97, 12), e -> onMoLaiTam());
        panel.add(btnMoLai);
        btnLuuTam = createTouchActionButton("LƯU TẠM", new Color(105, 97, 12), e -> onLuuTam());
        panel.add(btnLuuTam);
        panel.add(createTouchActionButton("TRẢ HÀNG", TOUCH_TEAL, e -> onChonMatHangTra()));
        return panel;
    }

    private void onTouchTimKiemMatHang() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        TouchTimKiemMatHangDialog dlg = new TouchTimKiemMatHangDialog(owner);
        dlg.setVisible(true);
        if (dlg.isConfirmed() && dlg.getSelectedProduct() != null) {
            onTouchProductCardClicked(dlg.getSelectedProduct());
        }
    }

    private JPanel createTouchInvoiceActionBar() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 7, 0));
        panel.setBackground(TOUCH_BLUE);
        panel.setPreferredSize(new Dimension(0, 72));
        btnThanhToan = createTouchActionButton("THANH TOÁN", new Color(92, 0, 102), e -> onThanhToan());
        btnTaoHoaDonMoi = createTouchActionButton("TẠO MỚI", TOUCH_GREEN, e -> resetForNewInvoice());
        btnMoDatHang = createTouchActionButton("MỞ ĐẶT HÀNG", TOUCH_TEAL, e -> onOpenDatHang());
        JButton exit = createTouchActionButton("THOÁT", new Color(165, 10, 10), e -> closeTouchWindow());
        panel.add(btnThanhToan);
        panel.add(btnTaoHoaDonMoi);
        panel.add(btnMoDatHang);
        panel.add(exit);
        return panel;
    }

    private JButton createTouchActionButton(String text, Color color, ActionListener action) {
        JButton button = new JButton("<html><center>" + text.replace(" ", "&nbsp;") + "</center></html>");
        styleTouchButton(button, color, 120, 68);
        button.addActionListener(action);
        return button;
    }

    private void styleTouchButton(JButton button, Color background, int width, int height) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 17));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(width, height));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 235, 240), 2, true),
                new EmptyBorder(4, 6, 4, 6)));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    private void styleTouchInput(JComponent component, int width, int height, boolean readOnly) {
        component.setPreferredSize(new Dimension(width, height));
        component.setMinimumSize(new Dimension(Math.min(width, 70), height));
        component.setMaximumSize(new Dimension(width, height));
        component.setFont(new Font("Segoe UI", Font.BOLD, 16));
        component.setBackground(TOUCH_FIELD);
        if (component instanceof JTextField && readOnly) {
            ((JTextField) component).setEditable(false);
        }
    }

    private JLabel createTouchLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(Color.WHITE);
        return label;
    }

    private void refreshTouchCategoryButtons() {
        if (!touchMode || touchCategoryPanel == null || treeNhomHang == null) return;
        touchCategoryPanel.removeAll();
        Object rootObject = treeNhomHang.getModel().getRoot();
        if (!(rootObject instanceof DefaultMutableTreeNode)) return;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) rootObject;
        addTouchCategoryButton("TẤT CẢ", root, new Color(0, 17, 68));
        Enumeration<?> children = root.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
            addTouchCategoryButton(String.valueOf(node.getUserObject()), node, new Color(0, 80, 32));
        }
        touchCategoryPanel.revalidate();
        touchCategoryPanel.repaint();
    }

    private void addTouchCategoryButton(String text, DefaultMutableTreeNode node, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setPreferredSize(new Dimension(140, 46));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 235, 240), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        button.addActionListener(e -> {
            treeNhomHang.setSelectionPath(new TreePath(node.getPath()));
            filterCatalogByKeyword();
        });
        touchCategoryPanel.add(button);
        touchCategoryPanel.add(Box.createVerticalStrut(4));
    }

    private void refreshTouchProductGrid() {
        if (!touchMode || touchProductGrid == null) return;
        touchProductGrid.removeAll();
        TouchBanHangProductColumnConfig cfg = TouchBanHangProductConfigDAO.loadConfig();

        for (int i = 0; i < filteredCatalogList.size(); i++) {
            MatHang item = filteredCatalogList.get(i);
            boolean isSelected = (selectedTouchProduct != null && selectedTouchProduct.getId() != null && selectedTouchProduct.getId().equals(item.getId()));

            JPanel card = new JPanel(new BorderLayout(6, 0));
            card.setOpaque(true);
            card.setBackground(isSelected ? new Color(211, 84, 0) : new Color(0, 79, 76));
            card.setBorder(new CompoundBorder(
                    new LineBorder(isSelected ? Color.WHITE : new Color(0, 110, 110), 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            String col1Text = (!cfg.getUsedColumns().isEmpty()) ? TouchBanHangProductColumnConfig.getFormattedText(item, cfg.getUsedColumns().get(0), true) : item.getName();
            JLabel lblLeft = new JLabel("<html><div style='width:130px;'>" + escapeHtml(col1Text) + "</div></html>");
            lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblLeft.setForeground(Color.WHITE);
            card.add(lblLeft, BorderLayout.WEST);

            String col2Text = (cfg.getUsedColumns().size() > 1) ? TouchBanHangProductColumnConfig.getFormattedText(item, cfg.getUsedColumns().get(1), false) : CurrencyUtil.format(item.getGiaBan());
            JLabel lblRight = new JLabel(col2Text, SwingConstants.RIGHT);
            lblRight.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblRight.setForeground(Color.WHITE);
            card.add(lblRight, BorderLayout.EAST);

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onTouchProductCardClicked(item);
                }
            });

            touchProductGrid.add(card);
        }

        int rows = Math.max(1, (filteredCatalogList.size() + 1) / 2);
        touchProductGrid.setPreferredSize(new Dimension(0, rows * 64));
        touchProductGrid.revalidate();
        touchProductGrid.repaint();
    }

    private void onTouchProductCardClicked(MatHang item) {
        if (item == null) return;
        selectedTouchProduct = item;

        // 1. Read quantity from txtSoLuongBarcode (default 1)
        double qty = 1.0;
        try {
            qty = Double.parseDouble(txtSoLuongBarcode.getText().trim().replace(",", "."));
            if (qty <= 0) qty = 1.0;
        } catch (Exception e) {
            qty = 1.0;
        }

        // 2. Read selling price
        double price = item.getGiaBan();

        // 3. Find if already in cart
        DonHangChiTiet existing = null;
        for (DonHangChiTiet ct : currentLayHangList) {
            if (item.getId() != null && item.getId().equals(ct.getMatHangId()) && Math.abs(ct.getDonGia() - price) < 0.001) {
                existing = ct;
                break;
            }
        }

        int targetIndex;
        if (existing != null) {
            existing.setSoLuong(existing.getSoLuong() + qty);
            existing.tinhThanhTien();
            targetIndex = currentLayHangList.indexOf(existing);
        } else {
            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setId(UUID.randomUUID().toString());
            ct.setMatHangId(item.getId());
            ct.setMaHang(item.getCode());
            ct.setTenHang(item.getName());
            ct.setDonViTinhId(item.getDonViTinhId());
            ct.setTenDonViTinh(item.getTenDonViTinh());
            ct.setDonGia(price);
            ct.setSoLuong(qty);
            ct.setTiLeGiamGia(0.0);
            ct.setBaoHanh(item.getBaoHanh());
            ct.tinhThanhTien();
            currentLayHangList.add(ct);
            targetIndex = currentLayHangList.size() - 1;
        }

        txtSoLuongBarcode.setText("1");
        refreshLayHangTable(targetIndex);
        refreshTouchCartList(targetIndex);
        recalculateFinancials();
        refreshTouchProductGrid();
    }

    private void onBarcodeAddTouch() {
        String code = txtBarcode.getText().trim();
        if (code.isEmpty()) return;
        MatHang found = null;
        for (MatHang m : allMatHangList) {
            if (code.equalsIgnoreCase(m.getCode()) || code.equalsIgnoreCase(m.getMaSanCo())) {
                found = m;
                break;
            }
        }
        if (found == null) {
            for (MatHang m : allMatHangList) {
                if (m.getName() != null && m.getName().toLowerCase().contains(code.toLowerCase())) {
                    found = m;
                    break;
                }
            }
        }
        if (found != null) {
            onTouchProductCardClicked(found);
            txtBarcode.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mặt hàng với mã: " + code, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String escapeHtml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private void toggleTouchWindowSize() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (!(window instanceof JFrame)) return;
        JFrame frame = (JFrame) window;
        boolean maximized = (frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
        frame.setExtendedState(maximized ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH);
        if (maximized) {
            frame.setSize(1180, 760);
            frame.setLocationRelativeTo(null);
        }
    }

    private void closeTouchWindow() {
        if (touchExitAction != null) {
            touchExitAction.run();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "Thoát màn hình bán hàng cảm ứng?",
                "TAP Shop TS", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) window.dispose();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(215, 230, 245));

        // Top Barcode Search Toolbar
        add(createTopBarcodePanel(), BorderLayout.NORTH);

        // Center SplitPane: Left (Category + Catalog) vs Right (Master Header + Details + Totals)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplit.setDividerLocation(380);
        mainSplit.setDividerSize(5);

        mainSplit.setLeftComponent(createLeftCatalogPanel());
        mainSplit.setRightComponent(createRightInvoicePanel());

        add(mainSplit, BorderLayout.CENTER);
    }

    private JPanel createTopBarcodePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setBackground(new Color(230, 240, 252));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 210, 235)));

        JLabel lblMavach = new JLabel("Mã vạch (F3):");
        lblMavach.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtBarcode = new JTextField(16);
        txtBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblSl = new JLabel("SL:");
        lblSl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoLuongBarcode = new JTextField("1", 4);
        txtSoLuongBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnThemBarcode = new JButton("Thêm >>");
        btnThemBarcode.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnThemBarcode.setBackground(Color.WHITE);
        btnThemBarcode.addActionListener(e -> onBarcodeAdd());

        txtBarcode.addActionListener(e -> onBarcodeAdd());
        txtBarcode.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
            @Override public void removeUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
            @Override public void changedUpdate(DocumentEvent e) { filterCatalogByKeyword(); }
        });

        panel.add(lblMavach);
        panel.add(txtBarcode);
        panel.add(lblSl);
        panel.add(txtSoLuongBarcode);
        panel.add(btnThemBarcode);

        return panel;
    }

    private JPanel createLeftCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(4, 4, 4, 2));

        JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitLeft.setDividerLocation(180);
        splitLeft.setDividerSize(4);

        // Category Tree
        treeNhomHang = new JTree();
        treeNhomHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconHelper.getIcon("folder.png", 16, 16));
        renderer.setOpenIcon(IconHelper.getIcon("Folder.png", 16, 16));
        renderer.setClosedIcon(IconHelper.getIcon("Folder.png", 16, 16));
        treeNhomHang.setCellRenderer(renderer);

        treeNhomHang.addTreeSelectionListener(e -> onTreeSelectionChanged());
        JScrollPane spTree = new JScrollPane(treeNhomHang);
        splitLeft.setTopComponent(spTree);

        // Catalog Table
        tableCatalog = new JTable();
        tableCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableCatalog.setRowHeight(22);
        tableCatalog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableCatalog.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableCatalog.getTableHeader().setBackground(new Color(235, 242, 252));

        rebuildCatalogTableColumns();

        // Popup context menu for catalog table columns
        popupCatalog = new JPopupMenu();
        JMenuItem miChonCotCatalog = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table.png", 16, 16));
        miChonCotCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miChonCotCatalog.addActionListener(e -> openChonCotHienThiCatalogDialog());

        JMenuItem miKhoiPhucCotCatalog = new JMenuItem("Khôi phục cột mặc định");
        miKhoiPhucCotCatalog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miKhoiPhucCotCatalog.addActionListener(e -> {
            visibleCatalogColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS);
            rebuildCatalogTableColumns();
            refreshCatalogTable();
        });

        popupCatalog.add(miChonCotCatalog);
        popupCatalog.add(miKhoiPhucCotCatalog);

        tableCatalog.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openChonCotHienThiCatalogDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableCatalog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateTonKhoDisplay();
                if (e.getClickCount() == 2) {
                    onCatalogItemSelected();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableCatalog.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableCatalog.getRowCount()) {
                        tableCatalog.setRowSelectionInterval(r, r);
                        updateTonKhoDisplay();
                    }
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableCatalog.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableCatalog.getRowCount()) {
                        tableCatalog.setRowSelectionInterval(r, r);
                        updateTonKhoDisplay();
                    }
                    popupCatalog.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableCatalog.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    onCatalogItemSelected();
                }
            }
        });

        JScrollPane spTable = new JScrollPane(tableCatalog);
        splitLeft.setBottomComponent(spTable);

        panel.add(splitLeft, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(215, 230, 245));

        // 1. Master Header Form
        panel.add(createMasterHeaderForm(), BorderLayout.NORTH);

        // 2. Middle Center: Vertical Mini-Toolbar + Details TabbedPane
        JPanel pnlCenter = new JPanel(new BorderLayout(4, 0));
        pnlCenter.setOpaque(false);
        pnlCenter.setBorder(new EmptyBorder(4, 4, 4, 4));

        pnlCenter.add(createMiniVerticalToolbar(), BorderLayout.WEST);
        pnlCenter.add(createDetailsTabbedPane(), BorderLayout.CENTER);

        panel.add(pnlCenter, BorderLayout.CENTER);

        // 3. Bottom: Bottom Actions (Left) + Financial Summary (Right)
        JPanel pnlBottom = new JPanel(new BorderLayout(8, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(0, 4, 4, 4));

        pnlBottom.add(createBottomActionButtons(), BorderLayout.WEST);
        pnlBottom.add(createFinancialSummaryPanel(), BorderLayout.EAST);

        panel.add(pnlBottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMasterHeaderForm() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(800, 130));
        panel.setBackground(new Color(215, 230, 245));
        panel.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Navigation Arrows
        JButton btnPrev = new JButton(IconHelper.getIcon("arrow-180.png", 14, 14));
        btnPrev.setBounds(425, 6, 26, 24);
        btnPrev.setBackground(Color.WHITE);
        btnPrev.setToolTipText("Hóa đơn trước");
        btnPrev.addActionListener(e -> onNavigateInvoice(-1));
        panel.add(btnPrev);

        // Header Title in large bold red text
        JLabel lblMainTitle = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblMainTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMainTitle.setForeground(new Color(160, 25, 25));
        lblMainTitle.setBounds(460, 4, 250, 28);
        panel.add(lblMainTitle);

        JButton btnNext = new JButton(IconHelper.getIcon("arrow-000.png", 14, 14));
        btnNext.setBounds(720, 6, 26, 24);
        btnNext.setBackground(Color.WHITE);
        btnNext.setToolTipText("Hóa đơn sau");
        btnNext.addActionListener(e -> onNavigateInvoice(1));
        panel.add(btnNext);

        // Row 1: Ngày, Số HĐ
        JLabel lblNgay = new JLabel("Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNgay.setBounds(10, 8, 50, 22);
        panel.add(lblNgay);

        dpkNgay = new DatePickerField();
        dpkNgay.setBounds(50, 8, 125, 24);
        panel.add(dpkNgay);

        JLabel lblSoHD = new JLabel("Số HĐ");
        lblSoHD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSoHD.setBounds(185, 8, 45, 22);
        panel.add(lblSoHD);

        txtSoHD = new JTextField();
        txtSoHD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtSoHD.setBackground(new Color(255, 254, 200));
        txtSoHD.setBounds(230, 8, 110, 24);
        panel.add(txtSoHD);

        // Row 2: K. hàng (F6)
        JLabel lblKhach = new JLabel("K. hàng (F6)");
        lblKhach.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKhach.setBounds(10, 36, 75, 22);
        panel.add(lblKhach);

        cbKhachHang = new JComboBox<>();
        cbKhachHang.addActionListener(e -> onCustomerSelected());

        customerLookupField = createCustomerLookupField();
        customerLookupField.setBounds(90, 36, 890, 24);
        panel.add(customerLookupField);

        // Row 3: Kho xuất, Nhân viên
        JLabel lblKho = new JLabel("Kho xuất");
        lblKho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblKho.setBounds(10, 64, 75, 22);
        panel.add(lblKho);

        cbKhoXuat = new JComboBox<>();
        cbKhoXuat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbKhoXuat.setBackground(new Color(235, 248, 200));
        cbKhoXuat.setBounds(90, 64, 250, 24);
        panel.add(cbKhoXuat);

        JLabel lblNv = new JLabel("Nhân viên");
        lblNv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNv.setBounds(360, 64, 70, 22);
        panel.add(lblNv);

        cbNhanVien = new JComboBox<>();
        cbNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbNhanVien.setBounds(435, 64, 545, 24);
        panel.add(cbNhanVien);

        // Row 4: Diễn giải, Đc giao hàng
        JLabel lblDiengiai = new JLabel("Diễn giải");
        lblDiengiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDiengiai.setBounds(10, 92, 75, 22);
        panel.add(lblDiengiai);

        txtDienGiai = new JTextField("Xuất bán hàng");
        txtDienGiai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDienGiai.setBounds(90, 92, 250, 24);
        panel.add(txtDienGiai);

        JLabel lblDcGiao = new JLabel("Đc giao hàng");
        lblDcGiao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDcGiao.setBounds(360, 92, 75, 22);
        panel.add(lblDcGiao);

        txtDiaChiGiaoHang = new JTextField();
        txtDiaChiGiaoHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDiaChiGiaoHang.setBounds(435, 92, 545, 24);
        panel.add(txtDiaChiGiaoHang);

        return panel;
    }

    private JPanel createMiniVerticalToolbar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(85, 260));

        btnMoDatHang = createMiniButton("Mở đặt hàng");
        btnMoDatHang.addActionListener(e -> onOpenDatHang());

        btnLuuTam = createMiniButton("Lưu tạm (F9)");
        btnLuuTam.addActionListener(e -> onLuuTam());

        btnMoLai = createMiniButton("Mở lại (F11)");
        btnMoLai.addActionListener(e -> onMoLaiTam());

        lblTonKho = new JLabel("TỒN: 0", SwingConstants.CENTER);
        lblTonKho.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTonKho.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTonKho.setMaximumSize(new Dimension(85, 22));

        btnXoaDong = createMiniButton("❌ Xóa");
        btnXoaDong.addActionListener(e -> onDeleteCurrentItem());

        btnTangSL = createMiniButton("Tăng sl 1");
        btnTangSL.addActionListener(e -> onAdjustQuantity(1));

        btnGiamSL = createMiniButton("Giảm sl 1");
        btnGiamSL.addActionListener(e -> onAdjustQuantity(-1));

        btnDatSL = createMiniButton("Đặt sl (F2)");
        btnDatSL.addActionListener(e -> onEditQuantityDialog());

        panel.add(btnMoDatHang);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnLuuTam);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnMoLai);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblTonKho);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnXoaDong);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnTangSL);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnGiamSL);
        panel.add(Box.createVerticalStrut(3));
        panel.add(btnDatSL);

        return panel;
    }

    private JButton createMiniButton(String text) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setMaximumSize(new Dimension(85, 28));
        btn.setPreferredSize(new Dimension(85, 28));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(Color.WHITE);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

    private JPanel createCustomerLookupField() {
        JPanel field = new JPanel(new BorderLayout());
        field.setBackground(Color.WHITE);
        field.setBorder(new LineBorder(new Color(170, 190, 215), 1));

        txtKhachHangLookup = new JTextField();
        txtKhachHangLookup.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtKhachHangLookup.setBorder(new EmptyBorder(0, 5, 0, 3));
        field.add(txtKhachHangLookup, BorderLayout.CENTER);

        JButton btnDropdown = new JButton("▾");
        btnDropdown.setPreferredSize(new Dimension(24, 22));
        btnDropdown.setMargin(new Insets(0, 0, 0, 0));
        btnDropdown.setFocusPainted(false);
        btnDropdown.setBackground(Color.WHITE);
        btnDropdown.setBorder(new MatteBorder(0, 1, 0, 0, new Color(190, 205, 225)));
        btnDropdown.addActionListener(e -> toggleCustomerPopup());
        field.add(btnDropdown, BorderLayout.EAST);

        initCustomerLookupPopup();
        txtKhachHangLookup.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onCustomerLookupTextChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onCustomerLookupTextChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onCustomerLookupTextChanged(); }
        });
        txtKhachHangLookup.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    showCustomerPopup();
                    moveCustomerLookupSelection(1);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    showCustomerPopup();
                    moveCustomerLookupSelection(-1);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chooseLookupCustomerAndClose();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupKhachHang.setVisible(false);
                }
            }
        });
        return field;
    }

    private void initCustomerLookupPopup() {
        popupKhachHang = new JPopupMenu();
        // Không để popup chiếm focus khi nó tự mở sau ký tự tìm kiếm đầu tiên.
        popupKhachHang.setFocusable(false);
        popupKhachHang.setBorder(new LineBorder(new Color(160, 185, 215), 1));

        JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(890, 245));
        content.setBackground(Color.WHITE);

        String[] columns = {"Tên khách hàng", "Mã khách", "Địa chỉ", "Điện thoại", "Email"};
        lookupKhachHangModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableLookupKhachHang = new JTable(lookupKhachHangModel);
        tableLookupKhachHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableLookupKhachHang.setRowHeight(24);
        tableLookupKhachHang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableLookupKhachHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLookupKhachHang.getTableHeader().setBackground(new Color(245, 248, 252));
        tableLookupKhachHang.setSelectionBackground(new Color(225, 245, 250));
        tableLookupKhachHang.setSelectionForeground(Color.BLACK);
        tableLookupKhachHang.getColumnModel().getColumn(0).setPreferredWidth(190);
        tableLookupKhachHang.getColumnModel().getColumn(1).setPreferredWidth(110);
        tableLookupKhachHang.getColumnModel().getColumn(2).setPreferredWidth(230);
        tableLookupKhachHang.getColumnModel().getColumn(3).setPreferredWidth(130);
        tableLookupKhachHang.getColumnModel().getColumn(4).setPreferredWidth(200);
        tableLookupKhachHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableLookupKhachHang.getSelectedRow();
                if (row >= 0 && row < filteredLookupKhachHangList.size()) {
                    selectLookupCustomer(filteredLookupKhachHangList.get(row));
                    if (e.getClickCount() == 2) popupKhachHang.setVisible(false);
                }
            }
        });
        tableLookupKhachHang.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chooseLookupCustomerAndClose();
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popupKhachHang.setVisible(false);
                    txtKhachHangLookup.requestFocusInWindow();
                }
            }
        });
        content.add(new JScrollPane(tableLookupKhachHang), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 4, 4, 0));
        actions.setBorder(new EmptyBorder(4, 4, 4, 4));
        actions.setBackground(new Color(240, 245, 252));
        JButton btnThem = createCustomerPopupButton("+ Thêm", "plus.png", new Color(0, 130, 60));
        JButton btnSua = createCustomerPopupButton("Sửa", "pencil.png", Color.BLACK);
        JButton btnTai = createCustomerPopupButton("Tải", "Refresh.png", new Color(0, 80, 180));
        JButton btnDanhMuc = createCustomerPopupButton("Danh mục", "table.png", Color.BLACK);
        btnThem.addActionListener(e -> addCustomerFromLookup());
        btnSua.addActionListener(e -> editCustomerFromLookup());
        btnTai.addActionListener(e -> reloadCustomerLookupData(true));
        btnDanhMuc.addActionListener(e -> openCustomerCatalog());
        actions.add(btnThem);
        actions.add(btnSua);
        actions.add(btnTai);
        actions.add(btnDanhMuc);
        content.add(actions, BorderLayout.SOUTH);
        popupKhachHang.add(content);
    }

    private JButton createCustomerPopupButton(String text, String icon, Color foreground) {
        JButton button = new JButton(text, IconHelper.getIcon(icon, 14, 14));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(foreground);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void onCustomerLookupTextChanged() {
        if (isUpdatingCustomerLookup) return;
        isUpdatingCustomerLookup = true;
        try {
            cbKhachHang.setSelectedItem(null);
        } finally {
            isUpdatingCustomerLookup = false;
        }
        filterLookupCustomers(txtKhachHangLookup.getText());
        if (txtKhachHangLookup.hasFocus()) showCustomerPopup();
    }

    private void showCustomerPopup() {
        if (!customerLookupField.isShowing()) return;
        filterLookupCustomers(txtKhachHangLookup.getText());
        if (!popupKhachHang.isVisible()) {
            popupKhachHang.show(customerLookupField, 0, customerLookupField.getHeight());
            SwingUtilities.invokeLater(() -> txtKhachHangLookup.requestFocusInWindow());
        }
    }

    private void moveCustomerLookupSelection(int direction) {
        int rowCount = tableLookupKhachHang.getRowCount();
        if (rowCount <= 0) return;
        int currentRow = tableLookupKhachHang.getSelectedRow();
        int targetRow = currentRow < 0
                ? (direction > 0 ? 0 : rowCount - 1)
                : Math.max(0, Math.min(rowCount - 1, currentRow + direction));
        tableLookupKhachHang.setRowSelectionInterval(targetRow, targetRow);
        tableLookupKhachHang.scrollRectToVisible(tableLookupKhachHang.getCellRect(targetRow, 0, true));
    }

    private void toggleCustomerPopup() {
        if (popupKhachHang.isVisible()) popupKhachHang.setVisible(false);
        else showCustomerPopup();
    }

    private void filterLookupCustomers(String keyword) {
        String value = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";
        filteredLookupKhachHangList.clear();
        lookupKhachHangModel.setRowCount(0);
        for (KhachHang customer : allLookupKhachHangList) {
            if (!matchesCustomer(customer, value)) continue;
            filteredLookupKhachHangList.add(customer);
            lookupKhachHangModel.addRow(new Object[]{
                    safeCustomerValue(customer.getName()), safeCustomerValue(customer.getMaKhach()),
                    safeCustomerValue(customer.getDiaChi()), safeCustomerValue(customer.getDienThoai()),
                    safeCustomerValue(customer.getEmail())
            });
        }
    }

    private boolean matchesCustomer(KhachHang customer, String keyword) {
        if (keyword.isEmpty()) return true;
        return containsCustomerValue(customer.getName(), keyword)
                || containsCustomerValue(customer.getMaKhach(), keyword)
                || containsCustomerValue(customer.getDiaChi(), keyword)
                || containsCustomerValue(customer.getDienThoai(), keyword)
                || containsCustomerValue(customer.getEmail(), keyword);
    }

    private boolean containsCustomerValue(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String safeCustomerValue(String value) {
        return value != null ? value : "";
    }

    private void chooseLookupCustomerAndClose() {
        int row = tableLookupKhachHang.getSelectedRow();
        if (row < 0 && filteredLookupKhachHangList.size() == 1) row = 0;
        if (row >= 0 && row < filteredLookupKhachHangList.size()) {
            selectLookupCustomer(filteredLookupKhachHangList.get(row));
        }
        popupKhachHang.setVisible(false);
    }

    private void selectLookupCustomer(KhachHang customer) {
        if (customer == null) return;
        isUpdatingCustomerLookup = true;
        try {
            cbKhachHang.setSelectedItem(customer);
            txtKhachHangLookup.setText(customer.toString());
        } finally {
            isUpdatingCustomerLookup = false;
        }
        onCustomerSelected();
    }

    private void reloadCustomerLookupData(boolean preserveSelection) {
        KhachHang selected = (KhachHang) cbKhachHang.getSelectedItem();
        String selectedId = preserveSelection && selected != null ? selected.getId() : null;

        allLookupKhachHangList.clear();
        KhachHang walkInCustomer = new KhachHang();
        walkInCustomer.setName("Khách lẻ");
        allLookupKhachHangList.add(walkInCustomer);
        for (KhachHang customer : khachHangDAO.getAll()) {
            if (customer.getStatus() >= 0) allLookupKhachHangList.add(customer);
        }

        isUpdatingCustomerLookup = true;
        try {
            cbKhachHang.removeAllItems();
            for (KhachHang customer : allLookupKhachHangList) cbKhachHang.addItem(customer);
            int selectedIndex = 0;
            if (selectedId != null) {
                for (int i = 1; i < cbKhachHang.getItemCount(); i++) {
                    if (selectedId.equals(cbKhachHang.getItemAt(i).getId())) {
                        selectedIndex = i;
                        break;
                    }
                }
            }
            if (cbKhachHang.getItemCount() > 0) cbKhachHang.setSelectedIndex(selectedIndex);
            KhachHang current = (KhachHang) cbKhachHang.getSelectedItem();
            txtKhachHangLookup.setText(current != null ? current.toString() : "");
        } finally {
            isUpdatingCustomerLookup = false;
        }
        filterLookupCustomers(txtKhachHangLookup.getText());
        onCustomerSelected();
    }

    private void addCustomerFromLookup() {
        popupKhachHang.setVisible(false);
        Set<String> existingIds = new HashSet<>();
        for (KhachHang customer : allLookupKhachHangList) {
            if (customer.getId() != null) existingIds.add(customer.getId());
        }
        KhachHangEditDialog dialog = new KhachHangEditDialog(this, null, () -> {
            reloadCustomerLookupData(false);
            for (KhachHang customer : allLookupKhachHangList) {
                if (customer.getId() != null && !existingIds.contains(customer.getId())) {
                    selectLookupCustomer(customer);
                    break;
                }
            }
        });
        dialog.setVisible(true);
    }

    private void editCustomerFromLookup() {
        KhachHang customer = null;
        int row = tableLookupKhachHang.getSelectedRow();
        if (row >= 0 && row < filteredLookupKhachHangList.size()) {
            customer = filteredLookupKhachHangList.get(row);
        }
        if (customer == null) customer = (KhachHang) cbKhachHang.getSelectedItem();
        if (customer == null || customer.getId() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần sửa!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        popupKhachHang.setVisible(false);
        KhachHang customerToEdit = customer;
        KhachHangEditDialog dialog = new KhachHangEditDialog(this, customerToEdit, () -> {
            reloadCustomerLookupData(false);
            KhachHang refreshed = khachHangDAO.findActiveById(customerToEdit.getId());
            if (refreshed != null) selectLookupCustomer(refreshed);
        });
        dialog.setVisible(true);
    }

    private void openCustomerCatalog() {
        popupKhachHang.setVisible(false);
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "DANH SÁCH KHÁCH HÀNG", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(950, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        DanhMucKhachHangPanel customerPanel = new DanhMucKhachHangPanel();
        dialog.add(customerPanel, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton btnChon = new JButton("Chọn khách hàng");
        JButton btnDong = new JButton("Đóng");
        btnChon.addActionListener(e -> {
            KhachHang customer = customerPanel.getSelectedKhachHang();
            if (customer == null) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một khách hàng!",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            reloadCustomerLookupData(false);
            KhachHang refreshed = khachHangDAO.findActiveById(customer.getId());
            selectLookupCustomer(refreshed != null ? refreshed : customer);
            dialog.dispose();
        });
        btnDong.addActionListener(e -> dialog.dispose());
        actions.add(btnChon);
        actions.add(btnDong);
        dialog.add(actions, BorderLayout.SOUTH);
        customerPanel.setOnCustomerDoubleClickListener(customer -> {
            reloadCustomerLookupData(false);
            KhachHang refreshed = khachHangDAO.findActiveById(customer.getId());
            selectLookupCustomer(refreshed != null ? refreshed : customer);
            dialog.dispose();
        });
        dialog.setVisible(true);
    }

    private JTabbedPane createDetailsTabbedPane() {
        tabbedPaneDetails = new JTabbedPane();
        tabbedPaneDetails.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Tab 1: KHÁCH LẤY HÀNG
        JPanel pnlLayHang = new JPanel(new BorderLayout());
        pnlLayHang.setBackground(Color.WHITE);

        tableLayHang = new JTable();
        tableLayHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableLayHang.setRowHeight(22);
        tableLayHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableLayHang.getTableHeader().setBackground(new Color(235, 242, 252));
        rebuildLayHangTableColumns();

        // Popup context menu for columns and actions
        JPopupMenu popupLayHang = new JPopupMenu();
        JMenuItem miChonCot = new JMenuItem("Chọn cột hiển thị...", IconHelper.getIcon("table.png", 16, 16));
        miChonCot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miChonCot.addActionListener(e -> openChonCotHienThiDialog());

        JMenuItem miKhoiPhucCot = new JMenuItem("Khôi phục cột mặc định");
        miKhoiPhucCot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miKhoiPhucCot.addActionListener(e -> {
            visibleLayHangColumns = new ArrayList<>(ChonCotHienThiDialog.DEFAULT_VISIBLE_COLUMNS);
            applyColumnVisibility();
        });

        JMenuItem miDatSL = new JMenuItem("Đặt số lượng (F2)");
        miDatSL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miDatSL.addActionListener(e -> onEditQuantityDialog());

        JMenuItem miXoaDong = new JMenuItem("Xóa dòng (Delete)");
        miXoaDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        miXoaDong.addActionListener(e -> onDeleteCurrentItem());

        popupLayHang.add(miChonCot);
        popupLayHang.add(miKhoiPhucCot);
        popupLayHang.addSeparator();
        popupLayHang.add(miDatSL);
        popupLayHang.add(miXoaDong);

        tableLayHang.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openChonCotHienThiDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        tableLayHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tableLayHang.rowAtPoint(e.getPoint());
                int viewColumn = tableLayHang.columnAtPoint(e.getPoint());
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1
                        && row >= 0 && isLayHangPriceColumn(viewColumn)) {
                    tableLayHang.setRowSelectionInterval(row, row);
                    showPriceDropdown(row, viewColumn);
                    return;
                }
                if (e.getClickCount() == 2) {
                    onEditQuantityDialog();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableLayHang.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableLayHang.getRowCount()) {
                        tableLayHang.setRowSelectionInterval(r, r);
                    }
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int r = tableLayHang.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < tableLayHang.getRowCount()) {
                        tableLayHang.setRowSelectionInterval(r, r);
                    }
                    popupLayHang.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane spLayHang = new JScrollPane(tableLayHang);
        pnlLayHang.add(spLayHang, BorderLayout.CENTER);
        tabbedPaneDetails.addTab("KHÁCH LẤY HÀNG", pnlLayHang);

        // Tab 2: KHÁCH TRẢ HÀNG
        JPanel pnlTraHang = new JPanel(new BorderLayout());
        pnlTraHang.setBackground(Color.WHITE);

        JPanel pnlTraHangTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        pnlTraHangTop.setOpaque(false);
        JButton btnChonTra = new JButton("Chọn mặt hàng trả", IconHelper.getIcon("table.png", 16, 16));
        btnChonTra.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChonTra.setBackground(Color.WHITE);
        btnChonTra.addActionListener(e -> onChonMatHangTra());
        pnlTraHangTop.add(btnChonTra);
        pnlTraHang.add(pnlTraHangTop, BorderLayout.NORTH);

        String[] headersTraHang = {"STT", "Mặt hàng", "ĐVT", "Mã hàng", "Số lượng", "Đơn giá", "CK%", "Thành tiền", "Ghi chú"};
        traHangModel = new DefaultTableModel(headersTraHang, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tableTraHang = new JTable(traHangModel);
        tableTraHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTraHang.setRowHeight(22);
        tableTraHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tableTraHang.getTableHeader().setBackground(new Color(235, 242, 252));

        JScrollPane spTraHang = new JScrollPane(tableTraHang);
        pnlTraHang.add(spTraHang, BorderLayout.CENTER);
        tabbedPaneDetails.addTab("KHÁCH TRẢ HÀNG", pnlTraHang);

        return tabbedPaneDetails;
    }

    private JPanel createBottomActionButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panel.setOpaque(false);

        btnTaoHoaDonMoi = createStyledActionButton("Tạo hóa đơn<br>mới", "btnNew.Image.png", new Color(255, 255, 255), new Color(175, 195, 225), 130, 52);
        btnTaoHoaDonMoi.addActionListener(e -> resetForNewInvoice());

        btnThanhToan = createStyledActionButton("<b style='color:#901515;'>Thanh toán</b><br>(F12)", "coins.png", new Color(255, 250, 205), new Color(225, 200, 120), 135, 52);
        btnThanhToan.addActionListener(e -> onThanhToan());

        btnThongKe = createStyledActionButton("Thống kê<br>(F10)", "Statistic16.png", new Color(255, 255, 255), new Color(175, 195, 225), 120, 52);
        btnThongKe.addActionListener(e -> onThongKe());

        panel.add(btnTaoHoaDonMoi);
        panel.add(btnThanhToan);
        panel.add(btnThongKe);

        return panel;
    }

    private JButton createStyledActionButton(String htmlText, String iconName, Color bg, Color borderCol, int width, int height) {
        JButton btn = new JButton("<html><center>" + htmlText + "</center></html>");
        if (iconName != null) {
            btn.setIcon(IconHelper.getIcon(iconName, 20, 20));
        }
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(6);
        btn.setBorder(new CompoundBorder(
                new LineBorder(borderCol, 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));

        final Color normalBg = bg;
        final Color hoverBg = (bg.equals(Color.WHITE)) ? new Color(235, 244, 255) : new Color(255, 244, 180);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(normalBg);
            }
        });

        return btn;
    }

    private JPanel createFinancialSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(180, 205, 230)),
                new EmptyBorder(2, 6, 2, 6)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 4, 2, 4);

        // Tiền hàng gốc (trước chiết khấu từng mặt hàng)
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblTh = new JLabel("Tiền hàng");
        lblTh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblTh, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienHang = createCyanNumberField();
        pnlForm.add(txtTienHang, gbc);

        // Tổng chiết khấu của các dòng mặt hàng
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblCkMatHang = new JLabel("CK mặt hàng");
        lblCkMatHang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblCkMatHang, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTongChietKhauMatHang = createCyanNumberField();
        pnlForm.add(txtTongChietKhauMatHang, gbc);

        // Giảm giá toàn hóa đơn
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel lblGg = new JLabel("Giảm giá (hóa đơn)");
        lblGg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblGg, gbc);

        gbc.gridx = 1;
        txtTiLeGiamGia = new JTextField("0 %", 4);
        txtTiLeGiamGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTiLeGiamGia.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlForm.add(txtTiLeGiamGia, gbc);

        gbc.gridx = 2;
        txtTienGiamGia = createCyanNumberField();
        pnlForm.add(txtTienGiamGia, gbc);

        // Thuế suất (chỉ hiện khi bật trong cấu hình bán hàng)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblThue = new JLabel("Tỉ lệ thuế");
        lblThue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblThue, gbc);
        taxSummaryComponents.add(lblThue);

        gbc.gridx = 1;
        txtTiLeThue = new JTextField("0", 4);
        txtTiLeThue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtTiLeThue.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlForm.add(txtTiLeThue, gbc);
        taxSummaryComponents.add(txtTiLeThue);

        gbc.gridx = 2;
        txtTienThue = createCyanNumberField();
        pnlForm.add(txtTienThue, gbc);
        taxSummaryComponents.add(txtTienThue);

        // Phí vận chuyển (chỉ hiện khi bật trong cấu hình bán hàng)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblVanChuyen = new JLabel("Phí vận chuyển");
        lblVanChuyen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblVanChuyen, gbc);
        shippingSummaryComponents.add(lblVanChuyen);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtPhiVanChuyen = new JTextField("0");
        txtPhiVanChuyen.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPhiVanChuyen.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlForm.add(txtPhiVanChuyen, gbc);
        shippingSummaryComponents.add(txtPhiVanChuyen);

        // Đổi trả
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel lblDt = new JLabel("Đổi trả");
        lblDt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlForm.add(lblDt, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTienDoiTra = createCyanNumberField();
        pnlForm.add(txtTienDoiTra, gbc);

        // Tổng cộng
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        JLabel lblTc = new JLabel("Tổng cộng");
        lblTc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlForm.add(lblTc, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        txtTongCong = createCyanNumberField();
        txtTongCong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtTongCong.setForeground(new Color(0, 30, 150));
        pnlForm.add(txtTongCong, gbc);

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { recalculateFinancials(); }
            @Override public void removeUpdate(DocumentEvent e) { recalculateFinancials(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculateFinancials(); }
        };
        txtTiLeGiamGia.getDocument().addDocumentListener(dl);
        txtTiLeThue.getDocument().addDocumentListener(dl);
        txtPhiVanChuyen.getDocument().addDocumentListener(dl);

        panel.add(pnlForm, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createCyanNumberField() {
        JTextField tf = new JTextField("0", 12);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setHorizontalAlignment(SwingConstants.RIGHT);
        tf.setBackground(new Color(216, 245, 250));
        tf.setEditable(false);
        return tf;
    }

    private void setupKeyBindings() {
        KeyStroke f3 = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        registerKeyboardAction(e -> txtBarcode.requestFocusInWindow(), f3, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f6 = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        registerKeyboardAction(e -> {
            txtKhachHangLookup.requestFocusInWindow();
            txtKhachHangLookup.selectAll();
            showCustomerPopup();
        }, f6, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f9 = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        registerKeyboardAction(e -> onLuuTam(), f9, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f11 = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        registerKeyboardAction(e -> onMoLaiTam(), f11, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f12 = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        registerKeyboardAction(e -> onThanhToan(), f12, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke f2 = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        registerKeyboardAction(e -> onEditQuantityDialog(), f2, JComponent.WHEN_IN_FOCUSED_WINDOW);

        KeyStroke del = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        registerKeyboardAction(e -> onDeleteCurrentItem(), del, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadInitialData() {
        try {
            systemConfig = systemConfigDAO.loadConfig();
        } catch (Exception ignored) {}
        if (systemConfig == null) systemConfig = new SystemConfig();

        loadCategoryTree();

        reloadCustomerLookupData(false);

        cbKhoXuat.removeAllItems();
        List<KhoHang> khoList = khoHangDAO.getAllActive();
        for (KhoHang k : khoList) {
            if (!k.isFolder()) cbKhoXuat.addItem(k);
        }

        cbNhanVien.removeAllItems();
        List<NhanVien> nvList = nhanVienDAO.getAllActive();
        for (NhanVien nv : nvList) {
            if (!nv.isFolder() && !nv.isSeparator()) cbNhanVien.addItem(nv);
        }

        allMatHangList = matHangDAO.getAll();
        filteredCatalogList = new ArrayList<>(allMatHangList);
        refreshCatalogTable();
    }

    private void loadCategoryTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Tất cả");
        List<NhomMatHang> groupList = nhomMatHangDAO.getAll();
        for (NhomMatHang grp : groupList) {
            if (grp.getStatus() >= 0) {
                root.add(new DefaultMutableTreeNode(grp));
            }
        }
        treeNhomHang.setModel(new DefaultTreeModel(root));
        if (touchMode) {
            treeNhomHang.setSelectionPath(new TreePath(root.getPath()));
            refreshTouchCategoryButtons();
        }
    }

    private void onTreeSelectionChanged() {
        filterCatalogByKeyword();
    }

    private void filterCatalogByKeyword() {
        String kw = txtBarcode != null ? txtBarcode.getText().trim().toLowerCase() : "";
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode) treeNhomHang.getLastSelectedPathComponent();
        String selectedGroupId = null;
        if (selected != null && !selected.isRoot() && selected.getUserObject() instanceof NhomMatHang) {
            selectedGroupId = ((NhomMatHang) selected.getUserObject()).getId();
        }

        filteredCatalogList = new ArrayList<>();
        for (MatHang m : allMatHangList) {
            if (m == null) continue;
            // 1. Lọc theo nhóm danh mục nếu có chọn
            if (selectedGroupId != null && !selectedGroupId.equals(m.getNhomMatHangId())) {
                continue;
            }
            // 2. Lọc theo từ khóa tìm kiếm (mã hàng, mã sẵn có, tên hàng, đơn vị tính)
            if (!kw.isEmpty()) {
                String code = m.getCode() != null ? m.getCode().toLowerCase() : "";
                String name = m.getName() != null ? m.getName().toLowerCase() : "";
                String maSanCo = m.getMaSanCo() != null ? m.getMaSanCo().toLowerCase() : "";
                String dvt = m.getTenDonViTinh() != null ? m.getTenDonViTinh().toLowerCase() : "";
                if (!code.contains(kw) && !name.contains(kw) && !maSanCo.contains(kw) && !dvt.contains(kw)) {
                    continue;
                }
            }
            filteredCatalogList.add(m);
        }
        refreshCatalogTable();
        if (!filteredCatalogList.isEmpty() && tableCatalog.getRowCount() > 0) {
            tableCatalog.setRowSelectionInterval(0, 0);
            updateTonKhoDisplay();
        }
    }

    private void rebuildCatalogTableColumns() {
        Vector<String> columnIdentifiers = new Vector<>();
        columnIdentifiers.add("STT");
        columnIdentifiers.addAll(visibleCatalogColumns);

        catalogModel = new DefaultTableModel(columnIdentifiers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tableCatalog.setModel(catalogModel);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        if (tableCatalog.getColumnModel().getColumnCount() > 0) {
            tableCatalog.getColumnModel().getColumn(0).setPreferredWidth(35);
            tableCatalog.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }

        for (int i = 0; i < visibleCatalogColumns.size(); i++) {
            String colName = visibleCatalogColumns.get(i);
            TableColumn tc = tableCatalog.getColumnModel().getColumn(i + 1);
            switch (colName) {
                case "Mã hàng":
                    tc.setPreferredWidth(65);
                    break;
                case "Tên hàng":
                case "Tên mặt hàng":
                    tc.setPreferredWidth(130);
                    break;
                case "ĐVT":
                    tc.setPreferredWidth(45);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "ĐVT chẵn":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "Giá bán":
                case "Đơn giá 2":
                case "giá blabla":
                case "Giá bán chẵn":
                case "Giá nhập":
                case "Tồn kho":
                    tc.setPreferredWidth(65);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Bảo hành":
                case "Quy đổi":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "Mã vạch":
                    tc.setPreferredWidth(75);
                    break;
                case "Ghi chú":
                    tc.setPreferredWidth(90);
                    break;
                default:
                    tc.setPreferredWidth(65);
                    break;
            }
        }
    }

    private void openChonCotHienThiCatalogDialog() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(
                parentWin,
                visibleCatalogColumns,
                ChonCotHienThiDialog.ALL_POSSIBLE_CATALOG_COLUMNS,
                ChonCotHienThiDialog.DEFAULT_VISIBLE_CATALOG_COLUMNS
        );
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            visibleCatalogColumns = dlg.getResultVisibleColumns();
            rebuildCatalogTableColumns();
            refreshCatalogTable();
        }
    }

    private void refreshCatalogTable() {
        catalogModel.setRowCount(0);
        int stt = 1;
        for (MatHang m : filteredCatalogList) {
            Object[] row = new Object[visibleCatalogColumns.size() + 1];
            row[0] = String.format("%04d", stt++);
            for (int i = 0; i < visibleCatalogColumns.size(); i++) {
                String col = visibleCatalogColumns.get(i);
                switch (col) {
                    case "Mã hàng":
                        row[i + 1] = m.getCode() != null ? m.getCode() : "";
                        break;
                    case "Tên hàng":
                    case "Tên mặt hàng":
                        row[i + 1] = m.getName() != null ? m.getName() : "";
                        break;
                    case "ĐVT":
                        row[i + 1] = m.getTenDonViTinh() != null ? m.getTenDonViTinh() : "";
                        break;
                    case "ĐVT chẵn":
                        row[i + 1] = m.getTenDonViTinhChan() != null ? m.getTenDonViTinhChan() : "";
                        break;
                    case "Giá bán":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan());
                        break;
                    case "Bảo hành":
                        row[i + 1] = m.getBaoHanh() != null ? m.getBaoHanh() : "";
                        break;
                    case "Đơn giá 2":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan2());
                        break;
                    case "giá blabla":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBan3());
                        break;
                    case "Quy đổi":
                        row[i + 1] = m.getQuyDoi() > 0 ? String.format("%.0f", m.getQuyDoi()) : "";
                        break;
                    case "Giá bán chẵn":
                        row[i + 1] = CurrencyUtil.format(m.getGiaBanChan());
                        break;
                    case "Mã vạch":
                        row[i + 1] = m.getMaSanCo() != null ? m.getMaSanCo() : "";
                        break;
                    case "Tồn kho":
                        row[i + 1] = "0";
                        break;
                    case "Giá nhập":
                        row[i + 1] = CurrencyUtil.format(m.getGiaNhap());
                        break;
                    case "Ghi chú":
                        row[i + 1] = m.getGhiChu() != null ? m.getGhiChu() : "";
                        break;
                    default:
                        row[i + 1] = "";
                        break;
                }
            }
            catalogModel.addRow(row);
        }
        refreshTouchProductGrid();
    }

    private double getPriceForMatHang(MatHang m) {
        return getPriceForMatHang(m, 0);
    }

    private double getPriceForMatHang(MatHang m, int priceIndex) {
        if (m == null) return 0.0;
        switch (priceIndex) {
            case 1: return m.getGiaBan2() > 0 ? m.getGiaBan2() : m.getGiaBan();
            case 2: return m.getGiaBan3() > 0 ? m.getGiaBan3() : m.getGiaBan();
            case 3: return m.getGiaBan4() > 0 ? m.getGiaBan4() : m.getGiaBan();
            default: return m.getGiaBan();
        }
    }

    private void onCatalogItemSelected() {
        int row = tableCatalog.getSelectedRow();
        if (row < 0 || row >= filteredCatalogList.size()) return;
        MatHang m = filteredCatalogList.get(row);
        if (touchMode) {
            onTouchProductCardClicked(m);
            return;
        }
        openNhapSoLuongForProduct(m);
    }

    private void onBarcodeAdd() {
        if (touchMode) {
            onBarcodeAddTouch();
            return;
        }
        String code = txtBarcode.getText().trim();
        if (code.isEmpty()) {
            int row = tableCatalog.getSelectedRow();
            if (row >= 0 && row < filteredCatalogList.size()) {
                openNhapSoLuongForProduct(filteredCatalogList.get(row));
            }
            return;
        }

        MatHang found = null;
        // Ưu tiên khớp chính xác mã hàng hoặc mã vạch
        for (MatHang m : filteredCatalogList) {
            if (code.equalsIgnoreCase(m.getCode()) || code.equalsIgnoreCase(m.getMaSanCo())) {
                found = m;
                break;
            }
        }
        if (found == null && !filteredCatalogList.isEmpty()) {
            int row = tableCatalog.getSelectedRow();
            if (row >= 0 && row < filteredCatalogList.size()) {
                found = filteredCatalogList.get(row);
            } else {
                found = filteredCatalogList.get(0);
            }
        }
        if (found == null) {
            for (MatHang m : allMatHangList) {
                if (code.equalsIgnoreCase(m.getCode()) || code.equalsIgnoreCase(m.getMaSanCo()) || (m.getName() != null && m.getName().toLowerCase().contains(code.toLowerCase()))) {
                    found = m;
                    break;
                }
            }
        }

        if (found != null) {
            openNhapSoLuongForProduct(found);
            txtBarcode.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mặt hàng với mã: " + code, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openNhapSoLuongForProduct(MatHang m) {
        KhachHang selectedCustomer = (KhachHang) cbKhachHang.getSelectedItem();
        boolean useCustomerPrice = systemConfig != null
                && systemConfig.isSuDungGiaTheoKhach()
                && selectedCustomer != null
                && selectedCustomer.getId() != null
                && !selectedCustomer.getId().trim().isEmpty();
        int customerPriceCode = useCustomerPrice
                ? selectedCustomer.getGiaBanIndex() : KhachHang.GIA_THEO_DANH_MUC;

        List<GiaBanGanDayKhachHang> recentPrices = Collections.emptyList();
        if (selectedCustomer != null && selectedCustomer.getId() != null
                && !selectedCustomer.getId().trim().isEmpty()
                && (!useCustomerPrice || customerPriceCode == KhachHang.GIA_GAN_NHAT)) {
            recentPrices = hoaDonDAO.getGiaBanGanDayCuaKhachHang(
                    selectedCustomer.getId(), m.getId(), m.getDonViTinhId());
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        int selectedPriceIndex = -1;
        double price;
        String selectedPriceName;
        boolean applyToAll = false;

        if (useCustomerPrice) {
            List<GiaBanGanDayKhachHang> filteredRecent = ChonLoaiGiaDialog.filterRecentPrices(recentPrices);
            Double latestPrice = filteredRecent.isEmpty() ? null : filteredRecent.get(0).getDonGia();
            price = SalesConfigSupport.resolveCustomerPrice(customerPriceCode,
                    m.getGiaBan(), m.getGiaBan2(), m.getGiaBan3(), m.getGiaBan4(),
                    m.getGiaVon(), latestPrice);
            selectedPriceName = SalesConfigSupport.customerPriceName(customerPriceCode, systemConfig);
        } else {
            ChonLoaiGiaDialog priceDlg = new ChonLoaiGiaDialog(parentWin, systemConfig,
                    m.getGiaBan(), m.getGiaBan2(), m.getGiaBan3(), m.getGiaBan4(), recentPrices);
            priceDlg.setVisible(true);
            if (!priceDlg.isConfirmed()) return;
            selectedPriceIndex = priceDlg.getSelectedPriceIndex();
            price = priceDlg.getSelectedPrice();
            selectedPriceName = priceDlg.getSelectedPriceName();
            applyToAll = priceDlg.isApplyToAll();
        }

        double defaultQty = 1.0;
        try {
            defaultQty = Double.parseDouble(txtSoLuongBarcode.getText().trim().replace(",", "."));
        } catch (Exception ignored) {}

        double discount = dotKhuyenMaiDAO.getActiveDiscountPercentForProduct(m.getId(), m.getCode(), m.getNhomMatHangId(), dpkNgay.getDate());
        if (discount <= 0 && m.getGiamGiaPt() > 0) {
            discount = m.getGiamGiaPt();
        }

        NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, price, m.getTenDonViTinh(),
                defaultQty, discount, isDiscountInputAllowed());
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            currentLoaiGiaName = selectedPriceName;

            if (applyToAll && selectedPriceIndex >= 0) {
                applyPriceToAllInvoiceItems(selectedPriceIndex);
            }

            double finalQty = dlg.getResultSoLuong();
            double finalDiscount = dlg.getResultChietKhau();

            DonHangChiTiet existing = null;
            for (DonHangChiTiet ct : currentLayHangList) {
                if (m.getId() != null && m.getId().equals(ct.getMatHangId()) && Math.abs(ct.getDonGia() - price) < 0.001) {
                    existing = ct;
                    break;
                }
            }

            if (existing != null) {
                existing.setSoLuong(existing.getSoLuong() + finalQty);
                existing.setTiLeGiamGia(finalDiscount);
                existing.tinhThanhTien();
            } else {
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setId(UUID.randomUUID().toString());
                ct.setMatHangId(m.getId());
                ct.setMaHang(m.getCode());
                ct.setTenHang(m.getName());
                ct.setDonViTinhId(m.getDonViTinhId());
                ct.setTenDonViTinh(m.getTenDonViTinh());
                ct.setDonGia(price);
                ct.setSoLuong(finalQty);
                ct.setTiLeGiamGia(finalDiscount);
                ct.setBaoHanh(m.getBaoHanh());
                ct.tinhThanhTien();
                currentLayHangList.add(ct);
            }

            refreshLayHangTable();
            recalculateFinancials();
        }
    }

    private void applyPriceToAllInvoiceItems(int priceIndex) {
        for (DonHangChiTiet ct : currentLayHangList) {
            MatHang mh = matHangDAO.findById(ct.getMatHangId());
            if (mh != null) {
                ct.setDonGia(getPriceForMatHang(mh, priceIndex));
                ct.tinhThanhTien();
            }
        }
    }

    private void onChonMatHangTra() {
        if (systemConfig != null && systemConfig.isBatBuocDoiTraHangTheoDon()) {
            onChonMatHangTraTheoHoaDon();
            return;
        }

        MatHang m = (touchMode && selectedTouchProduct != null) ? selectedTouchProduct : null;
        if (m == null) {
            int row = tableCatalog.getSelectedRow();
            if (row >= 0 && row < filteredCatalogList.size()) {
                m = filteredCatalogList.get(row);
            }
        }
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một mặt hàng để trả hàng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        double price = getPriceForMatHang(m);

        if (touchMode) {
            Window parentWin = SwingUtilities.getWindowAncestor(this);
            Double qty = TouchNumberInputDialog.showDoubleDialog(parentWin,
                    "SỐ LƯỢNG TRẢ HÀNG (" + m.getName() + ")", 1.0, 0.01, 999999);
            if (qty != null && qty > 0) {
                DonHangChiTiet ct = new DonHangChiTiet();
                ct.setId(UUID.randomUUID().toString());
                ct.setMatHangId(m.getId());
                ct.setMaHang(m.getCode());
                ct.setTenHang(m.getName());
                ct.setDonViTinhId(m.getDonViTinhId());
                ct.setTenDonViTinh(m.getTenDonViTinh());
                ct.setDonGia(price);
                ct.setTraHang(true);
                ct.setSoLuong(qty);
                ct.setTiLeGiamGia(0.0);
                ct.tinhThanhTien();
                currentTraHangList.add(ct);

                refreshTraHangTable();
                recalculateFinancials();
            }
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, price, m.getTenDonViTinh(),
                1.0, 0.0, isDiscountInputAllowed());
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            DonHangChiTiet ct = new DonHangChiTiet();
            ct.setId(UUID.randomUUID().toString());
            ct.setMatHangId(m.getId());
            ct.setMaHang(m.getCode());
            ct.setTenHang(m.getName());
            ct.setDonViTinhId(m.getDonViTinhId());
            ct.setTenDonViTinh(m.getTenDonViTinh());
            ct.setDonGia(price);
            ct.setTraHang(true);
            ct.setSoLuong(dlg.getResultSoLuong());
            ct.setTiLeGiamGia(dlg.getResultChietKhau());
            ct.tinhThanhTien();
            currentTraHangList.add(ct);

            refreshTraHangTable();
            recalculateFinancials();
        }
    }

    private void onChonMatHangTraTheoHoaDon() {
        // Thời hạn đổi trả luôn tính theo ngày thực tế, không theo ngày hóa đơn đang nhập
        // để tránh việc đổi ngày chứng từ làm vượt qua chính sách đổi trả.
        Date returnDate = new Date();
        int allowedDays = Math.max(0, systemConfig.getSoNgayChoPhepDoiTra());

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(SalesConfigSupport.startOfDay(returnDate));
        fromCalendar.add(Calendar.DAY_OF_MONTH, -allowedDays);

        List<DonHang> eligibleInvoices = new ArrayList<>();
        for (DonHang invoice : hoaDonDAO.getDanhSachHoaDon(fromCalendar.getTime(), returnDate, null, null)) {
            if (invoice.getChiTietLayHang() != null
                    && !invoice.getChiTietLayHang().isEmpty()
                    && SalesConfigSupport.isReturnAllowed(invoice.getNgay(), returnDate, allowedDays)) {
                eligibleInvoices.add(invoice);
            }
        }

        if (eligibleInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Không có hóa đơn đủ điều kiện đổi trả trong " + allowedDays + " ngày gần đây.",
                    "Đổi trả theo hóa đơn", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ReturnInvoiceOption[] invoiceOptions = eligibleInvoices.stream()
                .map(ReturnInvoiceOption::new)
                .toArray(ReturnInvoiceOption[]::new);
        ReturnInvoiceOption selectedInvoice = (ReturnInvoiceOption) JOptionPane.showInputDialog(
                this, "Chọn hóa đơn gốc:", "Đổi trả theo hóa đơn",
                JOptionPane.PLAIN_MESSAGE, null, invoiceOptions, invoiceOptions[0]);
        if (selectedInvoice == null) return;

        ReturnItemOption[] itemOptions = selectedInvoice.invoice.getChiTietLayHang().stream()
                .map(ReturnItemOption::new)
                .toArray(ReturnItemOption[]::new);
        ReturnItemOption selectedItem = (ReturnItemOption) JOptionPane.showInputDialog(
                this, "Chọn mặt hàng cần trả:", "Hóa đơn " + selectedInvoice.invoice.getName(),
                JOptionPane.PLAIN_MESSAGE, null, itemOptions, itemOptions[0]);
        if (selectedItem == null) return;

        DonHangChiTiet source = selectedItem.item;
        MatHang matHang = matHangDAO.findById(source.getMatHangId());
        if (matHang == null) {
            matHang = new MatHang(source.getMatHangId(), source.getMaHang(), source.getTenHang(), source.getDonGia(), 0);
            matHang.setTenDonViTinh(source.getTenDonViTinh());
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        NhapSoLuongDialog quantityDialog = new NhapSoLuongDialog(parentWin, matHang, source.getDonGia(),
                source.getTenDonViTinh(), 1.0, source.getTiLeGiamGia(), isDiscountInputAllowed());
        quantityDialog.setVisible(true);
        if (!quantityDialog.isConfirmed()) return;

        if (quantityDialog.getResultSoLuong() > source.getSoLuong()) {
            JOptionPane.showMessageDialog(this,
                    "Số lượng trả không được vượt quá số lượng đã bán (" + source.getSoLuong() + ").",
                    "Số lượng không hợp lệ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DonHangChiTiet returnedItem = new DonHangChiTiet();
        returnedItem.setId(UUID.randomUUID().toString());
        returnedItem.setMatHangId(source.getMatHangId());
        returnedItem.setMaHang(source.getMaHang());
        returnedItem.setTenHang(source.getTenHang());
        returnedItem.setDonViTinhId(source.getDonViTinhId());
        returnedItem.setTenDonViTinh(source.getTenDonViTinh());
        returnedItem.setDonGia(source.getDonGia());
        returnedItem.setTraHang(true);
        returnedItem.setSoLuong(quantityDialog.getResultSoLuong());
        returnedItem.setTiLeGiamGia(quantityDialog.getResultChietKhau());
        returnedItem.setGhiChu("Trả theo HĐ: " + selectedInvoice.invoice.getName());
        returnedItem.tinhThanhTien();
        currentTraHangList.add(returnedItem);

        selectCustomerById(selectedInvoice.invoice.getKhachHangId());
        refreshTraHangTable();
        recalculateFinancials();
    }

    private void rebuildLayHangTableColumns() {
        Vector<String> columnIdentifiers = new Vector<>();
        columnIdentifiers.add("STT");
        columnIdentifiers.addAll(visibleLayHangColumns);

        layHangModel = new DefaultTableModel(columnIdentifiers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (col <= 0 || col > visibleLayHangColumns.size()) return false;
                String colName = visibleLayHangColumns.get(col - 1);
                return "Ghi chú".equals(colName)
                        || "S.lượng".equals(colName)
                        || ("CK%".equals(colName) && isDiscountInputAllowed());
            }
        };
        layHangModel.addTableModelListener(this::onLayHangCellEdited);
        tableLayHang.setModel(layHangModel);
        tableLayHang.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        if (tableLayHang.getColumnModel().getColumnCount() > 0) {
            tableLayHang.getColumnModel().getColumn(0).setPreferredWidth(35);
            tableLayHang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        }

        for (int i = 0; i < visibleLayHangColumns.size(); i++) {
            String colName = visibleLayHangColumns.get(i);
            TableColumn tc = tableLayHang.getColumnModel().getColumn(i + 1);
            switch (colName) {
                case "Tên mặt hàng":
                    tc.setPreferredWidth(160);
                    break;
                case "Mã hàng":
                    tc.setPreferredWidth(75);
                    break;
                case "Đơn giá":
                    tc.setPreferredWidth(80);
                    DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
                        @Override
                        protected void setValue(Object value) {
                            setText((value != null ? value.toString() : "") + "  ▼");
                        }
                    };
                    priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
                    priceRenderer.setToolTipText("Nhấn để chọn giá bán");
                    tc.setCellRenderer(priceRenderer);
                    break;
                case "ĐVT":
                    tc.setPreferredWidth(50);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "CK%":
                    tc.setPreferredWidth(45);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Tiền giảm":
                    tc.setPreferredWidth(75);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Ghi chú":
                    tc.setPreferredWidth(90);
                    break;
                case "Bảo hành":
                    tc.setPreferredWidth(65);
                    tc.setCellRenderer(centerRenderer);
                    break;
                case "S.lượng":
                    tc.setPreferredWidth(55);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Thành tiền":
                    tc.setPreferredWidth(85);
                    tc.setCellRenderer(rightRenderer);
                    break;
                case "Kích thước":
                    tc.setPreferredWidth(75);
                    break;
                case "Hạn sử dụng":
                    tc.setPreferredWidth(85);
                    tc.setCellRenderer(centerRenderer);
                    break;
                default:
                    tc.setPreferredWidth(80);
                    break;
            }
        }
    }

    private void onLayHangCellEdited(TableModelEvent event) {
        if (isRefreshingLayHangTable || event.getType() != TableModelEvent.UPDATE) return;
        int row = event.getFirstRow();
        int column = event.getColumn();
        if (row < 0 || row >= currentLayHangList.size()
                || column < 0 || column >= layHangModel.getColumnCount()
                || !"CK%".equals(layHangModel.getColumnName(column))) {
            return;
        }

        Object editedValue = layHangModel.getValueAt(row, column);
        Double discountRate = parseDiscountRate(editedValue);
        if (discountRate == null || discountRate < 0 || discountRate > 100) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "Chiết khấu mặt hàng phải là số từ 0 đến 100%.",
                        "Chiết khấu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                refreshLayHangTable(row);
            });
            return;
        }

        DonHangChiTiet detail = currentLayHangList.get(row);
        detail.setTiLeGiamGia(discountRate);
        SwingUtilities.invokeLater(() -> {
            refreshLayHangTable(row);
            recalculateFinancials();
        });
    }

    private Double parseDiscountRate(Object value) {
        if (value == null) return null;
        String text = value.toString().trim().replace("%", "").replace(',', '.');
        if (text.isEmpty()) return null;
        try {
            double rate = Double.parseDouble(text);
            return Double.isFinite(rate) ? rate : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void refreshLayHangTable(int selectRow) {
        isRefreshingLayHangTable = true;
        try {
            layHangModel.setRowCount(0);
            int stt = 1;
            for (DonHangChiTiet ct : currentLayHangList) {
                Vector<Object> rowData = new Vector<>();
                rowData.add(String.format("%04d", stt++));
                for (String colName : visibleLayHangColumns) {
                    switch (colName) {
                    case "Tên mặt hàng":
                        rowData.add(ct.getTenHang() != null ? ct.getTenHang() : "");
                        break;
                    case "Mã hàng":
                        rowData.add(ct.getMaHang() != null ? ct.getMaHang() : "");
                        break;
                    case "Đơn giá":
                        rowData.add(CurrencyUtil.format(ct.getDonGia()));
                        break;
                    case "ĐVT":
                        rowData.add(ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "");
                        break;
                    case "CK%":
                        rowData.add(ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0");
                        break;
                    case "Tiền giảm":
                        rowData.add(CurrencyUtil.format(ct.getTienGiam()));
                        break;
                    case "Ghi chú":
                        rowData.add(ct.getGhiChu() != null ? ct.getGhiChu() : "");
                        break;
                    case "Bảo hành":
                        rowData.add(ct.getBaoHanh() != null ? ct.getBaoHanh() : "");
                        break;
                    case "S.lượng":
                        rowData.add(ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()));
                        break;
                    case "Thành tiền":
                        rowData.add(CurrencyUtil.format(ct.getThanhTien()));
                        break;
                    case "Kích thước":
                        rowData.add("");
                        break;
                    case "Hạn sử dụng":
                        rowData.add("");
                        break;
                    default:
                        rowData.add("");
                        break;
                    }
                }
                layHangModel.addRow(rowData);
            }
            int rowCount = tableLayHang.getRowCount();
            int targetRow = selectRow;
            if (targetRow < 0 && rowCount > 0) targetRow = rowCount - 1;
            if (targetRow >= 0 && targetRow < rowCount) {
                tableLayHang.setRowSelectionInterval(targetRow, targetRow);
            }
        } finally {
            isRefreshingLayHangTable = false;
        }
    }

    private void refreshLayHangTable() {
        int curSel = tableLayHang != null ? tableLayHang.getSelectedRow() : -1;
        refreshLayHangTable(curSel);
    }

    private boolean isLayHangPriceColumn(int viewColumn) {
        if (viewColumn < 0 || tableLayHang == null || layHangModel == null) return false;
        int modelColumn = tableLayHang.convertColumnIndexToModel(viewColumn);
        return "Đơn giá".equals(layHangModel.getColumnName(modelColumn));
    }

    /**
     * Flow du phong khi nhan vien them hang truoc roi moi chon khach:
     * nhan vao o Don gia de chon lai gia mac dinh hoac 3 gia khach da mua gan nhat.
     */
    private void showPriceDropdown(int row, int viewColumn) {
        if (row < 0 || row >= currentLayHangList.size()) return;
        DonHangChiTiet detail = currentLayHangList.get(row);
        MatHang matHang = matHangDAO.findById(detail.getMatHangId());
        if (matHang == null) return;

        JPopupMenu priceMenu = new JPopupMenu();
        addPriceSectionTitle(priceMenu, "Giá mặc định");
        addPriceMenuItem(priceMenu, "Giá bán", matHang.getGiaBan(), row);

        String nameGia2 = getConfiguredPriceName(
                systemConfig != null ? systemConfig.getDienGiaiGia2() : null, "Đơn giá 2");
        String nameGia3 = getConfiguredPriceName(
                systemConfig != null ? systemConfig.getDienGiaiGia3() : null, "Giá bán 3");
        String nameGia4 = getConfiguredPriceName(
                systemConfig != null ? systemConfig.getDienGiaiGia4() : null, "Giá bán 4");
        if ((systemConfig == null || systemConfig.isSuDungGia2()) && matHang.getGiaBan2() > 0) {
            addPriceMenuItem(priceMenu, nameGia2, matHang.getGiaBan2(), row);
        }
        if ((systemConfig == null || systemConfig.isSuDungGia3()) && matHang.getGiaBan3() > 0) {
            addPriceMenuItem(priceMenu, nameGia3, matHang.getGiaBan3(), row);
        }
        if ((systemConfig == null || systemConfig.isSuDungGia4()) && matHang.getGiaBan4() > 0) {
            addPriceMenuItem(priceMenu, nameGia4, matHang.getGiaBan4(), row);
        }

        KhachHang customer = (KhachHang) cbKhachHang.getSelectedItem();
        if (customer != null && customer.getId() != null && !customer.getId().trim().isEmpty()) {
            List<GiaBanGanDayKhachHang> recentPrices = ChonLoaiGiaDialog.filterRecentPrices(
                    hoaDonDAO.getGiaBanGanDayCuaKhachHang(
                            customer.getId(), detail.getMatHangId(), detail.getDonViTinhId()));
            if (!recentPrices.isEmpty()) {
                priceMenu.addSeparator();
                addPriceSectionTitle(priceMenu, "Giá khách đã mua gần đây");
                for (GiaBanGanDayKhachHang recent : recentPrices) {
                    String invoice = recent.getSoHoaDon() != null && !recent.getSoHoaDon().trim().isEmpty()
                            ? " · HĐ " + recent.getSoHoaDon().trim() : "";
                    String date = recent.getNgayHoaDon() != null
                            ? " · " + DateUtil.formatDate(recent.getNgayHoaDon()) : "";
                    addPriceMenuItem(priceMenu,
                            CurrencyUtil.format(Math.round(recent.getDonGia())) + invoice + date,
                            recent.getDonGia(), row, true);
                }
            }
        }

        Rectangle cell = tableLayHang.getCellRect(row, viewColumn, true);
        priceMenu.show(tableLayHang, cell.x, cell.y + cell.height);
    }

    private void addPriceSectionTitle(JPopupMenu menu, String title) {
        JMenuItem item = new JMenuItem(title);
        item.setEnabled(false);
        item.setFont(new Font("Segoe UI", Font.BOLD, 12));
        menu.add(item);
    }

    private void addPriceMenuItem(JPopupMenu menu, String name, double price, int row) {
        addPriceMenuItem(menu, name + ": " + CurrencyUtil.format(Math.round(price)), price, row, false);
    }

    private void addPriceMenuItem(JPopupMenu menu, String label, double price, int row,
                                  boolean recentPrice) {
        JMenuItem item = new JMenuItem(label);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (recentPrice) item.setBackground(new Color(245, 252, 240));
        item.addActionListener(e -> applySelectedRowPrice(row, price));
        menu.add(item);
    }

    private void applySelectedRowPrice(int row, double price) {
        if (row < 0 || row >= currentLayHangList.size() || price <= 0) return;
        DonHangChiTiet detail = currentLayHangList.get(row);
        detail.setDonGia(Math.round(price));
        detail.tinhThanhTien();
        refreshLayHangTable(row);
        recalculateFinancials();
    }

    private String getConfiguredPriceName(String configuredName, String fallback) {
        return configuredName != null && !configuredName.trim().isEmpty()
                ? configuredName.trim() : fallback;
    }

    private void refreshTraHangTable(int selectRow) {
        traHangModel.setRowCount(0);
        int stt = 1;
        for (DonHangChiTiet ct : currentTraHangList) {
            traHangModel.addRow(new Object[]{
                    String.format("%04d", stt++),
                    ct.getTenHang() != null ? ct.getTenHang() : "",
                    ct.getTenDonViTinh() != null ? ct.getTenDonViTinh() : "",
                    ct.getMaHang() != null ? ct.getMaHang() : "",
                    ct.getSoLuong() == (long) ct.getSoLuong() ? String.format("%d", (long) ct.getSoLuong()) : String.valueOf(ct.getSoLuong()),
                    CurrencyUtil.format(ct.getDonGia()),
                    ct.getTiLeGiamGia() > 0 ? String.format("%.0f", ct.getTiLeGiamGia()) : "0",
                    CurrencyUtil.format(ct.getThanhTien()),
                    ct.getGhiChu() != null ? ct.getGhiChu() : ""
            });
        }
        if (selectRow >= 0 && selectRow < tableTraHang.getRowCount()) {
            tableTraHang.setRowSelectionInterval(selectRow, selectRow);
        }
    }

    private void refreshTraHangTable() {
        int curSel = tableTraHang != null ? tableTraHang.getSelectedRow() : -1;
        refreshTraHangTable(curSel);
    }

    private void recalculateFinancials() {
        if (isUpdatingFinancials) return;
        isUpdatingFinancials = true;
        try {
            double tienHangGoc = 0.0;
            double chietKhauMatHang = 0.0;
            for (DonHangChiTiet ct : currentLayHangList) {
                tienHangGoc += ct.getSoLuong() * ct.getDonGia();
                chietKhauMatHang += ct.getTienGiam();
            }
            double tienSauChietKhauMatHang = Math.max(0, tienHangGoc - chietKhauMatHang);

            double tienTra = 0.0;
            for (DonHangChiTiet ct : currentTraHangList) {
                tienTra += ct.getThanhTien();
            }

            double tileGiam = parsePercentField(txtTiLeGiamGia);
            double tienGiam = tienSauChietKhauMatHang * (tileGiam / 100.0);
            double taxableAmount = Math.max(0, tienSauChietKhauMatHang - tienGiam);
            boolean hasTax = systemConfig != null && systemConfig.isCoThueSuat();
            boolean hasShipping = systemConfig != null && systemConfig.isCoPhiVanChuyen();
            double tileThue = hasTax ? parsePercentField(txtTiLeThue) : 0.0;
            double tienThue = SalesConfigSupport.calculateTax(taxableAmount, tileThue);
            double phiVanChuyen = hasShipping ? parseMoneyField(txtPhiVanChuyen) : 0.0;
            double tongCong = SalesConfigSupport.calculateInvoiceTotal(
                    taxableAmount, tienThue, phiVanChuyen, tienTra,
                    systemConfig != null ? systemConfig.getLamTronTien() : null);

            txtTienHang.setText(CurrencyUtil.format(tienHangGoc));
            txtTongChietKhauMatHang.setText(CurrencyUtil.format(chietKhauMatHang));
            txtTienGiamGia.setText(CurrencyUtil.format(tienGiam));
            txtTienThue.setText(CurrencyUtil.format(tienThue));
            txtTienDoiTra.setText(CurrencyUtil.format(tienTra));
            txtTongCong.setText(CurrencyUtil.format(tongCong));
        } finally {
            isUpdatingFinancials = false;
        }
    }

    private double parsePercentField(JTextField field) {
        if (field == null) return 0.0;
        try {
            String value = field.getText().replace("%", "").trim().replace(",", ".");
            return Math.max(0, Math.min(100, value.isEmpty() ? 0 : Double.parseDouble(value)));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private double parseMoneyField(JTextField field) {
        if (field == null) return 0.0;
        try {
            String value = field.getText().trim().replaceAll("[^0-9-]", "");
            return Math.max(0, value.isEmpty() ? 0 : Double.parseDouble(value));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private void onCustomerSelected() {
        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (!isUpdatingCustomerLookup && txtKhachHangLookup != null) {
            isUpdatingCustomerLookup = true;
            try {
                txtKhachHangLookup.setText(kh != null ? kh.toString() : "");
            } finally {
                isUpdatingCustomerLookup = false;
            }
        }
        txtDiaChiGiaoHang.setText(kh != null && kh.getDiaChi() != null ? kh.getDiaChi() : "");
        if (txtTouchPoints != null) {
            txtTouchPoints.setText(kh != null ? CurrencyUtil.format(kh.getDiemTichLuy()) : "0");
        }
    }

    private void updateTonKhoDisplay() {
        int row = tableCatalog.getSelectedRow();
        if (lblTonKho != null && row >= 0 && row < filteredCatalogList.size()) {
            MatHang m = filteredCatalogList.get(row);
            lblTonKho.setText("TỒN: " + (int) m.getTonToiDa());
        }
    }

    private void onDeleteCurrentItem() {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row >= 0 && row < currentLayHangList.size()) {
                currentLayHangList.remove(row);
                int nextRow = Math.min(row, currentLayHangList.size() - 1);
                refreshLayHangTable(nextRow);
                recalculateFinancials();
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row >= 0 && row < currentTraHangList.size()) {
                currentTraHangList.remove(row);
                int nextRow = Math.min(row, currentTraHangList.size() - 1);
                refreshTraHangTable(nextRow);
                recalculateFinancials();
            }
        }
    }

    private void onAdjustQuantity(int delta) {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row < 0 && tableLayHang.getRowCount() > 0) {
                row = tableLayHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentLayHangList.size()) {
                DonHangChiTiet ct = currentLayHangList.get(row);
                double newQty = ct.getSoLuong() + delta;
                if (newQty > 0) {
                    ct.setSoLuong(newQty);
                    ct.tinhThanhTien();
                    refreshLayHangTable(row);
                    recalculateFinancials();
                } else if (newQty == 0) {
                    currentLayHangList.remove(row);
                    int nextRow = Math.min(row, currentLayHangList.size() - 1);
                    refreshLayHangTable(nextRow);
                    recalculateFinancials();
                }
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row < 0 && tableTraHang.getRowCount() > 0) {
                row = tableTraHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentTraHangList.size()) {
                DonHangChiTiet ct = currentTraHangList.get(row);
                double newQty = ct.getSoLuong() + delta;
                if (newQty > 0) {
                    ct.setSoLuong(newQty);
                    ct.tinhThanhTien();
                    refreshTraHangTable(row);
                    recalculateFinancials();
                } else if (newQty == 0) {
                    currentTraHangList.remove(row);
                    int nextRow = Math.min(row, currentTraHangList.size() - 1);
                    refreshTraHangTable(nextRow);
                    recalculateFinancials();
                }
            }
        }
    }

    private void onEditQuantityDialog() {
        int selTab = tabbedPaneDetails.getSelectedIndex();
        if (selTab == 0) {
            int row = tableLayHang.getSelectedRow();
            if (row < 0 && tableLayHang.getRowCount() > 0) {
                row = tableLayHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentLayHangList.size()) {
                DonHangChiTiet ct = currentLayHangList.get(row);
                MatHang m = matHangDAO.findById(ct.getMatHangId());
                if (m == null) {
                    m = new MatHang(ct.getMatHangId(), ct.getMaHang(), ct.getTenHang(), ct.getDonGia(), 0);
                }

                Window parentWin = SwingUtilities.getWindowAncestor(this);
                NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, ct.getDonGia(), ct.getTenDonViTinh(),
                        ct.getSoLuong(), ct.getTiLeGiamGia(), isDiscountInputAllowed());
                dlg.setVisible(true);

                if (dlg.isConfirmed()) {
                    ct.setSoLuong(dlg.getResultSoLuong());
                    ct.setTiLeGiamGia(dlg.getResultChietKhau());
                    ct.tinhThanhTien();
                    refreshLayHangTable(row);
                    recalculateFinancials();
                }
            }
        } else {
            int row = tableTraHang.getSelectedRow();
            if (row < 0 && tableTraHang.getRowCount() > 0) {
                row = tableTraHang.getRowCount() - 1;
            }
            if (row >= 0 && row < currentTraHangList.size()) {
                DonHangChiTiet ct = currentTraHangList.get(row);
                MatHang m = matHangDAO.findById(ct.getMatHangId());
                if (m == null) {
                    m = new MatHang(ct.getMatHangId(), ct.getMaHang(), ct.getTenHang(), ct.getDonGia(), 0);
                }

                Window parentWin = SwingUtilities.getWindowAncestor(this);
                NhapSoLuongDialog dlg = new NhapSoLuongDialog(parentWin, m, ct.getDonGia(), ct.getTenDonViTinh(),
                        ct.getSoLuong(), ct.getTiLeGiamGia(), isDiscountInputAllowed());
                dlg.setVisible(true);

                if (dlg.isConfirmed()) {
                    ct.setSoLuong(dlg.getResultSoLuong());
                    ct.setTiLeGiamGia(dlg.getResultChietKhau());
                    ct.tinhThanhTien();
                    refreshTraHangTable(row);
                    recalculateFinancials();
                }
            }
        }
    }

    private void onLuuTam() {
        if (currentLayHangList.isEmpty() && currentTraHangList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có mặt hàng nào để lưu tạm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateRequiredSalesFields()) return;

        buildCurrentHoaDonObject();
        hoaDonDAO.saveTam(currentHoaDon);
        JOptionPane.showMessageDialog(this, "Đã lưu tạm hóa đơn " + currentHoaDon.getName() + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        resetForNewInvoice();
    }

    private void onMoLaiTam() {
        List<DonHang> tamList = hoaDonDAO.getDanhSachTam();
        if (tamList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn lưu tạm nào!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        DanhSachHoaDonLuuTamDialog dlg = new DanhSachHoaDonLuuTamDialog(parentWin, tamList);
        dlg.setVisible(true);

        if (dlg.isConfirmed() && dlg.getSelectedHoaDon() != null) {
            DonHang chosen = dlg.getSelectedHoaDon();
            loadHoaDonIntoUI(chosen);
            hoaDonDAO.removeTam(chosen.getId());
        }
    }

    private void onOpenDatHang() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        KhachDatHang chosen = null;
        if (touchMode) {
            TouchMoDatHangDialog dlg = new TouchMoDatHangDialog(parentWin);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                chosen = dlg.getSelectedOrder();
            }
        } else {
            TimKiemKhachDatHangDialog dlg = new TimKiemKhachDatHangDialog(parentWin);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                chosen = dlg.getSelectedOrder();
            }
        }

        if (chosen != null) {
            applyDatHangOrder(chosen);
        }
    }

    private void applyDatHangOrder(KhachDatHang chosen) {
        if (chosen == null) return;
        if (chosen.getTenKhach() != null) {
            for (int k = 0; k < cbKhachHang.getItemCount(); k++) {
                KhachHang kh = cbKhachHang.getItemAt(k);
                if (kh != null && (chosen.getTenKhach().equalsIgnoreCase(kh.getName()) || (chosen.getdKhachHangId() != null && chosen.getdKhachHangId().equals(kh.getId())))) {
                    cbKhachHang.setSelectedIndex(k);
                    break;
                }
            }
            if (btnTouchKhachHang != null) {
                btnTouchKhachHang.setText(chosen.getTenKhach());
            }
        }
        if (txtDiaChiGiaoHang != null) {
            txtDiaChiGiaoHang.setText(chosen.getDiaChi() != null ? chosen.getDiaChi() : "");
        }

        List<KhachDatHangChiTiet> details = khachDatHangDAO.getChiTietByKhachDatHangId(chosen.getId(), chosen.getName());
        currentLayHangList.clear();
        for (KhachDatHangChiTiet ct : details) {
            DonHangChiTiet dhct = new DonHangChiTiet();
            dhct.setId(UUID.randomUUID().toString());
            dhct.setMatHangId(ct.getdMatHangId());
            dhct.setMaHang(ct.getMaHang());
            dhct.setTenHang(ct.getTenHang());
            dhct.setDonViTinhId(ct.getdDonViTinhId());
            dhct.setTenDonViTinh(ct.getTenDonViTinh());
            dhct.setDonGia(ct.getDonGia());
            dhct.setSoLuong(ct.getSoLuong());
            dhct.setTiLeGiamGia(ct.getTiLeGiamGia());
            dhct.setGhiChu(ct.getNote());
            dhct.tinhThanhTien();
            currentLayHangList.add(dhct);
        }
        refreshLayHangTable();
        refreshTouchCartList(-1);
        recalculateFinancials();
    }

    private void onThanhToan() {
        if (currentLayHangList.isEmpty() && currentTraHangList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn chưa có mặt hàng nào!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateRequiredSalesFields()) return;

        buildCurrentHoaDonObject();

        Window parentWin = SwingUtilities.getWindowAncestor(this);
        if (touchMode) {
            TouchXacNhanThanhToanDialog dlg = new TouchXacNhanThanhToanDialog(parentWin, currentHoaDon,
                    systemConfig == null || systemConfig.isChoPhepInTamTinh());
            dlg.setVisible(true);

            TouchXacNhanThanhToanDialog.ResultAction action = dlg.getResultAction();
            if (action == TouchXacNhanThanhToanDialog.ResultAction.PRINT_TEMP) {
                ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
                printDlg.setVisible(true);
                return;
            }

            if (action == TouchXacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT || action == TouchXacNhanThanhToanDialog.ResultAction.CONFIRM_NO_PRINT) {
                double totalPaid = dlg.getTienKhachDua() + dlg.getTienATM();
                currentHoaDon.setKhachDua(dlg.getTienKhachDua());
                currentHoaDon.setTienMat(dlg.getTienKhachDua());
                currentHoaDon.setTienTheATM(dlg.getTienATM());
                currentHoaDon.setTraLai(dlg.getTienTraLai());
                currentHoaDon.setTienThanhToan(totalPaid);
                currentHoaDon.setConLai(Math.max(0, currentHoaDon.getTongCong() - totalPaid));
                currentHoaDon.setStatus(1);

                boolean ok = hoaDonDAO.insertHoaDon(currentHoaDon);
                if (ok) {
                    if (action == TouchXacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT) {
                        ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
                        printDlg.setVisible(true);
                    }
                    JOptionPane.showMessageDialog(this, "Thanh toán thành công hóa đơn " + currentHoaDon.getName() + "!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    resetForNewInvoice();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        XacNhanThanhToanDialog dlg = new XacNhanThanhToanDialog(parentWin, currentHoaDon,
                systemConfig == null || systemConfig.isChoPhepInTamTinh());
        dlg.setVisible(true);

        XacNhanThanhToanDialog.ResultAction action = dlg.getResultAction();

        if (action == XacNhanThanhToanDialog.ResultAction.PRINT_TEMP) {
            ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
            printDlg.setVisible(true);
            return;
        }

        if (action == XacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT || action == XacNhanThanhToanDialog.ResultAction.CONFIRM_NO_PRINT) {
            double totalPaid = dlg.getTienKhachDua() + dlg.getTienATM();
            currentHoaDon.setTienThanhToan(totalPaid);
            currentHoaDon.setConLai(Math.max(0, currentHoaDon.getTongCong() - totalPaid));
            currentHoaDon.setStatus(1);

            boolean ok = hoaDonDAO.insertHoaDon(currentHoaDon);
            if (ok) {
                if (action == XacNhanThanhToanDialog.ResultAction.CONFIRM_AND_PRINT) {
                    ChonMauInDialog printDlg = new ChonMauInDialog(parentWin, currentHoaDon);
                    printDlg.setVisible(true);
                }
                JOptionPane.showMessageDialog(this, "Thanh toán thành công hóa đơn " + currentHoaDon.getName() + "!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                resetForNewInvoice();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu hóa đơn vào cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onThongKe() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        TouchThongKeBanHangDialog dlg = new TouchThongKeBanHangDialog(parentWin);
        dlg.setVisible(true);
    }

    private void onNavigateInvoice(int direction) {
        String curId = (currentHoaDon != null && currentHoaDon.getId() != null) ? currentHoaDon.getId() : "";
        DonHang target = hoaDonDAO.getHoaDonByOffset(curId, direction);
        if (target != null) {
            loadHoaDonIntoUI(target);
        } else {
            JOptionPane.showMessageDialog(this, direction < 0 ? "Đã ở hóa đơn đầu tiên!" : "Đã ở hóa đơn cuối cùng!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openChonCotHienThiDialog() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        ChonCotHienThiDialog dlg = new ChonCotHienThiDialog(parentWin, visibleLayHangColumns);
        dlg.setVisible(true);

        if (dlg.isConfirmed()) {
            visibleLayHangColumns = dlg.getResultVisibleColumns();
            applyColumnVisibility();
        }
    }

    private void applyColumnVisibility() {
        int curSel = tableLayHang != null ? tableLayHang.getSelectedRow() : -1;
        rebuildLayHangTableColumns();
        refreshLayHangTable(curSel);
    }

    private void resetForNewInvoice() {
        currentHoaDon = new DonHang();
        currentHoaDon.setId(UUID.randomUUID().toString());
        currentHoaDon.setName(hoaDonDAO.generateNextSoHoaDon(new Date()));
        currentHoaDon.setNgay(new Date());
        applySalesConfig();

        dpkNgay.setDate(new Date());
        txtSoHD.setText(currentHoaDon.getName());
        txtDienGiai.setText("Xuất bán hàng");
        txtDiaChiGiaoHang.setText("");
        txtGhiChu = new JTextField();
        int defaultDiscount = systemConfig != null ? systemConfig.getMacDinhGiamGiaPt() : 0;
        defaultDiscount = Math.max(0, Math.min(100, defaultDiscount));
        txtTiLeGiamGia.setText(defaultDiscount + " %");
        int defaultTax = systemConfig != null && systemConfig.isCoThueSuat()
                ? systemConfig.getMacDinhThueSuat() : 0;
        txtTiLeThue.setText(Math.max(0, Math.min(100, defaultTax)) + " %");
        txtPhiVanChuyen.setText("0");

        currentLayHangList.clear();
        currentTraHangList.clear();
        refreshLayHangTable();
        refreshTraHangTable();
        recalculateFinancials();

        if (cbKhachHang.getItemCount() > 0) cbKhachHang.setSelectedIndex(0);
        if (cbKhoXuat.getItemCount() > 0) cbKhoXuat.setSelectedIndex(0);
        if (cbNhanVien.getItemCount() > 0) cbNhanVien.setSelectedIndex(0);
        if (txtTouchGuestCount != null) txtTouchGuestCount.setText("0");
        if (txtTouchTable != null) txtTouchTable.setText("");

        if (touchMode) {
            if (btnTouchNgay != null) btnTouchNgay.setText(DateUtil.formatDate(new Date()));
            if (btnTouchSoHD != null) btnTouchSoHD.setText(currentHoaDon.getName());
            if (btnTouchKhachHang != null) btnTouchKhachHang.setText("Khách lẻ");
            if (btnTouchDiem != null) btnTouchDiem.setText("0");
            if (btnTouchKho != null) btnTouchKho.setText(cbKhoXuat.getSelectedItem() != null ? cbKhoXuat.getSelectedItem().toString() : "KHO BÁN HÀNG");
            if (btnTouchNhanVien != null) btnTouchNhanVien.setText(cbNhanVien.getSelectedItem() != null ? cbNhanVien.getSelectedItem().toString() : "Admin");
            if (btnTouchSoKhach != null) btnTouchSoKhach.setText("0");
            if (btnTouchBan != null) btnTouchBan.setText("");
            if (btnTouchGiaoHang != null) btnTouchGiaoHang.setText("Chọn địa chỉ giao hàng");
            if (btnTouchNhanVienGiao != null) btnTouchNhanVienGiao.setText("Chọn nhân viên giao");
            if (btnTouchTrichNhanVien != null) btnTouchTrichNhanVien.setText("0");
            if (btnTouchGhiChuGiao != null) btnTouchGhiChuGiao.setText("Ghi chú giao hàng");
            deliveryNhanVien = null;
            deliveryTrichNhanVien = 0.0;
            deliveryGhiChu = "";
            selectedTouchProduct = null;
            selectedCartIndex = -1;
            if (touchItemActionBar != null) touchItemActionBar.setVisible(false);
            refreshTouchCartList(-1);
            refreshTouchProductGrid();
        }
    }

    private void loadHoaDonIntoUI(DonHang dh) {
        this.currentHoaDon = dh;
        applySalesConfig();
        dpkNgay.setDate(dh.getNgay() != null ? dh.getNgay() : new Date());
        txtSoHD.setText(dh.getName() != null ? dh.getName() : "");
        txtDienGiai.setText(dh.getDienGiai() != null ? dh.getDienGiai() : "Xuất bán hàng");
        txtDiaChiGiaoHang.setText(dh.getDiaChiGiaoHang() != null ? dh.getDiaChiGiaoHang() : "");
        txtTiLeGiamGia.setText(String.format("%.0f %%", dh.getTiLeGiamGia()));
        txtTiLeThue.setText(String.format("%.0f %%", dh.getTiLeThue()));
        txtPhiVanChuyen.setText(CurrencyUtil.format(dh.getPhiVanChuyen()));
        if (txtTouchGuestCount != null) txtTouchGuestCount.setText(String.valueOf(Math.max(0, dh.getSoKhach())));
        if (txtTouchTable != null) txtTouchTable.setText(dh.getBan() != null ? dh.getBan() : "");

        currentLayHangList.clear();
        if (dh.getChiTietLayHang() != null) {
            currentLayHangList.addAll(dh.getChiTietLayHang());
        }
        currentTraHangList.clear();
        if (dh.getChiTietTraHang() != null) {
            currentTraHangList.addAll(dh.getChiTietTraHang());
        }

        refreshLayHangTable();
        refreshTraHangTable();
        recalculateFinancials();

        if (touchMode) {
            if (btnTouchNgay != null) btnTouchNgay.setText(DateUtil.formatDate(dh.getNgay() != null ? dh.getNgay() : new Date()));
            if (btnTouchSoHD != null) btnTouchSoHD.setText(dh.getName() != null ? dh.getName() : "");
            updateTouchCustomerDisplay();
            if (btnTouchSoKhach != null) btnTouchSoKhach.setText(String.valueOf(Math.max(0, dh.getSoKhach())));
            if (btnTouchBan != null) btnTouchBan.setText(dh.getBan() != null ? dh.getBan() : "");
            if (btnTouchGiaoHang != null) btnTouchGiaoHang.setText(dh.getDiaChiGiaoHang() != null && !dh.getDiaChiGiaoHang().isEmpty() ? dh.getDiaChiGiaoHang() : "Chọn địa chỉ giao hàng");
            selectedTouchProduct = null;
            selectedCartIndex = -1;
            if (touchItemActionBar != null) touchItemActionBar.setVisible(false);
            refreshTouchCartList(-1);
            refreshTouchProductGrid();
        }
    }

    private void buildCurrentHoaDonObject() {
        if (currentHoaDon == null) {
            currentHoaDon = new DonHang();
            currentHoaDon.setId(UUID.randomUUID().toString());
        }
        currentHoaDon.setName(txtSoHD.getText().trim());
        currentHoaDon.setNgay(dpkNgay.getDate());
        currentHoaDon.setDienGiai(txtDienGiai.getText().trim());
        currentHoaDon.setDiaChiGiaoHang(txtDiaChiGiaoHang.getText().trim());
        if (btnTouchSoKhach != null) {
            try {
                currentHoaDon.setSoKhach(Math.max(0, Integer.parseInt(btnTouchSoKhach.getText().trim())));
            } catch (Exception ignored) {
                currentHoaDon.setSoKhach(0);
            }
        } else if (txtTouchGuestCount != null) {
            try {
                currentHoaDon.setSoKhach(Math.max(0, Integer.parseInt(txtTouchGuestCount.getText().trim())));
            } catch (NumberFormatException ignored) {
                currentHoaDon.setSoKhach(0);
            }
        }
        if (btnTouchBan != null) {
            currentHoaDon.setBan(btnTouchBan.getText().trim());
        } else if (txtTouchTable != null) {
            currentHoaDon.setBan(txtTouchTable.getText().trim());
        }

        KhachHang kh = (KhachHang) cbKhachHang.getSelectedItem();
        if (kh != null) {
            currentHoaDon.setKhachHangId(kh.getId());
            currentHoaDon.setTenKhach(kh.getName());
        }

        KhoHang kho = (KhoHang) cbKhoXuat.getSelectedItem();
        if (kho != null) {
            currentHoaDon.setKhoXuatId(kho.getId());
        }

        NhanVien nv = (NhanVien) cbNhanVien.getSelectedItem();
        if (nv != null) {
            currentHoaDon.setNhanVienXuatId(nv.getId());
            currentHoaDon.setTenNhanVien(nv.getName());
        }

        currentHoaDon.setLoaiGia(currentLoaiGiaName);
        currentHoaDon.setChiTietLayHang(new ArrayList<>(currentLayHangList));
        currentHoaDon.setChiTietTraHang(new ArrayList<>(currentTraHangList));

        double tienHangGoc = 0.0;
        double chietKhauMatHang = 0.0;
        for (DonHangChiTiet ct : currentLayHangList) {
            tienHangGoc += ct.getSoLuong() * ct.getDonGia();
            chietKhauMatHang += ct.getTienGiam();
        }
        double tienSauChietKhauMatHang = Math.max(0, tienHangGoc - chietKhauMatHang);
        currentHoaDon.setTienHang(tienHangGoc);

        double tt = 0.0;
        for (DonHangChiTiet ct : currentTraHangList) tt += ct.getThanhTien();
        currentHoaDon.setTienDoiTra(tt);

        double tile = parsePercentField(txtTiLeGiamGia);
        currentHoaDon.setTiLeGiamGia(tile);
        currentHoaDon.setTienGiamGia(tienSauChietKhauMatHang * (tile / 100.0));

        boolean hasTax = systemConfig != null && systemConfig.isCoThueSuat();
        boolean hasShipping = systemConfig != null && systemConfig.isCoPhiVanChuyen();
        double taxableAmount = Math.max(0,
                tienSauChietKhauMatHang - currentHoaDon.getTienGiamGia());
        double taxRate = hasTax ? parsePercentField(txtTiLeThue) : 0.0;
        double taxAmount = SalesConfigSupport.calculateTax(taxableAmount, taxRate);
        double shippingFee = hasShipping ? parseMoneyField(txtPhiVanChuyen) : 0.0;
        currentHoaDon.setTiLeThue(taxRate);
        currentHoaDon.setTienThue(taxAmount);
        currentHoaDon.setPhiVanChuyen(shippingFee);
        currentHoaDon.setTongCong(SalesConfigSupport.calculateInvoiceTotal(
                taxableAmount, taxAmount, shippingFee, tt,
                systemConfig != null ? systemConfig.getLamTronTien() : null));
    }

    private void applySalesConfig() {
        if (systemConfig == null) systemConfig = systemConfigDAO.loadConfig();
        boolean allowDiscount = isDiscountInputAllowed();
        txtTiLeGiamGia.setEditable(allowDiscount);
        txtTiLeGiamGia.setEnabled(allowDiscount);
        txtTiLeGiamGia.setToolTipText(allowDiscount
                ? "Nhập giảm giá cho toàn hóa đơn"
                : "Đã tắt nhập giảm giá trong Cấu hình toàn bộ hệ thống");

        boolean allowDateChange = systemConfig == null || systemConfig.isChoPhepThayDoiNgayTrenHoaDon();
        dpkNgay.setEnabled(allowDateChange);

        boolean hasTax = systemConfig != null && systemConfig.isCoThueSuat();
        for (Component component : taxSummaryComponents) component.setVisible(hasTax);
        boolean hasShipping = systemConfig != null && systemConfig.isCoPhiVanChuyen();
        for (Component component : shippingSummaryComponents) component.setVisible(hasShipping);
        txtTiLeThue.setEnabled(hasTax);
        txtPhiVanChuyen.setEnabled(hasShipping);
        revalidate();
        repaint();
        dpkNgay.setToolTipText(allowDateChange
                ? "Ngày hóa đơn"
                : "Đã khóa thay đổi ngày trong Cấu hình toàn bộ hệ thống");
    }

    public void refreshSalesConfiguration() {
        systemConfig = systemConfigDAO.loadConfig();
        if (currentLayHangList.isEmpty() && currentTraHangList.isEmpty()) {
            int defaultTax = systemConfig != null && systemConfig.isCoThueSuat()
                    ? systemConfig.getMacDinhThueSuat() : 0;
            txtTiLeThue.setText(Math.max(0, Math.min(100, defaultTax)) + " %");
            if (systemConfig == null || !systemConfig.isCoPhiVanChuyen()) {
                txtPhiVanChuyen.setText("0");
            }
        }
        applySalesConfig();
        recalculateFinancials();
    }

    private boolean isDiscountInputAllowed() {
        return systemConfig == null || systemConfig.isChoPhepNhapGiamGia();
    }

    private boolean validateRequiredSalesFields() {
        if (systemConfig == null) return true;

        KhachHang customer = (KhachHang) cbKhachHang.getSelectedItem();
        if (systemConfig.isBatBuocNhapKhachHang()
                && (customer == null || customer.getId() == null || customer.getId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trước khi lưu hóa đơn!",
                    "Thiếu khách hàng", JOptionPane.WARNING_MESSAGE);
            txtKhachHangLookup.requestFocusInWindow();
            return false;
        }

        NhanVien employee = (NhanVien) cbNhanVien.getSelectedItem();
        if (systemConfig.isBatBuocNhapNhanVienBanHang()
                && (employee == null || employee.getId() == null || employee.getId().trim().isEmpty())) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên bán hàng trước khi lưu hóa đơn!",
                    "Thiếu nhân viên bán hàng", JOptionPane.WARNING_MESSAGE);
            cbNhanVien.requestFocusInWindow();
            return false;
        }
        return true;
    }

    private void selectCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) return;
        for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
            KhachHang customer = cbKhachHang.getItemAt(i);
            if (customer != null && customerId.equals(customer.getId())) {
                cbKhachHang.setSelectedIndex(i);
                return;
            }
        }
    }

    private static final class ReturnInvoiceOption {
        private final DonHang invoice;

        private ReturnInvoiceOption(DonHang invoice) {
            this.invoice = invoice;
        }

        @Override
        public String toString() {
            String customer = invoice.getTenKhach() != null ? invoice.getTenKhach() : "Khách lẻ";
            return invoice.getName() + " - " + DateUtil.formatDate(invoice.getNgay()) + " - " + customer;
        }
    }

    private static final class ReturnItemOption {
        private final DonHangChiTiet item;

        private ReturnItemOption(DonHangChiTiet item) {
            this.item = item;
        }

        @Override
        public String toString() {
            return item.getMaHang() + " - " + item.getTenHang()
                    + " (SL đã bán: " + item.getSoLuong() + ")";
        }
    }
}
