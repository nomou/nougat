package freework.net;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonStructure;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Http utils.
 * <p>
 * By default, 'Orign', 'Host' request header is not allowed to be set,
 * if you need to set up you can use the following code:
 * <code>
 * <pre>System.setProperty("sun.net.http.allowRestrictedHeaders", "true");</pre>
 * </code>
 *
 * @author vacoor
 * @since 1.0
 */
public abstract class Http {
    /**
     * The 'UTF-8' charset.
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * The 'https' schema.
     */
    private static final String HTTPS = "https";

    /**
     * The 'Set-Cookie' response header.
     */
    private static final String SET_COOKIE = "Set-Cookie";

    /**
     * The 'Set-Cookie2' response header.
     */
    private static final String SET_COOKIE2 = "Set-Cookie2";

    /**
     * The default user agent.
     */
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36";


    /**
     * Non-instantiate.
     */
    private Http() {
    }

    /**
     * Opens the http connection using given server url and params.
     *
     * @param serverUrl the http server url
     * @param params    the form data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection get(final String serverUrl, final String... params) throws IOException {
        final String finalUrl = urlAppend(serverUrl, UTF_8.name(), params);
        final HttpURLConnection httpUrlConnection = open(finalUrl, "GET");

        httpUrlConnection.setDoOutput(false);
        return httpUrlConnection;
    }

    /**
     * Posts 'application/x-www-form-urlencoded' data to http connection.
     *
     * @param serverUrl the http server url
     * @param params    the form data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final String serverUrl, final String... params) throws IOException {
        final HttpURLConnection httpUrlConnection = open(serverUrl, "POST");
        return post(httpUrlConnection, params);
    }

    /**
     * Posts 'application/x-www-form-urlencoded' data to http connection.
     *
     * @param serverUrl the http server url
     * @param params    the form data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final String serverUrl, final Map<String, String> params) throws IOException {
        final HttpURLConnection httpUrlConnection = open(serverUrl, "POST");
        return post(httpUrlConnection, params);
    }

    /**
     * Posts data to http connection.
     *
     * @param serverUrl the http server url
     * @param ctype     the Content-Type
     * @param body      the http request body
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final String serverUrl, final String ctype, final String body) throws IOException {
        final HttpURLConnection httpUrlConnection = open(serverUrl, "POST");
        return post(httpUrlConnection, ctype, body);
    }

    /* ************ */

    /**
     * Posts 'application/x-www-form-urlencoded' data to http connection.
     *
     * @param httpUrlConnection the http connection
     * @param params            the form data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final HttpURLConnection httpUrlConnection, final String... params) throws IOException {
        final String charset = UTF_8.name();
        final String ctype = "application/x-www-form-urlencoded;charset=" + charset;
        final String requestBody = buildRawQuery(params);
        return post(httpUrlConnection, ctype, requestBody);
    }

    /**
     * Posts 'application/x-www-form-urlencoded' data to http connection.
     *
     * @param httpUrlConnection the http connection
     * @param params            the form data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final HttpURLConnection httpUrlConnection, final Map<String, String> params) throws IOException {
        final String charset = UTF_8.name();
        final String ctype = "application/x-www-form-urlencoded;charset=" + charset;
        final String requestBody = buildRawQuery(params);
        return post(httpUrlConnection, ctype, requestBody);
    }

    /**
     * Posts 'application/json' data to http connection.
     *
     * @param httpUrlConnection the http connection
     * @param json              the json data
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final HttpURLConnection httpUrlConnection, final JsonStructure json) throws IOException {
        final String charset = "UTF-8";
        final String ctype = "application/json;charset=" + charset;
        return post(httpUrlConnection, ctype, json.toString());
    }


    /**
     * Posts data to http connection.
     *
     * @param httpUrlConnection the http connection
     * @param ctype             the Content-Type
     * @param body              the http request body
     * @return the http connection
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection post(final HttpURLConnection httpUrlConnection, final String ctype, final String body) throws IOException {
        final Charset charset = determineCharset(ctype, UTF_8);

        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Content-Type", ctype);

        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.getOutputStream().write(body.getBytes(charset));
        return httpUrlConnection;
    }

    /**
     * Posts 'multipart/form-data' to the http connection.
     *
     * @param httpUrlConnection the http connection
     * @return the multipart object
     */
    public static Multipart postMultipart(final HttpURLConnection httpUrlConnection) {
        return postMultipart(httpUrlConnection, UTF_8);
    }

