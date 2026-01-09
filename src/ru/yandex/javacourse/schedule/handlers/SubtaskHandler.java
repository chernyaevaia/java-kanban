package ru.yandex.javacourse.schedule.handlers;  
  
import com.google.gson.Gson;  
import com.sun.net.httpserver.HttpExchange;  
import com.sun.net.httpserver.HttpHandler;

import ru.yandex.javacourse.schedule.BaseHttpHandler;
import ru.yandex.javacourse.schedule.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;  
import ru.yandex.javacourse.schedule.tasks.Subtask;  
  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.nio.charset.StandardCharsets;  
import java.util.regex.Pattern;  
  
public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {  
    private final TaskManager taskManager;  
    private final Gson gson;  
  
    public SubtaskHandler(TaskManager taskManager, Gson gson) {  
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
                    if (Pattern.matches("^/subtasks$", path)) {  
                        String response = gson.toJson(taskManager.getSubtasks());  
                        sendText(exchange, response);  
                    } else if (Pattern.matches("^/subtasks/\\d+$", path)) {  
                        String pathId = path.replaceFirst("/subtasks/", "");  
                        int id = Integer.parseInt(pathId);  
                        String response = gson.toJson(taskManager.getSubtask(id));  
                        sendText(exchange, response);  
                    }  
                    break;  
                }  
                case "POST": {  
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);  
                    Subtask subtask = gson.fromJson(reader, Subtask.class);  
                    if (subtask.getId() != 0) {  
                        taskManager.updateSubtask(subtask);  
                        sendText(exchange, "Subtask updated");  
                    } else {  
                        taskManager.addNewSubtask(subtask);  
                        sendText(exchange, "Subtask created");  
                    }  
                    break;  
                }  
                case "DELETE": {  
                    if (Pattern.matches("^/subtasks/\\d+$", path)) {  
                        String pathId = path.replaceFirst("/subtasks/", "");  
                        int id = Integer.parseInt(pathId);  
                        taskManager.deleteSubtask(id);  
                        sendText(exchange, "Subtask deleted");  
                    }  
                    break;  
                }  
                default:  
                    sendNotFound(exchange, "Method not allowed");  
            }  
        } catch (NotFoundException e) {  
            sendNotFound(exchange, "Subtask not found");  
        } catch (IllegalArgumentException e) {  
            sendHasInteractions(exchange, "Subtask overlaps");  
        } catch (Exception e) {  
            e.printStackTrace();  
            sendResponse(exchange, "Internal Server Error", 500);  
        }  
    }  
}  