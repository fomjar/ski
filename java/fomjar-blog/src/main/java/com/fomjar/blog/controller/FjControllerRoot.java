package com.fomjar.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FjControllerRoot {

	@RequestMapping(path = {"/", "/index"}, method = RequestMethod.GET)
	public ModelAndView do_get() {
	    return new ModelAndView("index");
	}
	
}
