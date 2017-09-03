package com.ski.frs.web;

import com.ski.frs.web.FeatureService.FeatureServicePool;
import com.ski.frs.web.filter.Filter1Authorize;
import com.ski.frs.web.filter.Filter5Document;
import com.ski.frs.web.filter.Filter6Interface;

import fomjar.server.FjServer;
import fomjar.server.web.FjWebTask;

public class WebTask extends FjWebTask {
    
    FeatureServicePool pool;

    @Override
    public void initialize(FjServer server) {
        super.initialize(server);
        registerFilter(new Filter1Authorize());
        registerFilter(new Filter5Document());
        registerFilter(new Filter6Interface());
        pool = FeatureService.pool0();
    }

    @Override
    public void destroy(FjServer server) {
        super.destroy(server);
        pool.close();
    }

}
