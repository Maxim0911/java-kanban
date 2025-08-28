package manager;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("resources/data.csv").toPath());
    }

    public static InMemoryTaskManager getDefaultHistory() {
        return new InMemoryTaskManager();
    }
}
