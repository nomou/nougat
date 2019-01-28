package freework.web.filter;

import freework.web.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import freework.web.ProxiedHttpRequest;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * OWASP AntiSamy 过滤器, 用户过滤用户在HTML中提交的JS恶意代码.
 * <p>
 * OWASP是一个开源的、非盈利的全球性安全组织，致力于应用软件的安全研究.
 * OWASP AntiSamy项目是一个可确保用户输入的HTML/CSS符合应用规范的API, 确保用户无法在HTML中提交恶意代码的API,
 * 而这些恶意代码通常被输入到个人资料、评论等会被服务端存储的数据中。在Web应用程序中，“恶意代码”通常是指 Javascript.
 * </p>
 * OWASP AntiSamy: https://www.owasp.org/index.php/Category:OWASP_AntiSamy_Project.<br>
 * 标准策略文件说明:<br>
 * <p>
 * antisamy-slashdot.xml:
 * Slashdot(http://www.slashdot.org/)是一个提供技术新闻的网站,它允许用户用有限的HTML格式的内容匿名回帖.
 * Slashdot 的安全策略非常严格：用户只能提交下列的 html 标签: b, u, i, a, blockquote, 并且还不支持CSS.
 * </p>
 * <p>
 * antisamy-ebay.xml:
 * 由于eBay允许输入的内容列表包含了比  Slashdot 更多的富文本内容，但是eBay没有公布规则.
 * </p>
 * <p>
 * antisamy-myspace.xml: <br>
 * MySpace(http://www.myspace.com/) 是最流行的一个社交网站之一.用户允许提交几乎所有的他们想用的HTML和CSS,只要不包含JavaScript.
 * MySpace现在用一个黑名单来验证用户输入的HTML,这就是为什么它曾受到Samy蠕虫攻击(http://namb.la/)的原因
 * Samy 蠕虫攻击利用了一个本应该列入黑名单的单词  (eval)  来进行组合碎片攻击的，其实这也是  AntiSamy 立项的原因。
 * </p>
 * <p>
 * antisamy-anythinggoes.xml: <br>
 * 如果你想允许所有有效的HTML和CSS元素输入(但能拒绝  JavaScript 或跟  CSS  相关的网络钓鱼攻击),你可以使用这个策略文件.
 * 它提供了一个很好的参考，因为它包含了对于每个元素的基本规则，所以你在裁剪其它策略文件的时候可以把它作为一个知识库.
 * 策略文件定制: http://www.owasp.org/index.php/AntiSamy_Directives
 * </p>
 *
 * @author vacoor
 * @version 1.0
 * @since 1.0
 */
public class AntiSamyFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AntiSamyFilter.class);
    private static final String DEFAULT_ANTI_SAMY_POLICY = "META-INF/antisamy/antisamy-default.xml";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String SPLASH = "/";

    private Policy policy;
    private String policyLocation;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        super.init();
        WebUtils.configure(this, getFilterConfig(), '-');

        final ClassLoader loader = getClass().getClassLoader();
        InputStream in = null;
        if (null == policy && StringUtils.isBlank(policyLocation)) {
            in = loader.getResourceAsStream(DEFAULT_ANTI_SAMY_POLICY);
            if (null == in) {
                throw new ServletException("No AntiSamy Policy found: no AntiSamy filter policy configure?");
            } else {
                LOGGER.warn("No AntiSamy Policy found, using default policy.");
                policy = loadPolicy(in);
            }
        } else if (null == policy) {
            // 如果是 classpath 资源, 从classpath加载.
            if (policyLocation.startsWith(CLASSPATH_PREFIX) && policyLocation.length() > CLASSPATH_PREFIX.length()) {
                String location = policyLocation.substring(CLASSPATH_PREFIX.length() + 1);

                if (!SPLASH.equals(location) && location.startsWith(SPLASH)) {
                    location = location.substring(1);
                }
                in = loader.getResourceAsStream(location);
            } else {
                // 默认资源.
                in = getServletContext().getResourceAsStream(policyLocation);
            }

            if (null == in) {
                throw new ServletException("AntiSamy Policy not found: " + policyLocation);
            }

            policy = loadPolicy(in);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        super.destroy();
        policy = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(final ServletRequest servletRequest,
                                    final ServletResponse servletResponse,
                                    final FilterChain filterChain)
            throws ServletException, IOException {
        ServletRequest request = servletRequest;
        if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
            final HttpServletRequest httpRequest = WebUtils.toHttp(servletRequest);
            request = new AntiSamyProxiedRequest(httpRequest, getPolicy());
        }

        filterChain.doFilter(request, servletResponse);
    }

    protected Policy loadPolicy(final InputStream in) throws ServletException {
        try {
            return Policy.getInstance(in);
        } catch (final PolicyException e) {
            throw new ServletException("AntiSamy Policy load exception", e);
        } finally {
            try {
                in.close();
            } catch (final IOException ex) {
                // ignore
            }
        }
    }

    /**
     * 获取AntiSamy策略.
     *
     * @return AntiSamyPolicy.
     */
    public Policy getPolicy() {
        return policy;
    }

    /**
     * 设置AntiSamy策略.
     *
     * @param policy AntiSamyPolicy.
     */
    public void setPolicy(final Policy policy) {
        this.policy = policy;
    }

    /**
     * 获取AntiSamy策略文件路径.
     *
     * @return AntiSamy策略文件路径.
     */
    public String getPolicyLocation() {
        return policyLocation;
    }

    /**
     * 设置AntiSamy策略文件路径.
     *
     * @param policyLocation AntiSamy策略文件路径.
     */
    public void setPolicyLocation(final String policyLocation) {
        this.policyLocation = policyLocation;
    }

    /**
     * AntiSamy 请求代理, 该类用于根据给定策略对请求参数进行清理.
     *
     * @author vacoor
     * @version 1.0
     * @since 1.0
     */
    private static class AntiSamyProxiedRequest extends ProxiedHttpRequest {
        private final AntiSamy antiSamy;

        /**
         * Create a AntiSamy proxied http request for clean http parameter.
         *
         * @param request        HttpServletRequest.
         * @param antiSamyPolicy AntiSamy policy.
         */
        public AntiSamyProxiedRequest(final HttpServletRequest request, final Policy antiSamyPolicy) {
            super(request);

            if (null == antiSamyPolicy) {
                throw new IllegalArgumentException("AntiSamyPolicy must be not null");
            }
            antiSamy = new AntiSamy(antiSamyPolicy);
        }

        /**
         * 清理给定的参数值.
         *
         * @param key   参数名称.
         * @param value 参数值.
         * @return 清理后的参数值.
         */
        @Override
        protected String doGetProxiedParameterValue(final String key, final String value) {
            if (null == value) {
                return null;
            }

            String cleaned;
            try {
                /* final CleanResults results = antiSamy.scan(value, AntiSamy.DOM); */
                final CleanResults results = antiSamy.scan(value);
                cleaned = results.getCleanHTML();

                if (0 < results.getNumberOfErrors()) {
                    LOGGER.warn("AntiSamy encountered illegal value for {}, [{}] -> [{}]", key, value, cleaned);
                    LOGGER.warn("AntiSamy encountered problem with input: " + results.getErrorMessages());
                }

            } catch (final ScanException e) {
                cleaned = "";

                LOGGER.error("AntiSamy scan error", e);
            } catch (final PolicyException e) {
                throw new IllegalStateException("AntiSamy policy exception", e);
            }

            return cleaned;
        }
    }
}
