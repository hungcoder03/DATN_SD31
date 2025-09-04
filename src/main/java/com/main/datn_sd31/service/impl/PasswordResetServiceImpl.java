package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.KhachHangService;
import com.main.datn_sd31.service.PasswordResetService;
import com.main.datn_sd31.service.SendMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final KhachHangRepository khachHangRepository;
    private final KhachHangService khachHangService;
    private final SendMailService sendMailService;
    private final BCryptPasswordEncoder passwordEncoder;

    // In-memory storage for reset codes: email -> ResetCode
    private final Map<String, ResetCode> resetCodes = new ConcurrentHashMap<>();

    @Override
    public boolean generateAndSendCode(String email) {
        try {
            // Check if email exists
            if (!khachHangService.emailDaTonTai(email)) {
                log.warn("Password reset requested for non-existent email: {}", email);
                return false;
            }

            // Generate 6-digit verification code
            String code = generateVerificationCode();
            
            // Store code with expiry (15 minutes)
            resetCodes.put(email, new ResetCode(code, LocalDateTime.now().plusMinutes(15)));
            
            // Send email
            sendResetCodeEmail(email, code);
            
            log.info("Password reset code sent to: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("Error generating reset code for email: {}", email, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        try {
            ResetCode storedCode = resetCodes.get(email);
            
            if (storedCode == null) {
                log.warn("No reset code found for email: {}", email);
                return false;
            }
            
            if (storedCode.isExpired()) {
                log.warn("Reset code expired for email: {}", email);
                resetCodes.remove(email);
                return false;
            }
            
            if (!storedCode.getCode().equals(code)) {
                log.warn("Invalid reset code for email: {}", email);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error verifying code for email: {}", email, e);
            return false;
        }
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        try {
            // Verify code is still valid
            ResetCode storedCode = resetCodes.get(email);
            if (storedCode == null || storedCode.isExpired()) {
                log.warn("Cannot reset password - no valid code for email: {}", email);
                return false;
            }
            
            // Get customer and update password
            KhachHang khachHang = khachHangService.findByEmail(email);
            khachHang.setMatKhau(passwordEncoder.encode(newPassword));
            khachHang.setNgaySua(LocalDateTime.now());
            
            khachHangService.save(khachHang);
            
            // Remove used code
            resetCodes.remove(email);
            
            log.info("Password reset successfully for email: {}", email);
            return true;
            
        } catch (Exception e) {
            log.error("Error resetting password for email: {}", email, e);
            return false;
        }
    }

    @Override
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupExpiredCodes() {
        try {
            int removedCount = 0;
            for (Map.Entry<String, ResetCode> entry : resetCodes.entrySet()) {
                if (entry.getValue().isExpired()) {
                    resetCodes.remove(entry.getKey());
                    removedCount++;
                }
            }
            if (removedCount > 0) {
                log.info("Cleaned up {} expired reset codes", removedCount);
            }
        } catch (Exception e) {
            log.error("Error cleaning up expired codes", e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendResetCodeEmail(String email, String code) throws MessagingException {
        try {
            String subject = "Mã xác nhận đặt lại mật khẩu - D&C Fashions";
            String htmlContent = generateResetCodeEmailHtml(code);
            sendMailService.sendHtmlMail(email, subject, htmlContent);
        } catch (Exception e) {
            log.error("Error sending reset code email to: {}", email, e);
            throw e;
        }
    }

    private String generateResetCodeEmailHtml(String code) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                "<div style=\"background: #667eea; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
                "<h1 style=\"margin: 0; font-size: 24px;\"> Đặt lại mật khẩu</h1>" +
                "</div>" +
                "<div style=\"background: white; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 10px 10px;\">" +
                "<p>Xin chào,</p>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình.</p>" +
                "<div style=\"background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;\">" +
                "<p style=\"margin: 0; font-size: 18px; font-weight: bold; color: #667eea;\">Mã xác nhận của bạn:</p>" +
                "<p style=\"margin: 10px 0; font-size: 32px; font-weight: bold; color: #333; letter-spacing: 5px;\">" + code + "</p>" +
                "</div>" +
                "<p><strong>Lưu ý quan trọng:</strong></p>" +
                "<ul>" +
                "<li>Mã này chỉ có hiệu lực trong 15 phút</li>" +
                "<li>Mỗi mã chỉ có thể sử dụng một lần</li>" +
                "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>" +
                "</ul>" +
                "<hr style=\"margin: 30px 0; border: none; height: 1px; background: #e0e0e0;\">" +
                "<p style=\"font-size: 14px; color: #666; text-align: center; margin: 0;\">" +
                "Email: support@dcfashions.com | Hotline: 1900-8386<br>" +
                "Website: www.dcfashions.com" +
                "</p>" +
                "</div>" +
                "</body></html>";
    }

    // Inner class to store reset code with expiry
    private static class ResetCode {
        private final String code;
        private final LocalDateTime expiry;

        public ResetCode(String code, LocalDateTime expiry) {
            this.code = code;
            this.expiry = expiry;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }
}
