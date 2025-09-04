package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.service.DichVuBanDichService;
import com.main.datn_sd31.service.HomePageService;
import com.main.datn_sd31.dto.HomeProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final DichVuBanDichService dichVuBanDichService;
    private final HomePageService homePageService;

    @GetMapping("/")
    public String home(Model model, Locale locale) {
        // Thêm thông tin ngôn ngữ hiện tại
        model.addAttribute("currentLocale", locale.getLanguage());
        model.addAttribute("danhSachNgonNgu", dichVuBanDichService.layDanhSachNgonNguHoatDong());
        
        // Lấy 8 sản phẩm mới nhất
        List<HomeProductDto> homeProducts = homePageService.getLatestProducts(8);
        model.addAttribute("homeProducts", homeProducts);
        
        return "client/pages/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, @RequestParam(value = "added", required = false) String added,
                          @RequestParam(value = "product", required = false) String productId, Locale locale) {
        // Add success message if product was added to cart
        if ("true".equals(added) && productId != null) {
            model.addAttribute("success", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        }
        
        // Thêm thông tin ngôn ngữ hiện tại
        model.addAttribute("currentLocale", locale.getLanguage());
        model.addAttribute("danhSachNgonNgu", dichVuBanDichService.layDanhSachNgonNguHoatDong());
        
        return "client/pages/home";
    }
}
