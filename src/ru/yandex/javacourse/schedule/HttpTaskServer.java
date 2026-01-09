package ru.yandex.javacourse.schedule;  
  
import com.google.gson.Gson;  
import com.google.gson.GsonBuilder;  
import com.google.gson.TypeAdapter;  
import com.google.gson.stream.JsonReader;  
import com.google.gson.stream.JsonWriter;  
import com.sun.net.httpserver.HttpServer;

import ru.yandex.javacourse.schedule.handlers.EpicHandler;
import ru.yandex.javacourse.schedule.handlers.HistoryHandler;
import ru.yandex.javacourse.schedule.handlers.PrioritizedHandler;
import ru.yandex.javacourse.schedule.handlers.SubtaskHandler;
import ru.yandex.javacourse.schedule.handlers.TaskHandler;
import ru.yandex.javacourse.schedule.manager.Managers;  
import ru.yandex.javacourse.schedule.manager.TaskManager;  
  
import java.io.IOException;  
import java.net.InetSocketAddress;  
import java.time.Duration;  
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
  
public class HttpTaskServer {  
    private static final int PORT = 8080;  
    private HttpServer httpServer;  
    private final TaskManager taskManager;  
    private final Gson gson;  
  
    public HttpTaskServer(TaskManager taskManager) {  
        this.taskManager = taskManager;  
          
        this.gson = new GsonBuilder()  
                .setPrettyPrinting()  
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  
                .registerTypeAdapter(Duration.class, new DurationAdapter())  
                .create();  
    }  
  
    public static void main(String[] args) throws IOException {  
        TaskManager taskManager = Managers.getDefault();  
        HttpTaskServer server = new HttpTaskServer(taskManager);  
        server.start();  
    }  
  
    public void start() {  
        try {  
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);  
  
            httpServer.createContext("/tasks", new TaskHandler(taskManager, gson));  
            httpServer.createContext("/subtasks", new SubtaskHandler(taskManager, gson));  
            httpServer.createContext("/epics", new EpicHandler(taskManager, gson));  
            httpServer.createContext("/history", new HistoryHandler(taskManager, gson));  
            httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));  
  
            httpServer.start();  
            System.out.println("HTTP Server started on port " + PORT);  
        } catch (IOException e) {  
            System.out.println("Server start failed: " + e.getMessage());  
        }  
    }  
  
    public void stop() {  
        if (httpServer != null) {  
            httpServer.stop(0);  
        }  
    }  
  
    public Gson getGson() {  
        return gson;  
    }  
  
    // --- Adapters ---  
  
    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {  
        private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;  
  
        @Override  
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {  
            if (localDateTime == null) {  
                jsonWriter.nullValue();  
            } else {  
                jsonWriter.value(localDateTime.format(dtf));  
            }  
        }  
  
        @Override  
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {  
            return LocalDateTime.parse(jsonReader.nextString(), dtf);  
        }  
    }  
  
    static class DurationAdapter extends TypeAdapter<Duration> {  
        @Override  
        public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {  
            if (duration == null) {  
                jsonWriter.nullValue();  
            } else {  
                jsonWriter.value(duration.toMinutes());  
            }  
        }  
  
        @Override  
        public Duration read(final JsonReader jsonReader) throws IOException {  
            return Duration.ofMinutes(jsonReader.nextLong());  
        }  
    }  
}  