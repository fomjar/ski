package com.ski.frs.web.filter;

public class FaceInterface {
    
    static {
        System.loadLibrary("FaceInterface");
    }
    
    public static final int DEVICE_GPU = 0;
    public static final int DEVICE_CPU = 1;
    
    /** 成功 */
    public static final int SUCCESS             = 0;
    /** 加载分类器失败 */
    public static final int ERROR_LOAD_FILTER   = 1;
    /** 缓存为空 */
    public static final int ERROR_BUFFER_EMPTY  = 2;
    /** 未初始化 */
    public static final int ERROR_NO_INIT       = 3;
    /** 没有对应索引的GPU设备 */
    public static final int ERROR_NO_DEV        = 4;
    /** GPU设备已经用光，能力不足 */
    public static final int ERROR_DISABLE       = 5;
    /** 没有人脸 */
    public static final int ERROR_NO_FACE       = 6;
    
    public static native int initInstance(int device);
    public static native String fv(int instance, String path);
    public static native int freeInstance(int instance);
    
//    public static void main(String[] args) {
//        int instance = initInstance(DEVICE_GPU);
//        System.out.println(String.format("init = 0x%X", instance));
//        System.out.println("fv = " + fv(instance, args[0]));
//        System.out.println("free = " + freeInstance(instance));
//    }

}
