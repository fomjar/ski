package fomjar.server.session;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;

public interface FjSessionTask {

    void onSession(FjServer server, FjSessionContext context, FjMessageWrapper wrapper);
    
}