package com.main.datn_sd31.service;

/**
 * Service interface for password reset operations
 */
public interface PasswordResetService {
    
    /**
     * Generate verification code and send email
     * @param email Customer email
     * @return true if successful, false if email not found
     */
    boolean generateAndSendCode(String email);
    
    /**
     * Verify the verification code
     * @param email Customer email
     * @param code Verification code
     * @return true if valid, false otherwise
     */
    boolean verifyCode(String email, String code);
    
    /**
     * Reset password with new password
     * @param email Customer email
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    boolean resetPassword(String email, String newPassword);
    
    /**
     * Clean up expired codes (called by scheduler)
     */
    void cleanupExpiredCodes();
}
