package manager;

import exception.TimeOverlapException;
import model.*;
import org.junit.jupiter.api.Test;

import managers.InMemoryTaskManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager() {
            @Override
            public SubTask deleteSubTask(long id) {
                return null;
            }

            @Override
            public SubTask updateSubTask(SubTask subTask) {
                return null;
            }
        };
    }

    @Test
    void testSubTaskHasEpic() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));
        SubTask subTask = new SubTask("Sub", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());

        SubTask created = taskManager.createSubtask(subTask);
        assertEquals(epic.getId(), created.getEpicId());

        // Проверяем, что эпик знает о подзадаче
        assertTrue(epic.getSubtaskIds().contains(created.getId()));
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

        assertEquals(Status.IN_PROGRESS, epic.getTaskStatus());
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
        assertThrows(TimeOverlapException.class, () -> {
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
    void testGetPrioritizedTasks() {
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
}