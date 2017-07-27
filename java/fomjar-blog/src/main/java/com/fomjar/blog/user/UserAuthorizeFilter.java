package com.fomjar.blog.user;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.util.URLEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UserAuthorizeFilter extends OncePerRequestFilter  {
    
    private static final Log logger = LogFactory.getLog(UserAuthorizeFilter.class);
    
    @Autowired
    private UserService service;
    private String[] list_black;
    
    public UserAuthorizeFilter() {
        list_black = new String[] {
                "/article/edit",
                "/article/update"
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        logger.info("[USER AUTHORIZE FILTER] " + request.getRequestURI());
        
        // no need to authorize
        
        String uri = request.getRequestURI();
        
        boolean need = false;
        for (String path : list_black) {
            if (uri.endsWith(path)) {
                need = true;
                break;
            }
        }
        if (!need) {
            chain.doFilter(request, response);
            return;
        }
        
        // need to authorize
        
        if (!service.auth_token(request)) {
            String redirect = request.getRequestURI();
            if (null != request.getQueryString()) redirect += "?" + request.getQueryString();
            
            response.sendRedirect("/user/login?redirect=" + new URLEncoder().encode(redirect, Charset.forName("utf-8")));
            return;
        }
        
        chain.doFilter(request, response);
    }

}
