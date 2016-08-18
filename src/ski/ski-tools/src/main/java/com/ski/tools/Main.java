package com.ski.tools;

import java.util.Arrays;

public class Main {
    
    public static void main(String[] args) {
        String      cmd = args[0];
        String[]    arg = Arrays.copyOfRange(args, 1, args.length);
        ToolExecutor  e = null;
        switch (cmd) {
        case "makeicon":
            e = new MakeIconExecutor();
            break;
        case "makeintr":
        	e = new MakeIntrExecutor();
        	break;
        }
        e.execute(arg);
    }

}
