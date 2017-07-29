package com.fomjar.blog.comm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fomjar.blog.article.ArticleService;

@Controller
@ControllerAdvice
public class CommonController {
    
    @Autowired
    private ArticleService service;
    
    @RequestMapping(path = {"/", "/index"})
    public ModelAndView index() {
        return new ModelAndView("index")
                .addObject("articles", service.list());
    }
    
    @ExceptionHandler(Exception.class)
    public ModelAndView exception(Exception e) {
        return new ModelAndView("error")
                .addObject("code", -1)
                .addObject("desc", e.getMessage());
    }

}
