package tasks;

import java.util.ArrayList;

public class Epic extends Task  {
    private ArrayList<SubTask> subTasks;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subTasks = new ArrayList<>();
    }


    public ArrayList<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(ArrayList<SubTask> subTasks) {
        this.subTasks = subTasks;
    }
}
