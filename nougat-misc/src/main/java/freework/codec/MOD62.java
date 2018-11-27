package freework.codec;

/**
 * MOD 62 校验和算法.
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class MOD62 {
    private final static String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 计算给定字符串的校验和.
     *
     * @param data 要计算校验和的数据
     * @return 校验和
     */
    public static char mod(final String data) {
        return mod(data.toCharArray());
    }

    /**
     * 计算给定字符数组的校验和.
     *
     * @param chars 要计算校验和的数据
     * @return 校验和
     */
    public static char mod(final char[] chars) {
        return mod(chars, 0, chars.length);
    }

    /**
     * 计算给定字符数组的校验和.
     *
     * @param chars  要计算校验和的数据
     * @param offset 数据偏移量
     * @param len    数据长度
     * @return 校验和
     */
    public static char mod(final char[] chars, final int offset, final int len) {
        // MOD 62 check digit - take the acsii value of each digit, sum them up, divide by 62. the remainder is the check digit (in ascii)
        int sum = 0;
        for (int i = offset; i < offset + len; ++i) {
            sum += CHARSET.indexOf(chars[i]);
        }
        final int remainder = sum % CHARSET.length();
        return CHARSET.charAt(remainder);
    }

    /**
     * 检查给定数据是否正确使用 MOD 62 进行校验和计算.
     *
     * @param value 使用 MOD 62 校验和算法校验的数据
     * @return MOD 62 校验是否通过
     */
    public static boolean check(final String value) {
        boolean valid = false;
        if (value != null && !"".equals(value)) {
            final String code = value.substring(0, value.length() - 1);
            final char checkDigit = value.charAt(value.length() - 1);
            try {
                if (mod(code) == checkDigit) {
                    valid = true;
                }
            } catch (Exception ignore) { /*ignore*/ }
        }
        return valid;
    }

    /**
     * 检查给定数据是否正确使用 MOD 62 进行校验和计算.
     *
     * @param chars  使用 MOD 62 校验和算法校验的数据
     * @param offset 数据便宜量
     * @param len    数据长度
     * @return MOD 62 校验是否通过
     */
    public static boolean check(final char[] chars, final int offset, final int len) {
        boolean valid = false;
        if (chars.length < offset + len) {
            throw new IllegalArgumentException();
        }
        try {
            valid = chars[offset + len - 1] == mod(chars, offset, len - 1);
        } catch (Exception ignore) {
        }
        return valid;
    }

    private MOD62() {
    }
}