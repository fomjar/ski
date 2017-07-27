package com.fomjar.blog.article;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fomjar.blog.config.ConfigService;

@Service
@SuppressWarnings("unchecked")
public class ArticleService {
    
    @Autowired
    private ConfigService config;
    
    public ArticleService() {}
    
    private static String new_aid() {
        return "article-" + UUID.randomUUID().toString().replace("-", "");
    }
    
    private static String get_path_data(String aid) {
        return ConfigService.PATH_ARTICLES + "/" + aid + ".md";
    }
    
    private static String get_name(String data) {
        String[] lines = data.split("\n");
        for (String name : lines) {
            while (name.startsWith("#")) name = name.substring(1);
            name = name.trim();
            if (0 < name.length()) return name;
        }
        return "untitled";
    }
    
    public Map<String, Object> get(String aid) {
        return (Map<String, Object>) config.mon_articles.config().get(aid);
    }
    
    public Collection<Object> list() {
        return config.mon_articles.config().values();
    }
    
    public String get_data(String aid) throws IOException {
        Map<String, Object> article = get(aid);
        if (null == article) return null;
        
        String path = (String) article.get("path_data");
        byte[] buff = Files.readAllBytes(new File(path).toPath());
        return new String(buff, "utf-8");
    }
    
    public String update(String author, String path_view, String data) throws UnsupportedEncodingException, IOException {
        return update(new_aid(), author, path_view, data);
    }
    
    public String update(String aid, String author, String path_view, String markdown) throws UnsupportedEncodingException, IOException {
        if (null == aid || 0 == aid.length()) aid = new_aid();
        
        String name = get_name(markdown);
        String path_data = get_path_data(aid);
        
        Files.write(new File(path_data).toPath(), markdown.getBytes("utf-8"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        
        Map<String, Object> article = new HashMap<>();
        article.put("aid",          aid);
        article.put("name",         name);
        article.put("author",       author);
        article.put("path_view",    path_view);
        article.put("path_data",    path_data);
        article.put("time_update", System.currentTimeMillis());
        if (config.mon_articles.config().containsKey(aid)) {
            Map<String, Object> article_old = (Map<String, Object>) config.mon_articles.config().get(aid);
            article_old.putAll(article);
        } else {
            article.put("time_create", System.currentTimeMillis());
            config.mon_articles.config().put(aid, article);
        }
        config.mon_articles.mod_mem();
        
        return aid;
    }
}
