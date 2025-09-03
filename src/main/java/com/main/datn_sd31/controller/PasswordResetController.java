package com.main.datn_sd31.controller;

import com.main.datn_sd31.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/quen-mat-khau")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Hiển thị form nhập email để quên mật khẩu
     */
    @GetMapping("")
    public String showForgotPasswordForm(Model model) {
        return "client/pages/auth/forgot-password";
    }

    /**
     * Xử lý yêu cầu gửi mã xác nhận
     */
    @PostMapping("")
    public String processForgotPassword(@RequestParam String email, 
                                       RedirectAttributes redirectAttributes) {
        try {
            boolean success = passwordResetService.generateAndSendCode(email);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/quen-mat-khau/dat-lai";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Email không tồn tại trong hệ thống. Vui lòng kiểm tra lại.");
                return "redirect:/quen-mat-khau";
            }
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra. Vui lòng thử lại sau.");
            return "redirect:/quen-mat-khau";
        }
    }

    /**
     * Hiển thị form nhập mã xác nhận và mật khẩu mới
     */
    @GetMapping("/dat-lai")
    public String showResetPasswordForm(@ModelAttribute("email") String email, 
                                       Model model) {
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/quen-mat-khau";
        }
        
        model.addAttribute("email", email);
        return "client/pages/auth/reset-password";
    }

    /**
     * Xử lý đặt lại mật khẩu
     */
    @PostMapping("/dat-lai")
    public String processResetPassword(@RequestParam String email,
                                      @RequestParam String code,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu phải có ít nhất 6 ký tự.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/quen-mat-khau/dat-lai";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mật khẩu xác nhận không khớp.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/quen-mat-khau/dat-lai";
            }
            
            // Verify code
            if (!passwordResetService.verifyCode(email, code)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Mã xác nhận không đúng hoặc đã hết hạn. Vui lòng thử lại.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/quen-mat-khau/dat-lai";
            }
            
            // Reset password
            boolean success = passwordResetService.resetPassword(email, newPassword);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
                return "redirect:/khach-hang/dang-nhap";
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/quen-mat-khau/dat-lai";
            }
            
        } catch (Exception e) {
            log.error("Error processing reset password", e);
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra. Vui lòng thử lại sau.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/quen-mat-khau/dat-lai";
        }
    }
}
