package kazuko.ex.ui.touch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Column/format setup matching the touch warehouse-receipt sample. */
final class TouchNhapKhoSettingsDialog extends JDialog {
    private static final Color BG = new Color(0,67,86), HEADER = new Color(126,165,194), TEAL = new Color(0,79,76), GREEN = new Color(0,85,20), GRAY = new Color(145,145,145), RED = new Color(165,15,15), PURPLE = new Color(90,10,80), ORANGE = new Color(226,79,5), OLIVE = new Color(90,85,0);
    private final TouchNhapKhoColumnConfig config = TouchNhapKhoConfigDAO.load();
    private final Runnable applied;
    private final JPanel unused = listPanel(), used = listPanel(), formatPanel = new JPanel(new BorderLayout(10,0));
    private final JButton format = button("ĐỊNH DẠNG", PURPLE), columns = button("SỐ CỘT: 3", OLIVE), rows = button("SỐ DÒNG: 6", OLIVE), cell = button("MÀU Ô", OLIVE), title = button("HIỆN TIÊU ĐỀ", OLIVE);
    private TouchNhapKhoColumnConfig.ColumnItem selectedUnused, selectedUsed;
    private boolean expanded;

    TouchNhapKhoSettingsDialog(Window owner, Runnable applied) {
        super(owner, "THIẾT LẬP CỘT HIỂN THỊ", ModalityType.APPLICATION_MODAL); this.applied = applied;
        setUndecorated(true); setContentPane(content()); setSize(1200,740); setLocationRelativeTo(owner); refresh(); updateFooter();
    }
    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout(0,7)); root.setBackground(BG); root.setBorder(new EmptyBorder(5,5,7,5));
        JLabel h = new JLabel("THIẾT LẬP CỘT HIỂN THỊ", SwingConstants.CENTER); h.setOpaque(true); h.setBackground(HEADER); h.setForeground(Color.WHITE); h.setFont(font(27)); h.setPreferredSize(new Dimension(0,74)); root.add(h,BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(10,5)); center.setOpaque(false); center.add(lists()); formatPanel.setOpaque(false); formatPanel.add(preview(),BorderLayout.WEST); formatPanel.add(formatControls()); formatPanel.setVisible(false); center.add(formatPanel,BorderLayout.SOUTH); root.add(center); root.add(footer(),BorderLayout.SOUTH); return root;
    }
    private JPanel lists() {
        JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(0,5,0,5); c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx=0;c.weightx=.42;p.add(titled("CÁC CỘT KHÔNG SỬ DỤNG",unused),c);
        JPanel x = new JPanel(new GridLayout(3,1,0,8));x.setOpaque(false);JButton add=button("THÊM >>",GRAY),remove=button("<< XÓA",GRAY),all=button("<< XÓA HẾT",GREEN);add.addActionListener(e->addColumn());remove.addActionListener(e->removeColumn());all.addActionListener(e->removeAllColumns());x.add(add);x.add(remove);x.add(all);JPanel xw=new JPanel(new GridBagLayout());xw.setOpaque(false);xw.add(x);c.gridx=1;c.weightx=.14;p.add(xw,c);
        c.gridx=2;c.weightx=.34;p.add(titled("CÁC CỘT SỬ DỤNG",used),c);
        JPanel o=new JPanel(new GridLayout(2,1,0,8));o.setOpaque(false);JButton up=button("LÊN",GRAY),down=button("XUỐNG",GRAY);up.addActionListener(e->move(-1));down.addActionListener(e->move(1));o.add(up);o.add(down);JPanel ow=new JPanel(new GridBagLayout());ow.setOpaque(false);ow.add(o);c.gridx=3;c.weightx=.10;p.add(ow,c);return p;
    }
    private JPanel preview() { JPanel p=new JPanel(new BorderLayout());p.setBackground(config.getCardBgColor());p.setBorder(new LineBorder(Color.LIGHT_GRAY,2,true));p.setPreferredSize(new Dimension(470,135));JLabel a=new JLabel("Số phiếu",SwingConstants.RIGHT),b=new JLabel("Tổng cộng",SwingConstants.RIGHT);a.setForeground(Color.WHITE);b.setForeground(Color.WHITE);a.setFont(font(17));b.setFont(font(17));p.add(a,BorderLayout.NORTH);p.add(b,BorderLayout.SOUTH);return p; }
    private JPanel formatControls() {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));p.setOpaque(false);JButton left=button("CĂN TRÁI",GRAY),right=button("CĂN PHẢI",GRAY),bold=button("ĐẬM",GRAY),italic=button("NGHIÊNG",GRAY),size=button("CỠ CHỮ",GRAY),color=button("MÀU CHỮ",GRAY),prefix=button("TIÊU ĐỀ VIẾT TẮT",GRAY);for(JButton b:new JButton[]{left,right,bold,italic,size,color,prefix}){b.setPreferredSize(new Dimension(135,58));p.add(b);}left.addActionListener(e->{if(selectedUsed!=null)selectedUsed.setRightAligned(false);});right.addActionListener(e->{if(selectedUsed!=null)selectedUsed.setRightAligned(true);});bold.addActionListener(e->{if(selectedUsed!=null)selectedUsed.setBold(!selectedUsed.isBold());});italic.addActionListener(e->{if(selectedUsed!=null)selectedUsed.setItalic(!selectedUsed.isItalic());});size.addActionListener(e->{if(selectedUsed!=null){Integer v=TouchNumberInputDialog.showDialog(this,"NHẬP CỠ CHỮ",selectedUsed.getFontSize(),10,50);if(v!=null)selectedUsed.setFontSize(v);}});color.addActionListener(e->{if(selectedUsed!=null){Color v=TouchChonMauDialog.show(this,selectedUsed.getColor());if(v!=null)selectedUsed.setColor(v);}});prefix.addActionListener(e->{if(selectedUsed!=null){String v=TouchTextKeyboardDialog.showDialog(this,"NHẬP TIÊU ĐỀ VIẾT TẮT",selectedUsed.getPrefix());if(v!=null)selectedUsed.setPrefix(v);}});return p;
    }
    private JPanel footer() {
        JPanel p=new JPanel(new BorderLayout());p.setOpaque(false);JPanel left=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));left.setOpaque(false);for(JButton b:new JButton[]{format,columns,rows,cell,title}){b.setPreferredSize(new Dimension(190,58));left.add(b);}format.addActionListener(e->toggle());TouchGridSettingsSupport.bind(this,columns,rows,config,this::updateFooter);cell.addActionListener(e->{Color v=TouchChonMauDialog.show(this,config.getCardBgColor());if(v!=null)config.setCardBgColor(v);});title.addActionListener(e->{config.setShowTitle(!config.isShowTitle());updateFooter();});p.add(left,BorderLayout.WEST);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);JButton save=button("CẬP NHẬT",GREEN),close=button("THOÁT",RED);save.setPreferredSize(new Dimension(200,58));close.setPreferredSize(new Dimension(190,58));save.addActionListener(e->save());close.addActionListener(e->dispose());actions.add(save);actions.add(close);p.add(actions,BorderLayout.EAST);return p;
    }
    private void refresh(){unused.removeAll();for(var i:config.getUnusedColumns())unused.add(column(i,false));unused.add(Box.createVerticalGlue());used.removeAll();for(var i:config.getUsedColumns())used.add(column(i,true));used.add(Box.createVerticalGlue());unused.revalidate();used.revalidate();unused.repaint();used.repaint();}
    private JButton column(TouchNhapKhoColumnConfig.ColumnItem i,boolean inUse){JButton b=button(i.getTitle(),(inUse?i==selectedUsed:i==selectedUnused)?ORANGE:TEAL);b.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));b.setPreferredSize(new Dimension(0,80));b.addActionListener(e->{if(inUse)selectedUsed=i;else selectedUnused=i;refresh();});return b;}
    private void addColumn(){if(selectedUnused==null)return;config.getUnusedColumns().remove(selectedUnused);config.getUsedColumns().add(selectedUnused);selectedUsed=selectedUnused;selectedUnused=null;refresh();}
    private void removeColumn(){if(selectedUsed==null)return;config.getUsedColumns().remove(selectedUsed);config.getUnusedColumns().add(selectedUsed);selectedUnused=selectedUsed;selectedUsed=null;refresh();}
    private void removeAllColumns(){config.getUnusedColumns().addAll(config.getUsedColumns());config.getUsedColumns().clear();selectedUsed=null;refresh();}
    private void move(int d){if(selectedUsed==null)return;int old=config.getUsedColumns().indexOf(selectedUsed),n=old+d;if(old<0||n<0||n>=config.getUsedColumns().size())return;config.getUsedColumns().remove(old);config.getUsedColumns().add(n,selectedUsed);refresh();}
    private void save(){if(config.getUsedColumns().isEmpty()){JOptionPane.showMessageDialog(this,"Phải giữ lại ít nhất một cột hiển thị.","THÔNG BÁO",JOptionPane.WARNING_MESSAGE);return;}if(TouchNhapKhoConfigDAO.save(config)){applied.run();dispose();}}
    private void toggle(){expanded=!expanded;formatPanel.setVisible(expanded);format.setBackground(expanded?ORANGE:PURPLE);setSize(1200,expanded?860:740);setLocationRelativeTo(getOwner());updateFooter();revalidate();}
    private void updateFooter(){TouchGridSettingsSupport.refreshLabels(columns,rows,config);title.setBackground(config.isShowTitle()?ORANGE:OLIVE);columns.setVisible(expanded);rows.setVisible(expanded);cell.setVisible(expanded);title.setVisible(expanded);}
    private static JPanel listPanel(){JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBackground(HEADER);p.setBorder(new EmptyBorder(5,5,5,5));return p;}
    private static JPanel titled(String t,JPanel content){JPanel p=new JPanel(new BorderLayout(0,5));p.setOpaque(false);JLabel l=new JLabel(t);l.setForeground(Color.WHITE);l.setFont(font(18));p.add(l,BorderLayout.NORTH);JScrollPane s=new JScrollPane(content);s.setBorder(new LineBorder(Color.LIGHT_GRAY,2,true));s.getVerticalScrollBar().setUnitIncrement(40);p.add(s);return p;}
    private static JButton button(String t,Color c){JButton b=new JButton(t);b.setBackground(c);b.setForeground(Color.WHITE);b.setFont(font(18));b.setFocusPainted(false);b.setBorder(new LineBorder(Color.LIGHT_GRAY,2,true));return b;}private static Font font(int s){return new Font("Segoe UI",Font.BOLD,s);}
}
