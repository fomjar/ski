package com.fomjar.blog.authorize;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fomjar.blog.config.ConfigService;

@Service
@SuppressWarnings("unchecked")
public class AuthorizeService {
    
    @Autowired
    private ConfigService service;
    
    public String auth_pass(String user, String pass) {
        if (null == user || null == pass) return null;
        if (!service.mon_users.config().containsKey(user)) {
            return null;
        }
        
        Map<String, Object> user_obj = (Map<String, Object>) service.mon_users.config().get(user);
        if (pass.equals(user_obj.get("pass"))) {
            String token = "token-" + UUID.randomUUID().toString().replace("-", "");
            user_obj.put("token", token);
            return token;
        }
        return null;
    }
    
    public boolean auth_token(String user, String token) {
        if (null == user || null == token) return false;
        if (!service.mon_users.config().containsKey(user)) {
            return false;
        }
        
        Map<String, Object> user_obj = (Map<String, Object>) service.mon_users.config().get(user);
        return token.equals(user_obj.get("token"));
    }
    
}
