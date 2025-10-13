package managers;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final int maxHistorySize = 10;
    private final List<Task> historyList = new ArrayList<>();

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyList);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        historyList.removeIf(existingTask -> existingTask.getId() == task.getId());

            historyList.add(task);

            if (historyList.size() > maxHistorySize) {
                historyList.remove(0);
            }
        }

    @Override
    public void remove(long id) {
        historyList.removeIf(task -> task.getId() == id);
    }
}


