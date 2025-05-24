package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Long> subtaskIds;  // Храним только ID подзадач

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtaskIds = new ArrayList<>();
    }

    public ArrayList<Long> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(Long subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(long subtaskId) {
        subtaskIds.remove(subtaskId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return subtaskIds.equals(epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + subtaskIds.hashCode();
        return result;
    }
}