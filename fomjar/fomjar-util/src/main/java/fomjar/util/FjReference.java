package fomjar.util;

public class FjReference <T> {
    
    public volatile T t;
    
    public FjReference(T t) {this.t = t;}

}
