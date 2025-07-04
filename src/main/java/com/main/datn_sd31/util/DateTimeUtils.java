package com.main.datn_sd31.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("utils_date")
public class DateTimeUtils {

    // Mặc định định dạng chuẩn: HH:mm:ss dd/MM/yyyy
    private static final String DEFAULT_PATTERN = "HH:mm:ss dd/MM/yyyy";

    // Định dạng cho LocalDateTime
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }
}