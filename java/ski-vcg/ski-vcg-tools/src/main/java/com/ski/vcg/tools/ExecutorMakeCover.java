package com.ski.vcg.tools;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class ExecutorMakeCover implements ToolExecutor {

    private static int      g_width     = 500;
    private static int      g_height    = 500;
    private static String   g_base      = ".";

    @Override
    public void execute(Map<String, String> args) {
        args.forEach((k, v)->{
            switch (k) {
            case "width":   g_width     = Integer.parseInt(v);  break;
            case "height":  g_height    = Integer.parseInt(v);  break;
            case "base":    g_base      = v;                    break;
            default:
                System.out.println(String.format("unknown argument: %s:%s", k, v));
                break;
            }
        });

        Map<Integer, Image> template = loadTemplate(g_base);
        Map<String, Map<Integer, Image>> inputs = loadInput(g_base);
        inputs.entrySet().forEach(input->{
            String  name = input.getKey();
            System.out.print(String.format("combining [%-40s]", name));

            Map<Integer, Image> combine = new HashMap<Integer, Image>(template);
            combine.putAll(input.getValue());

            List<Integer> keys = new LinkedList<Integer>(combine.keySet());
            Collections.sort(keys);

            BufferedImage buffer = new BufferedImage(g_width, g_height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = buffer.getGraphics();
            keys.forEach(key->{
                Image image = combine.get(key);
                g.drawImage(image, 0, 0, null);
            });
            try {
                ImageIO.write(buffer, "png", new File(String.format("%s/output/%s.png", g_base, name)));
                System.out.println(" done!");
            } catch (IOException e) {
                System.out.println(" fail!");
                e.printStackTrace();
            }
        });
    }

    private static Map<Integer, Image> loadTemplate(String dir) {
        Map<Integer, Image> template = new HashMap<Integer, Image>();
        for (File file : new File(dir + "/template").listFiles()) {
            if (file.getName().startsWith(".")) continue;

            try {
                Image   image = ImageIO.read(file);
                int     index = Integer.parseInt(file.getName().split("\\.")[0]);
                template.put(index, image);
            } catch (IOException e) {e.printStackTrace();}
        }
        return template;
    }

    private static Map<String, Map<Integer, Image>> loadInput(String dir) {
        Map<String, Map<Integer, Image>> input = new HashMap<String, Map<Integer, Image>>();
        for (File file : new File(dir + "/input").listFiles()) {
            if (file.getName().startsWith(".")) continue;

            try {
                Image   image   = ImageIO.read(file);
                String  name    = file.getName().split("\\.")[0];
                int     index   = Integer.parseInt(file.getName().split("\\.")[1]);

                if (!input.containsKey(name))
                    input.put(name, new HashMap<Integer, Image>());
                input.get(name).put(index, image);
            } catch (IOException e) {e.printStackTrace();}
        }
        return input;
    }

}
