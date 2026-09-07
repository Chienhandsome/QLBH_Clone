package kazuko.ex.dao;

import kazuko.ex.model.system.SQuickNote;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class QuickNoteFeatureTest {

    @Test
    public void testQuickNoteCrud() {
        QuickNoteDAO dao = new QuickNoteDAO();

        // 1. Create note
        SQuickNote note = new SQuickNote();
        String testId = UUID.randomUUID().toString();
        note.setId(testId);
        note.setSortOrder(999);
        note.setContent("Test Quick Note Content - Ghi chu nhanh");
        note.setNoteData("Test Quick Note Content - Ghi chu nhanh".getBytes(StandardCharsets.UTF_8));

        boolean inserted = dao.insertNote(note);
        assertTrue(inserted, "Insert quick note should succeed");

        // 2. Read notes
        List<SQuickNote> list = dao.getAllNotes();
        assertNotNull(list);
        assertFalse(list.isEmpty());

        SQuickNote found = null;
        for (SQuickNote n : list) {
            if (testId.equals(n.getId())) {
                found = n;
                break;
            }
        }
        assertNotNull(found, "Inserted note should be in list");
        assertTrue(found.getContent().contains("Test Quick Note Content"));

        // 3. Update note
        found.setContent("Updated content - Ghi chu nhanh da cap nhat");
        found.setNoteData("Updated content - Ghi chu nhanh da cap nhat".getBytes(StandardCharsets.UTF_8));
        boolean updated = dao.updateNote(found);
        assertTrue(updated, "Update quick note should succeed");

        // 4. Delete note
        boolean deleted = dao.deleteNote(testId);
        assertTrue(deleted, "Delete quick note should succeed");

        List<SQuickNote> listAfter = dao.getAllNotes();
        boolean stillExists = listAfter.stream().anyMatch(n -> testId.equals(n.getId()));
        assertFalse(stillExists, "Note should no longer exist after deletion");

        System.out.println(">> QuickNote CRUD Test passed successfully!");
    }

    @Test
    public void testDialogCreation() {
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            kazuko.ex.ui.system.GhiChuNhanhDialog dialog = new kazuko.ex.ui.system.GhiChuNhanhDialog(null);
            assertNotNull(dialog);
            dialog.dispose();
            System.out.println(">> GhiChuNhanhDialog instantiation test passed!");
        }
    }

    @Test
    public void testUserSpecificNotes() {
        QuickNoteDAO dao = new QuickNoteDAO();
        String userA = "user_A_" + UUID.randomUUID().toString().substring(0, 8);
        String userB = "user_B_" + UUID.randomUUID().toString().substring(0, 8);

        // Note for user A
        SQuickNote noteA = new SQuickNote();
        String noteAId = UUID.randomUUID().toString();
        noteA.setId(noteAId);
        noteA.setUserCreatedId(userA);
        noteA.setContent("Ghi chú bí mật của User A");
        assertTrue(dao.insertNote(noteA));

        // Note for user B
        SQuickNote noteB = new SQuickNote();
        String noteBId = UUID.randomUUID().toString();
        noteB.setId(noteBId);
        noteB.setUserCreatedId(userB);
        noteB.setContent("Ghi chú riêng của User B");
        assertTrue(dao.insertNote(noteB));

        // Verify User A only sees their note
        List<SQuickNote> listA = dao.getNotesByUserId(userA);
        assertEquals(1, listA.size());
        assertEquals("Ghi chú bí mật của User A", listA.get(0).getContent());

        // Verify User B only sees their note
        List<SQuickNote> listB = dao.getNotesByUserId(userB);
        assertEquals(1, listB.size());
        assertEquals("Ghi chú riêng của User B", listB.get(0).getContent());

        // Clean up
        dao.deleteNote(noteAId);
        dao.deleteNote(noteBId);
        System.out.println(">> User-specific note isolation test passed successfully!");
    }
}