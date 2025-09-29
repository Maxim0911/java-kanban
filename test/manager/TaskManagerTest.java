package manager;

import managers.TaskManager;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected SubTask subTask1;
    protected SubTask subTask2;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();

        // ИСПРАВЛЕНИЕ: Задачи не должны пересекаться по времени
        task = new Task("Test Task", "Test Description", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        epic = new Epic("Test Epic", "Test Epic Description");

        // ИСПРАВЛЕНИЕ: Подзадачи начинаются ПОСЛЕ окончания task
        // Task: 10:00 - 11:00, SubTask1: 11:30 - 12:00
        subTask1 = new SubTask("SubTask 1", "Description 1", Status.NEW, 0L,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 15, 11, 30));

        // SubTask2: 12:30 - 13:15
        subTask2 = new SubTask("SubTask 2", "Description 2", Status.NEW, 0L,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 1, 15, 12, 30));
    }

    // Тесты для Task
    @Test
    void testCreateAndGetTask() {
        Task created = taskManager.createTask(task);
        assertNotNull(created.getId());
        assertEquals(task.getName(), created.getName());

        Task retrieved = taskManager.getTask(created.getId());
        assertEquals(created, retrieved);
    }

    @Test
    void testUpdateTask() {
        Task created = taskManager.createTask(task);
        // ИСПРАВЛЕНИЕ: Обновленная задача не должна пересекаться с другими
        Task updated = new Task("Updated", "Updated", Status.IN_PROGRESS,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 16, 10, 0)); // Другой день
        updated.setId(created.getId());

        taskManager.updateTask(updated);
        Task retrieved = taskManager.getTask(created.getId());
        assertEquals("Updated", retrieved.getName());
        assertEquals(Status.IN_PROGRESS, retrieved.getTaskStatus());
    }

    @Test
    void testDeleteTask() {
        Task created = taskManager.createTask(task);
        taskManager.deleteTask(created.getId());

        assertNull(taskManager.getTask(created.getId()));
    }

    // Тесты для Epic
    @Test
    void testCreateAndGetEpic() {
        Epic created = taskManager.createEpic(epic);
        assertNotNull(created.getId());
        assertEquals(epic.getName(), created.getName());
    }

    @Test
    void testUpdateEpic() {
        Epic created = taskManager.createEpic(epic);
        Epic updated = new Epic("Updated Epic", "Updated Description");
        updated.setId(created.getId());

        taskManager.updateEppic(updated);
        Epic retrieved = taskManager.getEpic(created.getId());
        assertEquals("Updated Epic", retrieved.getName());
    }

    // Тесты для SubTask
    @Test
    void testCreateAndGetSubTask() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());

        SubTask created = taskManager.createSubtask(subTask1);
        assertNotNull(created.getId());
        assertEquals(epicCreated.getId(), created.getEpicId());
    }

    @Test
    void testUpdateSubTask() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask created = taskManager.createSubtask(subTask1);

        // ИСПРАВЛЕНИЕ: Обновленная подзадача не должна пересекаться
        SubTask updated = new SubTask("Updated Sub", "Updated Desc", Status.IN_PROGRESS,
                epicCreated.getId(), Duration.ofHours(1), LocalDateTime.of(2024, 1, 16, 10, 0)); // Другой день
        updated.setId(created.getId());

        taskManager.updateSubtask(updated);
        SubTask retrieved = taskManager.getSubTask(created.getId());
        assertEquals("Updated Sub", retrieved.getName());
        assertEquals(Status.IN_PROGRESS, retrieved.getTaskStatus());
    }

    // Тесты для получения всех задач определенного типа
    @Test
    void testGetAllEpics() {
        taskManager.createEpic(epic);
        List<Epic> epics = taskManager.getAllEpics();
        assertEquals(1, epics.size());
    }

    @Test
    void testGetAllSubTasks() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        taskManager.createSubtask(subTask1);

        List<SubTask> subTasks = taskManager.getAllSubTask();
        assertEquals(1, subTasks.size());
    }

    @Test
    void testGetHistory() {
        Task created = taskManager.createTask(task);
        taskManager.getTask(created.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(created, history.get(0));
    }

    @Test
    void testDeleteAllTasks() {
        taskManager.createTask(task);
        taskManager.deletedAllTask();

        assertNull(taskManager.getTask(task.getId()));
    }

    @Test
    void testDeleteAllEpics() {
        taskManager.createEpic(epic);
        taskManager.deleteAllEpics();

        List<Epic> epics = taskManager.getAllEpics();
        assertTrue(epics.isEmpty());
    }

    @Test
    void testDeleteAllSubTasks() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        taskManager.createSubtask(subTask1);

        taskManager.deleteAllSubTask();

        List<SubTask> subTasks = taskManager.getAllSubTask();
        assertTrue(subTasks.isEmpty());
    }

    // Дополнительный тест для проверки приоритетов
    @Test
    void testGetPrioritizedTasks() {
        Task task1 = taskManager.createTask(task);
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask subTaskCreated = taskManager.createSubtask(subTask1);

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        // Epic может быть в списке если у него есть startTime (рассчитанный из подзадач)
        // Поэтому проверяем что в списке есть как минимум Task и SubTask
        assertTrue(prioritized.size() >= 2);
        assertTrue(prioritized.contains(task1));
        assertTrue(prioritized.contains(subTaskCreated));

        // Проверяем порядок (по startTime)
        int taskIndex = prioritized.indexOf(task1);
        int subTaskIndex = prioritized.indexOf(subTaskCreated);

        // Task должен быть перед SubTask (10:00 vs 11:30)
        assertTrue(taskIndex < subTaskIndex);
    }
}