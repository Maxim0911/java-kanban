package manager;

import model.Epic;
import model.SubTask;
import model.Task;
import model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void getTask() {
        Task task = new Task("Test", "Description", Status.NEW);
        Task createdTask = taskManager.createTask(task);
        Task foundTask = taskManager.getTask(createdTask.getId());

        assertEquals(createdTask, foundTask);
        assertNull(taskManager.getTask(999));
    }

    @Test
    void updateTask() {
        Task task = new Task("Original", "Description", Status.NEW);
        Task createdTask = taskManager.createTask(task);
        Task updated = new Task("Updated", "New Description", Status.IN_PROGRESS);
        updated.setId(createdTask.getId());

        taskManager.updateTask(updated);
        Task result = taskManager.getTask(createdTask.getId());

        assertEquals("Updated", result.getName());
        assertEquals(Status.IN_PROGRESS, result.getTaskStatus());
    }

    @Test
    void createEpic() {
        Epic epic = new Epic("Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        assertNotNull(createdEpic.getId());
        assertEquals(Status.NEW, createdEpic.getTaskStatus());
        assertTrue(createdEpic.getSubtaskIds().isEmpty());
    }

    @Test
    void getEpic() {
        Epic epic = new Epic("Test", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        Epic foundEpic = taskManager.getEpic(createdEpic.getId());

        assertEquals(createdEpic, foundEpic);
        assertNull(taskManager.getEpic(999));
    }

    @Test
    void updateEppic() {
        Epic epic = new Epic("Original", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        Epic updated = new Epic("Updated", "New Description");
        updated.setId(createdEpic.getId());

        taskManager.updateEppic(updated);
        Epic result = taskManager.getEpic(createdEpic.getId());

        assertEquals("Updated", result.getName());
    }

    @Test
    void deleteAllEpics() {
        taskManager.createEpic(new Epic("Epic 1", "Description"));
        taskManager.createEpic(new Epic("Epic 2", "Description"));

        taskManager.deleteAllEpics();

        assertEquals(0, taskManager.getAllEpics().size());
    }

    @Test
    void createSubtask() {
        Epic epic = new Epic("Parent", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Sub", "Description", Status.NEW, createdEpic.getId());

        SubTask created = taskManager.createSubtask(subTask);

        assertNotNull(created.getId());
        assertEquals(1, taskManager.getAllSubTask().size());
        assertEquals(1, createdEpic.getSubtaskIds().size());
    }

    @Test
    void getSubTask() {
        Epic epic = new Epic("Parent", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Sub", "Description", Status.NEW, createdEpic.getId());
        SubTask createdSubTask = taskManager.createSubtask(subTask);

        SubTask found = taskManager.getSubTask(createdSubTask.getId());

        assertEquals(createdSubTask, found);
        assertNull(taskManager.getSubTask(999L));
    }

    @Test
    void updateSubtask() {
        Epic epic = new Epic("Parent", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Original", "Description", Status.IN_PROGRESS, createdEpic.getId());
        SubTask createdSubTask = taskManager.createSubtask(subTask);

        SubTask updated = new SubTask("Updated", "New Description", Status.DONE, createdEpic.getId());
        updated.setId(createdSubTask.getId());
        taskManager.updateSubtask(updated);

        SubTask result = taskManager.getSubTask(createdSubTask.getId());
        assertEquals("Updated", result.getName());
        assertEquals(Status.DONE, result.getTaskStatus());
    }

    @Test
    void deleteById() {
        Epic epic = new Epic("Parent", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("To delete", "Description", Status.NEW, createdEpic.getId());
        SubTask createdSubTask = taskManager.createSubtask(subTask);

        taskManager.deleteById(createdSubTask.getId());

        assertNull(taskManager.getSubTask(createdSubTask.getId()));
        assertEquals(0, createdEpic.getSubtaskIds().size());
    }

    @Test
    void getHistory() {
        Task task = new Task("Task", "Description", Status.NEW);
        Task createdTask = taskManager.createTask(task);
        Epic epic = new Epic("Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);

        taskManager.getTask(createdTask.getId());
        taskManager.getEpic(createdEpic.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(createdTask, history.get(0));
        assertEquals(createdEpic, history.get(1));
    }
}