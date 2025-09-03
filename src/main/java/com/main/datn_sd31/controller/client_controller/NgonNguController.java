package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.service.DichVuBanDichService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class NgonNguController {
    
    private final DichVuBanDichService dichVuBanDichService;
    private final LocaleResolver localeResolver;
    
    @GetMapping("/doi-ngon-ngu")
    public String doiNgonNgu(@RequestParam String ngonNgu, HttpServletRequest request, HttpServletResponse response) {
        // Thử parse mã ngôn ngữ an toàn (ví dụ: "vi", "en", "vi_VN", "en-US")
        String tag = (ngonNgu != null) ? ngonNgu.replace('_', '-') : "";
        Locale locale;
        try {
            locale = Locale.forLanguageTag(tag);
            if (locale == null || "".equals(locale.getLanguage())) {
                // fallback nếu không hợp lệ
                locale = new Locale("vi", "VN");
            }
        } catch (Exception e) {
            locale = new Locale("vi", "VN");
        }

        // Thiết lập locale trực tiếp (CookieLocaleResolver hoặc các resolver khác sẽ xử lý)
        try {
            localeResolver.setLocale(request, response, locale);
        } catch (Exception e) {
            // không ném lỗi ra ngoài, chỉ log nếu cần (ở đây để lại comment)
            // e.printStackTrace();
        }

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
