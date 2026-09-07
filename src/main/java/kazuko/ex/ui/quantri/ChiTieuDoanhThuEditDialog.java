package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.ProvinceTarget;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

final class ChiTieuDoanhThuEditDialog extends JDialog {
    private final ChiTieuDoanhThuDAO dao;
    private final Consumer<String> onSaved;
    private final List<ChiTieuDoanhThu> navigation;
    private ChiTieuDoanhThu target;
    private final JTextField year = new JTextField(8), note = new JTextField(10);
    private final JTextField[] months = new JTextField[12];
    private final JComboBox<Group> groups = new JComboBox<>();
    private final DefaultTableModel provinceModel = new DefaultTableModel(new String[]{"Tỉnh thành", "Chỉ tiêu", "Ghi chú"}, 0);
    private final JTable provinceTable = ChiTieuUI.table(provinceModel);
    private final JComboBox<ProvinceChoice> provinceChoices = new JComboBox<>();
    private String baseline;
    private record ProvinceChoice(String id, String name) { @Override public String toString() { return name; } }

    ChiTieuDoanhThuEditDialog(Window owner, ChiTieuDoanhThuDAO dao, ChiTieuDoanhThu target,
                              List<ChiTieuDoanhThu> navigation, Consumer<String> onSaved) throws Exception {
        super(owner, "CHỈ TIÊU DOANH THU", ModalityType.APPLICATION_MODAL);
        this.dao = dao; this.target = target.copy(false); this.navigation = navigation; this.onSaved = onSaved;
        JPanel body = new JPanel(new BorderLayout(0, 10)); body.setBackground(ChiTieuUI.BLUE);
        body.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
        JPanel north = new JPanel(new BorderLayout(0, 10)); north.setOpaque(false);
        JLabel title = new JLabel("Chỉ tiêu doanh thu"); title.setFont(ChiTieuUI.FONT.deriveFont(Font.BOLD, 17f));
        north.add(title, BorderLayout.NORTH);
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); toolbar.setOpaque(false);
        toolbar.add(ChiTieuUI.button("Trước (F10)", "", () -> navigate(-1)));
        toolbar.add(ChiTieuUI.button("Sau (F11)", "", () -> navigate(1)));
        toolbar.add(ChiTieuUI.button("Tạo mới", "plus.png", () -> fresh(false)));
        toolbar.add(ChiTieuUI.button("Sao chép", "copy.png", () -> fresh(true)));
        toolbar.add(ChiTieuUI.button("In", "Preview.png", this::print));
        toolbar.add(ChiTieuUI.button("Xóa", "cross.png", this::trashCurrent));
        north.add(toolbar); body.add(north, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 12)); content.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        addField(form, "Năm", year, 0, 0);
        JPanel groupPicker = new JPanel(new BorderLayout(2, 0)); groupPicker.setOpaque(false); groupPicker.add(groups);
        JButton addGroup = ChiTieuUI.button("+", "", this::addGroup); addGroup.setToolTipText("Thêm nhóm chỉ tiêu");
        JButton reloadGroup = ChiTieuUI.button("Tải", "", () -> {
            try { loadGroups(selectedGroupId()); } catch (Exception e) { ChiTieuUI.error(this, e); }
        }); reloadGroup.setToolTipText("Tải lại danh mục nhóm");
        JPanel groupTools = new JPanel(new GridLayout(1,2)); groupTools.add(addGroup); groupTools.add(reloadGroup);
        groupPicker.add(groupTools, BorderLayout.EAST);
        groupPicker.setMinimumSize(new Dimension(180, 26));
        addField(form, "Phân nhóm", groupPicker, 1, 0, 3);
        addField(form, "Ghi chú", note, 3, 0);
        for (int i=0; i<12; i++) {
            months[i] = new JTextField(7);
            months[i].setMinimumSize(new Dimension(70, 24));
            months[i].setHorizontalAlignment(JTextField.RIGHT);
            months[i].setToolTipText("Nhập số không âm; dùng dấu chấm cho phần thập phân.");
            addField(form, "Tháng " + (i+1), months[i], i/3, i%3+1);
        }
        content.add(form, BorderLayout.NORTH);
        JPanel provincePanel = new JPanel(new BorderLayout(0, 5)); provincePanel.setOpaque(false);
        JPanel provinceBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); provinceBar.setOpaque(false);
        provinceBar.add(new JLabel("Chỉ tiêu theo tỉnh"));
        provinceBar.add(ChiTieuUI.button("Thêm dòng", "plus.png", () -> provinceModel.addRow(new Object[]{null, "0", ""})));
        provinceBar.add(ChiTieuUI.button("Xóa dòng", "cross.png", () -> {
            if (provinceTable.isEditing() && !provinceTable.getCellEditor().stopCellEditing()) return;
            int row = provinceTable.getSelectedRow(); if (row>=0) provinceModel.removeRow(row);
        }));
        provincePanel.add(provinceBar, BorderLayout.NORTH);
        for (Map.Entry<String,String> p : dao.provinces().entrySet()) provinceChoices.addItem(new ProvinceChoice(p.getKey(), p.getValue()));
        provinceTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(provinceChoices));
        provinceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        provinceTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        provinceTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        provinceTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        JScrollPane provinceScroll = new JScrollPane(provinceTable);
        provinceScroll.setColumnHeaderView(provinceTable.getTableHeader());
        provincePanel.add(provinceScroll); content.add(provincePanel); body.add(content);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); footer.setOpaque(false);
        footer.add(ChiTieuUI.button("Lưu", "Save.png", () -> save(0)));
        footer.add(ChiTieuUI.button("Lưu & Mới", "", () -> save(1)));
        footer.add(ChiTieuUI.button("Lưu & thoát", "", () -> save(2)));
        footer.add(ChiTieuUI.button("Thoát", "", this::close)); body.add(footer, BorderLayout.SOUTH);
        setContentPane(body);
        ChiTieuUI.bind(getRootPane(), "control L", () -> save(0));
        ChiTieuUI.bind(getRootPane(), "control S", () -> save(1));
        ChiTieuUI.bind(getRootPane(), "control N", () -> fresh(false));
        ChiTieuUI.bind(getRootPane(), "control P", this::print);
        ChiTieuUI.bind(getRootPane(), "F10", () -> navigate(-1));
        ChiTieuUI.bind(getRootPane(), "F11", () -> navigate(1));
        ChiTieuUI.bind(getRootPane(), "ESCAPE", this::close);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { close(); } });
        loadGroups(target.groupId); populate();
        setSize(890, 710); setMinimumSize(new Dimension(830, 520)); setLocationRelativeTo(owner);
    }

    private void addField(JPanel panel, String text, Component field, int x, int y) { addField(panel,text,field,x,y,1); }
    private void addField(JPanel panel, String text, Component field, int x, int y, int width) {
        GridBagConstraints c = new GridBagConstraints(); c.gridx=x*2; c.gridy=y;
        c.insets=new Insets(3,3,3,5); c.anchor=GridBagConstraints.WEST;
        JLabel label = new JLabel(text);
        label.setMinimumSize(new Dimension(58,24));
        panel.add(label, c); c.gridx++; c.gridwidth=width; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL;
        panel.add(field,c);
    }

    private String selectedGroupId() { Group g=(Group)groups.getSelectedItem(); return g==null?null:g.id(); }

    private void loadGroups(String selectedId) throws Exception {
        groups.removeAllItems(); groups.addItem(new Group(null,"Chưa thiết lập","",null,false,null,30));
        for (Group g:dao.groups()) if (!g.folder() && g.status()>=0) groups.addItem(g);
        for (int i=0;i<groups.getItemCount();i++) if (Objects.equals(selectedId,groups.getItemAt(i).id())) groups.setSelectedIndex(i);
        if (selectedId != null && !Objects.equals(selectedGroupId(), selectedId)) {
            Group old = new Group(selectedId, ChiTieuUI.text(target.groupName) + " (không hoạt động)","",null,false,null,-1);
            groups.addItem(old); groups.setSelectedItem(old);
        }
    }

    private void addGroup() {
        String id=NhomChiTieuDialog.show(this,dao,new Group(null,"","",null,false,null,30));
        if (id != null) try { loadGroups(id); } catch (Exception e) { ChiTieuUI.error(this,e); }
    }

    private void populate() {
        year.setText(String.valueOf(target.year)); note.setText(target.note);
        for (int i=0;i<12;i++) months[i].setText(target.months[i].stripTrailingZeros().toPlainString());
        provinceModel.setRowCount(0);
        for (ProvinceTarget p:target.provinces) provinceModel.addRow(new Object[]{
                new ProvinceChoice(p.provinceId(),p.provinceName()),p.amount().toPlainString(),p.note()});
        setTitle("CHỈ TIÊU DOANH THU - " + (target.id==null?"THÊM MỚI":"CHỈNH SỬA"));
        baseline = fingerprint();
    }

    private ChiTieuDoanhThu read() {
        if (provinceTable.isEditing() && !provinceTable.getCellEditor().stopCellEditing())
            throw new IllegalArgumentException("Hãy hoàn tất ô đang nhập.");
        ChiTieuDoanhThu result=target.copy(false);
        try { result.year=Integer.parseInt(year.getText().trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Năm phải là số nguyên."); }
        result.groupId=selectedGroupId(); result.groupName=groups.getSelectedItem().toString(); result.note=note.getText();
        for(int i=0;i<12;i++) result.months[i]=ChiTieuUI.amount(months[i].getText());
        result.provinces.clear();
        for(int i=0;i<provinceModel.getRowCount();i++) {
            Object choice=provinceModel.getValueAt(i,0);
            if (!(choice instanceof ProvinceChoice p)) throw new IllegalArgumentException("Dòng " +(i+1)+": hãy chọn tỉnh thành.");
            result.provinces.add(new ProvinceTarget(p.id(),p.name(),ChiTieuUI.amount(provinceModel.getValueAt(i,1)),ChiTieuUI.text(provinceModel.getValueAt(i,2))));
        }
        result.validate(); return result;
    }

    private void save(int mode) {
        try {
            ChiTieuDoanhThu edited=read(); dao.save(edited); target=edited;
            for(int i=0;i<navigation.size();i++) if(Objects.equals(navigation.get(i).id,target.id)) navigation.set(i,target.copy(false));
            baseline=fingerprint(); onSaved.accept(target.id);
            if(mode==2) dispose();
            else if(mode==1) { target=new ChiTieuDoanhThu(); target.groupId=selectedGroupId(); populate(); }
            else setTitle("CHỈ TIÊU DOANH THU - ĐÃ LƯU");
        } catch(Exception e) { ChiTieuUI.error(this,e); }
    }

    private String fingerprint() {
        StringBuilder s=new StringBuilder(year.getText()).append('|').append(selectedGroupId()).append('|').append(note.getText());
        for(JTextField month:months) s.append('|').append(month.getText());
        s.append(provinceModel.getDataVector()); return s.toString();
    }

    private boolean canLeave() {
        if(provinceTable.isEditing() && !provinceTable.getCellEditor().stopCellEditing()) return false;
        return Objects.equals(baseline,fingerprint()) || JOptionPane.showConfirmDialog(this,
                "Bỏ các thay đổi chưa lưu?","Chỉ tiêu doanh thu",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    private void close() { if(canLeave()) dispose(); }
    private void trashCurrent() {
        if(target.id==null)return;
        if(JOptionPane.showConfirmDialog(this,"Chuyển kế hoạch đang mở vào thùng rác? Các thay đổi chưa lưu sẽ bị bỏ.",
                "Xóa kế hoạch",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try { dao.setTrash(target.id,false,true); onSaved.accept(null); dispose(); }
        catch(Exception e) { ChiTieuUI.error(this,e); }
    }
    private void fresh(boolean copy) {
        try {
            if(copy) {
                ChiTieuDoanhThu draft=read().copy(true);
                if(!canLeave())return;
                target=draft;
            }
            else { if(!canLeave()) return; target=new ChiTieuDoanhThu(); target.groupId=selectedGroupId(); }
            populate();
        } catch(Exception e) { ChiTieuUI.error(this,e); }
    }
    private void navigate(int direction) {
        int index=-1;
        for(int i=0;i<navigation.size();i++) if(Objects.equals(target.id,navigation.get(i).id)) index=i;
        int next=index+direction;
        if(index<0 || next<0 || next>=navigation.size() || !canLeave()) return;
        try {
            ChiTieuDoanhThu loaded=navigation.get(next).copy(false);
            loaded.provinces.clear(); loaded.provinces.addAll(dao.details(loaded.id));
            target=loaded; loadGroups(target.groupId); populate();
        } catch(Exception e) { ChiTieuUI.error(this,e); }
    }
    private void print() {
        try {
            ChiTieuDoanhThu value=read();
            DefaultTableModel report=new DefaultTableModel(new String[]{"Nội dung","Chỉ tiêu","Ghi chú"},0);
            for(int i=0;i<12;i++) report.addRow(new Object[]{"Tháng "+(i+1),value.months[i],""});
            report.addRow(new Object[]{"Tổng năm",value.total(),value.note});
            for(ProvinceTarget p:value.provinces) report.addRow(new Object[]{p.provinceName(),p.amount(),p.note()});
            ChiTieuUI.print(this,ChiTieuUI.table(report),"Chỉ tiêu doanh thu "+value.year+" - "+value.groupName);
        } catch(Exception e) { ChiTieuUI.error(this,e); }
    }
}
