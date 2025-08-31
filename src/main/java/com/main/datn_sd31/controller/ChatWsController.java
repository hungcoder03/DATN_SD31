package com.main.datn_sd31.controller;

import com.main.datn_sd31.Enum.MessageSenderType;
import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.Message;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.MessageRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChatSessionService chatSessionService;
    private final NhanVienRepository nhanVienRepository;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation.{conversationId}")
    public ChatMessageView handleChatMessage(@DestinationVariable Long conversationId, ChatMessagePayload payload) {
        try {
            if (conversationId == null) {
                throw new RuntimeException("Conversation ID cannot be null");
            }
            
            // Cập nhật lastActivity của conversation
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                // Thay vì throw exception, log lỗi và gửi thông báo lỗi
                System.err.println("Warning: Conversation not found with ID: " + conversationId + ". This might indicate a stale conversation ID.");
                
                // Gửi thông báo lỗi về client
                messagingTemplate.convertAndSend("/topic/conversation." + conversationId, 
                    new ChatMessageView(null, "Lỗi: Cuộc trò chuyện không tồn tại. Vui lòng tạo cuộc trò chuyện mới.", 
                    MessageSenderType.EMPLOYEE, null, LocalDateTime.now(), false));
                
                return null;
            }
            conversation.setLastActivity(LocalDateTime.now());
            conversationRepository.save(conversation);
            
            // Xử lý ChatSession khi nhân viên gửi tin nhắn
            if (payload.senderType() == MessageSenderType.EMPLOYEE) {
                // Kiểm tra xem conversation đã có nhân viên chưa
                var currentEmployee = chatSessionService.getCurrentEmployee(conversation);
                
                if (currentEmployee.isEmpty()) {
                    // Chưa có nhân viên, tạo session mới với nhân viên gửi tin nhắn
                    System.out.println("=== EMPLOYEE JOINED CONVERSATION ===");
                    System.out.println("Conversation ID: " + conversationId);
                    System.out.println("Employee ID: " + payload.senderId());
                    System.out.println("Creating new chat session...");
                    
                    // Tạo session mới với nhân viên có ID = senderId
                    if (payload.senderId() != null) {
                        var employee = nhanVienRepository.findById(payload.senderId());
                        if (employee.isPresent()) {
                            chatSessionService.createSession(conversation, employee.get());
                            System.out.println("Created chat session for employee: " + employee.get().getTen());
                            
                            // Gửi thông báo cập nhật thông tin nhân viên cho client
                            Map<String, Object> employeeUpdate = new HashMap<>();
                            employeeUpdate.put("type", "EMPLOYEE_ASSIGNED");
                            employeeUpdate.put("conversationId", conversationId);
                            employeeUpdate.put("employeeId", employee.get().getId());
                            employeeUpdate.put("employeeName", employee.get().getTen());
                            employeeUpdate.put("employeeAvatar", employee.get().getAnh() != null && !employee.get().getAnh().startsWith("/uploads/") ? 
                                             "/uploads/" + employee.get().getAnh() : employee.get().getAnh());
                            
                            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, employeeUpdate);
                        } else {
                            System.err.println("Employee not found with ID: " + payload.senderId());
                        }
                    }
                } else {
                    // Đã có nhân viên, cập nhật session hiện tại
                    var activeSession = chatSessionService.getActiveSession(conversation);
                    if (activeSession.isPresent()) {
                        chatSessionService.incrementMessageCount(activeSession.get());
                    }
                }
            }
            
            // Xử lý ChatSession khi nhân viên gửi tin nhắn
            if (payload.senderType() == MessageSenderType.EMPLOYEE) {
                // Kiểm tra xem conversation đã có nhân viên chưa
                var currentEmployee = chatSessionService.getCurrentEmployee(conversation);
                
                if (currentEmployee.isEmpty()) {
                    // Chưa có nhân viên, tạo session mới với nhân viên gửi tin nhắn
                    // Cần lấy thông tin nhân viên từ senderId
                    // TODO: Cần xác định cách lấy thông tin nhân viên từ senderId
                    System.out.println("=== EMPLOYEE JOINED CONVERSATION ===");
                    System.out.println("Conversation ID: " + conversationId);
                    System.out.println("Employee ID: " + payload.senderId());
                    System.out.println("Creating new chat session...");
                    
                    // Tạm thời tạo session với nhân viên có ID = senderId
                    // Cần implement logic để lấy NhanVien từ senderId
                } else {
                    // Đã có nhân viên, cập nhật session hiện tại
                    var activeSession = chatSessionService.getActiveSession(conversation);
                    if (activeSession.isPresent()) {
                        chatSessionService.incrementMessageCount(activeSession.get());
                    }
                }
            }
            
            // Lưu tin nhắn vào database
            Message message = Message.builder()
                    .conversation(conversation)
                    .senderType(payload.senderType())
                    .senderId(payload.senderId())
                    .content(payload.content())
                    .isRead(false)
                    .build();
            
            Message savedMessage = messageRepository.save(message);
            
            // Gửi thông báo tin nhắn mới nếu là từ khách hàng
            if (payload.senderType() == MessageSenderType.CUSTOMER) {
                ChatNotificationPayload notification = new ChatNotificationPayload(
                    "NEW_MESSAGE", 
                    payload.senderType(), 
                    conversationId, 
                    payload.content()
                );
                messagingTemplate.convertAndSend("/topic/chat.notifications", notification);
            }
            
            return ChatMessageView.from(savedMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error processing chat message", e);
        }
    }

    @MessageMapping("/typing/{conversationId}")
    public void handleTyping(@DestinationVariable Long conversationId, TypingPayload payload) {
        messagingTemplate.convertAndSend("/topic/typing." + conversationId, payload);
    }

    @MessageMapping("/chat.mark-read")
    public void markAllAsRead() {
        // Gửi thông báo reset số đếm tin nhắn chưa đọc
        ChatNotificationPayload notification = new ChatNotificationPayload(
            "MARK_READ", 
            MessageSenderType.EMPLOYEE, 
            null, 
            null
        );
        messagingTemplate.convertAndSend("/topic/chat.notifications", notification);
    }

    @MessageMapping("/chat.mark-conversation-read/{conversationId}")
    public void markConversationAsRead(@DestinationVariable Long conversationId) {
        try {
            // Kiểm tra conversation có tồn tại không
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                return;
            }
            
            // Đánh dấu tất cả tin nhắn từ khách hàng trong cuộc trò chuyện là đã đọc
            messageRepository.markAsReadByConversationAndSenderType(conversationId, MessageSenderType.CUSTOMER);
            
            // Gửi thông báo cập nhật số đếm
            ChatNotificationPayload notification = new ChatNotificationPayload(
                "CONVERSATION_READ", 
                MessageSenderType.EMPLOYEE, 
                conversationId, 
                null
            );
            messagingTemplate.convertAndSend("/topic/chat.notifications", notification);
        } catch (Exception e) {
            throw new RuntimeException("Error marking conversation as read", e);
        }
    }

    public record ChatMessagePayload(MessageSenderType senderType, Integer senderId, String content) {}

    public record ChatMessageView(Long id, String content, MessageSenderType senderType, Integer senderId, LocalDateTime createdAt, Boolean isRead) {
        public static ChatMessageView from(Message m) {
            return new ChatMessageView(
                    m.getId(),
                    m.getContent(),
                    m.getSenderType(),
                    m.getSenderId(),
                    m.getCreatedAt(),
                    m.getIsRead()
            );
        }
    }

    public record TypingPayload(MessageSenderType senderType, boolean typing) {}

    public record ChatNotificationPayload(String type, MessageSenderType senderType, Long conversationId, String content) {}
} 