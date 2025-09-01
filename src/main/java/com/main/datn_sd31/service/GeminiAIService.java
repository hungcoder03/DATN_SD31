package com.main.datn_sd31.service;

import com.main.datn_sd31.dto.GeminiRequest;
import com.main.datn_sd31.dto.GeminiResponse;
import com.main.datn_sd31.entity.AITrainingData;
import com.main.datn_sd31.repository.AITrainingDataRepository;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.Thuonghieurepository;
import com.main.datn_sd31.repository.Dotgiamgiarepository;
import com.main.datn_sd31.config.GeminiConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAIService {

    private final GeminiConfig geminiConfig;
    private final AITrainingDataRepository aiTrainingDataRepository;
    private final SanPhamRepository sanPhamRepository;
    private final Danhmucrepository danhMucRepository;
    private final Thuonghieurepository thuongHieuRepository;
    private final Dotgiamgiarepository dotGiamGiaRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
        Bạn là trợ lý AI thông minh của D&G Fashion - cửa hàng thời trang thú cưng hàng đầu Việt Nam.
        
        Nhiệm vụ của bạn:
        1. Trả lời câu hỏi về sản phẩm, chính sách, phí giao hàng
        2. Tư vấn mua sản phẩm phù hợp cho thú cưng
        3. Cung cấp thông tin về chất liệu, kích thước, xuất xứ
        4. Hỗ trợ về chương trình khuyến mãi và giảm giá
        5. Bạn có những fact về động vật siêu thú vị, mỗi khi được hỏi về động vật, bạn có thể cung cấp cho khách hàng những thông tin thú vị về động vật đó.
       
        Quy tắc:
        - Luôn trả lời bằng tiếng Việt
        - Thân thiện, nhiệt tình nhưng chuyên nghiệp
        - Trả lời ngắn gọn đủ trọng tâm, không lan man gây cảm giác khó chịu
        - Sử dụng emoji phù hợp để tạo cảm giác thân thiện
        - Nếu không biết câu trả lời, hãy nói "Xin lỗi, tôi không thể giúp bạn câu hỏi này. Vui lòng chọn 'Chat với Nhân viên' để được hỗ trợ trực tiếp."
        - Luôn nhắc đến thương hiệu "D&G Fashion" trong câu trả lời
        """;

    public String generateResponse(String userMessage, List<String> conversationHistory) {
        try {
            // 1. Tìm kiếm trong training data trước
            String trainingResponse = findMatchingTrainingData(userMessage);
            if (trainingResponse != null) {
                log.info("Found matching training data for: {}", userMessage);
                return trainingResponse;
            }
            
            // 2. Nếu không tìm thấy, sử dụng Gemini API với context từ training data
            String fullPrompt = buildPrompt(userMessage, conversationHistory);
            GeminiRequest request = createRequest(fullPrompt);
            String response = callGeminiAPI(request);
            
            if (response != null && !response.trim().isEmpty()) {
                return response;
            } else {
                return getFallbackResponse();
            }
            
        } catch (Exception e) {
            log.error("Error generating AI response: ", e);
            return getFallbackResponse();
        }
    }

    private String findMatchingTrainingData(String userMessage) {
        try {
            // Tìm kiếm theo keyword trong training data
            List<AITrainingData> matchingData = aiTrainingDataRepository.searchByKeyword(userMessage);
            
            if (!matchingData.isEmpty()) {
                log.info("Found {} matching training data entries", matchingData.size());
                // Trả về câu trả lời đầu tiên tìm thấy
                return matchingData.get(0).getAnswer();
            }
            
            // Tìm kiếm theo từ khóa chính (loại bỏ từ ngắn và từ dừng)
            String[] keywords = userMessage.toLowerCase()
                .replaceAll("[^a-zàáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ\\s]", " ")
                .split("\\s+");
            
            Set<String> stopWords = Set.of("của", "và", "với", "cho", "từ", "đến", "là", "có", "được", "này", "đó", "gì", "nào", "sao", "thế", "như", "về", "trong", "ngoài", "trên", "dưới", "bên", "giữa", "quanh", "theo", "bằng", "vì", "do", "nếu", "khi", "lúc", "mà", "để", "để", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười");
            
            for (String keyword : keywords) {
                if (keyword.length() > 2 && !stopWords.contains(keyword)) {
                    List<AITrainingData> keywordMatches = aiTrainingDataRepository.searchByKeyword(keyword);
                    if (!keywordMatches.isEmpty()) {
                        log.info("Found training data match for keyword: {}", keyword);
                        return keywordMatches.get(0).getAnswer();
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Error searching training data: ", e);
            return null;
        }
    }

    private String buildPrompt(String userMessage, List<String> conversationHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n\n");
        
        // Thêm training data context
        prompt.append("=== TRAINING DATA CONTEXT ===\n");
        prompt.append(getTrainingDataContext());
        prompt.append("\n=== THÔNG TIN SẢN PHẨM ===\n");
        prompt.append(getProductContext());
        prompt.append("\n=== LỊCH SỬ CHAT ===\n");
        
        // Thêm conversation history
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            for (int i = 0; i < conversationHistory.size(); i += 2) {
                if (i + 1 < conversationHistory.size()) {
                    prompt.append("Khách hàng: ").append(conversationHistory.get(i)).append("\n");
                    prompt.append("AI: ").append(conversationHistory.get(i + 1)).append("\n");
                }
            }
        }
        
        prompt.append("\n=== CÂU HỎI HIỆN TẠI ===\n");
        prompt.append("Khách hàng: ").append(userMessage).append("\n");
        prompt.append("AI: ");
        
        return prompt.toString();
    }

    private String getTrainingDataContext() {
        try {
            List<AITrainingData> allTrainingData = aiTrainingDataRepository.findByIsActiveTrue();
            if (allTrainingData.isEmpty()) {
                return "Chưa có dữ liệu training";
            }
            
            StringBuilder context = new StringBuilder();
            context.append("Dữ liệu training có sẵn (sử dụng để tham khảo khi trả lời):\n\n");
            
            // Nhóm theo category để dễ đọc
            Map<String, List<AITrainingData>> groupedByCategory = allTrainingData.stream()
                .collect(Collectors.groupingBy(AITrainingData::getCategory));
            
            for (Map.Entry<String, List<AITrainingData>> entry : groupedByCategory.entrySet()) {
                String category = entry.getKey();
                List<AITrainingData> dataList = entry.getValue();
                
                context.append("--- ").append(category).append(" ---\n");
                for (AITrainingData data : dataList) {
                    context.append("Q: ").append(data.getQuestion()).append("\n");
                    context.append("A: ").append(data.getAnswer()).append("\n\n");
                }
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.error("Error getting training data context: ", e);
            return "Không thể lấy dữ liệu training";
        }
    }

    private String getProductContext() {
        try {
            StringBuilder context = new StringBuilder();
            
            // Lấy danh mục sản phẩm
            var danhMucs = danhMucRepository.findAll();
            if (!danhMucs.isEmpty()) {
                context.append("Danh mục sản phẩm: ");
                context.append(danhMucs.stream()
                    .map(dm -> dm.getTen())
                    .collect(Collectors.joining(", ")));
                context.append("\n");
            }
            
            // Lấy thương hiệu
            var thuongHieus = thuongHieuRepository.findAll();
            if (!thuongHieus.isEmpty()) {
                context.append("Thương hiệu: ");
                context.append(thuongHieus.stream()
                    .map(th -> th.getTen())
                    .collect(Collectors.joining(", ")));
                context.append("\n");
            }
            
            // Lấy thông tin khuyến mãi (sử dụng findAll và filter)
            var allDotGiamGias = dotGiamGiaRepository.findAll();
            var activeDotGiamGias = allDotGiamGias.stream()
                .filter(dgg -> dgg.getTrangThai() != null && dgg.getTrangThai() == 1)
                .collect(Collectors.toList());
            
            if (!activeDotGiamGias.isEmpty()) {
                context.append("Chương trình khuyến mãi hiện tại: ");
                context.append(activeDotGiamGias.stream()
                    .map(dgg -> dgg.getTen() + " (Giảm " + dgg.getGiaTriDotGiamGia() + "%)")
                    .collect(Collectors.joining(", ")));
                context.append("\n");
            }
            
            return context.toString();
            
        } catch (Exception e) {
            log.error("Error getting product context: ", e);
            return "Không thể lấy thông tin sản phẩm";
        }
    }

    private GeminiRequest createRequest(String prompt) {
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

    private String callGeminiAPI(GeminiRequest request) {
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
            log.error("Error calling Gemini API: ", e);
            return null;
        }
    }

    private String getFallbackResponse() {
        return "Xin lỗi, tôi không thể giúp bạn câu hỏi này. Vui lòng chọn 'Chat với Nhân viên' để được hỗ trợ trực tiếp. 🐾";
    }

    public List<AITrainingData> getAllTrainingData() {
        return aiTrainingDataRepository.findByIsActiveTrue();
    }

    public List<String> getAllCategories() {
        return aiTrainingDataRepository.findAllActiveCategories();
    }

    public AITrainingData saveTrainingData(AITrainingData trainingData) {
        trainingData.setUpdatedAt(LocalDateTime.now());
        return aiTrainingDataRepository.save(trainingData);
    }

    public void deleteTrainingData(Long id) {
        aiTrainingDataRepository.deleteById(id);
    }
} 