package com.fomjar.blog.article;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/article")
public class ArticleController {
    
    private static final Log logger = LogFactory.getLog(ArticleController.class);
    
    @Autowired
    private ArticleService service;
    
    @RequestMapping("/view")
    public ModelAndView view(
            @RequestParam String aid
            ) {
        try {
            return new ModelAndView("/article/view")
                    .addObject("article", service.get(aid))
                    .addObject("markdown", service.get_data(aid));
        } catch (IOException e) {
            logger.error("article not found: " + aid, e);
            return new ModelAndView("/article/view")
                    .addObject("article", service.get(aid))
                    .addObject("markdown", "# article not found");
        }
    }
    
    @RequestMapping("/edit")
    public ModelAndView edit(@RequestParam(required = false) String aid) {
        try {
            return new ModelAndView("/article/edit")
                    .addObject("article", null == aid ? null : service.get(aid))
                    .addObject("markdown", null == aid ? null : service.get_data(aid));
        } catch (IOException e) {
            logger.error("article not found: " + aid);
            return new ModelAndView("/article/edit")
                    .addObject("article", null == aid ? null : service.get(aid))
                    .addObject("markdown", "# article not found");
        }
    }
    
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public ModelAndView update(
            @RequestParam(name = "aid",         required = false)   String aid,
            @RequestParam(name = "author",      required = true)    String author,
            @RequestParam(name = "path_view",   required = true)    String path_view,
            @RequestParam(name = "markdown",    required = true)    String markdown,
            HttpServletResponse response
    ) {
        try {
            service.update(aid, author, path_view, markdown);
            logger.info("[ARTICLE UPDATE] success: " + aid);
            response.sendRedirect("/article/view?aid=" + aid);
            return null;
        } catch (IOException e) {
            logger.error("[ARTICLE UPDATE] failed: " + aid, e);
            return new ModelAndView("/article/edit")
                    .addObject("markdown", markdown);
        }
    }

}
