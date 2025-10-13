package manager;

import managers.InMemoryTaskManager;
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

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        // Используем InMemoryTaskManager напрямую - все методы уже реализованы
        return new InMemoryTaskManager();
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
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 16, 10, 0));
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

        taskManager.updateEpic(updated); // Используем updateEpic вместо updateEppic
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
    }

    @Test
    void testUpdateSubTask() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask created = taskManager.createSubtask(subTask1);

        SubTask updated = new SubTask("Updated Sub", "Updated Desc", Status.IN_PROGRESS,
                epicCreated.getId(), Duration.ofHours(1), LocalDateTime.of(2024, 1, 16, 10, 0));
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

        List<SubTask> subTasks = taskManager.getAllSubTasks(); // Исправлен вызов метода
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

        List<SubTask> subTasks = taskManager.getAllSubTasks(); // Исправлен вызов метода
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

        // Проверяем порядок по времени начала
        int taskIndex = prioritized.indexOf(task1);
        int subTaskIndex = prioritized.indexOf(subTaskCreated);

        // task1 начинается в 10:00, subTask1 в 11:30, поэтому task1 должен быть раньше
        assertTrue(taskIndex < subTaskIndex);
    }

    @Test
    void testSubTaskHasEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        SubTask subTask = new SubTask("Sub", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());

        SubTask created = taskManager.createSubtask(subTask);
        assertEquals(epic.getId(), created.getEpicId());

        // Проверяем, что эпик знает о подзадаче - получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertTrue(updatedEpic.getSubtaskIds().contains(created.getId()));
    }

    @Test
    void testEpicStatusCalculation() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = new SubTask("Sub1", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        SubTask sub2 = new SubTask("Sub2", "Desc", Status.DONE, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        // Получаем обновленный эпик для проверки статуса
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getTaskStatus());
    }

    @Test
    void testTimeOverlapDetection() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));

        // Пересекающаяся задача
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 11, 0));

        taskManager.createTask(task1);

        // Должно выбросить исключение при пересечении
        assertThrows(RuntimeException.class, () -> {
            taskManager.createTask(task2);
        });
    }

    @Test
    void testNoTimeOverlap() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        // Непересекающаяся задача
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 12, 0));

        taskManager.createTask(task1);

        // Не должно быть исключения
        assertDoesNotThrow(() -> {
            taskManager.createTask(task2);
        });
    }

    @Test
    void testTimeOverlapException() {
        // Создаем первую задачу
        Task task1 = new Task("Task1", "Description", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));

        taskManager.createTask(task1);

        // Пересекающаяся задача (11:00-12:00 пересекается с 10:00-12:00)
        Task overlappingTask = new Task("Task2", "Description", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 11, 0));

        // Должно выбросить исключение при создании пересекающейся задачи
        assertThrows(RuntimeException.class, () -> {
            taskManager.createTask(overlappingTask);
        }, "Задачи пересекаются по времени - должно быть исключение");
    }

    @Test
    void testGetPrioritizedTasksOrder() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 12, 0));
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        // task2 должна быть первой (более раннее время)
        assertEquals("Task2", prioritized.get(0).getName());
        assertEquals("Task1", prioritized.get(1).getName());
    }

    @Test
    void testTasksWithoutTimeNotInPrioritized() {
        Task taskWithTime = new Task("WithTime", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));
        Task taskWithoutTime = new Task("WithoutTime", "Desc", Status.NEW);

        taskManager.createTask(taskWithTime);
        taskManager.createTask(taskWithoutTime);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals("WithTime", prioritized.get(0).getName());
    }

    @Test
    void testDeleteEpic() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask subTaskCreated = taskManager.createSubtask(subTask1);

        // Удаляем эпик
        Epic deletedEpic = taskManager.deleteEpic(epicCreated.getId());

        assertNotNull(deletedEpic);
        assertEquals(epicCreated.getId(), deletedEpic.getId());

        // Проверяем, что эпик удален
        assertNull(taskManager.getEpic(epicCreated.getId()));

        // Проверяем, что подзадачи тоже удалены
        assertNull(taskManager.getSubTask(subTaskCreated.getId()));
    }

    @Test
    void testDeleteNonExistentEpic() {
        Epic deletedEpic = taskManager.deleteEpic(999L);
        assertNull(deletedEpic);
    }

    @Test
    void testEpicStatusAfterSubTaskDeletion() {
        Epic epicCreated = taskManager.createEpic(epic);
        subTask1.setEpicId(epicCreated.getId());
        SubTask subTaskCreated = taskManager.createSubtask(subTask1);

        // Удаляем подзадачу
        taskManager.deleteById(subTaskCreated.getId());

        // Проверяем статус эпика после удаления подзадачи - получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epicCreated.getId());
        assertEquals(Status.NEW, updatedEpic.getTaskStatus());
    }

    @Test
    void testHistoryLimit() {
        // Создаем больше задач, чем максимальный размер истории
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task" + i, "Desc" + i, Status.NEW);
            Task created = taskManager.createTask(task);
            taskManager.getTask(created.getId()); // Добавляем в историю
        }

        List<Task> history = taskManager.getHistory();
        // История не должна превышать максимальный размер
        assertTrue(history.size() <= 10);
    }

    @Test
    void testTaskEquality() {
        Task task1 = new Task("Task", "Description", Status.NEW);
        Task task2 = new Task("Task", "Description", Status.NEW);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void testSubTaskEquality() {
        SubTask subTask1 = new SubTask("SubTask", "Description", Status.NEW, 1L);
        SubTask subTask2 = new SubTask("SubTask", "Description", Status.NEW, 1L);

        subTask1.setId(1);
        subTask2.setId(1);

        assertEquals(subTask1, subTask2);
        assertEquals(subTask1.hashCode(), subTask2.hashCode());
    }

    @Test
    void testEpicEquality() {
        Epic epic1 = new Epic("Epic", "Description");
        Epic epic2 = new Epic("Epic", "Description");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2);
        assertEquals(epic1.hashCode(), epic2.hashCode());
    }
}