package com.fomjar.blog.authorize;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthorizeController {
    
    private static final Log logger = LogFactory.getLog(AuthorizeController.class);
    
    @Autowired
    private AuthorizeService service;
    
    @RequestMapping(path = "/authorize", method = RequestMethod.POST)
    public String post_auth(
            @RequestParam   String user,
            @RequestParam   String pass,
            HttpServletResponse response
    ) {
        logger.info("[AUTHORIZE POST AUTH]");
        
        String token = null;
        if (null != (token = service.auth_pass(user, pass))) {
            response.addCookie(new Cookie("user",  user));
            response.addCookie(new Cookie("token", token));
            logger.info("authorize success: " + user);
            return "redirect:index.html";
        } else {
            logger.error("authorize failed:" + user);
            return "redirect:login.html";
        }
    }

}
