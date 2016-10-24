package fomjar.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fomjar.util.FjLoopTask;
import fomjar.util.FjThreadFactory;

public class FjReceiver extends FjLoopTask {

    private static Selector selector = null;

    private static final Logger         logger  = Logger.getLogger(FjReceiver.class);
    private static final Set<Integer>   alive   = new HashSet<Integer>();
    private static final int            BUF_LEN = 1024;

    static {
        alive.add(80);
        for (int i = 8080; i <= 8089; i++) alive.add(i);
    }

    public static Set<Integer> getAlivePorts() {return alive;}

    private ExecutorService pool;
    private FjMessageQueue mq;
    private int port;
    private ServerSocketChannel sock;
    private ByteBuffer buf;

    public FjReceiver(FjMessageQueue mq) {
        if (null == mq) throw new NullPointerException();
        this.mq = mq;
        this.buf = ByteBuffer.allocate(BUF_LEN);
        this.pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new FjThreadFactory("fjreceiver"));
    }

    public FjReceiver(FjMessageQueue mq, int port) {
        this(mq);
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
        keys.forEach(key->{
            try {
                if (key.isAcceptable()) {
                    SocketChannel conn = ((ServerSocketChannel) key.channel()).accept();
                    logger.debug("here comes a connection from: " + conn.getRemoteAddress());
                    conn.configureBlocking(false);
                    conn.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel conn = (SocketChannel) key.channel();
                    key.cancel();
                    if (port() != conn.socket().getLocalPort()) return;

                    if (alive.contains(port())) readAlive(conn);
                    else readClose(conn);
                }
            } catch (Exception e) {logger.error("accept connection from port: " + port() + " failed", e);}
        });
        keys.clear();
    }

    private void readAlive(SocketChannel conn) {
        pool.submit(()->{
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ByteBuffer buf = ByteBuffer.allocate(BUF_LEN);
                buf.clear();
                int     n = -1;
                long    begin = System.currentTimeMillis();
                while (0 <= (n = conn.read(buf)) && 100 > System.currentTimeMillis() - begin) {
                    if (0 < n) {
                        buf.flip();
                        baos.write(buf.array(), buf.position(), buf.limit());
                        buf.clear();
                        begin = System.currentTimeMillis();
                    }
                }
                String data = baos.toString("utf-8");
                baos.close();

                logger.debug("read raw data is: " + data);
                if (0 < data.length())
                    mq.offer(new FjMessageWrapper(FjServerToolkit.createMessage(data))
                            .attach("conn", conn)
                            .attach("raw", data));
                else conn.close();
            } catch (Exception e) {logger.error("read alive connection failed", e);}
        });
    }

    private void readClose(SocketChannel conn) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buf.clear();
        while (-1 != conn.read(buf)) {
            buf.flip();
            baos.write(buf.array(), buf.position(), buf.limit());
            buf.clear();
        }
        String data = baos.toString("utf-8");
        baos.close();

        logger.debug("read raw data is: " + data);
        if (0 < data.length())
            mq.offer(new FjMessageWrapper(FjServerToolkit.createMessage(data))
                    .attach("conn", conn)
                    .attach("raw", data));
        else conn.close();
    }
}
