package com.fomjar.blog.article;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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
    
    private static String get_path(String aid) {
        return ConfigService.PATH_ARTICLE + "/" + aid + ".md";
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
    
    public void article_edit(String author, String path_view, String data) throws UnsupportedEncodingException, IOException {
        article_edit(new_aid(), author, path_view, data);
    }
    
    public void article_edit(String aid, String author, String path_view, String data) throws UnsupportedEncodingException, IOException {
        if (null == aid || 0 == aid.length()) aid = new_aid();
        
        String name = get_name(data);
        String path_data = get_path(aid);
        
        Files.write(new File(path_data).toPath(), data.getBytes("utf-8"),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        
        Map<String, Object> article = new HashMap<>();
        article.put("aid",          aid);
        article.put("name",         name);
        article.put("author",       author);
        article.put("path.view",    path_view);
        article.put("path.data",    path_data.substring(ConfigService.PATH_ROOT.length() + 1));
        article.put("time.update", System.currentTimeMillis());
        if (config.mon_article_list.config().containsKey(aid)) {
            Map<String, Object> article_old = (Map<String, Object>) config.mon_article_list.config().get(aid);
            article_old.putAll(article);
        } else {
            article.put("time.create", System.currentTimeMillis());
            config.mon_article_list.config().put(aid, article);
        }
    }
}
