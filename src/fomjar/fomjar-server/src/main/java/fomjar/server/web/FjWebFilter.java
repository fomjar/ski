package fomjar.server.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;

public abstract class FjWebFilter {

    private static final Logger logger = Logger.getLogger(FjWebFilter.class);

    /**
     *
     * @param response
     * @param request
     * @return if continue filter next
     */
    public abstract boolean filter (FjHttpResponse response, FjHttpRequest request, SocketChannel conn);

    private static String document_root = "./";
    private static final Map<String, Long>   cache_file_modify  = new ConcurrentHashMap<String, Long>();
    private static final Map<String, byte[]> cache_file_content = new ConcurrentHashMap<String, byte[]>();

    public static void document(FjHttpResponse response, String path) {
        File file = new File(document_root + path);
        if (!file.isFile()) return;

        byte[] content = null;
        if (!cache_file_modify.containsKey(path)) cache_file_modify.put(path, 0l);

        if (file.lastModified() <= cache_file_modify.get(path)) content = cache_file_content.get(path);
        else {
            FileInputStream         fis = null;
            ByteArrayOutputStream   baos = null;
            try {
                byte[]  buf = new byte[1024 * 4];
                int     len = -1;
                fis     = new FileInputStream(file);
                baos    = new ByteArrayOutputStream();
                while (0 < (len = fis.read(buf))) baos.write(buf, 0, len);
                content = baos.toByteArray();

                cache_file_modify.put(path, file.lastModified());
                cache_file_content.put(path, content);
            } catch (IOException e) {logger.error("fetch file failed, path: " + path, e);}
            finally {
                try {
                    fis.close();
                    baos.close();
                } catch (IOException e) {e.printStackTrace();}
            }
        }
        if (null != content) {
            response.attr().put("Content-Type", mime(file));
            response.content(content);
        }
    }

    public static void documentRoot(String root) {
        FjWebFilter.document_root = root;
    }

    private static final Map<String, String> mime = new ConcurrentHashMap<String, String>();

    public static String mime(File file) {
        if (!file.getName().contains(".")) return mime("txt");

        String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();

        return mime(ext);
    }

    public static String mime(String ext) {
        if (mime.isEmpty()) initMime();

        if (mime.containsKey(ext)) return mime.get(ext);
        else return mime.get("txt");
    }

    public static void loadMime(Map<String, String> mime) {
        FjWebFilter.mime.clear();
        FjWebFilter.mime.putAll(mime);
    }

    private static void initMime() {
        if (mime.isEmpty()) {
            mime.put("txt",     "text/plain");
            mime.put("html",    "text/html");
            mime.put("htm",     "text/html");
            mime.put("js",      "application/x-javascript");
            mime.put("css",     "text/css");
            mime.put("less",    "text/css");
            mime.put("xml",     "text/xml");
            mime.put("json",    "application/json");
            mime.put("jpg",     "image/jpg");
            mime.put("jpeg",    "image/jpg");
            mime.put("bmp",     "image/bmp");
            mime.put("png",     "image/png");
            mime.put("gif",     "image/gif");
        }
    }

    public static void redirect(FjHttpResponse response, String url) {
        response.code(302);
        response.attr().put("Location", url);
    }
}