package managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import exception.TimeOverlapException;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

public abstract class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Long, Task> tasks = new HashMap<>();
    protected final HashMap<Long, Epic> epics = new HashMap<>();
    protected final HashMap<Long, SubTask> subtasks = new HashMap<>();

    protected long generatorId = 1;
    protected InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).thenComparing(Task::getId)
    );

    public abstract SubTask deleteSubTask(long id);

    private List<Task> getAllTasks() {
        return Stream.of(
                        tasks.values().stream(),
                        epics.values().stream(),
                        subtasks.values().stream()
                )
                .flatMap(stream -> stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getDuration() == null ||
                task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();
        return !(end1.isBefore(start2) || end2.isBefore(start1));
    }

    private boolean isTaskOverlapping(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        return getAllTasks().stream()
                .filter(task -> task.getId() != newTask.getId())
                .filter(task -> task.getStartTime() != null && task.getDuration() != null)
                .anyMatch(existingTask -> isTasksOverlap(newTask, existingTask));
    }

    private void calculateEpicTime(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStartTime = null;
        LocalDateTime latestEndTime = null;
        boolean hasTimeData = false;

        for (Long subtaskId : epic.getSubtaskIds()) {
            SubTask subtask = subtasks.get(subtaskId);
            if (subtask != null && subtask.getStartTime() != null && subtask.getDuration() != null) {
                hasTimeData = true;
                totalDuration = totalDuration.plus(subtask.getDuration());

                if (earliestStartTime == null || subtask.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = subtask.getStartTime();
                }

                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (subtaskEndTime != null && (latestEndTime == null || subtaskEndTime.isAfter(latestEndTime))) {
                    latestEndTime = subtaskEndTime;
                }
            }
        }

        if (hasTimeData) {
            epic.setDuration(totalDuration);
            epic.setStartTime(earliestStartTime);
            epic.setEndTime(latestEndTime);
        } else {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            epic.setEndTime(null);
        }
    }

    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setTaskStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Long subtaskId : epic.getSubtaskIds()) {
            SubTask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getTaskStatus() != Status.NEW) {
                    allNew = false;
                }
                if (subtask.getTaskStatus() != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (allDone) {
            epic.setTaskStatus(Status.DONE);
        } else if (allNew) {
            epic.setTaskStatus(Status.NEW);
        } else {
            epic.setTaskStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpic(Epic epic) {
        calculateEpicTime(epic);
        updateEpicStatus(epic);

        removeFromPrioritizedTasks(epic);
        addToPrioritizedTasks(epic);
    }

    public HashMap<Long, Task> getTasks() {
        return tasks;
    }

    public HashMap<Long, Epic> getEpics() {
        return epics;
    }

    public HashMap<Long, SubTask> getSubTasks() {
        return subtasks;
    }

    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
    }

    void updateEpicTime(Epic epic) {
        removeFromPrioritizedTasks(epic);
        updateEpic(epic);
        addToPrioritizedTasks(epic);
    }


    //this all methods for Task
    @Override
    public Task getTask(long id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public Task createTask(Task task) {
        if (isTaskOverlapping(task)) {
            throw new TimeOverlapException("Задача пересекается по времени с существующей задачей");
        }

        task.setId(getNextId());
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
        return task;
    }

    private long getNextId() {
        return generatorId++;
    }

    @Override
    public Task updateTask(Task task) {
        if (isTaskOverlapping(task)) {
            throw new TimeOverlapException("Обновленная задача пересекается по времени с другой задачей");
        }

        Task oldTask = tasks.get(task.getId());
        if (oldTask != null) {
            removeFromPrioritizedTasks(oldTask);
        }
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
        return task;
    }

    public abstract SubTask updateSubTask(SubTask subTask);

    @Override
    public void deletedAllTask() {
        for (Task task : tasks.values()) {
            removeFromPrioritizedTasks(task);
        }
        tasks.clear();
    }


    @Override
    public Task deleteTask(long id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritizedTasks(task);
        }
        return task;
    }

    //this methods for epicov
    @Override
    public Epic getEpic(long id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Epic updateEppic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            removeFromPrioritizedTasks(oldEpic);
        }
        epics.put(epic.getId(), epic);
        addToPrioritizedTasks(epic);
        return epic;
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            removeFromPrioritizedTasks(epic);
            for (Long subtaskId : epic.getSubtaskIds()) {
                SubTask subTask = subtasks.get(subtaskId);
                if (subTask != null) {
                    removeFromPrioritizedTasks(subTask);
                }
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public SubTask deleteById(long id) {
        SubTask subTask = subtasks.remove(id);
        if (subTask != null) {
            removeFromPrioritizedTasks(subTask);
            Epic epic = epics.get(subTask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpic(epic);
            }
        }
        return subTask;
    }


    //methods dlya subtasks

    @Override
    public SubTask createSubtask(SubTask subTask) {
        if (isTaskOverlapping(subTask)) {
            throw new TimeOverlapException("Подзадача пересекается по времени с существующей задачей");
        }

        subTask.setId(getNextId());
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subTask.getId());
            updateEpic(epic);
        }
        addToPrioritizedTasks(subTask);
        return subTask;
    }

    @Override
    public SubTask getSubTask(Long id) {
        SubTask subTask = subtasks.get(id);
        if (subTask != null) {
            historyManager.add(subTask);
        }
        return subTask;
    }

    @Override
    public ArrayList<SubTask> getAllSubTask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public SubTask updateSubtask(SubTask subTask) {
        if (isTaskOverlapping(subTask)) {
            throw new TimeOverlapException("Обновленная подзадача пересекается по времени с другой задачей");
        }

        SubTask oldSubTask = subtasks.get(subTask.getId());
        if (oldSubTask != null) {
            removeFromPrioritizedTasks(oldSubTask);
        }
        subtasks.put(subTask.getId(), subTask);
        addToPrioritizedTasks(subTask);

        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            updateEpic(epic);
        }
        return subTask;
    }

    @Override
    public void deleteAllSubTask() {
        for (SubTask subTask : subtasks.values()) {
            removeFromPrioritizedTasks(subTask);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtaskIds();
            updateEpic(epic);
        }
    }


    @Override
    public void updateEpicStatus(long id) {
        Epic epic = epics.get(id);
        if (epic == null) return;

        List<Long> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds.isEmpty()) {
            epic.setTaskStatus(Status.NEW);
            return;
        }

        // ИСПОЛЬЗОВАНИЕ STREAM API вместо циклов:
        List<Status> subtaskStatuses = subtaskIds.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(SubTask::getTaskStatus)
                .collect(Collectors.toList());

        boolean allNew = subtaskStatuses.stream().allMatch(status -> status == Status.NEW);
        boolean allDone = subtaskStatuses.stream().allMatch(status -> status == Status.DONE);

        if (allDone) {
            epic.setTaskStatus(Status.DONE);
        } else if (allNew) {
            epic.setTaskStatus(Status.NEW);
        } else {
            epic.setTaskStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() { //полуить список истории
        return historyManager.getHistory();
    }

    public void addHistory(Task task) {
        historyManager.add(task); //добавить таск в список с историе
    }

}

