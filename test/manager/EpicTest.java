package manager;

import model.Epic;
import model.Status;
import model.SubTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import managers.InMemoryTaskManager;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {
    private InMemoryTaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager() {
            @Override
            public SubTask deleteSubTask(long id) {
                return null;
            }

            @Override
            public SubTask updateSubTask(SubTask subTask) {
                return null;
            }
        };
        epic = taskManager.createEpic(new Epic("Test Epic", "Description"));
    }

    @Test
    void testEpicStatusAllNew() {
        // a. Все подзадачи со статусом NEW
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId());
        SubTask sub2 = createSubTask("Sub2", Status.NEW, epic.getId());

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        assertEquals(Status.NEW, epic.getTaskStatus());
    }

    @Test
    void testEpicStatusAllDone() {
        // b. Все подзадачи со статусом DONE
        SubTask sub1 = createSubTask("Sub1", Status.DONE, epic.getId());
        SubTask sub2 = createSubTask("Sub2", Status.DONE, epic.getId());

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        assertEquals(Status.DONE, epic.getTaskStatus());
    }

    @Test
    void testEpicStatusNewAndDone() {
        // c. Подзадачи со статусами NEW и DONE
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId());
        SubTask sub2 = createSubTask("Sub2", Status.DONE, epic.getId());

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getTaskStatus());
    }

    @Test
    void testEpicStatusInProgress() {
        // d. Подзадачи со статусом IN_PROGRESS
        SubTask sub1 = createSubTask("Sub1", Status.IN_PROGRESS, epic.getId());
        SubTask sub2 = createSubTask("Sub2", Status.IN_PROGRESS, epic.getId());

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);

        assertEquals(Status.IN_PROGRESS, epic.getTaskStatus());
    }

    @Test
    void testEpicStatusMixed() {
        SubTask sub1 = createSubTask("Sub1", Status.NEW, epic.getId());
        SubTask sub2 = createSubTask("Sub2", Status.IN_PROGRESS, epic.getId());
        SubTask sub3 = createSubTask("Sub3", Status.DONE, epic.getId());

        taskManager.createSubtask(sub1);
        taskManager.createSubtask(sub2);
        taskManager.createSubtask(sub3);

        assertEquals(Status.IN_PROGRESS, epic.getTaskStatus());
    }

    @Test
    void testEpicStatusEmpty() {
        // Эпик без подзадач
        assertEquals(Status.NEW, epic.getTaskStatus());
    }

    private SubTask createSubTask(String name, Status status, long epicId) {
        return new SubTask(name, "Description", status, epicId,
                Duration.ofMinutes(30), LocalDateTime.now());
    }
}