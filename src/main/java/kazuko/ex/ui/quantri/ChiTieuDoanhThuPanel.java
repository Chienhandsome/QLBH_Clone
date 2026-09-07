package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import kazuko.ex.util.ChiTieuDoanhThuExcel;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ChiTieuDoanhThuPanel extends JPanel {
    private final ChiTieuDoanhThuDAO dao;
    private List<Group> groups=new ArrayList<>();
    private List<ChiTieuDoanhThu> plans=new ArrayList<>(), visible=new ArrayList<>();
    private final JTree tree=new JTree(new DefaultMutableTreeNode("Nhóm chỉ tiêu doanh thu"));
    private final JTextField search=new JTextField(12);
    private final DefaultTableModel model=readOnly(headers());
    private final JTable table=ChiTieuUI.table(model);
    private final DefaultTableModel detailModel=readOnly(new String[]{"","Ghi chú","Chỉ tiêu doanh thu","Tỉnh thành","Chỉ tiêu"});
    private final JTable details=ChiTieuUI.table(detailModel);
    private final JLabel info=new JLabel("Chưa chọn kế hoạch"), total=new JLabel(" ");
    private boolean rebuilding;
    private String filter="ALL";
    private final JButton edit, delete, restore;

    public ChiTieuDoanhThuPanel() { this(new ChiTieuDoanhThuDAO()); }

    public ChiTieuDoanhThuPanel(ChiTieuDoanhThuDAO dao) {
        this.dao=dao; setLayout(new BorderLayout()); setBackground(ChiTieuUI.BLUE);
        JPanel left=new JPanel(new BorderLayout()); left.setPreferredSize(new Dimension(255,400));
        JPanel leftTop=new JPanel(new BorderLayout()); leftTop.setBackground(ChiTieuUI.BLUE);
        JLabel title=new JLabel(" Nhóm chỉ tiêu doanh thu"); title.setFont(ChiTieuUI.FONT.deriveFont(Font.BOLD));
        leftTop.add(title,BorderLayout.NORTH);
        JPanel groupTools=new JPanel(new FlowLayout(FlowLayout.LEFT,2,4)); groupTools.setOpaque(false);
        JButton addGroup=ChiTieuUI.button("","plus.png",()->editGroup(false,false));addGroup.setToolTipText("Thêm nhóm chỉ tiêu");groupTools.add(addGroup);
        groupTools.add(ChiTieuUI.button("Sửa","pencil.png",()->editGroup(true,false)));
        groupTools.add(ChiTieuUI.button("Thư mục","Folder.png",()->editGroup(false,true)));
        JButton refreshGroup=ChiTieuUI.button("","Refresh.png",()->reload(null));refreshGroup.setToolTipText("Tải lại danh mục");groupTools.add(refreshGroup);
        leftTop.add(groupTools); left.add(leftTop,BorderLayout.NORTH);
        tree.setRootVisible(false); tree.setShowsRootHandles(true); tree.setRowHeight(24); tree.setFont(ChiTieuUI.FONT);
        tree.addTreeSelectionListener(e->{
            if(rebuilding) return;
            Object value=selectedNode();
            if(value instanceof Group g) filter=g.id();
            else if(value instanceof Filter f) filter=f.id();
            else return;
            reloadPlans(null);
        });
        tree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override public Component getTreeCellRendererComponent(JTree t,Object v,boolean sel,boolean exp,boolean leaf,int row,boolean focus){
                super.getTreeCellRendererComponent(t,v,sel,exp,leaf,row,focus);
                Object item=((DefaultMutableTreeNode)v).getUserObject();
                if(item instanceof Group g) setIcon(UIManager.getIcon(g.folder()?"FileView.directoryIcon":"FileView.fileIcon"));
                return this;
            }
        });
        tree.addMouseListener(new MouseAdapter(){
            private void popup(MouseEvent e){
                if(!e.isPopupTrigger())return;
                TreePath path=tree.getPathForLocation(e.getX(),e.getY()); if(path!=null) tree.setSelectionPath(path);
                JPopupMenu menu=new JPopupMenu(); Object value=selectedNode();
                addMenu(menu,"Thêm nhóm",()->editGroup(false,false)); addMenu(menu,"Thêm thư mục",()->editGroup(false,true));
                if(value instanceof Group g){
                    if(g.status()>=0){addMenu(menu,"Chỉnh sửa",()->editGroup(true,false));addMenu(menu,"Chuyển nhóm vào thùng rác",()->trashGroup(g,true));}
                    else addMenu(menu,"Khôi phục nhóm",()->trashGroup(g,false));
                }
                menu.show(tree,e.getX(),e.getY());
            }
            @Override public void mousePressed(MouseEvent e){popup(e);}
            @Override public void mouseReleased(MouseEvent e){popup(e);}
        });
        left.add(new JScrollPane(tree));
        JPanel right=new JPanel(new BorderLayout());
        JPanel toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,3,4)); toolbar.setBackground(ChiTieuUI.BLUE);
        toolbar.add(new JLabel("Lọc (F3):")); toolbar.add(search);
        toolbar.add(ChiTieuUI.button("Thêm mới (Insert)","plus.png",()->openEditor(false,false)));
        edit=ChiTieuUI.button("Chỉnh sửa (F4)","pencil.png",()->openEditor(true,false)); toolbar.add(edit);
        delete=ChiTieuUI.button("Xóa (Del)","cross.png",()->trashPlan(true)); toolbar.add(delete);
        restore=ChiTieuUI.button("Khôi phục","Undo.png",()->trashPlan(false)); toolbar.add(restore);
        toolbar.add(ChiTieuUI.button("Thêm excel","page_white_excel.png",this::importExcel));
        toolbar.add(ChiTieuUI.button("Xuất excel","export_excel.png",()->exportExcel(false)));
        toolbar.add(ChiTieuUI.button("In","Preview.png",()->ChiTieuUI.print(this,table,"Chỉ tiêu doanh thu")));
        toolbar.add(ChiTieuUI.button("Σ Tổng","",()->total.setVisible(!total.isVisible())));
        JScrollPane toolbarScroll=new JScrollPane(toolbar,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        toolbarScroll.setBorder(null);toolbarScroll.setPreferredSize(new Dimension(500,54));right.add(toolbarScroll,BorderLayout.NORTH);
        table.getColumnModel().getColumn(1).setPreferredWidth(110); table.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane masterScroll=new JScrollPane(table);masterScroll.setColumnHeaderView(table.getTableHeader());
        JPanel master=new JPanel(new BorderLayout()); master.add(masterScroll); master.add(total,BorderLayout.SOUTH);
        JTabbedPane tabs=new JTabbedPane(); tabs.setFont(ChiTieuUI.FONT);
        JPanel infoPanel=new JPanel(new BorderLayout()); infoPanel.setBackground(Color.WHITE); infoPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); infoPanel.add(info,BorderLayout.NORTH);
        JScrollPane detailScroll=new JScrollPane(details);detailScroll.setColumnHeaderView(details.getTableHeader());
        tabs.addTab("Thông tin",infoPanel); tabs.addTab("Chỉ tiêu chi tiết",detailScroll); tabs.setSelectedIndex(1);
        tabs.setPreferredSize(new Dimension(500,185));
        JSplitPane vertical=new JSplitPane(JSplitPane.VERTICAL_SPLIT,master,tabs); vertical.setResizeWeight(.75); vertical.setDividerSize(4);
        right.add(vertical);
        JSplitPane horizontal=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right); horizontal.setDividerLocation(255); horizontal.setDividerSize(5);
        add(horizontal);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&!rebuilding)loadDetails();});
        table.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){if(e.getClickCount()==2&&SwingUtilities.isLeftMouseButton(e))openEditor(true,false);}
            private void popup(MouseEvent e){
                if(!e.isPopupTrigger())return;
                int row=table.rowAtPoint(e.getPoint()); if(row>=0)table.setRowSelectionInterval(row,row);
                JPopupMenu menu=new JPopupMenu();
                addMenu(menu,"Thêm mới",()->openEditor(false,false));
                if(selected()!=null && !isTrash()) {addMenu(menu,"Chỉnh sửa",()->openEditor(true,false));addMenu(menu,"Sao chép",()->openEditor(true,true));addMenu(menu,"Chuyển vào thùng rác",()->trashPlan(true));}
                if(selected()!=null && isTrash())addMenu(menu,"Khôi phục",()->trashPlan(false));
                addMenu(menu,"Tải file Excel mẫu",()->exportExcel(true));addMenu(menu,"Làm mới",()->reload(null));
                menu.show(table,e.getX(),e.getY());
            }
            @Override public void mousePressed(MouseEvent e){popup(e);}
            @Override public void mouseReleased(MouseEvent e){popup(e);}
        });
        search.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){render(null);} public void removeUpdate(DocumentEvent e){render(null);} public void changedUpdate(DocumentEvent e){render(null);}
        });
        ChiTieuUI.bind(table,"INSERT",()->openEditor(false,false)); ChiTieuUI.bind(table,"F4",()->openEditor(true,false));
        ChiTieuUI.bind(table,"DELETE",()->trashPlan(true)); ChiTieuUI.bind(this,"F3",()->search.requestFocusInWindow());
        ChiTieuUI.bind(this,"F5",()->reload(null));
        reload(null);
    }

    private record Filter(String id,String name){@Override public String toString(){return name;}}
    private static String[] headers(){String[] h=new String[16];h[0]="";h[1]="Năm";h[2]="Phân nhóm";for(int i=0;i<12;i++)h[i+3]="Tháng "+(i+1);h[15]="Ghi chú";return h;}
    private static DefaultTableModel readOnly(String[] cols){return new DefaultTableModel(cols,0){@Override public boolean isCellEditable(int r,int c){return false;}};}
    private static void addMenu(JPopupMenu menu,String text,Runnable action){JMenuItem item=new JMenuItem(text);item.addActionListener(e->action.run());menu.add(item);}
    private Object selectedNode(){TreePath p=tree.getSelectionPath();return p==null?null:((DefaultMutableTreeNode)p.getLastPathComponent()).getUserObject();}
    private ChiTieuDoanhThu selected(){int row=table.getSelectedRow();return row<0||row>=visible.size()?null:visible.get(row);}
    private boolean isTrash(){return "TRASH".equals(filter)||groups.stream().anyMatch(g->g.id().equals(filter)&&g.status()<0);}

    private void reload(String selectedId){
        try{
            groups=dao.groups(); rebuilding=true;
            DefaultMutableTreeNode root=new DefaultMutableTreeNode("Nhóm chỉ tiêu doanh thu");
            root.add(new DefaultMutableTreeNode(new Filter("ALL","Tất cả")));root.add(new DefaultMutableTreeNode(new Filter("NONE","Chưa thiết lập")));
            DefaultMutableTreeNode trash=new DefaultMutableTreeNode(new Filter("TRASH","Thùng rác"));
            Map<String,DefaultMutableTreeNode> nodes=new LinkedHashMap<>();for(Group g:groups)nodes.put(g.id(),new DefaultMutableTreeNode(g));
            for(Group g:groups){
                DefaultMutableTreeNode node=nodes.get(g.id()),parent=nodes.get(g.parentId());
                if(g.status()<0)trash.add(node);
                else if(parent!=null && !parent.isNodeAncestor(node) && parent!=node && ((Group)parent.getUserObject()).status()>=0)parent.add(node);
                else root.add(node);
            }
            root.add(trash);tree.setModel(new DefaultTreeModel(root));
            DefaultMutableTreeNode choice=nodes.get(filter);
            if(choice==null){
                if(!Set.of("ALL","NONE","TRASH").contains(filter))filter="ALL";
                choice="TRASH".equals(filter)?trash:(DefaultMutableTreeNode)root.getChildAt("NONE".equals(filter)?1:0);
            }
            tree.setSelectionPath(new TreePath(choice.getPath()));for(int i=0;i<tree.getRowCount();i++)tree.expandRow(i);
        }catch(Exception e){ChiTieuUI.error(this,e);}finally{rebuilding=false;}
        reloadPlans(selectedId);
    }
    private void reloadPlans(String id){
        try{plans=dao.list(isTrash());render(id);}
        catch(Exception e){plans=new ArrayList<>();render(null);ChiTieuUI.error(this,e);}
    }
    private void render(String id){
        rebuilding=true;model.setRowCount(0);visible=new ArrayList<>();
        String keyword=search.getText().trim().toLowerCase(Locale.ROOT);
        Set<String> included=new HashSet<>();included.add(filter);
        boolean changed;
        do{changed=false;for(Group g:groups)if(included.contains(g.parentId()))changed|=included.add(g.id());}while(changed);
        BigDecimal sum=BigDecimal.ZERO;int selectedRow=-1;
        for(ChiTieuDoanhThu t:plans){
            if("NONE".equals(filter)&&t.groupId!=null&&!t.groupId.isBlank())continue;
            if(!Set.of("ALL","NONE","TRASH").contains(filter)&&!included.contains(t.groupId))continue;
            if(!(t.year+" "+ChiTieuUI.text(t.groupName)+" "+ChiTieuUI.text(t.note)).toLowerCase(Locale.ROOT).contains(keyword))continue;
            visible.add(t);Object[] row=new Object[16];row[0]=visible.size();row[1]=t.year;row[2]=t.groupName;
            System.arraycopy(t.months,0,row,3,12);row[15]=t.note;model.addRow(row);sum=sum.add(t.total());
            if(Objects.equals(id,t.id))selectedRow=visible.size()-1;
        }
        total.setText("  Tổng "+visible.size()+" kế hoạch · Tổng chỉ tiêu 12 tháng: "+new java.text.DecimalFormat("#,##0.##").format(sum));
        if(!visible.isEmpty())table.setRowSelectionInterval(Math.max(0,selectedRow),Math.max(0,selectedRow));
        rebuilding=false;loadDetails();
    }
    private void loadDetails(){
        detailModel.setRowCount(0);info.setText("Chưa chọn kế hoạch");
        ChiTieuDoanhThu t=selected();edit.setEnabled(t!=null&&!isTrash());delete.setEnabled(t!=null&&!isTrash());restore.setVisible(isTrash());restore.setEnabled(t!=null);
        if(t==null)return;
        SimpleDateFormat dates=new SimpleDateFormat("dd/MM/yyyy HH:mm");
        info.setText("<html>Khởi tạo: "+(t.createdAt==null?"--":dates.format(t.createdAt))+" · "+escape(t.createdBy)
                +"<br>Sửa đổi gần nhất: "+(t.modifiedAt==null?"--":dates.format(t.modifiedAt))+" · "+escape(t.modifiedBy)+"</html>");
        try{
            t.provinces.clear();t.provinces.addAll(dao.details(t.id));int n=1;
            for(ProvinceTarget p:t.provinces)detailModel.addRow(new Object[]{n++,p.note(),t.year,p.provinceName(),p.amount()});
        }catch(Exception e){ChiTieuUI.error(this,e);}
    }
    private static String escape(String text){return ChiTieuUI.text(text).replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private void openEditor(boolean existing,boolean copy){
        try{
            if(existing&&(selected()==null||isTrash()))return;
            ChiTieuDoanhThu t=existing?selected().copy(copy):new ChiTieuDoanhThu();
            if(!existing && selectedNode() instanceof Group g && !g.folder()&&g.status()>=0){t.groupId=g.id();t.groupName=g.name();}
            if(existing){t.provinces.clear();t.provinces.addAll(dao.details(selected().id));}
            new ChiTieuDoanhThuEditDialog(SwingUtilities.getWindowAncestor(this),dao,t,new ArrayList<>(visible),this::reload).setVisible(true);
        }catch(Exception e){ChiTieuUI.error(this,e);}
    }
    private void editGroup(boolean editing,boolean folder){
        Object value=selectedNode();Group g=value instanceof Group group?group:null;
        if(editing&&(g==null||g.status()<0))return;
        Group draft=editing?g:new Group(null,"","",g!=null&&g.status()>=0?g.id():null,folder,null,30);
        String id=NhomChiTieuDialog.show(this,dao,draft);if(id!=null){filter=id;reload(null);}
    }
    private void trashGroup(Group g,boolean trash){
        if(JOptionPane.showConfirmDialog(this,(trash?"Chuyển vào thùng rác: ":"Khôi phục: ")+g.name()+"?","Nhóm chỉ tiêu",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.setTrash(g.id(),true,trash);filter=trash?"TRASH":g.id();reload(null);}catch(Exception e){ChiTieuUI.error(this,e);}
    }
    private void trashPlan(boolean trash){
        ChiTieuDoanhThu t=selected();if(t==null||trash==isTrash())return;
        if(JOptionPane.showConfirmDialog(this,(trash?"Chuyển kế hoạch vào thùng rác?":"Khôi phục kế hoạch?")+"\n"+t.year+" - "+ChiTieuUI.text(t.groupName),"Chỉ tiêu doanh thu",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{dao.setTrash(t.id,false,trash);reload(null);}catch(Exception e){ChiTieuUI.error(this,e);}
    }

    private JFileChooser excelChooser(){JFileChooser c=new JFileChooser();c.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx, *.xls)","xlsx","xls"));return c;}
    private void exportExcel(boolean template){
        JFileChooser chooser=excelChooser();chooser.setSelectedFile(new File(template?"MauChiTieuDoanhThu.xlsx":"ChiTieuDoanhThu.xlsx"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();if(!file.getName().toLowerCase(Locale.ROOT).endsWith(".xlsx"))file=new File(file+".xlsx");
        if(file.exists()&&JOptionPane.showConfirmDialog(this,"Ghi đè file "+file.getName()+"?","Xuất Excel",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{
            List<ChiTieuDoanhThu> export=new ArrayList<>();
            if(!template)for(ChiTieuDoanhThu value:visible){ChiTieuDoanhThu t=value.copy(false);t.provinces.clear();t.provinces.addAll(dao.details(t.id));export.add(t);}
            try(OutputStream out=new FileOutputStream(file)){ChiTieuDoanhThuExcel.write(out,export);}
            JOptionPane.showMessageDialog(this,"Đã xuất "+(template?"file mẫu":export.size()+" kế hoạch")+".\n"+file.getAbsolutePath());
        }catch(Exception e){ChiTieuUI.error(this,e);}
    }
    private void importExcel(){
        Object[] actions={"Chọn file nhập","Tải file mẫu","Hủy"};
        int action=JOptionPane.showOptionDialog(this,"File gồm 2 sheet: Chỉ tiêu doanh thu và Chỉ tiêu theo tỉnh.\nNhập mới các kế hoạch; nhóm và tỉnh thành phải có sẵn trong danh mục.","Thêm Excel",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE,null,actions,actions[0]);
        if(action==1){exportExcel(true);return;}if(action!=0)return;
        JFileChooser chooser=excelChooser();if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try{
            List<ChiTieuDoanhThu> imported;
            try(InputStream in=new FileInputStream(chooser.getSelectedFile())){imported=ChiTieuDoanhThuExcel.read(in,dao.groups(),dao.provinces());}
            BigDecimal sum=imported.stream().map(ChiTieuDoanhThu::total).reduce(BigDecimal.ZERO,BigDecimal::add);
            if(JOptionPane.showConfirmDialog(this,"Nhập "+imported.size()+" kế hoạch?\nTổng chỉ tiêu 12 tháng: "+sum.toPlainString(),"Xác nhận nhập Excel",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            dao.saveAll(imported);filter="ALL";search.setText("");reload(imported.get(0).id);
            JOptionPane.showMessageDialog(this,"Đã nhập "+imported.size()+" kế hoạch.");
        }catch(Exception e){ChiTieuUI.error(this,e);}
    }
}
