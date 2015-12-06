package fomjar.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class FjReceiver extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(FjReceiver.class);
	private static final int BUF_LEN = 1024 * 1024;
	private FjMq mq;
	private int port;
	private ServerSocket sock;
	private byte[] buf;
	
	public FjReceiver(FjMq mq) {
		if (null == mq) throw new NullPointerException();
		this.mq = mq;
		sock = null;
		buf = new byte[BUF_LEN];
	}
	
	public FjReceiver(FjMq mq, int port) {
		if (null == mq) throw new NullPointerException();
		this.mq = mq;
		sock = null;
		buf = new byte[BUF_LEN];
		reset(port);
	}
	
	public FjMq mq() {
		return mq;
	}

	public int port() {
		return port;
	}
	
	public void reset(int port) {
		try {if (null != sock) sock.close();}
		catch (IOException e) {logger.error("close old port: " + port() + " failed", e);}
		try {
			sock = new ServerSocket(port);
			this.port = port;
		} catch (IOException e) {logger.error("open new port: " + port() + " failed", e);}
	}

	@Override
	public void close() {
		super.close();
		try {if (null != sock) sock.close();}
		catch (IOException e) {logger.error("close port: " + port() + " failed", e);}
	}

	@Override
	public void perform() {
		try {
			Socket conn = sock.accept();
			logger.debug("here comes a connection from: " + conn.getInetAddress().getHostAddress() + ":" + conn.getPort());
			int n = conn.getInputStream().read(buf);
			if (0 < n) mq.offer(FjMsg.create(new String(buf, 0, n)), conn);
		} catch (Exception e) {logger.error("accept connection from port: " + port() + " failed", e);}
	}

}
