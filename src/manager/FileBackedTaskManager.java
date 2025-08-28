package manager;

import exception.ManagerSaveException;
import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;
import model.TypeTask;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {
   protected final Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTasksManager = new FileBackedTaskManager(file.toPath());
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                Task task = CSVFormatter.fromString(line);
                TypeTask type = TypeTask.valueOf(task.getType());

                switch (type) {
                    case TASK:
                        fileBackedTasksManager.getTasks().put(task.getId(), task);
                        break;
                    case EPIC:
                        fileBackedTasksManager.getEpics().put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        fileBackedTasksManager.getSubTasks().put(subTask.getId(), subTask);
                        Epic epic = fileBackedTasksManager.getEpics().get(subTask.getEpicId());
                        if (epic != null) {
                            epic.addSubtaskId(subTask.getId()); // Исправлено на addSubtaskId
                        }
                        break;
                }
            }

            // Восстановление истории (если есть)
            String historyLine = bufferedReader.readLine();
            if (historyLine != null && !historyLine.isEmpty()) {
                List<Long> historyIds = CSVFormatter.historyFromString(historyLine);
                for (Long id : historyIds) {
                    Task task = fileBackedTasksManager.getAnyTask(id);
                    if (task != null) {
                        fileBackedTasksManager.historyManager.add(task);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении из файла", e);
        }

        return fileBackedTasksManager;
    }

    // Вспомогательный метод для получения задачи любого типа
    private Task getAnyTask(long id) {
        if (getTasks().containsKey(id)) {
            return getTasks().get(id);
        } else if (getEpics().containsKey(id)) {
            return getEpics().get(id);
        } else {
            return getSubTasks().get(id);
        }
    }

    @Override
    public Task getTask(long id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public void deletedAllTask() {
        super.deletedAllTask();
        save();
    }

    @Override
    public Task deleteTask(long id) {
        Task task = super.deleteTask(id);
        save();
        return task;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл
     */
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(CSVFormatter.getHeader());
            writer.newLine();

            // Сохраняем все задачи в правильном порядке
            for (Task task : getAllTasks()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            // Сохраняем историю - исправлено на правильный метод
            writer.newLine();
            writer.write(CSVFormatter.historyToString(getHistory())); // Передаем список истории

        } catch (IOException ex) {
            throw new ManagerSaveException("Ошибка при записи файла", ex);
        }
    }

    private List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(getTasks().values());
        allTasks.addAll(getEpics().values());
        allTasks.addAll(getSubTasks().values());
        return allTasks;
    }
    }

