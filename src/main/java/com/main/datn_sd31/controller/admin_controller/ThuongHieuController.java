package com.main.datn_sd31.controller.admin_controller;


import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.repository.Thuonghieurepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/thuong-hieu")
@RequiredArgsConstructor
public class ThuongHieuController {

    private final Thuonghieurepository thuongHieuRepository;


    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("thuongHieu", new ThuongHieu()); // Form rỗng để thêm mới
        return "admin/pages/thuoc-tinh/thuong-hieu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("thuongHieu", thuongHieu); // Truyền object để binding lại form
        return "admin/pages/thuoc-tinh/thuong-hieu";
    }

    // Lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String save(@ModelAttribute ThuongHieu thuongHieu) {
        if (thuongHieu.getId() == null) {
            // THÊM MỚI
            thuongHieu.setNgayTao(LocalDateTime.now());
        } else {
            // SỬA: Lấy bản gốc để giữ nguyên ngày tạo
            ThuongHieu existing = thuongHieuRepository.findById(thuongHieu.getId()).orElse(null);
            if (existing != null) {
                thuongHieu.setNgayTao(existing.getNgayTao());
            }
        }

        // Ngày sửa luôn được cập nhật
        thuongHieu.setNgaySua(LocalDateTime.now());
        thuongHieuRepository.save(thuongHieu);
        return "redirect:/admin/thuong-hieu"; // Trở lại trang chính
    }

    // Xóa
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        thuongHieuRepository.deleteById(id);
        return "redirect:/admin/thuong-hieu";
    }
}