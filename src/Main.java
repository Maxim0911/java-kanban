import model.Epic;
import model.Status;
import model.Task;
import manager.TaskManager;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();


        //тесты:
        //1. Создание задачи ТАСК i epic
        Task task = new Task("Name", "Description", Status.NEW);
        taskManager.createTask(task);
        System.out.println(task);

        Epic epic = new Epic("Nazvanie zadachi", "opisanie zadachi");


    }
}

//Task - основной класс. Наследники от TAsk - subTask and epik.
// эпик содержит в себе сабтаск
// в хешмап будет ключ - айди задачи, и значение - сам объект задачи