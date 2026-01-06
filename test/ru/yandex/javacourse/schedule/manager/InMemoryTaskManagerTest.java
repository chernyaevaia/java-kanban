package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    TaskManager manager;

    @BeforeEach
    public void initManager() {
        manager = Managers.getDefault();
    }

    @Test
    public void testAddTask() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, Duration.ofMinutes(5), null);
        int taskId = manager.addNewTask(task);

        final Task savedTask = manager.getTask(taskId);

        assertNotNull(savedTask, "Task should be findable by its ID.");
        assertEquals(task, savedTask, "Saved task should be the same as the original.");
    }

    @Test
    public void testTaskFieldsRemainUnchangedAfterAdding() {
        Task task = new Task(1, "Test 1", "Desc 1", TaskStatus.NEW, Duration.ofMinutes(5), null);
        manager.addNewTask(task);

        final Task savedTask = manager.getTask(1);
        assertEquals("Test 1", savedTask.getName(), "Name should not have changed.");
        assertEquals("Desc 1", savedTask.getDescription(), "Description should not have changed.");
    }

    @Test
    public void testIdConflictHandlingWhenAdding() {
        Task predefinedTask = new Task(1, "Predefined", "Desc", TaskStatus.NEW, Duration.ofMinutes(5), null);
        Task generatedTask = new Task("Generated", "Desc", TaskStatus.NEW, Duration.ofMinutes(5), null);

        manager.addNewTask(predefinedTask);
        manager.addNewTask(generatedTask);

        assertNotEquals(predefinedTask.getId(), generatedTask.getId(), "Manager should not generate a conflicting ID.");
        assertEquals(2, manager.getTasks().size(), "Both tasks should exist in the manager.");
    }

    @Test
    public void testCannotAddEpicAsItsOwnSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        assertThrows(IllegalArgumentException.class, () -> {
            new Subtask(epicId, "Subtask", "Desc", TaskStatus.NEW, epicId, Duration.ZERO, null);
        }, "Should throw exception when Subtask ID equals Epic ID");
    }

    @Test
    public void testDeletingSubtaskRemovesItFromParentEpic() {
        Epic epic = new Epic(0, "Epic", "Epic desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Sub desc", TaskStatus.NEW, epicId, Duration.ofMinutes(5), null);
        int subtaskId = manager.addNewSubtask(subtask);

        assertEquals(1, manager.getEpic(epicId).getSubtaskIds().size(), "Epic should have 1 subtask before deletion.");

        manager.deleteSubtask(subtaskId);

        assertTrue(manager.getEpic(epicId).getSubtaskIds().isEmpty(),
                "Epic's subtask list should be empty after deletion.");
    }

    @Test
    public void testDeletingEpicRemovesAllItsSubtasks() {
        Epic epic = new Epic(0, "Epic", "Epic desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Sub desc 1", TaskStatus.NEW, epicId, Duration.ofMinutes(5), null);
        Subtask subtask2 = new Subtask("Subtask 2", "Sub desc 2", TaskStatus.NEW, epicId, Duration.ofMinutes(5), null);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getSubtasks().size(), "There should be 2 subtasks before epic deletion.");

        manager.deleteEpic(epicId);

        assertTrue(manager.getSubtasks().isEmpty(), "All subtasks should be deleted when their epic is deleted.");
    }
}