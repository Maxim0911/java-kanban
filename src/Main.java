import manager.Managers;
import model.Status;
import model.Task;
import manager.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();

        //тесты:
        //1. Создание задачи ТАСК i epic
        System.out.println("создаем таск");
        Task taskForCreate = new Task("Name", "Description", Status.NEW);
        long taskId = taskManager.createTask(taskForCreate).getId();
        taskManager.createTask(taskForCreate);
        System.out.println("Получаем задачу по ID: " + taskId);
        Task retrievedTask = taskManager.getTask(taskId); // Передаем ID в метод
        System.out.println("Полученная задача: " + retrievedTask);
        System.out.println();

        System.out.println("получить таск");
        System.out.println(taskManager.getTask(taskForCreate.getId()));
        System.out.println();

        System.out.println("Проверить обновление");
        Task taskForUpdate = new Task("New name", taskForCreate.getDescription(), Status.IN_PROGRESS);
        taskManager.updateTask(taskForUpdate);
        System.out.println(taskManager.getTask(taskId));
        System.out.println();

        System.out.println("удаляем таск");
        taskManager.deleteTask(taskForUpdate.getId());
        System.out.println(taskManager.getTask(taskId));
        System.out.println();

        System.out.println("проверяем историю");
        System.out.println(taskManager.getHistory());
        System.out.println();



    }
}

//Task - основной класс. Наследники от TAsk - subTask and epik.
// эпик содержит в себе сабтаск
// в хешмап будет ключ - айди задачи, и значение - сам объект задачи