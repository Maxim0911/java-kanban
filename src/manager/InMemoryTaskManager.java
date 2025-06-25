package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Long, Task> tasks = new HashMap<>();
    private final HashMap<Long, Epic> epics = new HashMap<>();
    private final HashMap<Long, SubTask> subtasks = new HashMap<>();

    private long generatorId = 1;
private HistoryManager historyManager = Managers.getDefaultHistory();


    //this all methods for Task
    @Override
    public Task getTask(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            return task;
        }
        historyManager.add(task);
        addHistory(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    private long getNextId() {
        return generatorId++;
    }

    @Override
    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void deletedAllTask() {

        tasks.clear();
    }

    @Override
    public Task deleteTask(long id) {
        Task task = tasks.remove(id);

        historyManager.remove(task);

        return task;
    }

    //this methods for epicov
    @Override
    public Epic getEpic(long id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEppic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void deleteAllEpics() {

        epics.clear();
    }

    @Override
    public SubTask deleteById(long id) {
        SubTask subTask = subtasks.remove(id);
        if (subTask != null) {
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);  // Удаляем ID из Epic
            }
        }
        return subTask;
    }


    //methods dlya subtasks

    @Override
    public SubTask createSubtask(SubTask subTask) {
        subTask.setId(getNextId());
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subTask.getId());  // Добавляем ID в Epic
        }
        return subTask;
    }

    @Override
    public SubTask getSubTask(Long id) {
        SubTask subTask = subtasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public SubTask updateSubtask(SubTask subTask) {
        subtasks.put(subTask.getId(), subTask);
        return subTask;
    }

    @Override
    public void deleteAllSubTask() {
        subtasks.clear();
    }

    @Override
    public SubTask deleteById(Long id) {
        return subtasks.remove(id);
    }

    @Override
    public void updateEpicStatus(long id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return;
        }
        boolean allNew = true;
        boolean allDone = true;

        for (SubTask subtask : subtasks.values()) {
            if (subtask.getEpicId() == id) {
                Status status = subtask.getTaskStatus();
                if (status != Status.NEW) {
                    allNew = false;
                }
                if (status != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (epic.getSubtaskIds().isEmpty()) {
            epic.setTaskStatus(Status.NEW);
        } else if (allNew) {
            epic.setTaskStatus(Status.NEW);
        } else if (allDone) {
            epic.setTaskStatus(Status.DONE);
        } else {
            epic.setTaskStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() { //полуить список истории
        return historyManager.getHistory();
    }

    public void addHistory(Task task) {
       historyManager.add(task);
    }

}


