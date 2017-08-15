package com.fomjar.blog.user;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class UserController {
    
    private static final Log logger = LogFactory.getLog(UserController.class);
    
    @Autowired
    private UserService service;
    
    @RequestMapping("/login")
    public ModelAndView login() {
        return new ModelAndView();
    }
    
    @RequestMapping(path = "/authorize", method = RequestMethod.POST)
    public void authorize(
            @RequestParam(required = true)  String user,
            @RequestParam(required = true)  String pass,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (null != service.auth_pass(user, pass, request, response)) {
            logger.info("[USER AUTHORIZE] success: " + user);
            try {response.sendRedirect("/");}
            catch (IOException e) {logger.error("send redirect failed", e);}
        } else {
            logger.error("[USER AUTHORIZE] failed:" + user);
            try {response.sendRedirect("/user/login");}
            catch (IOException e) {logger.error("send redirect failed", e);}
        }
    }
    
    @RequestMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
