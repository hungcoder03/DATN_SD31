package com.main.datn_sd31.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.main.datn_sd31.vnp.Config;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/vnpay")
public class VnpayController {

    @PostMapping("/tao-giao-dich")
    public ResponseEntity<?> taoGiaoDich(HttpServletRequest req,
                                         @RequestParam("amount") int amount,
                                         @RequestParam(value = "bankCode", required = false) String bankCode,
                                         @RequestParam(value = "language", required = false) String language) throws UnsupportedEncodingException {

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long vnp_Amount = amount * 100L;
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = Config.getIpAddress(req);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", Config.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_Locale", language != null ? language : "vn");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> it = fieldNames.iterator(); it.hasNext(); ) {
            String fieldName = it.next();
            String fieldValue = vnp_Params.get(fieldName);
            hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                    .append("=")
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
            if (it.hasNext()) {
                hashData.append("&");
                query.append("&");
            }
        }

        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        String paymentUrl = Config.vnp_PayUrl + "?" + query;
        JsonObject response = new JsonObject();
        response.addProperty("code", "00");
        response.addProperty("message", "success");
        response.addProperty("url", paymentUrl);

        return ResponseEntity.ok(new Gson().toJson(response));
    }
}
