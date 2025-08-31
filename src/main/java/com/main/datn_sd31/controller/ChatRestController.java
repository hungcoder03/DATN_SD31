package com.main.datn_sd31.controller;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ConversationRepository conversationRepository;
    private final KhachHangRepository khachHangRepository;
    private final ChatSessionService chatSessionService;

    @GetMapping("/find-existing")
    public ResponseEntity<?> findExistingConversation() {
        try {
            // Kiểm tra xem khách hàng đã đăng nhập chưa
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_KHACHHANG"))) {
                return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để sử dụng chat"));
            }
            
            // Lấy thông tin khách hàng từ session
            String customerEmail = auth.getName();
            KhachHang khachHang = khachHangRepository.findByEmail(customerEmail).orElse(null);
            
            if (khachHang == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy thông tin khách hàng"));
            }
            
            // Tìm conversation hiện tại
            var existingConversation = conversationRepository.findByCustomerAndActiveTrue(khachHang);
            
            if (existingConversation.isPresent()) {
                Conversation conv = existingConversation.get();
                
                // Sử dụng ChatSession để lấy nhân viên đang chat
                var currentEmployee = chatSessionService.getCurrentEmployee(conv);
                
                String employeeName = "Đang tìm nhân viên...";
                String employeeAvatar = null;
                Long employeeId = null;
                
                if (currentEmployee.isPresent()) {
                    NhanVien employee = currentEmployee.get();
                    employeeName = employee.getTen();
                    employeeId = employee.getId().longValue();
                    employeeAvatar = employee.getAnh();
                    if (employeeAvatar != null && !employeeAvatar.startsWith("/uploads/")) {
                        employeeAvatar = "/uploads/" + employeeAvatar;
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("conversationId", conv.getId());
                response.put("assignedEmployeeId", employeeId);
                response.put("assignedEmployeeName", employeeName);
                response.put("assignedEmployeeAvatar", employeeAvatar);
                response.put("isExisting", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(Map.of("conversationId", ""));
            }
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN findExistingConversation ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Có lỗi xảy ra khi tìm conversation: " + e.getMessage()));
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startConversation(@RequestBody StartChatRequest request) {
        try {
            // Kiểm tra xem khách hàng đã đăng nhập chưa
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_KHACHHANG"))) {
                return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để sử dụng chat"));
            }
            
            // Lấy thông tin khách hàng từ session
            String customerEmail = auth.getName();
            KhachHang khachHang = khachHangRepository.findByEmail(customerEmail).orElse(null);
            
            if (khachHang == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy thông tin khách hàng"));
            }
            
            // Kiểm tra xem khách hàng đã có conversation active chưa
            var existingConversation = conversationRepository.findByCustomerAndActiveTrue(khachHang);
            
            if (existingConversation.isPresent()) {
                Conversation conv = existingConversation.get();
                
                // Sử dụng ChatSession để lấy nhân viên đang chat
                var currentEmployee = chatSessionService.getCurrentEmployee(conv);
                
                String employeeName = "Đang tìm nhân viên...";
                String employeeAvatar = null;
                Long employeeId = null;
                
                if (currentEmployee.isPresent()) {
                    NhanVien employee = currentEmployee.get();
                    employeeName = employee.getTen();
                    employeeId = employee.getId().longValue();
                    employeeAvatar = employee.getAnh();
                    if (employeeAvatar != null && !employeeAvatar.startsWith("/uploads/")) {
                        employeeAvatar = "/uploads/" + employeeAvatar;
                    }
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("conversationId", conv.getId());
                response.put("assignedEmployeeId", employeeId);
                response.put("assignedEmployeeName", employeeName);
                response.put("assignedEmployeeAvatar", employeeAvatar);
                response.put("customerName", conv.getCustomerName());
                response.put("customerContact", conv.getCustomerContact());
                response.put("isExisting", true);
                response.put("message", "Sử dụng cuộc trò chuyện hiện có");
                return ResponseEntity.ok(response);
            }
            
            // Sử dụng thông tin từ session thay vì request
            String customerName = khachHang.getTen() != null ? khachHang.getTen() : request.name();
            String customerContact = khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai() : 
                                   (khachHang.getEmail() != null ? khachHang.getEmail() : request.contact());
            
            if (customerName == null || customerName.isBlank() || customerContact == null || customerContact.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin khách hàng"));
            }
            
            // Tạo conversation mới KHÔNG gán nhân viên cố định
            Conversation conversation = Conversation.builder()
                    .customer(khachHang)
                    .customerName(customerName)
                    .customerContact(customerContact)
                    .assignedEmployee(null) // Không gán nhân viên cố định
                    .active(true)
                    .lastActivity(LocalDateTime.now())
                    .build();
            
            Conversation savedConversation = conversationRepository.save(conversation);
            
            // Trả về thông tin conversation mới không có nhân viên
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", conversation.getId());
            response.put("assignedEmployeeId", null);
            response.put("assignedEmployeeName", "Đang tìm nhân viên...");
            response.put("assignedEmployeeAvatar", null);
            response.put("customerName", customerName);
            response.put("customerContact", customerContact);
            response.put("isExisting", false);
            response.put("message", "Tạo cuộc trò chuyện mới");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    @GetMapping("/employee-info/{conversationId}")
    public ResponseEntity<?> getEmployeeInfo(@PathVariable Long conversationId) {
        try {
            Conversation conversation = conversationRepository.findById(conversationId)
                .orElse(null);
            
            if (conversation == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Sử dụng ChatSession để lấy nhân viên đang chat
            var currentEmployee = chatSessionService.getCurrentEmployee(conversation);
            
            if (currentEmployee.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("assignedEmployeeName", "Đang tìm nhân viên...");
                response.put("assignedEmployeeAvatar", null);
                return ResponseEntity.ok(response);
            }
            
            NhanVien employee = currentEmployee.get();
            
            // Log thông tin nhân viên để debug
            System.out.println("=== GET EMPLOYEE INFO DEBUG ===");
            System.out.println("Conversation ID: " + conversationId);
            System.out.println("Employee ID: " + employee.getId());
            System.out.println("Employee name: " + employee.getTen());
            System.out.println("Employee avatar: " + employee.getAnh());
            System.out.println("================================");
            
            // Xử lý đường dẫn avatar
            String avatarPath = employee.getAnh();
            if (avatarPath != null && !avatarPath.startsWith("/uploads/")) {
                avatarPath = "/uploads/" + avatarPath;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("assignedEmployeeId", employee.getId());
            response.put("assignedEmployeeName", employee.getTen());
            response.put("assignedEmployeeAvatar", avatarPath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    public record StartChatRequest(String name, String contact) {}
} 