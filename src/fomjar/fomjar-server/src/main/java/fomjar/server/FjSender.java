package fomjar.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import org.apache.log4j.Logger;

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
			logger.error("failed to poll msg from queue");
			return;
		}
		Socket sock = null;
		if (msg instanceof FjJsonMsg && ((FjJsonMsg) msg).json().containsKey("ts")) {
			String ts = ((FjJsonMsg) msg).json().getString("ts");
			FjToolkit.FjAddress address = FjToolkit.getAddress(ts);
			if (null == address) {
				logger.error("can not find an address with server name: " + ts);
				return;
			}
			try {
				sock = new Socket(address.host, address.port);
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
				writer.write(msg.toString());
				writer.flush();
				logger.debug("send message successfully: " + msg);
			} catch (IOException e) {
				logger.error("failed to send the msg: " + msg + " with server name: " + ts, e);
				List<FjToolkit.FjAddress> addresses = FjToolkit.getAddresses(ts);
				logger.error("now try other addresses with the same module category: " + addresses);
				boolean isSuccess = false;
				for (FjToolkit.FjAddress addr : addresses) {
					try {
						sock = new Socket(addr.host, addr.port);
						BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
						writer.write(msg.toString());
						writer.flush();
						isSuccess = true;
						logger.error("try successfully of this address: " + addr);
						break;
					} catch (IOException e1) {logger.warn("try failed of this address: " + addr);}
				}
				if(!isSuccess) logger.error("there is no other available address");
			} finally {
				try {if (null != sock) sock.close();}
				catch (IOException e) {e.printStackTrace();}
			}
		} else {
			try {
				sock = msg.conn();
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "UTF-8"));
				writer.write(msg.toString());
				writer.flush();
				logger.debug("send message successfully: " + msg);
			} catch (IOException e) {logger.error("failed to reply the msg: " + msg, e);}
			finally {
				try {if (null != sock) sock.close();}
				catch (IOException e) {e.printStackTrace();}
			}
		}
	}
	
	public void send(FjMsg msg) {
		msg.markSending();
		mq.offer(msg);
	}
	
}
