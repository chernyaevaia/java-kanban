package ru.yandex.javacourse.schedule.manager;  
  
import com.google.gson.Gson;  
import com.google.gson.reflect.TypeToken;  
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
  
public class HttpTaskManagerHistoryTest {  
  
    TaskManager manager = new InMemoryTaskManager();  
    HttpTaskServer taskServer = new HttpTaskServer(manager);  
    Gson gson = taskServer.getGson();  
  
    public HttpTaskManagerHistoryTest() {  
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
    public void testGetHistory() throws IOException, InterruptedException {  
        Task task = new Task("Task", "Desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());  
        int id = manager.addNewTask(task);  
        manager.getTask(id); 
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/history");  
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
        assertEquals(200, response.statusCode());  
  
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());  
        assertEquals(1, history.size());  
        assertEquals(id, history.get(0).getId());  
    }  
  
    @Test  
    public void testGetPrioritized() throws IOException, InterruptedException {  
        Task t1 = new Task("Late", "Desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now().plusHours(1));  
        manager.addNewTask(t1);  
  
        Task t2 = new Task("Early", "Desc", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());  
        manager.addNewTask(t2);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/prioritized");  
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
        assertEquals(200, response.statusCode());  
  
        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());  
        assertEquals(2, prioritized.size());  
        assertEquals("Early", prioritized.get(0).getName()); // First element should be the early task  
        assertEquals("Late", prioritized.get(1).getName());  
    }  
}  