package ru.yandex.javacourse.schedule.manager;  
  
import com.google.gson.Gson;  
import com.google.gson.reflect.TypeToken;  
import org.junit.jupiter.api.AfterEach;  
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;

import ru.yandex.javacourse.schedule.HttpTaskServer;
import ru.yandex.javacourse.schedule.tasks.Epic;  
import ru.yandex.javacourse.schedule.tasks.Subtask;  
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
  
public class HttpTaskManagerEpicsTest {  
  
    TaskManager manager = new InMemoryTaskManager();  
    HttpTaskServer taskServer = new HttpTaskServer(manager);  
    Gson gson = taskServer.getGson();  
  
    public HttpTaskManagerEpicsTest() {  
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
    public void testAddEpic() throws IOException, InterruptedException {  
        Epic epic = new Epic("Test Epic", "Epic Desc");  
        String json = gson.toJson(epic);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/epics");  
        HttpRequest request = HttpRequest.newBuilder()  
                .uri(url)  
                .POST(HttpRequest.BodyPublishers.ofString(json))  
                .build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
        assertEquals(200, response.statusCode());  
        assertEquals(1, manager.getEpics().size());  
    }  
  
    @Test  
    public void testAddSubtask() throws IOException, InterruptedException {  
        Epic epic = new Epic("Epic for Subtask", "Desc");  
        int epicId = manager.addNewEpic(epic);  
  
        Subtask subtask = new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epicId,   
                                      Duration.ofMinutes(10), LocalDateTime.now());  
        String json = gson.toJson(subtask);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/subtasks");  
        HttpRequest request = HttpRequest.newBuilder()  
                .uri(url)  
                .POST(HttpRequest.BodyPublishers.ofString(json))  
                .build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
        assertEquals(200, response.statusCode());  
          
        // Verify linkage  
        assertEquals(1, manager.getSubtasks().size());  
        assertEquals(epicId, manager.getSubtasks().get(0).getEpicId());  
    }  
      
    @Test  
    public void testGetEpicSubtasks() throws IOException, InterruptedException {  
        Epic epic = new Epic("Epic", "Desc");  
        int epicId = manager.addNewEpic(epic);  
          
        Subtask s1 = new Subtask("S1", "D", TaskStatus.NEW, epicId, Duration.ofMinutes(5), LocalDateTime.now());  
        manager.addNewSubtask(s1);  
  
        HttpClient client = HttpClient.newHttpClient();  
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");  
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();  
  
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());  
        assertEquals(200, response.statusCode());  
  
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>(){}.getType());  
        assertEquals(1, subtasks.size());  
        assertEquals("S1", subtasks.get(0).getName());  
    }  
}  