package com.fomjar.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControllerDefault {

	@RequestMapping(path = "/", method = RequestMethod.GET)
	public ModelAndView get_index() {
	    return new ModelAndView("/index.html");
	}
    
}
