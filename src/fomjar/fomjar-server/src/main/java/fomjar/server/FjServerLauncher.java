package fomjar.server;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FjServerLauncher {
    
    private static void printUsage() {
        System.out.println("Usage: java -jar lib/fomjar-server.jar [-J\"jvm options\"] server-name-1 [server-name-2] [server-name-3] ...");
    }
    
    private static String collectClassPath() {
        File lib = new File("lib");
        String delimiter = System.getProperty("os.name").contains("indow") ? ";" : ":";
        return Arrays.asList(lib.listFiles())
                .stream()
                .map(file->"lib/" + file.getName())
                .collect(Collectors.joining(delimiter));
    }
    
    private static String collectJvmOptions(String[] args) {
        return Arrays.asList(args)
                .stream()
                .filter(arg->arg.startsWith("-J"))
                .map(arg->{
                    String opt = arg.substring(2);
                    if (opt.startsWith("\"")) opt = opt.substring(1);
                    if (opt.endsWith("\""))   opt = opt.substring(0, opt.length() - 1);
                    return opt;
                })
                .collect(Collectors.joining(" "));
    }
    
    private static List<String> collectServerName(String[] args) {
        return Arrays.asList(args)
                .stream()
                .filter(arg->!arg.startsWith("-J"))
                .collect(Collectors.toList());
    }
    
    public static void main(String[] args) {
        if (null == args || 0 == args.length) {
            printUsage();
            System.exit(0);
            return;
        }
        
        String       cp  = collectClassPath();
        String       jvm = collectJvmOptions(args);
        List<String> sns = collectServerName(args);
        String       cmd = "java %s -cp %s com.ski.%s.Main %s";
        for (String sn : sns) {
            String mod  = sn.split("-")[0];
            String cmd0 = String.format(cmd,
                    jvm,
                    cp,
                    mod,
                    sn);
            try {
                Process process = Runtime.getRuntime().exec(cmd0);
                Thread.sleep(1000L * 1);
                
                byte[] buf = new byte[1024 * 4];
                int    len = process.getInputStream().read(buf);
                System.out.print(new String(buf, 0, len));
            } catch (Exception e) {
                System.err.println("start server failed: " + sn);
                e.printStackTrace();
            }
        }
    }
}
