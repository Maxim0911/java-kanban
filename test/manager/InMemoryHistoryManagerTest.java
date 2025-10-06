package manager;

import managers.InMemoryHistoryManager;
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
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task1", "Desc1", Status.NEW);
        task1.setId(1);
        task2 = new Task("Task2", "Desc2", Status.IN_PROGRESS);
        task2.setId(2);
        task3 = new Task("Task3", "Desc3", Status.DONE);
        task3.setId(3);
    }

    @Test
    void testEmptyHistory() {
        // a. Пустая история задач
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void testAddToHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testDuplicateHandling() {
        // b. Дублирование
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // Дубликат

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size()); // Дубликаты не добавляются
        assertEquals(task2, history.get(0)); // task2 теперь первый
        assertEquals(task1, history.get(1)); // task1 переместился в конец
    }

    @Test
    void testRemoveFromBeginning() {
        // c. Удаление из начала
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void testRemoveFromMiddle() {
        // c. Удаление из середины
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void testRemoveFromEnd() {
        // c. Удаление из конца
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveNonExistent() {
        historyManager.add(task1);
        historyManager.remove(999); // Несуществующий ID

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void testHistoryOrder() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    // Добавляем тест на ограничение размера истории
    @Test
    void testHistorySizeLimit() {
        // Создаем больше задач, чем максимальный размер истории (10)
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Task" + i, "Desc" + i, Status.NEW);
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        // История не должна превышать максимальный размер (10)
        assertTrue(history.size() <= 10, "История не должна превышать 10 элементов");

        // Проверяем, что остались последние добавленные задачи
        assertEquals(6L, history.get(0).getId(), "Первым должен быть 6й элемент");
        assertEquals(15L, history.get(history.size() - 1).getId(),
                "Последним должен быть 15й элемент");
    }

    // Тест на добавление null задачи
    @Test
    void testAddNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна остаться пустой при добавлении null");
    }

    // Тест на последовательное добавление одинаковых задач
    @Test
    void testConsecutiveDuplicateAdd() {
        historyManager.add(task1);
        historyManager.add(task1);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Должна быть только одна уникальная задача");
        assertEquals(task1, history.get(0));
    }
}