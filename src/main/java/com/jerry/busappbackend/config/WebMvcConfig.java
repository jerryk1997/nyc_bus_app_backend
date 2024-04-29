package com.jerry.busappbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jerry.busappbackend.filter.RequestResponseLoggingFilter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RequestResponseLoggingFilter requestResponseLoggingFilter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedMethods("GET")
            .allowedOrigins("http://localhost:5173");
    }

    @Bean
    FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(requestResponseLoggingFilter);
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }
}
