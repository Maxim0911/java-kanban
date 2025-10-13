package managers;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {

    // Methods for Task
    Task getTask(long id);

    HashMap<Long, Task> getTasks();

    Task createTask(Task task);

    Task updateTask(Task task);

    void deleteAllTasks();

    void deletedAllTask();

    Task deleteTask(long id);

    List<Task> getPrioritizedTasks();


    // Methods for Epics
    Epic getEpic(long id);

    HashMap<Long, Epic> getEpics();

    Epic deleteEpic(long id);

    ArrayList<Epic> getAllEpics();

    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    void deleteAllEpics();


    // Methods for Subtasks
    SubTask deleteSubTask(long id);

    SubTask deleteById(long id);

    SubTask createSubtask(SubTask subTask);

    HashMap<Long, SubTask> getSubTasks();

    SubTask getSubTask(Long id);

    ArrayList<SubTask> getAllSubTasks();

    SubTask updateSubtask(SubTask subTask);

    void deleteAllSubTask();

    void updateEpicStatus(long id);

    List<Task> getHistory();
}