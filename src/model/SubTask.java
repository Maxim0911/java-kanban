package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private long epicId;

    public SubTask(String name, String description, Status taskStatus, long epicId) {
        super(name, description, taskStatus);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Status taskStatus, long epicId,
                   Duration duration, LocalDateTime startTime) {
        super(name, description, taskStatus, duration, startTime);
        this.epicId = epicId;
    }

    public long getEpicId() {
        return epicId;
    }

    public void setEpicId(long epicId) {
        this.epicId = epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Long.hashCode(epicId);
        return result;
    }

    @Override
    public String getType() {
        return "SUBTASK";
    }
}