package managers;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String getHeader() {
        return "id,type,name,status,description,epic,duration,startTime";
    }

    public static String toString(Task task) {
        String epicId = "";
        if (task instanceof SubTask) {
            epicId = String.valueOf(((SubTask) task).getEpicId());
        }

        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTimeStr = task.getStartTime() != null ?
                task.getStartTime().format(DATE_TIME_FORMATTER) : "";

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                escapeSpecialCharacters(task.getName()),
                task.getTaskStatus(),
                escapeSpecialCharacters(task.getDescription()),
                epicId,
                durationStr,
                startTimeStr
        );
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",", -1);
        if (fields.length < 8) return null;

        try {
            long id = Long.parseLong(fields[0]);
            String type = fields[1];
            String name = unescapeSpecialCharacters(fields[2]);
            Status status;
            try {
                status = Status.valueOf(fields[3]);
            } catch (IllegalArgumentException e) {
                status = Status.NEW;
            }

            String description = unescapeSpecialCharacters(fields[4]);
            String epicIdStr = fields[5];
            String durationStr = fields[6];
            String startTimeStr = fields[7];

            Duration duration = null;
            if (!durationStr.isEmpty() && !durationStr.equals("null")) {
                try {
                    duration = Duration.ofMinutes(Long.parseLong(durationStr));
                } catch (NumberFormatException e) {
                    duration = Duration.ZERO;
                }
            }

            LocalDateTime startTime = null;
            if (!startTimeStr.isEmpty() && !startTimeStr.equals("null")) {
                try {
                    startTime = LocalDateTime.parse(startTimeStr, DATE_TIME_FORMATTER);
                } catch (Exception e) {
                    startTime = null;
                }
            }

            switch (type) {
                case "TASK":
                    Task task = new Task(name, description, status);
                    task.setId(id);
                    if (duration != null) task.setDuration(duration);
                    if (startTime != null) task.setStartTime(startTime);
                    return task;

                case "EPIC":
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setTaskStatus(status);
                    if (duration != null) epic.setDuration(duration);
                    if (startTime != null) epic.setStartTime(startTime);
                    return epic;

                case "SUBTASK":
                    long epicId = epicIdStr.isEmpty() || epicIdStr.equals("null") ?
                            -1 : Long.parseLong(epicIdStr.trim());
                    SubTask subTask = new SubTask(name, description, status, epicId);
                    subTask.setId(id);
                    if (duration != null) subTask.setDuration(duration);
                    if (startTime != null) subTask.setStartTime(startTime);
                    return subTask;

                default:
                    return null;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при парсинге задачи: " + e.getMessage());
            System.err.println("Строка: " + value);
            return null;
        }
    }

    public static String historyToString(HistoryManager manager) {
        List<String> historyIds = new ArrayList<>();
        for (Task task : manager.getHistory()) {
            historyIds.add(String.valueOf(task.getId()));
        }
        return String.join(",", historyIds);
    }

    public static List<Long> historyFromString(String value) {
        List<Long> historyIds = new ArrayList<>();
        if (value == null || value.isEmpty()) return historyIds;

        String[] ids = value.split(",");
        for (String id : ids) {
            try {
                historyIds.add(Long.parseLong(id.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Некорректный ID в истории: " + id);
            }
        }
        return historyIds;
    }

    static String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        return data.replace(",", "\\,")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    static String unescapeSpecialCharacters(String data) {
        if (data == null) return "";
        return data.replace("\\,", ",")
                .replace("\\n", "\n")
                .replace("\\r", "\r");
    }
}