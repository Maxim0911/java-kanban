package manager;

import model.Epic;
import model.SubTask;
import model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager {
   private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        //Наполнение
        return taskManager;
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


    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(CSVFormatter.getHeader());
            writer.newLine();

            for (Task task : getTasks().values()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            for (SubTask subTask : getAllSubTask()) {
                writer.write(CSVFormatter.toString(subTask));
                writer.newLine();
            }

            for (Epic epic : getAllEpics()) {
                writer.write(CSVFormatter.toString(epic));
                writer.newLine();
            }
            writer.write(CSVFormatter.toString(historyManager));
            writer.newLine();

        } catch (IOException ex) {
            // свое исключение
        }
    }

}
