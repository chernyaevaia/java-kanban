package ru.yandex.javacourse.schedule.handlers;  
  
import com.google.gson.Gson;  
import com.sun.net.httpserver.HttpExchange;  
import com.sun.net.httpserver.HttpHandler;

import ru.yandex.javacourse.schedule.BaseHttpHandler;
import ru.yandex.javacourse.schedule.manager.TaskManager;  
  
import java.io.IOException;  
  
public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {  
    private final TaskManager taskManager;  
    private final Gson gson;  
  
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {  
        this.taskManager = taskManager;  
        this.gson = gson;  
    }  
  
    @Override  
    public void handle(HttpExchange exchange) throws IOException {  
        if ("GET".equals(exchange.getRequestMethod())) {  
            String response = gson.toJson(taskManager.getPrioritizedTasks());  
            sendText(exchange, response);  
        } else {  
            sendNotFound(exchange, "Method not allowed");  
        }  
    }  
}  