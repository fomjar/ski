package com.ski.tools;

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

public class MakeIconExecutor implements ToolExecutor {
    
    @Override
    public void execute(String[] args) {
        int width   = Integer.parseInt(args[0]);
        int height  = Integer.parseInt(args[1]);
        String dir  = args.length > 2 ? args[2] : ".";
        
        Map<Integer, Image> library = loadLibrary(dir);
        for (File file : new File(dir + "/input").listFiles()) {
            System.out.print(String.format("combining %-40s", file.getName().substring(file.getName().indexOf(".") + 1)));
            Map<Integer, Image> library1 = new HashMap<Integer, Image>(library);
            try {
                library1.put(Integer.parseInt(file.getName().split("\\.")[0]), ImageIO.read(file));
                List<Integer> keys = new LinkedList<Integer>(library1.keySet());
                Collections.sort(keys);
                
                BufferedImage buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics g = buffer.getGraphics();
                keys.forEach(key->{
                    Image image = library1.get(key);
                    g.drawImage(image, 0, 0, null);
                });
                ImageIO.write(buffer, "png", new File(dir + "/output/" + file.getName().substring(file.getName().indexOf(".") + 1)));
                System.out.println(" done!");
            } catch (NumberFormatException | IOException e) {
                System.out.println(" fail!");
                e.printStackTrace();
            }
        }
    }
    
    private static Map<Integer, Image> loadLibrary(String dir) {
        Map<Integer, Image> library = new HashMap<Integer, Image>();
        for (File file : new File(dir + "/library").listFiles()) {
            try {
                Image image = ImageIO.read(file);
                library.put(Integer.parseInt(file.getName().split("\\.")[0]), image);
            } catch (IOException e) {e.printStackTrace();}
        }
        return library;
    }
    
}
