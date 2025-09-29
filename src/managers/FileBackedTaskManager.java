package managers;

import exception.ManagerSaveException;
import model.Epic;
import model.SubTask;
import model.Task;
import model.TypeTask;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FileBackedTaskManager extends InMemoryTaskManager {
    protected final Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTasksManager = new FileBackedTaskManager(file.toPath()) {
            @Override
            public SubTask deleteSubTask(long id) {
                return null;
            }
        };
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine(); // Пропускаем заголовок
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                Task task = CSVFormatter.fromString(line);
                if (task == null) continue;

                TypeTask type = TypeTask.valueOf(task.getType());

                switch (type) {
                    case TASK:
                        fileBackedTasksManager.getTasks().put(task.getId(), task);
                        break;
                    case EPIC:
                        Epic epic = (Epic) task;
                        fileBackedTasksManager.getEpics().put(epic.getId(), epic);
                        break;
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        fileBackedTasksManager.getSubTasks().put(subTask.getId(), subTask);
                        Epic parentEpic = fileBackedTasksManager.getEpics().get(subTask.getEpicId());
                        if (parentEpic != null) {
                            parentEpic.addSubtaskId(subTask.getId());
                            fileBackedTasksManager.updateEppic(parentEpic);
                        }
                        break;
                    default:
                        throw new IllegalStateException("Неизвестный тип задачи: " + type);
                }
            }

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
    public Epic getEpic(long id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public SubTask getSubTask(Long id) {
        SubTask subTask = super.getSubTask(id);
        save();
        return subTask;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public SubTask createSubtask(SubTask subTask) {
        SubTask createdSubTask = super.createSubtask(subTask);
        save();
        return createdSubTask;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEppic(Epic epic) {
        Epic updatedEpic = super.updateEppic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public SubTask updateSubTask(SubTask subTask) {
        SubTask updatedSubTask = super.updateSubtask(subTask);
        save();
        return updatedSubTask;
    }

    @Override
    public void deletedAllTask() {
        super.deletedAllTask();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTask() {
        super.deleteAllSubTask();
        save();
    }

    @Override
    public Task deleteTask(long id) {
        Task task = super.deleteTask(id);
        save();
        return task;
    }

    @Override
    public SubTask deleteById(long id) {
        SubTask subTask = super.deleteById(id);
        save();
        return subTask;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(CSVFormatter.getHeader());
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            writer.newLine();
            writer.write(CSVFormatter.historyToString(getHistoryManager()));

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

