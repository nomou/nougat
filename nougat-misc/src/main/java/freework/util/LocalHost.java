package freework.util;

import freework.function.Condition;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author vacoor
 */
public abstract class LocalHost {

    public static boolean isAvailablePort(int port) {
        boolean available = true;
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.close();
        } catch (IOException e) {
            available = false;
        }
        return available;
    }

    public static String getHostName() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            return host.getHostName();
        } catch (UnknownHostException e) {
            return Throwables.unchecked(e);
        }
    }

    public static String getHostAddress() {
        byte[] bytes = getAddress();
        return (bytes[0] & 0xff) + "." + (bytes[1] & 0xff) + "." + (bytes[2] & 0xff) + "." + (bytes[3] & 0xff);
    }

    public static Set<String> getInet4HostAddresses() {
        return getHostAddresses(getInet4Addresses());
    }

    public static Set<String> getInet6HostAddresses() {
        return getHostAddresses(getInet6Addresses());
    }

    public static Set<String> getHostAddresses() {
        return getHostAddresses(getAddresses());
    }

    public static Set<String> getHostAddresses(Set<InetAddress> addresses) {
        Set<String> addrs = new HashSet<String>();
        for (InetAddress address : addresses) {
            addrs.add(address.getHostAddress());
        }
        return addrs;
    }

    public static String getHostNetworkInterfaceHardwareAddress() {
        return getHostNetworkInterfaceHardwareAddress('-');
    }

    public static String getHostNetworkInterfaceHardwareAddress(char sep) {
        return macToHex(getNetworkInterfaceHardwareAddress(), sep);
    }

        /* *************************

         */

    public static byte[] getAddress() {
        try {
            return InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            return Throwables.unchecked(e);
        }
    }

    public static byte[] getNetworkInterfaceHardwareAddress() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(host);
            return ni.getHardwareAddress();
        } catch (SocketException e) {
            return Throwables.unchecked(e);
        } catch (UnknownHostException e) {
            return Throwables.unchecked(e);
        }
    }

    public static Set<InetAddress> getInet4Addresses() {
        return getAddresses(new Condition<InetAddress>() {
            @Override
            public boolean value(InetAddress value) {
                return value instanceof Inet4Address;
            }
        });
    }

    public static Set<InetAddress> getInet6Addresses() {
        return getAddresses(new Condition<InetAddress>() {
            @Override
            public boolean value(InetAddress value) {
                return value instanceof Inet6Address;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static Set<InetAddress> getAddresses() {
        return getAddresses(Condition.TRUE);
    }

    public static Set<InetAddress> getAddresses(Condition<InetAddress> checker) {
        Set<InetAddress> localhost = new LinkedHashSet<InetAddress>();
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (null == checker || checker.value(addr)) {
                        localhost.add(addr);
                    }
                }
            }
        } catch (SocketException e) {
            localhost = Throwables.unchecked(e);
        }
        return localhost;
    }

    private static String macToHex(byte[] macBytes, char sep) {
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < macBytes.length; i++) {
            buff.append(i > 0 ? sep : "").append(toHex(macBytes[i]));
        }
        return buff.toString();
    }

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int SHIFT = 4;
    private static final int MASK = (1 << SHIFT) - 1;

    private static String toHex(byte b) {
        return (DIGITS[(b >>> SHIFT) & MASK]) + "" + (DIGITS[b & MASK]);
    }

    private LocalHost() {
    }
}