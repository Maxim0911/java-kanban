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
    void testEquals() {
        SubTask subTask1 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask2 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask3 = new SubTask(testName, testDescription, testStatus, 2L);

        // Устанавливаем одинаковые ID для сравнения
        subTask1.setId(1);
        subTask2.setId(1);
        subTask3.setId(2);

        // Проверяем рефлексивность
        assertEquals(subTask1, subTask1);

        // Проверяем симметричность
        assertEquals(subTask1, subTask2);
        assertEquals(subTask2, subTask1);

        // Проверяем разные объекты
        assertNotEquals(subTask1, subTask3);
        assertNotEquals(subTask3, subTask1);

        // Проверяем с null
        assertNotEquals(null, subTask1);

        // Проверяем с объектом другого класса
        assertNotEquals("not a subtask", subTask1);
    }

    @Test
    void testHashCode() {
        SubTask subTask1 = new SubTask(testName, testDescription, testStatus, testEpicId);
        SubTask subTask2 = new SubTask(testName, testDescription, testStatus, testEpicId);

        // Устанавливаем одинаковые ID для корректного сравнения хэш-кодов
        subTask1.setId(1);
        subTask2.setId(1);

        assertEquals(subTask1.hashCode(), subTask2.hashCode());

        // Проверяем консистентность - многократный вызов возвращает одинаковый результат
        int firstHash = subTask1.hashCode();
        int secondHash = subTask1.hashCode();
        assertEquals(firstHash, secondHash);
    }

    @Test
    void testToString() {
        SubTask subTask = new SubTask(testName, testDescription, testStatus, testEpicId);
        subTask.setId(1);

        String toStringResult = subTask.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("SubTask"));
        assertTrue(toStringResult.contains("id=1"));
        assertTrue(toStringResult.contains("name=" + testName));
        assertTrue(toStringResult.contains("epicId=" + testEpicId));
    }

    @Test
    void testGetType() {
        assertEquals("SUBTASK", subTask.getType());
    }

    @Test
    void testDefaultConstructor() {
        // Тестируем конструктор по умолчанию
        SubTask defaultSubTask = new SubTask();

        assertNotNull(defaultSubTask);
        assertEquals(0, defaultSubTask.getId());
        assertEquals("", defaultSubTask.getName());
        assertEquals("", defaultSubTask.getDescription());
        assertEquals(Status.NEW, defaultSubTask.getTaskStatus());
        assertEquals(0, defaultSubTask.getEpicId());
    }
}