package manager;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class inMemoryHistoryManager implements HistoryManager {
    private final int maxHistorySize = 10;
    private final List<Task> historyList = new ArrayList<>();


    //перенести сюда список историй из таск менеджер


    @Override
    public List<Task> getHistory() {     //получить список историй
        return new ArrayList<>(historyList);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;   //добавить таск в список историй
        }
            historyList.remove(task);

            historyList.add(task);

            if (historyList.size() > maxHistorySize) {
                historyList.remove(0);
            }
        }
    }

