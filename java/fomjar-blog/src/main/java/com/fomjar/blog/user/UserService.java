package com.fomjar.blog.user;

import java.util.Map;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fomjar.blog.config.ConfigService;

@Service
@SuppressWarnings("unchecked")
public class UserService {
    
    @Autowired
    private ConfigService service;
    
    public String auth_pass(String user, String pass, HttpServletRequest request, HttpServletResponse response) {
        if (null == user || null == pass) return null;
        
        Map<String, Object> user_obj = get(user);
        if (null == user_obj) return null;
        
        if (!pass.equals(user_obj.get("pass"))) return null;
        
        String token = "token-" + UUID.randomUUID().toString().replace("-", "");
        
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("token", token);
        
        Cookie cookie_user = new Cookie("user", user);
        cookie_user.setPath("/");
        Cookie cookie_token = new Cookie("token", token);
        cookie_token.setPath("/");
        response.addCookie(cookie_user);
        response.addCookie(cookie_token);
        
        return token;
    }
    
    public boolean auth_token(HttpServletRequest request) {
        String user_cookie = null;
        String token_cookie = null;
        String user_session = null;
        String token_session = null;
        
        if (null == request.getCookies()) return false;
        
        for (Cookie cookie : request.getCookies()) {
            if ("user".equals(cookie.getName()))  user_cookie = cookie.getValue();
            if ("token".equals(cookie.getName())) token_cookie = cookie.getValue();
        }
        
        HttpSession session = request.getSession();
        
        user_session = (String) session.getAttribute("user");
        token_session = (String) session.getAttribute("token");
        
        if (null == user_cookie
                || null == token_cookie
                || null == user_session
                || null == token_session) return false;
        
        if (!user_cookie.equals(user_session)
                || !token_cookie.equals(token_session)) return false;
        
        if (null == get(user_cookie)) return false;
        
        return true;
    }
    
    public Map<String, Object> get(String user) {
        return (Map<String, Object>) service.mon_users.config().get(user);
    }
    
}
