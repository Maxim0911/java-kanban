package handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import model.Task;
import exception.TimeOverlapException;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendBadRequest(exchange, "Method not allowed");
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getTasks().values().stream().toList();
            String response = gson.toJson(tasks);
            sendSuccess(exchange, response);
        } else if (path.matches("/tasks/\\d+")) {
            long taskId = extractIdFromPath(path);
            Task task = taskManager.getTask(taskId);
            if (task != null) {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                Task createdTask = taskManager.createTask(task);
                String response = gson.toJson(createdTask);
                sendCreated(exchange, response);
            } else {
                Task updatedTask = taskManager.updateTask(task);
                String response = gson.toJson(updatedTask);
                sendSuccess(exchange, response);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (TimeOverlapException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            long taskId = extractIdFromPath(path);
            Task task = taskManager.deleteTask(taskId);
            if (task != null) {
                sendSuccess(exchange, "{\"message\": \"Task deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/tasks")) {
            taskManager.deletedAllTask();
            sendSuccess(exchange, "{\"message\": \"All tasks deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[2]);
    }
}