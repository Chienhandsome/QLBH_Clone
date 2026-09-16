package kazuko.ex.ui.touch;

import kazuko.ex.dao.DonHangDAO;
import kazuko.ex.dao.KhoHangDAO;
import kazuko.ex.dao.NhaCungCapDAO;
import kazuko.ex.dao.NhanVienDAO;
import kazuko.ex.model.banhang.DonHang;
import kazuko.ex.model.danhmuc.KhoHang;
import kazuko.ex.model.danhmuc.NhaCungCap;
import kazuko.ex.model.danhmuc.NhanVien;
import kazuko.ex.ui.khohang.PhieuNhapKhoDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/** Full-screen touch list for warehouse receipts. */
final class TouchNhapKhoPanel extends JPanel {
    private static final Color BG=new Color(0,67,86), HEADER=new Color(79,139,169), TEAL=new Color(0,79,76), GREEN=new Color(0,90,20), RED=new Color(165,10,10), ORANGE=new Color(226,79,5), GRAY=new Color(145,145,145), BLUE=new Color(0,0,105);
    private final DonHangDAO dao=new DonHangDAO(); private final KhoHangDAO warehouseDao=new KhoHangDAO(); private final NhanVienDAO employeeDao=new NhanVienDAO(); private final NhaCungCapDAO supplierDao=new NhaCungCapDAO(); private final Runnable exit;
    private final SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy"); private final JTextField from=field(),to=field(),warehouse=field(),employee=field(),supplier=field(); private final TouchScrollableRowsPanel grid=new TouchScrollableRowsPanel();
    private final JButton edit=button("CHỈNH SỬA",GRAY),delete=button("XÓA",GRAY); private final List<DonHang> receipts=new ArrayList<>();
    private KhoHang selectedWarehouse; private NhanVien selectedEmployee; private NhaCungCap selectedSupplier; private DonHang selected; private String keyword=""; private TouchNhapKhoColumnConfig config;

