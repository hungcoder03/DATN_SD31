package com.main.datn_sd31.service;

import com.main.datn_sd31.config.GeminiConfig;
import com.main.datn_sd31.dto.GeminiRequest;
import com.main.datn_sd31.dto.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DichVuBanDichAiService {
    
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Mapping ngôn ngữ
    private final Map<String, String> ngonNguMapping = new HashMap<String, String>() {{
        put("vi", "tiếng Việt");
        put("en", "tiếng Anh");
        put("ja", "tiếng Nhật");
        put("ko", "tiếng Hàn");
        put("zh", "tiếng Trung");
    }};
    
    /**
     * Dịch văn bản bằng Gemini AI
     */
    public String dichVanBan(String vanBan, String ngonNguNguon, String ngonNguDich) {
        try {
            String tenNgonNguNguon = ngonNguMapping.getOrDefault(ngonNguNguon, ngonNguNguon);
            String tenNgonNguDich = ngonNguMapping.getOrDefault(ngonNguDich, ngonNguDich);
            
            String prompt = String.format(
                "Bạn là chuyên gia dịch thuật chuyên nghiệp. " +
                "Hãy dịch văn bản sau từ %s sang %s một cách chính xác và tự nhiên. " +
                "Chỉ trả về bản dịch, không giải thích thêm.\n\n" +
                "Văn bản cần dịch: %s",
                tenNgonNguNguon, tenNgonNguDich, vanBan
            );
            
            GeminiRequest request = taoRequest(prompt);
            String response = goiGeminiAPI(request);
            
            if (response != null && !response.trim().isEmpty()) {
                // Làm sạch response
                response = response.trim();
                if (response.startsWith("\"") && response.endsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }
                return response;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi dịch văn bản bằng AI: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Dịch nhiều văn bản cùng lúc
     */
    public Map<String, String> dichNhieuVanBan(Map<String, String> vanBanMap, String ngonNguNguon, String ngonNguDich) {
        Map<String, String> ketQua = new HashMap<>();
        
        for (Map.Entry<String, String> entry : vanBanMap.entrySet()) {
            String key = entry.getKey();
            String vanBan = entry.getValue();
            
            String banDich = dichVanBan(vanBan, ngonNguNguon, ngonNguDich);
            if (banDich != null) {
                ketQua.put(key, banDich);
            } else {
                ketQua.put(key, vanBan); // Fallback về văn bản gốc
            }
        }
        
        return ketQua;
    }
    
    /**
     * Dịch văn bản với context cụ thể (ví dụ: sản phẩm, danh mục)
     */
    public String dichVanBanVoiContext(String vanBan, String ngonNguNguon, String ngonNguDich, String context) {
        try {
            String tenNgonNguNguon = ngonNguMapping.getOrDefault(ngonNguNguon, ngonNguNguon);
            String tenNgonNguDich = ngonNguMapping.getOrDefault(ngonNguDich, ngonNguDich);
            
            String prompt = String.format(
                "Bạn là chuyên gia dịch thuật chuyên nghiệp về %s. " +
                "Hãy dịch văn bản sau từ %s sang %s một cách chính xác và tự nhiên, " +
                "phù hợp với ngữ cảnh %s. Chỉ trả về bản dịch, không giải thích thêm.\n\n" +
                "Văn bản cần dịch: %s",
                context, tenNgonNguNguon, tenNgonNguDich, context, vanBan
            );
            
            GeminiRequest request = taoRequest(prompt);
            String response = goiGeminiAPI(request);
            
            if (response != null && !response.trim().isEmpty()) {
                response = response.trim();
                if (response.startsWith("\"") && response.endsWith("\"")) {
                    response = response.substring(1, response.length() - 1);
                }
                return response;
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi dịch văn bản với context: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private GeminiRequest taoRequest(String prompt) {
        GeminiRequest.Part part = GeminiRequest.Part.builder()
            .text(prompt)
            .build();
        
        GeminiRequest.Content content = GeminiRequest.Content.builder()
            .parts(Arrays.asList(part))
            .role("user")
            .build();
        
        return GeminiRequest.builder()
            .contents(Arrays.asList(content))
            .build();
    }
    
    private String goiGeminiAPI(GeminiRequest request) {
        try {
            String url = String.format("%s/%s:generateContent?key=%s",
                geminiConfig.getBaseUrl(),
                geminiConfig.getModelName(),
                geminiConfig.getApiKey());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GeminiResponse geminiResponse = objectMapper.readValue(response.getBody(), GeminiResponse.class);
                
                if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                    var candidate = geminiResponse.getCandidates().get(0);
                    if (candidate.getContent() != null && !candidate.getContent().getParts().isEmpty()) {
                        return candidate.getContent().getParts().get(0).getText();
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi gọi Gemini API: {}", e.getMessage(), e);
            return null;
        }
    }
}
