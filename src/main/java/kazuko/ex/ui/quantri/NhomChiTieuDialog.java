package kazuko.ex.ui.quantri;

import kazuko.ex.dao.ChiTieuDoanhThuDAO;
import kazuko.ex.model.quantri.ChiTieuDoanhThu.Group;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

final class NhomChiTieuDialog extends JDialog {
    private String savedId;

    static String show(Component owner, ChiTieuDoanhThuDAO dao, Group group) {
        NhomChiTieuDialog dialog = new NhomChiTieuDialog(SwingUtilities.getWindowAncestor(owner), dao, group);
        dialog.setVisible(true);
        return dialog.savedId;
    }

    private record ImageChoice(String id, String name) { @Override public String toString() { return name; } }

    private NhomChiTieuDialog(Window owner, ChiTieuDoanhThuDAO dao, Group g) {
        super(owner, "Nhóm chỉ tiêu doanh thu", ModalityType.APPLICATION_MODAL);
        JPanel body = new JPanel(new BorderLayout(10, 12));
        body.setBorder(BorderFactory.createEmptyBorder(15, 15, 12, 15)); body.setBackground(ChiTieuUI.BLUE);
        JLabel title = new JLabel(g.folder() ? "Thư mục chỉ tiêu doanh thu" : "Chỉnh sửa nhóm chỉ tiêu doanh thu");
        title.setFont(ChiTieuUI.FONT.deriveFont(Font.BOLD, 14f)); body.add(title, BorderLayout.NORTH);
        JPanel fields = new JPanel(new BorderLayout(10, 8)); fields.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(8, 0)); top.setOpaque(false);
        JTextField name = new JTextField(g.name());
        JComboBox<ImageChoice> images = new JComboBox<>();
        images.addItem(new ImageChoice(null, "Mặc định"));
        try {
            for (Map.Entry<String,String> image : dao.images().entrySet())
                images.addItem(new ImageChoice(image.getKey(), image.getValue()));
        } catch (Exception e) { images.setToolTipText("Không tải được danh sách ảnh: " + e.getMessage()); }
        if (g.imageId() != null) {
            boolean found = false;
            for (int i=0; i<images.getItemCount(); i++) if (g.imageId().equals(images.getItemAt(i).id())) {
                images.setSelectedIndex(i); found = true; break;
            }
            if (!found) { ImageChoice old = new ImageChoice(g.imageId(), "Ảnh hiện tại"); images.addItem(old); images.setSelectedItem(old); }
        }
        images.setPreferredSize(new Dimension(150, 26));
        top.add(new JLabel("Tên nhóm:"), BorderLayout.WEST); top.add(name); top.add(images, BorderLayout.EAST);
        JPanel headingFields=new JPanel(new BorderLayout(0,8));headingFields.setOpaque(false);headingFields.add(top,BorderLayout.NORTH);
        JComboBox<Group> parents=new JComboBox<>();
        parents.addItem(new Group(null,"Cấp cao nhất","",null,true,null,30));
        try {
            List<Group> all=dao.groups();Set<String> excluded=new HashSet<>();if(g.id()!=null)excluded.add(g.id());
            boolean changed;
            do {changed=false;for(Group item:all)if(excluded.contains(item.parentId()))changed|=excluded.add(item.id());}while(changed);
            for(Group item:all)if(item.status()>=0&&!excluded.contains(item.id()))parents.addItem(item);
            for(int i=0;i<parents.getItemCount();i++)if(java.util.Objects.equals(g.parentId(),parents.getItemAt(i).id()))parents.setSelectedIndex(i);
        } catch(Exception e) { ChiTieuUI.error(this,e); }
        JPanel parentLine=new JPanel(new BorderLayout(8,0));parentLine.setOpaque(false);
        parentLine.add(new JLabel("Nhóm cha:"),BorderLayout.WEST);parentLine.add(parents);headingFields.add(parentLine);
        fields.add(headingFields, BorderLayout.NORTH);
        JTextArea note = new JTextArea(g.note(), 5, 40); note.setLineWrap(true); note.setWrapStyleWord(true);
        JPanel notes = new JPanel(new BorderLayout(8, 0)); notes.setOpaque(false);
        notes.add(new JLabel("Ghi chú:"), BorderLayout.WEST); notes.add(new JScrollPane(note)); fields.add(notes);
        body.add(fields);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT)); footer.setOpaque(false);
        Runnable save = () -> {
            try {
                savedId = dao.saveGroup(new Group(g.id(), name.getText(), note.getText(), ((Group)parents.getSelectedItem()).id(), g.folder(),
                        ((ImageChoice)images.getSelectedItem()).id(), g.status()));
                dispose();
            } catch (Exception e) { ChiTieuUI.error(this, e); }
        };
        JButton saveButton = ChiTieuUI.button("Ghi dữ liệu", "Save.png", save);
        footer.add(saveButton); footer.add(ChiTieuUI.button("Thoát", "", this::dispose)); body.add(footer, BorderLayout.SOUTH);
        setContentPane(body); getRootPane().setDefaultButton(saveButton);
        ChiTieuUI.bind(getRootPane(), "ESCAPE", this::dispose);
        setSize(620, 335); setLocationRelativeTo(owner);
    }
}