    TouchNhapKhoPanel(Runnable exit){this.exit=exit;dateFormat.setLenient(false);setLayout(new BorderLayout());add(content());setDefaultDates();load();}
    private JPanel content(){
        JPanel root=base(); JPanel top=new JPanel(new BorderLayout(10,0));top.setOpaque(false);JLabel title=label("DANH MỤC ĐƠN HÀNG",RED,SwingConstants.CENTER,20);title.setPreferredSize(new Dimension(375,58));top.add(title,BorderLayout.WEST);
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));toolbar.setOpaque(false);JButton add=button("THÊM MỚI",GREEN),refresh=button("REFRESH",GREEN),find=button("TÌM",GREEN);for(JButton b:new JButton[]{add,edit,delete,refresh,find}){b.setPreferredSize(new Dimension(170,58));toolbar.add(b);}add.addActionListener(e->openReceipt(null));edit.addActionListener(e->{if(selected!=null)openReceipt(selected);});delete.addActionListener(e->delete());refresh.addActionListener(e->{keyword="";load();});find.addActionListener(e->search());top.add(toolbar);root.add(top,BorderLayout.NORTH);
        JPanel center=new JPanel(new BorderLayout(10,0));center.setOpaque(false);center.add(filters(),BorderLayout.WEST);grid.setBackground(new Color(232,232,232));JScrollPane scroll=new JScrollPane(grid,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.setBorder(new EmptyBorder(0,0,0,0));scroll.getVerticalScrollBar().setUnitIncrement(50);center.add(scroll);root.add(center);
        JPanel footer=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));footer.setOpaque(false);JButton settings=button("",BG);settings.setIcon(new TouchMatHangPanel.GearIcon(38,Color.WHITE));settings.setPreferredSize(new Dimension(70,62));settings.addActionListener(e->new TouchNhapKhoSettingsDialog(SwingUtilities.getWindowAncestor(this),this::reloadConfig).setVisible(true));JButton close=button("THOÁT",RED);close.setPreferredSize(new Dimension(225,62));close.addActionListener(e->exit.run());footer.add(settings);footer.add(close);root.add(footer,BorderLayout.SOUTH);return root;
    }
    private JPanel filters(){
        JPanel p=new JPanel(new GridBagLayout());p.setBackground(HEADER);p.setPreferredSize(new Dimension(375,0));p.setBorder(new EmptyBorder(10,18,10,18));GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.insets=new Insets(3,0,3,0);
        int y=0;JLabel day=new JLabel("Ngày");day.setForeground(Color.WHITE);day.setFont(font(18));c.gridy=y++;p.add(day,c);
        JPanel dates=new JPanel(new GridBagLayout());dates.setOpaque(false);GridBagConstraints d=new GridBagConstraints();d.insets=new Insets(2,2,2,2);d.fill=GridBagConstraints.HORIZONTAL;d.gridx=0;d.gridy=0;dates.add(small("TỪ:"),d);d.gridx=1;d.weightx=1;dates.add(from,d);d.gridx=0;d.gridy=1;d.weightx=0;dates.add(small("ĐẾN:"),d);d.gridx=1;d.weightx=1;dates.add(to,d);JButton calendar=button("▼",TEAL);calendar.setFont(font(32));calendar.setPreferredSize(new Dimension(70,105));calendar.addActionListener(e->pickDates());d.gridx=2;d.gridy=0;d.gridheight=2;d.weightx=0;dates.add(calendar,d);c.gridy=y++;p.add(dates,c);
        addFilter(p,c,y++,"Kho nhập",warehouse,()->chooseWarehouse());addFilter(p,c,y++,"Nhân viên nhập",employee,()->chooseEmployee());addFilter(p,c,y++,"Nhà cung cấp",supplier,()->chooseSupplier());c.gridy=y*2-2;c.weighty=1;p.add(Box.createVerticalGlue(),c);return p;
    }
    private void addFilter(JPanel p,GridBagConstraints c,int y,String title,JTextField field,Runnable action){JLabel l=new JLabel(title);l.setForeground(Color.WHITE);l.setFont(font(17));c.gridy=y*2-2;c.weighty=0;p.add(l,c);JPanel row=new JPanel(new BorderLayout(8,0));row.setOpaque(false);field.setEditable(false);JButton find=button("⌕",TEAL);find.setFont(new Font("Segoe UI Symbol",Font.BOLD,38));find.setPreferredSize(new Dimension(72,52));find.addActionListener(e->action.run());row.add(field);row.add(find,BorderLayout.EAST);c.gridy=y*2-1;p.add(row,c);}
    private void setDefaultDates(){Calendar c=Calendar.getInstance();c.set(Calendar.DAY_OF_MONTH,1);from.setText(dateFormat.format(c.getTime()));c.set(Calendar.DAY_OF_MONTH,c.getActualMaximum(Calendar.DAY_OF_MONTH));to.setText(dateFormat.format(c.getTime()));}
    private void pickDates(){String a=TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this),from.getText());if(a!=null)from.setText(a);String b=TouchDatePickerDialog.showDialog(SwingUtilities.getWindowAncestor(this),to.getText());if(b!=null)to.setText(b);load();}
    private void chooseWarehouse(){List<KhoHang> values=warehouseDao.getAllActive();Object v=choose("CHỌN KHO NHẬP",values.toArray());if(v instanceof KhoHang x){selectedWarehouse=x;warehouse.setText(safe(x.getName()));}else if(v==CLEAR){selectedWarehouse=null;warehouse.setText("");}load();}
    private void chooseEmployee(){List<NhanVien> values=employeeDao.getAll();Object v=choose("CHỌN NHÂN VIÊN NHẬP",values.toArray());if(v instanceof NhanVien x){selectedEmployee=x;employee.setText(safe(x.getName()));}else if(v==CLEAR){selectedEmployee=null;employee.setText("");}load();}
    private void chooseSupplier(){List<NhaCungCap> values=supplierDao.getAll();Object v=choose("CHỌN NHÀ CUNG CẤP",values.toArray());if(v instanceof NhaCungCap x){selectedSupplier=x;supplier.setText(safe(x.getName()));}else if(v==CLEAR){selectedSupplier=null;supplier.setText("");}load();}
    private static final String CLEAR="(Xóa lựa chọn)";
    private Object choose(String title,Object[] values){Object[] options=new Object[values.length+1];options[0]=CLEAR;System.arraycopy(values,0,options,1,values.length);return JOptionPane.showInputDialog(this,title,title,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);}
    private Date parse(String value){try{return dateFormat.parse(value);}catch(ParseException e){return null;}}
    private void load(){receipts.clear();receipts.addAll(dao.getPhieuNhapKho(parse(from.getText()),parse(to.getText()),selectedWarehouse==null?null:selectedWarehouse.getId(),selectedEmployee==null?null:selectedEmployee.getId(),selectedSupplier==null?null:selectedSupplier.getId(),keyword));selected=null;config=TouchNhapKhoConfigDAO.load();render();updateActions();}
    private void reloadConfig(){config=TouchNhapKhoConfigDAO.load();render();}
    private void render(){if(config==null)config=TouchNhapKhoConfigDAO.load();grid.removeAll();grid.configureGrid(config.getGridColumns(),config.getGridRows(),7,7);grid.setBorder(new EmptyBorder(7,7,7,7));int h=Math.max(1,790/config.getGridRows());for(DonHang r:receipts)grid.add(card(r,h));grid.completeGrid();grid.revalidate();grid.repaint();}
    private JPanel card(DonHang receipt,int height){JPanel p=new JPanel(new GridLayout(0,2,4,0));p.setBackground(receipt==selected?ORANGE:config.getCardBgColor());p.setBorder(new LineBorder(new Color(220,235,235),2,true));p.setPreferredSize(new Dimension(350,height));for(var item:config.getUsedColumns()){String text=TouchNhapKhoColumnConfig.value(receipt,item,config.isShowTitle());if(text.isBlank())continue;JLabel l=new JLabel(text,item.isRightAligned()?SwingConstants.RIGHT:SwingConstants.LEFT);l.setForeground(item.getColor());l.setFont(item.getFont());l.setBorder(new EmptyBorder(2,10,2,10));p.add(l);}var listener=new java.awt.event.MouseAdapter(){@Override public void mousePressed(java.awt.event.MouseEvent e){selected=receipt;render();updateActions();}@Override public void mouseClicked(java.awt.event.MouseEvent e){if(e.getClickCount()==2)openReceipt(receipt);}};p.addMouseListener(listener);for(Component x:p.getComponents())x.addMouseListener(listener);return p;}
    private void updateActions(){boolean on=selected!=null;edit.setBackground(on?GREEN:GRAY);delete.setBackground(on?RED:GRAY);}
    private void search(){String q=TouchTextKeyboardDialog.showDialog(SwingUtilities.getWindowAncestor(this),"TÌM PHIẾU NHẬP",keyword);if(q!=null){keyword=q.trim();load();}}
    private void openReceipt(DonHang receipt){DonHang full=receipt==null?null:dao.getPhieuNhapById(receipt.getId());new PhieuNhapKhoDialog(SwingUtilities.getWindowAncestor(this),full==null?receipt:full,this::load,true).setVisible(true);}
    private void delete(){if(selected==null)return;if(!TouchConfirmDialog.show(SwingUtilities.getWindowAncestor(this),"BẠN CÓ MUỐN XÓA PHIẾU NHẬP<br>"+safe(selected.getName()).toUpperCase()+" KHÔNG?"))return;if(!dao.deletePhieuNhapKho(selected.getId()))JOptionPane.showMessageDialog(this,"Không thể xóa phiếu nhập kho!","LỖI",JOptionPane.ERROR_MESSAGE);else load();}
    private static JPanel base(){JPanel p=new JPanel(new BorderLayout(0,8));p.setBackground(BG);p.setBorder(new EmptyBorder(7,7,7,7));return p;}private static JTextField field(){JTextField f=new JTextField();f.setFont(new Font("Segoe UI",Font.PLAIN,22));f.setPreferredSize(new Dimension(200,50));f.setBorder(new EmptyBorder(4,7,4,7));return f;}private static JLabel small(String t){JLabel l=new JLabel(t);l.setForeground(Color.WHITE);l.setFont(font(17));return l;}private static JLabel label(String t,Color bg,int align,int size){JLabel l=new JLabel(t,align);l.setOpaque(true);l.setBackground(bg);l.setForeground(Color.WHITE);l.setFont(font(size));l.setBorder(new LineBorder(Color.LIGHT_GRAY,2,true));return l;}private static JButton button(String t,Color bg){JButton b=new JButton(t);b.setBackground(bg);b.setForeground(Color.WHITE);b.setFont(font(18));b.setFocusPainted(false);b.setBorder(new LineBorder(Color.LIGHT_GRAY,2,true));return b;}private static Font font(int s){return new Font("Segoe UI",Font.BOLD,s);}private static String safe(String v){return v==null?"":v;}
}
