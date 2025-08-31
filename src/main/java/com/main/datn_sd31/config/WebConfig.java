package com.main.datn_sd31.config;

import com.main.datn_sd31.security.EmployeeStatusInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private EmployeeStatusInterceptor employeeStatusInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cấu hình cho uploads - hỗ trợ cả classpath và file system
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                    "classpath:/static/uploads/",
                    "file:uploads/",
                    "file:./uploads/"
                )
                .setCachePeriod(3600);

        // Cấu hình cho images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // Cấu hình cho client-static (thêm mới)
        registry.addResourceHandler("/client-static/**")
                .addResourceLocations("classpath:/static/client-static/")
                .setCachePeriod(0); // Tắt cache cho development

        // Cấu hình cho static resources khác
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        // Thêm cấu hình cho static resources tổng quát
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Đăng ký interceptor kiểm tra trạng thái nhân viên
        registry.addInterceptor(employeeStatusInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/dang-nhap", "/admin/logout", "/admin/css/**", "/admin/js/**", "/admin/images/**");
    }
}
