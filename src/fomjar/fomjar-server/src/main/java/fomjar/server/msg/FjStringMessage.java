package fomjar.server.msg;

public class FjStringMessage implements FjMessage {
    
    private String string;
    
    public FjStringMessage(String string) {
        if (null == string) string = "";
        this.string = string;
    }

    @Override
    public String toString() {return string;}

}
