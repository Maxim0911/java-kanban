package manager;

import model.Status;
import model.SubTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    private SubTask subTask;
    private final long testEpicId = 1L;
    private final String testName = "Test SubTask";
    private final String testDescription = "Test Description";
    private final Status testStatus = Status.IN_PROGRESS;

    @BeforeEach
    void setUp() {
        subTask = new SubTask(testName, testDescription, testStatus, testEpicId);
    }

    @Test
    void getEpicIdTest() {
        assertEquals(testEpicId, subTask.getEpicId());
    }


    @Test
    void setEpicIdTest() {
        long newEpicId = 2L;
        subTask.setEpicId(newEpicId);
        assertEquals(newEpicId, subTask.getEpicId());
    }

    @Test
    void equalsTest() {
        SubTask subTask1 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask2 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask3 = new SubTask(testName, testDescription, testStatus, 2L);

        assertTrue(subTask1.equals(subTask2));
        assertEquals(subTask1, subTask2);

        assertFalse(subTask1.equals(subTask3));
        assertNotEquals(subTask1, subTask3);
    }

    @Test
    void hashCodeTest() {
        SubTask subTask1 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask2 = new SubTask(testName, testDescription, testStatus, testEpicId);
        assertEquals(subTask1.hashCode(), subTask2.hashCode());
    }
}

