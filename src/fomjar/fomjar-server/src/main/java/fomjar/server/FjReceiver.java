package fomjar.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.log4j.Logger;

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
			if (key_num <= 0) return;
			Set<SelectionKey> keys = selector.selectedKeys();
			for (SelectionKey key : keys) {
				if (key.isAcceptable()) {
					SocketChannel conn = ((ServerSocketChannel) key.channel()).accept();
					logger.debug("here comes a connection from: " + conn.getRemoteAddress());
					conn.configureBlocking(false);
					conn.register(selector, SelectionKey.OP_READ);
				} else if (key.isReadable()) {
					key.cancel();
					SocketChannel conn = (SocketChannel) key.channel();
					buf.clear();
					int n = conn.read(buf);
					buf.flip();
					if (0 < n) mq.offer(new FjMessageWrapper(FjServerToolkit.createMessage(Charset.forName("utf-8").decode(buf).toString())).attach("conn", conn));
				}
			}
			keys.clear();
		} catch (Exception e) {logger.error("accept connection from port: " + port() + " failed", e);}
	}

}
