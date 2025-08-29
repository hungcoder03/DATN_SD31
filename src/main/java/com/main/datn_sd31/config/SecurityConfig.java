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



import static com.main.datn_sd31.config.SecurityConstants.*;

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
            .securityMatcher(ADMIN_PATTERNS)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ADMIN_PERMIT_ALL).permitAll()
                .requestMatchers(ADMIN_ONLY_URLS).hasRole("ADMIN")
                .requestMatchers(ADMIN_AND_NHANVIEN_URLS).hasAnyRole("ADMIN", "NHANVIEN")
                .anyRequest().hasAnyRole("ADMIN", "NHANVIEN")
            )
            .formLogin(form -> form
                .loginPage(ADMIN_LOGIN)
                .loginProcessingUrl(ADMIN_LOGIN)
                .defaultSuccessUrl(ADMIN_DASHBOARD, true)
                .failureHandler(customAuthenticationFailureHandler)
                .permitAll()
            )
            // Logout được xử lý bởi AccessDeniedController
            .logout(logout -> logout.disable())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(CSRF_EXEMPTIONS)
            )
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/admin/access-denied")
            );
        return http.build();
    }

    @Order(2)
    @Bean
    public SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher(CUSTOMER_PATTERNS)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(CUSTOMER_PERMIT_ALL).permitAll()
                        .requestMatchers(CUSTOMER_PROTECTED).hasRole("KHACHHANG")
                        .anyRequest().hasRole("KHACHHANG")
                )
                .formLogin(form -> form
                        .loginPage(CUSTOMER_LOGIN)
                        .loginProcessingUrl(CUSTOMER_LOGIN)
                        .defaultSuccessUrl(PRODUCTS + "/danh-sach", true)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                // Logout được xử lý bởi KhachHangLoginController
                .logout(lg -> lg.disable());

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain publicSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATTERNS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/error");
                        })
                )
                .logout(lg -> lg.logoutSuccessUrl(HOME))
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