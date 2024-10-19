package ru.aston.task.arraylist;

/**
 * Интерфейс простого списка с основными операциями.
 *
 * @param <T> тип элементов в списке
 */
public interface SimpleList<T> {

    /**
     * Добавляет элемент в конец списка.
     *
     * @param element элемент для добавления
     * @throws IndexOutOfBoundsException если текущий размер списка равен {@link Integer#MAX_VALUE}
     */
    void add(T element);

    /**
     * Добавляет элемент по указанному индексу.
     *
     * @param index   индекс, по которому элемент должен быть добавлен
     * @param element элемент для добавления
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index > size() || index > Integer.MAX_VALUE)
     */
    void add(int index, T element);

    /**
     * Возвращает элемент по указанному индексу.
     *
     * @param index индекс элемента для возврата
     * @return элемент по указанному индексу
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index >= size() || index > Integer.MAX_VALUE)
     */
    T get(int index);

    /**
     * Удаляет элемент по указанному индексу.
     *
     * @param index индекс элемента для удаления
     * @return удаленный элемент
     * @throws IndexOutOfBoundsException если индекс находится вне диапазона (index < 0 || index >= size() || index > Integer.MAX_VALUE)
     */
    T remove(int index);

    /**
     * Очищает всю коллекцию, удаляя все элементы.
     */
    void clear();


    /**
     * Возвращает количество элементов в списке.
     *
     * @return количество элементов в списке
     */
    int size();

    /**
     * Сортирует список по возрастанию с использованием натурального порядка (natural ordering).
     */
    void sort();
}