    /**
     * Posts 'multipart/form-data' to the http connection.
     *
     * @param httpUrlConnection the http connection
     * @param charset           the charset
     * @return the multipart object
     */
    public static Multipart postMultipart(final HttpURLConnection httpUrlConnection, final Charset charset) {
        final String boundary = "----WebKitFormBoundary_" + System.nanoTime();
        final String ctype = "multipart/form-data;charset=" + charset + ";boundary=" + boundary;

        httpUrlConnection.setRequestProperty("Content-Type", ctype);
        return new Multipart(httpUrlConnection, boundary, charset);
    }

    /* *********************************************
     *             http request methods.
     * ******************************************* */

    /**
     * Builds child http url.
     *
     * @param endpoint the parent http url
     * @param path     the child path
     * @return the http url
     */
    public static String buildUrl(final String endpoint, final String path) {
        if (null == endpoint) {
            return path;
        }
        if (null == path) {
            return endpoint;
        }
        if (endpoint.endsWith("/")) {
            return endpoint + (path.startsWith("/") ? path.substring(1) : path);
        } else {
            return endpoint + (!path.startsWith("/") ? '/' + path : path);
        }
    }

    /**
     * Builds http 'GET' url using given params.
     *
     * @param serverUrl the server url
     * @param charset   the url encode encoding
     * @param params    the query name-value pairs
     * @return the http get url
     */
    public static String urlAppend(final String serverUrl, final String charset, final String... params) {
        final String query = buildQuery((null != charset ? charset : UTF_8.name()), params);
        return serverUrl + (-1 < serverUrl.indexOf('?') ? '&' : '?') + query;
    }

    /**
     * Builds not encoded http query string(n=v&n=v..).
     *
     * @param params the name-value pairs
     * @return the query string
     */
    public static String buildRawQuery(final String... params) {
        return buildQuery(null, params);
    }

    /**
     * Builds not encoded http query string(n=v&n=v..).
     *
     * @param params the name-value pairs
     * @return the query string
     */
    public static String buildRawQuery(final Map<String, String> params) {
        return buildQuery(null, params);
    }

    /**
     * Builds http query string(n=v&n=v..).
     *
     * @param charset the url encode encoding, not encode if null
     * @param params  the name-value pairs
     * @return the query string
     */
    public static String buildQuery(final String charset, final String... params) {
        if (0 != params.length % 2) {
            throw new IllegalArgumentException("params must appear in pairs: key1,value2,key2,value2,...");
        }

        final StringBuilder buff = new StringBuilder();
        if (1 < params.length) {
            for (int i = 0; i < params.length; i += 2) {
                if (0 < i) {
                    buff.append('&');
                }
                if (null != charset) {
                    buff.append(urlEncode(params[i], charset)).append('=').append(urlEncode(params[i + 1], charset));
                } else {
                    buff.append(params[i]).append('=').append(params[i + 1]);
                }
            }
        }
        return buff.toString();
    }

