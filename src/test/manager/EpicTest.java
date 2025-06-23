package test.manager;

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
    void addSubtaskIdTest() {
        epic.addSubtaskId(testSubtaskId);

        assertEquals(3, epic.getSubtaskIds().size());
        assertEquals(testSubtaskId, epic.getSubtaskIds().get(2));

    }


    @Test
    void removeSubtaskId_shouldRemoveIdTest() {
        epic.addSubtaskId(testSubtaskId);
        epic.removeSubtaskId(testSubtaskId);

        assertFalse(epic.getSubtaskIds().contains(testSubtaskId));

    }

    @Test
    void GetSubtaskIdsTest() {
        ArrayList<Long> expectedList = new ArrayList<>();
        expectedList.add(1L);
        expectedList.add(2L);

        ArrayList<Long> actualList = epic.getSubtaskIds();

        assertEquals(expectedList, actualList);
    }

}