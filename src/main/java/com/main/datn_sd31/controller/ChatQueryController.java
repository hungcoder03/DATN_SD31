package com.main.datn_sd31.controller;

import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.Message;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.MessageRepository;
import com.main.datn_sd31.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.main.datn_sd31.Enum.MessageSenderType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatQueryController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatSessionService chatSessionService;

    @GetMapping("/conversations")
    public List<ConversationView> listActiveConversations() {
        return conversationRepository.findByActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(c -> {
                    Message last = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(c);
                    long unreadCount = messageRepository.countByConversationAndSenderTypeAndIsReadFalse(c, MessageSenderType.CUSTOMER);
                    // Sử dụng ChatSession để lấy nhân viên đang chat
                    var currentEmployee = chatSessionService.getCurrentEmployee(c);
                    Long assignedEmployeeId = null;
                    String assignedEmployeeName = null;
                    
                    if (currentEmployee.isPresent()) {
                        assignedEmployeeId = currentEmployee.get().getId().longValue();
                        assignedEmployeeName = currentEmployee.get().getTen();
                    }
                    
                    return new ConversationView(
                            c.getId(),
                            c.getCustomerName(),
                            c.getCustomerContact(),
                            assignedEmployeeId,
                            assignedEmployeeName,
                            last != null ? last.getContent() : null,
                            last != null ? last.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .sorted((c1, c2) -> {
                    // Sắp xếp theo thời gian tin nhắn cuối cùng, mới nhất lên đầu
                    if (c1.lastMessageTime() == null && c2.lastMessageTime() == null) return 0;
                    if (c1.lastMessageTime() == null) return 1; // c1 lên cuối
                    if (c2.lastMessageTime() == null) return -1; // c2 lên cuối
                    return c2.lastMessageTime().compareTo(c1.lastMessageTime()); // Mới nhất lên đầu
                })
                .toList();
    }

    @GetMapping("/conversations/employee/{employeeId}")
    public List<ConversationView> listConversationsForEmployee(@PathVariable Integer employeeId) {
        return conversationRepository.findByActiveTrueOrderByCreatedAtAsc()
                .stream()
                .map(c -> {
                    Message last = messageRepository.findTop1ByConversationOrderByCreatedAtDesc(c);
                    long unreadCount = messageRepository.countByConversationAndSenderTypeAndIsReadFalse(c, MessageSenderType.CUSTOMER);
                    // Sử dụng ChatSession để lấy nhân viên đang chat
                    var currentEmployee = chatSessionService.getCurrentEmployee(c);
                    Long assignedEmployeeId = null;
                    String assignedEmployeeName = null;
                    
                    if (currentEmployee.isPresent()) {
                        assignedEmployeeId = currentEmployee.get().getId().longValue();
                        assignedEmployeeName = currentEmployee.get().getTen();
                    }
                    
                    return new ConversationView(
                            c.getId(),
                            c.getCustomerName(),
                            c.getCustomerContact(),
                            assignedEmployeeId,
                            assignedEmployeeName,
                            last != null ? last.getContent() : null,
                            last != null ? last.getCreatedAt() : null,
                            unreadCount
                    );
                })
                .filter(c -> {
                    // Lọc conversation: hiển thị nếu chưa gán hoặc đã gán cho nhân viên này
                    return c.assignedEmployeeId() == null || c.assignedEmployeeId().equals(employeeId.longValue());
                })
                .sorted((c1, c2) -> {
                    // Sắp xếp theo thời gian tin nhắn cuối cùng, mới nhất lên đầu
                    if (c1.lastMessageTime() == null && c2.lastMessageTime() == null) return 0;
                    if (c1.lastMessageTime() == null) return 1; // c1 lên cuối
                    if (c2.lastMessageTime() == null) return -1; // c2 lên cuối
                    return c2.lastMessageTime().compareTo(c1.lastMessageTime()); // Mới nhất lên đầu
                })
                .toList();
    }

    @GetMapping("/messages/{conversationId}")
    public List<MessageView> getRecentMessages(@PathVariable Long conversationId) {
        Conversation c = conversationRepository.findById(conversationId).orElse(null);
        if (c == null) {
            return new ArrayList<>();
        }
        List<Message> latest = messageRepository.findTop50ByConversationOrderByCreatedAtDesc(c);
        latest.sort(Comparator.comparing(Message::getCreatedAt));
        return latest.stream().map(MessageView::from).toList();
    }

    @GetMapping("/messages/all/{conversationId}")
    public List<MessageView> getAllMessagesAsc(@PathVariable Long conversationId) {
        Conversation c = conversationRepository.findById(conversationId).orElse(null);
        if (c == null) {
            return new ArrayList<>();
        }
        
        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(c);
        return messages.stream().map(MessageView::from).toList();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            // Đếm số tin nhắn chưa đọc từ khách hàng trong các cuộc trò chuyện đang hoạt động
            long count = messageRepository.countByConversationActiveTrueAndSenderTypeAndIsReadFalse(MessageSenderType.CUSTOMER);
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/unread-count/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getUnreadCountForEmployee(@PathVariable Integer employeeId) {
        try {
            // Đếm số tin nhắn chưa đọc từ khách hàng trong các cuộc trò chuyện của nhân viên này
            long count = conversationRepository.findByActiveTrueOrderByCreatedAtAsc()
                    .stream()
                    .filter(c -> {
                        // Chỉ đếm conversation chưa gán hoặc đã gán cho nhân viên này
                        var currentEmployee = chatSessionService.getCurrentEmployee(c);
                        return currentEmployee.isEmpty() || currentEmployee.get().getId().equals(employeeId);
                    })
                    .mapToLong(c -> messageRepository.countByConversationAndSenderTypeAndIsReadFalse(c, MessageSenderType.CUSTOMER))
                    .sum();
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("count", 0);
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/mark-read/{conversationId}")
    public ResponseEntity<Map<String, Object>> markConversationAsRead(@PathVariable Long conversationId) {
        try {
            // Đánh dấu tất cả tin nhắn từ khách hàng trong cuộc trò chuyện là đã đọc
            messageRepository.markAsReadByConversationAndSenderType(conversationId, MessageSenderType.CUSTOMER);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã đánh dấu tất cả tin nhắn là đã đọc");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public record ConversationView(Long id, String customerName, String customerContact, Long assignedEmployeeId, String assignedEmployeeName,
                                   String lastMessage, LocalDateTime lastMessageTime, Long unreadCount) {
        public static ConversationView from(Conversation c, Message last) {
            return new ConversationView(
                    c.getId(),
                    c.getCustomerName(),
                    c.getCustomerContact(),
                    c.getAssignedEmployee() != null ? c.getAssignedEmployee().getId().longValue() : null,
                    c.getAssignedEmployee() != null ? c.getAssignedEmployee().getTen() : null,
                    last != null ? last.getContent() : null,
                    last != null ? last.getCreatedAt() : null,
                    0L // Sẽ được cập nhật sau
            );
        }
    }

    public record MessageView(Long id, String content, MessageSenderType senderType, Integer senderId, LocalDateTime createdAt, Boolean isRead) {
        public static MessageView from(Message message) {
            return new MessageView(
                message.getId(),
                message.getContent(),
                message.getSenderType(),
                message.getSenderId(),
                message.getCreatedAt(),
                message.getIsRead()
            );
        }
    }
} 