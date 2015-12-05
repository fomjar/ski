package com.wtcrm.stub;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
	
	private static final String SERVER_WCAM = "120.55.195.12";
	private static final int    PORT_WCAM   = 80;
	
	private static final String SERVER_WA   = "120.27.135.230";
	private static final int    PORT_WA     = 3300;
	
	private static void send(String host, int port, String msg) {
		try {
			Socket sock = new Socket(host, port);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			bw.write(msg);
			bw.flush();
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		send(SERVER_WA, PORT_WA, "{\"fs\":\"stub-1\", \"ts\":\"wa\", \"sid\":\"12312312313\", \"ae\":\"ae.taobao.login\"}");
		send(SERVER_WA, PORT_WA, "{\"fs\":\"stub-1\", \"ts\":\"wa\", \"sid\":\"12312312313\", \"ae\":\"ae.taobao.order-list-new\"}");
	}

}
