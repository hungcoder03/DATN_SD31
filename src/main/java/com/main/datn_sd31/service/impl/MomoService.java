package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.util.HttpClientUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MomoService {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    public String createPaymentUrl(int amount, String orderId, String returnUrl) throws Exception {
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán đơn hàng " + orderId;
        String extraData = "";

        Map<String, String> params = new TreeMap<>();
        params.put("partnerCode", partnerCode);
        params.put("accessKey", accessKey);
        params.put("requestId", requestId);
        params.put("amount", String.valueOf(amount));
        params.put("orderId", orderId);
        params.put("orderInfo", orderInfo);
        params.put("returnUrl", returnUrl);
        params.put("notifyUrl", ipnUrl);
        params.put("extraData", extraData);
        params.put("requestType", "captureWallet");

        String rawHash = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String signature = hmacSHA256(rawHash, secretKey);
        params.put("signature", signature);

        JSONObject json = new JSONObject(params);

        // Gửi request HTTP tới MOMO (có thể dùng RestTemplate, HttpClient, WebClient,...)
        String response = HttpClientUtils.sendPost("https://test-payment.momo.vn/v2/gateway/api/create", json.toString());

        JSONObject responseJson = new JSONObject(response);
        return responseJson.getString("payUrl"); // Redirect người dùng tới URL này
    }

    private String hmacSHA256(String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("HmacSHA256 error", e);
        }
    }
}
