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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String error = "Lỗi đăng nhập không xác định!";

        if (exception instanceof UsernameNotFoundException) {
            error = "Không tìm thấy tài khoản với email: " + request.getParameter("username");
        } else if (exception instanceof BadCredentialsException) {
            error = "Mật khẩu không đúng cho tài khoản: " + request.getParameter("username");
        } else if (exception instanceof DisabledException) {
            error = exception.getMessage(); // Sử dụng message từ CustomUserDetailsService
        } else {
            error = "Đăng nhập thất bại!";
        }

        // Redirect về trang login với thông báo lỗi
        String loginPage = "/admin/dang-nhap";
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/khach-hang")) {
            loginPage = "/khach-hang/dang-nhap";
        }
        response.sendRedirect(loginPage + "?error=" + java.net.URLEncoder.encode(error, "UTF-8"));
    }
}