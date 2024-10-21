package ru.aston.task.arraylist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleArrayListTest {

    private static final String TEST_STRING = "Test String";
    private static final String NEW_TEST_STRING = "new String";
    private SimpleArrayList<String> list;

    @BeforeEach
    void setUp() {
        list = new SimpleArrayList<>();
    }

    @Test
    void shouldCreateEmptyList() {
        assertEquals(0, list.size());
    }

    @Test
    void shouldAddElementToNewList() {
        list.add(TEST_STRING);

        assertEquals(1, list.size());
    }

    @Test
    void shouldAddNewElementToListByIndex() {
        list.add("1");
        list.add("3");
        list.add("4");

        list.add(1, "2");

        assertEquals(4, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));
        assertEquals("4", list.get(3));
    }


    @Test
    void shouldThrowExceptionIfIndexLessThanZero() {
        list.add(TEST_STRING);

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, NEW_TEST_STRING));
    }

    @Test
    void shouldThrowExceptionIfIndexMoreThanSize() {
        list.add(TEST_STRING);

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(1, NEW_TEST_STRING));
    }

    @Test
    void shouldExtendCapacityWhileAddingElement() {
        SimpleArrayList<String> list = new SimpleArrayList<>(2);
        list.add("1");
        list.add("2");
        list.add("3");

        assertEquals(3, list.size());
    }

    @Test
    void shouldRemoveElementFromList() {
        list.add("1");
        String elementToRemove = "string to remove";
        list.add(elementToRemove);
        list.add("2");

        String removedElement = list.remove(1);

        assertEquals(2, list.size());
        assertEquals(elementToRemove, removedElement);
    }

    @Test
    void shouldClearList() {
        list.add(TEST_STRING);
        list.add(NEW_TEST_STRING);

        list.clear();

        assertEquals(0, list.size());
    }

    @Test
    void shouldSortList() {
        list.add("D");
        list.add(null);
        list.add(null);
        list.add("F");
        list.add("C");

        list.sort();

        assertNull(list.get(0));
        assertNull(list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("F", list.get(4));
        assertEquals(5, list.size());
    }

    @Test
    void shouldSortListWithNulls() {
        list.add(null);
        list.add(null);

        list.sort();

        assertNull(list.get(0));
        assertNull(list.get(1));
        assertEquals(2, list.size());
    }

    @Test
    void shouldSortListWithComparator() {
        list.add("D");
        list.add("A");
        list.add("B");
        list.add("F");
        list.add("C");

        list.sort(String::compareTo);

        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
        assertEquals("F", list.get(4));
        assertEquals(5, list.size());
    }

}