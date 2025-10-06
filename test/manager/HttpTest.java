package manager;

import Handlers.HttpTaskServer;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
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

        // Очищаем данные перед каждым тестом
        manager.deletedAllTask();
        manager.deleteAllEpics();
        manager.deleteAllSubTask();
    }

    @AfterEach
    void shutDown() {
        taskServer.stop();
    }

    private Task createTestTask() {
        return new Task("Test Task", "Test Description", Status.NEW);
    }

    private Epic createTestEpic() {
        return new Epic("Test Epic", "Test Epic Description");
    }

    private SubTask createTestSubTask(long epicId) {
        return new SubTask("Test SubTask", "Test SubTask Description", Status.NEW, epicId);
    }

    @Test
    void testGetTasks() throws Exception {
        // Создаем задачу через менеджер
        Task task = createTestTask();
        manager.createTask(task);

        // Получаем задачи через API
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Test Task", tasks[0].getName());
    }

    @Test
    void testGetTaskById() throws Exception {
        Task task = createTestTask();
        Task createdTask = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(createdTask.getId(), responseTask.getId());
        assertEquals("Test Task", responseTask.getName());
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void testCreateTask() throws Exception {
        Task task = createTestTask();
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks().values().stream().toList();
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test Task", tasksFromManager.get(0).getName());
    }

    @Test
    void testGetEpics() throws Exception {
        Epic epic = createTestEpic();
        manager.createEpic(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(1, epics.length);
        assertEquals("Test Epic", epics[0].getName());
    }

    @Test
    void testCreateEpic() throws Exception {
        Epic epic = createTestEpic();
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertEquals(1, epicsFromManager.size());
        assertEquals("Test Epic", epicsFromManager.get(0).getName());
    }

    @Test
    void testCreateSubTask() throws Exception {
        Epic epic = createTestEpic();
        Epic createdEpic = manager.createEpic(epic);

        SubTask subTask = createTestSubTask(createdEpic.getId());
        String subTaskJson = gson.toJson(subTask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<SubTask> subtasksFromManager = manager.getAllSubTasks();
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Test SubTask", subtasksFromManager.get(0).getName());
        assertEquals(createdEpic.getId(), subtasksFromManager.get(0).getEpicId());
    }

    @Test
    void testGetHistory() throws Exception {
        Task task = createTestTask();
        Task createdTask = manager.createTask(task);

        // Добавляем задачу в историю
        manager.getTask(createdTask.getId());

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals(createdTask.getId(), history[0].getId());
    }

    @Test
    void testGetPrioritizedTasks() throws Exception {
        Task task = createTestTask();
        manager.createTask(task);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritized);
    }

    // Добавляем дополнительные тесты для полноты покрытия

    @Test
    void testUpdateTask() throws Exception {
        // Создаем задачу
        Task task = createTestTask();
        Task createdTask = manager.createTask(task);

        // Обновляем задачу
        createdTask.setName("Updated Task");
        createdTask.setTaskStatus(Status.IN_PROGRESS);
        String updatedTaskJson = gson.toJson(createdTask);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        // Проверяем, что задача обновилась
        Task updatedTask = manager.getTask(createdTask.getId());
        assertEquals("Updated Task", updatedTask.getName());
        assertEquals(Status.IN_PROGRESS, updatedTask.getTaskStatus());
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = createTestTask();
        Task createdTask = manager.createTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        // Проверяем, что задача удалена
        assertNull(manager.getTask(createdTask.getId()));
    }

    @Test
    void testGetSubTasks() throws Exception {
        Epic epic = createTestEpic();
        Epic createdEpic = manager.createEpic(epic);
        SubTask subTask = createTestSubTask(createdEpic.getId());
        manager.createSubtask(subTask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        SubTask[] subtasks = gson.fromJson(response.body(), SubTask[].class);
        assertEquals(1, subtasks.length);
        assertEquals("Test SubTask", subtasks[0].getName());
    }
}