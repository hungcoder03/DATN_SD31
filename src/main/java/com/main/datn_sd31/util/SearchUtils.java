package com.main.datn_sd31.util;

public class SearchUtils {

    // Chuẩn hóa khoảng trắng: bỏ khoảng trắng đầu cuối, thay nhiều khoảng trắng liên tiếp thành 1 dấu cách
    public static String normalizeKeyword(String keyword) {
        if (keyword == null) return null;
        return keyword.trim().replaceAll("\\s+", " ");
    }
}

