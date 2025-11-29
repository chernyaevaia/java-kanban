package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration,
			LocalDateTime startTime) {
		super(id, name, description, status, duration, startTime);
		if (id != 0 && id == epicId) {
			throw new IllegalArgumentException("Subtask cannot be its own epic.");
		}
		this.epicId = epicId;
	}

	public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration,
			LocalDateTime startTime) {
		super(name, description, status, duration, startTime);
		this.epicId = epicId;
	}

	public int getEpicId() {
		return epicId;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", epicId=" + epicId +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", duration='" + duration + '\'' +
				", startTime='" + startTime + '\'' +
				'}';
	}
}
