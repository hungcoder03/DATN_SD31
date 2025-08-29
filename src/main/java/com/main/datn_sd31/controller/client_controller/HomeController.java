package com.main.datn_sd31.controller.client_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "client/pages/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, @RequestParam(value = "added", required = false) String added,
                          @RequestParam(value = "product", required = false) String productId) {
        // Add success message if product was added to cart
        if ("true".equals(added) && productId != null) {
            model.addAttribute("success", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        }
        
        return "client/pages/home";
    }
}
