package fomjar.server.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.util.FjThreadFactory;

public class FjWebTask implements FjServer.FjServerTask {

    private static final Logger logger = Logger.getLogger(FjWebTask.class);

    private ExecutorService   pool;
    private List<FjWebFilter> filters;

    public void registerFilter(FjWebFilter filter) {
        filters.add(filter);
    }

    @Override
    public void initialize(FjServer server) {
        pool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 5, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new FjThreadFactory("fjwebtask"));
        filters = new LinkedList<FjWebFilter>();
    }

    @Override
    public void destroy(FjServer server) {
        pool.shutdownNow();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjHttpRequest)) {
            logger.error("illegal message: " + msg);
            return;
        }

        FjHttpRequest   request     = (FjHttpRequest) msg;
        FjHttpResponse  response    = new FjHttpResponse(null, 200, null, null);
        SocketChannel conn = (SocketChannel) wrapper.attachment("conn");
        wrapper.attach("conn", null); // give up connection
        pool.submit(()->{
            prepProtocol(response, request);
            for (FjWebFilter filter : filters) {
                try {
                    if (!filter.filter(response, request, conn)) break;
                } catch (Exception e) {
                    logger.error("error occurs when web filter: " + filter.getClass().getName(), e);
                    onFilterException(response, request, conn, e);
                    break;
                }
            }
            postProtocol(response, request);
            if (conn.isOpen()) FjSender.sendHttpResponse(response, conn);
        });
    }

    protected void prepProtocol(FjHttpResponse response, FjHttpRequest request) {
        prepProtocolCommon(response, request);
        prepProtocolGzip(response, request);
    }
    
    protected void prepProtocolCommon(FjHttpResponse response, FjHttpRequest request) {
        response.attr().put("Server",   "fomjar/0.0.1");
        response.attr().put("Date",     new Date().toString());
    }
    
    protected void prepProtocolGzip(FjHttpResponse response, FjHttpRequest request) {
        if (request.attr().containsKey("Content-Encoding")
                && request.attr().get("Content-Encoding").toLowerCase().contains("gzip")) {
            try {
                GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(request.content()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len = 0;
                while (0 < (len = gzis.read(buf))) baos.write(buf, 0, len);
                gzis.close();
                
                request.attr().put("Content-Length", String.valueOf(baos.size()));
                request.content(baos.toByteArray());
            } catch (IOException e) {
                logger.error("prep protocol gzip failed", e);
            }
        }
    }

    protected void postProtocol(FjHttpResponse response, FjHttpRequest request) {
        postProtocolRange(response, request);
        postProtocolEncoding(response, request);
    }
    
    protected void postProtocolRange(FjHttpResponse response, FjHttpRequest request) {
        if (request.attr().containsKey("Range")) {
            byte[] data = response.content();

            String range = request.attr().get("Range");
            int range_start = Integer.parseInt(range.split("=")[1].split("-")[0]);
            int range_end   = Integer.parseInt(range.split("=")[1].split("-")[1]);
            if (range_end + 1 > data.length) range_end = data.length - 1;
            response.attr().put("Content-Range", String.format("bytes %d-%d/%d", range_start, range_end, data.length));
            data = Arrays.copyOfRange(data, range_start, range_end + 1);

            response.content(data);
        }
    }
    
    protected void postProtocolEncoding(FjHttpResponse response, FjHttpRequest request) {
        if (response.attr().containsKey("Content-Encoding")) {
            if (response.attr().get("Content-Encoding").toLowerCase().contains("gzip")) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    GZIPOutputStream gzos = new GZIPOutputStream(baos);
                    gzos.write(response.content());
                    gzos.finish();
                    gzos.flush();
                    gzos.close();
                    
                    response.content(baos.toByteArray());
                } catch (IOException e) {
                    logger.error("post protocol gzip failed", e);
                }
            } else if (response.attr().get("Content-Encoding").toLowerCase().contains("deflate")) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(9));
                    dos.write(response.content());
                    dos.finish();
                    dos.flush();
                    dos.close();
                    
                    response.content(baos.toByteArray());
                } catch (IOException e) {
                    logger.error("post protocol gzip failed", e);
                }
            }
        }
    }

    protected void onFilterException(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, Exception e) { }

}
