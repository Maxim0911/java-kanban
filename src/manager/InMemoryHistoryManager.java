package manager;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    Node first;
    Node last;
    private Map<Long, Node> nodes = new HashMap<>();


    private final List<Task> historyList = new ArrayList<>();

    @Override
    public List<Task> getHistory() {     //получить список историй
        return new ArrayList<>(historyList);
    }

    @Override
    public void remove(Task task) {
        removeNode(task.getId());
        historyList.remove(task); // Явное удаление задачи из historyList
    }

    @Override
    public void add(Task task) {
        if (!contains(task)) {
            linkLast(task);
            nodes.put(task.getId(), last);
            historyList.add(task);
        }
    }

    private boolean contains(Task task) {
        return nodes.containsKey(task.getId());
    }


    private void removeNode(Long taskId) {
        Node node = nodes.get(taskId);
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            first = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            last = node.prev;
        }
        nodes.remove(taskId);
    }


    private void linkLast(Task task) {
        Node node = new Node(task, last, null);
        if (first == null) {
            first = node;
        } else {
            last.next = node;
        }
        last = node;
    }


        private static class Node {
        private Task value;
        private Node prev;
        private Node next;

            public Node(Task value, Node prev, Node next) {
                this.value = value;
                this.prev = prev;
                this.next = next;
            }


            public Task getValue() {
                return value;
            }

            public void setValue(Task value) {
                this.value = value;
            }

            public Node getPrev() {
                return prev;
            }

            public void setPrev(Node prev) {
                this.prev = prev;
            }

            public Node getNext() {
                return next;
            }

            public void setNext(Node next) {
                this.next = next;
            }
        }
    }
//dlya pusha
