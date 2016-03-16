package fomjar.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.log4j.Logger;

import fomjar.util.FjFixedTask;
import fomjar.util.FjLoopTask;

public class FjReceiver extends FjLoopTask {
    
    private static Selector selector = null;
    
    private static final Logger logger = Logger.getLogger(FjReceiver.class);
    private static final int BUF_LEN = 1024 * 1024;
    private FjMessageQueue mq;
    private int port;
    private ServerSocketChannel sock;
    private ByteBuffer buf;
    
    public FjReceiver(FjMessageQueue mq) {
        if (null == mq) throw new NullPointerException();
        this.mq = mq;
        buf = ByteBuffer.allocate(BUF_LEN);
    }
    
    public FjReceiver(FjMessageQueue mq, int port) {
        if (null == mq) throw new NullPointerException();
        this.mq = mq;
        buf = ByteBuffer.allocate(BUF_LEN);
        reset(port);
    }
    
    public FjMessageQueue mq() {return mq;}

    public int port() {return port;}
    
    public void reset(int port) {
        try {if (null != sock) sock.close();}
        catch (IOException e) {logger.error("colse old server socket channel failed", e);}
        try {
            sock = ServerSocketChannel.open();
            sock.configureBlocking(false);
            sock.bind(new InetSocketAddress(port));
            if (null == selector) selector = Selector.open();
            else selector.wakeup();
            sock.register(selector, SelectionKey.OP_ACCEPT);
            this.port = port;
        } catch (IOException e) {
            logger.error("open new port: " + port + " failed", e);
            this.port = -1;
        }
    }
    
    @Override
    public void perform() {
        try {
            int key_num = selector.select();
            if (key_num <= 0) {
                try {Thread.sleep(100L);}
                catch (InterruptedException e) {e.printStackTrace();}
                return;
            }
        } catch (IOException e) {e.printStackTrace();}
            Set<SelectionKey> keys = selector.selectedKeys();
            keys.forEach((key)->{
                try {
                    if (key.isAcceptable()) {
                        SocketChannel conn = ((ServerSocketChannel) key.channel()).accept();
                        logger.debug("here comes a connection from: " + conn.getRemoteAddress());
                        conn.configureBlocking(false);
                        conn.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        SocketChannel conn = (SocketChannel) key.channel();
                        if (port() != conn.socket().getLocalPort()) return;
                        
                        key.cancel();
                        buf.clear();
                        int n = conn.read(buf);
                        buf.flip();
                        String data = Charset.forName("utf-8").decode(buf).toString();
                        logger.debug("read raw data is: " + data);
                        if (0 < n)
                            mq.offer(new FjMessageWrapper(FjServerToolkit.createMessage(data))
                                    .attach("conn", conn)
                                    .attach("raw", data));
                    }
                } catch (Exception e) {logger.error("accept connection from port: " + port() + " failed", e);}
            });
            keys.clear();
    }
    
    public static InputStream receive(int port, long timeout) throws IOException {
        ServerSocket server = new ServerSocket(port);
        InputStreamWrapper isw = new InputStreamWrapper();
        new FjFixedTask(timeout) {
            @Override
            public void perform() {
                try {isw.is = server.accept().getInputStream();}
                catch (IOException e) {logger.error("receive data from port: " + port + " failed", e);}
            }
            @Override
            protected void onTimeOut() {
                logger.error("receive data from port " + port + " timeout for " + timeout + "milliseconds");
                try {server.close();}
                catch (IOException e) {e.printStackTrace();}
            }
        }.run();
        return isw.is;
    }
    
    private static class InputStreamWrapper {
        public InputStream is = null;
    }
    
}
