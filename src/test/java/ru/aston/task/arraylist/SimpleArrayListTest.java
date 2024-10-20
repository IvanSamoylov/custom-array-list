package ru.aston.task.arraylist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleArrayListTest {


    @Test
    void shouldCreateEmptyList() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        assertEquals(0, list.size());
    }

    @Test
    void shouldAddElementToNewList() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        list.add(new Object());

        assertEquals(1, list.size());
    }

    @Test
    void shouldAddNewElementToListByIndex() {
        SimpleArrayList<Integer> list = new SimpleArrayList<>();
        list.add(1);
        list.add(3);
        list.add(4);

        int indexToInsert = 1;
        list.add(indexToInsert, 2);

        assertEquals(4, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(4, list.get(3));
    }


    @Test
    void shouldThrowExceptionIfIndexLessThanZero() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        list.add(new Object());

        int indexToInsert = -1;

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(indexToInsert, new Object()));
    }

    @Test
    void shouldThrowExceptionIfIndexMoreThanSize() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        list.add(new Object());

        int indexToInsert = 1;
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(indexToInsert, new Object()));
    }

    @Test
    void shouldExtendCapacityWhileAddingElement() {
        int initialCapacity = 2;
        SimpleArrayList<Object> list = new SimpleArrayList<>(initialCapacity);
        list.add(new Object());
        list.add(new Object());
        list.add(new Object());

        assertEquals(3, list.size());
    }

    @Test
    void shouldRemoveElementFromList() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        list.add(new Object());
        Object elementToRemove = new Object();
        list.add(elementToRemove);
        list.add(new Object());

        Object removedElement = list.remove(1);

        assertEquals(2, list.size());
        assertEquals(elementToRemove, removedElement);
    }

    @Test
    void shouldClearList() {
        SimpleArrayList<Object> list = new SimpleArrayList<>();
        list.add(new Object());
        list.add(new Object());

        list.clear();

        assertEquals(0, list.size());
    }

}