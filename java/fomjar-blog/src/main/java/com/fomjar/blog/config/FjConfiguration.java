package com.fomjar.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
public class FjConfiguration implements WebMvcConfigurer {

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
    
    public static class FjViewResolver {
        @Bean
        public ViewResolver getViewResolver() {
            InternalResourceViewResolver vr = new InternalResourceViewResolver();
            vr.setPrefix("/WEB-INF/");
            vr.setSuffix(".html");
            return vr;
        }
    }


}
