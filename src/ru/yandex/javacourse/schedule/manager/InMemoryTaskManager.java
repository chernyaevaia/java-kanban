package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	private final Map<Integer, Task> tasks = new HashMap<>();
	private final Map<Integer, Epic> epics = new HashMap<>();
	private final Map<Integer, Subtask> subtasks = new HashMap<>();
	protected int generatorId = 0;
	private final HistoryManager historyManager = Managers.getDefaultHistory();

	private final TreeSet<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
		if (!task1.getStartTime().equals(task2.getStartTime())) {
			return task1.getStartTime().compareTo(task2.getStartTime());
		}
		return Integer.compare(task1.getId(), task2.getId());
	});

	public boolean isOverlapping(Task newTask) {
		if (newTask.getStartTime() == null) {
			return false;
		}
		return prioritizedTasks.stream()
				.filter(oldTask -> oldTask.getId() != newTask.getId())
				.anyMatch(oldTask -> {
					LocalDateTime oldTaskStart = oldTask.getStartTime();
					LocalDateTime oldTaskEnd = oldTask.getEndTime();
					LocalDateTime newTaskStart = newTask.getStartTime();
					LocalDateTime newTaskEnd = newTask.getEndTime();
					return oldTaskStart.isBefore(newTaskEnd) && oldTaskEnd.isAfter(newTaskStart);
				});
	}

	private void addToPrioritized(Task task) {
		if (task.getStartTime() != null) {
			prioritizedTasks.add(task);
		}
	}

	private void removeFromPrioritized(Task task) {
		if (task != null) {
			prioritizedTasks.remove(task);
		}
	}

	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			throw new IllegalArgumentException("Subtask cannot be its own epic. ID: " + epicId);
		}
		for (int id : epic.getSubtaskIds()) {
			tasks.add(subtasks.get(id));
		}
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.addTask(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.addTask(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.addTask(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
		final int id;
		if (task.getId() == 0) {
			id = ++generatorId;
			task.setId(id);
		} else {
			id = task.getId();
			if (id > generatorId) {
				generatorId = id;
			}
		}
		tasks.put(id, task);
		addToPrioritized(task);
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		final int id;
		if (epic.getId() == 0) {
			id = ++generatorId;
			epic.setId(id);
		} else {
			id = epic.getId();
			if (id > generatorId) {
				generatorId = id;
			}
		}
		epics.put(id, epic);
		return id;
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		if (isOverlapping(subtask)) {
			throw new IllegalArgumentException("Subtask overlaps with an existing task.");
		}
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}

		if (epicId == subtask.getId() && subtask.getId() != 0) {
			return null;
		}

		final int id;
		if (subtask.getId() == 0) {
			id = ++generatorId;
			subtask.setId(id);
		} else {
			id = subtask.getId();
			if (id > generatorId) {
				generatorId = id;
			}
		}

		subtasks.put(id, subtask);
		epic.addSubtaskId(subtask.getId());
		updateEpicStatus(epicId);
		updateEpicTime(epic);
		addToPrioritized(subtask);
		return id;
	}

	@Override
	public void updateTask(Task task) {
		if (isOverlapping(task)) {
			throw new IllegalArgumentException("Task overlaps with an existing task.");
		}
		final int id = task.getId();
		final Task savedTask = tasks.get(id);
		if (savedTask == null) {
			return;
		}

		removeFromPrioritized(savedTask);
		tasks.put(id, task);
		addToPrioritized(task);
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		if (isOverlapping(subtask)) {
			throw new IllegalArgumentException("Subtask overlaps with an existing task.");
		}
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();
		final Subtask savedSubtask = subtasks.get(id);
		if (savedSubtask == null) {
			return;
		}
		final Epic epic = epics.get(epicId);
		if (epic == null) {
			return;
		}
		subtasks.put(id, subtask);
		removeFromPrioritized(savedSubtask);
		subtasks.put(id, subtask);
		updateEpicStatus(epicId);
		updateEpicTime(epic);
		addToPrioritized(subtask);

	}

	@Override
	public void deleteTask(int id) {
		Task task = tasks.remove(id);
		historyManager.remove(id);
		removeFromPrioritized(task);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		historyManager.remove(id);
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
			historyManager.remove(subtaskId);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		historyManager.remove(id);
		if (subtask == null) {
			return;
		}
		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
		updateEpicStatus(epic.getId());
		updateEpicTime(epic);
		removeFromPrioritized(subtask);
	}

	@Override
	public void deleteTasks() {
		for (Task task : tasks.values()) {
			removeFromPrioritized(task);
		}
		for (Integer taskId : tasks.keySet()) {
			historyManager.remove(taskId);
		}
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Subtask subtask : subtasks.values()) {
			removeFromPrioritized(subtask);
		}

		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
			updateEpicTime(epic);
		}

		for (Integer subtaskId : subtasks.keySet()) {
			historyManager.remove(subtaskId);
		}

		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		for (Integer epicId : epics.keySet()) {
			historyManager.remove(epicId);
		}
		for (Integer subtaskId : subtasks.keySet()) {
			historyManager.remove(subtaskId);
		}
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);

	}

	private void updateEpicTime(Epic epic) {
		List<Integer> subtaskIds = epic.getSubtaskIds();

		if (subtaskIds.isEmpty()) {
			epic.setDuration(Duration.ZERO);
			epic.setStartTime(null);
			epic.setEndTime(null);
			return;
		}

		Duration totalDuration = Duration.ZERO;
		LocalDateTime earliestStart = null;
		LocalDateTime latestEnd = null;

		for (int id : subtaskIds) {
			Subtask subtask = subtasks.get(id);
			if (subtask == null) {
				continue;
			}
			if (subtask.getDuration() != null) {
				totalDuration = totalDuration.plus(subtask.getDuration());
			}

			if (subtask.getStartTime() != null) {
				LocalDateTime subStart = subtask.getStartTime();
				LocalDateTime subEnd = subtask.getEndTime();

				if (earliestStart == null || subStart.isBefore(earliestStart)) {
					earliestStart = subStart;
				}

				if (latestEnd == null || subEnd.isAfter(latestEnd)) {
					latestEnd = subEnd;
				}
			}
		}

		epic.setDuration(totalDuration);
		epic.setStartTime(earliestStart);
		epic.setEndTime(latestEnd);
	}
}
