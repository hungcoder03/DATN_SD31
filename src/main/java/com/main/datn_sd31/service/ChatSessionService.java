package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.ChatSession;
import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.NhanVien;

import java.util.Optional;

public interface ChatSessionService {
    
    // Tạo session mới cho nhân viên với conversation
    ChatSession createSession(Conversation conversation, NhanVien employee);
    
    // Kết thúc session hiện tại
    ChatSession endCurrentSession(Conversation conversation);
    
    // Lấy session đang active
    Optional<ChatSession> getActiveSession(Conversation conversation);
    
    // Lấy nhân viên đang chat với conversation
    Optional<NhanVien> getCurrentEmployee(Conversation conversation);
    
    // Tăng số tin nhắn trong session
    void incrementMessageCount(ChatSession session);
    
    // Tìm nhân viên có ít session nhất để gán
    NhanVien findLeastLoadedEmployee();
    
    // Chuyển conversation sang nhân viên khác
    ChatSession transferToEmployee(Conversation conversation, NhanVien newEmployee);
} 