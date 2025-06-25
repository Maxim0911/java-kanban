package manager;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2", Status.NEW);
        task2.setId(2);
    }

    @Test
    void addShouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void removeShouldDeleteTaskFromHistory() {
        // Добавляем задачи в историю
        historyManager.add(task1);
        historyManager.add(task2);

        // Убеждаемся, что обе задачи присутствуют в истории
        assertEquals(2, historyManager.getHistory().size());
        assertTrue(historyManager.getHistory().contains(task1));
        assertTrue(historyManager.getHistory().contains(task2));

        // Удаляем task1 из истории
        historyManager.remove(task1);

        // Проверяем, что task1 удалён, а task2 остаётся в истории
        assertEquals(1, historyManager.getHistory().size());
        assertFalse(historyManager.getHistory().contains(task1));
        assertTrue(historyManager.getHistory().contains(task2));
    }



@Test
    void removeNonExistentTaskShouldDoNothing() {
        historyManager.add(task1);
        historyManager.remove(task2); // task2 не добавлен

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void getHistoryShouldReturnEmptyListForEmptyHistory() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getHistoryShouldReturnCopyOfInternalList() {
        historyManager.add(task1);
        List<Task> history1 = historyManager.getHistory();
        List<Task> history2 = historyManager.getHistory();

        assertNotSame(history1, history2);
        assertEquals(history1, history2);
    }
}