package com.fomjar.blog.authorize;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthorizeFilter extends OncePerRequestFilter  {
    
    @Autowired
    private AuthorizeService service;
    private String[] list_black;
    
    public AuthorizeFilter() {
        list_black = new String[] {
                "article-edit.html"
        };
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
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
        
        if (null == request.getCookies()) {
            response.sendRedirect("login.html");
            return;
        }
  
        String user = null;
        String token = null;
        for (Cookie cookie : request.getCookies()) {
            if ("user".equals(cookie.getName()))    user = cookie.getValue();
            if ("token".equals(cookie.getName()))   token = cookie.getValue();
        }
        if (!service.auth_token(user, token)) {
            response.sendRedirect("login.html");
            return;
        }
        
        chain.doFilter(request, response);
    }

}
