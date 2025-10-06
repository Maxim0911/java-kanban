package handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import model.Epic;
import model.SubTask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            String response = gson.toJson(epics);
            sendSuccess(exchange, response);
        } else if (path.matches("/epics/\\d+")) {
            long epicId = extractIdFromPath(path);
            Epic epic = taskManager.getEpic(epicId);
            if (epic != null) {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            long epicId = extractIdFromPath(path.split("/subtasks")[0]);
            Epic epic = taskManager.getEpic(epicId);
            if (epic != null) {
                List<SubTask> subtasks = epic.getSubtaskIds().stream()
                        .map(taskManager::getSubTask)
                        .toList();
                String response = gson.toJson(subtasks);
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
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                Epic createdEpic = taskManager.createEpic(epic);
                String response = gson.toJson(createdEpic);
                sendCreated(exchange, response);
            } else {
                Epic updatedEpic = taskManager.updateEpic(epic);
                String response = gson.toJson(updatedEpic);
                sendSuccess(exchange, response);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Invalid JSON format");
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            long epicId = extractIdFromPath(path);
            SubTask result = taskManager.deleteById(epicId);
            if (result != null) {
                sendSuccess(exchange, "{\"message\": \"Epic deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "{\"message\": \"All epics deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[2]);
    }
}