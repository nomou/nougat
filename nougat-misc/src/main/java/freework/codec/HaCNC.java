package freework.codec;


/**
 * 河南联通宽带帐号加密算法.
 *
 * @author vacoor
 */
public abstract class HaCNC {
    public static final String DIGITS = "9012345678abcdeABCDEFGHIJKLMNfghijklmnUVWXYZxyzuvwopqrstOPQRST";
    public static final int[] MASKS = {17, 52, 201, 35, 117, 24, 215, 226, 18, 53, 41, 43, 236, 182, 35, 25};
    public static final int CACHE = 37;
    public static final String INV = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 将河南联通宽带帐号转换为PPPoE帐号
     *
     * @param input 宽带账号 eg: 037966666666
     * @return PPPoE 账号(无2:前缀) eg: 2v49tdIngbJq --&gt; 实际 PPPoE 账号需要追加"2:"前缀 2:2v49tdIngbJq
     */
    public static String encode(String input) {
        StringBuilder buff = new StringBuilder();
        String dict = DIGITS;
        int cache = CACHE;
        int l;
        for (int i = 0; i < input.length(); i++) {
            for (int j = 0; j < dict.length(); j++) {
                if (input.charAt(i) == dict.charAt(j)) {
                    l = (((MASKS[i & 0xF]) ^ (3 * cache)) ^ (i * 5)) + j;
                    buff.append(dict.charAt(l % dict.length()));
                    cache = cache ^ (l % dict.length() + 9433);
                    break;
                }
            }
            if (buff.charAt(i) == ' ') {
                //dict[i] = i;
                dict = dict.substring(0, i) + i + dict.substring(i, dict.length());
            }
        }
        return buff.toString();
    }

    /**
     * 将PPPoE账号还原为宽带账号
     *
     * @param encoded 加密后的 PPPoE 账号(去除"2:"前缀) eg: 2:2v49tdIngbJq 应该为 2v49tdIngbJq
     * @return 原始宽带账号 eg: 037966666666
     */
    public static String decode(String encoded) {
        String result = "";
        for (int j = 0; j < encoded.length(); j++) {
            for (int i = 0; i < INV.length(); i++) {
                String tmp = result + INV.charAt(i);
                if (encoded.substring(0, j + 1).equals(encode(tmp))) {
                    result = tmp;
                    break;
                }
            }
        }
        return result;
    }

    /* *************************************************
     *
     * ************************************************/

    private static final String ENCODED_PREFIX = "2:";

    public static void main(String[] args) {
        // args = new String[]{"-encode", "037966666666"};
        // args = new String[]{"-decode", "2:2v49tdIngbJq"};
        if (2 != args.length) {
            displayHelp();
            System.exit(1);
        }

        if ("-encode".equals(args[0])) {
            System.out.println(ENCODED_PREFIX + encode(args[1]));
        } else if ("-decode".equals(args[0])) {
            String encoded = args[1];
            if (encoded.startsWith(ENCODED_PREFIX)) {
                System.out.println(decode(encoded.substring(ENCODED_PREFIX.length())));
            } else {
                System.err.println("无效的加密 PPPoE 账号, 加密 PPPoE 账号应该以 " + ENCODED_PREFIX + " 开始");
                System.exit(1);
            }
        } else {
            displayHelp();
            System.exit(1);
        }
    }

    public static void displayHelp() {
        String name = HaCNC.class.getSimpleName();
        System.out.println("Usage: java " + name + " option source");
        System.out.println("options:");
        System.out.println("   -encode: from Telephone to PPPoE");
        System.out.println("   -decode: from PPPoE to Telephone");
        System.out.println();
        System.out.println("java " + name + " -encode 037966666666");
        System.out.println("java " + name + " -decode 2:2v49tdIngbJq");
    }
}
/*-
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
    <title></title>
</head>
<body>
<div id="encoder">
    <input id="source" name="source" type="text">
    Result: <span id="result"></span>
    <hr>
    <button id="encode">encode(To PPPoE)</button>
    <br>
    <button id="decode">decode(To Telephone)</button>
</div>
<script type="text/javascript">
    function encode(source) {
        var result = '';
        var dic = '9012345678abcdeABCDEFGHIJKLMNfghijklmnUVWXYZxyzuvwopqrstOPQRST';
        var mask = [17, 52, 201, 35, 117, 24, 215, 226, 18, 53, 41, 43, 236, 182, 35, 25];
        var cache = 37;

        var l = 0;
        for (var i = 0; i < source.length; i++) {
            for (var j = 0; j < dic.length; j++) {
                if (source[i] == dic[j]) {
                    l = (((mask[i & 0xF]) ^ (3 * cache)) ^ (i * 5)) + j;
                    result += dic[l % dic.length];
                    cache ^= l % dic.length + 9433;
                    break;
                }
            }
            if (result[i] == '') {
                dic[i] = i;
            }
        }
        return '2:' + result;
    }

    function decode(source) {
        var dics = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var result = '';
        for (var j = 0; j < source.length; j++) {
            for (var i = 0; i < dics.length; i++) {
                var tmp = result + dics[i];
                if (source.substring(0, j + 1) == encode(tmp)) {
                    result = tmp;
                    break;
                }
            }
        }
        return result;
    }

        document.getElementById("encode").onclick = function () {
        document.getElementById("result").innerHTML = encode(document.getElementById("source").value.trim());
        }

        document.getElementById("decode").onclick = function () {
        document.getElementById("result").innerHTML = decode(document.getElementById("source").value.trim());
        }
</script>
</body>
</html>
 */
