package com.main.datn_sd31.entity;

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
@Table(name = "conversation", indexes = {
    @Index(name = "IX_conversation_customer_active", columnList = "customer_id, active")
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized
    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Nationalized
    @Column(name = "customer_contact", length = 100)
    private String customerContact; // phone or email

    // Relationship với KhachHang - Mỗi khách hàng chỉ có 1 conversation active
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", unique = true)
    private KhachHang customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private NhanVien assignedEmployee;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    // Trường để theo dõi hoạt động cuối cùng - Cập nhật mỗi khi có tin nhắn mới
    @Column(name = "last_activity")
    @Builder.Default
    private LocalDateTime lastActivity = LocalDateTime.now();
} 