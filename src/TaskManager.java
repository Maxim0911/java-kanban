import java.util.ArrayList;
import java.util.HashMap;

import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

public class TaskManager {
    private HashMap<Long, Task> tasks = new HashMap<>();
    private HashMap<Long, Epic> epics = new HashMap<>();
    private HashMap<Long, SubTask> subtasks = new HashMap<>();

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

    public Epic deleteEpicById(long id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (SubTask subTask : epic.getSubTasks()) {
                subtasks.remove(subTask.getId());
            }
        }
        return epic;
    }

    //methods dlya subtasks

    public SubTask createSubtask(SubTask subTask) {
        subTask.setId(getNextId());
        subtasks.put(subTask.getId(), subTask);
        return subTask;
    }

    public SubTask getSubTask(long id) {
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

    public SubTask deleteById(long id) {
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

        if (epic.getSubTasks().isEmpty()) {
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


