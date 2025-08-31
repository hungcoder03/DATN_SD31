package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.ChatSession;
import com.main.datn_sd31.entity.Conversation;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.ChatSessionRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatSessionServiceImpl implements ChatSessionService {
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Override
    public ChatSession createSession(Conversation conversation, NhanVien employee) {
        // Kết thúc session hiện tại nếu có
        endCurrentSession(conversation);
        
        // Tạo session mới
        ChatSession newSession = ChatSession.builder()
                .conversation(conversation)
                .employee(employee)
                .startTime(LocalDateTime.now())
                .isActive(true)
                .messageCount(0)
                .build();
        
        return chatSessionRepository.save(newSession);
    }
    
    @Override
    public ChatSession endCurrentSession(Conversation conversation) {
        Optional<ChatSession> currentSession = getActiveSession(conversation);
        if (currentSession.isPresent()) {
            ChatSession session = currentSession.get();
            session.setEndTime(LocalDateTime.now());
            session.setIsActive(false);
            return chatSessionRepository.save(session);
        }
        return null;
    }
    
    @Override
    public Optional<ChatSession> getActiveSession(Conversation conversation) {
        return chatSessionRepository.findByConversationAndIsActiveTrue(conversation);
    }
    
    @Override
    public Optional<NhanVien> getCurrentEmployee(Conversation conversation) {
        Optional<ChatSession> activeSession = getActiveSession(conversation);
        return activeSession.map(ChatSession::getEmployee);
    }
    
    @Override
    public void incrementMessageCount(ChatSession session) {
        session.setMessageCount(session.getMessageCount() + 1);
        chatSessionRepository.save(session);
    }
    
    @Override
    public NhanVien findLeastLoadedEmployee() {
        // Lấy danh sách nhân viên có ít session nhất
        List<Integer> employeeIds = chatSessionRepository.findEmployeeIdsOrderByActiveSessionCount();
        
        // Nếu có nhân viên đang có session, lấy người đầu tiên
        if (!employeeIds.isEmpty()) {
            Optional<NhanVien> employee = nhanVienRepository.findById(employeeIds.get(0));
            if (employee.isPresent()) {
                return employee.get();
            }
        }
        
        // Nếu không có session nào, lấy nhân viên đầu tiên có trạng thái active
        return nhanVienRepository.findAll().stream()
                .filter(nv -> Boolean.TRUE.equals(nv.getTrangThai()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public ChatSession transferToEmployee(Conversation conversation, NhanVien newEmployee) {
        // Kết thúc session hiện tại
        endCurrentSession(conversation);
        
        // Tạo session mới với nhân viên mới
        return createSession(conversation, newEmployee);
    }
} 