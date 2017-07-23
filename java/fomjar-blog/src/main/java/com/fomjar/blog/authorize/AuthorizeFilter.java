package com.fomjar.blog.authorize;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorizeFilter implements Filter  {
    
    @Autowired
    private AuthorizeService service;
    private String[] list_white;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        list_white = new String[] {
                "login.html",   // must add
                "index.html",
                "article-view.html"
        };
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hrsp = (HttpServletResponse) response;
        
        // no need to authorize
        
        String uri = hreq.getRequestURI();
        
        for (String path : list_white) {
            if (uri.endsWith(path)) {
                chain.doFilter(request, response);
                return;
            }
        }
        
        // need to authorize
        
        if (null == hreq.getCookies()) {
            hrsp.sendRedirect("login.html");
            return;
        }
  
        String user = null;
        String token = null;
        for (Cookie cookie : hreq.getCookies()) {
            if ("user".equals(cookie.getName()))    user = cookie.getValue();
            if ("token".equals(cookie.getName()))   token = cookie.getValue();
        }
        if (!service.auth_token(user, token)) {
            hrsp.sendRedirect("login.html");
            return;
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
