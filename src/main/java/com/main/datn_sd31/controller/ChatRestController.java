package com.main.datn_sd31.controller;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.ChatAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import java.util.Map;

@RestController
@Component
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ConversationRepository conversationRepository;
    private final ChatAssignmentService chatAssignmentService;
    private final KhachHangRepository khachHangRepository;

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== TEST ENDPOINT ===");
        System.out.println("Authentication: " + (auth != null ? auth.getName() : "null"));
        System.out.println("Authenticated: " + (auth != null ? auth.isAuthenticated() : "null"));
        if (auth != null) {
            System.out.println("Authorities: " + auth.getAuthorities());
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "ChatRestController is working!",
            "auth", auth != null ? auth.getName() : "null",
            "authenticated", auth != null ? auth.isAuthenticated() : false,
            "authorities", auth != null ? auth.getAuthorities().toString() : "null"
        ));
    }

    @PostMapping("/start")
    public ResponseEntity<?> startConversation(@RequestBody StartChatRequest request) {
        try {
            System.out.println("=== STARTING CONVERSATION ===");
            System.out.println("Request: " + request);
            
            // Kiểm tra xem khách hàng đã đăng nhập chưa
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication: " + (auth != null ? auth.getName() : "null"));
            
            if (auth == null || !auth.isAuthenticated() || !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_KHACHHANG"))) {
                System.out.println("Authentication failed - not logged in or not customer");
                return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập để sử dụng chat"));
            }
            
            // Lấy thông tin khách hàng từ session
            String customerEmail = auth.getName();
            System.out.println("Customer email: " + customerEmail);
            
            KhachHang khachHang = khachHangRepository.findByEmail(customerEmail).orElse(null);
            System.out.println("Found customer: " + (khachHang != null ? khachHang.getTen() : "null"));
            
            if (khachHang == null) {
                System.out.println("Customer not found in database");
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy thông tin khách hàng"));
            }
            
            // Sử dụng thông tin từ session thay vì request
            String customerName = khachHang.getTen() != null ? khachHang.getTen() : request.name();
            String customerContact = khachHang.getSoDienThoai() != null ? khachHang.getSoDienThoai() : 
                                   (khachHang.getEmail() != null ? khachHang.getEmail() : request.contact());
            
            System.out.println("Customer name: " + customerName);
            System.out.println("Customer contact: " + customerContact);
            
            if (customerName == null || customerName.isBlank() || customerContact == null || customerContact.isBlank()) {
                System.out.println("Missing customer information");
                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin khách hàng"));
            }
            
            System.out.println("Finding available employee...");
            NhanVien assigned = chatAssignmentService.findLeastLoadedAvailableEmployee();
            System.out.println("Assigned employee: " + (assigned != null ? assigned.getTen() : "null"));
            
            // Cho phép tạo conversation ngay cả khi không có nhân viên available
            System.out.println("Creating conversation...");
            Conversation conversation = Conversation.builder()
                    .customerName(customerName)
                    .customerContact(customerContact)
                    .assignedEmployee(assigned) // Có thể là null
                    .active(true)
                    .build();
            
            System.out.println("Saving conversation...");
            conversationRepository.save(conversation);
            System.out.println("Conversation saved with ID: " + conversation.getId());
            
            System.out.println("=== CONVERSATION STARTED SUCCESSFULLY ===");
            
            return ResponseEntity.ok(Map.of(
                    "conversationId", conversation.getId(),
                    "assignedEmployeeId", assigned != null ? assigned.getId() : null,
                    "assignedEmployeeName", assigned != null ? assigned.getTen() : null,
                    "customerName", customerName,
                    "customerContact", customerContact
            ));
        } catch (Exception e) {
            System.err.println("=== ERROR STARTING CONVERSATION ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi server: " + e.getMessage()));
        }
    }

    public record StartChatRequest(String name, String contact) {}
} 