package manager;

import managers.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {
    private InMemoryTaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        // Используем InMemoryTaskManager напрямую - все методы уже реализованы
        taskManager = new InMemoryTaskManager();
        epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
    }

    @Test
    void testEpicStatusAllNew() {
        // a. Все подзадачи со статусом NEW
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId(), 0);
        SubTask sub2 = createSubTask("Sub2", Status.NEW, epic.getId(), 1);

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        // Получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getTaskStatus());
    }

    @Test
    void testEpicStatusAllDone() {
        // b. Все подзадачи со статусом DONE
        SubTask sub1 = createSubTask("Sub1", Status.DONE, epic.getId(), 0);
        SubTask sub2 = createSubTask("Sub2", Status.DONE, epic.getId(), 1);

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        // Получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.DONE, updatedEpic.getTaskStatus());
    }

    @Test
    void testEpicStatusNewAndDone() {
        // c. Подзадачи со статусами NEW и DONE
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId(), 0);
        SubTask sub2 = createSubTask("Sub2", Status.DONE, epic.getId(), 1);

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        // Получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getTaskStatus());
    }

    @Test
    void testEpicStatusInProgress() {
        // d. Подзадачи со статусом IN_PROGRESS
        SubTask sub1 = createSubTask("Sub1", Status.IN_PROGRESS, epic.getId(), 0);
        SubTask sub2 = createSubTask("Sub2", Status.IN_PROGRESS, epic.getId(), 1);

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        // Получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getTaskStatus());
    }

    @Test
    void testEpicStatusMixed() {
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId(), 0);
        SubTask sub2 = createSubTask("Sub2", Status.IN_PROGRESS, epic.getId(), 1);
        SubTask sub3 = createSubTask("Sub3", Status.DONE, epic.getId(), 2);

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        taskManager.createSubtask(sub3);

        // Получаем обновленный эпик
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.IN_PROGRESS, updatedEpic.getTaskStatus());
    }

    @Test
    void testEpicStatusEmpty() {
        // Эпик без подзадач
        Epic updatedEpic = taskManager.getEpic(epic.getId());
        assertEquals(Status.NEW, updatedEpic.getTaskStatus());
    }

    private SubTask createSubTask(String name, Status status, long epicId, int hourOffset) {
        return new SubTask(name, "Description", status, epicId,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(hourOffset));
    }

    @Test
    void testManagerCreateSubTask() {
        System.out.println("=== TESTING MANAGER CREATE SUBTASK ===");

        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = taskManager.createEpic(epic);
        System.out.println("Created epic ID: " + createdEpic.getId());

        // Создаем подзадачу
        SubTask subTask = new SubTask("Test SubTask", "Description", Status.NEW, createdEpic.getId());

        try {
            System.out.println("Calling manager.createSubtask()...");
            SubTask result = taskManager.createSubtask(subTask);
            System.out.println("✓ SUCCESS! Created subtask with ID: " + result.getId());
            System.out.println("Result epicId: " + result.getEpicId());

            // Проверяем, что эпик знает о подзадаче
            Epic updatedEpic = taskManager.getEpic(createdEpic.getId());
            System.out.println("Epic subtask IDs: " + updatedEpic.getSubtaskIds());
            System.out.println("Epic status: " + updatedEpic.getTaskStatus());

            // Добавляем assertions для проверки
            assertNotNull(result, "Подзадача должна быть создана");
            assertEquals(createdEpic.getId(), result.getEpicId(), "EpicId должен совпадать");
            assertTrue(updatedEpic.getSubtaskIds().contains(result.getId()),
                    "Эпик должен содержать ID подзадачи");

        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            e.printStackTrace();
            fail("Тест не должен выбрасывать исключение: " + e.getMessage());
        }
    }
}