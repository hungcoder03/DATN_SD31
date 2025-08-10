package com.main.datn_sd31.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnpayConfig {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;

    // Getter và Setter
}