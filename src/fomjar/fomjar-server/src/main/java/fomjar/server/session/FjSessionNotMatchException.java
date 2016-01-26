package fomjar.server.session;

public class FjSessionNotMatchException extends Exception {
    
    private static final long serialVersionUID = 4138632049603657402L;
    
    private String sid;
    
    public FjSessionNotMatchException(String sid) {this.sid = sid;}
    
    public String getSid() {return sid;}

    @Override
    public String getMessage() {return "session does not match: " + getSid() ;}
    
}