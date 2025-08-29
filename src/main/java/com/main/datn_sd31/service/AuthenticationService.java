package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;

import java.util.Map;

/**
 * Service interface for authentication and authorization operations
 */
public interface AuthenticationService {
    
    /**
     * Get current authenticated user information
     * @return Map containing user details or null if not authenticated
     */
    Map<String, Object> getCurrentUserInfo();
    
    /**
     * Get current customer if authenticated
     * @return KhachHang entity or null
     */
    KhachHang getCurrentCustomer();
    
    /**
     * Get current employee if authenticated
     * @return NhanVien entity or null
     */
    NhanVien getCurrentEmployee();
    
    /**
     * Check if current user has specific role
     * @param role Role to check (e.g., "ROLE_ADMIN", "ROLE_KHACHHANG")
     * @return true if user has the role, false otherwise
     */
    boolean hasRole(String role);
    
    /**
     * Check if current user is authenticated
     * @return true if authenticated, false otherwise
     */
    boolean isAuthenticated();
    
    /**
     * Check if current user is customer
     * @return true if customer, false otherwise
     */
    boolean isCustomer();
    
    /**
     * Check if current user is admin
     * @return true if admin, false otherwise
     */
    boolean isAdmin();
    
    /**
     * Check if current user is employee
     * @return true if employee, false otherwise
     */
    boolean isEmployee();
} 