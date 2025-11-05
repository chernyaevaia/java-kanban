package ru.yandex.javacourse.schedule.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private Node head;
	private Node tail;
	private final HashMap<Integer, Node> nodeMap = new HashMap<>();

	static class Node {
		Task item;
		Node next;
		Node prev;

		Node(Node prev, Task element, Node next) {
			this.item = element;
			this.next = next;
			this.prev = prev;
		}
	};

	private void linkLast(Task task) {
		final Node last = tail;
		final Node newNode = new Node(last, task, null);
		tail = newNode;
		if (last == null) {
			head = newNode;
		} else {
			last.next = newNode;
		}
		nodeMap.put(task.getId(), newNode); 
	}

	private void removeNode(Node node) {
		final Node prev = node.prev;
		final Node next = node.next;
		if (prev == null) {
			head = next;
		} else {
			prev.next = next;
			node.prev = null;
		}
		if (next == null) {
			tail = prev;
		} else {
			next.prev = prev;
			node.next = null;
		}
	}

	@Override
	public List<Task> getHistory() {
		List<Task> history = new ArrayList<>();
		Node currentNode = head;
		while (currentNode != null) {
			history.add(currentNode.item);
			currentNode = currentNode.next;
		}
		return history;
	}

	@Override
	public void addTask(Task task) {
		if (task == null) {
			return;
		}
		int taskId = task.getId();
		if (nodeMap.containsKey(taskId)) {
			remove(taskId);
		}
		linkLast(task);
	}

	@Override
	public void remove(int id) {
		if (nodeMap.containsKey(id)) {
			Node nodeToRemove = nodeMap.get(id);
			removeNode(nodeToRemove);
			nodeMap.remove(id);
		}
	}
}
