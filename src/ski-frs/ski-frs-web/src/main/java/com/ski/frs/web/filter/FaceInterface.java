package com.ski.frs.web.filter;

public class FaceInterface {
    
    static {
        System.loadLibrary("libglog");
        System.loadLibrary("opencv_world300");
        System.loadLibrary("testFace");
        System.loadLibrary("FaceInterface");
    }
    
    public static final int DEVICE_GPU = 0;
    public static final int DEVICE_CPU = 1;
    
    public static native int init(int device);
    public static native int free();
    public static native String fv(String path);

}
