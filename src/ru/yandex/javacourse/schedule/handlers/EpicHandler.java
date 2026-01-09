package ru.yandex.javacourse.schedule.handlers;  
  
import com.google.gson.Gson;  
import com.sun.net.httpserver.HttpExchange;  
import com.sun.net.httpserver.HttpHandler;

import ru.yandex.javacourse.schedule.BaseHttpHandler;
import ru.yandex.javacourse.schedule.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;  
import ru.yandex.javacourse.schedule.tasks.Epic;  
  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.nio.charset.StandardCharsets;  
import java.util.regex.Pattern;  
  
public class EpicHandler extends BaseHttpHandler implements HttpHandler {  
    private final TaskManager taskManager;  
    private final Gson gson;  
  
    public EpicHandler(TaskManager taskManager, Gson gson) {  
        this.taskManager = taskManager;  
        this.gson = gson;  
    }  
  
    @Override  
    public void handle(HttpExchange exchange) throws IOException {  
        try {  
            String path = exchange.getRequestURI().getPath();  
            String method = exchange.getRequestMethod();  
  
            switch (method) {  
                case "GET": {  
                    if (Pattern.matches("^/epics$", path)) {  
                        String response = gson.toJson(taskManager.getEpics());  
                        sendText(exchange, response);  
                    } else if (Pattern.matches("^/epics/\\d+$", path)) {  
                        String pathId = path.replaceFirst("/epics/", "");  
                        int id = Integer.parseInt(pathId);  
                        String response = gson.toJson(taskManager.getEpic(id));  
                        sendText(exchange, response);  
                    } else if (Pattern.matches("^/epics/\\d+/subtasks$", path)) {  
                        String pathId = path.split("/")[2];   
                        int id = Integer.parseInt(pathId);  
                        String response = gson.toJson(taskManager.getEpicSubtasks(id));  
                        sendText(exchange, response);  
                    }  
                    break;  
                }  
                case "POST": {  
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);  
                    Epic epic = gson.fromJson(reader, Epic.class);  
                    if (epic.getId() != 0) {  
                        taskManager.updateEpic(epic);  
                        sendText(exchange, "Epic updated");  
                    } else {  
                        taskManager.addNewEpic(epic);  
                        sendText(exchange, "Epic created");  
                    }  
                    break;  
                }  
                case "DELETE": {  
                    if (Pattern.matches("^/epics/\\d+$", path)) {  
                        String pathId = path.replaceFirst("/epics/", "");  
                        int id = Integer.parseInt(pathId);  
                        taskManager.deleteEpic(id);  
                        sendText(exchange, "Epic deleted");  
                    }  
                    break;  
                }  
                default:  
                    sendNotFound(exchange, "Method not allowed");  
            }  
        } catch (NotFoundException e) {  
            sendNotFound(exchange, "Epic not found");  
        } catch (Exception e) {  
            e.printStackTrace();  
            sendResponse(exchange, "Internal Server Error", 500);  
        }  
    }  
}  