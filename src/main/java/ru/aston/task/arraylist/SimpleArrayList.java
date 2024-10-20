package ru.aston.task.arraylist;

import java.util.Comparator;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.Arrays;

/**
 * Реализует интерфейс {@link SimpleList} на основе массива.
 *
 * @param <T> тип элементов в списке
 */
public class SimpleArrayList<T> implements SimpleList<T> {
    private final Logger logger = Logger.getLogger(SimpleArrayList.class.getName());
    private int initialCapacity = 10;
    private int maxCapacity = Integer.MAX_VALUE;
    private T[] elements;
    private int size;

    /**
     * Конструктор по умолчанию, создающий список с начальной емкостью равной 10 элементам.
     */
    @SuppressWarnings("unchecked")
    public SimpleArrayList() {
        this.elements = (T[]) new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Конструктор, создающий список с начальной емкостью, определенной параметром initialCapacity.
     *
     * @param initialCapacity начальный размер массива
     */
    @SuppressWarnings("unchecked")
    public SimpleArrayList(int initialCapacity) {
        this.elements = (T[]) new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Добавляет элемент в конец списка.
     *
     * @param element элемент для добавления
     */
    @Override
    public void add(T element) {
        extendArrayIfRequired();
        elements[size] = element;
        size++;
    }

    /**
     * Добавляет элемент в существующий индекс массива.
     *
     * @param index   существующий индекс массива
     * @param element элемент для добавления
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index > size())
     */
    @Override
    public void add(int index, T element) {
        checkIndexRange(index);
        extendArrayIfRequired();
        moveElementsToRignt(index);

        elements[index] = element;
        size++;
    }

    private void extendArrayIfRequired() {
        if (isExtensionRequired()) {
            int newCapacity = calculateNewLength();
            logger.log(Level.FINE, String.format("Extending array from %d to %d", elements.length, newCapacity));
            elements = Arrays.copyOf(elements, calculateNewLength());
        }
    }

    private boolean isExtensionRequired() {
        return size == elements.length && size < maxCapacity;
    }

    private int calculateNewLength() {
        if (elements.length > maxCapacity / 2) {
            return maxCapacity;
        }
        return elements.length * 2;
    }


    /**
     * Возвращает элемент по указанному индексу.
     *
     * @param index индекс элемента для возврата
     * @return элемент по указанному индексу
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index >= size())
     */
    @Override
    public T get(int index) {
        return elements[index];
    }

    /**
     * Удаляет элемент из списка по индексу.
     *
     * @param index индекс элемента для удаления
     * @return удаленный элемент по указанному индексу
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index >= size())
     */
    @Override
    public T remove(int index) {
        checkIndexRange(index);
        T elementToRemove = elements[index];
        moveElementsToLeft(index);
        size--;
        return elementToRemove;
    }

    /**
     * Удаляет все элементы из списка.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void clear() {
        elements = (T[]) new Object[initialCapacity];
        size = 0;
    }


    /**
     * Возвращает количество элементов в списке.
     *
     * @return количество элементов в списке
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Сортирует список по возрастанию. Элементы "null" ставятся в начало списка.
     */
    @Override
    public void sort() {
        SortUtils.quickSort(elements, size);
    }

    /**
     * Сортирует список с использованием компаратора.
     *
     * @param comparator компаратор
     */
    @Override
    public void sort(Comparator<? super T> comparator) {
        SortUtils.quickSort(elements, size, comparator);
    }

    private void checkIndexRange(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Некорректный индекс: " + index);
        }
    }

    private void moveElementsToRignt(int index) {
        for (int i = size; i > index; i--) {
            elements[i] = elements[i - 1];
        }
    }

    private void moveElementsToLeft(int index) {
        for (int i = index; i < size; i++) {
            elements[i] = elements[i + 1];
        }
    }

}
