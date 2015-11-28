package com.wtcrm.am;

import fomjar.server.FjToolkit;

public class Main {

	/**
	 * @param args[0] server name
	 * @param args[1] server port
	 */
	public static void main(String[] args) {
		FjToolkit.loadConfig();
		FjToolkit.startServer("wcam-1").addServerTask(new WCAMTask());
		FjToolkit.startServer("tbam-1").addServerTask(new TBAMTask());
	}

}
