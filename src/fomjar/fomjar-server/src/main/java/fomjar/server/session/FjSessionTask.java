package fomjar.server.session;

import fomjar.server.FjMessageWrapper;

public interface FjSessionTask {

    boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper);
    
}