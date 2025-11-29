package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {

    @Test
    public void testEqualityById() {
        Epic e0 = new Epic(1, "Test 1", "Testing task 1", null, null);
        Epic e1 = new Epic(1, "Test 2", "Testing task 2", null, null);
        assertEquals(e0, e1, "task and subentities should be compared by id");
    }

}
