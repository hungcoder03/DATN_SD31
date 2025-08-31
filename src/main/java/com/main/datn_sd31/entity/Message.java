package com.main.datn_sd31.entity;

import com.main.datn_sd31.Enum.MessageSenderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", length = 20)
    private MessageSenderType senderType;

    @Column(name = "sender_id")
    private Integer senderId; // null for customer

    @Nationalized
    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
} 