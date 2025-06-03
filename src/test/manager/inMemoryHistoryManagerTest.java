package test.manager;

import manager.inMemoryHistoryManager;
import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private inMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new inMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3", Status.DONE);
        task3.setId(3);
    }

    @Test
    void addShouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void addShouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    @Test
    void addShouldMoveExistingTaskToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Добавляем существующую задачу

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }



    @Test
    void getHistoryShouldReturnCopyOfHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        history.add(task2); // Модифицируем полученный список

        List<Task> originalHistory = historyManager.getHistory();
        assertEquals(1, originalHistory.size());
    }

    @Test
    void historyShouldMaintainInsertionOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void addShouldHandleDuplicateTasksCorrectly() {
        Task task1Copy = new Task(task1.getName(), task1.getDescription(), task1.getTaskStatus());
        task1Copy.setId(task1.getId());

        historyManager.add(task1);
        historyManager.add(task1Copy);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
    }
}