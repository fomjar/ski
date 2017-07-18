package com.ski.vcg.mma;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FFMPEG {

    private String  ffmpeg;
    private File    source;
    private File    target;
    private OutputStream target_stream;
    private String  format;

    public FFMPEG() {
        String os = System.getProperty("os.name");
        String ar = System.getProperty("os.arch").contains("64") ? "x64" : "x86";
        if (os.contains("inux")) {
            this.ffmpeg = String.format("./lib/ffmpeg_%s", ar);
        } else if (os.contains("indow")) {
            this.ffmpeg = String.format("lib/ffmpeg_%s.exe", ar);
        }
    }

    public FFMPEG(String ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public FFMPEG source(byte[] source) throws IOException {
        return source(new ByteArrayInputStream(source));
    }

    public FFMPEG source(InputStream source) throws IOException {
        File file = new File(System.currentTimeMillis() + ".source");

        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len = -1;
        while (0 < (len = source.read(buf))) fos.write(buf, 0, len);
        fos.flush();
        fos.close();

        return source(file);
    }

    public FFMPEG source(File source) throws FileNotFoundException {
        if (!source.isFile()) throw new FileNotFoundException();
        this.source = source;
        return this;
    }

    public FFMPEG target(OutputStream target) {
        this.target = new File(System.currentTimeMillis() + ".target");
        this.target_stream = target;
        return this;
    }

    public FFMPEG target(File target) {
        this.target = target;
        this.target_stream = null;
        return this;
    }

    public FFMPEG format(String format) {
        this.format = format;
        return this;
    }

    public void execute() throws IOException, InterruptedException {
        if (null == source) throw new IOException("no source specified");
        if (null == target) throw new IOException("no target specified");
        if (null == format) throw new IOException("no format specified");

        String command = String.format("%s -i %s -f %s %s", ffmpeg, source.getAbsoluteFile(), format, target.getAbsolutePath());
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        if (null != target_stream) {
            FileInputStream fis = new FileInputStream(target);
            byte[] buf = new byte[1024];
            int len = -1;
            while (0 < (len = fis.read(buf))) target_stream.write(buf, 0, len);
            target_stream.flush();
            fis.close();
        }

        source.delete();
        target.delete();
    }

}
