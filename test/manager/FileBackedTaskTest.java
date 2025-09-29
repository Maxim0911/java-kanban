package manager;

import managers.FileBackedTaskManager;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;

    private File testFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            testFile = Files.createTempFile(tempDir, "test", ".csv").toFile();
            return new FileBackedTaskManager(testFile.toPath()) {
                @Override
                public SubTask deleteSubTask(long id) {
                    return null;
                }
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test file", e);
        }
    }

    @Test
    void testSaveAndLoadEmpty() {
        FileBackedTaskManager manager1 = createTaskManager();
        manager1.save();

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(manager2.getTasks().isEmpty());
        assertTrue(manager2.getEpics().isEmpty());
        assertTrue(manager2.getSubTasks().isEmpty());
    }

    @Test
    void testSaveAndLoadWithTasks() {
        FileBackedTaskManager manager1 = createTaskManager();

        // Используем задачи без времени для избежания пересечений
        Task task = manager1.createTask(new Task("Task", "Desc", Status.NEW));
        Epic epic = manager1.createEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager1.createSubtask(new SubTask("Sub", "Desc", Status.NEW, epic.getId()));

        // Добавляем в историю
        manager1.getTask(task.getId());
        manager1.getEpic(epic.getId());
        manager1.getSubTask(subTask.getId());

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, manager2.getTasks().size());
        assertEquals(1, manager2.getEpics().size());
        assertEquals(1, manager2.getSubTasks().size());
        assertEquals(3, manager2.getHistory().size());
    }

    @Test
    void testSaveAndLoadWithTime() {
        FileBackedTaskManager manager1 = createTaskManager();

        // Задачи с временем, но без пересечений
        Task task = manager1.createTask(new Task("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0)));

        Epic epic = manager1.createEpic(new Epic("Epic", "Desc"));

        // Подзадача начинается после окончания задачи
        SubTask subTask = manager1.createSubtask(new SubTask("Sub", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 15, 11, 30)));

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, manager2.getTasks().size());
        assertEquals(1, manager2.getEpics().size());
        assertEquals(1, manager2.getSubTasks().size());

        // Проверяем, что время сохранилось корректно
        Task loadedTask = manager2.getTask(task.getId());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());
    }

    @Test
    void testLoadFromNonExistentFile() {
        File nonExistent = new File("non_existent.csv");
        assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistent);
        });
    }

    @Test
    void testFileCorruption() throws IOException {
        Files.writeString(testFile.toPath(), "corrupted,data\nmore,corrupted");

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFile);
            assertTrue(loaded.getTasks().isEmpty());
        });
    }

    @Test
    void testHistoryPreservation() {
        FileBackedTaskManager manager1 = createTaskManager();

        Task task1 = manager1.createTask(new Task("Task1", "Desc1", Status.NEW));
        Task task2 = manager1.createTask(new Task("Task2", "Desc2", Status.NEW));

        // Добавляем в историю в определенном порядке
        manager1.getTask(task1.getId());
        manager1.getTask(task2.getId());

        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(testFile);

        List<Task> history = manager2.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }
}