    /**
     * Builds http query string(n=v&n=v..).
     *
     * @param charset the url encode encoding, not encode if null
     * @param params  the name-value pairs
     * @return the query string
     */
    public static String buildQuery(final String charset, final Map<String, String> params) {
        final StringBuilder buff = new StringBuilder();
        int i = 0;
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            if (0 < i) {
                buff.append('&');
            }
            String key = entry.getKey();
            String value = entry.getValue();
            key = null != key ? key : "";
            value = null != value ? value : "";
            if (null != charset) {
                buff.append(urlEncode(key, charset)).append('=').append(urlEncode(value, charset));
            } else {
                buff.append(key).append('=').append(value);
            }
            i++;
        }
        return buff.toString();
    }

    /**
     * Splits query string to map.
     *
     * @param query   the query string
     * @param charset the url encode encoding, not decode if null
     * @return the name-value pairs
     */
    public static Map<String, String> splitQuery(final String query, final String charset) {
        final Map<String, String> result = new HashMap<String, String>();
        final String[] pairs = query.split("&");
        if (pairs.length > 0) {
            for (final String pair : pairs) {
                final String[] param = pair.split("=", 2);
                if (param.length == 2) {
                    if (null != charset) {
                        result.put(urlDecode(param[0], charset), urlDecode(param[1], charset));
                    } else {
                        result.put(param[0], param[1]);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Encodes the text.
     *
     * @param text the text.
     * @param enc  the encoding
     * @return encoded text
     */
    public static String urlEncode(final String text, final String enc) throws UnsupportedCharsetException {
        try {
            return null != text ? URLEncoder.encode(text, enc).replace("%21", "!") : null;
        } catch (final UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(enc);
        }
    }

    /**
     * Decodes the text.
     *
     * @param text the encoded text.
     * @param enc  the encoding
     * @return decoded text
     */
    public static String urlDecode(final String text, final String enc) throws UnsupportedCharsetException {
        try {
            return URLDecoder.decode(text, enc);
        } catch (final UnsupportedEncodingException e) {
            throw new UnsupportedCharsetException(enc);
        }
    }

    /* *********************************************
     *             http response methods.
     * ******************************************* */

    /**
     * Gets the contents of a http connection as a String.
     *
     * @param httpUrlConnection the http connection
     * @return the contents
     * @throws IOException if an I/O error occurs
     */
    public static String getResponseBodyAsString(final HttpURLConnection httpUrlConnection) throws IOException {
        final Charset charset = getResponseCharset(httpUrlConnection, UTF_8);
        final InputStream es = httpUrlConnection.getErrorStream();

        if (null != es) {
            final String msg = getStreamAsString(es, charset);
            if (msg.isEmpty()) {
                throw new IOException(httpUrlConnection.getResponseCode() + ':' + httpUrlConnection.getResponseMessage());
            } else {
                throw new IOException(msg);
            }
        }

        return getStreamAsString(httpUrlConnection.getInputStream(), charset);
    }

    /**
     * Gets the contents of a http connection as a xml {@link Document} using the specified {@link DocumentBuilder}.
     *
     * @param httpUrlConnection the http connection
     * @param builder           the document builder
     *                          may require some feature configuration, eg: XEE attack prevention.
     * @return the xml document
     * @throws IOException if an I/O error occurs
     */
    public static Document getResponseBodyAsXml(final HttpURLConnection httpUrlConnection,
                                                final DocumentBuilder builder) throws IOException {
        final int responseCode = httpUrlConnection.getResponseCode();
        final String contentType = httpUrlConnection.getContentType();
        final String responseBody = getResponseBodyAsString(httpUrlConnection);

        try {
            return builder.parse(new InputSource(new StringReader(responseBody)));
        } catch (final SAXException e) {
            throw new IOException("can not parse xml response, " + responseCode + '(' + contentType + "), response body: " + responseBody, e);
        }
    }

    /**
     * Gets the contents of a http connection as JSR-353 {@link JsonStructure}.
     *
     * @param httpUrlConnection the http connection
     * @return the json
     * @throws IOException if an I/O error occurs
     */
    public static JsonStructure getResponseBodyAsJson(final HttpURLConnection httpUrlConnection) throws IOException {
        final int responseCode = httpUrlConnection.getResponseCode();
        final String contentType = httpUrlConnection.getContentType();
        final String responseBody = getResponseBodyAsString(httpUrlConnection);

        try {
            return Json.createReader(new StringReader(responseBody)).read();
        } catch (final JsonException jex) {
            throw new IOException("can not parse json response, " + responseCode + '(' + contentType + "), response body: " + responseBody, jex);
        }
    }

    /**
     * Gets the contents of a http connection as {@link BufferedImage}.
     *
     * @param httpUrlConnection the http connection
     * @return the {@link BufferedImage}
     * @throws IOException if an I/O error occurs
     */
    public static BufferedImage getResponseBodyAsBufferedImage(final HttpURLConnection httpUrlConnection) throws IOException {
        final int responseCode = httpUrlConnection.getResponseCode();
        final String contentType = httpUrlConnection.getContentType();
        final InputStream es = httpUrlConnection.getErrorStream();

        if (null != es) {
            final Charset charset = getResponseCharset(httpUrlConnection, UTF_8);
            final String msg = getStreamAsString(es, charset);
            if (msg.isEmpty()) {
                throw new IOException(responseCode + '(' + contentType + "):" + httpUrlConnection.getResponseMessage());
            } else {
                throw new IOException(msg);
            }
        }
        return ImageIO.read(httpUrlConnection.getInputStream());
    }

    /**
     * Gets the contents of a stream using given charset.
     *
     * @param stream  the input stream
     * @param charset the charset
     * @return the contents
     * @throws IOException if an I/O error occurs
     */
    private static String getStreamAsString(final InputStream stream, final Charset charset) throws IOException {
        try {
            final Reader reader = new InputStreamReader(stream, charset);
            final StringBuilder response = new StringBuilder();
            final char[] buffer = new char[1024];

            int read;
            // while (0 < (read = reader.read(buffer))) {
            while (-1 < (read = reader.read(buffer))) {
                response.append(buffer, 0, read);
            }
            return response.toString();
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }

    /**
     * Gets response charset of a http connection.
     *
     * @param httpUrlConnection the http connection
     * @param def               the default charset
     * @return response charset if found, otherwise default charset
     */
    public static Charset getResponseCharset(final HttpURLConnection httpUrlConnection, final Charset def) {
        return determineCharset(httpUrlConnection.getContentType(), def);
    }

    /**
     * Gets charset of the 'Content-Type' string.
     *
     * @param ctype the 'Content-Type' string
     * @param def   the default charset
     * @return ctype charset if found, otherwise default charset
     */
    public static Charset determineCharset(final String ctype, final Charset def) {
        Charset charset = def;
        if (null != ctype && !ctype.isEmpty()) {
            final String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim().toLowerCase();
                if (param.startsWith("charset")) {
                    final String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        pair[0] = pair[0].trim();
                        pair[1] = pair[1].trim();
                        if (!pair[0].isEmpty() && !pair[1].isEmpty()) {
                            try {
                                charset = Charset.forName(pair[1]);
                            } catch (final UnsupportedCharsetException ignore) {
                                charset = def;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return charset;
    }

    /**
     * Gets all the applicable cookies from the http url connection response header.
     *
     * @param httpUrlConnection the http url connection.
     * @return the applicable cookies.
     * @see java.net.CookieManager#put(java.net.URI, Map)
     */
    public static List<HttpCookie> getResponseCookies(final HttpURLConnection httpUrlConnection) {
        final URL url = httpUrlConnection.getURL();
        final Map<String, List<String>> headers = httpUrlConnection.getHeaderFields();

        final List<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            final String header = entry.getKey();
            final List<String> values = entry.getValue();
            // RFC 2965 3.2.2, key must be 'Set-Cookie2', we also accept 'Set-Cookie' here for backward compatibility
            if (null == header || (!SET_COOKIE2.equalsIgnoreCase(header) && !SET_COOKIE.equalsIgnoreCase(header))) {
                continue;
            }

            for (final String value : values) {
                List<HttpCookie> cookies;
                try {
                    cookies = HttpCookie.parse(value);
                } catch (final IllegalArgumentException iae) {
                    // Bogus header, make an empty list and log the error
                    continue;
                }

                for (final HttpCookie cookie : cookies) {
                    // If no path is specified, then by default, the path is the directory of the page/doc
                    if (null == cookie.getPath()) {
                        String path = url.getPath();
                        if (!path.endsWith("/")) {
                            final int i = path.lastIndexOf('/');
                            path = 0 < i ? path.substring(0, i + 1) : "/";
                        }
                        cookie.setPath(path);
                    }

                    /*-
                     * As per RFC 2965, section 3.3.1:
                     * Domain  Defaults to the effective request-host.
                     * (Note that because
                     * there is no dot at the beginning of effective request-host,
                     * the default Domain can only domain-match itself.)
                     */
                    if (null == cookie.getDomain()) {
                        String host = url.getHost();
                        if (null != host && !host.contains(".")) {
                            host += ".local";
                        }
                        cookie.setDomain(host);
                    }

                    // TODO check port
                    if (!cookie.hasExpired()) {
                        ret.add(cookie);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Adds the cookie candidates to the http url connection request header.
     *
     * @param httpUrlConnection the http url connection.
     * @param candidates        the cookie candidates.
     * @return the (possibly empty) list of invalid cookies.
     * @see java.net.CookieHandler#put(java.net.URI, Map)
     */
    public static Set<HttpCookie> addRequestCookies(final HttpURLConnection httpUrlConnection, final Set<HttpCookie> candidates) {
        final URL url = httpUrlConnection.getURL();
        final String requestHost = url.getHost();
        final boolean secure = HTTPS.equals(url.getProtocol());

        String requestPath = url.getPath();
        requestPath = null != requestPath ? requestPath : "/";

        final Set<HttpCookie> illegals = new HashSet<HttpCookie>();
        final List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        for (final HttpCookie cookie : candidates) {
            final String domain = null != cookie ? cookie.getDomain() : null;
            final String path = null != cookie ? cookie.getPath() : null;

            /*-
             * apply path-matches rule (RFC 2965 sec. 3.3.4) and check for the possible "secure" tag
             * (i.e. don't send 'secure' cookies over unsecure links)
             */
            if (null == domain || null == path || !requestPath.startsWith(path) || !requestHost.endsWith(domain) || (!secure && cookie.getSecure())) {
                // TODO expired ?
                illegals.add(cookie);
                continue;
            }

            // TODO check port
            // String portlist = cookie.getPortlist();
            cookies.add(cookie);
        }

        // apply sort rule (RFC 2965 sec. 3.3.4)
        Collections.sort(cookies, new Comparator<HttpCookie>() {
            @Override
            public int compare(final HttpCookie c1, final HttpCookie c2) {
                if (c1 == c2) {
                    return 0;
                }
                if (null == c1) {
                    return -1;
                }
                if (null == c2) {
                    return 1;
                }

                // path rule only applies to the cookies with same name
                if (!c1.getName().equals(c2.getName())) {
                    return 0;
                }

                // those with more specific Path attributes precede those with less specific
                if (c1.getPath().startsWith(c2.getPath())) {
                    return -1;
                } else if (c2.getPath().startsWith(c1.getPath())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        final StringBuilder buff = new StringBuilder();
        if (0 < cookies.size() && cookies.get(0).getVersion() > 0) {
            buff.append("$Version=\"1\";");
        }
        for (final HttpCookie cookie : cookies) {
            buff.append(cookie).append("; ");
        }

        httpUrlConnection.addRequestProperty("Cookie", buff.toString());
        return illegals;
    }

    public static String serialize(final HttpCookie cookie) {
        final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        final SimpleDateFormat df = new SimpleDateFormat("EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", Locale.US);
        final long maxAge = cookie.getMaxAge();

        long whenCreated = System.currentTimeMillis();
        try {
            final Field whenCreatedF = HttpCookie.class.getDeclaredField("whenCreated");
            if (!whenCreatedF.isAccessible()) {
                whenCreatedF.setAccessible(true);
            }
            whenCreated = (Long) whenCreatedF.get(cookie);
        } catch (final NoSuchFieldException e) {
            // ignore
        } catch (final IllegalAccessException e) {
            // ignore
        }

        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        df.setTimeZone(cal.getTimeZone());
        df.setLenient(false);
        df.set2DigitYearStart(cal.getTime());
        // _javaeye_cookie_id_=1505802882545536; domain=iteye.com; path=/; expires=Sat, 19-Sep-2020 06:34:42 GMT
        return cookie.getName() + '=' + cookie.getValue() + "; domain=" + cookie.getDomain() + "; path=" + cookie.getPath() + "; expires=" + df.format(new Date(whenCreated + maxAge * 1000));
    }

    public static List<HttpCookie> deserialize(final String header) {
        return HttpCookie.parse(header);
    }

    /* *********************************************
     *             http connection methods.
     * ******************************************* */

    /**
     * Open the server url, trust all hostname and certificate if the url schema is https.
     *
     * @param serverUrl   server url
     * @param method      http request method
     * @param keyManagers https key managers
     * @return HttpURLConnection/HttpsURLConnection instance
     * @throws IOException
     */
    public static HttpURLConnection open(final String serverUrl, final String method,
                                         final KeyManager... keyManagers) throws IOException {
        return open(new URL(serverUrl), method, keyManagers, new TrustManager[]{TRUST_ALL_TRUST_MANAGER});
    }

    /**
     * Open the server url, return {@link HttpsURLConnection} if it is a https url, otherwise {@link HttpURLConnection}.
     *
     * @param serverUrl     server url
     * @param method        http request method
     * @param trustManagers https trust managers
     * @return HttpURLConnection/HttpsURLConnection instance
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection open(final String serverUrl, final String method,
                                         final TrustManager[] trustManagers) throws IOException {
        return open(new URL(serverUrl), method, null, trustManagers);
    }

    /**
     * Open the server url, return {@link HttpsURLConnection} if it is a https url, otherwise {@link HttpURLConnection}.
     *
     * @param serverUrl     server url
     * @param method        http request
     * @param keyManagers   https key managers
     * @param trustManagers https trust managers
     * @return HttpURLConnection/HttpsURLConnection instance
     * @throws IOException if an I/O error occurs
     */
    public static HttpURLConnection open(final URL serverUrl, final String method,
                                         final KeyManager[] keyManagers, final TrustManager[] trustManagers) throws IOException {
        final HttpURLConnection httpUrlConnection = (HttpURLConnection) serverUrl.openConnection();

        try {
            final boolean https = null != keyManagers || null != trustManagers;
            if ((httpUrlConnection instanceof HttpsURLConnection) && https) {
                final HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) httpUrlConnection;
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(keyManagers, trustManagers, new SecureRandom());

                httpsUrlConnection.setSSLSocketFactory(context.getSocketFactory());
                httpsUrlConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(final String hostname, final SSLSession session) {
                        return true;
                    }
                });
            }
        } catch (final Exception e) {
            close(httpUrlConnection);
            throw new IOException(e);
        }

        httpUrlConnection.setRequestMethod(method);

        // TODO httpUrlConnection.setInstanceFollowRedirects(false);
        httpUrlConnection.setRequestProperty("Host", serverUrl.getHost());
        httpUrlConnection.setRequestProperty("User-Agent", DEFAULT_USER_AGENT);
        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
        return httpUrlConnection;
    }

    /*
    public static KeyManager[] x(final InputStream certStream, final String algorithm, final String password) throws Exception {
        // p12 file stream.
        if (null == certStream) {
            throw new IllegalStateException("无法加载证书文件");
        }

        final char[] passwordChars = password.toCharArray();
        final KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(certStream, passwordChars);

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passwordChars);
        return kmf.getKeyManagers();
    }
    */

    /**
     * Close the http url connection, if the connection is not null.
     *
     * @param httpUrlConnection http url connection.
     * @return closed http url connection or null(if given http url connection is null).
     */
    public static HttpURLConnection close(final HttpURLConnection httpUrlConnection) {
        if (null != httpUrlConnection) {
            httpUrlConnection.disconnect();
        }
        return httpUrlConnection;
    }

    /**
     *
     */
    public static class VerisignTrustManager implements X509TrustManager {
        private final Certificate verisign;

        public VerisignTrustManager(final Certificate verisign) {
            this.verisign = verisign;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Exception exp = null;
            for (final X509Certificate cert : chain) {
                /* 验证证书有效期. */
                cert.checkValidity();
                try {
                    /* 验证签名. */
                    cert.verify(verisign.getPublicKey());
                    exp = null;
                    break;
                } catch (Exception e) {
                    exp = e;
                }
            }
            if (null != exp) {
                throw new CertificateException(exp);
            }
        }
    }

    /**
     * Multipart uploader.
     */
    public static class Multipart {
        final HttpURLConnection httpUrlConnection;
        final String boundary;
        final Charset charset;

        public Multipart(final HttpURLConnection httpUrlConnection, final String boundary, final Charset charset) {
            this.httpUrlConnection = httpUrlConnection;
            this.boundary = boundary;
            this.charset = charset;
        }

        public Multipart addTextEntry(final String name, final String value) throws IOException {
            final OutputStream out = httpUrlConnection.getOutputStream();
            final String headers = buildPartHeaders("text/plain", name, null);

            out.write(("\r\n--" + boundary + "\r\n").getBytes(charset));
            out.write(headers.getBytes(charset));
            out.write(value.getBytes(charset));
            return this;
        }

        public Multipart addStreamEntry(final String name, final String filename, final String ctype, final InputStream in, final long length) throws IOException {
            final OutputStream out = httpUrlConnection.getOutputStream();
            final String partHeaders = buildPartHeaders(ctype, name, filename);

            out.write(("\r\n" + boundary + "\r\n").getBytes());
            out.write(partHeaders.getBytes());

            int read;
            int maxRead;
            long alreadyRead = 0;
            final byte[] buffer = new byte[1024];
            while (0 < (maxRead = Math.min(buffer.length, (int) (length - alreadyRead))) && -1 != (read = in.read(buffer, 0, maxRead))) {
                out.write(buffer, 0, read);
                alreadyRead += read;
            }
            return this;
        }

        public HttpURLConnection complete() throws IOException {
            final OutputStream out = httpUrlConnection.getOutputStream();
            final byte[] start = ("\r\n--" + boundary + "--\r\n").getBytes(charset);
            out.write(start);
            return httpUrlConnection;
        }

        private String buildPartHeaders(final String ctype, final String name, final String filename) {
            final String finalCtype = null != ctype ? ctype : "application/octet-stream";
            String header = "Content-Disposition:form-data;name=\"" + name + '"';
            if (null == filename || 1 > filename.length()) {
                header += "\r\n";
            } else {
                header += ";filename=\"" + filename + "\"\r\n";
            }
            return (header + "Content-Type:" + finalCtype + "\r\n\r\n");
        }
    }

    public static final X509TrustManager TRUST_ALL_TRUST_MANAGER = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // nothing.
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // nothing.
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
}
