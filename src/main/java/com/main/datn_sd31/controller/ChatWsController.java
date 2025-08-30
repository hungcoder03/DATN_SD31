package com.main.datn_sd31.controller;

import com.main.datn_sd31.Enum.MessageSenderType;
import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.Message;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;

import java.time.LocalDateTime;


@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversation.{conversationId}")
    public ChatMessageView handleChatMessage(@DestinationVariable Long conversationId, ChatMessagePayload payload) {
        try {
            // Lưu tin nhắn vào database
            Message message = Message.builder()
                    .conversation(conversationRepository.findById(conversationId).orElseThrow())
                    .senderType(payload.senderType())
                    .senderId(payload.senderId())
                    .content(payload.content())
                    .isRead(false) // Tin nhắn mới luôn chưa đọc
                    .build();
            
            messageRepository.save(message);
            
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
            
            return ChatMessageView.from(message);
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
            System.out.println("=== MARKING CONVERSATION AS READ ===");
            System.out.println("Conversation ID: " + conversationId);
            
            // Kiểm tra conversation có tồn tại không
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation == null) {
                System.err.println("Conversation not found: " + conversationId);
                return;
            }
            System.out.println("Found conversation: " + conversation.getCustomerName());
            
            // Đếm số tin nhắn trước khi update
            long beforeCount = messageRepository.countByConversationActiveTrueAndSenderTypeAndIsReadFalse(MessageSenderType.CUSTOMER);
            System.out.println("Unread messages before update: " + beforeCount);
            
            // Đánh dấu tất cả tin nhắn từ khách hàng trong cuộc trò chuyện là đã đọc
            int updatedCount = messageRepository.markAsReadByConversationAndSenderType(conversationId, MessageSenderType.CUSTOMER);
            System.out.println("Updated " + updatedCount + " messages as read");
            
            // Đếm số tin nhắn sau khi update
            long afterCount = messageRepository.countByConversationActiveTrueAndSenderTypeAndIsReadFalse(MessageSenderType.CUSTOMER);
            System.out.println("Unread messages after update: " + afterCount);
            
            // Gửi thông báo cập nhật số đếm
            ChatNotificationPayload notification = new ChatNotificationPayload(
                "CONVERSATION_READ", 
                MessageSenderType.EMPLOYEE, 
                conversationId, 
                null
            );
            messagingTemplate.convertAndSend("/topic/chat.notifications", notification);
            System.out.println("Sent CONVERSATION_READ notification");
            System.out.println("=== END MARKING CONVERSATION AS READ ===");
        } catch (Exception e) {
            System.err.println("Error marking conversation as read: " + e.getMessage());
            e.printStackTrace();
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