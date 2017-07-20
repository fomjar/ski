package com.fomjar.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FjControllerDefault {

	@RequestMapping(path = {"/", "/index"}, method = RequestMethod.GET)
	public ModelAndView get_root() {
	    return new ModelAndView("/index");
	}
	
//	@RequestMapping(path = "/**", method = RequestMethod.GET)
//	public ModelAndView get_default(HttpServletRequest request) {
//        return new ModelAndView(request.getRequestURI());
//	}
	
}
