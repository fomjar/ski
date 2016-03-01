package fomjar.server.session;

import fomjar.server.FjMessageWrapper;

public interface FjSessionTask {

    void onSession(FjSessionPath path, FjMessageWrapper wrapper);
    
}