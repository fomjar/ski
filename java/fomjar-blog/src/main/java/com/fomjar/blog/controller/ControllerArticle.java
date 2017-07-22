package com.fomjar.blog.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fomjar.blog.service.ServiceArticle;

@RestController
@RequestMapping(path = "/article",
                method = RequestMethod.POST)
public class ControllerArticle {
    
    private static final Log logger = LogFactory.getLog(ControllerArticle.class);
    
    @Autowired
    private ServiceArticle service;
    
    @RequestMapping("/edit")
    public Map<String, Object> post_edit(
            @RequestParam(name = "article.aid",     required = false)   String aid,
            @RequestParam(name = "article.author",  required = true)    String author,
            @RequestParam(name = "article.data",    required = true)    String data
    ) {
        logger.info("[ARTICLE POST EDIT]");
        
        Map<String, Object> rsp = new HashMap<>();
        try {
            service.article_edit(aid, author, data);
            rsp.put("code", 0);
            rsp.put("desc", "SUCCESS");
            logger.info("edit file success");
        } catch (Exception e) {
            rsp.put("code", -1);
            rsp.put("desc", e.getMessage());
            logger.error("edit file failed", e);
        }
            
        return rsp;
    }
    
    @RequestMapping("/view")
    public Map<String, Object> post_view(
            @RequestParam(name = "aid", required = true)    String aid
    ) {
        logger.info("[ARTICLE POST VIEW]");
        
        Map<String, Object> rsp = new HashMap<>();
        try {
            rsp.put("code", 0);
            rsp.put("desc", service.article_view(aid));
            logger.info("view file success: " + aid);
        } catch (Exception e) {
            rsp.put("code", -1);
            rsp.put("desc", e.getMessage());
            logger.error("view file failed: " + aid, e);
        }
            
        return rsp;
    }
    
    @RequestMapping("/list")
    public Object[] post_list() {
        logger.info("[ARTICLE POST VIEW]");
        return service.article_list().entrySet()
                .stream()
                .map(e->e.getValue())
                .toArray();
    }

}
