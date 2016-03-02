package fomjar.server.session;

import fomjar.server.FjMessageWrapper;

public interface FjSessionTask {

    boolean onSession(FjSessionPath path, FjMessageWrapper wrapper);
    
}