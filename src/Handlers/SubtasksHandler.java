package Handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import model.SubTask;
import exception.TimeOverlapException;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            logger.info("Received " + method + " request for " + path);

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
            logger.severe("Error in SubtasksHandler: " + e.getMessage());
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        logger.info("Handling GET request for path: " + path);

        if (path.equals("/subtasks")) {
            List<SubTask> subtasks = taskManager.getAllSubTasks();
            String response = gson.toJson(subtasks);
            logger.info("Returning " + subtasks.size() + " subtasks");
            sendSuccess(exchange, response);
        } else if (path.matches("/subtasks/\\d+")) {
            long subtaskId = extractIdFromPath(path);
            SubTask subtask = taskManager.getSubTask(subtaskId);
            if (subtask != null) {
                String response = gson.toJson(subtask);
                sendSuccess(exchange, response);
            } else {
                logger.warning("SubTask not found with ID: " + subtaskId);
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = "";
        try {
            body = readRequestBody(exchange);
            logger.info("Received POST body: " + body);
            logger.info("Attempting to parse SubTask from JSON...");

            SubTask subtask = gson.fromJson(body, SubTask.class);
            logger.info("Successfully parsed SubTask");
            logger.info("SubTask ID: " + subtask.getId());
            logger.info("SubTask Name: " + subtask.getName());
            logger.info("SubTask EpicId: " + subtask.getEpicId());
            logger.info("SubTask Status: " + subtask.getTaskStatus());

            if (subtask.getId() == 0) {
                logger.info("Creating new SubTask...");
                SubTask createdSubtask = taskManager.createSubtask(subtask);
                logger.info("Successfully created SubTask with ID: " + createdSubtask.getId());
                String response = gson.toJson(createdSubtask);
                sendCreated(exchange, response);
            } else {
                logger.info("Updating existing SubTask with ID: " + subtask.getId());
                SubTask updatedSubtask = taskManager.updateSubtask(subtask);
                String response = gson.toJson(updatedSubtask);
                sendSuccess(exchange, response);
            }
        } catch (JsonSyntaxException e) {
            logger.severe("JSON syntax error: " + e.getMessage());
            logger.severe("Problematic JSON: " + body);
            sendBadRequest(exchange, "Invalid JSON format");
        } catch (TimeOverlapException e) {
            logger.warning("Time overlap detected: " + e.getMessage());
            sendHasInteractions(exchange);
        } catch (Exception e) {
            logger.severe("Unexpected error in POST /subtasks: " + e.getMessage());
            logger.severe("Request body was: " + body);
            e.printStackTrace();
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            long subtaskId = extractIdFromPath(path);
            SubTask subtask = taskManager.deleteById(subtaskId);
            if (subtask != null) {
                sendSuccess(exchange, "{\"message\": \"Subtask deleted successfully\"}");
            } else {
                sendNotFound(exchange);
            }
        } else if (path.equals("/subtasks")) {
            taskManager.deleteAllSubTask();
            sendSuccess(exchange, "{\"message\": \"All subtasks deleted\"}");
        } else {
            sendNotFound(exchange);
        }
    }

    private long extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Long.parseLong(parts[2]);
    }
}