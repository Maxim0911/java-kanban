package manager;

import handlers.HttpTaskServer;
import com.google.gson.Gson;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTimeOverlapTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        // Используем InMemoryTaskManager напрямую - все методы уже реализованы
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();

        manager.deletedAllTask();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    @Test
    void testTimeOverlapReturns406() throws Exception {
        // Создаем первую задачу
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));

        String task1Json = gson.toJson(task1);
        URI url = URI.create("http://localhost:8080/tasks");

        // Создаем первую задачу
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .header("Content-Type", "application/json") // Добавляем заголовок
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        // Пытаемся создать пересекающуюся задачу
        Task overlappingTask = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 11, 0));

        String overlappingTaskJson = gson.toJson(overlappingTask);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(overlappingTaskJson))
                .header("Content-Type", "application/json") // Добавляем заголовок
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Должен вернуть 406 Not Acceptable
        assertEquals(406, response2.statusCode(),
                "При пересечении времени должен возвращаться статус 406");

        // Проверяем тело ответа
        assertTrue(response2.body().contains("error"),
                "Тело ответа должно содержать сообщение об ошибке");
        assertTrue(response2.body().contains("Task time overlap detected") ||
                        response2.body().contains("error"),
                "Тело ответа должно указывать на пересечение времени");
    }

    // Добавляем тест для случая без пересечения времени
    @Test
    void testNoTimeOverlapReturns201() throws Exception {
        // Создаем первую задачу
        Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 10, 0));

        String task1Json = gson.toJson(task1);
        URI url = URI.create("http://localhost:8080/tasks");

        // Создаем первую задачу
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        // Создаем непересекающуюся задачу
        Task nonOverlappingTask = new Task("Task 2", "Description 2", Status.NEW,
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 12, 0));

        String nonOverlappingTaskJson = gson.toJson(nonOverlappingTask);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(nonOverlappingTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Должен вернуть 201 Created
        assertEquals(201, response2.statusCode(),
                "При отсутствии пересечения времени должен возвращаться статус 201");
    }

    // Тест для пересечения времени в подзадачах
    @Test
    void testSubTaskTimeOverlapReturns406() throws Exception {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Description");
        Epic createdEpic = manager.createEpic(epic);

        // Создаем первую подзадачу
        SubTask subTask1 = new SubTask("SubTask 1", "Description 1", Status.NEW, createdEpic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 15, 10, 0));

        String subTask1Json = gson.toJson(subTask1);
        URI url = URI.create("http://localhost:8080/subtasks");

        // Создаем первую подзадачу
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1Json))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        // Пытаемся создать пересекающуюся подзадачу
        SubTask overlappingSubTask = new SubTask("SubTask 2", "Description 2", Status.NEW, createdEpic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 15, 11, 0));

        String overlappingSubTaskJson = gson.toJson(overlappingSubTask);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(overlappingSubTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Должен вернуть 406 Not Acceptable
        assertEquals(406, response2.statusCode(),
                "При пересечении времени в подзадачах должен возвращаться статус 406");
    }
}