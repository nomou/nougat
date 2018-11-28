package freework.util;

import freework.function.Condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 数组工具类
 *
 * @author vacoor
 */
@SuppressWarnings("unused")
public abstract class Arrays2 {
    /**
     * Non-instantiate.
     */
    private Arrays2() {
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] create(final Class<E> elementType, final int len) {
        return (E[]) Array.newInstance(elementType, len);
    }

    public static <E> E[] create(final E[] original, int newLength) {
        return Arrays.copyOf(original, newLength);
    }

    public static <E> E[] create(final Class<E> elementType, final Collection<? extends E> collection) {
        return create(elementType, -1, collection);
    }

    public static <E> E[] create(final Class<E> elementType, final int len, final Collection<? extends E> collection) {
        if (null == collection) {
            return create(elementType, 0);
        }
        final int finalLength = 0 > len ? collection.size() : len;
        final E[] elements = create(elementType, finalLength);
        return 0 < finalLength ? collection.toArray(elements) : elements;
    }

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
     * 获取给定数组的维度数, 如果不是数组, 返回 0
     *
     * @param array 数组
     * @return
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

    @SuppressWarnings("unchecked")
    public static <E> E[] filter(E[] elements, Condition<E> checker) {
        final List<E> filtered = new ArrayList<E>(elements.length);
        for (E element : elements) {
            if (checker.value(element)) {
                filtered.add(element);
            }
        }
        return create((Class<E>) getComponentClass(elements.getClass()), filtered);
    }

    public static boolean[] mergeArrays(boolean[] array1, boolean[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (boolean[] array : arrays) {
            len += array.length;
        }

        boolean[] results = new boolean[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (boolean[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static byte[] mergeArrays(byte[] array1, byte[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (byte[] array : arrays) {
            len += array.length;
        }

        byte[] results = new byte[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static short[] mergeArrays(short[] array1, short[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (short[] array : arrays) {
            len += array.length;
        }

        short[] results = new short[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (short[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static int[] mergeArrays(int[] array1, int[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (int[] array : arrays) {
            len += array.length;
        }

        int[] results = new int[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (int[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static long[] mergeArrays(long[] array1, long[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (long[] array : arrays) {
            len += array.length;
        }

        long[] results = new long[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (long[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static float[] mergeArrays(float[] array1, float[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (float[] array : arrays) {
            len += array.length;
        }

        float[] results = new float[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (float[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static double[] mergeArrays(double[] array1, double[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (double[] array : arrays) {
            len += array.length;
        }

        double[] results = new double[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (double[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    public static char[] mergeArrays(char[] array1, char[]... arrays) {
        if (arrays.length < 1) {
            return array1;
        }

        int len = array1.length;
        for (char[] array : arrays) {
            len += array.length;
        }

        char[] results = new char[len];
        System.arraycopy(array1, 0, results, 0, array1.length);

        int pos = array1.length;
        for (char[] array : arrays) {
            System.arraycopy(array, 0, results, pos, array.length);
            pos += array.length;
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] mergeArrays(E[] a1, E[] a2) {
        if (a2.length == 0) {
            return a1;
        }
        if (a1.length == 0) {
            return a2;
        }

        final Class<?> elementType1 = getComponentClass(a1.getClass());
        final Class<?> elementType2 = getComponentClass(a2.getClass());
        final Class<?> boundElementType = elementType1.isAssignableFrom(elementType2) ? elementType1 : elementType2;

        E[] results = (E[]) create(boundElementType, a1.length + a2.length);
        System.arraycopy(a1, 0, results, 0, a1.length);
        System.arraycopy(a2, 0, results, a1.length, a2.length);
        return results;
    }

    public static <E> E[] mergeArrayAndCollection(E[] array, Collection<E> collection, final Class<E> elementType) {
        return mergeArrayAndCollection(array, collection, new ArrayFactory<E>() {
            @Override
            public E[] create(int len) {
                return Arrays2.create(elementType, len);
            }
        });
    }

    /**
     * @param array
     * @param collection
     * @param factory    目标数组工厂, 应该返回 array 和 collection 元素类型的公共父类类型
     * @param <E>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] mergeArrayAndCollection(E[] array, Collection<E> collection, final ArrayFactory<E> factory) {
        if (null == collection || collection.isEmpty()) {
            return array;
        }

        final E[] array2;
        try {
            array2 = collection.toArray(factory.create(collection.size()));
        } catch (ArrayStoreException e) {
            throw new IllegalStateException("Bad elements in collection: " + collection, e);
        }

        if (array.length < 1) {
            return array2;
        }

        final E[] result = factory.create(array.length + array2.length);
        System.arraycopy(array, 0, result, 0, array.length);
        System.arraycopy(array2, 0, result, array.length, array2.length);
        return result;
    }


    public static int find(boolean[] array, boolean value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(short[] array, short value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(long[] array, long value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(float[] array, float value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(double[] array, double value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static int find(char[] array, char value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i]) {
                return i;
            }
        }
        return -1;
    }

    public static <E> int find(E[] array, E value) {
        for (int i = 0; i < array.length; i++) {
            if (value == array[i] || (null != value && value.equals(array[i]))) {
                return i;
            }
        }
        return -1;
    }

    public static <E> int find(E[] array, E value, Comparator<E> comparator) {
        for (int i = 0; i < array.length; i++) {
            if (0 == comparator.compare(value, array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String toString(final Object array) {
        return toString(array, ",");
    }

    public static String toString(final Object array, final String sep) {
        return toString(array, 0, -1, sep);
    }

    public static String toString(final Object array, final int offset, int len, final String sep) {
        final int length = Array.getLength(array);
        len = 0 > len ? length : len;
        if (offset + len > length) {
            throw new IndexOutOfBoundsException((offset + len) + " must be less than / equals length: " + length);
        }
        final StringBuilder buff = new StringBuilder();
        for (int i = offset; i < offset + len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(Array.get(array, i));
        }
        return buff.toString();
    }

    private static Class<?> getComponentClass(final Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        return Object.class;
    }

    public static void main(String[] args) {
        System.out.println(Arrays2.toString(StringUtils2.tokenizeToArray("1,2,3,4", ",")));
    }
}
