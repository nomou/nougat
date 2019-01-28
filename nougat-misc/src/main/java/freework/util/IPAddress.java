package freework.util;

/**
 * This class represents an IP address represented by an 32 bits integer value. Dotted-decimal notation divides the
 * 32-bit Internet address into four 8-bit (byte) fields and specifies the value of each field independently as a
 * decimal number with the fields separated by dots :<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;10010001 . 00001010 . 00100010 . 00000011<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;145&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;10&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;34
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;-> 145.10.34.3<br/>
 * </code> <br/>
 * <br/>
 * IP address are classified into three classes :<br/>
 * <br/>
 * class A:<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;1&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;7&nbsp;8&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------+------------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |0&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------+------------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <-- network number -->&nbsp;<------- host number --------><br/>
 * </code> <br/>
 * <br/>
 * class B:<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;2&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15&nbsp;16&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------------+------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |10|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +--+-------------------------+------------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <----- network number ----->&nbsp;<---- host number -----><br/>
 * </code> <br/>
 * <br/>
 * class C:<br/>
 * <br/>
 * <code>
 * &nbsp;&nbsp;&nbsp;&nbsp;bit#&nbsp;&nbsp;&nbsp;0&nbsp;&nbsp;&nbsp;3&nbsp;
 * &nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;23&nbsp;24
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;31<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +---+-----------------------------+-------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * |110|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * +---+-----------------------------+-------------------+<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <------- network  number -------->&nbsp;<-- host number --><br/>
 * </code> <br/>
 * <br/>
 *
 * @author vacoor
 */
public class IPAddress {
    private final int address;

    private IPAddress(int address) {
        this.address = address;
    }

    /**
     * 获取 IP 的十进制形式, 如果需要显示为正整数,可以 0xFFFFFFFFL & getAddress()
     *
     * @return IP 的十进制形式
     */
    public int getAddress() {
        return address;
    }

    public final byte[] toBytes() {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((address >> 24 & 0xff));
        bytes[1] = (byte) (((address & 0xffffff) >> 16 & 0xff));
        bytes[2] = (byte) (((address & 0xffff) >> 8 & 0xff));
        bytes[3] = (byte) ((address & 0xff));
        return bytes;
    }

    public String toHex() {
        byte[] bytes = toBytes();
        StringBuilder buff = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF).toUpperCase();
            if (2 > hex.length()) {
                buff.append('0');
            }
            buff.append(hex);
        }
        return buff.toString();
    }

    public final boolean isClassA() {
        return (address & 0x00000001) == 0;
    }

    public final boolean isClassB() {
        return (address & 0x00000003) == 1;
    }

    public final boolean isClassC() {
        return (address & 0x00000007) == 3;
    }

    public boolean in(IPRange range) {
        return null != range && range.isIPAddressInRange(this);
    }

    @Override
    public int hashCode() {
        return this.address;
    }

    @Override
    public boolean equals(Object another) {
        return another instanceof IPAddress && address == ((IPAddress) another).address;
    }

    public String toString() {
        final byte[] bytes = toBytes();
        return (bytes[0] & 0xFF) + "." + (bytes[1] & 0xFF) + "." + (bytes[2] & 0xFF) + "." + (bytes[3] & 0xFF);
    }

    public static IPAddress parse(String ipStr) {
        return new IPAddress(parseIPStr(ipStr));
    }

    private static int parseIPStr(String ipStr) {
        if (null == ipStr || 0 == ipStr.length()) {
            throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
        }

        final String[] pair = ipStr.split("\\.", -1);
        try {
            // decimal ip
            if (1 == pair.length) {
                int address = (int) Long.parseLong(pair[0]);
                if (0 > address) {
                    throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
                }
                return address;
            }

            // net address . host address
            if (2 == pair.length) {
                int net = Integer.parseInt(pair[0]);
                int host = Integer.parseInt(pair[1]);

                if (0 > net || net > 0xFF || 0 > host) {
                    throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
                }
                return ((net & 0xff) << 24) | (host & 0xff0000) | (host & 0xff00) | (host & 0xff);
            }

            // net address.subnet address.host address
            if (3 == pair.length) {
                int address = 0;
                int t;
                for (int i = 0; i < 2; ++i) {
                    t = Integer.parseInt(pair[i]);
                    if (0 > t || 0xff < t) {
                        throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
                    }
                    address |= ((t & 0xff) << (24 - 8 * i));
                }

                t = Integer.parseInt(pair[2]);
                if (0 > t || 0xffffL < t) {
                    throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
                }
                return address | ((t & 0xff00) | (t & 0xff));
            }

            // decimal dot address
            if (4 == pair.length) {
                int address = 0;
                for (int i = 0; i < 4; ++i) {
                    int t = Integer.parseInt(pair[i]);
                    if (0 > t || 0xFF < t) {
                        return -1;
                    }
                    address |= ((t & 0xffL) << (24 - 8 * i));
                }
                return address;
            }

            throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
        } catch (NumberFormatException ignore) {
            throw new IllegalArgumentException("Can't parse IP address: " + ipStr);
        }
    }
}