package freework.util;

import freework.codec.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>A globally unique identifier for objects.</p>
 * <p>
 * <p>Consists of 12 bytes, divided as follows:</p>
 * <table border="1">
 * <caption>ObjectID layout</caption>
 * <tr>
 * <td>0</td><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td><td>8</td><td>9</td><td>10</td><td>11</td>
 * </tr>
 * <tr>
 * <td colspan="4">time</td><td colspan="3">machine</td> <td colspan="2">pid</td><td colspan="3">inc</td>
 * </tr>
 * </table>
 * <p>
 * <p>Instances of this class are immutable.</p>
 * <p>
 * 修改自 MongoDB ObjectId
 * http://docs.mongodb.org/manual/reference/object-id/
 * https://github.com/mongodb/mongo-java-driver/blob/756008a01c11771abef5797f60d381231fb07c69/src/main/org/bson/types/ObjectId.java
 */
public class Oid implements Comparable<Oid>, java.io.Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(Oid.class);

    private final int time;
    private final int machine;
    private final int inc;


    /**
     * Gets a new object id.
     *
     * @return the new id
     */
    public static Oid get() {
        return new Oid();
    }

    /**
     * Checks if a string could be an {@code ObjectId}.
     *
     * @param hexOid a potential ObjectId as a String.
     * @return whether the string could be an object id
     * @throws IllegalArgumentException if hexString is null
     */
    public static boolean isValid(String hexOid) {
        if (hexOid == null)
            return false;

        final int len = hexOid.length();
        if (len != 24)
            return false;

        for (int i = 0; i < len; i++) {
            char c = hexOid.charAt(i);
            if (c >= '0' && c <= '9')
                continue;
            if (c >= 'a' && c <= 'f')
                continue;
            if (c >= 'A' && c <= 'F')
                continue;

            return false;
        }

        return true;
    }

    /**
     * Turn an object into an {@code ObjectId}, if possible. Strings will be converted into {@code ObjectId}s, if possible, and
     * {@code ObjectId}s will be cast and returned.  Passing in {@code null} returns {@code null}.
     *
     * @param o the object to convert
     * @return an {@code ObjectId} if it can be massaged, null otherwise
     */
    public static Oid massageToObjectId(Object o) {
        if (o == null)
            return null;

        if (o instanceof Oid)
            return (Oid) o;

        if (o instanceof String) {
            String s = o.toString();
            if (isValid(s))
                return new Oid(s);
        }

        return null;
    }

    /* *********************************
     *          Constructor
     * *********************************/

    /**
     * Constructs a new instance using the given date.
     *
     * @param time the date
     */
    public Oid(Date time) {
        this(time, GEN_MACHINE, COUNTER.getAndIncrement());
    }

    /**
     * Constructs a new instances using the given date and counter.
     *
     * @param time the date
     * @param inc  the counter
     * @throws IllegalArgumentException if the high order byte of counter is not zero
     */
    public Oid(Date time, int inc) {
        this(time, GEN_MACHINE, inc);
    }

    /**
     * Constructs an ObjectId using  time, machine and inc values.  The Java driver has done it this way for a long time, but it does not
     * match the <a href="http://docs.mongodb.org/manual/reference/object-id/">ObjectId specification</a>, which requires four values, not
     * three.  The next major release of the Java driver will conform to this specification, but will still need to support clients that are
     * relying on the current behavior.  To that end, this constructor is now deprecated in favor of the more explicit factory method
     * ObjectId#createFromLegacyFormat(int, int, int)}, and in the next major release this constructor will be removed.
     *
     * @param time    the date
     * @param machine the machine identifier
     * @param inc     the counter
     *                <a href="http://docs.mongodb.org/manual/reference/object-id/">ObjectId specification</a>. Please
     */
    public Oid(Date time, int machine, int inc) {
        this.time = (int) (time.getTime() / 1000);
        this.machine = machine;
        this.inc = inc;
    }

    /**
     * Creates a new instance from a string.
     *
     * @param s the string to convert
     * @throws IllegalArgumentException if the string is not a valid id
     */
    public Oid(String s) {

        if (!isValid(s)) {
            throw new IllegalArgumentException("invalid ObjectId [" + s + "]");
        }

        byte b[] = new byte[12];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) java.lang.Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        ByteBuffer bb = ByteBuffer.wrap(b);
        time = bb.getInt();
        machine = bb.getInt();
        inc = bb.getInt();
    }

    /**
     * Constructs an ObjectId given its 12-byte binary representation.
     *
     * @param b a byte array of length 12
     */
    public Oid(byte[] b) {
        if (b.length != 12) {
            throw new IllegalArgumentException("need 12 bytes");
        }
        ByteBuffer bb = ByteBuffer.wrap(b);
        time = bb.getInt();
        machine = bb.getInt();
        inc = bb.getInt();
    }

    /**
     * Create a new object id.
     */
    public Oid() {
        time = (int) (System.currentTimeMillis() / 1000);
        machine = GEN_MACHINE;
        inc = COUNTER.getAndIncrement();
    }

    @Override
    public int hashCode() {
        int x = time;
        x += (machine * 111);
        x += (inc * 17);
        return x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        Oid other = massageToObjectId(o);
        if (other == null)
            return false;

        return
                time == other.time &&
                        machine == other.machine &&
                        inc == other.inc;
    }

    /**
     * Convert to a byte array.  Note that the numbers are stored in big-endian order.
     *
     * @return the byte array
     */
    public byte[] toBytes() {
        byte b[] = new byte[12];
        ByteBuffer bb = ByteBuffer.wrap(b);
        // by default BB is big endian like we need
        bb.putInt(time);
        bb.putInt(machine);
        bb.putInt(inc);
        return b;
    }

    /**
     * Converts this instance into a 24-byte hexadecimal string representation.
     *
     * @return a string representation of the ObjectId in hexadecimal format
     */
    public String toHex() {
        final StringBuilder buf = new StringBuilder(24);

        for (final byte b : toBytes()) {
            buf.append(String.format("%02x", b & 0xff));
        }

        return buf.toString();
    }

    public String toBase64() {
        return Base64.encodeToString(toBytes());
    }

    public String toString() {
        return toHex();
    }


    private int compareUnsigned(int i, int j) {
        long li = 0xFFFFFFFFL;
        li = i & li;
        long lj = 0xFFFFFFFFL;
        lj = j & lj;
        long diff = li - lj;
        if (diff < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        if (diff > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) diff;
    }

    public int compareTo(Oid id) {
        if (id == null)
            return -1;

        int x = compareUnsigned(time, id.time);
        if (x != 0)
            return x;

        x = compareUnsigned(machine, id.machine);
        if (x != 0)
            return x;

        return compareUnsigned(inc, id.inc);
    }

    /**
     * Gets the timestamp (number of seconds since the Unix epoch).
     *
     * @return the timestamp
     */
    public int getTimestamp() {
        return time;
    }

    /**
     * Gets the timestamp as a {@code Date} instance.
     *
     * @return the Date
     */
    public Date getDate() {
        return new Date(time * 1000L);
    }

    /**
     * Gets the current value of the auto-incrementing counter.
     *
     * @return the current counter value.
     */
    public static int getCurrentCounter() {
        return COUNTER.get();
    }

    /* *******************************************
     *
     * *******************************************/

    private static final int GEN_MACHINE;
    private static final AtomicInteger COUNTER = new AtomicInteger((new java.util.Random()).nextInt());

    static {
        try {
            // build a 2-byte machine piece based on NICs info
            int machinePiece;
            {
                try {
                    StringBuilder sb = new StringBuilder();
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        NetworkInterface ni = e.nextElement();
                        sb.append(ni.toString());
                    }
                    machinePiece = sb.toString().hashCode() << 16;
                } catch (Throwable e) {
                    // exception sometimes happens with IBM JVM, use random
                    LOG.warn(e.getMessage(), e);

                    machinePiece = (new Random().nextInt()) << 16;
                }
                LOG.trace("machine piece post: " + Integer.toHexString(machinePiece));
            }

            // add a 2 byte process piece. It must represent not only the JVM but the class loader.
            // Since static var belong to class loader there could be collisions otherwise
            final int processPiece;
            {
                int processId = new java.util.Random().nextInt();
                try {
                    processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
                } catch (Throwable ignore) { /* ignore */ }

                ClassLoader loader = Oid.class.getClassLoader();
                int loaderId = loader != null ? System.identityHashCode(loader) : 0;

                StringBuilder buff = new StringBuilder();
                buff.append(Integer.toHexString(processId));
                buff.append(Integer.toHexString(loaderId));
                processPiece = buff.toString().hashCode() & 0xFFFF;
            }

            LOG.trace("process piece: " + Integer.toHexString(processPiece));

            GEN_MACHINE = machinePiece | processPiece;

            LOG.trace("machine : " + Integer.toHexString(GEN_MACHINE));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
