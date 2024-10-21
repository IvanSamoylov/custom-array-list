package ru.aston.task.arraylist;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SortUtils {
    private static final Logger logger = Logger.getLogger(SortUtils.class.getName());
    private static final Random random = new Random();

    public static <T> void quickSort(T[] arrayToSort, int size) {

        if (isElementsComparable(arrayToSort)) {
            quickSort(arrayToSort, 0, size - 1);
            return;
        }
        logger.log(Level.FINE, "Элементы массива не реализуют интерфейс Comparable. Массив не будет отсортирован.");
    }

    private static <T> boolean isElementsComparable(T[] arrayToSort) {
        return arrayToSort != null && Arrays.stream(arrayToSort)
                .filter(Objects::nonNull)
                .findAny()
                .map( t -> t instanceof Comparable)
                .orElse(false);
    }

    private static <T> void quickSort(T[] array, int low, int high) {
        if (low < high) {
            int pivotIndex = partition(array, low, high);
            quickSort(array, low, pivotIndex - 1);
            quickSort(array, pivotIndex + 1, high);
        }
    }

    private static <T> int partition(T[] array, int low, int high) {
        T pivot = getRandomPivot(array, low, high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (isNotComparable(array[j]) || ((Comparable<T>) array[j]).compareTo(pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return i + 1;
    }

    private static <T> T getRandomPivot(T[] array, int low, int high) {
        int pivotIndex = getPivotIndex(array, low, high);
        swap(array, pivotIndex, high);
        return array[high];
    }

    private static <T> int getPivotIndex(T[] array, int low, int high) {
        int pivot = random.nextInt((high - low) + 1) + low;
        if (Objects.isNull(array[pivot])) {
            return getFirstNonNullElementIndex(array, low, high);
        }
        return pivot;
    }

    private static <T> int getFirstNonNullElementIndex(T[] array, int low, int high) {
        for (int i = high; i >= low; i--) {
            if (Objects.nonNull(array[i])) {
                return i;
            }
        }
        return low;
    }

    private static <T> void swap(T[] arr, int i, int j) {
        T tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static <T> boolean isNotComparable(T element) {
        return !(element instanceof Comparable);
    }

    public static <T> void quickSort(T[] array, int size, Comparator<? super T> comparator) {
        quickSort(array, 0, size - 1, comparator);
    }

    private static <T> void quickSort(T[] array, int low, int high, Comparator<? super T> comparator) {
        if (low < high) {
            int pivotIndex = partition(array, low, high, comparator);
            quickSort(array, low, pivotIndex - 1, comparator);
            quickSort(array, pivotIndex + 1, high, comparator);
        }
    }

    private static <T> int partition(T[] array, int low, int high, Comparator<? super T> comparator) {
        T pivot = getRandomPivot(array, low, high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return i + 1;
    }

}
