package ru.yandex.javacourse.schedule.manager;  
  
import org.junit.jupiter.api.BeforeEach;  
import org.junit.jupiter.api.Test;  
import ru.yandex.javacourse.schedule.tasks.Task;  
import java.util.List;  
  
import static org.junit.jupiter.api.Assertions.*;  
  
public class InMemoryHistoryManagerTest {  
  
    private HistoryManager historyManager;  
  
    @BeforeEach  
    public void init() {  
        historyManager = Managers.getDefaultHistory();  
    }  
  
    // Test 1: adding a task   
    @Test  
    public void testAdd() {  
        Task task = new Task(1, "Test Task", "Description", null);  
        historyManager.addTask(task);  
        final List<Task> history = historyManager.getHistory();  
        assertNotNull(history, "History should not be null.");  
        assertEquals(1, history.size(), "History size should be 1.");  
    }  
  
    // Test 2: no duplicates 
    @Test  
    public void testNoDuplicates() {  
        Task task = new Task(1, "Test Task", "Description", null);  
        historyManager.addTask(task);  
        historyManager.addTask(task);   
        assertEquals(1, historyManager.getHistory().size(), "History should not contain duplicates.");  
    }  
  
    // Test 3: re-adding moves task to the end
    @Test  
    public void testReAddingMovesToEnd() {  
        Task task1 = new Task(1, "Task 1", "D1", null);  
        Task task2 = new Task(2, "Task 2", "D2", null);  
        Task task3 = new Task(3, "Task 3", "D3", null);  
  
        historyManager.addTask(task1);  
        historyManager.addTask(task2);  
        historyManager.addTask(task3);  
  
        historyManager.addTask(task1);  
  
        final List<Task> history = historyManager.getHistory();  
        assertEquals(3, history.size(), "History size should remain 3.");  
        assertEquals(task2, history.get(0), "Task 2 should be the first item.");  
        assertEquals(task3, history.get(1), "Task 3 should be the second item.");  
        assertEquals(task1, history.get(2), "Task 1 should be moved to the end.");  
    }  
  
    // Test 4: Removal 
    @Test  
    public void testRemoveFirst() {  
        Task task1 = new Task(1, "Task 1", "D1", null);  
        Task task2 = new Task(2, "Task 2", "D2", null);  
        historyManager.addTask(task1);  
        historyManager.addTask(task2);  
  
        historyManager.remove(1); 
        final List<Task> history = historyManager.getHistory();  
  
        assertEquals(1, history.size(), "History should contain one item after removal.");  
        assertEquals(task2, history.get(0), "Task 2 should be the only item left.");  
    }  
 
}  