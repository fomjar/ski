package fomjar.server;

public class FjServerMain {
	
	public static void main(String[] args) {
		FjServerToolkit.startConfigMonitor();
		FjServerToolkit.startServer(args[0]);
		FjServerContainer.getInstance().register(args[0], "task");
	}


}
