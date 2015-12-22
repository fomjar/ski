package fomjar.server;

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

import fomjar.util.FjLoopTask;

public class FjSender extends FjLoopTask {
	
	private static final Logger logger = Logger.getLogger(FjSender.class);
	private FjMq mq;
	
	public FjSender() {
		mq = new FjMq();
	}

	public FjMq mq() {
		return mq;
	}

	@Override
	public void perform() {
		FjMsg msg = mq.poll();
		if (null == msg) {
			logger.error("failed to poll message from queue");
			return;
		}
		SocketChannel conn = null;
		if (FjServerToolkit.isLegalMsg(msg)) {
			String ts = ((FjJsonMsg) msg).json().getString("ts");
			FjServerToolkit.FjAddress addr0 = FjServerToolkit.getSlb().getAddress(ts);
			if (null == addr0) {
				logger.error("can not find an address with server name: " + ts);
				return;
			}
			try {
				conn = SocketChannel.open();
				conn.bind(new InetSocketAddress(addr0.host, addr0.port));
				ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
				while (buf.hasRemaining()) conn.write(buf);
				logger.debug("send message successfully: " + msg);
			} catch (IOException e) {
				List<FjServerToolkit.FjAddress> addresses = FjServerToolkit.getSlb().getAddresses(ts);
				boolean isSuccess = false;
				for (FjServerToolkit.FjAddress addr : addresses) {
					if (addr.host.equals(addr0.host) && addr.port == addr0.port) continue;
					try {
						conn = SocketChannel.open();
						conn.bind(new InetSocketAddress(addr0.host, addr0.port));
						ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
						while (buf.hasRemaining()) conn.write(buf);
						isSuccess = true;
						break;
					} catch (IOException e1) {logger.warn("try failed of this address: " + addr);}
				}
				if(!isSuccess) logger.error("send message failed: " + msg);
			} finally {
				try {if (null != conn) conn.close();}
				catch (IOException e) {e.printStackTrace();}
			}
		} else if (null != (conn = mq.pollConnection(msg))) {
			try {
				ByteBuffer buf = ByteBuffer.wrap(msg.toString().getBytes(Charset.forName("utf-8")));
				while (buf.hasRemaining()) conn.write(buf);
				logger.debug("send message successfully: " + msg);
			} catch (IOException e) {logger.error("failed to reply the message: " + msg, e);}
			finally {
				try {if (null != conn) conn.close();}
				catch (IOException e) {e.printStackTrace();}
			}
		} else {
			logger.error("can not find a connection to send message: " + msg);
		}
	}
	
	public void send(FjMsg msg) {
		send(msg, null);
	}
	
	public void send(FjMsg msg, SocketChannel conn) {
		mq.offer(msg, conn);
	}
	
	private byte[] buf;
	
	public synchronized FjMsg sendHttpRequest(String method, String url, String body) {
		HttpURLConnection conn = null;
		FjMsg rsp = null;
		try {
			FjHttpRequest req = new FjHttpRequest(method, url, body);
			URL httpurl = new URL(url);
			if (url.startsWith("https")) {initSslContext();}
			conn = (HttpURLConnection) httpurl.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			OutputStream os = conn.getOutputStream();
			os.write(req.toString().getBytes());
			os.flush();
			InputStream is = conn.getInputStream();
			if (null == buf) buf = new byte[1024 * 1024];
			int n = is.read(buf);
			rsp = FjMsg.create(new String(buf, 0, n));
		} catch (IOException e) {logger.error("error occurs when send http request to url: " + url, e);}
		finally {if (null != conn) conn.disconnect();}
		return rsp;
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
}
