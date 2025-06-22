package manager;

import model.Task;

import java.util.List;

public interface HistoryManager {

List<Task> getHistory();

void remove(Task task);

void add(Task task);
}

