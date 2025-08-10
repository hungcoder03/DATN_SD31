package com.main.datn_sd31.config;

import com.main.datn_sd31.security.CustomAuthenticationFailureHandler;
import com.main.datn_sd31.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/dang-nhap", "/admin/logout").permitAll()
                .anyRequest().hasAnyRole("ADMIN", "NHANVIEN")
            )
            .formLogin(form -> form
                .loginPage("/admin/dang-nhap")
                .loginProcessingUrl("/admin/dang-nhap")
                .defaultSuccessUrl("/admin/thong-ke", true)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/dang-nhap?logout=true")
                .permitAll()
            );
        return http.build();
    }

    @Order(2)
    @Bean
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
                        .anyRequest().hasRole("KHACHHANG")
                )
                .formLogin(form -> form
                        .loginPage("/khach-hang/dang-nhap")
                        .loginProcessingUrl("/khach-hang/dang-nhap")
                        .defaultSuccessUrl("/san-pham/danh-sach", true)
                        .permitAll()
                )
                .logout(lg -> lg
                        .logoutUrl("/khach-hang/dang-xuat")
                        .logoutSuccessUrl("/san-pham/danh-sach")
                        .invalidateHttpSession(true)                  // ✅ XÓA SESSION
                        .deleteCookies("JSESSIONID")                  // ✅ XÓA COOKIE phiên
                        .clearAuthentication(true)                    // ✅ XÓA thông tin xác thực
                );

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain publicSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login",
                                "/uploads/**",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/webjars/**",
                                "/static/**", "/favicon.ico",
                                "/san-pham/**",
                                "/client-static/**", // Cho phép truy cập công khai thư mục client-static
                                "/bootstrap-5.3.7-dist/**",
                                "/uploads/**" // Added this line
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/error"); // 👉 Trang tự tạo
                        })
                )
                .logout(lg -> lg.logoutSuccessUrl("/"))
                .formLogin(AbstractHttpConfigurer::disable); // Vô hiệu hóa login mặc định
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}