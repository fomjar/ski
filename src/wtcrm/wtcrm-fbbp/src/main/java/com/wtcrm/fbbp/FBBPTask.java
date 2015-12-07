package com.wtcrm.fbbp;

import org.apache.log4j.Logger;

import com.wtcrm.fbbp.be.TaobaoOrderNew;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class FBBPTask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(FBBPTask.class);
	private BE be_taobao_order_new;
	
	public FBBPTask(String name) {
		be_taobao_order_new = new TaobaoOrderNew(name);
		new OrderGuard(name).start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")
				|| !((FjJsonMsg) msg).json().containsKey("sid")) {
			logger.error("message not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg jmsg = (FjJsonMsg) msg;
		BE be = null;
		if (be_taobao_order_new.match(jmsg)) be = be_taobao_order_new; // 优先判断现存会话
		else be = getBeWithNewSession(jmsg); // 这是业务执行器的一个新会话的开始
		if (null == be) {
			logger.error("can not find a be for this msg: " + jmsg);
			return;
		}
		String sid = jmsg.json().getString("sid");
		boolean end = false;
		try {end = be.execute(jmsg, be.getSession(sid));}
		catch (Exception e) {logger.error("error occurs when execute be for this msg: " + msg, e);}
		if (end) {
			be.removeSession(sid);
			logger.info("session " + sid + " closed");
		} else be.getSession(sid).add(jmsg);
	}
	
	/**
	 * 判断一个新的会话的消息应该用哪一个业务执行器执行
	 * 
	 * @param msg
	 * @return
	 */
	private BE getBeWithNewSession(FjJsonMsg msg) {
		if (msg.json().getString("fs").startsWith("wa")) {
			if (Constant.AE.CODE_SUCCESS == msg.json().getInt("ae-code")) {
				if (msg.json().containsKey("ae-desc")
						&& msg.json().getJSONArray("ae-desc").size() != 0
						&& msg.json().getJSONArray("ae-desc").getJSONObject(0).containsKey("toid")) return be_taobao_order_new;
			} else {
				logger.error("wa ae return error: " + msg);
			}
		}
		return null;
	}

}
