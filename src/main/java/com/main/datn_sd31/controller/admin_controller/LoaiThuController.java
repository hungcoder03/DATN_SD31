package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.LoaiThu;
import com.main.datn_sd31.repository.Loaithurepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/loai-thu")
@RequiredArgsConstructor
public class LoaiThuController {

    private final Loaithurepository loaiThuRepository;

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("loaiThus", loaiThuRepository.findAll());
        model.addAttribute("loaiThu", new LoaiThu()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/loai-thu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        LoaiThu loaiThu = loaiThuRepository.findById(id).orElse(null);
        model.addAttribute("loaiThus", loaiThuRepository.findAll());
        model.addAttribute("loaiThu", loaiThu); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/loai-thu";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute LoaiThu loaiThu) {
        if (loaiThu.getId() == null) {
            // THÊM MỚI
            loaiThu.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            LoaiThu existing = loaiThuRepository.findById(loaiThu.getId()).orElse(null);
            if (existing != null) {
                loaiThu.setNgayTao(existing.getNgayTao());
            }
        }

        // Ngày sửa luôn được cập nhật
        loaiThu.setNgaySua(LocalDateTime.now());
        loaiThuRepository.save(loaiThu);
        return "redirect:/admin/loai-thu"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        loaiThuRepository.deleteById(id);
        return "redirect:/admin/loai-thu";
    }
}