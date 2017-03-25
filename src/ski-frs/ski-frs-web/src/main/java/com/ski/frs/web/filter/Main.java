package com.ski.frs.web.filter;

public class Main {
    
    public static void main(String[] args) {
        FaceInterface.init(FaceInterface.DEVICE_GPU);
        System.out.println(FaceInterface.fv(args[0]));
        FaceInterface.free();
    }

}
