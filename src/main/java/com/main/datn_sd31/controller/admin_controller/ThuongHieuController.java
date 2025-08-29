package com.main.datn_sd31.controller.admin_controller;


import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.repository.Thuonghieurepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
            // === THÊM MỚI ===
            thuongHieu.setNgayTao(LocalDateTime.now());

            // Lấy thương hiệu có mã lớn nhất
            ThuongHieu last = thuongHieuRepository.findTopByOrderByMaDesc();
            int nextNumber = 1;

            if (last != null && last.getMa() != null && last.getMa().startsWith("TH")) {
                try {
                    // Cắt phần số sau "TH"
                    nextNumber = Integer.parseInt(last.getMa().substring(2)) + 1;
                } catch (NumberFormatException e) {
                    nextNumber = 1;
                }
            }
            // Format mã theo dạng TH001, TH002...
            thuongHieu.setMa(String.format("TH%03d", nextNumber));

        } else {
            // === SỬA ===
            ThuongHieu existing = thuongHieuRepository.findById(thuongHieu.getId()).orElse(null);
            if (existing != null) {
                thuongHieu.setNgayTao(existing.getNgayTao());
                thuongHieu.setMa(existing.getMa()); // Giữ nguyên mã khi sửa
            }
        }
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