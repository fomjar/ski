package com.fomjar.blog.article;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/article",
                method = RequestMethod.POST)
public class ArticleController {
    
    private static final Log logger = LogFactory.getLog(ArticleController.class);
    
    @Autowired
    private ArticleService service;
    
    @RequestMapping("/edit")
    public Map<String, Object> post_edit(
            @RequestParam(name = "article.aid",     required = false)   String aid,
            @RequestParam(name = "article.author",  required = true)    String author,
            @RequestParam(name = "article.path",    required = true)    String path,
            @RequestParam(name = "article.data",    required = true)    String data
    ) {
        logger.info("[ARTICLE POST EDIT]");
        
        Map<String, Object> rsp = new HashMap<>();
        try {
            service.article_edit(aid, author, path, data);
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

}
