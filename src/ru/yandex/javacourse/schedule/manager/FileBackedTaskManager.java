package ru.yandex.javacourse.schedule.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

enum TaskType {
    TASK, EPIC, SUBTASK
}

public class FileBackedTaskManager extends InMemoryTaskManager {
    protected File file = new File("history.csv");
    private boolean isLoading = false;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.isLoading = true;
        manager.updateGeneratorId();

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();

                if (line.isEmpty()) {
                    continue;
                }

                Task task = manager.createFromString(line);

                if (task instanceof Subtask) {
                    manager.addNewSubtask((Subtask) task);
                } else if (task instanceof Epic) {
                    Epic epic = (Epic) task;
                    manager.addNewEpic(epic);
                } else {
                    manager.addNewTask(task);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }

        manager.isLoading = false;
        return manager;
    }

    private void updateGeneratorId() {
        int maxId = 0;

        for (Task task : getTasks()) {
            maxId = Math.max(maxId, task.getId());
        }
        for (Epic epic : getEpics()) {
            maxId = Math.max(maxId, epic.getId());
        }
        for (Subtask subtask : getSubtasks()) {
            maxId = Math.max(maxId, subtask.getId());
        }

        generatorId = maxId;
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Subtask) {
            return TaskType.SUBTASK;
        } else if (task instanceof Epic) {
            return TaskType.EPIC;
        } else {
            return TaskType.TASK;
        }
    }

    private String convertToString(Task task) {
        String baseInfo = task.getId() + "," + getTaskType(task) + "," +
                task.getName() + "," + task.getStatus() + "," +
                task.getDescription();

        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return baseInfo + "," + subtask.getEpicId();
        } else {
            return baseInfo + ",";
        }
    }

    private Task createFromString(String value) {
        String[] parts = value.split(",");

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description);
            case SUBTASK:
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    public void save() {
        if (isLoading)
            return;

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write("id,type,name,status,description,epic\n");

            for (Task task : getTasks()) {
                fileWriter.write(convertToString(task) + "\n");
            }

            for (Epic epic : getEpics()) {
                fileWriter.write(convertToString(epic) + "\n");
            }

            for (Subtask subtask : getSubtasks()) {
                fileWriter.write(convertToString(subtask) + "\n");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }
}