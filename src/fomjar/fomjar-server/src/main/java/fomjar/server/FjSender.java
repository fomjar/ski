package fomjar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.util.FjLoopTask;

public class FjSender extends FjLoopTask {

    private static final Logger logger = Logger.getLogger(FjSender.class);
    private static int TIMEOUT_I = 1000 * 60;
    private static int TIMEOUT_O = 1000 * 3;
    private FjMessageQueue mq;

    public FjSender() {
        mq = new FjMessageQueue();
    }

    public FjMessageQueue mq() {
        return mq;
    }

    @Override
    public void perform() {
        FjMessageWrapper wrapper = mq.poll();
        if (null == wrapper) {
            logger.error("failed to poll message from queue");
            return;
        }
        FjMessage msg = wrapper.message();
        SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        if (null != conn) {
            try {
                ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
                while (buf.hasRemaining()) conn.write(buf);
            } catch (IOException e) {logger.error("failed to send message through an exist connection: " + msg, e);}
            finally {
                try {if (null != conn) conn.close();}
                catch (IOException e) {e.printStackTrace();}
            }
        } else if (msg instanceof FjDscpMessage) {
            FjDscpMessage dmsg = (FjDscpMessage) msg;
            FjServerToolkit.FjAddress addr0 = FjServerToolkit.getSlb().getAddress(dmsg.ts());
            if (null == addr0) logger.error("can not find an address with server name: " + dmsg.ts());
            else {
                try {
                    conn = SocketChannel.open();
                    conn.connect(new InetSocketAddress(addr0.host, addr0.port));
                    ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
                    while (buf.hasRemaining()) conn.write(buf);
                } catch (IOException e) {
                    FjServerToolkit.FjAddress addr = null;
                    while (!addr0.equals(addr = FjServerToolkit.getSlb().getAddress(dmsg.ts()))) {
                        try {
                            conn = SocketChannel.open();
                            conn.connect(new InetSocketAddress(addr.host, addr.port));
                            ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
                            while (buf.hasRemaining()) conn.write(buf);
                            break;
                        } catch (IOException e1) {logger.warn("try failed of this address: " + addr);}
                    }
                } finally {
                    try {if (null != conn) conn.close();}
                    catch (IOException e) {e.printStackTrace();}
                }
            }
        } else logger.error(String.format("unsupported format message, class: %s, content: %s", msg.getClass().getName(), msg));
    }

    public void send(FjMessage msg) {
        send(new FjMessageWrapper(msg));
    }

    public void send(FjMessageWrapper wrapper) {
        mq.offer(wrapper);
    }

    public static FjMessage sendHttpRequest(FjHttpRequest req) {
        return sendHttpRequest(req, TIMEOUT_I);
    }

    public static FjMessage sendHttpRequest(FjHttpRequest req, int timeout) {
        logger.debug("send http request:\n" + req);
        
        HttpURLConnection conn = null;
        FjMessage rsp = null;
        try {
            URL httpurl = new URL(req.url());
            if (req.url().startsWith("https")) initSslContext();
            conn = (HttpURLConnection) httpurl.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            conn.setRequestMethod(req.method());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type",   req.contentType());
            conn.setRequestProperty("Content-Length", String.valueOf(req.contentLength()));
            if (0 < req.contentLength()) {
                OutputStream os = conn.getOutputStream();
                os.write(req.content());
                os.flush();
            }
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] buf = new byte[1024];
            int n = -1;
            while (0 <= (n = is.read(buf))) baos.write(buf, 0, n);
            rsp = FjServerToolkit.message(baos.toString("utf-8"));
        } catch (IOException e) {logger.error("error occurs when send http request to url: " + req.url(), e);}
        finally {if (null != conn) conn.disconnect();}
        return rsp;
    }

    public static void sendHttpResponse(FjHttpResponse rsp, SocketChannel conn) {
        sendHttpResponse(rsp, conn, TIMEOUT_O);
    }

    public static void sendHttpResponse(FjHttpResponse rsp, SocketChannel conn, int timeout) {
        logger.debug("send http response:\n" + rsp);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(rsp.toString().getBytes("utf-8"));
            baos.write(rsp.content());
            baos.flush();

            ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
            long begin = System.currentTimeMillis();
            while(buf.hasRemaining() && timeout > System.currentTimeMillis() - begin) {
                int n = conn.write(buf);
                if (0 < n) begin = System.currentTimeMillis();
                else {
                    try {Thread.sleep(100L);}
                    catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        } catch (IOException e) {logger.error("error occurs when send http response: " + rsp, e);}
        finally {if (null != conn) try {conn.close();} catch (IOException e) {e.printStackTrace();}}
    }

    private static SSLContext sslcontext = null;

    private static void initSslContext() {
        if (null != sslcontext) return;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[]{new DefaultTrustManager()}, null);
            SSLContext.setDefault(sslcontext);
        } catch (GeneralSecurityException e) {logger.error("init ssl context failed!", e);}
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {return null;}
    }

}
