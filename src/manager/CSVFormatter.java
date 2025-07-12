package manager;

import model.Epic;
import model.Status;
import model.Task;
import model.SubTask;

public class CSVFormatter {

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static String toString(Task task) {
        return task.getId() + "," +
                task.getName() + "," +
                task.getDescription() + "," +
                task.getTaskStatus();
    }

    public static String toString(HistoryManager historyManager) {
        StringBuilder sb = new StringBuilder();
        for (Task task : historyManager.getHistory()) {
            sb.append(CSVFormatter.toString(task)).append("\n");
        }
        return sb.toString();
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        String type = fields[1];
        String name = fields[2];
        String status = fields[3];
        String description = fields[4];
        int epicId = type.equals("SUBTASK") ? Integer.parseInt(fields[5]) : -1;

        Task task;
        switch (type) {
            case "TASK":
                task = new Task(name, description,Status.NEW);
                break;
            case "EPIC":
                task = new Epic(name, description, Status.NEW);
                break;
            case "SUBTASK":
                task = new SubTask(name, description, Status.NEW, epicId);
                task.setId(id);
                break;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }

        task.setStatus(Status.valueOf(status));
        return task;
    }
}
// для пуша