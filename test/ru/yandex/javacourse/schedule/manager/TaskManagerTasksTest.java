package ru.yandex.javacourse.schedule.manager;  
  
import com.google.gson.Gson;  
import org.junit.jupiter.api.AfterEach;  
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;

import ru.yandex.javacourse.schedule.HttpTaskServer;
import ru.yandex.javacourse.schedule.tasks.Task;  
import ru.yandex.javacourse.schedule.tasks.TaskStatus;  
  
import java.io.IOException;  
import java.net.URI;  
import java.net.http.HttpClient;  
import java.net.http.HttpRequest;  
import java.net.http.HttpResponse;  
import java.time.Duration;  
import java.time.LocalDateTime;  
import java.util.List;  
  
import static org.junit.jupiter.api.Assertions.assertEquals;  
import static org.junit.jupiter.api.Assertions.assertNotNull;  
  
public class TaskManagerTasksTest {  
  
    TaskManager manager = new InMemoryTaskManager();  
    HttpTaskServer taskServer = new HttpTaskServer(manager);  
    Gson gson = taskServer.getGson();  
  
    public void HttpTaskManagerTasksTest() {  
    }  
  
    @BeforeEach  
    public void setUp() {  
        manager.deleteTasks();  
        manager.deleteSubtasks();  
        manager.deleteEpics();  
        taskServer.start();  
    }  
  
    @AfterEach  
    public void shutDown() {  
        taskServer.stop();  
    }  
  
    @Test  
    public void testAddTask() throws IOException, InterruptedException {  
        Task task = new Task("Test 2", "Testing task 2",  
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());  
        String taskJson = gson.toJson(task);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/tasks");  
        HttpRequest request = HttpRequest.newBuilder()  
                .uri(url)  
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))  
                .build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
  
        assertEquals(200, response.statusCode());  
  
        List<Task> tasksFromManager = manager.getTasks();  
        assertNotNull(tasksFromManager, "Tasks should not be null");  
        assertEquals(1, tasksFromManager.size(), "Should contain 1 task");  
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Name should match");  
    }  
  
    @Test  
    public void testGetTaskById() throws IOException, InterruptedException {  
        Task task = new Task("Test Get", "Desc", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());  
        int id = manager.addNewTask(task);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/tasks/" + id);  
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
  
        assertEquals(200, response.statusCode());  
          
        Task taskFromResponse = gson.fromJson(response.body(), Task.class);  
        assertEquals(id, taskFromResponse.getId());  
        assertEquals("Test Get", taskFromResponse.getName());  
    }  
  
    @Test  
    public void testDeleteTask() throws IOException, InterruptedException {  
        Task task = new Task("To Delete", "Desc", TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.now());  
        int id = manager.addNewTask(task);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/tasks/" + id);  
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
  
        assertEquals(200, response.statusCode());  
        assertEquals(0, manager.getTasks().size(), "Manager should be empty after delete");  
    }  
      
    @Test  
    public void testTaskNotFound() throws IOException, InterruptedException {  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/tasks/9999");  
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
  
        assertEquals(404, response.statusCode());  
    }  
  
    @Test  
    public void testTaskOverlap() throws IOException, InterruptedException {  
        LocalDateTime now = LocalDateTime.now();  
        Task task1 = new Task("Task 1", "Desc", TaskStatus.NEW, Duration.ofMinutes(10), now);  
        manager.addNewTask(task1);  
  
        Task task2 = new Task("Task 2", "Desc", TaskStatus.NEW, Duration.ofMinutes(10), now.plusMinutes(5));  
        String task2Json = gson.toJson(task2);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/tasks");  
        HttpRequest request = HttpRequest.newBuilder()  
                .uri(url)  
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))  
                .build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
  
        assertEquals(406, response.statusCode(), "Should return 406 Not Acceptable for overlap");  
    }  
}  