package freework.util;

import freework.function.Condition;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 数组工具类
 *
 * @author vacoor
 */
@SuppressWarnings("unused")
public abstract class Arrays2 {

    public static Object[] asArray(Iterable<?> elements) {
        return asArray(elements, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] asArray(Iterable<? extends E> elements, Class<E> targetElementClass) {
        if (elements instanceof Collection<?>) {
            Collection<?> elems = (Collection<?>) elements;

            if (Object.class.equals(targetElementClass)) {
                return (E[]) elems.toArray();
            }

            E[] result = newArray(targetElementClass, elems.size());
            return (E[]) elems.toArray(result);
        }
        List<E> container = new ArrayList<E>();
        for (E element : elements) {
            container.add(element);
        }
        return asArray(container, targetElementClass);
    }

    /**
     * 获取给定数组的维度数, 如果不是数组, 返回 0
     *
     * @param array 数组
     * @return
     */
    public static int getDimensions(Object array) {
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
        List<E> filtered = new ArrayList<E>(elements.length);
        for (E element : elements) {
            if (checker.value(element)) {
                filtered.add(element);
            }
        }
        return newArray((Class<E>) getComponentClass(elements.getClass()), filtered);
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

        E[] results = (E[]) newArray(boundElementType, a1.length + a2.length);
        System.arraycopy(a1, 0, results, 0, a1.length);
        System.arraycopy(a2, 0, results, a1.length, a2.length);
        return results;
    }

    public static <E> E[] mergeCollections(Collection<E> c1, Collection<E> c2, final Class<E> elementType) {
        return mergeCollections(c1, c2, new ArrayFactory<E>() {
            @Override
            public E[] create(int len) {
                return newArray(elementType, len);
            }
        });
    }

    public static <E> E[] mergeCollections(Collection<E> c1, Collection<E> c2, final ArrayFactory<E> factory) {
        E[] results = factory.create(c1.size() + c2.size());
        int i = 0;
        for (E e : c1) {
            results[i++] = e;
        }
        for (E e : c2) {
            results[i++] = e;
        }
        return results;
    }

    public static <E> E[] mergeArrayAndCollection(E[] array, Collection<E> collection, final Class<E> elementType) {
        return mergeArrayAndCollection(array, collection, new ArrayFactory<E>() {
            @Override
            public E[] create(int len) {
                return newArray(elementType, len);
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

    public static boolean has(boolean[] array, boolean value) {
        return -1 < find(array, value);
    }

    public static boolean has(byte[] array, byte value) {
        return -1 < find(array, value);
    }

    public static boolean has(short[] array, short value) {
        return -1 < find(array, value);
    }

    public static boolean has(int[] array, int value) {
        return -1 < find(array, value);
    }

    public static boolean has(long[] array, long value) {
        return -1 < find(array, value);
    }

    public static boolean has(float[] array, float value) {
        return -1 < find(array, value);
    }

    public static boolean has(double[] array, double value) {
        return -1 < find(array, value);
    }

    public static boolean has(char[] array, char value) {
        return -1 < find(array, value);
    }

    public static <T> boolean has(T[] array, T value) {
        return -1 < find(array, value);
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

    public static boolean[] append(boolean[] array, boolean... newElements) {
        return mergeArrays(array, newElements);
    }

    public static byte[] append(byte[] array, byte... newElements) {
        return mergeArrays(array, newElements);
    }

    public static short[] append(short[] array, short... newElements) {
        return mergeArrays(array, newElements);
    }

    public static int[] append(int[] array, int... newElements) {
        return mergeArrays(array, newElements);
    }

    public static long[] append(long[] array, long... newElements) {
        return mergeArrays(array, newElements);
    }

    public static float[] append(float[] array, float... newElements) {
        return mergeArrays(array, newElements);
    }

    public static double[] append(double[] array, double... newElements) {
        return mergeArrays(array, newElements);
    }

    public static char[] append(char[] array, char... newElements) {
        return mergeArrays(array, newElements);
    }


    public static <E> E[] append(E[] array, E... newElements) {
        return mergeArrays(array, newElements);
    }

    public static boolean[] prepend(boolean[] array, boolean... newElements) {
        return mergeArrays(array, newElements);
    }

    public static byte[] prepend(byte[] array, byte... newElements) {
        return mergeArrays(array, newElements);
    }

    public static short[] prepend(short[] array, short... newElements) {
        return mergeArrays(array, newElements);
    }

    public static int[] prepend(int[] array, int... newElements) {
        return mergeArrays(array, newElements);
    }

    public static long[] prepend(long[] array, long... newElements) {
        return mergeArrays(array, newElements);
    }

    public static float[] prepend(float[] array, float... newElements) {
        return mergeArrays(array, newElements);
    }

    public static double[] prepend(double[] array, double... newElements) {
        return mergeArrays(array, newElements);
    }

    public static char[] prepend(char[] array, char... newElements) {
        return mergeArrays(array, newElements);
    }

    public static <E> E[] prepend(E[] array, E... newElements) {
        return mergeArrays(newElements, array);
    }


    public static boolean[] insert(boolean[] array, int index, boolean... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        boolean[] results = new boolean[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static byte[] insert(byte[] array, int index, byte... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        byte[] results = new byte[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static short[] insert(short[] array, int index, short... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        short[] results = new short[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static int[] insert(int[] array, int index, int... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        int[] results = new int[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static long[] insert(long[] array, int index, long... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        long[] results = new long[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static float[] insert(float[] array, int index, float... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        float[] results = new float[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static double[] insert(double[] array, int index, double... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        double[] results = new double[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static char[] insert(char[] array, int index, char... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        char[] results = new char[len + newElements.length];
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] insert(E[] array, int index, E... newElements) {
        if (newElements.length < 1) {
            return array;
        }

        int len = Math.max(array.length, index);
        int firstLen = Math.min(array.length, index);

        E[] results = (E[]) newArray(getComponentClass(array.getClass()), len + newElements.length);
        System.arraycopy(array, 0, results, 0, firstLen);
        System.arraycopy(newElements, 0, results, index, newElements.length);
        System.arraycopy(newElements, 0, results, newElements.length + index, len - index);
        return results;
    }

    public static String toString(boolean[] array) {
        return toString(array, ",");
    }

    public static String toString(boolean[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(boolean[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(byte[] array) {
        return toString(array, ",");
    }

    public static String toString(byte[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(byte[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(int[] array) {
        return toString(array, ",");
    }

    public static String toString(int[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(int[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(long[] array) {
        return toString(array, ",");
    }

    public static String toString(long[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(long[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset){
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(float[] array) {
        return toString(array, ",");
    }

    public static String toString(float[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(float[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset){
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(double[] array) {
        return toString(array, ",");
    }

    public static String toString(double[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(double[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset){
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static String toString(char[] array) {
        return toString(array, ",");
    }

    public static String toString(char[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static String toString(char[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }


    public static <E> String toString(E[] array) {
        return toString(array, ",");
    }

    public static <E> String toString(E[] array, String sep) {
        return toString(array, 0, array.length, sep);
    }

    public static <E> String toString(E[] array, int offset, int len, String sep) {
        if (offset >= len) {
            throw new IllegalStateException("offset must be less than length");
        }
        StringBuilder buff = new StringBuilder();
        for (int i = offset; i < len; i++) {
            if (i > offset) {
                buff.append(sep);
            }
            buff.append(array[i]);
        }
        return buff.toString();
    }

    public static <E> E[] newArray(E[] original, int newLength) {
        return Arrays.copyOf(original, newLength);
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] newArray(Class<E> elementType, int len) {
        return (E[]) Array.newInstance(elementType, len);
    }

    @SuppressWarnings("unchecked")
    public static <E> E[] newArray(Class<E> elementType, Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return newArray(elementType, (Collection<E>) elements);
        }
        List<E> list = new ArrayList<E>();
        for (E next : elements) {
            list.add(next);
        }
        return newArray(elementType, list);
    }

    public static <E> E[] newArray(Class<E> elementType, Collection<? extends E> collection) {
        if (null == collection) {
            return newArray(elementType, 0);
        }
        E[] elements = newArray(elementType, collection.size());
        return collection.toArray(elements);
    }

    private static Class<?> getComponentClass(Class<?> clazz) {
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        return Object.class;
    }
}
