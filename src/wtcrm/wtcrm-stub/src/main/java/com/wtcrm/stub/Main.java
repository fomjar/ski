package com.wtcrm.stub;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
	
	private static void send(int port, String json) {
		try {
			Socket sock = new Socket("120.55.195.12", port);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			bw.write(json);
			bw.flush();
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		send(3000, "adfasdfasdfasdf");
	}

}
