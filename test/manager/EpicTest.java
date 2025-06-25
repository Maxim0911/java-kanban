package manager;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.Epic;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private Epic epic;
    private final long testSubtaskId = 123L;

    @BeforeEach
    void startEpic() {
        epic = new Epic("Epic", "Description");
        epic.subtaskIds = new ArrayList<>();
        epic.subtaskIds.add(1L);
        epic.subtaskIds.add(2L);
    }

    @Test
    void addSubtaskIdShouldAddNewId() {
        epic.addSubtaskId(testSubtaskId);

        assertEquals(3, epic.getSubtaskIds().size());
        assertEquals(testSubtaskId, epic.getSubtaskIds().get(2));
    }

    @Test
    void removeSubtaskIdShouldRemoveExistingId() {
        epic.addSubtaskId(testSubtaskId);
        epic.removeSubtaskId(testSubtaskId);

        assertFalse(epic.getSubtaskIds().contains(testSubtaskId));
        assertEquals(2, epic.getSubtaskIds().size());
    }

    @Test
    void getSubtaskIdsShouldReturnCurrentList() {
        ArrayList<Long> expectedList = new ArrayList<>();
        expectedList.add(1L);
        expectedList.add(2L);

        assertEquals(expectedList, epic.getSubtaskIds());
    }

    @Test
    void equalsShouldReturnTrueForSameEpics() {
        Epic sameEpic = new Epic("Epic", "Description");
        sameEpic.subtaskIds = new ArrayList<>(epic.subtaskIds);

        assertTrue(epic.equals(sameEpic));
    }

    @Test
    void equalsShouldReturnFalseForDifferentEpics() {
        Epic differentEpic = new Epic("Different", "Description");
        differentEpic.subtaskIds = new ArrayList<>();

        assertFalse(epic.equals(differentEpic));
    }

    @Test
    void hashCodeShouldBeConsistent() {
        int initialHashCode = epic.hashCode();
        assertEquals(initialHashCode, epic.hashCode());
    }
}