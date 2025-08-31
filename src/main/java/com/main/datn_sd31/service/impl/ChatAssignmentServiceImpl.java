package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.ConversationRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.ChatAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatAssignmentServiceImpl implements ChatAssignmentService {

    private final NhanVienRepository nhanVienRepository;
    private final ConversationRepository conversationRepository;

    @Override
    public NhanVien findLeastLoadedAvailableEmployee() {
        // Lấy tất cả nhân viên (không filter theo trạng thái)
        List<NhanVien> allEmployees = nhanVienRepository.findAll();
        
        System.out.println("=== CHAT ASSIGNMENT DEBUG ===");
        System.out.println("Total employees found: " + allEmployees.size());
        
        // Log thông tin từng nhân viên
        allEmployees.forEach(nv -> {
            System.out.println("Employee ID: " + nv.getId() + 
                             ", Name: " + nv.getTen() + 
                             ", Status: " + nv.getTrangThai() + 
                             ", Avatar: " + nv.getAnh());
        });
        
        // Tạm thời bỏ filter trạng thái để test
        List<NhanVien> availableEmployees = allEmployees;
        
        if (availableEmployees.isEmpty()) {
            System.out.println("No employees available");
            return null;
        }
        
        // Tìm nhân viên có ít conversation nhất
        NhanVien selected = availableEmployees.stream()
                .min(Comparator.comparingLong(nv -> conversationRepository.countByAssignedEmployeeAndActiveTrue(nv)))
                .orElse(null);
        
        System.out.println("Selected employee: " + (selected != null ? selected.getTen() : "NULL"));
        System.out.println("=============================");
        
        return selected;
    }
} 