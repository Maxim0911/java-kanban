package managers;

import model.SubTask;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("resources/data.csv").toPath()) {
            @Override
            public SubTask deleteSubTask(long id) {
                return null;
            }
        };
    }

    public static InMemoryTaskManager getDefaultHistory() {
        return new InMemoryTaskManager() {
            @Override
            public SubTask deleteSubTask(long id) {
                return null;
            }

            @Override
            public SubTask updateSubTask(SubTask subTask) {
                return null;
            }
        };
    }
}
