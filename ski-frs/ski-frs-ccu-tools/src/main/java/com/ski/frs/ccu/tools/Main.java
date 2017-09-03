package com.ski.frs.ccu.tools;

public class Main {

    public static void main(String[] args) {
        SBMaker maker = new SBMaker();
        maker.setFv(88);
        maker.setDid(args[0]);
        maker.make();
    }

}
