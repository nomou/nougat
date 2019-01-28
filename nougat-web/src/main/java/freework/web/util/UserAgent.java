package freework.web.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 浏览器 UserAgent 工具类
 * <p/>
 * 看到一个更 N 的 http://blog.jjonline.cn/phptech/168.html
 *
 * @author vacoor
 */
public class UserAgent {
    // 引擎类型
    public static final String WEBKIT = "webkit";
    public static final String GECKO = "gecko";
    public static final String PRESTO = "presto";

    // 浏览器类型
    public static final String CHROME = "chrome";
    public static final String SAFARI = "safari";
    public static final String FIREFOX = "firefox";
    public static final String OPERA = "opera";
    public static final String OPERA_MINI = "opera-mini";
    public static final String MSIE = "msie";

    /**
     * 原始信息
     */
    private final String browser;
    private final String revision;
    private final String engine;
    private final boolean mobile;   // 是否是手机

    public UserAgent(String browser, String revision, String engine, boolean mobile) {
        this.browser = browser;
        this.revision = revision;
        this.engine = engine;
        this.mobile = mobile;
    }

    public String getBrowser() {
        return browser;
    }

    public String getRevision() {
        return revision;
    }

    public String getEngine() {
        return engine;
    }

    public boolean isMobile() {
        return mobile;
    }

    public boolean isGecko() {
        return GECKO.equals(engine);
    }

    public boolean isWebkit() {
        return WEBKIT.equals(engine);
    }

    /*
    public boolean isPresto() {
        return PRESTO.equals(engine);
    }
    */

    public boolean isIE() {
        return MSIE.equals(browser);
    }

    public boolean isChrome() {
        return CHROME.equals(browser);
    }

    public boolean isFirefox() {
        return FIREFOX.equals(browser);
    }

    public boolean isSafari() {
        return SAFARI.equals(browser);
    }

    public static UserAgent parse(final String ua) {
        String browser = "unknown";
        String revision = "unknown";
        String engine = "unknown";
        boolean mobile = false;

        if (StringUtils.isBlank(ua)) {
            return new UserAgent(browser, revision, engine, mobile);
        }


        // 浏览器信息解析
        Matcher m = WEBKIT_PATTERN.matcher(ua);
        if (m.find()) {     // webkit
            engine = WEBKIT;
            // String engineRevision = m.group(1);
            if ((m = CHROME_PATTERN.matcher(ua)).find()) {          // chrome
                browser = CHROME;
                revision = m.group(1);
            } else if ((m = SAFARI_PATTERN.matcher(ua)).find()) {   // safari
                browser = SAFARI;
                revision = m.group(1);
            }

            /*
            if (APPLE_MOBILE_PATTERN.matcher(ua).find()) {               // iPad, iPhone or iPod Touch
                String mobile = "Apple";
            } else if ((m = OTHER_MOBILE_PATTERN.matcher(ua)).find()) {     // other webkit mobile browser
                String mobile = m.group(1);
            }
            */
        } else if ((m = GECKO_PATTERN.matcher(ua)).find()) {        // gecko
            engine = GECKO;

            // Gecko detected, look for revision
            // if ((m = GECKO_REVISION_PATTERN.matcher(ua)).find()) {
            //  String revision = m.group(1);
            // }

            if ((m = FIREFOX_PATTERN.matcher(ua)).find()) {         // firefox
                browser = FIREFOX;
                revision = m.group(1);
            }
        } else if ((m = OPERA_PATTERN.matcher(ua)).find()) {        // opera
            browser = OPERA;
            revision = m.group(1);

            if ((m = OPERA_MINI_PATTERN.matcher(ua)).find()) {
                browser = OPERA_MINI;
                revision = m.group(1);     // ex: Opera Mini/2.0.4509/1316
            }
        } else if ((m = MSIE_PATTERN.matcher(ua)).find()) {
            browser = MSIE;
            revision = m.group(1);
        }

        // 操作系统信息解析

        // 设备类型解析
        mobile = (WEBKIT.equals(engine) && APPLE_MOBILE_PATTERN.matcher(ua).find()) || OTHER_MOBILE_PATTERN.matcher(ua).find();
            /*
            if(ua.contains("iPod") || ua.contains("iPhone")) {
            } else if (ua.contains("iPad")) {
            }
            */
        return new UserAgent(browser, revision, engine, mobile);
    }

    public static UserAgent parse(HttpServletRequest request) {
        return parse(request.getHeader("User-Agent"));
    }


