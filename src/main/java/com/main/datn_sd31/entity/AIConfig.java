package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_config")
public class AIConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String modelName;

    @Column(name = "api_key", nullable = false, length = 500, columnDefinition = "NVARCHAR(500)")
    private String apiKey;

    @Column(name = "max_tokens")
    private Integer maxTokens;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 