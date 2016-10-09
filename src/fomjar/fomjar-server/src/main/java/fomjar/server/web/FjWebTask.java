package fomjar.server.web;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjSender;
import fomjar.server.FjServer;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;

public class FjWebTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(FjWebTask.class);
    
    private ExecutorService   pool;
    private List<FjWebFilter> filters;
    
    public void registerFilter(FjWebFilter filter) {
        filters.add(filter);
    }

    @Override
    public void initialize(FjServer server) {
        pool = Executors.newCachedThreadPool();
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
            logger.error("illagal request type: " + msg.getClass().getName());
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
        
    }
    
    protected void postProtocol(FjHttpResponse response, FjHttpRequest request) {
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
    
    protected void onFilterException(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, Exception e) { }
    
}
