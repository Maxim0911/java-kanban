package manager;

import java.util.ArrayList;
import java.util.HashMap;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

public class TaskManager {
    private final HashMap<Long, Task> tasks = new HashMap<>();
    private final HashMap<Long, Epic> epics = new HashMap<>();
    private final HashMap<Long, SubTask> subtasks = new HashMap<>();

    private long generatorId = 1;

    //this all methods for Task
    public Task getTask(long id) {
        return tasks.get(id);
    }

    public Task createTask(Task task) {
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);
        return task;
    }

    public void deletedAllTask() {
        tasks.clear();
    }

    public Task deleteTask(long id) {
        return tasks.remove(id);
    }

    private long getNextId() {
        return generatorId++;
    }

    //this methods for epicov
    public Epic getEpic(long id) {
        return epics.get(id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Epic updateEppic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public void deleteAllEpics() {
        epics.clear();
    }

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

    public SubTask createSubtask(SubTask subTask) {
        subTask.setId(getNextId());
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subTask.getId());  // Добавляем ID в Epic
        }
        return subTask;
    }

    public SubTask getSubTask(Long id) {
        return subtasks.get(id);
    }

    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subtasks.values());
    }

    public SubTask updateSubtask(SubTask subTask) {
        subtasks.put(subTask.getId(), subTask);
        return subTask;
    }

    public void deleteAllSubTask() {
        subtasks.clear();
    }

    public SubTask deleteById(Long id) {
        return subtasks.remove(id);
    }

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

}


