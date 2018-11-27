package freework.codec;

/**
 * ISO7064:1983.MOD11-2 算法
 * <p>
 * 校验码:   根据前十七位数字按照ISO 7064:1983.MOD 11-2校验码计算得出<br>
 * 计算规则:<br>
 * 1.前17位数分别乘以不同的系数.<br>
 * 分别为：7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4 2<br>
 * 2.将这17位数字与其系数相乘的结果相加。<br>
 * 3.用加出来和除以11，看余数是多少<br>
 * 4.余数只可能有0 1 2 3 4 5 6 7 8 9 10这11个数字.<br>
 * 其分别对应的最后一位身份证的号码为1 0 X 9 8 7 6 5 4 3 2。<br>
 * eg: 余数是2，第18位数字为X,余数是10，第18位数字为2。
 *
 * @author vacoor
 */
public abstract class MOD11_2 {
    private static final int[] FACTORS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3};  // 每位加权因子((int)Math.pow(2, 18 - i) % 11)) i=1,2,3
    private static final int[] CHECKSUMS = new int[]{1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2}; // 余数校验码

    /**
     * 计算给定的整型数组的 MOD11-2 运算的校验和
     *
     * @param array 需要进行 MOD11-2 运算的整型数组
     * @return MOD11-2 校验和
     */
    public static int mod(int[] array) {
        int sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i] * FACTORS[i % FACTORS.length];
        }
        return CHECKSUMS[sum % 11];
    }

    /**
     * 计算给定的整型字符串序列的 MOD11-2 校验和字符
     * <p/>
     * 对于校验和为10, 返回 X, 其他返回数字的字符形式
     *
     * @param data 整型字符串序列
     * @return 校验和字符
     */
    public static char mod(String data) {
        int sum = 0;
        for (int i = 0; i < data.length(); i++) {
            // 每位数字 * 加权因子
            sum += (data.charAt(i) - 48) * FACTORS[i % FACTORS.length];
        }
        int checksum = CHECKSUMS[sum % 11];
        return 10 == checksum ? 'X' : (char) (checksum + 48);
    }

    /**
     * 校验给定的整型字符串序列是否是经过 MOD11-2 校验
     *
     * @param value 需要校验的整形字符串数组
     */
    public static boolean check(String value) {
        boolean valid = false;
        if (null != value && !"".equals(value)) {
            String raw = value.substring(0, value.length() - 1);
            char checksum = value.charAt(value.length() - 1);
            try {
                valid = mod(raw) == checksum;
            } catch (Exception ignore) {
            }
        }
        return valid;
    }

    private MOD11_2() {
    }
}
