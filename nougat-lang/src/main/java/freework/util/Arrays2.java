package freework.util;

import freework.function.Condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities of array.
 *
 * @author vacoor
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class Arrays2 {
    /**
     * Non-instantiate.
     */
    private Arrays2() {
    }

    /**
     * Copies the specified collection into new-array.
     *
     * @param elementType the component type
     * @param collection  the collection of elements
     * @param <E>         the component instance type
     * @return the new array
     */
    public static <E> E[] create(final Class<E> elementType, final Collection<? extends E> collection) {
        return create(elementType, -1, collection);
    }

    /**
     * Copies the specified iterable(s) into new-array.
     *
     * @param elementType the component type
     * @param mElements   the collection of elements
     * @param <E>         the component instance type
     * @return the new array
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] create(final Class<E> elementType, final Iterable<? extends E>... mElements) {
        final List<E> list = new LinkedList<E>();
        for (final Iterable<? extends E> elements : mElements) {
            if (null == elements) {
                continue;
            }
            if (elements instanceof Collection) {
                list.addAll((Collection<E>) elements);
            } else {
                for (final E element : elements) {
                    list.add(element);
                }
            }
        }
        return create(elementType, list);
    }

    /**
     * Copies the specified collection into new-array, truncating or padding with nulls (if necessary)
     * so the new-array has the specified length.
     *
     * @param elementType the component type
     * @param len         the length of the copy to be returned
     * @param collection  the collection of elements
     * @param <E>         the component instance type
     * @return the new array, truncated or padded with nulls to obtain the specified length
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] create(final Class<E> elementType, final int len, final Collection<? extends E> collection) {
        if (null == collection) {
            return (E[]) Array.newInstance(elementType, 0);
        }
        final int finalLength = 0 > len ? collection.size() : len;
        final E[] elements = (E[]) Array.newInstance(elementType, finalLength);
        return 0 < finalLength ? collection.toArray(elements) : elements;
    }

    /**
     * Gets the number of dimensions of the array.
     *
     * @param array the array
     * @return the number of dimensions of the array if the {@code array} is array, otherwise 0
     */
    public static int getDimensions(final Object array) {
        if (null == array || !array.getClass().isArray()) {
            return 0;
        }
        int dimensions = 0;
        for (Class<?> componentType = array.getClass(); !Object.class.equals(componentType); componentType = getComponentClass(componentType)) {
            dimensions++;
        }
        return dimensions;
    }

    /**
     * Filters the given array to return a new array.
     *
     * @param elements  the array
     * @param condition the condition
     * @param <E>       the component type
     * @return the new array
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] filter(final E[] elements, final Condition<E> condition) {
        final List<E> filtered = new ArrayList<E>(elements.length);
        for (E element : elements) {
            if (condition.value(element)) {
                filtered.add(element);
            }
        }
        return create((Class<E>) getComponentClass(elements.getClass()), filtered);
    }

    /**
     * Searches the specified array of booleans for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final boolean[] array, final boolean value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of booleans for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final boolean[] array, final int fromIndex, final int toIndex, final boolean value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of bytes for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final byte[] array, final byte value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of bytes for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final byte[] array, final int fromIndex, final int toIndex, final byte value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of chars for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final char[] array, final char value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of chars for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final char[] array, final int fromIndex, final int toIndex, final char value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of shorts for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final short[] array, final short value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of shorts for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final short[] array, final int fromIndex, final int toIndex, final short value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of integers for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final int[] array, final int value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of integers for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final int[] array, final int fromIndex, final int toIndex, final int value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of longs for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final long[] array, final long value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of longs for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final long[] array, final int fromIndex, final int toIndex, final long value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of floats for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final float[] array, final float value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of floats for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final float[] array, final int fromIndex, final int toIndex, final float value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of doubles for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final double[] array, final double value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of doubles for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @return index of the search key if it is contained, otherwise -1
     */
    public static int search(final double[] array, final int fromIndex, final int toIndex, final double value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Searches the specified array of objects for the specified value.
     *
     * @param array the array to be searched
     * @param value the value to be searched for
     * @param <E>   the component instance type
     * @return index of the search key if it is contained, otherwise -1
     */
    public static <E> int search(final E[] array, final E value) {
        return search(array, 0, array.length, value);
    }

    /**
     * Searches a range of the specified array of objects for the specified value.
     *
     * @param array     the array to be searched
     * @param fromIndex the index of the first element (inclusive) to be searched
     * @param toIndex   the index of the last element (exclusive) to be searched
     * @param value     the value to be searched for
     * @param <E>       the component instance type
     * @return index of the search key if it is contained, otherwise -1
     */
    @SuppressWarnings("PMD.AvoidComplexConditionRule")
    public static <E> int search(final E[] array, final int fromIndex, final int toIndex, final E value) {
        rangeCheck(array.length, fromIndex, toIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            if (value == array[i] || (null != value && value.equals(array[i]))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns a string containing the string representation of each of elements, using the ',' between each.
     *
     * @param array the array
     * @return the string
     */
    public static String toString(final Object array) {
        return toString(array, ",");
    }

    /**
     * Returns a string containing the string representation of each of elements, using the separator between each.
     *
     * @param array the array
     * @param sep   the separator
     * @return the string
     */
    public static String toString(final Object array, final String sep) {
        return toString(array, 0, -1, sep);
    }

    /**
     * Returns a string containing the string representation of range of elements, using the separator between each.
     *
     * @param array     the array
     * @param fromIndex the index of the first element (inclusive)
     * @param toIndex   the index of the last element (exclusive)
     * @param sep       the separator
     * @return the string
     */
    public static String toString(final Object array, final int fromIndex, final int toIndex, final String sep) {
        final int length = Array.getLength(array);
        rangeCheck(length, fromIndex, toIndex);

        final StringBuilder buff = new StringBuilder();
        for (int i = fromIndex; i < toIndex; i++) {
            if (i > fromIndex) {
                buff.append(sep);
            }
            buff.append(Array.get(array, i));
        }
        return buff.toString();
    }

    /**
     * Returns component type of the specified array class.
     *
     * @param clazz the array class
     * @return the component type if class is array, otherwise Object.class
     */
    private static Class<?> getComponentClass(final Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        return Object.class;
    }

    /**
     * Checks that {@code fromIndex} and {@code toIndex} are in
     * the range and throws an exception if they aren't.
     *
     * @param arrayLength the length of array
     * @param fromIndex   the from index
     * @param toIndex     the to index
     */
    private static void rangeCheck(final int arrayLength, final int fromIndex, final int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }
}
