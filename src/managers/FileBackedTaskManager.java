package managers;

import exception.ManagerSaveException;
import model.Epic;
import model.SubTask;
import model.Task;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected final Path path;
    private final File file;

    public FileBackedTaskManager(File file) {
        this.path = file.toPath();
        this.file = file;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        loadTasksFromFile(manager, file);
        return manager;
    }

    private static void loadTasksFromFile(FileBackedTaskManager manager, File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = bufferedReader.readLine();
            if (line == null || !line.equals(CSVFormatter.getHeader())) {
                return;
            }

            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    break;
                }

                Task task = CSVFormatter.fromString(line);
                if (task == null) continue;

                if (task.getId() >= manager.generatorId) {
                    manager.generatorId = task.getId() + 1;
                }

                if (task instanceof Epic) {
                    Epic epic = (Epic) task;
                    manager.epics.put(epic.getId(), epic);
                } else if (task instanceof SubTask) {
                    SubTask subTask = (SubTask) task;
                    manager.subtasks.put(subTask.getId(), subTask);
                    Epic parentEpic = manager.epics.get(subTask.getEpicId());
                    if (parentEpic != null) {
                        parentEpic.addSubtaskId(subTask.getId());
                        manager.updateEpicStatus(parentEpic.getId());
                        manager.calculateEpicTime(parentEpic);
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
            }

            String historyLine = bufferedReader.readLine();
            if (historyLine != null && !historyLine.isEmpty()) {
                List<Long> historyIds = CSVFormatter.historyFromString(historyLine);
                for (Long id : historyIds) {
                    Task task = manager.getAnyTask(id);
                    if (task != null) {
                        manager.historyManager.add(task);
                    }
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении из файла", e);
        }
    }

    private Task getAnyTask(long id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        } else {
            return subtasks.get(id);
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
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public SubTask updateSubtask(SubTask subTask) {
        SubTask updatedSubTask = super.updateSubtask(subTask);
        save();
        return updatedSubTask;
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
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
    public Epic deleteEpic(long id) {
        Epic epic = super.deleteEpic(id);
        save();
        return epic;
    }

    @Override
    public SubTask deleteSubTask(long id) {
        SubTask subTask = super.deleteSubTask(id);
        save();
        return subTask;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSVFormatter.getHeader());
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            writer.newLine();
            writer.write(CSVFormatter.historyToString(historyManager));

        } catch (IOException ex) {
            throw new ManagerSaveException("Ошибка при записи файла", ex);
        }
    }

    public List<Task> getAllTasks() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        return allTasks;
    }
}