package com.main.datn_sd31.service.impl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GHNService {

//    @Value("${ghn.token}")
//    private String token;
//
//    @Value("${ghn.shop-id}")
//    private String shopId;

    private String token = "396562c3-43a9-11f0-8aa5-92c14b5ac9fb";
    private String shopId = "5824632";

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getProvinces() {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/province";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.get("code") != null && (Integer) body.get("code") == 200) {
                Object dataObj = body.get("data");
                if (dataObj instanceof List<?>) {
                    // Ép kiểu cho an toàn
                    List<Map<String, Object>> provinces = new ArrayList<>();
                    for (Object obj : (List<?>) dataObj) {
                        if (obj instanceof Map) {
                            provinces.add((Map<String, Object>) obj);
                        }
                    }
                    return provinces;
                            }
        }
    } catch (Exception e) {
        // GHN API call failed
            e.printStackTrace();
        }
        return List.of();
    }


    public List<Map<String, Object>> getDistricts(int provinceId) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/district";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("province_id", provinceId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyParams, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();

            if (body != null && Integer.valueOf(200).equals(body.get("code"))) {
                Object data = body.get("data");
                if (data instanceof List<?>) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        return (List<Map<String, Object>>) list;
                                    }
            }
        }
    } catch (Exception e) {
        // GHN API getDistricts call failed
            e.printStackTrace();
        }
        return List.of();
    }


    public List<Map<String, Object>> getWards(int districtId) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("district_id", districtId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyParams, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = response.getBody();

            if (body != null && Integer.valueOf(200).equals(body.get("code"))) {
                Object data = body.get("data");
                if (data instanceof List<?>) {
                    List<?> list = (List<?>) data;
                    if (!list.isEmpty() && list.get(0) instanceof Map) {
                        return (List<Map<String, Object>>) list;
                                    }
            }
        }
    } catch (Exception e) {
        // GHN API getWards call failed
            e.printStackTrace();
        }
        return List.of();
    }

    public List<Map<String, Object>> getAvailableServices(int fromDistrict, int toDistrict) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.set("ShopId", shopId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("shop_id", Integer.parseInt(shopId));  // Nếu shopId là String thì parse int
        bodyParams.put("from_district", fromDistrict);
        bodyParams.put("to_district", toDistrict);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyParams, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            Map<String, Object> body = response.getBody();

            if (body != null && Integer.valueOf(200).equals(body.get("code"))) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) body.get("data");
                return data;
                    }
    } catch (Exception e) {
        // GHN API getAvailableServices call failed
        }

        return Collections.emptyList();
    }



    public Integer getShippingFee(int fromDistrict, int toDistrict, String toWardCode, int weight, int serviceId) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", token);
        headers.set("ShopId", shopId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("from_district_id", fromDistrict);
        bodyParams.put("service_id", serviceId);
        bodyParams.put("to_district_id", toDistrict);
        bodyParams.put("to_ward_code", toWardCode);
        bodyParams.put("weight", weight);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(bodyParams, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            Map<String, Object> body = response.getBody();

            if (body != null && Integer.valueOf(200).equals(body.get("code"))) {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                return (Integer) data.get("total");
                    }
    } catch (Exception e) {
        // GHN API getShippingFee call failed
        }
        return 0;
    }


}
