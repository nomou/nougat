/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

/**
 * JVM 工具类
 */
@SuppressWarnings({"unused"})
public abstract class JVM {
    /**
     * JVM 规范版本
     */
    private static final float MAJOR_VERSION;

    /**
     * 供应商
     */
    private static final String VENDOR = System.getProperty("java.vm.vendor");

    private static final String RUNTIME = System.getProperty("java.runtime.name");

    private final boolean SUPPORTS_AWT = loadClass("java.awt.Color") != null;
    private final boolean SUPPORTS_SWING = loadClass("javax.swing.LookAndFeel") != null;
    private final boolean SUPPORTS_SQL = loadClass("java.sql.Date") != null;

    static {
        float majorVersion = -1;
        try {
            majorVersion = Float.parseFloat(System.getProperty("java.specification.version"));
        } catch (NumberFormatException ignore) {
        }
        MAJOR_VERSION = majorVersion;
    }

    public static float getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static String getVendor() {
        return VENDOR;
    }

    public static boolean isOneDotSeven() {
        return MAJOR_VERSION >= 1.7F && MAJOR_VERSION < 1.8F;
    }

    public static boolean isOneDotSix() {
        return MAJOR_VERSION >= 1.6F && MAJOR_VERSION < 1.7F;
    }

    public static boolean isOneDotFive() {
        return MAJOR_VERSION >= 1.5F && MAJOR_VERSION < 1.6F;
    }

    public static boolean isOneDotFour() {
        return MAJOR_VERSION >= 1.4F && MAJOR_VERSION < 1.5F;
    }

    public static boolean isOrLater(float version) {
        return MAJOR_VERSION >= version;
    }

    public static boolean is64Bit() {
        String model = System.getProperty("sun.arch.data.model", System.getProperty("com.ibm.vm.bitmode"));
        if (model != null) {
            return "64".equals(model);
        }
        return false;
    }

    public static boolean isIcedTea() {
        return RUNTIME.indexOf("IcedTea") != -1;
    }

    public static boolean isOpenJDK() {
        return RUNTIME.indexOf("OpenJDK") != -1;
    }


    // vendor
    public static boolean isSun() {
        return VENDOR.indexOf("Sun") != -1;
    }

    public static boolean isApple() {
        return VENDOR.indexOf("Apple") != -1;
    }

    public static boolean isHPUX() {
        return VENDOR.indexOf("Hewlett-Packard Company") != -1;
    }

    public static boolean isIBM() {
        return VENDOR.indexOf("IBM") != -1;
    }

    public static boolean isBlackdown() {
        return VENDOR.indexOf("Blackdown") != -1;
    }

    public static boolean isDiablo() {
        return VENDOR.indexOf("FreeBSD Foundation") != -1;
    }

    public static boolean isHarmony() {
        return VENDOR.indexOf("Apache Software Foundation") != -1;
    }

    /*
     * Support for sun.misc.Unsafe and sun.reflect.ReflectionFactory is present
     * in JRockit versions R25.1.0 and later, both 1.4.2 and 5.0 (and in future
     * 6.0 builds).
     */
    public static boolean isBEAWithUnsafeSupport() {
        // This property should be "BEA Systems, Inc."
        if (VENDOR.indexOf("BEA") != -1) {

            /*
             * Recent 1.4.2 and 5.0 versions of JRockit have a java.vm.version
             * string starting with the "R" JVM version number, i.e.
             * "R26.2.0-38-57237-1.5.0_06-20060209..."
             */
            String vmVersion = System.getProperty("java.vm.version");
            if (vmVersion.startsWith("R")) {
                /*
                 * We *could* also check that it's R26 or later, but that is
                 * implicitly true
                 */
                return true;
            }

            /*
             * For older JRockit versions we can check java.vm.info. JRockit
             * 1.4.2 R24 -> "Native Threads, GC strategy: parallel" and JRockit
             * 5.0 R25 -> "R25.2.0-28".
             */
            String vmInfo = System.getProperty("java.vm.info");
            if (vmInfo != null) {
                // R25.1 or R25.2 supports Unsafe, other versions do not
                return (vmInfo.startsWith("R25.1") || vmInfo
                        .startsWith("R25.2"));
            }
        }
        // If non-BEA, or possibly some very old JRockit version
        return false;
    }

    public static boolean isOracle() {
        return VENDOR.indexOf("Oracle") != -1;
    }

    public static boolean isHitachi() {
        return VENDOR.indexOf("Hitachi") != -1;
    }

    public static boolean isSAP() {
        return VENDOR.indexOf("SAP AG") != -1;
    }

    public boolean supportsAWT() {
        return SUPPORTS_AWT;
    }

    public boolean supportsSwing() {
        return SUPPORTS_SWING;
    }

    public boolean supportsSql() {
        return SUPPORTS_SQL;
    }

    //
    private Class loadClass(String name) {
        try {
            Class clazz = Class.forName(name, false, getClass().getClassLoader());
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private JVM() {
    }
}
