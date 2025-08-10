package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.NhanVien;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface NhanVienService {
    
    List<NhanVien> findAll();
    
    Optional<NhanVien> findById(Integer id);
    
    NhanVien save(NhanVien nhanVien);
    
    NhanVien saveWithImage(NhanVien nhanVien, MultipartFile imageFile) throws IOException;
    
    void deleteById(Integer id);
    
    List<NhanVien> search(String keyword);
    
    String generateMaNhanVien();
    
    boolean existsByMa(String ma);
    
    boolean existsByEmail(String email);
    
    boolean existsBySoDienThoai(String soDienThoai);
    
    boolean existsByChungMinhThu(String chungMinhThu);
    
    // For update validation
    boolean existsByMaAndIdNot(String ma, Integer id);
    
    boolean existsByEmailAndIdNot(String email, Integer id);
    
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    
    boolean existsByChungMinhThuAndIdNot(String chungMinhThu, Integer id);
} 