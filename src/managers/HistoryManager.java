package managers;

import model.Task;

import java.util.List;

public interface HistoryManager {

List<Task> getHistory();

void remove(long id);

void add(Task task);
}
