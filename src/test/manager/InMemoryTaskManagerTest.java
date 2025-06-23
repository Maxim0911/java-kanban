package test.manager;

import manager.InMemoryTaskManager;
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
        Task task = taskManager.createTask(new Task("Test", "Description", Status.NEW));
        Task foundTask = taskManager.getTask(task.getId());

        assertEquals(task, foundTask);
        assertNull(taskManager.getTask(999));
    }

    @Test
    void updateTask() {
        Task task = taskManager.createTask(new Task("Original", "Description", Status.NEW));
        Task updated = new Task("Updated", "New Description", Status.IN_PROGRESS);
        updated.setId(task.getId());

        taskManager.updateTask(updated);
        Task result = taskManager.getTask(task.getId());

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
        Epic epic = taskManager.createEpic(new Epic("Test", "Description"));
        Epic foundEpic = taskManager.getEpic(epic.getId());

        assertEquals(epic, foundEpic);
        assertNull(taskManager.getEpic(999));
    }

    @Test
    void updateEppic() {
        Epic epic = taskManager.createEpic(new Epic("Original", "Description"));
        Epic updated = new Epic("Updated", "New Description");
        updated.setId(epic.getId());

        taskManager.updateEppic(updated);
        Epic result = taskManager.getEpic(epic.getId());

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
        Epic epic = taskManager.createEpic(new Epic("Parent", "Description"));
        SubTask subTask = new SubTask("Sub", "Description", Status.NEW, epic.getId());

        SubTask created = taskManager.createSubtask(subTask);

        assertNotNull(created.getId());
        assertEquals(1, taskManager.getAllSubTask().size());
        assertEquals(1, epic.getSubtaskIds().size());
    }

    @Test
    void getSubTask() {
        Epic epic = taskManager.createEpic(new Epic("Parent", "Description"));
        SubTask subTask = taskManager.createSubtask(new SubTask("Sub", "Description", Status.NEW, epic.getId()));

        SubTask found = taskManager.getSubTask(subTask.getId());

        assertEquals(subTask, found);
        assertNull(taskManager.getSubTask(999L));
    }

    @Test
    void updateSubtask() {
        Epic epic = taskManager.createEpic(new Epic("Parent", "Description"));
        SubTask subTask = taskManager.createSubtask(new SubTask("Original", "Description", Status.NEW, epic.getId()));

        SubTask updated = new SubTask("Updated", "New Description", Status.DONE, epic.getId());
        updated.setId(subTask.getId());
        taskManager.updateSubtask(updated);

        SubTask result = taskManager.getSubTask(subTask.getId());
        assertEquals("Updated", result.getName());
        assertEquals(Status.DONE, result.getTaskStatus());
    }

    @Test
    void deleteById() {
        Epic epic = taskManager.createEpic(new Epic("Parent", "Description"));
        SubTask subTask = taskManager.createSubtask(new SubTask("To delete", "Description", Status.NEW, epic.getId()));

        taskManager.deleteById(subTask.getId());

        assertNull(taskManager.getSubTask(subTask.getId()));
        assertEquals(0, epic.getSubtaskIds().size());
    }

    @Test
    void getHistory() {
        Task task = taskManager.createTask(new Task("Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Epic", "Description"));

        taskManager.getTask(task.getId());
        taskManager.getEpic(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
    }
}
