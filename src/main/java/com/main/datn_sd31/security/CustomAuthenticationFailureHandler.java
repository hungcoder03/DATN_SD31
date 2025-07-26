package com.main.datn_sd31.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.main.datn_sd31.security.CombinedUserDetailsService;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String error = "Lỗi đăng nhập!";

        if (exception instanceof BadCredentialsException) {
            error = "Mật khẩu không đúng!";
        } else if (exception instanceof UsernameNotFoundException) {
            error = exception.getMessage();
        } else if (exception instanceof DisabledException) {
            error = "Tài khoản đã bị vô hiệu hóa!";
        } else if (exception instanceof LockedException) {
            error = "Tài khoản đã bị khóa!";
        } else if (exception instanceof AccountExpiredException) {
            error = "Tài khoản đã hết hạn!";
        } else if (exception instanceof CredentialsExpiredException) {
            error = "Mật khẩu đã hết hạn!";
        } else {
            error = exception.getMessage();
        }

        // Truyền lỗi qua query param
        response.sendRedirect("/login?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
    }
} 