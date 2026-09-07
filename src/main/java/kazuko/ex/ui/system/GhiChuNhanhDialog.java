package kazuko.ex.ui.system;

import kazuko.ex.dao.QuickNoteDAO;
import kazuko.ex.dao.UserDAO;
import kazuko.ex.model.system.SQuickNote;
import kazuko.ex.model.system.SUser;
import kazuko.ex.service.AuthService;
import kazuko.ex.util.IconHelper;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GhiChuNhanhDialog extends JDialog {
    private final QuickNoteDAO noteDAO = new QuickNoteDAO();
    private final UserDAO userDAO = new UserDAO();
    private final List<SQuickNote> noteList = new ArrayList<>();
    private int currentIndex = 0;

    private JTextPane textPane;
    private RTFEditorKit rtfKit;
    private JLabel lblPage;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnDelete;
    private JComboBox<String> cbFontFamily;
    private JComboBox<Integer> cbFontSize;

    private JToggleButton btnBold;
    private JToggleButton btnItalic;
    private JToggleButton btnUnderline;
    private JToggleButton btnStrike;

    private JToggleButton btnAlignLeft;
    private JToggleButton btnAlignCenter;
    private JToggleButton btnAlignRight;

    private JButton btnBullet;
    private JButton btnDecreaseIndent;
    private JButton btnIncreaseIndent;

    private JButton btnTime;
    private JButton btnSeparator;

    // Status bar at bottom
    private JLabel lblCreatedTime;
    private JLabel lblCreatedBy;

    private Timer autoSaveTimer;
    private boolean isUpdatingUi = false;

    public GhiChuNhanhDialog(Frame owner) {
        super(owner, "Ghi chú nhanh", false);
        initComponents();
        loadNotes();
    }

    private void initComponents() {
        setSize(780, 520);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Header Banner
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center: Toolbar + TextPane
        JPanel centerPanel = new JPanel(new BorderLayout());

        // Formatting Toolbar
        JToolBar toolbar = createFormattingToolbar();
        centerPanel.add(toolbar, BorderLayout.NORTH);

        // Editor
        rtfKit = new RTFEditorKit();
        textPane = new JTextPane();
        textPane.setEditorKit(rtfKit);
        textPane.setFont(new Font("Arial", Font.PLAIN, 13));
        textPane.setMargin(new Insets(8, 10, 8, 10));

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { triggerAutoSave(); }
            @Override
            public void removeUpdate(DocumentEvent e) { triggerAutoSave(); }
            @Override
            public void changedUpdate(DocumentEvent e) { triggerAutoSave(); }
        });

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 215, 235)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        // Auto-save debounce timer (600ms)
        autoSaveTimer = new Timer(600, e -> saveCurrentNote());
        autoSaveTimer.setRepeats(false);

        // Window Closing: save immediately
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveCurrentNote();
            }
        });
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(245, 245, 245));
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(210, 210, 210)),
                new EmptyBorder(4, 12, 4, 12)
        ));

        lblCreatedTime = new JLabel("Khởi tạo: ");
        lblCreatedTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCreatedTime.setForeground(new Color(60, 60, 60));

        lblCreatedBy = new JLabel("Khởi tạo bởi: Administrator");
        lblCreatedBy.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCreatedBy.setForeground(new Color(60, 60, 60));

        bar.add(lblCreatedTime, BorderLayout.WEST);
        bar.add(lblCreatedBy, BorderLayout.EAST);

        return bar;
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(215, 225, 240)));

        JLabel lblIcon = new JLabel(IconHelper.getIcon("FilterEdit.png", 24, 24));
        if (lblIcon.getIcon() == null) {
            lblIcon.setIcon(IconHelper.getIcon("edit-column.png", 24, 24));
        }

        JLabel lblTitle = new JLabel("Ghi chú nhanh");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(40, 40, 40));

        panel.add(lblIcon);
        panel.add(lblTitle);

        return panel;
    }

    private JToolBar createFormattingToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(236, 243, 252));
        toolbar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(195, 215, 238)),
                new EmptyBorder(3, 4, 3, 4)
        ));

        // 1. Delete current note button
        btnDelete = createIconButton("Cancelled.png", "Xóa", "Xóa ghi chú hiện tại");
        btnDelete.addActionListener(e -> deleteCurrentNote());
        toolbar.add(btnDelete);

        toolbar.add(Box.createHorizontalStrut(4));

        // 2. Navigation controls: [←] 1/2 [→] [+]
        btnPrev = createIconButton("arrow-180-medium.png", "<", "Trang trước");
        btnPrev.addActionListener(e -> goToPrevNote());
        toolbar.add(btnPrev);

        lblPage = new JLabel("1/1", SwingConstants.CENTER);
        lblPage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPage.setPreferredSize(new Dimension(38, 22));
        lblPage.setForeground(new Color(30, 50, 80));
        toolbar.add(lblPage);

        btnNext = createIconButton("arrow-000-medium.png", ">", "Trang sau");
        btnNext.addActionListener(e -> goToNextNote());
        toolbar.add(btnNext);

        JButton btnAdd = createIconButton("btnNew.Image.png", "+", "Thêm trang ghi chú mới");
        btnAdd.addActionListener(e -> createNewNote());
        toolbar.add(btnAdd);

        toolbar.addSeparator(new Dimension(8, 20));

        // 3. Font Family combo
        String[] fonts = {"Arial", "Segoe UI", "Tahoma", "Times New Roman", "Calibri", "Courier New", "Verdana"};
        cbFontFamily = new JComboBox<>(fonts);
        cbFontFamily.setPreferredSize(new Dimension(130, 22));
        cbFontFamily.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFontFamily.setBackground(Color.WHITE);
        cbFontFamily.addActionListener(e -> applyFontFamily((String) cbFontFamily.getSelectedItem()));
        toolbar.add(cbFontFamily);

        toolbar.add(Box.createHorizontalStrut(4));

        // 4. Font Size combo
        Integer[] sizes = {8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 28, 36};
        cbFontSize = new JComboBox<>(sizes);
        cbFontSize.setSelectedItem(13);
        cbFontSize.setPreferredSize(new Dimension(50, 22));
        cbFontSize.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFontSize.setBackground(Color.WHITE);
        cbFontSize.addActionListener(e -> {
            Integer sz = (Integer) cbFontSize.getSelectedItem();
            if (sz != null) applyFontSize(sz);
        });
        toolbar.add(cbFontSize);

        toolbar.addSeparator(new Dimension(8, 20));

        // 5. Style Toggles: B, I, U, S
        btnBold = createStyleToggle("tsbBold.Image.png", "B", Font.BOLD, "In đậm (Ctrl+B)");
        btnBold.addActionListener(new StyledEditorKit.BoldAction());
        toolbar.add(btnBold);

        btnItalic = createStyleToggle("tsbItalic.Image.png", "I", Font.ITALIC, "In nghiêng (Ctrl+I)");
        btnItalic.addActionListener(new StyledEditorKit.ItalicAction());
        toolbar.add(btnItalic);

        btnUnderline = createStyleToggle("tsbUnderline.Image.png", "U", Font.PLAIN, "Gạch chân (Ctrl+U)");
        btnUnderline.addActionListener(new StyledEditorKit.UnderlineAction());
        toolbar.add(btnUnderline);

        btnStrike = createStyleToggle("tsbStrickOut.Image.png", "S", Font.PLAIN, "Gạch ngang chữ");
        btnStrike.addActionListener(e -> applyStrikethrough());
        toolbar.add(btnStrike);

        toolbar.addSeparator(new Dimension(8, 20));

        // 6. Alignment buttons
        ButtonGroup alignGroup = new ButtonGroup();
        btnAlignLeft = createAlignToggle("tsbLeftAlign.Image.png", "L", "AlignLeft", StyleConstants.ALIGN_LEFT, "Căn trái");
        btnAlignLeft.setSelected(true);
        alignGroup.add(btnAlignLeft);
        toolbar.add(btnAlignLeft);

        btnAlignCenter = createAlignToggle("tsbCenterAlign.Image.png", "C", "AlignCenter", StyleConstants.ALIGN_CENTER, "Căn giữa");
        alignGroup.add(btnAlignCenter);
        toolbar.add(btnAlignCenter);

        btnAlignRight = createAlignToggle("tsbRightAlign.Image.png", "R", "AlignRight", StyleConstants.ALIGN_RIGHT, "Căn phải");
        alignGroup.add(btnAlignRight);
        toolbar.add(btnAlignRight);

        toolbar.addSeparator(new Dimension(8, 20));

        // 7. Bullet / Indent buttons
        btnBullet = createIconButton("tsbBullet.Image.png", "•", "Danh sách dấu chấm");
        btnBullet.addActionListener(e -> insertBulletPoint());
        toolbar.add(btnBullet);

        btnDecreaseIndent = createIconButton("tsbDecIndent.Image.png", "«", "Giảm thụt lề");
        btnDecreaseIndent.addActionListener(e -> changeIndent(-15));
        toolbar.add(btnDecreaseIndent);

        btnIncreaseIndent = createIconButton("tsbIncIndent.Image.png", "»", "Tăng thụt lề");
        btnIncreaseIndent.addActionListener(e -> changeIndent(15));
        toolbar.add(btnIncreaseIndent);

        toolbar.addSeparator(new Dimension(8, 20));

        // 8. Timestamp & Divider
        btnTime = createIconButton("tsbTimeStamp.Image.png", "🕒", "Chèn ngày giờ hiện tại");
        btnTime.addActionListener(e -> insertTimestamp());
        toolbar.add(btnTime);

        btnSeparator = createIconButton("tsbLine.Image.png", "—", "Chèn đường kẻ ngang");
        btnSeparator.addActionListener(e -> insertDivider());
        toolbar.add(btnSeparator);

        return toolbar;
    }

    private JButton createIconButton(String iconName, String fallbackText, String tooltip) {
        JButton btn = new JButton();
        ImageIcon icon = IconHelper.getIcon(iconName, 16, 16);
        if (icon != null) {
            btn.setIcon(icon);
        } else if (fallbackText != null) {
            btn.setText(fallbackText);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setMargin(new Insets(1, 2, 1, 2));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBackground(new Color(245, 248, 253));
        btn.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        return btn;
    }

    private JToggleButton createStyleToggle(String iconName, String fallbackText, int style, String tooltip) {
        JToggleButton btn = new JToggleButton();
        ImageIcon icon = IconHelper.getIcon(iconName, 16, 16);
        if (icon != null) {
            btn.setIcon(icon);
        } else if (fallbackText != null) {
            btn.setText(fallbackText);
            btn.setFont(new Font("Segoe UI", style, 12));
        }
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setMargin(new Insets(1, 2, 1, 2));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBackground(new Color(245, 248, 253));
        btn.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        return btn;
    }

    private JToggleButton createAlignToggle(String iconName, String fallbackText, String actionName, int alignment, String tooltip) {
        JToggleButton btn = new JToggleButton();
        ImageIcon icon = IconHelper.getIcon(iconName, 16, 16);
        if (icon != null) {
            btn.setIcon(icon);
        } else if (fallbackText != null) {
            btn.setText(fallbackText);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        }
        btn.addActionListener(new StyledEditorKit.AlignmentAction(actionName, alignment));
        btn.setPreferredSize(new Dimension(24, 22));
        btn.setMargin(new Insets(1, 2, 1, 2));
        btn.setFocusPainted(false);
        btn.setToolTipText(tooltip);
        btn.setBackground(new Color(245, 248, 253));
        btn.setBorder(new LineBorder(new Color(185, 205, 230), 1));
        return btn;
    }

    // ==================== ACTIONS & FORMATTING ====================

    private void applyFontFamily(String family) {
        if (family == null || isUpdatingUi) return;
        Action action = new StyledEditorKit.FontFamilyAction("FontFamily", family);
        action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, null));
        textPane.requestFocusInWindow();
    }

    private void applyFontSize(int size) {
        if (isUpdatingUi) return;
        Action action = new StyledEditorKit.FontSizeAction("FontSize", size);
        action.actionPerformed(new ActionEvent(textPane, ActionEvent.ACTION_PERFORMED, null));
        textPane.requestFocusInWindow();
    }

    private void applyStrikethrough() {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();
        if (start == end) return;

        Element element = doc.getCharacterElement(start);
        AttributeSet as = element.getAttributes();
        boolean isStrike = StyleConstants.isStrikeThrough(as);

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setStrikeThrough(sas, !isStrike);
        doc.setCharacterAttributes(start, end - start, sas, false);
        textPane.requestFocusInWindow();
    }

    private void insertBulletPoint() {
        try {
            int pos = textPane.getCaretPosition();
            textPane.getDocument().insertString(pos, " • ", null);
            textPane.requestFocusInWindow();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void changeIndent(int delta) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        Element element = doc.getParagraphElement(start);
        AttributeSet as = element.getAttributes();
        float currentIndent = StyleConstants.getLeftIndent(as);
        float newIndent = Math.max(0, currentIndent + delta);

        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setLeftIndent(sas, newIndent);
        doc.setParagraphAttributes(start, 1, sas, false);
        textPane.requestFocusInWindow();
    }

    private void insertTimestamp() {
        try {
            String timeStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
            int pos = textPane.getCaretPosition();
            textPane.getDocument().insertString(pos, "[" + timeStr + "] ", null);
            textPane.requestFocusInWindow();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void insertDivider() {
        try {
            int pos = textPane.getCaretPosition();
            String divider = "\n------------------------------------------------------------\n";
            textPane.getDocument().insertString(pos, divider, null);
            textPane.requestFocusInWindow();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // ==================== LOAD & SAVE NOTES ====================

    private String getCurrentUserId() {
        if (AuthService.getCurrentUser() != null && AuthService.getCurrentUser().getId() != null) {
            return AuthService.getCurrentUser().getId();
        }
        return "1";
    }

    private void loadNotes() {
        isUpdatingUi = true;
        try {
            noteList.clear();
            String userId = getCurrentUserId();
            noteList.addAll(noteDAO.getNotesByUserId(userId));

            if (noteList.isEmpty()) {
                SQuickNote firstNote = new SQuickNote();
                firstNote.setSortOrder(1);
                firstNote.setUserCreatedId(userId);
                firstNote.setTimeCreated(new java.sql.Timestamp(System.currentTimeMillis()));
                noteDAO.insertNote(firstNote);
                noteList.add(firstNote);
            }

            currentIndex = 0;
            displayCurrentNote();
        } finally {
            isUpdatingUi = false;
        }
    }

    private void displayCurrentNote() {
        if (noteList.isEmpty()) return;
        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= noteList.size()) currentIndex = noteList.size() - 1;

        isUpdatingUi = true;
        try {
            SQuickNote note = noteList.get(currentIndex);
            lblPage.setText((currentIndex + 1) + "/" + noteList.size());

            textPane.setText("");
            byte[] data = note.getNoteData();
            if (data != null && data.length > 0) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    rtfKit.read(bais, textPane.getDocument(), 0);
                } catch (Exception ex) {
                    // Fallback to text
                    textPane.setText(new String(data, StandardCharsets.UTF_8));
                }
            } else if (note.getContent() != null && !note.getContent().isEmpty()) {
                textPane.setText(note.getContent());
            }

            textPane.setCaretPosition(textPane.getDocument().getLength());

            // Update status bar
            if (lblCreatedTime != null) {
                Date createdDate = note.getTimeCreated() != null ? new Date(note.getTimeCreated().getTime()) : new Date();
                String formattedTime = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(createdDate);
                lblCreatedTime.setText("Khởi tạo: " + formattedTime);
            }
            if (lblCreatedBy != null) {
                String creatorName = getUserDisplayName(note.getUserCreatedId());
                lblCreatedBy.setText("Khởi tạo bởi: " + creatorName);
            }
        } finally {
            isUpdatingUi = false;
        }
    }

    private String getUserDisplayName(String userId) {
        if (userId == null || userId.trim().isEmpty() || "1".equals(userId)) {
            return "Administrator";
        }
        try {
            SUser user = userDAO.findById(userId);
            if (user != null) {
                if (user.getName() != null && !user.getName().trim().isEmpty()) {
                    return user.getName();
                }
                if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
                    return user.getUsername();
                }
            }
        } catch (Exception ignored) {
        }
        return "Administrator";
    }

    private void triggerAutoSave() {
        if (isUpdatingUi) return;
        if (autoSaveTimer != null) {
            autoSaveTimer.restart();
        }
    }

    private void saveCurrentNote() {
        if (isUpdatingUi || noteList.isEmpty() || currentIndex < 0 || currentIndex >= noteList.size()) {
            return;
        }

        SQuickNote note = noteList.get(currentIndex);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            rtfKit.write(baos, textPane.getDocument(), 0, textPane.getDocument().getLength());
            byte[] rtfBytes = baos.toByteArray();

            note.setNoteData(rtfBytes);
            String txt = textPane.getText();
            note.setContent(txt != null ? txt : "");
            if (AuthService.getCurrentUser() != null) {
                note.setUserModifiedId(AuthService.getCurrentUser().getId());
            }

            noteDAO.saveOrUpdate(note);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void goToPrevNote() {
        if (currentIndex > 0) {
            saveCurrentNote();
            currentIndex--;
            displayCurrentNote();
        }
    }

    private void goToNextNote() {
        saveCurrentNote();
        if (currentIndex < noteList.size() - 1) {
            currentIndex++;
            displayCurrentNote();
        } else {
            // If already at last page, check if current page has content, then auto-create new page
            String txt = textPane.getText();
            if (txt != null && !txt.trim().isEmpty()) {
                createNewNote();
            }
        }
    }

    private void createNewNote() {
        saveCurrentNote();

        String userId = getCurrentUserId();
        SQuickNote newNote = new SQuickNote();
        newNote.setSortOrder(noteList.size() + 1);
        newNote.setTimeCreated(new java.sql.Timestamp(System.currentTimeMillis()));
        newNote.setUserCreatedId(userId);
        noteDAO.insertNote(newNote);
        noteList.add(newNote);

        currentIndex = noteList.size() - 1;
        displayCurrentNote();
    }

    private void deleteCurrentNote() {
        if (noteList.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn muốn xóa trang ghi chú này không?",
                "Xác nhận xóa ghi chú",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        SQuickNote note = noteList.get(currentIndex);
        if (note.getId() != null) {
            noteDAO.deleteNote(note.getId());
        }
        noteList.remove(currentIndex);

        if (noteList.isEmpty()) {
            String userId = getCurrentUserId();
            SQuickNote blankNote = new SQuickNote();
            blankNote.setSortOrder(1);
            blankNote.setTimeCreated(new java.sql.Timestamp(System.currentTimeMillis()));
            blankNote.setUserCreatedId(userId);
            noteDAO.insertNote(blankNote);
            noteList.add(blankNote);
            currentIndex = 0;
        } else if (currentIndex >= noteList.size()) {
            currentIndex = noteList.size() - 1;
        }

        displayCurrentNote();
    }
}