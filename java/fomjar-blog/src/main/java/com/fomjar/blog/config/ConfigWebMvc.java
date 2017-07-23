package com.fomjar.blog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fomjar.blog.authorize.AuthorizeFilter;

@Configuration
@EnableWebMvc
public class ConfigWebMvc implements WebMvcConfigurer {
    
    @Autowired
    private AuthorizeFilter filter;

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // resolve static resource
        registry.addResourceHandler("/**")
                .addResourceLocations("file:./document/");
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // resolve ModelAndView
        registry.viewResolver(new InternalResourceViewResolver());
    }
    
    @Bean
    public FilterRegistrationBean<?> filter_authorize() {
        FilterRegistrationBean<AuthorizeFilter> register = new FilterRegistrationBean<AuthorizeFilter>();
        register.setFilter(filter);
        register.addUrlPatterns("*.html");
        return register;
    }

}
