package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
	protected ArrayList<Integer> subtaskIds = new ArrayList<>();
	protected LocalDateTime endTime;

	public Epic(int id, String name, String description, Duration duration, LocalDateTime startTime) {
		super(id, name, description, NEW, Duration.ZERO, null);
	}

	public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
		super(name, description, NEW, duration, startTime);
	}

	public void addSubtaskId(int id) {
		subtaskIds.add(id);
	}

	public List<Integer> getSubtaskIds() {
		return subtaskIds;
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public LocalDateTime getEndTime() {
		return this.endTime;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", duration='" + duration + '\'' +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				'}';
	}
}
