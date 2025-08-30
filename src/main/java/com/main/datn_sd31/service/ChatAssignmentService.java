package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.NhanVien;
 
public interface ChatAssignmentService {
    NhanVien findLeastLoadedAvailableEmployee();
} 