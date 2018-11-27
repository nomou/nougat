/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 基本数据类型 - 包装器类型转换工具
 *
 * @author vacoor
 */
public abstract class Primitives {
    private static final Map<String, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE = new HashMap<String, Class<?>>(9);
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE = new HashMap<Class<?>, Class<?>>(9);

    static {
        PRIMITIVE_TO_WRAPPER_TYPE.put(Boolean.TYPE.getName(), Boolean.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Character.TYPE.getName(), Character.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Byte.TYPE.getName(), Byte.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Short.TYPE.getName(), Short.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Integer.TYPE.getName(), Integer.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Long.TYPE.getName(), Long.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Float.TYPE.getName(), Float.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Double.TYPE.getName(), Double.class);
        PRIMITIVE_TO_WRAPPER_TYPE.put(Void.TYPE.getName(), Void.class);

        WRAPPER_TO_PRIMITIVE_TYPE.put(Boolean.class, Boolean.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Character.class, Character.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Byte.class, Byte.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Short.class, Short.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Integer.class, Integer.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Long.class, Long.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Float.class, Float.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Double.class, Double.TYPE);
        WRAPPER_TO_PRIMITIVE_TYPE.put(Void.class, Void.TYPE);
    }

    /**
     * 给定类型是否是基本数据类型的包装器类型
     *
     * @param type
     * @return
     */
    public static boolean isWrapperType(Class<?> type) {
        return WRAPPER_TO_PRIMITIVE_TYPE.containsKey(type);
    }

    /**
     * 获取给定基本数据类型名称对应的包装器类型
     *
     * @param primitive
     * @return
     */
    public static Class<?> wrap(String primitive) {
        return PRIMITIVE_TO_WRAPPER_TYPE.get(primitive);
    }

    /**
     * 获取给定基本数据类型对应的包装器类型
     * <p>
     * 如果给定类型不是基本数据类型则返回给定参数
     */
    public static Class<?> wrap(Class<?> primitive) {
        Class<?> wrapped = wrap(primitive.getName());
        return wrapped != null ? wrapped : primitive;
    }

    /**
     * 返回给定数组中基本数据类型被包装器类型替换后的数组
     *
     * @param primitives
     * @return
     */
    public static Class<?>[] wrap(Class<?>[] primitives) {
        Class<?>[] wrapped = new Class<?>[primitives.length];

        for (int i = 0; i < primitives.length; i++) {
            wrapped[i] = wrap(primitives[i]);
        }
        return wrapped;
    }

    /**
     * 返回给定包装器类型对应的基本数据类型
     *
     * @param wrapper
     * @return
     */
    public static Class<?> unwrap(Class<?> wrapper) {
        Class<?> unwrapped = WRAPPER_TO_PRIMITIVE_TYPE.get(wrapper);
        return unwrapped != null ? unwrapped : wrapper;
    }

    /**
     * 返回给定数组中包装器类型被基本数据类型替换后的数组
     *
     * @param wrappers
     * @return
     */
    public static Class<?>[] unwrap(Class<?>[] wrappers) {
        Class<?>[] unwrapped = new Class<?>[wrappers.length];

        for (int i = 0; i < wrappers.length; i++) {
            unwrapped[i] = unwrap(wrappers[i]);
        }
        return unwrapped;
    }

    /*
    public static boolean[] unwrap(Boolean[] values) {
        return unwrap(values, false);
    }

    public static boolean[] unwrap(Boolean[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static boolean[] unwrap(Collection<Boolean> values, boolean nullToDefault) {
        boolean[] results = new boolean[values.size()];
        int i = 0;
        for (Boolean value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Boolean value is null at " + (i + 1));
                }
                results[i++] = false;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static byte[] unwrap(Byte[] values) {
        return unwrap(values, false);
    }

    public static byte[] unwrap(Byte[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static byte[] unwrap(Collection<Byte> values, boolean nullToDefault) {
        byte[] results = new byte[values.size()];
        int i = 0;
        for (Byte value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Byte value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static short[] unwrap(Short[] values) {
        return unwrap(values, false);
    }

    public static short[] unwrap(Short[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static short[] unwrap(Collection<Short> values, boolean nullToDefault) {
        short[] results = new short[values.size()];
        int i = 0;
        for (Short value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Short value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static int[] unwrap(Integer[] values) {
        return unwrap(values, false);
    }

    public static int[] unwrap(Integer[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static int[] unwrap(Collection<Integer> values, boolean nullToDefault) {
        int[] results = new int[values.size()];
        int i = 0;
        for (Integer value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Integer value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static long[] unwrap(Long[] values) {
        return unwrap(values, false);
    }

    public static long[] unwrap(Long[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static long[] unwrap(Collection<Long> values, boolean nullToDefault) {
        long[] results = new long[values.size()];
        int i = 0;
        for (Long value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Long value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static float[] unwrap(Float[] values) {
        return unwrap(values, false);
    }

    public static float[] unwrap(Float[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static float[] unwrap(Collection<Float> values, boolean nullToDefault) {
        float[] results = new float[values.size()];
        int i = 0;
        for (Float value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Float value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static double[] unwrap(Double[] values) {
        return unwrap(values, false);
    }

    public static double[] unwrap(Double[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static double[] unwrap(Collection<Double> values, boolean nullToDefault) {
        double[] results = new double[values.size()];
        int i = 0;
        for (Double value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Double value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }

    public static char[] unwrap(Character[] values) {
        return unwrap(values, false);
    }

    public static char[] unwrap(Character[] values, boolean nullToDefault) {
        return unwrap(Arrays.asList(values), nullToDefault);
    }

    public static char[] unwrap(Collection<Character> values, boolean nullToDefault) {
        char[] results = new char[values.size()];
        int i = 0;
        for (Character value : values) {
            if (null == value) {
                if (!nullToDefault) {
                    throw new IllegalStateException("Character value is null at " + (i + 1));
                }
                results[i++] = 0;
            } else {
                results[i++] = value;
            }
        }
        return results;
    }
    */

    private Primitives() {
    }
}
