package ru.yandex.javacourse.schedule.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import ru.yandex.javacourse.schedule.BaseHttpHandler;
import ru.yandex.javacourse.schedule.NotFoundException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
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
                    if (Pattern.matches("^/tasks$", path)) {
                        String response = gson.toJson(taskManager.getTasks());
                        sendText(exchange, response);
                    } else if (Pattern.matches("^/tasks/\\d+$", path)) {
                        String pathId = path.replaceFirst("/tasks/", "");
                        int id = parsePathId(pathId);
                        String response = gson.toJson(taskManager.getTask(id));
                        sendText(exchange, response);
                    } else {
                        sendNotFound(exchange, "Invalid path");
                    }
                    break;
                }
                case "POST": {
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    Task task = gson.fromJson(reader, Task.class);

                    if (task.getId() != 0) {
                        taskManager.updateTask(task);
                        sendText(exchange, "Task updated");
                    } else {
                        taskManager.addNewTask(task);
                        sendText(exchange, "Task created");
                    }
                    break;
                }
                case "DELETE": {
                    if (Pattern.matches("^/tasks/\\d+$", path)) {
                        String pathId = path.replaceFirst("/tasks/", "");
                        int id = parsePathId(pathId);
                        taskManager.deleteTask(id);
                        sendText(exchange, "Task deleted");
                    } else {
                        sendText(exchange, "ID required for deletion");
                    }
                    break;
                }
                default:
                    sendNotFound(exchange, "Method not allowed");
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, "Task not found");
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, "Task overlaps with existing task");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "Internal Server Error", 500);
        }
    }

    private int parsePathId(String pathId) {
        try {
            return Integer.parseInt(pathId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}