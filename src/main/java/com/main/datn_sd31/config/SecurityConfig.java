package com.main.datn_sd31.config;

import com.main.datn_sd31.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /* ===== 1. Bảo vệ khu vực ADMIN ===== */
    @Bean
    @Order(1)           // Ưu tiên cao nhất
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")      // Chỉ áp dụng filter‑chain này cho /admin/**
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasRole("ADMIN")  // Phải có ROLE_ADMIN
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }

    /* ===== 2. Bảo vệ khu vực KHÁCH HÀNG ===== */
    @Bean
    @Order(2)
    public SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/khach-hang/**", "/gio-hang/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/khach-hang/dang-nhap",
                                "/khach-hang/dang-ky",
                                "/khach-hang/danh-sach",
                                "/khach-hang/chi-tiet/**",
                                "/khach-hang/quen-mat-khau",
                                "/khach-hang/public/**"
                        ).permitAll()
                        .anyRequest().hasRole("CUSTOMER")
                )
                .formLogin(form -> form
                        .loginPage("/khach-hang/dang-nhap")           // ⬅️ Đây là đường dẫn GET trả về form login
                        .loginProcessingUrl("/khach-hang/dang-nhap")   // ⬅️ Đây là POST để xử lý form
                        .defaultSuccessUrl("/khach-hang/danh-sach", true)
                        .permitAll()
                )
                .logout(lg -> lg
                        .logoutUrl("/khach-hang/dang-xuat")
                        .logoutSuccessUrl("/khach-hang/dang-nhap?logout")
                );

        return http.build();
    }

    /* ===== 3. Cấu hình chung cho phần công khai (static, trang chủ, v.v.) ===== */
    @Bean
    @Order(3)           // Thấp nhất
    public SecurityFilterChain publicSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/uploads/**",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/webjars/**",
                                "/static/**", "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")      // Trang đăng nhập chung
                        .permitAll()
                )
                .logout(lg -> lg.logoutSuccessUrl("/"));
        return http.build();
    }

    /* ===== Bean chung ===== */
    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();      // Phải trả về ROLE_ADMIN hoặc ROLE_CUSTOMER tương ứng
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}