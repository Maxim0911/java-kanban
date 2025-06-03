package manager;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    //this all methods for Task
    Task getTask(long id);

    Task createTask(Task task);

    Task updateTask(Task task);

    void deletedAllTask();

    Task deleteTask(long id);


    //this methods for epicov
    Epic getEpic(long id);

    ArrayList<Epic> getAllEpics();

    Epic createEpic(Epic epic);

    Epic updateEppic(Epic epic);

    void deleteAllEpics();

    SubTask deleteById(long id);

    SubTask createSubtask(SubTask subTask);

    SubTask getSubTask(Long id);

    ArrayList<SubTask> getAllSubTask();

    SubTask updateSubtask(SubTask subTask);

    void deleteAllSubTask();

    SubTask deleteById(Long id);

    void updateEpicStatus(long id);

    List<Task> getHistory();
}
