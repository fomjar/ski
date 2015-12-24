package fomjar.server.be;

public class SessionNotOpenException extends Exception {
	
	private static final long serialVersionUID = 4138632049603657402L;
	
	private String sid;
	
	public SessionNotOpenException(String sid) {
		this.sid = sid;
	}
	
	public String getSid() {
		return sid;
	}

	@Override
	public String getMessage() {
		return "session(" + getSid() + ") is not opened for BE";
	}
}