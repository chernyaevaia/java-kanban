package ru.yandex.javacourse.schedule.manager;  
  
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;  
import org.junit.jupiter.api.AfterEach;  
import ru.yandex.javacourse.schedule.tasks.Epic;  
import ru.yandex.javacourse.schedule.tasks.Subtask;  
import ru.yandex.javacourse.schedule.tasks.Task;  
import ru.yandex.javacourse.schedule.tasks.TaskStatus;  
  
import java.io.File;  
import java.io.IOException;  
import java.util.List;  
  
import static org.junit.jupiter.api.Assertions.*;  
  
public class FileBackedTaskManagerTest {  
  
    private FileBackedTaskManager taskManager;  
    private File tempFile;  
  
    @BeforeEach  
    public void init() throws IOException {  
        tempFile = File.createTempFile("test_tasks", ".csv");  
        taskManager = new FileBackedTaskManager(tempFile);  
    }  
  
    @AfterEach  
    public void cleanup() {  
        if (tempFile != null && tempFile.exists()) {  
            tempFile.delete();  
        }  
    }  
  
    // Test 1: saving and loading one task 
    @Test  
    public void testSaveAndLoadSingleTask() {  
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);  
        int taskId = taskManager.addNewTask(task);  
  
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);  
  
        assertEquals(1, loadedManager.getTasks().size(), "Should load 1 task");  
        Task loadedTask = loadedManager.getTask(taskId);  
        assertNotNull(loadedTask, "Task should not be null");  
        assertEquals("Test Task", loadedTask.getName(), "Task name should match");  
        assertEquals("Test Description", loadedTask.getDescription(), "Task description should match");  
        assertEquals(TaskStatus.NEW, loadedTask.getStatus(), "Task status should match");  
    }  
  
    // Test 2: saving and loading several tasks
    @Test  
    public void testSaveAndLoadMultipleTypes() {  
        Task task = new Task("Test Task", "Task Description", TaskStatus.NEW);  
        Epic epic = new Epic("Test Epic", "Epic Description");  
  
        int taskId = taskManager.addNewTask(task);  
        int epicId = taskManager.addNewEpic(epic);  
  
        Subtask subtask = new Subtask("Test Subtask", "Subtask Description", TaskStatus.DONE, epicId);  
        Integer subtaskId = taskManager.addNewSubtask(subtask);  
  
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);  
  
        assertEquals(1, loadedManager.getTasks().size(), "Should load 1 task");  
        assertEquals(1, loadedManager.getEpics().size(), "Should load 1 epic");  
        assertEquals(1, loadedManager.getSubtasks().size(), "Should load 1 subtask");  
  
        assertEquals("Test Task", loadedManager.getTask(taskId).getName());  
        assertEquals("Test Epic", loadedManager.getEpic(epicId).getName());  
        assertEquals("Test Subtask", loadedManager.getSubtask(subtaskId).getName());  
        assertEquals(epicId, loadedManager.getSubtask(subtaskId).getEpicId());  
    }  
  
    // Test 3: Epic-subtask relationships preserved  
    @Test  
    public void testEpicSubtaskRelationshipsPreserved() {  
        Epic epic = new Epic("Test Epic", "Epic Description");  
        int epicId = taskManager.addNewEpic(epic);  
  
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW, epicId);  
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.DONE, epicId);  
  
        taskManager.addNewSubtask(subtask1);  
        taskManager.addNewSubtask(subtask2);  
  
        // Load from file  
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);  
  
        // Verify epic knows about its subtasks  
        List<Subtask> epicSubtasks = loadedManager.getEpicSubtasks(epicId);  
        assertEquals(2, epicSubtasks.size(), "Epic should have 2 subtasks");  
    }  
  
    // Test 4: test automatic file update  
    @Test  
    public void testAutoSaveAfterOperations() throws IOException {  
        assertTrue(tempFile.length() == 0 || !tempFile.exists(), "File should be empty initially");  
  
        Task task = new Task("Auto Save Test", "Description", TaskStatus.NEW);  
        taskManager.addNewTask(task);  
  
        assertTrue(tempFile.exists(), "File should be created");  
        assertTrue(tempFile.length() > 0, "File should have content after adding task");  
  
        task.setStatus(TaskStatus.DONE);  
        taskManager.updateTask(task);  
  
        assertTrue(tempFile.exists(), "File should still exist after update");  
    }  

  
    // Test 5: test update and delete  
    @Test  
    public void testUpdatesAndDeletesPersist() {  
        Task task = new Task("Delete Test", "Description", TaskStatus.NEW);  
        int taskId = taskManager.addNewTask(task);  
  
        task.setStatus(TaskStatus.DONE);  
        taskManager.updateTask(task);  
  
        FileBackedTaskManager loadedManager1 = FileBackedTaskManager.loadFromFile(tempFile);  
        assertEquals(TaskStatus.DONE, loadedManager1.getTask(taskId).getStatus());  
  
        loadedManager1.deleteTask(taskId);  
  
        FileBackedTaskManager loadedManager2 = FileBackedTaskManager.loadFromFile(tempFile);  
        assertTrue(loadedManager2.getTasks().isEmpty(), "Task should be deleted");  
    }  
  
    // Test 6: test id generation 
    @Test  
    public void testIdGenerationAfterLoading() {  
        Task task1 = new Task("First Task", "Description", TaskStatus.NEW);  
        int id1 = taskManager.addNewTask(task1);  
  
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);  
  
        Task task2 = new Task("Second Task", "Description", TaskStatus.NEW);  
        int id2 = loadedManager.addNewTask(task2);  
  
        assertTrue(id2 > id1, "New task should get higher ID than loaded task");  
        assertNotEquals(id1, id2, "IDs should not be duplicated");  
    }  
}  