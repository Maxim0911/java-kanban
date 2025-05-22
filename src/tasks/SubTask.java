package tasks;

public class SubTask extends Task {
    private long epicId;

    public SubTask(String name, String description, Status taskStatus, long epicId) {
        super(name, description, taskStatus);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
    }
}
