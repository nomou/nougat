package freework.web.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Flash 策略文件服务 - servlet 监听
 *
 * @author vacoor
 */
public class FlashPolicyServerListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(FlashPolicyServerListener.class);
    private static final String DEFAULT_POLICY_CONFIG_LOCATION = "/crossdomain.xml";
    private static final String POLICY_CONFIG_PARAM = "policyConfigLocation";

    private FlashPolicyServer policyServer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String location = context.getInitParameter(POLICY_CONFIG_PARAM);

        location = null != location ? location.trim() : DEFAULT_POLICY_CONFIG_LOCATION;
        InputStream is = context.getResourceAsStream(location);

        try {
            DataInputStream dis = null != is ? new DataInputStream(is) : null;

            if (null != dis) {
                LOG.info("init flash policy server, use {}", location);

                byte[] content = new byte[dis.available()];
                dis.readFully(content);
                dis.close();

                policyServer = new FlashPolicyServer(content);
                policyServer.start();
            } else {
                LOG.warn("can not find flash policy file: {}", location);
            }
        } catch (IOException e) {
            LOG.warn("init flash policy server failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (null != policyServer) {
            LOG.info("shutdown flash policy server.");
            policyServer.shutdown();
        }
        policyServer = null;
    }

    /* ****************************************
     *
     * *************************************** */

    /**
     * Flash 跨站访问策略服务
     */
    private class FlashPolicyServer extends Thread {
        private final int port = 843;
        private final byte[] content;
        private ServerSocket server;
        private boolean running = false;

        public FlashPolicyServer(String crossdomainXml) throws IOException {
            this(crossdomainXml.getBytes(Charset.forName("UTF-8")));
        }

        public FlashPolicyServer(byte[] crossdomainXml) throws IOException {
            this.content = crossdomainXml;
            this.server = new ServerSocket(port);
            this.setDaemon(true);
        }

        public void shutdown() {
            this.running = false;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    final Socket client = server.accept();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (client.getInputStream().available() == 0) {
                                    // do mux
                                }

                                LOG.debug("flash policy server accepted: " + client.getRemoteSocketAddress());
                                LOG.debug("flash policy server sending crossdomain.xml");

                                OutputStream out = client.getOutputStream();
                                out.write(content);
                                out.flush();
                                client.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                LOG.warn("send crossdomain.xml failed", e);
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    LOG.warn("flash policy server exception", e);
                }
            }
        }
    }
}
