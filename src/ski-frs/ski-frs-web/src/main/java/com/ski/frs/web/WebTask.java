package com.ski.frs.web;

import com.ski.sn.web.filter.Filter1Authorize;
import com.ski.sn.web.filter.Filter5Document;
import com.ski.sn.web.filter.Filter6Interface;

import fomjar.server.FjServer;
import fomjar.server.web.FjWebTask;

public class WebTask extends FjWebTask {

    @Override
    public void initialize(FjServer server) {
        super.initialize(server);
        registerFilter(new Filter1Authorize());
        registerFilter(new Filter5Document());
        registerFilter(new Filter6Interface());
    }

    @Override
    public void destroy(FjServer server) {
        super.destroy(server);
    }

}
