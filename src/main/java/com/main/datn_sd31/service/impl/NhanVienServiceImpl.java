package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public List<NhanVien> findAll() {
        return nhanVienRepository.findAll();
    }

    @Override
    public Optional<NhanVien> findById(Integer id) {
        return nhanVienRepository.findById(id);
    }

    @Override
    public NhanVien save(NhanVien nhanVien) {
        // Mã hóa mật khẩu nếu có
        if (nhanVien.getMatKhau() != null && !nhanVien.getMatKhau().isEmpty()) {
            nhanVien.setMatKhau(passwordEncoder.encode(nhanVien.getMatKhau()));
        }
        
        // Set ngày tạo nếu là nhân viên mới
        if (nhanVien.getId() == null) {
            nhanVien.setNgayThamGia(LocalDate.now());
            nhanVien.setNgayTao(LocalDateTime.now());
        } else {
            nhanVien.setNgaySua(LocalDateTime.now());
        }
        
        return nhanVienRepository.save(nhanVien);
    }

    @Override
    public NhanVien saveWithImage(NhanVien nhanVien, MultipartFile imageFile) throws IOException {
        // Xử lý upload ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = saveUploadedFile(imageFile);
            nhanVien.setAnh(fileName);
        } else if (nhanVien.getAnh() == null || nhanVien.getAnh().isEmpty()) {
            nhanVien.setAnh("default-avatar.png");
        }
        
        return save(nhanVien);
    }

    @Override
    public void deleteById(Integer id) {
        nhanVienRepository.deleteById(id);
    }

    @Override
    public List<NhanVien> search(String keyword) {
        return nhanVienRepository.search(keyword);
    }

    @Override
    public String generateMaNhanVien() {
        String prefix = "NV";
        for (int i = 1; i <= 9999; i++) {
            String ma = prefix + String.format("%03d", i);
            if (!nhanVienRepository.existsByMa(ma)) {
                return ma;
            }
        }
        return prefix + "999"; // Fallback
    }

    @Override
    public boolean existsByMa(String ma) {
        return nhanVienRepository.existsByMa(ma);
    }

    @Override
    public boolean existsByEmail(String email) {
        return nhanVienRepository.existsByEmail(email);
    }

    @Override
    public boolean existsBySoDienThoai(String soDienThoai) {
        return nhanVienRepository.existsBySoDienThoai(soDienThoai);
    }

    @Override
    public boolean existsByChungMinhThu(String chungMinhThu) {
        return nhanVienRepository.existsByChungMinhThu(chungMinhThu);
    }

    @Override
    public boolean existsByMaAndIdNot(String ma, Integer id) {
        return nhanVienRepository.existsByMaAndIdNot(ma, id);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Integer id) {
        return nhanVienRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id) {
        return nhanVienRepository.existsBySoDienThoaiAndIdNot(soDienThoai, id);
    }

    @Override
    public boolean existsByChungMinhThuAndIdNot(String chungMinhThu, Integer id) {
        return nhanVienRepository.existsByChungMinhThuAndIdNot(chungMinhThu, id);
    }

    // Private helper method for file upload
    private String saveUploadedFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "default-avatar.png";
        }

        // Tạo tên file unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

        // Tạo thư mục nếu chưa tồn tại
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Lưu file
        Path filePath = uploadPath.resolve(fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileName;
    }
} 