    private static void parseOS(final String ua) {
        if (ua.contains("Windows")) {
            /**
             * Windows 系列
             * Windows NT 6.2   -   Windows 8
             * Windows NT 6.1   -   Windows 7
             * Windows NT 6.0   -   Windows Vista
             * Windows NT 5.2   -   Windows Server 2003; Windows XP x64 Edition
             * Windows NT 5.1   -   Windows XP
             * Windows NT 5.01  -   Windows 2000, Service Pack 1 (SP1)
             * Windows NT 5.0   -   Windows 2000
             * Windows NT 4.0   -   Microsoft Windows NT 4.0
             * Windows 98; Win 9x 4.90  -   Windows Millennium Edition (Windows Me)
             * Windows 98   -   Windows 98
             * Windows 95   -   Windows 95
             * Windows CE   -   Windows CE
             * 判断依据:http://msdn.microsoft.com/en-us/library/ms537503(v=vs.85).aspx
             */
            if (ua.contains("Windows NT 6.2")) {                // windows 8

            } else if (ua.contains("Windows NT 6.1")) {         // windows 7

            } else if (ua.contains("Windows NT 6.0")) {         // windows vista

            } else if (ua.contains("Windows NT 5.2")) {         // windows xp x64 edition or windows server 2003
                String version = ua.contains("Win64") ? "XP 64bit" : "Server 2003";
            } else if (ua.contains("Windows NT 5.1")) {         // windows xp

            } else if (ua.contains("Windows NT 5.0.1")) {       // windows 2000 sp1

            } else if (ua.contains("Windows NT 5.0") || ua.contains("Windows 2000")) {  // windows 2000

            } else if (ua.contains("Windows NT 4.0") || ua.contains("WinNT4.0")) {      // windows NT 4.0

            } else if (ua.contains("Windows 98")) {               // windows 98

            } else if (ua.contains("Windows 95")) {                // windows 95

            } else if (ua.contains("Windows ME") || ua.contains("Win 9x 4.90")) {      // windows ME

            } else if (ua.contains("Windows CE")) {               // windows CE

            } else if (ua.contains("Windows Phone")) {           // windows phone

            } else {
                // windows unknow
            }
        } else if (ua.contains("Mac OS X")) {
            /**
             * Apple
             * iPod -       Mozilla/5.0 (iPod; U; CPU iPhone OS 4_3_1 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8G4 Safari/6533.18.5
             * iPad -       Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10
             * iPad2    -   Mozilla/5.0 (iPad; CPU OS 5_1 like Mac OS X; en-us) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9B176 Safari/7534.48.3
             * iPhone 4 -   Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7
             * iPhone 5 -   Mozilla/5.0 (iPhone; CPU iPhone OS 5_0 like Mac OS X) AppleWebKit/534.46 (KHTML, like Gecko) Version/5.1 Mobile/9A334 Safari/7534.48.3
             * 判断依据:http://www.useragentstring.com/pages/Safari/
             * 参考:http://stackoverflow.com/questions/7825873/what-is-the-ios-5-0-user-agent-string
             * 参考:http://stackoverflow.com/questions/3105555/what-is-the-iphone-4-user-agent
             */
            if (ua.contains("iPad;")) {
            } else if (ua.contains("iPhone;")) {
            } else if (ua.contains("iPod")) {
            } else {
            }
        }
    }

    private static final Pattern WEBKIT_PATTERN = Pattern.compile("AppleWebKit\\/([\\d.]*)");
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome\\/([\\d.]*)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("\\/([\\d.]*) Safari");
    private static final Pattern APPLE_MOBILE_PATTERN = Pattern.compile(" Mobile\\/");
    private static final Pattern OTHER_MOBILE_PATTERN = Pattern.compile("NokiaN[^\\/]*|Android \\d\\.\\d|webOS\\/\\d\\.\\d");

    private static final Pattern OPERA_PATTERN = Pattern.compile("Opera\\/.* Version\\/([\\d.]*)");
    private static final Pattern OPERA_MINI_PATTERN = Pattern.compile("Opera Mini[^;]*");

    private static final Pattern MSIE_PATTERN = Pattern.compile("MSIE\\s([^;]*)");

    private static final Pattern GECKO_PATTERN = Pattern.compile("Gecko");
    private static final Pattern GECKO_REVISION_PATTERN = Pattern.compile("rv:([\\d.]*)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox\\/([\\d.]*)");
}
