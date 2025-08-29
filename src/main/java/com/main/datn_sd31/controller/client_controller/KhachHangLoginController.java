package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.service.KhachHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class KhachHangLoginController {

    private final KhachHangService khachHangService;

    @GetMapping("/dang-nhap")
    public String hienThiFormLogin() {
        return "client/pages/auth/login";  // trỏ tới file login.html trong templates/client/pages/auth/
    }

    @GetMapping("/dang-ky")
    public String hienThiFormDangKy(Model model) {
        model.addAttribute("khachHang", new KhachHang());
        return "client/pages/auth/register";
    }
    
    @PostMapping("/dang-ky")
    public String xuLyDangKy(@ModelAttribute KhachHang khachHang,
                             @RequestParam String xacNhanMatKhau,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra validation cơ bản
            if (khachHang.getTen() == null || khachHang.getTen().trim().isEmpty()) {
                model.addAttribute("error", "Họ và tên không được để trống!");
                model.addAttribute("khachHang", khachHang);
                return "client/pages/auth/register";
            }
            
            if (khachHang.getEmail() == null || khachHang.getEmail().trim().isEmpty()) {
                model.addAttribute("error", "Email không được để trống!");
                model.addAttribute("khachHang", khachHang);
                return "client/pages/auth/register";
            }
            
            if (khachHang.getMatKhau() == null || khachHang.getMatKhau().length() < 6) {
                model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
                model.addAttribute("khachHang", khachHang);
                return "client/pages/auth/register";
            }
            
            if (!khachHang.getMatKhau().equals(xacNhanMatKhau)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
                model.addAttribute("khachHang", khachHang);
                return "client/pages/auth/register";
            }
            
            if (khachHang.getSoDienThoai() == null || khachHang.getSoDienThoai().trim().isEmpty()) {
                model.addAttribute("error", "Số điện thoại không được để trống!");
                model.addAttribute("khachHang", khachHang);
                return "client/pages/auth/register";
            }
            
            // Xử lý đăng ký
            KhachHang khachHangMoi = khachHangService.dangKyKhachHang(khachHang, xacNhanMatKhau);
            
            // Chuyển hướng với thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/khach-hang/dang-nhap";
            
        } catch (RuntimeException e) {
            // Xử lý lỗi từ service
            model.addAttribute("error", e.getMessage());
            model.addAttribute("khachHang", khachHang);
            return "client/pages/auth/register";
        } catch (Exception e) {
            // Xử lý lỗi không xác định
            model.addAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
            model.addAttribute("khachHang", khachHang);
            return "client/pages/auth/register";
        }
    }
    
    @GetMapping("/dang-xuat")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/";
    }
}