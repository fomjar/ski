package com.fomjar.blog.article;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
    
    @RequestMapping("/view/{aid}")
    public ModelAndView view(
            @PathVariable String aid
    ) {
        if (null != aid && 0 < aid.length()) {
            Map<String, Object> article = service.get(aid);
            if (null == article) {
                logger.error("article not found: " + aid);
                return new ModelAndView("error")
                        .addObject("code", -1)
                        .addObject("desc", "article not found");
            }
        }
        try {
            return new ModelAndView("article/view")
                    .addObject("article", service.get(aid))
                    .addObject("markdown", service.get_data(aid));
        } catch (IOException e) {
            logger.error("article read failed: " + aid, e);
            return new ModelAndView("error")
                    .addObject("code", -1)
                    .addObject("desc", "article read failed: " + e.getMessage());
        }
    }
    
    @RequestMapping("/edit")
    public ModelAndView edit(
            @RequestParam(required = false) String aid,
            HttpServletRequest request
    ) {
        if (null != aid && 0 < aid.length()) {
            Map<String, Object> article = service.get(aid);
            if (null == article) {
                logger.error("article not found: " + aid);
                return new ModelAndView("error")
                        .addObject("code", -1)
                        .addObject("desc", "article not found");
            }
            if (!article.get("author").equals(request.getSession().getAttribute("user"))) {
                logger.error("illegal access! user=" + article.get("author") + ", aid=" + aid);
                return new ModelAndView("error")
                        .addObject("code", -1)
                        .addObject("desc", "illegal access");
            }
        }
        
        try {
            return new ModelAndView("article/edit")
                    .addObject("article", service.get(aid))
                    .addObject("markdown", service.get_data(aid));
        } catch (IOException e) {
            logger.error("article read failed: " + aid, e);
            return new ModelAndView("error")
                    .addObject("code", -1)
                    .addObject("desc", "article read failed: " + e.getMessage());
        }
    }
    
    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public ModelAndView update(
            @RequestParam(name = "aid",         required = false)   String aid,
            @RequestParam(name = "author",      required = true)    String author,
            @RequestParam(name = "path_view",   required = true)    String path_view,
            @RequestParam(name = "markdown",    required = true)    String markdown,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (null != aid && 0 < aid.length()) {
            Map<String, Object> article = service.get(aid);
            if (null != article) {
                if (!article.get("author").equals(request.getSession().getAttribute("user"))) {
                    logger.error("[ARTICLE UPDATE] illegal access! user=" + article.get("author") + ", aid=" + aid);
                    return new ModelAndView("error")
                            .addObject("code", -1)
                            .addObject("desc", "illegal access");
                }
            }
        }
        try {
            aid = service.update(aid, author, path_view, markdown);
            logger.info("[ARTICLE UPDATE] success: " + aid);
            response.sendRedirect("/article/view/" + aid);
            return null;
        } catch (IOException e) {
            logger.error("[ARTICLE UPDATE] failed: " + aid, e);
            return new ModelAndView("article/edit")
                    .addObject("markdown", markdown);
        }
    }

}
