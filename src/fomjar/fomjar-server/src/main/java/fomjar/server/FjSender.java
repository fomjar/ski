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
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.KeyManager;
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
        FjSenderObserver observer = (FjSenderObserver) wrapper.attachment("observer");
        boolean isSuccess = false;
        if (null != conn) {
            try {
                ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
                while (buf.hasRemaining()) conn.write(buf);
                isSuccess = true;
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
                    isSuccess = true;
                } catch (IOException e) {
                    List<FjServerToolkit.FjAddress> addresses = FjServerToolkit.getSlb().getAddresses(dmsg.ts());
                    for (FjServerToolkit.FjAddress addr : addresses) {
                        if (addr.host.equals(addr0.host) && addr.port == addr0.port) continue;
                        try {
                            conn = SocketChannel.open();
                            conn.connect(new InetSocketAddress(addr0.host, addr0.port));
                            ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
                            while (buf.hasRemaining()) conn.write(buf);
                            isSuccess = true;
                            break;
                        } catch (IOException e1) {logger.warn("try failed of this address: " + addr);}
                    }
                } finally {
                    try {if (null != conn) conn.close();}
                    catch (IOException e) {e.printStackTrace();}
                }
            }
        } else logger.error("unsupported format message: " + msg);
        if (isSuccess) {
            logger.debug("send message success: " + msg);
            try {if (null != observer) observer.onSuccess();}
            catch (Exception e) {e.printStackTrace();}
        } else {
            logger.error("send message failed:" + msg);
            try {if (null != observer) observer.onFail();}
            catch (Exception e) {e.printStackTrace();}
        }
    }

    public void send(FjMessage msg) {
        send(new FjMessageWrapper(msg));
    }
    
    public void send(FjMessageWrapper wrapper) {
        mq.offer(wrapper);
    }
    
    public static FjMessage sendHttpRequest(FjHttpRequest req) {
        HttpURLConnection conn = null;
        FjMessage rsp = null;
        try {
            URL httpurl = new URL(req.url());
            if (req.url().startsWith("https")) {initSslContext();}
            conn = (HttpURLConnection) httpurl.openConnection();
            conn.setRequestMethod(req.method());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type",   req.contentType());
            conn.setRequestProperty("Content-Length", String.valueOf(req.contentLength()));
            OutputStream os = conn.getOutputStream();
            os.write(req.content().getBytes(Charset.forName("utf-8")));
            os.flush();
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] buf = new byte[1024];
            int n = -1;
            while (0 < (n = is.read(buf))) baos.write(buf, 0, n);
            rsp = FjServerToolkit.createMessage(baos.toString("utf-8"));
        } catch (IOException e) {logger.error("error occurs when send http request to url: " + req.url(), e);}
        finally {if (null != conn) conn.disconnect();}
        return rsp;
    }
    
    public static void sendHttpResponse(FjHttpResponse rsp, SocketChannel conn) {
        StringBuffer sb = new StringBuffer();
        sb.append("HTTP/1.1 " + rsp.code() + " OK\r\n");
        sb.append("Content-Type: "   + rsp.contentType() + "; charset=utf-8\r\n");
        sb.append("Content-Length: " + rsp.contentLength() + "\r\n");
        sb.append("\r\n");
        sb.append(rsp.content());
        ByteBuffer buf = ByteBuffer.wrap(sb.toString().getBytes(Charset.forName("utf-8")));
        try {while(buf.hasRemaining()) conn.write(buf);}
        catch (IOException e) {logger.error("error occurs when send http response: " + rsp, e);}
        finally {if (null != conn) try {conn.close();} catch (IOException e) {}}
    }
    
    private static SSLContext sslcontext = null;
    
    private static void initSslContext() {
        if (null != sslcontext) return;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
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
    
    public static abstract class FjSenderObserver {
        public void onSuccess() {}
        public void onFail() {}
    }
    
}
