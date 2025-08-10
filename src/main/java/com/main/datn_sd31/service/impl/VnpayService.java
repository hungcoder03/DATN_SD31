package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.config.VnpayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Service
public class VnpayService {

    @Autowired
    private VnpayConfig vnpayConfig;

    public String createPaymentUrl(HttpServletRequest req, long amount, String orderInfo,String maHoaDon,String ids) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderType = "bill";
            String vnp_TxnRef = maHoaDon;
            String vnp_IpAddr = req.getRemoteAddr();
            String vnp_TmnCode = "W0QGUAGG";

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", "http://localhost:8080/vnpay-payment-return"+ (ids != null ? "?ids=" + URLEncoder.encode(ids, StandardCharsets.UTF_8) : ""));
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String value = vnp_Params.get(fieldName);
                if ((value != null) && (value.length() > 0)) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                    query.append(fieldName).append('=').append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }

            String secureHash = hmacSHA512("6QFTFA6YY4C76REKGAMPSYRCAW87AINL", hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);
            return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" + "?" + query.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error creating VNPay URL", e);
        }
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}