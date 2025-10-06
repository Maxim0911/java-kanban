package Handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public HistoryHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendBadRequest(exchange, "Only GET method is allowed");
            return;
        }

        try {
            List<Task> history = taskManager.getHistory();
            String response = gson.toJson(history);
            sendSuccess(exchange, response);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}