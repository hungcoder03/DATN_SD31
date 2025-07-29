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

        // Xác định trang login phù hợp dựa trên request
        String loginPage = "/login";
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/khach-hang")) {
            loginPage = "/khach-hang/dang-nhap";
        }
        response.sendRedirect(loginPage + "?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
    }
} 