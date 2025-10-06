package handlers;

import Adapters.DurationAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class BaseHttpHandler {
    protected static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    protected final Logger logger = Logger.getLogger(this.getClass().getName());

    public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String value = in.nextString();
            if (value == null || value.isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(value, formatter);
        }
    }

    protected void sendResponse(HttpExchange exchange, String text, int statusCode) throws IOException {
        try {
            byte[] response = text.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            exchange.sendResponseHeaders(statusCode, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception e) {
            logger.severe("Error sending response: " + e.getMessage());
            throw e;
        }
    }

    protected void sendSuccess(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendResponse(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        String response = "{\"error\": \"Not Found\"}";
        sendResponse(exchange, response, 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        String response = "{\"error\": \"Task time overlap detected\"}";
        sendResponse(exchange, response, 406);
    }

    protected void sendInternalError(HttpExchange exchange) throws IOException {
        String response = "{\"error\": \"Internal Server Error\"}";
        sendResponse(exchange, response, 500);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        sendResponse(exchange, response, 400);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}