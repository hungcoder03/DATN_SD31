package com.main.datn_sd31.controller.admin_controller.thuocTinh;


import com.main.datn_sd31.entity.ThuongHieu;
import com.main.datn_sd31.repository.thuocTinh.thuongHieuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/thuong-hieu")
public class thuongHieuController {
    @Autowired
    thuongHieuRepository thuongHieuRepository;

    public thuongHieuController(thuongHieuRepository ThuongHieuRepository) {
        this.thuongHieuRepository = ThuongHieuRepository;
    }

    // Hiển thị trang với form rỗng + danh sách
    @GetMapping
    public String hienThi(Model model) {
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("thuongHieu", new ThuongHieu()); // Form rỗng để thêm mới
        return "admin/pages/thuocTinh/thuongHieu";
    }

    // Nhấn sửa → đổ dữ liệu vào form
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        ThuongHieu thuongHieu = thuongHieuRepository.findById(id).orElse(null);
        model.addAttribute("thuongHieus", thuongHieuRepository.findAll());
        model.addAttribute("thuongHieu", thuongHieu); // Truyền object để binding lại form
        return "admin/pages/thuocTinh/thuongHieu";
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
