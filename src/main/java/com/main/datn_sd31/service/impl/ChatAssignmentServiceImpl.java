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
        List<NhanVien> activeEmployees = nhanVienRepository.findAll()
                .stream()
                .filter(nv -> Boolean.TRUE.equals(nv.getTrangThai()))
                .toList();
        return activeEmployees.stream()
                .min(Comparator.comparingLong(nv -> conversationRepository.countByAssignedEmployeeAndActiveTrue(nv)))
                .orElse(null);
    }
} 