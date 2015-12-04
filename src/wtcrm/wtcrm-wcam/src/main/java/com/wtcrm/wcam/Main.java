package com.wtcrm.wcam;

import fomjar.server.FjToolkit;

public class Main {

	/**
	 * @param args[0] server name
	 * @param args[1] server port
	 */
	public static void main(String[] args) {
		FjToolkit.startConfigGuard();
		FjToolkit.startServer("wcam-1").addServerTask(new WCAMTask());
		AccessTokenGuard.getInstance().start();
	}

}
