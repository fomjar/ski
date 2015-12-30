package com.ski.wca.be;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.wca.WechatInterface;

import fomjar.server.FjServer;
import fomjar.server.be.FjBusinessExecutor;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjDscpResponse;

public class DefaultBE extends FjBusinessExecutor {
	
	private static final Logger logger = Logger.getLogger(DefaultBE.class);

	public DefaultBE(FjServer server) {
		super(server);
	}

	@Override
	public void execute(FjSCB scb, FjDscpMessage msg) {
		String        user_from = scb.getString("user_from");
		String        user_to   = scb.getString("user_to");
		String        content   = ((FjDscpResponse) msg).desc().toString();
		SocketChannel conn      = (SocketChannel) scb.get("conn");
		WechatInterface.sendResponse(user_to, user_from, content, conn);
		logger.debug("response wechat user: " + user_from + " with content: " + content);
		scb.end();
	}

}