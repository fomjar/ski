package com.ski.fbbp;

import fomjar.server.FjServerToolkit;

public class Main {

	/**
	 * @param args[0] server name
	 * @param args[1] server port
	 */
	public static void main(String[] args) {
		FjServerToolkit.startConfigGuard();
		FjServerToolkit.startServer(args[0]).addServerTask(new FBBPTask(FjServerToolkit.getServer(args[0])));
	}

}