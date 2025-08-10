package com.main.datn_sd31.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình cho uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/")
                .setCachePeriod(3600);

        // Cấu hình cho images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // Cấu hình cho static resources khác
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // Thêm cấu hình cho static resources tổng quát
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
