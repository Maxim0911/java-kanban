package manager;

import managers.TaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
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

        task = new Task("Test Task", "Test Description", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        epic = new Epic("Test Epic", "Test Epic Description");

        subTask1 = new SubTask("SubTask 1", "Description 1", Status.NEW, 0L,
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 15, 11, 30));

        subTask2 = new SubTask("SubTask 2", "Description 2", Status.NEW, 0L,
                Duration.ofMinutes(45), LocalDateTime.of(2024, 1, 15, 12, 30));
    }

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

        taskManager.updateEpic(updated); // Исправлено: updateEppic -> updateEpic
        Epic retrieved = taskManager.getEpic(created.getId());
        assertEquals("Updated Epic", retrieved.getName());
    }

    @Test
    void testCreateAndGetSubTask() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());

        SubTask created = taskManager.createSubtask(subTask1);
        assertNotNull(created.getId());
        assertEquals(epicCreated.getId(), created.getEpicId());

        // Дополнительная проверка: эпик должен знать о подзадаче
        Epic updatedEpic = taskManager.getEpic(epicCreated.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(created.getId()));
    }

    @Test
    void testUpdateSubTask() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask created = taskManager.createSubtask(subTask1);

        SubTask updated = new SubTask("Updated Sub", "Updated Desc", Status.IN_PROGRESS,
                epicCreated.getId(), Duration.ofHours(1), LocalDateTime.of(2024, 1, 16, 10, 0)); // Другой день
        updated.setId(created.getId());

        taskManager.updateSubtask(updated);
        SubTask retrieved = taskManager.getSubTask(created.getId());
        assertEquals("Updated Sub", retrieved.getName());
        assertEquals(Status.IN_PROGRESS, retrieved.getTaskStatus());
    }

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

        List<SubTask> subTasks = taskManager.getAllSubTasks(); // Исправлено: getAllSubTask -> getAllSubTasks
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

        List<SubTask> subTasks = taskManager.getAllSubTasks(); // Исправлено: getAllSubTask -> getAllSubTasks
        assertTrue(subTasks.isEmpty());
    }

    @Test
    void testGetPrioritizedTasks() {
        Task task1 = taskManager.createTask(task);
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask subTaskCreated = taskManager.createSubtask(subTask1);

        List<Task> prioritized = taskManager.getPrioritizedTasks();

        assertTrue(prioritized.size() >= 2);
        assertTrue(prioritized.contains(task1));
        assertTrue(prioritized.contains(subTaskCreated));

        int taskIndex = prioritized.indexOf(task1);
        int subTaskIndex = prioritized.indexOf(subTaskCreated);

        assertTrue(taskIndex < subTaskIndex);
    }
}