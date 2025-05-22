import tasks.Epic;
import tasks.Status;
import tasks.Task;

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

        taskManager.getEpic(id);
        System.out.println();
    }
}

//Task - основной класс. Наследники от TAsk - subTask and epik.
// эпик содержит в себе сабтаск
// в хешмап будет ключ - айди задачи, и значение - сам объект задачи