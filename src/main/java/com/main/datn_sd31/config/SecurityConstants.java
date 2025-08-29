package com.main.datn_sd31.config;

/**
 * Security Constants - Centralized URL patterns and security configurations
 */
public final class SecurityConstants {
    
    // ==================== URL PATTERNS ====================
    
    // Admin URLs
    public static final String ADMIN_BASE = "/admin";
    public static final String ADMIN_LOGIN = "/admin/dang-nhap";
    public static final String ADMIN_LOGOUT = "/admin/logout";
    public static final String ADMIN_DASHBOARD = "/admin/thong-ke";
    
    // Customer URLs
    public static final String CUSTOMER_BASE = "/khach-hang";
    public static final String CUSTOMER_LOGIN = "/khach-hang/dang-nhap";
    public static final String CUSTOMER_LOGOUT = "/khach-hang/dang-xuat";
    public static final String CUSTOMER_REGISTER = "/khach-hang/dang-ky";
    public static final String CUSTOMER_FORGOT_PASSWORD = "/khach-hang/quen-mat-khau";
    
    // Auth API URLs
    public static final String AUTH_BASE = "/auth";
    public static final String AUTH_STATUS = "/auth/status";
    public static final String AUTH_ADMIN_LOGOUT = "/auth/admin/logout";
    public static final String AUTH_CUSTOMER_LOGOUT = "/auth/customer/logout";
    
    // Public URLs
    public static final String HOME = "/";
    public static final String PRODUCTS = "/san-pham";
    public static final String CART = "/gio-hang";
    public static final String PRODUCT_DETAIL = "/san-pham/chi-tiet/**";
    
    // ==================== ROLES ====================
    
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_NHANVIEN = "ROLE_NHANVIEN";
    public static final String ROLE_KHACHHANG = "ROLE_KHACHHANG";
    
    // ==================== SECURITY MATCHERS ====================
    
    public static final String[] ADMIN_PATTERNS = {"/admin/**"};
    public static final String[] CUSTOMER_PATTERNS = {"/khach-hang/**", "/gio-hang/**"};
    public static final String[] PUBLIC_PATTERNS = {
        "/", "/home", "/login",
        "/uploads/**", "/css/**", "/js/**", "/images/**",
        "/vendors/**", "/webjars/**", "/static/**", "/favicon.ico",
        "/san-pham/**", "/san-pham/chi-tiet/**", "/client-static/**", "/bootstrap-5.3.7-dist/**",
        "/api/auth/**"
    };
    
    public static final String[] ADMIN_PERMIT_ALL = {
        ADMIN_LOGIN, ADMIN_LOGOUT, "/admin/san-pham/api/export-excel"
    };
    
    // Admin URLs cho Quản lý (ADMIN)
    public static final String[] ADMIN_ONLY_URLS = {
        "/admin/quanlytaikhoan/**",  // Quản lý tài khoản
        "/admin/dot-giam-gia/**",    // Đợt giảm giá
        "/admin/phieu-giam-gia/**",  // Phiếu giảm giá
        "/admin/san-pham/**",        // Sản phẩm và thuộc tính
        "/admin/thuong-hieu/**",     // Thương hiệu
        "/admin/kieu-dang/**",       // Kiểu dáng
        "/admin/xuat-xu/**",         // Xuất xứ
        "/admin/chat-lieu/**",       // Chất liệu
        "/admin/danh-muc/**",        // Danh mục
        "/admin/loai-thu/**",        // Loại thú
        "/admin/mau-sac/**",         // Màu sắc
        "/admin/size/**"             // Size
    };
    
    // Admin URLs cho cả Quản lý và Nhân viên
    public static final String[] ADMIN_AND_NHANVIEN_URLS = {
        "/admin/thong-ke/**",        // Tổng quan
        "/admin/ban-hang/**",        // Bán hàng
        "/admin/hoa-don/**",         // Hoá đơn
        "/admin/don-hang/**"         // Đơn hàng
    };
    
    public static final String[] CUSTOMER_PERMIT_ALL = {
        CUSTOMER_LOGIN, CUSTOMER_REGISTER, CUSTOMER_FORGOT_PASSWORD,
        "/khach-hang/danh-sach", "/khach-hang/chi-tiet/**",
        "/khach-hang/public/**", CUSTOMER_LOGOUT, "/api/auth/logout"
    };
    
    public static final String[] CUSTOMER_PROTECTED = {
        "/khach-hang/thong-tin", "/khach-hang/don-hang",
        "/khach-hang/yeu-thich", "/khach-hang/dia-chi"
    };
    
    // ==================== CSRF EXEMPTIONS ====================
    
    public static final String[] CSRF_EXEMPTIONS = {
        "/admin/san-pham/tao-ma-ngau-nhien",
        "/admin/san-pham/api/export-excel"
    };
    
    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
} 