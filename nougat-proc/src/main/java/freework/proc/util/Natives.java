package freework.proc.util;

import com.sun.jna.Platform;

import java.io.File;

/**
 * Created by vacoor on 2018/11/25.
 */
public class Natives {
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();

    public static final String MACOSX = "macosx";
    public static final String WINDOWS = "windows";
    public static final String LINUX = "linux";
    public static final String FREEBSD = "freebsd";
    public static final String OPENBSD = "freebsd";

    public String getPlatformFolderName() {
        if (Platform.isMac()) {
            return MACOSX;
        } else if (Platform.isWindows()) {
            return WINDOWS;
        } else if (Platform.isLinux()) {
            return LINUX;
        } else if (Platform.isFreeBSD()) {
            return FREEBSD;
        } else if (Platform.isOpenBSD()) {
            return OPENBSD;
        } else {
            throw new IllegalStateException("Platform " + Platform.getOSType() + " is not supported");
        }
    }

    public String getPlatformArchFolderName() {
        return isWindowsXp() ? "xp" : Platform.is64Bit() ? "x86_64" : "x86";
    }

    private static boolean isWindowsXp() {
        return Platform.isWindows() && (OS_VERSION.equals("5.1") || OS_VERSION.equals("5.2"));
    }

    public static final String RESOURCE_PREFIX = "freework/proc/native";

    public static final String [] LOCATIONS = {

    };

    private void doInitialize(final String platformFolderName, final String platformArchFolderName) {
        final String prefix = platformArchFolderName + "/" + platformArchFolderName + "/";
        for (final String location : LOCATIONS) {
            if (location.startsWith(prefix)) {
                // copy
            }
        }
    }

    private void copy(final String location, final File destDir) {
        final String finalLocation = RESOURCE_PREFIX + "/" + location;
    }
}
